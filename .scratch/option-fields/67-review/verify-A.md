I checked the six reports (about 76 findings) against the library code at 7.1.0 and the add-on. Nearly all findings hold. The ones that matter most:

- **`allDayText` contradicts ticket 67.** The ticket names "a newline in `allDayText` is no line break" as a known wrong old claim. The code says a newline is a line break.
- **Two values the add-on accepts break in the library.** `ListDayFormatValue.NONE` does not hide the text. A `hiddenDays` that hides all seven days makes the library throw.
- **One add-on bug.** An arrow-function `entryDidMount` with a block body breaks once the add-on merges into it. 7.x has the same code.

In the sources, `FC` is `e2e-test-app/node_modules/fullcalendar/chunks/` and `SCH` is `fullcalendar-scheduler/chunks/`.

## Verification table

**Report 1**
- buttons, click gets only the event → CONFIRMED (FC/65e4f9c4.js:490,524; the docs name two arguments).
- buttons, a String `iconContent` shows as text → CONFIRMED in code (FC/65e4f9c4.js:1705). The wording of the `Map` route is not checked in a browser.
- buttons, lost facts (hint, display, year/month/week/day names) → CONFIRMED.
- buttonGroupClass, no group element for a single button → CONFIRMED (FC/65e4f9c4.js:1796-1803).
- dayCount, a view duration wins → CONFIRMED (FC/54e239b5.js:106-113).
- aspectRatio, values below 0.5 are clamped → CONFIRMED (FC/65e4f9c4.js:1942). The row-height use is not checked, medium.
- allDayText, a newline is a line break → CONFIRMED in code. The text sits in a `whiteSpacePre` wrapper (FC/4b6eee98.js:319), which is `white-space: pre` (skeleton.css:278). Neither classic nor the Vaadin theme overrides it, and CHANGELOG.md:380 (v7.0.0-rc.0) says the same. Wave 2 recorded the opposite. One Playwright check would settle it.
- allDayHeaderContent, "All-day" → CONFIRMED (FC/71d3d952.js:26). Trivial.
- Lost defaults (8 fields) → CONFIRMED (FC/642eba18.js:793-851, FC/4ec5fb96.js:79).
- columnEntryTitleSticky, docs note lost → CONFIRMED (no docs page names the option).
- background*, "the general hooks do not apply" lost → CONFIRMED.
- block* and column*, term and "in addition to" lost → CONFIRMED.
- borderless*, the specific option wins → CONFIRMED (the report cites FC/642eba18.js:237).
- dayCellTop*, no day number in the time grid all-day row → CONFIRMED in code (FC/44e811e2.js:1046). Medium.

