**What is the demo project**
The main purpose of the demos is to provide the users a place to check the several features and reuse the source
code for their own projects. So the sample code needs to be focused on what they shall show


**Main Goals**
There are multiple main goals, that shall be achieved with this phase.

* Align the demo with the newly introduced calendar item (CI) api.
* Restructure the demo to contain samples for most features.
* Provide sample code to copy from, similar like the Vaadin component docs do (demo and below that an expandable part with the code). Exception is the playground, it will not show sample code, as this would let the page "explode"
* Also each demo shall provide a small description text above the calendar to describe, what the user sees and can do here.


**Current state**

The current demo is a mix of the old entry api and new CI api. Especially the view internal methods, are still using a lot of entry.
Also there are some demos for main features, but no isolated feature demos.

**New demo layout**

The demo should be reworked to have the following structure:

* Playground > same as now, but streamlined. Stays a Scheduler-based calendar (with resources, timeline views). No separate Scheduler section needed.
* Basic Features
  * Events View > similar to current basic demo, but with feedback for all event listeners, the FC provides
  * Recurring & BG Items > shows several types of recurring and background calendar items
  * Business Hours > different combinations of business hours
  * Native Event Listeners > shows a set of browser events on an item, e.g mouse enter, mouse leave and contextmenu
  * i18n > browser timezone and the possibility to change TZ and Locale
* CalendarItemProvider > same as EntryProvider views but with a different model class
  * In Memory CIP > uses setters to update items on dnd or resize
  * Callback CIP > uses update mechanism to update items
  * Backend CIP > uses a simulated service layer (in-process store, e.g. ConcurrentHashMap) that pretends to be a backend, similar to the Backend EP pattern
* Entry Provider > same as now
  * In Memory EP
  * Callback EP
  * Backend EP
* Multi Month Demo > same as now, but streamlined if necessary
* Callbacks
  * Custom Properties > same as now, but streamlined if necessary
  * ItemClassname
  * ItemContent
  * ItemDidMount
  * ItemWillUnmount
  * ResourceLabelContent
  * resourceLaneContent
  * ... and so on (the remaining fc and scheduler callbacks)

... maybe other views? based on the samples or so...

**Sidenav structure**

Flat list with visual separator labels between groups (e.g. a bold header like "--- Basic Features ---"). No nested/grouped navigation.


**Existing unlisted views**

Views not listed in the new structure (DemoWithTooltip, CalendarItemProviderSchedulerDemo, the `samples/` package,
commented-out views like DemoCustomProperties, DemoCalendarWithBackgroundEvent, InlineCalendarDemo) will be kept
unlisted in the sidenav but remain in the codebase. They serve as reference and inspiration for the new demo views
(e.g. DemoCalendarWithBackgroundEvent provides good insights for creating the Recurring & BG Items demo).


**Toolbar approach**

The current `CalendarViewToolbar` is hardcoded to `FullCalendar<Entry>` (creates Entry objects, calls `getEntryProvider()`).
This means it cannot be used with CIP views that use a different model type.

Solution: **Separate toolbars**.
* Keep the existing Entry-typed `CalendarViewToolbar` for Entry-based views (Playground, Entry Provider demos, etc.)
* Create a new simplified toolbar for CIP views that does not contain entry-specific create/edit logic.


**Code display mechanism**

Each demo view (except the Playground) shall show collapsible source code below the calendar.

Approach: **Runtime source reading with marker comments + Maven resource copy**.
* Each view's Java source file contains `// DEMO-START` and `// DEMO-END` comments around the relevant code region.
* Maven copies the demo view source files as resources into the JAR (e.g. via `maven-resources-plugin` copying
  from `src/main/java/.../view/demos/` to `src/main/resources/demo-sources/`). This ensures the source is
  available at runtime in both dev and production builds.
* `AbstractDemoView` reads the corresponding `.java` resource from the classpath, extracts the marked region,
  and renders it in a collapsible `Details` + `Pre` component.
* The Playground is explicitly exempt from code display (it would make the page explode).


**Description texts**

Each demo view needs a short description text above the calendar explaining what the user sees and can do.
Descriptions will be written during implementation of each view and reviewed at the per-view approval gate.
Keep a personal, warm tone — these are for real users, not machines.


**Execution**
_Prerequisites_

Before any views are refactored, we need to get rid of the current abstract view structure. Every view shall use the FC api directly,
e.g. `addCalendarItemClickedListener` instead of some internal delegating `onEntryClick` or `onItemClick`.

