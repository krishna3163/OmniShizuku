// Package triage contains the core business logic: analyzing issues/PRs,
// applying labels, and posting comments.
package triage

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"log"
	"os"
	"path/filepath"
	"regexp"
	"strconv"
	"strings"
	"unicode/utf8"

	"aitriage/internal/ai"
	"aitriage/internal/attachments"
	"aitriage/internal/config"
	"aitriage/internal/ghclient"
	"aitriage/internal/model"
	"aitriage/internal/promptbuilder"
)

// marker is the hidden tag on every bot comment, used to check idempotency:
// "after one comment, the bot does nothing else".
const marker = "<!-- ai-triage-bot:processed -->"

// prompts holds the loaded system prompts and label config for one run.
type prompts struct {
	repoContext  string
	templates    string // system prompt #3 — optional, may be empty
	instructions string
	labels       *model.LabelsConfig
}

func loadPrompts(cfg *config.Config) (*prompts, error) {
	repoContext, err := os.ReadFile(filepath.Join(cfg.Workspace, cfg.RepoContextPath))
	if err != nil {
		return nil, fmt.Errorf("reading %s: %w (this is system prompt #1 — the repo description, it's required)", cfg.RepoContextPath, err)
	}
	instructions, err := os.ReadFile(filepath.Join(cfg.Workspace, cfg.InstructionsPath))
	if err != nil {
		return nil, fmt.Errorf("reading %s: %w (this is system prompt #2 — instructions/tone)", cfg.InstructionsPath, err)
	}

	// Templates (system prompt #3) is optional: a repo that hasn't set one
	// up yet (or deleted it) just gets no canned-pattern section, not a
	// failed run.
	var templates string
	templatesData, err := os.ReadFile(filepath.Join(cfg.Workspace, cfg.TemplatesPath))
	if err != nil {
		if !os.IsNotExist(err) {
			return nil, fmt.Errorf("reading %s: %w (this is system prompt #3 — optional canned patterns)", cfg.TemplatesPath, err)
		}
		log.Printf("no templates file at %s (optional) — skipping canned-response patterns", cfg.TemplatesPath)
	} else {
		templates = string(templatesData)
	}

	labelsData, err := os.ReadFile(filepath.Join(cfg.Workspace, cfg.LabelsConfigPath))
	if err != nil {
		return nil, fmt.Errorf("reading %s: %w", cfg.LabelsConfigPath, err)
	}
	var lc model.LabelsConfig
	if err := json.Unmarshal(labelsData, &lc); err != nil {
		return nil, fmt.Errorf("parsing %s: %w", cfg.LabelsConfigPath, err)
	}

	return &prompts{
		repoContext:  string(repoContext),
		templates:    templates,
		instructions: string(instructions),
		labels:       &lc,
	}, nil
}

// systemPrompt concatenates prompt #1 (repo context), the optional prompt
// #3 (known patterns/canned responses), and prompt #2 (instructions): repo
// context comes first so the model gets the domain before anything else,
// known patterns come next since they're still project-specific context,
// and the general behavior rules come last, right before the actual task.
func (p *prompts) systemPrompt() string {
	sp := p.repoContext
	if strings.TrimSpace(p.templates) != "" {
		sp += "\n\n---\n\n" + p.templates
	}
	sp += "\n\n---\n\n" + p.instructions
	return sp
}

func filterLabels(lc *model.LabelsConfig, kind string) []model.LabelDef {
	var out []model.LabelDef
	for _, l := range lc.Labels {
		// Mechanical labels (spam/moderation-removed/needs-triage/trash) are
		// applied by the bot's own code paths — they are never a model choice.
		if l.Mechanical {
			continue
		}
		for _, a := range l.AppliesTo {
			if a == kind {
				out = append(out, l)
				break
			}
		}
	}
	return out
}

// findLabelDef looks up a label definition in the config. If the model (or
// a fallback path) referenced a label that's missing from labels.json, it
// returns sane defaults so EnsureLabel doesn't fail.
func findLabelDef(lc *model.LabelsConfig, name string) model.LabelDef {
	for _, l := range lc.Labels {
		if l.Name == name {
			return l
		}
	}
	switch name {
	case "trash":
		return model.LabelDef{Name: "trash", Color: "b60205", Description: "Junk: spam, bots, off-topic, or no real content"}
	case "spam":
		return model.LabelDef{Name: "spam", Color: "5319e7", Description: "Flooding: too many issues from the same author"}
	default:
		return model.LabelDef{Name: "needs-triage", Color: "cccccc", Description: "Needs manual review"}
	}
}

