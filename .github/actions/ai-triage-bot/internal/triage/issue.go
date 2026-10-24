package triage

import (
	"context"
	"fmt"
	"log"
	"time"

	"aitriage/internal/ai"
	"aitriage/internal/antispam"
	"aitriage/internal/config"
	"aitriage/internal/duplicate"
	"aitriage/internal/ghclient"
	"aitriage/internal/githubevent"
	"aitriage/internal/laplace"
	"aitriage/internal/promptbuilder"
)

// HandleIssue is the entry point for "issues" events. modGH is the
// (optional, may be nil) elevated-permission client used only for real
// issue deletion and user blocking when the AI flags a moderation
// violation — see internal/triage/moderation.go.
func HandleIssue(ctx context.Context, cfg *config.Config, gh, modGH *ghclient.Client, aiClient *ai.Client, event *githubevent.Event) error {
	if event.Issue == nil {
		return fmt.Errorf("issues event payload is missing the issue field")
	}
	if event.Action != "opened" {
		log.Printf("issue #%d: action=%q, the bot only reacts to 'opened' — exiting", event.Issue.Number, event.Action)
		return nil
	}

	owner, repo := cfg.RepoOwner, cfg.RepoName
	number := event.Issue.Number
	author := event.Issue.User.Login

	// 1. Antispam — the cheapest, highest-priority check; the AI isn't called at all here.
	spamRes, err := antispam.Check(ctx, gh, owner, repo, author, cfg.SpamWindow, cfg.SpamThreshold)
	if err != nil {
		return fmt.Errorf("antispam check: %w", err)
	}
	if spamRes.IsSpam {
		log.Printf("author @%s: %d issues in %s (threshold %d) — treating as spam", author, spamRes.TotalCount, cfg.SpamWindow, cfg.SpamThreshold)
		return closeAsSpam(ctx, gh, owner, repo, spamRes.IssueNumbers, author, cfg.SpamThreshold, cfg.SpamWindow)
	}

	// 2. Idempotency — if the bot already commented, do nothing further.
	done, err := alreadyProcessed(ctx, gh, owner, repo, number)
	if err != nil {
		return fmt.Errorf("checking prior comments: %w", err)
	}
	if done {
		log.Printf("issue #%d was already processed by the bot — exiting", number)
		return nil
	}

	// 3. Load system prompts + label config.
	p, err := loadPrompts(cfg)
	if err != nil {
		return err
	}
	issueLabels := filterLabels(p.labels, "issue")

	// 4. Fetch fresh issue data (the webhook payload can be a second stale).
	issue, err := gh.GetIssue(ctx, owner, repo, number)
	if err != nil {
		return fmt.Errorf("fetching issue #%d: %w", number, err)
	}

	// 5. Anti-abuse: an issue that references an excessive number of file
	// paths is very likely trying to make the bot hammer the GitHub Contents
	// API rather than reporting a genuine bug — skip the AI entirely and
	// label it mechanically, the same way antispam does above.
	paths := extractFilePaths(issue.Body)
	if len(paths) > cfg.MaxExtractedPaths {
		log.Printf("issue #%d: body references %d file paths (limit %d) — treating as abuse, skipping AI", number, len(paths), cfg.MaxExtractedPaths)
		return closeAsTroll(ctx, gh, owner, repo, number, len(paths), cfg.MaxExtractedPaths)
	}

	// 6. Economical code reading: only the files the author pointed to
	// (your issue template requires citing a file), capped at MaxFiles.
	// Paths that were cited but couldn't be read are tracked separately
	// (failedFiles) so the model is told about the failed citation instead
	// of treating it the same as "nothing was cited".
	files, failedFiles := fetchFiles(ctx, gh, owner, repo, "", paths, cfg.MaxFiles, cfg.MaxFileBytes)
	log.Printf("issue #%d: found %d paths in the text, read %d files, %d failed", number, len(paths), len(files), len(failedFiles))

	// 6b. Cross-referenced issues/PRs (e.g. "duplicate of #42") — resolved
	// so the model can check the claim against what #42 actually says.
	refNumbers := extractIssueRefs(issue.Body, number, cfg.MaxReferencedIssues)
	references := fetchReferences(ctx, gh, owner, repo, refNumbers)
	if len(references) > 0 {
		log.Printf("issue #%d: resolved %d referenced issue(s)/PR(s)", number, len(references))
	}

	// 7. Author attachments (screenshots/log files) — size-limited, host
	// whitelisted to GitHub, and content verified by magic bytes. Anything
	// that fails that screening is NOT read — rejectedAtts carries why, so
	// the model is told explicitly and judges those cases by text alone
	// (see writeAttachmentsSection).
	imageAtts, textAtts, rejectedAtts := fetchBodyAttachments(ctx, cfg, issue.Body)

	// 8. Laplace factor — heuristic context about the author (see internal/laplace).
	factor := laplace.Compute(ctx, gh, laplace.Input{
		Owner: owner, Repo: repo, Author: author,
		AuthorType: event.Issue.User.Type, Body: issue.Body, IsPR: false,
	})
	log.Printf("issue #%d: laplace factor = %d/100 (%s), %d signals", number, factor.Score, factor.Level(), len(factor.Signals))

	// 8b. Duplicate shortlist (ON by default) — a cheap LOCAL keyword
	// search, no AI: it only decides which candidates the model gets to
	// compare full text against. See internal/duplicate.
	var dupCandidates []promptbuilder.DuplicateCandidate
	if cfg.DuplicateDetectionEnabled {
		dupCandidates = duplicate.Shortlist(ctx, gh, owner, repo, issue.Title, duplicate.Options{
			SelfNumber:    number,
			IsPR:          false,
			MaxKeywords:   cfg.DuplicateSearchKeywords,
			MaxCandidates: cfg.MaxDuplicateCandidates,
			MinSimilarity: cfg.DuplicateMinSimilarity,
			MaxBodyChars:  cfg.DuplicateMaxBodyChars,
		})
		if len(dupCandidates) > 0 {
			log.Printf("issue #%d: %d possible duplicate(s) shortlisted", number, len(dupCandidates))
		}
	}

	// 9. Ask the AI.
	userPrompt := promptbuilder.BuildIssuePrompt(promptbuilder.IssueInput{
		Number:              number,
		Title:               issue.Title,
		Body:                issue.Body,
		Author:              author,
		Files:               files,
		FailedFiles:         failedFiles,
		References:          references,
		AllowedLabels:       issueLabels,
		DuplicateCandidates: dupCandidates,
		LaplaceFactor:       factor.PromptBlock(),
		AttachedImages:      len(imageAtts),
		AttachedTexts:       textAtts,
		RejectedAttachments: rejectedAtts,
	})
	raw, err := aiClient.GenerateWithImages(ctx, p.systemPrompt(), userPrompt, imageAtts)
	if err != nil {
		return fmt.Errorf("gemini request: %w", err)
	}
	verdict, err := parseVerdict(raw)
	if err != nil {
		return fmt.Errorf("parsing gemini response: %w", err)
	}

	// 9b. Moderation takes priority over the normal label/comment flow:
	// doxxing or severe targeted abuse in the content gets it removed
	// (and the author blocked on a repeat offense) instead of labeled.
	if verdict.Moderation != nil && verdict.Moderation.Violation {
		return handleModerationViolation(ctx, cfg, gh, modGH, owner, repo, number, author, issue.Title, issue.NodeID, false, verdict.Moderation)
	}

	// 9c. Duplicate comes next: the model compared full text against the
	// shortlisted candidates and matched one — label, explain, close.
	// Junk is excluded on purpose: trash is a quality verdict, and closing
	// spam "as a duplicate of" another report would be nonsense.
	if verdict.Duplicate != nil && verdict.Duplicate.IsDuplicate && verdict.Verdict != "trash" {
		return handleDuplicate(ctx, gh, owner, repo, number, verdict, false)
	}

	// 10. Apply the label and comment.
	finalLabel := resolveLabel(verdict, issueLabels)
	labelDef := findLabelDef(p.labels, finalLabel)
	if err := gh.EnsureLabel(ctx, owner, repo, labelDef.Name, labelDef.Color, labelDef.Description); err != nil {
		return fmt.Errorf("creating label %q: %w", labelDef.Name, err)
	}
	if err := gh.AddLabels(ctx, owner, repo, number, []string{finalLabel}); err != nil {
		return fmt.Errorf("applying label %q to issue #%d: %w", finalLabel, number, err)
	}

	comment := formatComment(verdict.Comment, verdict.Verdict, attachmentScreeningNote(rejectedAtts))
	if err := gh.CreateComment(ctx, owner, repo, number, comment); err != nil {
		return fmt.Errorf("posting comment on issue #%d: %w", number, err)
	}

	log.Printf("issue #%d processed: verdict=%s label=%s", number, verdict.Verdict, finalLabel)
	return nil
}

