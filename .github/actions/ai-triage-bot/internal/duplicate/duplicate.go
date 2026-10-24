// Package duplicate shortlists existing issues/PRs that might be the same
// problem as the item currently being triaged.
//
// The design is deliberately two-stage, and the split matters:
//
//  1. a cheap LOCAL keyword-overlap search (a few GitHub search calls, no AI)
//     narrows the field to a handful of plausible candidates, and
//  2. the model then compares the item's FULL text against each candidate's
//     FULL text and decides — see promptbuilder.writeDuplicatesSection.
//
// The local score decides only what the model gets to LOOK at. It never
// decides the verdict: shared vocabulary is not a shared problem, and a
// wrong "duplicate" closes a legitimate report.
package duplicate

import (
	"context"
	"log"
	"regexp"
	"sort"
	"strings"
	"unicode"

	"aitriage/internal/ghclient"
	"aitriage/internal/promptbuilder"
)

// Options bounds one shortlisting run. Everything here is wired straight
// from the matching inputs in action.yml.
type Options struct {
	SelfNumber    int     // the issue/PR being triaged — never a candidate for itself
	IsPR          bool    // search type:pr instead of type:issue
	MaxKeywords   int     // how many keywords to extract from the title and search for
	MaxCandidates int     // how many candidates may be shown to the model at all
	MinSimilarity float64 // lowest local overlap score a candidate needs to be shown
	MaxBodyChars  int     // candidates' bodies are truncated to this many characters
}

// searchPerPage bounds ONE keyword's result page. Keywords are capped by
// MaxKeywords, so the total number of search calls is capped too.
const searchPerPage = 20

// keywordSplitRe cuts text into candidate keywords. Letters, digits, "_",
// "-" and "." are kept inside a token so that "404-error", "save.go" and
// "m3-expressive" survive as single keywords instead of being shredded.
var keywordSplitRe = regexp.MustCompile(`[^\p{L}\p{N}_.\-]+`)

// stopwords are high-frequency words that appear in almost every issue title
// across unrelated topics. Left in, they dominate the overlap score and the
// shortlist fills with "the", "crash", "when" noise; kept deliberately small
// and generic so real signal isn't dropped with them.
var stopwords = map[string]bool{
	"the": true, "and": true, "for": true, "with": true, "that": true,
	"this": true, "from": true, "are": true, "was": true, "were": true,
	"has": true, "have": true, "had": true, "not": true, "but": true,
	"you": true, "your": true, "its": true, "it's": true, "into": true,
	"when": true, "while": true, "after": true, "before": true, "about": true,
	"issue": true, "bug": true, "error": true, "problem": true, "crash": true,
	"please": true, "thanks": true, "hello": true, "help": true, "does": true,
	"doesn": true, "don't": true, "cant": true, "can't": true, "will": true,
	"app": true, "file": true, "files": true, "code": true, "fix": true,
	"fixed": true, "using": true, "used": true, "use": true, "new": true,
}

// Keywords extracts up to max significant keywords from text, in first
// appearance order (so results are deterministic). A token qualifies when
// it is at least 3 characters, isn't a stopword, and contains at least one
// letter — which drops bare numbers and punctuation runs while keeping
// version-ish tokens like "v1.2" and "404-error".
func Keywords(text string, max int) []string {
	if max <= 0 {
		return nil
	}
	seen := make(map[string]bool, max)
	out := make([]string, 0, max)
	for _, tok := range keywordSplitRe.Split(strings.ToLower(text), -1) {
		tok = strings.Trim(tok, "_.-\t\n\r")
		if len(tok) < 3 || stopwords[tok] || !hasLetter(tok) {
			continue
		}
		if seen[tok] {
			continue
		}
		seen[tok] = true
		out = append(out, tok)
		if len(out) >= max {
			break
		}
	}
	return out
}

// hasLetter reports whether s contains at least one letter. Digit-only
// tokens ("123", "2024") are dropped as keywords; non-ASCII letters
// (Cyrillic, CJK, ...) count as letters too.
func hasLetter(s string) bool {
	for _, r := range s {
		if unicode.IsLetter(r) {
			return true
		}
	}
	return false
}

