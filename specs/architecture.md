# Architecture

> Technology stack and module structure for the FullCalendar Flow addon. `pom.xml` is the source of truth for versions. Do not modify `pom.xml` without asking.

---

## 1. Technology Stack

- **Java 17** — minimum language level
- **Vaadin 24.10.x** — Flow component framework (BOM-managed)
- **FullCalendar JS v6.1.x** — client-side calendar library (bundled via `@NpmPackage`)
- **elemental.json** (provided by Vaadin Flow) — JSON serialization for client-server data exchange
- **Lombok** — boilerplate reduction (`@Getter`, `@Setter`, `@EqualsAndHashCode`, etc.)
- **Apache Commons Text** — string utilities (camelCase conversion for option names)
- **Maven** — multi-module build (wrapper available in `demo/`)
- **JUnit 5 + Mockito** — unit testing
- **Playwright** — E2E testing (separate npm project in `e2e-tests/`)

---

## 2. Module Structure

```
fullcalendar-parent (pom, root)
├── addon/                 # Core component — org.vaadin.stefan:fullcalendar2
├── addon-scheduler/       # Scheduler extension — org.vaadin.stefan:fullcalendar2-scheduler
├── demo/                  # Spring Boot 3.x demo application
├── e2e-test-app/          # Vaadin Spring Boot app serving Playwright test views
├── e2e-tests/             # Playwright test suite (npm, NOT a Maven module)
├── mcp-server/            # Node.js/TypeScript MCP server for addon documentation (NOT a Maven module)
└── fc-docs/               # Local copy of FullCalendar JS v6 docs (reference)
```

### addon/ (core)

The main Vaadin component. Custom element tag: `<vaadin-full-calendar>`.

```
org.vaadin.stefan.fullcalendar/
  FullCalendar.java           — Main component (extends Component, @JsModule, @NpmPackage)
  FullCalendarBuilder.java    — Fluent builder for calendar configuration
  Entry.java                  — Calendar entry model (title, start/end, color, recurrence, etc.)
  FullCalendar.Option         — Enum of all supported calendar options
  dataprovider/
    EntryProvider.java        — Interface for providing entries to the calendar
    InMemoryEntryProvider.java — Client-side entry storage (all entries sent to browser)
    CallbackEntryProvider.java — Lazy loading (entries fetched per visible date range)
  converters/                 — Java ↔ JS type converters (Duration, DayOfWeek, RRule, etc.)
  json/                       — Custom annotations (@JsonName, @JsonConverter, @JsonIgnore)
  model/                      — Header/Footer toolbar models
```

Frontend (TypeScript): `addon/src/main/resources/META-INF/resources/frontend/vaadin-full-calendar/full-calendar.ts`

### addon-scheduler/ (scheduler extension)

Extends the base calendar with resource management. Custom element tag: `<vaadin-full-calendar-scheduler>`.

```
org.vaadin.stefan.fullcalendar/
  FullCalendarScheduler.java  — Extends FullCalendar, implements Scheduler
  Scheduler.java              — Interface for resource management
  FullCalendarScheduler.SchedulerOption — Scheduler-specific option enum
  Resource.java               — Resource model (hierarchical, per-resource styling)
  ResourceEntry.java          — Entry subclass with resource assignments
  SchedulerView.java          — Scheduler view enum (timeline, resource-timeline, resource-timegrid)
  ResourceAreaColumn.java     — Column definition for resource area
```

---

## 3. Client-Server Communication

The addon uses Vaadin's built-in Element API for bidirectional communication:

- **Java → JS**: `getElement().callJsFunction(name, args)` for imperative calls; `getElement().setPropertyJson(name, json)` for option/state sync
- **JS → Java**: `@DomEvent` annotations on event classes trigger server-side `ComponentEvent` subclasses
- **Entry sync**: Entries are serialized to JSON via `Entry.toJson()` using reflection-based `BeanProperties` + custom `@JsonConverter` annotations (backed by elemental.json), then sent to the client as batched updates. **Important**: Setting entry properties (e.g., `entry.setTitle("new")`) does NOT auto-push to the client. You must call `provider.refreshItem(entry)` or `provider.refreshAll()` to sync changes. This is unlike `Resource`, where `setTitle()` and `setColor()` auto-push.
- **Option system**: `setOption(Option, value)` stores the value server-side and pushes it to the client via `callJsFunction("setOption", key, value)`. Option enum constants map to FC option names via camelCase conversion or explicit `@JsonName`.

---

## 4. Testing

### Unit Tests (JUnit 5 + Mockito)

- Located in `addon/src/test/java/` and `addon-scheduler/src/test/java/`
- Test entry model, JSON serialization, converters, options, builder, data providers
- Run: `mvn test` (all modules) or `mvn test -pl addon -Dtest=EntryTest` (single class)

### E2E Tests (Playwright)

- **Test app**: `e2e-test-app/` — Spring Boot Vaadin app with test views
- **Test suite**: `e2e-tests/tests/*.spec.js` — Playwright specs
- **Run**: `cd e2e-test-app && mvn clean verify -Pit` (starts app + runs Playwright)
- Tests cover: views, toolbar, entry interactions, drag-and-drop, responsiveness, accessibility, scheduler features, event sources, etc.

### Visual Verification (Playwright MCP)

- During development, use the Playwright MCP server to visually verify changes
- Default resolution: 1920x1080
- See [`verification.md`](verification.md) for the process

---

## 5. Key Design Decisions

1. **Light DOM** — FullCalendar manages its own DOM; shadow DOM would break FC's style injection and DOM queries. The component uses light DOM for full compatibility.
2. **Option enums over typed setters** — Most FC options are exposed via `setOption(Option, value)` rather than individual setter methods. This keeps the API surface manageable and makes adding new options trivial.
3. **Reflection-based JSON** — `BeanProperties` uses reflection + annotation caching to serialize `Entry` fields to JSON. This allows subclasses (like `ResourceEntry`) to add fields without modifying serialization code.
4. **Entry ≠ Event** — FullCalendar JS calls them "events"; this addon calls them **"entries"** to avoid collision with Vaadin's component event system. This applies to both class names (`Entry`, `ResourceEntry`) and enum constant prefixes (`ENTRY_*`). See also `README.md` Naming Convention section.
