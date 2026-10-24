package triage

import (
	"context"
	"fmt"
	"log"
	"strings"

	"aitriage/internal/config"
	"aitriage/internal/ghclient"
	"aitriage/internal/githubevent"
	"aitriage/internal/ratelimit"
	"aitriage/internal/trigger"
)

// HandleWorkflowDispatch implements the owner-only "run a workflow by
// comment" feature (see action.yml's workflow-dispatch-enabled).
//
// No AI is involved anywhere in this path: the comment text is matched
// against the configured trigger phrase and keywords, and the matched
// target's pre-declared workflow + inputs are dispatched verbatim. The bot
// never writes, edits, or infers the contents of the workflow that runs.
//
// Unlike every other feature, a refused trigger deliberately produces NO
// comment — a reply to an unauthorized author would turn any comment into a
// channel that makes the bot run workflows on demand. Refusals are logged.
func HandleWorkflowDispatch(ctx context.Context, cfg *config.Config, gh *ghclient.Client, event *githubevent.Event) error {
	if !cfg.WorkflowDispatchEnabled {
		return nil
	}
	if event.Action != "created" {
		return nil
	}
	if event.Comment == nil || event.Issue == nil {
		return nil
	}
	if len(cfg.WorkflowDispatchTargets) == 0 {
		// Unreachable (Load refuses to start in this state) — kept as a
		// guard so a future refactor can't dispatch into a void silently.
		return nil
	}

	body := event.Comment.Body
	rest, ok := trigger.Substring(body, cfg.WorkflowDispatchTrigger)
	if !ok {
		return nil
	}

	owner, repo := cfg.RepoOwner, cfg.RepoName
	author := event.Comment.User.Login

	// Authorization FIRST, before the rate-limit write: a refused author
	// must not even be able to make the bot touch the state file.
	allowed, reason := authorize(ctx, gh, owner, repo, author, "admin", cfg.WorkflowDispatchAllowedUsers)
	if !allowed {
		log.Printf("workflow dispatch refused for @%s: %s", author, reason)
		return nil
	}

	if !ratelimit.Allow(ctx, gh, owner, repo, cfg.WorkflowDispatchStatePath, author,
		cfg.WorkflowDispatchSpamWindow, cfg.WorkflowDispatchSpamThreshold) {
		log.Printf("workflow dispatch refused for @%s: rate limit reached", author)
		return nil
	}

	target, ok := pickDispatchTarget(cfg.WorkflowDispatchTargets, rest)
	if !ok {
		// Authorized but no target matched. The owner needs to know why
		// nothing happened, so this IS answered (unlike a refusal) — but
		// with a list of what the bot understands, not a guess.
		log.Printf("workflow dispatch by @%s: no target matched after the trigger phrase (text: %q)", author, strings.TrimSpace(rest))
		return replyNoTarget(ctx, gh, owner, repo, event.Issue.Number, cfg.WorkflowDispatchTargets, author)
	}

	// Resolve "head" against the actual PR. A plain issue, or a PR whose
	// head lives in a fork, cannot be dispatched against (workflow_dispatch
	// only reaches refs inside THIS repository) — fall back to the default
	// branch and say so in the reply.
	ref, refNote := resolveDispatchRef(ctx, gh, owner, repo, target.Ref, event)

	inputs := substituteInputs(target.Inputs, map[string]string{
		"{{pr_number}}":    fmt.Sprintf("%d", event.Issue.Number),
		"{{ref}}":          ref,
		"{{triggered_by}}": author,
	})

	if err := gh.DispatchWorkflow(ctx, owner, repo, target.Workflow, ref, inputs); err != nil {
		return fmt.Errorf("dispatching workflow %q on ref %q: %w", target.Workflow, ref, err)
	}
	log.Printf("workflow dispatch by @%s: ran target %q (%s) on ref %q", author, target.Name, target.Workflow, ref)

	return replyDispatchOK(ctx, gh, owner, repo, event.Issue.Number, target, ref, refNote, author)
}

// pickDispatchTarget selects the target the comment means. With exactly one
// configured target, keywords are optional (the trigger phrase alone is
// enough). With more than one, keywords are REQUIRED and are matched
// case-insensitively as substrings against the text after the trigger
// phrase. The first target whose keyword matches wins, in declaration order.
func pickDispatchTarget(targets []config.WorkflowDispatchTarget, rest string) (config.WorkflowDispatchTarget, bool) {
	if len(targets) == 0 {
		return config.WorkflowDispatchTarget{}, false
	}
	if len(targets) == 1 {
		return targets[0], true
	}
	for _, t := range targets {
		for _, kw := range t.Keywords {
			if kw == "" {
				continue
			}
			if strings.Contains(strings.ToLower(rest), strings.ToLower(kw)) {
				return t, true
			}
		}
	}
	return config.WorkflowDispatchTarget{}, false
}

