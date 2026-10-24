package ghclient

import (
	"context"
	"fmt"
	"net/http"
	"net/url"
	"time"
)

// SearchItem is one hit from /search/issues.
type SearchItem struct {
	Number int    `json:"number"`
	State  string `json:"state"`
	Title  string `json:"title"` // used by duplicate detection to score keyword overlap
	Body   string `json:"body"`  // ditto — the caller truncates it before use
	// GitHub adds PullRequest when the item is actually a pull request —
	// extra protection on top of the type:issue / type:pr query filter.
	PullRequest *struct{} `json:"pull_request,omitempty"`
}

// SearchIssuesResult is the part of the /search/issues response we need.
type SearchIssuesResult struct {
	TotalCount int          `json:"total_count"`
	Items      []SearchItem `json:"items"`
}

// SearchAuthorIssuesSince returns the given author's issues in this repo
// created at or after `since`. Only used for the antispam check, so it
// explicitly filters type:issue (pull requests don't count).
func (c *Client) SearchAuthorIssuesSince(ctx context.Context, owner, repo, author string, since time.Time) (*SearchIssuesResult, error) {
	q := fmt.Sprintf("repo:%s/%s type:issue author:%s created:>=%s",
		owner, repo, author, since.UTC().Format("2006-01-02T15:04:05"))

	var res SearchIssuesResult
	err := c.do(ctx, http.MethodGet, "/search/issues?q="+url.QueryEscape(q)+"&per_page=100", nil, &res)
	if err != nil {
		return nil, err
	}
	return &res, nil
}

// SearchRepoItemsByKeyword returns this repo's issues (or PRs) whose TITLE
// contains keyword. One query per keyword, deliberately, instead of one
// big OR-joined query: GitHub's search gives OR lower precedence than the
// repo:/type: qualifiers, so an OR query can silently escape the repository
// and return unrelated noise. One simple query per keyword has no such
// failure mode. Used only by duplicate detection.
func (c *Client) SearchRepoItemsByKeyword(ctx context.Context, owner, repo, keyword string, isPR bool, perPage int) (*SearchIssuesResult, error) {
	kind := "issue"
	if isPR {
		kind = "pr"
	}
	q := fmt.Sprintf("repo:%s/%s type:%s in:title %q", owner, repo, kind, keyword)
	if perPage < 1 {
		perPage = 1
	}
	if perPage > 100 {
		perPage = 100
	}

	var res SearchIssuesResult
	err := c.do(ctx, http.MethodGet, "/search/issues?q="+url.QueryEscape(q)+fmt.Sprintf("&per_page=%d", perPage), nil, &res)
	if err != nil {
		return nil, err
	}
	return &res, nil
}
