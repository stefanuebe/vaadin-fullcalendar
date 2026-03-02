# Phase 7: Internal Unification — Entry on CIP Foundation

**Goal:** Make `setEntryProvider()` internally set up a `CalendarItemProvider` + mapper, eliminating the
dual code paths in `FullCalendar`. After this phase, all calendars use CIP internally — Entry-based
calendars are just a convenient preset.

**Prerequisite:** Phases 1–6 complete.
**Breaking changes:** None — all public API behavior preserved.

---

## Context

Currently `FullCalendar` has two completely separate internal paths:

| Aspect | Entry Path | CIP Path |
|--------|-----------|---------:|
| Provider field | `entryProvider` | `calendarItemProvider` |
| State flag | `usingCalendarItemProvider == false` | `usingCalendarItemProvider == true` |
| Listener list | `entryProviderDataListeners` | `calendarItemProviderListeners` |
| Fetch method | `fetchEntries()` | `fetchCalendarItems()` |
| JSON conversion | `Entry.toJson()` | `mapper.toJson()` |
| Cache accessor | `getCachedEntryFromFetch()` (instanceof) | `getCachedItemFromFetch()` (flag check) |
| Refresh | `requestRefresh(Entry)` | `requestRefreshCalendarItem(T)` |

The original requirements say: *"Methods or fields, that are used internally (like cache or fetchFromServer)
will be rewritten to use the new api."* — this phase fulfills that intent.

---

## Design Decisions

### 1. Entry Mapper — Use a JSON Serializer Hook

`CalendarItemPropertyMapper` has a **private constructor** — it cannot be subclassed. Rather than
opening the constructor, we add a **JSON serializer hook**: a `Function<T, ObjectNode>` that `toJson()`
delegates to when set. This lets us plug in `Entry.toJson()` without subclassing:

```java
// In CalendarItemPropertyMapper:
private Function<T, ObjectNode> jsonSerializer;

public CalendarItemPropertyMapper<T> jsonSerializer(Function<T, ObjectNode> serializer) {
    ensureNotFrozen();
    this.jsonSerializer = Objects.requireNonNull(serializer);
    return this;
}

// In toJson():
public ObjectNode toJson(T item) {
    if (jsonSerializer != null) {
        return jsonSerializer.apply(item);
    }
    // ... existing readMappings iteration ...
}
```

The Entry mapper becomes:
```java
CalendarItemPropertyMapper<Entry> entryMapper = CalendarItemPropertyMapper.of(Entry.class)
    .id(Entry::getId)
    .jsonSerializer(Entry::toJson);
```

No subclass needed. The `id()` mapping is still required for cache lookups via `getId()`.

### 2. Entry Update Handler — Delegate to Entry.updateFromJson()

When `setEntryProvider()` is called, automatically register a `CalendarItemUpdateHandler<Entry>`
that delegates to `entry.updateFromJson(jsonObject)`. The JSON passed to the handler is the full
item data from `@EventData("event.detail.data")` — it always contains the matching `id` field,
so `updateFromJson(json, true)` (the default) works correctly.

This is a simple lambda — no class needed:
```java
CalendarItemUpdateHandler<Entry> entryHandler = (entry, changes) ->
        entry.updateFromJson(changes.getRawJson());
```

### 3. Remove Dual State

After unification, `FullCalendar` has:
- One `CalendarItemProvider<T>` field (always set)
- One `CalendarItemPropertyMapper<T>` field (always set)
- One `CalendarItemUpdateHandler<T>` field (optional)
- One listener list for provider events
- `entryProviderRef` kept as a convenience accessor (stored separately)
- `usingCalendarItemProvider` flag **removed**

### 4. Entry.setCalendar() Call Preserved

Two Entry-specific lifecycle calls must be preserved:
- `entryProvider.setCalendar(this)` — tells the provider about its parent calendar (timezone, etc.)
- `entry.setCalendar(this)` + `entry.setKnownToTheClient(true)` — per-entry in the fetch path

The first is handled in `setEntryProvider()` directly (before delegating to CIP). The second is
handled in the unified fetch method with `if (item instanceof Entry entry)` checks.

