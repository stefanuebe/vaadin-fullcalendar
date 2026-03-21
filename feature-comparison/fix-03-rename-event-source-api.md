# Fix 3: Rename event source API + return Registration

## Context

PR #223 review comments by stefanuebe (7 comments on the event source methods):
> addClientSideEventSource to make the api call clear

> return a vaadin Registration object, which allows the removal of the registered event source

> adapt new name pattern for this api (on removeEventSource, setEventSources, getEventSources, getEventSourceById)

> return a registration object (similar to addEventSource) [on setEventSources]

The current method names (`addEventSource`, `removeEventSource`, etc.) are ambiguous — they could be confused with the server-side `EntryProvider`. Prefix with `ClientSide` to clarify.

## Methods to rename

In `FullCalendar.java`:

| Current name | New name | Additional change |
|---|---|---|
| `addEventSource(ClientSideEventSource)` | `addClientSideEventSource(...)` | Return `Registration` that calls `removeClientSideEventSource(id)` |
| `removeEventSource(String id)` | `removeClientSideEventSource(String id)` | — |
| `setEventSources(Collection)` | `setClientSideEventSources(Collection)` | Return `Registration` that clears all sources |
| `getEventSources()` | `getClientSideEventSources()` | — |
| `getEventSourceById(String id)` | `getClientSideEventSourceById(String id)` | — |

Also rename the internal field:
- `eventSourceRegistry` → `clientSideEventSourceRegistry`

## Registration pattern

```java
public Registration addClientSideEventSource(ClientSideEventSource<?> source) {
    Objects.requireNonNull(source, "source must not be null");
    clientSideEventSourceRegistry.put(source.getId(), source);
    getElement().callJsFunction("addEventSource", source.toJson());
    return () -> removeClientSideEventSource(source.getId());
}
```

## Files to modify

- `addon/src/main/java/org/vaadin/stefan/fullcalendar/FullCalendar.java` — rename methods + field + add Registration returns
- `addon/src/test/java/org/vaadin/stefan/fullcalendar/EventSourcesTest.java` — update all calls
- `e2e-test-app/.../EventSourcesTestView.java` — update calls
- `docs/Samples.md`, `docs/Features.md` — update references
- TS file: The JS function names (`addEventSource`, `removeEventSource`, `setEventSources`) on the client side stay unchanged — those are FC's native API names. Only the Java method names change.

## Verification

1. `mvn test -pl addon` — all tests pass
2. Grep for old method names in Java code — zero hits (except TS, which keeps FC's names)
3. `mvn clean install -DskipTests` — compiles
