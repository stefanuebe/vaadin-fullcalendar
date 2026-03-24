# UC-001: Entry Display

**As a** Vaadin application developer, **I want to** display entries on the calendar **so that** end users can see scheduled items with their title, time, and color.

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon
**Related Options:** `Option.ENTRY_COLOR`, `Option.ENTRY_BACKGROUND_COLOR`, `Option.ENTRY_BORDER_COLOR`, `Option.ENTRY_TEXT_COLOR`, `Option.ENTRY_DISPLAY`, `Option.DISPLAY_ENTRY_TIME`, `Option.DISPLAY_EVENT_END`, `Option.FORCE_EVENT_DURATION`
**Related Events:** —

---

## User-Facing Behavior

- Entries appear on the calendar at their configured date/time position
- Timed entries show in the time grid with a colored block spanning their duration
- All-day entries appear in the all-day row at the top of timegrid views or as blocks in daygrid views
- Entry color, background, border, and text color are visible per the configured values
- Entries with `DisplayMode.BACKGROUND` render as shaded areas behind the grid
- Entries with `DisplayMode.INVERSE_BACKGROUND` shade everything except the entry's time range
- Entries with `DisplayMode.NONE` are not rendered
- Entries with a `url` render as clickable `<a>` tags

---

## Java API Usage

```java
Entry entry = new Entry();
entry.setTitle("Team Meeting");
entry.setStart(LocalDateTime.of(2025, 3, 15, 10, 0));
entry.setEnd(LocalDateTime.of(2025, 3, 15, 11, 0));
entry.setColor("#3788d8");

// All-day entry
Entry allDay = new Entry();
allDay.setTitle("Conference");
allDay.setStart(LocalDate.of(2025, 3, 20));
allDay.setEnd(LocalDate.of(2025, 3, 22));
allDay.setAllDay(true);

// Background entry
Entry bg = new Entry();
bg.setStart(LocalDateTime.of(2025, 3, 15, 8, 0));
bg.setEnd(LocalDateTime.of(2025, 3, 15, 18, 0));
bg.setDisplayMode(DisplayMode.BACKGROUND);
bg.setColor("#e8f5e9");

// Global defaults
calendar.setOption(Option.ENTRY_COLOR, "#4CAF50");
calendar.setOption(Option.DISPLAY_ENTRY_TIME, true);
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | Per-entry color overrides global `ENTRY_COLOR` |
| BR-02 | `DisplayMode.NONE` suppresses rendering entirely — entry exists in model but is invisible |
| BR-03 | Setting `start` as `LocalDate` converts to midnight `LocalDateTime` — it does NOT set `allDay = true` automatically. Call `setAllDay(true)` explicitly for all-day entries. |
| BR-04 | Omitting `end` creates a point-in-time entry (FC renders with default duration) |
| BR-05 | Entries with a `url` property are rendered as `<a>` tags and navigate on click |

---

## Acceptance Criteria

- [ ] Timed entry appears at correct position in timeGrid view
- [ ] All-day entry appears in the all-day row
- [ ] Entry color, background, border, and text color render correctly
- [ ] Background display mode shades the entry's time range *(manual verification)*
- [ ] Inverse background display mode shades everything except the entry's time range *(manual verification)*
- [ ] `DisplayMode.NONE` entry is not visible
- [ ] Entry without end time renders with FC default duration (timed entries: `defaultTimedEventDuration`, default 1 hour; all-day: `defaultAllDayEventDuration`, default 1 day)
- [ ] Global `ENTRY_COLOR` applies when entry has no individual color
- [ ] Entry with `url` renders as a link and navigates on click

---

## Tests

### Unit Tests
- [ ] `EntryTest` — property setters, JSON serialization of all entry fields

### E2E Tests
- [ ] `calendar-entry.spec.js` — visual entry display, color, positioning

---

## Related FullCalendar Docs

- [Event Object](https://fullcalendar.io/docs/event-object)
- [eventDisplay](https://fullcalendar.io/docs/eventDisplay)
- [eventColor](https://fullcalendar.io/docs/eventColor)