// closeAsSpam labels, comments on, and closes all of the author's open
// issues from the current burst. The AI is never called here — it's a
// purely mechanical defense.
func closeAsSpam(ctx context.Context, gh *ghclient.Client, owner, repo string, numbers []int, author string, threshold int, window time.Duration) error {
	if len(numbers) == 0 {
		log.Printf("antispam triggered for @%s, but no open issues found in the window (maybe already closed) — nothing to do", author)
		return nil
	}

	if err := gh.EnsureLabel(ctx, owner, repo, "spam", "5319e7", "Flooding: too many issues from the same author in a short time"); err != nil {
		log.Printf("warning: failed to create the spam label: %v", err)
	}

	comment := formatComment(
		fmt.Sprintf(
			"@%s opened more than %d issues in the last %s — treated as flooding/spam, so this issue was closed automatically without AI analysis.\n\nIf this is a mistake: open a single new issue describing one specific problem, and it will be reviewed.",
			author, threshold, humanizeDuration(window),
		),
		"trash",
	)

	for _, n := range numbers {
		done, err := alreadyProcessed(ctx, gh, owner, repo, n)
		if err == nil && done {
			continue // already handled this issue on a previous run
		}
		if err := gh.AddLabels(ctx, owner, repo, n, []string{"spam"}); err != nil {
			log.Printf("warning: failed to add the spam label to issue #%d: %v", n, err)
		}
		if err := gh.CreateComment(ctx, owner, repo, n, comment); err != nil {
			log.Printf("warning: failed to comment on issue #%d: %v", n, err)
		}
		if err := gh.CloseIssue(ctx, owner, repo, n, "not_planned"); err != nil {
			log.Printf("warning: failed to close issue #%d: %v", n, err)
		}
	}
	return nil
}

