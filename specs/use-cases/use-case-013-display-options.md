# UC-013: Display Options

**As a** Vaadin application developer, **I want to** configure various display options **so that** the calendar appearance matches my application's requirements.

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon
**Related Options:** `Option.WEEKENDS`, `Option.HIDDEN_DAYS`, `Option.ALL_DAY_SLOT`, `Option.SLOT_DURATION`, `Option.SLOT_MIN_TIME`, `Option.SLOT_MAX_TIME`, `Option.SLOT_HEADER_FORMAT`, `Option.SLOT_HEADER_INTERVAL`, `Option.HEIGHT`, `Option.CONTENT_HEIGHT`, `Option.ASPECT_RATIO`, `Option.EXPAND_ROWS`, `Option.FIXED_WEEK_COUNT`, `Option.SHOW_NON_CURRENT_DATES`, `Option.DAY_HEADERS`, `Option.DAY_HEADER_FORMAT`, `Option.DAY_MIN_WIDTH`, `Option.DAY_MAX_ENTRIES`, `Option.DAY_MAX_ENTRY_ROWS`, `Option.ENTRY_MAX_STACK`, `Option.NOW_INDICATOR`, `Option.WEEK_NUMBERS`, `Option.WEEK_TEXT_SHORT`, `Option.SCROLL_TIME`, `Option.NEXT_DAY_THRESHOLD`, `Option.MULTI_MONTH_MAX_COLUMNS`
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
calendar.setOption(Option.WEEKENDS, false);

// Hide specific days
calendar.setOption(Option.HIDDEN_DAYS, Set.of(DayOfWeek.SUNDAY));

// Time grid: 8am to 6pm in 15min slots
calendar.setOption(Option.SLOT_MIN_TIME, LocalTime.of(8, 0));
calendar.setOption(Option.SLOT_MAX_TIME, LocalTime.of(18, 0));
calendar.setOption(Option.SLOT_DURATION, Duration.ofMinutes(15));

// Sizing
calendar.setHeight("600px");
calendar.setOption(Option.EXPAND_ROWS, true);

// Limit entries per day
calendar.setOption(Option.DAY_MAX_ENTRIES, 3); // "+N more" link after 3
calendar.setOption(Option.DAY_MAX_ENTRIES, true); // auto based on cell height
calendar.setOption(Option.DAY_MAX_ENTRIES, false); // no limit

// What the "+N more" link does; the server receives the click with every value
calendar.setOption(Option.MORE_LINK_CLICK, FullCalendar.MoreLinkClickAction.DAY);

// Current time indicator
calendar.setOption(Option.NOW_INDICATOR, true);

// Week numbers
calendar.setOption(Option.WEEK_NUMBERS, true);

// Initial scroll position
calendar.setOption(Option.SCROLL_TIME, LocalTime.of(8, 0));
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | `WEEKENDS = false` hides Saturday and Sunday columns |
| BR-02 | `HIDDEN_DAYS` can hide any combination of days |
| BR-03 | `SLOT_MIN_TIME` / `SLOT_MAX_TIME` restrict visible time range in timegrid |
| BR-04 | Duration options accept `Duration`, `LocalTime`, or string (`"HH:MM:SS"`) |
| BR-05 | `DAY_MAX_ENTRIES` triggers "+N more" popover when exceeded |
| BR-06 | `MoreLinkClickedEvent` fires when user clicks "+N more", whatever `MORE_LINK_CLICK` is set to, also with a `JsCallback` |
| BR-07 | `NOW_INDICATOR` only works in timegrid views |
| BR-08 | View-specific options can override these for particular views |
| BR-09 | The "+N more" popover inherits `--fc-page-bg-color`. **Known gap**: FullCalendar's popover does not implement keyboard focus trapping or Escape-to-close-and-return-focus. This is a FullCalendar limitation. |
| BR-10 | Format options (`DAY_CELL_FORMAT`, `TITLE_FORMAT`, …) take a format object, e.g. a `Map<String, Object>` |
| BR-11 | `DURATION` takes a `Map` or a `String`, not a `java.time.Duration`, which is converted to hours, minutes and seconds and cannot express days or weeks. `DEFAULT_TIMED_ENTRY_DURATION` takes a `Duration` or a string, `DEFAULT_ALL_DAY_ENTRY_DURATION` a `Map` or a string |
| BR-12 | `DAY_COUNT` sets the exact number of days, regardless of `WEEKENDS` and `HIDDEN_DAYS`. With `DURATION`, hidden days are omitted |
| BR-13 | `NOW` takes a `LocalDate`, a `LocalDateTime` (sent as UTC, like entry start and end) or an ISO 8601 string |
| BR-14 | `COLOR_SCHEME` is per calendar. Without it, the calendar follows what the page sets (e.g. `data-color-scheme` on a parent element) |
| BR-15 | `HEADING_LEVEL` changes the `aria-level` of the title, not how it looks. Default 2 |
| BR-16 | `DAY_NARROW_WIDTH` is the day column width in pixels below which FullCalendar uses narrow text. It shows up as `isNarrow` in the `info` of related render hooks |
| BR-17 | `STICKY_HEADER_DATES` and `STICKY_FOOTER_SCROLLBAR` are deprecated aliases of `TABLE_HEADER_STICKY` and `FOOTER_SCROLLBAR_STICKY` |

---

## Acceptance Criteria

- [ ] Weekend columns hidden when `WEEKENDS = false`
- [ ] Hidden days not rendered
- [ ] Custom slot duration renders correct grid
- [ ] Time range limited to SLOT_MIN_TIME..SLOT_MAX_TIME
- [ ] "+N more" link appears when entry limit exceeded
- [ ] `MoreLinkClickedEvent` fires on "+N more" click
- [ ] Now indicator visible in timegrid
- [ ] Week numbers displayed when enabled
- [ ] Custom scroll time positions timegrid correctly
- [ ] Day cell and title formats change the shown text
- [ ] `DAY_COUNT` and `DURATION` change the number of days shown
- [x] `NO_ENTRIES_TEXT` shows in an empty list view
- [ ] `TODAY_TEXT` shows on the today button
- [ ] `BORDERLESS_X` and `COLOR_SCHEME` change the calendar's appearance
- [x] `HEADING_LEVEL` sets `aria-level` of the title

---

## Tests

### Unit Tests
- [ ] `DisplayOptionsTest` — option validation
- [ ] `FullCalendarOptionsTest` — option setting/getting
- `TypedOptionValuesTest` — typed values (`HeaderAlign`, `DateRange`, durations, `LocalDate`/`LocalDateTime` for `NOW`) reach the client in the expected form
- `OptionCompletenessTest` (addon-scheduler) — every FullCalendar 7.1.0 option has a constant, or is left out with a reason

### E2E Tests
- [ ] `display-options.spec.js` — display option rendering
- [x] `fc7-options.spec.js`: `NO_ENTRIES_TEXT` shows in an empty list view, `HEADING_LEVEL` sets the toolbar title's heading level
- [x] `more-link-click.spec.js`: action and callback, set before and after attach, are applied and the server receives the click

---

## Related FullCalendar Docs

- [weekends](https://fullcalendar.io/docs/weekends)
- [slotDuration](https://fullcalendar.io/docs/slotDuration)
- [dayMaxEvents](https://fullcalendar.io/docs/dayMaxEvents)
- [nowIndicator](https://fullcalendar.io/docs/nowIndicator)
