# 16: `headerToolbar` and `locale` as server initial values

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `04-client-without-option-fallback`, `05-options-object-first-fields` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** The fields `headerToolbar` and `footerToolbar` (value type: a toolbar or `false`, with their overloads and converter) and `locale`. `headerToolbar` starts as `false` and `locale` as `CalendarLocale.getDefaultLocale()`, as field initializers in the global data class. The client line for `headerToolbar` goes away. `withAutoUiLocale()` sets the `locale` field. `calendar.getLocale()` keeps its Vaadin meaning. Spec: *Add-on defaults*, user stories 40, 42.

**Note from the review of 21–24 (2026-10-15):** the toolbar value type follows the README section *Value types (21–24, 57)*: name, constant for `false`, factory, a setter taking the value type, and its row in the setter table.

**Blocked by:** `04-client-without-option-fallback`, `05-options-object-first-fields`

**Status:** ready-for-agent

- [ ] The initial state has `headerToolbar: false` and the default locale.
- [ ] `calendar.getLocale()` still returns the UI locale.
- [ ] ~~`withAutoUiLocale()` sets the `locale` field.~~ Dropped: `withAutoUiLocale()` is removed (PR #363 review, 2026-10-16, see the spec, *Removed API*).
- [ ] Playwright: no FullCalendar toolbar on a calendar with nothing set.
