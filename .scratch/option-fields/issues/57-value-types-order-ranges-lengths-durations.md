# 57: Value types: entry order, ranges, week number calculation, lengths, durations

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `24-value-types-auto-false-now` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Value types for the multi-form options the review of 24 found in the batches, which none of 21–24 covers (forms from the `<dt>Type</dt>` blocks of `Option` and `SchedulerOption`):

- `eventOrder` (28): a string of property names, an array of names and functions, or a compare function (`JsCallback`).
- `validRange`, `visibleRange` (25): a `DateRange`, an object with `start` and/or `end` (`validRange` may be open-ended), or a `JsCallback` returning such an object.
- `weekNumberCalculation` (25): `"local"`, `"ISO"` (the enum `WeekNumberCalculation`), or a `JsCallback` returning the week number.
- `resourceColumnsWidth` (39): a number of pixels or a CSS width string. Check whether a shared length type with `HeightValue` (24) fits.
- `toolbarElements` (30): a map of element name to content, where each content is a text or a `JsCallback` (the kinds of `ContentValue`). Decide whether a map of `ContentValue` with its own converter covers it.
- `eventConstraint`, `selectConstraint` (27): a fixed period with `start` and `end`, as a further form of `ConstraintValue` from 23. The library parses constraint objects as entries (`normalizeConstraint`), so the dates follow the convention of entry dates (UTC, as `NowValue.DateTime` in 24); an array may mix periods and recurring windows.
- Durations (`dateIncrement`, `duration`, `nextDayThreshold`, `slotDuration`, `snapDuration`, `slotHeaderInterval` and the other duration options in 25 and 26): decide whether `java.time.Duration` covers all forms or whether a form with calendar units (months, years, as in `dateIncrement: {months: 1}`) is needed.

Goal: full coverage of the library (maintainer, 2026-10-15). A form is left out only for a concrete technical reason. Besides the options below, check each value type of 21–24 against the library's `.d.ts` types and refiners for forms the old `Option` Javadoc did not list, and add them here.

The list below comes from a sweep of every non-deprecated `Type` block in `Option` and `SchedulerOption` (2026-10-15): all other options with more than one form are covered by 21–24, by a single Java type the old API already used (`Direction`, `DisplayMode`, `DayOfWeek`, `Locale`, a `Set<DayOfWeek>`, an `Integer` where `null` is the library default), or by their own tickets (`height` 20, `moreLinkClick` 19, `timeZone` 13–15, the toolbars 16).

Decide per option whether a value type is needed or one Java type covers all forms, and record the decision in the type's Javadoc, as in 24.

**Blocked by:** `24-value-types-auto-false-now`

**Status:** ready-for-agent

- [ ] Each type round-trips through its converter (`VALUE_TYPES` in `ConverterTest`) and has a switch in `ValueTypeSwitchTest`.
- [ ] Each option above has a decided Java type, recorded in the ticket of its batch.
