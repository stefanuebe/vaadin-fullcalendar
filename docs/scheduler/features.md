This addon uses a 3rd party library called FullCalendar Scheduler. We try to integrate all features the library provides. Therefore
the following list might not include all available features, but should list the most important ones. If you find
a feature in the FC library, that this addon not yet supports, please create an feature request in the issues page.

- adding / removing resources (hierarchies of resources are supported)
- Link one or multiple resources with entries.
- Activation of the Scheduler by method in the FullCalendarBuilder.
- List of possible Scheduler based views (timeline).
- List of possible Scheduler entries grouping.

- Event handling for
    - Timeslot clicked
    - Timeslots slected
    - Entry dropped

*Info:* Entries are linked to calendar internally. The calendar instance is used to resolve resources by after updating an
entry on the client side.