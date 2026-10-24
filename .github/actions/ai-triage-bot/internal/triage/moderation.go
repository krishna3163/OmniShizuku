package triage

// Handling for the AI's moderation flag (see internal/model.ModerationFlag):
// when the model reports that an issue/PR's content ITSELF is a doxxing or
// severe-abuse violation, this replaces the normal label+comment flow with
// content removal and, on a repeat offense by the same author, an account
// block.
//
// GitHub API reality check — this determines what actually happens, not
// just what gets attempted, so it's worth being explicit about it in code,
// not just docs:
//
//   - Issues CAN be deleted, but only via the GraphQL `deleteIssue`
//     mutation, and ONLY with a token that has ADMIN permission on the
//     repo. The default Actions GITHUB_TOKEN never qualifies for this, no
//     matter what the workflow's `permissions:` block says. Real deletion
//     needs cfg.ModerationToken to be a PAT from an admin/owner account.
//   - Pull requests can NEVER be deleted through any GitHub API, or even
//     the web UI — this is a hard platform limitation, not a permissions
//     problem (GitHub Support can do it manually, only for legal/security
//     reasons). "Removing" a PR here means redacting its title/body,
//     closing it, and locking the conversation — the closest available
//     equivalent, not a true delete.
//   - Blocking a user requires a token with the "Blocking users"
//     permission — an org-level `admin:org`-scoped token for an org-owned
//     repo, or the personal block-list permission for a personal-account
//     repo. GITHUB_TOKEN cannot do this either.
//
// Without cfg.ModerationToken configured, the bot still reacts (redact +
// close + lock — anything a normal collaborator-permissioned token can do)
// but skips real deletion and blocking, and says so plainly in the run
// logs rather than silently pretending it happened.

import (
	"context"
	"fmt"
	"log"
	"strings"

	"aitriage/internal/config"
	"aitriage/internal/ghclient"
	"aitriage/internal/model"
	"aitriage/internal/moderation"
)

// redactedTitle/redactedBody replace a violating issue/PR's own content
// when it's quarantined instead of truly deleted, so nobody has to read
// the removed personal data / abuse to understand what happened.
const (
	redactedTitle = "[removed by AI moderation]"
	redactedBody  = "*(Content removed automatically: it contained personal data about an individual and/or severe targeted abuse. See the repository's moderation log, if configured, for details.)*"
)

// handleModerationViolation runs instead of the normal label/comment path.
// It returns an error only for something that should actually fail the
// Action run (e.g. the item couldn't even be closed); a "handled, but
// degraded because no elevated token was configured" outcome is NOT an
// error — a safe partial removal beats crashing the run and leaving the
// violating content untouched.
func handleModerationViolation(
	ctx context.Context,
	cfg *config.Config,
	gh, modGH *ghclient.Client,
	owner, repo string,
	number int,
	author, title, nodeID string,
	isPR bool,
	flag *model.ModerationFlag,
) error {
	kind := flag.Kind
	if kind == "" {
		kind = "policy violation"
	}
	log.Printf("%s #%d: MODERATION violation flagged (kind=%s) — author=@%s reason=%q", itemWord(isPR), number, kind, author, flag.Reason)

	// 1. First offense or repeat? A persistence failure degrades to
	// "treat as first offense" (remove, don't block) — see the doc
	// comment on moderation.Record for why that's the safer failure mode.
	count, err := moderation.Record(ctx, gh, owner, repo, cfg.ModerationStatePath, author, kind, flag.Reason, number)
	if err != nil {
		log.Printf("%s #%d: moderation offense count could not be persisted, treating this as a first offense: %v", itemWord(isPR), number, err)
		count = 1
	}

	// 2. Remove the content. Real deletion is only ever attempted for
	// issues, and only with a moderation token — see the package doc
	// comment above for why PRs and GITHUB_TOKEN are excluded up front.
	deleted := false
	switch {
	case isPR:
		// No PR-delete API exists for anyone, at any permission level —
		// don't even try. Falls through to quarantine below.
	case modGH == nil:
		log.Printf("issue #%d: no moderation-token configured, cannot really delete — falling back to quarantine (redact+close+lock)", number)
	case nodeID == "":
		log.Printf("issue #%d: missing node_id, cannot attempt a GraphQL delete — falling back to quarantine", number)
	default:
		if derr := modGH.DeleteIssue(ctx, nodeID); derr == nil {
			deleted = true
			log.Printf("issue #%d: deleted via GraphQL using the moderation token", number)
		} else {
			log.Printf("issue #%d: GraphQL delete failed (the moderation token most likely lacks ADMIN rights on this repo — see DeleteIssue's doc comment), falling back to quarantine: %v", number, derr)
		}
	}

	if !deleted {
		if err := quarantine(ctx, gh, owner, repo, number, isPR); err != nil {
			return fmt.Errorf("quarantining %s #%d after a moderation violation: %w", itemWord(isPR), number, err)
		}
	}

	// 3. Repeat offense (2nd+): also block the author.
	blocked := false
	if count >= 2 {
		if modGH == nil {
			log.Printf("%s #%d: repeat offender @%s (offense #%d) — no moderation-token configured, cannot block automatically; a maintainer should block this account by hand", itemWord(isPR), number, author, count)
		} else if orgErr := modGH.BlockUserInOrg(ctx, owner, author); orgErr == nil {
			blocked = true
			log.Printf("@%s: blocked from the %s organization (offense #%d)", author, owner, count)
		} else if personalErr := modGH.BlockUserPersonal(ctx, author); personalErr == nil {
			blocked = true
			log.Printf("@%s: blocked account-wide via the personal block list (offense #%d) — NOTE this blocks them from every repo the moderation token's account touches, not just %s/%s", author, count, owner, repo)
		} else {
			log.Printf("@%s: both block attempts failed (org: %v; personal: %v) — the moderation token likely lacks the \"Blocking users\" permission; a maintainer should block this account by hand", author, orgErr, personalErr)
		}
	}

	// 4. Audit trail. The removed content is gone or redacted, so this is
	// best-effort logging plus, optionally, a comment on a dedicated
	// "moderation log" issue the repo owner configured.
	if cfg.ModerationLogIssue > 0 {
		note := formatModerationLogEntry(number, title, author, kind, flag.Reason, count, deleted, blocked, isPR)
		if err := gh.CreateComment(ctx, owner, repo, cfg.ModerationLogIssue, note); err != nil {
			log.Printf("moderation log: failed to post the audit comment on #%d: %v", cfg.ModerationLogIssue, err)
		}
	}

	log.Printf("%s #%d processed: MODERATION kind=%s offense=#%d deleted=%v blocked=%v", itemWord(isPR), number, kind, count, deleted, blocked)
	return nil
}

