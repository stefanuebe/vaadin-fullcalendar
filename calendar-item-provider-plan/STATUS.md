# Calendar Item Provider (CIP) — Implementation Status

## Overall Status: PLANNING

| Phase | Name | Status | Description |
|-------|------|--------|-------------|
| 0 | Spikes | NOT STARTED | Mapper prototype + generic component runtime verification |
| 1 | Foundation Types | NOT STARTED | New CIP types without touching existing code |
| 2 | Adapter Layer | NOT STARTED | Wire existing EntryProvider into CIP hierarchy |
| 3 | Core Integration | NOT STARTED | Connect CIP to FullCalendar<T> component |
| 4 | Event System | NOT STARTED | Typed parallel event hierarchy for CIP |
| 5 | Scheduler Extension | NOT STARTED | Integrate CIP with addon-scheduler |
| 6 | Migration & Docs | NOT STARTED | Deprecations, migration guide, documentation |

## Key Architectural Decisions

### 1. Generic `FullCalendar<T>` — CONFIRMED
`FullCalendar` becomes `FullCalendar<T>`. Vaadin's `@DomEvent` mechanism uses reflection on
erased types — generic parameters do not interfere. Existing code using raw `FullCalendar`
gets compiler warnings but no breakage.

### 2. Typed Event Hierarchy — CONFIRMED
- `EntryEvent extends ComponentEvent<FullCalendar<Entry>>` — no raw types
- `CalendarItemEvent<T> extends ComponentEvent<FullCalendar<T>>` — new parallel hierarchy for CIP
- Both hierarchies are fully typed, clean separation between Entry and CIP event paths

### 3. Client-to-Server Updates: A+B with Mutual Exclusion — CONFIRMED
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

## Open Questions (to be resolved by Spikes)

See [phase-0-spikes.md](phase-0-spikes.md) for remaining spike work.

## Requirements Review Summary

The original requirements (calendar-item-provider.md) had gaps identified by review.
See [requirements-review.md](requirements-review.md) for the full assessment.
Most gaps are now resolved by the architectural decisions above.
