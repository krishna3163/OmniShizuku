package promptbuilder

import (
	"fmt"
	"strings"
)

// This file holds the prompts for the two features whose output isn't a
// triage verdict: Discussions Q&A (a plain-text answer) and the AI agent
// (a structured plan of file changes, or a new issue).

// DiscussionInput is everything needed to answer a question in a GitHub
// Discussion: the discussion's own text plus its comment thread, so the
// model answers what was actually ASKED rather than re-summarizing the title.
type DiscussionInput struct {
	Number   int
	Title    string
	Category string
	Body     string
	Question string // the text after the trigger phrase — the actual ask
	Comments []struct {
		Author string
		Body   string
	}
}

// BuildDiscussionPrompt builds the prompt for answering a discussion
// question. The rules for HOW to answer (tone, refusal policy, what not to
// promise) live in system prompt #4 (discussion_instructions.md) — this only
// supplies the discussion content and the question.
func BuildDiscussionPrompt(in DiscussionInput) string {
	var b strings.Builder

	b.WriteString("# Task: Answer a question in a GitHub Discussion\n\n")
	fmt.Fprintf(&b, "Discussion #%d, category: %s\n", in.Number, orNA(in.Category))
	fmt.Fprintf(&b, "Title: %s\n\n", orNA(in.Title))
	b.WriteString("## Discussion body\n" + orNA(in.Body) + "\n\n")

	if len(in.Comments) > 0 {
		b.WriteString("## Existing comments in this thread\n")
		for _, c := range in.Comments {
			fmt.Fprintf(&b, "\n### @%s\n%s\n", orNA(c.Author), orNA(c.Body))
		}
		b.WriteString("\n")
	}

	b.WriteString("## The question to answer\n" + orNA(in.Question) + "\n\n")
	b.WriteString("## What to do\n")
	b.WriteString("1. Read the discussion, its comments, and the question above.\n")
	b.WriteString("2. Answer the question directly, following the rules in your instructions.\n")
	b.WriteString("3. Return your answer as plain text (not JSON) — it is posted verbatim as the reply.\n")
	b.WriteString("4. If the question is out of scope, unanswerable, or asks you to do something your instructions forbid, say so briefly and explain what would be needed instead.\n\n")
	b.WriteString("Write your answer in English, regardless of the language the question is written in.\n")
	return b.String()
}

// AgentFileChange is one file the agent proposes to create, modify or
// delete. The model returns a list of these; the bot validates the list
// against its limits BEFORE touching GitHub.
type AgentFileChange struct {
	Path    string `json:"path"`
	Action  string `json:"action"` // "create" | "modify" | "delete"
	Content string `json:"content,omitempty"`
}

// AgentPlan is the model's structured answer for an agent command: either a
// code change (Files non-empty, OpenPR true) or a new issue (IssueTitle
// set). Exactly one mode is used per command — see the prompt below.
type AgentPlan struct {
	Files      []AgentFileChange `json:"files"`
	OpenPR     bool              `json:"open_pr"`
	BranchName string            `json:"branch_name"`
	Title      string            `json:"title"`
	Summary    string            `json:"summary"`
	IssueBody  string            `json:"issue_body,omitempty"`
}

// AgentInput is everything needed to run one agent command.
type AgentInput struct {
	Command        string // the text after the trigger phrase
	TargetIssue    *ReferenceSnippet
	TargetPR       *ReferenceSnippet
	ContextFiles   []FileSnippet
	TreePaths      []string // path-only repo map, when the command cited no files
	MaxOutputFiles int
	MaxOutputBytes int
}

// BuildAgentPrompt builds the prompt for one agent command. The security
// rules (never touch the default branch, never modify the target, what it
// may and may not read) live in system prompt #5 (agent_instructions.md) —
// this supplies the command, its target, the grounding files, and the
// strict JSON shape the bot parses back out.
func BuildAgentPrompt(in AgentInput) string {
	var b strings.Builder

	b.WriteString("# Task: Execute an agent command\n\n")
	b.WriteString("## Command\n" + orNA(in.Command) + "\n\n")

	if in.TargetIssue != nil {
		fmt.Fprintf(&b, "## Target issue\n#%d: %s\n\n%s\n\n", in.TargetIssue.Number, orNA(in.TargetIssue.Title), orNA(in.TargetIssue.Body))
	}
	if in.TargetPR != nil {
		fmt.Fprintf(&b, "## Target pull request\n#%d: %s\n\n%s\n\n", in.TargetPR.Number, orNA(in.TargetPR.Title), orNA(in.TargetPR.Body))
	}

	if len(in.ContextFiles) > 0 {
		b.WriteString("## Files read as grounding context\n")
		for _, f := range in.ContextFiles {
			fmt.Fprintf(&b, "\n### %s\n```\n%s\n```\n", f.Path, orNA(f.Content))
		}
		b.WriteString("\n")
	}

	if len(in.TreePaths) > 0 {
		b.WriteString("## Repository file paths (names only, no content)\n")
		b.WriteString("Use these to decide which files your change touches. Read nothing beyond the grounding files above.\n\n")
		for _, p := range in.TreePaths {
			fmt.Fprintf(&b, "- %s\n", p)
		}
		b.WriteString("\n")
	}

	b.WriteString("## Hard limits for this run\n")
	fmt.Fprintf(&b, "- At most %d files may be created, modified or deleted.\n", in.MaxOutputFiles)
	fmt.Fprintf(&b, "- Total new content across all files must stay under %d bytes.\n", in.MaxOutputBytes)
	b.WriteString("- A proposal that exceeds EITHER limit will be refused outright — there is no partial apply.\n")
	b.WriteString("- Deletes require no content. Creates and modifies require the file's FULL new content.\n\n")

	b.WriteString("## What to do\n")
	b.WriteString("1. Work out the smallest change that genuinely addresses the command.\n")
	b.WriteString("2. Decide the mode: a code change (`open_pr: true`, with `files`) or a new related issue (`open_pr: false`, with `title` + `issue_body`).\n")
	b.WriteString("3. Return STRICTLY valid JSON and nothing else — no markdown wrapper, no text before or after.\n\n")
	b.WriteString("## Response format\n")
	b.WriteString("{\"open_pr\": <true|false>, \"title\": \"<PR or issue title>\", \"summary\": \"<2-4 sentences: what you changed and why>\", \"branch_name\": \"<short lowercase slug, no slashes — the bot prefixes it>\", \"issue_body\": \"<issue markdown, only when open_pr is false>\", \"files\": [{\"path\": \"<repo-relative path>\", \"action\": \"create|modify|delete\", \"content\": \"<full file content for create/modify; omit for delete>\"}]}\n\n")
	b.WriteString("Example (illustration only, don't copy it):\n")
	b.WriteString("{\"open_pr\": true, \"title\": \"Fix nil deref in parser\", \"summary\": \"Added a nil check before dereferencing the token stream, which crashed on empty input.\", \"branch_name\": \"fix-parser-nil\", \"files\": [{\"path\": \"internal/parser/parser.go\", \"action\": \"modify\", \"content\": \"package parser\\n\\n// ... full new file content ...\"}]}\n\n")
	b.WriteString("Write \"title\", \"summary\", \"issue_body\" and all file content in English. Never include git commit messages, never mention that an AI wrote this, and never touch any file outside what the command requires.\n")
	return b.String()
}