// Similarity is the Jaccard overlap of two keyword sets:
// |A ∩ B| / |A ∪ B|, in [0,1]. Jaccard rather than plain intersection
// count, so a long title can't rack up matches just by being long — the
// score measures how much of the two titles is actually the same words.
func Similarity(a, b []string) float64 {
	setA := toSet(a)
	setB := toSet(b)
	if len(setA) == 0 || len(setB) == 0 {
		return 0
	}
	inter := 0
	for k := range setA {
		if setB[k] {
			inter++
		}
	}
	union := len(setA) + len(setB) - inter
	if union == 0 {
		return 0
	}
	return float64(inter) / float64(union)
}

func toSet(keys []string) map[string]bool {
	set := make(map[string]bool, len(keys))
	for _, k := range keys {
		set[k] = true
	}
	return set
}

// Shortlist runs the two-stage pipeline and returns the candidates to show
// the model, best overlap first, capped at opt.MaxCandidates. An empty
// result is normal (feature off for this item, no keywords, nothing above
// MinSimilarity) and must NOT be reported to the model as "there are no
// duplicates" — see promptbuilder.writeDuplicatesSection, which simply
// renders no section at all in that case.
//
// Individual search failures are logged and skipped: one unreachable query
// must not take down the whole triage run over a feature that is, by
// nature, a hint.
func Shortlist(ctx context.Context, gh *ghclient.Client, owner, repo, title string, opt Options) []promptbuilder.DuplicateCandidate {
	if opt.MaxCandidates <= 0 {
		return nil
	}
	keywords := Keywords(title, opt.MaxKeywords)
	if len(keywords) == 0 {
		return nil
	}

	found := map[int]ghclient.SearchItem{}
	for _, kw := range keywords {
		res, err := gh.SearchRepoItemsByKeyword(ctx, owner, repo, kw, opt.IsPR, searchPerPage)
		if err != nil {
			log.Printf("duplicate: search for keyword %q failed, skipping it: %v", kw, err)
			continue
		}
		for _, it := range res.Items {
			if it.Number == opt.SelfNumber {
				continue
			}
			if _, dup := found[it.Number]; dup {
				continue
			}
			found[it.Number] = it
		}
	}
	if len(found) == 0 {
		return nil
	}

	type scoredItem struct {
		item  ghclient.SearchItem
		score float64
	}
	var scored []scoredItem
	for _, it := range found {
		// Scored on TITLES on both sides: the candidate's body is far longer
		// than the title, and Jaccard over title+body would collapse to ~0
		// for every candidate no matter how relevant it actually is.
		s := Similarity(keywords, Keywords(it.Title, len(keywords)*3))
		if s < opt.MinSimilarity {
			continue
		}
		scored = append(scored, scoredItem{item: it, score: s})
	}
	if len(scored) == 0 {
		return nil
	}

	// Deterministic order: best overlap first, then by number so that two
	// candidates tied on score don't swap between runs.
	sort.Slice(scored, func(i, j int) bool {
		if scored[i].score != scored[j].score {
			return scored[i].score > scored[j].score
		}
		return scored[i].item.Number < scored[j].item.Number
	})
	if len(scored) > opt.MaxCandidates {
		scored = scored[:opt.MaxCandidates]
	}

	out := make([]promptbuilder.DuplicateCandidate, 0, len(scored))
	for _, sc := range scored {
		body := sc.item.Body
		truncated := false
		if r := []rune(body); len(r) > opt.MaxBodyChars {
			body = string(r[:opt.MaxBodyChars])
			truncated = true
		}
		out = append(out, promptbuilder.DuplicateCandidate{
			Number:    sc.item.Number,
			Title:     sc.item.Title,
			Body:      body,
			State:     sc.item.State,
			IsPR:      sc.item.PullRequest != nil,
			Score:     sc.score,
			Truncated: truncated,
		})
	}
	return out
}
