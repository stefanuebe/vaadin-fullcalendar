# Phase 5: Missing Scheduler/Resource Features

## Goal

Complete the scheduler (resource) feature coverage: add missing typed options, resource display configuration, resource lifecycle callbacks, and resource property model improvements.

---

## Features Covered

### 5.1 `resourceAreaColumns` typed API

FC's `resourceAreaColumns` turns the resource sidebar from a plain title list into a multi-column data grid. Each column maps to a resource property. When set, FC renders a header row with column titles and a column for each configured field.

**New class `ResourceAreaColumn`:**
```java
public class ResourceAreaColumn {
    private String field;          // resource property name (matches a field in Resource.toJson() output, or extendedProps key)
    private String headerContent;  // header text or HTML string; also accepts JS function string for dynamic content
    private boolean group;         // if true, resources are grouped by this column's field value
    private String width;          // CSS width string: "150px", "20%"

    // Header render hooks (JS function strings, same pattern as other render hooks):
    private String headerClassNames;   // JS function: (headerInfo) => string[]
    private String headerDidMount;     // JS function: (headerInfo) => void
    private String headerWillUnmount;  // JS function: (headerInfo) => void

    public ResourceAreaColumn(String field) { ... }
    public ResourceAreaColumn(String field, String headerContent) { ... }
    // Fluent setters:
    public ResourceAreaColumn withWidth(String width) { ... }
    public ResourceAreaColumn withGroup(boolean group) { ... }
    public ResourceAreaColumn withHeaderClassNames(String jsFunction) { ... }
    // etc.
}
```

**`toJson()` output shape:**
```json
{
  "field": "department",
  "headerContent": "Department",
  "width": "150px",
  "group": false
}
```
With render hooks:
```json
{
  "field": "capacity",
  "headerContent": "Capacity",
  "headerClassNames": "function(info) { return ['capacity-header']; }",
  "headerDidMount": "function(info) { /* init tooltip */ }"
}
```

**New methods on `Scheduler`:**
```java
void setResourceAreaColumns(List<ResourceAreaColumn> columns)
void setResourceAreaColumns(ResourceAreaColumn... columns)  // varargs overload
```

Serializes to a JSON array: `setOption("resourceAreaColumns", jsonArray)`.

**Implementation pattern:** Follow `BusinessHours.toJson()` for the object-to-JSON pattern. The `Scheduler` implementation (`FullCalendarScheduler`) calls `setOption(SchedulerOption.RESOURCE_AREA_COLUMNS, jsonArray)` where `jsonArray` is built by calling `toJson()` on each column.

**JSON mapping note:** `field` maps to `"field"` in FC, `headerContent` to `"headerContent"`, `group` to `"group"`, `width` to `"width"`. All field names are direct — no camelCase conversion needed since they match FC's expected keys.

---

### 5.2 `resourceGroupField` typed setter

FC's `resourceGroupField` option causes resources to be **visually grouped** in the timeline by a field value. Example: if resources have an `extendedProp` called `department`, setting `resourceGroupField = "department"` renders a group header row ("Sales", "Engineering") above the resources in each group.

The grouping is entirely visual/display-level — it does not affect how resources are fetched or ordered (though resources must be ordered by the group field for contiguous groups to appear together).

```java
void setResourceGroupField(String fieldName)  // in Scheduler interface
// Implementation in FullCalendarScheduler:
public void setResourceGroupField(String fieldName) {
    setOption(SchedulerOption.RESOURCE_GROUP_FIELD, fieldName);
}
```

**When to use `group: true` in `ResourceAreaColumn` vs `resourceGroupField`:**
- `resourceGroupField` = global grouping option; creates collapsible group rows in the resource lane area
- `ResourceAreaColumn.group = true` = defines which column drives the grouping when `resourceAreaColumns` is in use

They work together: set `resourceGroupField` to define the grouping key, and optionally set `group: true` on the corresponding `ResourceAreaColumn`.

---

### 5.3 Resource group render hooks

When `resourceGroupField` is set, FC renders group header rows above each resource group. These can be customized with render hooks.

