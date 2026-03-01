# Phase 3: Core Integration — Generic `FullCalendar<T>`

**Goal:** Make `FullCalendar` generic and connect CIP to the component.
**Prerequisite:** Phase 2 complete, Spike verification 0.2 passed.
**Breaking changes:** Raw type warnings for existing users (not compile errors).

---

## Context

This is the largest phase. `FullCalendar` becomes `FullCalendar<T>`, gains a
`setCalendarItemProvider()` method alongside the existing `setEntryProvider()`, and the
internal cache/fetch mechanism dispatches based on which provider is active.

Two update strategies exist (A: mapper setters, B: update handler) with mutual exclusion.

## Module

Primary: `addon/src/main/java/org/vaadin/stefan/fullcalendar/FullCalendar.java`
Also: `addon/src/main/java/org/vaadin/stefan/fullcalendar/FullCalendarBuilder.java`

---

## Changes to FullCalendar.java

### 1. Class Signature

```java
// Before:
public class FullCalendar extends Component implements HasStyle, HasSize, HasTheme

// After:
public class FullCalendar<T> extends Component implements HasStyle, HasSize, HasTheme
```

Existing code `FullCalendar calendar = new FullCalendar()` becomes a raw type warning.
No compile errors. All existing methods continue to work identically.

### 2. New Fields

```java
// EXISTING (preserved):
private EntryProvider<? extends Entry> entryProvider;

// NEW:
private CalendarItemProvider<T> calendarItemProvider;
private CalendarItemPropertyMapper<T> calendarItemPropertyMapper;
private CalendarItemUpdateHandler<T> calendarItemUpdateHandler;
private boolean usingCalendarItemProvider = false;
```

### 3. Cache — Widened Type

```java
// Before:
private final Map<String, Entry> lastFetchedEntries = createBoundedEntryCache();

// After:
private final Map<String, Object> lastFetchedItems = createBoundedItemCache();
// Same bounded LRU cache (10,000 max), just with Object values

// Backward-compatible accessor (for Entry events):
public Optional<Entry> getCachedEntryFromFetch(String id) {
    Object item = lastFetchedItems.get(id);
    return item instanceof Entry entry ? Optional.of(entry) : Optional.empty();
}

// New typed accessor (for CIP events):
@SuppressWarnings("unchecked")
public Optional<T> getCachedItemFromFetch(String id) {
    return Optional.ofNullable((T) lastFetchedItems.get(id));
}
```

### 4. `setCalendarItemProvider()` — New Public API

```java
public void setCalendarItemProvider(
        CalendarItemProvider<T> provider,
        CalendarItemPropertyMapper<T> mapper) {
    Objects.requireNonNull(provider, "provider must not be null");
    Objects.requireNonNull(mapper, "mapper must not be null");
    mapper.validate();  // Fail fast if ID mapping missing

    // Mutual exclusion check
    if (mapper.hasSetters() && calendarItemUpdateHandler != null) {
        throw new IllegalStateException(
            "Cannot use both mapper setters and CalendarItemUpdateHandler. "
            + "Remove setters from the mapper or remove the update handler.");
    }

    this.calendarItemProvider = provider;
    this.calendarItemPropertyMapper = mapper;
    this.usingCalendarItemProvider = true;

    // Clear old entry provider state
    this.entryProvider = null;
    clearEntryProviderListeners();

    // Register CIP listeners (refreshAll, refreshItem)
    registerCalendarItemProviderListeners(provider);

    // Trigger client-side refresh
    requestRefreshAllEntries();
}
```

### 5. `setCalendarItemUpdateHandler()` — Strategy B Registration

```java
public void setCalendarItemUpdateHandler(CalendarItemUpdateHandler<T> handler) {
    Objects.requireNonNull(handler);
    if (calendarItemPropertyMapper != null && calendarItemPropertyMapper.hasSetters()) {
        throw new IllegalStateException(
            "Cannot use both mapper setters and CalendarItemUpdateHandler. "
            + "Remove setters from the mapper or do not set an update handler.");
    }
    this.calendarItemUpdateHandler = handler;
}
```

### 6. `fetchEntriesFromServer()` — Dual Path

The `@ClientCallable` method dispatches based on active provider:

```java
@ClientCallable
protected ArrayNode fetchEntriesFromServer(ObjectNode query) {
    LocalDateTime start = parseDateTime(query, "start");
    LocalDateTime end = parseDateTime(query, "end");
    ArrayNode array = JsonNodeFactory.instance.arrayNode();

    if (usingCalendarItemProvider) {
        fetchCalendarItems(start, end, array);
    } else {
        fetchEntries(start, end, array);
    }
    return array;
}

// Entry path — extracted from existing logic, unchanged behavior:
private void fetchEntries(LocalDateTime start, LocalDateTime end, ArrayNode array) {
    entryProvider.fetch(new EntryQuery(start, end, EntryQuery.AllDay.BOTH))
        .peek(entry -> {
            entry.setCalendar(this);
            entry.setKnownToTheClient(true);
            lastFetchedItems.put(entry.getId(), entry);
        })
        .map(Entry::toJson)
        .forEach(array::add);
}

// CIP path — new:
private void fetchCalendarItems(LocalDateTime start, LocalDateTime end, ArrayNode array) {
    calendarItemProvider.fetch(new CalendarQuery(start, end))
        .peek(item -> lastFetchedItems.put(calendarItemPropertyMapper.getId(item), item))
        .map(calendarItemPropertyMapper::toJson)
        .forEach(array::add);
}
```

