# Fix 2: Rename "Event" to "Entry" in new API methods

## Context

PR #223 review comments by stefanuebe:
> The FC names its calendar items "events", but we name them "entry" to prevent misinterpretation with events (like a click event). Therefore these new methods should be named `setEntry...`

> auch hier nochmal die Erinnerung: in Java heißen die FC Events (also calendar items) nicht Event sondern Entry, also API entsprechend benennen

This addon uses "Entry" instead of "Event" for calendar items (to avoid confusion with Java/Vaadin component events). All new methods that reference calendar items as "Event" must be renamed to "Entry".

## Methods to rename

Search `FullCalendar.java` for methods added in this branch that contain "Event" where it refers to a calendar item (NOT component events, NOT event sources). The `Option` enum uses a built-in `ENTRY→EVENT` mapping already, so enum constants stay as-is.

Known candidates (verify against current code):
- `setEventOverlap(boolean)` → `setEntryOverlap(boolean)` (if not removed by Fix 1)
- `setEventOverlapCallback(String)` → `setEntryOverlapCallback(String)`
- `setEventAllowCallback(String)` → `setEntryAllowCallback(String)`
- `setEventDataTransformCallback(String)` → `setEntryDataTransformCallback(String)`
- `setEventSourceSuccessCallback(String)` → stays (this is about event *sources*, not entries)
- `setDefaultTimedEventDuration(String)` → `setDefaultTimedEntryDuration(String)` (if not removed by Fix 1)
- `setDefaultAllDayEventDuration(String)` → `setDefaultAllDayEntryDuration(String)` (if not removed by Fix 1)
- `setDisplayEventEnd(boolean)` → `setDisplayEntryEnd(boolean)` (if not removed by Fix 1)
- `setDisplayEventTime(boolean)` → `setDisplayEntryTime(boolean)` (if not removed by Fix 1)
- `setProgressiveEventRendering(boolean)` → `setProgressiveEntryRendering(boolean)` (if not removed by Fix 1)
- `setEventInteractive(boolean)` → `setEntryInteractive(boolean)` (if not removed by Fix 1)

**Note:** Run Fix 1 (remove typed setters) first. Only methods that survive Fix 1 need renaming here.

Also check the `Option` enum — its `toCamelCase` conversion already maps `ENTRY_*` → `event*` on the client side, so no Option changes needed.

## Files to modify

- `addon/src/main/java/org/vaadin/stefan/fullcalendar/FullCalendar.java`
- Any tests/views/docs referencing the renamed methods
- `addon/src/main/resources/META-INF/resources/frontend/vaadin-full-calendar/full-calendar.ts` — rename any corresponding TS methods

## Verification

1. `mvn test -pl addon` — all tests pass
2. Grep for old method names — zero hits
3. `mvn clean install -DskipTests` — compiles
