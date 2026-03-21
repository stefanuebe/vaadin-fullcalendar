# Fix 10: Improve JsonFeedEventSource Javadocs

## Context

PR #223 review comments by stefanuebe:

On `withStartParam(String)` (line ~101):
> was genau wird denn hier erwartet? bitte javadocs konkretisieren. und ggf LocalDate oder LocalDateTime variante mit anbieten

On `withTimeZoneParam(String)` (line ~121):
> was genau wird hier erwartet? javadoc konkretisieren

## Current state

```java
/** Overrides the start parameter name for this source. */
public JsonFeedEventSource withStartParam(String startParam)

/** Overrides the end parameter name for this source. */
public JsonFeedEventSource withEndParam(String endParam)

/** Overrides the timeZone parameter name for this source. */
public JsonFeedEventSource withTimeZoneParam(String timeZoneParam)
```

The Javadoc doesn't explain that these set the **query parameter name** (not value), and what FC sends as the parameter value.

## Changes

Improve Javadocs to explain:
1. `withStartParam(String)` — sets the **name** of the query parameter. FC sends the start of the visible range as an ISO 8601 date string (e.g., `?start=2025-03-01T00:00:00`). Default name is `"start"`.
2. `withEndParam(String)` — same for end. Default is `"end"`.
3. `withTimeZoneParam(String)` — sets the **name** of the timezone query parameter. FC sends the calendar's timezone string (e.g., `?timeZone=UTC`). Default is `"timeZone"`.

These methods do NOT need `LocalDate`/`LocalDateTime` variants — they set parameter **names**, not values. FC populates the values automatically.

Also improve `withExtraParams(Map)` Javadoc to explain that keys are parameter names and values are sent as-is.

## Files to modify

- `addon/src/main/java/org/vaadin/stefan/fullcalendar/JsonFeedEventSource.java` — Javadoc only

## Verification

1. `mvn clean install -DskipTests` — compiles
