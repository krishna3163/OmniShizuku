package ghclient

import (
	"context"
	"fmt"
	"net/http"
)

// CommitIdentity is the author/committer name+email from a git commit
// object — not the same as GitHubUser below: git metadata is written by
// whatever local git client/CI produced the commit and can say anything,
// unlike GitHubLogin, which GitHub resolves itself from the email/token
// when it can.
type CommitIdentity struct {
	Name  string `json:"name"`
	Email string `json:"email"`
}

// GitHubUser is the part GitHub resolves itself (can be null if the
// commit's email isn't linked to any account).
type GitHubUser struct {
	Login string `json:"login"`
	Type  string `json:"type"`
}

// PullCommit is one commit from /pulls/{n}/commits — we only care about the
// author/committer git metadata for the anti-fraud heuristic (Laplace factor).
type PullCommit struct {
	SHA    string `json:"sha"`
	Commit struct {
		Author    CommitIdentity `json:"author"`
		Committer CommitIdentity `json:"committer"`
	} `json:"commit"`
	Author    *GitHubUser `json:"author"`    // resolved GitHub account of the commit author (may be nil)
	Committer *GitHubUser `json:"committer"` // resolved GitHub account of the committer (may be nil)
}

// ListPullCommits returns the git metadata of a PR's commits (up to 250 —
// enough for the heuristic; a PR with more commits than that will already
// get "needs-work" for other reasons before any anti-fraud check matters).
func (c *Client) ListPullCommits(ctx context.Context, owner, repo string, number int) ([]PullCommit, error) {
	var commits []PullCommit
	err := c.do(ctx, http.MethodGet, fmt.Sprintf("/repos/%s/%s/pulls/%d/commits?per_page=250", owner, repo, number), nil, &commits)
	if err != nil {
		return nil, err
	}
	return commits, nil
}

// Commit is the part of /repos/{owner}/{repo}/commits/{sha} the bot needs:
// the real commit message, which the raw diff endpoint does not include.
type Commit struct {
	SHA    string `json:"sha"`
	Commit struct {
		Message string         `json:"message"`
		Author  CommitIdentity `json:"author"`
	} `json:"commit"`
	Author *GitHubUser `json:"author"`
}

// GetCommit returns a single commit's metadata (message + identities).
func (c *Client) GetCommit(ctx context.Context, owner, repo, sha string) (*Commit, error) {
	var cm Commit
	err := c.do(ctx, http.MethodGet, fmt.Sprintf("/repos/%s/%s/commits/%s", owner, repo, sha), nil, &cm)
	if err != nil {
		return nil, err
	}
	return &cm, nil
}

// GetCommitDiff returns one commit's unified diff in a single request —
// the commit-review feature's main input.
func (c *Client) GetCommitDiff(ctx context.Context, owner, repo, sha string) (string, error) {
	return c.fetchRaw(ctx, http.MethodGet, fmt.Sprintf("/repos/%s/%s/commits/%s", owner, repo, sha),
		"application/vnd.github.v3.diff")
}

// ListCommitComments returns the comments attached to a COMMIT. Note this
// is a different endpoint (and a different kind of object) from issue/PR
// comments — commit comments do NOT show up under /issues/{n}/comments, so
// the idempotency check for commit review has to use this.
func (c *Client) ListCommitComments(ctx context.Context, owner, repo, sha string) ([]Comment, error) {
	var comments []Comment
	err := c.do(ctx, http.MethodGet, fmt.Sprintf("/repos/%s/%s/commits/%s/comments?per_page=100", owner, repo, sha), nil, &comments)
	if err != nil {
		return nil, err
	}
	return comments, nil
}

// CreateCommitComment posts a comment directly on a commit (again: not the
// issues endpoint — a commit comment is its own object).
func (c *Client) CreateCommitComment(ctx context.Context, owner, repo, sha, body string) error {
	return c.do(ctx, http.MethodPost, fmt.Sprintf("/repos/%s/%s/commits/%s/comments", owner, repo, sha),
		map[string]string{"body": body}, nil)
}
