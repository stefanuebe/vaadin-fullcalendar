# Phase 4: Event Source Improvements

## Goal

Add typed Java API for FC's remote event source capabilities — JSON feed, Google Calendar, iCalendar feeds — and add typed setters for event-source-related options. Currently these can only be configured via raw `setOption(String, Object)` or initial options JSON, bypassing the server-side Java data model entirely.

---

## Design Decisions (from requirements discussion)

### Client-side vs server-side event sources
The addon's primary data model is the `EntryProvider` abstraction:
- `InMemoryEntryProvider` — server-managed, all events sent from Java
- `CallbackEntryProvider` — lazy loading from Java backend per date range

Both are "Java-managed" event sources. FC's native event sources (JSON URL, Google Calendar, iCal) are "client-managed" — the browser fetches data directly. These are fundamentally different paradigms and must be clearly documented.

**Typical use case for client-side sources in a Vaadin app:** the server provides a `TextField` (or similar) to configure a Google Calendar ID or iCal URL, then passes it into the calendar via typed Java API. The server configures the source but does not own or observe the individual entries.

### Event mutation callbacks (`eventAdd`, `eventChange`, `eventRemove`, `eventsSet`) — EXCLUDED

These FC callbacks fire when the client-side event store mutates. In a Vaadin context:
- For server-managed sources (`EntryProvider`), the server already knows about all changes because it initiated them.
- For client-managed sources (this phase), the server never owns the individual entries, so mutation notifications carry no actionable meaning.

**Decision:** these callbacks are not implemented. `loading` and `eventSourceFailure` (which convey useful state regardless of who owns the data) are retained in this phase.

### Google Calendar and iCal are read-only
FC's Google Calendar plugin is explicitly read-only — it fetches but never writes back to Google. iCal is read-only by format. JSON feeds are also effectively read-only from FC's perspective (FC reads from the URL but does not POST changes).

**Decision:** client-managed event sources default to `editable: false`. Developers who want to enable drag/drop on these entries must opt in explicitly and handle persistence themselves (see mixed-source drag/drop below).

### Mixed-source drag/drop: `ExternalEntryDroppedEvent`
When a calendar mixes server-managed entries (from `EntryProvider`) and client-managed entries (from an event source), a drag/drop on a client-managed entry currently causes a server-side exception because the entry has no Java `Entry` counterpart.

**Decision:** instead of throwing, the addon creates a transient `Entry` data carrier from the JS event data and fires a distinct `ExternalEntryDroppedEvent` (separate from `EntryDroppedEvent`). This lets the developer identify the dropped entry and call the appropriate external API (e.g., Google Calendar API) to persist the change.

Requirements for this to work:
- Every `EventSource` must carry an **`id`** field (set by the developer) so the server knows which configured source the entry came from.
- The `ExternalEntryDroppedEvent` payload includes: the transient `Entry` data carrier, the source `id`, and the delta (old start/end → new start/end).
- The transient `Entry` is **not** added to any `EntryProvider` — it is a read-only snapshot.
- This same pattern applies to `ExternalEntryResizedEvent`.

This design also naturally extends to the external drag-drop feature (Phase 3, `eventReceive`) where an HTML element dragged onto the calendar from outside should fire a similar event.

---

## Features Covered

### 4.0 Abstract base class: `ClientSideEventSource`

All client-managed event source types share common properties. These live on an abstract base class (not an interface, since they carry shared state):

```java
public abstract class ClientSideEventSource {

    // Developer-assigned ID. Auto-generated UUID if not set.
    // Used server-side to identify which source a dropped/resized entry came from.
    private String id = UUID.randomUUID().toString();

    // Visual overrides applied to all entries from this source
    private String color;           // background + border shorthand
    private String backgroundColor;
    private String borderColor;
    private String textColor;
    private List<String> classNames;

    // Interaction — defaults to false (read-only) for all client-side sources
    private Boolean editable;          // null = use calendar default (which we set to false)
    private Boolean startEditable;
    private Boolean durationEditable;

    // Constraint / overlap
    private String constraint;         // business hours string or event group id
    private Boolean overlap;

    // Display
    private String display;            // "block", "list-item", "background", "inverse-background", "none"

    public abstract JsonObject toJson();
}
```

