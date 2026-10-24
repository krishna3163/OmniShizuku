// Package config reads the bot's configuration from environment variables,
// which action.yml passes in.
package config

import (
	"encoding/json"
	"errors"
	"fmt"
	"os"
	"strconv"
	"strings"
	"time"
)

// Config holds everything needed for one bot run (one event = one run).
type Config struct {
	GitHubToken   string
	GeminiAPIKeys []string
	GeminiModel   string

	Workspace        string // root of the checked-out repo (GITHUB_WORKSPACE)
	RepoContextPath  string // system prompt #1 (repo description)
	InstructionsPath string // system prompt #2 (instructions/tone)
	TemplatesPath    string // system prompt #3 (optional canned patterns for recurring issue/PR shapes)
	LabelsConfigPath string // config of allowed labels

	SpamWindow    time.Duration
	SpamThreshold int

	MaxFiles     int
	MaxFileBytes int
	MaxDiffBytes int

	// MaxExtractedPaths guards against issue bodies that list an excessive
	// number of file paths purely to make the bot hammer the GitHub API
	// (each path is a separate Contents API call). Above this count the
	// issue is treated as abuse and labeled without ever calling the AI.
	MaxExtractedPaths int

	// MaxReferencedIssues caps how many other issues/PRs (referenced in the
	// body as "#123") the bot will resolve and pass as context per run. Like
	// MaxExtractedPaths, this exists so a body can't force an unbounded
	// number of extra GitHub API calls just by listing "#1 #2 #3 ...".
	// 0 disables reference resolution entirely.
	MaxReferencedIssues int

	// MaxAPICalls is a hard ceiling on GitHub API calls for a single run —
	// a circuit breaker. If something drives the bot into making far more
	// requests than a normal run needs (a bug, a crafted payload, an
	// unbounded retry loop), the client starts refusing further calls and
	// the run aborts without posting anything.
	MaxAPICalls int

	// Attachments from the issue/PR body (screenshots and text files the
	// user drag-and-dropped in). See internal/attachments.
	MaxAttachments         int   // max attachments processed per run
	MaxAttachmentBytes     int64 // max size of a SINGLE attachment
	MaxAttachmentTotal     int64 // total byte budget for all attachments in one run
	AttachmentFetchTimeout time.Duration

	// Moderation: automatic removal of doxxing/severe-abuse content, with
	// escalation to an account block on a repeat offense by the same
	// author. GITHUB_TOKEN alone can only redact+close+lock content (see
	// internal/triage/moderation.go for exactly why) — ModerationToken
	// (optional) is a separate, more privileged PAT that unlocks real
	// issue deletion and real user blocking. Leaving it empty is a
	// perfectly normal, safe configuration, not a partially-broken one.
	ModerationToken     string
	ModerationStatePath string // where the per-author offense-count JSON file lives in the repo
	ModerationLogIssue  int    // issue number to post an audit-trail comment on for every moderation action (0 = disabled)

	// Gemini retry/backoff: how many EXTRA times to retry the SAME key on a
	// rate-limit/transient error before rotating to the next key in the
	// pool, and the exponential backoff bounds for those retries.
	GeminiMaxRetries     int
	GeminiRetryBaseDelay time.Duration
	GeminiRetryMaxDelay  time.Duration

	// Duplicate detection: a keyword-overlap search shortlists candidates,
	// the AI then compares full text against full text and decides.
	DuplicateDetectionEnabled bool
	MaxDuplicateCandidates    int
	DuplicateSearchKeywords   int
	DuplicateMinSimilarity    float64
	DuplicateMaxBodyChars     int

	// Discussions Q&A (a SEPARATE feature from issue/PR triage — see
	// internal/triage/discussion.go): only reacts when an authorized
	// collaborator @-mentions DiscussionTrigger in a discussion comment.
	DiscussionsEnabled         bool
	DiscussionTrigger          string
	DiscussionInstructionsPath string
	DiscussionSpamWindow       time.Duration
	DiscussionSpamThreshold    int
	DiscussionStatePath        string
	DiscussionMaxComments      int

	// AI agent (internal/triage/agent.go): authorized collaborator asks for
	// a proposed PR or a related issue; nothing is ever auto-merged.
	AgentEnabled          bool
	AgentTrigger          string
	AgentInstructionsPath string
	AgentAllowedUsers     []string
	AgentMaxContextFiles  int
	AgentMaxTreeEntries   int
	AgentMaxOutputFiles   int
	AgentMaxOutputBytes   int
	AgentMaxOutputTokens  int
	AgentBranchPrefix     string
	AgentSpamWindow       time.Duration
	AgentSpamThreshold    int
	AgentStatePath        string

	// Commit review (internal/triage/commitreview.go): the repo OWNER rates
	// one commit's message + diff — a stricter `admin`-only bar.
	CommitReviewEnabled          bool
	CommitReviewTrigger          string
	CommitReviewInstructionsPath string
	CommitReviewAllowedUsers     []string
	CommitReviewSpamWindow       time.Duration
	CommitReviewSpamThreshold    int
	CommitReviewStatePath        string

	// Workflow dispatch by comment (internal/triage/workflowdispatch.go):
	// the repo OWNER triggers a pre-configured workflow. No AI is involved
	// in picking the target — see WorkflowDispatchTargets.
	WorkflowDispatchEnabled       bool
	WorkflowDispatchTrigger       string
	WorkflowDispatchTargets       []WorkflowDispatchTarget
	WorkflowDispatchAllowedUsers  []string
	WorkflowDispatchSpamWindow    time.Duration
	WorkflowDispatchSpamThreshold int
	WorkflowDispatchStatePath     string

	EventName string
	EventPath string

	RepoOwner string
	RepoName  string
}

