# Wiki 8.0: Documentation overview page and sidebar

| | |
|---|---|
| GitHub | #344 |
| Labels | Docs, ready-for-agent |
| Status | open; the `Advanced` part is NOT yet in the GitHub issue |
| Blocked by | #319 |

---

## Parent

#255

## What to build

A new wiki page `Documentation` is the overview of the topic pages from #319: each topic with a link and one or two lines on what the reader finds there.

The sidebar (`_Sidebar.md`) shows the topic pages directly under `Documentation`, so they are easy to find. The order, as a first idea:

- Getting Started
- Features
- Documentation, with the topic pages nested below it (calendar configuration and options, entries and entry providers, recurrence, …; Themes and Stable class names belong to theming)
- FAQ
- Known Issues
- Release Notes
- Migration Guides
- Glossary
- Scheduler license

`Samples` is no longer in the sidebar.

The same applies one level down: a page `Advanced` is the overview of the advanced topic pages (A–I in #319), each with a link and one or two lines. In the sidebar, the advanced pages are nested under it.

## Where

The wiki is its own git repository. Work for 8.0 happens on its branch `8.0`, which is merged into the live wiki when 8.0 is released. Wiki-internal links stay full URLs.

## Acceptance criteria

- [ ] `Documentation` lists every topic page with one or two lines.
- [ ] `Advanced` lists every advanced topic page with one or two lines.
- [ ] The sidebar shows the topic pages under `Documentation`, and every link in it resolves.
- [ ] The list of key wiki pages in `CLAUDE.md` (*Documentation*) names the `Documentation` page.

## Blocked by

- #319
