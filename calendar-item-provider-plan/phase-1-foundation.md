# Phase 1: Foundation Types

**Goal:** Create all new CIP types as standalone additions. Zero changes to existing code.
**Prerequisite:** Phase 0 spikes completed.
**Breaking changes:** None — purely additive.

---

## Context

The CIP feature allows users to provide arbitrary POJOs instead of `Entry` objects as calendar
items. This phase creates the foundational types: the property mapper, the provider interface,
query objects, and change events. None of these touch existing code.

## Module

All classes in `addon/src/main/java/org/vaadin/stefan/fullcalendar/` (core module) unless
noted otherwise. The scheduler mapper extension lives in `addon-scheduler/`.

---

## New Classes

### 1. `CalendarItemPropertyMapper<T>`

Maps POJO properties to FullCalendar event properties. Supports both read (server→client via
getters) and write (client→server via optional setters).

**Package:** `org.vaadin.stefan.fullcalendar`

```java
public class CalendarItemPropertyMapper<T> {

    // ─── REQUIRED mapping ───
    private ValueProvider<T, String> idProvider;                  // REQUIRED, validated

    // ─── Read-only mappings (server → client) ───
    private ValueProvider<T, String> titleProvider;
    private ValueProvider<T, ?> startProvider;                    // LocalDateTime, Instant, etc.
    private ValueProvider<T, ?> endProvider;
    private ValueProvider<T, Boolean> allDayProvider;
    private ValueProvider<T, String> groupIdProvider;
    private ValueProvider<T, String> colorProvider;
    private ValueProvider<T, String> backgroundColorProvider;
    private ValueProvider<T, String> borderColorProvider;
    private ValueProvider<T, String> textColorProvider;
    private ValueProvider<T, Boolean> editableProvider;
    private ValueProvider<T, Boolean> startEditableProvider;
    private ValueProvider<T, Boolean> durationEditableProvider;
    private ValueProvider<T, DisplayMode> displayModeProvider;
    private ValueProvider<T, String> constraintProvider;
    private ValueProvider<T, Boolean> overlapProvider;
    private ValueProvider<T, Set<String>> classNamesProvider;
    private ValueProvider<T, Map<String, Object>> customPropertiesProvider;

    // ─── Recurrence mappings (read-only) ───
    private ValueProvider<T, LocalDate> recurringStartDateProvider;
    private ValueProvider<T, LocalDate> recurringEndDateProvider;
    private ValueProvider<T, RecurringTime> recurringStartTimeProvider;
    private ValueProvider<T, RecurringTime> recurringEndTimeProvider;
    private ValueProvider<T, Set<DayOfWeek>> recurringDaysOfWeekProvider;

    // ─── Optional write mappings (client → server) ───
    // Only for properties the client can change (@JsonUpdateAllowed in Entry):
    private SerializableBiConsumer<T, LocalDateTime> startSetter;
    private SerializableBiConsumer<T, LocalDateTime> endSetter;
    private SerializableBiConsumer<T, Boolean> allDaySetter;

    // ─── State ───
    private boolean hasSetters;  // true if any setter is registered
}
```

**Builder-style API:**
```java
// Factory
public static <T> CalendarItemPropertyMapper<T> of(Class<T> type) { ... }

// Read-only mapping (single arg — for all properties)
public CalendarItemPropertyMapper<T> id(ValueProvider<T, String> provider) { ... }
public CalendarItemPropertyMapper<T> title(ValueProvider<T, String> provider) { ... }
public CalendarItemPropertyMapper<T> color(ValueProvider<T, String> provider) { ... }
// ... etc for all properties

// Bidirectional mapping (getter + setter — only for start, end, allDay)
public CalendarItemPropertyMapper<T> start(ValueProvider<T, ?> getter,
                                            SerializableBiConsumer<T, LocalDateTime> setter) { ... }
public CalendarItemPropertyMapper<T> end(ValueProvider<T, ?> getter,
                                          SerializableBiConsumer<T, LocalDateTime> setter) { ... }
public CalendarItemPropertyMapper<T> allDay(ValueProvider<T, Boolean> getter,
                                             SerializableBiConsumer<T, Boolean> setter) { ... }

// String-based mapping (reflection via BeanProperties.read())
public CalendarItemPropertyMapper<T> id(String propertyName) { ... }
public CalendarItemPropertyMapper<T> title(String propertyName) { ... }
// ... etc

// Core methods
public ObjectNode toJson(T item) { ... }      // Serialize POJO to FC JSON
public String getId(T item) { ... }           // Extract ID (shortcut, uses idProvider)
public boolean hasSetters() { ... }           // Whether any setter is registered
public void validate() { ... }               // Throws if id mapping is missing

// Client→server update (Strategy A)
public void applyChanges(T item, ObjectNode changes) {
    // Throws IllegalStateException if no setters registered
    // For each changed property with a registered setter: parse JSON value, call setter
}
```

