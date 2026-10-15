# 12: Pinned public API of the options objects and converter round trip

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `06-value-or-callback-day-cell-class`, `07-view-options` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** A test pins the list of methods `FullCalendarOptions` and `FullCalendarViewOptions` expose and checks that neither has a public constructor, so no builder or helper leaks. A second test runs every value type through its converter and back. Both grow with each field batch. Spec: *Testing Decisions*, user story 53.

**Blocked by:** `06-value-or-callback-day-cell-class`, `07-view-options`

**Status:** ready-for-agent

- [ ] Adding a public method to an options object without updating the pinned list fails the test.
- [ ] The round trip covers `ClassNameValue`.
