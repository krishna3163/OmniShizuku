// Package promptbuilder assembles the final user prompt from an issue's
// title/body or a PR's diff, the repository files that were read, and the
// list of allowed labels.
package promptbuilder

import (
	"fmt"
	"strings"

	"aitriage/internal/model"
)

// FileSnippet is one file that was read (or a truncated piece of it) for context.
type FileSnippet struct {
	Path      string
	Content   string
	Truncated bool
}

// ReferenceSnippet is another issue/PR in the same repo that the author
// pointed to (e.g. "duplicate of #42", "fixes #17"), fetched so the model
// can actually check the claim instead of trusting the bare number.
type ReferenceSnippet struct {
	Number    int
	Title     string
	Body      string
	State     string
	IsPR      bool
	Truncated bool
}

// DuplicateCandidate is an existing issue/PR that the local keyword-overlap
// search shortlisted as possibly the same problem. Its FULL text (already
// truncated to duplicate-max-body-chars) is included so the model compares
// text against text instead of titles against titles.
type DuplicateCandidate struct {
	Number    int
	Title     string
	Body      string
	State     string
	IsPR      bool
	Score     float64 // local keyword-overlap score that got it shortlisted
	Truncated bool
}

// RejectedAttachment is a link the author included that was found but
// FAILED automatic screening (wrong host, too big, looked like an
// executable, unsupported type, ...) and was therefore never downloaded or
// read. This is passed to the model explicitly — a lower attachment count
// must never be indistinguishable from "the author attached nothing" (see
// writeAttachmentsSection and internal/triage/common.go's
// attachmentScreeningNote, which makes the same disclosure in the posted
// comment, deterministically, regardless of what the model says).
type RejectedAttachment struct {
	URL    string
	Reason string
}

// IssueInput is everything needed for an issue prompt.
type IssueInput struct {
	Number              int
	Title               string
	Body                string
	Author              string
	Files               []FileSnippet
	FailedFiles         []string // paths the author cited that could NOT be read (typo, wrong branch, missing)
	References          []ReferenceSnippet
	AllowedLabels       []model.LabelDef
	LaplaceFactor       string // from internal/laplace, Factor.PromptBlock() — heuristic context, not fact
	AttachedImages      int    // how many screenshots were passed separately as multimodal input
	AttachedTexts       []FileSnippet
	RejectedAttachments []RejectedAttachment
	DuplicateCandidates []DuplicateCandidate
}

// FileSide is one side of a changed file: the content at that ref, or an
// explicit record of why there is no content. Missing/Failed are never
// silently folded into "the file is empty" — the model must be able to tell
// "added by this PR" and "I could not fetch it" apart from real content.
type FileSide struct {
	Content   string
	Missing   bool // the file does not exist on this side (added / removed by the PR)
	Failed    bool // a fetch was attempted and failed
	Truncated bool // content was cut at max-file-bytes
}

// ChangedFile is one file touched by the pull request, together with its
// churn statistics and — when the bot could afford to read it — BOTH
// versions: the base-branch content BEFORE the PR and the PR-head content
// AFTER it. Diff hunks alone only show a few surrounding lines; reading both
// sides is what lets the model judge a change against the code it replaces.
type ChangedFile struct {
	Path         string
	PreviousPath string // set for renames: where the file came from
	Status       string // added | modified | removed | renamed | copied | changed
	Additions    int
	Deletions    int
	Changes      int

	Before *FileSide // base-branch side; nil = not fetched at all
	After  *FileSide // PR-head side; nil = not fetched at all

	// UnreadReason is set when NEITHER side was fetched, and says why
	// (noisy file, over the read budget, fetch error). Non-empty means
	// Before and After are both nil.
	UnreadReason string
}

// PRInput is everything needed for a pull request prompt.
type PRInput struct {
	Number              int
	Title               string
	Body                string
	Author              string
	Diff                string
	ChangedFiles        []ChangedFile
	References          []ReferenceSnippet
	AllowedLabels       []model.LabelDef
	LaplaceFactor       string
	AttachedImages      int
	AttachedTexts       []FileSnippet
	RejectedAttachments []RejectedAttachment
	DuplicateCandidates []DuplicateCandidate
}

