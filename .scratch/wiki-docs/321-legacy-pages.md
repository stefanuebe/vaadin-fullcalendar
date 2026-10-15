# Wiki: decide on the legacy and unrelated pages

| | |
|---|---|
| GitHub | #321 |
| Labels | Docs, needs-triage |
| Status | open |
| Blocked by | – |

---

## What to decide

The wiki holds pages that are not, or no longer, documentation of the current add-on:

- the pages from the time before the current page names: `FullCalendar-Examples`, `FullCalendar-Functionality`, `FullCalendar-Scheduler`, `FullCalendar-Scheduler-Examples`, `FullCalendar-Scheduler-Functionality`, `FullCalendar-Scheduler-License`, `FullCalendar-MigrationGuides` and `FullCalendar-ReleaseNotes`. Some of them duplicate a current page, for example `FullCalendar-Scheduler-License` and `Scheduler-license`. Old release notes and migration guides still link to some of them.
- `MCP-Server`, which only says that the MCP server was discontinued.
- `signals-patterns-reference`, a reference on Vaadin Signals that is not about the add-on.

For each page the maintainer decides whether it stays as it is, becomes a short page that links to its current replacement, or is deleted. GitHub wikis have no redirects, so deleting a page breaks links to it from outside the wiki.

`Features` and `Samples` are not part of this decision. They stay and are reworked in #319. The link from `Features` to `MCP-Server` follows the decision on `MCP-Server`.

Maintainer's rule (2026-10-15): pages that are completely outdated are deleted from the wiki repository. The agent proposes, per page, whether it is completely outdated, has a current replacement, or is unrelated, and the maintainer approves the list. Links from other pages to a deleted page point to its replacement or are removed. The old release notes and migration guides also count: duplicates such as `Release-Notes-4.0.x` next to `Release-notes-4.0` are part of the list.

## Acceptance criteria

- [ ] The maintainer has decided for each page.
- [ ] The decision is carried out on the wiki branch `8.0`, and no wiki page links to a deleted page.

## Blocked by

None (can start immediately).