Note: No `setCalendar()` or `setKnownToTheClient()` for POJOs — that bookkeeping is Entry-
specific. CIP items are tracked purely via the cache.

### 7. Internal Update Dispatch

Called from CIP events when client reports changes (drag/drop/resize):

```java
void applyCalendarItemChanges(T item, ObjectNode jsonDelta) {
    if (calendarItemUpdateHandler != null) {
        // Strategy B: explicit handler
        calendarItemUpdateHandler.handleUpdate(item, new CalendarItemChanges(jsonDelta));
    } else if (calendarItemPropertyMapper != null && calendarItemPropertyMapper.hasSetters()) {
        // Strategy A: mapper setters
        calendarItemPropertyMapper.applyChanges(item, jsonDelta);
    } else {
        throw new IllegalStateException(
            "No update strategy configured. Register setters on the mapper "
            + "or set a CalendarItemUpdateHandler via setCalendarItemUpdateHandler().");
    }
}
```

### 8. `requestRefresh()` — Generic Version

```java
// Existing (for Entry, preserved):
public void requestRefresh(Entry item) {
    // ... existing logic
}

// New (for CIP):
public void requestRefreshItem(T item) {
    if (!usingCalendarItemProvider) {
        throw new IllegalStateException("No CalendarItemProvider set.");
    }
    String id = calendarItemPropertyMapper.getId(item);
    lastFetchedItems.put(id, item);
    getElement().callJsFunction("refreshSingleEvent", calendarItemPropertyMapper.toJson(item));
}
```

### 9. Deprecate `setEntryProvider()`

```java
/**
 * Sets the entry provider for this calendar.
 * @deprecated Use {@link #setCalendarItemProvider} for custom POJOs,
 * or continue using this method for Entry-based calendars.
 */
@Deprecated(since = "7.2", forRemoval = false)
public void setEntryProvider(EntryProvider<? extends Entry> entryProvider) {
    this.usingCalendarItemProvider = false;
    this.calendarItemProvider = null;
    this.calendarItemPropertyMapper = null;
    this.calendarItemUpdateHandler = null;
    // ... existing logic unchanged
}
```

### 10. Accessor for Provider State

```java
public boolean isUsingCalendarItemProvider() { return usingCalendarItemProvider; }

@SuppressWarnings("unchecked")
public <P> CalendarItemProvider<P> getCalendarItemProvider() {
    return (CalendarItemProvider<P>) calendarItemProvider;
}

@SuppressWarnings("unchecked")
public <P> CalendarItemPropertyMapper<P> getCalendarItemPropertyMapper() {
    return (CalendarItemPropertyMapper<P>) calendarItemPropertyMapper;
}
```

---

## Changes to FullCalendarBuilder.java

The builder also becomes generic:

```java
// Before:
public class FullCalendarBuilder { ... }

// After:
public class FullCalendarBuilder<T> { ... }
```

**New methods:**
```java
public FullCalendarBuilder<T> withCalendarItemProvider(
        CalendarItemProvider<T> provider,
        CalendarItemPropertyMapper<T> mapper) {
    this.calendarItemProvider = provider;
    this.calendarItemPropertyMapper = mapper;
    return this;
}

public FullCalendarBuilder<T> withCalendarItemUpdateHandler(
        CalendarItemUpdateHandler<T> handler) {
    this.calendarItemUpdateHandler = handler;
    return this;
}
```

**Typed factory method:**
```java
// Existing (preserved, raw type):
public static FullCalendarBuilder create() { ... }

// New typed factory:
public static <T> FullCalendarBuilder<T> create(Class<T> itemType) { ... }
```

**`build()` method** applies CIP configuration after construction:
```java
public <R extends FullCalendar<T>> R build() {
    R calendar = ... // existing construction logic
    if (calendarItemProvider != null) {
        calendar.setCalendarItemProvider(calendarItemProvider, calendarItemPropertyMapper);
        if (calendarItemUpdateHandler != null) {
            calendar.setCalendarItemUpdateHandler(calendarItemUpdateHandler);
        }
    }
    return calendar;
}
```

---

## Testing

### CIP path
- `setCalendarItemProvider()` with test POJO → `fetchEntriesFromServer()` returns correct JSON
- Cache stores POJOs keyed by mapped ID
- `getCachedItemFromFetch()` returns typed POJO
- `requestRefreshItem()` sends updated JSON to client
- Switching from EntryProvider to CIP clears old state
- Switching from CIP to EntryProvider clears CIP state

### Mutual exclusion
- Setters on mapper + update handler → `IllegalStateException`
- Only setters → `applyCalendarItemChanges()` uses setters
- Only handler → `applyCalendarItemChanges()` calls handler
- Neither setters nor handler → `applyCalendarItemChanges()` throws

### Backward compatibility
- `getCachedEntryFromFetch()` returns empty when CIP is active
- `getCachedEntryFromFetch()` returns Entry when EntryProvider is active
- `setEntryProvider()` works exactly as before
- Raw type `FullCalendar calendar = new FullCalendar()` compiles (warning only)

### Builder
- `FullCalendarBuilder.create(MyPojo.class).withCalendarItemProvider(...)` builds correctly
- `FullCalendarBuilder.create()` (raw) still works for Entry-based calendars

### Regression
- Run ALL existing tests — entry provider path must be unaffected
- No compile errors in addon-scheduler (it still uses Entry/ResourceEntry)

## Completion Criteria

- CIP can be set on `FullCalendar<T>` and items render in the browser
- Client changes dispatch to the correct update strategy (A xor B)
- EntryProvider path works exactly as before
- Builder supports both typed CIP and raw Entry paths
