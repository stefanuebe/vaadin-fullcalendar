# Fix 4: Clarify refetchEventSource scope + rename

## Context

PR #223 review comments by stefanuebe:
> Does not describe, if entry provider sources can also be targeted with this. If yes, explain how. If no, then also state explicitly

> if only external sources can be refreshed with this, rename the method and add external to it

The methods `refetchEvents()` and `refetchEventSource(String sourceId)` need clearer documentation and possibly renaming.

## Analysis

- `refetchEvents()` — calls FC's `refetchEvents()` which re-fetches ALL sources including the server-side EntryProvider. This is correct and the name is fine (it's a global refresh).
- `refetchEventSource(String sourceId)` — calls `calendar.getEventSourceById(id).refetch()`. This only works for client-side event sources (JSON feed, Google Calendar, iCal). The server-side EntryProvider is not an "event source" in FC's sense — it's the events function.

## Changes

1. **Rename** `refetchEventSource(String)` → `refetchClientSideEventSource(String)` — consistent with Fix 3 naming
2. **Improve Javadoc** for both methods:
   - `refetchEvents()`: State explicitly that this refreshes both server-side EntryProvider entries AND client-side event sources
   - `refetchClientSideEventSource(String)`: State explicitly that this only works for client-side sources added via `addClientSideEventSource()`, NOT for the server-side EntryProvider

## Files to modify

- `addon/src/main/java/org/vaadin/stefan/fullcalendar/FullCalendar.java`
- Tests and docs referencing `refetchEventSource`

## Verification

1. `mvn test -pl addon`
2. Grep for old method name
3. `mvn clean install -DskipTests`