// evidenceReminder is repeated right before the JSON-format instructions
// (i.e. last thing the model reads before answering) as a second, more
// forceful pass at the same rule from instructions.md — a plausible-sounding
// claim is not evidence, and a missing/unread file is not silence to fill
// in with the author's version of events.
const evidenceReminder = "## Reminder before you answer\nOnly confirm a code-level claim (\"bug\", or treating a technical detail as true) if you can name a specific file from a \"Files read\" section above (or, for PRs, a specific line in the diff) and what in it supports the claim. A file the author cited but that's missing from \"Files read\" — or no files at all — means you cannot confirm it: say so explicitly and prefer \"needs-info\" (or \"invalid\" if the report is otherwise clearly mistaken) over taking the description at face value. A confident or detailed description is not, by itself, evidence.\n\n"

// responseFormatBlock assembles the JSON-format instructions. It is a
// function rather than a constant because pull requests carry one extra
// required field ("pr_quality" — how good or how garbage the diff is) that
// means nothing for an issue. Whatever is demanded here MUST be parseable
// by internal/triage.parseVerdict, which deliberately tolerates the optional
// objects being absent rather than failing the whole run over them.
func responseFormatBlock(isPR, hasDuplicates bool) string {
	var b strings.Builder

	b.WriteString("## Response format\n")
	b.WriteString("Return STRICTLY valid JSON and nothing else — no markdown wrapper, no explanation before or after. The \"label\" field must be one of the label names above, or \"trash\" if verdict=\"trash\". The \"moderation\" object is REQUIRED on every response (see the \"Moderation\" policy above): set \"violation\": true ONLY for genuine doxxing/personal-data exposure or severe targeted abuse, with \"kind\" as \"doxxing\", \"abuse\", or \"both\", and \"reason\" as one factual English sentence that does NOT repeat the exposed data or slurs verbatim. Otherwise \"violation\": false with \"kind\" and \"reason\" left as empty strings.")

	if isPR {
		b.WriteString(" A \"pr_quality\" object is also REQUIRED on a pull request: {\"score\": <integer 0-100, higher = better and less garbage>, \"reasons\": [<2-5 short strings, each naming something concrete you saw in the diff or in a before/after file version>]}. Score the diff itself, not the description: unrelated churn, dumped/generated/minified/vendored files, pure reformatting or whitespace noise, dead code, an empty or trivially tiny change for a stated goal, contradicting the stated problem, or missing tests for a behavioral change are all reasons to lower it.")
	}

	if hasDuplicates {
		b.WriteString(" If — and ONLY if — one of the \"Possible duplicates\" candidates really is the SAME problem (same failure in the same place, not merely the same topic or the same file), add a \"duplicate\" object: {\"is_duplicate\": true, \"duplicate_of\": <that candidate's number>, \"reason\": \"<one factual English sentence naming what makes them the same report>\"}. Otherwise omit the object or set \"is_duplicate\": false. Getting this wrong CLOSES a legitimate report as a duplicate, so when in doubt leave it out.")
	}

	b.WriteString(" Example structures (the values below are illustration only, don't copy them):\n")
	b.WriteString("{\"verdict\": \"trash\", \"label\": \"trash\", \"comment\": \"Spam/ads, unrelated to the repository.\", \"moderation\": {\"violation\": false, \"kind\": \"\", \"reason\": \"\"}}\n")
	b.WriteString("or\n")
	if isPR {
		b.WriteString("{\"verdict\": \"valid\", \"label\": \"needs-work\", \"comment\": \"The fix is in internal/storage/save.go but the new nil check is bypassed when the map is pre-allocated, see the BEFORE version.\", \"pr_quality\": {\"score\": 45, \"reasons\": [\"adds 300 lines of reformatting-only churn alongside the actual fix\", \"no test for the new branch\"]}, \"moderation\": {\"violation\": false, \"kind\": \"\", \"reason\": \"\"}}\n")
	} else {
		b.WriteString("{\"verdict\": \"valid\", \"label\": \"bug\", \"comment\": \"Confirmed in internal/storage/save.go: there's no nil check, so it panics on an empty input.\", \"moderation\": {\"violation\": false, \"kind\": \"\", \"reason\": \"\"}}\n")
	}
	b.WriteString("or, only in a genuine moderation case:\n")
	b.WriteString("{\"verdict\": \"trash\", \"label\": \"trash\", \"comment\": \"Off-topic and contains a policy violation, see below.\", \"moderation\": {\"violation\": true, \"kind\": \"doxxing\", \"reason\": \"The body publishes another contributor's home address and personal phone number.\"}}\n")
	b.WriteString("\nAlways write the \"comment\" and \"moderation.reason\" fields in English, regardless of what language the issue/PR itself is written in.")
	return b.String()
}

