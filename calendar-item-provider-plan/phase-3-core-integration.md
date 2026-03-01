# Phase 3: Core Integration

**Goal:** Make `FullCalendar` generic and connect CIP to the component.
**Prerequisite:** Phase 2 complete, Spike verification 0.2 passed.
**Breaking changes:** Raw type warnings for existing users (not compile errors).

---

## FullCalendar becomes `FullCalendar<T>`

```java
// Before:
public class FullCalendar extends Component implements HasStyle, HasSize, HasTheme

// After:
public class FullCalendar<T> extends Component implements HasStyle, HasSize, HasTheme
```

Existing user code `FullCalendar calendar = new FullCalendar()` becomes a raw type warning.
No compile errors. All existing methods continue to work.

---

## Changes to FullCalendar.java

### 1. Fields

```java
public class FullCalendar<T> extends Component implements HasStyle, HasSize, HasTheme {
    // EXISTING (type-updated):
    private EntryProvider<? extends Entry> entryProvider;

    // NEW:
    private CalendarItemProvider<T> calendarItemProvider;
    private CalendarItemPropertyMapper<T> calendarItemPropertyMapper;
    private CalendarItemUpdateHandler<T> calendarItemUpdateHandler;

    // Internal dispatch flag:
    private boolean usingCalendarItemProvider = false;

    // Cache: widened from Map<String, Entry> to Map<String, Object>
    private final Map<String, Object> lastFetchedItems = createBoundedItemCache();
}
```

### 2. New Public API — `setCalendarItemProvider()`

```java
public void setCalendarItemProvider(
        CalendarItemProvider<T> provider,
        CalendarItemPropertyMapper<T> mapper) {
    Objects.requireNonNull(provider);
    Objects.requireNonNull(mapper);
    mapper.validate();  // Fail fast if ID mapping missing

    // Mutual exclusion check: setters vs update handler
    if (mapper.hasSetters() && calendarItemUpdateHandler != null) {
        throw new IllegalStateException(
            "Cannot use both mapper setters and CalendarItemUpdateHandler. "
            + "Remove setters from the mapper or remove the update handler.");
    }

    this.calendarItemProvider = provider;
    this.calendarItemPropertyMapper = mapper;
    this.usingCalendarItemProvider = true;

    // Clear old entry provider
    this.entryProvider = null;
    clearEntryProviderListeners();

    // Register CIP listeners
    registerCalendarItemProviderListeners(provider);

    // Trigger refresh
    requestRefreshAllEntries();
}
```

### 3. New Public API — `setCalendarItemUpdateHandler()`

```java
public void setCalendarItemUpdateHandler(CalendarItemUpdateHandler<T> handler) {
    // Mutual exclusion check
    if (calendarItemPropertyMapper != null && calendarItemPropertyMapper.hasSetters()) {
        throw new IllegalStateException(
            "Cannot use both mapper setters and CalendarItemUpdateHandler. "
            + "Remove setters from the mapper or do not set an update handler.");
    }
    this.calendarItemUpdateHandler = handler;
}
```

### 4. Refactor Internal Cache

```java
// Backward-compatible accessor (existing, for Entry-based events):
@SuppressWarnings("unchecked")
public Optional<Entry> getCachedEntryFromFetch(String id) {
    Object item = lastFetchedItems.get(id);
    return item instanceof Entry ? Optional.of((Entry) item) : Optional.empty();
}

// New typed accessor (for CIP events):
@SuppressWarnings("unchecked")
public Optional<T> getCachedItemFromFetch(String id) {
    return Optional.ofNullable((T) lastFetchedItems.get(id));
}
```

### 5. Refactor `fetchEntriesFromServer()`

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

// Entry path (existing logic, extracted to method):
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

// CIP path (new):
private void fetchCalendarItems(LocalDateTime start, LocalDateTime end, ArrayNode array) {
    calendarItemProvider.fetch(new CalendarQuery(start, end))
        .peek(item -> lastFetchedItems.put(calendarItemPropertyMapper.getId(item), item))
        .map(calendarItemPropertyMapper::toJson)
        .forEach(array::add);
}
```

### 6. Internal Update Dispatch

Used by CIP events when client reports changes (drag/drop/resize):

```java
/**
 * Applies client-side changes to a CIP item using the configured strategy.
 * Called from CalendarItemDataEvent.applyChangesOnItem().
 */
void applyCalendarItemChanges(T item, ObjectNode jsonDelta) {
    if (calendarItemUpdateHandler != null) {
        // Strategy B: explicit handler
        calendarItemUpdateHandler.handleUpdate(item, new CalendarItemChanges(jsonDelta));
    } else if (calendarItemPropertyMapper.hasSetters()) {
        // Strategy A: mapper setters
        calendarItemPropertyMapper.applyChanges(item, jsonDelta);
    } else {
        throw new IllegalStateException(
            "No update strategy configured. Register setters on the mapper "
            + "or set a CalendarItemUpdateHandler.");
    }
}
```

### 7. Deprecate Old Methods

```java
/** @deprecated Use {@link #setCalendarItemProvider} for custom POJOs,
  * or continue using this method for Entry-based calendars. */
@Deprecated(since = "7.2", forRemoval = false)
public void setEntryProvider(EntryProvider<? extends Entry> entryProvider) {
    this.usingCalendarItemProvider = false;
    this.calendarItemProvider = null;
    this.calendarItemPropertyMapper = null;
    this.calendarItemUpdateHandler = null;
    // ... existing logic unchanged
}
```

---

## FullCalendarBuilder Changes

```java
public class FullCalendarBuilder<T> {
    // NEW:
    private CalendarItemProvider<T> calendarItemProvider;
    private CalendarItemPropertyMapper<T> calendarItemPropertyMapper;
    private CalendarItemUpdateHandler<T> calendarItemUpdateHandler;

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

    // In build(): if CIP is set, call calendar.setCalendarItemProvider()
    // then optionally calendar.setCalendarItemUpdateHandler()
}
```

---

## Testing

- Test `setCalendarItemProvider()` with a test POJO and verify `fetchEntriesFromServer` produces correct JSON
- Test switching between EntryProvider and CalendarItemProvider
- Test cache stores POJOs correctly
- Test `getCachedEntryFromFetch()` returns empty when CIP is active
- Test `getCachedItemFromFetch()` returns POJOs
- Test mutual exclusion: setters + update handler throws
- Test `applyCalendarItemChanges()` with Strategy A (setters)
- Test `applyCalendarItemChanges()` with Strategy B (update handler)
- Test `applyCalendarItemChanges()` with neither throws
- Run ALL existing tests — entry provider path must be unaffected
- Integration test: calendar with CIP renders items correctly in browser

## Completion Criteria

- CIP can be set on `FullCalendar<T>` and items render in the browser
- Client changes dispatch to the correct update strategy (A xor B)
- EntryProvider path works exactly as before
- No compile errors in addon-scheduler (it still uses Entry)
