// Package antispam guards against flooding: if one author opened more than
// N issues within a time window, the AI never reads that burst at all —
// all of them get closed immediately as not_planned with an explanation.
package antispam

import (
	"context"
	"time"

	"aitriage/internal/ghclient"
)

// Result is the outcome of checking one "issue opened" event.
type Result struct {
	IsSpam       bool
	TotalCount   int
	IssueNumbers []int // all of the author's OPEN issues in the window (including the current one), to be closed
}

// Check looks up the author's issues over the last `window` and decides
// whether it's spam.
func Check(ctx context.Context, gh *ghclient.Client, owner, repo, author string, window time.Duration, threshold int) (*Result, error) {
	since := time.Now().Add(-window)
	res, err := gh.SearchAuthorIssuesSince(ctx, owner, repo, author, since)
	if err != nil {
		return nil, err
	}

	if res.TotalCount <= threshold {
		return &Result{IsSpam: false, TotalCount: res.TotalCount}, nil
	}

	var open []int
	for _, item := range res.Items {
		if item.PullRequest != nil {
			continue
		}
		if item.State == "open" {
			open = append(open, item.Number)
		}
	}

	return &Result{IsSpam: true, TotalCount: res.TotalCount, IssueNumbers: open}, nil
}
