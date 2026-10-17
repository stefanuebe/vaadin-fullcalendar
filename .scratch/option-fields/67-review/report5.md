I could not write `report5.md`: the Write tool refused, saying subagents must return findings as text. The report is below instead. Paths are relative to `e2e-test-app/node_modules/` unless they start with `addon`, `addon-scheduler`, `fc-docs` or `.scratch`. I skipped `scrollTime` and `slotDuration` (ticket 70). Library version is 7.1.0. I read the library code but did not run anything.

### rowEntryClass, rowEntryBeforeClass, rowEntryAfterClass, rowEntryInnerClass, rowEntryTimeClass, rowEntryTitleClass
- **Axis:** Javadoc
- **Where:** `addon/src/main/java/org/vaadin/stefan/fullcalendar/ViewOptions.java` field Javadocs at 1953-1966, 1986-1998, 2054-2066, 2122-2134, 2155-2168, 2190-2203.
- **Claim:** "Without a value, no classes are added." This sentence appears in all six.
- **Source:**
  - `fullcalendar/themes/classic/index.js:42-45` sets `rowEventClass`, `rowEventInnerClass`, `rowEventTimeClass` and `rowEventTitleClass` by default. Lines 118-119 set `rowEventBeforeClass` and `rowEventAfterClass`.
  - The add-on's default Vaadin theme builds on classic (`addon/src/main/resources/META-INF/frontend/vaadin-full-calendar/vaadin-theme.ts:21,49`).
  - A user value does not replace the theme's value. Option names matching `(^c|C)lass(Name)?$` are joined with the theme's names via `joinFuncishClassNames` (`fullcalendar/chunks/28b4e459.js:28-37,65-77`).
- **Fix:** "Class names set here are added to the class names of the active theme; they do not replace them."
- **Confidence:** high

### rowEntryClass, rowEntryBeforeClass, rowEntryAfterClass, rowEntryBeforeContent, rowEntryAfterContent, rowEntryInnerClass, rowEntryTimeClass, rowEntryTitleClass
- **Axis:** Javadoc (fact lost)
- **Where:** The same fields, `ViewOptions.java` lines 1953-2203.
- **Claim:** The new Javadocs say "row entries" and never explain the term. The old `Option.ROW_ENTRY_CLASS` (`addon/.../Option.java:4119`) said "(entries that render as horizontal bars: all-day entries in day grid and time grid views, and timeline entries)". It also said the hook applies in addition to the general entry hook.
- **Source:** `fc-docs/_docs-v7/events/row-event-render-hooks.md`: "These hooks apply in addition to the general Event Render Hooks and Block Event Render Hooks." Code: `fullcalendar/chunks/2d6c825e.js:1283-1288`.
- **Fix:** "Row entries are the entries drawn as horizontal bars: all-day entries in day grid and time grid views and entries in timeline views. This hook applies in addition to the general entry hook."
- **Confidence:** high

### rowEntryTitleSticky
- **Axis:** Javadoc
- **Where:** `ViewOptions.java:5322-5332`
- **Claim:** The default and the "documented only partly" note are gone. The old `Option.ROW_ENTRY_TITLE_STICKY` (`Option.java:4181-4195`) had both. The new text says "Themes can change the default", which no theme here does for row entries.
- **Source:**
  - The default is `rowEventTitleSticky: true` (`fullcalendar/chunks/642eba18.js:846`).
  - No page in `fc-docs/_docs-v7` mentions the option, so the `@see` page does not list it.
  - Monarch sets only `columnEventTitleSticky: false` (`fullcalendar/themes/monarch/index.js:157`). No theme and not `vaadin-theme.ts` set `rowEventTitleSticky`.
- **Fix:** "The library default is {@code true}. The library docs do not describe this option." Drop or soften the theme sentence.
- **Confidence:** high for the default and the missing docs mention, medium for the theme sentence.

