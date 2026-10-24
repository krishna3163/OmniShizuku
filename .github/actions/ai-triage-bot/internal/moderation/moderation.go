// Package moderation tracks per-author moderation offenses (doxxing/severe
// abuse) ACROSS runs. GitHub Actions runs are otherwise stateless, and
// deleted content can't be searched afterward to reconstruct history — so
// this small JSON file, committed to the repo via the Contents API (see
// internal/ghclient.PutFileContent), is the only durable record of "has
// this author been warned before". Without it, every violation would look
// like a first offense forever, and the "1st time: remove, 2nd time: also
// block" escalation the bot implements would never actually trigger.
package moderation

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"strings"
	"time"

	"aitriage/internal/ghclient"
)

// Offense is one author's moderation history.
type Offense struct {
	Count      int       `json:"count"`
	LastKind   string    `json:"last_kind"`
	LastReason string    `json:"last_reason"`
	LastNumber int       `json:"last_number"`
	LastAt     time.Time `json:"last_at"`
}

// state is the root of the persisted JSON file.
type state struct {
	Offenses map[string]*Offense `json:"offenses"`
}

// maxAttempts bounds the read-increment-write retry loop below. The state
// file has no external locking — two runs (e.g. the same troll opening two
// issues seconds apart) can race on it, and GitHub's Contents API detects
// that via a stale-SHA 409. A few retries absorb the common case; if it
// still fails, the caller degrades to treating the offense as a first-time
// one (see the doc comment on Record).
const maxAttempts = 3

// Record loads the current state, increments author's offense count, and
// saves the update back — returning the NEW count (1 = first offense,
// 2+ = repeat offender). If the state file doesn't exist yet, it's created.
//
// A non-nil error here means the count could NOT be persisted (e.g. the
// repeated 409 conflicts above, or the token lacks contents:write). The
// caller should treat that as "assume first offense" rather than failing
// the whole run: under-escalating (skip a deserved block) is the safer
// failure mode than over-escalating (block someone based on a count we
// couldn't actually confirm) — and either way the abusive content itself
// still gets removed by the caller regardless of this function's outcome.
func Record(ctx context.Context, gh *ghclient.Client, owner, repo, path, author, kind, reason string, number int) (int, error) {
	var lastErr error
	for attempt := 1; attempt <= maxAttempts; attempt++ {
		st, sha, err := load(ctx, gh, owner, repo, path)
		if err != nil {
			return 0, fmt.Errorf("loading moderation state: %w", err)
		}

		off, ok := st.Offenses[author]
		if !ok {
			off = &Offense{}
			st.Offenses[author] = off
		}
		off.Count++
		off.LastKind = kind
		off.LastReason = reason
		off.LastNumber = number
		off.LastAt = time.Now().UTC()
		newCount := off.Count

		if err := save(ctx, gh, owner, repo, path, st, sha, author, newCount); err != nil {
			lastErr = err
			if strings.Contains(err.Error(), "HTTP 409") {
				log.Printf("moderation: state file write conflict (attempt %d/%d) — another run updated it concurrently, re-reading and retrying: %v", attempt, maxAttempts, err)
				continue
			}
			return 0, fmt.Errorf("saving moderation state: %w", err)
		}
		return newCount, nil
	}
	return 0, fmt.Errorf("saving moderation state after %d attempt(s) (concurrent writes kept conflicting): %w", maxAttempts, lastErr)
}

func load(ctx context.Context, gh *ghclient.Client, owner, repo, path string) (*state, string, error) {
	content, sha, found, err := gh.GetFileContentWithSHA(ctx, owner, repo, path, "")
	if err != nil {
		return nil, "", err
	}
	if !found {
		return &state{Offenses: map[string]*Offense{}}, "", nil
	}
	var st state
	if err := json.Unmarshal([]byte(content), &st); err != nil {
		return nil, "", fmt.Errorf("parsing %s: %w", path, err)
	}
	if st.Offenses == nil {
		st.Offenses = map[string]*Offense{}
	}
	return &st, sha, nil
}

func save(ctx context.Context, gh *ghclient.Client, owner, repo, path string, st *state, sha, author string, newCount int) error {
	data, err := json.MarshalIndent(st, "", "  ")
	if err != nil {
		return fmt.Errorf("encoding moderation state: %w", err)
	}
	msg := fmt.Sprintf("ai-triage-bot: record moderation offense #%d for @%s", newCount, author)
	return gh.PutFileContent(ctx, owner, repo, path, msg, data, sha)
}
