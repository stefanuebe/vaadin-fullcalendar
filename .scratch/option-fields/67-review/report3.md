I could not write `report3.md`: the Write tool refused it, saying subagents should return findings as text. The full report is below instead. I read the repo and the library sources only and ran no builds.

Library under test is 7.1.0 (fullcalendar, fullcalendar-scheduler, @full-ui/headless-calendar). `FC/` means `e2e-test-app/node_modules/fullcalendar/chunks/` and `D/` means `fc-docs/_docs-v7/`. I did not report dropped defaults ("library default applies"), because ticket 17 and `.scratch/option-fields/option-fields-defaults.md` decided that.

### listDayFormat, listDayAltFormat
- **Axis**: Javadoc, and Values (`NONE` does not do what it claims).
- **Where**: `addon/src/main/java/org/vaadin/stefan/fullcalendar/ViewOptions.java:5643-5655` (listDayFormat) and `:5611-5623` (listDayAltFormat). `ListDayFormatValueConverter` sends `NONE` as `false`.
- **Claim**: "…or {@link ListDayFormatValue#NONE} to hide the text."
- **Source**: `D/views/listDayFormat.md` says "If `false` is specified, no text is displayed." The code disagrees. The refiner `createFalsableFormatter` turns `false` into `null` (`FC/642eba18.js:947-949`, used at `:763-764`). The list day header then reads `options.listDayFormat ?? createDefaultListDayFormat(viewSpec)` and the same for the alt format (`FC/33aacced.js:37-38`). `null ?? default` gives the default, so the text stays visible. No Playwright or other test covers this. I did not run the library.
- **Fix**: Say so in the Javadoc: "The library docs say `false` hides the text. In the library version the add-on uses, `false` makes the library fall back to its default format, so the text stays visible." Then decide whether to keep `NONE`. A callback returning an empty string might hide the text, but that is untested, so don't write it into the Javadoc.
- **Confidence**: high on the code reading, not run in a browser.

### hiddenDays
- **Axis**: Values and Javadoc.
- **Where**: `ViewOptions.java:4012-4033`, including `setHiddenDays(Set<DayOfWeek>)`.
- **Claim**: Missing. The setter accepts all seven days. The Javadoc only says "Without a value, no day is hidden, unless weekends is false."
- **Source**: `FC/54e239b5.js:260-275` (`initHiddenDays`). It adds Sunday and Saturday when `weekends === false`, counts the visible days, and throws `Error('invalid hiddenDays')` when none are left.
- **Fix**: Javadoc: "Hiding every day, also together with `weekends` set to `false`, makes the library throw an error. At least one day must stay visible." The setter can reject seven days at once with `IllegalArgumentException`. The `weekends` combination can only be documented, because it spans two fields.
- **Confidence**: high.

### footerScrollbarSticky
- **Axis**: Javadoc.
- **Where**: `ViewOptions.java:5299-5309`.
- **Claim**: "…AUTO… activates it when the calendar height is automatic". This implies `TRUE` always sticks.
- **Source**: `FC/2d6c825e.js:327-334`. It returns `Boolean(footerScrollbarSticky) && isHeightAuto`. `getIsHeightAuto` is `height === 'auto' || contentHeight === 'auto'` (`:317-319`). So `TRUE` also needs an automatic height and behaves like `AUTO`; only `FALSE` differs. `D/overall-display/footerScrollbarSticky.md` is silent on this.
- **Fix**: "The scrollbar sticks only while the calendar height is automatic (`height` or `contentHeight` is `'auto'`), also with `TRUE`. `FALSE` turns it off."
- **Confidence**: high.

### expandRows
- **Axis**: Javadoc.
- **Where**: `ViewOptions.java:4216-4224`.
- **Claim**: "Applies to time grid and timeline views." The height condition is missing.
- **Source**: `FC/4b6eee98.js:1306` and `:1319` (`verticalScrolling = !forPrint && !getIsHeightAuto(options)`, then `verticalScrolling && options.expandRows`). Timeline: `fullcalendar-scheduler/chunks/8f1316dc.js:1383`. `D/overall-display/expandRows.md` is silent.
- **Fix**: "It has no effect while the calendar height is automatic."
- **Confidence**: medium. I read the time grid code closely and the timeline line only briefly.

### entryStartEditable
- **Axis**: Javadoc.
- **Where**: `CalendarOptions.java:396-405`.
- **Claim**: What the Javadoc says is correct, but it omits what happens without a value. The old Javadoc said "Default true, effective only when EDITABLE is also true". The docs say "default: `true`". Neither matches the code.
- **Source**: `FC/642eba18.js:1322` (`startEditable: refined.startEditable != null ? refined.startEditable : refined.editable`). `eventStartEditable` has no entry in `BASE_OPTION_DEFAULTS`, and `editable: false` is at `:805`.
- **Fix**: "Without a value, `editable` decides, whose library default is `false`. The library docs name `true` as the default of this option, but the code sets none."
- **Confidence**: high.