### resourcesInitiallyExpanded
- **Axis:** Javadoc
- **Where:** `addon-scheduler/src/main/java/org/vaadin/stefan/fullcalendar/SchedulerViewOptions.java:62-70`
- **Claim:** The default is not stated. The old `SchedulerOption.java:162-173` had "Default: true".
- **Source:** `fc-docs/_docs-v7/views/resourcesInitiallyExpanded.md` ("default: `true`"). Code: `fullcalendar-scheduler/chunks/8f1316dc.js:2310`.
- **Fix:** "The library default is {@code true}."
- **Confidence:** high

### selectable, selectMirror, selectOverlap, selectMinDistance, scrollTimeReset, showNonCurrentDates, slotEntryOverlap, singleMonthMinWidth (defaults lost)
- **Axis:** Javadoc
- **Where:** `ViewOptions.java`:
  - `selectable` 4800-4809
  - `selectMirror` 4834-4842
  - `selectOverlap` 4844-4859
  - `selectMinDistance` 4821-4832
  - `scrollTimeReset` 4315-4322
  - `showNonCurrentDates` 4130-4137
  - `slotEntryOverlap` 4382-4390
  - `singleMonthMinWidth` 4325-4332
- **Claim:** None of these Javadocs names the library default. The old `Option` Javadocs all had a Default line. The old values were: `scrollTimeReset` true, `selectable` false, `selectMirror` false, `selectOverlap` true, `selectMinDistance` 0, `showNonCurrentDates` true, `slotEntryOverlap` true, `singleMonthMinWidth` 350. Other new Javadocs in the same class do state the default (for example `ViewOptions.java:5207`). If dropping defaults is a deliberate rule, ignore this finding. I found no such rule in `STYLEGUIDE.md` or the option-fields spec.
- **Source:**
  - Docs pages: `date-time/scrollTimeReset.md`, `selectable.md`, `selectMirror.md`, `selectOverlap.md`, `selectMinDistance.md`, `views/showNonCurrentDates.md`, `slotEventOverlap.md`, `singleMonthMinWidth.md`.
  - Code: `fullcalendar/chunks/642eba18.js:808` (`scrollTimeReset`) and `:832` (`selectable`).
  - Code: `fullcalendar/chunks/5342e739.js:1313` (`selectMinDistance || 0`).
  - Code: `fullcalendar/chunks/4ec5fb96.js:81` (`slotEventOverlap`).
  - Code: `fullcalendar/chunks/0ca0581f.js:358` (`singleMonthMinWidth: 350` in the multiMonth view config).
  - `selectOverlap` has no explicit default; an unset value allows everything (`fullcalendar/chunks/f0ab0579.js:172-181` calls only a function value).
  - For `showNonCurrentDates`, the `multiMonthYear` view overrides it to `false` (`0ca0581f.js:365`).
- **Fix:** One sentence each, for example "The library default is {@code false}." For `showNonCurrentDates`: "The library default is {@code true}, except in the {@code multiMonthYear} view, which turns it off."
- **Confidence:** high for the values, medium that the project wants them back.

### resourceGroupField, resourceGroupHeaderClass, resourceGroupHeaderInnerClass, resourceGroupHeaderContent, resourceGroupHeaderDidMount, resourceGroupHeaderWillUnmount, resourceGroupLaneClass, resourceGroupLaneInnerClass, resourceGroupLaneContent, resourceGroupLaneDidMount, resourceGroupLaneWillUnmount
- **Axis:** Javadoc (the library docs are silent, the code disagrees with what the Javadocs imply)
- **Where:** `SchedulerViewOptions.java:50-60` (`resourceGroupField`) and `:800-1042` (the ten hooks).
- **Claim:** The Javadocs present these as always effective. In the library they apply only when no `resourceColumns` entry has `group: true`. When one does, `resourceGroupField` and all group header and lane options are ignored, and the column's own hooks apply.
- **Source:** `fullcalendar-scheduler/chunks/8f1316dc.js:2093-2113`:
  ```js
  if (groupColSpecs.length) { groupSpecs = groupColSpecs; }
  else { ... options.resourceGroupField ... labelClass: options.resourceGroupHeaderClass ... }
  ```
  `fc-docs/_docs-v7/views/resourceGroupField.md` does not mention it. `resourceColumns.md:48-50` describes `group: true`.
