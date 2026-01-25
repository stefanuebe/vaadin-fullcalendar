# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

FullCalendar for Flow is a Vaadin Flow integration of the FullCalendar JavaScript library (v6). It provides Java components that wrap FullCalendar for use in Vaadin applications.

**Version 7**: Vaadin 25 / Java 21 (current)
**Version 6**: Vaadin 14-24 / Java 11

## Build Commands

```bash
# Build all modules
mvn package

# Build for production (optimized frontend)
mvn clean package -Pproduction -DskipTests

# Run the demo application
cd demo && mvn spring-boot:run

# Run unit tests
mvn test

# Run integration tests
mvn verify
```

## Module Structure

```
addon/              # Core FullCalendar Flow component (org.vaadin.stefan:fullcalendar2)
addon-scheduler/    # Scheduler extension for resource-based views (org.vaadin.stefan:fullcalendar2-scheduler)
demo/               # Spring Boot demo application
```

## Architecture

### Core Component (`addon/`)

The main component is `FullCalendar` extending Vaadin's `Component`. Key classes:

- `FullCalendar.java` - Main calendar component with bidirectional JS communication
- `FullCalendarBuilder.java` - Fluent builder for calendar configuration
- `Entry.java` - Calendar event/entry model with properties (title, start/end, color, recurrence, etc.)
- `dataprovider/` - Data provider abstraction:
  - `EntryProvider` - Interface for providing entries
  - `InMemoryEntryProvider` - Client-side data storage
  - `CallbackEntryProvider` - Lazy loading from backend

### Scheduler Extension (`addon-scheduler/`)

Extends the base calendar with resource management:

- `FullCalendarScheduler.java` / `Scheduler.java` - Resource-based calendar views
- `Resource.java` - Resource model (supports hierarchies)
- Timeline and Vertical Resource views

**Note**: The Scheduler extension requires a separate FullCalendar license. The MIT license only covers this addon's code, not the underlying FullCalendar Scheduler library.

### Event System

Server-side events for calendar interactions:
- `EntryClickedEvent`, `EntryDroppedEvent`, `EntryResizedEvent`
- `TimeslotClickedEvent`, `TimeslotsSelectedEvent`
- `DatesRenderedEvent`, `MoreLinkClickedEvent`
- `DayNumberClickedEvent`, `WeekNumberClickedEvent`

### JSON Handling

Uses Jackson 3 for serialization (changed from elemental.json in v7.0). Custom annotations in `json/` package for property mapping.

## Tech Stack

- Java 21
- Vaadin 25.x
- Spring Boot 4.x (demo only)
- Maven multi-module build
- Lombok for boilerplate reduction
- JUnit 5 + Mockito for testing
- Vite for frontend bundling

## Key Patterns

1. **Vaadin Component Pattern**: Components extend `com.vaadin.flow.component.Component` and use `@JsModule` for frontend resources
2. **Builder Pattern**: `FullCalendarBuilder` provides fluent configuration API
3. **Data Provider Pattern**: Similar to Vaadin's DataProvider for managing calendar entries
4. **Light DOM**: v6+ uses light DOM instead of shadow DOM for easier styling

## Documentation

Detailed documentation available in `docs/`:
- `Features.md` - Feature overview
- `Samples.md` - Code examples
- `Migration-guides.md` - Version migration instructions
- `FAQ.md`, `Known-issues.md`

Wiki: https://github.com/stefanuebe/vaadin-fullcalendar/wiki
