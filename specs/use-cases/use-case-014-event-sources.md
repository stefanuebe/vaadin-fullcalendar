# UC-014: Client-Side Event Sources

**As a** Vaadin application developer, **I want to** add client-side event sources **so that** the calendar can load entries from external feeds (JSON, Google Calendar, iCal) without server roundtrips.

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon
**Related Options:** `Option.EXTERNAL_EVENT_SOURCE_START_PARAM`, `Option.EXTERNAL_EVENT_SOURCE_END_PARAM`, `Option.EXTERNAL_EVENT_SOURCE_TIME_ZONE_PARAM`, `Option.EXTERNAL_EVENT_SOURCE_GOOGLE_CALENDAR_API_KEY`
**Related Events:** `EventSourceFailureEvent`, `ExternalEntryDroppedEvent`, `ExternalEntryResizedEvent`

---

## User-Facing Behavior

- JSON Feed: calendar fetches entries from a REST endpoint (FC adds start/end query params)
- Google Calendar: displays events from a Google Calendar (requires API key)
- iCalendar: loads events from an .ics URL
- Client-side entries are read-only by default; DnD/resize can be enabled per source
- If a source fails to load, `EventSourceFailureEvent` fires

---

## Java API Usage

```java
// JSON feed
JsonFeedEventSource json = new JsonFeedEventSource("https://api.example.com/events");
json.withEditable(true); // allow DnD
calendar.addClientSideEventSource(json);

// Google Calendar
GoogleCalendarEventSource google = new GoogleCalendarEventSource("calId@gmail.com");
google.withApiKey("YOUR_KEY");
calendar.addClientSideEventSource(google);

// iCalendar
ICalendarEventSource ical = new ICalendarEventSource("https://example.com/cal.ics");
calendar.addClientSideEventSource(ical);

// Remove source — two patterns:
// 1. Via Registration (preferred when you have the reference)
Registration reg = calendar.addClientSideEventSource(json);
reg.remove();
// 2. Via source ID (when Registration reference is not available)
calendar.removeClientSideEventSource(json.getId());

// Handle DnD of external entries
calendar.addExternalEntryDroppedListener(event -> { ... });
calendar.addExternalEntryResizedListener(event -> { ... });

// Handle source failures
calendar.addEventSourceFailureListener(event -> { ... });
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | Client-side entries are read-only by default (`editable = false`) |
| BR-02 | `withEditable(true)` enables DnD/resize for entries from that source |
| BR-03 | External entries fire `ExternalEntryDroppedEvent` / `ExternalEntryResizedEvent` (not server-managed counterparts) |
| BR-04 | JSON feed receives `start`, `end`, `timeZone` query parameters (configurable) |
| BR-05 | Google Calendar requires an API key (per-source or global) |
| BR-06 | Source failures fire `EventSourceFailureEvent` |

---

## Acceptance Criteria

- [ ] JSON feed loads and displays entries
- [ ] Google Calendar entries display (with valid API key)
- [ ] iCalendar entries display
- [ ] Client-side entries are read-only by default
- [ ] `withEditable(true)` enables DnD for source entries
- [ ] `ExternalEntryDroppedEvent` fires on DnD of external entries
- [ ] `EventSourceFailureEvent` fires on load failure
- [ ] Removing a source removes its entries from display

---

## Tests

### Unit Tests
- [ ] `EventSourcesTest` — source construction and properties

### E2E Tests
- [ ] `event-sources.spec.js` — event source loading

---

## Related FullCalendar Docs

- [Event Sources](https://fullcalendar.io/docs/event-source-object)
- [JSON Feed](https://fullcalendar.io/docs/events-json-feed)
- [Google Calendar](https://fullcalendar.io/docs/google-calendar)
- [iCalendar](https://fullcalendar.io/docs/icalendar)
