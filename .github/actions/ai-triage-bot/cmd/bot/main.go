// Command bot is the entry point for the AI triage bot in GitHub Actions.
// One run = one event (issue opened / pull_request opened).
package main

import (
	"context"
	"log"
	"time"

	"aitriage/internal/ai"
	"aitriage/internal/config"
	"aitriage/internal/ghclient"
	"aitriage/internal/githubevent"
	"aitriage/internal/triage"
)

func main() {
	log.SetFlags(log.Ltime)

	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Minute)
	defer cancel()

	cfg, err := config.Load()
	if err != nil {
		log.Fatalf("config: %v", err)
	}

	event, err := githubevent.Load(cfg.EventPath)
	if err != nil {
		log.Fatalf("event: %v", err)
	}

	if cfg.RepoOwner == "" {
		cfg.RepoOwner = event.Repository.Owner.Login
	}
	if cfg.RepoName == "" {
		cfg.RepoName = event.Repository.Name
	}
	if cfg.RepoOwner == "" || cfg.RepoName == "" {
		log.Fatalf("could not determine the repository owner/name from the event")
	}

	gh := ghclient.New(cfg.GitHubToken, cfg.MaxAPICalls)
	aiClient := ai.New(cfg.GeminiAPIKeys, cfg.GeminiModel, cfg.GeminiMaxRetries, cfg.GeminiRetryBaseDelay, cfg.GeminiRetryMaxDelay)

	// modGH is the elevated-permission client for real issue deletion and
	// user blocking (see internal/triage/moderation.go for exactly why a
	// second, separate token is needed for this and GITHUB_TOKEN never
	// suffices). Left nil when unconfigured — every moderation code path
	// treats that as "degrade to redact+close+lock, skip the block", not
	// as an error.
	var modGH *ghclient.Client
	if cfg.ModerationToken != "" {
		modGH = ghclient.New(cfg.ModerationToken, cfg.MaxAPICalls)
	}

	log.Printf("event=%s repo=%s/%s model=%s keys=%d maxAPICalls=%d moderationToken=%v",
		cfg.EventName, cfg.RepoOwner, cfg.RepoName, cfg.GeminiModel, len(cfg.GeminiAPIKeys), cfg.MaxAPICalls, modGH != nil)

	var handleErr error
	switch cfg.EventName {
	case "issues":
		handleErr = triage.HandleIssue(ctx, cfg, gh, modGH, aiClient, event)
	case "pull_request", "pull_request_target":
		handleErr = triage.HandlePullRequest(ctx, cfg, gh, modGH, aiClient, event)
	case "issue_comment":
		// A comment on an issue OR a PR. Two independent features can fire
		// here: the agent (needs agent-enabled) and workflow dispatch (needs
		// workflow-dispatch-enabled). Each decides for itself whether the
		// comment matches, so a comment can legitimately trigger neither.
		if err := triage.HandleAgentComment(ctx, cfg, gh, aiClient, event); err != nil {
			handleErr = err
			break
		}
		handleErr = triage.HandleWorkflowDispatch(ctx, cfg, gh, event)
	case "discussion_comment":
		// Discussions Q&A and (optionally) the agent, which also accepts a
		// discussion comment as its trigger surface.
		if err := triage.HandleDiscussionComment(ctx, cfg, gh, aiClient, event); err != nil {
			handleErr = err
			break
		}
		handleErr = triage.HandleAgentComment(ctx, cfg, gh, aiClient, event)
	case "commit_comment":
		handleErr = triage.HandleCommitComment(ctx, cfg, gh, aiClient, event)
	default:
		log.Printf("event %q is not supported, nothing to do", cfg.EventName)
		return
	}

	if handleErr != nil {
		log.Fatalf("triage error: %v", handleErr)
	}
	log.Println("done")
}
