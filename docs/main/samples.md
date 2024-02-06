# Introduction
This page contains some samples to get you started with the FullCalendar addon. The samples are always based on the
latest version of the addon.

Some samples use an in-memory entry provider when modifiying the calendar data to keep things simple. You may
need to adapt those parts, if you use a different entry provider.

Also we tried to keep things short. So you may see variables like `calendar`, `entry` or `entryProvider`
without any declaration. In those cases these represent the basic types `FullCalendar`, `Entry` or `EntryProvider`.

If you find an outdated sample, please create an issue for that.

# Creating a basic calendar instance and add an entry

The FullCalendar is a normal Vaadin component, that can be added to your view as any other component. By default it uses
an eager loading in memory entry provider, with which you simply can add, update or remove calendar entries.

```java
// Create a new calendar instance and attach it to our layout
FullCalendar calendar = FullCalendarBuilder.create().build();
container.add(calendar);
container.setFlexGrow(1, calendar);

// Create a initial sample entry
Entry entry = new Entry();
entry.setTitle("Some event");
entry.setColor("#ff3333");

// the given times will be interpreted as utc based - useful when the times are fetched from your database
entry.setStart(LocalDate.now().withDayOfMonth(3).atTime(10, 0));
        entry.setEnd(entry.getStart().plusHours(2));

// FC uses a data provider concept similar to the Vaadin default's one, with some differences
// By default the FC uses a in-memory data provider, which is sufficient for most basic use cases.
        calendar.getEntryProvider().asInMemory().addEntries(entry);
```

# Add, update or remove a calendar entry

```java
InMemoryEntryProvider<Entry> entryProvider = calendar.getEntryProvider().asInMemory();

HorizontalLayout buttons = new HorizontalLayout();
Button buttonSave;
if (newInstance) {
buttonSave = new Button("Create", e -> {
        if (binder.validate().isOk()) {
        // add the entry to the calendar instance
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

# Calendar event handling
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

// ... show an editor
});

        /*
         * The entry click event listener is called when the user clicks on an existing entry.
         * The event provides the clicked event which might be then opened in a dialog.
         */
        calendar.addEntryClickedListener((event) -> {
// react on the clicked entry, for instance let the user edit it
Entry entry = event.getEntry();

// ... show an editor
});
```

# Entry providers

> Introduced in version 4.1

Entry providers allow you to minimize the memory footprint by activating lazy loading for calendar entries. The only
exception from that is the `EagerInMemoryEntryProvider`, which simulates the old behavior of the FullCalendar.

The following examples show the different types of `EntryProvider`s and how to use them. The eager variant is the way
to get rid of the deprecated API in the `FullCalendar`.

## In memory entry provider

The `InMemoryEntryProvider` caches all registered entries on the server side, but provides only a subset of them to
the client (i. e. the entries of the current shown period). This way you can use the CRUD API on the server side
without the need of implementing it yourself. On the other hand the client will be kept free of unnecessary information.

```java
// load items from backend
List<Entry> entryList = backend.streamEntries().collect(Collectors.toList());

// init lazy loading provider based on given collection - does NOT use the collection as backend as ListDataProvider does
LazyInMemoryEntryProvider<Entry> entryProvider = EntryProvider.lazyInMemoryFromItems(entryList);

// set entry provider
calendar.setEntryProvider(entryProvider);

// CRUD operations
// to add
Entry entry = new Entry();       // ... plus some init
entryProvider.addEntries(entry); // register in data provider
entryProvider.refreshAll();         // call refresh to inform the client about the data change and trigger a refetch

// after some change
entryProvider.refreshItem(entry); // call refresh to inform the client about the data change and trigger a refetch

// to remove
entryProvider.removeEntry(entry);
entryProvider.refreshAll(); // call refresh to inform the client about the data change and trigger a refetch
```

## Using callbacks

