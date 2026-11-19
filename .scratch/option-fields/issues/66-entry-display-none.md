# 66: `eventDisplay` and the client value of `DisplayMode.NONE`

| | |
|---|---|
| GitHub | #377 |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-16, 83e1c8f6) |
| Blocked by | `28-fields-entry-display` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Field `entryDisplay` (`@JsonName("eventDisplay")`, global per `option-names.txt`). The library takes `auto`, `block`, `list-item`, `background`, `inverse-background` and `none`. The existing enum `DisplayMode` has all six, but `NONE` writes `null` instead of `"none"`, so as an option value it would remove the option instead of hiding the entries.

`Entry.setDisplayMode(DisplayMode.NONE)` uses the same enum. It probably sends `display: null`, which the library reads as unset, so the entry would still show. Not verified yet, the ticket checks it first with a Playwright test.

Found by batch 28 (2026-10-16), which left the option out.

**Blocked by:** `28-fields-entry-display`

**Status:** done (2026-10-16, 83e1c8f6)

- [x] Playwright: an entry with `DisplayMode.NONE` is hidden (shows whether the entry has the bug).
- [x] The field `entryDisplay` sends every library value, `none` included.
- [x] The static checks, the pinned lists and the converter round trip include it.

**Decided by the maintainer (2026-10-16):** `NONE` writes `"none"` (the docs' value, "Won't render the event at all"; the client value itself does not matter as long as it works). The Javadoc must say that a `NONE` entry is still sent to the client with all its data and only not rendered, so nobody uses it to hide data. Facts come from the library docs, we build the server API only.

**Found while doing it (2026-10-16):** the existing Playwright test `displayMode NONE … renders as auto` documented the bug (entry visible). Also, `Entry` always sent `display: "auto"`, and an event's own `display` wins over the calendar's `eventDisplay` (library `refined.display || null`, then the event's value first), so the new calendar option would never reach an add-on entry. The docs say the root `eventDisplay` controls all events and an event's optional `display` controls specific ones, so the 1:1 mapping (maintainer: follow the docs) is: `Entry.displayMode` starts as null (not set, not sent), `AUTO` is an explicit value that is sent and keeps `auto` against the calendar. Rejected: sending nothing for `AUTO` (an explicit `AUTO` could not override the calendar) and leaving the option without effect.

**Done (2026-10-16):** besides the above, the review found that the client's `eventReceive` read `event.display`, which returns `'auto'` also for an entry without its own value, so an externally dropped entry became an explicit `AUTO`. It now reads the raw `_def.ui.display` (FC-UPDATE comment), and `DisplayModeConverter` reads a missing, empty or unknown value as not set. Tests: converter and JSON tests in `EntryModelTest`, `entry-properties.spec.js` (NONE hidden), `typed-options.spec.js` (calendar `BACKGROUND`, an entry without own mode follows it, an explicit `AUTO` keeps it), `external-drop.spec.js` (received entry has no display mode). 2 review rounds, gate green (655+175 unit, 446 E2E).