- **Fix:** "It has no effect while a resource column is marked as a group column ({@code resourceColumns}); the library then groups by that column and ignores this option. The library docs do not say this."
- **Confidence:** medium

### resourceGroupHeaderDidMount, resourceGroupHeaderWillUnmount, resourceGroupLaneDidMount, resourceGroupLaneWillUnmount, resourceLaneDidMount, resourceLaneWillUnmount
- **Axis:** Javadoc (fact lost)
- **Where:** `SchedulerViewOptions.java:863-875`, `909-920`, `985-997`, `1031-1042`, `1160-1171`, `1235-1246`.
- **Claim:** The `info` fields are listed without `el`. The old Javadocs listed `el`, the element, which is the reason to use a mount hook.
- **Source:** `fullcalendar/chunks/2d6c825e.js:277-292` (`{...renderProps, el}`). The pages `views/resource-group-header-render-hooks.md`, `resource-group-lane-render-hooks.md` and `resource-lane-render-hooks.md`: "`el` - ... only available in ...DidMount and ...WillUnmount".
- **Fix:** Add `{@code el}` (the element) to each list.
- **Confidence:** high

### resourceLaneClass, resourceLaneTopClass, resourceLaneBottomClass, resourceLaneTopContent, resourceLaneBottomContent, resourceLaneDidMount, resourceLaneWillUnmount
- **Axis:** Javadoc (fact lost)
- **Where:** `SchedulerViewOptions.java:1065-1246`
- **Claim:** "`resource` and `options`" without saying what `options` holds. The old Javadocs said "holds only `eventOverlap`". The library docs list only `resource` and `el`.
- **Source:** `fullcalendar-scheduler/chunks/8f1316dc.js:354-359` (`options: { eventOverlap }`). `fullcalendar-scheduler/chunks/dc3ec45f.d.ts:151-156`. The docs page `resource-lane-render-hooks.md`.
- **Fix:** "... {@code options} holds only {@code eventOverlap}; the library docs list {@code resource} only."
- **Confidence:** high

### resourceOrder (callback argument)
- **Axis:** Javadoc
- **Where:** `SchedulerViewOptions.java:73-83`. The example in `addon/src/main/java/org/vaadin/stefan/fullcalendar/option/Order.java:30`.
- **Claim:** "a single {@link JsCallback} that compares two resources", and the `Order` example `a.extendedProps.rank - b.extendedProps.rank`. For `resourceOrder` the compare function does not get objects with `extendedProps`.
- **Source:**
  - `fullcalendar-scheduler/chunks/0a9e97be.js:849-862`: the compare gets `resourceFields`. They are built as `{ ...resource.extendedProps, ...resource.ui, ...resource }`, then `delete obj.ui; delete obj.extendedProps`. A source comment reads "TODO: pass in ResourceApi?".
  - So the function gets `a.rank`, not `a.extendedProps.rank`.
  - The docs `resources/resourceOrder.md` describe only the string form. The array and function forms come from `parseFieldSpecs` (`fullcalendar/chunks/642eba18.js:157-183`).
- **Fix:** "A compare function gets two plain objects, not resource API objects: the resource fields with the extended properties merged in (so {@code a.rank}, not {@code a.extendedProps.rank}). The library docs describe only the string form; the list and function forms come from the library code." The `Order` example needs a note for resources.
- **Confidence:** high

### resourceOrder (Values)
- **Axis:** Values
- **Where:** `Order.java` (`Order.of(String...)`, `Property`), setter at `SchedulerViewOptions.java:85-93`.
- **Claim:**
  - The library's documented string form splits on commas (`'-type1,type2'`). The add-on sends an array, and the library does not split array elements.
  - `Order.of("-type1,type2")` sends one token. It sorts by a field named `type1,type2` descending, which matches no field.
  - Nothing rejects it, and the library docs' own example is written that way.