An abstract view is allowed, but it should only contain internal demo code, that would otherwise be duplicated all the time, like
the description, the sample code showing, the "demo card" lookalike, etc.

Reusable components, like the `CalendarViewToolbar` shall be reused in views and configured accordingly to serve the purpose of the
view.

The different callbacks and their respective options, like `onSamplesCreated` only should be used, when the view itself uses `Entry` as
its model class.

_Streamline_
Some views can be reused as they are (see the structure), but they might contain unnecessary code or lots of sample data. Try to keep things
as simple as they are.

_Entry vs Model_
In most cases the usage of the Entry or ResourceEntry class as the model is fine. Only the CalendarItemProvider demo views shall use a different
model.

This means, that also the usage of the EntryProvider is fine, BUT, everything else shall be done via the new CIP api, i.e. event listeners etc.
I do not want to have deprecated API in the demos. If there is a conflict between the here written and the real code, I WANT A PROPER FEEDBACK AND QUESTION WITHOUT
ANY GUESSING.

_Tests_
Tests are currently based on the current demos. A new **separate Maven module** (e.g. `e2e-test-app/`) will be created
to hold the views needed by E2E tests. This fully decouples the test infrastructure from the demo, so demo changes
never break tests.

Key facts about the current test coupling:
* The drag-resize regression tests (`e2e-tests/tests/drag-resize-regression.spec.js`) hard-code entry names
  ("Short trip", "Meeting 8", "Multi 1", "Multi 2") that are created inside `Playground.java`'s `createTestEntries()`.
* The entry-provider tests (`e2e-tests/tests/entry-provider.spec.js`) navigate to specific routes
  (`/in-memory-entry-provider`, `/callback-entry-provider`, `/backend-entry-provider`).
* The default test fixture navigates to `/` and waits for `.fc-event` elements.

All test-dependent views and their data must be replicated in the new `e2e-test-app/` module before any demo
views are modified.


