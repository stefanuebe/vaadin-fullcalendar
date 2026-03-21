# Project Context

> High-level context for the project: the problem being solved, who it's for, what's in scope, and what constraints apply.

## 1. Vision

FullCalendar for Flow brings the full power of the [FullCalendar](https://fullcalendar.io/) JavaScript library (v6) into the Vaadin Flow ecosystem as a first-class server-side Java component. Developers should be able to build rich, interactive calendar UIs — including drag-and-drop, recurrence, multiple views, and resource scheduling — without writing any JavaScript. The addon handles all client-server communication transparently, so calendar state is always synchronized between the Java backend and the browser.

Success looks like: a Vaadin developer adds the Maven dependency, creates a `FullCalendar` instance in Java, and immediately has a fully interactive calendar with entries, views, drag-and-drop, and event handling — all controlled from server-side Java code.

## 2. Users

| Role | Description |
|------|-------------|
| **Vaadin application developer** | Integrates the calendar into a Vaadin Flow application. Uses the Java API to add entries, configure views, handle events (clicks, drops, resizes), and customize appearance. Does not need to write JavaScript unless using advanced JS callbacks. |
| **Scheduler user** | Uses the `addon-scheduler` extension for resource-based views (timelines, vertical resource grids). Assigns entries to resources, configures resource hierarchies, and uses scheduler-specific views. Requires a FullCalendar Scheduler license for production. |

## 3. Constraints

- **Vaadin Flow only**: No Hilla/React support. The component extends `com.vaadin.flow.component.Component`.
- **FullCalendar JS v6**: The addon wraps FC v6.1.x. Not all FC options are exposed — only those with explicit `Option` / `SchedulerOption` enum constants or `setOption(String, Object)`.
- **Scheduler license**: The `addon-scheduler` module integrates the commercial FullCalendar Scheduler plugin. Production use requires a valid license key (CC-NonCommercial for development/evaluation, GPL v3 for open-source, or a commercial license).
- **Java 21 / Vaadin 25**: Current version (v7) requires Java 21 and Vaadin 25.x. Version 6 supported Vaadin 14-24 / Java 11.
- **Light DOM**: The component uses light DOM (not shadow DOM) for easier CSS styling and FullCalendar compatibility.
- **Jackson 3**: JSON serialization uses Jackson 3 (tools.jackson). Custom converters in the `converters/` package handle Java ↔ JS type mapping.
- **No database dependency**: The addon is a pure UI component. Data persistence is the responsibility of the consuming application.

> For technology stack and application structure details, see [`architecture.md`](architecture.md).

---

# Related Documents

- [Spec README](README.md) — process overview and workflow
- [Architecture](architecture.md) — technology stack and module structure
- [Data Model](datamodel/datamodel.md) — entity definitions and relationships
- [Use Case Template](use-cases/use-case-template.md) — template for feature specifications
- [Verification](verification.md) — testing and visual verification checklists