### 5. refreshSingleEvent — Use refetchEvents Uniformly

The client-side `refreshSingleEvent(id: string)` just calls `this.calendar.refetchEvents()`
regardless of the argument. The Entry path sends a String ID, the CIP path sends an ObjectNode —
both result in a full refetch. In the unified path, we can send either form. For simplicity, send
the String ID (via `calendarItemPropertyMapper.getId(item)`) since that matches the JS signature.

### 6. isUsingCalendarItemProvider() — Keep as Deprecated

The `isUsingCalendarItemProvider()` method is used in 8+ test assertions and in
`FullCalendarScheduler.removeFromEntries()`. Rather than removing it outright:
- Keep as `@Deprecated(since = "7.1")` with the semantics: returns `true` if the active provider
  is NOT an EntryProvider (i.e., a "pure CIP" calendar with custom POJOs)
- Implementation: `return !(calendarItemProvider instanceof EntryProvider)`
- This preserves existing test behavior and scheduler logic

---

## Steps

### Step 1: Add jsonSerializer Hook to CalendarItemPropertyMapper

**File:** `addon/src/main/java/org/vaadin/stefan/fullcalendar/CalendarItemPropertyMapper.java`

Add a `Function<T, ObjectNode> jsonSerializer` field and a fluent setter method. Update `toJson()` to
delegate when the hook is set:

```java
private Function<T, ObjectNode> jsonSerializer;

public CalendarItemPropertyMapper<T> jsonSerializer(Function<T, ObjectNode> serializer) {
    ensureNotFrozen();
    this.jsonSerializer = Objects.requireNonNull(serializer, "serializer");
    return this;
}

public ObjectNode toJson(T item) {
    Objects.requireNonNull(item, "item");
    freezeIfNeeded();

    if (jsonSerializer != null) {
        return jsonSerializer.apply(item);
    }

    // ... existing readMappings iteration (unchanged) ...
}
```

### Step 2: Rewrite setEntryProvider() to Delegate

`setEntryProvider()` becomes a thin wrapper that:
1. Calls `entryProvider.setCalendar(this)` (Entry lifecycle)
2. Creates an Entry mapper with `jsonSerializer(Entry::toJson)`
3. Delegates to `setCalendarItemProvider(provider, mapper)`
4. Sets the Entry update handler
5. Stores `entryProviderRef` for `getEntryProvider()`

```java
@Deprecated(since = "7.1", forRemoval = false)
public void setEntryProvider(EntryProvider<? extends Entry> entryProvider) {
    Objects.requireNonNull(entryProvider);

    // Store ref for getEntryProvider() and lifecycle
    EntryProvider<? extends Entry> oldRef = this.entryProviderRef;

    // EntryProvider lifecycle: setCalendar
    if (oldRef != null && oldRef != entryProvider) {
        oldRef.setCalendar(null);
    }
    entryProvider.setCalendar(this);
    this.entryProviderRef = entryProvider;

    // Create Entry mapper that delegates to Entry.toJson()
    @SuppressWarnings("unchecked")
    CalendarItemPropertyMapper<T> mapper = (CalendarItemPropertyMapper<T>)
            CalendarItemPropertyMapper.of(Entry.class)
                    .id(Entry::getId)
                    .jsonSerializer(Entry::toJson);

    // Delegate to CIP path
    @SuppressWarnings("unchecked")
    CalendarItemProvider<T> provider = (CalendarItemProvider<T>) entryProvider;
    setCalendarItemProvider(provider, mapper);

    // Set Entry update handler (Strategy B — delegates to entry.updateFromJson)
    @SuppressWarnings("unchecked")
    CalendarItemUpdateHandler<T> handler = (CalendarItemUpdateHandler<T>)
            (CalendarItemUpdateHandler<Entry>) (entry, changes) ->
                    entry.updateFromJson(changes.getRawJson());
    setCalendarItemUpdateHandler(handler);
}
```

Note: The unchecked casts are safe because when `setEntryProvider()` is called, `T` is always
`Entry` (or a subclass). These casts bridge the type system at the entry point.

### Step 3: Update setCalendarItemProvider() — No entryProvider Cleanup

