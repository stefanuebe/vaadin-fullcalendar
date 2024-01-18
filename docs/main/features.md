This addon uses a 3rd party library called FullCalendar. We try to integrate all features the library provides. Therefore
the following list might not include all available features, but should list the most important ones. If you find
a feature in the FC library, that this addon not yet supports, please create an feature request in the issues page.

Supported features are
- adding / updating / removing calendar entries using a data provider like mechanism,
- switching between shown intervals (next month, previous month, etc.),
- goto a specific date or today,
- switch the calendar view (month, basic views for days and weeks, agenda views for days and weeks, list views for day to year),
- setting a locale to be used for displaying week days, formatting values, calculating the first day of the week, etc. (supported locales are provided as constant list)
- setting the first day of week to be shown (overrides the locale setting),
- limit max shown entries per day (except basic views)
- activating day / week numbers / names to be links
- setting a eventRender JS function from server side
- setting business hours information (multiple entries possible)
- creating recurring events
- setting a client side timezone
- optional Lumo theme

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
    - rendering mode (normal, background, inversed background)
    - recurring data (day of week, start / end date and time)
    - etc.