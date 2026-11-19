# Ticket 67: verification of the six option reviews

All six reviewers wrote their report as text, because the Write tool refused report files. Together they cover 340 options with about 74 findings. I checked them against the 7.1.0 library code (`FC/` = `fullcalendar/chunks`, `SCH/` = `fullcalendar-scheduler/chunks`) and the docs in `fc-docs/_docs-v7`. I ran nothing. Where I say "confirmed by code", a browser test is still the final proof.

## Verification table

**Report 1**
- buttons, `click` gets one argument (the docs say two) → CONFIRMED (`FC/65e4f9c4.js:490,524`, `.d.ts:276`, `buttons.md:48`)
- buttons, an `iconContent` String is shown as text → CONFIRMED (`FC/2d6c825e.js:139-150`). The `Map("html")` form is plausible (`JsonUtils.java:110`) but untested.
- buttons, three lost facts → CONFIRMED (`65e4f9c4.js:476-519`)
- buttonGroupClass, a group of one button gets no group element → CONFIRMED (`65e4f9c4.js:1796`)
- dayCount, a view's duration wins over it → CONFIRMED by code (`54e239b5.js:106-113`, `65e4f9c4.js:1052`)
- aspectRatio, values below 0.5 act as 0.5, and the ratio is used in more places → CONFIRMED (`65e4f9c4.js:1942`, `44e811e2.js:2827`, `0ca0581f.js:83`)
- **allDayText, a newline IS a line break** → CONFIRMED (`4b6eee98.js:319` uses `whiteSpacePre`, `skeleton.css:278` sets `white-space: pre !important`, `CHANGELOG.md:380`). This contradicts the wave-2 note in ticket 67.
- allDayHeaderContent, the English text is "All-day" → CONFIRMED (trivial)
- 8 lost defaults → CONFIRMED values, see D1
- columnEntryTitleSticky, the "docs describe it only partly" note is lost → CONFIRMED
- background entries, the general entry hooks do not apply → CONFIRMED (`background-event-render-hooks.md`)
- block and column entries, the term and "applies in addition" are lost → CONFIRMED
- borderless, a set specific option wins over `borderless` → CONFIRMED (`642eba18.js:236-241`)
- dayCellTop, no effect in the time grid all-day section → CONFIRMED (`4b6eee98.js:1575`, `44e811e2.js:1046`)

**Report 2**
- dayHeaderDividerClass, `options` holds only `allDaySlot` → CONFIRMED (`996d27e3.d.ts:757-762`)
- dayMaxEntries and dayMaxEntryRows also limit the time grid all-day section (`true` means 5 rows) → CONFIRMED (`4b6eee98.js:1803-1810`)
- dayNarrowWidth, code default 80, docs say about 100 → CONFIRMED (`642eba18.js:840`; only breezy, forma, monarch and pulse set it)
- dayText is also the unit name in the hints → CONFIRMED (`65e4f9c4.js:476-519`)
- direction, the default comes from the locale (rtl for Arabic, Hebrew, Persian, Kurdish) → CONFIRMED (`65e4f9c4.js:1002,1011`). `Direction.java:27` marks `LTR` as "(default)", which is wrong.
- editable, the pointer to the start/duration options is lost → CONFIRMED
- entryDurationEditable and entryStartEditable, no default, they follow `editable` → CONFIRMED (`642eba18.js:1322-1323`)
- entryInteractive, docs and code differ → CONFIRMED (`642eba18.js:2461-2489`, `accessibility/eventInteractive.md`)
- entryDragMinDistance, ignored for touch dragging → CONFIRMED (`5342e739.js:1424`)
- entryOrder, the compare function gets plain objects, not entries → PARTLY. The objects are plain, but `...eventDef` keeps `extendedProps` (`642eba18.js:2344-2345`), so the `Order.java:30` example works for entries. The proposed fix sentence must not say otherwise.
- entryOrder, a comma inside a name is not split → CONFIRMED (`642eba18.js:162-170`), see D3
- entrySourceSuccess also runs for the add-on's own server fetch → CONFIRMED (`71d3d952.js:499-502`)
- entryDataTransform, returning nothing keeps the entry → CONFIRMED (`b00ce53f.js:75-79`)
- **entryDidMount with an arrow function and a body in braces breaks** → CONFIRMED by ECMAScript rules. An arrow function has no `arguments` of its own, and `FullCalendar.java:1166-1180` splices `arguments[0]` code into it. The client builds it with `new Function("return "+…)()` (`full-calendar.ts:94`), so `arguments[0]` is undefined. The default entry id snippet is on by default. Needs a reproducing test.
- entryConstraint, the field Javadoc lost the group id and business hours forms → CONFIRMED (`CalendarOptions.java:335`)

