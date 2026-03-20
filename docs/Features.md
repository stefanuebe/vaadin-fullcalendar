This page shows a summary of the most important features of the FullCalendar library, that this addon provides. It is
not a full list of all features. If you find any library features missing, please create a [feature request](https://github.com/stefanuebe/vaadin-fullcalendar/issues/new?template=BLANK_ISSUE).

## FullCalendar features
- Adding, updating, and removing calendar entries using a data provider pattern
- Switching between displayed intervals (next month, previous month, etc.)
- Navigating to a specific date or today
- Switching the calendar view (month, basic day/week, agenda day/week, list views, multi-month)
- Setting a locale for week days, number formatting, and first-day-of-week calculation
- Overriding the first day of the week independently of the locale
- Limiting the maximum number of stacked entries per day (except basic views)
- Activating day/week numbers as navigation links
- Setting business hours with multiple time ranges per week
- Creating recurring events (simple recurrence and RFC 5545 RRule)
- Setting a client-side timezone
- Optional Vaadin/Lumo theme
- Custom native JS event handlers for entries
- Client-side event sources (JSON feeds, Google Calendar, iCalendar)

### Entry model
The `Entry` class supports the following properties:

| Property | Method | Description |
|---|---|---|
| Title | `setTitle(String)` | Displayed label on the calendar |
| Start / End | `setStart(LocalDate)` / `setStart(LocalDateTime)` / `setStart(Instant)` and equivalently `setEnd(LocalDate)` / `setEnd(LocalDateTime)` / `setEnd(Instant)` | `LocalDate`, `LocalDateTime`, or `Instant`; `LocalDate` implies all-day. `setEnd` is optional — omitting it creates a single-day (or point-in-time) entry |
| All-day | `setAllDay(boolean)` | Force all-day rendering regardless of time part |
| Color | `setColor(String)` | HTML color string (`"#ff3333"`, `"red"`) |
| URL | `setUrl(String)` | FC renders the entry as an `<a>` tag and navigates on click |
| Interactive | `setInteractive(Boolean)` | Per-entry keyboard-focusability; `null` = inherit global `eventInteractive` |
| Editable | `setEditable(boolean)` | Enable/disable drag and resize for this entry |
| Display mode | `setDisplayMode(DisplayMode)` | `AUTO`, `BLOCK`, `LIST_ITEM`, `BACKGROUND`, `INVERSE_BACKGROUND`, `NONE` |
| Overlap | `setOverlap(Boolean)` | Whether this entry may overlap others; `null` = inherit global setting |
| Constraint | `setConstraint(String)` | Group id or `"businessHours"` restricting when this entry may be placed |
| Recurring (simple) | `setRecurringDaysOfWeek` / `setRecurringStartTime` / `setRecurringEndTime` | Built-in day-of-week recurrence |
| Recurring duration | `setRecurringDuration(String)` | ISO 8601 duration for multi-day recurring all-day events (e.g. `"P2D"`) |
| RRule | `setRRule(RRule)` | RFC 5545 recurrence rule for complex patterns |
| Exclusion dates | `rrule.excludeDates(LocalDate...)` | Dates excluded from an RRule recurrence — set on the `RRule` builder instance, transferred automatically to the entry |
| Custom properties | `setCustomProperty(String, Object)` | Arbitrary data accessible in JS callbacks |

### RRule — rich recurrence patterns

`RRule` provides a fluent Java API for RFC 5545 recurrence rules, backed by the `@fullcalendar/rrule` plugin (bundled automatically).

```java
// Weekly on Monday, Wednesday, Friday
Entry standup = new Entry();
standup.setTitle("Weekly Standup");
standup.setRRule(RRule.weekly()
    .byWeekday(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
    .dtstart(LocalDate.of(2025, 1, 1))
    .until(LocalDate.of(2025, 12, 31)));

// Last Friday of each month
// byWeekday accepts RFC 5545 BYDAY strings: "-1fr" = last Friday, "1mo" = first Monday,
// "-2tu" = second-to-last Tuesday. Positive/negative numbers select the nth occurrence from
// the start/end of the period.
Entry endOfMonth = new Entry();
endOfMonth.setTitle("Monthly Review");
endOfMonth.setRRule(RRule.monthly().byWeekday("-1fr"));

// Raw RFC 5545 string (for patterns not supported by the fluent builder)
Entry custom = new Entry();
custom.setRRule(RRule.ofRaw("FREQ=WEEKLY;BYDAY=MO,WE;INTERVAL=2"));
```

**When to use RRule vs. built-in recurrence:**
- Use the built-in `recurringDaysOfWeek` / `recurringStartTime` / `recurringEndTime` for simple weekly patterns — less setup, no plugin required.
- Use `RRule` for richer patterns: bi-weekly, monthly-by-weekday, yearly, exclusion dates, limited counts, etc.
- The two mechanisms are mutually exclusive per entry: setting an `RRule` overrides built-in recurrence fields.

### Entry URL behaviour

When `entry.setUrl(url)` is set, FullCalendar renders the entry as an `<a>` tag and navigates to the URL when clicked. The server-side `EntryClickedEvent` **is still fired** when the entry is clicked.

### Accessibility

Set `setOption(Option.ENTRY_INTERACTIVE, true)` to make all entries keyboard-focusable (`tabindex="0"`), enabling keyboard-only users to Tab to entries and activate them with Enter or Space. By default, only entries with a `url` property are focusable. Enabling this globally is recommended for WCAG 2.1 AA, Success Criterion 2.1.1. For per-entry control, use `Entry.setInteractive(Boolean)` (pass `null` to inherit the global setting).

Additional accessibility options (set via `setOption`):
- `Option.NATIVE_TOOLBAR_BUTTON_HINTS` — `Map<String, String>` of `aria-label` values for toolbar buttons
- `Option.NAV_LINK_HINT` / `Option.MORE_LINK_HINT` / `Option.NATIVE_TOOLBAR_VIEW_HINT` — screen-reader labels for navigation links, "+N more" overflow links, and view-switcher buttons (use `$0` as a runtime placeholder for the count or date value, e.g. `"$0 more events"`)
- `Option.CLOSE_HINT` / `Option.TIME_HINT` / `Option.ENTRY_HINT` — labels for popover close buttons, time displays, and entries

### Event handling

Server-side events fired by the calendar:

| Event | When fired |
|---|---|
| `TimeslotClickedEvent` | User clicks an empty time slot |
| `TimeslotsSelectedEvent` | User selects a range of time slots |
| `EntryClickedEvent` | User clicks an entry (including keyboard activation when `Option.ENTRY_INTERACTIVE` is true) |
| `EntryDroppedEvent` | User drops an entry after dragging (includes new start/end) |
| `EntryResizedEvent` | User resizes an entry (includes new start/end) |
| `EntryDragStartEvent` | User begins dragging an entry |
| `EntryDragStopEvent` | User releases an entry after dragging (fires regardless of whether a valid drop occurred) |
| `EntryResizeStartEvent` | User begins resizing an entry |
| `EntryResizeStopEvent` | User releases after resizing (fires regardless of whether a valid resize occurred) |
| `EntryMouseEnterEvent` / `EntryMouseLeaveEvent` | Mouse enters/leaves an entry |
| `DatesRenderedEvent` | The current view interval is rendered (use to update labels, refresh data, etc.) |
| `MoreLinkClickedEvent` | User clicks the "+N more" overflow link |
| `DayNumberClickedEvent` / `WeekNumberClickedEvent` | User clicks a day/week number (when `navLinks` is enabled) |
| `BrowserTimezoneObtainedEvent` | The client's local timezone is detected on first load |
| `EventSourceFailureEvent` | A client-side event source (JSON feed, Google Calendar, iCal) failed to load |
| `ExternalEntryDroppedEvent` | An entry from a client-side event source is dropped into the calendar |
| `ExternalEntryResizedEvent` | An entry from a client-side event source is resized |

### Event sources (client-side)

In addition to the server-managed `EntryProvider`, the calendar supports client-side event sources that load data directly in the browser:

```java
// JSON feed — FC fetches from your REST endpoint with start/end query parameters
JsonFeedEventSource jsonFeed = new JsonFeedEventSource("https://example.com/events");
// .withEditable(true)  // optional: enable drag/drop for entries from this source
calendar.addClientSideEventSource(jsonFeed);

// Google Calendar
GoogleCalendarEventSource google = new GoogleCalendarEventSource("calendarId@gmail.com");
google.withApiKey("YOUR_API_KEY");  // or set globally: calendar.setOption(Option.EXTERNAL_EVENT_SOURCE_GOOGLE_CALENDAR_API_KEY, key)
calendar.addClientSideEventSource(google);

// iCalendar (.ics) feed
ICalendarEventSource ical = new ICalendarEventSource("https://example.com/calendar.ics");
calendar.addClientSideEventSource(ical);
```

Client-side event source entries are read-only by default (`editable = false`). To enable drag/drop and resize for a source, call `.withEditable(true)` on the source instance before adding it to the calendar. When a drag-drop occurs, an `ExternalEntryDroppedEvent` is fired; when a resize occurs, an `ExternalEntryResizedEvent` is fired (both instead of their server-managed counterparts `EntryDroppedEvent` / `EntryResizedEvent`).


### View-specific options

Override any option for a specific view type only. The view type string is the FullCalendar view name
(e.g. `"dayGridMonth"`, `"dayGridWeek"`, `"timeGridWeek"`, `"timeGridDay"`, `"listWeek"`) or a
prefix that matches multiple views (e.g. `"timeGrid"` matches both `timeGridWeek` and `timeGridDay`).
You can also pass a `CalendarView` enum value instead of a raw string.

```java
// Limit event rows to 3 in month view, no limit in other views
calendar.setViewSpecificOption("dayGridMonth", Option.DAY_MAX_EVENT_ROWS, 3);

// Custom slot duration only in time-grid views
// All three forms work thanks to the DurationConverter:
calendar.setViewSpecificOption("timeGrid", Option.SLOT_DURATION, "00:30:00");     // string
calendar.setViewSpecificOption("timeGrid", Option.SLOT_DURATION, Duration.ofMinutes(30));  // Duration
calendar.setViewSpecificOption("timeGrid", Option.SLOT_DURATION, LocalTime.of(0, 30));    // LocalTime
```

### Navigation

```java
calendar.next();           // next interval
calendar.previous();       // previous interval
calendar.today();          // jump to today
calendar.gotoDate(date);   // jump to a specific date
```

## Scheduler features
The scheduler extension integrates the features of the commercial FullCalendar Scheduler plugin.
A valid license key is required for production use (see [Scheduler license](Scheduler-license)).

- Adding and removing resources (including hierarchical resource trees)
- Linking one or multiple resources to entries (`ResourceEntry`)
- Resource grouping by field value (`setOption(SchedulerOption.RESOURCE_GROUP_FIELD, ...)`) with customisable group headers
- Multiple resource area columns (`setResourceAreaColumns`)
- Timeline views and vertical resource views (`SchedulerView`)
- Filtering and ordering resources on the server side

Event handling:
- Timeslot clicked / selected
- Entry dropped (including the resource assignment after drop)

## Developer Tools

- **[MCP Server](MCP-Server)**: Model Context Protocol server for AI assistants (Claude Code, GitHub Copilot, etc.) providing searchable documentation, Java API reference, code examples, and model schemas.
