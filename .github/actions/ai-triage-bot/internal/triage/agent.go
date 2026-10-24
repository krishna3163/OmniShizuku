package triage

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"sort"
	"strings"
	"time"

	"aitriage/internal/ai"
	"aitriage/internal/config"
	"aitriage/internal/ghclient"
	"aitriage/internal/githubevent"
	"aitriage/internal/promptbuilder"
	"aitriage/internal/ratelimit"
	"aitriage/internal/trigger"
)

// HandleAgentComment implements the AI-agent feature (see action.yml's
// agent-enabled): an authorized collaborator points the bot at a target
// issue/PR, and it either opens a NEW pull request with a proposed change or
// files a NEW related issue.
//
// Two hard guarantees, enforced in code and not left to the model:
//   - Nothing is ever auto-merged. Every code change is a fresh PR for a
//     human to review, on a branch the agent created for that purpose.
//   - The target issue/PR is never modified. The agent only reads it.
func HandleAgentComment(ctx context.Context, cfg *config.Config, gh *ghclient.Client, aiClient *ai.Client, event *githubevent.Event) error {
	if !cfg.AgentEnabled {
		return nil
	}
	if event.Action != "created" {
		return nil
	}
	if event.Comment == nil {
		return nil
	}
	if event.Issue == nil && event.Discussion == nil {
		return nil
	}

	command, ok := trigger.SubstringOrFlexible(event.Comment.Body, cfg.AgentTrigger)
	if !ok {
		return nil
	}
	command = strings.TrimSpace(command)
	if command == "" {
		log.Printf("agent: trigger present but no command after it — exiting")
		return nil
	}

	owner, repo := cfg.RepoOwner, cfg.RepoName
	author := event.Comment.User.Login

	// Same live write-level bar as Discussions. Refusals are silent.
	allowed, reason := authorize(ctx, gh, owner, repo, author, "write", cfg.AgentAllowedUsers)
	if !allowed {
		log.Printf("agent refused for @%s: %s", author, reason)
		return nil
	}

	if !ratelimit.Allow(ctx, gh, owner, repo, cfg.AgentStatePath, author,
		cfg.AgentSpamWindow, cfg.AgentSpamThreshold) {
		log.Printf("agent refused for @%s: rate limit reached", author)
		return nil
	}

	// System prompt #1 (repo context) + system prompt #5 (the agent's
	// security/behavior rules). A missing rules file is a hard error.
	p, err := loadPrompts(cfg)
	if err != nil {
		return err
	}
	rules, err := loadFeaturePrompt(cfg.Workspace, cfg.AgentInstructionsPath,
		"the agent's rules (system prompt #5)")
	if err != nil {
		return err
	}
	system := p.repoContext + "\n\n---\n\n" + rules

	in := promptbuilder.AgentInput{
		Command:        command,
		MaxOutputFiles: cfg.AgentMaxOutputFiles,
		MaxOutputBytes: cfg.AgentMaxOutputBytes,
	}

	// A target issue/PR cited in the command is fetched in full so the
	// model works from what it actually says, not a bare number.
	if n := firstIssueRef(command); n > 0 {
		if issue, err := gh.GetIssue(ctx, owner, repo, n); err == nil {
			in.TargetIssue = refSnippetOf(issue)
		} else {
			log.Printf("agent: could not fetch cited issue #%d: %v", n, err)
		}
	}
	if in.TargetIssue == nil {
		// No explicit target: if the command is ON an issue/PR, that is the
		// most likely target.
		if event.Issue != nil && event.Issue.Number > 0 {
			if issue, err := gh.GetIssue(ctx, owner, repo, event.Issue.Number); err == nil {
				in.TargetIssue = refSnippetOf(issue)
			}
		}
	}

	// Grounding: the files the command names, or — when it names none and
	// the model has to guess — a bounded path-only map of the repo.
	paths := extractFilePaths(command)
	in.ContextFiles, _ = fetchFiles(ctx, gh, owner, repo, "", paths, cfg.AgentMaxContextFiles, cfg.MaxFileBytes)
	if len(in.ContextFiles) == 0 {
		in.TreePaths = agentTreePaths(ctx, gh, owner, repo, cfg.AgentMaxTreeEntries)
	}
	log.Printf("agent: command from @%s, %d grounding file(s), %d tree path(s), target=#%d",
		author, len(in.ContextFiles), len(in.TreePaths), issueNumOf(in.TargetIssue))

	raw, err := aiClient.GenerateWithOpts(ctx, system, promptbuilder.BuildAgentPrompt(in), ai.Options{
		MaxOutputTokens: cfg.AgentMaxOutputTokens,
	})
	if err != nil {
		return fmt.Errorf("gemini request: %w", err)
	}
	plan, err := parseAgentPlan(raw)
	if err != nil {
		return fmt.Errorf("parsing gemini response: %w", err)
	}

	// Validate the plan against the hard limits BEFORE any write. A proposal
	// over either limit is refused outright — no partial PR, no truncation.
	if err := validateAgentPlan(plan, cfg); err != nil {
		log.Printf("agent: refusing plan from @%s: %v", author, err)
		return replyAgentRefused(ctx, gh, owner, repo, event, author, err.Error())
	}

	if plan.OpenPR {
		return applyAgentPR(ctx, cfg, gh, owner, repo, plan, event, author)
	}
	return applyAgentIssue(ctx, gh, owner, repo, plan, event, author)
}

