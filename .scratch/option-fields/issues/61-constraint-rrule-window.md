# 61: RRule windows in constraints

| | |
|---|---|
| GitHub | – (local tracker) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `57-value-types-order-ranges-lengths-durations` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** `eventConstraint` and `selectConstraint` take a window defined by an RRule, as a further `ConstraintValue.Window`, beside the recurring `BusinessHours` and the fixed spans of 57.

The library reads each constraint object with its entry parser (`normalizeConstraint` calls `parseEvents`), and the add-on loads the RRule plugin, so an object with `rrule`, `duration`, `exdate` and `exrule` is a window, as for an entry (`Entry.setRRule`). Found while writing 57 (2026-10-16). It was split off because the existing `RRule` class has neither `equals` nor a reader from JSON, which a form needs to round-trip through its converter (`VALUE_TYPES` in `ConverterTest`).

- `RRule` gets `equals`/`hashCode` and a reader for the JSON `toJson()` writes (object and string form), with the excluded dates and rules.
- The window holds an `RRule` and an optional duration of each occurrence, written as the add-on writes them for an entry (`rrule`, `duration`, `exdate`, `exrule`).
- The converter of `ConstraintValue` tells the window apart by its `rrule` key.

**Blocked by:** `57-value-types-order-ranges-lengths-durations`

**Status:** ready-for-agent

- [ ] The RRule window round-trips through `ConstraintValueConverter` (`VALUE_TYPES` in `ConverterTest`) and has its case in the switch of `ValueTypeSwitchTest`.
- [ ] `RRule` round-trips through its JSON in both forms.
