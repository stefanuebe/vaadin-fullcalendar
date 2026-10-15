# 23: Value types: constraints, business hours, entry limits

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `06-value-or-callback-day-cell-class` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Value types for `businessHours` (`true` or a list of hours), `eventConstraint`/`selectConstraint` (group id, `"businessHours"` or an object), `dayMaxEvents` (`false`, `true` or a number), `dayMaxEventRows` (boolean or a number). List values are immutable (`List.copyOf`), no arrays. Spec: *Options with more than one value form*, user stories 26, 27.

**Blocked by:** `06-value-or-callback-day-cell-class`

**Status:** ready-for-agent

- [ ] Each type round-trips through its converter.
- [ ] A list passed in and changed afterwards does not change the value.
