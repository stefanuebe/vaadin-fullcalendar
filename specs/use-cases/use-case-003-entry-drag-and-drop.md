# UC-003: Entry Drag and Drop

**As a** Vaadin application developer, **I want to** let users drag entries to new dates/times **so that** they can reschedule items interactively.

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon
**Related Options:** `Option.EDITABLE`, `Option.ENTRY_START_EDITABLE`, `Option.SNAP_DURATION`, `Option.DRAG_SCROLL`, `Option.ALL_DAY_MAINTAIN_DURATION`, `Option.ENTRY_ALLOW`, `Option.ENTRY_OVERLAP`, `Option.ENTRY_CONSTRAINT`, `Option.ENTRY_DRAG_MIN_DISTANCE`, `Option.DRAG_REVERT_DURATION`, `Option.FIXED_MIRROR_PARENT`, `Option.ENTRY_LONG_PRESS_DELAY`, `Option.LONG_PRESS_DELAY`
**Related Events:** `EntryDroppedEvent`, `EntryDragStartEvent`, `EntryDragStopEvent`

---

## User-Facing Behavior

- When `editable` is enabled, the user can click and drag an entry to a new time slot or day
- A "mirror" (semi-transparent clone inheriting the entry's color) follows the cursor during drag. The mirror uses FC's default opacity (`--fc-event-mirror-opacity`). The Vaadin theme does not override this.
- When dropped, the entry snaps to the nearest slot (configurable via `SNAP_DURATION`)
- The server receives an `EntryDroppedEvent` with the new start/end and the delta
- If the drop is invalid (constraint/overlap violation), the entry reverts to its original position
- On touch devices, a long press initiates the drag (configurable delay)

---

## Java API Usage

```java
// Enable globally
calendar.setOption(Option.EDITABLE, true);

// Disable for specific entry
entry.setEditable(false);

// Constrain to business hours
calendar.setOption(Option.ENTRY_CONSTRAINT, "businessHours");

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
calendar.setOption(Option.SNAP_DURATION, Duration.ofMinutes(15));
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | `Option.EDITABLE = false` (default) disables all DnD globally |
| BR-02 | `Entry.setEditable(false)` overrides global editable for that entry |
| BR-03 | `ENTRY_START_EDITABLE = false` prevents time changes but allows other edits |
| BR-04 | `ENTRY_CONSTRAINT = "businessHours"` restricts drops to business hour slots |
| BR-05 | `ENTRY_OVERLAP = false` prevents dropping onto occupied slots |
| BR-06 | `ENTRY_ALLOW` JS callback can programmatically accept/reject each drop |
| BR-07 | Entry start/end are already updated in the Java model when the event fires |
| BR-08 | Dragging from all-day to timed slot (or vice versa) changes the entry's `allDay` state |

---

## Acceptance Criteria

- [ ] Entry can be dragged to a new time slot when `EDITABLE = true`
- [ ] Entry cannot be dragged when `EDITABLE = false`
- [ ] Per-entry `editable = false` prevents drag even with global `EDITABLE = true`
- [ ] `EntryDroppedEvent` fires with correct new start, end, and delta
- [ ] Constraint `"businessHours"` prevents drops outside business hours
- [ ] `ENTRY_OVERLAP = false` prevents drops on occupied slots
- [ ] Mirror (semi-transparent clone) follows cursor during drag *(manual verification)*
- [ ] `SNAP_DURATION` controls snap granularity
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
