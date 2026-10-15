# 23: Value types: constraints, business hours, entry limits

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-15) |
| Blocked by | `06-value-or-callback-day-cell-class` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Value types for `businessHours` (`true` or a list of hours), `eventConstraint`/`selectConstraint` (group id, `"businessHours"` or an object), `dayMaxEvents` (`false`, `true` or a number), `dayMaxEventRows` (boolean or a number). List values are immutable (`List.copyOf`), no arrays. Spec: *Options with more than one value form*, user stories 26, 27.

**Blocked by:** `06-value-or-callback-day-cell-class`

**Status:** done (2026-10-15)

- [x] Each type round-trips through its converter.
- [x] A list passed in and changed afterwards does not change the value.

**Notes from the implementation (2026-10-15):**

- The forms are nested records in each sealed interface, with constants and factories on the interface:
  - `BusinessHoursValue`: `NONE` (`false`), `DEFAULT` (`true`), `of(BusinessHours...)` / `Hours(List)` (an array of business hours objects). The ticket named `true` or a list; `false` is added because `businessHours` is per view, and without it a calendar-wide value cannot be turned off for one view.
  - `ConstraintValue` for `eventConstraint` and `selectConstraint`: `groupId(String)`, `BUSINESS_HOURS` (`"businessHours"`), `windows(BusinessHours...)` (an array of business hours objects; the library parses the constraint objects as entries, so it also takes an array of them). `groupId("businessHours")` is rejected, because the library reads that string as the business hours.
  - `EntryLimit` for `dayMaxEvents` and `dayMaxEventRows` (same kinds, one type): `NONE` (`false`), `CELL_HEIGHT` (`true`), `of(int)` (not negative; the library treats 0 like any other number). The Javadoc says how the library combines the two options: `true` on either wins, then a number on `dayMaxEvents`, then one on `dayMaxEventRows` (`resolveDayGridPlacementMode`).
- `Hours` and `Windows` copy their list (`List.copyOf`). `BusinessHours` now copies its set of days too (an unmodifiable `EnumSet`, which also keeps the client order of the days stable), so a set changed after `BusinessHours.of(set)` does not change the value either. Its `toJson()` compares the end with `LocalTime.MAX` by `equals` now, so a parsed end of day is written as `"24:00"` too.
- The converters read the client form back, so they round-trip in `ConverterTest`. A business hours object is read by `BusinessHoursValueConverter.toBusinessHours`, which `ConstraintValueConverter` uses as well. The older `BusinessHoursConverter` of the `Option` API stays until 51.
- The library also takes a constraint with concrete start and end dates (`{start, end}`, `normalizeConstraint` parses constraint objects as entries). `ConstraintValue` gets that form in 57 (maintainer 2026-10-15: full library coverage, a form is left out only for a concrete technical reason).
- `ValueTypeSwitchTest` now takes its list of types from `ConverterTest.VALUE_TYPES`, so a value type that `JsCallback` does not implement needs a switch too.
