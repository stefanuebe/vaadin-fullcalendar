# UC-007: Calendar Navigation

**As a** Vaadin application developer, **I want to** navigate the calendar to different dates **so that** end users can browse past and future schedules.

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon
**Related Options:** `Option.VALID_RANGE`, `Option.NAV_LINKS`, `Option.DATE_INCREMENT`, `Option.DATE_ALIGNMENT`
**Related Events:** `DatesRenderedEvent`, `DayNumberClickedEvent`, `WeekNumberClickedEvent`

---

## User-Facing Behavior

- Prev/Next buttons move forward/backward by one view interval
- "Today" button jumps to today's date
- Day and week numbers can be clickable links (`NAV_LINKS = true`)
- Navigation can be restricted to a valid date range

---

## Java API Usage

```java
// Programmatic navigation
calendar.next();
calendar.previous();
calendar.today();
calendar.gotoDate(LocalDate.of(2025, 6, 15));

// Enable nav links
calendar.setOption(Option.NAV_LINKS, true);

// Restrict navigable range
calendar.setValidRange(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));

// Listen to navigation
calendar.addDatesRenderedListener(event -> {
    // update external label, refresh data, etc.
});

calendar.addDayNumberClickedListener(event -> {
    LocalDate clicked = event.getDate();
});
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | `VALID_RANGE` prevents navigating outside the specified date range |
| BR-02 | `DatesRenderedEvent` fires after every navigation |
| BR-03 | `NAV_LINKS` makes day/week numbers clickable; fires `DayNumberClickedEvent` / `WeekNumberClickedEvent` |
| BR-04 | `DATE_INCREMENT` controls how far prev/next advance |

---

## Acceptance Criteria

- [ ] `next()` and `previous()` navigate by one interval
- [ ] `today()` jumps to current date
- [ ] `gotoDate()` navigates to specific date
- [ ] `VALID_RANGE` prevents navigation outside bounds
- [ ] `NAV_LINKS` makes day numbers clickable
- [ ] `DatesRenderedEvent` fires after navigation

---

## Tests

### Unit Tests
- [ ] No dedicated unit tests — navigation is covered by E2E tests

### E2E Tests
- [ ] `calendar-navigation.spec.js` — navigation controls

---

## Related FullCalendar Docs

- [Navigation](https://fullcalendar.io/docs/date-navigation)
- [validRange](https://fullcalendar.io/docs/validRange)
- [navLinks](https://fullcalendar.io/docs/navLinks)
