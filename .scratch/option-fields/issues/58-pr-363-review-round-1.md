# 58: Packages, shared `NONE` and raw options on the options objects (PR #363 review, round 1)

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-16) |
| Blocked by | `24-value-types-auto-false-now` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** The changes the maintainer asked for in the first review of draft PR #363 (2026-10-16). They go in before 57, because 57 adds value types that land in the new package and may take the shared `NONE`.

The maintainer reviewed the branch as draft PR #363. Decisions, all on this branch before 57:

- **Packages.** `…fullcalendar.option` holds every type a user passes as an option value: `AutoBoolean`, `NoneValue`, the `…Value` types, their forms (`ClassName`, `CssSelector`, `DateFormat`, `DateRange`, `HeaderAlign`, `Overlap`, `TextContent`, `ViewName`), and the existing `WeekNumberCalculation`, `Direction`, `NativeToolbarParts`, `NativeToolbarButtonDisplay`, `CalendarLocale`, `JsCallback`, `Timezone`, `BusinessHours`, `DisplayMode`; in the scheduler module the same package gets `EntryPrintLayout`. Breaking imports is accepted (major version, usually one or two wrapping classes per application). New value types of 57 and 25–41 go into `option`.
  - Stay in the root package: the options classes (`FullCalendarOptions`, `FullCalendarViewOptions`, `CalendarOptions`, `ViewOptions`, `AbstractOptions`, `OptionValueConversionException`), because they and `FullCalendar` use each other's package-private members. Moving them needs either public internals or a bridge; the maintainer chose to keep them in the root package instead (2026-10-16, after first deciding `option` + `option.value`). Same reason for the scheduler's resource column classes (`ResourceColumn`, `ResourceAreaColumn`, `ComponentResourceColumn`, `ComponentResourceAreaColumn`): `FullCalendarScheduler` drives their component lifecycle package-private. Also `CalendarView`/`CustomCalendarView`/`SchedulerView`, `Entry`, `RRule`, `RecurringTime`, `Delta`, the events, `FullCalendarTheme`/`FullCalendarPalette` and `ClientSideValue`.
  - Tests of a moved class that use its package-private members move along (`CalendarLocaleTest`).
- **One shared `None`.** `NoneValue` with the constant `NONE` and the record `None`, which `ListTextValue`, `ListDayFormatValue`, `EntryLimitValue` and `BusinessHoursValue` permit; it is the library's `false`. `StickyValue.NOT_STICKY` keeps its name (NONE vs. STICKY reads wrong). `AUTO` stays per type.
- **Raw options on the options objects.** An abstract base class gives `FullCalendarOptions` and `FullCalendarViewOptions` `setRawOption`/`getRawOption`; per view it is new and lays over that view's fields in `views` (the old `setViewSpecificOption` had it). `FullCalendar.setRawOption`/`getRawOption` go away. `getRawOption` names its deep copy in the Javadoc. A value that cannot be converted to JSON throws a dedicated exception class.
- **Attach.** `attachedUiId` is replaced by `isInitialAttach()` (Flow reports the `@PreserveOnRefresh` move and a client resync as initial attach), with a browserless test for the resync.
- `rawOptions` and `perViewOptions` become `HashMap`.
- **Javadoc of every field** says that the getter returns null while the value is not set (the library default applies). Rule for all later fields.
- `withAutoBrowserTimezone()` and `withAutoUiLocale()` are removed, with the scheduler overrides and `LocaleChangeObserver` (spec, *Removed API*).
- Kept as they are: `Boolean` fields (no `OptionBoolean`), `setTimeZone` after the library name, `@Delegate` (no removal announced; `delombok` is the exit), `getOption(String, boolean)` (goes with 51), `getTimezoneForOffsets()` (package-private).

**Blocked by:** `24-value-types-auto-false-now`

**Status:** done (2026-10-16)

- [x] Value types in `option` (core and scheduler); all callers, tests, Test views and demo views compile against the new package.
- [x] `NoneValue` replaces the four nested `None` records; switches and converters use `NoneValue.None`.
- [x] `setRawOption`/`getRawOption` on `FullCalendarOptions` and `FullCalendarViewOptions` through one abstract base class; a per-view raw option reaches the client under `views.<view>` and wins over that view's field; `clear()` removes it; `FullCalendar.setRawOption`/`getRawOption` are gone.
- [x] A value that cannot be converted to JSON throws the dedicated exception class.
- [x] `attachedUiId` is gone; a browserless test covers the client resync.
- [x] The Javadoc of every field without an initial value says the getter is null while the value is not set.
- [x] `withAutoBrowserTimezone()`, `withAutoUiLocale()` and `LocaleChangeObserver` are gone, callers migrated.
- [x] Review, gate, commit as for every ticket.
