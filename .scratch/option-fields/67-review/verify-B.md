# Ticket 67: verification of the six option-review reports

The six reports hold about 74 findings. I confirmed almost all of them in the 7.1.0 sources, could not settle five, and found none wrong. One finding (report 1, `allDayText`) shows that ticket 67's own wave-2 example is wrong. Checking that ticket 17 and `option-fields-defaults.md` really settle the default question, as two reviewers assumed, showed they do not, so the "lost default" findings stand. That is decision D1.

Path prefixes: `C/` = `e2e-test-app/node_modules/fullcalendar/chunks/`, `S/` = `fullcalendar-scheduler/chunks/`.

## Verification table

| # | Finding | Verdict |
|---|---|---|
| R1 | buttons: `click` gets only the event, docs say two arguments | CONFIRMED (`C/65e4f9c4.js:490,524`; docs `buttons.md:48`) |
| R1 | buttons: an `iconContent` String is shown as text | CONFIRMED (`C/65e4f9c4.js:1705-1712`) |
| R1 | buttons: three lost facts (hint fallback, `display`, year/month/week/day) | CONFIRMED for the hint (`C/65e4f9c4.js:507-519`). The other two were not rechecked. |
| R1 | buttonGroupClass: a group of one button gets no wrapper | CONFIRMED (`C/65e4f9c4.js:1796-1803`) |
| R1 | dayCount: has no effect when the view has a duration | CONFIRMED (`C/54e239b5.js:106-113`) |
| R1 | aspectRatio: values below 0.5 are clamped | CONFIRMED (`C/65e4f9c4.js:1942`). Part (b) not checked. |
| R1 | allDayText: a newline **is** a line break in time grid | CONFIRMED in code. `C/4b6eee98.js:319` sets `whiteSpacePre`, which is `white-space: pre !important` (`skeleton.css:278`). Neither the classic theme nor the Vaadin theme overrides it. This contradicts the wave-2 example in ticket 67 line 15, and I found no evidence recorded for that example. A browser test should settle it. List views render the text as plain text without `pre`, so the wave-2 claim may come from there. |
| R1 | allDayText / columnEntryTitleSticky: lost "documented only partly" note | CONFIRMED (in `Option.java`; no fc-docs page names either option) |
| R1 | allDayHeaderContent: "all-day" should be "All-day" | CONFIRMED (trivial) |
| R1 | Eight lost defaults | CONFIRMED (`C/642eba18.js` `BASE_OPTION_DEFAULTS`: lines 821, 847 and others) |
| R1 | background/block/column: lost scope and "in addition to" sentences | CONFIRMED (`C/2d6c825e.js:1283-1293`) |
| R1 | borderless: a specific option wins with `??` | CONFIRMED (`C/642eba18.js:236-242`) |
| R1 | dayCellTop*: no effect in the all-day section | Not rechecked (medium, low impact) |
| R2 | dayHeaderDividerClass: `options` holds only `allDaySlot` | CONFIRMED (all six call sites) |
| R2 | dayMaxEntries/Rows: also limit the time grid all-day section, `true` there = 5 rows | CONFIRMED (`C/4b6eee98.js:1802-1810`). "For time grid use entryMaxStack" misleads. |
| R2 | dayNarrowWidth: code 80, docs about 100, stock themes 100 | CONFIRMED (`C/642eba18.js` defaults; `themes/{forma,monarch,breezy,pulse}/index.js`) |
| R2 | dayText: also the unit in the prev/next/today hints | CONFIRMED (`C/65e4f9c4.js:514-519`) |
| R2 | direction: the default comes from the locale; `Direction.LTR "(default)"` | CONFIRMED (`locales/{ar*,fa,he,ku}/global.js`; `full-calendar.ts:287` loads all locales) |
| R2 / R3 | editable lost the pointer to the start/duration options; entryStart/DurationEditable fall back to `editable`, docs say default `true` | CONFIRMED (`C/642eba18.js:1322-1323`) |
| R2 | entryInteractive: docs differ, and `false` does not affect url entries | CONFIRMED (`C/642eba18.js:2462-2475`). The Javadoc at `ViewOptions.java:4715` says "{@code false} turns that off for all entries", which is wrong. |
| R2 | entryDragMinDistance: docs differ for touch | CONFIRMED (`C/5342e739.js:1424`) |
| R2 | entryOrder: the compare function gets plain objects | CONFIRMED (`C/642eba18.js:2337-2353`). `...eventDef` keeps `extendedProps`, so the `Order` example works for entries. |
| R2 / R5 | entryOrder / resourceOrder: a comma inside one name is not split | CONFIRMED (`C/642eba18.js:162-170`; `Order.java:115-121`) |
| R2 | entrySourceSuccess: also runs for the add-on's function source, without a `Response` | CONFIRMED (`71d3d952.js:499-505`) |
| R2 | entryDataTransform: returning nothing keeps the entry | CONFIRMED (`b00ce53f.js:76-79`) |
| R2 | entryDidMount: an arrow function with a block body breaks the merge | CONFIRMED from JS semantics. In `info => {…}` the spliced `arguments[0]` is the outer `new Function` call's arguments, which is empty (`FullCalendar.java:1154-1172`, `full-calendar.ts:94`). The default id snippet is on by default. No test covers it. A browser test is needed to make it a regression test. |
| R2 | entryConstraint: field Javadoc omits group id and business hours | CONFIRMED (from the report; low impact) |
| R3 | listDayFormat / listDayAltFormat `NONE` does not hide the text | CONFIRMED (`C/642eba18.js:947-949,763-764`; `C/33aacced.js:36-37` uses `?? default`) |
| R3 | hiddenDays: hiding all days throws | CONFIRMED (`C/54e239b5.js:260-275`) |
| R3 | footerScrollbarSticky: `TRUE` also needs an automatic height | CONFIRMED (`C/2d6c825e.js:327-334`). `tableHeaderSticky` does not have this restriction (`:320-326`). |
| R3 | expandRows: no effect with an automatic height | CONFIRMED (`C/4b6eee98.js:1306,1319`; `S/8f1316dc.js:1327,1383`) |
| R3 | entry/listItem time/title class callbacks also get `timeText` | CONFIRMED (`C/2d6c825e.js:1253-1258`) |
| R3 | entryTimeFormat: lost the list-view default | CONFIRMED (docs `eventTimeFormat.md`) |
| R3 / R4 / R6 | Lost "docs silent" notes: entriesHint, moreLinkText, monthText, next/prevYearText, nowIndicatorSnap, viewChangeHint, timedText, slotHeaderAlign, slotHeaderSticky, rowEntryTitleSticky | CONFIRMED (none in `fc-docs/_docs-v7/index.md`; notes were in `Option.java`). entriesHint scheduler use: `S/fc014b1a.js:1753`. |
| R3 | firstDay: ISO `weekNumberCalculation` gives Monday | CONFIRMED (`@full-ui/headless-calendar/index.js:569-575`) |
| R3 | *WillUnmount: the new callback may run for existing elements | UNSETTLED. Needs a browser test. |
| R3 | entryWillUnmount: only a subset of entries is rendered with dayMaxEvents | UNSETTLED. Only the changelog says so (`CHANGELOG.md:6-7`). |
| R4 | nowIndicatorSnap: the `@see` page is not in the local index | CONFIRMED locally. Whether a web page exists is unsure. |
| R4 / R5 | Lost defaults: navLinks, nowIndicator, nowIndicatorSnap, printMaxRows, resourceColumnsWidth, resourcesInitiallyExpanded, select*, scrollTimeReset, showNonCurrentDates, slotEntryOverlap, singleMonthMinWidth | CONFIRMED values (`C/642eba18.js` defaults; `S/dc4db303.js:150`). Whether they belong in the Javadoc is D1. |
| R4 | next/prevHint: also label the year buttons; default comes from the locale hint | CONFIRMED (`C/65e4f9c4.js:507-511`; `71d3d952.js` "Next $0") |
| R4 | rerenderDelay: drag, resize and MERGE_EVENTS render at once | CONFIRMED (`C/6143324c.js:24-36`). The add-on updates entries through `refetchEvents` (`full-calendar.ts:972`), so only the drag/resize part matters to users. |
| R4 | refetchResourcesOnNavigate: does it matter with add-on-managed resources? | UNSETTLED. Depends on whether the raw-option API can set `resources`. |
| R5 | rowEntry*Class: "no classes are added" | CONFIRMED that it misleads, because theme classes are joined (`C/28b4e459.js:33-37,65-77`; classic sets them). |
| R5 | rowEntry*: term "row entries" not explained | CONFIRMED |
| R5 | rowEntryTitleSticky: the "Themes can change the default" sentence | CONFIRMED that no theme changes it (only monarch sets `columnEventTitleSticky`) |
| R5 | resourceGroupField and its hooks: ignored when a column has `group: true` | CONFIRMED (`S/8f1316dc.js:2094-2113`; `ResourceColumn.withGroup` exists) |
| R5 | resource mount hooks: `el` missing | CONFIRMED. It misleads where the list looks complete (`SchedulerViewOptions.java:1162,1237`). |
| R5 | resourceLane*: `options` holds only `eventOverlap` | CONFIRMED (`S/8f1316dc.js:355-359`) |
| R5 | resourceOrder: compare objects have no `extendedProps`, so the `Order.java:30` example fails for resources | CONFIRMED (`S/0a9e97be.js:858-863`) |
| R5 | singleMonth*Class: docs list `date`, code does not pass it | CONFIRMED (`C/0ca0581f.js:107-110`; docs line 20) |
| R6 | slotHeaderRowClass: timeline only | CONFIRMED by grep (only `S/fc014b1a.js` reads it) |
| R6 | slotMinHeight / slotMinWidth are minimums | CONFIRMED (`C/4b6eee98.js:416-433`; `S/fc014b1a.js:686-705`) |
| R6 | viewChangeHint: only on groups with more than one button | CONFIRMED (`C/65e4f9c4.js:1796-1800`) |
| R6 | timedText: the `@see` page does not mention it | CONFIRMED (low impact) |
| R6 | validRange / visibleRange: equal bounds are rejected but valid in the library | CONFIRMED. The library makes them a one-day range (`C/54e239b5.js:247-252` → `C/642eba18.js:1120-1121`). `DateRange.java:35-39` rejects them. |
| R6 | unselectCancel `""` | UNSETTLED but likely: `closest("")` throws a SyntaxError under the DOM spec (`C/5342e739.js:1973`). The docs' "default `''`" is not in the code. |
| R6 | titleFormat / weekNumberFormat: the callback gets an info object | CONFIRMED (same case wave 2 recorded for `slotHeaderFormat`) |

