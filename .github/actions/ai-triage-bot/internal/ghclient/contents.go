package ghclient

import (
	"context"
	"encoding/base64"
	"fmt"
	"net/http"
	"net/url"
	"strings"
)

// contentFile is the Contents API response for a single file.
type contentFile struct {
	Path     string `json:"path"`
	SHA      string `json:"sha"`
	Size     int    `json:"size"`
	Content  string `json:"content"`
	Encoding string `json:"encoding"`
	Type     string `json:"type"` // "file", "dir", "symlink", ...
}

func (c *Client) getContentFile(ctx context.Context, owner, repo, path, ref string) (*contentFile, error) {
	q := ""
	if ref != "" {
		q = "?ref=" + url.QueryEscape(ref)
	}
	var cf contentFile
	err := c.do(ctx, http.MethodGet,
		fmt.Sprintf("/repos/%s/%s/contents/%s%s", owner, repo, escapePathSegments(path), q), nil, &cf)
	if err != nil {
		return nil, err
	}
	return &cf, nil
}

// GetFileContent reads a single repository file at the given ref (empty ref
// means the default branch).
func (c *Client) GetFileContent(ctx context.Context, owner, repo, path, ref string) (string, error) {
	cf, err := c.getContentFile(ctx, owner, repo, path, ref)
	if err != nil {
		return "", err
	}
	if cf.Type != "file" {
		return "", fmt.Errorf("%s is not a regular file (type=%s)", path, cf.Type)
	}
	if cf.Encoding != "base64" {
		return "", fmt.Errorf("unsupported encoding %q for %s", cf.Encoding, path)
	}
	raw, err := base64.StdEncoding.DecodeString(strings.ReplaceAll(cf.Content, "\n", ""))
	if err != nil {
		return "", fmt.Errorf("decode base64 content of %s: %w", path, err)
	}
	return string(raw), nil
}

// GetFileContentWithSHA is like GetFileContent, but also returns the
// blob's current SHA in the same API call — PutFileContent needs that SHA
// as a concurrency check when overwriting an existing file. found=false
// (with err=nil) means the file doesn't exist yet, which is a normal,
// expected outcome for callers (e.g. the moderation state file on its very
// first write), not an error.
func (c *Client) GetFileContentWithSHA(ctx context.Context, owner, repo, path, ref string) (content, sha string, found bool, err error) {
	cf, err := c.getContentFile(ctx, owner, repo, path, ref)
	if err != nil {
		if IsNotFound(err) {
			return "", "", false, nil
		}
		return "", "", false, err
	}
	if cf.Type != "file" {
		return "", "", false, fmt.Errorf("%s is not a regular file (type=%s)", path, cf.Type)
	}
	if cf.Encoding != "base64" {
		return "", "", false, fmt.Errorf("unsupported encoding %q for %s", cf.Encoding, path)
	}
	raw, err := base64.StdEncoding.DecodeString(strings.ReplaceAll(cf.Content, "\n", ""))
	if err != nil {
		return "", "", false, fmt.Errorf("decode base64 content of %s: %w", path, err)
	}
	return string(raw), cf.SHA, true, nil
}

// PutFileContent creates or updates a repository file via the Contents
// API. sha must be the file's current blob SHA (from
// GetFileContentWithSHA) when overwriting an existing file, and empty when
// creating a new one; GitHub rejects the write with a 409 if sha is stale,
// which callers can use to detect (and retry past) a concurrent write from
// another run. This is used ONLY by internal/moderation to persist its
// per-author offense-count file — nothing else in the bot writes to the
// repository.
func (c *Client) PutFileContent(ctx context.Context, owner, repo, path, message string, content []byte, sha string) error {
	body := map[string]any{
		"message": message,
		"content": base64.StdEncoding.EncodeToString(content),
	}
	if sha != "" {
		body["sha"] = sha
	}
	return c.do(ctx, http.MethodPut,
		fmt.Sprintf("/repos/%s/%s/contents/%s", owner, repo, escapePathSegments(path)), body, nil)
}

// IsNotFound reports whether err is GitHub's 404 for a missing path/ref.
// Callers use it to distinguish "this file does not exist at that ref"
// (a legitimate answer — the file is new, or was deleted) from a real
// fetch failure (permissions, rate limit, network), which must not be
// silently reported as "the file is empty".
func IsNotFound(err error) bool {
	return err != nil && strings.Contains(err.Error(), "HTTP 404")
}

func escapePathSegments(p string) string {
	p = strings.TrimPrefix(p, "/")
	parts := strings.Split(p, "/")
	for i, s := range parts {
		parts[i] = url.PathEscape(s)
	}
	return strings.Join(parts, "/")
}
