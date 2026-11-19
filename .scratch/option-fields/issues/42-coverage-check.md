# 42: Every supported option has a field

| | |
|---|---|
| GitHub | #398 |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-16), commit `0f1c1b43` |
| Blocked by | `13-timezone-local-server`, `14-timezone-constructor-json-and-raw`, `17-drop-addon-defaults`, `19-more-link-click`, `20-height-from-hassize`, `25-fields-dates`, `26-fields-time-grid`, `27-fields-interaction`, `28-fields-entry-display`, `29-fields-texts-formats`, `30-fields-toolbar-sources`, `31-hooks-days`, `32-hooks-slots`, `33-hooks-entry-layouts`, `34-hooks-entries`, `35-hooks-list-more-popover`, `36-hooks-view-toolbar`, `38-scheduler-resource-columns`, `39-scheduler-fields`, `40-scheduler-hooks-cells-columns`, `41-scheduler-hooks-groups-lanes` |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** A test compares every non-deprecated `Option` and `SchedulerOption` key (in the end every key, see the notes) with the fields (core and scheduler) and fails on a key without a field, apart from a short documented exception list (for example `height`, which `HasSize` covers). Deprecated constants are add-on aliases of the same library option and get no field; the migration guide maps them. This is the gate before callers move.

**Blocked by:** `13-timezone-local-server`, `14-timezone-constructor-json-and-raw`, `17-drop-addon-defaults`, `19-more-link-click`, `20-height-from-hassize`, `25-fields-dates`, `26-fields-time-grid`, `27-fields-interaction`, `28-fields-entry-display`, `29-fields-texts-formats`, `30-fields-toolbar-sources`, `31-hooks-days`, `32-hooks-slots`, `33-hooks-entry-layouts`, `34-hooks-entries`, `35-hooks-list-more-popover`, `36-hooks-view-toolbar`, `38-scheduler-resource-columns`, `39-scheduler-fields`, `40-scheduler-hooks-cells-columns`, `41-scheduler-hooks-groups-lanes`

**Status:** done (2026-10-16)

- [x] The test passes; every exception is listed with its reason.
- [x] The option-name resources and the fields agree.
- [x] Every form the library takes for an option (its `.d.ts` type and refiner, and its docs page) has a setter on its field, or the batch ticket gives a concrete technical reason it is left out. The `<dt>Type</dt>` blocks of the old Javadoc (there until 51) are checked too, as a second source. (Five gaps without a reason became tickets 70–74, see the notes.)

**Notes from the implementation (2026-10-16):**

- Criteria 1 and 2: `OptionFieldsStaticTest.everyOptionKeyHasAField`, `everyResourceNameHasAField`, `everyExceptionAndAlias_stillApplies`; the one exception is `height` (set by `HasSize`), and `class` is listed as the library's second name of `className` (`CALENDAR_ONLY_OPTION_REFINERS`), so the test checks that it maps to the field; `SchedulerOptionFieldsStaticTest.everySchedulerOptionKeyHasAField`, `everyResourceNameHasAField`. Deprecated constants are checked too: all are aliases, their keys have fields. Green; mutation checks (the `height` exception removed, `timelineTopClass` removed, the alias pointed to a missing field) fail them.
- Criterion 3 (gaps 1–5 are now tickets 70–74, maintainer 2026-10-16): audit of all 357 options by six agents (table generated from the fields, the pinned lists, the `.d.ts` and the old `Type` blocks), findings verified against the library source. Gaps without a recorded reason; a new form is not invented in 42 (README), so each became its own ticket and 42 closes with them open. They do not block the migration (43–50):
  1. (70) `DurationValue` has no weeks form. `{weeks: n}` sets `specifiedWeeks` (`@full-ui/headless-calendar/index.js` `parseObject`), so `greatestDurationDenominator` returns `week`; it aligns `duration`/`dateIncrement` ranges (`54e239b5.js:120,161`) and the scheduler's slot units/labels (`fc014b1a.js:171,425`). The converter writes days only. The old `DURATION` Type listed `weeks`, and the `duration` Javadoc promises the week alignment.
  2. (71) `ConstraintValue` (`eventConstraint`, `selectConstraint`): a constraint object is parsed as an entry (`normalizeConstraint` → `parseEvents`), so the scheduler reads its `resourceId`/`resourceIds` (`0a9e97be.js` `filterConstraints`); docs `eventConstraint.md`, *Resources*. No window form carries resource ids.
  3. (72) `ResourceColumn` (`resourceColumns`, `ColSpec` in `dc3ec45f.d.ts`): no `headerInnerClass`, `cellInnerClass`, `headerResizerClass`, `isMain`; content only text or callback, no `{html}`; `withWidth(String)` only, while the library takes a number (pixels) and reads strings only with `px` or `%` (`parseDimConfig`), so its Javadoc "any CSS width value" is wrong.
  4. (73) `locale`: the library also takes a list of codes (`LocaleCodeArg = string | string[]`, tried in order, `queryRawLocale`) and a locale object (`LocaleInput`, `buildLocale`). The field takes one `java.util.Locale`.
  5. (74) `initialDate`: the library takes a date-time (`DateInput`); `buildRangeFromDuration` starts the range at `startOf(date, unit)`, so the time matters for views with an hour duration. The field takes `LocalDate` only.
- Checked and not gaps: `now` strings (24 records that the typed forms replace them), command-string formats (24), `{domNodes}` (57), `timeZone`/`LocalTime` string conveniences of the old API (every library form stays reachable), comma-string orders (`Order.of(String...)` covers the list), `navLink*Click` view names (64).
- Review: two rounds, no major. Round 1 split the alias `class` from the exception list.
