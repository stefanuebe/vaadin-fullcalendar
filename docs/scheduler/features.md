The following functions are currently implemented and available to use from server side:

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