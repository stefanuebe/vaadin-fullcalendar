# Phase 3: Missing Interaction Callbacks and Server-Side Events

## Goal

Add server-side Java events and listener registration methods for FC interaction callbacks that are currently not covered. These enable the Java application to react to user gestures on the calendar.

---

## Features Covered

### 3.1 Drag/Resize lifecycle events (begin/end)

FC fires four callbacks for the start and end of drag/resize operations:
- `eventDragStart` — drag begins
- `eventDragStop` — drag ends (regardless of whether drop happened)
- `eventResizeStart` — resize begins
- `eventResizeStop` — resize ends (regardless of whether resize changed duration)

These complement the existing `EntryDroppedEvent` and `EntryResizedEvent` which fire only when the operation *committed* a change.

**Concrete use cases:**
- Disable a "Delete" or "Edit" button in the UI while a drag is in progress (re-enable in `EntryDragStopEvent`)
- Show a loading overlay or visual feedback: "Dragging to reschedule..." banner
- Lock the form/dialog that is open for the current entry
- Track analytics: how long do users take to drag?

**Critical distinction: `EntryDragStopEvent` vs `EntryDroppedEvent`:**
- `EntryDragStopEvent` fires **every time a drag ends**, including when the user releases the entry back in its original position (no-op drag). No data changed.
- `EntryDroppedEvent` fires **only when the drop caused a change** (the entry moved to a new time/date). The server-side entry data is updated.
- Always use `EntryDroppedEvent` if you need to react to actual changes. Use `EntryDragStopEvent` only for UI cleanup (e.g., re-enabling the Delete button after any drag, regardless of outcome).

**New server-side events:**
```java
EntryDragStartEvent    // new — fires when drag begins
EntryDragStopEvent     // new — fires when drag ends (even if position unchanged)
EntryResizeStartEvent  // new — fires when resize begins
EntryResizeStopEvent   // new — fires when resize ends (even if duration unchanged)
```

**Event payload (for both drag start/stop events):**
```java
// In EntryDragStartEvent / EntryDragStopEvent:
Entry entry;                 // the entry being dragged (resolved from server-side cache by id)
MouseEventDetails jsEvent;   // optional: JS mouse event coordinates, button, etc.
```

**Listener registration:**
```java
Registration addEntryDragStartListener(ComponentEventListener<EntryDragStartEvent> listener)
Registration addEntryDragStopListener(ComponentEventListener<EntryDragStopEvent> listener)
Registration addEntryResizeStartListener(ComponentEventListener<EntryResizeStartEvent> listener)
Registration addEntryResizeStopListener(ComponentEventListener<EntryResizeStopEvent> listener)
```

**Implementation notes:**
- Requires client-side change in the TS companion: add `eventDragStart`, `eventDragStop`, `eventResizeStart`, `eventResizeStop` callbacks to FC calendar options, each calling `this.$server.entryDragStart(eventId, jsEventData)` etc.
- The frontend resolves the entry ID and passes it to the server; the server looks up the entry in `lastFetchedEntries` cache (same as `EntryDroppedEvent` does).
- These are "quiet" events — the Java side simply fires the event; no revert/undo needed. Do not update entry state in these handlers.
- Follow the exact same pattern as the existing `entryDrop` / `entryResize` frontend wiring.

**Frontend TypeScript impact:** Required — add 4 new FC callback registrations and 4 new `@ClientCallable` method calls to the TS companion file.

---

### 3.2 Event lifecycle callbacks: eventAdd, eventChange, eventRemove, eventsSet

**STATUS: EXCLUDED — do not implement**

FC fires these when the internal event store changes:
- `eventAdd` — after an event is added to FC's store
- `eventChange` — after an event is modified (drag, resize, or programmatic setProp)
- `eventRemove` — after an event is removed from FC's store
- `eventsSet` — after events are initialized or any change happens (full snapshot)

**Rationale for exclusion** (from `discussion-event-sources.md`, design discussion 2026-03-18):

