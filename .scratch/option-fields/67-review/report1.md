The report file could not be written: the Write tool refused with "Subagents should return findings as text, not write report files". So `/tmp/claude-1000/-home-suebe-workspace-directory-vaadin-fullcalendar/51116935-77f7-4d15-82b4-1907620a3263/scratchpad/t67/report1.md` does not exist, and the full report is below.

# Ticket 67, part 1 (allDayDividerClass to dayHeaderAlign)

Library version is 7.1.0. In the Source lines, `E` means `e2e-test-app/node_modules/fullcalendar/chunks/`. "Old Javadoc" means `Option.java`. `ViewOptions.java` lines are in `addon/src/main/java/org/vaadin/stefan/fullcalendar/`.

Skipped: `dateIncrement` (ticket 70) and `class` (no field). For `className` I checked the Javadoc only and skipped the aliasing.

### buttons
- **Axis** Javadoc
- **Where** `CalendarOptions.java:886`
- **Claim 1** `click` is listed without its arguments. The old Javadoc said "called with the mouse event only. On a built-in button it runs before the button's own action, which ev.preventDefault() cancels". The new one keeps only the second sentence.
- **Source 1** Docs `buttons.md` say "`click` ... Accepts two arguments: `(mouseEvent, htmlElement)`". The code passes one argument: `E/65e4f9c4.js` around lines 497 and 528, `buttonInput?.click?.(ev)`. The `.d.ts` also says `click?: (ev: MouseEvent) => void` (`E/996d27e3.d.ts:276`). Docs and code disagree, so the Javadoc must say so.
- **Fix 1** "A click callback gets only the mouse event. The library docs also name the button element as a second argument, but the used library version does not pass it."
- **Claim 2** "{@code iconContent} and {@code className} take a string or a callback". The old Javadoc called `iconContent` "SVG markup". The new text does not say that a `String` is shown as plain text.
- **Source 2** `E/65e4f9c4.js:1705-1712` (`ButtonIcon`) passes `iconContent` to a `ContentContainer` as `customGenerator`. A string becomes a text child, and only `{html: ...}` is set as inner HTML (`E/2d6c825e.js:140-152`).
  - A nested `Map` is sent as a JSON object by `JsonUtils.toJsonNode`.
  - `setButtons` (`CalendarOptions.java:895`) keeps nested maps via `Map.copyOf`.
  - I read the code only. No test covers this.
- **Fix 2** "An iconContent String is shown as text, not as markup. For SVG markup pass a Map with the key html, for example Map.of("html", "<svg>...</svg>"), which the library sets as unescaped inner HTML, or a JsCallback. Never put unchecked user input into it."
- **Claim 3** Three facts from the old Javadoc were lost and are still true:
  - `hint` defaults to the button text and may be a callback (`E/65e4f9c4.js` around lines 497-510).
  - A built-in button that normally shows an icon needs `display` set to `text`, `text-icon` or `icon-text` before its `text` shows (docs `buttons.md`).
  - The names `year`, `month`, `week` and `day` can also be overridden (`.d.ts:1812-1823`).
- **Fix 3** Add the three facts back in one short sentence each.
- **Confidence** Claims 1 and 3 high. Claim 2 high for the string-as-text behaviour, medium for the `Map` wording.

### buttonGroupClass
- **Axis** Javadoc
- **Where** `ViewOptions.java:2870`
- **Claim** "CSS classes for each group of buttons in a toolbar section. A group is a set of buttons separated by commas."
- **Source** `E/65e4f9c4.js:1796-1802`: `if (children.length > 1) { return createElement('div', {... buttonGroupClass ...}) } return children[0]`. A group of one button gets no group element. `hasSelection` is true only when every widget in the group is a view button.
- **Fix** "A group with only one button has no group element, so the class is not applied to it."
- **Confidence** high

### dayCount
- **Axis** Javadoc
- **Where** `ViewOptions.java:3802`
- **Claim** Nothing says what happens when a view has both `dayCount` and a `duration`.
- **Source** `E/54e239b5.js:101-116`: `if (props.duration) {...} else if ((dayCount = this.props.dayCount)) {...}`. `duration` comes from the view spec (`E/65e4f9c4.js:1051`). Built-in views such as `timeGridWeek` have one, from the timegrid plugin definition in `E/4ec5fb96.js`. The docs example uses a base `timeGrid` view without a duration.
- **Fix** "It has no effect on a view that has a duration, such as the built-in timeGridWeek. The duration wins."
- **Confidence** medium (read from code, not run)