**Important:** `editable` is `null` by default in the Java class, but the TS frontend sets `editable: false` on all `eventSources` entries unless the developer explicitly calls `source.setEditable(true)`. This enforces read-only-by-default safely.

### 4.1 JSON feed event source typed API

FC fetches a URL directly from the browser and expects a JSON array of FC event objects.

**New class `JsonFeedEventSource extends ClientSideEventSource`:**

```java
public class JsonFeedEventSource extends ClientSideEventSource {

    private final String url;          // required

    // HTTP method — "GET" (default) or "POST"
    private String method = "GET";

    // Extra query parameters appended to every request
    // e.g. Map.of("roomId", "101", "dept", "engineering")
    private Map<String, Object> extraParams;

    // Per-source param name overrides (override the global calendar-level defaults)
    private String startParam;       // default: inherits global "start"
    private String endParam;         // default: inherits global "end"
    private String timeZoneParam;    // default: inherits global "timeZone"

    // Per-source transform hooks (JS strings, run in browser)
    private String eventDataTransformCallback;  // transforms each raw event object
    private String successCallback;             // transforms raw response before FC parses it
    private String failureCallback;             // JS-only error handler for this specific source

    public JsonFeedEventSource(String url) { ... }

    // builder-style setters returning `this` for chaining
    public JsonFeedEventSource withColor(String color) { ... }
    public JsonFeedEventSource withExtraParams(Map<String, Object> params) { ... }
    // etc.
}
```

**Usage:**
```java
JsonFeedEventSource source = new JsonFeedEventSource("/api/events")
    .withId("my-feed")
    .withColor("steelblue")
    .withExtraParams(Map.of("roomId", roomField.getValue()));
calendar.addEventSource(source);
```

**Global parameter defaults** (calendar-level, apply to all feed sources unless overridden per-source):
```java
calendar.setStartParam(String)      // default "start"
calendar.setEndParam(String)        // default "end"
calendar.setTimeZoneParam(String)   // default "timeZone"
calendar.setLazyFetching(boolean)   // default true
```

### 4.2 `loading` callback (async fetch indicator)

FC fires `loading(true)` when it starts an AJAX fetch and `loading(false)` when done.

**Decision: JS-string callback only** — this is a pure UI concern (show/hide a spinner). A server round-trip adds latency that would defeat the purpose.

```java
calendar.setLoadingCallback(String jsFunction)
// e.g. "function(isLoading) { spinner.style.display = isLoading ? 'block' : 'none'; }"
```

### 4.3 `eventSourceFailure` — server-side event

Called when any event source fails to load (network error, bad response, etc.).

**Decision: server event** — so Java code can react meaningfully (show an error `Notification`, log to backend, etc.). A JS-only callback cannot reach Vaadin server-side components.

```java
// New event class
public class EventSourceFailureEvent extends ComponentEvent<FullCalendar> {
    private final String sourceId;    // id of the EventSource that failed, if known
    private final String message;     // error message from FC
}

// Listener registration
calendar.addEventSourceFailureListener(
    ComponentEventListener<EventSourceFailureEvent> listener)
```

Frontend sends: `this.$server.eventSourceFailed(sourceId, errorMessage)`.

### 4.4 `eventSourceSuccess` / `eventDataTransform` — JS-string callbacks

These hooks run synchronously in the browser on raw response data before FC parses it. A server round-trip is architecturally impossible here.

```java
// Transforms the raw HTTP response body (before FC parses it into events)
calendar.setEventSourceSuccessCallback(String jsFunction)
// e.g. "function(content, xhr) { return content.data; }" // unwrap a wrapper object

// Transforms each individual raw event object from any source
calendar.setEventDataTransformCallback(String jsFunction)
// e.g. "function(event) { event.title = event.name; return event; }"
```