// resolveLabel guarantees the final label is either "trash" (by verdict) or
// one from the allowed list. If the model made something up, we fall back
// to needs-triage instead of creating a random label in the repo.
func resolveLabel(v *model.AIVerdict, allowed []model.LabelDef) string {
	if v.Verdict == "trash" {
		return "trash"
	}
	// "trash" is a mechanical label applied only by the verdict rule above:
	// verdict=valid + label="trash" is self-contradictory, and acting on the
	// label would post the plain "AI Triage" heading while stamping the item
	// as junk. Reject it before consulting the allowed list — even if a
	// labels.json forgets to mark it mechanical and lists it there.
	if strings.EqualFold(strings.TrimSpace(v.Label), "trash") {
		return "needs-triage"
	}
	for _, l := range allowed {
		if strings.EqualFold(l.Name, v.Label) {
			return l.Name
		}
	}
	return "needs-triage"
}

// parseVerdict parses the model's JSON response, tolerating an optional
// ```json ... ``` markdown wrapper.
func parseVerdict(raw string) (*model.AIVerdict, error) {
	cleaned := stripJSONFences(raw)

	var v model.AIVerdict
	if err := json.Unmarshal([]byte(cleaned), &v); err != nil {
		return nil, fmt.Errorf("failed to parse the model's JSON verdict: %w (raw response: %s)", err, truncateForLog(raw))
	}
	v.Verdict = strings.ToLower(strings.TrimSpace(v.Verdict))

	// Gemini occasionally collapses the label into the verdict field,
	// returning verdict="invalid" instead of the correct verdict="valid",
	// label="invalid". Normalize that specific case instead of failing the
	// run over what's really just the model using the wrong field.
	if v.Verdict == "invalid" {
		v.Verdict = "valid"
		if strings.TrimSpace(v.Label) == "" {
			v.Label = "invalid"
		}
	}

	if v.Verdict != "trash" && v.Verdict != "valid" {
		return nil, fmt.Errorf("unexpected verdict value %q", v.Verdict)
	}
	if strings.TrimSpace(v.Comment) == "" {
		return nil, errors.New("model returned an empty comment")
	}

	// Moderation is required by the prompt (see promptbuilder's
	// responseFormatBlock), but a missing/malformed field must NOT fail
	// the whole run — that would mean one forgotten field silently stops
	// the bot from labeling/commenting on anything at all. Default to "no
	// violation" instead; a false negative here just means the normal
	// label/comment flow runs as usual, which is the safe direction to
	// fail in (a genuine violation not caught this run can still be
	// caught on the next comment/edit event).
	if v.Moderation == nil {
		v.Moderation = &model.ModerationFlag{}
	}
	v.Moderation.Kind = strings.ToLower(strings.TrimSpace(v.Moderation.Kind))
	if !v.Moderation.Violation {
		v.Moderation.Kind = ""
		v.Moderation.Reason = ""
	}

	// Duplicate flag: only actionable when the model named the candidate it
	// matched. "is_duplicate: true, duplicate_of: 0" would otherwise close
	// the report with no way to say what it's a duplicate of — so that is
	// normalized down to "not a duplicate" instead of acting on it.
	if v.Duplicate != nil {
		v.Duplicate.Reason = strings.TrimSpace(v.Duplicate.Reason)
		if !v.Duplicate.IsDuplicate || v.Duplicate.DuplicateOf <= 0 {
			v.Duplicate.IsDuplicate = false
			v.Duplicate.DuplicateOf = 0
			v.Duplicate.Reason = ""
		}
	}

	// PR quality score is a 0-100 rating; a model that returns 150 or -3
	// would post an absurd number straight into the comment, so clamp it.
	if v.PRQuality != nil {
		if v.PRQuality.Score < 0 {
			v.PRQuality.Score = 0
		}
		if v.PRQuality.Score > 100 {
			v.PRQuality.Score = 100
		}
	}

	return &v, nil
}