// WorkflowDispatchTarget is one entry of the WORKFLOW_DISPATCH_TARGETS JSON
// array: the workflow an owner may trigger by comment, how a given comment
// maps to it, and the inputs to pass through to that workflow's own
// `workflow_dispatch: inputs:` schema.
type WorkflowDispatchTarget struct {
	Name     string            `json:"name"`     // required: human label used in the reply comment
	Keywords []string          `json:"keywords"` // matched (case-insensitively) against the text after the trigger phrase
	Workflow string            `json:"workflow"` // required: existing file under .github/workflows/ (or numeric ID)
	Ref      string            `json:"ref"`      // "default" | "head" | a literal branch/tag name ("" = "default")
	Inputs   map[string]string `json:"inputs"`   // passed straight through, after placeholder substitution
}

// Load reads and validates the configuration. Every parameter except the
// tokens has a sensible default.
func Load() (*Config, error) {
	c := &Config{}

	c.GitHubToken = os.Getenv("GITHUB_TOKEN")
	if c.GitHubToken == "" {
		return nil, errors.New("GITHUB_TOKEN is not set")
	}

	keysRaw := os.Getenv("GEMINI_API_KEYS")
	if strings.TrimSpace(keysRaw) == "" {
		return nil, errors.New("GEMINI_API_KEYS is not set (need at least 1 key, up to 100 separated by commas/newlines)")
	}
	c.GeminiAPIKeys = splitList(keysRaw)
	if len(c.GeminiAPIKeys) == 0 {
		return nil, errors.New("GEMINI_API_KEYS did not contain any valid keys")
	}
	if len(c.GeminiAPIKeys) > 100 {
		c.GeminiAPIKeys = c.GeminiAPIKeys[:100]
	}

	c.GeminiModel = envDefault("GEMINI_MODEL", "gemini-3.5-flash-lite")
	c.Workspace = envDefault("GITHUB_WORKSPACE", ".")
	c.RepoContextPath = envDefault("REPO_CONTEXT_PATH", ".github/ai-triage/repo_context.md")
	c.InstructionsPath = envDefault("INSTRUCTIONS_PATH", ".github/ai-triage/instructions.md")
	c.TemplatesPath = envDefault("TEMPLATES_PATH", ".github/ai-triage/templates.md")
	c.LabelsConfigPath = envDefault("LABELS_CONFIG_PATH", ".github/ai-triage/labels.json")

	windowMin, err := strconv.Atoi(envDefault("SPAM_WINDOW_MINUTES", "60"))
	if err != nil {
		return nil, fmt.Errorf("SPAM_WINDOW_MINUTES: %w", err)
	}
	c.SpamWindow = time.Duration(windowMin) * time.Minute

	c.SpamThreshold, err = strconv.Atoi(envDefault("SPAM_THRESHOLD", "5"))
	if err != nil {
		return nil, fmt.Errorf("SPAM_THRESHOLD: %w", err)
	}

	c.MaxFiles, err = strconv.Atoi(envDefault("MAX_FILES", "4"))
	if err != nil {
		return nil, fmt.Errorf("MAX_FILES: %w", err)
	}
	if c.MaxFiles < 1 {
		c.MaxFiles = 1
	}

	c.MaxFileBytes, err = strconv.Atoi(envDefault("MAX_FILE_BYTES", "20000"))
	if err != nil {
		return nil, fmt.Errorf("MAX_FILE_BYTES: %w", err)
	}
	if c.MaxFileBytes < 1 {
		c.MaxFileBytes = 1
	}

	c.MaxDiffBytes, err = strconv.Atoi(envDefault("MAX_DIFF_BYTES", "30000"))
	if err != nil {
		return nil, fmt.Errorf("MAX_DIFF_BYTES: %w", err)
	}
	if c.MaxDiffBytes < 1 {
		c.MaxDiffBytes = 1
	}

	c.MaxExtractedPaths, err = strconv.Atoi(envDefault("MAX_EXTRACTED_PATHS", "30"))
	if err != nil {
		return nil, fmt.Errorf("MAX_EXTRACTED_PATHS: %w", err)
	}
	if c.MaxExtractedPaths < 1 {
		c.MaxExtractedPaths = 1
	}

	c.MaxReferencedIssues, err = strconv.Atoi(envDefault("MAX_REFERENCED_ISSUES", "5"))
	if err != nil {
		return nil, fmt.Errorf("MAX_REFERENCED_ISSUES: %w", err)
	}
	if c.MaxReferencedIssues < 0 {
		c.MaxReferencedIssues = 0
	}

	c.MaxAPICalls, err = strconv.Atoi(envDefault("MAX_API_CALLS", "60"))
	if err != nil {
		return nil, fmt.Errorf("MAX_API_CALLS: %w", err)
	}
	if c.MaxAPICalls < 1 {
		c.MaxAPICalls = 1
	}

	c.MaxAttachments, err = strconv.Atoi(envDefault("MAX_ATTACHMENTS", "4"))
	if err != nil {
		return nil, fmt.Errorf("MAX_ATTACHMENTS: %w", err)
	}
	if c.MaxAttachments < 0 {
		c.MaxAttachments = 0
	}

	maxAttBytes, err := strconv.ParseInt(envDefault("MAX_ATTACHMENT_BYTES", "8000000"), 10, 64)
	if err != nil {
		return nil, fmt.Errorf("MAX_ATTACHMENT_BYTES: %w", err)
	}
	c.MaxAttachmentBytes = maxAttBytes

	maxAttTotal, err := strconv.ParseInt(envDefault("MAX_ATTACHMENT_TOTAL_BYTES", "20000000"), 10, 64)
	if err != nil {
		return nil, fmt.Errorf("MAX_ATTACHMENT_TOTAL_BYTES: %w", err)
	}
	c.MaxAttachmentTotal = maxAttTotal

	attTimeoutSec, err := strconv.Atoi(envDefault("ATTACHMENT_FETCH_TIMEOUT_SECONDS", "15"))
	if err != nil {
		return nil, fmt.Errorf("ATTACHMENT_FETCH_TIMEOUT_SECONDS: %w", err)
	}
	c.AttachmentFetchTimeout = time.Duration(attTimeoutSec) * time.Second

	// MODERATION_TOKEN is deliberately allowed to be empty — see the doc
	// comment on Config.ModerationToken. No validation beyond that: if
	// it's set but insufficiently privileged, individual API calls fail
	// at call time and the bot logs that and degrades gracefully, rather
	// than refusing to start.
	c.ModerationToken = os.Getenv("MODERATION_TOKEN")
	c.ModerationStatePath = envDefault("MODERATION_STATE_PATH", ".github/ai-triage/moderation-state.json")

	c.ModerationLogIssue, err = strconv.Atoi(envDefault("MODERATION_LOG_ISSUE", "0"))
	if err != nil {
		return nil, fmt.Errorf("MODERATION_LOG_ISSUE: %w", err)
	}
	if c.ModerationLogIssue < 0 {
		c.ModerationLogIssue = 0
	}

	// --- Gemini retry/backoff (see internal/ai) ---
	c.GeminiMaxRetries, err = strconv.Atoi(envDefault("GEMINI_MAX_RETRIES", "3"))
	if err != nil {
		return nil, fmt.Errorf("GEMINI_MAX_RETRIES: %w", err)
	}
	if c.GeminiMaxRetries < 0 {
		c.GeminiMaxRetries = 0
	}
	c.GeminiRetryBaseDelay, err = envMillis("GEMINI_RETRY_BASE_DELAY_MS", 2000)
	if err != nil {
		return nil, err
	}
	c.GeminiRetryMaxDelay, err = envMillis("GEMINI_RETRY_MAX_DELAY_MS", 20000)
	if err != nil {
		return nil, err
	}
	if c.GeminiRetryMaxDelay < c.GeminiRetryBaseDelay {
		c.GeminiRetryMaxDelay = c.GeminiRetryBaseDelay
	}

	// --- Duplicate detection (see internal/duplicate) ---
	if c.DuplicateDetectionEnabled, err = envBool("DUPLICATE_DETECTION_ENABLED", "true"); err != nil {
		return nil, err
	}
	c.MaxDuplicateCandidates, err = strconv.Atoi(envDefault("MAX_DUPLICATE_CANDIDATES", "5"))
	if err != nil {
		return nil, fmt.Errorf("MAX_DUPLICATE_CANDIDATES: %w", err)
	}
	if c.MaxDuplicateCandidates < 1 {
		c.MaxDuplicateCandidates = 1
	}
	c.DuplicateSearchKeywords, err = strconv.Atoi(envDefault("DUPLICATE_SEARCH_KEYWORDS", "6"))
	if err != nil {
		return nil, fmt.Errorf("DUPLICATE_SEARCH_KEYWORDS: %w", err)
	}
	if c.DuplicateSearchKeywords < 1 {
		c.DuplicateSearchKeywords = 1
	}
	if c.DuplicateSearchKeywords > c.MaxDuplicateCandidates*3 {
		// Bound the search fan-out relative to what can actually be shown:
		// 6 keyword queries feeding 5 candidates is useful, 60 is not.
		c.DuplicateSearchKeywords = c.MaxDuplicateCandidates * 3
	}
	minSimRaw := envDefault("DUPLICATE_MIN_SIMILARITY", "0.12")
	c.DuplicateMinSimilarity, err = strconv.ParseFloat(minSimRaw, 64)
	if err != nil {
		return nil, fmt.Errorf("DUPLICATE_MIN_SIMILARITY: %w", err)
	}
	if c.DuplicateMinSimilarity < 0 || c.DuplicateMinSimilarity > 1 {
		return nil, fmt.Errorf("DUPLICATE_MIN_SIMILARITY: %s is not in the 0.0-1.0 range", minSimRaw)
	}
	c.DuplicateMaxBodyChars, err = strconv.Atoi(envDefault("DUPLICATE_MAX_BODY_CHARS", "1500"))
	if err != nil {
		return nil, fmt.Errorf("DUPLICATE_MAX_BODY_CHARS: %w", err)
	}
	if c.DuplicateMaxBodyChars < 100 {
		c.DuplicateMaxBodyChars = 100
	}

	// --- Discussions Q&A (see internal/triage/discussion.go) ---
	if c.DiscussionsEnabled, err = envBool("DISCUSSIONS_ENABLED", "false"); err != nil {
		return nil, err
	}
	c.DiscussionTrigger = envDefault("DISCUSSION_TRIGGER", "@github-discussions")
	c.DiscussionInstructionsPath = envDefault("DISCUSSION_INSTRUCTIONS_PATH", ".github/ai-triage/discussion_instructions.md")
	if c.DiscussionSpamWindow, err = envWindow("DISCUSSION_SPAM_WINDOW_MINUTES", 60); err != nil {
		return nil, err
	}
	if c.DiscussionSpamThreshold, err = envThreshold("DISCUSSION_SPAM_THRESHOLD", 5); err != nil {
		return nil, err
	}
	c.DiscussionStatePath = envDefault("DISCUSSION_STATE_PATH", ".github/ai-triage/discussion-state.json")
	c.DiscussionMaxComments, err = strconv.Atoi(envDefault("DISCUSSION_MAX_COMMENTS", "40"))
	if err != nil {
		return nil, fmt.Errorf("DISCUSSION_MAX_COMMENTS: %w", err)
	}
	if c.DiscussionMaxComments < 1 {
		c.DiscussionMaxComments = 1
	}

	// --- AI agent (see internal/triage/agent.go) ---
	if c.AgentEnabled, err = envBool("AGENT_ENABLED", "false"); err != nil {
		return nil, err
	}
	c.AgentTrigger = envDefault("AGENT_TRIGGER", "@github-actions")
	c.AgentInstructionsPath = envDefault("AGENT_INSTRUCTIONS_PATH", ".github/ai-triage/agent_instructions.md")
	c.AgentAllowedUsers = splitList(os.Getenv("AGENT_ALLOWED_USERS"))
	if c.AgentMaxContextFiles, err = envPositive("AGENT_MAX_CONTEXT_FILES", 6); err != nil {
		return nil, err
	}
	if c.AgentMaxTreeEntries, err = envPositive("AGENT_MAX_TREE_ENTRIES", 500); err != nil {
		return nil, err
	}
	if c.AgentMaxOutputFiles, err = envPositive("AGENT_MAX_OUTPUT_FILES", 6); err != nil {
		return nil, err
	}
	if c.AgentMaxOutputBytes, err = envPositive("AGENT_MAX_OUTPUT_BYTES", 150000); err != nil {
		return nil, err
	}
	if c.AgentMaxOutputTokens, err = envPositive("AGENT_MAX_OUTPUT_TOKENS", 16384); err != nil {
		return nil, err
	}
	c.AgentBranchPrefix = envDefault("AGENT_BRANCH_PREFIX", "ai-agent/")
	if c.AgentSpamWindow, err = envWindow("AGENT_SPAM_WINDOW_MINUTES", 60); err != nil {
		return nil, err
	}
	if c.AgentSpamThreshold, err = envThreshold("AGENT_SPAM_THRESHOLD", 3); err != nil {
		return nil, err
	}
	c.AgentStatePath = envDefault("AGENT_STATE_PATH", ".github/ai-triage/agent-state.json")

	// --- Commit review (see internal/triage/commitreview.go) ---
	if c.CommitReviewEnabled, err = envBool("COMMIT_REVIEW_ENABLED", "false"); err != nil {
		return nil, err
	}
	c.CommitReviewTrigger = envDefault("COMMIT_REVIEW_TRIGGER", "@github-discussions")
	c.CommitReviewInstructionsPath = envDefault("COMMIT_REVIEW_INSTRUCTIONS_PATH", ".github/ai-triage/commit_review_instructions.md")
	c.CommitReviewAllowedUsers = splitList(os.Getenv("COMMIT_REVIEW_ALLOWED_USERS"))
	if c.CommitReviewSpamWindow, err = envWindow("COMMIT_REVIEW_SPAM_WINDOW_MINUTES", 60); err != nil {
		return nil, err
	}
	if c.CommitReviewSpamThreshold, err = envThreshold("COMMIT_REVIEW_SPAM_THRESHOLD", 5); err != nil {
		return nil, err
	}
	c.CommitReviewStatePath = envDefault("COMMIT_REVIEW_STATE_PATH", ".github/ai-triage/commit-review-state.json")

	// --- Workflow dispatch by comment (see internal/triage/workflowdispatch.go) ---
	if c.WorkflowDispatchEnabled, err = envBool("WORKFLOW_DISPATCH_ENABLED", "false"); err != nil {
		return nil, err
	}
	c.WorkflowDispatchTrigger = envDefault("WORKFLOW_DISPATCH_TRIGGER", "@github-discussions")
	c.WorkflowDispatchAllowedUsers = splitList(os.Getenv("WORKFLOW_DISPATCH_ALLOWED_USERS"))
	if c.WorkflowDispatchSpamWindow, err = envWindow("WORKFLOW_DISPATCH_SPAM_WINDOW_MINUTES", 60); err != nil {
		return nil, err
	}
	if c.WorkflowDispatchSpamThreshold, err = envThreshold("WORKFLOW_DISPATCH_SPAM_THRESHOLD", 3); err != nil {
		return nil, err
	}
	c.WorkflowDispatchStatePath = envDefault("WORKFLOW_DISPATCH_STATE_PATH", ".github/ai-triage/workflow-dispatch-state.json")
	if err := c.loadWorkflowTargets(); err != nil {
		return nil, err
	}

	c.EventName = os.Getenv("GITHUB_EVENT_NAME")
	c.EventPath = os.Getenv("GITHUB_EVENT_PATH")
	if c.EventName == "" || c.EventPath == "" {
		return nil, errors.New("GITHUB_EVENT_NAME/GITHUB_EVENT_PATH are not set — the bot is only meant to run inside GitHub Actions")
	}

	// Optional override for non-standard runners.
	c.RepoOwner = os.Getenv("REPO_OWNER_OVERRIDE")
	c.RepoName = os.Getenv("REPO_NAME_OVERRIDE")

	return c, nil
}

