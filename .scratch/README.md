# Local tickets (state 2026-10-15)

GitHub flagged the bot account `stefanuebe-bot` on 2026-10-15, right after it created
16 issues in one burst. Everything the bot authored is hidden (public API 404), for
example #319 and #340–#360. The maintainer asked GitHub Support for the reason and
for reinstatement. Until the account is back:

- no `gh` calls
- the files under `.scratch/` are the tickets, one folder per topic (`wiki-docs/`, `option-fields/`, `release/`); `/to-tickets` puts new ones into `<topic>/issues/`: work from them, and keep their *Status* row up to date
- the maintainer does not create the tickets by hand in the meantime

Each file starts with a table: GitHub number, labels, status, blocker. The body is
the ticket text.

## Index

| File | Ticket | Blocked by |
|---|---|---|
| `wiki-docs/319-topic-pages-spec.md` | #319 topic pages from Samples and Features (spec, approved) | – |
| `wiki-docs/topics/01-calendar-configuration.md` … `wiki-docs/topics/18-converters.md` | sub-issues of #319, one per topic page | each by the previous one |
| `wiki-docs/340-getting-started.md` | #340 Getting Started sets basic options | #319 |
| `wiki-docs/341-faq.md` | #341 FAQ without outdated answers | #319 |
| `wiki-docs/342-features-overview.md` | #342 Features as overview | #319 |
| `wiki-docs/344-documentation-sidebar.md` | #344 Documentation overview page and sidebar | #319 |
| `wiki-docs/320-writing-rules.md` | #320 remaining pages follow the writing rules | – |
| `wiki-docs/321-legacy-pages.md` | #321 decide on the legacy and unrelated pages | – |
| `wiki-docs/readme-8.0.md` | README describes 8.0 (not on GitHub) | – |
| `option-fields/initial-view-javadoc.md` | Javadoc of `INITIAL_VIEW` without `getClientSideValue()` (not on GitHub) | – |
| `option-fields/option-type-check.md` | `setOption` rejects values of the wrong type (not on GitHub, target 8.0) | – |
| `option-fields/option-fields-design.md` | options as typed fields (`ViewOptions`/`CalendarOptions`, Lombok): design source, superseded by the spec. Replaces `option-type-check.md` if the design holds | – |
| `option-fields/option-fields-spec.md` | spec for options as typed fields, merges design, time zone and defaults; reviewed, broken down into `option-fields/issues/` | – |
| `option-fields/issues/01…58` | the spec's tickets (expand, fields, migrate, contract, wiki); index and shared rules in `option-fields/issues/README.md` | see there |
| `option-fields/option-fields-timezone-client.md` | LOCAL: client keeps `local`, server holds the resolved zone (no refetch per page load); decided, in spec | – |
| `option-fields/option-fields-7.3.md` | what 7.3 does about the option API that 8.0 removes; open | `option-fields/option-fields-spec.md` |
| `option-fields/option-fields-timezone.md` | the time zone as an option field: `Timezone.LOCAL`, initial value `LOCAL`, no null; decided | `option-fields/option-fields-design.md` |
| `option-fields/option-fields-defaults.md` | drop the add-on's option defaults (timeZone, editable, weekNumbers, sticky); keep headerToolbar; decided | `option-fields/option-fields-design.md` |
| `release/343-release.md` | #343 release 7.3.0 and 8.0.0 | all of the above |

Order of work: first the option fields (`option-fields/issues/`, 58 tickets since 2026-10-16). If the field model holds, it changes every code sample in the wiki, so the topic pages wait for that decision. Then the topic pages in their numbered order, then #342, #344, #340, #341,
#320, #321, the README, then the release.

## Differences to GitHub

- `wiki-docs/topics/17-tooltips-with-tippy-js.md`: probably created as #361, but not linked as
  sub-issue of #319 and without the blocker on #360.
- `wiki-docs/topics/18-converters.md`: not created on GitHub.
- `wiki-docs/readme-8.0.md`: not created on GitHub.
- `option-fields/initial-view-javadoc.md`: not created on GitHub.
- `option-fields/option-type-check.md`: not created on GitHub.
- `option-fields/option-fields-design.md`: not created on GitHub.
- `option-fields/option-fields-timezone.md`: not created on GitHub.
- `option-fields/option-fields-defaults.md`: not created on GitHub.
- `option-fields/option-fields-spec.md`: not created on GitHub.
- `option-fields/option-fields-7.3.md`: not created on GitHub.
- `option-fields/option-fields-timezone-client.md`: not created on GitHub.
- `wiki-docs/344-documentation-sidebar.md`: the `Advanced` overview page is in this file, not in
  the GitHub issue.
- Status changes made here while GitHub is down.

## When the account is back

1. Check that the issues are visible again, including #361.
2. Bring GitHub in line with the list above, one call at a time with a pause of
   several seconds between calls. Ask the maintainer before creating more than about
   five issues.
3. If the content is gone for good, recreate it from these files the same way.

## Doc review server

`.doc-review/server.py 8090` serves the previews (http://172.17.0.2:8090/). `.doc-review/build.sh` rebuilds them; its `PAGES` list holds the pages under review (add each new topic page). Comments are saved per page into `.doc-review/comments.json`; handled ones go to `.doc-review/comments-done-*.json`. Since 2026-10-15 the server merges per page (before, one tab could overwrite another tab's comments).
