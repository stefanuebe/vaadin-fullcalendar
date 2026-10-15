# 10: Option-name resource for the scheduler module

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | None (can start immediately) |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** As 09, in `addon-scheduler`, generated from `SchedulerOption`. `resourceColumns` is global by the add-on's choice.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] The resource lists every non-deprecated `SchedulerOption` key once, with its flag.
- [ ] The global flags are checked against the scheduler source of FullCalendar 7.1.
