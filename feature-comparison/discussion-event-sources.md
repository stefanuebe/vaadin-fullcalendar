# Design Discussion: Event Sources & Event Mutation Callbacks

**Date:** 2026-03-18
**Participants:** Stefan (project owner), Claude
**Outcome:** Decisions captured in `phase-4-event-sources.md`

---

## 1. Should we add typed Java wrappers for client-side event sources?

**Context:** FullCalendar supports fetching events directly in the browser from a URL (JSON feed), Google Calendar, or an iCal feed. These bypass the Java server entirely. The question was whether the addon should expose a typed Java API for configuring them, or leave them to raw `setOption(String, Object)`.

**Stefan's vision:** In a typical Vaadin app, the server provides UI (e.g. a `TextField`) for the user to enter a Google Calendar ID or iCal URL. That value then needs to be passed to the calendar from the server side. A typed Java wrapper (e.g. `GoogleCalendarEventSource`, `ICalendarEventSource`) is the natural way to do this — the server *configures* the source even though it does not *own* the entries.

**Decision: yes, typed Java wrappers.** The server configures but does not observe. The wrapper is a value object that serializes to the FullCalendar `eventSources` JSON config. It does not interact with `EntryProvider`.

---

## 2. What about event mutation callbacks? (`eventAdd`, `eventChange`, `eventRemove`, `eventsSet`)

**Context:** FC fires these callbacks when its internal event store mutates. The question was whether to expose them as server-side Java events.

**Analysis:**

For **server-managed sources** (`InMemoryEntryProvider`, `CallbackEntryProvider`):
- The server already knows about all mutations because it initiated them. These callbacks would be redundant.

For **client-managed sources** (JSON feed, Google Calendar, iCal):
- The server never sees the individual entries from these sources. If the server doesn't know what was loaded initially, there is nothing meaningful it can do with a "this entry changed" notification. The entry objects would be transient JS data with no Java counterpart.

**Stefan's reasoning:** "If the server does not know about the initial loaded Google Calendar entries, it also does not need to know about any changes."

**Decision: `eventAdd`, `eventChange`, `eventRemove`, `eventsSet` are NOT implemented.**
`loading` and `eventSourceFailure` are retained because they convey useful state (loading indicator, error handling) regardless of who owns the data.

---

## 3. The mixed-source drag/drop edge case

**Context:** What happens when a calendar mixes server-managed entries (via `EntryProvider`) and client-managed entries (via an event source), and the user drags a client-managed entry to a new time slot?

### Sub-question A: How does pure FullCalendar handle dragging a Google Calendar entry?

FC's Google Calendar plugin is **read-only by design**. When a user drags a Google Calendar entry:
- FC fires `eventDrop` and moves the entry visually.
- FC does **not** write back to Google Calendar. There is no built-in persistence.
- The developer is entirely responsible for calling the Google Calendar API if they want the change to persist.
- In practice, most developers set `editable: false` on Google Calendar sources to prevent drag/drop entirely.
- Same applies to iCal (read-only by format) and JSON feeds (FC has no write mechanism).

### Sub-question B: What would currently happen in the addon?

Currently, when `eventDrop` fires on the client, the frontend calls `this.$server.entryDropped(id, ...)`. The server looks up the entry by `id` in the entry cache. If the entry came from a client-side source, it is not in the cache → **server-side exception**.

### Stefan's proposal: `ExternalEntryDroppedEvent`

Instead of throwing, the addon should:
1. Detect that the dropped entry's id is not in the server-side entry cache.
2. Construct a transient `Entry` data carrier from the JS event data (title, start, end, id, etc.).
3. Fire a distinct `ExternalEntryDroppedEvent` (NOT `EntryDroppedEvent`) with:
   - The transient `Entry` (read-only snapshot, not owned by any `EntryProvider`)
   - The **source id** — so the developer knows which configured event source the entry came from
   - The delta (old start/end → new start/end)

This allows the developer to:
```java
calendar.addExternalEntryDroppedListener(event -> {
    if ("my-google-cal".equals(event.getSourceId())) {
        googleCalendarApi.moveEvent(event.getEntry().getId(), event.getNewStart());
    }
});
```

**The source id is therefore critical:** every `EventSource` must carry a developer-assigned (or auto-generated) `id` so the server can route the event correctly when multiple sources are configured.

**Decision: implement `ExternalEntryDroppedEvent` and `ExternalEntryResizedEvent`.**
The same pattern extends naturally to Phase 3's external drag-drop (`eventReceive`) where an HTML element from outside the calendar is dropped onto it.

---

## 4. Summary of all decisions

| Topic | Decision |
|---|---|
| Typed Java wrappers for JSON feed, Google Calendar, iCal | ✅ Implement |
| `eventAdd`, `eventChange`, `eventRemove`, `eventsSet` | ❌ Exclude — no value in either paradigm |
| `loading` callback | ✅ JS-string callback only (UI concern, server latency undesirable) |
| `eventSourceFailure` | ✅ Server event (so Java can show error notification, log, etc.) |
| Client-side sources default `editable` | `false` — read-only by default; developer must opt in to drag/drop |
| Mixed-source drag/drop | ✅ `ExternalEntryDroppedEvent` + `ExternalEntryResizedEvent` with transient Entry + source id |
| `EventSource.id` field | Auto-generated UUID by default; developer should set a meaningful id if handling external events |
| `refetchEvents()` | ✅ Trivial addition regardless |

---

## 5. Open questions (not yet decided)

- **Should `GoogleCalendarEventSource` and `ICalendarEventSource` be optional modules** (separate Maven artifacts) rather than bundled in the core addon? They require additional npm packages (`@fullcalendar/google-calendar`, `@fullcalendar/icalendar`, `ical.js`) which increase bundle size for everyone even if they don't use these features. → Worth discussing before Phase 4 implementation begins.

- **Should the transient `Entry` in `ExternalEntryDroppedEvent` be a full `Entry` instance or a lighter read-only type** (e.g. `ExternalEntry` or `EntrySnapshot`)? Using `Entry` is convenient (same API), but it could mislead developers into adding it to an `EntryProvider`. A distinct type communicates the read-only/transient nature more clearly.