**JS callback argument** for all resource group hooks: the `groupInfo` object:
```js
{
  groupValue: any,       // the value of the group field (e.g., "Sales", "Engineering")
  resources: Resource[], // array of Resource objects in this group
  view: View             // current view object
}
```

Example for `resourceGroupContent`:
```js
"function(info) { return { html: '<b>' + info.groupValue + '</b> (' + info.resources.length + ')' }; }"
```

```java
void setResourceGroupClassNamesCallback(String jsFunction)   // in Scheduler
void setResourceGroupContentCallback(String jsFunction)
void setResourceGroupDidMountCallback(String jsFunction)
void setResourceGroupWillUnmountCallback(String jsFunction)
```

**Pattern:** Same as existing resource label/lane hooks already implemented in `Scheduler`. Calls `setOption("resourceGroupClassNames", s)` etc.

---

### 5.4 Resource area header render hooks

The resource area header (top-left cell in timeline view) can be customized beyond the plain string `setResourceAreaHeaderContent(String)` already supported:

```java
void setResourceAreaHeaderClassNamesCallback(String jsFunction)   // in Scheduler
void setResourceAreaHeaderDidMountCallback(String jsFunction)
void setResourceAreaHeaderWillUnmountCallback(String jsFunction)
```

**JS callback argument:**
```js
{
  view: View  // current view object
}
```

Note: `setResourceAreaHeaderContent(String)` already exists and accepts either a plain string (title text) or a JS function string. The `ClassNames`, `DidMount`, and `WillUnmount` hooks are the missing completions.

---

### 5.5 `datesAboveResources` typed setter

For vertical resource views (`resourceTimeGrid`, `resourceDayGrid`), determines whether date headings appear above resource headings (true) or resource headings appear above date headings (false).

```java
void setDatesAboveResources(boolean datesAboveResources)  // in Scheduler
// Implementation:
public void setDatesAboveResources(boolean datesAboveResources) {
    setOption(SchedulerOption.DATES_ABOVE_RESOURCES, datesAboveResources);
}
```

Default in FC: `false` (resources above dates).

---

### 5.6 `refetchResourcesOnNavigate` typed setter

`SchedulerOption.REFETCH_RESOURCES_ON_NAVIGATE` is in the enum but there is no typed setter in the `Scheduler` interface.

```java
void setRefetchResourcesOnNavigate(boolean refetch)  // in Scheduler
// Implementation:
public void setRefetchResourcesOnNavigate(boolean refetch) {
    setOption(SchedulerOption.REFETCH_RESOURCES_ON_NAVIGATE, refetch);
}
```

**When to use:** When using a `ResourceProvider` (Phase 5.9) that returns different resources depending on the date range, set this to `true` so FC re-fetches resources when the user navigates to a new period. Default is `false`.

Also cross-referenced in Phase 0.7 as a simple missing typed setter.

---

### 5.7 `eventMinWidth` (timeline view) typed setter

Determines the minimum width (in pixels) of events in timeline view. Useful to ensure very short events remain clickable.

```java
void setEventMinWidth(int pixels)  // in Scheduler
public void setEventMinWidth(int pixels) {
    setOption(SchedulerOption.EVENT_MIN_WIDTH, pixels);
}
```

Default in FC: `3` pixels.

---

### 5.8 Resource lifecycle callbacks (server-side events)

FC fires callbacks when resources change in its internal store:
- `resourceAdd` — after `addResource()`
- `resourceChange` — after `Resource.setProp()` or `Resource.setExtendedProp()`
- `resourceRemove` — after `Resource.remove()`
- `resourcesSet` — after resources are initialized or any change

**STATUS: JS-string callbacks only — same reasoning as `eventAdd/Change/Remove/Set` (Phase 3.2)**

Since resources are server-managed in the addon (Java `Resource` objects pushed to FC via `Scheduler.addResource()`), the server already knows about all additions and removals. These callbacks are only useful for client-side reactions (e.g., updating a DOM counter).

For the analogous event-side decision, see `discussion-event-sources.md`. The same logic applies: the server is the source of truth; the callbacks are redundant for server-managed data. If future function/JSON-feed resource sources are added (Phase 5.9), these callbacks would gain more value — but implement them then, not now.