The callback entry provider is a base implementation of the `EntryProvider` interface. It does care about how the
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
entryProvider.refreshAll();   // call refresh to inform the client about the 
```

## Custom implementation

Feel free to create your own custom implementation, for instance to provide advanced internal caching on the server
side. We recommend to extend the `AbstractEntryProvider` to start with.

The simples variant is similar to the callback variant, but with its own class:

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

## Activate prefetch mode (experimental)

By default the entry provider will always fetch data for the current period. Switching to an adjacent one can
lead to flickering, since the calendar will resize itself due to the shown entries.

To prevent that flickering, the calendar can be set to "prefetch mode". When activated, the calendar will always
fetch the previous and next period together with the current one. This means, that on switching to an adjacent
period will initially show the previously fetched entries and update those with the latest state from the server.
This leads to an increased amount of data transported between server and client, but will lead to a better
user experience as the above mentioned flickering will be prevented.

The prefetch mode tries to obtain the necessary period based on the current view range unit. The supported units
are "day", "month" and "year". If no range unit could be determined, a warning will show up. If the range unit
is not supported, prefetch will not work (in this case the calendar behaves as if prefetch has been disabled).

Please note, that this functionality is still experimental, but should work fine in most use cases. Thus ee recommend
to activate it to improve the user experience.

```java
calendar.setPrefetchEnabled(true);
```

# Setting the calendar's dimensions

You may set the dimensions as with every other Vaadin component. The FC library also brings in some additional
settings for content height or an aspect ratio, that can be taken into account. These can be set
via the Options API.

See https://fullcalendar.io/docs/sizing for details.

# Using timezones

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
        entry.setEnd(LocalDateTime.now()); // UTC

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

# Passing custom initial options in Java

You can fully customize the client side options in Java by passing a JsonObject when creating the FullCalendar.
Please be aware, that some options are always set, regardless of the values you set. Please check the
ApiDocs of the withInitialOptions method (or respective constructors) for details

The following example shows the default initial options as they are set internally by the web component.

```java
JsonObject initialOptions = Json.createObject();
initialOptions.put("height", "100%");
initialOptions.put("timeZone", "UTC");
initialOptions.put("header", false);
initialOptions.put("weekNumbers", true);
initialOptions.put("eventLimit", false); // pass an int value to limit the entries per day
initialOptions.put("navLinks", true); 
initialOptions.put("selectable", true);
calendar = FullCalendarBuilder.create().withScheduler().withInitialOptions(initialOptions).build();
```

# Style the calendar

## Lumo Theme
Since 6.1 the calendar provides a built-in Lumo theme, that changes minor parts of it to use Lumo variables, like
sizes, colors, etc. At this point, the theme is not applied automatically, but you have to opt-in to use it.

Please also be aware, that with 6.1, it still is considered experimental, so there might be parts, that have been
forgotten or not looking as expected. Also any additional custom stylings may override the Lumo stylings.

To activate the Lumo theme, simply add the respective theme variant, as you would do with other Vaadin components.

```java
calendar.addThemeVariant(FullCalendarVariant.LUMO);
```

## Global styles

Since 6.0 the component is part of the light dom and thus can be styles via plain css. For any details
on styling the FC please refer to the FC docs (regarding css properties, classes, etc.).

The styles can be defined as you are used to using the Vaadin theme mechanism or `@CssImport`s.

Sample styles.css

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

# Show the current shown time interval (e. g. month) with Vaadin components

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

# Creating a background entry
A background entry is an entry, that is rendered behind all other entries. It is not clickable and
has no tooltip. It is useful for marking a time range, e. g. for marking a vacation.

```java
Entry entry = new Entry();
// ... setup entry details
        
entry.setDisplayMode(DisplayMode.BACKGROUND);
calendar.addEntry(entry);
```

# Adding business hours
You can define business hours for each day of the week. If you don't define any business hours, the calendar will
assume, that the business hours are from 0:00 to 24:00 for each day.

Non-business hours are grayed out in the calendar.

```java
// Single instance for "normal" business week (mo-fr)
calendar.setBusinessHours(new BusinessHours(LocalTime.of(9, 0), LocalTime.of(17, 0),BusinessHours.DEFAULT_BUSINESS_WEEK));

// Multiple instances
        calendar.setBusinessHours(
new BusinessHours(LocalTime.of(9, 0), LocalTime.of(17, 0),BusinessHours.DEFAULT_BUSINESS_WEEK),
        new BusinessHours(LocalTime.of(12, 0), LocalTime.of(15, 0), DayOfWeek.SATURDAY)
        );

// Single instance for "each day from 9am to midnight"
        calendar.setBusinessHours(new BusinessHours(LocalTime.of(9, 0)));
```

# Using tippy.js for description tooltips
By default the calendar does not provide tooltips for entries. However, you can easily integrate any type of
tooltip mechanism or library, for instance by simply applying an html title or using a matured tooltip library
like tippy.js.

This sample shows how to easy integrate tippy.js into a custom subclass of FullCalendar to show an entry's description
as a tooltip when hovering the entry inside the FC. Please customize the example as needed.

1. Create a new TypeScript file inside the frontend folder of your project. It needs to extend either FullCalendar or
   FullCalendarScheduler. This example utilized FullCalendarScheduler. If you want to use the normal FC, simply remove
   all the -Scheduler parts. You may also have a simply JavaScript file, in that case you need to change the
   following script a bit.

full-calendar-with-tooltips.ts

```typescript
import {FullCalendarScheduler} from '@vaadin/flow-frontend/vaadin-full-calendar/full-calendar-scheduler';
import tippy from 'tippy.js';