// splitList splits a comma/semicolon/newline/tab/space separated list
// (used for Gemini API keys and for the *-allowed-users login lists).
func splitList(raw string) []string {
	fields := strings.FieldsFunc(raw, func(r rune) bool {
		switch r {
		case ',', ';', '\n', '\r', '\t', ' ':
			return true
		}
		return false
	})
	seen := make(map[string]bool, len(fields))
	out := make([]string, 0, len(fields))
	for _, f := range fields {
		f = strings.TrimSpace(f)
		if f == "" || seen[f] {
			continue
		}
		seen[f] = true
		out = append(out, f)
	}
	return out
}

func envDefault(key, def string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return def
}

// envBool parses a true/false toggle, failing loudly on a typo instead of
// silently falling back to the default — a misconfigured feature input that
// silently disables a feature is exactly what this bot must not do.
func envBool(key, def string) (bool, error) {
	v := strings.TrimSpace(envDefault(key, def))
	b, err := strconv.ParseBool(v)
	if err != nil {
		return false, fmt.Errorf("%s: %q is not a boolean (expected true/false)", key, v)
	}
	return b, nil
}

// envMillis parses a duration expressed in milliseconds.
func envMillis(key string, def int) (time.Duration, error) {
	v := envDefault(key, strconv.Itoa(def))
	ms, err := strconv.Atoi(v)
	if err != nil {
		return 0, fmt.Errorf("%s: %w", key, err)
	}
	if ms < 0 {
		return 0, fmt.Errorf("%s: %s must not be negative", key, v)
	}
	return time.Duration(ms) * time.Millisecond, nil
}