For **server-managed sources** (`InMemoryEntryProvider`, `CallbackEntryProvider`): The server already knows about all mutations because it initiated them. These callbacks would be redundant — the server is the source of truth.

For **client-managed sources** (JSON feed, Google Calendar, iCal): The server never sees the individual entries from these sources. If the server doesn't know what was loaded initially, there is nothing meaningful it can do with a "this entry changed" notification. The entry objects would be transient JS data with no Java counterpart.

**Stefan's reasoning (quoted from `discussion-event-sources.md`):** "If the server does not know about the initial loaded Google Calendar entries, it also does not need to know about any changes."

**Do not implement** `eventAdd`, `eventChange`, `eventRemove`, or `eventsSet` as either server events or JS-string callbacks. The original recommendation in this file to add JS-string callbacks is hereby revoked.

**See `discussion-event-sources.md` for full rationale.**

---

### 3.3 `unselect` callback

FC fires `unselect` when the user's date selection is cleared:
- User clicks elsewhere on the calendar (when `unselectAuto` is `true`, which is the default)
- User presses Escape
- A new selection is made (which clears the old one)
- Code calls `calendar.unselect()` programmatically

**Payload:** The `unselect` callback receives:
- `jsEvent` — the mouse event (or null if triggered programmatically / by keyboard)
- `view` — the current view object

The server-side event should include the JS event details when available.

**New event:**
```java
public class TimeslotsUnselectEvent extends ComponentEvent<FullCalendar> {
    // payload:
    MouseEventDetails jsEvent;  // nullable — null when triggered programmatically/by keyboard
}
```

**Listener registration:**
```java
Registration addTimeslotsUnselectListener(ComponentEventListener<TimeslotsUnselectEvent> listener)
```

**Frontend:** Call `this.$server.timeslotsUnselect(jsEventJson)` from the FC `unselect` callback. The `jsEvent` parameter contains mouse coordinates and button info; serialize only what's useful.

**Note:** `unselect` fires when `unselectAuto` is `true` (default) and the user clicks outside the selection. If `unselectAuto` is `false`, the app must call `calendar.unselect()` programmatically to clear the selection; this event still fires in that case (triggered by the programmatic call). `setUnselectCancel(String cssSelector)` can be used to prevent unselect when clicking on specific elements (e.g., a form bound to the selection).

---

### 3.4 `selectAllow` JS callback

FC's `selectAllow` callback gives programmatic control over which date ranges the user can select. It runs **synchronously on every mouse move** during a drag-to-select operation.

**Why this must be JS-only (no server round-trip):**
Adding even 50ms of server latency would make date selection feel broken — the selection would not follow the cursor, or would allow invalid selections to appear before the rejection fires. This is the same principle as why `selectAllow` cannot be a server event: synchronous drag feedback requires zero-latency decision making.

```java
setSelectAllowCallback(String jsFunction)
// Calls setOption("selectAllow", s)
// e.g.: "function(selectInfo) { return selectInfo.start >= new Date('2023-01-01'); }"
```

**JS callback argument:**
```js
selectInfo = {
  start: Date,
  end: Date,
  startStr: String,
  endStr: String,
  allDay: boolean,
  resource: Resource|null  // scheduler only
}
```
The function must return `true` (allow) or `false` (deny). Returning `false` prevents the selection from being created.

**Pattern:** Same as `setEntryClassNamesCallback` — calls `setOption("selectAllow", s)`.

**Frontend TypeScript impact:** None — passes through existing `setOption` channel.

---

### 3.5 `eventAllow` JS callback

Same rationale as `selectAllow` — runs during drag, must be client-side only. Determines whether a dragged event can be dropped at a given location.

**Why JS-only:** Same as 3.4 — synchronous drag feedback requirement. Even 50ms latency makes drag feel broken; the drag mirror would briefly appear at an invalid location before the server responds.

