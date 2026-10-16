# Option fields: tickets

Broken down from `../option-fields-spec.md` on 2026-10-15. The spec is the reference for every decision. A ticket only says which part of the spec it delivers.

## Order

The work runs in four stages, expand first, contract last:

1. **Expand (01–24, 57):** the new sync, `options()`, the view options, the time zone, the add-on defaults, the sync hook and the value types. The old API (`setOption` and the rest) keeps working, because 01 makes it write into the raw-option map (bridge).
2. **Fields (25–41):** the remaining options as typed fields, in batches of about 15 to 35 options.
3. **Migrate (42–50):** 42 checks that every option has a field. Then the callers move, one batch per place.
4. **Contract and docs (51–56):** 51 deletes the old API and the bridge and runs the full gate. 52–56 write the wiki.

A ticket can start once every ticket in its *Blocked by* row is done.

## Branch

Each ticket has its own branch from `master`, named `option-fields-<NN>`, and reaches `master` through its own pull request (maintainer, 2026-10-16). A ticket starts from `master` once the pull requests of its blockers are merged. Pushing and merging are the maintainer's step; the pull request is opened after the push, with `Closes #N` for the ticket's GitHub issue (section *GitHub*).

Until 2026-10-16 the tickets ran on one integration branch `option-fields`. Tickets 01–59 reached `master` from it in one step; ticket 60 was its last ticket and its own pull request. The state of `master` before, with the old option enums only, is kept as `wip_8_v1_option_enums`. Do not branch from `prototype/option-fields`. That branch is reference only.

## GitHub

The tickets live here, not on GitHub (maintainer, 2026-10-16). A ticket moves to GitHub only when its work starts: rewritten for the public (no review rounds, commit hashes or internal test names), created as one issue, one at a time, never several in a row. Its file then names the issue (`**GitHub:** #N`) and its status stays here. Done tickets stay here.

## For every ticket

- Read the spec sections the ticket names, and `STYLEGUIDE.md` before writing a browser test.
- Tests on the seams of the spec (*Testing Decisions*): browserless via `calendar.options()`, `options().view(…)` and `setRawOption`, with assertions on the element property `initialOptions` and on the calls sent to the client. Never on internal fields. Playwright only for behaviour that needs the client, with its own Test view in `e2e-test-app`.
- Run the tests that cover the change, then the three-agent review, then the gate, then commit (`CLAUDE.md`).
- Never push.
- When done, set *Status* in the ticket to `done (<date>, <commit>)` and tick the criteria.

## Field batches (25–41)

The batch tickets list the library option names. The lists were generated from the non-deprecated constants of `Option` and `SchedulerOption` on 2026-10-15. The generator missed a few constants (for example `headerToolbar` and `footerToolbar`, which are in 16). 42 catches every option that has no field.

Every field:

