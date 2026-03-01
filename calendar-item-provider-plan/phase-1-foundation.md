# Phase 1: Foundation Types

**Goal:** Create all new CIP types as standalone additions. Zero changes to existing code.
**Prerequisite:** Phase 0 spikes completed.
**Breaking changes:** None.

---

## New Classes to Create

All in `addon/src/main/java/org/vaadin/stefan/fullcalendar/`

### 1. `CalendarItemPropertyMapper<T>`

The central mapping class. Maps POJO properties to FullCalendar event properties.
Supports both read (server->client) and write (client->server) directions.

```java
package org.vaadin.stefan.fullcalendar;

public class CalendarItemPropertyMapper<T> {

    // --- REQUIRED mapping (fail fast if missing) ---
    private ValueProvider<T, String> idProvider;      // REQUIRED

    // --- Core read mappings (server -> client) ---
    private ValueProvider<T, String> titleProvider;
    private ValueProvider<T, ?> startProvider;         // LocalDateTime, Instant, etc.
    private ValueProvider<T, ?> endProvider;
    private ValueProvider<T, Boolean> allDayProvider;
    private ValueProvider<T, String> colorProvider;
    private ValueProvider<T, String> backgroundColorProvider;
    private ValueProvider<T, String> borderColorProvider;
    private ValueProvider<T, String> textColorProvider;
    private ValueProvider<T, String> groupIdProvider;
    private ValueProvider<T, Boolean> editableProvider;
    private ValueProvider<T, Boolean> startEditableProvider;
    private ValueProvider<T, Boolean> durationEditableProvider;
    private ValueProvider<T, DisplayMode> displayModeProvider;
    private ValueProvider<T, String> constraintProvider;
    private ValueProvider<T, Boolean> overlapProvider;
    private ValueProvider<T, Set<String>> classNamesProvider;
    private ValueProvider<T, Map<String, Object>> customPropertiesProvider;

    // Recurrence mappings (read-only)
    private ValueProvider<T, LocalDate> recurringStartDateProvider;
    private ValueProvider<T, LocalDate> recurringEndDateProvider;
    private ValueProvider<T, RecurringTime> recurringStartTimeProvider;
    private ValueProvider<T, RecurringTime> recurringEndTimeProvider;
    private ValueProvider<T, Set<DayOfWeek>> recurringDaysOfWeekProvider;

    // --- Optional write mappings (client -> server, for @JsonUpdateAllowed properties) ---
    private SerializableBiConsumer<T, LocalDateTime> startSetter;
    private SerializableBiConsumer<T, LocalDateTime> endSetter;
    private SerializableBiConsumer<T, Boolean> allDaySetter;

    // --- State flag ---
    private boolean hasSetters;  // true if any setter is registered

    // Builder API
    public static <T> CalendarItemPropertyMapper<T> of(Class<T> type) { ... }

    // --- Read-only mapping (single arg) ---
    public CalendarItemPropertyMapper<T> id(ValueProvider<T, String> provider) { ... }
    public CalendarItemPropertyMapper<T> title(ValueProvider<T, String> provider) { ... }
    public CalendarItemPropertyMapper<T> color(ValueProvider<T, String> provider) { ... }
    // ... etc for all read-only properties

    // --- Bidirectional mapping (getter + setter) for updatable properties ---
    public CalendarItemPropertyMapper<T> start(ValueProvider<T, ?> getter) { ... }
    public CalendarItemPropertyMapper<T> start(ValueProvider<T, ?> getter,
                                                SerializableBiConsumer<T, LocalDateTime> setter) {
        this.startProvider = getter;
        this.startSetter = setter;
        this.hasSetters = true;
        return this;
    }

    public CalendarItemPropertyMapper<T> end(ValueProvider<T, ?> getter) { ... }
    public CalendarItemPropertyMapper<T> end(ValueProvider<T, ?> getter,
                                              SerializableBiConsumer<T, LocalDateTime> setter) { ... }

    public CalendarItemPropertyMapper<T> allDay(ValueProvider<T, Boolean> getter) { ... }
    public CalendarItemPropertyMapper<T> allDay(ValueProvider<T, Boolean> getter,
                                                 SerializableBiConsumer<T, Boolean> setter) { ... }

    // --- String-based mapping (reflection) ---
    public CalendarItemPropertyMapper<T> id(String propertyName) { ... }
    public CalendarItemPropertyMapper<T> title(String propertyName) { ... }
    // ... etc

    // --- Core methods ---
    public ObjectNode toJson(T item) { ... }          // Serialize POJO to FC-compatible JSON
    public String getId(T item) { ... }               // Extract ID (shortcut)
    public void validate() { ... }                    // Fail fast if required mappings missing

    // --- Client-to-server update (Strategy A) ---
    public boolean hasSetters() { return hasSetters; }

    /**
     * Applies client-side changes to the POJO using registered setters.
     * Only works if setters were registered for the changed properties.
     * @throws IllegalStateException if no setters are registered
     */
    public void applyChanges(T item, ObjectNode changes) {
        if (!hasSetters) {
            throw new IllegalStateException(
                "No setters registered on mapper. Use setter overloads "
                + "(e.g. .start(getter, setter)) or set a CalendarItemUpdateHandler.");
        }
        // Apply each changed property via its setter if present
        if (changes.has("start") && startSetter != null) {
            startSetter.accept(item, parseDateTime(changes.get("start")));
        }
        if (changes.has("end") && endSetter != null) {
            endSetter.accept(item, parseDateTime(changes.get("end")));
        }
        if (changes.has("allDay") && allDaySetter != null) {
            allDaySetter.accept(item, changes.get("allDay").asBoolean());
        }
    }
}
```

