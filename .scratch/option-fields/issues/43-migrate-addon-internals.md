# 43: Migrate the add-on internals

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-17, option-fields-migration) |
| Blocked by | `42-coverage-check` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Move every use of the old option API in `addon/src/main` and `addon-scheduler/src/main` outside the enums (`FullCalendar`, `FullCalendarScheduler`, `Scheduler`, entry sources, Javadoc references in `JsCallback`, `DateRange`, `Entry` and others) to `options()`, `options().view(…)` or `setRawOption`. Tests of removed bookkeeping are deleted, not migrated. Spec: *Migration of callers*.

**Blocked by:** `42-coverage-check`

**Status:** done

- [x] No use of `setOption`, `getOption`, `removeOption`, `setViewSpecificOption`, `getOptionOrDefault`, `Option` or `SchedulerOption` is left in the listed files.
- [x] The migrated tests pass.
