# Creating a basic calendar instance and add an entry
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

calendar.add(entry);
```

# Add, update or remove a calendar entry
```java
// ... create a form and binder to provide editable components to the user

HorizontalLayout buttons = new HorizontalLayout();
Button buttonSave;
if (newInstance) {
    buttonSave = new Button("Create", e -> {
        if (binder.validate().isOk()) {
            // add the entry to the calendar instance
            calendar.addEntry(entry);
        }
    });
} else {
    buttonSave = new Button("Save", e -> {
        if (binder.validate().isOk()) {
             // update an existing entry in the client side
             // this will only send changed data
             calendar.updateEntry(entry);
        }
    });
}       
buttons.add(buttonSave);

if (!newInstance) {
    Button buttonRemove = new Button("Remove", e -> calendar.removeEntry(entry));
    buttons.add(buttonRemove);
}
```

# Basic calendar interaction
```java
/*
 * The day click event listener is called when a user clicks in an empty space inside of the 
 * calendar. Depending of if the clicked point was a day or time slot the event will provide the 
 * time details of the clicked point. With this info you can show a dialog to create a new entry.
 */
calendar.addTimeslotsSelectedListener((event) -> {
    // react on the selected timeslot, for instance create a new instance and let the user edit it 
    Entry entry = new Entry();

    entry.setStart(event.getStartDateTime()); // also event times are always utc based
    entry.setEnd(event.getEndDateTime());
    entry.setAllDay(event.isAllDay());

    entry.setColor("dodgerblue");

    // ... show and editor
});

/*
 * The entry click event listener is called when the user clicks on an existing entry. 
 * The event provides the clicked event which might be then opened in a dialog.
 */
calendar.addEntryClickListener(event -> /* ... */);
```

# Setting the calendar's dimensions
```java
// The embedded calendar has its own height calculation based on the container. That has to be
// respected, when emebedding the calendar. 
// Depending on the parent's "display" setting, it may differ how you want to set the calendar's height.

// Variant 1: parent is a flex-box container (e.g. VerticalLayout)
// Here we recommend to use setHeightByParent together with flex-box properties.
calendar.setHeightByParent(); // calculate the height by parent
calendar.getElement().getStyle().set("flex-grow", "1");

// if parent is for instance a vertical layout, you may also use the dedicated java api here
VerticalLayout parent = ...;
calendar.setHeightByParent();
parent.add(calendar);
parent.setFlexGrow(1, calendar);
parent.setHorizontalAlignment(Alignment.STRETCH);

// Variant 2: parent is a block container (e.g. normal Divs).
// you can set the height in different ways        
calendar.setHeight(500); // fixed pixel height
calendar.setHeightAuto(); // auto height
calendar.setHeightByParent(); // height by parent
        
calendar.setSizeFull(); // also set the size full to take all the content
```

# Using timezones
```java
// FC allows to show entries in a specifc timezone. Setting a timezone only affects the client side
// and might be interesting, when editing those entries in some kind of edit form

Timezone tzBerlinGermany = new Timezone("Europe/Berlin");
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
calendar.setTimezone(tzBerlinGermany) // times are now 1-2 hours "ahead" (depending on daylight saving)
entry.setStart(LocalDate.of(2000,1,1).atStartOfDay());

LocalDateTime utcStart = entry.getStart(); // will be 2000-01-01, 00:00
LocalDateTime offsetStart = entry.getStartWithOffset() // will be 2000-01-01, 01:00

// ... modify the offset start, for instance in a date picker
// e.g. modifiedOffsetStart = offsetStart.plusHours(5);

