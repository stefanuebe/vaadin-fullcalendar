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

# Run unit tests (all modules)
mvn test

# Run a single test class
mvn test -pl addon -Dtest=EntryTest

# Run a single test method
mvn test -pl addon -Dtest=EntryTest#testSomeMethod

# Run integration tests
mvn verify

# Run E2E tests (starts Vaadin app + Playwright)
cd e2e-test-app && mvn clean verify -Pit

# Run mutation testing (PIT — unit tests)
mvn test -pl addon -Ppit
# Report: addon/target/pit-reports/index.html

# Run manual mutation test scripts (see specs/verification.md §3)
bash mutation-test-b.sh        # Unit mutations (~75s, standalone)
bash mutation-test-a.sh        # E2E mutations (~5min, requires app on :8080)

# Alternative: Use Maven wrapper from demo/ if mvn not available
./demo/mvnw clean install
```

## Release Workflow

The `v-herd-demo` branch always reflects the **currently released** version — it is the source for the demo server and the MCP server deployment. Therefore, releases must happen in a strict order so the tag, the demo branch, and the next snapshot all line up.

For every release (patch, minor, or major):

1. **Strip `-SNAPSHOT`** from every `<version>` and `<fullcalendar.version>` across all POMs (root, `addon`, `addon-scheduler`, `demo`, `e2e-test-app`).
2. **Update `ADDON_VERSION`** in `demo/src/main/java/org/vaadin/stefan/ui/layouts/AbstractLayout.java` to the release version (without `-SNAPSHOT`). This string constant is rendered in the demo/MCP-server footer, so it must match the deployed artifact version. The same file also exists in `spike/` but is not on the release path — leave it alone.
3. **Commit** as `Release <version>` (matches the style of existing release commits, e.g. `5f861d00`). The commit must include both the POM and the `ADDON_VERSION` change so the tag and the `v-herd-demo` merge both carry a consistent tree.
4. **Tag** the commit as annotated tag `<version>` with message `Release <version>` (e.g. `git tag -a 7.2.1 -m "Release 7.2.1"`).
5. **Do not bump the snapshot yet.** The next snapshot bump goes *after* step 6.
6. **Sync `v-herd-demo` to the release state.** Check out `v-herd-demo` and merge master (`git merge master -X theirs` to take master's POM values over the historical `v-herd-version` commit). Resulting tree should show the released version across all POMs *and* in `ADDON_VERSION`. Push.
7. **Back on master: bump to the next snapshot** (usually `+1` on the patch, e.g. `7.2.1 → 7.2.2-SNAPSHOT`). Update both the POMs *and* `ADDON_VERSION` (e.g. `"7.2.2-SNAPSHOT"`). Commit as `Bump to <next>-SNAPSHOT`.
8. **Push master and the new tag.**

Why this order matters: if the snapshot is bumped before `v-herd-demo` is synced, merging master into `v-herd-demo` brings in the snapshot version — then the demo server redeploys against a `-SNAPSHOT` artifact that isn't in any public repo, and the deployment breaks.

Why `ADDON_VERSION` matters: the demo server and the MCP server both render this constant as "Version X" in their UI. If it drifts from the actual POM version, deployed demos display a misleading version number. Forgetting to update it at release time has historically required a follow-up cherry-pick into `v-herd-demo`, which is the kind of thing the workflow is supposed to prevent.

Never push the release tag before confirming with the user — tags are public the moment they hit the remote and can't be rewritten cleanly.

## Verification / Testing Rules

- **Bug report triage: always check for existing tests first.** When a bug is reported, immediately check whether an existing test (unit, integration, or E2E) covers the affected use case. If a test exists but didn't catch the bug, fix the test. If no test exists, create one.
- **Every bug fix must include a verification test.** No bug fix is complete without a test that reproduces the bug and verifies the fix.
- **Every new feature must include verification** as defined in the relevant spec (see `specs/` directory).
- **Never commit test code without running it first.** Tests must pass before they are considered done.
- **Do not start a dev server for the user to verify.** Claude runs inside the devcontainer and its servers are unreachable from the host browser. For UI verification either (a) run Playwright yourself inside the devcontainer and report findings, or (b) hand verification back to the user explicitly ("please run `mvn spring-boot:run` on your host and tell me what you see"). Never leave a background `spring-boot:run` expecting the user to click around.

## Module Structure

```
addon/              # Core FullCalendar Flow component (org.vaadin.stefan:fullcalendar2)
addon-scheduler/    # Scheduler extension for resource-based views (org.vaadin.stefan:fullcalendar2-scheduler)
demo/               # Spring Boot demo application
e2e-test-app/       # Vaadin Spring Boot app serving as E2E test target (Playwright)
e2e-tests/          # Playwright test suite (tests/*.spec.js) — NOT a Maven module, uses npm
mcp-server/         # Node.js MCP server for addon documentation (TypeScript/Express)
fc-docs/            # Local copy of FullCalendar JS docs — v6 (current) + v7 changelog/migration guide
```

## Specs (Working Basis)

The `specs/` directory is the **primary working basis** for implementation tasks. Always read the relevant spec files before starting work:

1. `specs/project-context.md` — Read first: vision, problem, users, scope, risks
2. `specs/architecture.md` — Tech stack and application structure
3. `specs/datamodel/datamodel.md` — Entity definitions and relationships
4. `specs/use-cases/` — Individual feature specs (copy `use-case-template.md` per feature)
5. `specs/verification.md` — Visual verification checklists (Playwright MCP)
6. `specs/design-system.md` — Design system rules

Workflow: Define context → Outline architecture → Specify features → Implement → Verify → Write tests. Specs are the single source of truth — keep them up to date as the project evolves.

## Naming Convention

FullCalendar JS calls them "events"; this Java addon calls them **"entries"** (`Entry.java`, not `Event.java`). This avoids collision with Vaadin's event system. All enum constants, method names, and class names use `ENTRY_` / `Entry` prefix, never `EVENT_`.

## Architecture

### Core Component (`addon/`)

The main component is `FullCalendar` (custom element tag: `<vaadin-full-calendar>`) extending Vaadin's `Component`. Key classes:

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

### Frontend

TypeScript source lives at `addon/src/main/resources/META-INF/resources/frontend/vaadin-full-calendar/full-calendar.ts`. This is the client-side web component that communicates with the Java `FullCalendar` class. The FullCalendar JS client version is defined by `FullCalendar.FC_CLIENT_VERSION` (currently 6.1.20).

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

User-facing documentation lives in the **GitHub wiki as the single source of truth** — there is no in-repo `docs/` folder. Key pages:

- [Home](https://github.com/stefanuebe/vaadin-fullcalendar/wiki)
- [Getting Started](https://github.com/stefanuebe/vaadin-fullcalendar/wiki/Getting-Started)
- [Samples](https://github.com/stefanuebe/vaadin-fullcalendar/wiki/Samples)
- [Features](https://github.com/stefanuebe/vaadin-fullcalendar/wiki/Features)
- [Release notes](https://github.com/stefanuebe/vaadin-fullcalendar/wiki/Release-notes) — one detail page per minor (`Release-notes-<major>.<minor>`)
- [Migration guides](https://github.com/stefanuebe/vaadin-fullcalendar/wiki/Migration-guides) — one detail page per version jump (`Migration-guide-<from>-to-<to>`)
- [MCP-Server](https://github.com/stefanuebe/vaadin-fullcalendar/wiki/MCP-Server), [FAQ](https://github.com/stefanuebe/vaadin-fullcalendar/wiki/FAQ), [Known Issues](https://github.com/stefanuebe/vaadin-fullcalendar/wiki/Known-Issues), [Scheduler license](https://github.com/stefanuebe/vaadin-fullcalendar/wiki/Scheduler-license)

The wiki is a separate git repo: `https://github.com/stefanuebe/vaadin-fullcalendar.wiki.git`. In this devcontainer it is checked out at `/workspace/wiki/` (remote `origin-wiki`). Edit files there and commit/push to the wiki remote.

### Documentation conventions

- **Link style** — wiki-internal links use full GitHub URLs (`https://github.com/stefanuebe/vaadin-fullcalendar/wiki/Page-Name`), not relative wiki links. External viewers (Vaadin Directory etc.) do not resolve relative wiki links.
- **Work-in-progress pages** — while a release or migration guide is being drafted, the wiki page title carries a `-wip` suffix (`Release-notes-7.2-wip`, `Migration-guide-7.1-to-7.2-wip`). Add an entry to the relevant index (`Release-notes` / `Migration-guides`) with a visible "(work in progress)" marker. On release, rename the page (GitHub wiki auto-creates redirects), drop the suffix and marker, and update every cross-reference.
- **Page structure** — one release-notes detail page per minor version; one migration-guide detail page per version jump; main index pages stay one-line-per-entry.
- **v6 vs v7 in the same wiki** — code-level difference is only elemental JSON (v6) vs Jackson 3 (v7). Call out version-specific API differences inline where relevant; do not maintain parallel page sets.

### MCP server documentation

`mcp-server/` extracts content from the wiki (cloned during Docker build) and serves it as MCP resources. If you rewrite wiki pages, the MCP server picks them up on the next container rebuild — no in-repo sync needed.

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