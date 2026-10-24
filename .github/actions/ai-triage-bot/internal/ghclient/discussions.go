package ghclient

import (
	"context"
	"fmt"
)

// Discussion is the part of a GitHub Discussion the bot needs. GitHub has
// NO REST API for discussions at all — read and reply are both GraphQL-only,
// which is why this file exists separately from the REST-only rest of the
// client.
type Discussion struct {
	ID       string // GraphQL node ID — required by the reply mutation
	Number   int
	Title    string
	Body     string
	Category struct {
		Name string
	}
	Comments []DiscussionComment
}

// DiscussionComment is one existing comment in a discussion.
type DiscussionComment struct {
	Author string
	Body   string
}

// GetDiscussion reads a discussion by number, including its comment thread.
// Comments are capped at 100 — enough for the Q&A feature's context, and a
// discussion with more comments than that is already too long for the model
// to answer usefully anyway.
func (c *Client) GetDiscussion(ctx context.Context, owner, repo string, number int) (*Discussion, error) {
	const query = `
query($owner: String!, $repo: String!, $number: Int!) {
  repository(owner: $owner, name: $repo) {
    discussion(number: $number) {
      id
      number
      title
      body
      category { name }
      comments(first: 100) {
        nodes {
          author { login }
          body
        }
      }
    }
  }
}`

	var out struct {
		Repository *struct {
			Discussion *struct {
				ID       string `json:"id"`
				Number   int    `json:"number"`
				Title    string `json:"title"`
				Body     string `json:"body"`
				Category struct {
					Name string `json:"name"`
				} `json:"category"`
				Comments struct {
					Nodes []struct {
						Author *struct {
							Login string `json:"login"`
						} `json:"author"`
						Body string `json:"body"`
					} `json:"nodes"`
				} `json:"comments"`
			} `json:"discussion"`
		} `json:"repository"`
	}

	vars := map[string]any{"owner": owner, "repo": repo, "number": number}
	if err := c.doGraphQL(ctx, query, vars, &out); err != nil {
		return nil, err
	}
	repoNode := out.Repository
	if repoNode == nil || repoNode.Discussion == nil {
		return nil, fmt.Errorf("discussion #%d not found in %s/%s", number, owner, repo)
	}
	d := repoNode.Discussion
	res := &Discussion{
		ID:     d.ID,
		Number: d.Number,
		Title:  d.Title,
		Body:   d.Body,
	}
	res.Category.Name = d.Category.Name
	for _, n := range d.Comments.Nodes {
		author := ""
		if n.Author != nil {
			author = n.Author.Login
		}
		res.Comments = append(res.Comments, DiscussionComment{Author: author, Body: n.Body})
	}
	return res, nil
}

// AddDiscussionComment posts a reply to a discussion (by GraphQL node ID).
func (c *Client) AddDiscussionComment(ctx context.Context, discussionID, body string) error {
	const mutation = `
mutation($DiscussionID: ID!, $Body: String!) {
  addDiscussionComment(input: {discussionId: $DiscussionID, body: $Body}) {
    comment { id }
  }
}`
	return c.doGraphQL(ctx, mutation, map[string]any{
		"DiscussionID": discussionID,
		"Body":         body,
	}, nil)
}
