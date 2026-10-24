// Package ratelimit remembers when a privileged comment trigger last fired
// for each author — across runs. GitHub Actions runs are stateless, so the
// timestamps live in a small JSON file in the repository (the per-feature
// *-state-path inputs), written through the Contents API exactly like
// internal/moderation's offense counter.
//
// The point of this package is a very specific threat: a collaborator
// account that gets compromised (or is simply careless) must not be able to
// fire the agent / workflow-dispatch / discussions / commit-review
// features in a loop. Random users can never trigger any of them at all —
// that bar lives in internal/triage's live permission check — so this only
// ever constrains already-authorized accounts.
package ratelimit

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"time"

	"aitriage/internal/ghclient"
)

// state is the root of the persisted JSON file: author -> trigger times
// still inside some window.
type state struct {
	Triggers map[string][]time.Time `json:"triggers"`
}

// maxAttempts bounds the read-increment-write retry loop. The state file has
// no external locking, and GitHub's Contents API surfaces a concurrent write
// as a stale-SHA 409 — a couple of retries absorbs the common case.
const maxAttempts = 3

// Allow records one trigger by author if the author is still under threshold
// for window, and reports whether the trigger may proceed.
//
// The CURRENT attempt counts, matching internal/antispam: `threshold`
// triggers are allowed per window and the next one is refused.
//
// Persistence failure deliberately ALLOWS the trigger and logs loudly. The
// caller has already verified the author is an authorized collaborator, so
// the worst case of failing open is one extra authorized run; failing closed
// instead would let a missing/readonly state file disable the feature
// entirely, which is a worse and much more likely failure.
func Allow(ctx context.Context, gh *ghclient.Client, owner, repo, path, author string, window time.Duration, threshold int) bool {
	st, sha, err := load(ctx, gh, owner, repo, path)
	if err != nil {
		log.Printf("ratelimit: cannot read %s — allowing this trigger (rate limiting temporarily off) and continuing: %v", path, err)
		return true
	}

	now := time.Now().UTC()
	recent := prune(st.Triggers[author], now.Add(-window))
	if len(recent) >= threshold {
		log.Printf("ratelimit: @%s already triggered this feature %d time(s) within %s (threshold %d) — refusing", author, len(recent), window, threshold)
		return false
	}

	st.Triggers[author] = append(recent, now)
	if err := save(ctx, gh, owner, repo, path, st, sha, author); err != nil {
		log.Printf("ratelimit: cannot persist %s — allowing this trigger, but the next run will not remember it: %v", path, err)
		return true
	}
	return true
}

// prune drops timestamps older than cutoff and returns the rest.
func prune(times []time.Time, cutoff time.Time) []time.Time {
	var out []time.Time
	for _, t := range times {
		if t.After(cutoff) {
			out = append(out, t)
		}
	}
	return out
}

func load(ctx context.Context, gh *ghclient.Client, owner, repo, path string) (*state, string, error) {
	content, sha, found, err := gh.GetFileContentWithSHA(ctx, owner, repo, path, "")
	if err != nil {
		return nil, "", err
	}
	if !found {
		return &state{Triggers: map[string][]time.Time{}}, "", nil
	}
	var st state
	if err := json.Unmarshal([]byte(content), &st); err != nil {
		return nil, "", fmt.Errorf("parsing %s: %w", path, err)
	}
	if st.Triggers == nil {
		st.Triggers = map[string][]time.Time{}
	}
	return &st, sha, nil
}

func save(ctx context.Context, gh *ghclient.Client, owner, repo, path string, st *state, sha, author string) error {
	data, err := json.MarshalIndent(st, "", "  ")
	if err != nil {
		return fmt.Errorf("encoding rate-limit state: %w", err)
	}
	msg := fmt.Sprintf("ai-triage-bot: record a privileged trigger by @%s", author)
	return gh.PutFileContent(ctx, owner, repo, path, msg, data, sha)
}
