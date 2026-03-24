# Data Model

> Entity definitions and relationships for the FullCalendar Flow addon. Evolves as features are added.

---

## Core Entities

### Entry

The primary data object representing a calendar entry (FC "event"). All entries have a unique ID.

| Field | Type | Description |
|-------|------|-------------|
| `id` | `String` | Unique identifier (auto-generated UUID if not provided) |
| `title` | `String` | Display label on the calendar |
| `start` | `LocalDateTime` | Start date/time (stored as UTC). Setters accept `LocalDateTime`, `Instant`, `LocalDate` |
| `end` | `LocalDateTime` | End date/time (stored as UTC, optional — omitting creates point-in-time entry). Setters accept `LocalDateTime`, `Instant`, `LocalDate` |
| `allDay` | `boolean` | Force all-day rendering regardless of time part |
| `editable` | `boolean` | Per-entry drag/resize toggle |
| `startEditable` | `Boolean` | Per-entry override for `ENTRY_START_EDITABLE` (`null` = inherit global) |
| `durationEditable` | `Boolean` | Per-entry override for `ENTRY_DURATION_EDITABLE` (`null` = inherit global) |
| `color` | `String` | Combined background + border color (CSS color string) |
| `backgroundColor` | `String` | Background color override |
| `borderColor` | `String` | Border color override |
| `textColor` | `String` | Text color override |
| `description` | `String` | Description (available in JS callbacks) |
| `groupId` | `String` | Group ID for visual linking and constraint-by-group |
| `displayMode` | `DisplayMode` | Rendering mode: `AUTO`, `BLOCK`, `LIST_ITEM`, `BACKGROUND`, `INVERSE_BACKGROUND`, `NONE` |
| `overlap` | `Boolean` | Whether entry may overlap others (`null` = inherit global) |
| `constraint` | `String` | Group ID or `"businessHours"` restricting placement |
| `url` | `String` | Makes entry an `<a>` tag — navigates on click |
| `interactive` | `Boolean` | Keyboard focusability (`null` = inherit global `eventInteractive`) |
| `classNames` | `Set<String>` | CSS classes applied to the entry element |
| `recurringDaysOfWeek` | `Set<DayOfWeek>` | Simple weekly recurrence |
| `recurringStartTime` | `RecurringTime` | Start time for recurring entries |
| `recurringEndTime` | `RecurringTime` | End time for recurring entries |
| `recurringStartDate` | `LocalDate` | Start bound for recurrence range |
| `recurringEndDate` | `LocalDate` | End bound for recurrence range |
| `recurringDuration` | `String` | Duration for multi-day recurring all-day entries (ISO 8601 string, e.g., `"P2D"`) |
| `rrule` | `RRule` | RFC 5545 recurrence rule |
| `customProperties` | `Map<String, Object>` | Arbitrary data accessible in JS callbacks via `setCustomProperty(key, value)` |

**Serialization**: `Entry.toJson()` uses reflection-based `BeanProperties` with `@JsonName`, `@JsonConverter`, and `@JsonIgnore` annotations. The output is an elemental.json `JsonObject` (provided by Vaadin Flow).

### ResourceEntry (extends Entry)

Entry subclass for scheduler views that can be assigned to one or more resources.

| Field | Type | Description |
|-------|------|-------------|
| `resources` | `Set<Resource>` | Assigned resources (LinkedHashSet) |
| `resourceEditable` | `boolean` | Whether entry can be dragged between resources |

**Convenience accessor**: `getResource()` returns `Optional<Resource>` (first assigned resource). Use `getResourcesOrEmpty()` for all resources.

**Constraint**: Can only be added to a `FullCalendarScheduler` (not a plain `FullCalendar`).

### Resource

Represents a schedulable resource (room, person, equipment). Supports hierarchical trees.

| Field | Type | Description |
|-------|------|-------------|
| `id` | `String` | Unique identifier (auto-generated UUID if not provided) |
| `title` | `String` | Display name |
| `color` | `String` | Color shorthand for associated entries |
| `children` | `Set<Resource>` | Child resources (hierarchical) |
| `parent` | `Resource` | Parent resource (null for top-level) |
| `businessHoursArray` | `BusinessHours[]` | Per-resource business hours |
| `extendedProps` | `Map<String, Object>` | Custom properties |
| `entryBackgroundColor` | `String` | Per-resource entry background color (Java setter: `setEntryBackgroundColor()`) |
| `entryBorderColor` | `String` | Per-resource entry border color (Java setter: `setEntryBorderColor()`) |
| `entryTextColor` | `String` | Per-resource entry text color (Java setter: `setEntryTextColor()`) |
| `entryConstraint` | `String` | Per-resource entry constraint (Java setter: `setEntryConstraint()`) |
| `entryOverlap` | `Boolean` | Per-resource entry overlap setting (Java setter: `setEntryOverlap()`) |
| `entryClassNames` | `Set<String>` | Per-resource CSS classes for entries (Java setter: `setEntryClassNames()`) |
| `entryAllow` | `JsCallback` | Per-resource drop-allow callback (Java setter: `setEntryAllow(JsCallback)` — see UC-016) |

