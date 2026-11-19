# UC-007: Calendar Navigation

**As a** Vaadin application developer, **I want to** navigate the calendar to different dates **so that** end users can browse past and future schedules.

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon
**Related Options:** `validRange`, `navLinks`, `dateIncrement`, `dateAlignment`
**Related Events:** `DatesRenderedEvent`, `DayNumberClickedEvent`, `WeekNumberClickedEvent`

---

## User-Facing Behavior

- Prev/Next buttons move forward/backward by one view interval
- "Today" button jumps to today's date
- Day and week numbers can be clickable links (`navLinks = true`)
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
calendar.options().setNavLinks(true);

// Restrict navigable range
calendar.options().setValidRange(new DateRange(LocalDate.of(2025, 1, 1), LocalDate.of(2026, 1, 1)));

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
| BR-01 | `validRange` prevents navigating outside the specified date range |
| BR-02 | `DatesRenderedEvent` fires after every navigation |
| BR-03 | `navLinks` makes day/week numbers clickable; fires `DayNumberClickedEvent` / `WeekNumberClickedEvent` |
| BR-04 | `dateIncrement` controls how far prev/next advance |

---

## Acceptance Criteria

- [ ] `next()` and `previous()` navigate by one interval
- [ ] `today()` jumps to current date
- [ ] `gotoDate()` navigates to specific date
- [ ] `validRange` prevents navigation outside bounds
- [ ] `navLinks` makes day numbers clickable
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
