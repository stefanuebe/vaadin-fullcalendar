# 51: Remove the old option API

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-17, 1b611f22) |
| Blocked by | `43-migrate-addon-internals`, `44-migrate-tests-calendar`, `45-migrate-tests-advanced-typed`, `46-migrate-tests-remaining-core`, `47-migrate-tests-scheduler`, `48-migrate-e2e-views-a`, `49-migrate-e2e-views-b`, `50-migrate-demo`, `67-option-by-option-review` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Delete `Option`, `SchedulerOption` and every method in *Removed API* of the spec, the deprecated typed methods (`setTimezone`/`getTimezone`, `setMaxEntriesPerDay…`, `setMoreLinkClickAction`, `setValidRange…`/`clearValidRange`), the bridge from 01 and 08, and the coverage test of 42 (its job is done; the option-name resources stay). Old code fails to compile. Spec: *Removed API*.

**Blocked by:** `43-migrate-addon-internals`, `44-migrate-tests-calendar`, `45-migrate-tests-advanced-typed`, `46-migrate-tests-remaining-core`, `47-migrate-tests-scheduler`, `48-migrate-e2e-views-a`, `49-migrate-e2e-views-b`, `50-migrate-demo`, `67-option-by-option-review`

**Status:** done

- [x] Nothing in the repository references the removed API.
- [x] Full gate green: `mvn clean install`, then the E2E run in `e2e-test-app`.

**Notes from the review of the migration (43–50, 2026-10-17):**

- "The coverage test of 42" is the part of `OptionFieldsStaticTest` and `SchedulerOptionFieldsStaticTest` that reads `Option.values()`/`SchedulerOption.values()`. The other checks in those files (scope, JSON names, entry naming, `@see` tags, Object methods) stay. `OptionCompletenessTest` (scheduler module) compares the fields with the library's key list and stays too.
- `SchedulerFeaturesTest` still tests the deprecated alias `setResourceAreaColumns`; that test goes with the alias.
- The old method bodies of `FullCalendar`/`FullCalendarScheduler` (`updateOption` with its `Option.TIMEZONE` and `ENTRY_DID_MOUNT` branches, the `RESOURCE_COLUMNS` branches of the scheduler's `setOption`) and the `Scheduler.setResourceColumns` Javadoc example go with the API.
