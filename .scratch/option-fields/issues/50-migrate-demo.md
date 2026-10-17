# 50: Migrate the demo

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-17, option-fields-migration) |
| Blocked by | `42-coverage-check` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Move every use of the old option API in `demo/` (verified with `mvn -pl demo verify`) to `options()`, `options().view(…)` or `setRawOption`. Tests of removed bookkeeping are deleted, not migrated. Spec: *Migration of callers*.

**Blocked by:** `42-coverage-check`

**Status:** done

- [x] No use of `setOption`, `getOption`, `removeOption`, `setViewSpecificOption`, `getOptionOrDefault`, `Option` or `SchedulerOption` is left in the listed files.
- [x] The migrated tests pass.

**Left as they are after the review (2026-10-17), demo only, for the maintainer to judge:** `OptionResetPlayground` no longer offers `locale` (it cannot be removed any more) and parses `firstDay` (above 7 throws) and `now` (an ISO date) strictly. `CalendarViewToolbar`'s time zone selector shows UTC for a calendar on `Timezone.LOCAL`; that was so on master before the migration. `SettingsDialog` offers `Timezone.LOCAL`, so opening it no longer switches a LOCAL calendar to UTC.
