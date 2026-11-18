# LocalDateTime in UTC as the main time type

The times of entries and events are `LocalDateTime` values that mean UTC: `getStart()`, `setStart(LocalDateTime)` and the date time getters of the events. The `…WithOffset` methods convert to and from the time zone of the calendar, which is what a user sees and edits. `Instant` and `ZonedDateTime` variants exist beside them, but are not the main way. We keep `LocalDateTime` because it is the common Java type for date and time and because the Vaadin `DateTimePicker` takes and returns a `LocalDateTime` (the `DatePicker` a `LocalDate`). A main type of `Instant` or `ZonedDateTime` would force a conversion at every form. All-day values stay `LocalDate`, since a whole day has no time zone.

The cost is that the type does not say which zone a value is in. Docs and samples therefore name the zone: `getStart()` and the event getters are UTC, values from or for a form go through `…WithOffset`.

## Considered Options

- `Instant` as the main type: tried in 2019 (`eaa096508`) and reverted (`b5d44cc30`); the 4.0 release (2022, `a565463d2`) moved the add-on from `Instant` to `LocalDateTime` in UTC. Every form would need a conversion.
- `ZonedDateTime` or `OffsetDateTime` as the main type: carries its zone, but the Vaadin pickers do not take it, so the same conversion applies.
- An own `UtcDateTime` type: the java.time classes are final, so it could only wrap one. It would duplicate `Instant` and need a conversion at every boundary (pickers, persistence, JSON).
