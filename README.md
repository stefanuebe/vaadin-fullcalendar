# FullCalendar integration
This addon is an integration of the FullCalendar (v3.9.0) as Flow component for Vaadin Platform / Vaadin 10+. 

For information about the FullCalendar (functionality, features, license information, etc.) visit https://fullcalendar.io/

If you want to use the Scheduler, please have a look here: https://vaadin.com/directory/component/full-calendar-scheduler-extension/samples

## Addon Functionality
The following functions are implemented and available to use from server side:
- adding / updating / removing calendar items,
- switching between shown intervals (next month, previous month, etc.),
- goto a specific date or today,
- switch the calendar view (month, basic views for days and weeks, agenda views for days and weeks, list views for day to year),
- setting a locale to be used for displaying week days, formatting values, calculating the first day of the week, etc. (supported locales are provided as constant list)
- setting the first day of week to be shown (overrides the locale setting),
- setting the height of the calendar instance (calculated by parent, aspect ratio or fixed pixel size)
- show of week numbers
- limit max shown entries per day (except basic views)
- showing now indicator
- activating day / week numbers / names to be links
- styles are overridable via custom properties

- Event handling for
    - clicking an empty time spot in the calendar,
    - selecting a block of empty time spots in the calendar, 
    - clicking an entry,
    - moving an entry via drag and drop (event is fired on drop + changed time),
    - resizing an entry (event is fired after resize + changed time),
    - view rendered (i. e. to update a label of the shown interval)
    - clicking on limited entries link "+ X more"
    - clicking on a day's or week's number link (when activated)
    
- Model supports setting 
    - title, 
    - start / end / all day flag, 
    - color (html colors, like "#f00" or "red"), 
    - description (not shown via FC), 
    - editable / read only

## Feedback and co.
If there are bugs or you need more features (and I'm not fast enough) feel free to contribute on GitHub. :)
I'm also happy for feedback or suggestions about improvements.

## Examples
### Creating a basic instance and add an entry
```
// Create a new calendar instance and attach it to our layout
FullCalendar calendar = FullCalendarBuilder.create().build();
container.add(calendar);
container.setFlexGrow(1, calendar);

// Create a initial sample entry
Entry entry = new Entry();
entry.setTitle("Some event");
entry.setStart(LocalDate.now().withDayOfMonth(3).atTime(10, 0));
entry.setEnd(entry.getStart().plusHours(2));
entry.setColor("#ff3333");

calendar.add(entry);
```

### Show a dialog to create new entries or modify existing ones
```
/*
 * The day click event listener is called when a user clicks in an empty space inside of the 
 * calendar. Depending of if the clicked point was a day or time slot the event will provide the 
 * time details of the clicked point. With this info you can show a dialog to create a new entry.
 */
calendar.addTimeslotClickedListener(event -> {
        Entry entry = new Entry();
        
        LocalDateTime start = event.getClickedDateTime();
        entry.setStart(start);
        
        boolean allDay = event.isAllDay();
        entry.setAllDay(allDay);
        entry.setEnd(allDay ? start.plusDays(FullCalendar.DEFAULT_DAY_EVENT_DURATION) : start.plusHours(FullCalendar.DEFAULT_TIMED_EVENT_DURATION));
        
        entry.setColor("dodgerblue");
        
        // ... open a dialog or other view to edit details 
    });

/*
 * The entry click event listener is called when the user clicks on an existing entry. 
 * The event provides the clicked event which might be then opened in a dialog.
 */
calendar.addEntryClickListener(event -> 
    new DemoDialog(calendar, event.getEntry(), false).open());
```

### Add, update or remove an entry
```
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

### Show the current shown interval (e. g. month)
```
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
    calendar.addViewRenderedListener(event -> 
        LocalDate intervalStart = event.getIntervalStart();
        CalendarView cView = viewBox.getValue();

        String formattedInterval = ... // format the intervalStart based on cView 

        intervalLabel.setText(formattedInterval);
    });
}
```

### Example combinations of parent and calendar height settings.
```
// #1 setting a fixed height
calendar.setHeight(500);

// #2 setting a auto height - this is calculated by the w-h-ratio of the calendar
calendar.setHeightAuto();

// #3 calculate height by parent. parent is a block container.
calendar.setHeightByParent();
calendar.setSizeFull();

// #4 calculate height by parent + usage of css calc(). parent is a block container.
calendar.setHeightByParent();
calendar.getElement().getStyle().set("height", "calc(100vh - 450px)");