### 4.2 `loading` callback (async fetch indicator)

FC fires `loading(true)` when it starts an AJAX fetch and `loading(false)` when done. Useful for showing a loading spinner.

**Option A (JS-string callback):**
```java
setLoadingCallback(String jsFunction)
// e.g.: "function(isLoading) { document.getElementById('spinner').style.display = isLoading ? 'block' : 'none'; }"
```

**Option B (server round-trip):**
Since the loading state change is a UI concern (show/hide spinner), a server event adds latency. JS-string callback is preferable.

### 4.3 `eventSourceFailure` callback

Called when a JSON/Google Calendar/iCal feed fails to load.

**JS-string callback:**
```java
setEventSourceFailureCallback(String jsFunction)
```

**Or server event:**
```java
EventSourceFailureEvent  // payload: error message String
addEventSourceFailureListener(ComponentEventListener<EventSourceFailureEvent>)
```

Frontend: call `this.$server.eventSourceFailed(errorMessage)` from FC's `eventSourceFailure`.

Recommendation: Implement as server event so Java code can react (show an error notification, log, etc.).

### 4.4 `eventSourceSuccess` callback / data transform

FC's `eventSourceSuccess` can transform the raw response before FC parses it. `eventDataTransform` transforms individual event records.

Since these are data transformation hooks that run synchronously on raw data in the browser, they must be JS-string callbacks only:

```java
setEventSourceSuccessCallback(String jsFunction)
setEventDataTransformCallback(String jsFunction)
```

### 4.5 Google Calendar event source typed API

FC can fetch from a **public** Google Calendar feed using a Calendar ID and an API key.

**New class `GoogleCalendarEventSource extends ClientSideEventSource`:**

```java
public class GoogleCalendarEventSource extends ClientSideEventSource {

    private final String googleCalendarId;  // required, e.g. "abc@group.calendar.google.com"
    private String googleCalendarApiKey;    // optional — falls back to calendar.setGoogleCalendarApiKey()

    public GoogleCalendarEventSource(String googleCalendarId) { ... }
}
```

**Usage:**
```java
// Global API key (set once)
calendar.setGoogleCalendarApiKey("AIzaSy...");

// Add one or more Google Calendar sources
calendar.addEventSource(new GoogleCalendarEventSource("holidays@group.calendar.google.com")
    .withId("holidays")
    .withColor("green"));

calendar.addEventSource(new GoogleCalendarEventSource("team@group.calendar.google.com")
    .withId("team-cal")
    .withApiKey("different-key-if-needed"));
```

**Important notes:**
- Only works with **public** Google Calendars. Private calendars require OAuth and cannot use this plugin.
- `editable` defaults to `false`. Google Calendar is read-only from FC's perspective — FC fires `eventDrop` if enabled, but nothing writes back to Google. Developer must call Google Calendar API manually via `ExternalEntryDroppedEvent` if they want persistence.
- **Frontend requirement:** `@fullcalendar/google-calendar` npm package must be added and registered as a plugin.
- **Open question:** should this live in a separate optional Maven module to avoid adding the npm dependency for everyone? (See `discussion-event-sources.md` §5.)

### 4.6 iCalendar feed event source typed API

FC can fetch from a public iCalendar (`.ics`) feed URL.

**New class `ICalendarEventSource extends ClientSideEventSource`:**

```java
public class ICalendarEventSource extends ClientSideEventSource {

    private final String url;  // required — must be publicly accessible from the browser

    public ICalendarEventSource(String url) { ... }
}
```

**Usage:**
```java
calendar.addEventSource(new ICalendarEventSource("https://example.com/holidays.ics")
    .withId("holiday-ics")
    .withColor("purple"));
```

**Important notes:**
- The URL must be accessible from the browser (CORS headers required if cross-origin).
- iCal is a read-only format — `editable` defaults to `false` and drag/drop should not be enabled.
- **Frontend requirement:** `@fullcalendar/icalendar` and `ical.js` npm packages must be added.
- **Open question:** same optional-module consideration as Google Calendar.

