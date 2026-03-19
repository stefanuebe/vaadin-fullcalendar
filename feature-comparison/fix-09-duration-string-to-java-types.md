# Fix 9: Use proper Java types instead of String for durations

## Context

PR #223 review comment by stefanuebe on `getSnapDuration() → Optional<String>`:
> Warum ist das ein String? Wir leben in Java, wir haben bessere Datentypen als das. Bitte API diesbzüglich (generell) nochmal gegenprüfen

## Scope

Review all getter methods that return `Optional<String>` or `String` for duration/time values and check whether they should use `java.time.Duration`, `java.time.LocalTime`, or similar.

**Known candidates** (verify against current code):
- `getSnapDuration()` → should return `Optional<Duration>` or keep as string if FC uses non-ISO format
- Any setter/getter pair for slot durations, scroll times, etc.

**Important:** FC duration strings use format `"HH:MM:SS"` (e.g., `"00:30:00"`) which maps to `java.time.Duration` or `java.time.LocalTime`. Some also accept ISO 8601 (`"P1W"`). Check what FC actually expects for each option.

If the method is a typed setter that gets removed in Fix 1, skip it here. Only fix methods that survive (getters, callbacks, etc.).

## Approach

For each affected method:
1. Check what FC format the option expects
2. If it's `HH:MM:SS` → accept `Duration` and convert to string internally
3. If it's ISO 8601 (`P1W`) → accept `Duration` directly
4. If the format is ambiguous or FC-specific → keep as String but document the expected format clearly

## Files to modify

- `addon/src/main/java/org/vaadin/stefan/fullcalendar/FullCalendar.java`
- Related tests

## Verification

1. `mvn test -pl addon`
2. `mvn clean install -DskipTests`
