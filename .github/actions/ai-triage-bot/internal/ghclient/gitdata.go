package ghclient

import (
	"context"
	"encoding/base64"
	"fmt"
	"net/http"
	"net/url"
	"strings"
)

// This file holds the write-side git/PR operations the AI-agent feature
// needs and nothing else in the bot uses. Everything here is strictly
// scoped: the agent writes to a NEW branch it created, and opens a NEW pull
// request. It never pushes to the default branch and never modifies the
// issue/PR it was pointed at.

// CreateBranch creates a new branch pointing at fromSHA. It is used ONLY to
// give the agent a place to put its generated files; an existing branch name
// is a hard error rather than a silent overwrite, so a rerun can never clobber
// someone else's branch.
func (c *Client) CreateBranch(ctx context.Context, owner, repo, branch, fromSHA string) error {
	body := map[string]any{
		"ref": "refs/heads/" + branch,
		"sha": fromSHA,
	}
	return c.do(ctx, http.MethodPost, fmt.Sprintf("/repos/%s/%s/git/refs", owner, repo), body, nil)
}

// GetDefaultBranchSHA returns the HEAD commit SHA of the default branch —
// the base the agent branches from.
func (c *Client) GetDefaultBranchSHA(ctx context.Context, owner, repo string) (string, error) {
	branch, err := c.GetDefaultBranch(ctx, owner, repo)
	if err != nil || branch == "" {
		branch = "main" // GitHub's default when a repo has no configured name yet
	}
	var r struct {
		Object struct {
			SHA string `json:"sha"`
		} `json:"object"`
	}
	if err := c.do(ctx, http.MethodGet, fmt.Sprintf("/repos/%s/%s/git/ref/heads/%s",
		owner, repo, url.PathEscape(branch)), nil, &r); err != nil {
		return "", err
	}
	if r.Object.SHA == "" {
		return "", fmt.Errorf("%s/%s branch %q has no HEAD commit", owner, repo, branch)
	}
	return r.Object.SHA, nil
}

// PutFileOnBranch writes (creates or overwrites) a file on a specific
// BRANCH. This is deliberately separate from PutFileContent, which always
// writes to the default branch: the agent must never be able to reach the
// default branch by accident, and a separate method with no default-branch
// fallback makes that structurally impossible rather than a matter of
// remembering to pass the right argument.
func (c *Client) PutFileOnBranch(ctx context.Context, owner, repo, branch, path, message, content string) error {
	body := map[string]any{
		"message": message,
		"content": base64.StdEncoding.EncodeToString([]byte(content)),
		"branch":  branch,
	}
	return c.do(ctx, http.MethodPut,
		fmt.Sprintf("/repos/%s/%s/contents/%s", owner, repo, escapePathSegments(path)), body, nil)
}

// DeleteFileOnBranch removes a file on a specific branch. Requires the
// file's current blob SHA (from GetFileContentWithSHA) — GitHub rejects a
// delete without it, and requiring it means the agent can't delete a file it
// never read.
func (c *Client) DeleteFileOnBranch(ctx context.Context, owner, repo, branch, path, sha, message string) error {
	body := map[string]any{
		"message": message,
		"sha":     sha,
		"branch":  branch,
	}
	return c.do(ctx, http.MethodDelete,
		fmt.Sprintf("/repos/%s/%s/contents/%s", owner, repo, escapePathSegments(path)), body, nil)
}

// CreatePullRequest opens a NEW pull request. The agent uses this to propose
// its change for human review — nothing is ever auto-merged, and the base is
// always the default branch.
func (c *Client) CreatePullRequest(ctx context.Context, owner, repo, title, head, base, body string) (*PullRequest, error) {
	var pr PullRequest
	err := c.do(ctx, http.MethodPost, fmt.Sprintf("/repos/%s/%s/pulls", owner, repo),
		map[string]string{
			"title": title,
			"head":  head,
			"base":  base,
			"body":  body,
		}, &pr)
	if err != nil {
		return nil, err
	}
	return &pr, nil
}

// CreateIssue files a NEW issue. The agent's other output mode is filing a
// related issue instead of opening a PR (see internal/triage/agent.go).
func (c *Client) CreateIssue(ctx context.Context, owner, repo, title, body string) (*Issue, error) {
	var issue Issue
	err := c.do(ctx, http.MethodPost, fmt.Sprintf("/repos/%s/%s/issues", owner, repo),
		map[string]string{"title": title, "body": body}, &issue)
	if err != nil {
		return nil, err
	}
	return &issue, nil
}

// TreeEntry is one path in a repository tree — path only, no content. Used
// to give the model a bounded map of the repo when it has to guess which
// files are relevant (see agent-max-tree-entries).
type TreeEntry struct {
	Path string `json:"path"`
	Type string `json:"type"` // "blob" (file) | "tree" (dir)
}

// ListTreePaths returns the repository tree (recursive, so a single request)
// as path/type pairs, capped at max entries. Only paths are returned — no
// file content — which is what makes this cheap enough to show the model.
func (c *Client) ListTreePaths(ctx context.Context, owner, repo, ref string, max int) ([]TreeEntry, error) {
	if max <= 0 {
		return nil, nil
	}
	var r struct {
		Tree []TreeEntry `json:"tree"`
	}
	if err := c.do(ctx, http.MethodGet,
		fmt.Sprintf("/repos/%s/%s/git/trees/%s?recursive=1", owner, repo, url.PathEscape(ref)), nil, &r); err != nil {
		return nil, err
	}
	out := make([]TreeEntry, 0, len(r.Tree))
	for _, e := range r.Tree {
		if e.Type != "blob" {
			continue
		}
		out = append(out, e)
		if len(out) >= max {
			break
		}
	}
	return out, nil
}

// SanitizeBranchName turns arbitrary text into a git-safe branch name: only
// [a-z0-9._-], no leading dot/dash, collapsed separators, capped in length.
// The agent derives branch names from an issue number plus a timestamp, so
// this is mostly belt-and-braces — but a branch name that GitHub rejects
// would fail the whole agent run after the model already spent its tokens.
func SanitizeBranchName(s string) string {
	var b strings.Builder
	lastDash := false
	for _, r := range strings.ToLower(s) {
		ok := (r >= 'a' && r <= 'z') || (r >= '0' && r <= '9') || r == '.' || r == '_' || r == '-'
		if !ok {
			if !lastDash {
				b.WriteByte('-')
			}
			lastDash = true
			continue
		}
		b.WriteRune(r)
		lastDash = r == '-'
	}
	out := strings.Trim(b.String(), "-._")
	if len(out) > 80 {
		out = out[:80]
		out = strings.Trim(out, "-._")
	}
	return out
}