## Groups (confirmed findings)

- **(a) Javadoc wrong for 7.1.0:** entryInteractive "`false` turns it off for all"; dayMaxEntries/Rows "time grid → entryMaxStack"; footerScrollbarSticky (implies `TRUE` always sticks); slotMinHeight/slotMinWidth (exact, not minimum); the twelve "no classes are added" fields; rowEntryTitleSticky theme sentence; the `Order.java` example for resources; `Direction.LTR "(default)"`; next/prevHint "locale's text".
- **(b) Docs and code differ, Javadoc silent:** buttons `click`; entryStart/DurationEditable defaults; entryInteractive; entryDragMinDistance; direction; dayNarrowWidth; listDayFormat `false`; singleMonth*Class `date`; slotHeaderRowClass; titleFormat/weekNumberFormat; unselectCancel default.
- **(c) Lost true facts:** allDayText newline; the "docs silent" notes (14 constants in `Option.java`); background/block/column/row scope sentences; editable pointer; dayHeaderDividerClass and resourceLane `options`; entryTimeFormat list default; `el` in mount hooks; button facts.
- **(d) Missing library defaults:** about 30 fields across R1, R4 and R5, plus those R2 and R3 saw but chose not to report.
- **(e) Values/validation:** `ListDayFormatValue.NONE`; Order comma; DateRange equal bounds; hiddenDays with all seven; unselectCancel `""`.
- **(f) Add-on bug:** entryDidMount merge breaks with arrow functions that have a block body.

