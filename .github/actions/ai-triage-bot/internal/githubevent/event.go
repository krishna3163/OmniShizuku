// Package githubevent reads the event JSON payload from GITHUB_EVENT_PATH.
package githubevent

import (
	"encoding/json"
	"fmt"
	"os"
)

// Actor holds minimal author info. GitHub gives us Type for free in the
// webhook (no extra request), and it reliably distinguishes "User" from
// "Bot" — the first and cheapest signal for the Laplace factor (see
// internal/laplace).
type Actor struct {
	Login string `json:"login"`
	ID    int64  `json:"id"`
	Type  string `json:"type"` // "User" | "Bot" | "Organization"
}

// IssuePayload is the "issues" event's relevant payload fields.
type IssuePayload struct {
	Number int    `json:"number"`
	Title  string `json:"title"`
	Body   string `json:"body"`
	User   Actor  `json:"user"`
	// PullRequest is non-nil when this "issue" is actually a pull request —
	// the shape issue_comment carries for a comment on a PR, where the
	// comment's target may be a PR (workflow-dispatch's "head" ref, the
	// agent's target) rather than a plain issue.
	PullRequest *struct {
		HTMLURL string `json:"html_url"`
	} `json:"pull_request,omitempty"`
}

// PullRequestPayload is the "pull_request"/"pull_request_target" event's
// relevant payload fields.
type PullRequestPayload struct {
	Number int    `json:"number"`
	Title  string `json:"title"`
	Body   string `json:"body"`
	User   Actor  `json:"user"`
}

// CommentPayload is the shared "comment" object of the three comment
// events: issue_comment, discussion_comment and commit_comment. Only
// CommitID is specific to one of them (commit_comment).
type CommentPayload struct {
	ID       int64  `json:"id"`
	Body     string `json:"body"`
	User     Actor  `json:"user"`
	CommitID string `json:"commit_id"` // commit_comment only: the commit commented on
}

// DiscussionPayload is the discussion part of a "discussion_comment" event.
// Only Number is load-bearing: the handler re-reads the whole discussion
// (title, body, comments) live over GraphQL rather than trusting a payload
// that may be a second stale — see internal/ghclient/discussions.go.
type DiscussionPayload struct {
	Number   int    `json:"number"`
	Title    string `json:"title"`
	Body     string `json:"body"`
	Category struct {
		Name string `json:"name"`
	} `json:"category"`
}

// Repository is the repo the event belongs to.
type Repository struct {
	Name          string `json:"name"`
	DefaultBranch string `json:"default_branch"` // used to resolve workflow-dispatch's "default" ref
	Owner         Actor  `json:"owner"`
}

// Event is the shared payload shape covering every event type the bot
// listens to. Fields not present in a given event are simply nil/empty.
type Event struct {
	Action      string              `json:"action"`
	Issue       *IssuePayload       `json:"issue"`
	PullRequest *PullRequestPayload `json:"pull_request"`
	Comment     *CommentPayload     `json:"comment"`
	Discussion  *DiscussionPayload  `json:"discussion"`
	Repository  Repository          `json:"repository"`
}

// Load reads and parses the event payload at path (usually GITHUB_EVENT_PATH).
func Load(path string) (*Event, error) {
	data, err := os.ReadFile(path)
	if err != nil {
		return nil, fmt.Errorf("reading event payload: %w", err)
	}
	var e Event
	if err := json.Unmarshal(data, &e); err != nil {
		return nil, fmt.Errorf("parsing event payload: %w", err)
	}
	return &e, nil
}
