# Phase 4: Event System — Typed Parallel Hierarchy

**Goal:** Update existing entry events to use `FullCalendar<Entry>` typing, create parallel
CIP event hierarchy, and set up event dispatch.
**Prerequisite:** Phase 3 complete.
**Breaking changes:** None — existing events get tighter typing (source-compatible due to erasure).

---

## Context

The calendar fires DOM events (click, drag, resize, etc.) that are mapped to Java events via
Vaadin's `@DomEvent` annotation. Currently all events use raw `FullCalendar` as their component
type. This phase updates them to typed `FullCalendar<Entry>` and creates a parallel
`CalendarItemEvent<T>` hierarchy for CIP usage.

**Key constraint:** Registering an `addEntryClickedListener()` on a CIP calendar will cause
`IllegalArgumentException` when the event fires (cache contains POJOs, not Entries). This is
documented in Javadoc as a warning but not prevented at runtime, because listeners can be
registered before the provider is set.

## Module

`addon/src/main/java/org/vaadin/stefan/fullcalendar/`

---

## Part A: Update Existing Entry Events to `FullCalendar<Entry>`

All existing entry events change their `ComponentEvent` type parameter:

### EntryEvent.java
```java
// Before:
public abstract class EntryEvent extends ComponentEvent<FullCalendar> {
    public EntryEvent(FullCalendar source, boolean fromClient, String entryId) { ... }

// After:
public abstract class EntryEvent extends ComponentEvent<FullCalendar<Entry>> {
    /**
     * @param source the calendar (must use EntryProvider, not CalendarItemProvider)
     * @apiNote Registering entry event listeners on a calendar using CalendarItemProvider
     *          will cause IllegalArgumentException when events fire, because the cache
     *          contains POJOs rather than Entry instances.
     */
    public EntryEvent(FullCalendar<Entry> source, boolean fromClient, String entryId) { ... }
```

This is source-compatible: `FullCalendar<Entry>` erases to `FullCalendar` at runtime.
Vaadin's `@DomEvent` reflective instantiation is unaffected.

### All @DomEvent Entry Subclasses

Update constructor parameter from `FullCalendar` to `FullCalendar<Entry>`:

| File | Constructor Change |
|------|-------------------|
| `EntryDataEvent.java` | `FullCalendar source` → `FullCalendar<Entry> source` |
| `EntryClickedEvent.java` | `FullCalendar source` → `FullCalendar<Entry> source` |
| `EntryChangedEvent.java` | `FullCalendar source` → `FullCalendar<Entry> source` |
| `EntryTimeChangedEvent.java` | `FullCalendar source` → `FullCalendar<Entry> source` |
| `EntryDroppedEvent.java` | `FullCalendar source` → `FullCalendar<Entry> source` |
| `EntryResizedEvent.java` | `FullCalendar source` → `FullCalendar<Entry> source` |
| `EntryMouseEnterEvent.java` | `FullCalendar source` → `FullCalendar<Entry> source` |
| `EntryMouseLeaveEvent.java` | `FullCalendar source` → `FullCalendar<Entry> source` |

### Non-Entry Events — Use `FullCalendar<T>`

Events not related to items use the generic type from the calendar they belong to.
Since these events don't care about the item type, use `FullCalendar` with a wildcard
or raw type. The simplest approach: keep them with the generic `T` from the source:

| File | Constructor Change |
|------|-------------------|
| `TimeslotClickedEvent.java` | `FullCalendar source` → `FullCalendar<?> source` |
| `TimeslotsSelectedEvent.java` | `FullCalendar source` → `FullCalendar<?> source` |
| `DatesRenderedEvent.java` | `FullCalendar source` → `FullCalendar<?> source` |
| `ViewSkeletonRenderedEvent.java` | `FullCalendar source` → `FullCalendar<?> source` |
| `DayNumberClickedEvent.java` | `FullCalendar source` → `FullCalendar<?> source` |
| `WeekNumberClickedEvent.java` | `FullCalendar source` → `FullCalendar<?> source` |
| `MoreLinkClickedEvent.java` | `FullCalendar source` → `FullCalendar<?> source` |
| `BrowserTimezoneObtainedEvent.java` | `FullCalendar source` → `FullCalendar<?> source` |

