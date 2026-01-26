# Getting Started

This guide will help you add FullCalendar for Vaadin Flow to your project.

## Requirements

- **Version 7.x**: Vaadin 25+ / Java 21+
- **Version 6.x**: Vaadin 14-24 / Java 11+

## Maven Dependency

Add the Vaadin Directory repository to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>vaadin-addons</id>
        <url>https://maven.vaadin.com/vaadin-addons</url>
    </repository>
</repositories>
```

Add the core dependency:

```xml
<dependency>
    <groupId>org.vaadin.stefan</groupId>
    <artifactId>fullcalendar2</artifactId>
    <version>7.0.0</version>
</dependency>
```

### Scheduler Extension (Optional)

If you need resource-based views (Timeline, Vertical Resource), add the Scheduler extension:

```xml
<dependency>
    <groupId>org.vaadin.stefan</groupId>
    <artifactId>fullcalendar2-scheduler</artifactId>
    <version>7.0.0</version>
</dependency>
```

**Note**: The Scheduler extension requires a separate [commercial license](https://fullcalendar.io/pricing) from FullCalendar LLC for production use. The MIT license of this addon only covers the Java integration code.

## Basic Usage

Create a simple calendar and add it to your view:

```java
// Create the calendar
FullCalendar calendar = FullCalendarBuilder.create().build();
calendar.setSizeFull();

// Add it to your layout
add(calendar);

// Create and add an entry
Entry entry = new Entry();
entry.setTitle("My Event");
entry.setStart(LocalDate.now().atTime(10, 0));
entry.setEnd(LocalDate.now().atTime(12, 0));

calendar.getEntryProvider().asInMemory().addEntry(entry);
```

## Next Steps

- Check the [Samples](Samples.md) for more code examples
- Review [Features](Features.md) for a full feature overview
- See the [FAQ](FAQ.md) for common questions and troubleshooting
