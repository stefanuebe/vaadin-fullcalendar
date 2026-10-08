# UC-014: Remote Entry Sources

**As a** Vaadin application developer, **I want to** add remote entry sources **so that** the calendar can load entries from external feeds (JSON, Google Calendar, iCal) without server roundtrips.

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon
**Related Options:** `Option.ENTRY_SOURCE_START_PARAM`, `Option.ENTRY_SOURCE_END_PARAM`, `Option.ENTRY_SOURCE_TIME_ZONE_PARAM`, `Option.ENTRY_SOURCE_GOOGLE_CALENDAR_API_KEY`
**Related Events:** `RemoteEntrySourceFailureEvent`, `RemoteEntryDroppedEvent`, `RemoteEntryResizedEvent`

---

## User-Facing Behavior

- JSON Feed: calendar fetches entries from a REST endpoint (FullCalendar adds start/end query params)
- Google Calendar: displays events from a Google Calendar (requires API key)
- iCalendar: loads events from an .ics URL
- Entries of remote entry sources are read-only by default; DnD/resize can be enabled per source
- If a source fails to load, `RemoteEntrySourceFailureEvent` fires

---

## Java API Usage

```java
// JSON feed
JsonFeedEntrySource json = new JsonFeedEntrySource("https://api.example.com/events");
json.withEditable(true); // allow DnD
calendar.addRemoteEntrySource(json);

// Google Calendar
GoogleCalendarEntrySource google = new GoogleCalendarEntrySource("calId@gmail.com");
google.withApiKey("YOUR_KEY");
calendar.addRemoteEntrySource(google);

// iCalendar
ICalendarEntrySource ical = new ICalendarEntrySource("https://example.com/cal.ics");
calendar.addRemoteEntrySource(ical);

// Remove source — two patterns:
// 1. Via Registration (preferred when you have the reference)
Registration reg = calendar.addRemoteEntrySource(json);
reg.remove();
// 2. Via source ID (when Registration reference is not available)
calendar.removeRemoteEntrySource(json.getId());

// Handle DnD of entry source entries
calendar.addRemoteEntryDroppedListener(event -> { ... });
calendar.addRemoteEntryResizedListener(event -> { ... });

// Handle source failures
calendar.addRemoteEntrySourceFailureListener(event -> { ... });
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | Entries of remote entry sources are read-only by default (`editable = false`) |
| BR-02 | `withEditable(true)` enables DnD/resize for entries from that source |
| BR-03 | Entry source entries fire `RemoteEntryDroppedEvent` / `RemoteEntryResizedEvent` (not server-managed counterparts). `getEntry().getId()` is the id the entry has in its source |
| BR-04 | JSON feed receives `start`, `end`, `timeZone` query parameters (configurable) |
| BR-05 | Google Calendar requires an API key (per-source or global) |
| BR-06 | Source failures fire `RemoteEntrySourceFailureEvent`. A callback set with `withFailure` runs first |

---

## Acceptance Criteria

- [ ] JSON feed loads and displays entries
- [ ] Google Calendar entries display (with valid API key)
- [ ] iCalendar entries display
- [ ] Entries of remote entry sources are read-only by default
- [ ] `withEditable(true)` enables DnD for source entries
- [ ] `RemoteEntryDroppedEvent` fires on DnD of entry source entries
- [x] `RemoteEntrySourceFailureEvent` fires on load failure, and the `withFailure` callback runs as well (#275)
- [ ] Removing a source removes its entries from display

---

## Tests

### Unit Tests
- [x] `EventSourcesTest`: source construction and properties, drop / resize events reach their listeners

### E2E Tests
- [x] `event-sources.spec.js` — load failure reaches the server listener and the `withFailure` callback

---

## Related FullCalendar Docs

- [Event Sources](https://fullcalendar.io/docs/event-source-object)
- [JSON Feed](https://fullcalendar.io/docs/events-json-feed)
- [Google Calendar](https://fullcalendar.io/docs/google-calendar)
- [iCalendar](https://fullcalendar.io/docs/icalendar)
