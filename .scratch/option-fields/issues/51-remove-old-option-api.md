# 51: Remove the old option API

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `43-migrate-addon-internals`, `44-migrate-tests-calendar`, `45-migrate-tests-advanced-typed`, `46-migrate-tests-remaining-core`, `47-migrate-tests-scheduler`, `48-migrate-e2e-views-a`, `49-migrate-e2e-views-b`, `50-migrate-demo`, `67-option-by-option-review` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Delete `Option`, `SchedulerOption` and every method in *Removed API* of the spec, the deprecated typed methods (`setTimezone`/`getTimezone`, `setMaxEntriesPerDay…`, `setMoreLinkClickAction`, `setValidRange…`/`clearValidRange`), the bridge from 01 and 08, and the coverage test of 42 (its job is done; the option-name resources stay). Old code fails to compile. Spec: *Removed API*.

**Blocked by:** `43-migrate-addon-internals`, `44-migrate-tests-calendar`, `45-migrate-tests-advanced-typed`, `46-migrate-tests-remaining-core`, `47-migrate-tests-scheduler`, `48-migrate-e2e-views-a`, `49-migrate-e2e-views-b`, `50-migrate-demo`, `67-option-by-option-review`

**Status:** ready-for-agent

- [ ] Nothing in the repository references the removed API.
- [ ] Full gate green: `mvn clean install`, then the E2E run in `e2e-test-app`.