func truncateForLog(s string) string {
	const max = 800
	if len(s) <= max {
		return s
	}
	return s[:max] + "...(truncated for logging)"
}

// handleDuplicate applies the model's duplicate verdict: the "duplicate"
// label, a comment naming which existing item it duplicates and why, and
// closing the report. The comment is posted BEFORE the close so a failed
// close can never leave a closed thread with no explanation of why.
//
// Only ever called after moderation and after the "trash" verdict are
// handled — junk is junk, it isn't "a duplicate of" anything.
func handleDuplicate(ctx context.Context, gh *ghclient.Client, owner, repo string, number int, verdict *model.AIVerdict, isPR bool) error {
	target := verdict.Duplicate.DuplicateOf
	what := "issue"
	if isPR {
		what = "pull request"
	}

	if err := gh.EnsureLabel(ctx, owner, repo, "duplicate", "cfd3d7", "Looks like an already-existing issue or PR"); err != nil {
		return fmt.Errorf("creating the duplicate label: %w", err)
	}
	if err := gh.AddLabels(ctx, owner, repo, number, []string{"duplicate"}); err != nil {
		return fmt.Errorf("applying the duplicate label to %s #%d: %w", what, number, err)
	}

	var body strings.Builder
	body.WriteString(strings.TrimSpace(verdict.Comment))
	fmt.Fprintf(&body, "\n\nClosing as a duplicate of [#%d](#%d).", target, target)
	if r := strings.TrimSpace(verdict.Duplicate.Reason); r != "" {
		fmt.Fprintf(&body, " %s", r)
	}
	if err := gh.CreateComment(ctx, owner, repo, number, formatComment(body.String(), "duplicate")); err != nil {
		return fmt.Errorf("posting the duplicate comment on %s #%d: %w", what, number, err)
	}

	if isPR {
		if err := gh.ClosePullRequest(ctx, owner, repo, number); err != nil {
			return fmt.Errorf("closing duplicate PR #%d: %w", number, err)
		}
	} else {
		if err := gh.CloseIssue(ctx, owner, repo, number, "not_planned"); err != nil {
			return fmt.Errorf("closing duplicate issue #%d: %w", number, err)
		}
	}

	log.Printf("%s #%d processed: duplicate of #%d", what, number, target)
	return nil
}

// loadFeaturePrompt reads a feature's REQUIRED system-prompt file (the
// discussion / agent / commit-review instructions). A feature being
// enabled while its instructions file is missing is a hard error with a
// message naming the file — never a silent degradation to asking the model
// to do a privileged job with no rules for it.
func loadFeaturePrompt(workspace, path, what string) (string, error) {
	data, err := os.ReadFile(filepath.Join(workspace, path))
	if err != nil {
		return "", fmt.Errorf("reading %s: %w (this is %s, and it must exist while the feature is enabled)", path, err, what)
	}
	return string(data), nil
}

// authorize runs the live permission gate for one of the privileged comment
// features: an optional extra allowlist FIRST, then GitHub's own permission
// level for this author in THIS repository, resolved on every trigger.
//
// need is "write" (admin or write — the bar for discussions and the agent)
// or "admin" (owner-only — commit review and workflow dispatch).
//
// The returned reason is for the RUN LOG only: callers deliberately do not
// reply to a refused trigger, because replying would turn any comment into
// a channel that makes the bot post on demand.
func authorize(ctx context.Context, gh *ghclient.Client, owner, repo, author, need string, allowed []string) (bool, string) {
	if len(allowed) > 0 && !containsFold(allowed, author) {
		return false, fmt.Sprintf("@%s is not in this feature's allowlist", author)
	}
	p, err := gh.GetCollaboratorPermission(ctx, owner, repo, author)
	if err != nil {
		return false, fmt.Sprintf("could not verify @%s's permission on this repository: %v", author, err)
	}
	if !ghclient.HasPermissionAtLeast(p.Permission, need) {
		return false, fmt.Sprintf("@%s has %q permission on this repository, this feature requires %q or higher", author, p.Permission, need)
	}
	return true, ""
}

func containsFold(list []string, s string) bool {
	for _, v := range list {
		if strings.EqualFold(strings.TrimSpace(v), s) {
			return true
		}
	}
	return false
}

