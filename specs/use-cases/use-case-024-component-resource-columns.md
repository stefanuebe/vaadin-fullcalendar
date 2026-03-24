# UC-024: Component Resource Area Columns

**As a** Vaadin application developer, **I want to** define resource area columns that render Vaadin components (e.g., DatePicker, TextField) per resource **so that** end users can interact with custom input fields directly in the resource sidebar — similar to Vaadin Grid's ComponentColumn.

**Status:** Draft
**Date:** 2026-03-23

---

## Scope

**Addon module:** addon-scheduler
**Related Options:** `SchedulerOption.RESOURCE_AREA_COLUMNS`
**Related Events:** none (leverages existing entry/resource events for reading component state)
**Related Classes:** `ResourceAreaColumn`, `Resource`, `FullCalendarScheduler`, `Scheduler`

---

## User-Facing Behavior

- The resource area sidebar shows additional columns that contain Vaadin input components (DatePicker, TextField, ComboBox, etc.)
- Each resource row has its own component instance in that column
- Components are fully interactive — the user can type, select, open dropdowns, etc.
- Components survive FullCalendar view changes and re-renders (they are re-injected after each FC re-render cycle)
- Components are visually integrated into the resource area cells
- Works in all resource-capable views: timeline views (resourceTimelineDay/Week/Month/Year) and vertical resource views (resourceTimeGridDay/Week)

---

## Java API Usage

### Basic: Component column with callback

```java
FullCalendarScheduler scheduler = FullCalendarBuilder.create()
    .withScheduler(Scheduler.GPL_V3_LICENSE_KEY)
    .build();

// Define a component column — callback receives Resource, returns Component.
// The "field" parameter serves as a unique column key. FC still looks up the field
// value internally, but cellContent is suppressed so it is not displayed.
// The callback must not return null and must return a unique instance per call.
ComponentResourceAreaColumn<DatePicker> dateColumn = new ComponentResourceAreaColumn<>(
    "deadline", "Deadline",
    resource -> {
        DatePicker picker = new DatePicker();
        picker.setWidth("130px");
        picker.setValue(LocalDate.now());
        return picker;
    }
);

// Mix with regular columns — withWidth() returns ComponentResourceAreaColumn<DatePicker>
scheduler.setResourceAreaColumns(
    new ResourceAreaColumn("title", "Name").withWidth("200px"),
    dateColumn.withWidth("160px")
);
```

### Runtime access to components (type-safe)

```java
// Access a specific resource's component at any time — no cast needed
scheduler.addEntryResizedListener(event -> {
    Resource resource = event.getEntry().getResource();

    dateColumn.getComponent(resource).ifPresent(picker -> {
        picker.setValue(event.getEntry().getEnd().toLocalDate());
    });
});

// Iterate all resource components (unmodifiable view, keyed by resource ID)
dateColumn.getComponents().forEach((resourceId, picker) -> {
    // read or update — picker is already typed as DatePicker
});
```

### Updating components when resources change

```java
// When resources are added/removed, components are automatically created/destroyed
scheduler.addResource(newResource);   // triggers component creation via callback
scheduler.removeResource(oldResource); // component is removed and detached

// Force re-creation of all components (e.g., after bulk data change)
// Note: this destroys and re-creates — component state is lost.
// To update without losing state, use getComponent() directly.
dateColumn.refreshAll();

// Re-create a single resource's component (no-op if resource not registered)
dateColumn.refresh(resource);
```

---

## Architecture

### DOM Teleportation Pattern

This feature uses the same principle as Vaadin Grid's `ComponentRenderer`: components live in the Vaadin component tree but their DOM elements are physically moved into FullCalendar-managed cells.

```
Vaadin Component Tree (logical):          FC DOM (physical):

FullCalendarScheduler                     <vaadin-full-calendar-scheduler>
  +-- [hidden container div]                +-- .fc-resource-area
  |     +-- DatePicker (res-A)              |     +-- <td> (res-A, col "deadline")
  |     +-- DatePicker (res-B)              |     |     +-- DatePicker DOM <-- moved here
  |     +-- DatePicker (res-C)              |     +-- <td> (res-B, col "deadline")
  |                                         |     |     +-- DatePicker DOM <-- moved here
  ...                                       ...
```

**Why this works:** Vaadin's server-client protocol identifies elements by **state node IDs**, not DOM position. A DatePicker moved into an FC `<td>` still fires its `change` events with the correct node ID. The server-side logical tree (hidden container as parent) remains consistent — only the physical DOM position changes. This is transport-independent — works with both HTTP polling and Push/WebSocket.

### Flow

