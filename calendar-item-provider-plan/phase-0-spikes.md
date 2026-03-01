# Phase 0: Technical Spikes

**Goal:** Resolve remaining open questions before committing to implementation.
**Estimated scope:** 1 spike + 1 verification test.
**Important:** Spike code must NOT be discarded until the full CIP feature is complete.
Keep spike code in `addon/src/test/java/.../spike/` for reference during implementation.

---

## Context

The CIP feature introduces `CalendarItemProvider<T>` and `CalendarItemPropertyMapper<T>` to
allow arbitrary POJOs (not just `Entry`) as calendar items. The core architectural decisions
are documented in STATUS.md. Two questions remain before implementation begins.

## Resolved (no spike needed)

- ~~**Generic @DomEvent feasibility**~~ — **RESOLVED.** Generic erasure means `FullCalendar<T>`
  erases to `FullCalendar` at runtime. Vaadin's `ComponentEventBus` uses reflection on erased
  types + annotation metadata. Constructor parameter `FullCalendar<Entry>` erases to
  `FullCalendar` — same match as before. Confirmed safe.

- ~~**Event system approach**~~ — **RESOLVED.** Parallel typed hierarchy:
  `EntryEvent extends ComponentEvent<FullCalendar<Entry>>`,
  `CalendarItemEvent<T> extends ComponentEvent<FullCalendar<T>>`.

---

## Spike 0.1: CalendarItemPropertyMapper Prototype

**Question:** What is the optimal API design for the mapper, including bidirectional
(setter) support and the mutual exclusion check?

**Background:** The mapper must convert arbitrary POJOs to FullCalendar JSON and optionally
apply client-side changes back to POJOs. Two mapping styles are proposed: lambda-based
(`MyPojo::getName`) and string-based (`"name"` via reflection).

**Approach:**
1. Implement a minimal `CalendarItemPropertyMapper<T>` with:
   - Lambda-based read mapping: `.id(Pojo::getId).title(Pojo::getName).start(Pojo::getFrom)`
   - String-based read mapping: `.title("name").start("from")`
   - Bidirectional mapping with setter: `.start(Pojo::getFrom, Pojo::setFrom)`
   - `toJson(T item)` → `ObjectNode`
   - `applyChanges(T item, ObjectNode changes)` using registered setters
   - `validate()` — throws if `id` mapping is missing
   - `hasSetters()` — returns true if any setter is registered
2. Implement the `forItem(T)` bound pattern from the original requirements doc as alternative
3. Benchmark both approaches with 10,000 items:
   - Allocations (bound mapper objects vs direct `toJson()`)
   - Serialization throughput
   - Compare to current `Entry.toJson()` baseline
4. Test type conversions: `LocalDateTime` → ISO string, `DayOfWeek` → int, etc.
   Can existing `JsonItemPropertyConverter` instances be reused?
5. Validate mutual exclusion: register both setters and update handler → must throw

**Expected Outcome:** A working prototype answering:
- Direct `toJson(T)` vs `forItem(T)` bound pattern — which is better?
- Should string-based mapping use `BeanProperties.read()` or separate reflection?
- How to handle default values for unmapped properties?
- Should the mapper be immutable after construction? (Thread-safety)

---

## Verification 0.2: Generic Component + @DomEvent Runtime Test

**Goal:** Quick sanity check (not a full spike) to confirm generic approach at runtime.

**Approach:**
1. Create a minimal generic Vaadin component `TestComponent<T>` extending `Component`
2. Add a `@JsModule` or `@Tag` annotation (minimal setup)
3. Create `TestEvent extends ComponentEvent<TestComponent<String>>` with `@DomEvent`
4. Register listener via `addListener(TestEvent.class, ...)`
5. Fire event programmatically or via simulated DOM event
6. Verify `event.getSource()` returns the typed component, no ClassCastException

**Time-box:** 1 hour max. If it passes, proceed. If it fails, reassess.

---

## Spike Completion Criteria

1. Working mapper prototype with getter+setter support in `addon/src/test/java/.../spike/`
2. Benchmark results documented
3. Runtime verification test passing
4. Any findings that change the plan are documented here

All spikes must complete before Phase 1 begins.