**JS-string callbacks only:**
```java
void setResourceAddCallback(String jsFunction)       // in Scheduler
void setResourceChangeCallback(String jsFunction)
void setResourceRemoveCallback(String jsFunction)
void setResourcesSetCallback(String jsFunction)
```

---

### 5.9 Function / JSON feed resource sources

Similar to event sources (Phase 4), FC supports loading resources from a function or a URL. This is analogous to `CallbackEntryProvider` for resources.

**Concept:** Instead of the server pushing all resources up-front, the browser calls a function that fetches resources for the current date range. This enables server-side resource filtering based on the visible date range.

**Proposed Java API:**

```java
// Functional interface for the resource provider:
@FunctionalInterface
public interface ResourceProvider {
    List<Resource> fetchResources(LocalDate start, LocalDate end, Timezone timezone);
}

// Registration on Scheduler:
void setResourceProvider(ResourceProvider provider)  // in Scheduler
```

**Implementation approach (analogous to `CallbackEntryProvider`):**
1. The TS companion registers a `resources` function option on the FC calendar
2. When FC calls this function, the TS calls `this.$server.fetchResources(startStr, endStr, timezone)` (a `@ClientCallable` method)
3. The Java `fetchResources` method calls the registered `ResourceProvider`
4. The result is serialized to JSON and sent back to the client

**JSON feed URL approach (simpler, no server callback):**
```java
void setResourceFeedUrl(String url)
void setResourceFeedUrl(String url, Map<String, Object> extraParams)
```
This requires no server-side `@ClientCallable` — FC fetches the URL directly. However, the URL must be publicly accessible from the browser, making it less suitable for secured Vaadin applications.

**Note:** `refetchResourcesOnNavigate` (5.6) is especially important when using `setResourceProvider` — set it to `true` if resources differ by date range.

**Frontend TypeScript impact:** Required for `setResourceProvider` — must add a `resources` callback that calls `@ClientCallable` and returns resources asynchronously (using the FC success callback pattern, analogous to the existing `CallbackEntryProvider` frontend implementation).

---

### 5.10 Resource property model improvements

Currently `Resource.java` has `id`, `title`, `color`, `businessHoursArray` as `final` fields. The `toJson()` method outputs:
```json
{
  "id": "room-1",
  "title": "Meeting Room 1",
  "eventColor": "#3788d8",
  "businessHours": [...],
  "children": [...],
  // ... extendedProps keys spread directly into root object
}
```

FC supports updating resource properties after add via `Resource.setProp(name, value)` on the client. The addon provides no equivalent server-side mechanism — once a resource is added, its title and color cannot be changed without removing and re-adding it.

**Proposed changes:**

Make `title` and `color` mutable with server-push update mechanism:
```java
// In Resource.java — change final to mutable:
private String title;     // was final
private String color;     // was final

// Add setters that also trigger client update:
public void setTitle(String title) {
    this.title = title;
    pushUpdateToClient();  // if attached to a scheduler
}

public void setColor(String color) {
    this.color = color;
    pushUpdateToClient();
}
```

**`Scheduler.updateResource(Resource)` mechanism:**

The push mechanism needs a reference to the `Scheduler`. Two options:
1. Resource holds a weak reference to the scheduler it was added to (like `Entry` holds a reference to its `FullCalendar`)
2. Developer calls `scheduler.updateResource(resource)` explicitly

Option 1 is more ergonomic (like `Entry.calendar`). Option 2 is more explicit and avoids back-references. Recommend option 1 for consistency with `Entry`.

The client-side push would use:
```java
getElement().callJsFunction("updateResource", resource.toJson().toString());
```
where `updateResource` is a new TS companion function that calls FC's `getResourceById(id).setProp(key, value)` for each changed property.

---

### 5.11 Per-resource event property overrides on `Resource`

The FC resource object supports per-resource event styling overrides. All events associated with the resource inherit these styles unless the event overrides them explicitly.

Currently `Resource` only supports `eventColor` (shorthand that sets background+border). Missing:

| FC field | Java field | Default |
|---|---|---|
| `eventBackgroundColor` | `private String eventBackgroundColor` | null |
| `eventBorderColor` | `private String eventBorderColor` | null |
| `eventTextColor` | `private String eventTextColor` | null |
| `eventConstraint` | `private String eventConstraint` | null |
| `eventOverlap` | `private Boolean eventOverlap` | null (use Boolean not boolean) |
| `eventClassNames` | `private Set<String> eventClassNames` | null |

**Additions to `Resource.java`:**
```java
private String eventBackgroundColor;
private String eventBorderColor;
private String eventTextColor;
private String eventConstraint;
private Boolean eventOverlap;      // nullable: null = inherit calendar default
private Set<String> eventClassNames;
```

**JSON serialization in `toJson()`:** Add each non-null field to the JSON object:
```java
if (eventBackgroundColor != null) jsonObject.put("eventBackgroundColor", eventBackgroundColor);
if (eventBorderColor != null) jsonObject.put("eventBorderColor", eventBorderColor);
// etc.
```

Field names match FC's expected property names exactly — no mapping needed.

**Note:** `eventColor` (already present) is a shorthand that sets both `eventBackgroundColor` and `eventBorderColor` simultaneously. When both `eventColor` and `eventBackgroundColor` are set, `eventBackgroundColor` takes precedence in FC.

---

### 5.12 Typo fix: `setResourceLablelWillUnmountCallback`

The method `Scheduler.setResourceLablelWillUnmountCallback(String)` has a typo (`Lablel` instead of `Label`). Fix to `setResourceLabelWillUnmountCallback(String)` with a deprecated alias for the old name.

**This is a genuine bug (typo), so use `@Deprecated(forRemoval = true)`:**
```java
/**
 * @deprecated Typo in method name. Use {@link #setResourceLabelWillUnmountCallback(String)} instead.
 */
@Deprecated(forRemoval = true)
default void setResourceLablelWillUnmountCallback(String jsFunction) {
    setResourceLabelWillUnmountCallback(jsFunction);
}

void setResourceLabelWillUnmountCallback(String jsFunction);
```

Also cross-referenced in Phase 0.2 naming inconsistencies.

---

### 5.13 `slotMinWidth` (timeline views)

The `slotMinWidth` option sets the minimum width in pixels for each time slot column in timeline views. Without it, FC calculates column width automatically based on the visible range. Setting a minimum prevents columns from becoming too narrow to read.

```java
void setSlotMinWidth(int pixels)  // in Scheduler
// Implementation:
public void setSlotMinWidth(int pixels) {
    setOption(SchedulerOption.SLOT_MIN_WIDTH, pixels);  // or "slotMinWidth" string if no enum entry
}
```

Default: automatically calculated. Common values: `30` (narrow, dense timeline) to `100` (wide, spacious).

**Use case:** A week timeline with 30-minute slots would have 14 slots/day × 7 days = 98 slots. At default auto width, each slot may be 5-10px — unreadable. Setting `slotMinWidth = 30` ensures each slot is at least 30px wide and enables horizontal scrolling.

---

### 5.14 `resourceAreaWidth` — resource area sidebar width

Sets the width of the resource sidebar (the left column showing resource names) in timeline views. Accepts any CSS width value.

```java
void setResourceAreaWidth(String width)  // in Scheduler
// Implementation:
public void setResourceAreaWidth(String width) {
    setOption(SchedulerOption.RESOURCE_AREA_WIDTH, width);  // or "resourceAreaWidth" string
}
```

Examples: `"200px"`, `"20%"`, `"15em"`.

Default: FC uses approximately `30%` of the calendar width. For narrow resource names, `"150px"` is typical; for long names or multi-column area, `"300px"` may be needed.

**When `resourceAreaColumns` is set (5.1):** The `resourceAreaWidth` controls the total width of the multi-column area, not individual column widths (those are set per-column via `ResourceAreaColumn.width`).

---

## Implementation Notes

- `ResourceAreaColumn` is the most complex new class in this phase — it requires its own `toJson()` method following the pattern of `BusinessHours.toJson()`
- Resource update propagation (5.10) requires either a new client-side function call or extending the existing resource management JS code in the TS companion
- The typo fix (5.12) requires a deprecated alias — add the corrected method and mark the old one `@Deprecated(forRemoval = true)` since it is clearly a bug, not a naming preference
- Resource group render hooks (5.3) follow exactly the same pattern as resource label/lane hooks already implemented in `Scheduler`
- Items 5.13 and 5.14 are trivially small typed setter additions