These extend `ComponentEvent<FullCalendar<?>>`. The `?` wildcard means `getSource()` returns
`FullCalendar<?>` — the user can cast if they need the typed calendar, but typically these
events don't need item-type access.

**Alternative:** If `FullCalendar<?>` causes issues with Vaadin's event bus, these can stay
raw or use an unbounded type. Evaluate during implementation.

---

## Part B: New CIP Event Hierarchy

### CalendarItemEvent<T>

Base class for all CIP item events:

```java
public abstract class CalendarItemEvent<T> extends ComponentEvent<FullCalendar<T>> {
    private final T item;
    private final String itemId;

    protected CalendarItemEvent(FullCalendar<T> source, boolean fromClient, String itemId) {
        super(source, fromClient);
        this.itemId = itemId;
        this.item = source.<T>getCachedItemFromFetch(itemId)
            .orElseThrow(() -> new IllegalArgumentException(
                "No cached item for id: " + itemId));
    }

    public T getItem() { return item; }
    public String getItemId() { return itemId; }
}
```

### CalendarItemDataEvent<T>

Extends `CalendarItemEvent<T>` with the JSON delta from client:

```java
public abstract class CalendarItemDataEvent<T> extends CalendarItemEvent<T> {
    private final ObjectNode jsonDelta;

    protected CalendarItemDataEvent(FullCalendar<T> source, boolean fromClient,
                                     ObjectNode jsonData) {
        super(source, fromClient, jsonData.get("id").asString());
        this.jsonDelta = jsonData;
    }

    public ObjectNode getChangesAsJson() { return jsonDelta; }
    public CalendarItemChanges getChanges() { return new CalendarItemChanges(jsonDelta); }

    /**
     * Applies changes to the item using the configured update strategy.
     * Strategy A: mapper setters (if registered)
     * Strategy B: CalendarItemUpdateHandler (if set)
     * Throws if neither is configured.
     */
    public T applyChangesOnItem() {
        T item = getItem();
        getSource().applyCalendarItemChanges(item, jsonDelta);
        return item;
    }
}
```

### Concrete CIP Events

```java
// NOT @DomEvent — fired programmatically from dispatch (see Part C)
public class CalendarItemClickedEvent<T> extends CalendarItemDataEvent<T> {
    public CalendarItemClickedEvent(FullCalendar<T> source, boolean fromClient,
                                     ObjectNode itemData) {
        super(source, fromClient, itemData);
    }
}

public class CalendarItemDroppedEvent<T> extends CalendarItemDataEvent<T> {
    private final ObjectNode timeDelta;

    public CalendarItemDroppedEvent(FullCalendar<T> source, boolean fromClient,
                                     ObjectNode itemData, ObjectNode timeDelta) {
        super(source, fromClient, itemData);
        this.timeDelta = timeDelta;
    }

    public ObjectNode getTimeDelta() { return timeDelta; }
}

public class CalendarItemResizedEvent<T> extends CalendarItemDataEvent<T> {
    private final ObjectNode durationDelta;

    public CalendarItemResizedEvent(FullCalendar<T> source, boolean fromClient,
                                     ObjectNode itemData, ObjectNode durationDelta) {
        super(source, fromClient, itemData);
        this.durationDelta = durationDelta;
    }

    public ObjectNode getDurationDelta() { return durationDelta; }
}
```

Optional: `CalendarItemMouseEnterEvent<T>`, `CalendarItemMouseLeaveEvent<T>` if hover
events are desired for CIP items.

---

## Part C: Event Dispatch

CIP events are NOT `@DomEvent`-annotated. They are fired programmatically via direct DOM
event listeners registered on the element.

**Why not @DomEvent?** Because the `@DomEvent` Entry events (e.g., `EntryClickedEvent`) are
always registered on the component class. Their constructors call
`source.getCachedEntryFromFetch()` which fails for POJOs. We cannot conditionally disable
`@DomEvent` annotations. Instead, CIP events are fired from separate element-level listeners.

**Implementation in FullCalendar<T>:**

