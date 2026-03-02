# Phase 8: Event Hierarchy Unification — Entry Events Extend CIP Events

**Goal:** Make every Entry event class extend its CIP counterpart, so that Entry events
ARE CIP events. This allows CIP listeners to receive Entry events, and keeps `getEntry()`
as a backward-compatible convenience method.

**Prerequisite:** Phase 7 (internal unification) complete. Specifically:
- `getCachedItemFromFetch()` must work for Entry-based calendars (flag check removed)
- The Entry update handler must be set up so `applyChangesOnItem()` correctly delegates
  to `Entry.updateFromJson()`
- `calendarItemPropertyMapper` is always set (even for Entry calendars)

**Breaking changes:** None for consumers. Potential breakage for:
- Code using `instanceof EntryTimeChangedEvent` to catch both dropped and resized events
  (they no longer extend through `EntryTimeChangedEvent` — use `instanceof CalendarItemTimeChangedEvent` instead)
- Code using `instanceof EntryChangedEvent` or `instanceof EntryDataEvent` to catch
  dropped/resized events (same reason — they extend through the CIP hierarchy now)

---

## Context

After Phase 7, the internals are unified — all calendars use CIP. But the event hierarchies
remain parallel:

```
Current:
ComponentEvent<FullCalendar<Entry>>
  └─ EntryEvent                          # Entry base, has field: Entry entry
       └─ EntryDataEvent                 # has field: ObjectNode jsonObject
            └─ EntryClickedEvent         # @DomEvent("eventClick")
            └─ EntryChangedEvent         # empty abstract
                 └─ EntryTimeChangedEvent # has field: Delta delta
                      └─ EntryDroppedEvent    # @DomEvent("eventDrop")
                      └─ EntryResizedEvent    # @DomEvent("eventResize")
            └─ EntryMouseEnterEvent      # @DomEvent("eventMouseEnter")
            └─ EntryMouseLeaveEvent      # @DomEvent("eventMouseLeave")

ComponentEvent<FullCalendar<T>>
  └─ CalendarItemEvent<T>                # CIP base, has field: T item
       └─ CalendarItemDataEvent<T>       # has field: ObjectNode jsonObject
            └─ CalendarItemClickedEvent<T>    # @DomEvent("eventClick")
            └─ CalendarItemDroppedEvent<T>    # @DomEvent("eventDrop"), has field: Delta delta (DUPLICATED)
            └─ CalendarItemResizedEvent<T>    # @DomEvent("eventResize"), has field: Delta delta (DUPLICATED)
            └─ CalendarItemMouseEnterEvent<T> # @DomEvent("entryMouseEnter")
            └─ CalendarItemMouseLeaveEvent<T> # @DomEvent("entryMouseLeave")
```

Note: `Delta` and `getDelta()` are currently **duplicated** in both `CalendarItemDroppedEvent`
and `CalendarItemResizedEvent` — this was an oversight in Phase 4. The Entry hierarchy has a
proper intermediate class (`EntryTimeChangedEvent`) for this.

After this phase:

```
Target:
CalendarItemEvent<T>                              # CIP base (unchanged)
  └─ CalendarItemDataEvent<T>                     # has JSON object (unchanged)
       └─ CalendarItemClickedEvent<T>             # @DomEvent("eventClick")
       │    └─ EntryClickedEvent                  # extends CalendarItemClickedEvent<Entry>
       └─ CalendarItemTimeChangedEvent<T>         # NEW: has Delta (extracted from dropped/resized)
       │    └─ CalendarItemDroppedEvent<T>         # @DomEvent("eventDrop")
       │    │    └─ EntryDroppedEvent              # extends CalendarItemDroppedEvent<Entry>
       │    └─ CalendarItemResizedEvent<T>         # @DomEvent("eventResize")
       │         └─ EntryResizedEvent              # extends CalendarItemResizedEvent<Entry>
       └─ CalendarItemMouseEnterEvent<T>           # @DomEvent("eventMouseEnter")
       │    └─ EntryMouseEnterEvent                # extends CalendarItemMouseEnterEvent<Entry>
       └─ CalendarItemMouseLeaveEvent<T>           # @DomEvent("eventMouseLeave")
            └─ EntryMouseLeaveEvent                # extends CalendarItemMouseLeaveEvent<Entry>

Scheduler events:
  CalendarItemDroppedSchedulerEvent<T>             # extends CalendarItemDroppedEvent<T>
       └─ EntryDroppedSchedulerEvent               # extends CalendarItemDroppedSchedulerEvent<Entry>

Deprecated (kept for backward compatibility):
  EntryEvent              → empty abstract, extends CalendarItemEvent<Entry>
  EntryDataEvent          → empty abstract, extends CalendarItemDataEvent<Entry>, preserves createCopyBasedOnChanges()
  EntryChangedEvent       → deprecated thin wrapper, extends CalendarItemDataEvent<Entry>
  EntryTimeChangedEvent   → deprecated thin wrapper, extends CalendarItemTimeChangedEvent<Entry>

Out of scope:
  MultipleEntriesEvent / MultipleEntriesDataEvent / MoreLinkClickedEvent — these are
  Entry-only event classes with no CIP counterpart. They remain unchanged.
```

