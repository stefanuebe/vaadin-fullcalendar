# Calendar Item Provider (CIP) — Implementation Status

## CIP Implementation: COMPLETE

| Phase | Name | Status | Description |
|-------|------|--------|-------------|
| 0 | Spikes | COMPLETE | Mapper prototype + generic component runtime verification |
| 1 | Foundation Types | COMPLETE | New CIP types without touching existing code |
| 2 | Adapter Layer | COMPLETE | Wire existing EntryProvider into CIP hierarchy |
| 3 | Core Integration | COMPLETE | Make FullCalendar generic, connect CIP |
| 4 | Event System | COMPLETE | Typed parallel event hierarchy for CIP |
| 4b | Code Review Fixes | COMPLETE | Thread-safety, null checks, caching, Javadoc fixes |
| 5 | Scheduler Extension | COMPLETE | Integrate CIP with addon-scheduler |
| 6 | Migration & Docs | COMPLETE | Deprecations, migration guide, documentation, demos |
| 7 | Internal Unification | COMPLETE | setEntryProvider() delegates to CIP internally |
| 8 | Event Unification | COMPLETE | Entry events extend CIP event counterparts |
| 9 | API Naming & Cleanup | COMPLETE | Remove entryProviderRef, rename Entry→Item API, migrate demos |

## Demo Rework: IN PROGRESS

See [demo-rework.md](demo-rework.md) for full plan details.

| Sub-Phase | Name | Status | Description |
|-----------|------|--------|-------------|
| 10.1 | Test Decoupling | COMPLETE | Separate e2e-test-app module, all E2E tests green |
| 10.2 | Demo Infrastructure | COMPLETE | AbstractDemoView, Maven resource copy, CIP toolbar |
| 10.3 | Sidenav + Stubs | COMPLETE | Flat sidenav with separator labels, 15 empty view stubs |
| 10.4 | Business Hours View | COMPLETE | Validation view to prove AbstractDemoView design |
| 10.5 | Playground | COMPLETE | Rewritten on AbstractDemoView, non-deprecated API |
| 10.6 | Events View | COMPLETE | All FC event listeners with visible feedback |
| 10.7 | Recurring & BG Items | PENDING | Recurring events and background items |
| 10.8 | Native Event Listeners | PENDING | Browser-level native event listeners |
| 10.9 | i18n | PENDING | Timezone and locale customization |
| 10.10 | CIP: In Memory | PENDING | InMemoryCalendarItemProvider with custom POJO |
| 10.11 | CIP: Callback | PENDING | CallbackCalendarItemProvider with update handler |
| 10.12 | CIP: Backend | PENDING | Simulated backend service pattern |
| 10.13 | Entry Provider Streamline | PENDING | Clean up existing EP demos |
| 10.14 | Multi Month Streamline | PENDING | Clean up Multi Month demo |
| 10.15 | Callbacks: Custom Props | PENDING | Streamline Custom Properties demo |
| 10.16 | Callbacks: Remaining | PENDING | ItemClassname, ItemContent, ItemDidMount, etc. |
| 10.17 | Cleanup | PENDING | Remove old abstract views, dead code, final verification |

## Key Architectural Decisions

### 1. Generic `FullCalendar<T>` — CONFIRMED
`FullCalendar` becomes `FullCalendar<T>`. Vaadin's `@DomEvent` mechanism uses reflection on
erased types — generic parameters do not interfere at runtime. Existing code using raw
`FullCalendar` gets compiler warnings but no breakage.

### 2. Generic `FullCalendarScheduler<T>` — COMPLETE
Phase 5: `FullCalendarScheduler<T> extends FullCalendar<T>` — fully generic.
`removeFromEntries()` has a CIP early-return path. The `Scheduler` interface stays non-generic
(manages resources, not items). Listener methods on the interface use wildcard event types
(`TimeslotClickedSchedulerEvent<?>`, etc.) with raw casts in the implementation.

### 3. Typed Event Hierarchy — CONFIRMED (Phase 3 implementation)
- Entry events: `EntryEvent extends ComponentEvent<FullCalendar<Entry>>` — no raw types
- Non-entry events are generic: `DateEvent<T>`, `DateTimeEvent<T>`, `TimeslotsSelectedEvent<T>`,
  `DatesRenderedEvent<T>`, `TimeslotClickedEvent<T>`, etc. — all `extends ComponentEvent<FullCalendar<T>>`
- Scheduler events: `TimeslotClickedSchedulerEvent extends TimeslotClickedEvent<Entry>`,
  `TimeslotsSelectedSchedulerEvent extends TimeslotsSelectedEvent<Entry>`, constructors take
  `FullCalendar<Entry>` and cast to `(Scheduler)` for resource lookup
- Listener delegation in FullCalendarScheduler uses lambdas (no raw casts):
  `event -> listener.onComponentEvent(event)`
- Phase 4 added the parallel `CalendarItemEvent<T>` hierarchy for CIP (using @DomEvent, not programmatic dispatch)

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
- [phase-7-internal-unification.md](phase-7-internal-unification.md) — setEntryProvider() delegates to CIP
- [phase-8-event-unification.md](phase-8-event-unification.md) — Entry events extend CIP events
