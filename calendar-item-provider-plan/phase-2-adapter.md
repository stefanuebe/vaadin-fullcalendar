# Phase 2: Adapter Layer

**Goal:** Wire existing EntryProvider hierarchy into the new CIP type hierarchy so that
`EntryProvider` becomes a specialization of `CalendarItemProvider`.
**Prerequisite:** Phase 1 complete.
**Breaking changes:** None — existing APIs preserved, new supertypes added.

---

## Context

Phase 1 created standalone CIP types. This phase connects them to the existing Entry-based
types by making `EntryProvider<T extends Entry>` extend `CalendarItemProvider<T>` and
`EntryQuery` extend `CalendarQuery`. This ensures the Entry path is just a specialized
case of the generic CIP path.

## Module

All changes in `addon/src/main/java/org/vaadin/stefan/fullcalendar/dataprovider/`

---

## Changes

### 1. `EntryQuery extends CalendarQuery`

**File:** `addon/src/main/java/org/vaadin/stefan/fullcalendar/dataprovider/EntryQuery.java`

```java
// Before:
public class EntryQuery {
    private final LocalDateTime start;
    private final LocalDateTime end;
    private final AllDay allDay;
    ...
}

// After:
public class EntryQuery extends CalendarQuery {
    private final AllDay allDay;  // Entry-specific filter, retained

    public EntryQuery(LocalDateTime start, LocalDateTime end, AllDay allDay) {
        super(start, end);        // delegates to CalendarQuery
        this.allDay = allDay;
    }

    // applyFilter() stays — it uses Entry-specific methods (isRecurring, getEnd, etc.)
    // This is fine because EntryQuery is only used with EntryProvider<T extends Entry>
}
```

### 2. `EntryProvider<T extends Entry> extends CalendarItemProvider<T>`

**File:** `addon/src/main/java/org/vaadin/stefan/fullcalendar/dataprovider/EntryProvider.java`

```java
// Before:
public interface EntryProvider<T extends Entry> extends Serializable { ... }

// After:
public interface EntryProvider<T extends Entry> extends CalendarItemProvider<T> { ... }
```

The existing `EntryProvider` methods already satisfy `CalendarItemProvider`'s contract:
- `fetch(EntryQuery)` — `EntryQuery extends CalendarQuery`, so this satisfies
  `fetch(CalendarQuery)`. However, the parameter type difference means we need a bridge:

```java
// In EntryProvider, add a default bridge method:
@Override
default Stream<T> fetch(@NonNull CalendarQuery query) {
    // If called with a CalendarQuery (not EntryQuery), wrap it
    if (query instanceof EntryQuery entryQuery) {
        return fetch(entryQuery);
    }
    return fetch(new EntryQuery(query.getStart(), query.getEnd(), EntryQuery.AllDay.BOTH));
}

// Keep the existing signature as the primary method:
Stream<T> fetch(@NonNull EntryQuery query);
```

- `fetchById(String)` — same signature, no change needed
- Listener methods — bridge to generic equivalents via default methods

### 3. `AbstractEntryProvider<T extends Entry> extends AbstractCalendarItemProvider<T>`

**File:** `addon/src/main/java/org/vaadin/stefan/fullcalendar/dataprovider/AbstractEntryProvider.java`

```java
// Before:
public abstract class AbstractEntryProvider<T extends Entry> implements EntryProvider<T> { ... }

// After:
public abstract class AbstractEntryProvider<T extends Entry>
    extends AbstractCalendarItemProvider<T>
    implements EntryProvider<T> { ... }
```

Listener management moves to `AbstractCalendarItemProvider`. `AbstractEntryProvider` becomes
a thin layer that delegates to the parent and adds Entry-specific behavior (e.g., calling
`entry.setCalendar()` on items).

Duplicate listener fields/methods in `AbstractEntryProvider` are removed; the parent handles
it. Ensure that existing `EntriesChangeEvent` / `EntryRefreshEvent` listeners still fire
correctly — they can bridge from the generic `CalendarItemsChangeEvent` / `CalendarItemRefreshEvent`
via type casting or by firing both event types.

### 4. Event Type Bridges

```java
// EntriesChangeEvent<T extends Entry> bridges to CalendarItemsChangeEvent<T>:
// Option A: EntriesChangeEvent extends CalendarItemsChangeEvent (cleanest)
public class EntriesChangeEvent<T extends Entry> extends CalendarItemsChangeEvent<T> { ... }

// Option B: AbstractEntryProvider fires both event types (if A causes issues)
```

Same pattern for `EntryRefreshEvent<T extends Entry>` → `CalendarItemRefreshEvent<T>`.

### 5. `CalendarItemProvider` Factory Methods (from Phase 1) — Verify

Ensure the static factory methods on `CalendarItemProvider` work correctly:
```java
CalendarItemProvider.fromCallbacks(query -> ..., id -> ...);
CalendarItemProvider.emptyInMemory(MyPojo::getId);
CalendarItemProvider.inMemoryFrom(MyPojo::getId, pojo1, pojo2);
```

And that `EntryProvider` factory methods still work:
```java
EntryProvider.fromCallbacks(query -> ..., id -> ...);
EntryProvider.emptyInMemory();
EntryProvider.inMemoryFrom(entry1, entry2);
```

---

## Testing

### Compile-time checks
- `EntryProvider` is assignable to `CalendarItemProvider` (compile-time)
- `EntryQuery` is assignable to `CalendarQuery` (compile-time)

### Behavioral tests
- Existing `InMemoryEntryProvider` works unchanged — add items, fetch, refresh
- Existing `CallbackEntryProvider` works unchanged — delegates to callbacks
- `EntryProvider.fetch(CalendarQuery)` bridge works — wraps as EntryQuery internally
- `EntriesChangeEvent` still fires when entries change
- `EntryRefreshEvent` still fires on entry refresh
- New `CalendarItemsChangeEvent` listener on an EntryProvider also fires

### Regression
- Run ALL existing tests — nothing should break
- No deprecation warnings introduced yet (those come in Phase 3)

## Completion Criteria

- `EntryProvider extends CalendarItemProvider` compiles
- `EntryQuery extends CalendarQuery` compiles
- All existing entry provider tests pass unchanged
- New CalendarItemProvider listeners work on EntryProvider instances

---

## Implementation Notes (completed)

**Status: COMPLETE**

Implemented Option A for event bridges (`EntriesChangeEvent extends CalendarItemsChangeEvent`,
`EntryRefreshEvent extends CalendarItemRefreshEvent`). The `isAssignableFrom()` check in
`AbstractCalendarItemProvider.fireEvent()` ensures both legacy and CIP listeners fire when
Entry-specific events are dispatched.

Additional change not in original plan: removed `isInMemory()`/`asInMemory()` default methods
from `CalendarItemProvider` to avoid covariant return type conflicts with `EntryProvider`.
These convenience methods remain on `EntryProvider` where they belong.

Files modified:
- `CalendarItemProvider.java` — removed `isInMemory()`/`asInMemory()` defaults
- `EntriesChangeEvent.java` — extends `CalendarItemsChangeEvent<T>`
- `EntryRefreshEvent.java` — extends `CalendarItemRefreshEvent<T>`, removed redundant field
- `EntryQuery.java` — extends `CalendarQuery`, manual constructors/builder replacing Lombok
- `EntryProvider.java` — extends `CalendarItemProvider<T>`, bridge `fetch(CalendarQuery)`
- `AbstractEntryProvider.java` — extends `AbstractCalendarItemProvider<T>`, removed duplicate listener infra

Tests: 215 pass (including new `EntryProviderAdapterTest` with 16 tests).
