# UC-008: Editable / Read-Only State

**As a** Vaadin application developer, **I want to** control whether the calendar is editable or read-only **so that** I can prevent modifications in certain contexts (e.g., view-only dashboards).

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon
**Related Options:** `editable`, `entryStartEditable`, `entryDurationEditable`, `selectable`
**Related Events:** —

---

## User-Facing Behavior

- **Default state**: Calendar is read-only (calendar `editable = false`). Entries cannot be dragged or resized.
- When calendar `editable = true`: entries can be dragged and resized (unless individual entries override).
- `entryStartEditable` and `entryDurationEditable` offer fine-grained control:
  - `entryStartEditable = false`: entries cannot be moved but can be resized
  - `entryDurationEditable = false`: entries cannot be resized but can be moved
- Per-entry `editable` overrides global setting.
- `selectable` controls time-range selection independently of entry editing.
- Cursor changes (move cursor on hover) are automatically shown/hidden based on editable state.

---

## Java API Usage

```java
// Full editing
calendar.options().setEditable(true);
calendar.options().setSelectable(true);

// Move only (no resize)
calendar.options().setEditable(true);
calendar.options().setEntryDurationEditable(false);

// Resize only (no move)
calendar.options().setEditable(true);
calendar.options().setEntryStartEditable(false);

// Individual entry override
entry.setEditable(false); // this entry is read-only even if global is editable

// Fine-grained per-entry overrides
entry.setStartEditable(false); // this entry cannot be moved
entry.setDurationEditable(false); // this entry cannot be resized
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | calendar `editable = false` (default) disables all DnD and resize globally |
| BR-02 | `entryStartEditable` and `entryDurationEditable` only take effect when calendar `editable = true` |
| BR-03 | Per-entry `editable` overrides all global options for that entry. Per-entry `startEditable` and `durationEditable` provide finer control. |
| BR-04 | `selectable` is independent of the calendar's `editable` — a read-only calendar can still allow selection |
| BR-05 | Click events (`EntryClickedEvent`, `TimeslotClickedEvent`) always fire regardless of editable state |

---

## Acceptance Criteria

- [ ] Default state: entries are not draggable or resizable
- [ ] calendar `editable = true`: entries are draggable and resizable
- [ ] calendar `editable = true` + `entryDurationEditable = false`: drag works, resize is disabled
- [ ] calendar `editable = true` + `entryStartEditable = false`: resize works, drag is disabled
- [ ] Per-entry `editable = false` overrides global calendar `editable = true`
- [ ] Click events fire in both editable and read-only states
- [ ] `selectable` works independently of the calendar's `editable`
- [ ] Cursor changes match editable state — move cursor on hover for editable entries *(manual verification)*

---

## Tests

### Unit Tests
- [ ] `FullCalendarOptionsTest` — option setting/getting

### E2E Tests
- [ ] `calendar-interactions.spec.js` — editable state behavior

---

## Related FullCalendar Docs

- [editable](https://fullcalendar.io/docs/editable)
- [eventStartEditable](https://fullcalendar.io/docs/eventStartEditable)
- [eventDurationEditable](https://fullcalendar.io/docs/eventDurationEditable)
