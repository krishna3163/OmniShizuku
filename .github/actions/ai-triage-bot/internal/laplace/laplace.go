// Package laplace computes the "Laplace factor" — a heuristic estimate of
// how much an issue/PR looks like the output of an automated/AI pipeline
// rather than an ordinary human submission. This is NOT a lie detector: it
// has no access to the author's intent, only to the signals available (git
// metadata, account age, activity rate, text patterns). Because of that, the
// Laplace factor never renders a verdict on its own — it's only passed to
// the model as ADDITIONAL context to orient it faster, and the model is
// required to check it against the actual code/diff rather than trust it
// as fact.
package laplace

import (
	"context"
	"fmt"
	"log"
	"regexp"
	"strings"
	"time"

	"aitriage/internal/ghclient"
)

// Factor is the computed result for one issue/PR.
type Factor struct {
	Score   int      // 0..100, higher means more signs of automated/AI origin
	Signals []string // human-readable list of triggered signals (for logs and the prompt)
}

// Level returns a coarse risk label — convenient for the prompt.
func (f *Factor) Level() string {
	switch {
	case f.Score >= 60:
		return "high"
	case f.Score >= 30:
		return "medium"
	default:
		return "low"
	}
}

// PromptBlock formats the factor for insertion into the model's prompt,
// with an explicit caveat that it's a heuristic, not a fact.
func (f *Factor) PromptBlock() string {
	var b strings.Builder
	fmt.Fprintf(&b, "Heuristic risk estimate of automated/AI origin: %d/100 (level: %s).\n", f.Score, f.Level())
	if len(f.Signals) == 0 {
		b.WriteString("No signals triggered.\n")
	} else {
		b.WriteString("Triggered signals:\n")
		for _, s := range f.Signals {
			fmt.Fprintf(&b, "- %s\n", s)
		}
	}
	b.WriteString("IMPORTANT: this is a metadata heuristic, not proof. Use it only as supporting context and call out explicitly in the comment if it influenced the verdict. It is not by itself grounds for \"trash\" — that always has to come from the code/diff/text you actually read.\n")
	return b.String()
}

// Input is everything needed to compute the factor.
type Input struct {
	Owner, Repo string
	Author      string
	AuthorType  string // from the webhook (Actor.Type): "User"/"Bot"/"Organization" — a free signal
	Body        string
	IsPR        bool
	PRNumber    int // only needed if IsPR
}

// Compute calculates the Laplace factor. Errors from individual sub-checks
// (GitHub API unavailable, author deleted their account, etc.) aren't
// fatal — the corresponding signal is just skipped, not the whole run.
func Compute(ctx context.Context, gh *ghclient.Client, in Input) *Factor {
	f := &Factor{}

	// 1. Account type — the cheapest, most reliable signal, already in the webhook.
	if strings.EqualFold(in.AuthorType, "Bot") {
		f.add(45, fmt.Sprintf("account @%s is flagged by GitHub itself as a Bot", in.Author))
	}

	// 2. Account age + total activity across all of GitHub.
	user, err := gh.GetUser(ctx, in.Author)
	if err != nil {
		log.Printf("laplace factor: failed to fetch profile for @%s: %v", in.Author, err)
	} else {
		ageDays := time.Since(user.CreatedAt).Hours() / 24
		switch {
		case ageDays < 3:
			f.add(30, fmt.Sprintf("account @%s was created %.1f days ago (under 3 days old)", in.Author, ageDays))
		case ageDays < 14:
			f.add(18, fmt.Sprintf("account @%s was created %.0f days ago (under 2 weeks old)", in.Author, ageDays))
		case ageDays < 60:
			f.add(8, fmt.Sprintf("account @%s was created %.0f days ago (under 2 months old)", in.Author, ageDays))
		}

		if total, err := gh.SearchAuthorActivityTotal(ctx, in.Author); err != nil {
			log.Printf("laplace factor: failed to compute activity for @%s: %v", in.Author, err)
		} else if ageDays >= 1 {
			ratePerDay := float64(total) / ageDays
			switch {
			case ratePerDay > 8:
				f.add(35, fmt.Sprintf("abnormally high activity rate: %d issues/PRs over the account's whole history / %.0f days ≈ %.1f per day", total, ageDays, ratePerDay))
			case ratePerDay > 3:
				f.add(15, fmt.Sprintf("elevated activity rate: ≈%.1f issues/PRs per day over the account's whole history", ratePerDay))
			}
		}
	}

	// 3. Commit git metadata (PRs only — issues have no commits).
	if in.IsPR {
		checkCommits(ctx, gh, in, f)
	}

	// 4. Text patterns and emoji.
	checkTextPatterns(in.Body, f)

	if f.Score > 100 {
		f.Score = 100
	}
	return f
}

