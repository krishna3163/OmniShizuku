package ghclient

import (
	"context"
	"fmt"
	"net/http"
	"net/url"
	"time"
)

// User holds the public account metadata from /users/{login}. This is
// where the account creation date comes from for the Laplace factor — the
// issue/PR webhook payload doesn't include the user's created_at.
type User struct {
	Login       string    `json:"login"`
	ID          int64     `json:"id"`
	Type        string    `json:"type"` // "User" | "Bot" | "Organization"
	Name        string    `json:"name"`
	Email       string    `json:"email"` // often empty — GitHub only returns it if public
	CreatedAt   time.Time `json:"created_at"`
	PublicRepos int       `json:"public_repos"`
	Followers   int       `json:"followers"`
	Following   int       `json:"following"`
}

// GetUser returns a user's public profile by login.
func (c *Client) GetUser(ctx context.Context, login string) (*User, error) {
	var u User
	err := c.do(ctx, http.MethodGet, fmt.Sprintf("/users/%s", login), nil, &u)
	if err != nil {
		return nil, err
	}
	return &u, nil
}

// BlockUserInOrg blocks a user from every repository owned by the given
// organization. Requires the caller's token to have the "Blocking users"
// organization permission (write) — a classic PAT with the `admin:org`
// scope, or a fine-grained PAT with that permission explicitly granted.
// GITHUB_TOKEN cannot do this. Fails harmlessly (from the caller's point of
// view — just return the error) with 404 if `org` isn't actually an
// organization (e.g. the repo is owned by a personal account), in which
// case the caller should fall back to BlockUserPersonal.
func (c *Client) BlockUserInOrg(ctx context.Context, org, username string) error {
	return c.do(ctx, http.MethodPut, fmt.Sprintf("/orgs/%s/blocks/%s", org, username), nil, nil)
}

// BlockUserPersonal blocks a user on behalf of the token's own account (the
// classic personal block list). IMPORTANT, and this must be surfaced to
// whoever configures the moderation token: this blocks the user
// ACCOUNT-WIDE, from every repository that account owns or collaborates
// on — not just the repo the bot is running in. Only meaningful when the
// repo is owned by a personal account (BlockUserInOrg doesn't apply
// there). Requires a fine-grained PAT with the personal "Blocking users"
// permission (write); GITHUB_TOKEN cannot do this either.
func (c *Client) BlockUserPersonal(ctx context.Context, username string) error {
	return c.do(ctx, http.MethodPut, fmt.Sprintf("/user/blocks/%s", username), nil, nil)
}

// SearchAuthorActivityTotal returns the author's TOTAL issue+PR count across
// all of GitHub (no repo scope, no time window) — used to estimate how many
// issues/PRs per day this account opens on average over its whole history.
func (c *Client) SearchAuthorActivityTotal(ctx context.Context, author string) (int, error) {
	q := "author:" + author
	var res SearchIssuesResult
	err := c.do(ctx, http.MethodGet, "/search/issues?q="+url.QueryEscape(q)+"&per_page=1", nil, &res)
	if err != nil {
		return 0, err
	}
	return res.TotalCount, nil
}
