# 22: Value types for the other value-or-callback options

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `06-value-or-callback-day-cell-class` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Sealed value types for: boolean or callback (`eventOverlap`, `selectOverlap`), CSS selector or callback (`dropAccept`), view name or callback (`navLinkDayClick`, `navLinkWeekClick`), text or callback (`moreLinkText`). Reuse one type where two options take the same kinds. The fields themselves come in the batches.

**Blocked by:** `06-value-or-callback-day-cell-class`

**Status:** ready-for-agent

- [ ] Each type round-trips through its converter.
- [ ] A test switches over each type without `default`.
