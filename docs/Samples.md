This page contains some samples to get you started with the FullCalendar addon. The samples are always based on the
latest version of the addon.

Some samples use an in-memory entry provider when modifying the calendar data to keep things simple. You may
need to adapt those parts, if you use a different entry provider.

Also we tried to keep things short. So you may see variables like `calendar`, `entry` or `entryProvider`
without any declaration. In those cases these represent the basic types `FullCalendar`, `Entry` or `EntryProvider`.

If you find an outdated sample, please create an issue for that.

## Creating a basic calendar instance and add an entry

The FullCalendar is a normal Vaadin component, that can be added to your view as any other component. By default it uses
an eager loading in memory entry provider, with which you simply can add, update or remove calendar entries.

```java
// Create a new calendar instance and attach it to our layout
FullCalendar calendar = FullCalendarBuilder.create().build();
calendar.setSizeFull();
container.add(calendar);

// Create an initial sample entry
Entry entry = new Entry();
entry.setTitle("Some event");
entry.setColor("#ff3333");

// the given times will be interpreted as utc based - useful when the times are fetched from your database
entry.setStart(LocalDate.now().withDayOfMonth(3).atTime(10, 0));
entry.setEnd(entry.getStart().plusHours(2));

// FC uses a data provider concept similar to the Vaadin default's one, with some differences
// By default the FC uses a in-memory data provider, which is sufficient for most basic use cases.
calendar.getEntryProvider().asInMemory().addEntry(entry); // use addEntries(e1, e2, ...) to add multiple at once
```

## Add, update or remove a calendar entry