// applyAgentPR puts the generated files on a NEW branch and opens a PR. It
// never writes to the default branch, and it never touches the target.
func applyAgentPR(ctx context.Context, cfg *config.Config, gh *ghclient.Client, owner, repo string, plan *promptbuilder.AgentPlan, event *githubevent.Event, author string) error {
	baseSHA, err := gh.GetDefaultBranchSHA(ctx, owner, repo)
	if err != nil {
		return fmt.Errorf("resolving the default branch HEAD: %w", err)
	}

	branch := ghclient.SanitizeBranchName(plan.BranchName)
	if branch == "" {
		branch = "change"
	}
	// A timestamp suffix makes the branch name unique per run, so two agent
	// runs never collide on the same ref (which would be a hard 422).
	branch = fmt.Sprintf("%s%s-%d", cfg.AgentBranchPrefix, branch, time.Now().UnixNano())

	if err := gh.CreateBranch(ctx, owner, repo, branch, baseSHA); err != nil {
		return fmt.Errorf("creating branch %q: %w", branch, err)
	}

	for _, f := range plan.Files {
		switch strings.ToLower(strings.TrimSpace(f.Action)) {
		case "delete":
			_, sha, found, err := gh.GetFileContentWithSHA(ctx, owner, repo, f.Path, branch)
			if err != nil {
				return fmt.Errorf("reading %q to delete it: %w", f.Path, err)
			}
			if !found {
				log.Printf("agent: skipping delete of %q — it does not exist on %q", f.Path, branch)
				continue
			}
			if err := gh.DeleteFileOnBranch(ctx, owner, repo, branch, f.Path, sha,
				agentCommitMessage(plan.Title, author)); err != nil {
				return fmt.Errorf("deleting %q on %q: %w", f.Path, branch, err)
			}
		default: // "create" and "modify" are both a full-content write
			if err := gh.PutFileOnBranch(ctx, owner, repo, branch, f.Path,
				agentCommitMessage(plan.Title, author), f.Content); err != nil {
				return fmt.Errorf("writing %q on %q: %w", f.Path, branch, err)
			}
		}
	}

	pr, err := gh.CreatePullRequest(ctx, owner, repo, plan.Title, branch, defaultBranchName(ctx, gh, owner, repo),
		agentPRBody(plan, author, event))
	if err != nil {
		return fmt.Errorf("opening the pull request: %w", err)
	}
	log.Printf("agent: opened PR #%d on branch %q for @%s", pr.Number, branch, author)
	return replyAgentPR(ctx, gh, owner, repo, event, author, plan, pr)
}