// CommitReviewInput is everything needed for the commit-review prompt: a
// rating of ONE commit's message and diff. Deliberately NOT an
// AIVerdict — there is no label to apply and nothing to close here, only a
// score the repo owner asked for.
type CommitReviewInput struct {
	Sha     string
	Message string
	Author  string
	Diff    string
}

// BuildCommitReviewPrompt builds the prompt for rating a single commit.
// The rubric itself lives in system prompt #6 (commit_review_instructions.
// md) — this only supplies the commit and the response shape, so the scoring
// rules exist in exactly one place.
func BuildCommitReviewPrompt(in CommitReviewInput) string {
	var b strings.Builder

	b.WriteString("# Task: Rate a single Git commit\n\n")
	fmt.Fprintf(&b, "Commit `%s`, author @%s\n\n", orNA(in.Sha), orNA(in.Author))
	b.WriteString("## Commit message\n" + orNA(in.Message) + "\n\n")
	b.WriteString("## Commit diff\n```diff\n" + orNA(in.Diff) + "\n```\n\n")

	b.WriteString("## What to do\n")
	b.WriteString("1. Read the commit message and the diff above.\n")
	b.WriteString("2. Apply the rating rubric from your instructions — judge what is actually in front of you, not what the message claims.\n")
	b.WriteString("3. Return the JSON rating described below.\n\n")

	b.WriteString("## Response format\n")
	b.WriteString("Return STRICTLY valid JSON and nothing else — no markdown wrapper, no explanation before or after:\n")
	b.WriteString("{\"score\": <integer 0-100, higher = better>, \"comment\": \"<1-4 sentences of diagnosis>\", \"reasons\": [<2-5 short strings, each naming something concrete you saw in the message or the diff>]}\n\n")
	b.WriteString("Example (illustration only, don't copy it):\n")
	b.WriteString("{\"score\": 62, \"comment\": \"The message says 'fix crash' but the diff also renames a public API and drops a caller; the fix itself is correct.\", \"reasons\": [\"message does not mention the API rename bundled in\", \"fix is correct: the nil check is on the right side of the guard\"]}\n\n")
	b.WriteString("Always write \"comment\" and every entry of \"reasons\" in English. A commit message that over-promises relative to its diff must be called out explicitly.\n")
	return b.String()
}

// BuildIssuePrompt builds the prompt for triaging an issue.
func BuildIssuePrompt(in IssueInput) string {
	var b strings.Builder

	b.WriteString("# Task: Triage a GitHub Issue\n\n")
	fmt.Fprintf(&b, "Issue #%d, author @%s\n\n", in.Number, in.Author)
	fmt.Fprintf(&b, "## Title\n%s\n\n", orNA(in.Title))
	fmt.Fprintf(&b, "## Issue body\n%s\n\n", orNA(in.Body))

	writeFilesSection(&b, in.Files, "no files were successfully read — either the author didn't cite one, or the cited path doesn't exist / is wrong. This is NOT license to judge the claim from the text alone: if confirming it requires code you don't have, say so and prefer \"needs-info\" over trusting the description.")
	writeFailedFilesSection(&b, in.FailedFiles)
	writeReferencesSection(&b, in.References)
	writeDuplicatesSection(&b, in.DuplicateCandidates)
	writeAttachmentsSection(&b, in.AttachedImages, in.AttachedTexts, in.RejectedAttachments)
	writeLaplaceSection(&b, in.LaplaceFactor)
	writeLabelsSection(&b, in.AllowedLabels)

	b.WriteString("\n## What to do\n")
	b.WriteString("1. Decide whether this issue has real content or is junk (spam, ads, abuse, meaningless text, obviously bot-generated with no connection to the repo, a completely empty description).\n")
	b.WriteString("2. If a \"Possible duplicates\" section is present, check this issue's full text against each candidate's full text before anything else — see the instructions for when to mark something a duplicate.\n")
	b.WriteString("3. If it has real content, check the claimed problem against the files and repo context you were given: is this a genuine bug/reasonable request, or is the author mistaken/making things up?\n")
	b.WriteString("4. Pick ONE label from the list above (except trash, which is applied by a separate rule).\n")
	b.WriteString("5. Write a short comment with your diagnosis — why this verdict.\n\n")

	b.WriteString(evidenceReminder)
	b.WriteString(responseFormatBlock(false, len(in.DuplicateCandidates) > 0))
	return b.String()
}