// #5 calculate height by parent. parent is a flex container.
calendar.setHeightByParent();
calendar.getElement().getStyle().set("flex-grow", "1");
```

### Using custom styles to modify FCs appearance
1. Copy the styles.html from the github demo or create your own custom style file and place it in your applications webapp/frontend folder (e. g. webapp/frontend/styles/styles/my-custom-full-calendar-styles.html)

The github demo file can be obtained from here:
https://github.com/stefanuebe/vaadin_fullcalendar/blob/master/demo/src/main/webapp/frontend/styles.html


2. Modify the styles as needed.

```
<custom-style>
    <style>
        html{
            /* light blue to be used instead of default light yellow*/
            --fc-unthemed_tdfc-today-background: #81DAF5 !important;
            
            /* and some fancy border */
            --fc_td-border-style: dotted !important;
            --fc_td-border-width: 2px !important;
        }
    </style>
</custom-style>
```

3. Use the styles file in your application.
```
@HtmlImport("frontend://styles/full-calendar-styles.html")
public class FullCalendarApplication extends Div {
    // ...
}
```

# FullCalendar Scheduler extension
This addon extends the **FullCalendar integration addon** with the FullCalendar Scheduler (v1.9.4) as Flow component for Vaadin Platform / Vaadin 10+.
It needs the FC integration addon ((1.3.0+) as basis (https://vaadin.com/directory/component/edit/full-calendar-web-component).

For information about the Schedular (functionality, features, license information, etc.) 
visit https://fullcalendar.io/scheduler. 

## License information:
Please be aware, that the Scheduler has a different license model then the FullCalendar.
For details of when to use which license, visit https://fullcalendar.io/scheduler/license.

**This addon does not provide any commercial license for the Scheduler. The license model of MIT does only affect
the additional files of this addon, not the used original files.** 

## Activating the Scheduler
By default the scheduler is not active, when you use a FullCalendar instance. To have an instance with scheduler
activated, use the `withScheduler()` method of the `FullCalendarBuilder`. 

This method will throw an exception, if
the scheduler extension is not on the class path.

## Additional Features of the Scheduler extension
- Activation of the Scheduler by method in the FullCalendarBuilder.
- Adding resources to a calendar (hierarchies of resources are not yet supported). 
- Link one or multiple resources with entries (`ResourceEntry`).
- List of possible Scheduler based views (timeline).

*Info:* Entries are linked to calendar internally. The calendar instance is used to resolve resources by after updating an
entry on the client side.

## Feedback and co.
If there are bugs or you need more features (and I'm not fast enough) feel free to contribute on GitHub. :)
I'm also happy for feedback or suggestions about improvements.

## Examples
### Activating the scheduler
```
FullCalendar calendar = FullCalendarBuilder.create().withScheduler().build();

// scheduler options
((Scheduler) calendar).setSchedulerLicenseKey(...);
```

### Adding a resource to a calendar and link it with entries
```
Resource resource = new Resource(null, s, color);
calendar.addResource(resource);

ResourceEntry entry = new ResourceEntry(null, title, start.atStartOfDay(), start.plusDays(days).atStartOfDay(), true, true, color, "Some description...");
entry.setResource(resource);
calendar.addEntry(entry);
```

### Handling change of an entry's assigned resource by drag and drop
```
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
```
calendar.changeView(SchedulerView.TIMELINE_DAY);
```

### Activate vertical resource view 
```
// currently done by plain options, will provide a better API later

// Switch to a non timeline view, then use one of these cases

// Case 1 Deactivate grouping
calendar.setOption("groupByResource", false);
calendar.setOption("groupByDateAndResource", false);
                    
// Case 2 Activate grouping by resource / date                    
calendar.setOption("groupByResource", true);
calendar.setOption("groupByDateAndResource", false);

// Case 3 Activate grouping by date / resoute
calendar.setOption("groupByResource", false);
calendar.setOption("groupByDateAndResource", true);
```

### Using custom styles to modify FCs appearance
1. Copy the styles_scheduler.html from the github demo or create your own custom style file and place it in your applications webapp/frontend folder (e. g. webapp/frontend/styles/styles/my-custom-full-calendar-styles.html)

The github demo file can be obtained from here:
https://github.com/stefanuebe/vaadin_fullcalendar/blob/master/demo/src/main/webapp/frontend/styles_scheduler.html


2. Modify the styles as needed.

```
<custom-style>
    <style>
        html{
               --fc-timeline_fc-divider-border-style: dashed;
               --fc-timeline_fc-divider-width: 2px;
        }
    </style>
</custom-style>
```

3. Use the styles file in your application.
```
@HtmlImport("frontend://styles/full-calendar-styles-scheduler.html")
public class FullCalendarApplication extends Div {
    // ...
}
```