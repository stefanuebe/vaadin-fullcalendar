# 21: Value types for render hooks: content and header alignment

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `06-value-or-callback-day-cell-class` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Sealed value types for the render-hook kinds still missing: content (text or `JsCallback`) and header alignment (`HeaderAlign` or `JsCallback`), with converters and a pattern for the setter overloads the batches copy. No fields yet beyond one each to prove the type. Spec: *Options with more than one value form*.

**Blocked by:** `06-value-or-callback-day-cell-class`

**Status:** ready-for-agent

- [ ] Each type round-trips through its converter (12).
- [ ] One field per type (`allDayHeaderContent`, `dayHeaderAlign`) reaches the client in both forms.
