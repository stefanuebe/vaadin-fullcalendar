# 09: Option-name resource for the core module

| | |
|---|---|
| GitHub | – (not created, GitHub account flagged) |
| Labels | enhancement, ready-for-agent |
| Status | done (2026-10-15) |
| Blocked by | None (can start immediately) |
| Spec | `../option-fields-spec.md`; shared rules in `README.md` |

---

**What to build:** A resource file in `addon` lists every library option name the core add-on supports, generated once from `Option` (before 51 deletes it), corrected where the key differs from the library (`class` becomes `className`), with one flag per name: global or per view. The global names are the two groups in *Types* of the spec (read only from the calendar options by FullCalendar 7.1: `buildDateEnv` and `parseToolbars` inputs, `views`, `plugins`, `initialEvents`, `events`, `eventSources`; global by the add-on's choice: listeners, `eventDidMount`, `moreLinkClick`). Each group is re-derived from the library source; the groups grew during the work, see the notes below for the final list. Carries an `FC-UPDATE:` note. Spec: *Library updates*.

**Blocked by:** None (can start immediately)

**Status:** done (2026-10-15). Review stopped at round 5 (last major: an overreaching `defaultAllDay` sentence in the resource header, rewritten to state only what is verified); the maintainer chose no round 6.

- [x] The resource lists every non-deprecated `Option` key once, with its flag and corrected names.
- [x] The global flags are checked against the FullCalendar 7.1 source in `e2e-test-app/node_modules/fullcalendar`; the ticket notes where each group was found.

**Where each group was found (2026-10-15, library 7.1.0 in `e2e-test-app/node_modules`):**

- `buildDateEnv(refinedOptions.timeZone, …locale, …weekNumberCalculation, …firstDay, …weekTextLong, …weekTextShort, …)` in `processRawCalendarOptions` (`fullcalendar/chunks/65e4f9c4.js`).
- Toolbar: `parseToolbars(calendarOptions, …)` reads `headerToolbar`, `footerToolbar`, `direction` and `toolbarElements`. `getButtonState()` reads `buttons`, `prevText`, `nextText`, `prevYearText`, `nextYearText`, `todayText`, `todayHint`, `prevHint`, `nextHint`, `viewHint` and `yearText` from `currentData.calendarOptions`. The view buttons read `listText` through the list view's `buttonTextKey`, `dayText` and `monthText` through `getSingleUnitText` (`65e4f9c4.js`).
- `direction`: the calendar root sets its `dir` from `currentData.calendarOptions.direction` (`6143324c.js`). The only view-level reader sets `dir` on a dragged entry element (`2d6c825e.js`), so a per-view value has no visible effect.
- `CALENDAR_ONLY_OPTION_REFINERS` (`642eba18.js`): `class`, `className`, `viewClass`, `viewDidMount`, `viewWillUnmount`, `views`, `plugins`, `initialEvents`, `events`, `eventSources`. The last five have no `Option` constant and are not in the resource.
- Read only from the calendar options: `initialView` and `initialDate` when the calendar is created (`optionsData.calendarOptions.initialView`, `getInitialDate(optionsData.calendarOptions, …)`), `rerenderDelay` (`6143324c.js`).
- `buildViewUiProps(calendarContext)` (`65e4f9c4.js`, `calendarContext.options` is `optionsData.calendarOptions`) reads `eventDisplay`, `eventStartEditable`, `eventDurationEditable`, `eventConstraint`, `eventAllow`, `selectConstraint`, `selectAllow`, with no other reader. It also reads `editable`, `eventOverlap` and `selectOverlap`, but views read these too: `editable` decides whether a view takes entries dropped from another calendar (`5342e739.js`, `receivingOptions.editable && receivingOptions.droppable` on the view context), the function form of `selectOverlap` and `eventOverlap` are read from the view context (`f0ab0579.js`), and entry render props read `eventOverlap` (`2d6c825e.js`, `44e811e2.js`). They stay per view.
- Fetching entries runs in the calendar context: `eventDataTransform` (`b00ce53f.js`), `lazyFetching`, `eventSourceSuccess`, `startParam`, `endParam`, `timeZoneParam` (`71d3d952.js`, the scheduler's resource fetch in `fullcalendar-scheduler/chunks/0a9e97be.js` too), `googleCalendarApiKey` (`@fullcalendar/google-calendar/global.js`, `arg.context.options`).
- `CALENDAR_LISTENER_REFINERS` (`642eba18.js`): of these, only `loading` has an `Option` constant.
- Add-on choice: `eventDidMount`, `moreLinkClick`.
- Checked and left per view: `visibleRange` (read from the view's refined options for the date profile), `businessHours` (the hours come from the calendar options, but a view reads whether to show them, so `false` per view hides them), `eventOverlap` (see above), the toolbar class and button display options (the toolbar renders in the view context), `height`, `contentHeight`, `aspectRatio`, `colorScheme`, `borderless*`, `now`, `navLinks`, `titleFormat`. `defaultAllDay` is read only through `computeIsDefaultAllDay(eventSource, context)` in `parseEvent`. The external drop does not read it. The contexts passed by the callers of `parseEvents` (event source reducer, `addEvent`, `normalizeConstraint`) were not all traced, so it stays per view.
- `editable` was briefly moved to `CalendarOptions` during the review and moved back. The view tests of 07 and 08 now use `weekends`, which is read per view (`refinedOptions.weekends` for the date profile); the field `weekends` was taken from 25 for this.
