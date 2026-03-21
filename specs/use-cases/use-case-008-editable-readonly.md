# UC-008: Editable / Read-Only State

**As a** Vaadin application developer, **I want to** control whether the calendar is editable or read-only **so that** I can prevent modifications in certain contexts (e.g., view-only dashboards).

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon
**Related Options:** `Option.EDITABLE`, `Option.ENTRY_START_EDITABLE`, `Option.ENTRY_DURATION_EDITABLE`, `Option.SELECTABLE`
**Related Events:** —

---

## User-Facing Behavior

- **Default state**: Calendar is read-only (`EDITABLE = false`). Entries cannot be dragged or resized.
- When `EDITABLE = true`: entries can be dragged and resized (unless individual entries override).
- `ENTRY_START_EDITABLE` and `ENTRY_DURATION_EDITABLE` offer fine-grained control:
  - `ENTRY_START_EDITABLE = false`: entries cannot be moved but can be resized
  - `ENTRY_DURATION_EDITABLE = false`: entries cannot be resized but can be moved
- Per-entry `editable` overrides global setting.
- `SELECTABLE` controls time-range selection independently of entry editing.
- Cursor changes (move cursor on hover) are automatically shown/hidden based on editable state.

---

## Java API Usage

```java
// Full editing
calendar.setOption(Option.EDITABLE, true);
calendar.setOption(Option.SELECTABLE, true);

// Move only (no resize)
calendar.setOption(Option.EDITABLE, true);
calendar.setOption(Option.ENTRY_DURATION_EDITABLE, false);

// Resize only (no move)
calendar.setOption(Option.EDITABLE, true);
calendar.setOption(Option.ENTRY_START_EDITABLE, false);

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
| BR-01 | `EDITABLE = false` (default) disables all DnD and resize globally |
| BR-02 | `ENTRY_START_EDITABLE` and `ENTRY_DURATION_EDITABLE` only take effect when `EDITABLE = true` |
| BR-03 | Per-entry `editable` overrides all global options for that entry. Per-entry `startEditable` and `durationEditable` provide finer control. |
| BR-04 | `SELECTABLE` is independent of `EDITABLE` — a read-only calendar can still allow selection |
| BR-05 | Click events (`EntryClickedEvent`, `TimeslotClickedEvent`) always fire regardless of editable state |

---

## Acceptance Criteria

- [ ] Default state: entries are not draggable or resizable
- [ ] `EDITABLE = true`: entries are draggable and resizable
- [ ] `EDITABLE = true` + `ENTRY_DURATION_EDITABLE = false`: drag works, resize is disabled
- [ ] `EDITABLE = true` + `ENTRY_START_EDITABLE = false`: resize works, drag is disabled
- [ ] Per-entry `editable = false` overrides global `EDITABLE = true`
- [ ] Click events fire in both editable and read-only states
- [ ] `SELECTABLE` works independently of `EDITABLE`
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
