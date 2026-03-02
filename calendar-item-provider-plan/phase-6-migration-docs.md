# Phase 6: Migration, Documentation & Demo

**Goal:** Finalize deprecations, write migration guide, update all documentation, add demos.
**Prerequisite:** Phases 1–5 complete.
**Breaking changes:** None — deprecation warnings only.

---

## Context

With all CIP functionality implemented, this phase focuses on documentation, demos, and
ensuring a smooth migration path. `setEntryProvider()` is deprecated but not removed —
Entry-based calendars remain a valid, convenient use case.

---

## Deprecations

### In FullCalendar<T>

```java
/** @deprecated Use {@link #setCalendarItemProvider} for custom POJOs,
  * or continue using this method for Entry-based calendars. */
@Deprecated(since = "7.1", forRemoval = false)
public void setEntryProvider(EntryProvider<? extends Entry> entryProvider) { ... }
```

**`forRemoval = false`** — EntryProvider is a valid use case and remains as a convenience.
The deprecation signals that CIP is the recommended approach for new code with custom models.

### In FullCalendarBuilder<T>

```java
/** @deprecated Use {@link #withCalendarItemProvider} */
@Deprecated(since = "7.1", forRemoval = false)
public FullCalendarBuilder<T> withEntryProvider(EntryProvider<? extends Entry> provider) { ... }
```

---

## Documentation Updates

### 1. `docs/Features.md`
Add section:
- **Calendar Item Provider** — overview and motivation
- When to use CIP vs EntryProvider
- CalendarItemPropertyMapper overview
- Update strategies (setters vs handler)

### 2. `docs/Samples.md`
Add code samples:
- Basic CIP with a JPA entity (lambda mapping)
- CIP with string-based mapping (reflection)
- CIP with callback provider (lazy loading from database)
- CIP with in-memory provider
- CIP with scheduler (resource mapping via `SchedulerCalendarItemPropertyMapper`)
- CIP event handling (click, drag, resize)
- CIP with update handler (Strategy B, for immutable objects)
- Migration from EntryProvider to CIP (side-by-side comparison)

### 3. `docs/Migration-guides.md`
Add migration section for 7.0 → 7.1 (or whatever version ships CIP):
- Raw type warnings: what they mean, how to fix (`FullCalendar<Entry>`)
- Step-by-step migration from Entry to POJO
- Mapping reference: complete table of all properties with types
- Event handling: `addEntryClickedListener` → `addCalendarItemClickedListener`
- Scheduler: `ResourceEntry` → POJO with `SchedulerCalendarItemPropertyMapper`
- Update strategies: when to use setters vs handler

### 4. `docs/Home.md`
Update documentation index with CIP section link.

### 5. MCP Server Documentation
Update **both** `mcp-server/README.md` AND `docs/MCP-Server.md` (keep in sync per CLAUDE.md).
Add CIP-related API reference, code examples, and documentation sections to the MCP server.

### 6. Javadoc
Ensure all new public classes and methods have thorough Javadoc:
- `CalendarItemPropertyMapper` — all builder methods, type conversion rules, validation
- `CalendarItemProvider` — contract, factory methods, comparison to EntryProvider
- `CalendarItemUpdateHandler` — when to use, mutual exclusion with setters
- `CalendarItemChanges` — available change accessors
- All CIP events — what triggers them, how to use getItem/applyChangesOnItem
- Scheduler extensions — SchedulerCalendarItemPropertyMapper, resource mapping

---

## Demo Application

Add demo views in `demo/src/main/java/.../`:

### `CalendarItemProviderBasicDemo.java`
- Simple POJO with `CalendarItemPropertyMapper` (lambda-based)
- In-memory provider with a few sample items
- Click listener showing item details in a Notification

### `CalendarItemProviderCallbackDemo.java`
- Callback provider simulating backend database access
- Date-range filtering in the callback
- Drag-and-drop with Strategy A (mapper setters)

### `CalendarItemProviderUpdateHandlerDemo.java`
- Immutable record as calendar item
- Strategy B: `CalendarItemUpdateHandler` that creates new instances
- Shows that immutable objects work with CIP

### `CalendarItemProviderSchedulerDemo.java`
- POJO with resource ID field
- `SchedulerCalendarItemPropertyMapper` with `resourceIds` mapping
- Timeline view with multiple resources
- Drag-drop between resources updates the POJO

---

## Release Notes

Add to `docs/Release-notes.md` and create `docs/release-notes-detail/Release-notes-7.1.md`:
- Feature announcement with motivation
- API overview (CalendarItemProvider, CalendarItemPropertyMapper, events)
- Generic FullCalendar<T> change and what it means for existing code
- Update strategies (A + B)
- Scheduler support
- Links to documentation and samples
- Known limitations in v1 (if any remain)

---

## Testing

- All demo views render correctly and are interactive
- Documentation code samples compile (extract to test if feasible)
- Migration guide steps are accurate (test with a sample project)

## Completion Criteria

- All new APIs have Javadoc
- Documentation covers all CIP features with code examples
- Demo application has 4 working CIP views
- Migration guide is complete and tested
- MCP server docs updated and in sync
- Release notes written
- Spike code cleaned up (removed from test sources — it was retained until this point)
