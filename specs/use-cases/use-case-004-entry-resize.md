# UC-004: Entry Resize

**As a** Vaadin application developer, **I want to** let users resize entries **so that** they can adjust the duration of calendar items interactively.

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon
**Related Options:** `Option.EDITABLE`, `Option.ENTRY_DURATION_EDITABLE`, `Option.ENTRY_RESIZABLE_FROM_START`, `Option.ENTRY_ALLOW`, `Option.ENTRY_OVERLAP`, `Option.ENTRY_CONSTRAINT`
**Related Events:** `EntryResizedEvent`, `EntryResizeStartEvent`, `EntryResizeStopEvent`

---

## User-Facing Behavior

- When editable, entries in timegrid views have a resize handle at the bottom edge (8px wide dot; Vaadin theme sets `--fc-event-resizer-thickness: 8px`). Note: 8px does not meet the 44px WCAG touch target recommendation — on touch devices, long press delay compensates partially.
- Dragging the handle changes the entry's end time
- With `ENTRY_RESIZABLE_FROM_START = true`, a handle appears at the top edge too
- The server receives an `EntryResizedEvent` with the new start/end and delta
- Constraint and overlap rules apply during resize (same as DnD)

---

## Java API Usage

```java
calendar.setOption(Option.EDITABLE, true);

// Allow resize from start edge
calendar.setOption(Option.ENTRY_RESIZABLE_FROM_START, true);

// Handle resize
calendar.addEntryResizedListener(event -> {
    Entry entry = event.getEntry();
    Delta delta = event.getDelta();
    // persist changes...
});

// Resize start/stop (UI feedback)
calendar.addEntryResizeStartListener(event -> { /* resize started */ });
calendar.addEntryResizeStopListener(event -> { /* resize ended */ });
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | Requires `EDITABLE = true` (or per-entry `editable = true`) |
| BR-02 | `ENTRY_DURATION_EDITABLE = false` disables resize while keeping drag enabled |
| BR-03 | `ENTRY_RESIZABLE_FROM_START` adds top-edge handle (default: `false`) |
| BR-04 | Constraint and overlap rules apply during resize |
| BR-05 | Entry start/end are updated in the Java model when the event fires |

---

## Acceptance Criteria

- [ ] Entry in timegrid view shows resize handle at bottom edge when editable
- [ ] Dragging resize handle changes entry end time
- [ ] `ENTRY_RESIZABLE_FROM_START` adds top-edge resize handle
- [ ] `EntryResizedEvent` fires with correct new start, end, and delta
- [ ] `ENTRY_DURATION_EDITABLE = false` disables resize even with `EDITABLE = true`
- [ ] Constraint and overlap rules are respected during resize
- [ ] `EntryResizeStartEvent` fires when resize begins
- [ ] `EntryResizeStopEvent` fires when resize ends (regardless of valid resize)

---

## Tests

### Unit Tests
- [ ] `DeltaTest` — delta calculation

### E2E Tests
- [ ] `calendar-interactions.spec.js` — resize behavior

---

## Related FullCalendar Docs

- [eventResize](https://fullcalendar.io/docs/eventResize)
- [eventDurationEditable](https://fullcalendar.io/docs/eventDurationEditable)
- [eventResizableFromStart](https://fullcalendar.io/docs/eventResizableFromStart)