```java
setEventAllowCallback(String jsFunction)
// Calls setOption("eventAllow", s)
// e.g.: "function(dropInfo, draggedEvent) { return dropInfo.resource.id !== 'locked-room'; }"
```

**JS callback argument:**
```js
// dropInfo:
{
  start: Date,
  end: Date,
  startStr: String,
  endStr: String,
  allDay: boolean,
  resource: Resource|null  // scheduler only
}
// draggedEvent: EventApi (the event being dragged)
```
Return `true` to allow the drop, `false` to deny. When denied, FC shows a "not allowed" cursor and prevents the drop.

---

### 3.6 `navLinkDayClick` and `navLinkWeekClick` server events

**Important distinction: nav links vs. day/week number grid clicks**

FC has two separate click mechanisms that look similar but are different:

1. **Grid day/week numbers** — the day and week numbers rendered inside the calendar grid cells (e.g., in month view, clicking "15" in a day cell). These fire `DayNumberClickedEvent` and `WeekNumberClickedEvent` (already implemented in the addon).

2. **Toolbar nav links** — when `navLinks` is enabled, the header/toolbar day/week text becomes clickable nav links. Clicking a nav link in the toolbar calls `navLinkDayClick` or `navLinkWeekClick`. **These are different callbacks** from the grid number clicks, and they **override FC's default navigation behavior** (instead of changing the view, your custom function runs).

So `navLinkDayClick` / `navLinkWeekClick` are for toolbar-level nav link overrides, while `DayNumberClickedEvent` / `WeekNumberClickedEvent` are for grid-cell number clicks. They are not the same thing.

**Option A (simple):** JS string callbacks only:
```java
setNavLinkDayClickCallback(String jsFunction)
setNavLinkWeekClickCallback(String jsFunction)
```

**Option B (server round-trip) — Recommended:** New server events with a `LocalDate` payload. This is consistent with `DayNumberClickedEvent` and `WeekNumberClickedEvent` and provides more value.

```java
public class NavLinkDayClickedEvent extends ComponentEvent<FullCalendar> {
    LocalDate date;     // the day that was clicked
    // jsEvent available but typically not needed
}

public class NavLinkWeekClickedEvent extends ComponentEvent<FullCalendar> {
    LocalDate weekStart; // the first day of the week that was clicked
}
```

**New listener methods:**
```java
Registration addNavLinkDayClickedListener(ComponentEventListener<NavLinkDayClickedEvent> listener)
Registration addNavLinkWeekClickedListener(ComponentEventListener<NavLinkWeekClickedEvent> listener)
```

**Frontend:** The TS companion registers `navLinkDayClick` and `navLinkWeekClick` FC callbacks that call `this.$server.navLinkDayClicked(dateStr)` and `this.$server.navLinkWeekClicked(dateStr)`.

**Caveat:** Setting `navLinkDayClick` / `navLinkWeekClick` **overrides** FC's default behavior (navigating to day/week view). If the developer registers a server listener, FC will NOT automatically navigate. The developer must call `calendar.changeView(...)` or `calendar.gotoDate(...)` explicitly in their listener if they want navigation. Document this clearly.

---

### 3.7 `windowResize` server event

FC fires `windowResize` when the browser window is resized and the calendar recalculates its layout.

```java
public class WindowResizeEvent extends ComponentEvent<FullCalendar> {
    CalendarView view;  // the current view after resize
}

Registration addWindowResizeListener(ComponentEventListener<WindowResizeEvent> listener)
```

**Frontend:** Call `this.$server.windowResized(viewName)` from the FC `windowResize` callback.

**Caveat:** This event fires every time the calendar redraws due to window resize, which can be frequent. The `windowResizeDelay` option (Phase 2.1) debounces FC's internal handling; the Java event fires after the debounce.

**Use case:** Update server-side layout decisions when the calendar changes responsive breakpoints (e.g., switch from timeGridWeek to timeGridDay on small screens).

---