**Key principles:**
- `getEntry()` delegates to `getItem()` — deprecated but preserved
- `applyChangesOnEntry()` delegates to `applyChangesOnItem()` — deprecated but preserved
- `createCopyBasedOnChanges()` preserved on `EntryDataEvent` (used in production code/docs)
- `CalendarItemTimeChangedEvent<T>` fills the missing gap in the CIP hierarchy
- No duplicate `Delta` field — it lives in `CalendarItemTimeChangedEvent<T>` only
- All Entry events take `ObjectNode` in constructors (matching their CIP parents)

---

## Design Decisions

### 1. New: CalendarItemTimeChangedEvent\<T\>

Create `CalendarItemTimeChangedEvent<T> extends CalendarItemDataEvent<T>` — the CIP counterpart
to `EntryTimeChangedEvent`. It holds the `Delta` and sits between `CalendarItemDataEvent` and
both `CalendarItemDroppedEvent` / `CalendarItemResizedEvent`.

This:
- Removes the Delta duplication in CalendarItemDroppedEvent and CalendarItemResizedEvent
- Gives CIP users access to `getDelta()` on both dropped and resized events
- Makes `EntryTimeChangedEvent extends CalendarItemTimeChangedEvent<Entry>` natural (no diamond problem)
- Keeps the hierarchy symmetrical between Entry and CIP

### 2. Entry Events Keep @DomEvent (CIP Events Keep @DomEvent Too)

Both the parent CIP event and the child Entry event have `@DomEvent` with the same event name.
Vaadin's `ComponentEventBus` creates instances of the **exact registered class**, so:
- `addEntryClickedListener()` → registers `EntryClickedEvent.class` → fires `EntryClickedEvent`
- `addCalendarItemClickedListener()` → registers `CalendarItemClickedEvent.class` → fires `CalendarItemClickedEvent`

Both work independently. Since `EntryClickedEvent extends CalendarItemClickedEvent<Entry>`,
an `EntryClickedEvent` IS-A `CalendarItemClickedEvent<Entry>`, but the listener dispatch is
by exact class, so there's no double-firing.

**Double-instantiation note:** If both listener types are registered on the same calendar,
a single DOM event will create **two separate Java event objects** (one `EntryClickedEvent`,
one `CalendarItemClickedEvent`), each resolving the item from cache independently. This is
expected behavior — two listeners, two event instances. This should be documented.

### 3. getEntry() / applyChangesOnEntry() Convenience Methods

Deprecated convenience methods that delegate to the CIP equivalents:
```java
/** @deprecated Use {@link #getItem()} */
@Deprecated(since = "7.2", forRemoval = false)
public Entry getEntry() { return getItem(); }

/** @deprecated Use {@link #applyChangesOnItem()} */
@Deprecated(since = "7.2", forRemoval = false)
public void applyChangesOnEntry() { applyChangesOnItem(); }
```

Note: `applyChangesOnEntry()` previously called `entry.updateFromJson(jsonObject)` directly.
After Phase 7, `applyChangesOnItem()` delegates to `FullCalendar.applyCalendarItemChanges()`,
which uses the EntryUpdateHandler that calls `entry.updateFromJson(changes.getRawJson())`.
The `getRawJson()` returns the same `jsonObject` — behavior is identical.

### 4. Intermediate Entry Classes Become Thin Wrappers

`EntryEvent`, `EntryDataEvent`, `EntryChangedEvent`, and `EntryTimeChangedEvent` become
deprecated thin wrappers that extend their CIP counterparts. They exist solely for backward
compatibility with code that references them as types.

