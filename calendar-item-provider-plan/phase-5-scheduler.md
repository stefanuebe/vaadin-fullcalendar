# Phase 5: Scheduler Extension — Generic `FullCalendarScheduler<T>`

**Goal:** Make `FullCalendarScheduler` generic and integrate CIP with resource-based views.
**Prerequisite:** Phase 4 complete.
**Breaking changes:** Raw type warnings for existing `FullCalendarScheduler` users.

---

## Context

The scheduler extension (`addon-scheduler/`) adds resource management to FullCalendar.
Currently it uses `ResourceEntry extends Entry` with `Set<Resource> resources`. Making the
scheduler generic means POJOs can also be used with timeline and resource views.

### Current Scheduler Architecture (Key Points)

**Resource management is item-type-agnostic.** The `Scheduler` interface and `Resource` class
manage resources independently of entries. Methods like `addResources()`, `getResources()`,
`removeResources()` work with `Resource` objects, not entries. This part needs no generic changes.

**Entry coupling exists in 3 places:**
1. `removeFromEntries()` in `FullCalendarScheduler.java:215` — does `instanceof ResourceEntry`
   check to remove resource associations from entries
2. `EntryDroppedSchedulerEvent.applyChangesOnEntry()` — casts to `ResourceEntry`
3. `ResourceConverter` — hardcoded for `Set<Resource>, ResourceEntry`

**Scheduler-specific events** (`TimeslotClickedSchedulerEvent`, `TimeslotsSelectedSchedulerEvent`)
only add a `Resource` field — they don't reference Entry at all.

## Module

`addon-scheduler/src/main/java/org/vaadin/stefan/fullcalendar/`

---

## Changes

### 1. `FullCalendarScheduler<T> extends FullCalendar<T>`

```java
// Before:
public class FullCalendarScheduler extends FullCalendar implements Scheduler

// After:
public class FullCalendarScheduler<T> extends FullCalendar<T> implements Scheduler
```

Existing code `FullCalendarScheduler scheduler = ...` becomes raw type warning.
For Entry-based usage: `FullCalendarScheduler<ResourceEntry>` or `FullCalendarScheduler<Entry>`.
For CIP usage: `FullCalendarScheduler<Meeting>`.

### 2. `Scheduler` Interface — No Generic Parameter

The `Scheduler` interface manages resources, not entries. It stays non-generic:

```java
// Stays as-is:
public interface Scheduler {
    void addResources(Iterable<Resource>, boolean);
    void removeResources(Iterable<Resource>);
    Set<Resource> getResources();
    Optional<Resource> getResourceById(String id);
    // ... all resource management methods unchanged
}
```

### 3. `removeFromEntries()` — Dual Path

Currently hardcoded to `ResourceEntry`:

```java
// Before (line 215-225):
private void removeFromEntries(Iterable<Resource> resources) {
    EntryProvider<Entry> entryProvider = getEntryProvider();
    if (entryProvider.isInMemory()) {
        entryProvider.asInMemory().getEntries().stream()
            .filter(e -> e instanceof ResourceEntry)
            .forEach(e -> ((ResourceEntry) e).removeResources(resources));
    }
}

// After — dispatch based on active provider:
private void removeFromEntries(Iterable<Resource> iterableResources) {
    List<Resource> resources = StreamSupport.stream(
        iterableResources.spliterator(), false).toList();

    if (isUsingCalendarItemProvider()) {
        // CIP path: resource associations are managed by the POJO, not by us.
        // The user is responsible for updating resource associations.
        // We only remove from the client-side.
        return;
    }

    // Entry path: existing behavior
    EntryProvider<? extends Entry> entryProvider = getEntryProvider();
    if (entryProvider.isInMemory()) {
        entryProvider.asInMemory().getEntries().stream()
            .filter(e -> e instanceof ResourceEntry)
            .forEach(e -> ((ResourceEntry) e).removeResources(resources));
    }
}
```

### 4. `SchedulerCalendarItemPropertyMapper<T>`

Extends the base mapper with resource-related mappings:

```java
package org.vaadin.stefan.fullcalendar;

public class SchedulerCalendarItemPropertyMapper<T> extends CalendarItemPropertyMapper<T> {

    // Read mapping
    private ValueProvider<T, Set<String>> resourceIdsProvider;
    private ValueProvider<T, Boolean> resourceEditableProvider;

    // Write mapping (for drag between resources)
    private SerializableBiConsumer<T, Set<String>> resourceIdsSetter;

    // Builder API
    public static <T> SchedulerCalendarItemPropertyMapper<T> of(Class<T> type) { ... }

    public SchedulerCalendarItemPropertyMapper<T> resourceIds(
            ValueProvider<T, Set<String>> getter) {
        this.resourceIdsProvider = getter;
        return this;
    }

    public SchedulerCalendarItemPropertyMapper<T> resourceIds(
            ValueProvider<T, Set<String>> getter,
            SerializableBiConsumer<T, Set<String>> setter) {
        this.resourceIdsProvider = getter;
        this.resourceIdsSetter = setter;
        this.hasSetters = true;  // inherited flag
        return this;
    }

    public SchedulerCalendarItemPropertyMapper<T> resourceEditable(
            ValueProvider<T, Boolean> getter) {
        this.resourceEditableProvider = getter;
        return this;
    }

    @Override
    public ObjectNode toJson(T item) {
        ObjectNode node = super.toJson(item);
        if (resourceIdsProvider != null) {
            Set<String> ids = resourceIdsProvider.apply(item);
            if (ids != null && !ids.isEmpty()) {
                ArrayNode arr = node.putArray("resourceIds");
                ids.forEach(arr::add);
            }
        }
        if (resourceEditableProvider != null) {
            Boolean val = resourceEditableProvider.apply(item);
            if (val != null) {
                node.put("resourceEditable", val);
            }
        }
        return node;
    }

    @Override
    public void applyChanges(T item, ObjectNode changes) {
        super.applyChanges(item, changes);
        if (changes.has("resourceIds") && resourceIdsSetter != null) {
            Set<String> newIds = new LinkedHashSet<>();
            changes.get("resourceIds").forEach(n -> newIds.add(n.asString()));
            resourceIdsSetter.accept(item, newIds);
        }
    }
}
```

### 5. `SchedulerCalendarItemChanges extends CalendarItemChanges`

```java
package org.vaadin.stefan.fullcalendar;

public class SchedulerCalendarItemChanges extends CalendarItemChanges {
    public SchedulerCalendarItemChanges(ObjectNode jsonDelta) { super(jsonDelta); }

    public Optional<String> getOldResourceId() {
        return Optional.ofNullable(getRawJson().get("oldResource"))
            .filter(JsonNode::isString)
            .map(JsonNode::asString);
    }

    public Optional<String> getNewResourceId() {
        return Optional.ofNullable(getRawJson().get("newResource"))
            .filter(JsonNode::isString)
            .map(JsonNode::asString);
    }

    // Convenience: resolve Resource objects from calendar
    public Optional<Resource> getOldResource(Scheduler scheduler) {
        return getOldResourceId().flatMap(scheduler::getResourceById);
    }

    public Optional<Resource> getNewResource(Scheduler scheduler) {
        return getNewResourceId().flatMap(scheduler::getResourceById);
    }
}
```

### 6. Scheduler-Specific CIP Events

#### CalendarItemDroppedSchedulerEvent<T>

```java
public class CalendarItemDroppedSchedulerEvent<T> extends CalendarItemDroppedEvent<T> {
    private final Resource oldResource;
    private final Resource newResource;

    public CalendarItemDroppedSchedulerEvent(FullCalendarScheduler<T> source,
            boolean fromClient, ObjectNode itemData, ObjectNode timeDelta) {
        super(source, fromClient, itemData, timeDelta);
        Scheduler scheduler = (Scheduler) source;
        this.oldResource = Optional.ofNullable(itemData.get("oldResource"))
            .filter(JsonNode::isString)
            .flatMap(n -> scheduler.getResourceById(n.asString()))
            .orElse(null);
        this.newResource = Optional.ofNullable(itemData.get("newResource"))
            .filter(JsonNode::isString)
            .flatMap(n -> scheduler.getResourceById(n.asString()))
            .orElse(null);
    }

    public Optional<Resource> getOldResource() { return Optional.ofNullable(oldResource); }
    public Optional<Resource> getNewResource() { return Optional.ofNullable(newResource); }
}
```

### 7. Existing Scheduler Events — Type Updates

Same pattern as Phase 4 for base events:

| Event | Before | After |
|-------|--------|-------|
| `EntryDroppedSchedulerEvent` | `extends EntryTimeChangedEvent` with `FullCalendarScheduler source` | `FullCalendarScheduler<Entry> source` |
| `TimeslotClickedSchedulerEvent` | `extends TimeslotClickedEvent` with `FullCalendarScheduler source` | `FullCalendarScheduler<?> source` |
| `TimeslotsSelectedSchedulerEvent` | `extends TimeslotsSelectedEvent` with `FullCalendarScheduler source` | `FullCalendarScheduler<?> source` |

### 8. Scheduler Event Dispatch