1. **Server**: `ComponentResourceAreaColumn<T>` stores `SerializableFunction<Resource, T>` callback
2. **Server**: When `setResourceAreaColumns()` is called (and when resources are added/removed), the callback is invoked per resource, components are created and appended as children of a hidden `<div>` inside the calendar element
3. **Server**: Each component's root element gets attributes: `data-rc-resource-id="{resourceId}"` and `data-rc-column-key="{columnKey}"`
4. **Server**: `cellContent` is set to `function() { return { domNodes: [] } }` to suppress FC's default field-value rendering in the cell (returns empty DOM array — cleaner than empty string, avoids FC generating wrapper elements)
5. **Client (TS)**: The column's `cellDidMount` callback finds the matching component element by attribute selector (with `CSS.escape()` on resource ID) and moves (`appendChild`) it from the hidden container into the FC cell
6. **Client (TS)**: The column's `cellWillUnmount` callback moves the element back into the hidden container before FC destroys the cell
7. **Server**: A `Map<String, T>` (resource ID → component) is maintained for runtime access

### Integration with addResources() / removeResources() / removeAllResources()

`FullCalendarScheduler` must be aware of active `ComponentResourceAreaColumn` instances to create/destroy components when resources change.

**Key field:** `FullCalendarScheduler` holds a `private List<ComponentResourceAreaColumn<?>> activeComponentColumns` field (initially empty). This list is updated by `setResourceAreaColumns()`.

**Implementation approach:**

- `setResourceAreaColumns()` detects `ComponentResourceAreaColumn` instances via `instanceof`. It unbinds and destroys components of previously active columns, then binds new columns and creates components for all currently registered resources. Updates `activeComponentColumns`.
- `addResources()` iterates `activeComponentColumns` and calls each column's `createComponent(resource)` for each new resource. Component creation happens immediately (even before attach — Vaadin tracks server-side element tree). Components are appended to the hidden container.
- `removeResources()` iterates `activeComponentColumns` and calls each column's `destroyComponent(resource)` for each removed resource. **This UC also fixes the pre-existing gap in `removeResources()`:** child resources must be recursively removed from `this.resources` map when their parent is removed. This ensures component cleanup and resource map stay consistent.
- `removeAllResources()` iterates `activeComponentColumns` and calls each column's `destroyAllComponents()`
- Child resources added via `Resource.addChild()` → `registerResourcesInternally()` must trigger the same creation logic
- When `setResourceAreaColumns()` is called again (replacing columns), old columns' components are destroyed and columns are unbound, new columns' components are created for all existing resources
- `setResourceAreaColumns(List.of())` (empty list): destroys all components, unbinds all columns, sets the `resourceAreaColumns` FC option to `null` (removes it — FC falls back to default single-column resource label display). An empty array `[]` is NOT sent to FC as its behaviour is undefined.

### Multiple Calendar Instances

### Hidden Container Lifecycle

The hidden container `Element` is created **lazily** on the first `setResourceAreaColumns()` call that contains at least one `ComponentResourceAreaColumn`. It is held as a `private Element hiddenContainer` field on `FullCalendarScheduler`.

- **Creation:** `hiddenContainer = new Element("div")` with attribute `data-fc-component-container` and style `display:none`. Appended to the calendar element via `getElement().appendChild(hiddenContainer)`. This works even before `onAttach()` — Vaadin tracks it in the server-side state tree.
- **Pre-attach:** Components created before attach are appended to the hidden container. Vaadin sends the full subtree on initial attach.
- **Reattach:** The same `hiddenContainer` field survives detach. On reattach (Step 2), it is re-appended to the calendar element, followed by all component elements. Vaadin re-creates the DOM from the server-side state.
- **Not re-created:** The container is created once and reused across column replacements and detach/reattach cycles.

### Bind / Unbind Mechanism

`ComponentResourceAreaColumn<T>` holds a `private FullCalendarScheduler boundCalendar` field (initially `null`).

- **`bind(FullCalendarScheduler)`** (package-private): Sets `boundCalendar`. Throws `IllegalStateException` if already bound to a different calendar.
- **`unbind()`** (package-private): Clears `boundCalendar` to `null`. Called by `setResourceAreaColumns()` when a column is removed from the active set.
- **`isBound()`**: Returns `boundCalendar != null`.
- **`refresh()` / `refreshAll()`**: Check `boundCalendar != null && boundCalendar.isAttached()`. If not attached, silently return (no `beforeClientResponse` registered, no JS call, complete no-op — BR-25).
- **`createComponent(resource)`** / **`destroyComponent(resource)`** / **`destroyAllComponents()`**: Package-private methods called by `FullCalendarScheduler`. These manage the `Map<String, T> components` field and the hidden container DOM.

### Multiple Calendar Instances

Each `FullCalendarScheduler` instance maintains its own independent hidden container and component pool. A `ComponentResourceAreaColumn` instance is bound to one specific calendar — the binding happens when `setResourceAreaColumns()` is called. Columns are unbound when removed from the active column list via a subsequent `setResourceAreaColumns()` call.

