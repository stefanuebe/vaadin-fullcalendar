# 54: Wiki: release notes 8.0 for the option fields

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `51-remove-old-option-api` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** Release notes 8.0: option fields, `options()`, `options().view(…)` (replaces `setViewSpecificOption`), `setRawOption`, the dropped defaults, the value types for options with several forms (`HeightValue.AUTO`, `EntryLimitValue.NONE`, `JsCallback` as one form) with the two ways to set a form (the short setter per form and the setter taking the value type, see 52); rewrite the note "Why are the typed setters and getters deprecated?" (`e4a6df9`). From 58: the option value types moved to the package `…fullcalendar.option` (imports change), `setRawOption`/`getRawOption` live on `options()` and `options().view(…)` (raw options per view are new), `NONE` is one shared constant (`NoneValue.NONE`, reachable as `ListTextValue.NONE` and the like), `withAutoBrowserTimezone()` and `withAutoUiLocale()` are gone (use `Timezone.LOCAL`; for the UI locale an own `LocaleChangeObserver` that sets `options().setLocale(…)`). On the local wiki branch `8.0`, written as the final state, reviewed per section with three agents, language by Opus. Spec: *Documentation*.

**Blocked by:** `51-remove-old-option-api`

**Status:** ready-for-agent

- [ ] The release notes name each item above.