entry.setStartWithOffset(modifiedOffsetStart); // automatically takes care of conversion back to utc
LocalDateTime utcStart = entry.getStart(); // will be 2000-01-01, 04:00
LocalDateTime offsetStart = entry.getStartWithOffset() // will be 2000-01-01, 05:00
```

# Passing custom initial options in Java
You can fully customize the client side options in Java by passing a JsonObject when creating the FullCalendar.
Please be aware, that some options are always set, regardless of the values you set. Please check the
ApiDocs of the withInitialOptions method (or respective constructors) for details

The following example shows the default initial options as they are set internally by the web component.

```java
JsonObject initialOptions = Json.createObject();
initialOptions.put("height", "parent");
initialOptions.put("timeZone", "UTC");
initialOptions.put("header", false);
initialOptions.put("weekNumbers", true);
initialOptions.put("eventLimit", false); // pass an int value to limit the entries per day
initialOptions.put("navLinks", true); 
initialOptions.put("selectable", true);
calendar = FullCalendarBuilder.create().withScheduler().withInitialOptions(initialOptions).build();
```

# Style the calendar
## Modify the calendar's appearance by using css variables (deprecated with 4.x)
1. Copy the styles.css from the GitHub demo or create your own css file and place it in your
   applications frontend folder (e. g. frontend/styles/my-custom-full-calendar-styles.css)

An example file can be found from here:
https://github.com/stefanuebe/vaadin_fullcalendar/blob/master/demo/frontend/styles.css

Please be aware, that these custom properties are generated from the original styles. Since some dom elements
have classes, which are not used in the css files, there are no generated custom attributes for these class
combinations. In that case you'll have to subclass the Polymer class.
Also with version 4.x the css properties will not be maintained any longer.


2. Modify the styles as needed.
```css
html{
    /* light blue to be used instead of default light yellow*/
    --fc-unthemed_tdfc-today-background: #81DAF5 !important;
    
    /* and some fancy border */
    --fc_td-border-style: dotted !important;
    --fc_td-border-width: 2px !important;
}
```

3. Use the styles file in your application.
```java
@CssImport("./styles/my-custom-full-calendar-styles.css")
public class FullCalendarApplication extends ... {
    // ...
}
```

## Modifiy the calendar's appearance by using a custom polymer class.
Create a custom component, that extends FullCalendar or FullCalendarScheduler.
Override the static template method and reuse the parent's methods to create the basic styles.

The following example shows how a custom class extends the basic fc class and adds it's own styles. It will
set the background of the "today" cell to red.

Please note, that you also need a Java class using this Polymer class on the server side. The Scheduler is
working the same way, please have a look at its implementation for further details.

```javascript
import {html} from '@polymer/polymer/polymer-element.js';
import {FullCalendar} from 'full-calendar';

export class MyFullCalendar extends FullCalendar {
    static get template() {
        return html`
            ${this.templateCalendarCss} // defined in the parent class
            
            ${this.templateCustomCalendarCss} // defined in this class
        
            ${this.templateElementCss} // defined in the parent class
            ${this.templateContainer} // defined in the parent class
        `;
    }

    static get templateCustomCalendarCss() {
        return html`
        <style>
             .fc-unthemed td.fc-today {
               background: red;
             }
        </style>
        `;
    }
}

customElements.define('my-full-calendar', MyFullCalendar);
```

## Modify the calendar's appearance by adding a custom style element
You can add a style tag to the existing full calendar implementation to add custom styles.  
Please be advised, that this method can be used to introduce malicious code into your
page, so you should be sure, that the added css code is safe (e.g. not taken from user input or the databse).

```java
String customCss = "" +
    ".fc-today.fc-day {" + // marks today with red
    "   background-color: red !important;" +
    "}";
