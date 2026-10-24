package triage

import (
	"strings"
	"testing"
	"unicode/utf8"

	"aitriage/internal/config"
	"aitriage/internal/model"
)

func TestTruncateUTF8_NeverSplitsRune(t *testing.T) {
	// "привет" is 12 bytes (6 Cyrillic runes × 2 bytes each). Cutting at an
	// odd byte offset must land on a rune boundary, not mid-character.
	s := "привет"
	for max := 0; max <= len(s); max++ {
		got := truncateUTF8(s, max)
		if !utf8.ValidString(got) {
			t.Fatalf("truncateUTF8(%q, %d) = %q, which is not valid UTF-8 (splits a rune)", s, max, got)
		}
		if len(got) > max {
			t.Fatalf("truncateUTF8(%q, %d) returned %d bytes, above the limit", s, max, len(got))
		}
	}
}

func TestTruncateUTF8_NegativeMaxIsZero(t *testing.T) {
	if got := truncateUTF8("abc", -5); got != "" {
		t.Errorf("truncateUTF8 with a negative max = %q, want empty", got)
	}
}

func TestTruncateUTF8_ShortStringUntouched(t *testing.T) {
	if got := truncateUTF8("abc", 10); got != "abc" {
		t.Errorf("got %q, want %q", got, "abc")
	}
}

func TestResolveLabel_TrashVerdictWins(t *testing.T) {
	allowed := []model.LabelDef{{Name: "bug"}}
	got := resolveLabel(&model.AIVerdict{Verdict: "trash", Label: "bug"}, allowed)
	if got != "trash" {
		t.Errorf("got %q, want %q", got, "trash")
	}
}

// A verdict of "valid" paired with label "trash" is self-contradictory. The
// label must NOT be applied — falling back to needs-triage stops the bot
// stamping a substantive item as junk.
func TestResolveLabel_RejectsTrashLabelOnValidVerdict(t *testing.T) {
	allowed := []model.LabelDef{{Name: "bug"}}
	got := resolveLabel(&model.AIVerdict{Verdict: "valid", Label: "trash"}, allowed)
	if got != "needs-triage" {
		t.Errorf("got %q, want %q", got, "needs-triage")
	}
}

// Case-insensitive matching, and an unknown label falls back rather than
// letting the model invent one.
func TestResolveLabel_AllowedAndUnknown(t *testing.T) {
	allowed := []model.LabelDef{{Name: "bug"}, {Name: "question"}}
	if got := resolveLabel(&model.AIVerdict{Verdict: "valid", Label: "BUG"}, allowed); got != "bug" {
		t.Errorf("case-insensitive match: got %q, want %q", got, "bug")
	}
	if got := resolveLabel(&model.AIVerdict{Verdict: "valid", Label: "made-up"}, allowed); got != "needs-triage" {
		t.Errorf("unknown label: got %q, want %q", got, "needs-triage")
	}
}

// filterLabels must never offer a mechanical label (spam/trash/...) to the
// model as a choice — those are applied by code, not by verdict.
func TestFilterLabels_SkipsMechanical(t *testing.T) {
	lc := &model.LabelsConfig{Labels: []model.LabelDef{
		{Name: "bug", AppliesTo: []string{"issue"}},
		{Name: "spam", AppliesTo: []string{"issue"}, Mechanical: true},
		{Name: "trash", AppliesTo: []string{"issue"}, Mechanical: true},
		{Name: "needs-triage", AppliesTo: []string{"issue"}, Mechanical: true},
		{Name: "good-pr", AppliesTo: []string{"pull_request"}},
	}}
	got := filterLabels(lc, "issue")
	if len(got) != 1 || got[0].Name != "bug" {
		t.Fatalf("filterLabels(issue) = %v, want only [bug]", got)
	}
	for _, l := range got {
		if l.Mechanical {
			t.Errorf("mechanical label %q was offered to the model", l.Name)
		}
	}
}

func TestStripJSONFences(t *testing.T) {
	cases := []struct{ in, want string }{
		{`{"a":1}`, `{"a":1}`},
		{"```json\n{\"a\":1}\n```", `{"a":1}`},
		{"```\n{\"a\":1}\n```", `{"a":1}`},
	}
	for _, tc := range cases {
		if got := stripJSONFences(tc.in); got != tc.want {
			t.Errorf("stripJSONFences(%q) = %q, want %q", tc.in, got, tc.want)
		}
	}
}

