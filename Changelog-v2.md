# General changes
* Moved to Vaadin 14
* moved frondend files one level up, deleted bower_components
* replaced javax.annotation.Nonnull with javax.validation.constraints.NotNull since

# General full-calendar changes
* is now a Polymer 3 object
* _toEventData is now a static method, taking additionally the calendar as first parameter

# Events
## Event handler init
Events are now initialized in a separate method, that returns an object with
a client-server-eventinfo map.

The method name is `_createEventHandlers`and it returns for instance such an object: 
```
return {
    dateClick: (eventInfo) => {
        return {
            date: eventInfo.dateStr,
            allDay: eventInfo.allDay,
            resource: typeof eventInfo.resource === 'object' ? eventInfo.resource.id : null
        }
    },
    eventClick: (eventInfo) => {
        return {
            id: eventInfo.event.id
        }
    },
    ...
};
```

You can either override this method if you want to implement own
event handlers or extend them, if an event or event information
is missing.

The method `_addEventHandlersToOptions` is used by `_createInitOptions` to register
the event handlers created by `createEventHandlers` in the options object, that will be passed to the calendar
constructor. Override it for special event registration (e.g. if an event needs more than the default single parameter).

* eventDrop also now has info about old and new resource (scheduler)

## Changed event names
Some Java event names have changed to match the client side event names, others have changed 
since the client side name also changed Includes the addListener methods.
* TimeslotClickedEvent > DateClickedEvent
* viewRendererEvent > DatesRenderedEvent