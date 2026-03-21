# UC-010: Recurring Entries

**As a** Vaadin application developer, **I want to** define recurring entries **so that** repeating entries (daily standups, weekly meetings) display on all applicable dates.

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon
**Related Options:** —
**Related Events:** —

---

## User-Facing Behavior

- Recurring entries appear on every matching date within the visible range
- Simple recurrence: entries repeat on specified days of the week
- RRule recurrence: complex patterns (bi-weekly, monthly-by-weekday, yearly, etc.)
- Individual occurrences cannot be edited separately (FC renders them as repeating instances)

---

## Java API Usage

```java
// Simple weekly recurrence
Entry standup = new Entry();
standup.setTitle("Daily Standup");
standup.setRecurringDaysOfWeek(Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));
standup.setRecurringStartTime(RecurringTime.of(9, 0));
standup.setRecurringEndTime(RecurringTime.of(9, 15));

// RRule: bi-weekly on Monday
Entry biWeekly = new Entry();
biWeekly.setTitle("Sprint Review");
biWeekly.setRRule(RRule.weekly()
    .interval(2)
    .byWeekday(DayOfWeek.MONDAY)
    .dtstart(LocalDate.of(2025, 1, 6))
    .until(LocalDate.of(2025, 12, 31)));

// RRule: last Friday of each month
Entry monthly = new Entry();
monthly.setTitle("Monthly Review");
monthly.setRRule(RRule.monthly().byWeekday("-1fr"));

// Exclusion dates
RRule rule = RRule.weekly().byWeekday(DayOfWeek.MONDAY);
rule.excludeDates(LocalDate.of(2025, 3, 17)); // skip this Monday

// Raw RFC 5545 string
Entry custom = new Entry();
custom.setRRule(RRule.ofRaw("FREQ=WEEKLY;BYDAY=MO,WE;INTERVAL=2"));
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | Simple recurrence and RRule are mutually exclusive per entry |
| BR-02 | Setting an RRule overrides `recurringDaysOfWeek` / `recurringStartTime` / `recurringEndTime` |
| BR-03 | `excludeDates` removes specific occurrences from the recurrence pattern |
| BR-04 | `recurringDuration` sets duration for multi-day recurring all-day entries (ISO 8601, e.g., `"P2D"`) |
| BR-05 | Recurring entries expand client-side — the server sends the rule, not individual instances |
| BR-06 | Dragging individual occurrences of a recurring entry is not supported — the entire rule would need to be changed |

---

## Acceptance Criteria

- [ ] Simple weekly recurrence shows on correct days
- [ ] RRule entries appear on all matching dates
- [ ] Excluded dates are skipped
- [ ] Bi-weekly interval works correctly
- [ ] Monthly-by-weekday (e.g., last Friday) renders correctly
- [ ] Raw RFC 5545 strings are supported
- [ ] `recurringDuration` creates multi-day recurring all-day entries

---

## Tests

### Unit Tests
- [ ] `RecurringTimeTest` — RecurringTime conversion
- [ ] `EntryTest` — RRule serialization

### E2E Tests
- [ ] `entry-model.spec.js` — recurring entry display

---

## Related FullCalendar Docs

- [Recurring Events](https://fullcalendar.io/docs/recurring-events)
- [RRule Plugin](https://fullcalendar.io/docs/rrule-plugin)