### 3.8 External drag-drop support (`droppable`, `drop`, `eventReceive`, `eventLeave`)

FC supports dragging arbitrary HTML elements onto the calendar and from one calendar to another. This is an advanced feature.

**How FC's `Draggable` API works:**
FC makes external HTML elements draggable by requiring either:
1. The CSS class `fc-event` on the element — FC auto-detects these
2. A `data-event` attribute containing JSON (title, duration, etc.)
3. Explicit registration via `new Draggable(containerEl, { itemSelector: '.my-event' })`

For Vaadin apps, the most practical approach is using `data-event` attributes on server-rendered elements:
```html
<div class="draggable-event"
     data-event='{"title":"Meeting","duration":"01:00"}'>
  Meeting
</div>
```
When such an element is dropped onto the calendar, FC calls `eventReceive` with a newly created event object.

**Java API additions:**

```java
// Enable accepting external drops:
public void setDroppable(boolean droppable) {
    setOption("droppable", droppable);
}

// Filter which external elements can be dropped (CSS selector or JS function):
public void setDropAccept(String cssSelectorOrJsFunction) {
    setOption("dropAccept", cssSelectorOrJsFunction);
}
```

**New server events:**

```java
// Fires when ANY external element is dropped (even non-event HTML elements)
public class DropEvent extends ComponentEvent<FullCalendar> {
    LocalDate date;            // the date/datetime of the drop location
    boolean allDay;
    // Note: draggedEl (the DOM element) cannot be passed to server
    // Only data attributes on the element can be read and sent
    String draggedElData;      // the data-event JSON string if present, else null
}

// Fires when an external event element is dropped and FC adds it as an event
public class EntryReceiveEvent extends ComponentEvent<FullCalendar> {
    Entry entry;               // the newly created entry (constructed from data-event attributes)
}

// Fires when a calendar event is dragged out to another calendar (if configured)
public class EntryLeaveEvent extends ComponentEvent<FullCalendar> {
    Entry entry;               // the entry that left
}
```

**Listener registration:**
```java
Registration addDropListener(ComponentEventListener<DropEvent> listener)
Registration addEntryReceiveListener(ComponentEventListener<EntryReceiveEvent> listener)
Registration addEntryLeaveListener(ComponentEventListener<EntryLeaveEvent> listener)
```

**Implementation note:** The `drop` callback's `draggedEl` is a DOM element — only limited info can be reliably passed to the server. The most useful info is the element's `data-event` attribute content (if set by the developer). `eventReceive` is more tractable since FC creates a full event object. Consider implementing `eventReceive` first (higher value) and treating `drop` as an advanced follow-up.

**Relationship to Phase 4 `ExternalEntryDroppedEvent`:** `ExternalEntryDroppedEvent` (Phase 4 / `discussion-event-sources.md`) is for entries that came from client-managed event sources (Google Calendar, JSON feed) and were dragged within the calendar. `EntryReceiveEvent` (this section) is for external HTML elements dropped onto the calendar. These are two different scenarios.

**Frontend TypeScript impact:** Required — register `drop`, `eventReceive`, `eventLeave` callbacks in the FC calendar options and wire to server-callable methods.

---

### 3.9 `eventOverlap` as a JS callback (global option)

FC's global `eventOverlap` option accepts either a boolean or a function. The boolean form controls whether all events can overlap by default. The function form allows per-combination control: "can event A overlap with event B?"

The boolean form is already accessible via `setSlotEntryOverlap(boolean)` (Phase 0.1). The function form needs a separate setter:

```java
public void setEventOverlapCallback(String jsFunction) {
    setOption("eventOverlap", jsFunction);
}
// e.g.: "function(stillEvent, movingEvent) { return stillEvent.display === 'background'; }"
```

**JS callback arguments:**
```js
// stillEvent: EventApi — the event that is stationary
// movingEvent: EventApi — the event being dragged
// Return true to allow overlap, false to deny
```

