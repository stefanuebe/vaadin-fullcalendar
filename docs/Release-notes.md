## Index
* [7.1.x](#71x)
* [7.0.x](#70x)
* [6.2.x](#62x)
* [6.1.x](#61x)
* [6.0.x](#60x)
* [4.1.x](#41x)
* [4.0.x](#40x)

## 7.1.x

Significant expansion of the Java API surface to cover FullCalendar v6 options that were previously
only accessible via raw `setOption` calls. All new features are purely additive; no breaking changes
are introduced in this release. See the [migration guide](Migration-guides#migration-notes-70--71)
for the one behaviour change involving `Entry.overlap`.

### Entry model enhancements
- `Entry.setUrl(String)` — entries with a URL are rendered as `<a>` tags; FC navigates to the URL on click
- `Entry.setInteractive(Boolean)` — per-entry keyboard-focusability override (null = inherit global `eventInteractive` setting)
- `Entry.setRecurringDuration(String)` — ISO 8601 duration for multi-day all-day recurring events (e.g. `"P2D"`)
- `Entry.setRRule(RRule)` — RFC 5545 recurrence rules via the `RRule` fluent builder; use `RRule.excludeDates(LocalDate...)` to exclude specific dates from the recurrence
- `Entry.setOverlap(Boolean)` changed from `boolean` to `Boolean` — `null` means "inherit global overlap setting" (see migration notes)

### Display options
- `setCallbackOption(FullCalendar.CallbackOption, String)` — generic callback setter for all render hook callbacks (day cell, day header, slot label, now indicator, resource label/lane, etc.)
- `FullCalendar.CallbackOption` enum — access to all FullCalendar render hook options (DAY_CELL_CLASS_NAMES, DAY_HEADER_CONTENT, SLOT_LABEL_DID_MOUNT, etc.)
- `setProgressiveEventRendering(boolean)` — render events in batches as they arrive

### Interaction callbacks
- Drag/resize lifecycle events: `EntryDragStartEvent`, `EntryDragStopEvent`, `EntryResizeStartEvent`, `EntryResizeStopEvent`
- `setCallbackOption(FullCalendar.CallbackOption.SELECT_ALLOW, String)` / `setCallbackOption(FullCalendar.CallbackOption.ENTRY_ALLOW, String)` / `setCallbackOption(FullCalendar.CallbackOption.ENTRY_OVERLAP, String)` — JS callback setters
- `setUnselectCancel(String)` — CSS selector for elements that should not cancel an active selection
- `ExternalEntryDroppedEvent` — fired when an entry from a client-side event source is dropped into the calendar
- `ExternalEntryResizedEvent` — fired when an entry from a client-side event source is resized
- `setDropAccept(String)` — CSS selector filter for accepted draggable elements

### Event sources
- `JsonFeedEventSource` — typed Java wrapper for FC's JSON feed event source
- `GoogleCalendarEventSource` — typed Java wrapper for the Google Calendar event source
- `ICalendarEventSource` — typed Java wrapper for iCalendar (.ics) feeds
- `setStartParam` / `setEndParam` / `setTimeZoneParam` — control parameter names sent to JSON feeds
- `EventSourceFailureEvent` — server-side event fired when an event source fails to load

### Scheduler resource features
- `setResourceAreaColumns(ResourceAreaColumn...)` — typed column configuration for the resource area
- `setResourceGroupField(String)` — group resources by a shared field value
- `setResourceGroupClassNamesCallback(String)` — CSS class callback for resource group header rows
- `setRefetchResourcesOnNavigate(boolean)` — re-fetch resources on each navigation

### Accessibility
- `Option.ENTRY_INTERACTIVE` — make all entries keyboard-focusable (WCAG 2.1 AA) via `setOption`
- `Option.NATIVE_TOOLBAR_BUTTON_HINTS` — accessible labels for toolbar buttons (use via `setOption`)
- `setViewHint(String)` / `setNavLinkHint(String)` / `setMoreLinkHint(String)` — accessible labels for view switchers, nav links, and "+N more" links
- `setCloseHint(String)` / `setTimeHint(String)` / `Option.ENTRY_HINT` — accessible labels for popover close buttons, time displays, and entries

### Advanced and niche options
- `setCallbackOption(FullCalendar.CallbackOption.VALID_RANGE, String)` / `setCallbackOption(FullCalendar.CallbackOption.SELECT_OVERLAP, String)` — dynamic JS function overrides for `validRange` and `selectOverlap`
- `setEntryConstraint(String/BusinessHours)` — constrain drag/resize to specific time slots
- `setDateIncrement(String)` / `setDateAlignment(String)` — navigation increment and alignment for custom views
- `setContentSecurityPolicyNonce(String)` — CSP nonce for dynamically generated `<style>` tags
- `setViewSpecificOption(viewType, option, value)` — per-view option overrides
- `setFixedMirrorParent(String)` — JS expression for the drag-mirror parent element
- `setDragScrollEls(String...)` — CSS selectors for auto-scroll containers during drag
- `incrementDate(String)` / `previousYear()` / `nextYear()` / `updateSize()` — new navigation and layout methods
- `getCurrentIntervalStart()` / `getCurrentIntervalEnd()` — returns `Optional<LocalDate>` for the current view interval; empty before the first `DatesRenderedEvent` is received

### API surface improvements
- 43 new typed setters in `FullCalendar` (previously required raw `setOption` calls)
- `FullCalendarBuilder` refactored to mutable fluent builder (all `withXxx` methods now return `this`)
- New enums: `Direction`, `WeekNumberCalculation`
- `Entry.setConstraintToBusinessHours()` helper
- `Scheduler.setRefetchResourcesOnNavigate(boolean)` / typo fix `setResourceLabelWillUnmountCallback`

## 7.0.x
[Details](https://github.com/stefanuebe/vaadin_fullcalendar/wiki/Release-notes-7.0)
- updated to FullCalendar 6.1.20
- increased required Vaadin version to 25
- increased required Java version to 21
- replaced elemental Json with Jackson 3, renamed some related methods
- reworked BusinessHours
- added `forRemoval` to deprecated API
- renamed theme variant `LUMO` to `VAADIN` and integrated Aura into theming

## 6.2.x
[Details](https://github.com/stefanuebe/vaadin_fullcalendar/wiki/Release-notes-6.2)
- added custom native event handlers for entries

## 6.1.x
[Details](https://github.com/stefanuebe/vaadin_fullcalendar/wiki/Release-notes-6.1)
- added an optional Lumo theme for the addon

## 6.0.x
- updated to FullCalendar 6.1.6
- Migrated from Polymer 3 to simple HTML Element based web component (no Lit nor Polymer)
- Migrated source code from JavaScript to TypeScript (ongoing process, not yet finished)
- Folder structures changed
- Tag names prefixed with "vaadin-"
- Content is now part of the light dom, thus styling will be easier
- Client side eager loading removed, items will now always be fetched
- Added prefetch mode to allow smoother transition between periods
- Breaking changes regarding methods and fields (client side and server side). Also usage of private / protected modifiers in TS.
- Added support for FC's "multi-month" views.
- Added proper API for creating and registering custom views. Also added an internal handling of "anonymous" custom views created by initial options.
- Deprecated code from previous versions has been removed
- JsonItem has been removed, Entry is a "normal field" class again due to issues with proxying frameworks
- setHeight has been minimalized to be more aligned with Vaadin standards. FC internal height settings / options are not
  supported anymore. Calendar content will take only as much space as needed.
- added type `RecurringTime` to allow setting an entry recurrence of more than 24h


Minor changes:
- getResources now may return null. Use getOrCreateResources.
- CalendarLocale is now an enum. Use getLocale() to obtain the contained locale value.
- week numbers within days is no longer available, weeknumbers are now always display inside days.
- RenderingMode and alike namings have been named to DisplayMode / display to match the FC library naming. Also DisplayMode is now a top level class.
- added resize observer to client side to automatically take care of resizes
- added our own @NotNull annotation to allow support for Vaadin 23 and 24
- Entry's method `copy(Class<T>)` has been renamed to `copyAsType(Class<T>)`.

Other things that we may have overseen :)

Due to lack of time, we have no release note details at this time. We tried to provide additional info as part of the migration page.

## 4.1.x
[Details](https://github.com/stefanuebe/vaadin_fullcalendar/wiki/Release-notes-4.1)
- added EntryProvider, a data provider like callback based class to allow lazy loading entries based on the actual displayed timespan

## 4.0.x
[Details](https://github.com/stefanuebe/vaadin_fullcalendar/wiki/Release-notes-4.0)
- introduced a new type JsonItem for creating item classes with dynamic property handling and automated conversion from and to json
- integrated json item api into Entry types for dynamic type conversion. Due to that entries will not send all data to the client, when updating existing ones
- changed date time handling on server side and communication to be always utc
- entries are not resent to server anymore when changing timezone on server
- entry data changes are now sent at once the the client
- client side entries ("event") have now a getCustomProperty method inside eventDidMount or eventContent callbacks
- removed official support of custom timezones for entries
- renamed several methods
- recurrence has some changes regarding enable recurrence and timezones