// stripJSONFences pulls the model's JSON out of an optional ```json ... ```
// wrapper. Kept in one place because BOTH verdict parsing and the extra
// features' own parsers have to tolerate the model wrapping its answer.
func stripJSONFences(raw string) string {
	cleaned := strings.TrimSpace(raw)
	cleaned = strings.TrimPrefix(cleaned, "```json")
	cleaned = strings.TrimPrefix(cleaned, "```")
	cleaned = strings.TrimSuffix(cleaned, "```")
	return strings.TrimSpace(cleaned)
}

// mandatoryNote is appended to EVERY bot comment. It's added here in code,
// not in the system prompt, because the model might ignore the prompt (or
// be talked into ignoring it via the issue/PR text itself), while this code
// always runs regardless of what the model returned. Requirement: people
// must understand that (a) this is AI and not 100% accurate, (b) the bot
// doesn't read or reply to thread comments — there's no conversing with it.
const mandatoryNote = "> **Note:** this is an automated AI bot (Gemini); its verdict isn't 100% accurate and can be wrong. The bot doesn't read or reply to further comments in this thread — if it got this wrong, ping a human maintainer."

// formatComment builds the final comment text: heading + model text + any
// extra deterministic notes (e.g. attachmentScreeningNote below) + the
// mandatory note + hidden marker for idempotency. Empty notes are skipped
// silently so call sites don't need to special-case "nothing to add".
func formatComment(text, verdict string, extraNotes ...string) string {
	prefix := "🤖 **AI Triage**"
	switch verdict {
	case "trash":
		prefix = "🤖 **AI Triage — rejected**"
	case "duplicate":
		prefix = "🤖 **AI Triage — duplicate**"
	}
	return formatSpecialComment(prefix, text, extraNotes...)
}

// formatSpecialComment is the generic version: heading chosen by the
// caller, then the body, then any deterministic notes, then the mandatory
// note and the hidden idempotency marker. Used by the features whose output
// isn't a triage verdict (commit review, the agent, workflow dispatch) so
// every comment the bot posts carries the same guarantees: the disclaimer
// and the marker are never left to the model to remember.
func formatSpecialComment(heading, text string, extraNotes ...string) string {
	parts := []string{heading, strings.TrimSpace(text)}
	for _, n := range extraNotes {
		if strings.TrimSpace(n) != "" {
			parts = append(parts, n)
		}
	}
	parts = append(parts, mandatoryNote, marker)
	return strings.Join(parts, "\n\n")
}

// attachmentScreeningNote is appended to the comment IN CODE, not left to
// the model to remember — it's the equivalent of mandatoryNote for a
// narrower case: when one or more attachments failed automatic screening
// (too big, wrong host, looked like an executable, ...), people reading
// the issue/PR need a guaranteed, accurate statement that the bot did NOT
// see those files and judged the affected parts on text alone, not a
// disclosure that depends on the model choosing to mention it.
func attachmentScreeningNote(rejected []promptbuilder.RejectedAttachment) string {
	if len(rejected) == 0 {
		return ""
	}
	var b strings.Builder
	fmt.Fprintf(&b, "> **Note:** %d attachment(s) did **not** pass automatic screening and were **not read** — the verdict above is based on the text only (and any attachments that DID pass screening). Reasons:", len(rejected))
	for _, r := range rejected {
		fmt.Fprintf(&b, "\n> - %s", r.Reason)
	}
	return b.String()
}

// alreadyProcessed checks whether the bot already commented on this
// issue/PR — this is the "after 1 comment, do nothing else" mechanism.
func alreadyProcessed(ctx context.Context, gh *ghclient.Client, owner, repo string, number int) (bool, error) {
	comments, err := gh.ListComments(ctx, owner, repo, number)
	if err != nil {
		return false, err
	}
	for _, c := range comments {
		if strings.Contains(c.Body, marker) {
			return true, nil
		}
	}
	return false, nil
}

var (
	quotedPathRe = regexp.MustCompile("`([a-zA-Z0-9_\\-./]+\\.[a-zA-Z0-9]{1,10})`")
	looseePathRe = regexp.MustCompile(`(?:^|\s)([a-zA-Z0-9_\-]+(?:/[a-zA-Z0-9_\-]+)+\.[a-zA-Z0-9]{1,10})(?:[\s,.:;)]|$)`)
)

