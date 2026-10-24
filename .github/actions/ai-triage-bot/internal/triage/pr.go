package triage

import (
	"context"
	"fmt"
	"log"
	"strings"

	"aitriage/internal/ai"
	"aitriage/internal/config"
	"aitriage/internal/duplicate"
	"aitriage/internal/ghclient"
	"aitriage/internal/githubevent"
	"aitriage/internal/laplace"
	"aitriage/internal/model"
	"aitriage/internal/promptbuilder"
)

// HandlePullRequest is the entry point for "pull_request"/"pull_request_target"
// events. modGH is the (optional, may be nil) elevated-permission client
// used only for user blocking when the AI flags a moderation violation —
// PRs themselves can never be truly deleted via any GitHub API, so modGH
// doesn't change what happens to the PR's content, only whether a repeat
// offender also gets blocked. See internal/triage/moderation.go.
func HandlePullRequest(ctx context.Context, cfg *config.Config, gh, modGH *ghclient.Client, aiClient *ai.Client, event *githubevent.Event) error {
	if event.PullRequest == nil {
		return fmt.Errorf("pull_request event payload is missing the pull_request field")
	}
	if event.Action != "opened" {
		log.Printf("PR #%d: action=%q, the bot only reacts to 'opened' — exiting", event.PullRequest.Number, event.Action)
		return nil
	}

	owner, repo := cfg.RepoOwner, cfg.RepoName
	number := event.PullRequest.Number
	author := event.PullRequest.User.Login

	// Idempotency: one comment, and the bot never touches this PR again.
	done, err := alreadyProcessed(ctx, gh, owner, repo, number)
	if err != nil {
		return fmt.Errorf("checking prior comments: %w", err)
	}
	if done {
		log.Printf("PR #%d was already processed by the bot — exiting", number)
		return nil
	}

	p, err := loadPrompts(cfg)
	if err != nil {
		return err
	}
	prLabels := filterLabels(p.labels, "pull_request")

	pr, err := gh.GetPullRequest(ctx, owner, repo, number)
	if err != nil {
		return fmt.Errorf("fetching PR #%d: %w", number, err)
	}

	// The diff is the main source of context for a PR, read in one request.
	diff, err := gh.GetPullDiff(ctx, owner, repo, number)
	if err != nil {
		return fmt.Errorf("fetching diff for PR #%d: %w", number, err)
	}
	if len(diff) > cfg.MaxDiffBytes {
		diff = truncateUTF8(diff, cfg.MaxDiffBytes) + "\n... (diff truncated to save tokens) ...\n"
	}

	// Plus BOTH sides of every changed file the budget allows: the
	// base-branch content the PR replaces and the PR-head content it
	// produces. The diff only carries a few surrounding lines either side
	// of each hunk, so without the before/after versions the model can't
	// check a change against the code it supersedes — nor spot a "fix"
	// that reads well in the diff but doesn't hold up in the whole file.
	files, err := gh.ListPullFiles(ctx, owner, repo, number)
	if err != nil {
		return fmt.Errorf("fetching file list for PR #%d: %w", number, err)
	}
	changed := buildChangedFiles(ctx, gh, owner, repo, pr, files, cfg)
	readCount := 0
	for _, f := range changed {
		if f.UnreadReason == "" {
			readCount++
		}
	}
	log.Printf("PR #%d: diff %d bytes, %d files changed, %d read with before/after", number, len(diff), len(changed), readCount)

	// Cross-referenced issues/PRs (e.g. "fixes #42") — resolved so the model
	// can check the claim against what #42 actually says.
	refNumbers := extractIssueRefs(pr.Body, number, cfg.MaxReferencedIssues)
	references := fetchReferences(ctx, gh, owner, repo, refNumbers)
	if len(references) > 0 {
		log.Printf("PR #%d: resolved %d referenced issue(s)/PR(s)", number, len(references))
	}

	// Author attachments (screenshots/logs attached to the PR description).
	// Anything that fails automatic screening is NOT read; rejectedAtts
	// carries why, so the model judges those cases by text alone.
	imageAtts, textAtts, rejectedAtts := fetchBodyAttachments(ctx, cfg, pr.Body)

	// Duplicate shortlist (ON by default): cheap LOCAL keyword search over
	// this repo's other PRs — no AI, it only decides what the model may
	// compare full text against. See internal/duplicate.
	var dupCandidates []promptbuilder.DuplicateCandidate
	if cfg.DuplicateDetectionEnabled {
		dupCandidates = duplicate.Shortlist(ctx, gh, owner, repo, pr.Title, duplicate.Options{
			SelfNumber:    number,
			IsPR:          true,
			MaxKeywords:   cfg.DuplicateSearchKeywords,
			MaxCandidates: cfg.MaxDuplicateCandidates,
			MinSimilarity: cfg.DuplicateMinSimilarity,
			MaxBodyChars:  cfg.DuplicateMaxBodyChars,
		})
		if len(dupCandidates) > 0 {
			log.Printf("PR #%d: %d possible duplicate(s) shortlisted", number, len(dupCandidates))
		}
	}

	// Laplace factor: for PRs, also checks commit git metadata (author/
	// committer email/name — noreply/actions/codex/copilot/claude patterns
	// suggest an automated pipeline rather than a human).
	factor := laplace.Compute(ctx, gh, laplace.Input{
		Owner: owner, Repo: repo, Author: author,
		AuthorType: event.PullRequest.User.Type, Body: pr.Body,
		IsPR: true, PRNumber: number,
	})
	log.Printf("PR #%d: laplace factor = %d/100 (%s), %d signals", number, factor.Score, factor.Level(), len(factor.Signals))

	userPrompt := promptbuilder.BuildPRPrompt(promptbuilder.PRInput{
		Number:              number,
		Title:               pr.Title,
		Body:                pr.Body,
		Author:              author,
		Diff:                diff,
		ChangedFiles:        changed,
		References:          references,
		DuplicateCandidates: dupCandidates,
		AllowedLabels:       prLabels,
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

	// Moderation takes priority over the normal label/comment flow: see
	// internal/triage/moderation.go. Note PRs can never truly be deleted
	// (no such GitHub API exists) — this redacts/closes/locks instead.
	if verdict.Moderation != nil && verdict.Moderation.Violation {
		return handleModerationViolation(ctx, cfg, gh, modGH, owner, repo, number, author, pr.Title, "", true, verdict.Moderation)
	}

	// Duplicate verdict: the model matched this PR against a shortlisted
	// candidate's full text — label, explain, close. Junk excluded, same
	// reasoning as the issue flow.
	if verdict.Duplicate != nil && verdict.Duplicate.IsDuplicate && verdict.Verdict != "trash" {
		return handleDuplicate(ctx, gh, owner, repo, number, verdict, true)
	}

	finalLabel := resolveLabel(verdict, prLabels)
	labelDef := findLabelDef(p.labels, finalLabel)
	if err := gh.EnsureLabel(ctx, owner, repo, labelDef.Name, labelDef.Color, labelDef.Description); err != nil {
		return fmt.Errorf("creating label %q: %w", labelDef.Name, err)
	}
	if err := gh.AddLabels(ctx, owner, repo, number, []string{finalLabel}); err != nil {
		return fmt.Errorf("applying label %q to PR #%d: %w", finalLabel, number, err)
	}
	comment := formatComment(verdict.Comment, verdict.Verdict, attachmentScreeningNote(rejectedAtts), prQualityNote(verdict.PRQuality, changed))
	if err := gh.CreateComment(ctx, owner, repo, number, comment); err != nil {
		return fmt.Errorf("posting comment on PR #%d: %w", number, err)
	}

	log.Printf("PR #%d processed: verdict=%s label=%s", number, verdict.Verdict, finalLabel)
	return nil
}

// buildChangedFiles turns the API's file list into the before/after shape
// promptbuilder renders: statistics for every changed file (so the size/
// shape summary is complete even for files too expensive to read), and
// content for both sides of up to cfg.MaxFiles non-noisy files.
//
// Noisy files (lockfiles, binaries, vendored code) and files past the read
// budget are NOT dropped from the list — they're kept with an UnreadReason,
// so the model is told a file exists but has no content for it, rather than
// silently believing the PR touched fewer files than it did.
func buildChangedFiles(ctx context.Context, gh *ghclient.Client, owner, repo string, pr *ghclient.PullRequest, files []ghclient.PullFile, cfg *config.Config) []promptbuilder.ChangedFile {
	out := make([]promptbuilder.ChangedFile, 0, len(files))
	budget := cfg.MaxFiles

	for _, f := range files {
		cf := promptbuilder.ChangedFile{
			Path:         f.Filename,
			PreviousPath: f.PreviousFilename,
			Status:       f.Status,
			Additions:    f.Additions,
			Deletions:    f.Deletions,
			Changes:      f.Changes,
		}

		switch {
		case isLikelyNoise(f.Filename):
			cf.UnreadReason = "noisy file (lockfile, binary, minified or vendored content) — deliberately not read, it costs context without adding signal"
		case budget <= 0:
			cf.UnreadReason = fmt.Sprintf("outside the %d-file read budget for this run", cfg.MaxFiles)
		default:
			// For a rename/copy the base-side content lives under the OLD
			// path — reading the new path against the base ref would 404 and
			// wrongly look like a brand-new file.
			beforePath := f.Filename
			if f.PreviousFilename != "" {
				beforePath = f.PreviousFilename
			}
			before := readFileSide(ctx, gh, owner, repo, beforePath, pr.Base.SHA, f.Status == "added", cfg.MaxFileBytes)
			after := readFileSide(ctx, gh, owner, repo, f.Filename, pr.Head.SHA, f.Status == "removed", cfg.MaxFileBytes)
			if before.Failed && after.Failed {
				cf.UnreadReason = "the bot tried to read this file on both sides and both fetches failed — see the run log"
			} else {
				cf.Before, cf.After = before, after
				budget--
			}
		}
		out = append(out, cf)
	}
	return out
}

// readFileSide fetches one side of a changed file at ref. It never returns
// an error: "this file doesn't exist at that ref" is a legitimate answer
// (added/deleted, or a ref the path isn't on yet) and is reported as
// Missing; a real failure is reported as Failed. Both are rendered
// explicitly by promptbuilder — neither is ever shown as empty content.
func readFileSide(ctx context.Context, gh *ghclient.Client, owner, repo, path, ref string, missing bool, maxBytes int) *promptbuilder.FileSide {
	if missing {
		return &promptbuilder.FileSide{Missing: true}
	}
	content, err := gh.GetFileContent(ctx, owner, repo, path, ref)
	if err != nil {
		if ghclient.IsNotFound(err) {
			return &promptbuilder.FileSide{Missing: true}
		}
		log.Printf("PR: could not read %q at %s: %v", path, ref, err)
		return &promptbuilder.FileSide{Failed: true}
	}
	side := &promptbuilder.FileSide{Content: content}
	if len(content) > maxBytes {
		side.Content = truncateUTF8(content, maxBytes)
		side.Truncated = true
	}
	return side
}

// prQualityNote renders the quality block that goes into the POSTED comment
// — the model's 0-100 score with its reasons, plus the size/shape facts
// computed by the bot. It's built in code rather than left to the model so
// the numbers are always present and always right: the model supplies the
// judgment, the bot supplies the arithmetic. Returns "" when there's
// nothing to report (formatComment skips empty notes).
func prQualityNote(v *model.PRQuality, files []promptbuilder.ChangedFile) string {
	if v == nil && len(files) == 0 {
		return ""
	}
	var b strings.Builder

	if v != nil {
		fmt.Fprintf(&b, "**PR quality: %d/100** (higher = better; this is the model's rating of the diff, the label above is the triage verdict)", v.Score)
		if len(v.Reasons) > 0 {
			b.WriteString("\n")
			for _, r := range v.Reasons {
				fmt.Fprintf(&b, "\n- %s", r)
			}
		}
	}

	if len(files) > 0 {
		var add, mod, del, ren, other, plus, minus, read int
		for _, f := range files {
			plus += f.Additions
			minus += f.Deletions
			if f.UnreadReason == "" {
				read++
			}
			switch f.Status {
			case "added":
				add++
			case "modified":
				mod++
			case "removed":
				del++
			case "renamed":
				ren++
			default:
				other++
			}
		}
		if b.Len() > 0 {
			b.WriteString("\n\n")
		}
		fmt.Fprintf(&b, "**Diff size:** %d file(s) changed (%d added, %d modified, %d removed, %d renamed", len(files), add, mod, del, ren)
		if other > 0 {
			fmt.Fprintf(&b, ", %d other", other)
		}
		fmt.Fprintf(&b, "), **+%d / -%d lines**. Before/after versions read for %d of %d file(s).", plus, minus, read, len(files))
	}
	return b.String()
}
