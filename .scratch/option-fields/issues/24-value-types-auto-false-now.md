# 24: Value types: `"auto"`, `false` and date-or-date-time variants

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `06-value-or-callback-day-cell-class` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Value types for: number or `"auto"` (`contentHeight`), boolean or `"auto"` (`tableHeaderSticky`, `footerScrollbarSticky`, `nowIndicatorSnap`), boolean, number or string (`slotHeaderSticky`), text or `false` (`listText`), format or `false` (`listDayAltFormat`), date or date-time (`now`). Decide per option whether a value type is needed or one Java type covers all forms; record the decision in the type's Javadoc.

**Blocked by:** `06-value-or-callback-day-cell-class`

**Status:** ready-for-agent

- [ ] Each type round-trips through its converter.
