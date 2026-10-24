package ghclient

import (
	"context"
	"fmt"
	"net/http"
)

// Issue holds only the fields the bot actually needs.
type Issue struct {
	Number int    `json:"number"`
	NodeID string `json:"node_id"` // needed by DeleteIssue (GraphQL uses node IDs, not numbers)
	Title  string `json:"title"`
	Body   string `json:"body"`
	State  string `json:"state"`
	User   struct {
		Login string `json:"login"`
	} `json:"user"`
	// PullRequest is non-nil when this "issue" is actually a pull request —
	// GitHub's REST API serves PRs from /issues/{number} too. Only used to
	// label a cross-reference as "Issue" vs "PR" in the prompt; the bot
	// still doesn't fetch diffs for referenced PRs, just title/body/state.
	PullRequest *struct{} `json:"pull_request,omitempty"`
}

// Comment is an issue/PR comment (GitHub uses one shared endpoint for both).
type Comment struct {
	ID   int64  `json:"id"`
	Body string `json:"body"`
	User struct {
		Login string `json:"login"`
	} `json:"user"`
}

// GetIssue returns the current issue state — the webhook payload alone
// isn't trusted since it can be a second or two stale.
func (c *Client) GetIssue(ctx context.Context, owner, repo string, number int) (*Issue, error) {
	var issue Issue
	err := c.do(ctx, http.MethodGet, fmt.Sprintf("/repos/%s/%s/issues/%d", owner, repo, number), nil, &issue)
	if err != nil {
		return nil, err
	}
	return &issue, nil
}

// CloseIssue closes an issue with the given reason ("not_planned", "completed").
func (c *Client) CloseIssue(ctx context.Context, owner, repo string, number int, stateReason string) error {
	body := map[string]string{"state": "closed", "state_reason": stateReason}
	return c.do(ctx, http.MethodPatch, fmt.Sprintf("/repos/%s/%s/issues/%d", owner, repo, number), body, nil)
}

// ListComments returns an issue/PR's comments (used for the idempotency check).
func (c *Client) ListComments(ctx context.Context, owner, repo string, number int) ([]Comment, error) {
	var comments []Comment
	err := c.do(ctx, http.MethodGet, fmt.Sprintf("/repos/%s/%s/issues/%d/comments?per_page=100", owner, repo, number), nil, &comments)
	if err != nil {
		return nil, err
	}
	return comments, nil
}

// CreateComment posts a comment on an issue/PR.
func (c *Client) CreateComment(ctx context.Context, owner, repo string, number int, body string) error {
	return c.do(ctx, http.MethodPost, fmt.Sprintf("/repos/%s/%s/issues/%d/comments", owner, repo, number), map[string]string{"body": body}, nil)
}

// AddLabels adds labels to an issue/PR (GitHub uses the same /issues/{n}/labels endpoint for both).
func (c *Client) AddLabels(ctx context.Context, owner, repo string, number int, labels []string) error {
	return c.do(ctx, http.MethodPost, fmt.Sprintf("/repos/%s/%s/issues/%d/labels", owner, repo, number), map[string][]string{"labels": labels}, nil)
}

// DeleteIssue permanently deletes an issue via the GraphQL API — the REST
// API has no delete endpoint for issues, full stop. This requires the
// CALLER'S TOKEN to have ADMIN permission on the repository. GitHub's
// default Actions GITHUB_TOKEN never qualifies for this, no matter what
// the workflow's `permissions:` block says (that block only affects REST
// scopes) — the call fails with a GraphQL-level "Viewer not authorized to
// delete" error. Only a personal access token belonging to an account with
// admin/owner rights on the repo works (a classic PAT with the `repo`
// scope from such an account, or a fine-grained PAT with "Administration:
// write" + "Issues: write"). Treat failure here as expected/recoverable,
// not fatal — see internal/triage/moderation.go for the redact+close+lock
// fallback used when this isn't available.
func (c *Client) DeleteIssue(ctx context.Context, nodeID string) error {
	const mutation = `mutation($id: ID!) { deleteIssue(input: {issueId: $id}) { clientMutationId } }`
	return c.doGraphQL(ctx, mutation, map[string]any{"id": nodeID}, nil)
}

// UpdateIssueContent overwrites an issue's title/body. Used by the
// moderation flow to redact doxxed/abusive content in place when real
// deletion isn't available (see DeleteIssue).
func (c *Client) UpdateIssueContent(ctx context.Context, owner, repo string, number int, title, body string) error {
	return c.do(ctx, http.MethodPatch, fmt.Sprintf("/repos/%s/%s/issues/%d", owner, repo, number),
		map[string]string{"title": title, "body": body}, nil)
}

// LockIssue locks an issue/PR's conversation (GitHub uses one endpoint for
// both) so only collaborators can comment further. An empty reason omits
// the optional `lock_reason` field rather than guessing one of GitHub's
// fixed enum values.
func (c *Client) LockIssue(ctx context.Context, owner, repo string, number int, reason string) error {
	var body any
	if reason != "" {
		body = map[string]string{"lock_reason": reason}
	}
	return c.do(ctx, http.MethodPut, fmt.Sprintf("/repos/%s/%s/issues/%d/lock", owner, repo, number), body, nil)
}
