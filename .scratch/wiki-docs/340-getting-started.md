# Wiki 8.0: Getting Started sets basic options

| | |
|---|---|
| GitHub | #340 |
| Labels | Docs, ready-for-agent |
| Status | in progress (2026-10-15): page done and maintainer-reviewed on wiki `8.0` (first calendar view with INITIAL_VIEW/LOCALE/FIRST_DAY, subsections Options + Entries, in-memory provider, `fromCallbacks` example). Left: *Next steps* and the `Samples#entry-providers` link point to the #319 topic pages once they exist |
| Blocked by | #319 |

---

## Parent

#255

## What to build

`Getting-Started` takes the reader from the Maven setup to a first running calendar. Its basic usage example sets 2–3 basic options through the option API (for example the initial view, the locale and the first day of the week), so the reader sees the pattern used on every other page from the first step on.

The *Next steps* section links the Features page as the overview and the topic pages from #319.

## Where

The wiki is its own git repository. Work for 8.0 happens on its branch `8.0`, which is merged into the live wiki when 8.0 is released. Wiki-internal links stay full URLs.

## Acceptance criteria

- [ ] The basic usage example sets 2–3 basic options with `setOption` and compiles against 8.0.
- [ ] The links in *Next steps* point to the pages from #319.
- [ ] The page follows the *Writing* section of `STYLEGUIDE.md`. Together with #320 for this page.

## Blocked by

- #319 (for the links)
