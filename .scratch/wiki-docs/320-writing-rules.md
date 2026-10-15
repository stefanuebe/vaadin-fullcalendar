# Wiki 8.0: the remaining pages follow the writing rules

| | |
|---|---|
| GitHub | #320 |
| Labels | Docs, ready-for-agent |
| Status | in progress (2026-10-15): Home rewritten (capability intro approved by maintainer, grouped links, full URLs) and Getting-Started done on wiki `8.0`. Open: Themes, Stable-class-names, FAQ, Known-Issues, Scheduler-license, Migration-guides |
| Blocked by | – |

---

## Parent

#255

## What to build

The wiki pages that describe the current version and are not covered by #315 to #319 follow the writing rules and terms from #314:

- Home
- Getting-Started
- Themes
- Stable-class-names
- FAQ
- Known-Issues
- Scheduler-license
- Migration-guides (the index page)

Typical issues are "addon" instead of "add-on" and versions that do not say what they belong to, for example "The look of FullCalendar 6" in Themes. Other typical issues are lists of names inside a sentence and paragraphs that carry more than one kind of information.

Where a page uses a term from the [Glossary](https://github.com/stefanuebe/vaadin-fullcalendar/wiki/Glossary) in a way a reader could misread, it links the glossary.

Content changes have their own tickets: Getting-Started gets basic options in #340, the FAQ drops outdated answers in #341. This ticket covers only the writing rules on those pages.

## Where

The wiki is its own git repository. Work for 8.0 happens on its branch `8.0`, which is merged into the live wiki when 8.0 is released. Wiki-internal links stay full URLs.

## Acceptance criteria

- [ ] Each listed page follows the *Writing* section of `STYLEGUIDE.md` and the terms in `CONTEXT.md`.
- [ ] Every claim still matches the code of 8.0 (`master`).
- [ ] No content changes beyond what the rules require. Pages that need more than that get a note in this ticket for a follow-up.

## Blocked by

- #314