// resolveDispatchRef turns a target's configured ref into the concrete ref
// to dispatch on, plus an optional note for the reply explaining a fallback.
func resolveDispatchRef(ctx context.Context, gh *ghclient.Client, owner, repo, configured string, event *githubevent.Event) (ref, note string) {
	switch configured {
	case "", "default":
		ref = event.Repository.DefaultBranch
		if ref == "" {
			var err error
			if ref, err = gh.GetDefaultBranch(ctx, owner, repo); err != nil {
				log.Printf("workflow dispatch: could not resolve the default branch: %v", err)
			}
		}
		return ref, ""
	case "head":
		// "head" only means something when the comment is on a PR.
		if event.Issue.PullRequest == nil {
			fb := event.Repository.DefaultBranch
			return fb, fmt.Sprintf("`ref: \"head\"` was requested, but this is a plain issue rather than a pull request, so the dispatch ran on the default branch (`%s`).", fb)
		}
		pr, err := gh.GetPullRequest(ctx, owner, repo, event.Issue.Number)
		if err != nil {
			log.Printf("workflow dispatch: could not fetch PR #%d to resolve the head ref: %v", event.Issue.Number, err)
			fb := event.Repository.DefaultBranch
			return fb, fmt.Sprintf("the PR's head branch could not be resolved, so the dispatch ran on the default branch (`%s`).", fb)
		}
		// workflow_dispatch can only target a ref inside THIS repository:
		// a fork PR's head branch does not exist here.
		if pr.Head.Repo == nil || !strings.EqualFold(pr.Head.Repo.FullName, owner+"/"+repo) {
			fb := event.Repository.DefaultBranch
			return fb, fmt.Sprintf("PR #%d's head branch lives in a fork (`%s`), which this repository's workflow_dispatch API cannot reach, so the dispatch ran on the default branch (`%s`).", event.Issue.Number, pr.Head.Repo.FullName, fb)
		}
		return pr.Head.Ref, ""
	default:
		// A literal branch/tag name inside this repository.
		return configured, ""
	}
}

// substituteInputs replaces the documented placeholders in each input value.
// Only the three documented placeholders are substituted; any other "{{...}}"
// is left alone so a workflow that legitimately wants that literal text still
// gets it.
func substituteInputs(inputs map[string]string, subs map[string]string) map[string]string {
	if len(inputs) == 0 {
		return nil
	}
	out := make(map[string]string, len(inputs))
	for k, v := range inputs {
		for placeholder, value := range subs {
			v = strings.ReplaceAll(v, placeholder, value)
		}
		out[k] = v
	}
	return out
}

// replyDispatchOK posts the confirmation comment naming what ran, on which
// ref, and (when a fallback happened) why.
func replyDispatchOK(ctx context.Context, gh *ghclient.Client, owner, repo string, number int, target config.WorkflowDispatchTarget, ref, refNote, author string) error {
	var b strings.Builder
	fmt.Fprintf(&b, "@%s: dispatched workflow `%s` (target **%s**) on ref `%s`.", author, target.Workflow, target.Name, ref)
	if refNote != "" {
		fmt.Fprintf(&b, "\n\n%s", refNote)
	}
	b.WriteString("\n\nThe run appears in this repository's Actions tab. This is a manual dispatch — the bot did not inspect or author anything that workflow will do.")
	return gh.CreateComment(ctx, owner, repo, number, formatSpecialComment("🤖 **AI Triage — workflow dispatched**", b.String()))
}

// replyNoTarget tells an authorized author that nothing matched, and lists
// the targets that ARE configured so they can correct the wording.
func replyNoTarget(ctx context.Context, gh *ghclient.Client, owner, repo string, number int, targets []config.WorkflowDispatchTarget, author string) error {
	var b strings.Builder
	fmt.Fprintf(&b, "@%s: I couldn't match that comment to any configured workflow target, so nothing was dispatched.", author)
	b.WriteString("\n\nConfigured targets:\n")
	for _, t := range targets {
		if len(t.Keywords) == 0 {
			fmt.Fprintf(&b, "\n- **%s** → `%s` (no keywords; trigger phrase alone selects it)", t.Name, t.Workflow)
			continue
		}
		fmt.Fprintf(&b, "\n- **%s** → `%s` (keywords: %s)", t.Name, t.Workflow, strings.Join(t.Keywords, ", "))
	}
	b.WriteString("\n\nTo run one, include the trigger phrase followed by the target's name or one of its keywords.")
	return gh.CreateComment(ctx, owner, repo, number, formatSpecialComment("🤖 **AI Triage — no matching workflow**", b.String()))
}