---

## Testing

### JUnit tests
Add a test class `Phase5SchedulerTest.java` in `addon-scheduler/src/test/java/org/vaadin/stefan/fullcalendar/`.

Cover:
- `ResourceAreaColumn`: verify `toJson()` output has correct field mappings (field → "field", headerContent → "headerContent", etc.), null handling, render hook string storage
- Scheduler option setters: verify `setResourceAreaColumns()`, `setResourceGroupField()`, `setSlotMinWidth()`, `setResourceAreaWidth()` call `setOption()` with correct keys
- JS callback storage: verify `setResourceGroupClassNamesCallback()` and similar callbacks store JS functions correctly
- Resource mutability (5.10): verify `resource.setTitle()` and `resource.setColor()` update the resource and trigger client sync
- Typo fix (5.12): verify both `setResourceLabelWillUnmountCallback()` and deprecated `setResourceLablelWillUnmountCallback()` work

### Playwright tests (client-side effects)
Add demo view at `demo/src/main/java/org/vaadin/stefan/ui/view/testviews/Phase5SchedulerTestView.java` with:
- A timeline view with multiple resources
- `resourceAreaColumns` configured to show resource properties
- Resource grouping enabled with `setResourceGroupField()`
- Data-testid markers on resource group headers and resource labels to verify custom CSS/HTML from render hooks

Add Playwright spec at `e2e-tests/tests/phase5-scheduler.spec.js` to verify:
- Resource area columns render with correct headers
- Resource grouping creates group header rows
- Resource group render hooks apply custom CSS classes
- Slot and lane render hooks customize their appearance

### Code and test review
After implementing all features and writing tests, review each artifact before committing:
- Run a `code-reviewer` agent on the implementation code (new classes, FullCalendar.java changes, frontend TypeScript). Fix all issues found.
- Run a `code-reviewer` agent on the JUnit tests. Fix all issues found (missing null-clearing tests, weak assertions, missing edge cases, etc.).
- Run a `code-reviewer` agent on the Playwright spec. Fix all issues (weak selectors, missing value assertions, flaky timing patterns, duplicate helpers vs. fixtures.js, etc.).
- Commit only after all review passes are clean.



---

## Files to Modify

- `addon-scheduler/src/main/java/org/vaadin/stefan/fullcalendar/Scheduler.java`
  - Add new methods: `setResourceAreaColumns`, `setResourceGroupField`, `setDatesAboveResources`, `setRefetchResourcesOnNavigate`, `setEventMinWidth`, `setSlotMinWidth`, `setResourceAreaWidth`
  - Add resource group render hook callbacks: `setResourceGroupClassNamesCallback`, `setResourceGroupContentCallback`, `setResourceGroupDidMountCallback`, `setResourceGroupWillUnmountCallback`
  - Add JS-only resource lifecycle callbacks: `setResourceAddCallback`, `setResourceChangeCallback`, `setResourceRemoveCallback`, `setResourcesSetCallback`
  - Fix typo: add `setResourceLabelWillUnmountCallback`, deprecate `setResourceLablelWillUnmountCallback`
- `addon-scheduler/src/main/java/org/vaadin/stefan/fullcalendar/FullCalendarScheduler.java`
  - Implement all new `Scheduler` interface methods
  - Add `SchedulerOption` enum entries for new options (or use raw string keys where no enum entry exists)
- `addon-scheduler/src/main/java/org/vaadin/stefan/fullcalendar/Resource.java`
  - Add mutable title/color with optional push mechanism
  - Add per-resource event property overrides: `eventBackgroundColor`, `eventBorderColor`, `eventTextColor`, `eventConstraint`, `eventOverlap`, `eventClassNames`
  - Update `toJson()` to include all new fields
- New class: `addon-scheduler/src/main/java/org/vaadin/stefan/fullcalendar/ResourceAreaColumn.java`
- Optional new interface: `ResourceProvider.java`
