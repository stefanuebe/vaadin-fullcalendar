# 74: Initial date with a time

| | |
|---|---|
| GitHub | – |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `25-fields-dates` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** `initialDate` takes a date and time, as the library does (`DateInput`; `getInitialDate` in `fullcalendar/chunks/65e4f9c4.js` keeps the time). The time matters for views with a duration in hours: `buildRangeFromDuration` starts the range at the start of the duration's unit (`54e239b5.js`), so a six-hour timeline starts at the given hour. The field takes `LocalDate` only. Found by 42 (2026-10-16). Decide the form with the maintainer (for example a value type like `NowValue`, date or date-time), following *Value types* in `README.md`.

**Decided by the maintainer (triage, 2026-10-17):** a value type `InitialDateValue` with the forms `Day` (`LocalDate`) and `DateTime` (`LocalDateTime`), built like `NowValue` but without the callback form, because the library takes no function for `initialDate` (`DateInput`). Setters: `setInitialDate(LocalDate)` as today and `setInitialDate(LocalDateTime)`, plus the one taking the value type. The setter table in `README.md` gets the row `InitialDateValue | LocalDate, LocalDateTime`. The Javadoc says the time is read in the calendar's time zone, as for `now`.

**Blocked by:** `25-fields-dates`

**Status:** ready-for-agent

- [ ] A date-time set as initial date reaches the client as the library reads it, a date as before.
- [ ] The setter table in `README.md` and `PER_FORM_PARAMETERS` have `InitialDateValue`; the converter tests include both forms.
- [ ] The Javadoc says how the time is read (time zone, as for `now`).
