# 46: Migrate the remaining core tests

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `42-coverage-check` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Move every use of the old option API in the other tests of `addon` (`DisplayOptionsTest`, `AccessibilityTouchTest`, `JsCallbackTest`, `AutoBrowserSettingsTest`, `ToolbarOptionsTest`, `InteractionCallbacksTest`, `EventSourcesTest`, `ShortTimeValuesTest`, `SerializationTest`, `EntryTest`) to `options()`, `options().view(…)` or `setRawOption`. Tests of removed bookkeeping are deleted, not migrated. Spec: *Migration of callers*.

**Blocked by:** `42-coverage-check`

**Status:** ready-for-agent

- [ ] No use of `setOption`, `getOption`, `removeOption`, `setViewSpecificOption`, `getOptionOrDefault`, `Option` or `SchedulerOption` is left in the listed files.
- [ ] The migrated tests pass.
