I could not write `report4.md`. The Write tool refused with "Subagents should return findings as text, not write report files". I did not try to get around that with Bash, so the report is below instead.

# Report part 4 (navLinkClass ... resourceDayHeaderInnerClass)

Skipped as excluded: navLinkDayClick, navLinkWeekClick, nextDayThreshold. I checked the other 57 options.

There is no finding on Values or Mapping.
- **Class-only options.** `navLinkClass`, `nonBusinessHoursClass`, `nowIndicatorDotClass`, `popoverClass`, `popoverCloseClass` and `resourceColumnDividerClass` are `refineClassName` (`fullcalendar/chunks/642eba18.js:24-30`), so a plain `String` field is right.
- **Left-out forms.** The format-string form of `popoverFormat` and the `Date`/ms forms of `now` are left out with a reason in `.scratch/option-fields/issues/24-value-types-auto-false-now.md:31,33`.
- **`WidthValue`.** It matches `parseDimConfig` in `@full-ui/headless-grid/index.js:5-25` (number, `%` and `px` strings).

Statements I verified and found correct:
- **Hints.** `nextHint`, `prevHint` and `navLinkHint` set `aria-label` only (`65e4f9c4.js:1782`, `2d6c825e.js:775`).
- **Now indicator.** `info.view` exists in the now-indicator hooks (`996d27e3.d.ts:629-641`, which the docs page omits). `now` is read only at creation (`65e4f9c4.js:673-695`, `1049`).
- **`popoverFormat`.** The default and the callback argument are right (`642eba18.js:827`, `@full-ui/headless-calendar/index.js:309-319`).
- **Resource content hooks.** The merge rules for `resourceCellContent` and `resourceColumnHeaderContent` are right (`fullcalendar-scheduler/chunks/8f1316dc.js:2058-2090`, `fullcalendar/chunks/28b4e459.js:76-97`).
- **Add-on claims.** These hold for `navLinks` (`full-calendar.ts:386-397`), `resourceAdd` and `resourceChange` (`full-calendar-scheduler.ts:71-143`), and the adaptive plugin (`full-calendar-scheduler.ts:65-66`).

### nowIndicatorSnap
**Axis** Javadoc
**Where** addon/src/main/java/org/vaadin/stefan/fullcalendar/ViewOptions.java:4247-4258 (`@see`)
**Claim** `@see <a href="https://fullcalendar.io/docs/nowIndicatorSnap">`, carried over from the old `NOW_INDICATOR_SNAP`.
**Source**
- `fc-docs/_docs-v7/index.md` has no `nowIndicatorSnap` page.
- The option appears only in `fc-docs/_docs-v7/CHANGELOG.md:622-623` ("setting to control how nowIndicator displays in all-day slots ... use `false` to ensure time-granularity always").
- The new Javadoc also lost the old note that the docs are thin. Compare old `NEXT_YEAR_TEXT`: "FullCalendar documents this option only partly or not at all."

**Fix** Link `https://fullcalendar.io/docs/nowIndicator` instead and add: "FullCalendar does not document this option on its own page."
**Confidence** high that the page is missing from the local index. Unsure whether it exists on the web.

### nowIndicatorSnap (default)
**Axis** Javadoc
**Where** ViewOptions.java:4258
**Claim** The Javadoc does not say what applies when the option is not set. The old one had `Default: "auto"`.
**Source** `fullcalendar/chunks/642eba18.js:848` (`nowIndicatorSnap: 'auto'`).
**Fix** "Without a value, the library uses {@link AutoBoolean#AUTO}."
**Confidence** medium. The ticket README may have dropped the old `<dl>` Default blocks on purpose.

### navLinks (default)
**Axis** Javadoc
**Where** ViewOptions.java:4799
**Claim** The old default `false` is gone.
**Source** `fc-docs/_docs-v7/date-time/navLinks.md`: "boolean. *default*: `false`".
**Fix** "Without a value, day and week labels are no links."
**Confidence** medium

### nowIndicator (default)
**Axis** Javadoc
**Where** ViewOptions.java:4244
**Claim** The old default `false` is gone.
**Source** `fc-docs/_docs-v7/date-time/nowIndicator.md`: "Boolean. *default*: `false`".
**Fix** "Without a value, no marker is shown."
**Confidence** medium

### nextHint
**Axis** Javadoc
**Where** addon/src/main/java/org/vaadin/stefan/fullcalendar/CalendarOptions.java:658-669
**Claim** "Accessible label (aria-label) of the "next" button ... Without a value, the locale's text is used." It also says "a canonical unit id such as day, week or month".
**Source**
- The option also labels the "next year" button, with `$0` = `yearText` and unit id `'year'` (`65e4f9c4.js:511`, `1629`).
- The locale defines its own hint (`71d3d952.js:45`, `nextHint: 'Next $0'`). The button text `nextText` is only the fallback when no string or function is given (`642eba18.js:219-227`).
- "The locale's text" can be read as `nextText`, which is wrong.

