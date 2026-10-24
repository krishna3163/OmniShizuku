package ghclient

import (
	"context"
	"fmt"
	"net/http"
	"net/url"
	"strings"
)

// ghLabel is the request body for creating a repo label.
type ghLabel struct {
	Name        string `json:"name"`
	Color       string `json:"color"`
	Description string `json:"description"`
}

// EnsureLabel guarantees a label with this name exists in the repo,
// creating it if needed. Idempotent and race-safe (if the label was created
// concurrently, a 422 "already_exists" isn't treated as an error).
func (c *Client) EnsureLabel(ctx context.Context, owner, repo, name, color, description string) error {
	getErr := c.do(ctx, http.MethodGet, fmt.Sprintf("/repos/%s/%s/labels/%s", owner, repo, url.PathEscape(name)), nil, nil)
	if getErr == nil {
		return nil // already exists
	}

	createErr := c.do(ctx, http.MethodPost, fmt.Sprintf("/repos/%s/%s/labels", owner, repo),
		ghLabel{Name: name, Color: color, Description: description}, nil)
	if createErr != nil && !strings.Contains(strings.ToLower(createErr.Error()), "already_exists") {
		return createErr
	}
	return nil
}