- **Source:** `fullcalendar/chunks/642eba18.js:162-177` (only `typeof input === 'string'` is split). `resources/resourceOrder.md`. `Property` rejects only empty names and a leading `-`.
- **Fix:** Either reject a comma in `Order.Property` with an error naming `Order.of("-type1", "type2")`, or say in the Javadoc that each argument is one name and a comma is not a separator.
- **Confidence:** medium

### singleMonthClass
- **Axis:** Javadoc (docs and code disagree)
- **Where:** `ViewOptions.java:2900-2912`
- **Claim:** The Javadoc lists `multiMonthColumns`, `isFirst` and `isLast`, which is correct. It does not say that the docs also list `date`, which the library does not pass.
- **Source:** `fc-docs/_docs-v7/views/single-month-render-hooks.md` lists `date`. Code `fullcalendar/chunks/0ca0581f.js:107-110` passes only `{isFirst, isLast, multiMonthColumns}`.
- **Fix:** "The library docs also list {@code date}; the used library version does not pass it."
- **Confidence:** high

### singleMonthHeaderClass, singleMonthHeaderInnerClass
- **Axis:** Javadoc (docs and code disagree)
- **Where:** `ViewOptions.java:2941-2953`, `2972-2984`.
- **Claim:** "`hasNavLink`, `isSticky`, `isNarrow` and more, see the library docs". The docs it points to list `date`, which the library does not pass.
- **Source:** `fc-docs/_docs-v7/views/single-month-header-render-hooks.md`. Code `fullcalendar/chunks/0ca0581f.js:96-101` builds `{ multiMonthColumns, isSticky, isNarrow, hasNavLink }`.
- **Fix:** Replace "and more, see the library docs" with "{@code multiMonthColumns}. The library docs also list {@code date}; the used library version does not pass it."
- **Confidence:** high

### slotHeaderAlign
- **Axis:** Javadoc
- **Where:** `ViewOptions.java:4392-4404`
- **Claim:** The old note "FullCalendar documents this option only partly or not at all" is gone. The `@see` page does not list `slotHeaderAlign`. The default is missing.
- **Source:** `fc-docs/_docs-v7/date-time/slot-header-render-hooks.md` does not contain the option. The default is `'start'` (`fullcalendar/chunks/642eba18.js:842`). The new "timeline views" wording is correct: the only reader is `fullcalendar-scheduler/chunks/fc014b1a.js:1035-1039`. The old "time grid and timeline" was wrong.
- **Fix:** "The library default is {@code "start"}. The library docs do not describe this option; the linked page lists the slot header hooks only."
- **Confidence:** high

### Checked without findings
- **Javadoc, values and mapping all fine:**
  - `schedulerLicenseKey`: constants match `fullcalendar-scheduler/chunks/dc4db303.js:9-12`.
  - `selectLongPressDelay`: the fallback to `longPressDelay` is right (`fullcalendar/chunks/5342e739.js:1327-1329`).
  - `singleMonthTitleFormat`: default and callback info are correct. The string form has its mapping reason in the `DateFormatValue` Javadoc and in `.scratch/option-fields/issues/24-value-types-auto-false-now.md`.
- **Javadoc statements correct:** `singleMonthDidMount` and `singleMonthWillUnmount`. The library never wires `handleEl` as a ref and never sets `renderProps` (`fullcalendar/chunks/0ca0581f.js:25-33,168-171`).
- **No findings:**
  - `resourceDayHeaderWillUnmount`
  - `resourceExpanderClass`
  - `resourceExpanderContent`
  - `resourceHeaderRowClass`
  - `resourceIndentClass`
  - `resourceRemove`
  - `resourceRowClass`
  - `resourcesSet`
  - `rowMoreLinkClass`
  - `rowMoreLinkInnerClass`
  - `selectAllow`
  - `selectConstraint`

Value types and converters for all of the options above match the library's refiners and `.d.ts` types.

Checked 58 options, 13 findings.