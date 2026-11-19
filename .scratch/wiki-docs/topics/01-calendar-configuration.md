# 1. Wiki 8.0: topic page Calendar configuration

| | |
|---|---|
| GitHub | #345 |
| Labels | Docs, ready-for-agent |
| Status | in review (2026-10-15): page `Calendar-configuration` written on wiki `8.0`, three-agent review done (no major). Moved sections removed from Samples and Features, link in Release-notes-8.0 and Getting-Started updated. Open: maintainer review |
| Blocked by | none |
| Parent | #319 (`319-topic-pages-spec.md`) |

---

## What to build

The topic page **Calendar configuration** of the wiki. How the option API works: `setOption`, `getOption`, removing an option with `null`, initial options in the constructor, view-specific options, and options set with a string key (custom views). A few typical options show the pattern. The page does not list the constants of `Option`: their Javadoc has type, default and an example for each, and the page links there.

Content that moves here, see the table in `319-topic-pages-spec.md`: Samples: Passing custom initial options in Java, Setting the calendar's dimensions, View-specific options, Adding business hours, moreLinkClick (static value). Features: View-specific options.

## Acceptance criteria

- [ ] The sections listed above are on this page, and duplicates are merged into one version.
- [ ] The page starts with its most basic example and goes from general to specific.
- [ ] Claims and code samples match 8.0 (`master`). Notes on how 7.x differs stay with their content.
- [ ] The page follows the *Writing* section of `STYLEGUIDE.md` and the terms of `CONTEXT.md`.
- [ ] Links of other wiki pages to the moved sections point here, and no link or anchor points to a missing target.
- [ ] The three-agent review ran, and the maintainer reviewed the page.

## Where

The wiki is its own git repository. Work for 8.0 happens on its branch `8.0`. Wiki-internal links stay full URLs.
