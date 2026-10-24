package ghclient

import (
	"context"
	"fmt"
	"net/http"
)

// PullRequest holds only the fields the bot needs.
type PullRequest struct {
	Number int    `json:"number"`
	Title  string `json:"title"`
	Body   string `json:"body"`
	State  string `json:"state"`
	User   struct {
		Login string `json:"login"`
	} `json:"user"`
	Base struct {
		Ref string `json:"ref"`
		SHA string `json:"sha"`
	} `json:"base"`
	Head struct {
		Ref string `json:"ref"`
		SHA string `json:"sha"`
		// Repo is the head's repository. Nil-safe on purpose: it is missing
		// from some payloads, and its absence must not be mistaken for
		// "same repo". Fork detection (workflow-dispatch's "head" ref can
		// only target a branch INSIDE this repository) compares FullName
		// against the base repo, not just Fork.
		Repo *struct {
			FullName string `json:"full_name"`
			Fork     bool   `json:"fork"`
		} `json:"repo"`
	} `json:"head"`
}

// PullFile is one changed file from /pulls/{n}/files.
type PullFile struct {
	Filename         string `json:"filename"`
	PreviousFilename string `json:"previous_filename"` // set for renames/copies — where the file came from
	Status           string `json:"status"`            // added, modified, removed, renamed, copied, changed
	Additions        int    `json:"additions"`
	Deletions        int    `json:"deletions"`
	Changes          int    `json:"changes"`
}

// GetPullRequest returns the current PR metadata.
func (c *Client) GetPullRequest(ctx context.Context, owner, repo string, number int) (*PullRequest, error) {
	var pr PullRequest
	err := c.do(ctx, http.MethodGet, fmt.Sprintf("/repos/%s/%s/pulls/%d", owner, repo, number), nil, &pr)
	if err != nil {
		return nil, err
	}
	return &pr, nil
}

// GetPullDiff returns the whole PR's unified diff in one request (cheaper
// than downloading each file's patch separately).
func (c *Client) GetPullDiff(ctx context.Context, owner, repo string, number int) (string, error) {
	return c.fetchRaw(ctx, http.MethodGet, fmt.Sprintf("/repos/%s/%s/pulls/%d", owner, repo, number), "application/vnd.github.v3.diff")
}

// ListPullFiles returns the list of changed files (no patches — we already
// have the full diff).
func (c *Client) ListPullFiles(ctx context.Context, owner, repo string, number int) ([]PullFile, error) {
	var files []PullFile
	err := c.do(ctx, http.MethodGet, fmt.Sprintf("/repos/%s/%s/pulls/%d/files?per_page=100", owner, repo, number), nil, &files)
	if err != nil {
		return nil, err
	}
	return files, nil
}

// UpdatePullRequestContent overwrites a PR's title/body. GitHub has NO API
// (REST or GraphQL) and no UI action to delete a pull request — this is a
// hard platform limitation, not a permissions issue (GitHub Support can do
// it manually, only for legal/security reasons). Redacting the title/body
// via this method, then closing (ClosePullRequest) and locking
// (Client.LockIssue, shared with issues), is the closest available
// equivalent to "removing" a PR — see internal/triage/moderation.go.
func (c *Client) UpdatePullRequestContent(ctx context.Context, owner, repo string, number int, title, body string) error {
	return c.do(ctx, http.MethodPatch, fmt.Sprintf("/repos/%s/%s/pulls/%d", owner, repo, number),
		map[string]string{"title": title, "body": body}, nil)
}

// ClosePullRequest closes a PR without merging it.
func (c *Client) ClosePullRequest(ctx context.Context, owner, repo string, number int) error {
	return c.do(ctx, http.MethodPatch, fmt.Sprintf("/repos/%s/%s/pulls/%d", owner, repo, number),
		map[string]string{"state": "closed"}, nil)
}