### aspectRatio
- **Axis** Javadoc
- **Where** `ViewOptions.java:4912`
- **Claim** "It is used only while neither height nor a content height is set."
- **Source**
  - (a) `E/65e4f9c4.js:1942`: `Math.max(options.aspectRatio, 0.5); // prevent from getting too tall`. A value below 0.5 acts as 0.5. The docs do not mention this.
  - (b) The ratio is also used elsewhere. `E/44e811e2.js:2823-2829` (`computeRowBasis`) uses it for the minimum row height of day grid views when `rowCount > 6 || isHeightAuto`. Multi-month views use it too (`E/0ca0581f.js:83` and `146`).
- **Fix** "A value below 0.5 is treated as 0.5 for the calendar height. Day grid views with height auto (or more than six rows) and multi-month views also use it to size their rows."
- **Confidence** (a) high, (b) medium

### allDayText
- **Axis** Javadoc
- **Where** `ViewOptions.java:5357`
- **Claim 1** The old Javadoc said "A newline in the text is shown as a line break in time grid views". The new one dropped it. The brief lists this as a wrong carried-over statement, but the code says it is **true** for 7.1.0.
- **Source 1**
  - `E/4b6eee98.js:319-321` wraps the text in a div with `classNames.whiteSpacePre`, which is `white-space: pre` (`fullcalendar/skeleton.css:278-280`).
  - `fc-docs/_docs-v7/CHANGELOG.md:380`: "Newlines in `allDayText` will be displayed as line breaks in TimeGrid".
  - Locales use it, for example `'Toute la\njournée'`.
  - List views put the text in the time cell as plain text (`E/33aacced.js:80-81`).
- **Fix 1** "A newline in the text is shown as a line break in time grid views."
- **Claim 2** The old sentence "FullCalendar documents this option only partly or not at all." was lost. It is still true. No docs page describes `allDayText`, only the changelog does. The `@see` target `allDaySlot` does not mention it. The statement "List views also show it as the time text of all-day entries" is also slightly short. List views show it for entries that span the whole day too, and only while `displayEventTime` is not false (`E/33aacced.js:79-81`).
- **Fix 2** Restore the old sentence. Also write "List views show it as the time text of all-day entries and of entries that span the whole day, unless entry times are hidden."
- **Confidence** high for claim 1 and the first half of claim 2, medium for the list-view detail.

### allDayHeaderContent
- **Axis** Javadoc
- **Where** `ViewOptions.java:159`
- **Claim** `("all-day" in English)`
- **Source** `E/71d3d952.js:26`: `allDayText: 'All-day'`.
- **Fix** `("All-day" in English)`
- **Confidence** high (minor)

### Lost defaults
- **Axis** Javadoc
- **Where**
  - `allDayMaintainDuration` `ViewOptions.java:3734`
  - `allDaySlot` `:4190`
  - `borderless` `:4936`
  - `borderlessBottom` `:4945`
  - `borderlessTop` `:4954`
  - `borderlessX` `:4964`
  - `columnEntryTitleSticky` `:5025`
  - `datesAboveResources` `SchedulerViewOptions.java:121`
- **Claim** Each Javadoc ends with "then the library default applies" and does not name the default. The old Javadoc had a Default entry for each.
- **Source**
  - `E/642eba18.js:821`: `allDayMaintainDuration: false`.
  - `E/4ec5fb96.js:79`: `allDaySlot: true`. Docs `allDaySlot.md` agree.
  - Docs `borderless.md`: default `false` for all four.
  - `E/642eba18.js:847`: `columnEventTitleSticky: true`.
  - Docs `datesAboveResources.md`: default `false`.
- **Fix** Add "The library default is false." (`allDayMaintainDuration`, the four `borderless*`, `datesAboveResources`) or "The library default is true." (`allDaySlot`, `columnEntryTitleSticky`).
- **Confidence** high

### columnEntryTitleSticky
- **Axis** Javadoc
- **Where** `ViewOptions.java:5025`
- **Claim** The old sentence "FullCalendar documents this option only partly or not at all." was lost. Still true: the `@see` page `column-event-render-hooks` never mentions `columnEventTitleSticky` (a grep of `fc-docs/_docs-v7` finds nothing). "Themes can change the default" is correct: `themes/monarch/index.js:157` sets it to `false`.
- **Source** `E/2d6c825e.js:1284`: `props.display === 'column' && options.columnEventTitleSticky && classNames.stickyT`.
- **Fix** Restore the old sentence and name the monarch theme as an example of a changed default.
- **Confidence** high

### backgroundEntryClass, backgroundEntryInnerClass, backgroundEntryTitleClass
- **Axis** Javadoc
- **Where** `ViewOptions.java:2239`, `:2320`, `:2352`
- **Claim** The old Javadoc said "The general entry hooks do not apply to background entries". The new one does not.
- **Source** Docs `background-event-render-hooks.md`: "The general Event Render Hooks DO NOT APPLY to background events." `E/44e811e2.js:508-565` (`BgEvent`) reads only the `backgroundEvent*` options.
- **Fix** "The general entry class options do not apply to background entries."
- **Confidence** high

