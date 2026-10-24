package trigger

import "testing"

func TestSubstring(t *testing.T) {
	cases := []struct {
		name      string
		body      string
		phrase    string
		wantOK    bool
		wantRests []string // any of these is an acceptable remainder
	}{
		{"simple", "@github-discussions how do I build this?", "@github-discussions", true, []string{" how do I build this?"}},
		{"case-insensitive", "@GitHub-DISCUSSIONS please explain", "@github-discussions", true, []string{" please explain"}},
		{"absent", "just a normal comment", "@github-discussions", false, nil},
		{"empty phrase", "anything", "", false, nil},
		{"phrase at end", "look at @github-discussions", "@github-discussions", true, []string{""}},
	}
	for _, tc := range cases {
		t.Run(tc.name, func(t *testing.T) {
			rest, ok := Substring(tc.body, tc.phrase)
			if ok != tc.wantOK {
				t.Fatalf("ok = %v, want %v (rest = %q)", ok, tc.wantOK, rest)
			}
			if !tc.wantOK {
				return
			}
			if !contains(tc.wantRests, rest) {
				t.Errorf("rest = %q, want one of %q", rest, tc.wantRests)
			}
		})
	}
}

func TestSubstringOrFlexible(t *testing.T) {
	cases := []struct {
		name   string
		body   string
		phrase string
		wantOK bool
	}{
		{"exact", "@github-actions fix the parser", "@github-actions", true},
		{"space instead of hyphen", "@GitHub actions fix the parser", "@github-actions", true},
		{"comma separator", "@GitHub-actions, fix the parser", "@github-actions", true},
		{"underscore", "@github_actions fix the parser", "@github-actions", true},
		{"absent", "no mention here", "@github-actions", false},
		{"empty phrase", "anything", "", false},
	}
	for _, tc := range cases {
		t.Run(tc.name, func(t *testing.T) {
			_, ok := SubstringOrFlexible(tc.body, tc.phrase)
			if ok != tc.wantOK {
				t.Errorf("ok = %v, want %v", ok, tc.wantOK)
			}
		})
	}
}

// A phrase containing regex metacharacters must be matched literally, not as
// a pattern — otherwise a custom trigger could match far more than intended.
func TestSubstringOrFlexible_RegexMetacharactersAreLiteral(t *testing.T) {
	if _, ok := SubstringOrFlexible("run (please)", "(please)"); !ok {
		t.Error("expected the literal phrase with parentheses to match")
	}
	if _, ok := SubstringOrFlexible("run please", "(please)"); ok {
		t.Error("phrase with parentheses must not match text without them")
	}
}

func contains(list []string, s string) bool {
	for _, v := range list {
		if v == s {
			return true
		}
	}
	return false
}