// extractFilePaths looks for file paths in the issue text: first inside
// `backticks` (what the issue template requires), then heuristically as
// "dir/file.ext" in free text.
func extractFilePaths(body string) []string {
	seen := map[string]bool{}
	var out []string
	add := func(p string) {
		p = strings.Trim(p, "`\"'()[]{}.,;: \t")
		p = strings.TrimPrefix(p, "/")
		// Strip git-diff-style "a/" and "b/" prefixes (e.g. copy-pasted from
		// a diff or patch) — the Contents API has no such prefix, so a path
		// like "a/ignis/widgets/button.lua" 404s until it's stripped down to
		// "ignis/widgets/button.lua".
		p = strings.TrimPrefix(p, "a/")
		p = strings.TrimPrefix(p, "b/")
		if p == "" || seen[p] {
			return
		}
		seen[p] = true
		out = append(out, p)
	}
	for _, m := range quotedPathRe.FindAllStringSubmatch(body, -1) {
		add(m[1])
	}
	for _, m := range looseePathRe.FindAllStringSubmatch(body, -1) {
		add(m[1])
	}
	return out
}

// truncateUTF8 cuts s to at most max bytes WITHOUT splitting a multi-byte
// rune. A raw s[:max] can land in the middle of a character and hand the
// model a broken byte sequence at exactly the truncation point, which is
// the one place a garbled character is most likely to change meaning.
func truncateUTF8(s string, max int) string {
	if max < 0 {
		max = 0
	}
	if len(s) <= max {
		return s
	}
	cut := max
	for cut > 0 && !utf8.RuneStart(s[cut]) {
		cut--
	}
	return s[:cut]
}

// fetchFiles reads up to maxFiles files at the given paths, truncating each
// to maxBytes. An empty ref means the default branch. Files that fail to
// read (typo'd path, deleted file, etc.) are not silently dropped anymore —
// they're returned separately as failed paths, so the caller can tell the
// model "this was cited but couldn't be verified" instead of leaving it
// indistinguishable from "nothing was cited at all" (see writeFailedFilesSection).
func fetchFiles(ctx context.Context, gh *ghclient.Client, owner, repo, ref string, paths []string, maxFiles, maxBytes int) (files []promptbuilder.FileSnippet, failed []string) {
	for _, p := range paths {
		if len(files) >= maxFiles {
			break
		}
		content, err := gh.GetFileContent(ctx, owner, repo, p, ref)
		if err != nil {
			log.Printf("skipping file %q: %v", p, err)
			failed = append(failed, p)
			continue
		}
		truncated := false
		if len(content) > maxBytes {
			content = truncateUTF8(content, maxBytes)
			truncated = true
		}
		files = append(files, promptbuilder.FileSnippet{Path: p, Content: content, Truncated: truncated})
	}
	return files, failed
}

// issueRefRe matches bare "#123"-style cross-references to another
// issue/PR in the same repo — the standard GitHub convention for "duplicate
// of #42", "see #17", "fixes #99". The character before "#" must not be a
// word character or "&"/"/" (to avoid matching inside a URL fragment or an
// entity), and the digits must end on a word boundary — so a mixed
// alphanumeric token like "#1a2b3c" doesn't match (the boundary check fails
// right after the "1"). A purely numeric 6-digit hex color like "#123456"
// is indistinguishable from a large issue number and can still slip
// through; that's a harmless false positive — GetIssue just 404s and it's
// silently skipped, same as any other unresolved reference.
var issueRefRe = regexp.MustCompile(`(?:^|[^0-9A-Za-z&/])#([0-9]{1,6})\b`)

// extractIssueRefs finds cross-referenced issue/PR numbers in body text,
// excluding the current issue/PR's own number and capped at max entries —
// like extractFilePaths, this bounds how many extra GitHub API calls a
// single body can force just by listing "#1 #2 #3 ...".
func extractIssueRefs(body string, selfNumber, max int) []int {
	if max <= 0 {
		return nil
	}
	seen := map[int]bool{selfNumber: true}
	var out []int
	for _, m := range issueRefRe.FindAllStringSubmatch(body, -1) {
		n, err := strconv.Atoi(m[1])
		if err != nil || n <= 0 || seen[n] {
			continue
		}
		seen[n] = true
		out = append(out, n)
		if len(out) >= max {
			break
		}
	}
	return out
}

