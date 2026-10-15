# 48: Migrate e2e Test views A–E

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `42-coverage-check` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Move every use of the old option API in the Test views of `e2e-test-app` from `AbstractEntryProviderView` to `ExternalDropTestView` (alphabetical); the Playwright specs of these views pass to `options()`, `options().view(…)` or `setRawOption`. Tests of removed bookkeeping are deleted, not migrated. Spec: *Migration of callers*.

**Blocked by:** `42-coverage-check`

**Status:** ready-for-agent

- [ ] No use of `setOption`, `getOption`, `removeOption`, `setViewSpecificOption`, `getOptionOrDefault`, `Option` or `SchedulerOption` is left in the listed files.
- [ ] The migrated tests pass.
