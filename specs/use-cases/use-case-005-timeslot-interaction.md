# UC-005: Timeslot Click and Selection

**As a** Vaadin application developer, **I want to** react to timeslot clicks and range selections **so that** end users can create new entries by clicking or selecting time ranges.

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon
**Related Options:** `Option.SELECTABLE`, `Option.SELECT_MIRROR`, `Option.SELECT_CONSTRAINT`, `Option.SELECT_OVERLAP`, `Option.SELECT_MIN_DISTANCE`, `Option.SELECT_ALLOW`, `Option.UNSELECT_AUTO`, `Option.UNSELECT_CANCEL`, `Option.SELECT_LONG_PRESS_DELAY`
**Related Events:** `TimeslotClickedEvent`, `TimeslotsSelectedEvent`, `TimeslotsUnselectEvent`

---

## User-Facing Behavior

- Clicking an empty timeslot fires `TimeslotClickedEvent` with the clicked date/time
- When `SELECTABLE = true`, the user can click and drag to select a time range
- A highlight (or mirror) appears during selection
- On mouse release, `TimeslotsSelectedEvent` fires with the selected start/end
- Clicking outside the selection deselects (if `UNSELECT_AUTO = true`)

---

## Java API Usage

```java
// Enable selection
calendar.setOption(Option.SELECTABLE, true);
calendar.setOption(Option.SELECT_MIRROR, true);

// Timeslot click
calendar.addTimeslotClickedListener(event -> {
    LocalDateTime clickedTime = event.getDateTime();
    // open "create entry" dialog...
});

// Range selection
calendar.addTimeslotsSelectedListener(event -> {
    LocalDateTime start = event.getStart();
    LocalDateTime end = event.getEnd();
    boolean allDay = event.isAllDay();
    // create entry for selected range...
});

// Selection cleared
calendar.addTimeslotsUnselectListener(event -> {
    // selection was cleared (click outside, or programmatic unselect)
});
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | `TimeslotClickedEvent` fires regardless of `SELECTABLE` setting |
| BR-02 | `TimeslotsSelectedEvent` requires `SELECTABLE = true` |
| BR-03 | `SELECT_CONSTRAINT` restricts selection ranges (e.g., `"businessHours"`) |
| BR-04 | `SELECT_OVERLAP = false` prevents selecting ranges that overlap entries |
| BR-05 | `SELECT_ALLOW` JS callback can programmatically accept/reject selections |
| BR-06 | In dayGrid views, selections span whole days; in timeGrid views, they span time ranges |
| BR-07 | `TimeslotClickedEvent.getDateTime()` returns a `LocalDateTime` in UTC. Convert to the user's timezone or to `Instant` before persisting. |

---

## Acceptance Criteria

- [ ] Clicking an empty timeslot fires `TimeslotClickedEvent` with correct date/time
- [ ] Dragging across timeslots with `SELECTABLE = true` creates a selection highlight
- [ ] `TimeslotsSelectedEvent` fires with correct start and end
- [ ] `SELECT_MIRROR` shows a mirror entry during selection
- [ ] `SELECT_CONSTRAINT = "businessHours"` limits selection to business hours
- [ ] `SELECT_OVERLAP = false` prevents selection over existing entries
- [ ] Clicking outside deselects when `UNSELECT_AUTO = true`
- [ ] `TimeslotsUnselectEvent` fires when selection is cleared

---

## Tests

### Unit Tests
- [ ] `TimeslotsSelectedEventTest` — event data

### E2E Tests
- [ ] `calendar-interactions.spec.js` — click and selection behavior

---

## Related FullCalendar Docs

- [selectable](https://fullcalendar.io/docs/selectable)
- [select callback](https://fullcalendar.io/docs/select-callback)
- [dateClick](https://fullcalendar.io/docs/dateClick)