func (f *Factor) add(points int, signal string) {
	f.Score += points
	f.Signals = append(f.Signals, signal)
}

// automationNeedles are fragments of a committer's email/name that are
// characteristic of automated pipelines (CI bots, AI coding-tool commit
// agents) rather than a human who committed their own PR by hand.
var automationNeedles = []string{
	"github-actions", "actions@github.com", "dependabot",
	"codex", "openai", "chatgpt", "copilot",
	"claude", "anthropic",
	"devin", "cursor.sh", "cursor.com", "sweep.dev", "aider",
	"[bot]", "noreply@anthropic.com",
}

// genericNoreplyRe matches a legacy/non-standard noreply address without an
// id and login (a legitimate GitHub noreply is always
// "12345+login@users.noreply.github.com").
var genericNoreplyRe = regexp.MustCompile(`^noreply@github\.com$`)

func checkCommits(ctx context.Context, gh *ghclient.Client, in Input, f *Factor) {
	commits, err := gh.ListPullCommits(ctx, in.Owner, in.Repo, in.PRNumber)
	if err != nil {
		log.Printf("laplace factor: failed to fetch commits for PR #%d: %v", in.PRNumber, err)
		return
	}
	if len(commits) == 0 {
		return
	}

	seenAutomation := map[string]bool{}
	authorLoginMatched := false
	for _, c := range commits {
		identities := []ghclient.CommitIdentity{c.Commit.Author, c.Commit.Committer}
		for _, id := range identities {
			needle := strings.ToLower(id.Email + " " + id.Name)
			for _, n := range automationNeedles {
				if strings.Contains(needle, n) && !seenAutomation[n] {
					seenAutomation[n] = true
					f.add(20, fmt.Sprintf("a git commit's metadata contains an automation/AI-pipeline marker (%q) instead of an ordinary author email", n))
				}
			}
			if genericNoreplyRe.MatchString(strings.ToLower(strings.TrimSpace(id.Email))) && !seenAutomation["generic-noreply"] {
				seenAutomation["generic-noreply"] = true
				f.add(15, "a commit uses the generic noreply@github.com instead of a personal users.noreply.github.com address — unusual for a commit made through GitHub by hand")
			}
		}
		if c.Author != nil && strings.EqualFold(c.Author.Login, in.Author) {
			authorLoginMatched = true
		}
	}

	if !authorLoginMatched {
		f.add(12, fmt.Sprintf("none of the PR's commits resolve to the PR author's GitHub account (@%s) — the PR may have been opened via a proxy account or assembled from someone else's commits", in.Author))
	}
}

// emojiRe roughly covers the main Unicode emoji blocks.
var emojiRe = regexp.MustCompile(`[\x{1F300}-\x{1FAFF}\x{2600}-\x{27BF}\x{2300}-\x{23FF}\x{FE0F}]`)

// llmLeakPhrases are phrases that show up en masse in text generated by a
// language model and left uncleaned before being pasted into an issue/PR
// (an AI tool's response accidentally copied in whole, boilerplate included).
var llmLeakPhrases = []string{
	"as an ai language model", "as a large language model",
	"i don't have the ability to browse", "i cannot browse the internet",
	"i'm sorry, but i", "i'm sorry, but as an ai",
	"i hope this helps", "let me know if you need any further",
}

var structuredBulletRe = regexp.MustCompile(`(?m)^\s*[-*]\s+\*\*[^*]+\*\*\s*:`)

func checkTextPatterns(body string, f *Factor) {
	if strings.TrimSpace(body) == "" {
		return
	}
	lower := strings.ToLower(body)

	wordCount := len(strings.Fields(body))
	if wordCount == 0 {
		wordCount = 1
	}
	emojis := emojiRe.FindAllString(body, -1)
	ratio := float64(len(emojis)) / float64(wordCount)
	switch {
	case len(emojis) >= 8 && ratio > 0.2:
		f.add(15, fmt.Sprintf("very high emoji density in the text: %d across %d words", len(emojis), wordCount))
	case len(emojis) >= 4 && ratio > 0.1:
		f.add(7, fmt.Sprintf("elevated emoji density in the text: %d across %d words", len(emojis), wordCount))
	}

	for _, phrase := range llmLeakPhrases {
		if strings.Contains(lower, phrase) {
			f.add(25, fmt.Sprintf("found a leaked AI-response boilerplate phrase in the text: %q", phrase))
			break // one occurrence is enough, don't duplicate the signal
		}
	}

	if matches := structuredBulletRe.FindAllString(body, -1); len(matches) >= 4 {
		f.add(8, fmt.Sprintf("text is formatted as %d bulleted items like \"- **Heading**: ...\" — a typical AI-formatting style", len(matches)))
	}
}
