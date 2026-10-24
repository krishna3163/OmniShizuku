package ghclient

import (
	"context"
	"fmt"
	"net/http"
	"net/url"
)

// DispatchWorkflow triggers an existing workflow through GitHub's
// workflow_dispatch API. workflow is the workflow's FILE name (or numeric
// ID) under .github/workflows/ — the bot never writes, edits, or infers the
// contents of what runs (see internal/triage/workflowdispatch.go).
//
// Requires `actions: write` on the token; GitHub's default GITHUB_TOKEN
// does NOT get that from contents/issues permissions alone, so the
// workflow's permissions: block has to declare it separately.
func (c *Client) DispatchWorkflow(ctx context.Context, owner, repo, workflow, ref string, inputs map[string]string) error {
	body := map[string]any{"ref": ref}
	if len(inputs) > 0 {
		body["inputs"] = inputs
	}
	path := fmt.Sprintf("/repos/%s/%s/actions/workflows/%s/dispatches",
		owner, repo, url.PathEscape(workflow))
	return c.do(ctx, http.MethodPost, path, body, nil)
}

// GetDefaultBranch returns the repository's default branch name, used to
// resolve a workflow-dispatch target's "default" ref.
func (c *Client) GetDefaultBranch(ctx context.Context, owner, repo string) (string, error) {
	var r struct {
		DefaultBranch string `json:"default_branch"`
	}
	if err := c.do(ctx, http.MethodGet, fmt.Sprintf("/repos/%s/%s", owner, repo), nil, &r); err != nil {
		return "", err
	}
	if r.DefaultBranch == "" {
		return "", fmt.Errorf("%s/%s has no default branch", owner, repo)
	}
	return r.DefaultBranch, nil
}
