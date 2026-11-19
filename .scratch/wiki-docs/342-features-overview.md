# Wiki 8.0: Features as overview

| | |
|---|---|
| GitHub | #342 |
| Labels | Docs, ready-for-agent |
| Status | open |
| Blocked by | #319 |

---

## Parent

#255

## What to build

`Features` becomes the overview of the wiki: what can the add-on do, and where do I start? One section per area from #319, each with a short paragraph, one small code snippet and a link to its topic page.

- The list of what the calendar can do is dropped. The add-on covers the library, so the list says nothing.
- The detailed examples of `Features` have already moved to the topic pages in #319. The snippet of `Features` is a short entry into the topic. It does not copy an example of the topic page, it leads into it.
- The anchors that other pages link into `Features` (for example from the release notes 8.0) point to the new sections or to the topic pages.

## Where

The wiki is its own git repository. Work for 8.0 happens on its branch `8.0`, which is merged into the live wiki when 8.0 is released. Wiki-internal links stay full URLs.

## Acceptance criteria

- [ ] Each area from #319 has one section with a short paragraph, one snippet that compiles against 8.0, and a link to its topic page.
- [ ] No wiki page links to an anchor of `Features` that no longer exists.
- [ ] The page follows the *Writing* section of `STYLEGUIDE.md`.

## Blocked by

- #319