// BuildPRPrompt builds the prompt for triaging a pull request.
func BuildPRPrompt(in PRInput) string {
	var b strings.Builder

	b.WriteString("# Task: Triage a GitHub Pull Request\n\n")
	fmt.Fprintf(&b, "PR #%d, author @%s\n\n", in.Number, in.Author)
	fmt.Fprintf(&b, "## Title\n%s\n\n", orNA(in.Title))
	fmt.Fprintf(&b, "## PR description\n%s\n\n", orNA(in.Body))

	b.WriteString("## Diff\n```diff\n")
	b.WriteString(orNA(in.Diff))
	b.WriteString("\n```\n\n")

	// Both sections below are built from the same ChangedFiles slice: the
	// size/shape summary is pure arithmetic the model must not have to do
	// (and must not get wrong) itself, and the per-file before/after blocks
	// are the actual evidence for the quality call.
	writePRSizeSection(&b, in.ChangedFiles)
	writeChangedFilesSection(&b, in.ChangedFiles, "no file versions could be read beyond the diff above — base your assessment on the diff itself, and if it doesn't show enough surrounding context to confirm or refute a specific claim, say so rather than guessing")
	writeReferencesSection(&b, in.References)
	writeDuplicatesSection(&b, in.DuplicateCandidates)
	writeAttachmentsSection(&b, in.AttachedImages, in.AttachedTexts, in.RejectedAttachments)
	writeLaplaceSection(&b, in.LaplaceFactor)
	writeLabelsSection(&b, in.AllowedLabels)

	b.WriteString("\n## What to do\n")
	b.WriteString("1. Decide whether this is a meaningful PR or junk (empty/meaningless diff, unrelated to the repo, obvious vandalism, generated noise).\n")
	b.WriteString("2. If a \"Possible duplicates\" section is present, compare this PR's full text against each candidate's full text — see the instructions for when to mark something a duplicate.\n")
	b.WriteString("3. If it has real content, assess quality using the BEFORE and AFTER versions above: does it actually solve the stated problem, does it follow the repo's conventions, does it improve or damage the code it replaces?\n")
	b.WriteString("4. Pick ONE label from the list above (except trash, which is applied by a separate rule).\n")
	b.WriteString("5. Write a short comment with your diagnosis, and give the required \"pr_quality\" score.\n\n")

	b.WriteString(evidenceReminder)
	b.WriteString(responseFormatBlock(true, len(in.DuplicateCandidates) > 0))
	return b.String()
}