This sample shows some basic CRUD functions using the in memory entry provider.
When you use a callback entry provider, the add/remove methods are not available. Instead
you need to update your data structure and call the refresh methods. See details
in the [EntryProvider](#Entry-providers) section.

```java
// ... create a form and binder to provide editable components to the user
InMemoryEntryProvider<Entry> entryProvider = calendar.getEntryProvider().asInMemory();

HorizontalLayout buttons = new HorizontalLayout();
Button buttonSave;
if (newInstance) {
    buttonSave = new Button("Create", e -> {
        if (binder.validate().isOk()) {
            // add the entry to the calendar instance and inform the client to update itself
            entryProvider.addEntry(entry);
            entryProvider.refreshAll();
        }
    });
} else {
    buttonSave = new Button("Save", e -> {
        if (binder.validate().isOk()) {
            // update an existing entry in the client side
            // this will only send changed data
            entryProvider.refreshItem(entry);
        }
   });
}
buttons.add(buttonSave);

if (!newInstance) {
    Button buttonRemove = new Button("Remove", e -> {
        entryProvider.removeEntry(entry);
        entryProvider.refreshAll();
    });
    buttons.add(buttonRemove);
}
```

## Calendar event handling
This sample shows how to react on calendar events, that are triggered by the user or the calendar lifecycle.

```java
/*
 * The day click event listener is called when a user clicks in an empty space inside of the
 * calendar. Depending of if the clicked point was a day or time slot the event will provide the
 * time details of the clicked point. With this info you can show a dialog to create a new entry.
 */
calendar.addTimeslotsSelectedListener((event) -> {
// react on the selected timeslot, for instance create a new instance and let the user edit it
    Entry entry = new Entry();
   
    entry.setStart(event.getStart()); // also event times are always utc based
    entry.setEnd(event.getEnd());
    entry.setAllDay(event.isAllDay());

    entry.setColor("dodgerblue");

    // ... show an editor or do something else with the entry
});

/*
 * The entry click event listener is called when the user clicks on an existing entry.
 * The event provides the clicked event which might be then opened in a dialog.
 */
calendar.addEntryClickedListener((event) -> {
    // react on the clicked entry, for instance let the user edit it
    Entry entry = event.getEntry();

    // ... show an editor or do something else with the entry
});
```

## Entry providers

Entry providers allow you to minimize the memory footprint by activating lazy loading for calendar entries.
The `InMemoryEntryProvider` keeps all entries server-side and streams only the visible period to the client;
the `CallbackEntryProvider` delegates every fetch to your own backend query.

### In memory entry provider

The `InMemoryEntryProvider` caches all registered entries on the server side, but provides only a subset of them to
the client (i. e. the entries of the current shown period). This way you can use the CRUD API on the server side
without the need of implementing it yourself. On the other hand the client will be kept free of unnecessary information.

```java
// load items from backend
List<Entry> entryList = backend.streamEntries().collect(Collectors.toList());

// init provider from a collection — does NOT use the collection as a live backend (changes must be pushed via addEntries/removeEntry)
InMemoryEntryProvider<Entry> entryProvider = EntryProvider.inMemoryFrom(entryList);

// set entry provider
calendar.setEntryProvider(entryProvider);

// CRUD operations
// to add
Entry entry = new Entry();          // ... plus some init
entryProvider.addEntry(entry);      // use addEntries(e1, e2, ...) to add multiple at once
entryProvider.refreshAll();         // call refresh to inform the client about the data change and trigger a refetch

// after some change
entryProvider.refreshItem(entry);   // call refresh to inform the client about the data change and trigger a refetch

// to remove
entryProvider.removeEntry(entry);
entryProvider.refreshAll();         // call refresh to inform the client about the data change and trigger a refetch
```

### Using callbacks

The callback entry provider is a base implementation of the `EntryProvider` interface. It does not care about how the
backend creates or stores the entry data, but only fetches the entries to show from it by passing a query. The backend
is responsible for providing the entries and handle any changes to the data (e. g. due to calendar entry events).

```java
// the callback provider uses the given callback to fetch entries when necessary
CallbackEntryProvider<Entry> entryProvider = EntryProvider.fromCallbacks(
        query -> backend.streamEntries(query),
        entryId -> backend.getEntry(entryId).orElse(null)
);

// set entry provider
calendar.setEntryProvider(entryProvider);

// CRUD operations
// to add
Entry entry = new Entry();          // ... plus some init
backend.addEntry(entry);            // register in your backend
entryProvider.refreshAll();         // call refresh to inform the client about the data change and trigger a refetch

// after some change
backend.updateEntry(entry);         // inform your backend
entryProvider.refreshItem(entry);   // call refresh to inform the client about the data change and trigger a refetch

// to remove
backend.removeEntry(entry);         // remove from your backend
entryProvider.refreshAll();   // call refresh to inform the client about the data change and trigger a refetch
```

### Custom implementation

Feel free to create your own custom implementation, for instance to provide advanced internal caching on the server
side. We recommend to extend the `AbstractEntryProvider` to start with.

The simplest variant is similar to the callback variant, but with its own class:

```java
private static class BackendEntryProvider extends AbstractEntryProvider<Entry> {
    private final EntryService service;

    public BackendEntryProvider(EntryService service) {
        this.service = service;
    }

    @Override
    public Stream<Entry> fetch(@NonNull EntryQuery query) {
        return service.streamEntries(query);
    }

    @Override
    public Optional<Entry> fetchById(@NonNull String id) {
        return service.getEntry(id);
    }
}
```

### Prefetch mode 
Normally an entry provider would only fetch data for the current period. Switching to an adjacent one could
lead to flickering, since the calendar will most likely resize itself (because the now shown entries have a different
size, are on different days, the month is shorter or longer, etc).

To prevent that flickering, the calendar provides a feature called "prefetch mode". This by default activated feature 
ensures, that the calendar will additionally fetch the previous and next period together with the current one. 
When switching to an adjacent period, the calendar will not simply be empty, but show the prefetched entries to prevent
visual glitches. In the same time, the client will fetch the latest state for the current period and update it
again, so that the shown data is up-to-date. 

Be aware, that this feature leads to an increased amount of data transported between server and client, but in the same 
time leads to a better user experience as the above mentioned flickering will be prevented.

The prefetch mode determines adjacent periods based on the active view's *range unit* — the natural navigation step of the view (e.g., `timeGridWeek` has a "week" unit, `dayGridMonth` has a "month" unit). The supported units are `"day"`, `"month"`, and `"year"`. If the range unit cannot be determined, a warning is logged to the Java console (SLF4J at WARN level) and prefetch behaves as if disabled. Custom or unsupported views also fall back silently to the non-prefetch behavior.

We recommend keeping this feature enabled, unless you experience issues due to slow bandwidth or other network issues.

```java
calendar.setPrefetchEnabled(false); // disables the prefetch feature
```

## Setting the calendar's dimensions

You may set the dimensions as with every other Vaadin component. The FC library also brings in some additional
settings for content height or an aspect ratio, that can be taken into account. These can be set via `setOption`,
e.g. `calendar.setOption(Option.CONTENT_HEIGHT, "500px")` or `calendar.setOption(Option.ASPECT_RATIO, 1.5)`.
See the [FullCalendar sizing docs](https://fullcalendar.io/docs/sizing) for all available options.

## Using timezones
You can set a timezone to the calendar, so that your UTC-based entries are automatically shown with the respective
timezone's offset at the client. 

Please note, that this only affects the FullCalendar. Vaadin date pickers, that are for instance there to edit
calendar entries in a form have to be configured respectively. 

```java
// FC allows to show entries in a specifc timezone. Setting a timezone only affects the client side
// and might be interesting, when editing those entries in some kind of edit form

Timezone tzBerlinGermany = new Timezone(ZoneId.of("Europe/Berlin"));
calendar.setTimezone(tzBerlinGermany); // will rerender the client side and show all times 1-2 hours "later".

// We can also reset the timezone to default.
calendar.setTimezone(Timezone.UTC);

// We can also read the browsers timezone, after the component has been attached to the client side.
// There are other ways to obtain the browser's timezone, so you are not obliged to use the listener.
calendar.addBrowserTimezoneObtainedListener(event -> calendar.setTimezone(event.getTimezone()));

// If you want to let the calendar obtain the browser time zone automatically, you may simply use the builder.
// In that case as soon as the client connected, it will set it's timezone in the server side instance.
FullCalendarBuilder.create().withAutoBrowserTimezone().build();

// Entries use internally utc to define times. The LocalDateTime and Instant methods setStart/End have the same effect.
entry.setStart(Instant.now()); // UTC
entry.setEnd(LocalDateTime.now()); // treated as UTC — LocalDateTime carries no timezone info in this API

// Entry provides some additional convenience methods to handle the current calendar's timezone's offset, e.g. to allow easy
// integration into edit forms.
calendar.setTimezone(tzBerlinGermany); // times are now 1-2 hours "ahead" (depending on daylight saving)
entry.setStart(LocalDate.of(2000, 1, 1).atStartOfDay());

LocalDateTime utcStart = entry.getStart(); // will be 2000-01-01, 00:00
LocalDateTime offsetStart = entry.getStartWithOffset(); // will be 2000-01-01, 01:00

// ... modify the offset start, for instance in a date picker
// e.g. modifiedOffsetStart = offsetStart.plusHours(5);
LocalDateTime modifiedOffsetStart = offsetStart.plusHours(5);

entry.setStartWithOffset(modifiedOffsetStart); // automatically takes care of conversion back to utc
utcStart = entry.getStart(); // will be 2000-01-01, 04:00
offsetStart = entry.getStartWithOffset(); // will be 2000-01-01, 05:00
```

## Passing custom initial options in Java

You can fully customize the client side options in Java by passing a json object when creating the FullCalendar.
Please be aware, that some options are always set, regardless of the values you set. Please check the
ApiDocs of the `withInitialOptions` method (or respective constructors) for details

The following example shows the default initial options as they are set internally by the web component.

```java
import org.vaadin.stefan.fullcalendar.JsonFactory;

// ...

ObjectNode initialOptions = JsonFactory.createObject();
initialOptions.put("height", "100%");
initialOptions.put("timeZone", "UTC");
initialOptions.put("headerToolbar", false);
initialOptions.put("weekNumbers", true);
initialOptions.put("dayMaxEvents", false); // pass an int value to limit the entries per day
initialOptions.put("navLinks", true); 
initialOptions.put("selectable", true);

calendar = FullCalendarBuilder.create().withInitialOptions(initialOptions).build();
```

## Style the calendar

### Vaadin Theming
The calendar provides a built-in Vaadin theme variant, that applies some styling from the current Vaadin theme, like
sizes, colors, etc. It is active by default. 

Please note, that there might be parts, that have been forgotten or not looking as expected. 
Also any additional custom stylings may override the Vaadin stylings. If you find anything, that looks suspicious,
please create an issue. 

To remove the Vaadin theme, simply remove the theme variant, as you would do with other Vaadin components. Please note,
that the overall styling still may affect your calendar component in some ways.

```java
calendar.removeThemeVariants(FullCalendarVariant.VAADIN);
```

### Global / custom styles

The FullCalendar client element is part of the light dom and thus can be styles via plain css. For any details
on styling the FullCalendar please refer to the FC docs (regarding css properties, classes, etc.).

The styles can be defined as you are used to using the Vaadin theme mechanism.

Sample styles.css, that utilizes custom css properties from Lumo.

```css
/* change the border color of the fc in a global way*/
.fc {
    --fc-border-color: #ddd;
}

/* change the border color of the fc for dark themes*/
[theme~="dark"] .fc {
    --fc-border-color: #333;
}

/* change the appearance of the week and day number to a more button like style when hovering */
.fc a:is(.fc-daygrid-week-number, .fc-daygrid-day-number) {
    background: transparent;
    font-size: 12px;
    transition: background 200ms ;
    border-radius: 3px;
}

.fc a:is(.fc-daygrid-week-number, .fc-daygrid-day-number):hover {
    background: var(--lumo-primary-color-10pct);
    text-decoration: none;
}

.fc a.fc-daygrid-day-number {
    padding-left: 6px;
    padding-right: 6px;
}
```

## Show the current shown time interval (e. g. month) with Vaadin components

You can use the "dates rendered event" to show details about the current shown period in separate elements instead
of the built-in FullCalendar header.

```java
private void init() {
    // The element that should show the current interval.
    HasText intervalLabel = new Span();

    // combo box to select a view for the calendar, like "monthly", "weekly", ...
    ComboBox<CalendarView> viewBox = new ComboBox<>("", CalendarViewImpl.values());
    viewBox.addValueChangeListener(e -> {
        CalendarView value = e.getValue();
        calendar.changeView(value == null ? CalendarViewImpl.DAY_GRID_MONTH : value);
    });
    viewBox.setValue(CalendarViewImpl.DAY_GRID_MONTH);

    /*
     * The view rendered listener is called when the view has been rendererd on client side
     * and FC is aware of the current shown interval. Might be accessible more directly in
     * future.
     */
    calendar.addDatesRenderedListener(event -> {
        LocalDate intervalStart = event.getIntervalStart();
        CalendarView cView = viewBox.getValue();

        String formattedInterval = ... // format the intervalStart based on cView. See the demos for examples.

        intervalLabel.setText(formattedInterval);
    });
}
```

## Creating a background entry
A background entry is an entry, that is rendered behind all other entries. It is not clickable and
has no tooltip. It is useful for marking a time range, e. g. for marking a vacation.

```java
Entry entry = new Entry();
// ... setup entry details
        
entry.setDisplayMode(DisplayMode.BACKGROUND);
calendar.getEntryProvider().asInMemory().addEntry(entry);
```

## Adding business hours
You can define business hours for each day of the week to provide visual feedback to the user, when someone is available
or not. If you don't define any business hours, the calendar will assume, that the business hours are from 0:00 to 24:00 for each day.

Non-business hours are grayed out in the calendar.

```java
// Single instance for "normal" business week (mo-fr)
calendar.setOption(Option.BUSINESS_HOURS, BusinessHours.businessWeek().start(LocalTime.of(9, 0)).end(LocalTime.of(17, 0)));

// Multiple instances
calendar.setOption(Option.BUSINESS_HOURS, new BusinessHours[]{
    BusinessHours.businessWeek().start(9).end(17),
    BusinessHours.of(DayOfWeek.SATURDAY).start(10).end(14)
});

// Single instance for "each day from 9am to midnight"
calendar.setOption(Option.BUSINESS_HOURS, BusinessHours.allDays().start(9));
```

## Using the Scheduler
The scheduler is a commercial plugin of the FullCalendar library, that provides some additional features like
resource related calendar entries and additional views.  

### Activating the Scheduler

```java
FullCalendar calendar = FullCalendarBuilder.create().withScheduler().build();
// scheduler options
calendar.setOption(SchedulerOption.LICENSE_KEY, "YourFullCalendarSchedulerKey");
```

### Adding a resource to a calendar and link it with entries
```java
Scheduler scheduler = (Scheduler) calendar;

// null as first argument means: let FullCalendar auto-generate an ID
Resource resource = new Resource(null, "Room A", color);
scheduler.addResource(resource);

// When we want to link an entry with a resource, we need to use ResourceEntry
// (a subclass of Entry)
ResourceEntry entry = new ResourceEntry();
entry.setTitle(title);
entry.setStart(start.atStartOfDay());
entry.setEnd(start.plusDays(days).atStartOfDay());
entry.setAllDay(true);
entry.setColor(color);
entry.setDescription("Some description...");
entry.addResources(resource);
calendar.getEntryProvider().asInMemory().addEntry(entry);
```

### Handling change of an entry's assigned resource by drag and drop
```java
calendar.addEntryDroppedListener(event -> {
    event.applyChangesOnEntry();

    Entry entry = event.getEntry();

    if(entry instanceof ResourceEntry) {
        Set<Resource> resources = ((ResourceEntry) entry).getResources();
        if(!resources.isEmpty()) {
            // do something with the resource info
        }
    }
});
```

### Switching to a timeline view
```java
calendar.changeView(SchedulerView.TIMELINE_DAY);
```

### Activate vertical resource view
```java
calendar.setGroupEntriesBy(GroupEntriesBy.RESOURCE_DATE);
```

### Creating a resource based background entry
```java
ResourceEntry entry = new ResourceEntry();
// ... setup entry details, including addResource()

entry.setDisplayMode(DisplayMode.BACKGROUND);
calendar.getEntryProvider().asInMemory().addEntry(entry);
```

### Creating hierarchical resources
```java
// Create a parent resource. When adding the sub resources first before adding the parent to the calendar,
// the sub resources are registered automatically on client side and server side.

Scheduler scheduler = (Scheduler) calendar;

Resource parent = new Resource();
parent.addChildren(new Resource(), new Resource(), new Resource());

scheduler.addResource(parent); // will add the resource and also its children to server and client

// add new resources to already registered parents
Resource child = new Resource();
parent.addChild(child);
scheduler.addResource(child); // this will update the client side

// or remove them from already registered ones
scheduler.removeResource(child);
parent.removeChild(child);
```

### Making a resource entry draggable between resources
```java
// activate globally — allows all entries to be dragged between resources
calendar.setOption(SchedulerOption.ENTRY_RESOURCES_EDITABLE, true);
```

### Using component resource area columns

Resource area columns can display interactive Vaadin components (such as DatePicker, TextField, ComboBox, etc.) — one component per resource. This is useful for inline editing or displaying resource-related metadata. Components are created by a callback and support type-safe runtime access.

#### Basic component column

```java
FullCalendarScheduler scheduler = (FullCalendarScheduler) FullCalendarBuilder.create()
    .withScheduler(Scheduler.GPL_V3_LICENSE_KEY)
    .build();

// Create a DatePicker column that receives a Resource and returns a component
ComponentResourceAreaColumn<DatePicker> deadlineCol = new ComponentResourceAreaColumn<>(
    "deadline",  // unique field key
    "Deadline",  // column header
    resource -> {
        DatePicker picker = new DatePicker();
        picker.setWidth("130px");
        picker.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                // Handle user interaction — e.g., update entry or resource data
                System.out.println("Deadline for " + resource.getTitle() + " → " + e.getValue());
            }
        });
        return picker;
    }
);

// Combine with regular text columns
scheduler.setResourceAreaColumns(
    new ResourceAreaColumn("title", "Name").withWidth("200px"),
    deadlineCol.withWidth("160px")
);
```

#### Accessing and updating components

```java
// Type-safe access to a component at any time
deadlineCol.getComponent(resource).ifPresent(picker ->
    picker.setValue(LocalDate.now())
);

// Iterate all resource-component mappings (unmodifiable, keyed by resource ID)
deadlineCol.getComponents().forEach((resourceId, picker) -> {
    // Read or update — picker is already typed as DatePicker
});

// Force re-creation of all components (state is lost)
deadlineCol.refreshAll();

// Re-create a single resource's component
deadlineCol.refresh(resource);
```

#### Syncing with entry drag/resize

Components and entries can be kept in sync — for example, a DatePicker showing entry start/end dates that updates when the entry is dragged or resized:

```java
ComponentResourceAreaColumn<DatePicker> startCol = new ComponentResourceAreaColumn<>(
    "start", "Start",
    resource -> new DatePicker()
);

scheduler.addEntryResizedListener(event -> {
    event.applyChangesOnEntry();

    // Update component(s) with new entry time
    if (event.getEntry() instanceof ResourceEntry re) {
        for (Resource resource : re.getResources()) {
            startCol.getComponent(resource).ifPresent(picker ->
                picker.setValue(event.getEntry().getStart().toLocalDate())
            );
        }
    }
});
```

## Using tippy.js for description tooltips
By default the calendar does not provide tooltips for entries. However, you can easily integrate any type of
tooltip mechanism or library, for instance by simply applying an html title or using a matured tooltip library
like tippy.js.

This sample shows how to easy integrate tippy.js into a custom subclass of FullCalendar to show an entry's description
as a tooltip when hovering the entry inside the FC. Please customize the example as needed.

1. Create a new TypeScript file inside the frontend folder of your project. It needs to extend either FullCalendar or
   FullCalendarScheduler. This example utilizes FullCalendarScheduler. If you want to use the normal FC, simply remove
   all the -Scheduler parts. You may also use plain JavaScript instead of TypeScript — in that case remove the type
   annotations and rename the file to `.js`.

   > **Note:** The `@JsModule` annotation in the Java class references `full-calendar-with-tooltip.js` (without the
   > `s` at the end, and `.js` extension). Vaadin's frontend build (Vite) compiles the `.ts` file to `.js` automatically,
   > so the annotation must point to the compiled output name.

full-calendar-with-tooltip.ts

```typescript
import {FullCalendarScheduler} from 'Frontend/generated/jar-resources/vaadin-full-calendar/full-calendar-scheduler';
import tippy from 'tippy.js';


export class FullCalendarWithTooltip extends FullCalendarScheduler {
    private _tooltipInitialized: boolean = false;

    initCalendar() {
        super.initCalendar();

        // Prevent duplicate event handler registration on re-initialization
        if (this._tooltipInitialized) {
            return;
        }
        this._tooltipInitialized = true;

        this.calendar!.setOption("eventDidMount", e => {
            this.initTooltip(e);
        });
    }

    initTooltip(e: any) {
        if (e.event.title && !e.isMirror) {
            e.el.addEventListener("mouseenter", () => {
                let tooltip = e.event.getCustomProperty("description", e.event.title);

                e.el._tippy = tippy(e.el, {
                    theme: 'light',
                    content: tooltip,
                    trigger: 'manual'
                });

                e.el._tippy.show();
            })

            e.el.addEventListener("mouseleave", () => {
                if (e.el._tippy) {
                    e.el._tippy.destroy();
                }
            })
        }
    }
}

customElements.define("full-calendar-with-tooltip", FullCalendarWithTooltip);

```

2. Now create a simple JavaClass, that utilizes your js file. This Java class also imports the needed CSS files.

```java
@Tag("full-calendar-with-tooltip")
@JsModule("./full-calendar-with-tooltip.js")
@CssImport("tippy.js/dist/tippy.css")
@CssImport("tippy.js/themes/light.css")
public class FullCalendarWithTooltip extends FullCalendarScheduler {

    public FullCalendarWithTooltip() {
    }
}
```

As shown in the subclass sample, you may also use the FullCalendarBuilder to create your custom class.

## Customize entry rendering (render hooks)

FC allows you to hook into the rendering of entries using `setOption` with a `JsCallback` value
and an `Option` constant (see [FullCalendar render hook docs](https://fullcalendar.io/docs/event-render-hooks)
for callback arguments). The function string is evaluated in the browser — no server round-trip occurs.

> **Note for `FullCalendarBuilder` users:** `withEntryContent(String)` on the builder is deprecated.
> Use `setOption(Option.ENTRY_CONTENT, JsCallback.of(...))` after building instead.

**`Option.ENTRY_DID_MOUNT`** — called after an entry element is added to the DOM. Use it for setup, e.g.
setting element attributes:

```java
calendar.setOption(Option.ENTRY_DID_MOUNT, JsCallback.of("""
        function(info) {
            info.el.id = "entry-" + info.event.id;
        }
        """));
```

**`Option.ENTRY_CONTENT`** — customize the HTML content rendered inside the entry element. See
[content injection](https://fullcalendar.io/docs/content-injection) for the return value format:

```java
calendar.setOption(Option.ENTRY_CONTENT,
        JsCallback.of("function(info) { return { html: '<b>' + info.event.title + '</b>' }; }"));
```

Inside entry callbacks you may access the entry's default properties or custom ones set via
`entry.setCustomProperty(String, Object)`. The `getCustomProperty(key)` and
`getCustomProperty(key, defaultValue)` methods are injected automatically onto the event object:

```java
// set the custom property beforehand
entry.setCustomProperty(Entry.EntryCustomProperties.DESCRIPTION, "some description");

// access it inside the callback
calendar.setOption(Option.ENTRY_CONTENT, JsCallback.of(
        "function(info) {" +
        "   let desc = info.event.getCustomProperty('" + Entry.EntryCustomProperties.DESCRIPTION + "', '');" +
        "   return { html: '<b>' + info.event.title + '</b><br>' + desc };" +
        "}"));
```

Callbacks can be set before or after the calendar is attached.

Also make sure that your callback function does not contain any harmful code or allow cross-site scripting.

## Use native javascript events for entries
Sometimes the available events are not enough. For that purpose, we added native event listeners for calendar entries. 
These allow you to setup JavaScript events for each entry, e.g. a mouse over event handler. Inside these event handlers 
you may also access the created entry dom element.

Custom native event handlers are added to the FullCalender object. They will then be applied to each created
entry object (using the entryDidMount callback).

To add an event handler, simply call the method `addEntryNativeEventListener` on the calendar. The first parameter
is the JavaScript event name (e.g. "mouseover"), the second parameter is the callback, that shall be used for
that event. Please be aware, that we do NOT check or sanitize the given JavaScript. It is up to you to prevent
malicious code from being sent to your users.

Inside the event callback, you may access the entryDidMount argument object, that contains additional information
about the current entry. See the official docs (https://fullcalendar.io/docs/event-render-hooks)
for more details about which details it provide.

```java
FullCalendar calendar = new FullCalendar();

// ... other configurations

// write the js event, the current entry info and the current entry's element to the browser console.
calendar.addEntryNativeEventListener("mouseover", "e => console.warn(e, info.event, info.el)");

add(calendar);
```

This sample will change the element style, when the mouse moves over it and changes back, when leaving the element.

```java
calendar.addEntryNativeEventListener("mouseover", "e => info.el.style.opacity = '0.5'");
calendar.addEntryNativeEventListener("mouseout", "e => info.el.style.opacity = ''");
```

You can also access the client side dom to utilize other elements, like the parents. With this you may for instance
call a server side method.

The following sample shows, how a client callable method in the current view, containing the FullCalendar object, can
be called, when right clicking the entry. With this info you can for instance open a custom popup as a context menu.

```java
@Route(...)
public class MyCalendarView extends VerticalLayout {
    public MyCalendarView() {

        FullCalendar calendar = new FullCalendar();
        // adds a contextmenu / right client event listener, that calls our openContextMenu.
        // "this" is the fc object, "this.el" is the Flow element and "this.el.parentElement" is our current view.
        // This hierarchy access may change, when you nest the FC into other containers.

        calendar.addEntryNativeEventListener("contextmenu",
                "e => this.el.parentElement.$server.openContextMenu(info.event, e.clientX, e.clientY)");

        add(calendar);
    }

    @ClientCallable
    public void openContextMenu(JsonObject e, int pointerX, int pointerY) {
        System.out.println(e);
        System.out.println(pointerX);
        System.out.println(pointerY);
    }
} 
```

You can combine the event handlers with a custom entryDidMount callback, if you want additional customizations
of the entries. The FC will take care of combining the event handlers and your EDM callback
```java
calendar.setOption(Option.ENTRY_DID_MOUNT, JsCallback.of("""
       function(info) {
           console.warn("my custom callback");
       }"""));

calendar.addEntryNativeEventListener("mouseover", "e => info.el.style.opacity = '0.5'");
calendar.addEntryNativeEventListener("mouseout", "e => info.el.style.opacity = ''");
```

The following sample shows how to utilize the entryDidMount callback, the native event handlers and the
[Popup addon](https://vaadin.com/directory/component/popup) to show a context menu. In this sample, the context
menu is based on a ListBox.

```java
@Route(...)
public class MyCalendarView extends VerticalLayout {

    private Popup popup;

    public MyCalendarView() {

        FullCalendar calendar = new FullCalendar();
        // adds a contextmenu / right client event listener, that calls our openContextMenu.
        // "this" is the fc object, "this.el" is the Flow element and "this.el.parentElement" is our current view.
        // This hierarchy access may change, when you nest the FC into other containers.

        calendar.addEntryNativeEventListener("contextmenu",
                "e => {" +
                        "   e.preventDefault(); " +
                        "   this.el.parentElement.$server.openContextMenu(info.event.id);" +
                        "}");

        // by default, the entry element has no id attribute. Therefore we have to add it ourselves, using the
        // entry id, that is by default an auto generated UUID
        calendar.setOption(Option.ENTRY_DID_MOUNT, JsCallback.of("""
                function(info) {
                    info.el.id = "entry-" + info.event.id;
                }"""));

    }

    @ClientCallable
    public void openContextMenu (String id){
        initPopup(); // init the popup

        popup.removeAll(); // remove old content


        // setup the context menu
        // (side note: the list box shows a checkmark, when selecting an item, therefore you may want to use a different 
        // component for a real application or hide the checkmark with CSS)
        ListBox<String> listBox = new ListBox<>();
        listBox.setItems("Option A", "Option B", "Option C");
        listBox.addValueChangeListener(event -> {
            Notification.show("Selected " + event.getValue());
            popup.hide();
        });

        popup.add(listBox);
        popup.setFor("entry-" + id);

        popup.show();
    }

    private void initPopup () {
        if (popup == null) {
            popup = new Popup();
            popup.setFocusTrap(true);
            add(popup);
        }
    }
} 
```

## Creating a subclass of FullCalendar for custom mods
The FullCalendar itself is just a simple Vaadin component on the server and client side, that can be extended
and customized beyond the default behavior - be aware, that anything you do here is on your own risk and that
support for this use case is limited. 

The client side has the following methods, that you can override / extend to customize the behavior. There are
others, but they should normally not be overridden - except for you know what you do ;) 

* connectedCallback() - called when the component is attached to the dom
* initCalendar() - called when the calendar is initialized - only called once
* createInitOptions(initialOptions) - called when the calendar is initialized. You can modify the initial options here.
* createEventHandlers() - called when the calendar is initialized. You can modify the event handlers here.

Be aware, that during `connectedCallback()` and before calling `initCalendar()` no internal calendar
object is available. Calling `this.calendar` will automatically infer `initCalendar()` and thus can lead
to unwanted side effects. Therefore, if you want to set options, add entries or do other things with the
calendar object, do it after `super.initCalendar()` has been called.

If you want to modifiy options, that are passed into the calendar object, you can extend the method
`createInitOptions(initialOptions)` and return a modified options object.

If you want to modify or extend the event handlers, you can override the method `createEventHandlers()`-

We recommend to use the `override` modifier on overridden methods to make sure, that the method is
always up-to-date.

1. Create a custom web component
   Create a custom component, that extends FullCalendar or FullCalendarScheduler.


```typescript
import {FullCalendar} from 'Frontend/generated/jar-resources/vaadin-full-calendar/full-calendar';

export class MyFullCalendar extends FullCalendar {
    connectedCallback() {
        super.connectedCallback();

        // do something with this.calendar
        // ...
    }

    initCalendar() {
        super.initCalendar();

        // do something with this.calendar
        // ...
    }

    createInitOptions(initialOptions) {
        let options = super.createInitOptions(initialOptions);
        // modify the initial options
        // attention: this.calendar is not available here!
        // ...

        return options;
    }

    createEventHandlers() {
        // modify the event handlers
        // attention: this.calendar is not available here!
        // ...

        return super.createEventHandlers();
    }
}

customElements.define("my-full-calendar", MyFullCalendar);
```

2. Create a subclass of FullCalendar

```java
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import org.vaadin.stefan.fullcalendar.FullCalendar;

@Tag("my-full-calendar")
@JsModule("./my-full-calendar.js")
public class MyFullCalendar extends FullCalendar {

    public MyFullCalendar() {
    }
}
```

3. Use this class in your code

```java
calendar = new MyFullCalendar();
```

You can even use the FullCalendarBuilder to create your custom class. Be aware, that your
custom class needs to provide all constructors, that the extended FullCalendar has.

```java
calendar = FullCalendarBuilder.create().withCustomType(MyFullCalendar.class).build();
```   

## Entry data utilities

### Handling data changes in events

When an event occurs, you may want to check or apply the event's changes against the related calendar item.

You may either do this manually or use the provided utility functions, that are part of the `EntryDataEvent` class
(and subclasses).

It might come in handy, that you get changes from an event in form of an `Entry` object instead of the different
changed values of the event itself.

To do so, you can simply call `createCopyBasedOnChanges()`, that the `EntryDataEvent` provides.
This will create complete copy of the entry but with the changes of the event applied. This copy will not be added to
the calendar and is simply intended for data checks.

To apply any incoming changes to the related `Entry`, the event class provides the method `applyChangesOnEntry()`.
This will override the entry with the event data, but not automatically update the client side. This is up to you
to do with an `refreshItem()` or `refreshAll()` call. Also any backend updates (except for the in memory provider)
needs to be handled by your logic.

```java
// directly apply the changes
calendar.addEntryDroppedListener(event -> {
    event.applyChangesOnEntry(); // includes now the allDay attribute if sent by client
});

// create a copy to do some business logic checks
calendar.addEntryDroppedListener(event -> {
    Entry copy = event.createCopyBasedOnChanges();

    if(copy.getStartAsLocalDate().isBefore(someRequiredMinimalDate) /* do some background checks on the changed data */) {
        event.applyChangesOnEntry();
        event.getSource().getEntryProvider().refreshItem(event.getEntry()); // refresh the entry to update the UI
    }
});
```

### Auto-revert unapplied changes

By default (`autoRevertUnappliedEntryChanges = true`), when `applyChangesOnEntry()` is **not** called in a
drop or resize listener, the calendar automatically reverts the entry to its original position on the client.
This keeps the client and server in sync without requiring explicit handling.

```java
// Validation example: reject drops before a certain date
calendar.addEntryDroppedListener(event -> {
    Entry copy = event.createCopyBasedOnChanges();
    if (copy.getStartAsLocalDate().isAfter(minDate)) {
        event.applyChangesOnEntry();
        entryProvider.refreshItem(event.getEntry());
    }
    // If validation fails: entry automatically reverts on the client
});
```

To disable auto-revert and keep the previous behavior (client keeps new position regardless):

```java
calendar.setAutoRevertUnappliedEntryChanges(false);
```

### Create a temporary copy

The `Entry` class provides a copy API, that allows you to create a copy of an entry or from a given entry. With this you
can easily create temporary instances for an edit dialog without needing to write a lot of "get-set" calls. This is
useful, when your binders work with the `setBean` api

Please be aware, that this api is considered "experimental" and might not work in every special use case or with every
custom property key.

```java
Entry tmpEntry = entry.copy(); // create a temporary copy
// you may also call copyAsType to allow the copy to be of a different type

Binder<Entry> binder = new Binder<>();

// ... init binder

binder.setBean(tmpEntry); // you can of course also use the read/writeBean api

// modify the bound fields

if (binder.validate().isOk()) {
    entry.copyFrom(tmpEntry); // this will overwrite the entry with the values of the tmpEntry
    // ... update the backend as needed, e.g. by calling refreshItem on the entry provider
}
```

Alternatively you can use the copy API in a JPA fashion, where new instances are created on changes.

```java
Entry tmpEntry = entry.copy(); // create a temporary copy

// ... modify the temporary copy

// return a new copy at the end without changing the initial entry
return tmpEntry.copy();
```

## RRule — RFC 5545 recurrence rules

Use `RRule` for recurrence patterns that the built-in `recurringDaysOfWeek` / `recurringStartTime` approach cannot express.
The `@fullcalendar/rrule` plugin is bundled automatically; no extra dependency is needed.

```java
// Weekly on Monday, Wednesday, Friday — all of 2025.
// dtstart uses LocalDate, so occurrences are all-day.
Entry standup = new Entry();
standup.setTitle("Weekly Standup");
standup.setRRule(RRule.weekly()
    .byWeekday(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
    .dtstart(LocalDate.of(2025, 1, 1))
    .until(LocalDate.of(2025, 12, 31)));

// Last Friday of each month
// byWeekday("-1fr") uses RFC 5545 notation: sign + ordinal + 2-letter day code.
// "-1fr" = last Friday; "1mo" = first Monday; "-2tu" = second-to-last Tuesday.
// Positive numbers count from the start of the period; negative from the end.
Entry review = new Entry();
review.setTitle("Monthly Review");
review.setRRule(RRule.monthly().byWeekday("-1fr"));

// Every two weeks on Tuesday (bi-weekly)
Entry planning = new Entry();
planning.setTitle("Bi-weekly Planning");
planning.setRRule(RRule.weekly().byWeekday(DayOfWeek.TUESDAY).interval(2)
    .dtstart(LocalDate.of(2025, 1, 7))
    .excludeDates(LocalDate.of(2025, 7, 22), LocalDate.of(2025, 12, 30)));

// Raw RFC 5545 string for unsupported patterns.
// Note: raw strings use uppercase RFC 5545 syntax (e.g. BYDAY=1MO), not
// the fluent API's lowercase notation (e.g. byWeekday("1mo")).
Entry custom = new Entry();
custom.setRRule(RRule.ofRaw("FREQ=MONTHLY;BYDAY=1MO,3MO;COUNT=12"));

calendar.getEntryProvider().asInMemory().addEntries(standup, review, planning, custom);
```

## Entry URL and keyboard accessibility

Entries with a URL are rendered as `<a>` tags and navigate the browser on click.
Entries marked `interactive` are keyboard-focusable (Tab navigation + Enter/Space activation).

```java
// URL entry — FC navigates to the URL when clicked
Entry link = new Entry();
link.setTitle("Visit Documentation");
link.setStart(LocalDate.now());
link.setAllDay(true);
link.setUrl("https://vaadin.com/docs");

// Keyboard-accessible entry (no URL, no drag — just focusable)
Entry keyboardEntry = new Entry();
keyboardEntry.setTitle("Press Enter to open wizard");
keyboardEntry.setStart(LocalDate.now().plusDays(1));
keyboardEntry.setAllDay(true);
keyboardEntry.setInteractive(true); // per-entry override

// Or make ALL entries keyboard-accessible globally:
calendar.setOption(FullCalendar.Option.ENTRY_INTERACTIVE, true);
// Then per-entry override is still possible (e.g., to opt out):
keyboardEntry.setInteractive(false);

calendar.addEntryClickedListener(e -> {
    // Fired for both mouse clicks and keyboard activations (Enter/Space)
    System.out.println("Clicked: " + e.getEntry().getTitle());
});
```

## Entry overlap control

Control whether entries may visually overlap. The `overlap` field is nullable: `null` means
"inherit the global `eventOverlap` setting".

```java
// This entry cannot be overlapped by other entries
Entry blocked = new Entry();
blocked.setTitle("Blocked Time");
blocked.setStart(LocalDateTime.of(2025, 3, 5, 9, 0));
blocked.setEnd(LocalDateTime.of(2025, 3, 5, 11, 0));
blocked.setOverlap(false);

// Explicitly allow overlap (overrides a global eventOverlap=false if set)
Entry flexible = new Entry();
flexible.setTitle("Flexible Slot");
flexible.setOverlap(true);

// null means "use whatever the calendar-level eventOverlap says" (the default)
Entry normal = new Entry();
normal.setOverlap(null);  // same as not calling setOverlap at all
```

## View-specific options

Apply an option only when a specific view type is active. Options set here override the global value
for that view; other views are unaffected.

```java
// Limit stacked events to 3 only in month view (not in week or day views)
calendar.setViewSpecificOption("dayGridMonth", FullCalendar.Option.DAY_MAX_EVENT_ROWS, 3);

// Custom slot duration for all time-grid variants
calendar.setViewSpecificOption("timeGrid", FullCalendar.Option.SLOT_DURATION, "00:30:00");

// Multiple options at once for the same view.
// Keys here are raw FullCalendar option names (camelCase strings), not Java Option enum values.
calendar.setViewSpecificOptions("listWeek", Map.of(
    "noEventsText", "No events scheduled this week"
));

// Use CalendarView enum instead of a raw string
calendar.setViewSpecificOption(CalendarViewImpl.DAY_GRID_MONTH,
    FullCalendar.Option.NOW_INDICATOR, true);
```

## Force calendar to recalculate its size

If a calendar is rendered inside a hidden container (e.g., a `Dialog`), it may have zero dimensions
on first render. A `ResizeObserver` on the client side handles most cases automatically, but in some
edge cases you may need to trigger a resize manually:

```java
Dialog dialog = new Dialog();
FullCalendar calendar = FullCalendarBuilder.create().build();
dialog.add(calendar);

dialog.addOpenedChangeListener(event -> {
    if (event.isOpened()) {
        // Force recalculation now that the dialog is visible
        calendar.getElement().callJsFunction("updateSize");
    }
});
```

## Accessibility hints for toolbar buttons

Set `aria-label` values on the prev/next/today toolbar buttons for screen-reader users.

```java
calendar.setOption(FullCalendar.Option.NATIVE_TOOLBAR_BUTTON_HINTS, Map.of(
    "today", "Jump to today",
    "prev",  "Go to previous period",
    "next",  "Go to next period"
));

// Accessible label for the "+N more" overflow link (e.g., "+ 3 more")
// $0 is replaced with the hidden-event count at runtime
calendar.setOption(Option.MORE_LINK_HINT, "$0 more events — click to expand");

// Accessible label for day-number navigation links
calendar.setOption(Option.NAV_LINK_HINT, "Go to $0");  // $0 = full date text
```

## Client-side event sources

Client-side event sources let the **browser** fetch events directly from an external URL — bypassing the Vaadin server.
This is useful when events come from third-party services (Google Calendar, iCal feeds, REST APIs).

Client-side sources coexist with the server-side `EntryProvider`. Entries from both appear on the same calendar.

### JSON feed

```java
// The browser will GET this URL whenever the visible date range changes.
JsonFeedEventSource jsonFeed = new JsonFeedEventSource("https://example.com/api/events");
jsonFeed.withId("company-events");
jsonFeed.withColor("#3788d8");
jsonFeed.withExtraParams(Map.of("department", "engineering"));  // appended as query params

calendar.addClientSideEventSource(jsonFeed);
```

### Google Calendar

```java
// Set the API key globally (applies to all Google sources on this calendar)
calendar.setOption(FullCalendar.Option.EXTERNAL_EVENT_SOURCE_GOOGLE_CALENDAR_API_KEY, "YOUR_GOOGLE_API_KEY");

GoogleCalendarEventSource holidays = new GoogleCalendarEventSource("en.german#holiday@group.v.calendar.google.com");
holidays.withId("holidays");
holidays.withColor("#4caf50");

calendar.addClientSideEventSource(holidays);
```

### iCalendar (ICS)

```java
ICalendarEventSource ical = new ICalendarEventSource("https://example.com/calendar.ics");
ical.withId("team-calendar");
ical.withColor("#ff9800");

calendar.addClientSideEventSource(ical);
```

### Error handling and per-source refresh

```java
// React to fetch failures on the server side
calendar.addEventSourceFailureListener(event -> {
    Notification.show("Failed to load source '" + event.getSourceId() + "': " + event.getMessage(),
            3000, Notification.Position.BOTTOM_START);
});

// Refresh only a specific source (e.g. after the user changed a filter)
calendar.refetchClientSideEventSource("company-events");

// Or refresh all sources at once
calendar.refetchEvents();
```

### Per-source options

```java
// Event sources support the same display options as entries
JsonFeedEventSource feed = new JsonFeedEventSource("https://example.com/api/events");
feed.withId("external");
feed.withEditable(false);          // entries from this source are read-only
feed.withColor("#888");
feed.withDisplay("background");    // render as background events
feed.withDefaultAllDay(true);

// Transform incoming event data before FC processes it
feed.withEventDataTransform(JsCallback.of("""
    function(eventData) {
        eventData.title = '[EXT] ' + eventData.title;
        return eventData;
    }"""));

calendar.addClientSideEventSource(feed);
```

## External drag and drop

Make external Vaadin components draggable onto the calendar using the `Draggable` API.

### Simple draggable with entry data

```java
// 1. Enable external drops on the calendar
calendar.setOption(Option.DROPPABLE, true);

// 2. Create a Vaadin component and an Entry with the data for the drop
Div meetingItem = new Div("New Meeting");
meetingItem.getStyle().set("cursor", "grab");

Entry meetingData = new Entry();
meetingData.setTitle("New Meeting");
meetingData.setColor("#4CAF50");

// 3. Register the draggable on the calendar
calendar.addDraggable(new Draggable(meetingItem, meetingData));

// 4. Listen for entry creation — fires when FC creates an entry from the drop
//    The entry is transient — add it to your provider to persist it
calendar.addEntryReceiveListener(event -> {
    Entry created = event.getEntry();
    calendar.getEntryProvider().asInMemory().addEntry(created);
    calendar.getEntryProvider().asInMemory().refreshAll();

    // Access the original Draggable if needed
    event.getDraggable().ifPresent(d -> {
        System.out.println("Dropped component: " + d.getComponent());
    });
});
```

The `DropEvent` (via `addDropListener`) fires for all external drops, including elements without
entry data. Use it when you need to react to any drop regardless of whether an entry was created.

### Container draggable with itemSelector

Make children of a container draggable using a CSS selector. A `JsCallback` dynamically
creates entry data from the dragged child element.

```java
// A container with multiple draggable children
Div taskList = new Div();
taskList.add(createTask("Write report"), createTask("Fix bug #42"), createTask("Review PR"));

// Only children matching ".task-item" are draggable.
// The JS callback reads the element's text to create the entry title.
calendar.addDraggable(new Draggable(taskList)
        .withItemSelector(".task-item")
        .withEventDataCallback(JsCallback.of(
                "function(el) { return { title: el.innerText, duration: '01:00' }; }")));

// Helper
private Div createTask(String name) {
    Div item = new Div(name);
    item.addClassName("task-item");
    item.getStyle().set("cursor", "grab");
    return item;
}
```

### Multi-calendar: entry leave listener

When entries can be dragged between calendars, listen on the source calendar for entries leaving:

```java
calendar.addEntryLeaveListener(event -> {
    Entry leaving = event.getEntry();
    calendar.getEntryProvider().asInMemory().removeEntry(leaving);
    calendar.getEntryProvider().asInMemory().refreshAll();
});
```

## Drag and resize lifecycle events

These events fire at the **start** and **end** of a drag or resize gesture — before the final
`EntryDroppedEvent` / `EntryResizedEvent`. Use them for visual feedback (e.g. disabling a delete button
during drag).

**Important:** Do NOT call `applyChangesOnEntry()` on these events — they report the *original* position,
not the final one. Use `EntryDroppedEvent` / `EntryResizedEvent` for applying changes.

```java
Button deleteButton = new Button("Delete");

// Disable the delete button while the user is dragging an entry
calendar.addEntryDragStartListener(event -> {
    deleteButton.setEnabled(false);
});
calendar.addEntryDragStopListener(event -> {
    deleteButton.setEnabled(true);
});

// Same pattern for resize
calendar.addEntryResizeStartListener(event -> {
    deleteButton.setEnabled(false);
});
calendar.addEntryResizeStopListener(event -> {
    deleteButton.setEnabled(true);
});

// Apply changes only in the final event
calendar.addEntryDroppedListener(event -> {
    event.applyChangesOnEntry();
    calendar.getEntryProvider().asInMemory().refreshItem(event.getEntry());
});
```

## Resource area columns (Scheduler)

Define multiple columns in the resource area of timeline views. Each column can display a different
resource property.

```java
Scheduler scheduler = (Scheduler) calendar;

// Define columns — the field name must match a key in Resource.extendedProps or a standard property
ResourceAreaColumn nameCol = new ResourceAreaColumn("title", "Name");
nameCol.withWidth("150px");

ResourceAreaColumn roleCol = new ResourceAreaColumn("role", "Role");
roleCol.withWidth("100px");

ResourceAreaColumn deptCol = new ResourceAreaColumn("department", "Dept");
deptCol.withWidth("80px");
deptCol.withGroup(true);  // group resources by this column's values

scheduler.setResourceAreaColumns(List.of(nameCol, roleCol, deptCol));

// IMPORTANT: grouping requires both withGroup(true) on the column AND setOption(SchedulerOption.RESOURCE_GROUP_FIELD, ...)
scheduler.setOption(SchedulerOption.RESOURCE_GROUP_FIELD, "department");

// Create resources with matching extendedProps
Resource dev1 = new Resource("1", "Alice", null);
dev1.addExtendedProps("role", "Developer");
dev1.addExtendedProps("department", "Engineering");

Resource dev2 = new Resource("2", "Bob", null);
dev2.addExtendedProps("role", "Designer");
dev2.addExtendedProps("department", "Design");

scheduler.addResources(dev1, dev2);
```

### Column render hooks

```java
// Customize how cells in a column are rendered — pass JsCallback for function values
ResourceAreaColumn statusCol = new ResourceAreaColumn("status", "Status");
statusCol.withCellContent(JsCallback.of("""
    function(arg) {
        var val = arg.resource.extendedProps.status || 'unknown';
        return { html: '<span class="status-' + val + '">' + val + '</span>' };
    }"""));
statusCol.withCellClassNames(JsCallback.of("""
    function(arg) {
        return arg.resource.extendedProps.status === 'active' ? ['active-cell'] : [];
    }"""));

// Static content is also supported — pass a plain String instead
statusCol.withCellContent("n/a");  // displayed as-is in every cell
```

## Entry constraints with BusinessHours

Restrict where a specific entry can be dragged or resized — independently of the calendar's
global business hours display.

```java
// This entry can only be placed during custom hours (Mon–Fri, 9–17)
Entry meeting = new Entry();
meeting.setTitle("Client Meeting");
meeting.setStart(LocalDateTime.of(2025, 3, 10, 10, 0));
meeting.setEnd(LocalDateTime.of(2025, 3, 10, 11, 0));
meeting.setConstraint(BusinessHours.businessWeek().start(9).end(17));

// This entry defers to the calendar's defined business hours
Entry standup = new Entry();
standup.setTitle("Standup");
standup.setConstraintToBusinessHours();

// This entry can only overlap with entries in the same group
Entry teamEvent = new Entry();
teamEvent.setGroupId("team-a");
teamEvent.setConstraint("team-a");  // only droppable where other "team-a" entries are
```

## Global event overlap control

Control whether events may overlap when dragged or resized. Per-entry `setOverlap()` overrides
the global setting.

```java
// Prevent all entries from overlapping each other
calendar.setOption(FullCalendar.Option.ENTRY_OVERLAP, false);

// Individual entries can still opt in
Entry flexible = new Entry();
flexible.setTitle("Flexible");
flexible.setOverlap(true);  // overrides the global false

// For more complex logic, use a JS callback
calendar.setOption(FullCalendar.Option.ENTRY_OVERLAP, JsCallback.of("""
    function(stillEvent, movingEvent) {
        // Allow overlap only with background events
        return stillEvent.display === 'background';
    }"""));
```

## Render hook callbacks

FullCalendar provides render hooks for almost every visual element: day cells, day headers, slot labels,
slot lanes, week numbers, now indicator, more-link, no-events, all-day, and view. All follow the same
pattern: `classNames` / `content` / `didMount` / `willUnmount`.

Use `setOption(Option, JsCallback.of(...))` with the appropriate `Option` constant to set these callbacks.
This is the recommended approach for all render hook callbacks.

### Highlight weekends in the day grid

```java
calendar.setOption(Option.DAY_CELL_CLASS_NAMES, JsCallback.of("""
    function(arg) {
        var dow = arg.date.getUTCDay();
        return (dow === 0 || dow === 6) ? ['weekend-cell'] : [];
    }"""));
```

Then in your CSS:
```css
.weekend-cell {
    background-color: rgba(255, 200, 200, 0.15);
}
```

### Custom slot labels in the time grid

```java
calendar.setOption(Option.SLOT_LABEL_CONTENT, JsCallback.of("""
    function(arg) {
        var h = arg.date.getUTCHours();
        if (h < 9 || h >= 17) return { html: '<span style="color:#999">' + arg.text + '</span>' };
        return arg.text;
    }"""));
```

### Custom day header with extra info

```java
calendar.setOption(Option.DAY_HEADER_CONTENT, JsCallback.of("""
    function(arg) {
        var d = arg.date;
        var dayName = d.toLocaleDateString('en', { weekday: 'short' });
        var dayNum = d.getDate();
        return { html: '<div>' + dayName + '<br><b>' + dayNum + '</b></div>' };
    }"""));
```

### Other render hooks

The same pattern applies to all other hooks. A few examples:

```java
// Week numbers — e.g. prefix with "CW "
calendar.setOption(Option.WEEK_NUMBER_CONTENT, JsCallback.of(
    "function(arg) { return { html: 'CW ' + arg.num }; }"));

// Now indicator — custom styling
calendar.setOption(Option.NOW_INDICATOR_CLASS_NAMES, JsCallback.of(
    "function() { return ['my-now-line']; }"));

// More-link — custom text
calendar.setOption(Option.MORE_LINK_CONTENT, JsCallback.of(
    "function(arg) { return { html: arg.num + ' more...' }; }"));

// No-events message in list view
calendar.setOption(Option.NO_ENTRIES_CONTENT, JsCallback.of(
    "function() { return { html: '<em>Nothing scheduled</em>' }; }"));
```

## Resource-level display overrides (Scheduler)

Resources can define default display properties for all entries assigned to them. The cascade is:
**Calendar defaults → Resource overrides → Entry overrides** (most specific wins).

```java
// Room A: all its entries are green by default
Resource roomA = new Resource("room-a", "Room A", null);
roomA.setEntryBackgroundColor("#4caf50");
roomA.setEntryBorderColor("#388e3c");

// Room B: entries cannot overlap each other on this resource
Resource roomB = new Resource("room-b", "Room B", null);
roomB.setEntryOverlap(false);
roomB.setEntryBackgroundColor("#2196f3");

// Room C: restrict entries to business hours on this resource
Resource roomC = new Resource("room-c", "Room C", null);
roomC.setEntryConstraint("businessHours");

// Add CSS classes to all entries on this resource
roomA.setEntryClassNames(Set.of("room-a-entry", "highlight"));

Scheduler scheduler = (Scheduler) calendar;
scheduler.addResources(roomA, roomB, roomC);
```

## JsCallback usage

`JsCallback` wraps a JavaScript function string so that FullCalendar evaluates it in the browser.
Pass a `JsCallback` value to `setOption` with any `Option` constant that accepts a function.
This works for render hooks, interaction guards, and any other FC option that expects a JS function.

### Render hook example — custom entry content with an icon

```java
// Use a render hook to prepend an icon based on a custom property
entry.setCustomProperty("category", "meeting");

calendar.setOption(Option.ENTRY_CONTENT, JsCallback.of("""
    function(info) {
        var cat = info.event.getCustomProperty('category', '');
        var icon = cat === 'meeting' ? '\u{1F4C5} ' : '';
        return { html: icon + '<b>' + info.event.title + '</b>' };
    }"""));
```

### Interaction guard example — conditional overlap via callback

```java
// Use a JsCallback to allow overlap only with background entries
calendar.setOption(Option.ENTRY_OVERLAP, JsCallback.of("""
    function(stillEvent, movingEvent) {
        return stillEvent.display === 'background';
    }"""));
```

### Clearing a callback

Use `JsCallback.clearCallback()` to explicitly clear a previously set callback:

```java
// Clear the entry-content callback
calendar.setOption(Option.ENTRY_CONTENT, JsCallback.clearCallback());
```

Alternatively, passing `null` directly or `JsCallback.of(null)` has the same effect — useful when
you have a nullable string variable:

```java
String userFn = ...; // may be null
calendar.setOption(Option.ENTRY_CONTENT, JsCallback.of(userFn));  // clears if userFn is null
```

## fixedMirrorParent — controlling the drag-mirror container

The `fixedMirrorParent` option controls where the drag-mirror element is appended during drag operations.
Set it via `Option.FIXED_MIRROR_PARENT` with a `JsCallback` that returns a DOM element.

### Simple case — document.body

```java
// The drag mirror will be appended to document.body
calendar.setOption(Option.FIXED_MIRROR_PARENT,
    JsCallback.of("function() { return document.body; }"));
```

### Dynamic lookup — function evaluated on each drag start

```java
calendar.setOption(Option.FIXED_MIRROR_PARENT, JsCallback.of("""
    function() {
        return document.querySelector('.my-drag-container');
    }"""));
```

To clear the option, pass `null`:

```java
calendar.setOption(Option.FIXED_MIRROR_PARENT, null);
```

## moreLinkClick callback

When the number of visible entries exceeds the row limit, FullCalendar shows a "+N more" link.
You can control what happens when the user clicks it.

### Static value — use Option or the convenience method

```java
// Show a popover with the hidden entries (default behaviour)
calendar.setMoreLinkClickAction(MoreLinkClickAction.POPUP);

// Or navigate to the day view
calendar.setOption(FullCalendar.Option.MORE_LINK_CLICK, "day");
```

### Function callback — use JsCallback

Use `Option.MORE_LINK_CLICK` with a `JsCallback` when you need custom logic, for instance logging or
conditionally choosing the action.

```java
calendar.setOption(Option.MORE_LINK_CLICK, JsCallback.of("""
    function(info) {
        console.log('More link clicked on ' + info.date);
        return 'day';
    }"""));
```

## Restricting navigation with valid range

Limit which dates the user can navigate to. Dates outside the range are greyed out and the
prev/next buttons are automatically disabled when the edge is reached.

```java
// Only allow navigation within the current year
calendar.setValidRange(
    LocalDate.of(2025, 1, 1),
    LocalDate.of(2025, 12, 31)
);

// Or set only one boundary
calendar.setValidRangeStart(LocalDate.now());  // no past navigation
calendar.setValidRangeEnd(null);               // no future limit
```
