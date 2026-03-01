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

---

## Results (completed 2026-03-01)

### Spike 0.1: CalendarItemPropertyMapper — COMPLETE

**Files:**
- `addon/src/test/java/org/vaadin/stefan/fullcalendar/spike/CalendarItemPropertyMapper.java`
- `addon/src/test/java/org/vaadin/stefan/fullcalendar/spike/SamplePojo.java`
- `addon/src/test/java/org/vaadin/stefan/fullcalendar/spike/CalendarItemPropertyMapperTest.java`

**33 tests passing.** All questions answered:

#### Q: Direct `toJson(T)` vs `forItem(T)` bound pattern — which is better?
**A: Direct `toJson(T)` is recommended as primary API.** Benchmark results (10,000 items):
- Direct `toJson()`: **~7 ms**
- `forItem().toJson()`: **~18 ms** (2.5x slower due to BoundMapper allocation)
- `Entry.toJson()` baseline: **~31 ms** (reflection-based BeanProperties is slower)

The mapper's lambda-based approach is **~4x faster than Entry.toJson()** because it avoids
reflection overhead. `forItem()` is kept as a convenience API for single-item property access
(reading title, start, etc.) but `toJson(T)` should be the primary serialization path.

#### Q: Should string-based mapping use `BeanProperties.read()` or separate reflection?
**A: Separate lightweight reflection.** The mapper uses `Class.getMethod()` directly to resolve
getters. This is simpler than `BeanProperties.read()` which inspects ALL fields and creates
complex annotation metadata. The mapper only needs individual getter lookups for explicitly
named fields. String-based mapping validates eagerly at `.map()` call time — invalid field
names throw `IllegalArgumentException` immediately.

#### Q: How to handle default values for unmapped properties?
**A: Unmapped properties are simply omitted from JSON.** FullCalendar JS uses its own defaults
for missing properties. This is by design — the mapper only outputs what the developer maps.
Entry.toJson() includes ALL fields (including defaults like `editable=true`, `overlap=true`),
which means CIP output is leaner. No special handling needed.

#### Q: Should the mapper be immutable after construction? (Thread-safety)
**A: Yes — freeze on first use.** The mapper freezes (becomes immutable) after the first call
to `toJson()` or `forItem()`. This auto-validates (throws if `id` is missing) and prevents
modification after the mapper is in use. Thread-safe for reads after freeze. Attempting to add
mappings after freeze throws `IllegalStateException`.

#### Additional findings:
- **Type converters are self-contained.** `LocalDateTime`, `LocalDate`, `DayOfWeek` conversions
  are implemented as static methods on the mapper. No need to reuse `JsonItemPropertyConverter`
  instances (which are coupled to `<T extends Entry>`). Phase 1 should create standalone
  converter functions or a small converter registry.
- **Mutual exclusion** (Strategy A setters vs Strategy B update handler) is not tested in the
  mapper itself — it belongs at the `FullCalendar` level (Phase 3). The mapper's `hasSetters()`
  method enables the calendar to check and enforce exclusion.
- **Bidirectional roundtrip works** — serialize via `toJson()`, then `applyChanges()` with the
  same JSON restores identical values. `LocalDateTime` survives the ISO-8601 "Z" suffix roundtrip.

### Verification 0.2: Generic Component + @DomEvent — COMPLETE (PASS)

**File:** `addon/src/test/java/org/vaadin/stefan/fullcalendar/spike/GenericComponentDomEventTest.java`

**6 tests passing.** Confirms:

1. `TestComponent<T> extends Component` works — no runtime issues
2. `TestEvent extends ComponentEvent<TestComponent<String>>` fires and delivers typed source
3. `GenericTestEvent<T> extends ComponentEvent<TestComponent<T>>` fires with typed item access
4. Multiple listeners on generic component all fire correctly
5. **Raw type backward compatibility works** — `TestComponent` (raw) can register and receive events
6. Different type parameters (`TestComponent<String>`, `TestComponent<Integer>`) coexist

**Key insight:** The unchecked cast `(TestComponent<String>) (TestComponent<?>) this` is needed
at compile time because Java's type system won't allow `TestComponent<T>` → `TestComponent<String>`
directly. At runtime this is a no-op due to erasure. This pattern will be used when
`FullCalendar<T>` fires `EntryEvent` (which expects `FullCalendar<Entry>`) — the cast is safe
because it's only reached when the calendar IS operating in Entry mode.