// envWindow parses a time window expressed in minutes.
func envWindow(key string, def int) (time.Duration, error) {
	v := envDefault(key, strconv.Itoa(def))
	min, err := strconv.Atoi(v)
	if err != nil {
		return 0, fmt.Errorf("%s: %w", key, err)
	}
	if min < 1 {
		return 0, fmt.Errorf("%s: %s must be at least 1 minute", key, v)
	}
	return time.Duration(min) * time.Minute, nil
}

// envThreshold parses a "strictly more than this" rate-limit threshold.
func envThreshold(key string, def int) (int, error) {
	v := envDefault(key, strconv.Itoa(def))
	n, err := strconv.Atoi(v)
	if err != nil {
		return 0, fmt.Errorf("%s: %w", key, err)
	}
	if n < 0 {
		return 0, fmt.Errorf("%s: %s must not be negative", key, v)
	}
	return n, nil
}

// envPositive parses a limit that must be at least 1 to be meaningful.
func envPositive(key string, def int) (int, error) {
	v := envDefault(key, strconv.Itoa(def))
	n, err := strconv.Atoi(v)
	if err != nil {
		return 0, fmt.Errorf("%s: %w", key, err)
	}
	if n < 1 {
		return 0, fmt.Errorf("%s: %s must be at least 1", key, v)
	}
	return n, nil
}

