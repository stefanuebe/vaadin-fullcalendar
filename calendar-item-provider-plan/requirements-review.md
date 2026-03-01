# CIP Requirements Review — Gaps & Resolutions

Source: `/workspace/calendar-item-provider.md`

## Verdict: ALL GAPS RESOLVED

The core idea is sound and addresses a real user pain point (avoiding Entry translation overhead).
The original document covered the server-to-client direction. All gaps in client-to-server
updates, event system, POJO identity, and scheduler integration have been resolved through
architectural decisions documented in STATUS.md.

---

## Gap 1: Client-to-Server Updates — RESOLVED

**Original gap:** The event system depends on `Entry` objects. Drag/drop/resize send JSON
back to server via `getCachedEntryFromFetch()` + `Entry.updateFromJson()`. No POJO equivalent.

**Resolution:** A+B strategy with mutual exclusion:
- **Strategy A:** Optional setters on `CalendarItemPropertyMapper` for `start`, `end`, `allDay`
  (and `resourceIds` in scheduler). `event.applyChangesOnItem()` mutates POJO in-place.
- **Strategy B:** `CalendarItemUpdateHandler<T>` callback receives POJO + `CalendarItemChanges`.
  Works for immutable objects (records, value objects).
- If both are configured → `IllegalStateException` at configuration time.

**Phase:** 1 (types), 3 (integration)

## Gap 2: POJO Identity / ID Mapping — RESOLVED

**Original gap:** No `id` mapping mentioned in requirements doc.

**Resolution:** `id` is a mandatory mapping. `CalendarItemPropertyMapper.validate()` throws
`IllegalStateException` if not set. ID used for caching, event resolution, client-server roundtrips.

**Phase:** 1

## Gap 3: Incomplete Property List — RESOLVED

**Original gap:** Only `title`, `color`, `startDate`, `endDate` shown in examples.

**Resolution:** Full property list supported (22 core + 2 scheduler):
- **Bidirectional** (client can update): `start`, `end`, `allDay` + `resourceIds` (scheduler)
- **Read-only** (server→client): `id`, `groupId`, `title`, `editable`, `startEditable`,
  `durationEditable`, `color`, `backgroundColor`, `borderColor`, `textColor`, `constraint`,
  `overlap`, `displayMode`, `classNames`, `customProperties`, `recurringStartDate`,
  `recurringEndDate`, `recurringStartTime`, `recurringEndTime`, `recurringDaysOfWeek`,
  `resourceEditable` (scheduler)
- Unmapped properties omitted from JSON (FullCalendar JS uses defaults).

**Phase:** 1, 5 (scheduler properties)

## Gap 4: JSON Serialization Strategy — RESOLVED

**Original gap:** Who builds the JSON for POJOs? How are type conversions handled?

**Resolution:** `CalendarItemPropertyMapper.toJson(T item)` produces the `ObjectNode`.
Reuses existing `JsonItemPropertyConverter` infrastructure: `LocalDateTimeConverter`,
`LocalDateConverter`, `RecurringTimeConverter`, `DayOfWeekItemConverter`. Null values omitted.

**Phase:** 1

## Gap 5: String-Based Property Mapping Safety — DEFERRED TO SPIKE

**Original gap:** When does validation happen? What about nested properties?

**Resolution:** Validate at mapper construction time via `BeanProperties.read()`.
No nested properties in v1. Details finalized by spike 0.1.

**Phase:** 0 (spike), 1

## Gap 6: CalendarQuery Filtering for POJOs — RESOLVED

**Original gap:** `EntryQuery.applyFilter()` is deeply Entry-coupled.

**Resolution:** `CalendarQuery` provides only time range (start, end). No `allDay` filter.
`EntryQuery extends CalendarQuery` retains Entry-specific filtering. Callback providers
do their own filtering. In-memory providers use mapper's start/end for time-range filtering.

**Phase:** 1, 2

## Gap 7: Entry.setCalendar() / knownToTheClient Equivalent — RESOLVED

**Original gap:** POJOs have no lifecycle methods.

**Resolution:** No lifecycle interface required for POJOs. Bookkeeping stays internal to
`FullCalendar<T>`: POJOs stored in `lastFetchedItems` cache keyed by mapped ID.
No `setCalendar()` or `setKnownToTheClient()` needed.

**Phase:** 3

## Gap 8: InMemoryCalendarItemProvider Identity — RESOLVED

**Original gap:** How to extract IDs from POJOs for in-memory storage.

**Resolution:** `InMemoryCalendarItemProvider<T>` takes a `SerializableFunction<T, String>`
ID extractor at construction. Factory methods: `CalendarItemProvider.emptyInMemory(idExtractor)`.

**Phase:** 1

## Gap 9: Event System Strategy — RESOLVED

**Original gap:** How do events work with arbitrary POJOs instead of Entry?

**Resolution:** Parallel typed event hierarchy with no raw types:
- `EntryEvent extends ComponentEvent<FullCalendar<Entry>>` — existing, updated
- `CalendarItemEvent<T> extends ComponentEvent<FullCalendar<T>>` — new parallel hierarchy
- Non-entry events use `ComponentEvent<FullCalendar<?>>` — work in both modes
- CIP events fired via direct element DOM listeners (not @DomEvent bridge)
- `CalendarItemDataEvent.applyChangesOnItem()` delegates to Strategy A or B
- Registering entry listeners on CIP calendar: documented warning in Javadoc only,
  no runtime guard (listeners can be registered before provider is set)

**Phase:** 4

## Gap 10: "etc" in Additional Types — RESOLVED

**Original gap:** Incomplete list of types needing generic counterparts.

**Resolution:** Complete mapping:

| Existing Type | New Generic Type | Relationship |
|--------------|-----------------|--------------|
| `EntryProvider<T extends Entry>` | `CalendarItemProvider<T>` | EntryProvider extends CalendarItemProvider |
| `EntryQuery` | `CalendarQuery` | EntryQuery extends CalendarQuery |
| `InMemoryEntryProvider` | `InMemoryCalendarItemProvider<T>` | Parallel |
| `CallbackEntryProvider` | `CallbackCalendarItemProvider<T>` | Parallel |
| `AbstractEntryProvider` | `AbstractCalendarItemProvider<T>` | AbstractEntryProvider extends AbstractCalendarItemProvider |
| `EntriesChangeEvent` | `CalendarItemsChangeEvent<T>` | EntriesChangeEvent extends CalendarItemsChangeEvent |
| `EntryRefreshEvent` | `CalendarItemRefreshEvent<T>` | EntryRefreshEvent extends CalendarItemRefreshEvent |
| `EntryEvent` | `CalendarItemEvent<T>` | Parallel hierarchy |
| `EntryClickedEvent` | `CalendarItemClickedEvent<T>` | Parallel |
| `EntryDroppedEvent` | `CalendarItemDroppedEvent<T>` | Parallel |
| `EntryResizedEvent` | `CalendarItemResizedEvent<T>` | Parallel |
| — | `CalendarItemPropertyMapper<T>` | New (no Entry equivalent) |
| — | `CalendarItemUpdateHandler<T>` | New (no Entry equivalent) |
| — | `CalendarItemChanges` | New (no Entry equivalent) |
| — | `SchedulerCalendarItemPropertyMapper<T>` | New (no Entry equivalent) |
| — | `CalendarItemDroppedSchedulerEvent<T>` | Parallel to EntryDroppedSchedulerEvent |

**Phase:** 1, 2, 4, 5
