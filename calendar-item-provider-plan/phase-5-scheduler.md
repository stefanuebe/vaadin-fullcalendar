# Phase 5: Scheduler Extension Integration

**Goal:** Integrate CIP with the addon-scheduler module for resource-based views.
**Prerequisite:** Phase 4 complete.
**Breaking changes:** None — additive only.

---

## Overview

The Scheduler extension adds resource management to FullCalendar. Currently, it uses
`ResourceEntry extends Entry` with a `Set<Resource> resources` field. For CIP, POJOs
need a way to declare their resource associations.

---

## FullCalendarScheduler Typing

```java
// Before:
public class FullCalendarScheduler extends FullCalendar implements Scheduler

// After — concrete type for Entry-based usage:
public class FullCalendarScheduler extends FullCalendar<Entry> implements Scheduler
```

This is the natural choice: the scheduler without CIP always works with Entry/ResourceEntry.
When a user sets a CIP on the scheduler, the generic type is effectively overridden internally
(the CIP fields hold the actual `<T>` items, while the class signature stays `<Entry>` for
backward compatibility with existing code).

**Alternative** (if more type safety is desired):
```java
public class FullCalendarScheduler<T> extends FullCalendar<T> implements Scheduler
```
This is more flexible but breaks existing `FullCalendarScheduler scheduler = ...` code with
raw type warnings. Evaluate during implementation.

---

## SchedulerCalendarItemPropertyMapper<T>

Extends the base mapper with resource-related mappings. Lives in addon-scheduler:

```java
package org.vaadin.stefan.fullcalendar;

public class SchedulerCalendarItemPropertyMapper<T> extends CalendarItemPropertyMapper<T> {
    private ValueProvider<T, Set<String>> resourceIdsProvider;
    private ValueProvider<T, Boolean> resourceEditableProvider;

    // Optional setter for resource changes (drag between resources)
    private SerializableBiConsumer<T, Set<String>> resourceIdsSetter;

    public SchedulerCalendarItemPropertyMapper<T> resourceIds(
            ValueProvider<T, Set<String>> provider) { ... }
    public SchedulerCalendarItemPropertyMapper<T> resourceIds(
            ValueProvider<T, Set<String>> getter,
            SerializableBiConsumer<T, Set<String>> setter) { ... }

    public SchedulerCalendarItemPropertyMapper<T> resourceEditable(
            ValueProvider<T, Boolean> provider) { ... }

    @Override
    public ObjectNode toJson(T item) {
        ObjectNode node = super.toJson(item);
        if (resourceIdsProvider != null) {
            // Add resourceIds array to JSON
        }
        if (resourceEditableProvider != null) {
            node.put("resourceEditable", resourceEditableProvider.apply(item));
        }
        return node;
    }

    @Override
    public void applyChanges(T item, ObjectNode changes) {
        super.applyChanges(item, changes);
        if (changes.has("resourceIds") && resourceIdsSetter != null) {
            Set<String> newIds = parseResourceIds(changes.get("resourceIds"));
            resourceIdsSetter.accept(item, newIds);
        }
    }

    public static <T> SchedulerCalendarItemPropertyMapper<T> of(Class<T> type) { ... }
}
```

---

## Scheduler Events for CIP

Extend CIP events with resource info:

```java
public class CalendarItemDroppedSchedulerEvent<T> extends CalendarItemDroppedEvent<T> {
    private final Resource oldResource;
    private final Resource newResource;

    public Optional<Resource> getOldResource() { ... }
    public Optional<Resource> getNewResource() { ... }
}
```

The scheduler overrides the event dispatch to fire these scheduler-specific CIP events
instead of the base CIP events when resource data is present in the DOM event.

---

## FullCalendarScheduler — CIP Overrides

```java
public class FullCalendarScheduler extends FullCalendar<Entry> implements Scheduler {

    // Override to handle resource removal for both Entry and CIP paths
    @Override
    public void removeResources(Resource... resources) {
        if (!isUsingCalendarItemProvider()) {
            // Existing behavior: filter instanceof ResourceEntry, remove resources
            EntryProvider<Entry> entryProvider = getEntryProvider();
            // ... existing logic
        }
        // Both paths: remove from resource registry and client
        getElement().callJsFunction("removeResources", ...);
    }
}
```

---

## Builder Support

```java
// Existing builder supports scheduler. CIP + scheduler combo:
FullCalendar<Meeting> calendar = FullCalendarBuilder.create(Meeting.class)
    .withScheduler("your-license-key")
    .withCalendarItemProvider(provider, schedulerMapper)
    .build();
```

---

## Example Usage

```java
public class Meeting {
    private Long id;
    private String subject;
    private LocalDateTime start;
    private LocalDateTime end;
    private String roomId;  // Resource ID
}

var mapper = SchedulerCalendarItemPropertyMapper.of(Meeting.class)
    .id(m -> String.valueOf(m.getId()))
    .title(Meeting::getSubject)
    .start(Meeting::getStart, Meeting::setStart)
    .end(Meeting::getEnd, Meeting::setEnd)
    .resourceIds(m -> Set.of(m.getRoomId()),
                 (m, ids) -> m.setRoomId(ids.iterator().next()));

var provider = CalendarItemProvider.fromCallbacks(
    query -> meetingService.findBetween(query.getStart(), query.getEnd()),
    id -> meetingService.findById(Long.parseLong(id))
);

FullCalendar<Meeting> calendar = FullCalendarBuilder.create(Meeting.class)
    .withScheduler("your-license-key")
    .withCalendarItemProvider(provider, mapper)
    .build();

calendar.addCalendarItemDroppedListener(event -> {
    Meeting meeting = event.applyChangesOnItem(); // applies start, end, resourceIds via setters
    meetingService.save(meeting);
});
```

---

## Testing

- Test `SchedulerCalendarItemPropertyMapper` adds `resourceIds` to JSON output
- Test `FullCalendarScheduler` with CIP renders items in correct resource lanes
- Test scheduler-specific CIP events include old/new resource info
- Test `applyChanges()` updates resourceIds via setter
- Test `ResourceEntry`-based EntryProvider still works unchanged
- Run all existing scheduler tests

## Completion Criteria

- CIP works with scheduler views (Timeline, ResourceTimeGrid, ResourceDayGrid)
- Resource associations correctly mapped from POJOs
- Resource drag-and-drop works with CIP (via setter or update handler)
- Existing ResourceEntry users are unaffected
