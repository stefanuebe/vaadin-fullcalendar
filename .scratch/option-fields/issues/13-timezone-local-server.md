# 13: Time zone field with `Timezone.LOCAL` (server)

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-15) |
| Blocked by | `05-options-object-first-fields` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** The field `timeZone` of type `Timezone`, initial value `LOCAL`, the library default. `Timezone.LOCAL` is `new Timezone(ZONE_ID_UTC, "local")`. The hand-written `setTimeZone` on `FullCalendarOptions` rejects `null` and sets or clears a follow-browser flag. While the flag is set, `setBrowserTimezone` (`@ClientCallable`) stores the reported zone and the sync sends `local`, so a report changes nothing on the client. A concrete zone is sent as it is. If the browser zone is known when `LOCAL` is set, it applies at once. The offset helpers use UTC until the report. `withAutoBrowserTimezone()` becomes a synonym for `setTimeZone(LOCAL)`. Spec: *Time zone*, user stories 34–38, 59.

**Blocked by:** `05-options-object-first-fields`

**Status:** done (2026-10-15)

- [x] `options().setTimeZone(null)` throws.
- [x] Before a report the getter returns `LOCAL` and the offset helpers use UTC.
- [x] A report sets the getter to the concrete zone and sends nothing to the client.
- [x] With `LOCAL`, a second report replaces the zone; with a concrete zone it does not.
- [x] `withAutoBrowserTimezone()` has the same effect as `setTimeZone(LOCAL)`.

**Notes from the implementation (2026-10-15):**

- The flag lives in `FullCalendar` (`followBrowserTimezone`, starts set), the rule in `applyTimeZone`, which `FullCalendarOptions.setTimeZone` calls. The field has no public Lombok setter, so `@Delegate` generates none. `currentOptions()` puts `local` for `timeZone` while the flag is set.
- The old API goes into the field: `setOption(Option.TIMEZONE, …)` and `setTimezone` call `options().setTimeZone` (`"local"` in any case gives `LOCAL`, null gives `LOCAL`, the library default). `getOption(Option.TIMEZONE)` returns the field, so it is never empty now. `getTimezone()` returns `LOCAL` before the report.
- `withAutoBrowserTimezone()` no longer sets the element property `autoBrowserTimezone`. The client line `timeZone: autoBrowserTimezone ? 'local' : 'UTC'` has no effect any more, because `initialOptions` always carries `timeZone`. 15 removes both.
- Until 14, a concrete constructor JSON `timeZone` is still a raw option: the client shows it, while the server follows the browser (before 13 it converted with UTC). Both are wrong; 14 puts it into the field.
- Playwright: the browser's zone is now pinned to UTC in `playwright.config.js`, because the calendar starts in the browser's zone and two specs assumed UTC on a machine in another zone. `auto-browser-timezone.spec.js` expects the client to keep `local` after the report. `option-reset.spec.js` expects the start in the browser's zone (19:00 in Tokyo), as the note in 15 asked.
- `getOption(Option.TIMEZONE, true)` returns what the client gets: `local` while the calendar follows the browser.
- Users notice: a calendar without a time zone set now shows the browser's zone, not UTC. Entries reach the client as UTC instants, so their displayed times shift. The release notes and the migration guide (52–56) must say so. The demo's settings dialog reads the zone with `getOptionOrDefault(TIMEZONE, UTC)`, which now gets `LOCAL` or the browser's zone instead of UTC.
- Mutation checks: without the `local` rule in the sync, two tests fail (a report would send the concrete zone). With the flag always set, three tests fail. With an empty `withAutoBrowserTimezone()`, its test fails.
