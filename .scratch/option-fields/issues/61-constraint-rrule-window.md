# 61: RRule windows in constraints

| | |
|---|---|
| GitHub | #379 |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-16) |
| Blocked by | `57-value-types-order-ranges-lengths-durations` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** `eventConstraint` and `selectConstraint` take a window defined by an RRule, as a further `ConstraintValue.Window`, beside the recurring `BusinessHours` and the fixed spans of 57.

The library reads each constraint object with its entry parser (`normalizeConstraint` calls `parseEvents`), and the add-on loads the RRule plugin, so an object with `rrule`, `duration`, `exdate` and `exrule` is a window, as for an entry (`Entry.setRRule`). Found while writing 57 (2026-10-16). It was split off because the existing `RRule` class has neither `equals` nor a reader from JSON, which a form needs to round-trip through its converter (`VALUE_TYPES` in `ConverterTest`).

- `RRule` gets `equals`/`hashCode` and a reader for the JSON `toJson()` writes (object and string form), with the excluded dates and rules.
- The window holds an `RRule` and an optional duration of each occurrence, written as the add-on writes them for an entry (`rrule`, `duration`, `exdate`, `exrule`).
- The converter of `ConstraintValue` tells the window apart by its `rrule` key.

**Blocked by:** `57-value-types-order-ranges-lengths-durations`

**Status:** done (2026-10-16)

- [x] The RRule window round-trips through `ConstraintValueConverter` (`VALUE_TYPES` in `ConverterTest`) and has its case in the switch of `ValueTypeSwitchTest`.
- [x] `RRule` round-trips through its JSON in both forms.

**Found by the review (2026-10-16):** an excluded rule with a positional weekday (such as `-1fr`) is written as a string, and the rrule plugin runs `parseRRuleObject` on every `exrule`, which probably mis-reads a string. This holds for entries too and is not specific to the window. Read from the library code, not run. Follows in 68.

**Done (2026-10-16):** `ConstraintValue.RRuleWindow` with `Window.ofRRule(RRule)` and `ofRRule(RRule, Duration)` (named per *of<Form>*; `Window.span` predates the rule). The window copies its rule (`RRule.copy()`, field by field), `RRule` compares by what the client gets (`toJson()` plus excluded dates and rules) and reads back with `RRule.fromJson`. Javadoc says what the library does without `dtstart` (starts now), without a time (whole-day windows) and without a duration (default entry duration). 3 review rounds, gate green (665+175 unit, 446 E2E).