func TestParseVerdict_NormalizesInvalidVerdict(t *testing.T) {
	// Gemini sometimes puts the LABEL in the verdict field. That must be
	// normalized, not treated as a parse failure.
	v, err := parseVerdict(`{"verdict":"invalid","label":"invalid","comment":"ok"}`)
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if v.Verdict != "valid" || v.Label != "invalid" {
		t.Errorf("got verdict=%q label=%q, want valid/invalid", v.Verdict, v.Label)
	}
}

func TestParseVerdict_RejectsUnknownVerdict(t *testing.T) {
	if _, err := parseVerdict(`{"verdict":"banana","label":"bug","comment":"ok"}`); err == nil {
		t.Error("expected an error for an unknown verdict value")
	}
}

func TestParseVerdict_RequiresComment(t *testing.T) {
	if _, err := parseVerdict(`{"verdict":"valid","label":"bug","comment":"   "}`); err == nil {
		t.Error("expected an error for an empty comment")
	}
}

// Moderation must default to "no violation" when the model omits it — a
// missing field must never fail the run, and must never be read as a
// violation that would trigger deletion.
func TestParseVerdict_MissingModerationDefaultsToNoViolation(t *testing.T) {
	v, err := parseVerdict(`{"verdict":"valid","label":"bug","comment":"fine"}`)
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if v.Moderation == nil || v.Moderation.Violation {
		t.Error("missing moderation must default to a non-nil, non-violating flag")
	}
}

// A duplicate flag with no named target is unactionable — it must be
// normalized to "not a duplicate" so nothing gets closed without saying what
// it duplicates.
func TestParseVerdict_DuplicateWithoutTargetIsNotADuplicate(t *testing.T) {
	v, err := parseVerdict(`{"verdict":"valid","label":"bug","comment":"x","duplicate":{"is_duplicate":true,"duplicate_of":0,"reason":"same"}}`)
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if v.Duplicate.IsDuplicate || v.Duplicate.DuplicateOf != 0 {
		t.Errorf("got is_duplicate=%v duplicate_of=%d, want false/0", v.Duplicate.IsDuplicate, v.Duplicate.DuplicateOf)
	}
}

// A PRQuality score outside 0-100 would be posted verbatim into a comment.
func TestParseVerdict_ClampsPRQualityScore(t *testing.T) {
	for _, score := range []int{-5, 150, 1000} {
		v, err := parseVerdict(`{"verdict":"valid","label":"bug","comment":"x","pr_quality":{"score":` + itoa(score) + `}}`)
		if err != nil {
			t.Fatalf("score %d: unexpected error: %v", score, err)
		}
		if v.PRQuality.Score < 0 || v.PRQuality.Score > 100 {
			t.Errorf("score %d was not clamped (got %d)", score, v.PRQuality.Score)
		}
	}
}

func itoa(n int) string {
	if n < 0 {
		return "-" + itoa(-n)
	}
	if n < 10 {
		return string(rune('0' + n))
	}
	return itoa(n/10) + string(rune('0'+n%10))
}

// An issue body citing an absurd number of file paths is an attempt to make
// the bot hammer the Contents API — the extraction must still be bounded so
// that check has something to compare against.
func TestExtractFilePaths_FindsQuotedAndBare(t *testing.T) {
	body := "Broken in `internal/parser/parser.go` and also src/main/main.go"
	got := extractFilePaths(body)
	if !containsStr(got, "internal/parser/parser.go") {
		t.Errorf("quoted path not found in %v", got)
	}
	if !containsStr(got, "src/main/main.go") {
		t.Errorf("bare path not found in %v", got)
	}
}

// A path pasted out of a git diff carries a/ and b/ prefixes that the
// Contents API would 404 on — they must be stripped.
func TestExtractFilePaths_StripsDiffPrefixes(t *testing.T) {
	got := extractFilePaths("see `a/pkg/util.go` and `b/pkg/util.go`")
	for _, p := range got {
		if strings.HasPrefix(p, "a/") || strings.HasPrefix(p, "b/") {
			t.Errorf("diff prefix not stripped: %q", p)
		}
	}
	if !containsStr(got, "pkg/util.go") {
		t.Errorf("expected pkg/util.go after prefix stripping, got %v", got)
	}
}

func TestExtractIssueRefs_ExcludesSelfAndBounds(t *testing.T) {
	got := extractIssueRefs("duplicates #12, see #13, also #14", 12, 2)
	if containsInt(got, 12) {
		t.Errorf("self-reference #12 must be excluded, got %v", got)
	}
	if len(got) > 2 {
		t.Errorf("max=2 was not respected, got %v", got)
	}
}

