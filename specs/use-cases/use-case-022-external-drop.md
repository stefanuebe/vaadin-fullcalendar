# UC-022: External Drop and Inter-Calendar Drag

**As a** Vaadin application developer, **I want to** accept drops of external HTML elements onto the calendar and support dragging entries between calendar instances **so that** users can create entries by dragging from external lists or move entries between calendars.

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon
**Related Options:** `droppable`, `dropAccept`
**Related Events:** `DropEvent`, `EntryReceiveEvent`, `EntryLeaveEvent`

---

## User-Facing Behavior

### External Drop
- When `droppable = true`, external HTML elements (e.g., items from a sidebar list) can be dropped onto the calendar
- External elements are registered with `addDraggable(Draggable)`, optionally with entry data (an `Entry` template or an entry data callback). With `withItemSelector`, the children of a container are draggable
- On drop, `DropEvent` fires with the drop date and time, the `Draggable` and a created entry
- A drop with entry data also fires `EntryReceiveEvent`. A `Draggable` without entry data fires only `DropEvent`
- The developer is responsible for adding the entry to the entry provider

### Inter-Calendar Drag (Entry Receive/Leave)
- When an entry from one calendar instance is dragged to another, `EntryReceiveEvent` fires on the receiving calendar
- `EntryLeaveEvent` fires on the source calendar when an entry leaves
- Requires `editable = true` on the source calendar and `droppable = true` on the target

---

## Java API Usage

```java
// Enable external drops
calendar.options().setDroppable(true);

// Filter which elements can be dropped (CSS selector or JS callback)
calendar.options().setDropAccept(".draggable-item");

// Handle external element drop
calendar.addDropListener(event -> {
    LocalDateTime dateTime = event.getDateTime();  // UTC, the start of the day for an all-day drop
    boolean allDay = event.isAllDay();
    Optional<Draggable> draggable = event.getDraggable(); // the registered Draggable, also for a child of a container
    Entry created = event.getCreatedEntry(); // built from the entry data, or starting at the drop time
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
| BR-01 | `droppable = false` (default) disables all external drops |
| BR-02 | `dropAccept` filters which external elements can be dropped (CSS selector or JS callback) |
| BR-03 | `DropEvent` provides the drop time (`getDateTime()` in UTC, `getDateTimeWithOffset()` in the calendar's time zone, the start of the day for an all-day drop) and a created entry that is not added to the provider. Without entry data, the created entry starts at the drop time and lasts one hour, or one day for an all-day drop |
| BR-03a | `EntryReceiveEvent` fires only for a drop with entry data. A `Draggable` without entry data fires only `DropEvent` |
| BR-04 | `EntryReceiveEvent` provides the entry data, including the extended props of the dragged entry data, but does NOT add it to the provider automatically |
| BR-05 | Inter-calendar drag requires `editable = true` on source and `droppable = true` on target |
| BR-06 | `EntryLeaveEvent` fires on the source calendar when an entry is dragged out |

---

## Acceptance Criteria

- [ ] External HTML elements can be dropped when `droppable = true`
- [x] `DropEvent` fires with the drop time, the time of the slot for a timed drop
- [x] A `Draggable` without entry data fires only `DropEvent`
- [x] A child of a container `Draggable` resolves to its `Draggable`, and the created entry carries the callback data
- [ ] `dropAccept` filters droppable elements
- [ ] `EntryReceiveEvent` fires when entry from another calendar is received
- [ ] `EntryLeaveEvent` fires when entry leaves this calendar
- [ ] External drops are rejected when `droppable = false`

---

## Tests

### Unit Tests
- [x] `DropEventTest` — the created entry of a timed and an all-day drop without entry data

### E2E Tests
- [x] `external-drop.spec.js` — a registered `Draggable` dropped onto the calendar fires `DropEvent` with component and entry data, and both the dropped and the received entry carry the extended props of the entry data
- [x] `external-drop-cases.spec.js` — a `Draggable` without entry data fires only `DropEvent` at the time of the slot, and a child of a container `Draggable` resolves to it with the callback data

---

## Related FullCalendar Docs

- [droppable](https://fullcalendar.io/docs/droppable)
- [drop](https://fullcalendar.io/docs/drop)
- [eventReceive](https://fullcalendar.io/docs/eventReceive)
- [eventLeave](https://fullcalendar.io/docs/eventLeave)
- [External Dragging](https://fullcalendar.io/docs/external-dragging)
