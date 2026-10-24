// Package attachments pulls attachment links (screenshots, logged text
// files — anything the user drag-and-dropped into the GitHub issue/PR form)
// out of an issue/PR body and downloads them under hard limits:
//
//   - host must be on GitHub's own whitelist (no arbitrary external URLs —
//     this guards against SSRF: a pull_request_target event gives the bot a
//     write-scoped token while the PR body is fully controlled by the fork's
//     author);
//   - a size limit per attachment and a total byte budget for all
//     attachments in one run (guards against wasting budget/bandwidth on
//     bloated files);
//   - content type is sniffed from magic bytes, not from the file extension
//     or the Content-Type header (both easy to spoof) — this catches
//     renamed executables/archives;
//   - only two kinds of content are ever passed to the bot: images (as
//     multimodal model input) and plain text in an allowed encoding/
//     extension. Everything else is silently skipped.
//
// Nothing downloaded here is EVER executed or unpacked — files are only
// used as bytes (image) or as a string (text).
package attachments

import (
	"bytes"
	"context"
	"fmt"
	"io"
	"log"
	"net/http"
	"net/url"
	"path"
	"regexp"
	"strings"
	"time"
	"unicode/utf8"
)

// Kind is the type of a successfully recognized attachment.
type Kind string

const (
	KindImage Kind = "image"
	KindText  Kind = "text"
)

// Attachment is one attachment after safe processing.
type Attachment struct {
	URL      string
	Kind     Kind
	MIMEType string
	Data     []byte // set for KindImage
	Text     string // set for KindText (truncated to maxTextChars)
}

// SkipReason records one attachment that did NOT pass screening and was
// therefore never read: the model must be told this explicitly (not left
// to guess from a shorter attachment count) so it judges the corresponding
// claim on the surrounding text alone instead of silently assuming "no
// screenshot was provided" — see internal/triage/common.go's use of this
// and promptbuilder.writeAttachmentsSection.
type SkipReason struct {
	URL    string
	Reason string
}

// Limits are the configurable download limits.
type Limits struct {
	MaxCount      int
	MaxBytesEach  int64
	MaxBytesTotal int64
	Timeout       time.Duration
}

const maxTextChars = 8000

// attachmentURLRe matches the links GitHub generates when a file is
// drag-and-dropped into an issue/PR/comment: always on
// *.githubusercontent.com or github.com/.../assets|files/.... This is
// distinct from ordinary markdown links to code in the repo itself.
var attachmentURLRe = regexp.MustCompile(`https://(?:[a-zA-Z0-9_-]+\.)*githubusercontent\.com/\S+|https://github\.com/[A-Za-z0-9_.\-/]+/(?:files|assets)/\S+`)

// ExtractURLs finds every GitHub-attachment-like link in the text and
// returns them stripped of surrounding markdown/HTML punctuation.
func ExtractURLs(body string) []string {
	raw := attachmentURLRe.FindAllString(body, -1)
	seen := map[string]bool{}
	var out []string
	for _, u := range raw {
		u = strings.TrimRight(u, ")>\"'.,;")
		if u == "" || seen[u] {
			continue
		}
		seen[u] = true
		out = append(out, u)
	}
	return out
}

// allowedHost checks that a URL is on a domain GitHub itself owns — the
// only hosts we ever download from. Any other host (even one that "looks"
// like an attachment, or is claimed to be one in the PR body) is rejected
// without a request.
func allowedHost(u *url.URL) bool {
	if u.Scheme != "https" {
		return false
	}
	h := strings.ToLower(u.Hostname())
	if h == "github.com" {
		return true
	}
	if strings.HasSuffix(h, ".githubusercontent.com") {
		return true
	}
	return false
}

var textExtWhitelist = map[string]bool{
	".txt": true, ".log": true, ".md": true, ".json": true,
	".yaml": true, ".yml": true, ".csv": true, ".diff": true, ".patch": true,
}

// sniffImage checks image magic-byte signatures (bytes, not extension/Content-Type).
func sniffImage(b []byte) (mime string, ok bool) {
	switch {
	case len(b) >= 8 && bytes.Equal(b[:8], []byte{0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A}):
		return "image/png", true
	case len(b) >= 3 && b[0] == 0xFF && b[1] == 0xD8 && b[2] == 0xFF:
		return "image/jpeg", true
	case len(b) >= 6 && (string(b[:6]) == "GIF87a" || string(b[:6]) == "GIF89a"):
		return "image/gif", true
	case len(b) >= 12 && string(b[0:4]) == "RIFF" && string(b[8:12]) == "WEBP":
		return "image/webp", true
	}
	return "", false
}

// dangerousSignatures are the magic bytes of executables/archives. If an
// attachment starts with one of these, it's dropped regardless of the URL's
// extension — this is the content-level "malware" guard, rather than
// trusting a filename.
var dangerousSignatures = [][]byte{
	{'M', 'Z'},                         // Windows PE (.exe/.dll)
	{0x7F, 'E', 'L', 'F'},              // Linux ELF
	{'P', 'K', 0x03, 0x04},             // ZIP/JAR/APK/DOCX-as-archive
	{'R', 'a', 'r', '!'},               // RAR
	{'7', 'z', 0xBC, 0xAF, 0x27, 0x1C}, // 7-Zip
	{0x1F, 0x8B},                       // GZIP
	{0xCA, 0xFE, 0xBA, 0xBE},           // Mach-O / Java class
	{'%', 'P', 'D', 'F'},               // PDF (not whitelisted — not handled)
}

