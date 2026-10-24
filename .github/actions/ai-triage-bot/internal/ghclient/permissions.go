package ghclient

import (
	"context"
	"fmt"
	"net/http"
	"net/url"
	"strings"
)

// CollabPermission is the /repos/{owner}/{repo}/collaborators/{username}/
// permission response — GitHub's own answer to "what may this account do in
// THIS repository", resolved live rather than from a cached allowlist.
type CollabPermission struct {
	Permission string `json:"permission"` // "admin" | "maintain" | "write" | "triage" | "read" | "none"
	User       struct {
		Login string `json:"login"`
	} `json:"user"`
}

// permissionRank orders the levels so callers can ask "at least write?" or
// "exactly admin?" with one comparison. An unrecognized value ranks lowest,
// so a future/unknown level fails CLOSED on a privileged feature — the safe
// direction: refusing a legitimate collaborator once is recoverable,
// silently letting everyone through is not.
var permissionRank = map[string]int{
	"none":     0,
	"read":     1,
	"triage":   2,
	"write":    3,
	"maintain": 4,
	"admin":    5,
}

// GetCollaboratorPermission returns GitHub's live permission level for
// username in this repository. Callers must treat an error as "not
// authorized" — never as "assume they're allowed".
func (c *Client) GetCollaboratorPermission(ctx context.Context, owner, repo, username string) (*CollabPermission, error) {
	if strings.TrimSpace(username) == "" {
		return nil, fmt.Errorf("empty username")
	}
	var p CollabPermission
	err := c.do(ctx, http.MethodGet,
		fmt.Sprintf("/repos/%s/%s/collaborators/%s/permission", owner, repo, url.PathEscape(username)), nil, &p)
	if err != nil {
		return nil, err
	}
	return &p, nil
}

// HasPermissionAtLeast reports whether `got` satisfies the `need` bar
// ("write" = write/maintain/admin, "admin" = admin only).
func HasPermissionAtLeast(got, need string) bool {
	return permissionRank[got] >= permissionRank[need]
}
