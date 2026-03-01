# Phase 0: Technical Spikes

**Goal:** Resolve remaining open questions before committing to implementation.
**Estimated scope:** 1 spike + 1 verification test.

---

## Resolved (no longer needs spike)

- ~~**Spike 0.1: Generic @DomEvent feasibility**~~ — **RESOLVED.** Generic erasure means
  `FullCalendar<T>` erases to `FullCalendar` at runtime. Vaadin's `ComponentEventBus` uses
  reflection on erased types + annotation metadata. Constructor matching is unaffected.
  `EntryEvent extends ComponentEvent<FullCalendar<Entry>>` is safe.

- ~~**Spike 0.3: Event system approach**~~ — **RESOLVED.** Decision: parallel typed hierarchy.
  `EntryEvent extends ComponentEvent<FullCalendar<Entry>>` for existing events,
  `CalendarItemEvent<T> extends ComponentEvent<FullCalendar<T>>` for CIP events.
  No raw types anywhere.

---

## Spike 0.1: CalendarItemPropertyMapper Prototype

**Question:** What is the optimal API design for the mapper, including the bidirectional
(setter) support?

**Approach:**
1. Implement a minimal `CalendarItemPropertyMapper<T>` with:
   - Lambda-based mapping: `.id(Pojo::getId).title(Pojo::getName).start(Pojo::getFrom)`
   - String-based mapping: `.title("name").start("from")`
   - Optional setter support: `.start(Pojo::getFrom, Pojo::setFrom)`
   - `toJson(T item)` method that produces an `ObjectNode`
   - `applyChanges(T item, ObjectNode changes)` using registered setters
2. Implement the `forItem()` bound pattern from the requirements doc as an alternative
3. Benchmark both approaches with 10,000 items:
   - Measure allocations (bound mapper objects vs direct)
   - Measure serialization throughput
   - Compare to current Entry.toJson() baseline
4. Test type conversion: how to handle `LocalDateTime` -> ISO string, `DayOfWeek` -> int, etc.
   Can existing `JsonItemPropertyConverter` instances be reused?
5. Validate the mutual exclusion check: when both setters and an update handler are registered,
   the mapper/calendar must throw a clear exception at configuration time.

**Expected Outcome:** A working prototype that validates the API design and identifies
the best serialization approach (bound mapper vs direct toJson).

**Key design questions to answer:**
- Should string-based mapping use `BeanProperties.read()` or a separate reflection mechanism?
- How to handle default values for unmapped properties?
- Should the mapper be immutable after construction (thread-safety)?
- Does the `forItem()` pattern (from requirements doc) add value over direct `toJson(T)`?

---

## Verification 0.2: Generic Component + @DomEvent Runtime Test

**Goal:** Quick sanity check (not a full spike) to confirm the generic approach works at runtime.

**Approach:**
1. Create a minimal generic Vaadin component `TestComponent<T>` with a `@DomEvent`
2. Create an event class `TestEvent extends ComponentEvent<TestComponent<String>>`
3. Run in a Vaadin 25 context, fire the event, verify `getSource()` returns typed component
4. Verify no warnings/errors from Vaadin's internals

**Time-box:** 1 hour max. If it passes, proceed. If it fails, reassess the generic approach.

---

## Spike Completion Criteria

1. A working mapper prototype with getter+setter support
2. Runtime verification of generic component + @DomEvent
3. Updates to this plan if findings change the approach

All spikes must complete before Phase 1 begins.

## Important: Spike Code Retention

Spike code must NOT be thrown away until the full CIP feature implementation is complete.
Keep spike code in a dedicated package or module (e.g., `addon/src/test/java/.../spike/`)
so it can be referenced during implementation. Clean up only after Phase 6 is done.
