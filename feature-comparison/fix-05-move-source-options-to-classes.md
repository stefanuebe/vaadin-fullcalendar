# Fix 5: Move event source options into source classes

## Context

PR #223 review comment by stefanuebe:
> this needs rework. There are dedicated interfaces and classes for external sources. I do not want to have this api to be part of the "root" FC. It belongs into the respective sources!

The following methods on `FullCalendar` are global FC options that only affect client-side event sources. They should be removed from the calendar root:

## Methods to remove from FullCalendar

1. `setStartParam(String)` — default query param name "start" for JSON feeds → already available per-source via `JsonFeedEventSource.withStartParam(String)`
2. `setEndParam(String)` — default query param name "end" → already available per-source via `JsonFeedEventSource.withEndParam(String)`
3. `setTimeZoneParam(String)` — default query param name "timeZone" → already available per-source via `JsonFeedEventSource.withTimeZoneParam(String)`
4. `setGoogleCalendarApiKey(String)` — API key → already available per-source via `GoogleCalendarEventSource.withApiKey(String)`

These are FC global defaults, but the per-source API already exists. Users who need to set defaults can use `setOption(Option.START_PARAM, ...)` directly.

## Also remove these callback setters from FullCalendar

5. `setLoadingCallback(String)` — global loading indicator callback. Remove dedicated method, keep Option if it exists.
6. `setEventDataTransformCallback(String)` — global data transform. Already available per-source via `ClientSideEventSource.withEventDataTransform(String)`. Remove.
7. `setEventSourceSuccessCallback(String)` — global success handler. Already available per-source via `ClientSideEventSource.withSuccess(String)`. Remove.

## Option enum constants to KEEP

Keep `START_PARAM`, `END_PARAM`, `TIME_ZONE_PARAM`, `GOOGLE_CALENDAR_API_KEY` in the Option enum — users can still set global defaults via `setOption()`.

## Files to modify

- `addon/src/main/java/org/vaadin/stefan/fullcalendar/FullCalendar.java` — remove 7 methods
- `addon/src/test/java/org/vaadin/stefan/fullcalendar/EventSourcesTest.java` — update tests
- `e2e-test-app/` views — update if they call these methods
- `docs/Samples.md`, `docs/Features.md` — update examples to use per-source API or `setOption()`

## Verification

1. `mvn test -pl addon`
2. Grep for removed method names
3. `mvn clean install -DskipTests`
