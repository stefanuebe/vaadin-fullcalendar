# UC-015: Scheduler — Resources

**As a** Vaadin application developer, **I want to** define resources and assign entries to them **so that** end users see a resource-based calendar (rooms, people, equipment).

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon-scheduler
**Related Options:** `resourceColumnsWidth`, `resourceColumnHeaderContent`, `resourceOrder`, `resourcesInitiallyExpanded`, `filterResourcesWithEntries`, `resourceGroupField`, `resourceColumns`, `entryResourceEditable`, `datesAboveResources`, `resourceDayHeaderAlign`, `resourceColumnDividerClass`, `resourceExpander*`, and the render hooks `resourceCell*`, `resourceDayHeader*`, `resourceLane*`, `resourceGroupHeader*`, `resourceColumnHeader*`
**Related Events:** `EntryDroppedSchedulerEvent`, `TimeslotClickedSchedulerEvent`, `TimeslotsSelectedSchedulerEvent`

---

## User-Facing Behavior

- Resources appear as rows (timeline views) or columns (vertical resource views)
- Entries are displayed in the row/column of their assigned resource(s)
- Resources support hierarchical trees (parent/child)
- Resources can be colored, grouped, filtered, and ordered
- Entries can be dragged between resources (when `entryResourceEditable = true`)
- Resource columns (the resource area of timeline views) can have multiple columns showing resource properties

---

## Java API Usage

```java
// Create scheduler
FullCalendarScheduler scheduler = new FullCalendarScheduler();
scheduler.options().setSchedulerLicenseKey(Scheduler.AGPL_V3_LICENSE_KEY);

// Add resources
Resource room1 = new Resource(null, "Room A", "#3788d8");
Resource room2 = new Resource(null, "Room B", "#e53935");
scheduler.addResources(room1, room2);

// Hierarchical resources
Resource building = new Resource(null, "Building 1", null);
Resource floor1 = new Resource(null, "Floor 1", null);
building.addChild(floor1);
scheduler.addResource(building);

// Assign entry to resource
ResourceEntry entry = new ResourceEntry();
entry.setTitle("Meeting");
entry.addResources(room1);
scheduler.getEntryProvider().asInMemory().addEntry(entry);

// Per-resource styling
room1.setColor("#e3f2fd");
room1.setEntryContrastColor("#1565c0");
room1.setEntryClassNames(Set.of("room-a-entry"));
scheduler.updateResource(room1);

// Multiple resource columns
scheduler.options().setResourceColumns(List.of(
    new ResourceColumn("title", "Name").withWidth(200),
    new ResourceColumn("department", "Dept").withWidth(150)
));

// Render hooks: class name string or callback returning a string
scheduler.options().setResourceLaneClass("my-lane");
scheduler.options().setResourceDayHeaderContent(
    JsCallback.of("function(info) { return 'Res ' + info.resource.title; }"));

// Enable inter-resource DnD
scheduler.options().setEntryResourceEditable(true);

// Scheduler-specific events (resources are Optional)
scheduler.addEntryDroppedSchedulerListener(event -> {
    Optional<Resource> newResource = event.getNewResource();
    Optional<Resource> oldResource = event.getOldResource();
});
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | `ResourceEntry` can only be added to `FullCalendarScheduler` (throws if plain `FullCalendar`) |
| BR-02 | A `ResourceEntry` can be assigned to multiple resources (M:N relationship) |
| BR-03 | `Resource.setTitle()` and `setColor()` auto-push to client; other style properties (`setEntryContrastColor`, `setEntryClassNames`, ...) need `updateResource()`. Entries already shown repaint with the new entry style props |
| BR-04 | `filterResourcesWithEntries = true` hides resources with no entries |
| BR-05 | `resourcesInitiallyExpanded = false` collapses child resources on load |
| BR-06 | `EntryDroppedSchedulerEvent` includes old and new resource as `Optional<Resource>` |
| BR-07 | FullCalendar 7 knows one entry color (`eventColor`), set with `Resource.setColor()` or the constructor. FullCalendar sets it on the entry's element as the CSS variable `--fc-event-color`, and the theme's styles decide where it shows. `Resource.setEntryContrastColor()` sends `eventContrastColor`, `setEntryClassNames()` sends one space-separated `eventClass` string. |
| BR-08 | The scheduler option fields carry the library's option names. The resource label hooks are split into `resourceCell*` (timeline resource cells) and `resourceDayHeader*` (resource headers in resource time grid and day grid views). The lane content is split into `resourceLaneTopContent` and `resourceLaneBottomContent`. Class hooks (`*Class`) take a class name string or a callback returning one. |
| BR-09 | `scheduler.options().setResourceColumns(List)` binds component columns to the calendar and unbinds the columns set before. An empty list or null removes the option. |
| BR-10 | The date/resource order of resource time grid and day grid views is set with `datesAboveResources`. |
| BR-11 | `Scheduler.AGPL_V3_LICENSE_KEY` is the key for open source projects. FullCalendar 7 treats the former GPL key as invalid. |
| BR-12 | The newer resource render hooks (`*InnerClass` variants, `resourceColumnDividerClass`, `resourceColumnResizerClass`, `resourceExpander*`, `resourceIndentClass`, `resourceHeaderRowClass`, `resourceRowClass`, `resourceGroupLaneInnerClass`) and `resourceDayHeaderAlign` (`HeaderAlignValue`) have fields. Some take a class name string only, see the Javadoc of `SchedulerViewOptions`. Virtualization and printing are in UC-026 |

---

## Acceptance Criteria

- [ ] Resources render as rows in timeline views
- [ ] Resources render as columns in vertical resource views
- [ ] Entries appear in their assigned resource's row/column
- [ ] Hierarchical resources display with expand/collapse
- [x] Per-resource colors apply to associated entries (`eventColor`, `eventContrastColor`, `eventClass`)
- [ ] Dragging between resources updates resource assignment
- [ ] `EntryDroppedSchedulerEvent` fires with old/new resource
- [ ] `filterResourcesWithEntries` hides empty resources
- [x] Multiple resource columns display correctly
- [ ] Resource grouping by field works
- [x] Scheduler option fields send the library's option names
- [x] Column header content, resource cell class, lane class and top content reach resource timeline views
- [x] Resource day header class and content reach resource time grid and day grid views
- [x] `updateResource` sends `eventColor`, `eventContrastColor` and `eventClass` under their FullCalendar 7 keys
- [x] After `updateResource` the entries of the resource that are already shown repaint with the new color, contrast color and classes, and lose the ones removed on the server (#276)
- [x] A resource without color keeps the default entry color (`eventColor` is sent only when set)
- [x] A removed or replaced extended prop of a shown resource reaches the client. FullCalendar cannot delete an extended prop, so the key stays with the value `undefined`

---

## Tests

### Unit Tests
- [ ] Resource model tests — hierarchy, JSON serialization
- [x] `SchedulerOptionsTest` — `setResourceColumns` binds component columns, clears them with null, rejects duplicate fields and sends the columns, a raw `resourceColumns` option is sent as it is, print layout, license constants

### E2E Tests
- [ ] `scheduler-features.spec.js` — resource display and interaction
- [x] `scheduler-views.spec.js` — resource timeline, resource time grid and resource day grid: column header content, resource cell class, lane class and top content, resource day header class and content, resource entry color / contrast color / class, `updateResource` payload and repaint

---

## Related FullCalendar Docs

- [Resources](https://fullcalendar.io/docs/resource-data)
- [resourceColumns](https://fullcalendar.io/docs/resourceColumns)
- [eventResourceEditable](https://fullcalendar.io/docs/eventResourceEditable)