calendar.addCustomStyles(customCss);
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
        calendar.changeView(value == null ? CalendarViewImpl.MONTH : value);
    });
    viewBox.setValue(CalendarViewImpl.MONTH);

    /*
      * The view rendered listener is called when the view has been rendererd on client side 
      * and FC is aware of the current shown interval. Might be accessible more directly in 
      * future.
      */ 
    calendar.addDatesRenderedListener(event -> 
        LocalDate intervalStart = event.getIntervalStart();
        CalendarView cView = viewBox.getValue();

        String formattedInterval = ... // format the intervalStart based on cView 

        intervalLabel.setText(formattedInterval);
    });
}
```

# Customize the entry content
FC allows you to modify the content of an entry. The given string will be interpreted as js function on client side
and attached as `eventContent` callback. See https://fullcalendar.io/docs/content-injection ("...a function") for details.

```java
calendar.setEntryContentCallback("" +
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

Make sure, that your callback function does not contain any harmful code or allow cross side scripting. 

```java
// set the custom property beforehand
Entry someEntry = ...;
someEntry.setCustomProperty(EntryCustomProperties.DESCRIPTION, "some description");

// use the custom property
calendar.setEntryContentCallback("" +
	"function(info) {" +
        "   let entry = info.event;" +
        "   console.log(entry.title);" + // standard property
        "   console.log(entry.getCustomProperty('" +EntryCustomProperties.DESCRIPTION+ "'));" + // custom property
        "   /* ... do something with the event content ...*/" +
        "   return info.el; " +
        "}"
);
```

# Creating a subclass of FullCalendar for custom mods
1. Create a custom Polymer component
   Create a custom component, that extends FullCalendar or FullCalendarScheduler.

For changes on the appeareance, override the static template method and reuse the parent's methods
to create the basic styles and layout (see example for modifying FC's appearance for details).

For changes on the initial options see the following example.

```javascript
import {html} from '@polymer/polymer/polymer-element.js';
import {FullCalendar} from 'full-calendar';

export class MyFullCalendar extends FullCalendar {
    _createInitOptions() {
        var options = super._createInitOptions();
        options.eventContent = function (event, element) {
            element.css('color', 'red');
            return element;
        };
        return options;
    }
}

customElements.define('my-full-calendar', MyFullCalendar);
```

2. Create a subclass of FullCalendar

```java
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import org.vaadin.stefan.fullcalendar.FullCalendar;

@Tag("my-full-calendar")
@JsModule("./my-full-calendar.js")
public class MyFullCalendar extends FullCalendar {
    MyFullCalendar(int entryLimit) {
        super(entryLimit);
    }
}
```

3. Use this class in your code

```java
calendar = new MyFullCalendar(5);
```

# Creating a background event
```java
Entry entry = new Entry();
// ... setup entry details
        
entry.setRenderingMode(Entry.RenderingMode.BACKGROUND);
calendar.addEntry(entry);

# Adding business hours
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
This sample shows how to easy integrate tippy.js into a custom subclass of FullCalendar to show an entry's description
as a tooltip when hovering the entry inside the FC. Please customize the example as needed.

1. Create a new javascript file inside the frontend folder of your project. It needs to extend either FullCalendar or
   FullCalendarScheduler. This example utilized FullCalendarScheduler. If you want to use the normal FC, simply remove
   all the -Scheduler parts.

full-calendar-with-tooltips.js

```javascript
import {FullCalendarScheduler} from '@vaadin/flow-frontend/full-calendar-scheduler.js';
import tippy from 'tippy.js';

export class FullCalendarWithTooltip extends FullCalendarScheduler {
    static get is() {
        return 'full-calendar-with-tooltip';
    }

    _initCalendar() {
        super._initCalendar();
        this.getCalendar().setOption("eventDidMount", e => {
            this.initTooltip(e);
        });
    }

    initTooltip(e) {
        if (!e.isMirror) {
            e.el.addEventListener("mouseenter", () => {
                let tooltip = e.event.getCustomProperty("description");

                if(tooltip) {
                    e.el._tippy = tippy(e.el, {
                        theme: 'light',
                        content: tooltip,
                        trigger: 'manual'
                    });
    
                    e.el._tippy.show();
                }
            })

            e.el.addEventListener("mouseleave", () => {
                if (e.el._tippy) {
                    e.el._tippy.destroy();
                }
            })

        }
    }
}

customElements.define(FullCalendarWithTooltip.is, FullCalendarWithTooltip);
```

2. Now create a simple JavaClass, that utilizes your js file. This Java class also imports the needed CSS files.

```java
@Tag("full-calendar-with-tooltip")
@JsModule("./full-calendar-with-tooltip.js")
@CssImport("tippy.js/dist/tippy.css")
@CssImport("tippy.js/themes/light.css")
public class FullCalendarWithTooltip extends FullCalendarScheduler {

    public FullCalendarWithTooltip() {
        super(3);
    }
}
```

# Use the low level JsonItem API to modify a calendar item
In normal use cases you should use the provided high level api to access the `Entry`'s properties, e.g. 
`setTitle(String)` or `getStart()`.

But there might be scenarios, where you want to override the provided behavior for some reasons, e.g.
due to a bug or missing feature.

Subclasses of `JsonItem` (for instance `Entry` and `ResourceEntry`, there might be additional in future) provide
a  `set(Key, ...)` and a `get(Key)` method (and variants), which are also used internal. With these you may access
a property of a predefined `Key` or define your own using the `KeyBuilder`.

Let's say we've forgotten to create a high level api to set the "all day" property of the entry. You can simply create
your own key and either set/get the respective value on the Entry instance directly or extend it with
your own Entry class. 

```java
public class EntryWithAllDay extends EntryWithoutAllDay {
    private static final KEY_ALL_DAY = JsonItem.Key.builder()
           .name("allDay")
           .updateFromClientAllowed(true)
           .build();
    
    public boolean isAllDay() {
        return get(KEY_ALL_DAY, false); // alternatively we can use native based methods like getBoolean(Key)
    }

   public void setAllDay(boolean allDay) {
        set(KEY_ALL_DAY, allDay);
   }
}
```

That's all. You may provide additional information on the key, like a converter or some restrictions,
but for now that is all you need. The JsonItem will take care of converting the given property and its
value to json and back (if you want that). Also it takes care of only sending changed values to the
client when updating an existing item, to prevent unnecessary network overhead.

When allowed, events, that apply changes to your server side entries will also take the new property
into consideration, if it had changed.
```java
    // directly apply the changes
    calendar.addEntryDroppedListener(event -> {
        event.applyChangesOnEntry(); // includes now the allDay attribute if sent by client
    });

    // create a copy to do some business logic checks
    calendar.addEntryDroppedListener(event -> {
       Entry copy = event.createCopyBasedOnChanges();
       
       if(/* do some background checks on the changed data */){
            event.applyChangesOnEntry(); // includes now the allDay attribute if sent by client
       }
    });
```

