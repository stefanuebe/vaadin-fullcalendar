# Phase 2: Adapter Layer

**Goal:** Wire existing EntryProvider hierarchy into the new CIP type hierarchy.
**Prerequisite:** Phase 1 complete.
**Breaking changes:** None (existing APIs preserved, new supertypes added).

---

## Changes

### 1. `EntryProvider<T extends Entry>` extends `CalendarItemProvider<T>`

```java
// Before:
public interface EntryProvider<T extends Entry> extends Serializable { ... }

// After:
public interface EntryProvider<T extends Entry> extends CalendarItemProvider<T> { ... }
```

The existing `EntryProvider` methods already satisfy `CalendarItemProvider`'s contract:
- `fetch(EntryQuery)` satisfies `fetch(CalendarQuery)` — `EntryQuery` will extend `CalendarQuery`
- `fetchById(String)` — same signature
- Listener methods — map to generic equivalents

**Implementation detail:** Bridge methods may be needed if `CalendarQuery` and `EntryQuery`
are separate types. Two approaches:
- (a) `EntryQuery extends CalendarQuery` — cleanest, but adds an `extends Entry` constraint
  in the superclass filter logic. Feasible since `EntryQuery.applyFilter()` already requires `Entry`.
- (b) `EntryProvider` implements both interfaces separately with adapter methods.

**Recommended:** Option (a) — `EntryQuery extends CalendarQuery`.

### 2. `EntryQuery` extends `CalendarQuery`

```java
// Before:
public class EntryQuery { ... }

// After:
public class EntryQuery extends CalendarQuery {
    private final AllDay allDay;  // Entry-specific filter retained

    // applyFilter() still works, using Entry-specific methods
}
```

### 3. `AbstractEntryProvider` extends `AbstractCalendarItemProvider`

```java
// Before:
public abstract class AbstractEntryProvider<T extends Entry> implements EntryProvider<T> { ... }

// After:
public abstract class AbstractEntryProvider<T extends Entry>
    extends AbstractCalendarItemProvider<T>
    implements EntryProvider<T> { ... }
```

Listener management moves to `AbstractCalendarItemProvider`. `AbstractEntryProvider` becomes
a thin layer that delegates to the parent and adds Entry-specific behavior (setCalendar on items).

### 4. Event Type Bridges

`EntriesChangeEvent<T extends Entry>` wraps or extends `CalendarItemsChangeEvent<T>`.
`EntryRefreshEvent<T extends Entry>` wraps or extends `CalendarItemRefreshEvent<T>`.

This ensures existing listeners still work while internal dispatch uses the new generic types.

### 5. Static Factory Methods

`CalendarItemProvider` gets factory methods mirroring `EntryProvider`:

```java
public interface CalendarItemProvider<T> {
    static <T> CallbackCalendarItemProvider<T> fromCallbacks(
        SerializableFunction<CalendarQuery, Stream<T>> fetch,
        SerializableFunction<String, T> fetchById) { ... }

    static <T> InMemoryCalendarItemProvider<T> emptyInMemory(
        SerializableFunction<T, String> idExtractor) { ... }

    static <T> InMemoryCalendarItemProvider<T> inMemoryFrom(
        SerializableFunction<T, String> idExtractor, T... items) { ... }
}
```

---

## Testing

- Verify `EntryProvider` is assignable to `CalendarItemProvider` (compile-time check)
- Verify existing `InMemoryEntryProvider` and `CallbackEntryProvider` still work unchanged
- Run ALL existing tests — nothing should break
- Test that `EntryQuery` works as a `CalendarQuery`

## Completion Criteria

- `EntryProvider extends CalendarItemProvider` compiles
- All existing tests pass
- No deprecation warnings introduced yet (those come in Phase 3)
