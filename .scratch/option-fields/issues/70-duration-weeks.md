# 70: CalendarDuration, durations in weeks

| | |
|---|---|
| GitHub | #399 |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-17, b8873763) |
| Blocked by | `57-value-types-order-ranges-lengths-durations` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** A duration given in weeks reaches the library as weeks. The library marks `{weeks: n}` as given in weeks (`parseObject` in `@full-ui/headless-calendar`), and `greatestDurationDenominator` then returns the unit `week`: `duration` and `dateIncrement` align their range to the start of the week (`fullcalendar/chunks/54e239b5.js`, docs `views/duration.md`, *Special Case*), the scheduler uses it for slot units and labels (`slotDuration`, `slotHeaderInterval`, `fullcalendar-scheduler/chunks/fc014b1a.js`). `{days: 7}` gives the unit `day` and no week alignment. Today `DurationValue` and its converter write days only: `Period` has no weeks field, `Period.ofWeeks(1)` is `P7D` and equals `Period.ofDays(7)`, so the converter cannot tell them apart and sends `{days: 7}`. The old API reached weeks through a map (`setOption(DURATION, Map.of("weeks", 1))`), and the Javadoc of the `duration` field already promises the week alignment. Found by 42 (2026-10-16).

**Decided by the maintainer (triage, 2026-10-17):**

- A new data type `CalendarDuration` in `…fullcalendar.option` replaces the sealed `DurationValue` with its forms `Time` and `Calendar`. It holds every unit the library reads, each on its own: years, months, weeks, days, hours, minutes, seconds, milliseconds. One week and seven days stay two different values, as in the library.
- Name: `CalendarDuration`, to show it is more than `java.time.Duration`, which takes times only. Rejected: `DurationValue` (by the rules a `…Value` is a sealed type with forms, which this no longer is), `CalendarTimespan` and `CalendarSpan` ("span" already means a range with start and end in the add-on: `ConstraintValue.DateSpan`, `DateTimeSpan`, "the shown timespan" in the Javadoc).
- Factories `ofYears`, `ofMonths`, `ofWeeks`, `ofDays`, `ofHours`, `ofMinutes`, `ofSeconds`, `ofMillis` and matching `plus…` methods; `ofTimeString(String)` stays; `of(Duration)` and `of(Period)` as shortcuts. The converter sends only the units that are not zero.
- Every duration field keeps its shortcut setters `setX(Duration)`, `setX(Period)` and `setX(String)`, and takes `setX(CalendarDuration)`. The Javadoc of `of(Period)` and of each `setX(Period)` says that `Period.ofWeeks(n)` arrives as `n * 7` days, without the week alignment, and points to `CalendarDuration.ofWeeks`.

The fields concerned: `dateIncrement`, `defaultAllDayEntryDuration`, `defaultTimedEntryDuration`, `duration`, `nextDayThreshold`, `scrollTime`, `slotDuration`, `slotHeaderInterval`, `slotMaxTime`, `slotMinTime`, `snapDuration`.

**Blocked by:** `57-value-types-order-ranges-lengths-durations`

**Status:** done (2026-10-17, b8873763)

- [x] `CalendarDuration` replaces `DurationValue`; every field listed above takes it, and the client receives each unit as the library reads it, `{weeks: n}` included.
- [x] `CalendarDuration.ofWeeks(1)` and `CalendarDuration.ofDays(7)` reach the client as `{weeks: 1}` and `{days: 7}`.
- [x] The shortcut setters for `Duration`, `Period` and `String` remain; the `Period` ones and `of(Period)` carry the note on `Period.ofWeeks`.
- [x] The rules and the setter table in `README.md` describe `CalendarDuration` as a data type without forms; `ConverterTest`, `ValueTypeSwitchTest` and `PER_FORM_PARAMETERS` in `TypedOptionsSyncTest` are updated.
- [x] The Javadoc of `duration` and `CalendarDuration` matches what is sent.

**Done (2026-10-17):** `CalendarDuration` is a record of the eight units; the converter sends an object of the units that are not zero, also for a time-only duration (before: an `"hh:mm:ss"` string). The library refines all eleven options with `createDuration`, which reads both alike, an empty object included. `of(Duration)` splits into hours, minutes, seconds and milliseconds with one sign; reading back takes a number of milliseconds, the library string and an object with the plural unit names and integer values. Review: two rounds with three agents (standards, spec, library integration and test quality), no major. Left as minors: the read-back does not take the singular unit names the library also reads (`week`, `ms`), because the add-on never writes them. Gate: `mvn clean install` green; E2E 463 passed, 2 failed in `vaadin-theme.spec.js` (drag out of the dark calendar), the same on `master`, a date-dependent test, see 76.
