I could not write `report2.md`. The Write tool refused with "Subagents should return findings as text". The full report is below instead.

# Ticket 67, part 2: findings

Library paths are relative to `e2e-test-app/node_modules/fullcalendar/chunks/` (FullCalendar 7.1.0). Docs paths are relative to `fc-docs/_docs-v7/`. Add-on Java paths are relative to `addon/src/main/java/org/vaadin/stefan/fullcalendar/`. `full-calendar.ts` is in `addon/src/main/resources/META-INF/frontend/vaadin-full-calendar/`.

### dayHeaderDividerClass
- **Axis:** Javadoc
- **Where:** `ViewOptions.java:574` (field Javadoc names `options` without saying what it holds)
- **Claim:** The docs (`date-time/day-header-divider-render-hooks.md`) say `options` is "the calendar options object". The old Javadoc said "options (holds only allDaySlot)". That fact is lost.
- **Source:**
  - `0ca0581f.js:135-139` has `options: { allDaySlot: Boolean(options.allDaySlot) }`.
  - `996d27e3.d.ts:757-762` types it `options: { allDaySlot: boolean }`.
  - I read the other call sites only partly.
- **Fix:** "{@code options} is not the calendar options object the library docs describe. In the used library version it holds only {@code allDaySlot}."
- **Confidence:** high

### dayMaxEntryRows
- **Axis:** Javadoc
- **Where:** `ViewOptions.java:5106` ("For time grid and timeline views use entryMaxStack")
- **Claim:** The all-day section of the time grid views also uses this option. `true` there means 5 rows.
- **Source:**
  - `events/dayMaxEventRows.md`: "For the all-day section in the TimeGrid views, a value of `true` will limit the number of events to `5`."
  - `4b6eee98.js:1803-1810` (`AUTO_ALL_DAY_MAX_EVENT_ROWS = 5`, `getAllDayMaxEventProps` reads both options).
- **Fix:** "In time grid views these options limit the all-day section only; {@link EntryLimitValue#CELL_HEIGHT} limits it to 5 rows there. For the timed part use {@link #getEntryMaxStack() entryMaxStack}."
- **Confidence:** high

### dayMaxEntries
- **Axis:** Javadoc
- **Where:** `ViewOptions.java:5073`
- **Claim:** Same all-day gap as `dayMaxEntryRows`. The field Javadoc also does not say how the two options combine. The `EntryLimitValue` class Javadoc does.
- **Source:**
  - `44e811e2.js:2013-2024` (`true` on either option wins, then a number on `dayMaxEvents`, then a number on `dayMaxEventRows`).
  - `4b6eee98.js:1805-1810`.
- **Fix:** Add the same all-day sentence, plus "If {@link #getDayMaxEntryRows() dayMaxEntryRows} is set too, see {@link EntryLimitValue} for which one wins."
- **Confidence:** high for the all-day part, medium for the combination note.

### dayNarrowWidth
- **Axis:** Javadoc
- **Where:** `ViewOptions.java:4205`
- **Claim:** The new Javadoc gives no default. The old one said "80 with the add-on's theme, some FullCalendar themes set 100", which is still true. The docs (`date-time/dayNarrowWidth.md`) say "theme-dependent, but usually about 100", which differs from the library base default.
- **Source:**
  - `642eba18.js:840` has `dayNarrowWidth: 80`.
  - The stock themes `monarch`, `breezy`, `forma` and `pulse` set 100 (`fullcalendar/themes/*/index.js`, for example `pulse/index.js:234`).
  - The add-on's `vaadin-theme.ts` and `vaadin-theme.css` set none.
- **Fix:** "Without a value the library uses 80 pixels. The library docs say the default depends on the theme and is about 100. The stock themes set 100, the Vaadin theme of the add-on does not."
- **Confidence:** high for the numbers. Which theme a calendar loads is a runtime choice (`full-calendar.ts:48-59`).

### dayText
- **Axis:** Javadoc
- **Where:** `CalendarOptions.java:609`
- **Claim:** "Text of the day view button" is incomplete. The library also uses it as the unit name in the hints of the prev, next and today buttons. It is the fallback for every view whose single unit is day. `listDay` uses `listText` instead.
- **Source:**
  - `65e4f9c4.js:477-481` (view button text).
  - `65e4f9c4.js:514-519` (hints use `calendarOptions[currentUnit + 'Text']`).
  - `33aacced.js:252` (`buttonTextKey: 'listText'`).
- **Fix:** "Text for the day unit: the label of the toolbar buttons of day views, and the unit name in the hints of the previous, next and today buttons. The list day view uses {@code listText} instead."
- **Confidence:** medium (read from code, not run)

### direction
- **Axis:** Javadoc
- **Where:** `CalendarOptions.java:255`, and `option/Direction.java` (the `LTR` constant says "(default)")
- **Claim:** The docs say "`'ltr'` (default)". In the library the default comes from the locale, and some locales set `rtl`.
- **Source:**
  - `71d3d952.js:13` sets the default locale to `direction: 'ltr'`.
  - A grep over the locale chunks for `direction: 'rtl'` finds `ar`, `ar-ma`, `ar-dz`, `ar-sa`, `ar-kw`, `ar-tn`, `ar-ly`, `he`, `fa` and `ku`.
  - `full-calendar.ts` does not set `direction`.