`FullCalendarScheduler<T>` overrides the CIP event dispatch to fire scheduler-specific events:

```java
// In FullCalendarScheduler<T>:
@Override
protected void setupCalendarItemEventListeners() {
    super.setupCalendarItemEventListeners();

    // Override eventDrop to fire CalendarItemDroppedSchedulerEvent instead
    getElement().addEventListener("eventDrop", domEvent -> {
        if (isUsingCalendarItemProvider()) {
            ObjectNode data = extractObjectNode(domEvent, "event.detail.data");
            ObjectNode delta = extractObjectNode(domEvent, "event.detail.delta");
            fireEvent(new CalendarItemDroppedSchedulerEvent<>(this, true, data, delta));
        }
    }).addEventData("event.detail.data").addEventData("event.detail.delta");
}
```

### 9. Listener Registration on Scheduler

```java
// In FullCalendarScheduler<T>:
@SuppressWarnings("unchecked")
public Registration addCalendarItemDroppedSchedulerListener(
        ComponentEventListener<CalendarItemDroppedSchedulerEvent<T>> listener) {
    return addListener((Class) CalendarItemDroppedSchedulerEvent.class, listener);
}
```

### 10. Builder — Scheduler + CIP Combo

The builder already supports scheduler (via `withScheduler()`). CIP is additive:

```java
FullCalendar<Meeting> calendar = FullCalendarBuilder.create(Meeting.class)
    .withScheduler("license-key")
    .withCalendarItemProvider(provider, mapper)
    .build();
```

The `build()` method uses reflection to create `FullCalendarScheduler` when `withScheduler()`
was called. The generic type propagates through the builder.

---

## Example Usage

```java
public class Meeting {
    private Long id;
    private String subject;
    private LocalDateTime start;
    private LocalDateTime end;
    private String roomId;
    // getters + setters
}

var mapper = SchedulerCalendarItemPropertyMapper.of(Meeting.class)
    .id(m -> String.valueOf(m.getId()))
    .title(Meeting::getSubject)
    .start(Meeting::getStart, Meeting::setStart)
    .end(Meeting::getEnd, Meeting::setEnd)
    .resourceIds(m -> Set.of(m.getRoomId()),
                 (m, ids) -> m.setRoomId(ids.iterator().next()));

var provider = CalendarItemProvider.fromCallbacks(
    query -> meetingService.findBetween(query.getStart(), query.getEnd()),
    id -> meetingService.findById(Long.parseLong(id))
);

FullCalendar<Meeting> calendar = FullCalendarBuilder.create(Meeting.class)
    .withScheduler("your-license-key")
    .withCalendarItemProvider(provider, mapper)
    .build();

// Add resources
((Scheduler) calendar).addResources(
    new Resource("room1", "Room 1", "blue"),
    new Resource("room2", "Room 2", "green")
);

// Listen for drops with resource change
calendar.addCalendarItemDroppedListener(event -> {
    Meeting m = event.applyChangesOnItem(); // applies start, end, resourceIds
    meetingService.save(m);
});
```

---

## Testing

### Scheduler typing
- `FullCalendarScheduler<ResourceEntry>` compiles and works with existing Entry-based code
- `FullCalendarScheduler<Meeting>` compiles with CIP
- Raw `FullCalendarScheduler` produces warnings but compiles

### SchedulerCalendarItemPropertyMapper
- `resourceIds` mapping produces correct JSON array
- `resourceEditable` mapping produces correct JSON boolean
- `applyChanges()` updates resource IDs via setter
- Inherits all base mapper behavior (start, end, allDay, etc.)

### Scheduler CIP events
- `CalendarItemDroppedSchedulerEvent` includes old/new resource
- Scheduler CIP events fire when CIP active on scheduler
- Entry-based scheduler events still fire when using ResourceEntry

### removeFromEntries()
- Entry path: still removes resources from ResourceEntry instances
- CIP path: skips (user manages POJO resource associations)

### Builder
- `FullCalendarBuilder.create(Meeting.class).withScheduler("key").withCalendarItemProvider(...)` works
- Existing `FullCalendarBuilder.create().withScheduler("key")` still works (raw type)

### Regression
- ALL existing scheduler tests pass
- ResourceEntry-based calendars work identically

## Completion Criteria

- CIP works with scheduler views (Timeline, ResourceTimeGrid, ResourceDayGrid)
- Resource associations correctly mapped from POJOs via `SchedulerCalendarItemPropertyMapper`
- Resource drag-and-drop works with CIP (via setter or update handler)
- Existing ResourceEntry users unaffected
- All scheduler configuration methods (license, resource area, slots, etc.) work with generic type