**Type conversion rules for `toJson()`:**
- `LocalDateTime` → ISO-8601 string (reuse `LocalDateTimeConverter`)
- `LocalDate` → ISO date string (reuse `LocalDateConverter`)
- `RecurringTime` → formatted string (reuse `RecurringTimeConverter`)
- `Set<DayOfWeek>` → `List<Integer>` with 0=Sunday (reuse `DayOfWeekItemConverter`)
- `DisplayMode` → string via `.getClientSideValue()`
- `Set<String>` (classNames) → JSON array
- `Map<String, Object>` (customProperties) → JSON object
- `null` values → property omitted from JSON (FullCalendar uses its own defaults)

**Design decisions (finalized by spike 0.1):**
- `id` is mandatory; `validate()` throws `IllegalStateException` if not set
- Immutable after first use (thread-safe); mutations after `toJson()` or `validate()` throw
- String-based mapping uses `BeanProperties.read()` for reflection (reuses existing infra)

---

### 2. `CalendarItemUpdateHandler<T>` (Functional Interface)

Strategy B for client→server updates. Alternative to mapper setters.

**Package:** `org.vaadin.stefan.fullcalendar`

```java
@FunctionalInterface
public interface CalendarItemUpdateHandler<T> extends Serializable {
    void handleUpdate(T item, CalendarItemChanges changes);
}
```

### 3. `CalendarItemChanges`

Typed wrapper around the raw JSON delta sent by the client on drag/drop/resize.

**Package:** `org.vaadin.stefan.fullcalendar`

```java
public class CalendarItemChanges {
    private final ObjectNode jsonDelta;

    public CalendarItemChanges(ObjectNode jsonDelta) { ... }

    public Optional<LocalDateTime> getChangedStart() { ... }
    public Optional<LocalDateTime> getChangedEnd() { ... }
    public Optional<Boolean> getChangedAllDay() { ... }
    public ObjectNode getRawJson() { return jsonDelta; }
}
```

For the scheduler extension, `SchedulerCalendarItemChanges extends CalendarItemChanges`
adds resource-related change accessors (created in Phase 5).

---

### 4. `CalendarItemProvider<T>` (Interface)

**Package:** `org.vaadin.stefan.fullcalendar.dataprovider`

```java
public interface CalendarItemProvider<T> extends Serializable {
    Stream<T> fetch(@NonNull CalendarQuery query);
    Optional<T> fetchById(@NonNull String id);
    Registration addItemsChangeListener(CalendarItemsChangeEvent.Listener<T> listener);
    Registration addItemRefreshListener(CalendarItemRefreshEvent.Listener<T> listener);
    void refreshAll();
    void refreshItem(T item);

    // Convenience factory methods (mirrors EntryProvider pattern)
    static <T> CallbackCalendarItemProvider<T> fromCallbacks(
        SerializableFunction<CalendarQuery, Stream<T>> fetch,
        SerializableFunction<String, T> fetchById) { ... }

    static <T> InMemoryCalendarItemProvider<T> emptyInMemory(
        SerializableFunction<T, String> idExtractor) { ... }

    @SafeVarargs
    static <T> InMemoryCalendarItemProvider<T> inMemoryFrom(
        SerializableFunction<T, String> idExtractor, T... items) { ... }
}
```

### 5. `CalendarQuery`

**Package:** `org.vaadin.stefan.fullcalendar.dataprovider`

Generalized query without Entry-specific filtering. `EntryQuery` will extend this in Phase 2.

```java
public class CalendarQuery {
    private final LocalDateTime start;
    private final LocalDateTime end;

    public CalendarQuery(@NonNull LocalDateTime start, @NonNull LocalDateTime end) { ... }
    public LocalDateTime getStart() { ... }
    public LocalDateTime getEnd() { ... }
}
```

No `allDay` filter — that's Entry-specific (lives in `EntryQuery`).
In-memory providers can use the mapper's start/end accessors for time-range filtering.
Callback providers receive the range and do their own filtering.

### 6. `CalendarItemsChangeEvent<T>` and `CalendarItemRefreshEvent<T>`

**Package:** `org.vaadin.stefan.fullcalendar.dataprovider`

