# UC-022: External Drop and Inter-Calendar Drag

**As a** Vaadin application developer, **I want to** accept drops of external HTML elements onto the calendar and support dragging entries between calendar instances **so that** users can create entries by dragging from external lists or move entries between calendars.

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon
**Related Options:** `Option.DROPPABLE`, `Option.DROP_ACCEPT`
**Related Events:** `DropEvent`, `EntryReceiveEvent`, `EntryLeaveEvent`

---

## User-Facing Behavior

### External Drop
- When `DROPPABLE = true`, external HTML elements (e.g., items from a sidebar list) can be dropped onto the calendar
- On drop, `DropEvent` fires with the drop date and optional data from the dragged element
- The developer is responsible for creating an `Entry` from the drop data

### Inter-Calendar Drag (Entry Receive/Leave)
- When an entry from one calendar instance is dragged to another, `EntryReceiveEvent` fires on the receiving calendar
- `EntryLeaveEvent` fires on the source calendar when an entry leaves
- Requires `EDITABLE = true` on the source calendar and `DROPPABLE = true` on the target

---

## Java API Usage

```java
// Enable external drops
calendar.setOption(Option.DROPPABLE, true);

// Filter which elements can be dropped (CSS selector or JS callback)
calendar.setOption(Option.DROP_ACCEPT, ".draggable-item");

// Handle external element drop
calendar.addDropListener(event -> {
    LocalDate date = event.getDate();  // only date, not time
    boolean allDay = event.isAllDay();
    String data = event.getDraggedElData(); // optional data from the dragged element
    // create entry from drop data...
});

// Handle entry received from another calendar
calendar.addEntryReceiveListener(event -> {
    Entry receivedEntry = event.getEntry();
    // the entry is NOT automatically added to this calendar's provider
});

// Handle entry leaving this calendar (dragged to another)
calendar.addEntryLeaveListener(event -> {
    Entry leavingEntry = event.getEntry();
    // optionally remove from this calendar's provider
});
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | `DROPPABLE = false` (default) disables all external drops |
| BR-02 | `DROP_ACCEPT` filters which external elements can be dropped (CSS selector or JS callback) |
| BR-03 | `DropEvent` provides the drop date (`LocalDate`) but NOT a time or an Entry — the developer creates one. Since the return type is `LocalDate` (no time component), timezone normalization does not apply. |
| BR-04 | `EntryReceiveEvent` provides the entry data but does NOT add it to the provider automatically |
| BR-05 | Inter-calendar drag requires `EDITABLE = true` on source and `DROPPABLE = true` on target |
| BR-06 | `EntryLeaveEvent` fires on the source calendar when an entry is dragged out |

---

## Acceptance Criteria

- [ ] External HTML elements can be dropped when `DROPPABLE = true`
- [ ] `DropEvent` fires with correct date (LocalDate only, no time)
- [ ] `DROP_ACCEPT` filters droppable elements
- [ ] `EntryReceiveEvent` fires when entry from another calendar is received
- [ ] `EntryLeaveEvent` fires when entry leaves this calendar
- [ ] External drops are rejected when `DROPPABLE = false`

---

## Tests

### Unit Tests
- [ ] No dedicated unit tests — external DnD requires browser interaction

### E2E Tests
- [ ] No dedicated E2E tests for external drop — coverage gap. Requires real DOM drag simulation.

---

## Related FullCalendar Docs

- [droppable](https://fullcalendar.io/docs/droppable)
- [drop](https://fullcalendar.io/docs/drop)
- [eventReceive](https://fullcalendar.io/docs/eventReceive)
- [eventLeave](https://fullcalendar.io/docs/eventLeave)
- [External Dragging](https://fullcalendar.io/docs/external-dragging)
