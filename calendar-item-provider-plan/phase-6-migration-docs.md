# Phase 6: Migration, Documentation & Demo

**Goal:** Finalize deprecations, write migration guide, update documentation, add demo samples.
**Prerequisite:** Phases 1–5 complete.
**Breaking changes:** None — deprecation warnings only.

---

## Deprecations

Mark old APIs as deprecated with clear migration pointers:

```java
// FullCalendar.java:
/** @deprecated Use {@link #setCalendarItemProvider} for custom POJOs,
  * or continue using this method for Entry-based calendars. */
@Deprecated(since = "7.2", forRemoval = false)
public void setEntryProvider(EntryProvider<? extends Entry> entryProvider) { ... }
```

**Note:** `forRemoval = false` — EntryProvider is a valid use case and should remain
as a convenience API. Deprecation signals that CIP is the preferred approach for new code.

---

## Documentation Updates

### 1. `docs/Features.md`
Add section on Calendar Item Provider with overview and motivation.

### 2. `docs/Samples.md`
Add code samples:
- Basic CIP with a JPA entity
- CIP with callback provider (lazy loading from database)
- CIP with in-memory provider
- CIP with Scheduler (resource-based views)
- CIP event handling (click, drag, resize)
- Migration from EntryProvider to CIP

### 3. `docs/Migration-guides.md`
Add migration guide for 7.x -> CIP:
- When to use CIP vs EntryProvider
- Step-by-step migration from Entry to POJO
- Mapping reference (all available properties)
- Event handling differences

### 4. `docs/Home.md`
Update documentation index with CIP section.

### 5. MCP Server Documentation
Update `mcp-server/README.md` AND `docs/MCP-Server.md` (keep in sync per CLAUDE.md).

### 6. Wiki
Update https://github.com/stefanuebe/vaadin-fullcalendar/wiki with CIP documentation.

---

## Demo Application

Add demo views in `demo/` module:

### `CalendarItemProviderDemo.java`
- Shows a calendar using a simple POJO with CalendarItemPropertyMapper
- Demonstrates lambda-based and string-based mapping
- Shows event handling with CIP

### `CalendarItemProviderSchedulerDemo.java`
- Shows CIP with scheduler views and resource mapping
- Uses SchedulerCalendarItemPropertyMapper

### `CalendarItemProviderCallbackDemo.java`
- Shows lazy loading with CallbackCalendarItemProvider
- Demonstrates pagination/date-range filtering

---

## Release Notes

Add to `docs/Release-notes.md` and `docs/release-notes-detail/Release-notes-7.x.md`:
- Feature announcement
- API overview
- Links to documentation
- Known limitations in v1

---

## Completion Criteria

- All new APIs have Javadoc
- Documentation covers all CIP features
- Demo application has working CIP examples
- Migration guide is complete and accurate
- MCP server docs updated and in sync
