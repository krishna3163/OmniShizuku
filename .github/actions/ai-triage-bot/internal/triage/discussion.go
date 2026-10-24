package triage

import (
	"context"
	"fmt"
	"log"
	"strings"

	"aitriage/internal/ai"
	"aitriage/internal/config"
	"aitriage/internal/ghclient"
	"aitriage/internal/githubevent"
	"aitriage/internal/promptbuilder"
	"aitriage/internal/ratelimit"
	"aitriage/internal/trigger"
)

// HandleDiscussionComment implements the Discussions Q&A feature (see
// action.yml's discussions-enabled).
//
// The bot does nothing to Discussions on its own. It reacts ONLY when an
// authorized collaborator @-mentions the discussion trigger phrase in a
// discussion comment; the text after the phrase is the question, and the bot
// posts ONE answer as a reply.
//
// GitHub has no REST API for Discussions at all, so all reads and the reply
// go through GraphQL (see internal/ghclient/discussions.go).
func HandleDiscussionComment(ctx context.Context, cfg *config.Config, gh *ghclient.Client, aiClient *ai.Client, event *githubevent.Event) error {
	if !cfg.DiscussionsEnabled {
		return nil
	}
	if event.Action != "created" {
		return nil
	}
	if event.Comment == nil || event.Discussion == nil {
		return nil
	}

	question, ok := trigger.Substring(event.Comment.Body, cfg.DiscussionTrigger)
	if !ok {
		return nil
	}
	question = strings.TrimSpace(question)
	if question == "" {
		// The phrase alone isn't a question — nothing to answer, and
		// replying "what?" would just be noise in the thread.
		log.Printf("discussion #%d: trigger present but no question after it — exiting", event.Discussion.Number)
		return nil
	}

	owner, repo := cfg.RepoOwner, cfg.RepoName
	author := event.Comment.User.Login

	// Authorization is a live GitHub permission check on every trigger —
	// "write" or higher (the same bar as the agent). Refusals are silent
	// for the same reason as the other privileged features.
	allowed, reason := authorize(ctx, gh, owner, repo, author, "write", nil)
	if !allowed {
		log.Printf("discussion Q&A refused for @%s: %s", author, reason)
		return nil
	}

	if !ratelimit.Allow(ctx, gh, owner, repo, cfg.DiscussionStatePath, author,
		cfg.DiscussionSpamWindow, cfg.DiscussionSpamThreshold) {
		log.Printf("discussion Q&A refused for @%s: rate limit reached", author)
		return nil
	}

	// Re-read the discussion live: the webhook payload can be stale, and the
	// comment thread is the whole context for what's being asked.
	disc, err := gh.GetDiscussion(ctx, owner, repo, event.Discussion.Number)
	if err != nil {
		return fmt.Errorf("fetching discussion #%d: %w", event.Discussion.Number, err)
	}

	// System prompt #1 (repo context) + system prompt #4 (the Q&A rules).
	p, err := loadPrompts(cfg)
	if err != nil {
		return err
	}
	rules, err := loadFeaturePrompt(cfg.Workspace, cfg.DiscussionInstructionsPath,
		"the Discussions Q&A rules (system prompt #4)")
	if err != nil {
		return err
	}
	system := p.repoContext + "\n\n---\n\n" + rules

	userPrompt := promptbuilder.BuildDiscussionPrompt(buildDiscussionInput(disc, question, cfg.DiscussionMaxComments))
	answer, err := aiClient.GeneratePlain(ctx, system, userPrompt)
	if err != nil {
		return fmt.Errorf("gemini request: %w", err)
	}
	answer = strings.TrimSpace(answer)
	if answer == "" {
		return fmt.Errorf("model returned an empty answer for discussion #%d", disc.Number)
	}

	if err := gh.AddDiscussionComment(ctx, disc.ID,
		formatSpecialComment("🤖 **AI Triage — discussion answer**", answer)); err != nil {
		return fmt.Errorf("posting the reply on discussion #%d: %w", disc.Number, err)
	}
	log.Printf("discussion #%d: answered a question from @%s", disc.Number, author)
	return nil
}

// buildDiscussionInput maps a live Discussion onto the prompt shape, capped
// at cfg.DiscussionMaxComments comments so one huge thread can't blow the
// token budget.
func buildDiscussionInput(disc *ghclient.Discussion, question string, maxComments int) promptbuilder.DiscussionInput {
	in := promptbuilder.DiscussionInput{
		Number:   disc.Number,
		Title:    disc.Title,
		Category: disc.Category.Name,
		Body:     disc.Body,
		Question: question,
	}
	comments := disc.Comments
	if maxComments > 0 && len(comments) > maxComments {
		comments = comments[:maxComments]
	}
	for _, c := range comments {
		in.Comments = append(in.Comments, struct {
			Author string
			Body   string
		}{Author: c.Author, Body: c.Body})
	}
	return in
}