**Field removal:** Each wrapper **removes its own field** and **removes `@Getter` from the class**
(or excludes the field from Lombok). The parent CIP class provides the field and getter.
Specifically:
- `EntryEvent`: remove `private final Entry entry` field → `getEntry()` becomes manual delegation to `getItem()`
- `EntryDataEvent`: remove `private final ObjectNode jsonObject` field → `getJsonObject()` inherited from parent
- `EntryTimeChangedEvent`: remove `private final Delta delta` field → `getDelta()` inherited from parent

### 5. createCopyBasedOnChanges() Preserved

`EntryDataEvent.createCopyBasedOnChanges()` is used in production code and documented in
`Samples.md`. It is **preserved** on `EntryDataEvent` (which now extends
`CalendarItemDataEvent<Entry>`). The method uses `getEntry().copy()` + `updateFromJson()` —
both still available.

### 6. EntryChangedEvent → Deprecated Thin Wrapper

`EntryChangedEvent` is only extended by `EntryTimeChangedEvent`. It has no external references
and no unique behavior (empty abstract class). It becomes a deprecated thin wrapper extending
`CalendarItemDataEvent<Entry>`:

```java
/** @deprecated Use CalendarItemDataEvent<Entry> or CalendarItemTimeChangedEvent<Entry>. */
@Deprecated(since = "7.2", forRemoval = true)
public abstract class EntryChangedEvent extends CalendarItemDataEvent<Entry> { ... }
```

### 7. instanceof Pattern Changes (Known Breakage)

After Phase 8, `EntryDroppedEvent` extends `CalendarItemDroppedEvent<Entry>` (not
`EntryTimeChangedEvent`). This means:
- `event instanceof EntryTimeChangedEvent` → **no longer matches** EntryDroppedEvent/EntryResizedEvent
- `event instanceof EntryChangedEvent` → **no longer matches** EntryDroppedEvent/EntryResizedEvent
- `event instanceof EntryDataEvent` → **no longer matches** EntryDroppedEvent/EntryResizedEvent

**Instead, use:**
- `event instanceof CalendarItemTimeChangedEvent` → matches both dropped and resized
- `event instanceof CalendarItemDataEvent` → matches all data events

This is documented in the migration guide (Phase 6 already has a migration section to update).

### 8. EntryDroppedSchedulerEvent — Resource Logic

`EntryDroppedSchedulerEvent.applyChangesOnEntry()` currently overrides to also update
resource associations (`updateResourcesFromEventResourceDelta()`). After Phase 8:
- `EntryDroppedSchedulerEvent` extends `CalendarItemDroppedSchedulerEvent<Entry>`
- `applyChangesOnEntry()` is preserved as a non-trivial deprecated method that:
  1. Calls `applyChangesOnItem()` (delegates to Entry.updateFromJson via Phase 7 handler)
  2. Calls `updateResourcesFromEventResourceDelta()` for resource changes
- This is NOT a simple delegation — it adds resource logic on top

---

## Steps

### Step 1: Create CalendarItemTimeChangedEvent\<T\>

**New file:** `addon/src/main/java/.../CalendarItemTimeChangedEvent.java`

```java
@Getter
@ToString(callSuper = true)
public abstract class CalendarItemTimeChangedEvent<T> extends CalendarItemDataEvent<T> {

    private final Delta delta;

    protected CalendarItemTimeChangedEvent(FullCalendar<T> source, boolean fromClient,
            ObjectNode jsonItem, ObjectNode jsonDelta) {
        super(source, fromClient, jsonItem);
        this.delta = Delta.fromJson(jsonDelta);
    }
}
```

No `@DomEvent` — this is abstract. The `@DomEvent` stays on the concrete subclasses
(`CalendarItemDroppedEvent`, `CalendarItemResizedEvent`).

### Step 2: Update CalendarItemDroppedEvent and CalendarItemResizedEvent

Change both from `extends CalendarItemDataEvent<T>` to `extends CalendarItemTimeChangedEvent<T>`.
**Remove the `delta` field** and `Delta.fromJson()` call — now inherited from parent.
**Remove `@Getter` from the class** (or remove just the field) — `getDelta()` is inherited.

```java
@DomEvent("eventDrop")
@ToString(callSuper = true)
public class CalendarItemDroppedEvent<T> extends CalendarItemTimeChangedEvent<T> {
    public CalendarItemDroppedEvent(FullCalendar<T> source, boolean fromClient,
            @EventData("event.detail.data") ObjectNode jsonItem,
            @EventData("event.detail.delta") ObjectNode jsonDelta) {
        super(source, fromClient, jsonItem, jsonDelta);
    }
    // getDelta() inherited from CalendarItemTimeChangedEvent
}
```

