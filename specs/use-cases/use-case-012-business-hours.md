# UC-012: Business Hours

**As a** Vaadin application developer, **I want to** define business hours **so that** non-working times are visually distinct and can serve as constraints for entry placement.

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon
**Related Options:** `Option.BUSINESS_HOURS`, `Option.ENTRY_CONSTRAINT`, `Option.SELECT_CONSTRAINT`
**Related Events:** —

---

## User-Facing Behavior

- Business hours are highlighted in timegrid views (non-business time is shaded)
- Business hours can vary by day of the week
- Multiple time ranges per week are supported
- Business hours can serve as constraints: entries can only be placed within business hours
- Resources can have their own business hours (scheduler)

---

## Java API Usage

```java
// Default business hours (Mo-Fr 9-17)
calendar.setOption(Option.BUSINESS_HOURS, true);

// Custom business hours
calendar.setOption(Option.BUSINESS_HOURS,
    BusinessHours.businessWeek().start(8).end(18));

// Multiple ranges
calendar.setOption(Option.BUSINESS_HOURS, new BusinessHours[]{
    BusinessHours.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        .start(8).end(12),
    BusinessHours.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)
        .start(10).end(16)
});

// Use as constraint
calendar.setOption(Option.ENTRY_CONSTRAINT, "businessHours");
calendar.setOption(Option.SELECT_CONSTRAINT, "businessHours");
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | `true` activates default 9am-5pm Mon-Fri |
| BR-02 | Multiple `BusinessHours` arrays allow different ranges per day |
| BR-03 | `"businessHours"` as constraint value restricts DnD and selection to business hours |
| BR-04 | Resources can have per-resource business hours (scheduler) |

---

## Acceptance Criteria

- [ ] Non-business times are visually shaded in timegrid views
- [ ] Custom hours display correctly
- [ ] Multiple time ranges per week render correctly
- [ ] `ENTRY_CONSTRAINT = "businessHours"` prevents DnD outside business hours
- [ ] `SELECT_CONSTRAINT = "businessHours"` prevents selection outside business hours

---

## Tests

### Unit Tests
- [ ] `BusinessHoursTest` — construction, JSON serialization

### E2E Tests
- [ ] `display-options.spec.js` — business hours display

---

## Related FullCalendar Docs

- [businessHours](https://fullcalendar.io/docs/businessHours)