// applyAgentIssue files a NEW related issue. Again: the target is never
// modified, and nothing is auto-merged (there is nothing to merge here).
func applyAgentIssue(ctx context.Context, gh *ghclient.Client, owner, repo string, plan *promptbuilder.AgentPlan, event *githubevent.Event, author string) error {
	issue, err := gh.CreateIssue(ctx, owner, repo, plan.Title,
		agentIssueBody(plan, author, event))
	if err != nil {
		return fmt.Errorf("filing the issue: %w", err)
	}
	log.Printf("agent: filed issue #%d for @%s", issue.Number, author)
	return replyAgentIssue(ctx, gh, owner, repo, event, author, plan, issue)
}

func parseAgentPlan(raw string) (*promptbuilder.AgentPlan, error) {
	var plan promptbuilder.AgentPlan
	if err := json.Unmarshal([]byte(stripJSONFences(raw)), &plan); err != nil {
		return nil, fmt.Errorf("failed to parse the model's JSON plan: %w (raw response: %s)", err, truncateForLog(raw))
	}
	plan.Title = strings.TrimSpace(plan.Title)
	if plan.Title == "" {
		return nil, fmt.Errorf("model returned an empty title (raw response: %s)", truncateForLog(raw))
	}
	// Normalizes each file's action and drops entries with no path.
	var files []promptbuilder.AgentFileChange
	for _, f := range plan.Files {
		f.Path = strings.TrimSpace(f.Path)
		f.Action = strings.ToLower(strings.TrimSpace(f.Action))
		if f.Path == "" {
			continue
		}
		switch f.Action {
		case "create", "modify", "delete":
		default:
			return nil, fmt.Errorf("file %q has an unknown action %q (want create, modify or delete)", f.Path, f.Action)
		}
		files = append(files, f)
	}
	plan.Files = files
	return &plan, nil
}

// validateAgentPlan enforces the two hard limits declared in action.yml. A
// breach is refused outright rather than truncated: a half-applied change is
// worse than none.
func validateAgentPlan(plan *promptbuilder.AgentPlan, cfg *config.Config) error {
	if plan.OpenPR && len(plan.Files) == 0 {
		return fmt.Errorf("the plan asked for a pull request but proposed no file changes")
	}
	if !plan.OpenPR && strings.TrimSpace(plan.IssueBody) == "" {
		return fmt.Errorf("the plan asked for a new issue but provided no body")
	}
	if len(plan.Files) > cfg.AgentMaxOutputFiles {
		return fmt.Errorf("the plan touches %d files, above the %d-file limit", len(plan.Files), cfg.AgentMaxOutputFiles)
	}
	total := 0
	for _, f := range plan.Files {
		if f.Action == "delete" {
			continue
		}
		total += len(f.Content)
	}
	if total > cfg.AgentMaxOutputBytes {
		return fmt.Errorf("the plan's new content is %d bytes, above the %d-byte limit", total, cfg.AgentMaxOutputBytes)
	}
	return nil
}

// agentTreePaths returns a bounded, sorted list of repo file paths — the
// cheap map the model gets when a command cites no specific file.
func agentTreePaths(ctx context.Context, gh *ghclient.Client, owner, repo string, max int) []string {
	branch, err := gh.GetDefaultBranch(ctx, owner, repo)
	if err != nil || branch == "" {
		branch = "main"
	}
	entries, err := gh.ListTreePaths(ctx, owner, repo, branch, max)
	if err != nil {
		log.Printf("agent: could not read the repo tree: %v", err)
		return nil
	}
	out := make([]string, 0, len(entries))
	for _, e := range entries {
		out = append(out, e.Path)
	}
	sort.Strings(out)
	return out
}

// firstIssueRef finds the first "#123" cross-reference in the command text.
func firstIssueRef(text string) int {
	refs := extractIssueRefs(text, 0, 1)
	if len(refs) == 0 {
		return 0
	}
	return refs[0]
}

func refSnippetOf(issue *ghclient.Issue) *promptbuilder.ReferenceSnippet {
	return &promptbuilder.ReferenceSnippet{
		Number: issue.Number,
		Title:  issue.Title,
		Body:   issue.Body,
		State:  issue.State,
		IsPR:   issue.PullRequest != nil,
	}
}

func issueNumOf(r *promptbuilder.ReferenceSnippet) int {
	if r == nil {
		return 0
	}
	return r.Number
}

