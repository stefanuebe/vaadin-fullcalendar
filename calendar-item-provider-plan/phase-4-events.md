# Phase 4: Event System — Typed Parallel Hierarchy

**Goal:** Create a fully typed parallel event hierarchy for CIP alongside the existing Entry events.
**Prerequisite:** Phase 3 complete.
**Breaking changes:** None. Existing Entry events stay as-is with `FullCalendar<Entry>` typing.

---

## Existing Entry Events — Update to `FullCalendar<Entry>`

All existing entry events update their `ComponentEvent` type parameter from raw `FullCalendar`
to `FullCalendar<Entry>`. This is source-compatible since the erased type is identical.

```java
// Before:
public abstract class EntryEvent extends ComponentEvent<FullCalendar> {
    public EntryEvent(FullCalendar source, boolean fromClient, String entryId) { ... }
}

// After:
public abstract class EntryEvent extends ComponentEvent<FullCalendar<Entry>> {
    public EntryEvent(FullCalendar<Entry> source, boolean fromClient, String entryId) { ... }
}
```

Same for all @DomEvent-annotated subclasses — constructor parameter becomes `FullCalendar<Entry>`.
At runtime this erases identically, so Vaadin's reflective event instantiation is unaffected.

Non-entry events (TimeslotClickedEvent, DatesRenderedEvent, etc.) use `FullCalendar<?>` since
they are not tied to a specific item type.

---

## New CIP Event Hierarchy

```
ComponentEvent<FullCalendar<T>>
└── CalendarItemEvent<T>
    └── CalendarItemDataEvent<T>
        ├── CalendarItemClickedEvent<T>
        └── CalendarItemChangedEvent<T>
            ├── CalendarItemDroppedEvent<T>
            └── CalendarItemResizedEvent<T>
```

### CalendarItemEvent<T>

```java
public abstract class CalendarItemEvent<T> extends ComponentEvent<FullCalendar<T>> {
    private final T item;
    private final String itemId;

    protected CalendarItemEvent(FullCalendar<T> source, boolean fromClient, String itemId) {
        super(source, fromClient);
        this.itemId = itemId;
        this.item = source.getCachedItemFromFetch(itemId)
            .orElseThrow(() -> new IllegalArgumentException("No cached item for id: " + itemId));
    }

    public T getItem() { return item; }
    public String getItemId() { return itemId; }
}
```

### CalendarItemDataEvent<T>

```java
public abstract class CalendarItemDataEvent<T> extends CalendarItemEvent<T> {
    private final ObjectNode jsonDelta;

    protected CalendarItemDataEvent(FullCalendar<T> source, boolean fromClient,
                                     ObjectNode jsonData) {
        super(source, fromClient, jsonData.get("id").asString());
        this.jsonDelta = jsonData;
    }

    /** Raw JSON delta from client. */
    public ObjectNode getChangesAsJson() { return jsonDelta; }

    /** Typed access to changed properties. */
    public CalendarItemChanges getChanges() { return new CalendarItemChanges(jsonDelta); }

    /**
     * Applies changes to the item using the configured update strategy (mapper setters
     * or update handler). Mirrors Entry's applyChangesOnEntry().
     * @return the modified item
     */
    public T applyChangesOnItem() {
        T item = getItem();
        getSource().applyCalendarItemChanges(item, jsonDelta);
        return item;
    }
}
```

### CalendarItemClickedEvent<T>

```java
public class CalendarItemClickedEvent<T> extends CalendarItemDataEvent<T> {
    // NOT annotated with @DomEvent — fired programmatically (see dispatch below)
    public CalendarItemClickedEvent(FullCalendar<T> source, boolean fromClient,
                                     ObjectNode itemData) {
        super(source, fromClient, itemData);
    }
}
```

### CalendarItemDroppedEvent<T>, CalendarItemResizedEvent<T>

Same pattern as CalendarItemClickedEvent — no @DomEvent, fired programmatically.
Include additional delta data (time delta for drop, duration delta for resize).