func TestExtractIssueRefs_IgnoresNonIssueTokens(t *testing.T) {
	if got := extractIssueRefs("color #1a2b3c", 0, 5); len(got) != 0 {
		t.Errorf("alphanumeric token must not match, got %v", got)
	}
}

// authorize is the gate on every privileged feature. The extra allowlist is
// checked FIRST and is an AND, not an OR — being listed does not bypass the
// live permission check, and not being listed refuses before any HTTP call.
//
// The other half (an empty allowlist falling through to GetCollaboratorPermission
// and failing CLOSED on an error) is deliberately not unit-tested here: that
// path needs a real *ghclient.Client, whose baseURL is not injectable, so a
// test would either hit api.github.com or panic on a nil receiver.
func TestAuthorize_MissingAllowlistEntryRefuses(t *testing.T) {
	allowed, reason := authorize(nil, nil, "o", "r", "mallory", "admin", []string{"alice", "bob"})
	if allowed {
		t.Fatal("expected refusal for a user absent from the allowlist")
	}
	if reason == "" {
		t.Error("a refusal must carry a reason for the run log")
	}
}

func TestParseCommitRating_ClampsAndRequiresComment(t *testing.T) {
	r, err := parseCommitRating(`{"score":250,"comment":"solid","reasons":["a"]}`)
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if r.Score != 100 {
		t.Errorf("got %d, want 100", r.Score)
	}
	if _, err := parseCommitRating(`{"score":10,"comment":"  "}`); err == nil {
		t.Error("expected an error for an empty comment")
	}
}

func TestPickDispatchTarget_SingleTargetNeedsNoKeyword(t *testing.T) {
	targets := []config.WorkflowDispatchTarget{{Name: "ci", Workflow: "ci.yml"}}
	got, ok := pickDispatchTarget(targets, " anything at all")
	if !ok || got.Name != "ci" {
		t.Errorf("got %+v ok=%v, want the single target", got, ok)
	}
}

func TestPickDispatchTarget_MultipleRequireKeyword(t *testing.T) {
	targets := []config.WorkflowDispatchTarget{
		{Name: "ci", Keywords: []string{"test", "verify"}, Workflow: "ci.yml"},
		{Name: "apk", Keywords: []string{"apk", "release"}, Workflow: "apk.yml"},
	}
	if _, ok := pickDispatchTarget(targets, "nothing relevant"); ok {
		t.Error("no keyword matched, expected no target")
	}
	// Match is case-insensitive substring against the text AFTER the phrase.
	if got, ok := pickDispatchTarget(targets, " please run the APK now"); !ok || got.Name != "apk" {
		t.Errorf("got %+v ok=%v, want the apk target", got, ok)
	}
	// First matching target wins, in declaration order.
	if got, _ := pickDispatchTarget(targets, " run tests and apk"); got.Name != "ci" {
		t.Errorf("got %q, want the first-declared target", got.Name)
	}
}

func TestSubstituteInputs_OnlyDocumentedPlaceholders(t *testing.T) {
	in := map[string]string{
		"pr":      "{{pr_number}}",
		"ref":     "{{ref}}",
		"who":     "{{triggered_by}}",
		"mixed":   "PR {{pr_number}} on {{ref}} by {{triggered_by}}",
		"literal": "keep {{unknown}} as-is",
	}
	got := substituteInputs(in, map[string]string{
		"{{pr_number}}":    "42",
		"{{ref}}":          "main",
		"{{triggered_by}}": "alice",
	})
	if got["pr"] != "42" || got["ref"] != "main" || got["who"] != "alice" {
		t.Errorf("simple substitution failed: %v", got)
	}
	if got["mixed"] != "PR 42 on main by alice" {
		t.Errorf("mixed substitution failed: %q", got["mixed"])
	}
	if got["literal"] != "keep {{unknown}} as-is" {
		t.Errorf("an undocumented placeholder must be left alone, got %q", got["literal"])
	}
	if substituteInputs(nil, nil) != nil {
		t.Error("empty inputs must return nil")
	}
}

func containsStr(list []string, s string) bool {
	for _, v := range list {
		if v == s {
			return true
		}
	}
	return false
}

func containsInt(list []int, n int) bool {
	for _, v := range list {
		if v == n {
			return true
		}
	}
	return false
}
