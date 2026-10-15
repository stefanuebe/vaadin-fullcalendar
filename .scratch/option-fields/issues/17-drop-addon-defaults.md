# 17: Drop the add-on defaults `editable`, `weekNumbers` and the sticky options

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | ready-for-agent |
| Blocked by | `15-timezone-client`, `16-header-toolbar-and-locale-initial-values` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** `createInitOptions` and `postConstruct` set none of `editable`, `weekNumbers`, `tableHeaderSticky`, `footerScrollbarSticky`; the guard for `OptionFieldsPrototype*` is not carried over. The library defaults apply. Spec: *Add-on defaults*, user story 39.

**Blocked by:** `15-timezone-client`, `16-header-toolbar-and-locale-initial-values`

**Status:** ready-for-agent

- [ ] The initial state contains none of the dropped options.
- [ ] Playwright: a calendar with no options set has no week numbers, no draggable entries, non-sticky headers at full height and the browser's time zone.
