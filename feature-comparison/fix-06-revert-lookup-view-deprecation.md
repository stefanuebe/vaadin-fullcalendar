# Fix 6: Revert lookupViewByClientSideValue — keep lookupViewName

## Context

PR #223 review comment by stefanuebe:
> No. I want to keep this method. "lookupViewByClientSideValue" makes my head exploding just from reading. revert this deprecation and remove the new method

## Current state

```java
@Deprecated(forRemoval = true)
public <T extends CalendarView> Optional<T> lookupViewName(String clientSideValue) {
    return lookupViewByClientSideValue(clientSideValue);
}

public <T extends CalendarView> Optional<T> lookupViewByClientSideValue(String clientSideValue) {
    Optional<T> optional = (Optional<T>) CalendarViewImpl.ofClientSideValue(clientSideValue);
    if (optional.isPresent()) {
        return optional;
    }
    return Optional.ofNullable((T) customCalendarViews.get(clientSideValue));
}
```

**Note:** Both methods already exist on `master` (lines 1558-1573). The branch added the deprecation + the new long-named method.

## Changes

1. Remove `@Deprecated` annotation from `lookupViewName`
2. Move the implementation body back into `lookupViewName`
3. Delete `lookupViewByClientSideValue` entirely
4. Update any callers that reference the new method

## Files to modify

- `addon/src/main/java/org/vaadin/stefan/fullcalendar/FullCalendar.java`
- Search all Java files for `lookupViewByClientSideValue` and revert to `lookupViewName`

## Verification

1. `mvn test -pl addon`
2. Grep for `lookupViewByClientSideValue` — zero hits
3. Grep for `@Deprecated.*lookupViewName` — zero hits
4. `mvn clean install -DskipTests`
