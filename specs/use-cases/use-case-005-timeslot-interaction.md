# UC-005: Timeslot Click and Selection

**As a** Vaadin application developer, **I want to** react to timeslot clicks and range selections **so that** end users can create new entries by clicking or selecting time ranges.

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon
**Related Options:** `selectable`, `selectMirror`, `selectConstraint`, `selectOverlap`, `selectMinDistance`, `selectAllow`, `unselectAuto`, `unselectCancel`, `selectLongPressDelay`
**Related Events:** `TimeslotClickedEvent`, `TimeslotsSelectedEvent`, `TimeslotsUnselectEvent`

---

## User-Facing Behavior

- Clicking an empty timeslot fires `TimeslotClickedEvent` with the clicked date/time
- When `selectable = true`, the user can click and drag to select a time range
- A highlight (or mirror) appears during selection
- On mouse release, `TimeslotsSelectedEvent` fires with the selected start/end
- Clicking outside the selection deselects (if `unselectAuto = true`)

---

## Java API Usage

```java
// Enable selection
calendar.options().setSelectable(true);
calendar.options().setSelectMirror(true);

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
| BR-01 | `TimeslotClickedEvent` fires regardless of `selectable` setting |
| BR-02 | `TimeslotsSelectedEvent` requires `selectable = true` |
| BR-03 | `selectConstraint` restricts selection ranges (e.g., `ConstraintValue.BUSINESS_HOURS`) |
| BR-04 | `selectOverlap = false` prevents selecting ranges that overlap entries |
| BR-05 | `selectAllow` JS callback can programmatically accept/reject selections |
| BR-06 | In dayGrid views, selections span whole days; in timeGrid views, they span time ranges |
| BR-07 | `TimeslotClickedEvent.getDateTime()` returns a `LocalDateTime` in UTC. Convert to the user's timezone or to `Instant` before persisting. |

---

## Acceptance Criteria

- [ ] Clicking an empty timeslot fires `TimeslotClickedEvent` with correct date/time
- [ ] Dragging across timeslots with `selectable = true` creates a selection highlight
- [ ] `TimeslotsSelectedEvent` fires with correct start and end
- [ ] `selectMirror` shows a mirror entry during selection
- [ ] `selectConstraint = ConstraintValue.BUSINESS_HOURS` limits selection to business hours
- [ ] `selectOverlap = false` prevents selection over existing entries
- [ ] Clicking outside deselects when `unselectAuto = true`
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