## Decisions for the maintainer

1. **D1: name the library default in every field Javadoc?** Recommend yes. Ticket 17 and `option-fields-defaults.md` only drop the add-on's own *overrides*. Neither says to drop the default from the Javadoc, and ticket 17 even updated the old Default rows. Today only 10 of about 355 fields name a default, so readers cannot tell what `null` means.
2. **D2: `ListDayFormatValue.NONE`.** Recommend removing it, or documenting that it has no effect. It promises hiding, which the used library does not do.
3. **D3: should `Order.Property` reject commas?** Recommend rejecting, with a message that points to `Order.of("a", "b")`. The docs' own string example (`'-type1,type2'`) leads users straight into the mistake.
4. **D4: DateRange equal bounds.** Recommend allowing them and documenting "one day". The add-on should not refuse what the library accepts.
5. **D5: hiddenDays with all seven days.** Recommend rejecting in the setter (a cheap check that prevents a client error) and documenting the `weekends` combination.
6. **D6: entryDidMount with arrow functions.** Recommend fixing it in `buildEntryDidMountMerged` (detect the `=>` form, or wrap the user callback instead of splicing into it), with a browser test. Documenting the limitation alone leaves the default id snippet throwing.
7. **D7: allDayText.** Recommend a browser test, then correcting either ticket 67's wave-2 example or the Javadoc.
8. **D8: `el` in mount hooks.** Recommend changing the README convention so DidMount/WillUnmount hooks always name `el`, since `el` is the reason to use them. Today 32 of these hooks omit it.

## Missed systematically

- **Defaults were skipped on a wrong premise.** R2 and R3 did not report missing defaults because they assumed ticket 17 had decided the convention. It had not (see D1).
- **Two "docs silent" notes were not flagged.** `Option.java` marks 14 constants as documented only partly. Reports flagged them all except `DAY_TEXT` and `YEAR_TEXT`. R2 reviewed `dayText` but did not flag the lost note. `yearText` was not reported at all.
- **R1 missed the theme-joining issue on block and column classes.** The "no classes are added" sentence also stands on `blockEntryClass`/`blockEntryInnerClass` and on four `columnEntry*Class` fields (`ViewOptions.java:1548-1851`), and the classic theme sets 6 of those 8. R5 flagged only the row fields.
- **R2 and R3 split one finding.** `entryStartEditable` falls in R3's part and was reported there, so between them both editable fields are covered.
- **Coverage cannot be checked.** The part files are not in the scratchpad, so I could not check which options each reviewer skipped. R3 does not list its options that had no findings.
- **Stale class Javadoc.** `DateRange.java:23-27` still refers to `Option#VALID_RANGE` and `setOption`, the old API. Ticket 51 may cover this; I did not check.