# UC-013: Display Options

**As a** Vaadin application developer, **I want to** configure various display options **so that** the calendar appearance matches my application's requirements.

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon
**Related Options:** `Option.WEEKENDS`, `Option.HIDDEN_DAYS`, `Option.ALL_DAY_SLOT`, `Option.SLOT_DURATION`, `Option.SLOT_MIN_TIME`, `Option.SLOT_MAX_TIME`, `Option.SLOT_LABEL_FORMAT`, `Option.SLOT_LABEL_INTERVAL`, `Option.HEIGHT`, `Option.CONTENT_HEIGHT`, `Option.ASPECT_RATIO`, `Option.EXPAND_ROWS`, `Option.FIXED_WEEK_COUNT`, `Option.SHOW_NON_CURRENT_DATES`, `Option.DAY_HEADERS`, `Option.DAY_HEADER_FORMAT`, `Option.DAY_MIN_WIDTH`, `Option.MAX_ENTRIES_PER_DAY`, `Option.DAY_MAX_EVENT_ROWS`, `Option.ENTRY_MAX_STACK`, `Option.NOW_INDICATOR`, `Option.WEEK_NUMBERS`, `Option.WEEK_TEXT`, `Option.SCROLL_TIME`, `Option.NEXT_DAY_THRESHOLD`, `Option.MULTI_MONTH_MAX_COLUMNS`
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
calendar.setMaxEntriesPerDay(3); // "+N more" link after 3
calendar.setMaxEntriesPerDayFitToCell(); // auto based on cell height
calendar.setMaxEntriesPerDayUnlimited(); // no limit

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
| BR-05 | `MAX_ENTRIES_PER_DAY` triggers "+N more" popover when exceeded |
| BR-06 | `MoreLinkClickedEvent` fires when user clicks "+N more" |
| BR-07 | `NOW_INDICATOR` only works in timegrid views |
| BR-08 | View-specific options can override these for particular views |
| BR-09 | The "+N more" popover inherits `--fc-page-bg-color`. **Known gap**: FullCalendar's popover does not implement keyboard focus trapping or Escape-to-close-and-return-focus. This is a FC limitation. |

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

---

## Tests

### Unit Tests
- [ ] `DisplayOptionsTest` — option validation
- [ ] `FullCalendarOptionsTest` — option setting/getting

### E2E Tests
- [ ] `display-options.spec.js` — display option rendering

---

## Related FullCalendar Docs

- [weekends](https://fullcalendar.io/docs/weekends)
- [slotDuration](https://fullcalendar.io/docs/slotDuration)
- [dayMaxEvents](https://fullcalendar.io/docs/dayMaxEvents)
- [nowIndicator](https://fullcalendar.io/docs/nowIndicator)