// loadWorkflowTargets parses and validates WORKFLOW_DISPATCH_TARGETS.
// Per action.yml this is a HARD config error when the feature is on and
// the target list is unusable — the bot refuses to start rather than
// running with a feature that would silently never fire.
func (c *Config) loadWorkflowTargets() error {
	raw := strings.TrimSpace(os.Getenv("WORKFLOW_DISPATCH_TARGETS"))
	if !c.WorkflowDispatchEnabled {
		// Still parse when provided, so a typo fails loudly even before the
		// feature is switched on — but an empty value is fine while off.
		if raw == "" {
			return nil
		}
	} else if raw == "" {
		return errors.New("WORKFLOW_DISPATCH_TARGETS is empty while workflow-dispatch-enabled is true — configure at least one target (see action.yml) or disable the feature")
	}

	var targets []WorkflowDispatchTarget
	if err := json.Unmarshal([]byte(raw), &targets); err != nil {
		return fmt.Errorf("WORKFLOW_DISPATCH_TARGETS: not a valid JSON array: %w", err)
	}
	if c.WorkflowDispatchEnabled && len(targets) == 0 {
		return errors.New("WORKFLOW_DISPATCH_TARGETS parsed to an empty list while workflow-dispatch-enabled is true")
	}
	for i := range targets {
		t := &targets[i]
		if t.Name == "" {
			return fmt.Errorf("WORKFLOW_DISPATCH_TARGETS[%d]: \"name\" is required", i)
		}
		if t.Workflow == "" {
			return fmt.Errorf("WORKFLOW_DISPATCH_TARGETS[%d] (%s): \"workflow\" is required", i, t.Name)
		}
		switch t.Ref {
		case "":
			t.Ref = "default"
		case "default", "head":
			// explicit, valid
		default:
			// treated as a literal branch/tag name inside this repository
		}
	}
	c.WorkflowDispatchTargets = targets
	return nil
}
