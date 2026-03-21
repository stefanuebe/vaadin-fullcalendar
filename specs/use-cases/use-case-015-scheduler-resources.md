# UC-015: Scheduler — Resources

**As a** Vaadin application developer, **I want to** define resources and assign entries to them **so that** end users see a resource-based calendar (rooms, people, equipment).

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon-scheduler
**Related Options:** `SchedulerOption.RESOURCE_AREA_WIDTH`, `SchedulerOption.RESOURCE_AREA_HEADER_CONTENT`, `SchedulerOption.RESOURCE_ORDER`, `SchedulerOption.RESOURCES_INITIALLY_EXPANDED`, `SchedulerOption.FILTER_RESOURCES_WITH_ENTRIES`, `SchedulerOption.RESOURCE_GROUP_FIELD`, `SchedulerOption.RESOURCE_AREA_COLUMNS`, `SchedulerOption.ENTRY_RESOURCES_EDITABLE`
**Related Events:** `EntryDroppedSchedulerEvent`, `TimeslotClickedSchedulerEvent`, `TimeslotsSelectedSchedulerEvent`

---

## User-Facing Behavior

- Resources appear as rows (timeline views) or columns (vertical resource views)
- Entries are displayed in the row/column of their assigned resource(s)
- Resources support hierarchical trees (parent/child)
- Resources can be colored, grouped, filtered, and ordered
- Entries can be dragged between resources (when `ENTRY_RESOURCES_EDITABLE = true`)
- Resource area can have multiple columns showing resource properties

---

## Java API Usage

```java
// Create scheduler
FullCalendarScheduler scheduler = FullCalendarBuilder.create()
    .withScheduler(Scheduler.GPL_V3_LICENSE_KEY)
    .build();

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
room1.setEntryBackgroundColor("#e3f2fd");
room1.setEntryTextColor("#1565c0");
scheduler.updateResource(room1);

// Multiple resource area columns
scheduler.setResourceAreaColumns(
    new ResourceAreaColumn("title", "Name").withWidth("200px"),
    new ResourceAreaColumn("department", "Dept").withWidth("150px")
);

// Enable inter-resource DnD
scheduler.setOption(SchedulerOption.ENTRY_RESOURCES_EDITABLE, true);

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
| BR-03 | `Resource.setTitle()` and `setColor()` auto-push to client; other style properties need `updateResource()` |
| BR-04 | `FILTER_RESOURCES_WITH_ENTRIES = true` hides resources with no entries |
| BR-05 | `RESOURCES_INITIALLY_EXPANDED = false` collapses child resources on load |
| BR-06 | `EntryDroppedSchedulerEvent` includes old and new resource as `Optional<Resource>` |

---

## Acceptance Criteria

- [ ] Resources render as rows in timeline views
- [ ] Resources render as columns in vertical resource views
- [ ] Entries appear in their assigned resource's row/column
- [ ] Hierarchical resources display with expand/collapse
- [ ] Per-resource colors apply to associated entries
- [ ] Dragging between resources updates resource assignment
- [ ] `EntryDroppedSchedulerEvent` fires with old/new resource
- [ ] `FILTER_RESOURCES_WITH_ENTRIES` hides empty resources
- [ ] Multiple resource area columns display correctly
- [ ] Resource grouping by field works

---

## Tests

### Unit Tests
- [ ] Resource model tests — hierarchy, JSON serialization

### E2E Tests
- [ ] `scheduler-features.spec.js` — resource display and interaction

---

## Related FullCalendar Docs

- [Resources](https://fullcalendar.io/docs/resource-data)
- [resourceAreaColumns](https://fullcalendar.io/docs/resourceAreaColumns)
- [eventResourceEditable](https://fullcalendar.io/docs/eventResourceEditable)
