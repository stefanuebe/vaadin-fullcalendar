# 16: `headerToolbar` and `locale` as server initial values

| | |
|---|---|
| GitHub | #387 |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-16) |
| Blocked by | `04-client-without-option-fallback`, `05-options-object-first-fields`, `60-setters-without-form-overloads` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** The fields `headerToolbar` and `footerToolbar` (value type: a toolbar or `false`, with their overloads and converter) and `locale`. `headerToolbar` starts as `false` and `locale` as `CalendarLocale.getDefaultLocale()`, as field initializers in the global data class. The client line for `headerToolbar` goes away. `withAutoUiLocale()` sets the `locale` field. `calendar.getLocale()` keeps its Vaadin meaning. Spec: *Add-on defaults*, user stories 40, 42.

**Note from the review of 21–24 (2026-10-15):** the toolbar value type follows the README section *Value types (21–24, 57)*: name, constant for `false`, factory, a setter taking the value type, and its row in the setter table.

**Blocked by:** `04-client-without-option-fallback`, `05-options-object-first-fields`, `60-setters-without-form-overloads`

**Status:** done (2026-10-16)

- [x] The initial state has `headerToolbar: false` and the default locale.
- [x] `calendar.getLocale()` still returns the UI locale.
- [ ] ~~`withAutoUiLocale()` sets the `locale` field.~~ Dropped: `withAutoUiLocale()` is removed (PR #363 review, 2026-10-16, see the spec, *Removed API*).
- [ ] ~~Playwright: no FullCalendar toolbar on a calendar with nothing set.~~ Dropped: the library shows no toolbar by default either, so the test would pass without the add-on's initial value. The browserless test checks that `false` is sent.

**Notes from the implementation (2026-10-16):**

- Gate green: full build, E2E 459 passed. Mutation check: removing the null guard in `setLocale` or the null-locale drop in the constructor fails `AutoBrowserSettingsTest`.
- Decisions (maintainer): `ToolbarValue` with forms `StartEnd`, `LeftRight` (purpose documented: screen sides that do not swap in RTL, checked in `parseToolbar`) and `NONE`; no per-form setter with three Strings; `headerToolbar` keeps the initial value `NONE` (consistent with other initial values, although the library default is also no toolbar); `locale` behaves as in 7.x: initial `CalendarLocale.getDefaultLocale()`, `setLocale(null)` throws, a JSON `"locale": null` in the constructor is dropped. Rejected: falling back to English or the UI locale on the client. Reason: without a locale the library takes the first of the passed `locales` (`organizeRawLocales`), which is Afrikaans, and 7.x already rules that out.
- `ToolbarValueConverter` throws for unknown keys, non-string sections and a mix of start/end with left/right. The library tolerates the mix; the add-on rejects it because the server could not say which form it holds.
- The client line `headerToolbar: false` is gone; the server sends the field's `NONE` as `false`.
- `setOption(Option.HEADER_TOOLBAR, null)` drops the raw value, so the field's `false` applies again; the typed setter with null removes the option. A comment in `ToolbarOptionsTest` names the difference.
- The `@deprecated` notes of the toolbar model (`Header`, `Footer`, `AbstractHeaderFooter`, `HeaderFooterPartPosition`, `ToolbarConverter`) point to the typed setters and `ToolbarValue`.