**Proposed steps**
1. Analyze the tests and create a full dependency map: which E2E test files rely on which routes, views, and entry names. Then create the new `e2e-test-app/` Maven module with the necessary views, data, and Spring Boot test harness so that all Playwright tests pass against the new module.
2. Create an `AbstractDemoView` that contains the structure for each demo view, like a title, description, the calendar and code sample area (with the runtime source reading mechanism). The calendar shall be abstract and is to be created by each demo view respectively.
3. **Validate with a simple view first.** Migrate one simple demo (e.g. Business Hours) to validate the full `AbstractDemoView` design including code display, description text, and the demo card layout. Get approval before proceeding.
4. Create the stubs for each remaining demo view and update the sidenav (flat list with separator labels).
5. Migrate each view step by step. Start with the Playground after the simple view is approved — this will be the biggest challenge. Remember, that the Playground will not have sample code to show.
6. After each view is migrated, the **ui-explorer subagent** (Claude's Playwright-based agent) tests it for visual and functional correctness. After that I will manually test the respective view. When I gave the go, the next view will be migrated.


**Deprecated API inventory**

The following deprecated APIs must NOT appear in reworked demo code. Use the replacement listed for each.

_Event listener registration (FullCalendar / Scheduler)_

| Deprecated method | Replacement |
|---|---|
| `addEntryClickedListener(...)` | `addCalendarItemClickedListener(...)` |
| `addEntryMouseEnterListener(...)` | `addCalendarItemMouseEnterListener(...)` |
| `addEntryMouseLeaveListener(...)` | `addCalendarItemMouseLeaveListener(...)` |
| `addEntryResizedListener(...)` | `addCalendarItemResizedListener(...)` |
| `addEntryDroppedListener(...)` | `addCalendarItemDroppedListener(...)` |
| `addEntryDroppedSchedulerListener(...)` | `addCalendarItemDroppedSchedulerListener(...)` |

_Event classes and their methods_

| Deprecated class | Replacement class |
|---|---|
| `EntryClickedEvent` | `CalendarItemClickedEvent<Entry>` |
| `EntryDroppedEvent` | `CalendarItemDroppedEvent<Entry>` |
| `EntryResizedEvent` | `CalendarItemResizedEvent<Entry>` |
| `EntryMouseEnterEvent` | `CalendarItemMouseEnterEvent<Entry>` |
| `EntryMouseLeaveEvent` | `CalendarItemMouseLeaveEvent<Entry>` |
| `EntryDroppedSchedulerEvent` | `CalendarItemDroppedSchedulerEvent<Entry>` |
| `EntryEvent` | `CalendarItemEvent<Entry>` |
| `EntryDataEvent` | `CalendarItemDataEvent<Entry>` |
| `EntryChangedEvent` (forRemoval) | `CalendarItemDataEvent` / `CalendarItemTimeChangedEvent` |
| `EntryTimeChangedEvent` | `CalendarItemTimeChangedEvent<Entry>` |

On all event instances:
| Deprecated method | Replacement |
|---|---|
| `.getEntry()` | `.getItem()` |
| `.applyChangesOnEntry()` | `.applyChangesOnItem()` |
| `.createCopyBasedOnChanges()` | Use CIP event hierarchy directly |

_Provider / data methods (FullCalendar)_

| Deprecated method | Replacement |
|---|---|
| `setEntryProvider(...)` | `setCalendarItemProvider(...)` (for CIP views; `setEntryProvider` is OK for Entry-based views) |
| `getEntryProvider()` | `getCalendarItemProvider()` |
| `isUsingCalendarItemProvider()` | Check provider type directly |
| `isInMemoryEntryProvider()` | `isInMemoryProvider()` |
| `fetchEntriesFromServer(...)` | `fetchItemsFromServer(...)` |
| `getCachedEntryFromFetch(...)` | `getCachedItemFromFetch(...)` |
| `requestRefresh(Entry)` | `requestRefreshCalendarItem(Object)` |
| `requestRefreshAllEntries()` | `requestRefreshAllItems()` |

_Callback setters (FullCalendar)_

| Deprecated method | Replacement |
|---|---|
| `setEntryClassNamesCallback(...)` | `setItemClassNamesCallback(...)` |
| `setEntryDidMountCallback(...)` | `setItemDidMountCallback(...)` |
| `setEntryWillUnmountCallback(...)` | `setItemWillUnmountCallback(...)` |
| `setEntryContentCallback(...)` | `setItemContentCallback(...)` |
| `addEntryNativeEventListener(...)` | `addItemNativeEventListener(...)` |
| `setEntryDisplay(...)` | `setItemDisplay(...)` |

_Editability methods (FullCalendar / Scheduler)_

| Deprecated method | Replacement |
|---|---|
| `getEntryDurationEditable()` | `getItemDurationEditable()` |
| `setEntryDurationEditable(...)` | `setItemDurationEditable(...)` |
| `getEntryResizableFromStart()` | `getItemResizableFromStart()` |
| `setEntryResizableFromStart(...)` | `setItemResizableFromStart(...)` |
| `getEntryStartEditable()` | `getItemStartEditable()` |
| `setEntryStartEditable(...)` | `setItemStartEditable(...)` |
| `setEntryResourceEditable(...)` | `setItemResourceEditable(...)` |

_Limit / display methods (FullCalendar)_

| Deprecated method | Replacement |
|---|---|
| `setMaxEntriesPerDay(...)` | `setMaxItemsPerDay(...)` |
| `setMaxEntriesPerDayFitToCell()` | `setMaxItemsPerDayFitToCell()` |
| `setMaxEntriesPerDayUnlimited()` | `setMaxItemsPerDayUnlimited()` |
| `setGroupEntriesBy(...)` | `setGroupItemsBy(...)` |

_Builder methods (FullCalendarBuilder)_

| Deprecated method | Replacement |
|---|---|
| `withEntryProvider(...)` | `withCalendarItemProvider(...)` (for CIP) |
| `withEntryLimit(...)` | `withCalendarItemLimit(...)` |
| `withEntryContent(...)` | `withCalendarItemContent(...)` |

_Legacy methods (forRemoval=true) — must not appear at all_

| Deprecated | Replacement |
|---|---|
| `new FullCalendar(int entryLimit)` | Use `FullCalendarBuilder#withCalendarItemLimit(int)` |
| `new FullCalendarScheduler(int entryLimit)` | Use `FullCalendarBuilder#withCalendarItemLimit(int)` |
| `setHeight(int)` | `setHeight(String)` or `setHeight(float, Unit)` |
| `setHeightByParent()` | `setHeight(String)` or `setHeight(float, Unit)` |
| `setHeightAuto()` | `setHeight(String)` or `setHeight(float, Unit)` |
| `setWeekNumbersWithinDays(...)` | Remove call entirely |
| `lookupViewName(...)` | `lookupViewByClientSideValue(...)` |
| `Option.COLUMN_HEADER` | `Option.DAY_HEADERS` |
| `Entry.assignClassName(...)` etc. | `Entry.addClassNames(...)` |
| `Entry.unassignClassName(...)` etc. | `Entry.removeClassNames(...)` |
| `ResourceEntry.assignResource(...)` | `ResourceEntry.addResources(...)` |
| `ResourceEntry.unassignAllResources()` | `ResourceEntry.removeAllResources()` |


---

**Sub-phases**

Each sub-phase is a self-contained work package. View migration phases (10.4+) each end with a
**gate**: ui-explorer subagent test, then manual approval before proceeding to the next phase.

**Before starting any phase with a fresh context**, read this entire `demo-rework.md` document first —
especially the sections on deprecated API inventory, code display mechanism, toolbar approach,
description texts, and the sidenav structure. Also read `/workspace/CLAUDE.md` for project structure
and build commands.

---

_Phase 10.1 — Test decoupling_

Goal: Fully decouple E2E tests from the demo module so the demo can be freely restructured.

* Analyze all E2E test files in `e2e-tests/tests/` and create a complete dependency map:
  which test files rely on which routes, views, entry/item names, and CSS selectors.
* Create a new `e2e-test-app/` Maven module with its own Spring Boot application context
  and Vaadin routing. Use `demo/pom.xml` as template for dependencies (Spring Boot, Vaadin,
  addon/addon-scheduler).
* Replicate all test-dependent views and their sample data verbatim (e.g. Playground's
  `createTestEntries()` with "Short trip", "Meeting 8", "Multi 1", "Multi 2"; the three
  entry provider views at their exact routes).
* Update E2E test configuration (`e2e-tests/playwright.config.js` or equivalent) to run
  against `e2e-test-app/` instead of `demo/`.
* Verify all existing Playwright tests pass against the new module without modification.

Key files to analyze:
- `e2e-tests/tests/drag-resize-regression.spec.js` — hard-codes entry names from Playground
- `e2e-tests/tests/entry-provider.spec.js` — navigates to `/in-memory-entry-provider`, `/callback-entry-provider`, `/backend-entry-provider`
- `e2e-tests/tests/fixtures.js` — default fixture navigates to `/` and waits for `.fc-event`
- `demo/src/main/java/org/vaadin/stefan/ui/view/demos/playground/Playground.java` — source of test entries

Deliverable: All E2E tests green against `e2e-test-app/`. Demo module can now be changed freely.

---

_Phase 10.2 — Demo infrastructure_

Goal: Build the reusable foundation that all demo views will use.

* Create `AbstractDemoView` in `demo/src/main/java/org/vaadin/stefan/ui/view/` with:
  - Title area (view name as heading)
  - Description text area (short, warm-toned explanation)
  - Abstract calendar creation method (each view creates its own calendar)
  - Code display panel: reads the view's `.java` source from `demo-sources/` classpath resources
    (see "Code display mechanism" section above), extracts the region between `// DEMO-START`
    and `// DEMO-END`, renders in a collapsible `Details` + `Pre` component.
  - Opt-out mechanism for views that don't show code (Playground).
* Configure Maven resource copy: add `maven-resources-plugin` execution in `demo/pom.xml` to copy
  `src/main/java/org/vaadin/stefan/ui/view/demos/**/*.java` to `target/classes/demo-sources/`.
* Create a simplified CIP toolbar (for CIP views — no Entry-specific create/edit logic).
  See current `CalendarViewToolbar` at `demo/src/main/java/org/vaadin/stefan/ui/view/CalendarViewToolbar.java`
  for reference on toolbar structure.
* Update the existing `CalendarViewToolbar` to use only non-deprecated API internally
  (it stays Entry-typed, but should call `addCalendarItemClickedListener` etc. instead of
  deprecated equivalents).

Deliverable: `AbstractDemoView`, Maven resource config, CIP toolbar, cleaned-up `CalendarViewToolbar` — all compilable, no view using them yet.

---

_Phase 10.3 — Sidenav restructuring + stubs_

Goal: Set up the new navigation structure and create empty stubs for all planned views.

* Restructure `MainLayout` sidenav as a flat list with visual separator labels between groups:
  - `--- Playground ---`
  - `--- Basic Features ---` (Events View, Recurring & BG Items, Business Hours, Native Event Listeners, i18n)
  - `--- Calendar Item Provider ---` (In Memory CIP, Callback CIP, Backend CIP)
  - `--- Entry Provider ---` (In Memory EP, Callback EP, Backend EP)
  - `--- Multi Month ---`
  - `--- Callbacks ---` (Custom Properties, ItemClassname, ItemContent, ItemDidMount, ItemWillUnmount, ResourceLabelContent, resourceLaneContent, ...)
* Create stub classes for each new view (extending `AbstractDemoView`, with `@Route` annotation,
  placeholder title/description, empty calendar). Views compile but show nothing yet.
* Existing unlisted views remain in the codebase but are not added to the sidenav.

Deliverable: Demo app compiles and runs. Sidenav shows the full new structure. All stubs are navigable placeholders.

---

_Phase 10.4 — Validation view: Business Hours_ **[approval gate]**

Goal: Prove the `AbstractDemoView` design end-to-end with a simple, isolated demo.

* Implement the Business Hours demo view:
  - Creates a plain `FullCalendar<Entry>` with `InMemoryEntryProvider`.
  - Shows several combinations of business hours (weekday-only, split hours, custom per day).
  - Uses non-deprecated CIP API for everything except the EntryProvider itself.
  - Has `// DEMO-START` / `// DEMO-END` markers around the relevant code.
  - Has a warm, descriptive text above the calendar.
* Validate that code display works (collapsible panel shows the correct source extract).
* Validate the overall demo card layout (title, description, calendar, code panel).

Gate: ui-explorer test + manual approval. If the design needs changes, iterate on `AbstractDemoView` here
before migrating further views.

---

_Phase 10.5 — Playground_ **[approval gate]**

Goal: Streamline the Playground — the most complex view.

Starting point: `demo/src/main/java/org/vaadin/stefan/ui/view/demos/playground/Playground.java`
(formerly `FullDemo.java`). Read this file thoroughly before making changes.

* Stays Scheduler-based (`FullCalendarScheduler` / `ResourceEntry`).
* Uses the Entry-typed `CalendarViewToolbar`.
* No code display (exempt from `AbstractDemoView` code panel).
* Strip unnecessary sample data and complexity. Keep it functional for exploring features interactively.
  Core features to preserve: view switching, entry CRUD, resource management, drag/drop, resize.
* Use only non-deprecated API (event listeners, callbacks, editability methods etc.).
  Refer to the deprecated API inventory at the end of this document.

Gate: ui-explorer test + manual approval.

---

_Phase 10.6 — Events View_ **[approval gate]**

Goal: Demonstrate all FC event listeners with visible feedback.

* Creates a `FullCalendar<Entry>` with some sample items.
* Registers all available calendar event listeners (item clicked, item dropped, item resized,
  timeslot clicked, timeslots selected, dates rendered, more link clicked, day/week number clicked, etc.).
* Shows feedback for each fired event (e.g. a log panel or notification area below the calendar).
* Only non-deprecated API.

Gate: ui-explorer test + manual approval.

---

_Phase 10.7 — Recurring & BG Items_ **[approval gate]**

Goal: Show recurring events and background calendar items in isolation.

* Creates a `FullCalendar<Entry>` with various recurring entries (daily, weekly, custom recurrence rules)
  and background/inverse-background display modes.
* Reference `DemoCalendarWithBackgroundEvent` and existing samples for inspiration.
* Only non-deprecated API.

Gate: ui-explorer test + manual approval.

---

_Phase 10.8 — Native Event Listeners_ **[approval gate]**

Goal: Demonstrate browser-level native event listeners on calendar items.

* Creates a `FullCalendar<Entry>` with sample items.
* Registers native event listeners via `addItemNativeEventListener(...)`: mouse enter, mouse leave,
  contextmenu, and potentially others.
* Shows feedback for each native event.
* Only non-deprecated API.

Gate: ui-explorer test + manual approval.

---

_Phase 10.9 — i18n_ **[approval gate]**

Goal: Show timezone and locale customization.

* Creates a `FullCalendar<Entry>` with sample items spanning different times.
* Shows the browser timezone (via `BrowserTimezoneObtainedEvent`).
* Provides controls to change timezone and locale dynamically.
* Only non-deprecated API.

Gate: ui-explorer test + manual approval.

---

_Phase 10.10 — CIP: In Memory_ **[approval gate]**

Goal: Demonstrate CalendarItemProvider with an in-memory provider and a custom POJO model.

Reference existing CIP demos for patterns and inspiration:
- `demo/src/main/java/org/vaadin/stefan/ui/view/demos/entryproviders/` — existing entry provider demos
- Search for `CalendarItemProvider` usages in the demo module for any existing CIP examples.
- The POJO model can be a simple `Meeting` class (or similar) with fields like id, title, start, end,
  color. Create it fresh for the demo — do NOT reuse `Entry`.

* Uses a custom POJO as the model — NOT `Entry`.
* Uses `InMemoryCalendarItemProvider` with setter-based property mapper.
* Handles drag/drop and resize via setters on the POJO.
* Uses the simplified CIP toolbar (from Phase 10.2).
* Only non-deprecated API.

Gate: ui-explorer test + manual approval.

---

_Phase 10.11 — CIP: Callback_ **[approval gate]**

Goal: Demonstrate CalendarItemProvider with a callback provider and update handler.

* Uses the same or similar custom POJO model as Phase 10.10.
* Uses `CallbackCalendarItemProvider` with date-range-based fetching.
* Uses `CalendarItemUpdateHandler` for handling drag/drop and resize (immutable update pattern).
* Only non-deprecated API.

Gate: ui-explorer test + manual approval.

---

_Phase 10.12 — CIP: Backend_ **[approval gate]**

Goal: Demonstrate CalendarItemProvider with a simulated backend service.

* Uses the same or similar custom POJO model as Phase 10.10.
* Uses a simulated service layer (in-process `ConcurrentHashMap` store pretending to be a database).
  Create a simple `MeetingService` class that encapsulates the store.
* Shows realistic patterns: service call for fetching by date range, service call for persisting changes.
* The key difference to the Callback CIP demo is the explicit service abstraction layer.
* Only non-deprecated API.

Gate: ui-explorer test + manual approval.

---

_Phase 10.13 — Entry Provider demos (streamline)_ **[approval gate]**

Goal: Streamline the three existing Entry Provider demos.

* In Memory EP, Callback EP, Backend EP — these already exist and work.
* Remove unnecessary complexity and excess sample data.
* Replace any deprecated API calls with their non-deprecated equivalents.
* Add `// DEMO-START` / `// DEMO-END` markers and description text.
* Integrate into `AbstractDemoView` structure.

Gate: ui-explorer test + manual approval (can be reviewed as a batch since changes are small).

---

_Phase 10.14 — Multi Month Demo (streamline)_ **[approval gate]**

Goal: Streamline the existing Multi Month demo.

* Remove unnecessary code, simplify sample data.
* Replace any deprecated API calls.
* Add code markers and description text.
* Integrate into `AbstractDemoView` structure.

Gate: ui-explorer test + manual approval.

---

_Phase 10.15 — Callbacks: Custom Properties_ **[approval gate]**

Goal: Streamline the existing Custom Properties demo.

* Already exists — streamline and remove unnecessary code.
* Replace deprecated API calls.
* Add code markers and description text.
* Integrate into `AbstractDemoView` structure.

Gate: ui-explorer test + manual approval.

---

_Phase 10.16 — Callbacks: remaining_ **[approval gate]**

Goal: Create the remaining callback demo views.

* ItemClassname, ItemContent, ItemDidMount, ItemWillUnmount — each showing the respective
  `setItem*Callback(...)` with a practical example.
* ResourceLabelContent, resourceLaneContent — these require a Scheduler-capable calendar.
* Any other FC/Scheduler callbacks not yet covered (discover from the Java API surface
  and FullCalendar JS docs).
* Each view gets its own stub, code markers, and description text.
* Only non-deprecated API.

Gate: ui-explorer test + manual approval (can be reviewed in batches of related callbacks).

---

_Phase 10.17 — Cleanup_

Goal: Remove leftover code from the old demo structure.

* Delete the old abstract view hierarchy (`AbstractCalendarView`, `AbstractSchedulerView`)
  if no longer referenced by any view.
* Delete any sample/utility classes from the `samples/` package that are no longer referenced.
* Remove any commented-out sidenav entries in `MainLayout`.
* Final check: grep the entire demo module for any remaining deprecated API calls from
  the inventory and fix them.
* Final build + full E2E test run against `e2e-test-app/`.

Deliverable: Clean demo codebase, no deprecated API, no dead code, all tests green.