### 4.7 Multiple event sources + management API

FC supports multiple concurrent event sources, and they coexist with the server-side `EntryProvider`. The `EntryProvider` maps to FC's `events` function option; client-side sources are pushed into `eventSources`. Both work simultaneously.

**New API on `FullCalendar`:**
```java
// Add a client-side source (registers it and pushes config to client)
calendar.addEventSource(ClientSideEventSource source)

// Remove by the developer-assigned id
calendar.removeEventSource(String id)

// Replace all client-side sources at once (useful for reconfiguration)
calendar.setEventSources(Collection<ClientSideEventSource> sources)

// Read back (server-side registry only — entries are not accessible)
Collection<ClientSideEventSource> calendar.getEventSources()

// Force the client to re-fetch all sources (including EntryProvider)
calendar.refetchEvents()
```

**State management:** `FullCalendar` keeps a `Map<String, ClientSideEventSource>` server-side. On `addEventSource`, it calls `getElement().callJsFunction("addEventSource", source.toJson())`. On `removeEventSource`, it calls `removeEventSource(id)`. The registry allows server-side lookup by id (needed for `ExternalEntryDroppedEvent` routing).

### 4.8 `ExternalEntryDroppedEvent` and `ExternalEntryResizedEvent`

When `editable` is enabled on a client-side source (opt-in), drag/drop and resize of those entries must reach the server. Since these entries are not in the server-side entry cache, the normal `EntryDroppedEvent` path would throw.

**Frontend detection:** after `eventDrop`, check `event.event.id` against the entry cache. If not found → call `this.$server.externalEntryDropped(...)` instead of `entryDropped`.

**New server-side events:**

```java
public class ExternalEntryDroppedEvent extends ComponentEvent<FullCalendar> {
    private final Entry entry;        // transient data carrier — NOT in any EntryProvider
    private final String sourceId;    // id of the ClientSideEventSource it came from
    private final LocalDateTime oldStart;
    private final LocalDateTime oldEnd;
    // new start/end are on entry.getStart() / entry.getEnd()
    private final boolean allDay;
}

public class ExternalEntryResizedEvent extends ComponentEvent<FullCalendar> {
    private final Entry entry;        // transient data carrier
    private final String sourceId;
    private final LocalDateTime oldEnd;
}
```

**Usage pattern:**
```java
calendar.addExternalEntryDroppedListener(e -> {
    if ("my-google-cal".equals(e.getSourceId())) {
        googleApi.moveEvent(e.getEntry().getId(), e.getEntry().getStart());
    }
});
```

**Open question:** should the transient entry be a full `Entry` instance or a lighter read-only `EntrySnapshot` type? Using `Entry` is convenient (same API) but may mislead developers into adding it to a provider. A distinct `EntrySnapshot` communicates its transient nature more clearly. (See `discussion-event-sources.md` §5.)

### 4.9 `refetchEvents()` method

FC's `Calendar.refetchEvents()` forces all sources to re-fetch immediately. Simple addition.

```java
calendar.refetchEvents()  // calls getElement().callJsFunction("refetchEvents")
```

---

## Implementation Notes

- Client-managed event sources bypass the Java `EntryProvider` system entirely — events fetched this way are NOT accessible as Java `Entry` objects server-side. Document clearly.
- `eventAdd`, `eventChange`, `eventRemove`, `eventsSet` mutation callbacks are **intentionally excluded** — they are redundant for server-managed sources and carry no actionable meaning for client-managed sources where the server never owned the entries.
- `loading` stays as JS-string callback (UI concern, adding server round-trip latency would be wrong).
- `eventSourceFailure` is a server event so Java code can react (show error notification, log, etc.).
- `ExternalEntryDroppedEvent` / `ExternalEntryResizedEvent` require frontend changes: after a drag/drop, check whether the entry id exists in the server-side entry cache; if not, route to the external handler.
- The Google Calendar and iCal sources require frontend npm package additions — the most invasive part of this phase.
- `refetchEvents()` is a trivial one-liner that should be added regardless.
- All client-managed `EventSource` implementations default `editable = false`. Developers must explicitly call `source.setEditable(true)` to allow drag/drop, and are responsible for persistence via `ExternalEntryDroppedEvent`.