// maxReferenceBodyChars truncates a referenced issue/PR's body so a single
// "#42" mention can't blow up the prompt's token budget.
const maxReferenceBodyChars = 1500

// fetchReferences resolves cross-referenced issue/PR numbers to their
// title/body/state, so the model can check a claim like "duplicate of #42"
// against what #42 actually says instead of trusting the bare number.
// Individual failures (deleted, wrong number, API budget exhausted) are
// logged and skipped — not fatal for the run.
func fetchReferences(ctx context.Context, gh *ghclient.Client, owner, repo string, numbers []int) []promptbuilder.ReferenceSnippet {
	var out []promptbuilder.ReferenceSnippet
	for _, n := range numbers {
		issue, err := gh.GetIssue(ctx, owner, repo, n)
		if err != nil {
			log.Printf("reference #%d: skipping, couldn't fetch: %v", n, err)
			continue
		}
		body := issue.Body
		truncated := false
		if len(body) > maxReferenceBodyChars {
			body = truncateUTF8(body, maxReferenceBodyChars)
			truncated = true
		}
		out = append(out, promptbuilder.ReferenceSnippet{
			Number:    n,
			Title:     issue.Title,
			Body:      body,
			State:     issue.State,
			IsPR:      issue.PullRequest != nil,
			Truncated: truncated,
		})
	}
	return out
}

var noisyPathParts = []string{
	"vendor/", "node_modules/", "dist/", "build/", ".min.js", ".min.css",
	"go.sum", "package-lock.json", "yarn.lock", "pnpm-lock.yaml",
	".svg", ".png", ".jpg", ".jpeg", ".gif", ".ico", ".woff", ".ttf",
}

// isLikelyNoise filters out files that aren't worth the maxFiles budget to
// read in full (lockfiles, binaries, vendored code).
func isLikelyNoise(path string) bool {
	lower := strings.ToLower(path)
	for _, n := range noisyPathParts {
		if strings.Contains(lower, n) {
			return true
		}
	}
	return false
}

// attachmentLimits maps the config's limits onto the type the attachments
// package expects.
func attachmentLimits(cfg *config.Config) attachments.Limits {
	return attachments.Limits{
		MaxCount:      cfg.MaxAttachments,
		MaxBytesEach:  cfg.MaxAttachmentBytes,
		MaxBytesTotal: cfg.MaxAttachmentTotal,
		Timeout:       cfg.AttachmentFetchTimeout,
	}
}

// fetchBodyAttachments pulls attachment links (screenshots, logged text
// files) out of an issue/PR body and downloads them within the configured
// limits. Returns images and text attachments separately — either (or both)
// can be empty, which is normal, not an error — plus a third list of
// attachments that were found but REJECTED by screening (wrong host, too
// big, looks like an executable, unsupported type, ...): those are never
// downloaded/read, and the caller must surface that fact rather than treat
// a lower count as silently equivalent to "nothing was attached" — see
// attachmentScreeningNote and promptbuilder.writeAttachmentsSection.
func fetchBodyAttachments(ctx context.Context, cfg *config.Config, body string) ([]ai.ImagePart, []promptbuilder.FileSnippet, []promptbuilder.RejectedAttachment) {
	if cfg.MaxAttachments <= 0 {
		return nil, nil, nil
	}
	urls := attachments.ExtractURLs(body)
	if len(urls) == 0 {
		return nil, nil, nil
	}
	fetched, skipped := attachments.Fetch(ctx, urls, attachmentLimits(cfg))

	var images []ai.ImagePart
	var texts []promptbuilder.FileSnippet
	for _, a := range fetched {
		switch a.Kind {
		case attachments.KindImage:
			images = append(images, ai.ImagePart{MIMEType: a.MIMEType, Data: a.Data})
		case attachments.KindText:
			texts = append(texts, promptbuilder.FileSnippet{Path: a.URL, Content: a.Text})
		}
	}

	var rejected []promptbuilder.RejectedAttachment
	for _, s := range skipped {
		rejected = append(rejected, promptbuilder.RejectedAttachment{URL: s.URL, Reason: s.Reason})
	}

	log.Printf("attachments: found %d links, accepted %d images, %d text files, %d rejected by screening", len(urls), len(images), len(texts), len(rejected))
	return images, texts, rejected
}
