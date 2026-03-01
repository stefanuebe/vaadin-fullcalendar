# CIP Requirements Review — Gaps & Recommendations

Source: `/workspace/calendar-item-provider.md`

## Verdict: GAPS IDENTIFIED AND RESOLVED

The core idea is sound and addresses a real user pain point. The original document covered
the server-to-client direction well. Gaps in client-to-server updates, event system, and
POJO identity have been resolved through architectural decisions documented in STATUS.md.

---

## Gap Status

### 1. Client-to-Server Updates — RESOLVED

**Original gap:** The event system depends on `Entry` objects. Drag/drop/resize send JSON
back to server, resolved via `getCachedEntryFromFetch()` + `Entry.updateFromJson()`.

**Resolution:** A+B strategy with mutual exclusion:
- **Strategy A:** Optional setters on `CalendarItemPropertyMapper` for `start`, `end`, `allDay`
  (and `resourceIds` in scheduler). `event.applyChangesOnItem()` mutates POJO in-place.
- **Strategy B:** `CalendarItemUpdateHandler<T>` callback receives the POJO + typed changes.
  Works for immutable objects.
- If both are configured, an exception is thrown at configuration time.

### 2. POJO Identity / ID Mapping — RESOLVED

**Original gap:** No `id` mapping mentioned in requirements doc.

**Resolution:** `id` is a mandatory mapping. `CalendarItemPropertyMapper.validate()` throws
if not set. Used for caching, event resolution, and client-server round-trips.

### 3. Incomplete Property List — RESOLVED

**Original gap:** Only `title`, `color`, `startDate`, `endDate` shown.

**Resolution:** Full property list documented. 20+ Entry properties supported in mapper,
plus 2 scheduler-specific properties. Unmapped properties omitted from JSON (FullCalendar
JS uses its own defaults).

Bidirectional (client can update): `start`, `end`, `allDay`, `resourceIds` (scheduler)
Read-only (server->client): all others (`id`, `title`, `color`, `editable`, recurrence, etc.)

### 4. JSON Serialization Strategy — RESOLVED

**Original gap:** Who builds the JSON for POJOs?

**Resolution:** `CalendarItemPropertyMapper.toJson(T item)` produces the `ObjectNode`.
Reuses existing `JsonItemPropertyConverter` infrastructure for type conversion
(LocalDateTime, DayOfWeek, RecurringTime, etc.).

### 5. String-Based Property Mapping Safety — NOTED

**Original gap:** Validation timing and error reporting.

**Resolution:** Validate at mapper construction time via `BeanProperties.read()`.
No nested properties in v1. Clear error messages for type mismatches.
To be finalized during spike 0.1 (mapper prototype).

### 6. CalendarQuery Filtering for POJOs — RESOLVED

**Original gap:** `EntryQuery.applyFilter()` is deeply Entry-coupled.

**Resolution:** `CalendarQuery` provides only time range (start, end). No allDay filter.
Callback providers do their own filtering. In-memory providers use mapper's start/end
accessors for time-range filtering.

### 7. Entry.setCalendar() / knownToTheClient Equivalent — RESOLVED

**Original gap:** POJOs have no lifecycle methods.

**Resolution:** Bookkeeping stays in `FullCalendar<T>` internal state. POJOs are stored
in the cache (`lastFetchedItems`) keyed by their mapped ID. No lifecycle interface required.

### 8. InMemoryCalendarItemProvider Identity — RESOLVED

**Original gap:** How to extract IDs from POJOs for in-memory storage.

**Resolution:** `InMemoryCalendarItemProvider` accepts a `SerializableFunction<T, String>`
ID extractor at construction. Alternatively, uses the mapper's ID provider.

### 9. Event System Strategy — RESOLVED

**Original gap:** How do Entry-based events work with arbitrary POJOs?

**Resolution:** Parallel typed event hierarchy. No raw types:
- `EntryEvent extends ComponentEvent<FullCalendar<Entry>>` — existing, updated
- `CalendarItemEvent<T> extends ComponentEvent<FullCalendar<T>>` — new parallel hierarchy
- CIP events fired via direct DOM event listeners (not @DomEvent bridge)
- `CalendarItemDataEvent.applyChangesOnItem()` delegates to Strategy A or B

### 10. "etc" in Additional Types — RESOLVED

**Original gap:** Incomplete list of types to generalize.

**Resolution:** Full type mapping:
- `EntryQuery` → `CalendarQuery` (EntryQuery extends CalendarQuery)
- `EntriesChangeEvent` → `CalendarItemsChangeEvent<T>`
- `EntryRefreshEvent` → `CalendarItemRefreshEvent<T>`
- `EntryProvider` → `CalendarItemProvider<T>` (EntryProvider extends CalendarItemProvider)
- `InMemoryEntryProvider` → `InMemoryCalendarItemProvider<T>`
- `CallbackEntryProvider` → `CallbackCalendarItemProvider<T>`
- `AbstractEntryProvider` → `AbstractCalendarItemProvider<T>`
- Entry events → CalendarItem events (parallel hierarchy, not replacement)
