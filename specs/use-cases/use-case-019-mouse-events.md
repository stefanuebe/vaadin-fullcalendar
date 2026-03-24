# UC-019: Mouse Enter/Leave Events

**As a** Vaadin application developer, **I want to** react to mouse hover on entries **so that** I can show tooltips or highlight related information.

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon
**Related Options:** —
**Related Events:** `EntryMouseEnterEvent`, `EntryMouseLeaveEvent`

---

## User-Facing Behavior

- When the mouse enters an entry element, `EntryMouseEnterEvent` fires
- When the mouse leaves, `EntryMouseLeaveEvent` fires
- Useful for showing tooltips, detail panels, or highlighting

### Tooltip Pattern

The native HTML `title` attribute (set via `ENTRY_DID_MOUNT`) provides basic tooltips but is **not accessible** on touch or keyboard-only devices. For accessible tooltips, use a Vaadin `Tooltip` component or a custom overlay positioned relative to the entry element. See UC-016 for the `ENTRY_DID_MOUNT` callback pattern.

---

## Java API Usage

```java
calendar.addEntryMouseEnterListener(event -> {
    Entry entry = event.getEntry();
    // show tooltip or detail panel
});

calendar.addEntryMouseLeaveListener(event -> {
    // hide tooltip
});
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | Events fire for all visible entries regardless of editable state |
| BR-02 | Events contain the entry reference |

---

## Acceptance Criteria

- [ ] `EntryMouseEnterEvent` fires when hovering an entry
- [ ] `EntryMouseLeaveEvent` fires when leaving an entry
- [ ] Events contain correct entry data

---

## Tests

### Unit Tests
- [ ] No dedicated unit tests — hover events are covered by E2E tests

### E2E Tests
- [ ] `calendar-entry.spec.js` — hover events

---

## Related FullCalendar Docs

- [eventMouseEnter](https://fullcalendar.io/docs/eventMouseEnter)
- [eventMouseLeave](https://fullcalendar.io/docs/eventMouseLeave)
