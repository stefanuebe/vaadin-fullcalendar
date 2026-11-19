# 74: Initial date with a time

| | |
|---|---|
| GitHub | #400 |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-17, ae47bb6c) |
| Blocked by | `25-fields-dates` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** `initialDate` takes a date and time, as the library does (`DateInput`; `getInitialDate` in `fullcalendar/chunks/65e4f9c4.js` keeps the time). The time matters for views with a duration in hours: `buildRangeFromDuration` starts the range at the start of the duration's unit (`54e239b5.js`), so a six-hour timeline starts at the given hour. The field takes `LocalDate` only. Found by 42 (2026-10-16). Decide the form with the maintainer (for example a value type like `NowValue`, date or date-time), following *Value types* in `README.md`.

**Decided by the maintainer (triage, 2026-10-17):** a value type `InitialDateValue` with the forms `Day` (`LocalDate`) and `DateTime` (`LocalDateTime`), built like `NowValue` but without the callback form, because the library takes no function for `initialDate` (`DateInput`). Setters: `setInitialDate(LocalDate)` as today and `setInitialDate(LocalDateTime)`, plus the one taking the value type. The setter table in `README.md` gets the row `InitialDateValue | LocalDate, LocalDateTime`. The Javadoc says the time is read in the calendar's time zone, as for `now`.

**Blocked by:** `25-fields-dates`

**Status:** done (2026-10-17, ae47bb6c)

- [x] A date-time set as initial date reaches the client as the library reads it, a date as before.
- [x] The setter table in `README.md` and `PER_FORM_PARAMETERS` have `InitialDateValue`; the converter tests include both forms.
- [x] The Javadoc says how the time is read (time zone, as for `now`).

**Done (2026-10-17):** `InitialDateValue` with `Day` and `DateTime`, nested records as in `NowValue`, and `InitialDateValueConverter`. A date-time is sent as a UTC string (`"2025-03-03T06:00Z"`); the library's `getInitialDate` passes it to `dateEnv.createMarker`, which reads it as an instant and converts it to the calendar's zone (checked in the library source during the review). The Javadoc follows `now`: read as UTC, shown in the calendar's zone. `setInitialDate(null)` needs a cast now, as for every field with setters per form. Review: two rounds with three agents (standards, spec, library integration and test quality), no major. Gate: `mvn clean install` green; E2E 463 passed, 2 failed, the date-dependent theme drag test of 76.
