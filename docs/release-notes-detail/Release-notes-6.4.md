# FullCalendar Addon 6.4.0 Release Notes

Version 6.4.0 represents a significant platform modernization and feature expansion of the FullCalendar addon. This release bridges the gap for Vaadin 24 users who cannot yet migrate to Vaadin 25, while bringing a comprehensive set of v7 features to the v6 codebase.

## Platform Upgrade

### Vaadin 14 → Vaadin 24.10

The addon has been upgraded from Vaadin 14 (LTS) to Vaadin 24.10.x. This is a major platform shift that modernizes the underlying component framework.

- **Java version**: Requires Java 17+ (upgraded from Java 8)
- **Polymer removal**: Vaadin 14's Polymer 3 has been replaced with plain HTML Element-based web components (already completed in v6.0)
- **Vaadin Flow**: Updated to Vaadin Flow 24.10.x with all associated ecosystem updates

> **Breaking Change**: This version requires **Vaadin 24.10**. Vaadin 14 LTS is no longer supported.

## Entry Model Enhancements

### New Entry Fields

The `Entry` class now includes several new properties backported from v7:

| Field | Type | Purpose |
|-------|------|---------|
| `url` | String | Hyperlink for the entry; integrated into entry rendering |
| `interactive` | Boolean | Controls whether the entry is interactive (draggable, resizable) |
| `overlap` | Boolean | **Breaking change** — now `Boolean` (nullable) instead of `boolean` primitive; use getters carefully |
| `recurringDuration` | String | For recurring entries, controls the duration of recurrence windows independently of event duration |

### Recurrence Rule Support (RFC 5545 — RRULE)

Version 6.4 introduces full RFC 5545 recurrence rule support via the new `RRule` class:

```java
Entry event = new Entry("Team Meeting");
event.setStart(LocalDateTime.of(2025, 3, 24, 10, 0));
event.setEnd(LocalDateTime.of(2025, 3, 24, 11, 0));

// Weekly meetings, Monday and Friday, until end of April
RRule rule = RRule.weekly()
    .byWeekday(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)
    .until(LocalDate.of(2025, 4, 30));

event.setRRule(rule);
calendar.getEntryProvider().asInMemory().addEntry(event);
```

**Key Features**:
- Structured form (recommended): Set frequency and properties like `byWeekday`, `until`, `count`, etc.
- Raw RRULE string: For importing from external sources (e.g., iCalendar exports): `RRule.ofRaw("FREQ=WEEKLY;BYDAY=MO,FR")`
- Exclusions: Use `excludeDates()` or `excludeRules()` to define exception dates or patterns
- Mutually exclusive with legacy recurrence fields (`recurringDaysOfWeek`, `recurringStartDate`, etc.)

The `@fullcalendar/rrule` npm package is automatically bundled and loaded.

### Nullable Overlap Property

The `overlap` field's type has changed from `boolean` to `Boolean`:

```java
entry.setOverlap(true);      // supported
entry.setOverlap(null);      // now allowed
Boolean val = entry.getOverlap();  // returns null if not set
```

Recompilation is required. Be careful when reading the value — `null` is now a distinct state from `false`.

## JsCallback System (Type-Safe Client-Side Callbacks)

Version 6.4 introduces the `JsCallback` wrapper for client-side event handlers, replacing raw string-based callback injection:

```java
// New (6.4+): type-safe wrapper
calendar.setOption(Option.ENTRY_DID_MOUNT,
    JsCallback.of("function(info) { info.el.title = info.event.title; }"));

// Clear a callback
calendar.setOption(Option.ENTRY_DID_MOUNT, JsCallback.clearCallback());
```

This replaces the previous `setEntryDidMountCallback(String)` pattern. The `JsCallback.of()` factory method wraps a JavaScript function string for safe transport to the client, where it is evaluated via `new Function()`.

## FullCalendar JavaScript Library Updates

The bundled FullCalendar JavaScript library has been upgraded from **6.1.9 to 6.1.20**, bringing:
- Bug fixes and performance improvements
- Enhanced compatibility with modern browser APIs
- Better internationalization support

## Event Classes and DOM Event Integration

Eleven new event classes have been added to support additional user interactions:

| Event Class | Purpose |
|-------------|---------|
| `EntryDragStartEvent` | Fired when user begins dragging an entry |
| `EntryDragStopEvent` | Fired when user completes or cancels a drag operation |
| `EntryResizeStartEvent` | Fired when user begins resizing an entry |
| `EntryResizeStopEvent` | Fired when user completes or cancels a resize operation |
| `DropEvent` | Fired when content is dropped from outside the calendar |
| `EntryReceiveEvent` | Fired when an entry is received from an external source |
| `ExternalEntryDroppedEvent` | Fired when an external entry is dropped on the calendar |
| `ExternalEntryResizedEvent` | Fired when an external entry is resized |
| `ExternalEntryEvent` | Base class for external entry events |
| `TimeslotsUnselectEvent` | Fired when a selection is cleared |
| `EntryTimeChangedEvent` | Fired when an entry's time is changed |

