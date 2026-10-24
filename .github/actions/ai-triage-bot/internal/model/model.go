// Package model contains data structures shared across the bot's packages.
package model

// LabelDef describes one label from .github/ai-triage/labels.json.
type LabelDef struct {
	Name        string   `json:"name"`
	Color       string   `json:"color"`       // hex without "#", e.g. "d73a4a"
	Description string   `json:"description"` // shown to the AI and in the GitHub UI
	AppliesTo   []string `json:"applies_to"`  // "issue" and/or "pull_request"
	// Mechanical marks a label the bot's own CODE applies (spam flood,
	// moderation removal, the needs-triage fallback, trash-by-verdict).
	// Mechanical labels are deliberately NOT offered to the model as
	// choices — they aren't triage verdicts, and letting the model pick
	// e.g. "spam" for an issue it merely dislikes would mislabel it.
	Mechanical bool `json:"mechanical,omitempty"`
}

// LabelsConfig is the root of labels.json.
type LabelsConfig struct {
	Labels []LabelDef `json:"labels"`
}

// AIVerdict is exactly what the model must return as JSON.
type AIVerdict struct {
	Verdict    string          `json:"verdict"`              // "trash" | "valid"
	Label      string          `json:"label"`                // a label name from the allowed list (or "trash")
	Comment    string          `json:"comment"`              // short comment for the issue/PR
	Moderation *ModerationFlag `json:"moderation,omitempty"` // doxxing/abuse verdict, independent of the above
	Duplicate  *DuplicateFlag  `json:"duplicate,omitempty"`  // only present when duplicate candidates were shown
	PRQuality  *PRQuality      `json:"pr_quality,omitempty"` // only meaningful for pull requests
}

// DuplicateFlag is the model's own decision after comparing the issue/PR
// full text against the full text of the shortlisted candidates (see
// internal/duplicate). It is absent — and must be ignored — when no
// candidates were shown, and a false Positive here closes a legitimate
// report, so the prompt demands the same evidence bar as a bug verdict.
type DuplicateFlag struct {
	IsDuplicate bool   `json:"is_duplicate"`
	DuplicateOf int    `json:"duplicate_of"` // candidate number, 0 when IsDuplicate is false
	Reason      string `json:"reason"`       // one factual English sentence
}

// PRQuality is the model's overall rating of how good (or how much garbage)
// a pull request is. Score is 0-100 with higher = better; the label flow
// (good-pr / needs-work / trash) stays separate, this is the granularity on
// top of it. Reasons must name concrete things from the diff.
type PRQuality struct {
	Score   int      `json:"score"`
	Reasons []string `json:"reasons"`
}

// ModerationFlag is the model's judgment on whether the issue/PR content
// ITSELF is a moderation violation (personal-data exposure or severe
// targeted abuse) — as opposed to "trash", which is a quality verdict. The
// two are independent: a well-formed, substantive bug report can still
// doxx someone, and junk text can be abusive without containing anyone's
// personal data. See internal/triage/moderation.go for what happens when
// Violation is true.
type ModerationFlag struct {
	Violation bool   `json:"violation"`
	Kind      string `json:"kind"`   // "doxxing" | "abuse" | "both" | "" (empty when Violation is false)
	Reason    string `json:"reason"` // one factual sentence, English, without repeating the exposed data/slurs verbatim
}