```java
private void setupCalendarItemEventListeners() {
    // eventClick → CalendarItemClickedEvent
    getElement().addEventListener("eventClick", domEvent -> {
        if (usingCalendarItemProvider) {
            ObjectNode data = extractObjectNode(domEvent, "event.detail.data");
            fireEvent(new CalendarItemClickedEvent<>(this, true, data));
        }
    }).addEventData("event.detail.data");

    // eventDrop → CalendarItemDroppedEvent
    getElement().addEventListener("eventDrop", domEvent -> {
        if (usingCalendarItemProvider) {
            ObjectNode data = extractObjectNode(domEvent, "event.detail.data");
            ObjectNode delta = extractObjectNode(domEvent, "event.detail.delta");
            fireEvent(new CalendarItemDroppedEvent<>(this, true, data, delta));
        }
    }).addEventData("event.detail.data").addEventData("event.detail.delta");

    // eventResize → CalendarItemResizedEvent
    getElement().addEventListener("eventResize", domEvent -> {
        if (usingCalendarItemProvider) {
            ObjectNode data = extractObjectNode(domEvent, "event.detail.data");
            ObjectNode delta = extractObjectNode(domEvent, "event.detail.delta");
            fireEvent(new CalendarItemResizedEvent<>(this, true, data, delta));
        }
    }).addEventData("event.detail.data").addEventData("event.detail.delta");

    // ... similar for eventMouseEnter, eventMouseLeave if needed
}
```

When `usingCalendarItemProvider == false`, these listeners do nothing and the existing
`@DomEvent`-annotated Entry events fire as usual.

**Call this method** from the FullCalendar constructor or `onAttach()`.

---

## Part D: Listener Registration on FullCalendar<T>

```java
// In FullCalendar<T>:

@SuppressWarnings("unchecked")
public Registration addCalendarItemClickedListener(
        ComponentEventListener<CalendarItemClickedEvent<T>> listener) {
    return addListener((Class) CalendarItemClickedEvent.class, listener);
}

@SuppressWarnings("unchecked")
public Registration addCalendarItemDroppedListener(
        ComponentEventListener<CalendarItemDroppedEvent<T>> listener) {
    return addListener((Class) CalendarItemDroppedEvent.class, listener);
}

@SuppressWarnings("unchecked")
public Registration addCalendarItemResizedListener(
        ComponentEventListener<CalendarItemResizedEvent<T>> listener) {
    return addListener((Class) CalendarItemResizedEvent.class, listener);
}
```

**Javadoc on existing `addEntryClickedListener()` etc.:**
```java
/**
 * Registers a listener for entry click events.
 * @apiNote When using CalendarItemProvider instead of EntryProvider, use
 *          {@link #addCalendarItemClickedListener} instead. Using this method
 *          with CalendarItemProvider will cause IllegalArgumentException at runtime.
 */
public Registration addEntryClickedListener(...) { ... }
```

---

## Testing

### Entry event typing
- `EntryClickedEvent.getSource()` returns `FullCalendar<Entry>` (compile-time check)
- All existing entry event tests pass unchanged
- `@DomEvent` reflective instantiation works (test via `ComponentEventBusUtil.getEventConstructor`)

### CIP events
- `CalendarItemClickedEvent` fires when CIP active and item clicked
- `CalendarItemDroppedEvent` fires on drag-drop with correct POJO + delta
- `CalendarItemResizedEvent` fires on resize with correct POJO + delta
- `event.getItem()` returns the cached POJO
- `event.applyChangesOnItem()` calls Strategy A or B correctly
- `event.getChanges().getChangedStart()` returns correct parsed value

### Dispatch
- Entry events fire when EntryProvider active, CIP events do NOT fire
- CIP events fire when CalendarItemProvider active, Entry events do NOT fire
- Non-entry events (TimeslotClickedEvent, DatesRenderedEvent) fire in both modes
- Registering both CIP and Entry listeners: no interference between them

### Non-entry events
- `TimeslotClickedEvent.getSource()` returns `FullCalendar<?>` (compile-time)
- All existing timeslot/date event tests pass

## Completion Criteria

- All existing events typed with `FullCalendar<Entry>` (no raw types)
- CIP events fire correctly with typed POJOs
- Event dispatch: clean separation between Entry and CIP paths
- Update strategies (A xor B) work through `applyChangesOnItem()`
- Non-entry events work in both Entry and CIP modes