**Implications:**
- The same `Resource` object can be used in multiple calendars. Each calendar creates its own component instances via the callback — they are fully independent.
- A `ComponentResourceAreaColumn` instance must NOT be shared across multiple calendars simultaneously. If the same column configuration is needed, create separate instances (the callback can be shared as a variable).
- A column that was unbound (removed from active set) can be reused with a different calendar.
- `getComponent(resource)` always returns the component for the calendar this column is bound to.

```java
// OK: separate column instances, shared callback
SerializableFunction<Resource, DatePicker> factory = res -> new DatePicker();

var col1 = new ComponentResourceAreaColumn<>("deadline", "Deadline", factory);
var col2 = new ComponentResourceAreaColumn<>("deadline", "Deadline", factory);

scheduler1.setResourceAreaColumns(col1);
scheduler2.setResourceAreaColumns(col2);

// NOT OK: same instance in two calendars simultaneously
// scheduler1.setResourceAreaColumns(sharedCol);
// scheduler2.setResourceAreaColumns(sharedCol); // IllegalStateException — already bound

// OK: reuse after unbind
scheduler1.setResourceAreaColumns(new ResourceAreaColumn("title")); // col1 unbound
scheduler2.setResourceAreaColumns(col1); // col1 now bound to scheduler2
```

### Type Parameter

`ComponentResourceAreaColumn<T extends Component>` provides type-safe access:

```java
// Typed column — getComponent() returns Optional<DatePicker>, no casting needed
ComponentResourceAreaColumn<DatePicker> dateCol = new ComponentResourceAreaColumn<>(...);
Optional<DatePicker> picker = dateCol.getComponent(resource);

// getComponents() returns Map<String, DatePicker>
Map<String, DatePicker> all = dateCol.getComponents();
```

### Fluent Method Return Types

`ComponentResourceAreaColumn<T>` overrides all inherited `withXxx()` fluent methods from `ResourceAreaColumn` to return `ComponentResourceAreaColumn<T>` (covariant return types). This preserves the specific type through chaining:

```java
// Returns ComponentResourceAreaColumn<DatePicker>, not ResourceAreaColumn
dateColumn.withWidth("160px").withGroup(true).withHeaderClassNames("bold");
```

The three managed methods (`withCellContent()`, `withCellDidMount()`, `withCellWillUnmount()`) throw `UnsupportedOperationException` — all overloads (String and JsCallback variants, 6 methods total).

### Component Lifecycle

| Event | Action |
|-------|--------|
| `setResourceAreaColumns()` | Bind column to calendar, create components for all currently registered resources |
| `setResourceAreaColumns()` (replacing) | Unbind old columns, destroy old components, bind new columns, create new components |
| `setResourceAreaColumns(List.of())` | Unbind all columns, destroy all components, remove FC option |
| `addResource(res)` | Create component for `res`, append to hidden container, FC picks it up on next render |
| `removeResource(res)` | Remove component from map (incl. children recursively), detach from Vaadin tree |
| `removeAllResources()` | Destroy all components from all active component columns |
| `updateResource(res)` | Component is NOT re-created (update only changes FC display props). Use `refresh(res)` if component must change. |
| `refreshAll()` | Client-side: return all teleported components. Server-side: destroy all, re-create. Then trigger FC resource re-render to fire `cellDidMount`. |
| `refresh(resource)` | Client-side: return teleported component. Server-side: destroy + re-create. No-op if resource not registered. |
| FC view change | `cellWillUnmount` → move back; `cellDidMount` → move to new cell |
| Calendar `detach` | `onDetach()` calls JS to return all teleported components to hidden container. Components kept in server-side map. |
| Calendar `reattach` | Hidden container re-appended, then components re-appended to it; FC `cellDidMount` re-injects them |
| Resources added before `onAttach()` | Queued; components created and appended when calendar attaches |

### Detach / Reattach

The existing `onAttach()` pattern in `FullCalendarScheduler` restores resources via `addResources()` on reattach. Component columns must follow the same pattern:

**On detach (`onDetach()`):**
- **An explicit `onDetach()` override calls a JS function (`returnAllComponentsToContainer()`) to move all teleported components back to the hidden container BEFORE Vaadin removes the DOM tree.** This is critical because FC may NOT fire `cellWillUnmount` during a full DOM removal (it only fires on view changes). Without this, components would be orphaned in FC cells that no longer exist.
- The server-side `Map<String, T>` is **preserved** — component instances and their state survive detach
- The hidden container `Element` object is held as a server-side field and survives detach

**On reattach (`onAttach` with `!isInitialAttach()`):**

Executed as three sequential `beforeClientResponse` steps. All three use the synchronous `runWhenAttached` → `beforeClientResponse` pattern, registered in this exact order within the `onAttach()` method body to guarantee FIFO execution:

1. **Step 1** (`super.onAttach()` → `restoreStateFromServer`): Restores the `resourceAreaColumns` option with the auto-generated JS callbacks (`cellDidMount`/`cellWillUnmount`/`cellContent`)
2. **Step 2** (component re-append, registered in `FullCalendarScheduler.onAttach()` BEFORE Step 3): Re-append the hidden container `Element` to the calendar element, then re-append all component elements to the hidden container. Both operations in the same `beforeClientResponse` callback. The container element object is the same server-side instance — Vaadin sends a full state diff to re-create the DOM.
3. **Step 3** (`FullCalendarScheduler.onAttach()` → `addResources`, registered AFTER Step 2): Re-adds resources to FC → FC renders resource rows → `cellDidMount` fires → components are moved from hidden container into cells

Component state (e.g., DatePicker value, TextField text) is preserved because the Vaadin component instances were never destroyed — Vaadin sends full state via UIDL on reattach.

**Key invariant:** The component _instances_ survive detach/reattach. Only the DOM attachment changes. This is consistent with how Vaadin handles all child components on detach/reattach.

```
Timeline:
  attach    → components created, appended to hidden container, injected into cells
  onDetach  → JS returns all teleported components to hidden container
  detach    → DOM removed, components still in server-side map
  reattach  → Step 1: restore options, Step 2: re-append container+components, Step 3: addResources → cellDidMount
```

### Client-Side Implementation (full-calendar-scheduler.ts)

The TypeScript side needs the following new functionality:

1. **Hidden container**: Created as `<div style="display:none" data-fc-component-container>` inside the calendar element. The attribute is scoped per calendar instance (no ID needed — `cellDidMount` traverses up from `info.el` to find its own calendar's container).

2. **cellDidMount generation**: For each `ComponentResourceAreaColumn`, auto-generate a `cellDidMount` JS callback. The column key is baked into the generated function string at generation time (server-controlled, safe). Resource IDs are escaped with `CSS.escape()` to prevent selector injection:
   ```javascript
   function(info) {
     var calendarEl = info.el.closest('vaadin-full-calendar-scheduler');
     if (!calendarEl) return;
     var container = calendarEl.querySelector('[data-fc-component-container]');
     if (!container) return;
     var resourceId = CSS.escape(info.resource.id);
     var columnKey = 'deadline'; // baked in at generation time, not dynamic
     var component = container.querySelector(
       '[data-rc-resource-id="' + resourceId + '"][data-rc-column-key="' + columnKey + '"]'
     );
     if (component) {
       info.el.appendChild(component);
       component.style.display = '';
     }
   }
   ```

3. **cellWillUnmount generation**: Move element back. Uses the same escaped resource ID pattern:
   ```javascript
   function(info) {
     var calendarEl = info.el.closest('vaadin-full-calendar-scheduler');
     if (!calendarEl) return;
     var container = calendarEl.querySelector('[data-fc-component-container]');
     if (!container) return;
     var resourceId = CSS.escape(info.resource.id);
     var columnKey = 'deadline'; // baked in at generation time
     var component = info.el.querySelector(
       '[data-rc-resource-id="' + resourceId + '"][data-rc-column-key="' + columnKey + '"]'
     );
     if (component) {
       component.style.display = 'none';
       container.appendChild(component);
     }
   }
   ```

4. **cellContent suppression**: Auto-set to `function() { return { domNodes: [] } }` to prevent FC from rendering any default content in the cell.

5. **`returnComponentToContainer(resourceId, columnKey)` method**: New callable method on the TS web component. Finds a teleported component by its data attributes (using `CSS.escape()` on `resourceId`) and moves it back to the hidden container. Called by server-side `refresh()` before component replacement.

6. **`returnAllComponentsToContainer()` method**: New callable method on the TS web component. Uses a **flat `querySelectorAll('[data-rc-resource-id]')` on the calendar element** (NOT per-cell queries) to find ALL teleported components regardless of their current DOM position — they may be in FC cells, in the hidden container, or in limbo if FC has partially destroyed cells. Moves each found element back to the hidden container.

7. **`rerenderResources()` method**: New callable method on the TS web component. One-liner: `this._calendar.render()`. Forces FC to unmount and remount all resource cells, triggering `cellDidMount` for each. Called by `refreshAll()` after server-side component re-creation.

### Refresh and Server-Side Re-Append Safety

**Problem:** When `refresh(resource)` is called, the component may currently be teleported into an FC cell. If the server calls `removeChild` + `appendChild` (to replace the component), Vaadin's reconciler would pull the old component out of the FC cell, causing a visual glitch.

**Solution:** `refresh()` must execute a client-side callback FIRST to return the component to the hidden container, THEN perform the server-side destroy/re-create in a `beforeClientResponse` callback. Sequence:

1. `getElement().callJsFunction("returnComponentToContainer", resourceId, columnKey)` — client moves element back
2. In `beforeClientResponse`: remove old component from map and Vaadin tree, create new one via callback, append to hidden container
3. FC's next render cycle picks up the new component via `cellDidMount`

**`refreshAll()` additionally triggers an FC resource re-render** via `getElement().callJsFunction("rerenderResources")`. This is a new one-line TS method on the web component (`this._calendar.render()`) that forces FC to unmount and remount all resource cells, firing `cellDidMount` for each. This is cleaner than re-transmitting the entire column JSON via option re-set.

### Integration with existing ResourceAreaColumn

`ComponentResourceAreaColumn<T extends Component>` **extends** `ResourceAreaColumn`:
- Inherits `field`, `width`, `group`, `headerClassNames`, etc.
- `field` serves as a unique column key; the default FC cell rendering is suppressed via an auto-generated empty `cellContent` callback. FC still looks up the field value internally, but the suppressed `cellContent` prevents it from being displayed.
- Overrides all `withXxx()` fluent methods to return `ComponentResourceAreaColumn<T>` (covariant return)
- Overrides `withCellContent()`, `withCellDidMount()`, `withCellWillUnmount()` (all 6 overloads: String + JsCallback per method) to throw `UnsupportedOperationException` — these are managed internally
- Overrides `toJson()`: calls `super.toJson()` for inherited properties (field, width, group, headerClassNames, etc.) then **adds** the `cellContent`, `cellDidMount`, and `cellWillUnmount` JSON entries with auto-generated JsCallback markers. (The parent does not write these entries since its fields are null — the override adds, not replaces.)

---

## Error Handling

| Scenario | Behavior |
|----------|----------|
| **Null callback** in constructor | `NullPointerException` (constructor parameter annotated `@NonNull`) |
| **Callback returns null** | `IllegalStateException("Component callback returned null for resource '" + resource.getId() + "'")` — fail fast with clear message |
| **Callback throws exception** | Exception propagates to caller. If thrown during `addResource()`, the resource is still added to FC but has no component (no entry in the component map). `getComponent(resource)` returns `Optional.empty()` for this resource. `refresh(resource)` retries the callback. |
| **Callback throws during `refreshAll()`** | Partial failure: `refreshAll()` continues iterating remaining resources, collects all exceptions, then throws an `IllegalStateException` wrapping the first failure after completing the full iteration. Successfully re-created components are in the map; failed ones retain the old component (not destroyed if re-creation fails). |
| **Callback returns already-attached component** (same instance for two resources) | `IllegalStateException("Component returned by callback is already attached — each resource must receive a unique component instance")` — detected by checking `component.getParent().isPresent()` before append |
| **`getComponent(null)`** | `NullPointerException` |
| **`refresh(null)`** | `NullPointerException` |
| **`refresh(resource)` for unregistered resource** | No-op (silent, no exception) |
| **`refreshAll()` with no resources** | No-op (safe) |
| **`refresh()` called while calendar is detached** | Complete no-op — no JS call, no `beforeClientResponse` registered, no map change. The old component remains in the map and will be re-injected on reattach. The user must call `refresh()` again after reattach if re-creation is needed. Implementation: guard with `boundCalendar.isAttached()` at the top of `refresh()`/`refreshAll()`. |
| **Duplicate column keys** in `setResourceAreaColumns()` | `IllegalArgumentException("Duplicate column field key: 'xxx'")` — two columns (component or regular) with the same `field` value cause selector collisions and are rejected. |

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | Each resource gets exactly one component instance per `ComponentResourceAreaColumn` |
| BR-02 | The component callback is invoked on the server thread (UI access is available). `getComponent()`, `getComponents()`, `refresh()`, and `refreshAll()` must only be called from the UI thread. |
| BR-03 | Components are full Vaadin Flow components — all server-side features (listeners, data binding, validation) work |
| BR-04 | User must NOT manually set `cellContent`, `cellDidMount`, or `cellWillUnmount` on a `ComponentResourceAreaColumn` — these are managed internally. All 6 overloads of `withCellContent()`, `withCellDidMount()`, `withCellWillUnmount()` (String + JsCallback) throw `UnsupportedOperationException`. |
| BR-05 | `getComponent(resource)` returns `Optional.empty()` for resources that do not have a component in this column (either not registered in the scheduler, or callback failed during creation) |
| BR-06 | When a resource is removed, its component is detached from the Vaadin tree (preventing memory leaks). This includes recursive cleanup of child resources. |
| BR-07 | `ComponentResourceAreaColumn` and regular `ResourceAreaColumn` can be freely mixed in `setResourceAreaColumns()` |
| BR-08 | If `setResourceAreaColumns()` is called again, all previous component columns are unbound and their components destroyed. New component columns are bound and components created for all existing resources. |
| BR-09 | Components must survive FC view changes (e.g., switching from `resourceTimelineDay` to `resourceTimelineWeek`) without losing state |
| BR-10 | The hidden container div is a raw `Element` (not a Vaadin `Component`) — just a DOM parking spot. Created as `new Element("div")`. |
| BR-11 | A `ComponentResourceAreaColumn` instance is bound to one calendar at a time. Passing it to a second calendar's `setResourceAreaColumns()` while still bound throws `IllegalStateException`. |
| BR-12 | On calendar detach, `onDetach()` calls `returnAllComponentsToContainer()` JS to return teleported components before DOM removal. Component instances and their state are preserved in the server-side map. On reattach, they are re-appended to the hidden container and re-injected into FC cells via `cellDidMount`. |
| BR-13 | Multiple `FullCalendarScheduler` instances on the same page each maintain independent component pools — no shared state, no cross-calendar interference |
| BR-14 | JS callbacks use `info.el.closest('vaadin-full-calendar-scheduler')` for DOM traversal — naturally scoped to the owning scheduler element. Resource IDs are escaped with `CSS.escape()` in selectors to prevent injection. |
| BR-15 | `updateResource()` does NOT re-create the component. The existing component survives resource property changes. Use `refresh(resource)` to force re-creation. |
| BR-16 | `getComponents()` returns an unmodifiable `Map<String, T>` view keyed by **resource ID** (String). Modifications throw `UnsupportedOperationException`. |
| BR-17 | Resources added before the calendar is attached to the UI are queued. Components are created when `onAttach()` fires. |
| BR-18 | `refresh()` / `refreshAll()` must trigger a client-side return-to-container BEFORE server-side component replacement, to avoid Vaadin's reconciler pulling elements from FC cells mid-display. `refreshAll()` additionally triggers an FC resource re-render to fire `cellDidMount`. |
| BR-19 | `removeAllResources()` destroys all components from all active component columns. |
| BR-20 | The callback must return a non-null, unique (not already attached) component instance per invocation. Violations are detected and throw `IllegalStateException` with a descriptive message. |
| BR-21 | Columns are unbound when removed from the active set via `setResourceAreaColumns()`. An unbound column can be reused with a different calendar. |
| BR-22 | `setResourceAreaColumns()` validates that all column `field` keys are unique across the provided list. Duplicate keys throw `IllegalArgumentException`. |
| BR-23 | If the callback throws during `addResource()`, the resource is added to FC but has no component. `getComponent()` returns empty. `refresh()` retries the callback. |
| BR-24 | `refreshAll()` continues on partial callback failures: successful re-creations are applied, failed ones retain the old component. After iteration, an `IllegalStateException` wrapping the first failure is thrown. |
| BR-25 | `refresh()` / `refreshAll()` called while the calendar is detached are complete no-ops (no JS call, no `beforeClientResponse`, no map change). Guard: `boundCalendar.isAttached()`. The existing components remain and are re-injected on reattach. |

---

## Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| FC destroys cells on view change → components lose DOM parent | Components become orphaned, Vaadin loses connection | `cellWillUnmount` moves them back to hidden container BEFORE FC removes the cell |
| FC does NOT fire `cellWillUnmount` during full calendar detach/destruction | Components orphaned in non-existent FC cells | Explicit `onDetach()` override calls `returnAllComponentsToContainer()` JS before DOM removal. Uses flat `querySelectorAll` on calendar element, not per-cell queries. |
| Large number of resources → many component instances | Memory/performance degradation | Document recommended limit (~100-200 resources with component columns); consider lazy creation for collapsed child resources. Implementation note: consider logging a warning when component count exceeds 200. |
| Component focus lost on FC re-render | User experience degradation (e.g., typing in TextField, FC re-renders, focus lost) | Accept as known limitation; document it. FC re-renders are typically user-initiated (navigation) |
| Keyboard event propagation to FC | Enter/Arrow keys in TextField could trigger FC navigation (FC listens at calendar container level) | E2E test for keyboard events in components. FC typically checks `event.target` and skips form elements, but verify per view type. Document as potential issue. |
| Thread safety — component creation on resource add | Race condition if resources added from background thread | Components are created in the UI access context (same as Vaadin's `access()` pattern). Document that `getComponent()`/`getComponents()` must be called from UI thread only. |
| Components added before calendar is attached | Components not rendered, `appendChild` has no effect | Queue resource-component creation; flush in `onAttach()` after `super.onAttach()` in `beforeClientResponse` |
| Detach/reattach: hidden container DOM is lost on detach | Components no longer visible after reattach | Hidden container Element held as field (survives on server), re-appended in `onAttach()` `beforeClientResponse` |
| Multiple calendars: DOM selector collisions | Wrong component injected into wrong calendar's cell | Use `info.el.closest('vaadin-full-calendar-scheduler')` — naturally scoped, no global selectors |
| ComponentResourceAreaColumn shared across calendars | Component belongs to two parents → Vaadin exception | Throw `IllegalStateException` on second bind; document "create separate instances" pattern |
| Server-side re-append races with teleported position | Vaadin reconciler pulls component from FC cell mid-display | `refresh()` calls client-side `returnComponentToContainer` JS BEFORE server-side re-append (see Architecture section) |
| CSS selector injection via resource IDs | Malicious resource ID breaks querySelector, matches wrong element | All resource IDs escaped with `CSS.escape()` before embedding in attribute selectors. Column keys are server-controlled (baked into generated JS at generation time). |
| `setResourceAreaColumns()` called with no component columns after previous call had them | Orphaned components, memory leak | Explicitly destroy all components from previous component columns and unbind them when `setResourceAreaColumns()` replaces the column list |
| Removing parent resource does not clean up child components | Memory leak, stale components in map | `removeResources()` recursively destroys child resource components |
| Callback returns null or duplicate component | NPE deep in DOM logic or silent component theft | Explicit validation after callback invocation: null → `IllegalStateException`, already-attached → `IllegalStateException` (see Error Handling) |
| `display:none` container affects initial component rendering | Vaadin web components may not fully initialize while hidden | Components are Lit-based — initialization is deferred until first visible render. Verify in E2E tests that DatePicker/ComboBox work correctly after being moved from hidden container to visible cell. |

**Confirmed non-issues (from Vaadin Expert review):**
- **Overlay positioning**: Vaadin 25 uses the native Popover API (`popover="manual"`, top layer rendering). CSS `transform` on FC ancestors does NOT affect overlay positioning. This was a concern in older Vaadin versions but is resolved.
- **Push/WebSocket**: Pattern is transport-independent; node IDs work identically with Push.
- **Lit component `appendChild` move**: Safe — Lit guards against full re-initialization on reconnect. `connectedCallback`/`disconnectedCallback` fire but state is preserved.
- **`evaluateCallbacks()` in full-calendar.ts**: Correctly handles nested `__jsCallback` markers in arrays (recurses into `resourceAreaColumns` array elements).

---

## Acceptance Criteria

- [ ] A `ComponentResourceAreaColumn<T>` can be created with a `SerializableFunction<Resource, T>` callback
- [ ] `getComponent(resource)` returns `Optional<T>` (type-safe, no casting needed)
- [ ] `getComponents()` returns `Map<String, T>` (unmodifiable, keyed by resource ID)
- [ ] Components render visually inside the resource area cells in timeline views
- [ ] Components render visually inside the resource area cells in vertical resource views (resourceTimeGrid)
- [ ] Components are interactive (e.g., DatePicker opens calendar popup, TextField accepts input)
- [ ] Components survive FC view changes without losing state (e.g., a DatePicker value persists)
- [ ] Adding a resource creates a component; removing a resource destroys its component
- [ ] Removing a parent resource also destroys child resources' components
- [ ] Child resources (added via `Resource.addChild()`) also get components created
- [ ] `removeAllResources()` destroys all components
- [ ] `refresh(resource)` destroys and re-creates a single resource's component (state is lost — documented). No-op for unregistered resources.
- [ ] `refreshAll()` destroys, re-creates all components, and triggers FC re-render to inject them
- [ ] `updateResource()` does NOT destroy/re-create the component
- [ ] Component columns and regular columns can be mixed freely
- [ ] All 6 overloads of `withCellContent()`/`withCellDidMount()`/`withCellWillUnmount()` throw `UnsupportedOperationException`
- [ ] All inherited `withXxx()` fluent methods return `ComponentResourceAreaColumn<T>` (covariant)
- [ ] Components are properly detached when the calendar is removed from the UI
- [ ] No memory leaks — removed resources' components are garbage-collectible
- [ ] Replacing component columns via second `setResourceAreaColumns()` call cleans up old components and unbinds old columns
- [ ] `setResourceAreaColumns(List.of())` cleans up everything
- [ ] Unbound columns can be reused with a different calendar
- [ ] **Error handling**: Null callback → NPE; callback returns null → ISE; callback returns duplicate → ISE; duplicate column keys → IAE
- [ ] **Callback throws**: Resource added to FC but no component; `getComponent()` returns empty; `refresh()` retries
- [ ] **refreshAll() partial failure**: Continues iterating, applies successful re-creations, throws ISE after completion
- [ ] **refresh() while detached**: Silently ignored, old component retained
- [ ] **Detach/Reattach**: `onDetach()` returns teleported components to container; after reattach, components are functional (values preserved, listeners still fire)
- [ ] **Multiple calendars**: Two `FullCalendarScheduler` instances on one page with component columns do not interfere with each other
- [ ] **Column binding**: Passing the same `ComponentResourceAreaColumn` instance to two calendars simultaneously throws `IllegalStateException`
- [ ] **Pre-attach resources**: Resources added before calendar attach get components created on attach
- [ ] **CSS.escape()**: Resource IDs with special characters in selectors are handled correctly
- [ ] **Keyboard events**: TextField inside component column accepts Enter/Arrow keys without triggering FC navigation

---

## Tests

### Unit Tests

- [ ] `ComponentResourceAreaColumnTest` — callback invocation, component map management, JSON serialization (incl. auto-generated `cellContent`/`cellDidMount`/`cellWillUnmount` in `toJson()`), refresh logic
- [ ] `ComponentResourceAreaColumnTest` — all 6 overloads of `withCellContent()`/`withCellDidMount()`/`withCellWillUnmount()` throw `UnsupportedOperationException`
- [ ] `ComponentResourceAreaColumnTest` — `IllegalStateException` on double-bind to two calendars
- [ ] `ComponentResourceAreaColumnTest` — unbound column can be reused with different calendar
- [ ] `ComponentResourceAreaColumnTest` — `getComponents()` returns unmodifiable `Map<String, T>`
- [ ] `ComponentResourceAreaColumnTest` — type-safe `getComponent()` returns `Optional<T>`
- [ ] `ComponentResourceAreaColumnTest` — callback returns null → `IllegalStateException`
- [ ] `ComponentResourceAreaColumnTest` — callback returns already-attached component → `IllegalStateException`
- [ ] `ComponentResourceAreaColumnTest` — callback receives correct Resource instance (same object reference, with extendedProps)
- [ ] `ComponentResourceAreaColumnTest` — `refresh(unregisteredResource)` is no-op
- [ ] `ComponentResourceAreaColumnTest` — fluent `withXxx()` methods return `ComponentResourceAreaColumn<T>`
- [ ] `ComponentResourceAreaColumnTest` — null callback in constructor → `NullPointerException`
- [ ] `ComponentResourceAreaColumnTest` — callback throws exception → resource has no component, `getComponent()` returns empty
- [ ] `ComponentResourceAreaColumnTest` — `refreshAll()` partial failure: successful re-creations applied, ISE thrown
- [ ] `ComponentResourceAreaColumnTest` — `getComponent(null)` → NPE, `refresh(null)` → NPE
- [ ] `ComponentResourceAreaColumnTest` — `refreshAll()` with no resources → safe no-op
- [ ] `FullCalendarSchedulerTest` — `addResource`/`removeResource` triggers component creation/destruction on active component columns
- [ ] `FullCalendarSchedulerTest` — `updateResource()` does NOT trigger component re-creation
- [ ] `FullCalendarSchedulerTest` — `removeResource` with children recursively destroys child components
- [ ] `FullCalendarSchedulerTest` — `removeAllResources()` destroys all components
- [ ] `FullCalendarSchedulerTest` — `setResourceAreaColumns()` replacing columns cleans up old components and unbinds old columns
- [ ] `FullCalendarSchedulerTest` — `setResourceAreaColumns(List.of())` cleans up everything
- [ ] `FullCalendarSchedulerTest` — set columns → add resources → set columns again (re-creation for existing resources)
- [ ] `FullCalendarSchedulerTest` — duplicate column field keys in `setResourceAreaColumns()` → `IllegalArgumentException`
- [ ] `FullCalendarSchedulerTest` — mixed regular + component columns produce correct `toJson()` output

### E2E Tests

- [ ] `component-resource-columns.spec.js` — DatePicker renders in resource cell, is interactive, survives view change, value persists after navigation
- [ ] `component-resource-columns.spec.js` — Resource added after column setup gets component rendered
- [ ] `component-resource-columns.spec.js` — Resource removed cleans up component from DOM
- [ ] `component-resource-columns.spec.js` — Two calendars on same page with component columns are independent
- [ ] `component-resource-columns.spec.js` — Detach/reattach preserves component state
- [ ] `component-resource-columns.spec.js` — Keyboard events in TextField (Enter, Arrow keys) don't trigger FC navigation
- [ ] `component-resource-columns.spec.js` — DatePicker overlay opens at correct position in timeline view
- [ ] `component-resource-columns.spec.js` — Component rendered from hidden container is fully functional (not broken by initial `display:none`)

---

## Open Questions

1. **Should collapsed child resources eagerly create components?** Lazy creation would save memory but adds complexity. Recommendation: eager creation initially, optimize later if needed.
2. **Should the header cell also support a component?** (e.g., a "Select All" checkbox in the header). This could be a separate `withHeaderComponent(Component)` method. Recommendation: defer to a follow-up UC. Current API does not preclude it.
3. **What about ComponentRenderer-style reuse (like Grid)?** Grid reuses component instances when scrolling. FC resource lists are typically small enough that reuse isn't needed. Recommendation: one component per resource, no reuse pool.
4. **Lazy-loaded resources**: Currently not supported server-side (resources are always eagerly added). If lazy resource loading is added later, component creation must hook into the lazy-load arrival point. Not a concern for the initial implementation.

---

## Related FullCalendar Docs

- [resourceAreaColumns](https://fullcalendar.io/docs/resourceAreaColumns)
- [Content Injection](https://fullcalendar.io/docs/content-injection)
- [cellDidMount / cellWillUnmount (render hooks)](https://fullcalendar.io/docs/resourceAreaColumns)
