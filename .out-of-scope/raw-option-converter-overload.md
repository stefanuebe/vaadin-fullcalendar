# Raw option with a converter

`setRawOption` has no overload that takes a `JsonItemPropertyConverter`. The
previous version (7.3) had `setOption(String, Object, JsonItemPropertyConverter...)`,
where a converter built the JSON the client got.

## Why this is out of scope

`setRawOption(String, Object)` already takes a `JsonNode` and sends it as it is. A
converter is one call away:

```java
// 7.3: calendar.setOption("myOption", value, converter);
calendar.options().setRawOption("myOption", converter.toClientModel(value, null));
```

With several converters, the caller picks the first one whose `supports(value)`
returns true, as 7.3 did. The migration guide 7.3 to 8.0 ("FullCalendar and
options", section "Raw options") describes this.

An overload saves that one call but adds API with questions of its own: one
converter or several, and what `currentInstance` it passes. An options object has
no meaningful instance to pass, so it would always be `null`, as in the call above.
Since 8.0, the option API is the typed fields plus `setRawOption` for the rest, and
converters belong to the typed fields and to entry properties.

## Prior requests

- #477: "setRawOption with a JsonItemPropertyConverter"
