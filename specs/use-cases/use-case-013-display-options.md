# UC-013: Display Options

**As a** Vaadin application developer, **I want to** configure various display options **so that** the calendar appearance matches my application's requirements.

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon
**Related Options:** `weekends`, `hiddenDays`, `allDaySlot`, `slotDuration`, `slotMinTime`, `slotMaxTime`, `slotHeaderFormat`, `slotHeaderInterval`, `contentHeight`, `aspectRatio`, `expandRows`, `fixedWeekCount`, `showNonCurrentDates`, `dayHeaders`, `dayHeaderFormat`, `dayMinWidth`, `dayMaxEntries`, `dayMaxEntryRows`, `entryMaxStack`, `nowIndicator`, `weekNumbers`, `weekTextShort`, `scrollTime`, `nextDayThreshold`, `multiMonthMaxColumns` (the calendar height is not an option field, it is set with `setHeight`)
**Related Events:** `MoreLinkClickedEvent`

---

## User-Facing Behavior

- Hide/show weekends, specific days, all-day slot
- Control time grid granularity (slot duration, min/max time)
- Configure calendar sizing (height, aspect ratio)
- Limit entries per day with "+N more" link
- Show current time indicator
- Show/hide week numbers
- Configure initial scroll position in timegrid

---

## Java API Usage

```java
// Hide weekends
calendar.options().setWeekends(false);

// Hide specific days
calendar.options().setHiddenDays(Set.of(DayOfWeek.SUNDAY));

// Time grid: 8am to 6pm in 15min slots
calendar.options().setSlotMinTime(Duration.ofHours(8));
calendar.options().setSlotMaxTime(Duration.ofHours(18));
calendar.options().setSlotDuration(Duration.ofMinutes(15));

// Sizing
calendar.setHeight("600px");
calendar.options().setExpandRows(true);

// Limit entries per day
calendar.options().setDayMaxEntries(3); // "+N more" link after 3
calendar.options().setDayMaxEntries(EntryLimitValue.CELL_HEIGHT); // auto based on cell height
calendar.options().setDayMaxEntries(EntryLimitValue.NONE); // no limit

// What the "+N more" link does; the server receives the click with every value
calendar.options().setMoreLinkClick(MoreLinkClickValue.Action.DAY);

// Current time indicator
calendar.options().setNowIndicator(true);

// Week numbers
calendar.options().setWeekNumbers(true);

// Initial scroll position
calendar.options().setScrollTime(Duration.ofHours(8));
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | `weekends = false` hides Saturday and Sunday columns |
| BR-02 | `hiddenDays` can hide any combination of days |
| BR-03 | `slotMinTime` / `slotMaxTime` restrict visible time range in timegrid |
| BR-04 | Duration options accept `Duration`, `Period`, `CalendarDuration` or a string (`"hh:mm:ss"`) |
| BR-05 | `dayMaxEntries` triggers "+N more" popover when exceeded |
| BR-06 | `MoreLinkClickedEvent` fires when user clicks "+N more", whatever `moreLinkClick` is set to, also with a `JsCallback` |
| BR-07 | `nowIndicator` only works in timegrid views |
| BR-08 | View-specific options can override these for particular views |
| BR-09 | The "+N more" popover inherits `--fc-page-bg-color`. **Known gap**: FullCalendar's popover does not implement keyboard focus trapping or Escape-to-close-and-return-focus. This is a FullCalendar limitation. |
| BR-10 | Format options (`dayCellFormat`, `titleFormat`, …) take a `DateFormatValue`, a `DateFormat` or a `JsCallback`. Most also have a setter that takes a `Map<String, Object>` |
| BR-11 | `duration`, `defaultTimedEntryDuration` and `defaultAllDayEntryDuration` take a `Duration`, a `Period`, a string or a `CalendarDuration`. A `Duration` counts days as 24 hours, so use `CalendarDuration` for days, weeks or months, for example `CalendarDuration.ofWeeks(1)`, which aligns the view to the start of the week |
| BR-12 | `dayCount` sets the exact number of days, regardless of `weekends` and `hiddenDays`. With `duration`, hidden days are omitted |
| BR-13 | `now` takes a `LocalDate` or a `LocalDateTime` (sent as UTC, like entry start and end), or a `NowValue` |
| BR-14 | `colorScheme` is per calendar. Without it, the calendar follows what the page sets (e.g. `data-color-scheme` on a parent element) |
| BR-15 | `headingLevel` changes the `aria-level` of the title, not how it looks. Default 2 |
| BR-16 | `dayNarrowWidth` is the day column width in pixels below which FullCalendar uses narrow text. It shows up as `isNarrow` in the `info` of related render hooks |

---

## Acceptance Criteria

- [ ] Weekend columns hidden when `weekends = false`
- [ ] Hidden days not rendered
- [ ] Custom slot duration renders correct grid
- [ ] Time range limited to `slotMinTime`..`slotMaxTime`
- [ ] "+N more" link appears when entry limit exceeded
- [ ] `MoreLinkClickedEvent` fires on "+N more" click
- [ ] Now indicator visible in timegrid
- [ ] Week numbers displayed when enabled
- [ ] Custom scroll time positions timegrid correctly
- [ ] Day cell and title formats change the shown text
- [ ] `dayCount` and `duration` change the number of days shown
- [x] `noEntriesText` shows in an empty list view
- [ ] `todayText` shows on the today button
- [ ] `borderlessX` and `colorScheme` change the calendar's appearance
- [x] `headingLevel` sets `aria-level` of the title

---

## Tests

### Unit Tests
- [ ] `DisplayOptionsTest` — option validation
- [ ] `FullCalendarOptionsTest` — option setting/getting
- `TypedOptionValuesTest` — typed values (`HeaderAlign`, `DateRange`, durations, `LocalDate`/`LocalDateTime` for `now`) reach the client in the expected form
- `OptionCompletenessTest` (addon-scheduler) — every FullCalendar option has a field, or is left out with a reason

### E2E Tests
- [ ] `display-options.spec.js` — display option rendering
- [x] `fc7-options.spec.js`: `noEntriesText` shows in an empty list view, `headingLevel` sets the toolbar title's heading level
- [x] `more-link-click.spec.js`: action and callback, set before and after attach, are applied and the server receives the click

---

## Related FullCalendar Docs

- [weekends](https://fullcalendar.io/docs/weekends)
- [slotDuration](https://fullcalendar.io/docs/slotDuration)
- [dayMaxEvents](https://fullcalendar.io/docs/dayMaxEvents)
- [nowIndicator](https://fullcalendar.io/docs/nowIndicator)