- has the Java name from *Names* in the spec: library name, with "event" written as "entry", plus `@JsonName` where the two differ;
- is a wrapper type (`Boolean`, `Integer`), not a primitive, and starts as `null`;
- sits in `CalendarOptions` if the option-name resource flags it as global, otherwise in `ViewOptions`;
- sits in the topic section of its batch in the source;
- has Javadoc with a short description and an `@see` link to its library docs page. A field without an initial value says that the getter returns null while the option is not set (58: "The getter returns null while the option is not set; then the library default applies."). The old Javadoc of the `Option` constant is the source for this text. Its `<dt>Type</dt>` blocks and its `setOption` examples are not carried over;
- has its getter and setters in the pinned method lists `FullCalendarOptions-api.txt` or `FullCalendarViewOptions-api.txt` (test resources, see 12). A new value type goes into `VALUE_TYPES` in `ConverterTest`, with its converter and a value of each form with the JSON the library reads for it, and gets a `form(…)` switch in `ValueTypeSwitchTest`; a field with a converter for a one-form type adds the type and its converter to `ONE_FORM_TYPES` in `ConverterTest`. A field of a new one-form type adds a value of it with its client JSON to `ONE_FORM_SAMPLES` in `TypedOptionsSyncTest`, which sets every field through each of its setters and checks what reaches the client; a new setter parameter type goes into `PARAMETER_SAMPLES` there, and a setter whose field writes that type differently gets its own entry in `SETTER_SAMPLES`. A field goes into `SYNCED_APART` only when the sync handles it apart and tests of its own cover it, never to turn that test green; the test checks that the named test class mentions the field. A `form(…)` switch in `ValueTypeSwitchTest` has no `default` branch;
- takes every form the library takes for the option, if it takes more than one: its `.d.ts` type and refiner in `e2e-test-app/node_modules`, and its docs page, not only the forms of the old `Option` Javadoc. The goal is full coverage of the library: a form is left out only for a concrete technical reason, recorded in the batch ticket (as the format strings in 24, which need a plugin the add-on does not load). "Nobody asked for it" or "the old API did not have it" is no reason. Its setters, each with Javadoc that links to the getter (`{@link}`), are hand-written:
  - one taking the value type itself, which also sets every form, those without data such as `HeightValue.AUTO` included (maintainer, 2026-10-15). A field whose value type has no setter per form (`HeaderAlignValue`) keeps the Lombok setter, which is this one;
  - one per form whose data is not itself a value type or a form (JDK types and add-on data classes such as `String`, `int`, `LocalDate`, `BusinessHours`), taking that data: the type of the form's record component or factory parameter, a primitive staying primitive (`setContentHeight(int)`; a field with one form keeps its Lombok setter, which takes the wrapper type of the field), a `List<X>` becoming `X...` (`setBusinessHours(BusinessHours...)`). A form with several factories gets one setter per factory parameter type (`ViewName.of(String)` and `ViewName.of(CalendarView)`); this includes an enum form with a factory (`Overlap.of(boolean)` gives `setEntryOverlap(boolean)`) and a map (`DateFormat.of(Map)` gives `setDayHeaderFormat(Map<String, Object>)`), so an app that holds the plain value needs no conversion (maintainer, 2026-10-15). A factory with more than one parameter gets no setter;
  - none taking a form itself, such as `JsCallback`, an enum form (`HeaderAlign`) or a record form (`DateRange`). The form implements the value type, so the setter taking the value type takes it already, and a second setter would only repeat it (maintainer, 2026-10-16). When two forms have the same data type, the one that shows the value as text keeps the data setter (`TextContent` beside the `{html}` content).

  The ticket that adds a value type adds its row to the setter table in *Value types* below. That table is binding. The wiki explains why both ways exist, the value type and the plain data (52).

A value form the value-type tickets (21–24, 57) do not cover is a finding. Report it, do not invent a type in the batch.

## Value types (21–24, 57)

- **Packages (58):** every type a user passes as an option value lives in `…fullcalendar.option` (also in the scheduler module, resource columns included, 59). New value types and forms go there. The options classes stay in the root package.

Decided by the maintainer on 2026-10-15, after a review of 21–24 together. The rules apply to every ticket that adds a value type, also 16 (toolbar) and 19 (`moreLinkClick`):

- A value type is a sealed interface named `<Kind>Value` (`HeightValue`, `EntryLimitValue`), also when one of its forms is a `JsCallback`. A form is named for what it is (`ClassName`, `TextContent`, `HeightValue.Pixels`).
- A form shared by several value types, and the value form of a value-or-callback type with a single value form, is a top-level type that implements each value type it belongs to (for example `JsCallback`, `TextContent`, `DateFormat`, `ClassName`, `HeaderAlign`, `Overlap`). Any other form is a record nested in its value type (`HeightValue.Pixels`).
- A form without data is a constant on its value type (`HeightValue.AUTO`, `EntryLimitValue.NONE`, `ListTextValue.NONE`). The library's `true` and `false` get constants named for their meaning in that option (`NONE`, `CELL_HEIGHT`, `NOT_STICKY`); an enum form carries its own (`Overlap.FORBIDDEN`). Where switching the option off is the library's `false`, the value type extends `NoneValue` and takes the shared `NONE` (58); its Javadoc says what `NONE` means for that option. Other names stay where `NONE` would read wrong (`StickyValue.NOT_STICKY` next to `STICKY`).
- A form with data gets a factory: `of(…)` when the value type has one form with data, otherwise `of<Form>(…)` (`ofPixels`, `ofCss`, `ofGroupId`, `ofDay`). A top-level form has its own `of(…)` (`TextContent.of`, `JsCallback.of`), which its value types do not repeat.
- The Javadoc of the value type lists its forms and says in one sentence why one Java type does not cover them.
- A converter writes each form as the library reads it and, reading back, throws `IllegalArgumentException` for a JSON kind no form has.
- In the options class, a field is followed by its setters.
- The setters per form of each value type, besides the one taking the value type. A value type added later adds its row here and in `PER_FORM_PARAMETERS` in `TypedOptionsSyncTest`, which checks every field's setters against it.

  | Value type | Setters per form take |
  |---|---|
  | `ClassNameValue` | `String` |
  | `ContentValue` | `String` (text) |
  | `CssSelectorValue` | `String` |
  | `HeaderAlignValue` | none |
  | `ViewNameValue` | `String`, `CalendarView` |
  | `OverlapValue` | `boolean` |
  | `DateFormatValue` | `Map<String, Object>` |
  | `ListDayFormatValue` | `Map<String, Object>` |
  | `ListTextValue` | `String` |
  | `HeightValue` | `int`, `String` |
  | `WidthValue` | `int` (pixels); no `double` setter for the percentage, because `setX(25)` and `setX(25.0)` would differ only by the literal |
  | `StickyValue` | `String` (offset) |
  | `EntryLimitValue` | `int` |
  | `BusinessHoursValue` | `BusinessHours...` |
  | `ConstraintValue` | `String` (group id), `BusinessHours...` (recurring windows), `ConstraintValue.Window...` (mixed windows) |
  | `NowValue` | `LocalDate`, `LocalDateTime` |
  | `TextValue` | `String` |
  | `DateRangeValue` | none |
  | `WeekNumberCalculationValue` | none |
  | `EntryOrderValue` | `String...` (property names), `EntryOrder.Key...` (mixed keys) |
  | `DurationValue` | `Duration`, `Period`, `String` (library format) |
  | `AutoBoolean` | none: one enum covers every form, its field keeps the Lombok setter |

