# Fix 8: Extract ExternalEntryEvent base class

## Context

PR #223 review comment by stefanuebe:
> partially duplicated with ExternalEntryResizedEvent -> create base class ExternalEntryEvent, where the basic entry data is parsed into an Entry

## Current state

`ExternalEntryDroppedEvent` and `ExternalEntryResizedEvent` both:
1. Receive `ObjectNode entryData` via `@EventData`
2. Parse it into a transient `Entry` object (same parsing logic)
3. Have a `sourceId` field
4. Have a `delta` field (though semantically different — drop delta vs resize delta)

The entry-parsing logic is duplicated.

## Changes

1. Create `ExternalEntryEvent` abstract base class extending `ComponentEvent<FullCalendar>`:
   - Fields: `Entry entry`, `String sourceId`
   - Constructor: receives `ObjectNode entryData`, `String sourceId` → parses entry data into `Entry`
   - The entry parsing logic moves here (parse JSON into a transient Entry)

2. `ExternalEntryDroppedEvent extends ExternalEntryEvent`:
   - Adds: `Delta delta`, `LocalDateTime oldStart`, `LocalDateTime oldEnd`
   - Keeps its `@DomEvent("externalEntryDrop")` annotation

3. `ExternalEntryResizedEvent extends ExternalEntryEvent`:
   - Adds: `Delta delta`, `LocalDateTime oldEnd`
   - Keeps its `@DomEvent("externalEntryResize")` annotation

## Files to create

- `addon/src/main/java/org/vaadin/stefan/fullcalendar/ExternalEntryEvent.java`

## Files to modify

- `addon/src/main/java/org/vaadin/stefan/fullcalendar/ExternalEntryDroppedEvent.java`
- `addon/src/main/java/org/vaadin/stefan/fullcalendar/ExternalEntryResizedEvent.java`
- Tests if they reference internal structure

## Verification

1. `mvn test -pl addon`
2. `mvn clean install -DskipTests`
