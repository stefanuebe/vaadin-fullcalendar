# Multiline events drag & drop cause the scrollbar to lost position
The problem appear when you remove the events and add them back, when you remove all the events from the calendar it resize it-self. When the calendar has no events, it is much shorter which explains why the scroll position is changing.

Take a look here for a workaround https://github.com/stefanuebe/vaadin_fullcalendar/issues/76

Another good way to fix it is to provide the resources/events as JSON feed and refetch the events instead of removing and adding them back

# Calendar crashes when clicking (V14+)
For some, currently unknown reason, sizing a calendar after the view has changed manually on a newly created calendar lets the calendar crash, when clicking inside somewhere. I have no idea, why that is so. Please see https://github.com/stefanuebe/vaadin_fullcalendar/issues/45 for details and progress.

# Build problems / JS (client side) errors (V14+)
It might be, that the transitive dependencies are not resolved correctly. This mostly happens in Spring Boot due to its built in class path scanning, which is adapted by Vaadin.

Please ensure, that, if you are using the `vaadin.allowed-packages` property, that it lists the addon's package `org.vaadin.stefan` (or simply `org.vaadin`).

If you are not using any the allowed or blocking list in the properties and still have the issue, please check, if you have added the `@EnableVaadin` annotation to your Spring application class. If that is the case, check, if there are packages listed. If yes, add the package `org.vaadin.stefan` to it.

If the annotation is not added or added without any parameter and the issue occur, please add
the package `org.vaadin.stefan` plus other necessary package, that have to be scanned, as parameters.

This should enable Spring to analyze all relevant npm dependencies at runtime. Other CDI version should work the same.