### blockEntryClass, blockEntryBeforeClass, blockEntryAfterClass, blockEntryInnerClass, blockEntryTimeClass, blockEntryTitleClass
- **Axis** Javadoc
- **Where** `ViewOptions.java:1559`, `:1591`, `:1625`, `:1659`, `:1693`, `:1728`
- **Claim** "Block entries" is not explained. The old Javadoc said "(rectangular, color-filled entries, essentially all entries except list entries)" and "in addition to the general entry hook of the same part".
- **Source** Docs `block-event-render-hooks.md`. Code `E/2d6c825e.js:1251` and `1288-1293` joins the general class and the block class.
- **Fix** Say "block entries, the rectangular, color-filled entries (essentially all entries except list entries). It applies in addition to the general entry option of the same part."
- **Confidence** high

### columnEntryClass, columnEntryBeforeClass, columnEntryAfterClass, columnEntryInnerClass, columnEntryTimeClass, columnEntryTitleClass
- **Axis** Javadoc
- **Where** `ViewOptions.java:1762`, `:1794`, `:1828`, `:1862`, `:1897`, `:1932`
- **Claim** "Column entries" is not explained. The old Javadoc said "(rectangular, color-filled entries that render vertically: timed entries in time grid views)" and "in addition to the general entry hook".
- **Source** Docs `column-event-render-hooks.md`: "These hooks apply in addition to the general Event Render Hooks and Block Event Render Hooks". Code `E/2d6c825e.js:1283-1293`.
- **Fix** "Column entries are the color-filled entries that render vertically (timed entries in time grid views). It applies in addition to the general and the block option of the same part."
- **Confidence** high

### borderless, borderlessTop, borderlessBottom, borderlessX
- **Axis** Javadoc
- **Where** `ViewOptions.java:4936`, `:4945`, `:4954`, `:4964`
- **Claim** `borderless` says "Use borderlessTop, borderlessBottom or borderlessX to remove only some of them", and nothing says how the options combine.
- **Source** `E/642eba18.js:237-241` and `E/65e4f9c4.js:1694-1696`: `options.borderlessTop ?? options.borderless`. A set specific option, even `false`, wins over `borderless`.
- **Fix** On `borderless`: "A set borderlessTop, borderlessBottom or borderlessX wins over this option. So borderless true with borderlessTop false keeps the top border."
- **Confidence** high

### dayCellTopContent, dayCellTopInnerClass
- **Axis** Javadoc
- **Where** `ViewOptions.java:420`, `:452`
- **Claim** Nothing says the all-day section of time grid views has no day number.
- **Source** `E/4b6eee98.js:1575`: `TimeGridAllDayLane ... showDayNumbers: false`. `E/44e811e2.js:1046` renders the content and the inner wrapper only `props.showDayNumber && ...`. So in that section these two options have no effect, and `info.text` is empty (`hasLabel: props.showDayNumber`, line 1024).
- **Fix** "In the all-day section of time grid views the cells show no day number, so this option has no effect there."
- **Confidence** medium (read from code, not run)

## Checked, no finding
- Javadoc matches docs and code for:
  - `allDayDividerClass`, `allDayHeaderClass`, `allDayHeaderDidMount`, `allDayHeaderInnerClass`, `allDayHeaderWillUnmount`
  - `backgroundEntryColor`, `backgroundEntryContent`, `backgroundEntryDidMount`, `backgroundEntryWillUnmount`
  - `buttonClass`, `buttonDisplay`, `className`, `closeHint`, `colorScheme`
  - `columnMoreLinkClass`, `columnMoreLinkInnerClass`
  - `contentHeight`, `dateAlignment`
  - `dayCellBottomClass`, `dayCellClass`, `dayCellDidMount`, `dayCellFormat`, `dayCellInnerClass`, `dayCellTopClass`, `dayCellWillUnmount`, `dayHeaderAlign`
- `businessHours` matches the library defaults and `DEFAULT`/`NONE`. Past-midnight hours are ticket 62.
- Values: the converters for class names, content, height, header alignment and date formats match what the library reads.
- All `@see` pages exist in the fc-docs index. `allDayText` and `columnEntryTitleSticky` point to pages that do not describe them (see above).
- Mapping: no unmapped library form lacks a written reason.
  - `{domNodes}` is explained in the `ContentValue` Javadoc.
  - Command-string date formats are explained in the `DateFormatValue` Javadoc and ticket 24.
  - `buttons` as a `Map` is explained in ticket 30.

Checked 58 options, 13 findings.