**Report 3**
- **listDayFormat and listDayAltFormat, `NONE` does not hide the text** → CONFIRMED by code (`642eba18.js:947-949,763`, `33aacced.js:37-38`). The only test is `ConverterTest:225`, which checks JSON only.
- hiddenDays, hiding all seven days makes the library throw → CONFIRMED (`54e239b5.js:260-275`)
- footerScrollbarSticky, `TRUE` also needs an automatic height → CONFIRMED (`2d6c825e.js:327-334`)
- expandRows, no effect with automatic height → CONFIRMED for the time grid (`4b6eee98.js:1306,1319`). I did not check the timeline line.
- entryStartEditable → CONFIRMED (as above)
- entryTimeClass, entryTitleClass, listItemEntryTimeClass also get `timeText` → CONFIRMED (`2d6c825e.js:1253-1258`)
- entryTimeFormat, list views show "7pm" → CONFIRMED (docs)
- entriesHint is also used in timeline views → CONFIRMED (`SCH/fc014b1a.js:1753`, `8f1316dc.js:1501`)
- moreLinkText and monthText, the "docs describe it only partly" note is lost → CONFIRMED
- firstDay, ISO week numbering makes Monday the first day → CONFIRMED (`headless-calendar/index.js:569-575`)
- WillUnmount hooks, does a changed callback reach existing elements → UNSETTLED. Needs a browser test (`2d6c825e.js:288` reads the props at removal time).
- entryWillUnmount, only rendered entries go through the hook → UNSETTLED, and probably outside the wrapper's scope

**Report 4**
- nowIndicatorSnap, the `@see` page does not exist → CONFIRMED (not in `index.md`, only in the CHANGELOG)
- nowIndicatorSnap, navLinks, nowIndicator, printMaxRows, resourceColumnsWidth defaults → CONFIRMED values, see D1
- nextHint and prevHint also label the next-year and previous-year buttons → CONFIRMED (`65e4f9c4.js:506-511`)
- nextYearText and prevYearText are undocumented → CONFIRMED
- rerenderDelay, drag, resize and entry merges render at once → CONFIRMED (`6143324c.js:24-36`)
- refetchResourcesOnNavigate → UNSETTLED. The raw resource-function route is not verified. The add-on forces `resources: []` (`full-calendar-scheduler.ts:63`).

**Report 5**
- rowEntry class fields, "no classes are added" is wrong, theme classes are joined in → CONFIRMED (`28b4e459.js:37,65-77`, `vaadin-theme.ts:21,49`)
- row entries, the term is lost → CONFIRMED
- rowEntryTitleSticky, default `true`, and no theme changes it → CONFIRMED (`642eba18.js:846`)
- resourcesInitiallyExpanded and the 8 other lost defaults → CONFIRMED values, see D1
- resourceGroupField and the group hooks are ignored while a column is marked as group column → CONFIRMED (`SCH/8f1316dc.js:2094-2114`)
- resource group and lane DidMount/WillUnmount lists miss `el` → CONFIRMED. These six list the properties completely, without "and more".
- resourceLane hooks, `options` holds only `eventOverlap` → CONFIRMED (`8f1316dc.js:354-358`)
- resourceOrder, the compare function gets no `extendedProps` → CONFIRMED (`SCH/0a9e97be.js:858-862`). The `Order.java:30` example fails for resources.
- resourceOrder, comma → CONFIRMED, see D3
- singleMonthClass and singleMonthHeaderClass, the docs list `date`, the code does not pass it → CONFIRMED (`0ca0581f.js:97-110`)
- slotHeaderAlign, docs-thin note and default → CONFIRMED

**Report 6**
- slotHeaderRowClass is read only in timeline views → CONFIRMED (the only reader is `SCH/fc014b1a.js:1146`)
- slotMinHeight and slotMinWidth are minimums → CONFIRMED (`4b6eee98.js:416-433`, `fc014b1a.js:686-705`)
- slotHeaderSticky, docs-thin note and default → CONFIRMED
- viewChangeHint, set only on groups of more than one button → CONFIRMED (`65e4f9c4.js:1796-1800`)
- timedText, the `@see` page does not mention it → CONFIRMED (minor)
- validRange and visibleRange, the library accepts equal bounds → CONFIRMED (`54e239b5.js:247-252`, `642eba18.js:1117-1121`), see D4. The stale `Option#VALID_RANGE` reference in the `DateRange` Javadoc is covered by ticket 43 (`43-migrate-addon-internals.md:13`), so it is no finding.
- unselectCancel, the docs default `''` → UNSETTLED. The code has no default (`642eba18.js:635`). By the DOM spec `closest('')` throws, but nothing was run. Minor.
- titleFormat and weekNumberFormat lack the docs-vs-code note → CONFIRMED, and the gap is wider (see Missed)

## Groups

