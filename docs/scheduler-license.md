# Scheduler license
The FullCalendar Scheduler library has a different license model then the basic FullCalendar.

For more details please visit https://fullcalendar.io/license and https://fullcalendar.io/docs/plugin-index

**This addon does not provide any commercial license for the Scheduler. The license model of MIT does only affect the additional files of this addon, not the used original files.**

# Activating the Scheduler - [Example](https://github.com/stefanuebe/vaadin_fullcalendar/wiki/FullCalendar-Scheduler-Examples#activating-the-scheduler)

By default the scheduler is not active, when you use a FullCalendar instance. To have an instance with scheduler activated, use the `.withScheduler(...)` method of the `FullCalendarBuilder`.

This method will throw an exception, if the scheduler extension is not on the class path.

To link a resource with entries, use the Entry subclass `ResourceEntry`.