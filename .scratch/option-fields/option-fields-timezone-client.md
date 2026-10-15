# LOCAL: does the client get `local` or the resolved zone?

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement |
| Status | decided 2026-10-15 (maintainer's direction "catch the case", details left to Claude), merged into `option-fields-spec.md` |
| Blocked by | – (blocks the time-zone part of `option-fields-spec.md`) |

---

## Question

With `Timezone.LOCAL` (the new initial value), the server resolves the browser zone. Does the client then get the concrete zone (variant a, decided earlier in `option-fields-timezone.md`) or keep `local` while only the server holds the concrete zone (variant a′)?

## Facts found (2026-10-15, FullCalendar 7.1.0 source in `e2e-test-app/node_modules/fullcalendar`)

- FullCalendar compares the `timeZone` option as a string (`chunks/65e4f9c4.js:958-965`). `'local'` to `'Europe/Berlin'` is a change even when both are the same zone: a new `DateEnv`, a refetch of all non-static event sources (`reduceEventSourcesNewTimeZone`, `chunks/71d3d952.js:438`), a rezone of the loaded entries from their exact instants (same positions for the same zone), a rerender.
- The add-on's `handleTimeZoneChange` (`full-calendar.ts:821`) calls `refetchEvents()` again and sends `timezone-changed` to the server.
- So variant a costs an extra entry fetch (one or two, not checked whether they merge) on every page load: the client starts with `local`, the browser reports, the server sends the concrete zone.
- Variant a′ avoids the switch; the getter still names the zone the server computes with.
- Variant a also renders a stale zone after a `@PreserveOnRefresh` reload in another time zone until the report (edge case).
- Not checked: whether the dates the client sends to the server are formatted identically under `local` and a named zone.
- **Master has the same behaviour today, opt-in only** (code reading, not checked in the browser). With `withAutoBrowserTimezone()` the client starts with `timeZone: 'local'` (`createInitOptions`), `connectedCallback` reports the browser zone, and `setBrowserTimezone` (`FullCalendar.java`, `@ClientCallable`) calls `setOption(Option.TIMEZONE, browserTimezone)`, which switches the client from `local` to the concrete zone. With `LOCAL` as the new initial value it would hit every calendar.

## Decided (2026-10-15)

- While the calendar follows the browser, the sync sends `local` for `timeZone`; the field holds the concrete zone for the getter and the offset helpers. A browser report therefore changes nothing on the client: no switch, no refetch. A concrete zone set explicitly is sent as it is. The hook point is the existing `@ClientCallable` `setBrowserTimezone`.
- `handleTimeZoneChange` drops its own `refetchEvents()` if FullCalendar's refetch on a zone change already covers the add-on's entry source (to check in the implementation).
