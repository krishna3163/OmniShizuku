// Package trigger decides whether a comment is addressed to the bot, and
// what command it carries. There are two documented matching styles and
// both are implemented here rather than ad hoc at each call site, because
// "the bot didn't fire" and "the bot fired on someone else's comment" are
// exactly the failure modes these inputs exist to control.
package trigger

import (
	"regexp"
	"strings"
)

// Substring matches phrase as a case-insensitive substring — the documented
// behaviour of the discussion, commit-review and workflow-dispatch
// triggers. Returns everything AFTER the phrase (the command/question), or
// ok=false when the phrase isn't present at all.
func Substring(body, phrase string) (rest string, ok bool) {
	if phrase == "" {
		return "", false
	}
	i := strings.Index(strings.ToLower(body), strings.ToLower(phrase))
	if i < 0 {
		return "", false
	}
	return body[i+len(phrase):], true
}

// spaceRun matches the separators a human might type instead of the ones in
// the configured phrase.
var spaceRun = regexp.MustCompile(`[\s_\-]+`)

// SubstringOrFlexible is the agent trigger's matching: case-insensitive AND
// tolerant of a space/hyphen/underscore anywhere the phrase has one, so
// "@github-actions", "@GitHub actions" and "@GitHub-actions," all match the
// default phrase. Implemented as a compiled regex built from the phrase, so
// a custom phrase gets the same treatment without a special case per word.
func SubstringOrFlexible(body, phrase string) (rest string, ok bool) {
	if phrase == "" {
		return "", false
	}
	parts := spaceRun.Split(phrase, -1)
	for i, p := range parts {
		parts[i] = regexp.QuoteMeta(p)
	}
	re, err := regexp.Compile("(?i)" + strings.Join(parts, `[\s_\-]*`))
	if err != nil {
		// A phrase that can't be compiled (shouldn't happen after QuoteMeta,
		// but a caller could pass something exotic) degrades to plain
		// substring matching rather than silently never firing.
		return Substring(body, phrase)
	}
	loc := re.FindStringIndex(body)
	if loc == nil {
		return "", false
	}
	return body[loc[1]:], true
}
