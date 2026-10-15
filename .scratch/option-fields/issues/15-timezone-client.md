# 15: Client time zone: no own default, no extra refetch

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `04-client-without-option-fallback`, `13-timezone-local-server` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** The client's `autoBrowserTimezone` property and the line `timeZone: autoBrowserTimezone ? 'local' : 'UTC'` go away. `handleTimeZoneChange` drops its own `refetchEvents()` and keeps the `timezone-changed` dispatch; FullCalendar refetches on a zone change itself (`reduceEventSourcesNewTimeZone`). Spec: *Time zone*.

**Blocked by:** `04-client-without-option-fallback`, `13-timezone-local-server`

**Status:** ready-for-agent

- [ ] Playwright: a calendar with no time zone set shows times in the browser's zone (`LOCAL` resolves).
- [ ] Playwright: a browser report while following the browser causes no entry fetch.
- [ ] Playwright: an explicit zone change fetches the entries exactly once.
- [ ] `auto-browser-timezone.spec.js` passes or is adjusted.

**Note from 04 (2026-10-15):** `e2e-tests/tests/option-reset.spec.js` now has "removing the time zone gives FullCalendar's default, the browser's zone", run with the browser zone `Asia/Tokyo`. It still expects the calendar to start in UTC (10:00). With `LOCAL` as the initial value the calendar starts in the browser's zone (19:00); adjust the start expectation here.

**Note from 13 (2026-10-15):** the start expectation in `option-reset.spec.js` is adjusted already (19:00), and `auto-browser-timezone.spec.js` expects the client to keep `local` after the report. The Java side no longer sets the element property `autoBrowserTimezone`. Playwright now runs all specs with the browser zone UTC unless a spec sets its own.
