## Known Issues

### Multiline events drag & drop cause the scrollbar to lose position
The problem appears when you remove the events and add them back. When you remove all the events from the calendar it resizes itself. When the calendar has no events, it is much shorter which explains why the scroll position is changing.

Take a look here for a workaround https://github.com/stefanuebe/vaadin_fullcalendar/issues/76

Another good way to fix it is to provide the resources/events as JSON feed and refetch the events instead of removing and adding them back.

### Build problems / JS (client side) errors
It might be that the transitive dependencies are not resolved correctly. This mostly happens in Spring Boot due to its built-in class path scanning, which is adapted by Vaadin.

Please ensure that, if you are using the `vaadin.allowed-packages` property, it lists the addon's package `org.vaadin.stefan` (or simply `org.vaadin`).

If you are not using any allowed or blocking list in the properties and still have the issue, please check if you have added the `@EnableVaadin` annotation to your Spring application class. If that is the case, check if there are packages listed. If yes, add the package `org.vaadin.stefan` to it.

If the annotation is not added or added without any parameter and the issue occurs, please add the package `org.vaadin.stefan` plus other necessary packages that have to be scanned, as parameters.

This should enable Spring to analyze all relevant npm dependencies at runtime. Other CDI versions should work the same.

### Entry cache limits
The calendar maintains an internal cache of entries limited to 10,000 items (LRU eviction). This is to prevent unbounded memory growth when using lazy-loaded entry providers with large datasets. If you need more entries cached, consider implementing a custom caching strategy.

### Server-defined JavaScript callbacks
The addon uses `new Function()` to evaluate server-defined JavaScript callbacks (e.g., for entry render hooks). This is intentional and allows the server to define client-side behavior dynamically. See [FullCalendar Event Render Hooks](https://fullcalendar.io/docs/event-render-hooks) for the underlying feature.

## Notes for Custom Subclasses

When creating custom TypeScript/JavaScript subclasses of FullCalendar:

1. **Prevent duplicate event handler registration**: Use a flag (e.g., `_tooltipInitialized`) to ensure event handlers are only registered once, even if `initCalendar()` is called multiple times.

2. **Clean up resources**: If you add ResizeObservers or other resources, clean them up in `disconnectedCallback()` to prevent memory leaks.