- **Fix:** "Without a value the direction comes from the locale: right-to-left for the Arabic, Hebrew, Persian and Kurdish locales, left-to-right otherwise. The library docs name {@code ltr} as the default." Also drop "(default)" from `Direction.LTR`.
- **Confidence:** high

### editable
- **Axis:** Javadoc
- **Where:** `ViewOptions.java:94`
- **Claim:** The old Javadoc said to use the start-editable and duration-editable options to change only one of them. The new one lost that. It is still true.
- **Source:** `events/editable.md`: "use the more specific eventStartEditable and eventDurationEditable instead". The fields are `CalendarOptions.java:394` and `:405`.
- **Fix:** "To allow only one of them use {@link CalendarOptions#getEntryStartEditable() entryStartEditable} or {@link CalendarOptions#getEntryDurationEditable() entryDurationEditable}."
- **Confidence:** high

### entryDurationEditable
- **Axis:** Javadoc
- **Where:** `CalendarOptions.java:385` (`entryStartEditable` at `:397` has the same issue, it is outside my part)
- **Claim:** The text "takes precedence over editable" is right. The docs say "default: true". The old Javadoc said "true, effective only when EDITABLE is also true". Both are wrong for the used library. The new Javadoc does not say that it differs from the docs.
- **Source:**
  - `65e4f9c4.js:1159-1160`.
  - `642eba18.js:1322-1323`: `durationEditable: refined.durationEditable != null ? refined.durationEditable : refined.editable`.
  - `BASE_OPTION_DEFAULTS` (`642eba18.js:793-851`) has no entry for it.
- **Fix:** "Without a value it follows {@code editable}. The library docs name {@code true} as the default, the used library version has no default."
- **Confidence:** high

### entryInteractive
- **Axis:** Javadoc
- **Where:** `ViewOptions.java:4714`
- **Claim:** The Javadoc follows the code but does not say that the docs differ. The docs say "by default only events with a url are tabbable". Also, `false` does not turn it off for entries that have a `url`.
- **Source:** `642eba18.js:2461-2489`. The order is `url` first, then the option, then the entry's own `interactive`, then `emitter.hasHandlers('eventClick')`. `full-calendar.ts:312` registers `eventClick`.
- **Fix:** "The library docs say only entries with a url are focusable by default. The used library makes every entry focusable as soon as a click handler exists. An entry with a url stays focusable also with {@code false}."
- **Confidence:** high

### entryDragMinDistance
- **Axis:** Javadoc
- **Where:** `ViewOptions.java:4703`
- **Claim:** The Javadoc matches the code but does not say that the docs differ. The docs say "mouse/touch must move".
- **Source:**
  - `5342e739.js:1424` (touch gets 0).
  - `5342e739.js:1779` (resizing has no touch exception).
  - `642eba18.js:829` (comment: "only applies to mouse").
- **Fix:** Add "The library docs say it applies to mouse and touch, the used library version ignores it for touch dragging."
- **Confidence:** high (low impact)

### entryOrder (Javadoc)
- **Axis:** Javadoc
- **Where:** `ViewOptions.java:5280-5290` ("a single JsCallback that compares two entries"). The `Order.java` class example uses `a.extendedProps.rank`.
- **Claim:** The compare function does not get entries. It gets plain objects per displayed entry. These contain `extendedProps` spread at top level, the definition fields, `id`, `start` and `end` as epoch milliseconds, `duration` in milliseconds and `allDay` as 0 or 1.
- **Source:** `642eba18.js:2337-2353` (`buildSegCompareObj`) and `:195-201`. The docs are silent.
- **Fix:** "A compare function gets two plain objects, not entries: the extended properties of the entry at top level, {@code id}, {@code title}, {@code start} and {@code end} as epoch milliseconds, {@code duration} in milliseconds and {@code allDay} as 0 or 1."
- **Confidence:** medium (I did not list every definition field)

### entryOrder (values)
- **Axis:** Values
- **Where:** `option/Order.java:41-47` and the setter `setEntryOrder(String...)`
- **Claim:** The library splits only a string at commas. `setEntryOrder("title,start")` is accepted and sent as one array element. The library then reads it as one field name that matches nothing.
- **Source:** `642eba18.js:157-170`. `Order.Property` rejects only an empty name or a leading `-`.
- **Fix:** Either reject a comma in `Order.Property`, or document "one name per argument".
- **Confidence:** medium. Drop it if you count this as a user-error edge case.

### entrySourceSuccess
- **Axis:** Javadoc
- **Where:** `CalendarOptions.java:944`
- **Claim:** The callback also runs for the add-on's own server fetch. It gets the entry array and no `Response`. A callback that unwraps a wrapper object breaks it unless it returns nothing for an array.
- **Source:**
  - `71d3d952.js:499-505` (runs for every source, `|| rawEvents`).
  - `71d3d952.js:210` (function source).
  - `full-calendar.ts:594-640` (the add-on fetches through `$server.fetchEntriesFromServer`).
  - `events/eventSourceSuccess.md`: "Gets called for any type of Event source".
