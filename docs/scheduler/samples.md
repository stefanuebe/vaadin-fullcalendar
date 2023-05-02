# Activating the scheduler
**using the builder**
```java
FullCalendar calendar = FullCalendarBuilder.create().withScheduler().build();
```

**setSchedulerLicenseKey*
```java
// scheduler options
((Scheduler) calendar).setSchedulerLicenseKey(...);
```

# Adding a resource to a calendar and link it with entries
```java
Resource resource = new Resource(null, s, color);
calendar.addResource(resource);

// When we want to link an entry with a resource, we need to use ResourceEntry
// (a subclass of Entry)
ResourceEntry entry = new ResourceEntry(null, title, start.atStartOfDay(), start.plusDays(days).atStartOfDay(), true, true, color, "Some description...");
entry.setResource(resource);
calendar.addEntry(entry);
```

# Handling change of an entry's assigned resource by drag and drop
```java
calendar.addEntryDroppedListener(event -> {
    event.applyChangesOnEntry();

    Entry entry = event.getEntry();

    if(entry instanceof ResourceEntry) {
        Set<Resource> resources = ((ResourceEntry) entry).getResources();
        if(!resources.isEmpty()) {
            // do something with the resource info
        }
    }
});
```

# Switching to a timeline view
```java
calendar.changeView(SchedulerView.TIMELINE_DAY);
```

# Activate vertical resource view
```java
calendar.setGroupEntriesBy(GroupEntriesBy.RESOURCE_DATE);
```

# Creating a resource bases background event
ResourceEntry entry = new ResourceEntry();
// ... setup entry details, including addResource()

entry.setDisplayMode(DisplayMode.BACKGROUND);
calendar.addEntry(entry);

# Creating hierarchical resources
```java
// Create a parent resource. When adding the sub resources first before adding the parent to the calendar,
// the sub resources are registered automatically on client side and server side.

Resource parent = new Resource();
parent.addChildren(new Resource(), new Resource(), new Resource());

calendar.addResource(parent); // will add the resource and also it's children to server and client

// add new resources to already registered parents
Resource child = new Resource()
parent.addChild(child);
calendar.addResource(child); // this will update the client side

// or remove them from already registered ones
calendar.removeResource(child); 
parent.removeChild(child); 
```

# Making a resource entry draggable between resources
```java
// activate for the client to have an entry being draggable between resources
resourceEntry.setResourceEditableOnClientSide(true);

// update the entry on the client side, if it is already added to the calendar
calendar.refreshAll();
```