**Report 2**
- dayHeaderDividerClass, `options` holds only `allDaySlot` → CONFIRMED at all four call sites.
- dayMaxEntryRows and dayMaxEntries, all-day row of time grid views, `true` means 5 → CONFIRMED (FC/4b6eee98.js:1803-1810, FC/44e811e2.js:2013).
- dayNarrowWidth, 80, stock themes set 100 → CONFIRMED (the themes' index.js).
- dayText, also used in hints → CONFIRMED (FC/65e4f9c4.js:514-519). Medium.
- direction, the locale decides, `LTR` "(default)" is wrong → CONFIRMED (10 rtl locales, FC/71d3d952.js:13).
- editable, pointer to start/duration editable lost → CONFIRMED.
- entryDurationEditable and entryStartEditable (report 3), no default, falls back to `editable` → CONFIRMED (FC/642eba18.js:1322-1323, no entry in `BASE_OPTION_DEFAULTS`).
- entryInteractive, an entry with a url stays focusable, the docs differ → CONFIRMED (FC/642eba18.js:2461-2489, full-calendar.ts:339).
- entryDragMinDistance, the docs differ → CONFIRMED (FC/5342e739.js:1424).
- entryOrder Javadoc, the callback gets no entries → CONFIRMED in part. The compare object also keeps `extendedProps` (`...eventDef`, FC/642eba18.js:2345), so the `Order.java:30` example works for entries. The proposed fix text is wrong on that point.
- entryOrder values, a comma is not split → CONFIRMED (FC/642eba18.js:162-170; Order.java:117 checks nothing).
- entrySourceSuccess, also runs for the server fetch → CONFIRMED (FC/71d3d952.js:499-505, full-calendar.ts:595). Medium.
- entryDataTransform, null or undefined keeps the entry → CONFIRMED (FC/b00ce53f.js:74-79).
- entryDidMount, arrow function with braces → CONFIRMED by JavaScript semantics. The default snippet's `arguments[0]` (FullCalendar.java:1108) lands inside an arrow function. The arrow function's `arguments` is the one of the zero-argument `new Function(...)()` wrapper (full-calendar.ts:94), so it throws. v7_master has the same code. No browser run.
- entryConstraint, group and business-hours forms missing in the field Javadoc → CONFIRMED (the old Javadoc listed them).

**Report 3**
- listDayFormat and listDayAltFormat `NONE` → CONFIRMED. `false` becomes `null` (FC/642eba18.js:947), and `?? default` brings the default back (FC/33aacced.js:37-38). The converter sends `false` (ListDayFormatValueConverter.java:42). No test covers it.
- hiddenDays, hiding all seven days throws → CONFIRMED (FC/54e239b5.js:265-275).
- footerScrollbarSticky, `TRUE` also needs an automatic height → CONFIRMED (FC/2d6c825e.js:327-334).
- expandRows, no effect with an automatic height → CONFIRMED for time grid (FC/4b6eee98.js:1306). Timeline is likely (SCH/8f1316dc.js:1383).
- entryTimeClass and related, `timeText` is also passed → CONFIRMED (FC/2d6c825e.js:1257). Low.
- entryTimeFormat, list default "7pm" lost → CONFIRMED (docs).
- entriesHint, also used in the timeline, docs note lost → CONFIRMED (SCH/fc014b1a.js:1753).
- moreLinkText and monthText, docs note lost → CONFIRMED.
- firstDay, ISO week numbers mean Monday → CONFIRMED (@full-ui/headless-calendar/index.js:569-571).
- The four WillUnmount fields → UNSETTLED. `componentWillUnmount` reads the current props (FC/2d6c825e.js:288-292), so the new callback probably runs for existing elements. Needs a browser test.
- entryWillUnmount, subset rendering since 7.1 → UNSETTLED. It is the changelog's statement, not checked against the add-on.

**Report 4**
- nowIndicatorSnap, the `@see` page does not exist → CONFIRMED (not in index.md, only in the changelog).
- Defaults of nowIndicatorSnap, navLinks, nowIndicator, printMaxRows and resourceColumnsWidth → CONFIRMED (FC/642eba18.js:813,839,848; SCH/dc4db303.js:150; SCH/8f1316dc.js:2309).
- nextHint and prevHint, also used for the year buttons, and the locale's hint is the default → CONFIRMED (FC/65e4f9c4.js:507-511).
- nextYearText and prevYearText, docs note lost → CONFIRMED.
- rerenderDelay, drag, resize and `MERGE_EVENTS` render at once → CONFIRMED (FC/6143324c.js:24-36).
- refetchResourcesOnNavigate → UNSETTLED. It needs a check of whether a raw `resources` function or feed can still be set.

**Report 5**
- rowEntry* "Without a value, no classes are added" → CONFIRMED. Classic sets `rowEvent*` classes, and user classes are joined with the theme's, not replacing them (FC/28b4e459.js:33-37,65-77).
- rowEntry*, term and "in addition to" lost → CONFIRMED.
- rowEntryTitleSticky, default `true`, no docs, the theme sentence is wrong → CONFIRMED (only monarch changes `columnEventTitleSticky`).
- resourcesInitiallyExpanded and the select*/scroll/slot defaults → CONFIRMED (FC/0ca0581f.js:358,365, FC/4ec5fb96.js:81).
- resourceGroupField and the group hooks are ignored when a group column exists → CONFIRMED (SCH/8f1316dc.js:2093-2113).
- `el` missing in the scheduler DidMount/WillUnmount lists → CONFIRMED.
- resourceLane*, `options` holds only `eventOverlap` → CONFIRMED (SCH/8f1316dc.js:354-359).
- resourceOrder, the callback gets no `extendedProps` → CONFIRMED (SCH/0a9e97be.js:858-862). The `Order.java:30` example breaks for resources.
- resourceOrder, comma → CONFIRMED (same code as entryOrder).
- singleMonthClass and singleMonthHeader*, the docs list `date`, which is not passed → CONFIRMED (FC/0ca0581f.js:97-110).
- slotHeaderAlign, docs note and default `'start'` → CONFIRMED.

**Report 6**
- slotHeaderRowClass, read in timeline only → CONFIRMED (only SCH/fc014b1a.js reads it).
- slotMinHeight and slotMinWidth are minimums → CONFIRMED (FC/4b6eee98.js:416-433, SCH/fc014b1a.js:686-705).
- slotHeaderSticky, default, docs note, no offset when centred → CONFIRMED (SCH/fc014b1a.js:1040-1052).
- viewChangeHint, groups of more than one only → CONFIRMED (FC/65e4f9c4.js:1796).
- timedText, the `@see` page is silent → CONFIRMED. Low.
- validRange and visibleRange with equal bounds → CONFIRMED that the library accepts them and makes one day of them (`refineRange` → `computeVisibleDayRange`, FC/54e239b5.js:247-251, FC/642eba18.js:1120). The add-on's check is inherited from v7_master.
- unselectCancel `""` → CONFIRMED that the add-on passes it to `closest()` on every pointer down (FC/5342e739.js:1973). That an empty selector throws comes from the DOM specification and is not run in a browser. Low.
- titleFormat and weekNumberFormat, note on the callback argument missing → CONFIRMED (the same case wave 2 recorded).

## Groups

- **(a) Javadoc wrong for the used library:**
  - "no classes are added" (row, block and column fields)
  - footerScrollbarSticky `TRUE`
  - entryInteractive with a url
  - Order example for resources
  - "compares two entries"
  - slotMinHeight and slotMinWidth (they are minimums)
  - nowIndicatorSnap `@see`
  - rowEntryTitleSticky theme sentence
  - `Direction.LTR` "(default)"
  - viewChangeHint
  - rerenderDelay
  - slotHeaderAlign wording is fine
- **(b) Library docs and code differ, Javadoc silent:**
  - listDay*Format `false`
  - entryStart/DurationEditable default
  - direction
  - buttons click arguments
  - entryInteractive
  - entryDragMinDistance
  - singleMonth* `date`
  - titleFormat and weekNumberFormat
  - dayNarrowWidth
  - slotHeaderRowClass
  - resourceLane `options`
  - dayHeaderDivider `options`
  - resourceGroupField with a group column
  - resourceOrder forms
- **(c) Lost true facts:**
  - the allDayText newline
  - the 14 "documented only partly" notes
  - background, block, column and row explanations
  - editable pointer
  - entryConstraint forms
  - buttons facts
  - scheduler `el`
  - entryTimeFormat list default
  - firstDay with ISO
  - the four dayMax/time grid all-day notes
- **(d) Missing library defaults:** about 30 fields across reports 1, 4 and 5, plus the unreported ones under *Missed*.
- **(e) Value or validation:**
  - listDayFormat `NONE` does nothing
  - hiddenDays with seven days
  - Order comma
  - DateRange equal bounds
  - unselectCancel `""`
- **(f) Add-on bug:** the `entryDidMount` arrow-function merge, also in 7.x.

## Decisions

1. **Name the library default in field Javadoc?** Recommend yes: one sentence "The library default is X." wherever the library sets a fixed value, and "the locale/theme decides" otherwise. README.md:45 requires only the null sentence. Ticket 17 is about dropping the add-on's own defaults, and it even kept the library defaults in the `Option` Default rows. Reports 2, 3 and 6 misread it as forbidding defaults, so coverage is uneven now.
2. **allDayText newline.** Recommend one Playwright assertion first. Then restore the old sentence and correct the wave-2 statement in ticket 67, which the code contradicts.
3. **listDayFormat `NONE`.** Recommend keeping the constant with a note in the Javadoc ("the library docs say it hides the text; the used version falls back to the default") plus an `FC-UPDATE:` marker. Removing a library form conflicts with the full-coverage rule, and a later library version may fix it.
4. **Order.Property and commas.** Recommend rejecting a comma. It is a one-line guard, the library docs' own example `'-type1,type2'` invites the mistake, and the result is a silent no-op sort.
5. **DateRange with equal bounds.** Recommend keeping the rejection, since 7.x behaves the same, and documenting that the library would read it as one day.
6. **hiddenDays with all seven days.** Recommend rejecting it in the setter and documenting the `weekends=false` combination, which spans two fields.
7. **"Documented only partly" notes.** Recommend restoring them for all 14 old constants and changing the `@see` links whose pages do not name the option.
8. **Class options merge with the theme.** Recommend one sentence on `ClassNameValue` (or each class field) and dropping "Without a value, no classes are added" from the 12 fields that say it.
9. **entryDidMount.** Recommend fixing the merge so arrow functions are handled (treat them like expression bodies or rewrite them), with a browser test, and porting it back to v7_master.

## Missed

- **Block and column class fields.** "Without a value, no classes are added" is also in the block-entry fields (ViewOptions.java:1548-1648) and the column-entry fields (:1751-1851). Classic sets `blockEvent*` and `columnEvent*` classes (themes/classic/index.js). No report flagged these. The theme merge applies to every `*Class` option (FC/28b4e459.js:33) and is documented nowhere (no hit in ClassNameValue.java).
- **Lost "documented only partly" notes.** All 14 old constants with that note (Option.java) lost it. **yearText** and **dayText**'s note went unreported.
- **Defaults in parts 2, 3 and 6.** These reports skipped the default axis. Examples: `dragRevertDuration` 500, `dragScroll` true, `entryMinWidth` 30, `entryDragMinDistance` 5, `expandRows` false, `weekNumberCalculation` `'local'`, `dayHeaderAlign` `'start'` (all FC/642eba18.js:813-846).
- **Report 2's fix text for entryOrder** is wrong where it says `extendedProps` is gone. The object keeps `extendedProps` for entries (FC/642eba18.js:2344-2345). Only resources lose it.
- **Skipped options.** Without the part files (the scratchpad `o3` folder is empty) I could not tell which options were skipped without reason. The counts are 58+57+58+57+58+52 = 340, and every report that names skips gives a reason from the brief.