- **(a) Javadoc wrong for 7.1.0:**
  - "no classes are added" on the row, block and column class fields
  - footerScrollbarSticky, expandRows
  - dayMaxEntries/dayMaxEntryRows ("use entryMaxStack" for the time grid)
  - slotHeaderRowClass, slotMinHeight, slotMinWidth
  - buttonGroupClass, viewChangeHint
  - "compares two resources" plus the `Order` example (for resources)
  - default wording of nextHint/prevHint, dayText
  - nowIndicatorSnap `@see`, `Direction.LTR` "(default)"
  - the `el`-less resource mount lists, entryTime/Title class info lists
  - rowEntryTitleSticky theme sentence
- **(b) docs and code differ, Javadoc silent:**
  - buttons `click`
  - every date-format callback (see Missed), dayHeaderFormat defaults
  - singleMonth `date`, direction, dayNarrowWidth
  - entryStartEditable, entryDurationEditable, entryInteractive, entryDragMinDistance
  - listDayFormat `false`, dayHeaderDivider and resourceLane `options`
  - aspectRatio minimum
- **(c) lost true facts:**
  - the allDayText newline
  - the "documents this option only partly" note (14 constants in `Option.java`, none survive)
  - the background, block, column and row entry terms and "in addition"
  - the editable pointer, the entryConstraint forms, the buttons facts, entryTimeFormat "7pm"
- **(d) missing defaults:** about 25 fields named by reports 1, 4 and 5. Reports 2 and 3 skipped them on purpose, so the list is incomplete.
- **(e) value problems:** listDayFormat `NONE`, Order comma, DateRange equal bounds, hiddenDays with all seven days, iconContent as String.
- **(f) add-on bug:** the entryDidMount merge with arrow functions.

## Decisions

- **D1, name library defaults in field Javadoc?** Ticket 17 and `option-fields-defaults.md` only decide what the add-on sets on the client, not what the Javadoc says. Ticket 17's own notes even say the `Option` Default rows name the library defaults. Today 14 of 355 fields name one.
  - Recommendation: one sentence per field whose library default is a fixed value. Where it depends on the view, theme or locale, say that instead.
  - Reason: the old Javadoc had 109 Default rows, and leaving them out makes the current state inconsistent.
- **D2, bring back the "the library docs do not describe this option" note?** Yes, for the 14 options. This fits README rule 45 (known library quirks are carried over) and the docs-vs-code rule of ticket 67.
- **D3, should `Order.Property` reject a comma?** Yes, with an `IllegalArgumentException` that points to `Order.of("a", "b")`. The old API took `"title,start"` as a string, so migrating users will write `Order.of("title,start")` and silently get no sorting.
- **D4, DateRange with equal bounds?** Keep rejecting. 7.x does the same (`v7_master` `FullCalendar.java:2645`), which follows the "follow 7.x behaviour" rule. No change.
- **D5, listDayFormat `NONE`?**
  - Check it with Playwright first.
  - If the text stays, keep `NONE`: it is a library form, and the full-coverage rule applies.
  - Then add the docs-vs-code note and an `FC-UPDATE:` marker. Do not suggest an empty-string callback, which is untested.
- **D6, fixing the entryDidMount merge?**
  - Recommended: wrap the user callback instead of splicing into its body: `function(){<snippet>; return (<user>).apply(this, arguments);}`. This covers arrow, async and destructured-parameter forms in one place.
  - Add a Playwright test, as the bug-fix rule requires.
- **D7, allDayText:** correct the wave-2 claim in ticket 67 and restore the old sentence. A quick browser check is worth it, because it contradicts a recorded finding.
- **D8, hiddenDays with all seven days:** document only. The case with `weekends` spans two fields, and a guard in the setter would be speculative.

## Missed

1. **Block and column class fields.** The same "Without a value, no classes are added" text is wrong on `blockEntryClass` and `blockEntryInnerClass` and all four `columnEntry*Class` fields. Classic sets `blockEventClass` and `columnEventClass` (`themes/classic/index.js:111,124`), plus inner, before and after for the column fields. Report 1 covered these fields and missed it. Only `blockEntryBefore/AfterClass` are correct.
2. **Format callback note.** Only `slotHeaderFormat` (`ViewOptions.java:5780`) has it. The others link to `date-formatting.md:185` ("will receive a date instance") but lack the note: dayCellFormat (:5517), dayHeaderFormat (:5550), entryTimeFormat, monthStartFormat, popoverFormat, singleMonthTitleFormat, listDayFormat and listDayAltFormat. Ticket 67 names this case explicitly.
3. **dayHeaderFormat defaults.** The docs say 'Mon 9/7' and 'Monday' (`date-time/dayHeaderFormat.md`). The code uses weekday plus day number (`44e811e2.js:2677-2692`). The Javadoc (:5545) follows the code without the note. Report 2 marked it "match", although ticket 67 names it as a wave-2 case.
4. **Docs-thin notes not reported** for `dayText`, `yearText` and `timedText` (partly). All three are among the 14 in `Option.java`.
5. **A pattern across reports.** Reviewers 2 and 3 read ticket 17 as having decided against defaults in the Javadoc. It did not, so (d) is incomplete until D1 is decided.