// closeAsTroll labels an issue "trash" without ever calling the AI, because
// its body references a suspiciously large number of file paths — a
// pattern much more consistent with trying to exhaust the bot's GitHub API
// budget than with a genuine bug report. Unlike closeAsSpam this doesn't
// close the issue, since a human may still want to take a second look.
func closeAsTroll(ctx context.Context, gh *ghclient.Client, owner, repo string, number, pathCount, limit int) error {
	if err := gh.EnsureLabel(ctx, owner, repo, "trash", "b60205", "Junk: spam, ads, abuse, or automated noise"); err != nil {
		log.Printf("warning: failed to create the trash label: %v", err)
	}

	comment := formatComment(
		fmt.Sprintf(
			"This issue references %d file paths, well above the %d-path limit for a single triage run. That pattern looks like an attempt to make the bot hammer the GitHub API rather than a genuine bug report, so it's closed as trash without AI analysis.\n\nIf this is a mistake: open a new issue pointing to a small, specific set of files.",
			pathCount, limit,
		),
		"trash",
	)

	if err := gh.AddLabels(ctx, owner, repo, number, []string{"trash"}); err != nil {
		return fmt.Errorf("applying trash label to issue #%d: %w", number, err)
	}
	if err := gh.CreateComment(ctx, owner, repo, number, comment); err != nil {
		return fmt.Errorf("posting comment on issue #%d: %w", number, err)
	}
	return nil
}

func humanizeDuration(d time.Duration) string {
	if d >= time.Hour && d%time.Hour == 0 {
		h := int(d / time.Hour)
		return fmt.Sprintf("%dh", h)
	}
	return fmt.Sprintf("%dm", int(d/time.Minute))
}
