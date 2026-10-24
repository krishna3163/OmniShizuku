package triage

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"strings"

	"aitriage/internal/ai"
	"aitriage/internal/config"
	"aitriage/internal/ghclient"
	"aitriage/internal/githubevent"
	"aitriage/internal/promptbuilder"
	"aitriage/internal/ratelimit"
	"aitriage/internal/trigger"
)

// commitRating is the model's answer for a single-commit review. It is
// deliberately NOT an AIVerdict: there is no label to apply and nothing to
// close — only a score the repo owner asked for.
type commitRating struct {
	Score   int      `json:"score"`
	Comment string   `json:"comment"`
	Reasons []string `json:"reasons"`
}

// HandleCommitComment implements the owner-only commit-review feature (see
// action.yml's commit-review-enabled): a comment on a COMMIT carrying the
// trigger phrase makes the bot rate that commit's message + diff and reply
// with the score.
//
// A commit comment is a distinct GitHub object: it does NOT show up under
// /issues/{n}/comments, so idempotency uses ListCommitComments instead of
// the issue-comment check every other flow shares.
func HandleCommitComment(ctx context.Context, cfg *config.Config, gh *ghclient.Client, aiClient *ai.Client, event *githubevent.Event) error {
	if !cfg.CommitReviewEnabled {
		return nil
	}
	if event.Action != "created" {
		return nil
	}
	if event.Comment == nil {
		return nil
	}
	sha := event.Comment.CommitID
	if sha == "" {
		return nil
	}
	if _, ok := trigger.Substring(event.Comment.Body, cfg.CommitReviewTrigger); !ok {
		return nil
	}

	owner, repo := cfg.RepoOwner, cfg.RepoName
	author := event.Comment.User.Login

	// Owner-only bar. Refusals produce no comment (same reasoning as
	// workflow dispatch): a reply would make the bot answer anyone.
	allowed, reason := authorize(ctx, gh, owner, repo, author, "admin", cfg.CommitReviewAllowedUsers)
	if !allowed {
		log.Printf("commit review refused for @%s: %s", author, reason)
		return nil
	}

	if !ratelimit.Allow(ctx, gh, owner, repo, cfg.CommitReviewStatePath, author,
		cfg.CommitReviewSpamWindow, cfg.CommitReviewSpamThreshold) {
		log.Printf("commit review refused for @%s: rate limit reached", author)
		return nil
	}

	// Idempotency: the hidden marker on a prior bot comment on THIS commit.
	if done, err := alreadyReviewedCommit(ctx, gh, owner, repo, sha); err != nil {
		return fmt.Errorf("checking prior commit comments: %w", err)
	} else if done {
		log.Printf("commit %s was already reviewed by the bot — exiting", sha)
		return nil
	}

	// System prompt #1 (repo context) + the commit-review rubric (system
	// prompt #6). A missing rubric is a hard error, never a silent
	// degradation to "rate this with no rules".
	p, err := loadPrompts(cfg)
	if err != nil {
		return err
	}
	rubric, err := loadFeaturePrompt(cfg.Workspace, cfg.CommitReviewInstructionsPath,
		"the commit-review rubric (system prompt #6)")
	if err != nil {
		return err
	}
	system := p.repoContext + "\n\n---\n\n" + rubric

	commit, err := gh.GetCommit(ctx, owner, repo, sha)
	if err != nil {
		return fmt.Errorf("fetching commit %s: %w", sha, err)
	}
	diff, err := gh.GetCommitDiff(ctx, owner, repo, sha)
	if err != nil {
		return fmt.Errorf("fetching diff for commit %s: %w", sha, err)
	}
	if len(diff) > cfg.MaxDiffBytes {
		diff = truncateUTF8(diff, cfg.MaxDiffBytes) + "\n... (diff truncated to save tokens) ...\n"
	}

	userPrompt := promptbuilder.BuildCommitReviewPrompt(promptbuilder.CommitReviewInput{
		Sha:     sha,
		Message: commit.Commit.Message,
		Author:  firstNonEmpty(commitAuthorLogin(commit), commit.Commit.Author.Name),
		Diff:    diff,
	})
	raw, err := aiClient.Generate(ctx, system, userPrompt)
	if err != nil {
		return fmt.Errorf("gemini request: %w", err)
	}
	rating, err := parseCommitRating(raw)
	if err != nil {
		return fmt.Errorf("parsing gemini response: %w", err)
	}

	return replyCommitReview(ctx, gh, owner, repo, sha, rating)
}

// alreadyReviewedCommit checks the COMMIT's own comments for the bot's
// marker — a different endpoint from issue/PR comments (see ghclient's
// ListCommitComments).
func alreadyReviewedCommit(ctx context.Context, gh *ghclient.Client, owner, repo, sha string) (bool, error) {
	comments, err := gh.ListCommitComments(ctx, owner, repo, sha)
	if err != nil {
		return false, err
	}
	for _, c := range comments {
		if strings.Contains(c.Body, marker) {
			return true, nil
		}
	}
	return false, nil
}

// parseCommitRating parses the model's rating JSON, tolerating the optional
// ```json wrapper. The score is clamped to 0-100 for the same reason
// parseVerdict clamps PRQuality: a model that returns 150 or -3 must not
// post an absurd number straight into the comment.
func parseCommitRating(raw string) (*commitRating, error) {
	var r commitRating
	if err := json.Unmarshal([]byte(stripJSONFences(raw)), &r); err != nil {
		return nil, fmt.Errorf("failed to parse the model's JSON rating: %w (raw response: %s)", err, truncateForLog(raw))
	}
	if r.Score < 0 {
		r.Score = 0
	}
	if r.Score > 100 {
		r.Score = 100
	}
	if strings.TrimSpace(r.Comment) == "" {
		return nil, fmt.Errorf("model returned an empty comment (raw response: %s)", truncateForLog(raw))
	}
	return &r, nil
}

// replyCommitReview posts the score. The reasons are rendered by this code,
// not the model, so a score is never posted without its supporting points.
func replyCommitReview(ctx context.Context, gh *ghclient.Client, owner, repo, sha string, r *commitRating) error {
	var b strings.Builder
	fmt.Fprintf(&b, "**Commit quality: %d/100** (higher = better)\n\n%s", r.Score, strings.TrimSpace(r.Comment))
	if len(r.Reasons) > 0 {
		b.WriteString("\n")
		for _, reason := range r.Reasons {
			fmt.Fprintf(&b, "\n- %s", reason)
		}
	}
	short := sha
	if len(short) > 7 {
		short = short[:7]
	}
	return gh.CreateCommitComment(ctx, owner, repo, sha,
		formatSpecialComment(fmt.Sprintf("🤖 **AI Triage — commit review `%s`**", short), b.String()))
}

func firstNonEmpty(values ...string) string {
	for _, v := range values {
		if strings.TrimSpace(v) != "" {
			return v
		}
	}
	return ""
}

// commitAuthorLogin returns the GitHub login the commit's author resolved
// to, or "" when GitHub couldn't link the commit's email to an account.
func commitAuthorLogin(c *ghclient.Commit) string {
	if c == nil || c.Author == nil {
		return ""
	}
	return c.Author.Login
}
