This page gives you an overview of the major changes that came with the release of
[FullCalendar for Flow, version 7.1](https://vaadin.com/directory/component/full-calendar-flow).

Version 7.1 is driven by two goals:

1. **Close the feature gap** — bring the Java API up to parity with FullCalendar v6 so that options which
   previously required raw `setOption` calls now have proper typed support.
2. **Keep the API lean** — rather than growing a sprawling set of individual setter methods, new functionality
   is surfaced through the generic `setOption` API, backed by typed enums, automatic value converters, and
   `JsCallback` for function values. Dedicated one-off setters that duplicate what `setOption` can already
   express are deprecated.
3. New event types to allow more fine-grain control over what happens on the client side

No APIs have been removed in this release. Deprecated methods continue to work and will be removed in a future
major version. See the [migration guide](Migration-guides#migration-notes-70--71) for the one behaviour change
involving `Entry.overlap` and the full list of deprecated methods.

## Entry model enhancements

Several new properties have been added to `Entry`:

- `setUrl(String)` — entries with a URL are rendered as `<a>` tags; FC navigates to the URL on click
- `setInteractive(Boolean)` — per-entry keyboard-focusability override (`null` inherits the global `eventInteractive` setting)
- `setRecurringDuration(String)` — ISO 8601 duration for multi-day all-day recurring events (e.g. `"P2D"`)
- `setRRule(RRule)` — RFC 5545 recurrence rules via the `RRule` fluent builder, including `RRule.excludeDates(LocalDate...)` to exclude specific dates
- `setOverlap(Boolean)` — changed from primitive `boolean` to `Boolean`; `null` means "inherit global overlap setting"

## Unified callback API: `JsCallback` and `setOption`

JavaScript callbacks and static option values now share a single API path: `setOption(Option, value)`.
`JsCallback` is a lightweight wrapper that marks a string as a JavaScript function — the client
evaluates it via `new Function()` before passing it to FullCalendar.

The `Option` enum now includes all callback-related constants (render hooks, interaction guards,
data transforms, etc.). Scheduler-specific callback constants are in `SchedulerOption`:

```java
// Static value — unchanged
calendar.setOption(Option.ENTRY_OVERLAP, false);

// Function — wrap in JsCallback
calendar.setOption(Option.ENTRY_OVERLAP,
    JsCallback.of("function(stillEvent, movingEvent) { return stillEvent.display === 'background'; }"));

// Render hook
calendar.setOption(Option.ENTRY_DID_MOUNT,
    JsCallback.of("function(info) { info.el.title = info.event.title; }"));

// Scheduler resource hook
scheduler.setOption(SchedulerOption.RESOURCE_LABEL_CLASS_NAMES,
    JsCallback.of("function(arg) { return arg.resource.extendedProps.isSpecial ? ['special'] : []; }"));
```

`JsCallback.of(null)` returns `null`, enabling clean "clear callback" patterns:

```java
calendar.setOption(Option.ENTRY_CONTENT, JsCallback.of(userProvidedFunction));  // safe when null
```

The individual callback setter methods from 7.0 (`setEntryClassNamesCallback`, `setEntryDidMountCallback`,
etc.) are deprecated. See the [migration guide](Migration-guides#deprecated-individual-callback-methods-use-setoption--jscallback-instead) for
the complete replacement table.

## Interaction events

New server-side events cover the full drag/resize lifecycle:
`EntryDragStartEvent`, `EntryDragStopEvent`, `EntryResizeStartEvent`, `EntryResizeStopEvent`.

Please note, that these should be mainly used for visual feedback. `EntryDroppedEvent` and `EntryResizedEvent` are still
the main events for reacting on calendar modification by the user.

External entry support has been added via `ExternalEntryDroppedEvent` and `ExternalEntryResizedEvent`, fired when
entries from a client-side event source are dropped into or resized within the calendar.

Additional interaction options are available via the `Option` enum:
- `Option.DROP_ACCEPT` — CSS selector or JS function to filter accepted draggable elements
- `Option.UNSELECT_CANCEL` — CSS selector for elements that should not cancel an active selection

## Event sources

Typed Java wrappers have been added for three common FC event source types:

- `JsonFeedEventSource` — FC's JSON feed event source
- `GoogleCalendarEventSource` — Google Calendar integration
- `ICalendarEventSource` — iCalendar (.ics) feeds

The `JsonFeedEventSource` builder supports per-source parameter name overrides via `withStartParam(String)`,
`withEndParam(String)`, and `withTimeZoneParam(String)`. For global overrides, use the corresponding
`Option.EXTERNAL_EVENT_SOURCE_START_PARAM`, `EXTERNAL_EVENT_SOURCE_END_PARAM`, and
`EXTERNAL_EVENT_SOURCE_TIME_ZONE_PARAM` option keys via `setOption`.

`EventSourceFailureEvent` is fired server-side when an event source fails to load.

## Scheduler resource features

The resource area can now be configured with typed column definitions via
`setResourceAreaColumns(ResourceAreaColumn...)`. Resources can be grouped by a shared field using
`setOption(SchedulerOption.RESOURCE_GROUP_FIELD, "fieldName")`,
with customizable render hooks for group headers and lanes via callback options.
Use `setOption(SchedulerOption.REFETCH_RESOURCES_ON_NAVIGATE, true)` to re-fetch resources on each navigation.
Resource lifecycle events (`resourceAdd`, `resourceChange`, `resourceRemove`, `resourcesSet`) are
available as callback options for custom logic on resource data changes.

## Accessibility

A set of new options improves keyboard accessibility and screen reader support:

- `Option.ENTRY_INTERACTIVE` — make all entries keyboard-focusable (WCAG 2.1 AA) via `setOption`
- `Option.NATIVE_TOOLBAR_BUTTON_HINTS` — accessible labels for toolbar buttons
- `Option.NATIVE_TOOLBAR_VIEW_HINT` / `Option.NAV_LINK_HINT` / `Option.MORE_LINK_HINT` — labels for view switchers, nav links, and "+N more" links
- `Option.CLOSE_HINT` / `Option.TIME_HINT` / `Option.ENTRY_HINT` — labels for popover close buttons, time displays, and entries

## Other new options and methods

A number of smaller additions round out the release:

- `Option.PROGRESSIVE_EVENT_RENDERING` — render events in batches as they arrive (via `setOption`)
- `Option.DATE_INCREMENT` / `Option.DATE_ALIGNMENT` — navigation increment and alignment for custom views (via `setOption`)
- `Entry.setConstraint(BusinessHours)` — constrain drag/resize to specific time slots
- `Option.CONTENT_SECURITY_POLICY` — CSP nonce for dynamically generated `<style>` tags (via `setOption`)
- `setViewSpecificOption(viewType, option, value)` — per-view option overrides
- `Option.FIXED_MIRROR_PARENT` — drag-mirror parent element; accepts `JsCallback.of("function() { return document.body; }")` (via `setOption`)
- `Option.DRAG_SCROLL_ELS` — CSS selectors for auto-scroll containers during drag (via `setOption`)
- `Option.MORE_LINK_CLICK` — custom JS function for "+N more" link click behavior; accepts a static string value or `JsCallback.of(...)` (via `setOption`)
- `getCurrentIntervalStart()` / `getCurrentIntervalEnd()` — returns `Optional<LocalDate>` for the current view interval; empty before the first `DatesRenderedEvent` is received

`FullCalendarBuilder` has been refactored to a mutable fluent builder — all `withXxx` methods now return `this`,
so chained calls no longer need to be re-assigned.

New enums `Direction` and `WeekNumberCalculation` replace raw string options.

## Option converters

A new converter system allows `setOption` to accept type-rich Java values. Each converter is registered
on the `Option` / `SchedulerOption` constant via the `@JsonConverter` annotation and converts automatically
when a matching type is passed.

**Duration options** — `DurationConverter` converts `Duration` or `LocalTime` to FullCalendar's `"HH:mm:ss"` format:
- `Option.SNAP_DURATION`, `Option.SLOT_DURATION`, `Option.SLOT_LABEL_INTERVAL`
- `Option.SLOT_MIN_TIME`, `Option.SLOT_MAX_TIME`, `Option.SCROLL_TIME`, `Option.NEXT_DAY_THRESHOLD`

**Business hours** — `BusinessHoursConverter` converts `BusinessHours` or `BusinessHours[]` to FC's business-hours JSON:
- `Option.BUSINESS_HOURS`, `Option.ENTRY_CONSTRAINT`

**Day of week** — `DayOfWeekConverter` converts `DayOfWeek` to FC's 0-based weekday number (0=Sunday):
- `Option.FIRST_DAY`

**Day of week arrays** — `DayOfWeekArrayConverter` converts `DayOfWeek[]` or `Collection<DayOfWeek>`:
- `Option.HIDDEN_DAYS`

**Locale** — `LocaleConverter` converts `java.util.Locale` to FC's locale string:
- `Option.LOCALE`

**String arrays** — `StringArrayConverter` joins `String[]` or `Collection<String>` with commas:
- `Option.DRAG_SCROLL_ELS`

**Toolbar** — `ToolbarConverter` converts `Header`, `Footer`, or `Map<String, String>` to FC's toolbar config:
- `Option.HEADER_TOOLBAR`, `Option.FOOTER_TOOLBAR`

Example:
```java
calendar.setOption(Option.SLOT_DURATION, Duration.ofMinutes(30));
calendar.setOption(Option.FIRST_DAY, DayOfWeek.MONDAY);
calendar.setOption(Option.LOCALE, Locale.GERMAN);
calendar.setOption(Option.BUSINESS_HOURS, BusinessHours.businessWeek().start(LocalTime.of(9, 0)).end(LocalTime.of(17, 0)));
calendar.setOption(Option.HIDDEN_DAYS, new DayOfWeek[]{DayOfWeek.SATURDAY, DayOfWeek.SUNDAY});
```

The legacy setter methods `setSnapDuration(String)`, `setSlotMinTime(...)`, `setSlotMaxTime(...)`, etc.
from 7.0 are now deprecated. Use `setOption` with the corresponding `Option` enum value instead.

## Deprecated methods

A number of individual setter methods from 7.0 are now deprecated in favour of the generic `setOption` API.
Examples:

```java
// Before (deprecated)
calendar.setSlotMinTime(LocalTime.of(9, 0));
calendar.setEntryDidMountCallback("function(info) { ... }");
scheduler.setResourceLabelClassNamesCallback("function(arg) { ... }");

// After
calendar.setOption(Option.SLOT_MIN_TIME, LocalTime.of(9, 0));
calendar.setOption(Option.ENTRY_DID_MOUNT, JsCallback.of("function(info) { ... }"));
scheduler.setOption(SchedulerOption.RESOURCE_LABEL_CLASS_NAMES, JsCallback.of("function(arg) { ... }"));
```

See the [migration guide](Migration-guides#migration-notes-70--71) for the complete replacement table.

## Bug fixes

- `EntryQuery.applyFilter()`: entries without an explicit end date (e.g. all-day single-day entries created with
  only `setStart(LocalDate)`) were incorrectly excluded from `InMemoryEntryProvider` fetch results. They now
  appear correctly when the start date falls within the visible date range.