export class FullCalendarWithTooltip extends FullCalendarScheduler {
   initCalendar() {
      super.initCalendar();

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

# Customize the entry content

FC allows you to modify the content of an entry. The given string will be interpreted as js function on client side
and attached as `eventContent` callback. See https://fullcalendar.io/docs/content-injection ("...a function") for
details.

```java
calendar.setEntryDidMountCallback(
   "function(info) {" +
           "   info.el.style.color = 'red';" +
           "   return info.el; " +
           "}"
);

```

Inside the javascript callback you may access the entry's default properties or custom ones, that
you can set beforehand, using the custom property api (e. g.`setCustomProperty(String, Object)`).
In the callback you can access the custom property in a similar way, using `getCustomProperty(key)`
or `getCustomProperty(key, defaultValue)`.

Please be aware, that the entry content callback has to be set before the client side is attached. Setting it afterwards
has no effect.

Also make sure, that your callback function does not contain any harmful code or allow cross side scripting.

```java
// set the custom property beforehand
entry.setCustomProperty(Entry.EntryCustomProperties.DESCRIPTION, "some description");

// use the custom property
calendar = FullCalendarBuilder.create()
        .withEntryContent(
                "function(info) {" +
                        "   let entry = info.event;" +
                        "   console.log(entry.title);" + // standard property
                        "   console.log(entry.getCustomProperty('" + Entry.EntryCustomProperties.DESCRIPTION+ "'));" + // custom property
                        "   /* ... do something with the event content ...*/" +
                        "   return info.el; " +
                        "}"
)
// ... other settings
        .build();

// or use the custom property in the entryDidMountCallback
```

# Use native javascript events for entries
With 6.2 custom native event handlers for entries have been added. These allow you to setup JavaScript events for
each entry, e.g. a mouse over event handler. Inside these event handlers you may also access the created entry dom
element.

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

Please be aware, that due to the design of the used library, these event handlers have to be setup before the
calendar is initialized on the client side.

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
public void MyCalendarView extends VerticalLayout {
    public MyCalendarView() {

      FullCalendar calender = new FullCalendar();
      // adds a contextmenu / right client event listener, that calls our openContextMenu. 
      // "this" is the fc object, "this.el" is the Flow element and "this.el.parentElement" is our current view.
      // This hierarchy access may changed, when you nest the FC into other containers. 

      calendar.addEntryNativeEventListener("contextmenu",
              "e => this.el.parentElement.$server.openContextMenu(info.event, e.clientX, e.clientY)");

      add(calender);
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
of the entries. The FC will take care of combining the event handlers and you EDM callback
```java
calendar.setEntryDidMountCallback("""
       function(info) {
           console.warn("my custom callback");
       }""");

calendar.addEntryNativeEventListener("mouseover", "e => info.el.style.opacity = '0.5'");
calendar.addEntryNativeEventListener("mouseout", "e => info.el.style.opacity = ''");
```

The following sample shows how to utilize the entryDidMount callback, the native event handlers and the
[Popup addon](https://vaadin.com/directory/component/popup) to show a context menu. In this sample, the context
menu is based on a ListBox.

```java
@Route(...)
public void MyCalendarView extends VerticalLayout {
    
    private Popup popup;
   
    public MyCalendarView() {

        FullCalendar calender = new FullCalendar();
        // adds a contextmenu / right client event listener, that calls our openContextMenu. 
        // "this" is the fc object, "this.el" is the Flow element and "this.el.parentElement" is our current view.
        // This hierarchy access may changed, when you nest the FC into other containers. 

        calendar.addEntryNativeEventListener("contextmenu",
                "e => {" +
                "   e.preventDefault(); " +
                "   this.el.parentElement.$server.openContextMenu(info.event.id);" +
                "}");

        // by default, the entry element has no id attribute. Therefore we have to add it ourselves, using the 
        // entry id, that is by default an auto generated UUID
        calendar.setEntryDidMountCallback("""
                function(info) {
                    info.el.id = "entry-" + info.event.id;
                }""");

    }

    @ClientCallable
    public void openContextMenu (String id){
        initPopup(); // init the popp

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

# Creating a subclass of FullCalendar for custom mods
Since version 6.0.0, the client side component is based on a simple HTMLElement and thus there is
no framework lifecycle to hook into. The main entry points for your components are therefore:
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
import {FullCalendar} from '@vaadin/flow-frontend/vaadin-full-calendar/full-calendar';

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
      // modify the initial options
      // attention: this.calendar is not available here!
      // ...

      return initialOptions;
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

You can even use the FullCalendarBuilder to create your custom class. Be aware, that in the current version your
custom class needs to provide all constructors, that the extended FC class has. This will be fixed in future versions.

```java
calendar = FullCalendarBuilder.create().withCustomType(MyFullCalendar.class).build();
```   


# Entry data utilities

## Handling data changes in events

When an event occurs, you may want to check or apply the event's changes against the related calendar item.

You may either do this manually or use the provided utility functions, that are part of the `EntryDataEvent` classe
(and subclasses).

It might come in handy, that you get changes from an event in form of an `Entry` object instead of the different
changed values of the event itself.

To do so, you can simply call the method `createCopyBasedOnChanges()` method, that the `EntryDataEvent` provides.
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

    if(copy.getStartAsLocalDate().isBefore(someRequiredMinimalDate) /* do some background checks on the changed data */){
        event.applyChangesOnEntry();
        event.getSource().getEntryProvider().refreshItem(event.getEntry()); // refresh the entry to update the UI
        }
        });
```

## Create a temporary copy

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