**Fix** "Without a value, the hint of the locale is used, such as "Next week" in English. The option also labels the "next year" button; then the placeholder and the unit id stand for the year ({@code 'year'})."
**Confidence** high for the year button, medium for the wording of the default.

### prevHint
**Axis** Javadoc
**Where** CalendarOptions.java:709-720
**Claim** Same as `nextHint`.
**Source** `65e4f9c4.js:508`, `1624`; `71d3d952.js:44`.
**Fix** Same as `nextHint`, for the "previous year" button.
**Confidence** high for the year button, medium for the default wording.

### nextYearText
**Axis** Javadoc
**Where** CalendarOptions.java:705
**Claim** The old note "FullCalendar documents this option only partly or not at all" is gone.
**Source**
- No page in `fc-docs/_docs-v7` names `nextYearText` or `prevYearText`. Only the `prevYear`/`nextYear` buttons appear, in `headerToolbar.md:24,27`.
- The option exists in the library (`642eba18.js:475-476`), with default `'Next year'` in `71d3d952.js:18`.

**Fix** "FullCalendar does not document this option. The locale defines the default text." For `prevYearText` the default is `'Prev year'`.
**Confidence** medium

### prevYearText
**Axis** Javadoc
**Where** CalendarOptions.java:756
**Claim, Source, Fix** See `nextYearText`.
**Confidence** medium

### rerenderDelay
**Axis** Javadoc
**Where** CalendarOptions.java:580-591
**Claim** "...before it rerenders everything at once", after "an operation that may rerender it, such as changing the view or adding an entry". It misses that some operations skip the delay.
**Source**
- `fullcalendar/chunks/6143324c.js:24-36` renders at once for `SET_EVENT_DRAG`, `UNSET_EVENT_DRAG`, `SET_EVENT_RESIZE`, `UNSET_EVENT_RESIZE` and `MERGE_EVENTS`.
- `MERGE_EVENTS` is what changes to an existing entry via the client API dispatch (`642eba18.js:2027`, `2055`).
- `addEvent` uses `ADD_EVENTS` (`65e4f9c4.js:1460`), so the "adding an entry" example is right.

**Fix** "The delay does not apply while an entry is dragged or resized, nor to a change of an existing entry made with the client API. Those render at once."
**Confidence** medium. I did not trace which client calls the add-on uses to update entries.

### printMaxRows
**Axis** Javadoc
**Where** addon-scheduler/src/main/java/org/vaadin/stefan/fullcalendar/SchedulerViewOptions.java:247
**Claim** The old default 1000 is gone.
**Source** `fc-docs/_docs-v7/views/printMaxRows.md` ("*default*: `1000`"); `fullcalendar-scheduler/chunks/dc4db303.js:150`.
**Fix** "Without a value, at most 1000 rows are rendered."
**Confidence** medium

### resourceColumnsWidth
**Axis** Javadoc
**Where** SchedulerViewOptions.java:167
**Claim** The old default `"30%"` is gone.
**Source** `fc-docs/_docs-v7/views/resourceColumnsWidth.md` (*default*: `"30%"`); `fullcalendar-scheduler/chunks/8f1316dc.js:2309` (set for the `resourceTimeline` view).
**Fix** "Without a value, the width is 30 percent in the resource timeline view."
**Confidence** medium

### refetchResourcesOnNavigate
**Axis** Javadoc
**Where** addon-scheduler/src/main/java/org/vaadin/stefan/fullcalendar/SchedulerCalendarOptions.java:58-67
**Claim** "The resources function or JSON feed then receives start, end and timezone." The old default `false` is also gone.
**Source**
- Docs: `fc-docs/_docs-v7/resources/refetchResourcesOnNavigate.md` (default `false`, function and feed).
- The add-on sets `options.resources = options.resources ?? []` (`full-calendar-scheduler.ts:63`) and sends resources through `addResource` (`:71-75`).
- A static array is not refetched (`fullcalendar-scheduler/chunks/0a9e97be.js:334-343`).
- I found no typed way to set a resources function or feed. I did not check whether the raw option API can.

**Fix** If the raw option is the only way: "The add-on manages resources itself. The option only matters for a resources function or JSON feed set as a raw option." Also add "Without a value, the resources are not refetched."
**Confidence** unsure, because I did not verify the raw option route.

Checked 57 options, 12 findings.