# System prompt #3 (optional): known patterns & canned responses

> This file is **optional**. If it's missing or deleted, the bot just skips
> this section entirely and writes every comment from scratch — nothing
> breaks. Its job: catch the issue/PR shapes that show up **over and over**
> in your repo, so the model recognizes them immediately and gives the same
> answer every time, instead of re-deriving (and re-wording) the same
> diagnosis on each occurrence.
>
> This is NOT a replacement for `instructions.md` (general tone/rules) or
> `repo_context.md` (what the project is) — it's a lookup table of specific,
> recurring shapes: "if it looks like THIS, the answer is always THAT."
>
> **Format for each pattern:**
> - **Recognize when:** a concrete, specific trigger — specific enough that
>   the model won't over-match dissimilar issues/PRs onto this pattern.
> - **Verdict / label:** the verdict (`trash` or `valid`) and the label.
> - **Canned comment:** the text to use (lightly adapted with the specific
>   detail — a file name, a referenced issue number, etc.) instead of
>   writing a new diagnosis. Same rules as any other comment: English,
>   1-4 sentences.
>
> The model treats these as a fast path, not a blind rule — it still
> verifies the pattern actually applies before using its label, and falls
> back to ordinary analysis (`instructions.md`) for anything only
> superficially similar.

## Example patterns (replace with your repo's actual recurring cases)

### Pattern: wrong repository / third-party fork
**Recognize when:** the issue describes a bug in, or asks a question about,
a fork, a differently-named similar project, or software this repo doesn't
produce at all — no real connection to this codebase.
**Verdict / label:** valid / invalid
**Canned comment:**
> This doesn't describe an issue with this repository specifically — it
> looks like it belongs to a different or forked project. Please reopen it
> against the correct repository.

### Pattern: issue template ignored, no reproduction info
**Recognize when:** the body exists (so it's not "trash") but skips the
required template sections entirely — no steps to reproduce, no version, no
file reference — leaving nothing concrete to check.
**Verdict / label:** valid / needs-info
**Canned comment:**
> Not enough information to act on this: please fill in the issue template
> — reproduction steps, the version/commit you're on, and the specific
> file(s) involved.

### Pattern: known/by-design behavior mistaken for a bug
**Recognize when:** the described "bug" matches something explicitly listed
in `repo_context.md` under "Known limitations" or "What's out of scope".
**Verdict / label:** valid / invalid
**Canned comment:**
> This is expected behavior, not a bug — see the "Known limitations"
> section of this project's docs. [fill in the specific reason from
> repo_context.md]

### Pattern: PR only touches generated/lockfile output
**Recognize when:** the diff changes only lockfiles or other generated/build
artifacts, with no corresponding source change to justify it.
**Verdict / label:** valid / needs-work
**Canned comment:**
> This PR only touches generated/lockfile output with no accompanying
> source change — that's usually an accidental commit or a missing actual
> fix. Please include the source change that produced this diff.

---

## Add your own patterns below

<!-- Copy the format above. Delete the examples once you've replaced them
     with cases that actually recur in this repo — the closed issues/PRs
     labeled "invalid", "duplicate", or "needs-work" are the best source
     for finding them. -->
