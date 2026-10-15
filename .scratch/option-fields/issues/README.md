# Option fields: tickets

Broken down from `../option-fields-spec.md` on 2026-10-15. The spec is the reference for every decision. A ticket only says which part of the spec it delivers.

## Order

The work runs in four stages, expand first, contract last:

1. **Expand (01–24):** the new sync, `options()`, the view options, the time zone, the add-on defaults, the sync hook and the value types. The old API (`setOption` and the rest) keeps working, because 01 makes it write into the raw-option map (bridge).
2. **Fields (25–41):** the remaining options as typed fields, in batches of about 15 to 35 options.
3. **Migrate (42–50):** 42 checks that every option has a field. Then the callers move, one batch per place.
4. **Contract and docs (51–56):** 51 deletes the old API and the bridge and runs the full gate. 52–56 write the wiki.

A ticket can start once every ticket in its *Blocked by* row is done.

## Branch

One integration branch `option-fields` for all 56 tickets, branched from `master` (maintainer, 2026-10-15). It reaches `master` through one pull request at the end. Each ticket is one or more commits on this branch. Do not branch from `prototype/option-fields`. That branch is reference only.

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
- has Javadoc with a short description and an `@see` link to its library docs page. The old Javadoc of the `Option` constant is the source for this text. Its `<dt>Type</dt>` blocks and its `setOption` examples are not carried over;
- has one hand-written setter per value form if it takes more than one form. Each setter has Javadoc that links to the getter (`{@link}`). There is no public setter that takes the value type itself.

A value form the value-type tickets (21–24) do not cover is a finding. Report it, do not invent a type in the batch.

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
| `16-header-toolbar-and-locale-initial-values.md` | `headerToolbar` and `locale` as server initial values | 04, 05 |
| `17-drop-addon-defaults.md` | Drop the add-on defaults `editable`, `weekNumbers` and the sticky options | 15, 16 |
| `18-sync-hook-entry-did-mount.md` | Protected sync hook, with `entryDidMount` as first user | 05 |
| `19-more-link-click.md` | `moreLinkClick` as a field through the sync hook | 04, 18 |
| `20-height-from-hassize.md` | `height` from `HasSize` through the sync hook | 18 |
| `21-value-types-render-hooks.md` | Value types for render hooks: content and header alignment | 06 |
| `22-value-types-other-callbacks.md` | Value types for the other value-or-callback options | 06 |
| `23-value-types-constraints-business-hours-day-max.md` | Value types: constraints, business hours, entry limits | 06 |
| `24-value-types-auto-false-now.md` | Value types: `"auto"`, `false` and date-or-date-time variants | 06 |
| `25-fields-dates.md` | Fields: dates, ranges and durations | 11, 12, 24 |
| `26-fields-time-grid.md` | Fields: time grid and slots | 11, 12, 21, 24 |
| `27-fields-interaction.md` | Fields: interaction | 11, 12, 22, 23 |
| `28-fields-entry-display.md` | Fields: entry display and layout | 11, 12, 21, 23, 24 |
| `29-fields-texts-formats.md` | Fields: texts, hints and formats | 11, 12, 22, 24 |
| `30-fields-toolbar-sources.md` | Fields: toolbar parts and entry sources | 11, 12, 16 |
| `31-hooks-days.md` | Render hooks: day cells, day headers, day lanes, all-day | 11, 12, 21 |
| `32-hooks-slots.md` | Render hooks: slots, now indicator, week numbers, tables | 11, 12, 21 |
| `33-hooks-entry-layouts.md` | Render hooks: block, column and row entries | 11, 12, 21 |
| `34-hooks-entries.md` | Render hooks: entries, background entries, list items | 11, 12, 18, 21 |
| `35-hooks-list-more-popover.md` | Render hooks: list view, more link, no entries, popover | 11, 12, 19, 21 |
| `36-hooks-view-toolbar.md` | Render hooks: view, toolbar, buttons, single month, nav links | 11, 12, 21 |
| `37-scheduler-options-object.md` | Scheduler options object and scheduler view options | 07, 10, 12 |
| `38-scheduler-resource-columns.md` | `resourceColumns` through the sync hook, resource re-add flush | 03, 18, 37 |
| `39-scheduler-fields.md` | Scheduler fields | 12, 21, 37 |
| `40-scheduler-hooks-cells-columns.md` | Scheduler render hooks: resource cells, columns, day headers, expander | 12, 21, 37 |
| `41-scheduler-hooks-groups-lanes.md` | Scheduler render hooks: groups, lanes, rows, timeline | 12, 21, 37 |
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
