# 25: Fields: dates, ranges and durations

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `11-static-checks-core`, `12-pinned-api-and-converter-roundtrip`, `24-value-types-auto-false-now` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Add the typed fields for these library options (23), named as in *Names* of the spec ("event" becomes "entry" in Java, `@JsonName` keeps the library name): `allDayMaintainDuration`, `dateAlignment`, `dateIncrement`, `dayCount`, `dayHeaders`, `defaultAllDay`, `defaultAllDayEventDuration`, `defaultTimedEventDuration`, `direction`, `duration`, `fixedWeekCount`, `forceEventDuration`, `headingLevel`, `hiddenDays`, `initialView`, `nextDayThreshold`, `now`, `showNonCurrentDates`, `validRange`, `visibleRange`, `weekends`, `weekNumbers`, `weekNumberCalculation`. `initialView` takes a `CalendarView`.

**Blocked by:** `11-static-checks-core`, `12-pinned-api-and-converter-roundtrip`, `24-value-types-auto-false-now`

**Status:** ready-for-agent

- [ ] Every field follows *Field batches* in `README.md` (Javadoc with `@see`, section, wrapper type, global or per view as the option-name resource says).
- [ ] The static checks (11) pass with the new fields; the pinned method list and the converter round trip (12) include them.
- [ ] A browserless test sets one field per value form of this batch and finds it in the client state.
- [ ] Playwright: `initialView` and `now` take effect.