func defaultBranchName(ctx context.Context, gh *ghclient.Client, owner, repo string) string {
	name, err := gh.GetDefaultBranch(ctx, owner, repo)
	if err != nil || name == "" {
		return "main"
	}
	return name
}

func agentCommitMessage(title, author string) string {
	return fmt.Sprintf("ai-triage-bot: %s (proposed for @%s)", title, author)
}

func agentPRBody(plan *promptbuilder.AgentPlan, author string, event *githubevent.Event) string {
	var b strings.Builder
	fmt.Fprintf(&b, "This pull request was **proposed by the AI triage bot** in response to a command from @%s. It is NOT auto-merged — please review before merging.\n\n", author)
	b.WriteString(strings.TrimSpace(plan.Summary))
	b.WriteString("\n\n## Files changed\n")
	for _, f := range plan.Files {
		fmt.Fprintf(&b, "\n- `%s` (%s)", f.Path, f.Action)
	}
	if src := agentSourceRef(event); src != "" {
		fmt.Fprintf(&b, "\n\nTriggered from %s.", src)
	}
	return b.String()
}

func agentIssueBody(plan *promptbuilder.AgentPlan, author string, event *githubevent.Event) string {
	var b strings.Builder
	b.WriteString(strings.TrimSpace(plan.IssueBody))
	b.WriteString("\n\n---\n")
	fmt.Fprintf(&b, "*This issue was **filed by the AI triage bot** after a command from @%s. It was not reviewed by a human yet.*", author)
	if src := agentSourceRef(event); src != "" {
		fmt.Fprintf(&b, " Triggered from %s.", src)
	}
	return b.String()
}

// agentSourceRef names the issue/PR/discussion the command came from, so the
// new PR/issue links back to where it was asked for.
func agentSourceRef(event *githubevent.Event) string {
	if event.Issue != nil && event.Issue.Number > 0 {
		return fmt.Sprintf("issue/PR #%d", event.Issue.Number)
	}
	if event.Discussion != nil && event.Discussion.Number > 0 {
		return fmt.Sprintf("discussion #%d", event.Discussion.Number)
	}
	return ""
}

func replyAgentPR(ctx context.Context, gh *ghclient.Client, owner, repo string, event *githubevent.Event, author string, plan *promptbuilder.AgentPlan, pr *ghclient.PullRequest) error {
	var b strings.Builder
	fmt.Fprintf(&b, "@%s: opened [#%d](#%d) — %s\n\n%s\n\n**Nothing is auto-merged.** The target above was only read, never modified. The new PR is yours to review.", author, pr.Number, pr.Number, plan.Title, strings.TrimSpace(plan.Summary))
	return gh.CreateComment(ctx, owner, repo, sourceNumber(event), formatSpecialComment("🤖 **AI Triage — agent opened a pull request**", b.String()))
}

func replyAgentIssue(ctx context.Context, gh *ghclient.Client, owner, repo string, event *githubevent.Event, author string, plan *promptbuilder.AgentPlan, issue *ghclient.Issue) error {
	var b strings.Builder
	fmt.Fprintf(&b, "@%s: filed [#%d](#%d) — %s\n\nThe target above was only read, never modified.", author, issue.Number, issue.Number, plan.Title)
	return gh.CreateComment(ctx, owner, repo, sourceNumber(event), formatSpecialComment("🤖 **AI Triage — agent filed an issue**", b.String()))
}

// replyAgentRefused tells the author their proposal was rejected before any
// write happened, and why. Unlike the other features this IS answered: the
// trigger was authorized, so silence would look like a broken bot rather
// than a deliberate refusal.
func replyAgentRefused(ctx context.Context, gh *ghclient.Client, owner, repo string, event *githubevent.Event, author, reason string) error {
	body := fmt.Sprintf("@%s: I did not apply that change — it was refused before anything was written.\n\nReason: %s\n\nNothing was committed and no pull request was opened.", author, reason)
	return gh.CreateComment(ctx, owner, repo, sourceNumber(event), formatSpecialComment("🤖 **AI Triage — agent refused the plan**", body))
}

func sourceNumber(event *githubevent.Event) int {
	if event.Issue != nil {
		return event.Issue.Number
	}
	return 0
}
