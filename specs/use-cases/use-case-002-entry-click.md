# UC-002: Entry Click

**As a** Vaadin application developer, **I want to** react to entry clicks **so that** end users can select or inspect calendar entries.

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon
**Related Options:** `Option.ENTRY_INTERACTIVE`
**Related Events:** `EntryClickedEvent`

---

## User-Facing Behavior

- When the user clicks an entry, the browser fires a click event
- The server receives an `EntryClickedEvent` containing the clicked `Entry` and mouse event details
- When `Option.ENTRY_INTERACTIVE` is `true`, entries are keyboard-focusable (Tab) and activatable (Enter/Space)
- Entries with a `url` still fire `EntryClickedEvent` before navigating

---

## Java API Usage

```java
calendar.addEntryClickedListener(event -> {
    Entry entry = event.getEntry();
    Notification.show("Clicked: " + entry.getTitle());
});

// Enable keyboard activation
calendar.setOption(Option.ENTRY_INTERACTIVE, true);
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | `EntryClickedEvent` fires for all visible entries regardless of `editable` state |
| BR-02 | Entries with `url` fire the event AND trigger navigation |
| BR-03 | `ENTRY_INTERACTIVE = true` adds `tabindex="0"` to all entries for keyboard access |
| BR-04 | Per-entry `interactive` overrides global setting (`null` = inherit) |

---

## Acceptance Criteria

- [ ] Clicking an entry fires `EntryClickedEvent` with correct entry data
- [ ] The event contains the clicked entry's ID, title, start, end
- [ ] Entries with `url` still fire the click event
- [ ] With `ENTRY_INTERACTIVE = true`, entries are focusable via Tab key
- [ ] Focused entries can be activated with Enter or Space

---

## Tests

### Unit Tests
- [ ] `EntryTest` — event data serialization

### E2E Tests
- [ ] `calendar-entry.spec.js` — click interaction
- [ ] `accessibility.spec.js` — keyboard focus and activation

---

## Related FullCalendar Docs

- [eventClick](https://fullcalendar.io/docs/eventClick)
- [eventInteractive](https://fullcalendar.io/docs/eventInteractive)