- **Fix:** "It runs for every entry source, including the calendar's own server-side entry provider, which gives it the array of entries and no {@code Response}. Return nothing to keep the received data."
- **Confidence:** medium. I did not read which source type the add-on registers, but all types go through `fetchSource`.

### entryDataTransform
- **Axis:** Javadoc
- **Where:** `CalendarOptions.java:927`
- **Claim:** A callback that returns nothing (`null` or `undefined`) keeps the entry as received. Only other falsy values discard it. The source's own transform runs before this one.
- **Source:** `b00ce53f.js:55-80`, the branch `else if (refinedEvent == null) refinedEvents.push(rawEvent)`.
- **Fix:** "A callback that returns nothing keeps the entry data unchanged."
- **Confidence:** high (low impact)

### entryDidMount
- **Axis:** Javadoc
- **Where:** `CalendarOptions.java:169-178` ("The merge needs a function with a body in braces")
- **Claim:** An arrow function with braces, `info => { ... }`, also counts as having a body in braces. The merged code uses `arguments[0]`. An arrow function has no own `arguments`. The client builds the callback with `new Function("return " + ...)()`, so `arguments[0].el` is undefined and throws.
- **Source:**
  - `FullCalendar.java:1108-1123` (snippet).
  - `FullCalendar.java:1141-1186` (`buildEntryDidMountMerged` checks only for `{` and `}`).
  - `full-calendar.ts:94`.
  - The default id snippet is on by default.
- **Fix:** Treat arrow functions like expression-body callbacks in `buildEntryDidMountMerged`, or write "a {@code function(info) {...}} with a body in braces, arrow functions are not supported".
- **Confidence:** medium. This is reasoned from JavaScript semantics, I did not run it. Please confirm in a browser first.

### entryConstraint
- **Axis:** Javadoc
- **Where:** `CalendarOptions.java:334`
- **Claim:** The field Javadoc names only windows, RRule windows and fixed spans. The group id form and the calendar's own business hours (`ConstraintValue.BUSINESS_HOURS`) appear only in the setters. The old Javadoc listed both.
- **Source:**
  - `events/eventConstraint.md`.
  - `642eba18.js:1278-1288`.
  - `f0ab0579.js:205-217`.
- **Fix:** "Limits entry dragging and resizing to the entries of a group, to the business hours of the calendar, or to windows of time: recurring business hours, windows defined by an RRule and fixed spans."
- **Confidence:** high

## Checked, no finding

- **Header hooks:** `dayHeaderClass`, `dayHeaderContent`, `dayHeaderDidMount`, `dayHeaderInnerClass`, `dayHeaderRowClass`, `dayHeaderWillUnmount`, `dayHeaders`.
- **`dayHeaderFormat`:** the default formats match `44e811e2.js:2667-2690`. The callback argument matches `@full-ui/headless-calendar/index.js:309-318`.
- **Day lane and row:** `dayLaneClass`, `dayLaneDidMount`, `dayLaneInnerClass`, `dayLaneWillUnmount`, `dayRowClass`.
- **`dayMinWidth`:** the `scrollgrid` claim is right. `2d6c825e.js:336-340` throws without the plugin, and `full-calendar-scheduler.ts:48` loads it.
- **Drag and drop:** `defaultAllDay`, `dragRevertDuration`, `dragScroll`, `dropAccept` (`interaction.js:157-166`), `droppable`.
- **Source params:** `endParam`.
- **Entry hooks and classes:** `entryAfterClass`, `entryBeforeClass`, `entryClass`, `entryColor`, `entryContent`, `entryContrastColor`, `entryInnerClass`.
- **Entry behaviour:** `entryAllow` (`f0ab0579.js:132-148`), `entryDisplay`, `entryLongPressDelay`, `entryMaxStack`, `entryMinHeight`, `entryMinWidth`, `entryOrderStrict`, `entryOverlap` (`f0ab0579.js:106-125`), `entryResizableFromStart`, `entryShortHeight`, `entrySlicing`.
- **Scheduler fields:** `entryPrintLayout` (`4b6eee98.js:690-700`, `935e429d.js:18`), `entryResourceEditable` (`0a9e97be.js:38-51`).
- **Not reported:** the old `Default` rows were dropped from many Javadocs. The new Javadocs name a library default in some fields (`entryMinHeight`, `entrySlicing`) and not in others (`entryMinWidth`, `dragRevertDuration`, `dragScroll`, `entryDragMinDistance`). I did not report each one because ticket 17 may have settled that convention.
- **Mapping axis:** I found no unwritten non-1:1 form in my options. The `DateFormat` string form is explained in the `DateFormatValue` and `DateFormat` Javadoc.
- **Excluded by the brief:** `defaultAllDayEventDuration`, `defaultTimedEventDuration` and `duration` (ticket 70).

Checked 57 options, 15 findings.