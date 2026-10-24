// Package ai wraps the official Google Gen AI Go SDK
// (google.golang.org/genai) with support for a pool of 1..100 API keys: if
// the current key hits a rate limit / transient error, the bot retries that
// key with exponential backoff and then transparently switches to the next
// one.
package ai

import (
	"context"
	"errors"
	"fmt"
	"log"
	"strings"
	"time"

	"google.golang.org/genai"
)

// defaultMaxOutputTokens is the response budget for every call except the
// agent's code-generation call, which sets its own much larger one.
const defaultMaxOutputTokens = 2048

// Client is a Gemini client with key rotation.
type Client struct {
	keys  []string
	model string
	idx   int // index of the key to start the next request with

	// Per-key retry policy for rate-limit/transient errors, applied BEFORE
	// rotating to the next key in the pool (see the retry/backoff inputs
	// in action.yml).
	retries   int
	baseDelay time.Duration
	maxDelay  time.Duration
}

// ImagePart is a single screenshot attached as multimodal input alongside
// the text prompt. The data has already been validated and bounded by
// internal/attachments — only bytes whose signature actually matches an
// image end up here.
type ImagePart struct {
	MIMEType string
	Data     []byte
}

// Options carries the per-call knobs that differ between call sites:
// screenshots, the response token budget (the agent's code-generation call
// needs far more than the default, since its response contains full file
// contents), and whether the answer is plain prose rather than JSON (the
// Discussions Q&A reply is posted verbatim, so it must not be JSON-wrapped).
// A zero Options is the default triage call.
type Options struct {
	Images          []ImagePart
	MaxOutputTokens int  // <=0 means "use the default"
	PlainOutput     bool // true = ask for plain text instead of strict JSON
}

// New creates a client. keys is a list of 1..100 API keys, model is the
// model id, e.g. "gemini-3.5-flash-lite". retries is how many EXTRA times
// to retry the SAME key on a rate-limit/transient error before rotating to
// the next key; baseDelay/maxDelay bound the exponential backoff between
// those retries.
func New(keys []string, model string, retries int, baseDelay, maxDelay time.Duration) *Client {
	if retries < 0 {
		retries = 0
	}
	if baseDelay < 0 {
		baseDelay = 0
	}
	if maxDelay < baseDelay {
		maxDelay = baseDelay
	}
	return &Client{
		keys: keys, model: model,
		retries: retries, baseDelay: baseDelay, maxDelay: maxDelay,
	}
}

// Generate sends the system prompt + user prompt and asks the model to
// respond with strict JSON (responseMimeType=application/json).
// On a rate-limit/transient error it automatically tries the next key in
// the pool. This is a thin wrapper over GenerateWithOpts with no
// attachments and the default token budget.
func (c *Client) Generate(ctx context.Context, systemPrompt, userPrompt string) (string, error) {
	return c.GenerateWithOpts(ctx, systemPrompt, userPrompt, Options{})
}

// GenerateWithImages is the same as Generate, but also passes the model any
// screenshots (multimodal input) the user attached to the issue/PR.
// gemini-3.5-flash-lite and the rest of the Gemini 3.x line accept images
// natively in the same request as text — no separate "recognize this image"
// call is needed.
func (c *Client) GenerateWithImages(ctx context.Context, systemPrompt, userPrompt string, images []ImagePart) (string, error) {
	return c.GenerateWithOpts(ctx, systemPrompt, userPrompt, Options{Images: images})
}

// GeneratePlain asks for a prose answer rather than strict JSON. Used by the
// Discussions Q&A feature, whose reply is posted verbatim — a JSON object
// there would be posted as JSON. Retries/rotation behave exactly as in
// Generate; only the response shape differs.
func (c *Client) GeneratePlain(ctx context.Context, systemPrompt, userPrompt string) (string, error) {
	return c.GenerateWithOpts(ctx, systemPrompt, userPrompt, Options{PlainOutput: true})
}

