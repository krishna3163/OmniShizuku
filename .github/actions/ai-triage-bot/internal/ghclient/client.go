// Package ghclient is a minimal GitHub REST API client built on the
// standard library (net/http), no external dependencies — go-github is
// skipped on purpose to avoid the extra weight for a dozen endpoints.
package ghclient

import (
	"bytes"
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net/http"
	"strings"
	"time"
)

const apiVersion = "2022-11-28"

// ErrCallBudgetExceeded is returned once a run's GitHub API call budget
// (Client.maxCalls) is used up. Treat it as fatal: don't retry, don't fall
// back to another code path, just let the run end without posting anything.
var ErrCallBudgetExceeded = errors.New("github api call budget exceeded for this run")

// Client is a thin wrapper over the GitHub REST API.
type Client struct {
	http    *http.Client
	token   string
	baseURL string

	maxCalls int // circuit breaker: 0 disables the limit
	calls    int
}

// New creates a client with the given token (usually GITHUB_TOKEN from
// Actions) and a call budget for this run. maxCalls <= 0 disables the
// breaker entirely — pass a positive number in production.
func New(token string, maxCalls int) *Client {
	return &Client{
		http:     &http.Client{Timeout: 30 * time.Second},
		token:    token,
		baseURL:  "https://api.github.com",
		maxCalls: maxCalls,
	}
}

// checkBudget is the circuit breaker: every outbound request goes through
// here first. Once a run has made more calls than maxCalls allows, every
// subsequent call fails immediately instead of hitting the network — this
// is what stops a runaway loop (e.g. hundreds of bogus file paths) from
// burning through the run's rate limit, and it means the run ends in error
// before ever reaching the "post a comment" step.
func (c *Client) checkBudget() error {
	if c.maxCalls <= 0 {
		return nil
	}
	c.calls++
	if c.calls > c.maxCalls {
		return fmt.Errorf("%w (limit: %d calls)", ErrCallBudgetExceeded, c.maxCalls)
	}
	return nil
}

// do performs a JSON request: marshals body (if any), unmarshals the
// response into out (if out != nil). Any status >= 300 is returned as an error.
func (c *Client) do(ctx context.Context, method, path string, body any, out any) error {
	if err := c.checkBudget(); err != nil {
		return err
	}

	var reader io.Reader
	if body != nil {
		b, err := json.Marshal(body)
		if err != nil {
			return fmt.Errorf("marshal request body: %w", err)
		}
		reader = bytes.NewReader(b)
	}

	req, err := http.NewRequestWithContext(ctx, method, c.baseURL+path, reader)
	if err != nil {
		return fmt.Errorf("build request %s %s: %w", method, path, err)
	}
	c.setCommonHeaders(req)
	if body != nil {
		req.Header.Set("Content-Type", "application/json")
	}

	resp, err := c.http.Do(req)
	if err != nil {
		return fmt.Errorf("do request %s %s: %w", method, path, err)
	}
	defer resp.Body.Close()

	data, err := io.ReadAll(resp.Body)
	if err != nil {
		return fmt.Errorf("read response %s %s: %w", method, path, err)
	}

	if resp.StatusCode >= 300 {
		return fmt.Errorf("github api %s %s: HTTP %d: %s", method, path, resp.StatusCode, truncate(string(data), 500))
	}

	if out != nil && len(data) > 0 {
		if err := json.Unmarshal(data, out); err != nil {
			return fmt.Errorf("decode response %s %s: %w", method, path, err)
		}
	}
	return nil
}

// fetchRaw is for endpoints that return a raw body instead of JSON (e.g. a
// pull request's unified diff via Accept: application/vnd.github.v3.diff).
func (c *Client) fetchRaw(ctx context.Context, method, path, accept string) (string, error) {
	if err := c.checkBudget(); err != nil {
		return "", err
	}

	req, err := http.NewRequestWithContext(ctx, method, c.baseURL+path, nil)
	if err != nil {
		return "", fmt.Errorf("build request %s %s: %w", method, path, err)
	}
	c.setCommonHeaders(req)
	req.Header.Set("Accept", accept)

	resp, err := c.http.Do(req)
	if err != nil {
		return "", fmt.Errorf("do request %s %s: %w", method, path, err)
	}
	defer resp.Body.Close()

	data, err := io.ReadAll(resp.Body)
	if err != nil {
		return "", fmt.Errorf("read response %s %s: %w", method, path, err)
	}
	if resp.StatusCode >= 300 {
		return "", fmt.Errorf("github api %s %s: HTTP %d: %s", method, path, resp.StatusCode, truncate(string(data), 500))
	}
	return string(data), nil
}

// doGraphQL performs a request against GitHub's GraphQL endpoint
// (/graphql). Used only for the handful of operations the REST API doesn't
// expose at all — right now that's just deleting an issue. Everything else
// in this client is deliberately REST, per the package doc comment.
func (c *Client) doGraphQL(ctx context.Context, query string, variables map[string]any, out any) error {
	if err := c.checkBudget(); err != nil {
		return err
	}

	reqBody := struct {
		Query     string         `json:"query"`
		Variables map[string]any `json:"variables,omitempty"`
	}{Query: query, Variables: variables}

	b, err := json.Marshal(reqBody)
	if err != nil {
		return fmt.Errorf("marshal graphql request: %w", err)
	}

	req, err := http.NewRequestWithContext(ctx, http.MethodPost, c.baseURL+"/graphql", bytes.NewReader(b))
	if err != nil {
		return fmt.Errorf("build graphql request: %w", err)
	}
	c.setCommonHeaders(req)
	req.Header.Set("Content-Type", "application/json")

	resp, err := c.http.Do(req)
	if err != nil {
		return fmt.Errorf("do graphql request: %w", err)
	}
	defer resp.Body.Close()

	data, err := io.ReadAll(resp.Body)
	if err != nil {
		return fmt.Errorf("read graphql response: %w", err)
	}
	if resp.StatusCode >= 300 {
		return fmt.Errorf("github graphql api: HTTP %d: %s", resp.StatusCode, truncate(string(data), 500))
	}

	var envelope struct {
		Data   json.RawMessage `json:"data"`
		Errors []struct {
			Message string `json:"message"`
		} `json:"errors"`
	}
	if err := json.Unmarshal(data, &envelope); err != nil {
		return fmt.Errorf("decode graphql response: %w", err)
	}
	if len(envelope.Errors) > 0 {
		msgs := make([]string, len(envelope.Errors))
		for i, e := range envelope.Errors {
			msgs[i] = e.Message
		}
		// deleteIssue's characteristic failure mode when the token lacks
		// admin rights on the repo is exactly this: a GraphQL-level error
		// ("Viewer not authorized to delete"), not an HTTP error — so
		// this branch is what callers actually need to detect and handle.
		return fmt.Errorf("github graphql api returned error(s): %s", strings.Join(msgs, "; "))
	}
	if out != nil && len(envelope.Data) > 0 {
		if err := json.Unmarshal(envelope.Data, out); err != nil {
			return fmt.Errorf("decode graphql data: %w", err)
		}
	}
	return nil
}

func (c *Client) setCommonHeaders(req *http.Request) {
	req.Header.Set("Authorization", "Bearer "+c.token)
	req.Header.Set("Accept", "application/vnd.github+json")
	req.Header.Set("X-GitHub-Api-Version", apiVersion)
	req.Header.Set("User-Agent", "ai-triage-bot")
}

func truncate(s string, n int) string {
	if len(s) <= n {
		return s
	}
	return s[:n] + "...(truncated)"
}