### entryTimeClass, entryTitleClass, listItemEntryTimeClass
- **Axis**: Javadoc.
- **Where**: `ViewOptions.java:2548-2560`, `:2580-2592`, `:2759-2772`.
- **Claim**: "A callback gets an info object with event, isNarrow and isShort". The old Javadoc had the same sentence.
- **Source**: The `.d.ts` (`FC/996d27e3.d.ts:1939-1963`) lists those three, but the runtime also passes `timeText`. `FC/2d6c825e.js:1260-1265` builds `subcontentRenderProps` with `timeText`, and `:1283-1284` passes it to these three hooks. The add-on's `listItemEntryTitleClass` Javadoc already names `timeText`.
- **Fix**: "…with `event`, `timeText`, `isNarrow` and more, see the library docs."
- **Confidence**: medium. The runtime shows it, but the typings and docs do not.

### entryTimeFormat
- **Axis**: Javadoc (lost fact).
- **Where**: `ViewOptions.java:5578-5591`.
- **Claim**: The list-view default is gone. The old Javadoc had "like 7pm in list views".
- **Source**: `D/events/eventTimeFormat.md` (list views, `meridiem: 'short'`, "7pm").
- **Fix**: Add "and `7pm` in list views".
- **Confidence**: high.

### entriesHint
- **Axis**: Javadoc.
- **Where**: `ViewOptions.java:5369-5378`.
- **Claim**: "…in list views" only. The old note "FullCalendar documents this option only partly or not at all" is gone. The `@see` page `D/accessibility/hints.md` exists but never mentions `eventsHint`.
- **Source**: The scheduler also uses it. `fullcalendar-scheduler/chunks/fc014b1a.js:1753` sets it as `aria-label` on the timeline body. `fullcalendar-scheduler/chunks/8f1316dc.js:1501` uses it as offscreen column header text. List view: `FC/33aacced.js:114`. English default "Events": `FC/71d3d952.js:25`.
- **Fix**: "…in list views and in the scheduler's timeline views. FullCalendar's docs do not describe this option; the behaviour comes from the library code."
- **Confidence**: high.

### moreLinkText, monthText
- **Axis**: Javadoc (lost fact).
- **Where**: `ViewOptions.java:5412-5426` and `CalendarOptions.java:647-654`.
- **Claim**: The old Javadoc of both said the docs cover the option only partly or not at all, and the new one does not. The `@see` page of `moreLinkText` (`D/events/more-link-render-hooks.md`) never mentions `moreLinkText`.
- **Source**: `moreLinkText` appears in the docs only at `D/CHANGELOG.md:2066`. `monthText` appears only as a locale example at `D/overall-display/locale.md:115`. The statements in the new `moreLinkText` Javadoc do match the code (`FC/44e811e2.js` around `buildMoreLinkRenderProps`).
- **Fix**: Add "FullCalendar's docs describe this option only in passing; the behaviour here comes from the library code." Adjust the `@see` label of `moreLinkText` so it does not imply the page documents the option.
- **Confidence**: high that the note was lost, medium that it must come back.

### firstDay
- **Axis**: Javadoc.
- **Where**: `CalendarOptions.java:75-83`.
- **Claim**: "Without a value, the locale decides."
- **Source**: `D/overall-display/firstDay.md` says ISO `weekNumberCalculation` makes the default Monday. Code: `@full-ui/headless-calendar/index.js:567-575`.
- **Fix**: "Without a value, the locale decides, or Monday if `weekNumberCalculation` is `ISO`."
- **Confidence**: high.

### entryWillUnmount, inlineWeekNumberWillUnmount, listDayHeaderWillUnmount, moreLinkWillUnmount
- **Axis**: Javadoc.
- **Where**: `ViewOptions.java:2612-2625`, `:889-902`, `:3339-3351`, `:3460-3471`.
- **Claim**: "…the new setting only applies to newly rendered elements… Existing elements are not updated." This holds for the DidMount fields, but may be wrong for WillUnmount.
- **Source**: `FC/2d6c825e.js:288-293`. `componentWillUnmount` calls `this.props.willUnmount`, the props at removal time. If an existing element is re-rendered after an option change, which I did not trace, it calls the new callback when removed.
- **Fix**: Verify with a browser test first. If the new callback runs, say so for these four fields.
- **Confidence**: unsure.

### entryWillUnmount (extra note)
- **Axis**: Javadoc.
- **Where**: `ViewOptions.java:2612-2625`.
- **Claim**: Missing. With `dayMaxEvents: true` in day grid, not every entry goes through the hook.
- **Source**: `D/CHANGELOG.md:6-7` (v7.1.0): "only a necessary subset of events will be DOM-inserted… BREAKING: a subset of events will be run through event render hooks". I did not check whether the add-on sets `dayMaxEvents`.
- **Fix**: Optional: "In day grid views with `dayMaxEvents` set to `true`, the callback runs only for the entries the library renders."
- **Confidence**: unsure whether it belongs here.

Checked 58 options, 12 findings.

The 58 are the 60 options in part 3 minus the excluded `height` and `moreLinkClick`. The other options had no finding, including `eventTimeFormat`'s string-format gap, which is a documented mapping reason in ticket 24 and in the `DateFormat` Javadoc.