Currently `setCalendarItemProvider()` clears the `entryProvider` field. After unification, this
is no longer needed since `setEntryProvider()` handles its own lifecycle. Update
`setCalendarItemProvider()`:
- Remove the block that clears `this.entryProvider` (this is now handled by `setEntryProvider()`)
- Keep CIP listener cleanup and re-registration

### Step 4: Remove Dual Fetch from fetchEntriesFromServer()

Replace the `if (usingCalendarItemProvider)` branch in `fetchEntriesFromServer()` with a single
unified fetch path. Remove the `fetchEntries()` method entirely.

```java
@ClientCallable
protected ArrayNode fetchEntriesFromServer(ObjectNode query) {
    Objects.requireNonNull(query);
    lastFetchedItems.clear();

    LocalDateTime start = query.hasNonNull("start") ? JsonUtils.parseClientSideDateTime(query.get("start").asString()) : null;
    LocalDateTime end = query.hasNonNull("end") ? JsonUtils.parseClientSideDateTime(query.get("end").asString()) : null;

    ArrayNode array = JsonFactory.createArray();
    fetchCalendarItems(start, end, array);
    return array;
}
```

Update `fetchCalendarItems()` to handle Entry-specific setup:
```java
private void fetchCalendarItems(LocalDateTime start, LocalDateTime end, ArrayNode array) {
    Objects.requireNonNull(calendarItemProvider, "calendarItemProvider is not set");
    Objects.requireNonNull(calendarItemPropertyMapper, "calendarItemPropertyMapper is not set");
    calendarItemProvider.fetch(new CalendarQuery(start, end))
            .peek(item -> {
                lastFetchedItems.put(calendarItemPropertyMapper.getId(item), item);
                if (item instanceof Entry entry) {
                    entry.setCalendar(this);
                    entry.setKnownToTheClient(true);
                }
            })
            .map(calendarItemPropertyMapper::toJson)
            .forEach(array::add);
}
```

Note: `EntryProvider.fetch(CalendarQuery)` has a default bridge method that converts to
`EntryQuery(start, end, AllDay.BOTH)`, so existing behavior is preserved.

### Step 5: Remove Dual State Fields

Remove from `FullCalendar`:
- `private EntryProvider<? extends Entry> entryProvider` → replace with `private EntryProvider<? extends Entry> entryProviderRef`
- `private boolean usingCalendarItemProvider` → remove
- `private final List<Registration> entryProviderDataListeners` → remove (use calendarItemProviderListeners)
- `fetchEntries()` method → remove (unified into fetchCalendarItems)

### Step 6: Unify Cache Accessors

`getCachedItemFromFetch()` becomes the primary accessor — remove the flag check:
```java
@SuppressWarnings("unchecked")
public Optional<T> getCachedItemFromFetch(String id) {
    return Optional.ofNullable((T) lastFetchedItems.get(id));
}
```

`getCachedEntryFromFetch()` stays as-is — it already uses `instanceof Entry`:
```java
public Optional<Entry> getCachedEntryFromFetch(String id) {
    Object item = lastFetchedItems.get(id);
    return item instanceof Entry entry ? Optional.of(entry) : Optional.empty();
}
```

### Step 7: Unify Refresh Methods

Collapse `requestRefresh(Entry)` and `requestRefreshCalendarItem(T)` into one method.
Keep `requestRefresh(Entry)` as a deprecated wrapper:

```java
protected void requestRefreshCalendarItem(T item) {
    Objects.requireNonNull(item);
    Objects.requireNonNull(calendarItemPropertyMapper, "No CalendarItemPropertyMapper configured.");
    getElement().getNode().runWhenAttached(ui -> {
        ui.beforeClientResponse(this, ctx -> {
            String id = calendarItemPropertyMapper.getId(item);
            lastFetchedItems.put(id, item);
            // JS refreshSingleEvent(id) calls refetchEvents() regardless of argument type
            getElement().callJsFunction("refreshSingleEvent", id);
        });
    });
}

/** @deprecated Use requestRefreshCalendarItem */
@Deprecated(since = "7.1")
protected void requestRefresh(Entry item) {
    @SuppressWarnings("unchecked")
    T typed = (T) item;
    requestRefreshCalendarItem(typed);
}
```

