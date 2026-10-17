# 7.3 and the option API

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement |
| Status | direction decided 2026-10-18, the list below waits for the maintainer |
| Blocked by | `option-fields-spec.md` |

---

## Why

7.3 introduces the option API (`setOption(Option, …)`) and deprecates typed setters in its favour. 8.0 removes the option API again (`option-fields-spec.md`). 7.3 is not released and may still change, as long as nothing breaks against 7.2 (maintainer, 2026-10-15). It must not send its users to an API that 8.0 removes.

## To decide

- Only adjust deprecations and docs in 7.3 (no deprecation in favour of the option API), or backport the option fields to `v7_master` next to the existing API.
- What the 7.3 release notes and migration guide say about configuring a calendar.

## Decided

- `FullCalendar.withAutoBrowserTimezone()` and `FullCalendar.withAutoUiLocale()` (and the scheduler's overrides) are removed again from `v7_master`. They came with #273 on master and #295 on 7.x, and no release has them. In 7.2.x they exist only on `FullCalendarBuilder`, deprecated since 7.2.0; nobody has complained about that, so 7.3 keeps it simple (maintainer, 2026-10-16). The wiki pages that name them on branch `8.0` (`Release-notes-7.3`, `Migration-guide-7.2-to-7.3`, and for 8.0 `Release-notes-8.0`, `Migration-guide-7.3-to-8.0`, `Features`, `Samples`, `FullCalendar-Examples`) are fixed with the wiki tickets.
- Direction (maintainer, 2026-10-18): 7.3 makes the smallest cut. What 7.3 added and 8.0 still has stays, so code can move to it before 8.0. What only 7.3 has goes out again. Bug fixes stay. 7.3 is a release of its own, not only a step towards 8.0: the break to 8.0 is large, and users who move at all may need time.

## Proposed list (inventory of `7.2.4..v7_master`, 2026-10-18)

Stays, because 8.0 has it in the same form:
- Entry and Resource extended props, and the deprecation of custom properties and the description in their favour.
- `RemoteEntrySource`, `JsonFeedEntrySource`, `ICalendarEntrySource`, `GoogleCalendarEntrySource`, the remote entry events and the methods with the new names. The old client-side event source names stay as deprecated shells.
- The deprecations of `GroupEntriesBy` / `SchedulerOption.GROUP_BY_*` (no effect), of the 6-argument `Delta` constructor and of the builder's years/months (the builder exists in 8.0).
- All bug fixes, the Vaadin 25.3 and FullCalendar 6.1.21 bumps, the frontend move to `META-INF/frontend` (#252).

Goes out, because only 7.3 has it:
- `withAutoBrowserTimezone()` / `withAutoUiLocale()` (decided 2026-10-16).
- `getOptionOrDefault(…)`: 8.0 has no option API.
- The renamed option constants (`DAY_MAX_ENTRIES`, `VIEW_HINT`, `ENTRY_SOURCE_*`, …) and the deprecation of the old names: 8.0 has no option constants.
- `DateRange` as value of `Option.VALID_RANGE`, its converter, and the deprecation of `setValidRange*`: 8.0 has `options().setValidRange(option.DateRange)`, another package and another call.
- The `Map` toolbar via `setOption(HEADER_TOOLBAR, …)` with `NativeToolbarParts`, and the deprecation of `Header` / `Footer`: 8.0 has `options().setHeaderToolbar(ToolbarValue)` and `option.NativeToolbarParts`.
- The `FullCalendarBuilder` deprecation texts go back to their 7.2.4 wording where they name API only 7.3 has.
