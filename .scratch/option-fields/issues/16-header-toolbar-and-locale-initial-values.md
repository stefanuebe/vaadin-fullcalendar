# 16: `headerToolbar` and `locale` as server initial values

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `04-client-without-option-fallback`, `05-options-object-first-fields`, `60-setters-without-form-overloads` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** The fields `headerToolbar` and `footerToolbar` (value type: a toolbar or `false`, with their overloads and converter) and `locale`. `headerToolbar` starts as `false` and `locale` as `CalendarLocale.getDefaultLocale()`, as field initializers in the global data class. The client line for `headerToolbar` goes away. `withAutoUiLocale()` sets the `locale` field. `calendar.getLocale()` keeps its Vaadin meaning. Spec: *Add-on defaults*, user stories 40, 42.

**Note from the review of 21–24 (2026-10-15):** the toolbar value type follows the README section *Value types (21–24, 57)*: name, constant for `false`, factory, a setter taking the value type, and its row in the setter table.

**Blocked by:** `04-client-without-option-fallback`, `05-options-object-first-fields`, `60-setters-without-form-overloads`

**Status:** ready-for-agent

- [ ] The initial state has `headerToolbar: false` and the default locale.
- [ ] `calendar.getLocale()` still returns the UI locale.
- [ ] ~~`withAutoUiLocale()` sets the `locale` field.~~ Dropped: `withAutoUiLocale()` is removed (PR #363 review, 2026-10-16, see the spec, *Removed API*).
- [ ] ~~Playwright: no FullCalendar toolbar on a calendar with nothing set.~~ Dropped: the library shows no toolbar by default either, so the test would pass without the add-on's initial value. The browserless test checks that `false` is sent.

**In progress (2026-10-16), branch `option-fields-16`, not committed yet:**

- Decisions (maintainer): `ToolbarValue` with forms `StartEnd`, `LeftRight` (purpose documented: screen sides that do not swap in RTL, checked in `parseToolbar`) and `NONE`; no per-form setter with three Strings; `headerToolbar` keeps the initial value `NONE` (consistent with other initial values, although the library default is also no toolbar); `locale` behaves as in 7.x: initial `CalendarLocale.getDefaultLocale()`, `setLocale(null)` throws, a JSON `"locale": null` in the constructor is dropped. Rejected: falling back to English or the UI locale on the client. Reason: without a locale the library takes the first of the passed `locales`, which is Afrikaans, and 7.x already rules that out.
- Done: `ToolbarValue`, `ToolbarValueConverter` (throws for unknown keys, non-string sections, mixed start/end with left/right), fields in `CalendarOptions`, constructor fallbacks and client `headerToolbar: false` removed, tests adapted and added, README setter-table row, pinned API list. All addon tests green.
- Review round 1: standards and tests/spec without major (minors fixed). FC axis found a major (the Afrikaans fallback), now fixed by the 7.x behaviour above.
- Next: review round 2 on the whole scope (three agents), then the extra round, then the gate, issue, commit, record, merge as for 14/15.
