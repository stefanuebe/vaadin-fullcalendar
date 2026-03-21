# UC-006: Calendar Views

**As a** Vaadin application developer, **I want to** switch between different calendar views **so that** end users can see their schedule in the most appropriate format.

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon (+ addon-scheduler for scheduler views)
**Related Options:** `Option.HEADER_TOOLBAR`, `Option.FOOTER_TOOLBAR`
**Related Events:** `DatesRenderedEvent`

---

## User-Facing Behavior

### Standard Views (CalendarViewImpl)

| View | Client-Side Name | Description |
|------|-------------------|-------------|
| `DAY_GRID_MONTH` | `dayGridMonth` | Month grid (default view) |
| `DAY_GRID_WEEK` | `dayGridWeek` | Week as day grid |
| `DAY_GRID_DAY` | `dayGridDay` | Single day as grid |
| `DAY_GRID_YEAR` | `dayGridYear` | Year as day grid |
| `TIME_GRID_WEEK` | `timeGridWeek` | Week with time slots |
| `TIME_GRID_DAY` | `timeGridDay` | Single day with time slots |
| `LIST_WEEK` | `listWeek` | Week as entry list |
| `LIST_DAY` | `listDay` | Day as entry list |
| `LIST_MONTH` | `listMonth` | Month as entry list |
| `LIST_YEAR` | `listYear` | Year as entry list |
| `MULTI_MONTH` | `multiMonthYear` | Full year showing all months |

### Scheduler Views (SchedulerView)

| View | Client-Side Name | Description |
|------|-------------------|-------------|
| `TIMELINE_DAY/WEEK/MONTH/YEAR` | `timelineDay`, etc. | Horizontal timeline |
| `RESOURCE_TIMELINE_DAY/WEEK/MONTH/YEAR` | `resourceTimelineDay`, etc. | Timeline with resources on Y axis |
| `RESOURCE_TIME_GRID_DAY/WEEK` | `resourceTimeGridDay`, etc. | Vertical time grid with resources as columns |

### Custom Views

`CustomCalendarView` allows defining arbitrary FC views with custom duration.

---

## Java API Usage

```java
// Change view programmatically
calendar.changeView(CalendarViewImpl.TIME_GRID_WEEK);

// Configure toolbar with view buttons
calendar.setOption(Option.HEADER_TOOLBAR,
    Map.of("left", "prev,next,today",
           "center", "title",
           "right", "dayGridMonth,timeGridWeek,timeGridDay,listWeek"));

// Listen for view changes
calendar.addDatesRenderedListener(event -> {
    // Interval (e.g., the month): event.getIntervalStart(), event.getIntervalEnd()
    // All visible dates (may extend beyond interval in month view):
    //   event.getStart(), event.getEnd()
});

// Custom view: implement the CustomCalendarView interface
CustomCalendarView threeDayView = new CustomCalendarView() {
    @Override
    public String getClientSideValue() { return "threeDay"; }
    @Override
    public ObjectNode getViewSettings() {
        ObjectNode s = JsonFactory.createObject();
        s.put("type", "timeGrid");
        ObjectNode d = JsonFactory.createObject();
        d.put("days", 3);
        s.set("duration", d);
        return s;
    }
};
calendar.changeView(threeDayView);
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | Scheduler views require `FullCalendarScheduler` (not plain `FullCalendar`) |
| BR-02 | `DatesRenderedEvent` fires whenever the visible date range changes (view switch or navigation) |
| BR-03 | Toolbar button names must match FC view names exactly |
| BR-04 | View-specific options can override global options for particular views |

---

## Acceptance Criteria

- [ ] All standard views render correctly
- [ ] View switching via toolbar buttons works
- [ ] `changeView()` switches view programmatically
- [ ] `DatesRenderedEvent` fires on view change with correct date range
- [ ] Custom views with arbitrary durations work
- [ ] Scheduler views show resource columns/rows
- [ ] `setViewSpecificOption()` overrides options for specific views

---

## Tests

### Unit Tests
- [ ] No dedicated unit tests — views are covered by E2E tests

### E2E Tests
- [ ] `calendar-views.spec.js` — view rendering and switching

---

## Related FullCalendar Docs

- [Views](https://fullcalendar.io/docs/view-api)
- [headerToolbar](https://fullcalendar.io/docs/headerToolbar)