// GenerateWithOpts runs one generation under the retry/rotation policy
// documented on New: for each key in the pool, up to 1+retries attempts
// with exponential backoff, then rotate to the next key. A non-transient
// error aborts immediately — retrying an invalid request against another
// key would just repeat the same error.
func (c *Client) GenerateWithOpts(ctx context.Context, systemPrompt, userPrompt string, opts Options) (string, error) {
	if len(c.keys) == 0 {
		return "", errors.New("gemini key pool is empty")
	}

	var lastErr error
	for attempt := 0; attempt < len(c.keys); attempt++ {
		keyIdx := (c.idx + attempt) % len(c.keys)
		key := c.keys[keyIdx]

		for retry := 0; retry <= c.retries; retry++ {
			if retry > 0 {
				delay := backoffDelay(c.baseDelay, c.maxDelay, retry-1)
				if err := sleep(ctx, delay); err != nil {
					return "", fmt.Errorf("waiting out the gemini backoff: %w", err)
				}
			}
			text, err := c.tryOnce(ctx, key, systemPrompt, userPrompt, opts)
			if err == nil {
				c.idx = keyIdx // next call starts with this same working key
				return text, nil
			}
			lastErr = err
			if !isRateLimitOrTransient(err) {
				// Not a rate limit (e.g. an invalid request) — burning through
				// the rest of the keys would just repeat the same error.
				return "", err
			}
			log.Printf("gemini: key #%d attempt %d/%d hit a rate limit/transient error: %v", keyIdx, retry+1, c.retries+1, err)
		}
		log.Printf("gemini: key #%d still unavailable after %d attempt(s) — rotating to the next key in the pool", keyIdx, c.retries+1)
	}

	return "", fmt.Errorf("all %d gemini keys exhausted/unavailable: %w", len(c.keys), lastErr)
}

// backoffDelay is the exponential backoff for the 0-based retry index:
// base, 2*base, 4*base, ... never longer than max. base=0 is a legal
// configuration ("retry immediately"), so nothing here assumes base > 0.
func backoffDelay(base, max time.Duration, retry int) time.Duration {
	if base <= 0 {
		return 0
	}
	d := base
	for i := 0; i < retry; i++ {
		d *= 2
		if d >= max {
			return max
		}
	}
	if d > max {
		return max
	}
	return d
}

// sleep waits for d, aborting early if the run's context expires first.
func sleep(ctx context.Context, d time.Duration) error {
	if d <= 0 {
		return nil
	}
	t := time.NewTimer(d)
	defer t.Stop()
	select {
	case <-ctx.Done():
		return ctx.Err()
	case <-t.C:
		return nil
	}
}

func (c *Client) tryOnce(ctx context.Context, apiKey, systemPrompt, userPrompt string, opts Options) (string, error) {
	cctx, cancel := context.WithTimeout(ctx, 60*time.Second)
	defer cancel()

	client, err := genai.NewClient(cctx, &genai.ClientConfig{
		APIKey:  apiKey,
		Backend: genai.BackendGeminiAPI,
	})
	if err != nil {
		return "", fmt.Errorf("creating genai client: %w", err)
	}

	maxTokens := opts.MaxOutputTokens
	if maxTokens <= 0 {
		maxTokens = defaultMaxOutputTokens
	}
	cfg := &genai.GenerateContentConfig{
		SystemInstruction: genai.NewContentFromText(systemPrompt, genai.RoleUser),
		ResponseMIMEType:  "application/json",
		MaxOutputTokens:   int32(maxTokens),
		// Temperature/TopP/TopK are intentionally left unset: for the
		// Gemini 3.x line, Google recommends not overriding the sampling
		// defaults — determinism here comes from the system prompt's
		// instructions, not from sampling parameters.
	}
	// Discussions Q&A posts the model's answer verbatim, so it must be
	// prose, not a JSON object — overriding the MIME type here is what
	// stops the model from wrapping a plain answer in quotes/braces.
	if opts.PlainOutput {
		cfg.ResponseMIMEType = "text/plain"
	}

	parts := make([]*genai.Part, 0, len(opts.Images)+1)
	parts = append(parts, genai.NewPartFromText(userPrompt))
	for _, img := range opts.Images {
		parts = append(parts, genai.NewPartFromBytes(img.Data, img.MIMEType))
	}
	contents := []*genai.Content{genai.NewContentFromParts(parts, genai.RoleUser)}

	resp, err := client.Models.GenerateContent(cctx, c.model, contents, cfg)
	if err != nil {
		return "", err
	}

	text := resp.Text()
	if strings.TrimSpace(text) == "" {
		return "", errors.New("model returned an empty response")
	}
	return text, nil
}

// isRateLimitOrTransient is a rough but practical check of whether the
// error text suggests a retry with the next key is worth it (429/quota/
// unavailable), versus a substantive error a retry won't fix.
func isRateLimitOrTransient(err error) bool {
	msg := strings.ToLower(err.Error())
	needles := []string{
		"429", "resource_exhausted", "rate limit", "quota",
		"unavailable", "503", "internal error", "500", "deadline exceeded",
	}
	for _, n := range needles {
		if strings.Contains(msg, n) {
			return true
		}
	}
	return false
}
