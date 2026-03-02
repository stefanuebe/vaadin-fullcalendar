This page gives you an overview of the major changes that came with the release of
[FullCalendar for Flow, version 7.2](https://vaadin.com/directory/component/full-calendar-flow).

The main addition in version 7.2 is the **Calendar Item Provider (CIP)**, a new way to display
arbitrary POJOs on the calendar without extending `Entry`.

## Calendar Item Provider (CIP)

### Motivation
Previously, all calendar items had to be instances of `Entry` (or `ResourceEntry`). This required
mapping your domain objects to Entry and back, which adds boilerplate and makes it harder to
integrate with existing JPA entities, records, or DTOs.

With CIP, you define a `CalendarItemPropertyMapper` that tells the calendar how to read (and
optionally write) your POJO's properties, and a `CalendarItemProvider` that supplies the data.

### API overview

- **`CalendarItemPropertyMapper<T>`** — fluent builder mapping POJO properties to FullCalendar JSON
  (`id`, `title`, `start`, `end`, `color`, `allDay`, `editable`, recurring properties, resource IDs, etc.)
- **`CalendarItemProvider<T>`** — data provider interface with factory methods:
  - `CalendarItemProvider.fromCallbacks(fetch, fetchById)` — callback-based (lazy loading)
  - `CalendarItemProvider.inMemoryFrom(idExtractor, items)` — in-memory
  - `CalendarItemProvider.emptyInMemory(idExtractor)` — empty in-memory
- **`CalendarItemUpdateHandler<T>`** — optional handler for applying drag/drop/resize changes
- **`CalendarItemChanges`** — typed wrapper around the client-sent delta JSON

### Generic FullCalendar\<T\>

`FullCalendar` is now `FullCalendar<T>`, where `T` is the item type. For Entry-based calendars,
this is `FullCalendar<Entry>`. Existing code using raw `FullCalendar` produces compiler warnings
but continues to compile and run without changes.

`FullCalendarScheduler` is likewise `FullCalendarScheduler<T>`.

### Update strategies

Two strategies for applying client-side changes (drag/drop/resize) back to POJOs:

**Strategy A — Setters on mapper:** Register setters alongside getters. Then call
`event.applyChangesOnItem()` to mutate the POJO in-place.

**Strategy B — Update handler:** Register a `CalendarItemUpdateHandler` via
`calendar.setCalendarItemUpdateHandler(...)`. Full control for immutable objects or custom logic.

Only one strategy can be active. If both setters and a handler are registered, an exception
is thrown at configuration time.

### Typed event hierarchy

New parallel event hierarchy for CIP:
- `CalendarItemClickedEvent<T>`
- `CalendarItemDroppedEvent<T>`
- `CalendarItemResizedEvent<T>`
- `CalendarItemMouseEnterEvent<T>` / `CalendarItemMouseLeaveEvent<T>`
- `CalendarItemDroppedSchedulerEvent<T>` (scheduler extension)

### Scheduler support

CIP works with scheduler/resource views. Map `resourceIds` on the mapper and use
`CalendarItemDroppedSchedulerEvent` for resource-aware drag/drop events.

### Deprecations

`setEntryProvider()` and `FullCalendarBuilder.withEntryProvider()` are deprecated with
`forRemoval = false`. Entry-based calendars remain fully supported.

## Links
- [Code samples](Samples.md#calendar-item-provider-cip)
- [Migration guide](Migration-guides.md#migrating-from-71--72)
- [Feature overview](Features.md#calendar-item-provider-cip)
