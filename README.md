# FullCalendar integration
This addon is an integration of the FullCalendar as Flow component for Vaadin Platform / Vaadin 10+. The component is currently in an early alpha. 

For information about the FullCalendar (functionality, features, license information, etc.) visit https://fullcalendar.io/

## Addon Functionality
The following functions are implemented and available to use from server side:
* adding / updating / removing calendar items,
* switching between shown intervals (next month, previous month, etc.),
* goto a specific date or today,
* switch the calendar view (month, basic views for days and weeks, agenda views for days and weeks, list views for day to year),
* setting a locale to be used for displaying week days, formatting values, calculating the first day of the week, etc. (supported locales are provided as constant list)
* setting the first day of week to be shown (overrides the locale setting),
* setting the height of the calendar instance (calculated by parent, aspect ratio or fixed pixel size)
* show of week numbers
* limit max shown entries per day (except basic views)
* showing now indicator
* activating day / week numbers / names to be links, that forward to another, specific details view

* Event handling for
    * clicking an empty time spot in the calendar,
    * selecting a block of empty time spots in the calendar, 
    * clicking an entry,
    * moving an entry via drag and drop (event is fired on drop + changed time),
    * resizing an entry (event is fired after resize + changed time),
    * view rendered (i. e. to update a label of the shown interval)
    * clicking on limited entries link "+ X more"
    
* Model supports setting 
    * title, 
    * start / end / all day flag, 
    * color (html colors, like "#f00" or "red"), 
    * description (not shown via FC), 
    * editable / read only

## Feedback and co.
If there are bugs or you need more features (and I'm not fast enough) feel free to contribute on GitHub. :)
I'm also happy for feedback or suggestions about improvements.

# Examples
## Creating a basic instance and add an entry
```
// Create a new calendar instance and attach it to our layout
FullCalendar calendar = new FullCalendar();
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

## Show a dialog to create new entries or modify existing ones
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

## Add, update or remove an entry
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

## Show the current shown interval (e. g. month)
private void init() {
    // The element that should show the current interval. 
    HasText intervalLabel = new Span();

    // combo box to select a view for the calendar, like "monthly", "weekly", ...
    ComboBox<CalendarView> viewBox = new ComboBox<>("", CalendarView.values());
    viewBox.addValueChangeListener(e -> {
        CalendarView value = e.getValue();
        calendar.changeView(value == null ? CalendarView.MONTH : value);
    });
    viewBox.setValue(CalendarView.MONTH);

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

## Example combinations of parent and calendar height settings.
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