### Step 8: Update isUsingCalendarItemProvider()

Keep as deprecated with new semantics:
```java
/** @deprecated Check provider type directly instead. */
@Deprecated(since = "7.1", forRemoval = false)
public boolean isUsingCalendarItemProvider() {
    return !(calendarItemProvider instanceof EntryProvider);
}
```

### Step 9: Update isInMemoryEntryProvider() and assureInMemoryProvider()

Both reference the removed `entryProvider` field. Update to use `entryProviderRef`:

```java
public boolean isInMemoryEntryProvider() {
    return entryProviderRef instanceof InMemoryEntryProvider;
}

protected InMemoryEntryProvider<Entry> assureInMemoryProvider() {
    if (!(entryProviderRef instanceof InMemoryEntryProvider)) {
        throw new UnsupportedOperationException("Needs an InMemoryEntryProvider to work.");
    }
    return (InMemoryEntryProvider<Entry>) entryProviderRef;
}
```

### Step 10: Update getEntryProvider()

Returns the stored `entryProviderRef`:
```java
@SuppressWarnings("unchecked")
public <R extends Entry, EP extends EntryProvider<R>> EP getEntryProvider() {
    return (EP) entryProviderRef;
}
```

### Step 11: Update FullCalendarScheduler.removeFromEntries()

Currently has a CIP early-return using `isUsingCalendarItemProvider()`. Update to check
provider type:
```java
if (!(calendarItemProvider instanceof EntryProvider<?>)) {
    return; // Not Entry-based — user manages resource associations
}
```

Note: `getCalendarItemProvider()` returns `CalendarItemProvider<T>` (not Optional), so no
`.orElse()` call.

### Step 12: Update FullCalendarBuilder

The builder's `build()` method currently has separate branches for entryProvider vs
calendarItemProvider. Since `setEntryProvider()` now delegates to CIP internally, the
builder can simplify to always use one path. If an `entryProvider` is configured, call
`setEntryProvider()` (which delegates internally). If a CIP is configured, call
`setCalendarItemProvider()` directly.

### Step 13: Handle postConstruct() Safely

`postConstruct()` calls `setEntryProvider(EntryProvider.emptyInMemory())`. After the rewrite,
this delegates to `setCalendarItemProvider()`. Ensure the delegation handles the initial state
where no previous provider exists (no NPE on `oldRef.setCalendar(null)` when `oldRef` is null).
The null guard `if (oldRef != null && oldRef != entryProvider)` in Step 2 handles this.

### Step 14: Update Tests

- Existing tests should pass with minimal changes (public API behavior unchanged)
- Tests asserting `isUsingCalendarItemProvider() == false` after `setEntryProvider()` still pass
  (deprecated method now checks `!(provider instanceof EntryProvider)` → still false for Entry)
- Tests asserting `isUsingCalendarItemProvider() == true` after `setCalendarItemProvider()`
  with a non-Entry provider still pass
- Tests asserting `getCalendarItemProvider() == null` after `setEntryProvider()` **NEED UPDATING**:
  after unification, `getCalendarItemProvider()` returns the EntryProvider (which is-a CIP)
- Tests asserting `getEntryProvider() == null` after `setCalendarItemProvider()` **NEED UPDATING**:
  `getEntryProvider()` now returns null when `entryProviderRef` is null (still correct)
- Add tests verifying that `setEntryProvider()` internally sets up CIP
- Add tests verifying CIP listeners work on Entry-based calendars
- Add tests verifying `getCachedItemFromFetch()` works for Entry items

---

## Verification

```bash
mvn clean install -DskipTests   # compile
mvn test                         # all existing tests pass + new unification tests
```

After this phase:
- `setEntryProvider()` internally delegates to `setCalendarItemProvider()`
- One fetch path, one cache, one listener list
- `addCalendarItemClickedListener()` works on Entry-based calendars
- All existing Entry-based code continues to work unchanged
- `isUsingCalendarItemProvider()` preserved as deprecated for backward compatibility
- `getCachedItemFromFetch()` works for both Entry and non-Entry items
