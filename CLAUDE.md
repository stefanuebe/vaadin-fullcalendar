# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

FullCalendar for Flow is a Vaadin Flow integration of the FullCalendar JavaScript library (v6). It provides Java components that wrap FullCalendar for use in Vaadin applications.

**Version 7**: Vaadin 25 / Java 21 (current)
**Version 6**: Vaadin 14-24 / Java 11

## Build Commands

```bash
# Build all modules (install to local repo for cross-module deps)
mvn clean install

# Build for production (optimized frontend)
mvn clean install -Pproduction -DskipTests

# Run the demo application (requires production profile for full build)
cd demo && mvn spring-boot:run -Pproduction

# Run unit tests
mvn test

# Run integration tests
mvn verify

# Alternative: Use Maven wrapper from demo/ if mvn not available
./demo/mvnw clean install
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
- `Home.md` - Documentation index
- `Features.md` - Feature overview
- `Samples.md` - Code examples
- `Migration-guides.md` - Version migration instructions
- `Release-notes.md` - Version release notes
- `Scheduler-license.md` - Scheduler extension licensing info
- `FAQ.md` - Frequently asked questions
- `Known-issues.md` - Known issues and workarounds
- `MCP-Server.md` - MCP server documentation (copy of `mcp-server/README.md` - keep in sync!)

**Note**: `docs/MCP-Server.md` is a full copy of `mcp-server/README.md`. When updating the MCP server README, also update the docs copy.

Wiki: https://github.com/stefanuebe/vaadin-fullcalendar/wiki

## Thread Safety & Performance Notes

- `FullCalendar.refreshAllEntriesRequested` uses volatile + synchronized for thread safety
- `BeanProperties` caches reflection data (annotations, converters) for performance
- Entry cache is bounded to 10,000 entries max (LRU eviction)
- ResizeObserver is cleaned up in `disconnectedCallback()` to prevent memory leaks
- Server-defined JS callbacks use `new Function()` intentionally for dynamic evaluation

## MCP Servers and other docs

FullCalendar Vaadin MCP server for addon-specific documentation, API reference, and code examples:

```json
{
  "mcpServers": {
    "fullcalendar": {
      "type": "http",
      "url": "https://v-herd.eu/vaadin-fullcalendar-mcp/mcp"
    }
  }
}
```

Vaadin documentation MCP server for component DOM structure and API reference:

```json
{
  "mcpServers": {
    "vaadin": {
      "type": "http",
      "url": "https://mcp.vaadin.com/docs"
    }
  }
}
```

If the MCP server does not have instructions on particular client side elements (web-components) of Vaadin, you
may also check the typescript api: https://cdn.vaadin.com/vaadin-web-components/25.0.2, where each element has its
own page, e.g. https://cdn.vaadin.com/vaadin-web-components/25.0.2/elements/vaadin-menu-bar/ . Only use that page as
a last resort and only open the respective element's page.