// quarantine is the always-available fallback for "remove this content"
// when real deletion isn't possible (no moderation token, or it's a PR,
// which can NEVER be deleted via any API — see the package doc comment):
// redact the title/body, close it, and lock the conversation. Anyone who
// already has the URL, or a fork/clone/webhook mirror made before this
// ran, can still have a copy — unlike a real delete, this only changes
// what's rendered on GitHub going forward. If the exposed data is
// genuinely sensitive (credentials, etc.), the repo owner should also
// rotate/revoke it and consider a GitHub Support request for a real
// removal.
func quarantine(ctx context.Context, gh *ghclient.Client, owner, repo string, number int, isPR bool) error {
	if isPR {
		if err := gh.UpdatePullRequestContent(ctx, owner, repo, number, redactedTitle, redactedBody); err != nil {
			return fmt.Errorf("redacting PR content: %w", err)
		}
		if err := gh.ClosePullRequest(ctx, owner, repo, number); err != nil {
			return fmt.Errorf("closing PR: %w", err)
		}
	} else {
		if err := gh.UpdateIssueContent(ctx, owner, repo, number, redactedTitle, redactedBody); err != nil {
			return fmt.Errorf("redacting issue content: %w", err)
		}
		if err := gh.CloseIssue(ctx, owner, repo, number, "not_planned"); err != nil {
			return fmt.Errorf("closing issue: %w", err)
		}
	}

	if err := gh.EnsureLabel(ctx, owner, repo, "moderation-removed", "000000", "Removed automatically: personal data (doxxing) and/or severe targeted abuse"); err != nil {
		log.Printf("%s #%d: warning, failed to create the moderation-removed label: %v", itemWord(isPR), number, err)
	}
	if err := gh.AddLabels(ctx, owner, repo, number, []string{"moderation-removed"}); err != nil {
		log.Printf("%s #%d: warning, failed to apply the moderation-removed label: %v", itemWord(isPR), number, err)
	}
	if err := gh.LockIssue(ctx, owner, repo, number, ""); err != nil {
		log.Printf("%s #%d: warning, failed to lock the conversation: %v", itemWord(isPR), number, err)
	}
	return nil
}

func itemWord(isPR bool) string {
	if isPR {
		return "PR"
	}
	return "issue"
}

func formatModerationLogEntry(number int, title, author, kind, reason string, offenseCount int, deleted, blocked, isPR bool) string {
	item := "Issue"
	if isPR {
		item = "PR"
	}
	action := "content redacted, closed, and locked"
	if deleted {
		action = "permanently deleted"
	}
	blockNote := ""
	switch {
	case blocked:
		blockNote = fmt.Sprintf(" @%s was also **blocked** (offense #%d).", author, offenseCount)
	case offenseCount >= 2:
		blockNote = fmt.Sprintf(" This is offense #%d for @%s — a block was warranted but could not be confirmed automatically; check manually.", offenseCount, author)
	}
	return fmt.Sprintf(
		"🚨 **Moderation action** — %s #%d (%q) by @%s\n\nKind: %s\nReason: %s\nAction: %s.%s",
		item, number, orNAModeration(title), author, kind, orNAModeration(reason), action, blockNote,
	)
}

func orNAModeration(s string) string {
	if strings.TrimSpace(s) == "" {
		return "(not given)"
	}
	return s
}
