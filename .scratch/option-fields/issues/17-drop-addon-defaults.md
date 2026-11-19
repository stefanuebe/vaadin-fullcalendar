# 17: Drop the add-on defaults `editable`, `weekNumbers` and the sticky options

| | |
|---|---|
| GitHub | #388 |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-16, d58efa1a) |
| Blocked by | `15-timezone-client`, `16-header-toolbar-and-locale-initial-values` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** `createInitOptions` and `postConstruct` set none of `editable`, `weekNumbers`, `tableHeaderSticky`, `footerScrollbarSticky`; the guard for `OptionFieldsPrototype*` is not carried over. The library defaults apply. Spec: *Add-on defaults*, user story 39.

**Blocked by:** `15-timezone-client`, `16-header-toolbar-and-locale-initial-values`

**Status:** done (2026-10-16, d58efa1a)

- [x] The initial state contains none of the dropped options.
- [x] Playwright: a calendar with no options set has no week numbers, no draggable entries, non-sticky headers at full height and the browser's time zone.

**Note from 05 (2026-10-15):** the `editable` default is now the field (`viewOptions.setEditable(true)` in `postConstruct`, still skipped when the constructor JSON sets `editable`), so a raw `editable` from the old API lies over it and removing that raw value brings `true` back. Dropping the default removes that line.

**Notes from the implementation (2026-10-16):**

- `createInitOptions` starts from an empty object, `postConstruct` no longer sets `editable`. The skip branch for a constructor JSON `editable` went with it, and so did its two unit tests.
- `Option` Javadoc *Default* rows and `SchedulerOption.ENTRY_RESOURCE_EDITABLE` name the library defaults. `Entry.isEditable()` says that an entry without its own value is only draggable when the calendar sets `editable`; the getter itself keeps returning `true` for an unset entry.
- New `library-defaults.spec.js` with `LibraryDefaultsTestView`: no week numbers, no `fc-event-draggable`, sticky options unset (the library treats unset as `auto`, which sticks only for an automatic height), height `100%`, time zone `local`. With the old defaults put back, the week number, draggable and sticky tests fail.
- Every test view that drags entries or shows week numbers already set those options itself; the full E2E suite passed unchanged.
- Gate green: full build, E2E 463 passed.
- Wiki (56) and demo (50) cover the user-facing side.
