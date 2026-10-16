# 25: Fields: dates, ranges and durations

| | |
|---|---|
| GitHub | #371 |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-16, f0b04aaa) |
| Blocked by | `11-static-checks-core`, `12-pinned-api-and-converter-roundtrip`, `24-value-types-auto-false-now`, `57-value-types-order-ranges-lengths-durations`, `60-setters-without-form-overloads` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Add the typed fields for these library options (23), named as in *Names* of the spec ("event" becomes "entry" in Java, `@JsonName` keeps the library name): `allDayMaintainDuration`, `dateAlignment`, `dateIncrement`, `dayCount`, `dayHeaders`, `defaultAllDay`, `defaultAllDayEventDuration`, `defaultTimedEventDuration`, `direction`, `duration`, `fixedWeekCount`, `forceEventDuration`, `headingLevel`, `hiddenDays`, `initialView`, `nextDayThreshold`, `now`, `showNonCurrentDates`, `validRange`, `visibleRange`, `weekends`, `weekNumbers`, `weekNumberCalculation`. `initialView` takes a `CalendarView`.

**Value types from 57 (2026-10-16):** `validRange` and `visibleRange` take `DateRangeValue` (`DateRange` or `JsCallback`; `visibleRange` needs both bounds, the library ignores an open one, which the setter Javadoc says). `weekNumberCalculation` takes `WeekNumberCalculationValue` (`LOCAL`, `ISO` or `JsCallback`). `dateIncrement`, `duration`, `nextDayThreshold`, `defaultTimedEventDuration` and `defaultAllDayEventDuration` take `DurationValue`.

**Blocked by:** `11-static-checks-core`, `12-pinned-api-and-converter-roundtrip`, `24-value-types-auto-false-now`, `57-value-types-order-ranges-lengths-durations`, `60-setters-without-form-overloads`

**Status:** done (2026-10-16, f0b04aaa)

- [x] Every field follows *Field batches* in `README.md` (Javadoc with `@see`, section, wrapper type, global or per view as the option-name resource says).
- [x] The static checks (11) pass with the new fields; the pinned method list and the converter round trip (12) include them.
- [x] A browserless test sets one field per value form of this batch and finds it in the client state.
- [x] Playwright: `initialView` and `now` take effect.

**Note from 09 (2026-10-15):** `weekends` already exists in `ViewOptions` (the view tests of 07 and 08 use it as their simple per-view field). Do not add it again.

**Note from the work (2026-10-16):** `initialView` is a `ViewName` field, not a `CalendarView` field, because every option field type lives in package `option` (`ConverterTest`). `setInitialView(CalendarView)` and `setInitialView(String)` take the plain values. `hiddenDays` is a `Set<DayOfWeek>` with a hand-written setter that stores an unmodifiable copy.