func hasDangerousSignature(b []byte) bool {
	for _, sig := range dangerousSignatures {
		if len(b) >= len(sig) && bytes.Equal(b[:len(sig)], sig) {
			return true
		}
	}
	return false
}

// Fetch downloads attachments under all the Limits. Errors for individual
// attachments (unreachable, over the limit, unsupported type) are logged
// AND returned as SkipReason entries — they don't fail the whole triage
// run, but they also aren't silently dropped anymore: the caller passes
// them to the model so it explicitly knows some attachments went unread
// and judges those parts of the claim on text alone, instead of the model
// having no way to distinguish "nothing was attached" from "something was
// attached but rejected".
func Fetch(ctx context.Context, urls []string, lim Limits) (out []Attachment, skipped []SkipReason) {
	var totalBytes int64
	client := &http.Client{Timeout: lim.Timeout}

	skip := func(u, reason string) {
		skipped = append(skipped, SkipReason{URL: u, Reason: reason})
	}

	for i, raw := range urls {
		if len(out) >= lim.MaxCount {
			log.Printf("attachments: reached the %d-item limit, skipping the rest", lim.MaxCount)
			for _, remaining := range urls[i:] {
				skip(remaining, fmt.Sprintf("per-run attachment count limit (%d) already reached", lim.MaxCount))
			}
			break
		}
		if totalBytes >= lim.MaxBytesTotal {
			log.Printf("attachments: total budget of %d bytes exhausted, skipping the rest", lim.MaxBytesTotal)
			for _, remaining := range urls[i:] {
				skip(remaining, "total attachment byte budget for this run was exhausted")
			}
			break
		}

		u, err := url.Parse(raw)
		if err != nil {
			log.Printf("attachments: failed to parse URL, skipping: %v", err)
			skip(raw, "the URL could not be parsed")
			continue
		}
		if !allowedHost(u) {
			log.Printf("attachments: host %q is not on the GitHub whitelist, skipping (SSRF guard)", u.Hostname())
			skip(raw, "not on the github.com/githubusercontent.com whitelist (SSRF guard)")
			continue
		}

		data, err := fetchLimited(ctx, client, raw, lim.MaxBytesEach)
		if err != nil {
			log.Printf("attachments: failed to download %q: %v", raw, err)
			skip(raw, "the download failed")
			continue
		}
		if int64(len(data)) > lim.MaxBytesEach {
			log.Printf("attachments: %q exceeds the %d-byte limit, skipping", raw, lim.MaxBytesEach)
			skip(raw, fmt.Sprintf("exceeds the %d-byte per-attachment size limit", lim.MaxBytesEach))
			continue
		}
		if totalBytes+int64(len(data)) > lim.MaxBytesTotal {
			log.Printf("attachments: %q doesn't fit in the remaining total budget, skipping", raw)
			skip(raw, "would exceed the remaining total attachment byte budget for this run")
			continue
		}

		if hasDangerousSignature(data) {
			log.Printf("attachments: %q looks like an executable/archive by byte signature, skipping (malware guard)", raw)
			skip(raw, "byte signature matches an executable/archive, blocked for safety")
			continue
		}

		if mime, ok := sniffImage(data); ok {
			out = append(out, Attachment{URL: raw, Kind: KindImage, MIMEType: mime, Data: data})
			totalBytes += int64(len(data))
			continue
		}

		ext := strings.ToLower(path.Ext(u.Path))
		if textExtWhitelist[ext] && utf8.Valid(data) {
			text := string(data)
			if len(text) > maxTextChars {
				text = text[:maxTextChars] + "\n... (truncated to save tokens) ..."
			}
			out = append(out, Attachment{URL: raw, Kind: KindText, MIMEType: "text/plain", Text: text})
			totalBytes += int64(len(data))
			continue
		}

		log.Printf("attachments: %q is an unrecognized or unsupported type (not an image, not a whitelisted text extension), skipping", raw)
		skip(raw, "not a recognized image format and not a whitelisted text extension")
	}

	return out, skipped
}

func fetchLimited(ctx context.Context, client *http.Client, rawURL string, maxBytes int64) ([]byte, error) {
	cctx, cancel := context.WithTimeout(ctx, client.Timeout)
	defer cancel()

	req, err := http.NewRequestWithContext(cctx, http.MethodGet, rawURL, nil)
	if err != nil {
		return nil, err
	}
	req.Header.Set("User-Agent", "ai-triage-bot")

	resp, err := client.Do(req)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode >= 300 {
		return nil, fmt.Errorf("HTTP %d", resp.StatusCode)
	}

	// Read one byte past the limit so we can tell "exactly at the limit"
	// apart from "over the limit" without risking memory blowup on a huge file.
	limited := io.LimitReader(resp.Body, maxBytes+1)
	data, err := io.ReadAll(limited)
	if err != nil {
		return nil, err
	}
	return data, nil
}