// writePRSizeSection renders the deterministic size/shape summary of the
// PR, computed here rather than by the model: totals that are wrong in the
// prompt would propagate straight into the verdict, and this costs nothing.
func writePRSizeSection(b *strings.Builder, files []ChangedFile) {
	if len(files) == 0 {
		return
	}
	var add, mod, del, ren, other, plus, minus, read int
	for _, f := range files {
		plus += f.Additions
		minus += f.Deletions
		if f.Before != nil || f.After != nil {
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

	b.WriteString("## PR size & shape (computed by the bot from the GitHub API — treat these as facts)\n")
	fmt.Fprintf(b, "- %d file(s) changed: %d added, %d modified, %d removed, %d renamed", len(files), add, mod, del, ren)
	if other > 0 {
		fmt.Fprintf(b, ", %d other", other)
	}
	b.WriteString("\n")
	fmt.Fprintf(b, "- diff total: +%d / -%d lines (%d changed lines)\n", plus, minus, plus+minus)
	fmt.Fprintf(b, "- full before/after file versions read: %d of %d\n", read, len(files))
	b.WriteString("\n")
}

// writeChangedFilesSection shows, per changed file, the content BEFORE the
// PR (base branch) and AFTER it (PR head) where the bot could read it — and
// explicitly flags files it could not read, so a shorter file list is never
// indistinguishable from "the PR only touched these files".
func writeChangedFilesSection(b *strings.Builder, files []ChangedFile, emptyNote string) {
	if len(files) == 0 {
		fmt.Fprintf(b, "## Changed files — before → after\n(%s)\n\n", emptyNote)
		return
	}
	b.WriteString("## Changed files — before → after\n")
	b.WriteString("For each file: BEFORE is the base-branch content this PR replaces, AFTER is the PR-head content. Use them together with the diff — a file listed as NOT read has no content shown, so do not describe what's inside it.\n")

	for _, f := range files {
		fmt.Fprintf(b, "\n### `%s`", f.Path)
		if f.PreviousPath != "" && f.PreviousPath != f.Path {
			fmt.Fprintf(b, " (renamed from `%s`)", f.PreviousPath)
		}
		fmt.Fprintf(b, " — %s (+%d / -%d, %d changed lines)\n", orNA(f.Status), f.Additions, f.Deletions, f.Changes)

		if f.UnreadReason != "" {
			fmt.Fprintf(b, "**NOT READ** — %s\n", f.UnreadReason)
			continue
		}
		writeFileSide(b, "BEFORE (base branch)", f.Before)
		writeFileSide(b, "AFTER (PR head)", f.After)
	}
	b.WriteString("\n")
}

func writeFileSide(b *strings.Builder, label string, side *FileSide) {
	fmt.Fprintf(b, "**%s**\n", label)
	switch {
	case side == nil:
		b.WriteString("(not fetched for this side)\n")
	case side.Failed:
		b.WriteString("(the bot tried to fetch this side and failed — no content available)\n")
	case side.Missing:
		b.WriteString("(the file does not exist on this side)\n")
	default:
		fmt.Fprintf(b, "```\n%s\n```\n", side.Content)
		if side.Truncated {
			b.WriteString("(truncated, only part is shown)\n")
		}
	}
}

// writeDuplicatesSection lists the candidates the local keyword-overlap
// search shortlisted. Absent entirely (no section) means duplicate
// detection was off, or nothing scored high enough — never "the AI decided
// there were no duplicates".
func writeDuplicatesSection(b *strings.Builder, cands []DuplicateCandidate) {
	if len(cands) == 0 {
		return
	}
	b.WriteString("## Possible duplicates\n")
	b.WriteString("Shortlisted by a keyword-overlap search over this repository, then ranked by keyword overlap (score 0.0-1.0, higher = more shared keywords). This is a HINT that a candidate might be the same problem — it is NOT evidence that it is. Read each candidate's full text against the issue/PR above: match on the actual problem, not on shared words or the same general area of the code. A report about a different failure, a different version, or a different component is not a duplicate even if the vocabulary overlaps.\n")
	for _, c := range cands {
		kind := "Issue"
		if c.IsPR {
			kind = "PR"
		}
		suffix := ""
		if c.Truncated {
			suffix = " (truncated)"
		}
		fmt.Fprintf(b, "\n### %s #%d — %s (state: %s, overlap score %.2f)%s\n%s\n", kind, c.Number, orNA(c.Title), orNA(c.State), c.Score, suffix, orNA(c.Body))
	}
	b.WriteString("\n")
}

func writeFilesSection(b *strings.Builder, files []FileSnippet, emptyNote string) {
	if len(files) == 0 {
		fmt.Fprintf(b, "## Files read from the repository\n(%s)\n\n", emptyNote)
		return
	}
	b.WriteString("## Files read from the repository\n")
	for _, f := range files {
		suffix := ""
		if f.Truncated {
			suffix = " (truncated, only part is shown)"
		}
		fmt.Fprintf(b, "\n### %s%s\n```\n%s\n```\n", f.Path, suffix, f.Content)
	}
	b.WriteString("\n")
}

// writeFailedFilesSection lists paths the author cited in the text that the
// bot tried and failed to read (typo, wrong branch, deleted file). This is
// deliberately surfaced as its own section rather than silently folded into
// "no files were read" — the model needs to know a citation existed and
// specifically could not be verified, not just that the section is empty.
func writeFailedFilesSection(b *strings.Builder, failed []string) {
	if len(failed) == 0 {
		return
	}
	b.WriteString("## Files cited by the author that could NOT be read\n")
	b.WriteString("These paths were referenced in the text, but the bot could not fetch their content (typo, wrong branch, or the file doesn't exist in the repo). You have NOT seen what these files actually contain — do not treat the author's description of them as confirmed.\n")
	for _, p := range failed {
		fmt.Fprintf(b, "- `%s`\n", p)
	}
	b.WriteString("\n")
}

// writeReferencesSection includes other issues/PRs the author pointed to
// (e.g. "duplicate of #42"), fetched so the model can check the claim
// against their actual content instead of trusting the bare reference.
func writeReferencesSection(b *strings.Builder, refs []ReferenceSnippet) {
	if len(refs) == 0 {
		return
	}
	b.WriteString("## Other issues/PRs referenced in the text\n")
	b.WriteString("Fetched because the author pointed to them (e.g. \"#42\", \"duplicate of #17\", \"fixes #99\"). Treat this as context to verify, not as automatic confirmation — check that it actually says what the author claims before relying on it.\n")
	for _, r := range refs {
		kind := "Issue"
		if r.IsPR {
			kind = "PR"
		}
		suffix := ""
		if r.Truncated {
			suffix = " (truncated)"
		}
		fmt.Fprintf(b, "\n### %s #%d — %s (state: %s)%s\n%s\n", kind, r.Number, orNA(r.Title), orNA(r.State), suffix, orNA(r.Body))
	}
	b.WriteString("\n")
}

func writeLabelsSection(b *strings.Builder, labels []model.LabelDef) {
	b.WriteString("## Available labels\n")
	if len(labels) == 0 {
		b.WriteString("(list is empty — use the \"needs-triage\" label in this case)\n")
		return
	}
	for _, l := range labels {
		fmt.Fprintf(b, "- %s — %s\n", l.Name, l.Description)
	}
}

// writeAttachmentsSection describes the attachments the author included on
// the issue/PR (screenshots, log files). Images are NOT inlined into the
// prompt text — they're passed separately as multimodal input (see
// ai.GenerateWithImages); this section only states the count so the model
// knows to look at them. rejected lists attachments that were found but
// failed automatic screening and were therefore never downloaded/read —
// the model must judge those specific claims on text alone, not assume
// nothing was attached (see internal/attachments.SkipReason and
// internal/triage/common.go's attachmentScreeningNote, which makes the
// same disclosure in the posted comment regardless of what the model does
// with this).
func writeAttachmentsSection(b *strings.Builder, imageCount int, texts []FileSnippet, rejected []RejectedAttachment) {
	if imageCount == 0 && len(texts) == 0 && len(rejected) == 0 {
		return
	}
	b.WriteString("## Attachments from the author\n")
	if imageCount > 0 {
		fmt.Fprintf(b, "%d screenshot(s) passed automatic screening and are attached to this request — passed to you as separate message parts (multimodal input). Look at them: they may confirm or contradict the claimed problem.\n", imageCount)
	}
	for _, t := range texts {
		fmt.Fprintf(b, "\n### Attached file: %s\n```\n%s\n```\n", t.Path, t.Content)
	}
	if len(rejected) > 0 {
		fmt.Fprintf(b, "\n%d attachment(s) FAILED automatic screening and were NOT downloaded or shown to you in any form — do not guess their content, do not assume they support or contradict the claim, and do not penalize the author for something you can't see. Judge the affected part(s) of the issue/PR on the surrounding text alone. Reasons:\n", len(rejected))
		for _, r := range rejected {
			fmt.Fprintf(b, "- %s\n", r.Reason)
		}
	}
	b.WriteString("\n")
}

// writeLaplaceSection inserts the heuristic "Laplace factor" — context about
// the likely automated/AI origin of the submission. block is already fully
// formatted by internal/laplace (including the mandatory "this isn't fact"
// caveat), so it's inserted as-is here.
func writeLaplaceSection(b *strings.Builder, block string) {
	if strings.TrimSpace(block) == "" {
		return
	}
	b.WriteString("## Laplace factor (context about the author, NOT grounds for a verdict by itself)\n")
	b.WriteString(block)
	b.WriteString("\n")
}

func orNA(s string) string {
	if strings.TrimSpace(s) == "" {
		return "(empty)"
	}
	return s
}