**Parallel run (maintainer, 2026-10-15):** once 11, 12, 21–24 and 57 are done (57 only for the batches it blocks), the batches can run as parallel Sonnet subagents, each in its own worktree. Before that, one small commit creates the empty topic sections in `CalendarOptions` and `ViewOptions`, so each agent only adds to its own section and the merges stay free of conflicts. Each agent gets its own `~/.m2` copy and its own ports (see the memory on parallel agent isolation). Each batch is its own branch and pull request (section *Branch*). The main agent reviews each batch and runs the full gate on each branch. The empty topic sections go to `master` before the wave branches start. When one pull request of a wave is merged, the other branches of the wave are rebased onto `master` before they are merged. Propose the concrete waves to the maintainer before 25 starts. The first wave starts after 24 and a compact, not earlier, so the value types are fixed and the main agent has room for the reviews (decided 2026-10-15). On a merge, the pinned method lists (`*-api.txt`) are not merged by hand: run `OptionsApiTest` and take the actual list it prints.

## Index

| File | Ticket | Blocked by |
|---|---|---|
| `01-raw-option-sync.md` | Raw options and the new option sync replace the option bookkeeping | – |
| `02-detach-attach-resync.md` | A calendar keeps its options across detach, reattach and a new client element | 01 |
| `03-flush-before-client-calls.md` | Pending options are sent before a direct client call | 01 |
| `04-client-without-option-fallback.md` | The client keeps no option fallback of its own | 01, 02 |
| `05-options-object-first-fields.md` | `calendar.options()` with the first typed fields | 01 |
| `06-value-or-callback-day-cell-class.md` | A value-or-callback option: `dayCellClass` | 05 |
| `07-view-options.md` | Per-view options with `options().view(view)` and `clear()` | 05 |
| `08-server-built-views.md` | The server builds the complete `views` object | 04, 07 |
| `09-option-names-core.md` | Option-name resource for the core module | – |
| `10-option-names-scheduler.md` | Option-name resource for the scheduler module | – |
| `11-static-checks-core.md` | Static checks on the core option classes | 05, 07, 09 |
| `12-pinned-api-and-converter-roundtrip.md` | Pinned public API of the options objects and converter round trip | 06, 07 |
| `13-timezone-local-server.md` | Time zone field with `Timezone.LOCAL` (server) | 05 |
| `14-timezone-constructor-json-and-raw.md` | Time zone from constructor JSON, raw time zone rejected | 13 |
| `15-timezone-client.md` | Client time zone: no own default, no extra refetch | 04, 13 |
| `16-header-toolbar-and-locale-initial-values.md` | `headerToolbar` and `locale` as server initial values | 04, 05, 60 |
| `17-drop-addon-defaults.md` | Drop the add-on defaults `editable`, `weekNumbers` and the sticky options | 15, 16 |
| `18-sync-hook-entry-did-mount.md` | Protected sync hook, with `entryDidMount` as first user | 05 |
| `19-more-link-click.md` | `moreLinkClick` as a field through the sync hook | 04, 18, 60 |
| `20-height-from-hassize.md` | `height` from `HasSize` through the sync hook | 18, 60 |
| `21-value-types-render-hooks.md` | Value types for render hooks: content and header alignment | 06 |
| `22-value-types-other-callbacks.md` | Value types for the other value-or-callback options | 06 |
| `23-value-types-constraints-business-hours-day-max.md` | Value types: constraints, business hours, entry limits | 06 |
| `24-value-types-auto-false-now.md` | Value types: `"auto"`, `false` and date-or-date-time variants | 06 |
| `58-pr-363-review-round-1.md` | Packages, shared `NONE`, raw options on the options objects (PR #363 review) | 24 |
| `59-resource-columns-option-package.md` | Resource column classes in `option`, with internals for the scheduler | 58 |
| `57-value-types-order-ranges-lengths-durations.md` | Value types: entry order, ranges, week number calculation, lengths, durations | 24, 58 |
| `60-setters-without-form-overloads.md` | Remove the setters that take a form itself | 57 |
| `61-constraint-rrule-window.md` | RRule windows in constraints | 57 |
| `62-business-hours-past-midnight.md` | Business hours past midnight | 57 |
| `25-fields-dates.md` | Fields: dates, ranges and durations | 11, 12, 24, 57, 60 |
| `26-fields-time-grid.md` | Fields: time grid and slots | 11, 12, 21, 24, 57, 60 |
| `27-fields-interaction.md` | Fields: interaction | 11, 12, 22, 23, 57, 60 |
| `28-fields-entry-display.md` | Fields: entry display and layout | 11, 12, 21, 23, 24, 57, 60 |
| `29-fields-texts-formats.md` | Fields: texts, hints and formats | 11, 12, 22, 24, 57, 60 |
| `30-fields-toolbar-sources.md` | Fields: toolbar parts and entry sources | 11, 12, 16, 57, 60 |
| `31-hooks-days.md` | Render hooks: day cells, day headers, day lanes, all-day | 11, 12, 21, 60 |
| `32-hooks-slots.md` | Render hooks: slots, now indicator, week numbers, tables | 11, 12, 21, 60 |
| `33-hooks-entry-layouts.md` | Render hooks: block, column and row entries | 11, 12, 21, 60 |
| `34-hooks-entries.md` | Render hooks: entries, background entries, list items | 11, 12, 18, 21, 60 |
| `35-hooks-list-more-popover.md` | Render hooks: list view, more link, no entries, popover | 11, 12, 19, 21, 60 |
| `36-hooks-view-toolbar.md` | Render hooks: view, toolbar, buttons, single month, nav links | 11, 12, 21, 60 |
| `37-scheduler-options-object.md` | Scheduler options object and scheduler view options | 07, 10, 12 |
| `38-scheduler-resource-columns.md` | `resourceColumns` through the sync hook, resource re-add flush | 03, 18, 37 |
| `39-scheduler-fields.md` | Scheduler fields | 12, 21, 37, 57, 60 |
| `40-scheduler-hooks-cells-columns.md` | Scheduler render hooks: resource cells, columns, day headers, expander | 12, 21, 37, 60 |
| `41-scheduler-hooks-groups-lanes.md` | Scheduler render hooks: groups, lanes, rows, timeline | 12, 21, 37, 60 |
| `42-coverage-check.md` | Every supported option has a field | 13, 14, 17, 19, 20, 25–36, 38–41 |
| `43-migrate-addon-internals.md` | Migrate the add-on internals | 42 |
| `44-migrate-tests-calendar.md` | Migrate tests: `FullCalendarOptionsTest`, `FullCalendarTest` | 42 |
| `45-migrate-tests-advanced-typed.md` | Migrate tests: `AdvancedOptionsTest`, `TypedOptionValuesTest` | 42 |
| `46-migrate-tests-remaining-core.md` | Migrate the remaining core tests | 42 |
| `47-migrate-tests-scheduler.md` | Migrate the scheduler tests | 42 |
| `48-migrate-e2e-views-a.md` | Migrate e2e Test views A–E | 42 |
| `49-migrate-e2e-views-b.md` | Migrate e2e Test views F–Z | 42 |
| `50-migrate-demo.md` | Migrate the demo | 42 |
| `51-remove-old-option-api.md` | Remove the old option API | 43–50 |
| `52-wiki-calendar-configuration.md` | Wiki: `Calendar-configuration` describes the options object | 51 |
| `53-wiki-examples.md` | Wiki: every other example uses the new API | 51 |
| `54-wiki-release-notes.md` | Wiki: release notes 8.0 for the option fields | 51 |
| `55-wiki-migration-option-mapping.md` | Wiki: migration guide maps each `Option` constant to its field | 51 |
| `56-wiki-migration-methods-defaults.md` | Wiki: migration guide for removed methods and dropped defaults | 51 |