**Why JS-only:** Same reasoning as `selectAllow` (3.4) — this runs synchronously during drag feedback. Any server latency would make drag feel broken.

**Relationship to `Entry.overlap`:** The global `eventOverlap` function is the calendar-level dynamic policy. `Entry.overlap` is a static per-entry override. The per-entry `overlap` takes precedence: if an entry has `overlap = false`, it cannot overlap regardless of the global function. The global function is only consulted for entries with `overlap = null` (unset) or `overlap = true`.

---

## Implementation Notes

- All new server-side events follow the pattern of existing events (e.g. `EntryClickedEvent`, `EntryDroppedEvent`)
- Events that carry an `Entry` obtain it from `calendar.lastFetchedEntries` cache (keyed by entry ID), the same way `EntryDroppedEvent` does
- The drag/resize start/stop events (3.1) are the highest value additions since they enable UI feedback without any data mutation
- The `unselect` event (3.3) completes the selection API alongside `TimeslotsSelectedEvent`
- External drag-drop (3.8) is the most complex — can be a separate sub-phase or deferred
- Section 3.2 (`eventAdd/Change/Remove/Set`) is EXCLUDED — see rationale above and `discussion-event-sources.md`

---

## Testing

### JUnit tests
Add a test class `Phase3InteractionCallbacksTest.java` in `addon/src/test/java/org/vaadin/stefan/fullcalendar/`.

Cover:
- Listener registration: verify `addEntryDragStartListener()`, `addTimeslotsUnselectListener()`, etc. return valid `Registration` objects and listeners are stored
- JS callback string setters: verify `setSelectAllowCallback()`, `setEventAllowCallback()`, `setEventOverlapCallback()` call `setOption()` with the correct FC key
- JS callback with option verification: ensure the callback string is passed through unchanged (no escaping)
- Event payload construction: create mock events and verify they are fired with correct entry/date data

### Playwright tests (client-side effects)
Add demo view at `demo/src/main/java/org/vaadin/stefan/ui/view/testviews/Phase3InteractionCallbacksTestView.java` to verify:
- Drag start/stop events fire (check via JS console or a badge showing event count)
- Resize start/stop events fire similarly
- Unselect event fires when selection is cleared (click outside selection)
- Nav link clicks navigate correctly and fire server events
- `selectAllow` callback prevents invalid date range selections
- `eventAllow` callback prevents drops to disallowed locations

Add Playwright spec at `e2e-tests/tests/phase3-interaction-callbacks.spec.js` to verify event firing and drag/drop constraint behavior.

---

## Files to Modify

- `addon/src/main/java/org/vaadin/stefan/fullcalendar/FullCalendar.java`
  - Add listener registration methods for 3.1, 3.3, 3.6, 3.7, 3.8
  - Add JS-string callback setters for `selectAllow` (3.4), `eventAllow` (3.5), `eventOverlap` (3.9), `dropAccept` (3.8)
  - Add `setDroppable(boolean)` (3.8)
- `addon/src/main/java/org/vaadin/stefan/fullcalendar/` (new event classes):
  - `EntryDragStartEvent.java`
  - `EntryDragStopEvent.java`
  - `EntryResizeStartEvent.java`
  - `EntryResizeStopEvent.java`
  - `TimeslotsUnselectEvent.java`
  - `NavLinkDayClickedEvent.java`
  - `NavLinkWeekClickedEvent.java`
  - `WindowResizeEvent.java`
  - (Phase 2 follow-up) `DropEvent.java`, `EntryReceiveEvent.java`, `EntryLeaveEvent.java`
- Frontend TypeScript companion file:
  - Wire new `@ClientCallable` methods for each new server event
  - Register FC callbacks: `eventDragStart`, `eventDragStop`, `eventResizeStart`, `eventResizeStop`, `unselect`, `navLinkDayClick`, `navLinkWeekClick`, `windowResize`, (optionally) `drop`, `eventReceive`, `eventLeave`