**Auto-push**: `setTitle()` and `setColor()` auto-push updates to client. Other entry style properties require manual `scheduler.updateResource(resource)`.

---

## Supporting Types

### RRule

Fluent builder for RFC 5545 recurrence rules. Backed by `@fullcalendar/rrule` plugin.

| Field | Type | Description |
|-------|------|-------------|
| `frequency` | `Frequency` | `DAILY`, `WEEKLY`, `MONTHLY`, `YEARLY` |
| `interval` | `Integer` | Repeat interval |
| `dtstart` | `String` | Recurrence start date (stored as ISO 8601 string; builder accepts `LocalDate`, `LocalDateTime`, or `String`) |
| `until` | `String` | Recurrence end date (stored as ISO 8601 string; builder accepts `LocalDate`, `LocalDateTime`, or `String`) |
| `count` | `Integer` | Max occurrences |
| `byweekday` | `List<String>` | Days of week (builder accepts `DayOfWeek` or RFC 5545 BYDAY strings like `"-1fr"`) |
| `bymonth` | `List<Integer>` | Months (1-12) |
| `bymonthday` | `List<Integer>` | Days of month |
| `byyearday` | `List<Integer>` | Days of year (1-366) |
| `byhour` | `List<Integer>` | Hours (0-23) |
| `byminute` | `List<Integer>` | Minutes (0-59) |
| `wkst` | `String` | Week start day (e.g., `"MO"`, `"SU"`) |
| `excludedDates` | `List<LocalDate>` | Dates excluded from recurrence |
| `excludedRules` | `List<RRule>` | Sub-rules excluded from recurrence |

### BusinessHours

Defines business hour ranges for visual highlighting and entry constraints.

| Field | Type | Description |
|-------|------|-------------|
| `dayOfWeeks` | `Set<DayOfWeek>` | Days of the business week |
| `start` | `LocalTime` | Start time |
| `end` | `LocalTime` | End time |

Factory methods: `BusinessHours.allDays()`, `BusinessHours.businessWeek()`, `BusinessHours.of(DayOfWeek...)`.

### Delta

Represents a time difference from drag-and-drop or resize events.

| Field | Type | Description |
|-------|------|-------------|
| `years`, `months`, `days` | `int` | Date delta |
| `hours`, `minutes`, `seconds` | `int` | Time delta |

### Timezone

Wraps a timezone identifier for the calendar's `timeZone` option.

Constants: `Timezone.UTC`, `Timezone.getSystem()`. Constructor: `new Timezone(ZoneId.of("America/New_York"))`.

---

## Data Provider Pattern

### EntryProvider (interface)

Provides entries to the calendar. Two implementations:

| Provider | Description | When to use |
|----------|-------------|-------------|
| `InMemoryEntryProvider` | All entries sent to client; client handles filtering | Small datasets (< few thousand entries) |
| `CallbackEntryProvider` | Entries fetched lazily per visible date range via callback | Large datasets or database-backed calendars |

### Client-Side Event Sources

Load entries directly in the browser (bypassing Java backend):

| Source | Class | Description |
|--------|-------|-------------|
| JSON Feed | `JsonFeedEventSource` | FC fetches from a REST endpoint with `start`/`end` params |
| Google Calendar | `GoogleCalendarEventSource` | Requires API key |
| iCalendar | `ICalendarEventSource` | Loads `.ics` feeds |

Client-side entries fire `ExternalEntryDroppedEvent` / `ExternalEntryResizedEvent` (not the server-managed counterparts).

---

## Entity Relationships

```
FullCalendar
  ├── has one EntryProvider
  │     └── manages many Entry instances
  └── has many ClientSideEventSource instances

FullCalendarScheduler (extends FullCalendar)
  ├── has many Resource instances
  │     ├── has many child Resource instances (tree)
  │     └── has many BusinessHours instances
  └── EntryProvider manages ResourceEntry instances
        └── assigned to many Resource instances (M:N)
```
