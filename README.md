# FullCalendar for Flow
This addon is an integration of the [FullCalendar](https://fullcalendar.io/) as a Flow component. It is based on version 6 of the
FullCalendar library.

## Requirements / versions / support
There are several versions of the addon
* Version 7, based on Vaadin 25 / Java 21
* Version 6.4, based on Vaadin 24.9 / Java 17
* Version 6.3 and before, based on Vaadin 14 / Java 11, which should also support any version up to Vaadin 24 (not supported anymore)

Addon versions prior to 6.4 and 7.1 are not supported. If you need a fix for such a version
feel free to fork the project. You may create an issue, but due to limited time it is very unlikely, that we will fix
it.

We also made the decision to explicitly introduce a breaking change with version 6.4.0. On one hand, we normally support
only the latest major version of Vaadin, but also know on the other hand, that migrating from 24 to 25 might not be possible
at the moment. Therefore we decided to backport all the changes from 7.1.x to 6.4.x as long as it is suitable.

Please also have a look at the demo for some basic examples and source code of how to integrate the FC.
For more examples please have a look into the example section.

## FullCalendar Scheduler for Flow addon
This separate addon extends the **FullCalendar for Flow** addon with the FullCalendar Scheduler, which provides
additional resource based views (Timeline View and Vertical Resource View).

For information about the Scheduler (functionality, features, license information, etc.)
visit https://fullcalendar.io/scheduler.

### License information:
Please be aware, that the FullCalender Scheduler library this addon is based on has a different license model
then the basic FullCalendar. For details about the license, visit https://fullcalendar.io/license.

**The scheduler addon does not provide any commercial license for the Scheduler. The license model of MIT does only affect
the additional files of this addon, not the used original files.**

## Documentation and Samples
Please visit out [github wiki](https://github.com/stefanuebe/vaadin-fullcalendar/wiki) for samples and other information about this addon.

## Authors of this addon
This addon is currently maintained by Carlo Zanocco and me. If you have any questions or issues, please
use the GitHub repository issues page.

## Vaadin directory
* Fullcalendar: https://vaadin.com/directory/component/full-calendar-flow
* Fullcalendar Scheduler: https://vaadin.com/directory/component/full-calendar-scheduler-flow/overview

## AI Assistant Integration (MCP Server) — removed
The FullCalendar Vaadin MCP server has been **discontinued** and its sources were removed from this
repository. It was a Node.js service that only re-served the wiki content, and keeping its npm
dependency tree free of security advisories cost more than the convenience was worth.

Point your AI assistant at the [wiki](https://github.com/stefanuebe/vaadin-fullcalendar/wiki)
instead — it is and always was the single source of truth.

## Additional links and information
* Homepage about the FullCalendar (functionality, features, license information, etc.) visit https://fullcalendar.io/
* GitHub profile of Carlo: https://github.com/aetasoul

## Feedback and co.
If there are bugs or you need more features (and I'm not fast enough) feel free to contribute on GitHub. :)
I'm also happy for feedback or suggestions about improvements.
