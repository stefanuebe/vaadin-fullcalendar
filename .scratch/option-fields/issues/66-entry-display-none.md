# 66: `eventDisplay` and the client value of `DisplayMode.NONE`

| | |
|---|---|
| GitHub | – (local tracker) |
| Labels | enhancement, ready-for-agent |
| Status | in progress (2026-10-16, branch `option-fields-66`) |
| Blocked by | `28-fields-entry-display` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Field `entryDisplay` (`@JsonName("eventDisplay")`, global per `option-names.txt`). The library takes `auto`, `block`, `list-item`, `background`, `inverse-background` and `none`. The existing enum `DisplayMode` has all six, but `NONE` writes `null` instead of `"none"`, so as an option value it would remove the option instead of hiding the entries.

`Entry.setDisplayMode(DisplayMode.NONE)` uses the same enum. It probably sends `display: null`, which the library reads as unset, so the entry would still show. Not verified yet, the ticket checks it first with a Playwright test.

Found by batch 28 (2026-10-16), which left the option out.

**Open decision:** change `NONE` to write `"none"` (fixes the entry too, if the check confirms the bug), or a separate value for the option.

**Blocked by:** `28-fields-entry-display`

**Status:** in progress (2026-10-16, branch `option-fields-66`)

- [ ] Playwright: an entry with `DisplayMode.NONE` is hidden (shows whether the entry has the bug).
- [ ] The field `entryDisplay` sends every library value, `none` included.
- [ ] The static checks, the pinned lists and the converter round trip include it.

**Decided by the maintainer (2026-10-16):** `NONE` writes `"none"` (the docs' value, "Won't render the event at all"; the client value itself does not matter as long as it works). The Javadoc must say that a `NONE` entry is still sent to the client with all its data and only not rendered, so nobody uses it to hide data. Facts come from the library docs, we build the server API only.

**Found while doing it (2026-10-16):** the existing Playwright test `displayMode NONE … renders as auto` documented the bug (entry visible). Also, `Entry` always sent `display: "auto"`, and an event's own `display` wins over the calendar's `eventDisplay` (library `refined.display || null`, then the event's value first), so the new calendar option would never reach an add-on entry. The docs say the root `eventDisplay` controls all events and an event's optional `display` controls specific ones, so the 1:1 mapping (maintainer: follow the docs) is: `Entry.displayMode` starts as null (not set, not sent), `AUTO` is an explicit value that is sent and keeps `auto` against the calendar. Rejected: sending nothing for `AUTO` (an explicit `AUTO` could not override the calendar) and leaving the option without effect.

**State (2026-10-16, uncommitted on `option-fields-66`, from `option-fields-wave-2` 5ed3eab5):** `DisplayMode.NONE("none")` with the Javadoc note; `Entry.displayMode` nullable without default, `setDisplayMode(null)` removes it; `DisplayModeConverter` reads null/empty as null; field `entryDisplay` (`@JsonName("eventDisplay")`, global, section *Entry display and layout* of `CalendarOptions`) with its two API lines; `EntryTest`/`EntryModelTest` updated (default null, not in JSON, `NONE` sent as `"none"`); `entry-properties.spec.js` now expects the `NONE` entry hidden; new calendar `cal-entry-display` in `TypedOptionsTestView` + Playwright test (calendar `BACKGROUND`, entry without own mode is the only `.fc-bg-event`, entry with `BLOCK` stays a normal entry). `mvn -o test -pl addon` green; E2E not run yet.

**Next:** 3-agent review of the branch diff (`git diff option-fields-wave-2`), gate (`mvn clean install`, rm prod.bundle, E2E), commit + "Record the commit", GitHub issue only when committing (one at a time). Then merge into `option-fields-wave-2`.
