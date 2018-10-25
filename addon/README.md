# vaadin_fullcalendar
Integration of FullCalendar as Flow component for Vaadin Platform / Vaadin 10+. The component is currently in an early alpha. 

## Functionality
The following functions are available to use from server side:
* adding / updating / removing calendar items,
* switching between shown intervals (next month, previous month, etc.),
* goto a specific date or today,
* switch the calendar view (month, basic / agenda day, basic / agenda week),
* setting the first day of week to be shown,

* Event handling for
    * clicking an empty time spot in the calendar,
    * clicking an entry,
    * moving an entry via drag and drop (event is fired on drop + changed time),
    * resizing an entry (event is fired after resize + changed time),
    
* Model supports setting 
    * title, 
    * start / end / all day flag, 
    * color (html colors, like "#f00" or "red"), 
    * description (not shown via FC), 
    * editable / read only

## Feedback and co.
If there are bugs or you need more features (and I'm not fast enough) feel free to contribute on GitHub. :)
I'm also happy for feedback or suggestions about improvements.

##About FullCalendar
For a full overview of what FullCalendar is capable of have a look at https://fullcalendar.io/ 
