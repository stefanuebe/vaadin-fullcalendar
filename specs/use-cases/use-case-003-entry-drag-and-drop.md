# UC-003: Entry Drag and Drop

**As a** Vaadin application developer, **I want to** let users drag entries to new dates/times **so that** they can reschedule items interactively.

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon
**Related Options:** `editable`, `entryStartEditable`, `snapDuration`, `dragScroll`, `allDayMaintainDuration`, `entryAllow`, `entryOverlap`, `entryConstraint`, `entryDragMinDistance`, `dragRevertDuration`, `entryLongPressDelay`, `longPressDelay`
**Related Events:** `EntryDroppedEvent`, `EntryDragStartEvent`, `EntryDragStopEvent`

---

## User-Facing Behavior

- When `editable` is enabled, the user can click and drag an entry to a new time slot or day
- A clone of the entry, inheriting its color, follows the cursor during drag. It carries `fc-event-dragging` (see UC-025 BR-04).
- When dropped, the entry snaps to the nearest slot (configurable via `snapDuration`)
- The server receives an `EntryDroppedEvent` with the new start/end and the delta
- If the drop is invalid (constraint/overlap violation), the entry reverts to its original position
- On touch devices, a long press initiates the drag (configurable delay)

---

## Java API Usage

```java
// Enable globally
calendar.options().setEditable(true);

// Disable for specific entry
entry.setEditable(false);

// Constrain to business hours
calendar.options().setEntryConstraint(ConstraintValue.BUSINESS_HOURS);

// Handle drop
calendar.addEntryDroppedListener(event -> {
    Entry entry = event.getEntry();
    // entry.getStart() and entry.getEnd() are already updated
    Delta delta = event.getDelta();
    // persist changes...
});

// Drag start/stop (UI feedback, e.g., show loading indicator)
calendar.addEntryDragStartListener(event -> {
    // drag started — show visual feedback
});
calendar.addEntryDragStopListener(event -> {
    // drag ended (fires regardless of valid drop)
});

// Custom snap duration
calendar.options().setSnapDuration(Duration.ofMinutes(15));
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | `editable = false` (default) disables all DnD globally |
| BR-02 | `Entry.setEditable(false)` overrides global editable for that entry |
| BR-03 | `entryStartEditable = false` prevents time changes but allows other edits |
| BR-04 | `entryConstraint = ConstraintValue.BUSINESS_HOURS` restricts drops to business hour slots |
| BR-05 | `entryOverlap = false` prevents dropping onto occupied slots |
| BR-06 | `entryAllow` JS callback can programmatically accept/reject each drop |
| BR-07 | Entry start/end are already updated in the Java model when the event fires |
| BR-08 | Dragging from all-day to timed slot (or vice versa) changes the entry's `allDay` state |

---

## Acceptance Criteria

- [ ] Entry can be dragged to a new time slot when `editable = true`
- [ ] Entry cannot be dragged when `editable = false`
- [ ] Per-entry `editable = false` prevents drag even with calendar `editable = true`
- [ ] `EntryDroppedEvent` fires with correct new start, end, and delta
- [ ] Constraint `ConstraintValue.BUSINESS_HOURS` prevents drops outside business hours
- [ ] `entryOverlap = false` prevents drops on occupied slots
- [ ] Mirror (semi-transparent clone) follows cursor during drag *(manual verification)*
- [ ] `snapDuration` controls snap granularity
- [ ] Touch devices: long press initiates drag *(manual verification — not automatable in headless Playwright)*
- [ ] Dragging between all-day row and time grid works correctly
- [ ] `EntryDragStartEvent` fires when drag begins
- [ ] `EntryDragStopEvent` fires when drag ends (regardless of valid drop)

---

## Tests

### Unit Tests
- [ ] `DeltaTest` — delta calculation
- [ ] `EntryTest` — editable property

### E2E Tests
- [ ] `calendar-interactions.spec.js` — drag-and-drop behavior

---

## Related FullCalendar Docs

- [editable](https://fullcalendar.io/docs/editable)
- [eventDrop](https://fullcalendar.io/docs/eventDrop)
- [eventConstraint](https://fullcalendar.io/docs/eventConstraint)
- [eventOverlap](https://fullcalendar.io/docs/eventOverlap)
- [snapDuration](https://fullcalendar.io/docs/snapDuration)