---

## Event Dispatch

CIP events are NOT @DomEvent-annotated. Instead, FullCalendar intercepts the raw DOM events
and fires the appropriate event type based on whether CIP or EntryProvider is active.

Two approaches (choose during implementation):

**Approach A — Bridge from existing @DomEvent events:**

```java
// In FullCalendar constructor or init:
private void setupCalendarItemEventBridge() {
    // Internal entry click listener — always registered
    addEntryClickedListener(entryClickedEvent -> {
        if (usingCalendarItemProvider) {
            fireEvent(new CalendarItemClickedEvent<>(
                this, true, entryClickedEvent.getJsonObject()));
        }
    });
    // ... similar for drop, resize, mouse enter/leave
}
```

Note: This requires that the existing @DomEvent events can fire even when CIP is active.
The `EntryEvent` constructor calls `getCachedEntryFromFetch()` which would fail for POJOs.
So either:
- The bridge must use the raw `@EventData` before the EntryEvent constructor runs, OR
- Use Approach B instead.

**Approach B — Direct element event listeners (recommended):**

```java
// In FullCalendar, register raw DOM event listeners alongside @DomEvent:
private void setupCalendarItemEventListeners() {
    getElement().addEventListener("eventClick", domEvent -> {
        if (usingCalendarItemProvider) {
            ObjectNode data = extractObjectNode(domEvent, "event.detail.data");
            fireEvent(new CalendarItemClickedEvent<>(this, true, data));
        }
        // Entry path: handled by existing @DomEvent EntryClickedEvent automatically
    });
    // ... similar for eventDrop, eventResize, eventMouseEnter, eventMouseLeave
}
```

This avoids the EntryEvent constructor issue entirely. When CIP is active, the raw DOM
listener fires the CIP event. When EntryProvider is active, the @DomEvent-annotated
EntryClickedEvent fires as usual.

---

## New Listener Registration Methods on FullCalendar<T>

```java
@SuppressWarnings("unchecked")
public Registration addCalendarItemClickedListener(
        ComponentEventListener<CalendarItemClickedEvent<T>> listener) {
    return addListener((Class) CalendarItemClickedEvent.class, listener);
}

public Registration addCalendarItemDroppedListener(
        ComponentEventListener<CalendarItemDroppedEvent<T>> listener) { ... }

public Registration addCalendarItemResizedListener(
        ComponentEventListener<CalendarItemResizedEvent<T>> listener) { ... }
```

---

## Example Usage

```java
FullCalendar<MyPojo> calendar = FullCalendarBuilder.create(MyPojo.class)
    .withCalendarItemProvider(provider, mapper)
    .build();

// CIP event — fully typed, returns MyPojo
calendar.addCalendarItemClickedListener(event -> {
    MyPojo clicked = event.getItem();   // typed!
    Notification.show("Clicked: " + clicked.getName());
});

// CIP drop event — apply changes via Strategy A or B
calendar.addCalendarItemDroppedListener(event -> {
    MyPojo dropped = event.applyChangesOnItem();  // uses mapper setters or update handler
    myRepo.save(dropped);
});
```

---

## Testing

- Test CIP events fire when CIP is active and items are clicked/dropped/resized
- Test Entry events still fire when using EntryProvider (not CIP)
- Test that CIP events contain the correct typed POJO from cache
- Test `applyChangesOnItem()` delegates to mapper setters (Strategy A)
- Test `applyChangesOnItem()` delegates to update handler (Strategy B)
- Test `getChanges()` returns correctly typed CalendarItemChanges
- Test that registering both CIP and Entry listeners doesn't cause interference
- Test non-entry events (TimeslotClickedEvent, DatesRenderedEvent) work with both modes

## Completion Criteria

- Users can listen for CIP events and receive their typed POJOs
- Entry events are unaffected and use `FullCalendar<Entry>` typing
- All client interactions (click, drag, resize) produce the correct CIP event
- Update strategies (A xor B) work through the event's `applyChangesOnItem()`