```java
public class CalendarItemsChangeEvent<T> extends EventObject {
    public CalendarItemsChangeEvent(CalendarItemProvider<T> source) { super(source); }

    @FunctionalInterface
    public interface Listener<T> extends Serializable {
        void onDataChange(CalendarItemsChangeEvent<T> event);
    }
}

public class CalendarItemRefreshEvent<T> extends EventObject {
    private final T itemToRefresh;

    public CalendarItemRefreshEvent(CalendarItemProvider<T> source, T item) { ... }
    public T getItemToRefresh() { return itemToRefresh; }

    @FunctionalInterface
    public interface Listener<T> extends Serializable {
        void onDataRefresh(CalendarItemRefreshEvent<T> event);
    }
}
```

### 7. `AbstractCalendarItemProvider<T>`

**Package:** `org.vaadin.stefan.fullcalendar.dataprovider`

Base implementation with listener management. Mirrors `AbstractEntryProvider` pattern.

```java
public abstract class AbstractCalendarItemProvider<T> implements CalendarItemProvider<T> {
    // Listener storage: maps event class → list of listeners
    // Same pattern as AbstractEntryProvider
    // Provides addItemsChangeListener(), addItemRefreshListener(), fireItemsChangeEvent(), etc.
}
```

### 8. `InMemoryCalendarItemProvider<T>`

**Package:** `org.vaadin.stefan.fullcalendar.dataprovider`

```java
public class InMemoryCalendarItemProvider<T> extends AbstractCalendarItemProvider<T> {
    private final Map<String, T> itemsMap = new LinkedHashMap<>();
    private final SerializableFunction<T, String> idExtractor;

    public InMemoryCalendarItemProvider(SerializableFunction<T, String> idExtractor) { ... }

    // CRUD
    public void addItem(T item) { ... }
    public void addItems(Collection<T> items) { ... }
    public void removeItem(T item) { ... }
    public void removeAllItems() { ... }
    public Collection<T> getItems() { ... }

    // CalendarItemProvider implementation
    @Override public Stream<T> fetch(CalendarQuery query) { ... }  // returns all items (no time filtering in base impl)
    @Override public Optional<T> fetchById(String id) { ... }
}
```

### 9. `CallbackCalendarItemProvider<T>`

**Package:** `org.vaadin.stefan.fullcalendar.dataprovider`

```java
public class CallbackCalendarItemProvider<T> extends AbstractCalendarItemProvider<T> {
    private final SerializableFunction<CalendarQuery, Stream<T>> fetchCallback;
    private final SerializableFunction<String, T> fetchByIdCallback;

    public CallbackCalendarItemProvider(
        SerializableFunction<CalendarQuery, Stream<T>> fetch,
        SerializableFunction<String, T> fetchById) { ... }

    @Override public Stream<T> fetch(CalendarQuery query) { return fetchCallback.apply(query); }
    @Override public Optional<T> fetchById(String id) { return Optional.ofNullable(fetchByIdCallback.apply(id)); }
}
```

---

## Testing

All tests in `addon/src/test/java/org/vaadin/stefan/fullcalendar/`

### CalendarItemPropertyMapperTest
- Lambda mapping: all property types mapped, `toJson()` produces correct JSON keys + values
- String mapping: same properties mapped via string names, same JSON output
- `id` is mandatory: `validate()` throws `IllegalStateException` if not set
- `getId(item)` returns the mapped ID string
- Type conversion: `LocalDateTime` → ISO string, `DayOfWeek` → int array, etc.
- Null values: unmapped/null properties omitted from JSON
- Setters: `applyChanges(item, changes)` modifies POJO via registered setters
- No setters: `applyChanges()` throws `IllegalStateException`
- `hasSetters()` returns correct boolean
- Immutability: mutations after first `toJson()`/`validate()` call throw

### CalendarItemChangesTest
- `getChangedStart()` returns parsed LocalDateTime from JSON
- `getChangedEnd()` returns parsed LocalDateTime
- `getChangedAllDay()` returns boolean
- Missing properties return `Optional.empty()`
- `getRawJson()` returns original ObjectNode

### InMemoryCalendarItemProviderTest
- Add/remove items, verify `getItems()`
- `fetch()` returns all items as stream
- `fetchById()` returns correct item or empty
- `refreshAll()` fires `CalendarItemsChangeEvent`
- `refreshItem()` fires `CalendarItemRefreshEvent`

### CallbackCalendarItemProviderTest
- Delegates to provided callbacks
- `fetch()` passes CalendarQuery to callback
- `fetchById()` passes ID to callback

### CalendarQueryTest
- Construction with start/end
- Getters return correct values

## Completion Criteria

- All new classes compile and have passing tests
- No existing code modified
- No existing tests broken
- Spike code retained in test sources for reference