**Key design decisions:**
- `id` is mandatory; `validate()` called at attachment time, throws if missing
- `toJson()` produces the ObjectNode directly (no intermediate bound mapper — see spike 0.1)
- String-based mapping uses `BeanProperties.read()` for reflection (reuse existing infra)
- Immutable after construction (thread-safe)
- Type conversions for temporal types use existing `JsonItemPropertyConverter` instances
- Setters are opt-in per property (only for `start`, `end`, `allDay` — the `@JsonUpdateAllowed` fields)

### 2. `CalendarItemUpdateHandler<T>` (Functional Interface)

Strategy B for client-to-server updates:

```java
package org.vaadin.stefan.fullcalendar;

@FunctionalInterface
public interface CalendarItemUpdateHandler<T> extends Serializable {
    /**
     * Called when the client reports changes to a calendar item (drag/drop/resize).
     * @param item the original POJO from cache
     * @param changes convenience object with typed access to changed properties
     */
    void handleUpdate(T item, CalendarItemChanges changes);
}
```

### 3. `CalendarItemChanges`

Typed wrapper around the raw JSON delta from the client:

```java
package org.vaadin.stefan.fullcalendar;

public class CalendarItemChanges {
    private final ObjectNode jsonDelta;

    public Optional<LocalDateTime> getChangedStart() { ... }
    public Optional<LocalDateTime> getChangedEnd() { ... }
    public Optional<Boolean> getChangedAllDay() { ... }
    public ObjectNode getRawJson() { return jsonDelta; }
}
```

### 4. `CalendarItemProvider<T>` (Interface)

New in `dataprovider/` package:

```java
package org.vaadin.stefan.fullcalendar.dataprovider;

public interface CalendarItemProvider<T> extends Serializable {
    Stream<T> fetch(@NonNull CalendarQuery query);
    Optional<T> fetchById(@NonNull String id);
    Registration addItemsChangeListener(CalendarItemsChangeEvent.Listener<T> listener);
    Registration addItemRefreshListener(CalendarItemRefreshEvent.Listener<T> listener);
    void refreshAll();
    void refreshItem(T item);
}
```

### 5. `CalendarQuery`

Generalized query without Entry-specific filtering:

```java
package org.vaadin.stefan.fullcalendar.dataprovider;

public class CalendarQuery {
    private final LocalDateTime start;
    private final LocalDateTime end;
    // No allDay filter — that's Entry-specific.
    // Filtering is done by the provider implementation using the mapper.
}
```

### 6. `CalendarItemsChangeEvent<T>` and `CalendarItemRefreshEvent<T>`

Generic versions of `EntriesChangeEvent` and `EntryRefreshEvent`:

```java
public class CalendarItemsChangeEvent<T> extends EventObject {
    @FunctionalInterface
    public interface Listener<T> extends Serializable {
        void onDataChange(CalendarItemsChangeEvent<T> event);
    }
}

public class CalendarItemRefreshEvent<T> extends EventObject {
    private final T itemToRefresh;

    @FunctionalInterface
    public interface Listener<T> extends Serializable {
        void onDataRefresh(CalendarItemRefreshEvent<T> event);
    }
}
```

### 7. `AbstractCalendarItemProvider<T>`

Base implementation with listener management:

```java
public abstract class AbstractCalendarItemProvider<T> implements CalendarItemProvider<T> {
    private final Map<Class<?>, List<SerializableConsumer<?>>> listeners = new HashMap<>();
    // Same listener management pattern as AbstractEntryProvider
}
```

### 8. `InMemoryCalendarItemProvider<T>`

```java
public class InMemoryCalendarItemProvider<T> implements CalendarItemProvider<T> {
    private final Map<String, T> itemsMap;
    private final SerializableFunction<T, String> idExtractor;
    // Constructor requires idExtractor OR CalendarItemPropertyMapper
}
```

### 9. `CallbackCalendarItemProvider<T>`

```java
public class CallbackCalendarItemProvider<T> implements CalendarItemProvider<T> {
    private final SerializableFunction<CalendarQuery, Stream<T>> fetchItems;
    private final SerializableFunction<String, T> fetchSingleItem;
}
```

---

## Testing

- Unit tests for `CalendarItemPropertyMapper` with a test POJO
  - All property types mapped via lambda
  - All property types mapped via string
  - `toJson()` output matches expected FullCalendar JSON structure
  - Missing required `id` mapping throws on validate()
  - Type conversion for temporal types
  - `applyChanges()` with setters modifies POJO correctly
  - `applyChanges()` without setters throws IllegalStateException
- Unit tests for `CalendarItemUpdateHandler` + `CalendarItemChanges`
- Unit tests for `InMemoryCalendarItemProvider` and `CallbackCalendarItemProvider`
- Unit tests for `CalendarQuery`

## Completion Criteria

- All new classes compile and have tests
- No existing code modified
- No existing tests broken
