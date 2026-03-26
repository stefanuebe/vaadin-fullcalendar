# UC-025: Signal Binding (Vaadin 25.1 Signals Integration)

**As a** Vaadin application developer, **I want to** bind entries (and resources) to the calendar via Vaadin Signals **so that** the calendar reactively updates when my data changes, without manual refresh calls.

**Status:** Draft
**Date:** 2026-03-26
**Target version:** 7.2.0

---

## Scope

**Addon module:** both (addon + addon-scheduler)
**Phases:** Phase 1 = entry binding (addon), Phase 2 = resource binding (addon-scheduler). Resource binding is designed from the start but implemented separately.
**Related Options:** —
**Related Events:** `EntryDroppedEvent`, `EntryResizedEvent`, `EntryDroppedSchedulerEvent` (applyChanges interaction)
**Vaadin version:** 25.1+ (Signals API is experimental). Addon minimum version raised from 25.0 to 25.1.
**Hard blocker:** [#225](https://github.com/stefanuebe/vaadin-fullcalendar/issues/225) — auto-revert for unapplied entry changes. Must be implemented first (also 7.2.0). Signal binding cannot work without AutoRevert.

---

## Motivation

The current `EntryProvider` pattern requires explicit `addEntry()` / `removeEntry()` / `refreshItem()` / `refreshAll()` calls. Vaadin 25.1 introduces Signals — reactive containers that automatically propagate changes to bound components. A `bindEntries` API would let developers work with a `ListSignal<Entry>` and have the calendar update automatically.

This is an **addition**, not a replacement. `EntryProvider` remains the default and fully supported path.

**Risk:** Vaadin Signals is marked experimental in 25.1. The API may change in future Vaadin versions. The signal binding API in this addon should be clearly documented as depending on an experimental Vaadin feature.

---

## User-Facing Behavior

- When entries are bound via Signals, the calendar automatically reflects additions, removals, and modifications — no manual refresh needed
- When resources are bound via Signals (Scheduler, Phase 2), the resource list updates automatically
- Visually identical to `EntryProvider`-based calendar — the binding mechanism is transparent to the end user

---

## Java API Usage

### Basic Entry Binding

```java
FullCalendar calendar = FullCalendarBuilder.create().build();

ListSignal<Entry> entries = new ListSignal<>();

// Bind — throws BindingActiveException if a non-default EntryProvider is active
calendar.bindEntries(entries);

// Add entries — calendar updates automatically
ValueSignal<Entry> meetingSignal = entries.insertLast(
    new Entry("1", "Meeting", start, end));

// Modify entry — calendar updates automatically
// IMPORTANT: Direct mutation (entry.setTitle(...)) does NOT trigger reactivity.
// Always use modify() on the ValueSignal.
meetingSignal.modify(e -> e.setTitle("Updated Meeting"));

// Remove entry — calendar updates automatically
entries.remove(meetingSignal);

// Unbind (returns to default empty InMemoryEntryProvider)
calendar.bindEntries(null);
```

### Builder Integration

```java
// Build with signal binding directly
ListSignal<Entry> entries = new ListSignal<>();
FullCalendar calendar = FullCalendarBuilder.create()
    .withSignalBinding(entries)
    .build();

// withEntryProvider() and withSignalBinding() are mutually exclusive in the builder
FullCalendar bad = FullCalendarBuilder.create()
    .withEntryProvider(someProvider)
    .withSignalBinding(entries)  // ERROR — throws BindingActiveException
    .build();
```

### Exclusivity with EntryProvider

```java
// Signal binding active
calendar.bindEntries(entries);

// This throws BindingActiveException (like Vaadin's bindValue/setValue)
calendar.setEntryProvider(someProvider);  // ERROR

// Must unbind first
calendar.bindEntries(null);
calendar.setEntryProvider(someProvider);  // OK

// And vice versa: non-default EntryProvider active
calendar.setEntryProvider(someProvider);
calendar.bindEntries(entries);  // ERROR — throws BindingActiveException
```

### AutoRevert Prerequisite

```java
// AutoRevert is true by default — bindEntries works
calendar.bindEntries(entries);  // OK

// Disabling AutoRevert while signal-bound throws
calendar.setAutoRevertUnappliedEntryChanges(false);  // ERROR — throws

// Disabling AutoRevert first, then trying to bind, also throws
calendar.bindEntries(null);
calendar.setAutoRevertUnappliedEntryChanges(false);
calendar.bindEntries(entries);  // ERROR — throws
```

### Resource Binding (Scheduler — Phase 2)

```java
FullCalendarScheduler scheduler = FullCalendarBuilder.create()
    .withScheduler(licenseKey)
    .build();

ListSignal<Resource> resources = new ListSignal<>();
ListSignal<ResourceEntry> entries = new ListSignal<>();

// Bind both
scheduler.bindResources(resources);
scheduler.bindEntries(entries);

// Add resources — scheduler updates automatically
ValueSignal<Resource> room = resources.insertLast(
    new Resource(null, "Room A", "#3788d8"));

// Add entry with resource reference
ResourceEntry entry = new ResourceEntry();
entry.setTitle("Meeting");
entry.addResources(room.peek());
entries.insertLast(entry);

// Modify resource — scheduler updates automatically
room.modify(r -> r.setTitle("Room A (renovated)"));
```

### Interaction with applyChanges (Drop/Resize Events)

```java
calendar.bindEntries(entries);

calendar.addEntryDroppedListener(event -> {
    // User validates the change (existing pattern, unchanged)
    if (isDropAllowed(event)) {
        // applyChangesOnEntry() internally uses signal.modify()
        // when a signal binding is active
        event.applyChangesOnEntry();
    }
    // If not called: client auto-reverts (autoRevert is required for signal binding),
    // server-side Entry in signal remains unchanged
});
```

---

## Architecture

### Internal Mechanism

When `bindEntries(ListSignal<Entry>)` is called:

1. Validates: no non-default EntryProvider active, `autoRevertUnappliedEntryChanges` is `true`
2. A `SignalEntryProvider` is created internally (implements `EntryProvider`)
3. The `SignalEntryProvider` is set as the active provider (reusing existing `setEntryProvider` internally)
4. A `Signal.effect(calendar, ...)` observes the `ListSignal` for changes
5. The effect runs immediately on creation (initial sync of all entries in the signal to the client)
6. On subsequent changes: targeted client refreshes (add/remove/update)
7. Individual `ValueSignal<Entry>` changes trigger single-entry refresh
8. The `Registration` returned by `Signal.effect()` is stored for cleanup on unbind

### SignalEntryProvider (internal class)

```
EntryProvider
  ├── InMemoryEntryProvider   (existing)
  ├── CallbackEntryProvider   (existing)
  └── SignalEntryProvider     (new, internal)
        - wraps ListSignal<Entry>
        - implements fetch/fetchAll/fetchById by reading from signal (using peek())
        - applyChanges routes through signal.modify() instead of direct mutation
```

### applyChangesOnEntry() Routing

`EntryDataEvent.applyChangesOnEntry()` currently mutates the Entry directly. With signal binding, it must route through `signal.modify()` so all effects (including external ones) observe the change.

The routing mechanism: `applyChangesOnEntry()` calls an internal method on `FullCalendar` (e.g., `applyEntryChanges(entry, json)`). The calendar checks whether a signal binding is active:
- **Signal active:** finds the `ValueSignal<Entry>` for the entry and calls `signal.modify(e -> e.updateFromJson(json))`
- **No signal:** direct mutation as today (`entry.updateFromJson(json)`)

This keeps `EntryDataEvent` unaware of signals — the calendar handles the routing.

### The Circularity Problem — Solved by AutoRevert

When a client-side change (drop/resize) occurs with signal binding active:

1. Client shows entry at new position temporarily
2. Server receives `EntryDroppedEvent` / `EntryResizedEvent`
3. **If `applyChangesOnEntry()` is called:**
   - Internally calls `signal.modify(e -> e.updateFromJson(json))`
   - Signal effect detects change → triggers client refresh
   - AutoRevert does NOT fire (suppressed because `applyChanges` was called)
   - Client receives the confirmed new position — no visual jump
4. **If `applyChangesOnEntry()` is NOT called:**
   - AutoRevert fires → client reverts to original position
   - Signal is unchanged → no effect fires → consistent state

**Critical design requirement for #225:** The AutoRevert mechanism must be aware of whether `applyChangesOnEntry()` was called during the event listener. AutoRevert only fires when no apply happened. This prevents the double-update flicker (revert → re-apply) that would occur if AutoRevert always fires unconditionally.

### Resource Binding Complexity (Phase 2 Design Considerations)

Resources add complexity because:

1. **No ResourceProvider abstraction** — Unlike entries, resources are managed via direct `addResource()` / `removeResource()` on the Scheduler interface. `bindResources()` needs a different internal mechanism (no provider to wrap).
2. **Hierarchical structure** — `ListSignal<Resource>` is a flat list; parent/child relationships live in the `Resource` objects themselves
3. **Entry↔Resource cross-reference** — `ResourceEntry.addResources(resource)` creates a link. Modifying a resource's properties must refresh the resource display; associated entries refresh on the next fetch cycle.
4. **Independent lifecycles** — entries and resources can be bound/unbound independently

The `bindResources` effect must handle:
- Resource add/remove → `addResource()` / `removeResource()` on client
- Resource property change → `updateResource()` on client
- Same `BindingActiveException` semantics as entries (mutually exclusive with manual `addResource`/`removeResource`)

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | `bindEntries` and `setEntryProvider` are mutually exclusive. Calling one while the other is active throws Vaadin's `BindingActiveException`. |
| BR-02 | `bindEntries(null)` removes the binding, cleans up the effect `Registration`, and restores a default empty `InMemoryEntryProvider`. |
| BR-03 | `bindResources` (Phase 2) and manual resource management (`addResource`/`removeResource`) are mutually exclusive. Same `BindingActiveException` semantics. |
| BR-04 | `bindResources(null)` (Phase 2) removes the binding and clears resources. |
| BR-05 | `applyChangesOnEntry()` MUST use `signal.modify()` internally when a signal binding is active. This ensures all effects (including external ones) observe the change. The routing is handled by `FullCalendar`, not by `EntryDataEvent`. |
| BR-06 | `autoRevertUnappliedEntryChanges` must be `true` when `bindEntries` is active. Calling `bindEntries()` while AutoRevert is `false` throws. Calling `setAutoRevertUnappliedEntryChanges(false)` while a binding is active throws. |
| BR-07 | The signal binding is active while the component is attached; paused when detached; re-synced on re-attach (standard Vaadin Signal lifecycle). The effect runs immediately on creation/re-attach for initial sync. |
| BR-08 | Signal-bound calendars still support all existing event listeners (click, drop, resize, etc.) — only data management changes. |
| BR-09 | `ListSignal` is the only supported signal type for binding (not `Signal<List<Entry>>`). |
| BR-10 | Entry cache (`lastFetchedEntries`) remains active for signal-bound calendars — used for event lookups. |
| BR-11 | `SignalEntryProvider.fetch*()` methods use `peek()` (not `get()`) to read from the signal, since they are called outside a reactive context. |
| BR-12 | `FullCalendarBuilder`: `withSignalBinding()` and `withEntryProvider()` are mutually exclusive. Calling both throws `BindingActiveException`. |
| BR-13 | AutoRevert (#225) must suppress the revert when `applyChangesOnEntry()` was called during the event listener. AutoRevert only fires when no apply happened. This prevents double-update flicker. |
| BR-14 | Entry identity is ID-based (`Entry.equals`/`hashCode` use the entry ID). This is compatible with signal change tracking. |

---

## Acceptance Criteria

### Entry Binding — Phase 1 (addon)

- [ ] `bindEntries(ListSignal)` makes entries appear on the calendar
- [ ] `insertLast` on the signal adds an entry to the calendar without manual refresh
- [ ] `remove` on the signal removes an entry from the calendar without manual refresh
- [ ] `ValueSignal.modify()` on an entry updates it on the calendar without manual refresh
- [ ] `bindEntries(null)` removes all signal-bound entries and restores default provider
- [ ] `setEntryProvider()` while `bindEntries` is active throws `BindingActiveException`
- [ ] `bindEntries()` while a non-default `EntryProvider` is active throws `BindingActiveException`
- [ ] `bindEntries()` while `autoRevertUnappliedEntryChanges` is `false` throws
- [ ] `setAutoRevertUnappliedEntryChanges(false)` while `bindEntries` is active throws
- [ ] `applyChangesOnEntry()` works correctly with signal binding (uses `signal.modify()`, no inconsistent state)
- [ ] Client auto-reverts when `applyChangesOnEntry()` is not called (AutoRevert prerequisite)
- [ ] No double-update flicker when `applyChangesOnEntry()` IS called (AutoRevert suppressed)
- [ ] Detach/re-attach lifecycle works (binding pauses and resumes, re-syncs on re-attach)
- [ ] Effect `Registration` is cleaned up on unbind
- [ ] Builder: `withSignalBinding()` works
- [ ] Builder: `withSignalBinding()` + `withEntryProvider()` throws `BindingActiveException`

### Resource Binding — Phase 2 (addon-scheduler)

- [ ] `bindResources(ListSignal)` makes resources appear in timeline/resource views
- [ ] Adding/removing resources via signal updates the scheduler
- [ ] Modifying resource properties via `ValueSignal.modify()` updates the display
- [ ] Hierarchical resources (parent/child) work with signal binding
- [ ] `bindResources(null)` clears all signal-bound resources
- [ ] `addResource()`/`removeResource()` while `bindResources` is active throws `BindingActiveException`
- [ ] Entry↔Resource cross-references work (entries display in correct resource rows)

### Combined (Phase 2)

- [ ] `bindEntries` + `bindResources` together work correctly
- [ ] Dropping an entry to a different resource works with both bindings active

---

## Open Questions

| # | Question | Notes |
|---|----------|-------|
| OQ-01 | **Resource hierarchy in ListSignal**: Should child resources be separate entries in the `ListSignal`, or only top-level resources? If flat: how does the effect know about hierarchy changes within a `Resource`? If nested: `ListSignal` only tracks top-level, children are part of the `Resource` object. | Likely: flat list, hierarchy via Resource.parent/children. Phase 2. |
| OQ-02 | **Granularity of entry change detection**: When `ValueSignal.modify()` is called, can we detect *which* properties changed to send a minimal JSON delta? Or always send the full entry JSON? | Start with full refresh per entry, optimize later |
| OQ-03 | **ResourceEntry type constraint**: Should `bindEntries` on a `FullCalendarScheduler` require `ListSignal<ResourceEntry>` instead of `ListSignal<Entry>`? | Likely yes, mirroring existing type constraints. Phase 2. |

---

## Spike (Pre-Implementation)

Before implementation, a spike must validate:

1. **`ValueSignal<Entry>` with mutable bean**: Verify that `modify()` correctly triggers effects when mutating a mutable POJO (Entry). Test with `Signal.effect()` and confirm reactivity.
2. **`ListSignal<Entry>` add/remove/modify**: Verify the full lifecycle works with Entry objects.
3. **Effect timing**: Confirm that `Signal.effect(component, ...)` fires in the same server roundtrip as `signal.modify()`. This is critical for the "no visual jump" guarantee when `applyChangesOnEntry()` is called.
4. **`BindingActiveException` availability**: Confirm the class exists in `com.vaadin.flow.dom` in Vaadin 25.1 and is appropriate for reuse.

---

## Tests

See `specs/verification.md` §2 for the general decision rule on Unit vs Browserless vs E2E tests.

### Unit Tests (pure logic, no Vaadin context)

- [ ] `SignalEntryProviderTest` — fetch/fetchAll/fetchById delegate to ListSignal via peek()

### Browserless Tests (Vaadin context, no browser)

Browserless tests are appropriate here because signal binding is purely server-side Java state — no `@EventData` or JS client needed. Unlike the auto-revert feature (where drop/resize events require structured JSON from the real JS client), signal binding lifecycle, exception enforcement, and `signal.modify()` state changes can all be verified without a browser.

- [ ] `FullCalendarSignalBindingTest` — bindEntries/unbind lifecycle, exclusivity with setEntryProvider, AutoRevert prerequisite enforcement, builder integration
- [ ] `SignalApplyChangesTest` — applyChangesOnEntry routes through signal.modify(), verifies signal state after apply
- [ ] `SignalBindingExceptionTest` — all `BindingActiveException` scenarios (both directions, builder, AutoRevert coupling)
- [ ] `SchedulerSignalResourceBindingTest` (Phase 2) — bindResources lifecycle, exclusivity with addResource

### E2E Tests (browser required — visual/JS verification)

E2E is needed here because we must verify that `callJsFunction()` actually causes entries to appear/disappear/move in the rendered calendar. The FullCalendar JS client must execute for this.

- [ ] `signal-entry-binding.spec.js` — add/remove/update entries via signal, verify entries appear/disappear/change in the calendar
- [ ] `signal-resource-binding.spec.js` (Phase 2) — add/remove/update resources via signal, verify scheduler display
- [ ] `signal-drop-resize.spec.js` — drop/resize with signal binding, verify applyChanges + autoRevert visual behavior (no flicker)

---

## Related

- [#225: Auto-revert unapplied entry changes](https://github.com/stefanuebe/vaadin-fullcalendar/issues/225) — hard blocker, also 7.2.0
- [Vaadin Signals Documentation](https://vaadin.com/docs/latest/flow/reactive/signals) (25.1, experimental)
- [UC-009: Entry Providers](use-case-009-entry-provider.md)
- [UC-015: Scheduler Resources](use-case-015-scheduler-resources.md)
- [UC-003: Entry Drag and Drop](use-case-003-entry-drag-and-drop.md)
- [UC-004: Entry Resize](use-case-004-entry-resize.md)
