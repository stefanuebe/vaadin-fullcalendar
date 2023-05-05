This addon extends the **FullCalendar for Flow** addon with the **FullCalendar Scheduler**, which provides
additional resource based views (Timeline View and Vertical Resource View) for Vaadin 14+.

It is based on the version 6.1.6 of the FullCalendar Scheduler library.

It needs the basic addon (https://vaadin.com/directory/component/full-calendar-flow) to work.
Since this addon is not always updated when the basis gets an update, I suggest, that you add both dependencies
(basis and extension) to always use the latest versions. This extension is compatible as long as the readme
does not tells anything else.

## Requirements / versions / support
The addon is built against Vaadin 14.x and Java 8, but is intended to be used with the latest major Vaadin version
and thus also supports Vaadin 23 and 24 / Java 11+.

Addon versions prior to the current major version are not supported anymore. If you need a fix for such a version
feel free to fork the project. You may create an issue, but due to limited time it is very unlikely, that we will fix
it.

Please also have a look at the demo for some basic examples and source code of how to integrate the FC.
For more examples please have a look into the example section.

## Additional links and information

For information about the Scheduler (functionality, features, license information, etc.)
visit https://fullcalendar.io/scheduler.

For a Vaadin 10-13 version (that is built on FC 3.10.x), see https://vaadin.com/directory/component/full-calendar-scheduler-extension-vaadin10

## License information:
Please be aware, that the FullCalender Scheduler library this addon is based on has a different license model
then the basic FullCalendar. For details about the license, visit https://fullcalendar.io/license.

**This addon does not provide any commercial license for the Scheduler. The license model of MIT does only affect
the additional files of this addon, not the used original files.**

## Activating the Scheduler
By default the scheduler is not active, when you use a FullCalendar instance. To have an instance with scheduler
activated, use the `withScheduler()` method of the `FullCalendarBuilder`.

This method will throw an exception, if the scheduler extension is not on the class path.

To link a resource with entries, use the Entry subclass `ResourceEntry`.

## Building with V14
It might be, that the transitive dependencies are not resolved correctly.

If you are using Spring Boot please add the `@EnableVaadin` annotation to your application class. Add
the package `org.vaadin.stefan` plus your root package as parameters. This should enable Spring to analyze
all npm dependencies at runtime. Other CDI version should work the same.

If you are not using Spring, but have similiar issues try to add also the goal `build-frontend` to the vaadin maven plugin. This should resolve transitive npm dependencies at build time.


For instance:
```
<plugin>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-maven-plugin</artifactId>
    <version>${vaadin.version}</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-frontend</goal>
                <goal>build-frontend</goal>
            </goals>
        </execution>
    </executions>
</plugin>
``` 

## Additional Features of the Scheduler extension
- Activation of the Scheduler by method in the FullCalendarBuilder.
- Adding resources to a calendar (hierarchies of resources are not yet supported).
- Link one or multiple resources with entries.
- List of possible Scheduler based views (timeline).

*Info:* Entries are linked to calendar internally. The calendar instance is used to resolve resources by after updating an
entry on the client side.

## Feedback and co.
If there are bugs or you need more features (and I'm not fast enough) feel free to contribute on GitHub. :)
I'm also happy for feedback or suggestions about improvements.