These events integrate native browser DOM events with the FullCalendar backend, allowing precise control over drag, resize, and drop behaviors.

## New Option Enum Constants

A large number of new `Option` enum constants have been added for advanced customization. See the `Option` enum Javadoc for the complete list.

- **Render hooks**: `ENTRY_CONTENT`, `ENTRY_DID_MOUNT`, `DAY_CELL_CONTENT`, `SLOT_LABEL_CONTENT`, etc.
- **Interaction handlers**: `SELECT_CONSTRAINT`, `SELECT_ALLOW`, `SELECT_OVERLAP`, etc.
- **Display configuration**: `SLOT_LABEL_FORMAT`, `ENTRY_TIME_FORMAT`, `BUSINESS_HOURS`, etc.

These options map directly to FullCalendar's JavaScript API and allow fine-grained control over rendering and behavior without requiring custom JavaScript.

## Client-Side Event Sources

Three new event source types enable fetching calendar entries from external systems:

| Source Type | Purpose |
|-------------|---------|
| **Google Calendar** | Load events directly from Google Calendar feeds |
| **iCalendar (ICS)** | Import .ics files and feed URLs |
| **JSON Feed** | Load custom event feeds from URLs or APIs |

These are automatically integrated with the `@fullcalendar/google-calendar`, `@fullcalendar/icalendar`, and `@fullcalendar/core` packages.

```java
// Set a global API key (applies to all Google Calendar sources)
calendar.setOption(Option.EXTERNAL_EVENT_SOURCE_GOOGLE_CALENDAR_API_KEY, "AIzaSy...");

// Add a Google Calendar event source
calendar.addClientSideEventSource(new GoogleCalendarEventSource("holidays@group.calendar.google.com")
    .withId("holidays")
    .withColor("green"));
```

## Scheduler Extensions

### ComponentResourceAreaColumn

The scheduler component now supports Vaadin components directly in resource area columns:

```java
FullCalendarScheduler scheduler = new FullCalendarScheduler();
ComponentResourceAreaColumn column = new ComponentResourceAreaColumn(
    resource -> new Button("Edit " + resource.getTitle(),
        e -> editResource(resource))
);
scheduler.setResourceAreaColumns(List.of(column));
```

### ResourceAreaColumn Configuration

A new `ResourceAreaColumn` configuration class provides declarative control over scheduler resource area styling and layout.

## Performance Improvements

### Bounded Entry Cache

The in-memory entry provider now applies an internal size bound to prevent unbounded memory growth. This is handled automatically and requires no configuration change. No new API was added.

### Thread-Safe Refresh Operations

Entry provider refresh operations are now fully thread-safe, allowing background processes to update the calendar without synchronization overhead.

### BeanProperties Converter Caching

Conversion overhead for Entry serialization has been reduced via reflection-based caching of property accessors, improving responsiveness during calendar interaction.

## Dependencies

### New NPM Packages (Automatic)

The following npm packages are now automatically bundled and loaded:

- `@fullcalendar/rrule` — RFC 5545 recurrence rules
- `@fullcalendar/google-calendar` — Google Calendar integration
- `@fullcalendar/icalendar` — iCalendar (ICS) support
- `ical.js` — ICS file parsing

These are included in the build automatically; **no manual npm installation required**.

## Deprecated Functionality

Approximately 30 convenience methods have been deprecated in favor of the unified `setOption()` API. Examples:

```java
// Deprecated (still works)
calendar.setFirstDay(DayOfWeek.MONDAY);

// Preferred (v6.4+)
calendar.setOption(Option.FIRST_DAY, DayOfWeek.MONDAY);
```

Deprecated methods remain functional but log warnings if invoked. They will be removed in a future major version.

See the Migration Guide for a complete list of deprecated methods and their replacements.

## What's NOT Included

The following v7 features are **not** backported to v6.4:

- **Aura Theme variant** — The Aura theme (introduced in Vaadin 25) is not available. The Lumo-based `full-calendar-theme-vaadin.css` is included and works with Vaadin 24.
- **Jackson JSON** — The addon continues to use `elemental.json` (Vaadin's built-in JSON library) for compatibility
- **Java 21+ Language Features** — Code remains Java 17-compatible; no records, pattern matching, or sealed classes

## Upgrade Path

| Current Version | Recommended Action |
|-----------------|-------------------|
| v6.3.x | Review Migration Guide, update Maven dependency, recompile |
| v6.2.x or earlier | Review full v6.0 migration guide first, then v6.3→6.4 |
| v5.x or earlier | Not recommended; consider v7.x instead |

## Support

- Documentation: See Migration Guide for step-by-step instructions
- Issues: [GitHub Issues](https://github.com/stefanuebe/vaadin-fullcalendar/issues)
- Demo: Clone the repository and run the demo app with Vaadin 24.10

---

*Release date: March 2026*
*FullCalendar JS Library: 6.1.20*
*Vaadin: 24.10.x*
*Java: 17+*
