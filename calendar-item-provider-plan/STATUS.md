# Calendar Item Provider (CIP) — Implementation Status

## Overall Status: PLANNING

| Phase | Name | Status | Description |
|-------|------|--------|-------------|
| 0 | Spikes | NOT STARTED | Mapper prototype + generic component runtime verification |
| 1 | Foundation Types | NOT STARTED | New CIP types without touching existing code |
| 2 | Adapter Layer | NOT STARTED | Wire existing EntryProvider into CIP hierarchy |
| 3 | Core Integration | NOT STARTED | Make FullCalendar generic, connect CIP |
| 4 | Event System | NOT STARTED | Typed parallel event hierarchy for CIP |
| 5 | Scheduler Extension | NOT STARTED | Integrate CIP with addon-scheduler |
| 6 | Migration & Docs | NOT STARTED | Deprecations, migration guide, documentation |

## Key Architectural Decisions

### 1. Generic `FullCalendar<T>` — CONFIRMED
`FullCalendar` becomes `FullCalendar<T>`. Vaadin's `@DomEvent` mechanism uses reflection on
erased types — generic parameters do not interfere at runtime. Existing code using raw
`FullCalendar` gets compiler warnings but no breakage.

### 2. Generic `FullCalendarScheduler<T>` — CONFIRMED
`FullCalendarScheduler` becomes `FullCalendarScheduler<T> extends FullCalendar<T>`.
Existing Entry-based usage becomes `FullCalendarScheduler<ResourceEntry>`. CIP usage
becomes `FullCalendarScheduler<MyPojo>`. Resource management (addResources, getResources,
etc.) is item-type-agnostic and stays unchanged.

### 3. Typed Event Hierarchy — CONFIRMED
- `EntryEvent extends ComponentEvent<FullCalendar<Entry>>` — existing events, no raw types
- `CalendarItemEvent<T> extends ComponentEvent<FullCalendar<T>>` — new parallel hierarchy
- Non-entry events (timeslot, dates, etc.) use `ComponentEvent<FullCalendar<T>>`
- Scheduler events: `EntryDroppedSchedulerEvent extends ComponentEvent<FullCalendarScheduler<Entry>>`
  and new `CalendarItemDroppedSchedulerEvent<T> extends ComponentEvent<FullCalendarScheduler<T>>`

### 4. Client-to-Server Updates: A+B with Mutual Exclusion — CONFIRMED
Two mechanisms for applying client-side changes (drag/drop/resize) back to POJOs:

**Strategy A — Setters on mapper (opt-in per property):**
```java
var mapper = CalendarItemPropertyMapper.of(MyPojo.class)
    .start(MyPojo::getFrom, MyPojo::setFrom)    // getter + setter
    .end(MyPojo::getTo, MyPojo::setTo);          // getter + setter
// Then: event.applyChangesOnItem() mutates the POJO in-place
```

**Strategy B — Explicit update handler:**
```java
calendar.setCalendarItemUpdateHandler((item, changes) -> {
    // User applies changes manually (works for immutable objects too)
});
```

**Mutual exclusion:** If both setters AND an update handler are registered, an exception
is thrown at configuration time forcing the developer to choose one approach.

### 5. EntryClickedListener on CIP Calendar — Javadoc Warning Only
No runtime guard. Registering an `addEntryClickedListener()` on a CIP calendar will cause
`IllegalArgumentException` when the event fires (cache contains POJOs, not Entries).
This is documented in Javadoc as a warning. A runtime guard is not feasible because listeners
can be registered before the provider is set.

## Open Questions (to be resolved by Spikes)

See [phase-0-spikes.md](phase-0-spikes.md) for remaining spike work.

## File Index

- [requirements-review.md](requirements-review.md) — Gap analysis of original requirements
- [phase-0-spikes.md](phase-0-spikes.md) — Mapper prototype + runtime verification
- [phase-1-foundation.md](phase-1-foundation.md) — New standalone CIP types
- [phase-2-adapter.md](phase-2-adapter.md) — EntryProvider extends CalendarItemProvider wiring
- [phase-3-core-integration.md](phase-3-core-integration.md) — Generic FullCalendar<T>, CIP connection
- [phase-4-events.md](phase-4-events.md) — Typed parallel event hierarchy
- [phase-5-scheduler.md](phase-5-scheduler.md) — Scheduler generics + CIP integration
- [phase-6-migration-docs.md](phase-6-migration-docs.md) — Deprecations, docs, demos