Same for `CalendarItemResizedEvent<T>`.

### Step 3: Verify CalendarItemDroppedSchedulerEvent

`CalendarItemDroppedSchedulerEvent<T>` extends `CalendarItemDroppedEvent<T>` — no changes needed
(it inherits the new hierarchy automatically). Its own `oldResource`/`newResource` fields are
unaffected.

### Step 4: Restructure EntryEvent → CalendarItemEvent\<Entry\>

Remove `@Getter` from class. Remove `private final Entry entry` field.
Add manual `getEntry()` delegation.

```java
/** @deprecated Use {@link CalendarItemEvent} directly. */
@Deprecated(since = "7.2", forRemoval = false)
@ToString
public abstract class EntryEvent extends CalendarItemEvent<Entry> {
    public EntryEvent(FullCalendar<Entry> source, boolean fromClient, String entryId) {
        super(source, fromClient, entryId);
    }
    /** @deprecated Use {@link #getItem()} */
    @Deprecated(since = "7.2", forRemoval = false)
    public Entry getEntry() { return getItem(); }
}
```

### Step 5: Restructure EntryClickedEvent → CalendarItemClickedEvent\<Entry\>

**Constructor keeps ObjectNode** (matching CIP parent's signature):

```java
@DomEvent("eventClick")
@ToString(callSuper = true)
public class EntryClickedEvent extends CalendarItemClickedEvent<Entry> {
    public EntryClickedEvent(FullCalendar<Entry> source, boolean fromClient,
            @EventData("event.detail.data") ObjectNode entryData) {
        super(source, fromClient, entryData);
    }
    /** @deprecated Use {@link #getItem()} */
    @Deprecated(since = "7.2", forRemoval = false)
    public Entry getEntry() { return getItem(); }
}
```

Note: EntryClickedEvent currently takes `ObjectNode entryData` and passes it to
`EntryDataEvent(source, fromClient, jsonObject)` which extracts the ID from JSON. The CIP parent
`CalendarItemClickedEvent` also takes `ObjectNode itemData` and passes to `CalendarItemDataEvent`
which extracts `jsonObject.get("id").asString()`. Same constructor pattern — no signature change.

### Step 6: Restructure EntryDataEvent → CalendarItemDataEvent\<Entry\>

Remove `@Getter` from class. Remove `private final ObjectNode jsonObject` field.
**Preserve `createCopyBasedOnChanges()`** and `applyChangesOnEntry()`.

```java
/** @deprecated Use {@link CalendarItemDataEvent} directly. */
@Deprecated(since = "7.2", forRemoval = false)
@ToString(callSuper = true)
public abstract class EntryDataEvent extends CalendarItemDataEvent<Entry> {
    protected EntryDataEvent(FullCalendar<Entry> source, boolean fromClient, ObjectNode jsonObject) {
        super(source, fromClient, jsonObject);
    }
    /** @deprecated Use {@link #getItem()} */
    @Deprecated(since = "7.2", forRemoval = false)
    public Entry getEntry() { return getItem(); }
    /** @deprecated Use {@link #applyChangesOnItem()} */
    @Deprecated(since = "7.2", forRemoval = false)
    public Entry applyChangesOnEntry() {
        return applyChangesOnItem();
    }
    /**
     * Creates a copy based on the referenced entry and the received data.
     * @param <R> return type
     * @return copy
     */
    @SuppressWarnings("unchecked")
    public <R extends Entry> R createCopyBasedOnChanges() {
        try {
            Entry copy = getItem().copy();
            copy.updateFromJson(getJsonObject());
            return (R) copy;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
```

### Step 7: Restructure EntryChangedEvent → CalendarItemDataEvent\<Entry\>

```java
/** @deprecated Use {@link CalendarItemDataEvent} or {@link CalendarItemTimeChangedEvent}. */
@Deprecated(since = "7.2", forRemoval = true)
@ToString(callSuper = true)
public abstract class EntryChangedEvent extends EntryDataEvent {
    public EntryChangedEvent(FullCalendar<Entry> source, boolean fromClient, ObjectNode jsonObject) {
        super(source, fromClient, jsonObject);
    }
}
```

Note: `EntryChangedEvent extends EntryDataEvent` (not directly `CalendarItemDataEvent<Entry>`)
to preserve `EntryDataEvent`'s `createCopyBasedOnChanges()` and `applyChangesOnEntry()`.
Since `EntryDataEvent extends CalendarItemDataEvent<Entry>`, the chain works.

### Step 8: Restructure EntryTimeChangedEvent → CalendarItemTimeChangedEvent\<Entry\>

Remove `@Getter` from class. Remove `private final Delta delta` field.

```java
/** @deprecated Use {@link CalendarItemTimeChangedEvent} directly. */
@Deprecated(since = "7.2", forRemoval = false)
@ToString(callSuper = true)
public abstract class EntryTimeChangedEvent extends CalendarItemTimeChangedEvent<Entry> {
    public EntryTimeChangedEvent(FullCalendar<Entry> source, boolean fromClient,
            ObjectNode jsonEntry, ObjectNode jsonDelta) {
        super(source, fromClient, jsonEntry, jsonDelta);
    }
    /** @deprecated Use {@link #getItem()} */
    @Deprecated(since = "7.2", forRemoval = false)
    public Entry getEntry() { return getItem(); }
    /** @deprecated Use {@link #applyChangesOnItem()} */
    @Deprecated(since = "7.2", forRemoval = false)
    public Entry applyChangesOnEntry() {
        return applyChangesOnItem();
    }
    // getDelta() inherited from CalendarItemTimeChangedEvent
}
```

Note: `EntryTimeChangedEvent` no longer extends `EntryChangedEvent` → it extends
`CalendarItemTimeChangedEvent<Entry>` directly. This breaks `instanceof EntryChangedEvent`
and `instanceof EntryDataEvent` for dropped/resized events (see Design Decision 7).

### Step 9: Restructure EntryDroppedEvent and EntryResizedEvent

```java
@DomEvent("eventDrop")
@ToString(callSuper = true)
public class EntryDroppedEvent extends CalendarItemDroppedEvent<Entry> {
    public EntryDroppedEvent(FullCalendar<Entry> source, boolean fromClient,
            @EventData("event.detail.data") ObjectNode jsonEntry,
            @EventData("event.detail.delta") ObjectNode jsonDelta) {
        super(source, fromClient, jsonEntry, jsonDelta);
    }
    /** @deprecated Use {@link #getItem()} */
    @Deprecated(since = "7.2", forRemoval = false)
    public Entry getEntry() { return getItem(); }
    /** @deprecated Use {@link #applyChangesOnItem()} */
    @Deprecated(since = "7.2", forRemoval = false)
    public Entry applyChangesOnEntry() {
        return applyChangesOnItem();
    }
    // getDelta() inherited from CalendarItemTimeChangedEvent
}
```

Same for `EntryResizedEvent extends CalendarItemResizedEvent<Entry>`.

Note: `EntryDroppedEvent` no longer extends `EntryTimeChangedEvent` — it extends
`CalendarItemDroppedEvent<Entry>` directly. But it IS-A `CalendarItemTimeChangedEvent<Entry>`
(via the CalendarItemDroppedEvent → CalendarItemTimeChangedEvent chain), so `getDelta()` works.

### Step 10: Restructure Mouse Events

```java
@DomEvent("eventMouseEnter")
@ToString(callSuper = true)
public class EntryMouseEnterEvent extends CalendarItemMouseEnterEvent<Entry> {
    public EntryMouseEnterEvent(FullCalendar<Entry> source, boolean fromClient,
            @EventData("event.detail.data") ObjectNode entryData) {
        super(source, fromClient, entryData);
    }
    /** @deprecated Use {@link #getItem()} */
    @Deprecated(since = "7.2", forRemoval = false)
    public Entry getEntry() { return getItem(); }
}
```

Same for `EntryMouseLeaveEvent extends CalendarItemMouseLeaveEvent<Entry>`.

Note: Constructors take `ObjectNode` (matching CIP parent signature), NOT `String id`.

### Step 11: Restructure EntryDroppedSchedulerEvent

`EntryDroppedSchedulerEvent` extends `CalendarItemDroppedSchedulerEvent<Entry>`:

```java
@DomEvent("eventDrop")
@ToString(callSuper = true)
public class EntryDroppedSchedulerEvent extends CalendarItemDroppedSchedulerEvent<Entry> {
    public EntryDroppedSchedulerEvent(FullCalendar<Entry> source, boolean fromClient,
            @EventData("event.detail.data") ObjectNode jsonEntry,
            @EventData("event.detail.delta") ObjectNode jsonDelta) {
        super(source, fromClient, jsonEntry, jsonDelta);
    }
    /** @deprecated Use {@link #getItem()} */
    @Deprecated(since = "7.2", forRemoval = false)
    public Entry getEntry() { return getItem(); }

    /**
     * Applies the contained changes including resource updates.
     * @deprecated Use {@link #applyChangesOnItem()} for basic changes,
     *             then update resources via the scheduler changes accessor.
     */
    @Deprecated(since = "7.2", forRemoval = false)
    public Entry applyChangesOnEntry() {
        Entry entry = applyChangesOnItem();  // delegates to Entry.updateFromJson via Phase 7 handler
        // Resource delta logic — preserved from existing behavior
        if (entry instanceof ResourceEntry resourceEntry) {
            updateResourcesFromEventResourceDelta(resourceEntry, getJsonObject());
        }
        return entry;
    }

    // static updateResourcesFromEventResourceDelta() — kept as-is
}
```

Note: The `applyChangesOnEntry()` override is NOT a simple delegation — it adds resource
update logic on top of `applyChangesOnItem()`. This is the one case where the deprecated
method does more than just delegate.

### Step 12: Update Listener Methods (Verify)

With Entry events extending CIP events, the listener dispatch works naturally. Verify:
- `addEntryClickedListener()` still works (registers EntryClickedEvent — has @DomEvent)
- `addCalendarItemClickedListener()` works on Entry calendars (registers CalendarItemClickedEvent — has @DomEvent)
- Both fire independently when the DOM event arrives (separate event instances)
- No double-firing (Vaadin creates the exact registered class)

### Step 13: Update Javadoc and Migration Guide

- Update Javadoc on `addEntryClickedListener()` etc. to note that `addCalendarItemClickedListener()`
  is the recommended generic alternative
- Update `docs/Migration-guides.md` to document the `instanceof` pattern changes:
  - `instanceof EntryTimeChangedEvent` → use `instanceof CalendarItemTimeChangedEvent`
  - `instanceof EntryChangedEvent` → use `instanceof CalendarItemDataEvent`

### Step 14: Update Tests

- Test that Entry events are instances of their CIP parents:
  `assertInstanceOf(CalendarItemClickedEvent.class, entryClickedEvent)`
  `assertInstanceOf(CalendarItemTimeChangedEvent.class, entryDroppedEvent)`
- Test that `getEntry()` and `getItem()` return the same object
- Test that `applyChangesOnEntry()` and `applyChangesOnItem()` have the same effect
- Test that CIP listeners fire on Entry-based calendars
- Test that Entry listeners still work unchanged
- Test that `getDelta()` works via CalendarItemTimeChangedEvent on CIP events
- Test that `createCopyBasedOnChanges()` still works on EntryDataEvent subclasses
- Test `EntryDroppedSchedulerEvent.applyChangesOnEntry()` preserves resource updates

### Step 15: Update Demo/Samples

Update migration guide samples to show that on an Entry calendar, both listener types work:
```java
// Both work on a FullCalendar<Entry>:
calendar.addEntryClickedListener(event -> event.getEntry());
calendar.addCalendarItemClickedListener(event -> event.getItem()); // same Entry object

// Delta is available on both CIP and Entry events:
calendar.addCalendarItemDroppedListener(event -> {
    Delta delta = event.getDelta();  // available via CalendarItemTimeChangedEvent<T>
});
```

---

## Verification

```bash
mvn clean install -DskipTests   # compile
mvn test                         # all tests pass
```

After this phase:
- `CalendarItemTimeChangedEvent<T>` provides `getDelta()` for all CIP dropped/resized events
- `EntryClickedEvent` IS-A `CalendarItemClickedEvent<Entry>`
- `EntryDroppedEvent` IS-A `CalendarItemDroppedEvent<Entry>` IS-A `CalendarItemTimeChangedEvent<Entry>`
- `addCalendarItemClickedListener()` works on Entry-based calendars
- `getEntry()` is preserved as deprecated convenience, delegates to `getItem()`
- `applyChangesOnEntry()` is preserved as deprecated convenience, delegates to `applyChangesOnItem()`
- `createCopyBasedOnChanges()` preserved on EntryDataEvent
- Entry events and CIP events share a single hierarchy — no more parallel structures
- No duplicate Delta field anywhere
- `EntryDroppedSchedulerEvent.applyChangesOnEntry()` preserves resource update logic
- `MultipleEntriesEvent` / `MoreLinkClickedEvent` are out of scope (unchanged)