---

## Testing

### JUnit tests
Add a test class `Phase4EventSourcesTest.java` in `addon/src/test/java/org/vaadin/stefan/fullcalendar/`.

Cover:
- `JsonFeedEventSource` / `GoogleCalendarEventSource` / `ICalendarEventSource`: verify `toJson()` output has correct structure (id, url, color, editable, etc.)
- Event source option setters: verify `setJsonFeedEventSource()`, `setGoogleCalendarApiKey()` call `setOption()` with correct FC keys
- Callback string storage: verify `setLoadingCallback()`, `setEventSourceFailureCallback()` store JS functions correctly
- `refetchEvents()` calls the correct JS function

### Playwright tests (client-side effects)
Add demo view at `demo/src/main/java/org/vaadin/stefan/ui/view/testviews/Phase4EventSourcesTestView.java` with:
- A JSON feed event source pointing to a test endpoint that returns sample events
- Optional: a Google Calendar source (requires test API key or mock)
- Data-testid markers on events to verify they rendered from external source

Add Playwright spec at `e2e-tests/tests/phase4-event-sources.spec.js` to:
- Verify external events appear in the calendar
- Verify `ExternalEntryDroppedEvent` fires when dragging an external event (check via a server-side counter or flag)
- Test `eventSourceFailure` callback behavior if the source URL returns an error

### Code and test review
After implementing all features and writing tests, review each artifact before committing:
- Run a `code-reviewer` agent on the implementation code (new classes, FullCalendar.java changes, frontend TypeScript). Fix all issues found.
- Run a `code-reviewer` agent on the JUnit tests. Fix all issues found (missing null-clearing tests, weak assertions, missing edge cases, etc.).
- Run a `code-reviewer` agent on the Playwright spec. Fix all issues (weak selectors, missing value assertions, flaky timing patterns, duplicate helpers vs. fixtures.js, etc.).
- Commit only after all review passes are clean.



---

## Files to Modify

- `addon/src/main/java/org/vaadin/stefan/fullcalendar/FullCalendar.java`
  - Add `setJsonFeedEventSource(String url, ...)`, `setGoogleCalendarApiKey(String)`, `refetchEvents()`, `setLazyFetching(boolean)`, `setStartParam(String)`, `setEndParam(String)`, `setTimeZoneParam(String)`, `setLoadingCallback(String)`, `setEventDataTransformCallback(String)`, `setEventSourceSuccessCallback(String)`, `setEventSourceFailureCallback(String)`
- New classes in `addon/src/main/java/org/vaadin/stefan/fullcalendar/`:
  - `EventSource.java` (abstract base or interface — carries `id`, `color`, `textColor`, `editable`, `toJson()`)
  - `JsonFeedEventSource.java`
  - `GoogleCalendarEventSource.java` (may go in its own submodule/optional dependency)
  - `ICalendarEventSource.java` (may go in its own submodule)
- New event classes:
  - `EventSourceFailureEvent.java` — payload: source `id`, error message
  - `ExternalEntryDroppedEvent.java` — payload: transient `Entry` data carrier, source `id`, old/new start+end delta
  - `ExternalEntryResizedEvent.java` — payload: transient `Entry` data carrier, source `id`, old/new end delta
- Frontend TypeScript: add Google Calendar and iCal plugin registrations (conditional based on configuration)
- `package.json` / frontend dependencies: add `@fullcalendar/google-calendar`, `@fullcalendar/icalendar`, `ical.js` as optional peer deps
- Frontend: detect drag/drop on entries whose id is not in the server-side entry cache → route to `$server.externalEntryDropped(...)` instead of the normal `entryDropped` call
