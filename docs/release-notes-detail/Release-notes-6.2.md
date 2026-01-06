With 6.2 custom native event handlers for entries have been added. These allow you to setup JavaScript events for
each entry, e.g. a mouse over event handler. Inside these event handlers you may also access the created entry dom
element.

Custom native event handlers are added to the FullCalender object. They will then be applied to each created
entry object (using the entryDidMount callback).

To add an event handler, simply call the method `addEntryNativeEventListener` on the calendar. The first parameter
is the JavaScript event name (e.g. "mouseover"), the second parameter is the callback, that shall be used for
that event. Please be aware, that we do NOT check or sanitize the given JavaScript. It is up to you to prevent
malicious code from being sent to your users.

Inside the event callback, you may access the entryDidMount argument object, that contains additional information
about the current entry. See the official docs (https://fullcalendar.io/docs/event-render-hooks) 
for more details about which details it provide.

```java
FullCalendar calendar = new FullCalendar();

// ... other configurations

// write the js event, the current entry info and the current entry's element to the browser console.
calendar.addEntryNativeEventListener("mouseover", "e => console.warn(e, info.event, info.el)");

add(calendar);
```

Please be aware, that due to the design of the used library, these event handlers have to be setup before the
calendar is initialized on the client side.

You can combine the event handlers with a custom entryDidMount callback, if you want additional customizations
of the entries. The FC will take care of combining the event handlers and you EDM callback