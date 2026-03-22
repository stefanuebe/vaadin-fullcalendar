# Verification

> Testing strategy and visual verification process for the FullCalendar Flow addon.

---

## 1. Unit Tests (JUnit 5 + Mockito)

### Where

- `addon/src/test/java/org/vaadin/stefan/fullcalendar/` — core component tests
- `addon-scheduler/src/test/java/` — scheduler-specific tests

### How to Run

```bash
# All unit tests
mvn test

# Single test class
mvn test -pl addon -Dtest=EntryTest

# Single test method
mvn test -pl addon -Dtest=EntryTest#testSomeMethod

# Scheduler tests only
mvn test -pl addon-scheduler
```

### What to Test

- Entry model: property getters/setters, JSON serialization (`toJson()`), `updateFromJson()`
- Converters: Java ↔ JS type conversions (Duration, DayOfWeek, RRule, Locale, etc.)
- Options: setting/getting options, option name mapping
- Data providers: InMemoryEntryProvider CRUD, CallbackEntryProvider queries
- Builder: FullCalendarBuilder configuration
- Resource model: hierarchy, style overrides, JSON serialization
- ResourceEntry: resource assignment, cross-validation

### Naming Conventions

- Test class: `[Feature]Test.java` (e.g., `EntryTest`, `FullCalendarBuilderTest`)
- Test methods: descriptive names mapping to behavior (e.g., `settingStartUpdatesJson`, `recurringEntryWithRRule`)

---

## 2. E2E Tests (Playwright)

### Architecture

- **Test app** (`e2e-test-app/`): Vaadin Spring Boot app with dedicated test views, each exposing specific features
- **Test suite** (`e2e-tests/tests/*.spec.js`): Playwright specs that navigate to test views and verify behavior

### How to Run

```bash
# Full E2E run (builds app, starts server, runs Playwright, stops server)
cd e2e-test-app && mvn clean verify -Pit
```

**Important**: Do NOT pipe Maven output through `grep`/`tail`/`head` — this causes buffering issues and the process appears hung.

### Core Purpose of E2E Tests

E2E tests verify that the **server-side Java API produces the correct visual and behavioral effect in the browser**. Every E2E test should ultimately answer one or more of these questions:

1. When I set a **property on an entry**, does it have the expected effect in the client (e.g. the title, color, display mode)?
2. When I set an **option on the calendar**, does it have the expected effect in the client (e.g. the locale, editable state, hidden days)?
3. When I register a **server-side event listener**, is it properly notified from the client interaction?
4. When I use a server-side event listener, does the **event contain the correct data**?
5. When I use a server-side event listener and **update an entry/resource in the handler**, are the changes correctly shown in the client afterwards?

These five patterns form the foundation. The feature-specific tests below are concrete applications of them.

### What E2E Tests Cover

- Calendar views (dayGrid, timeGrid, list, multiMonth)
- Toolbar navigation and view switching
- Entry display, click events, hover events
- Drag-and-drop (entry moved, new start/end)
- Resize (entry resized, new end)
- Selection (timeslot click, range select)
- Scheduler features (resources, timeline views, resource DnD)
- Event sources (JSON feed, iCal)
- Accessibility (keyboard navigation, ARIA labels)
- Responsive behavior (mobile/desktop widths)

---

## 3. Mutation Testing

Mutation testing validates that tests actually catch bugs. A mutation introduces a deliberate defect; if no test fails, the test suite has a false positive.

### Tools & Scripts

**PIT (automated, unit tests):**
```bash
# Full mutation analysis via PIT Maven plugin (~25 seconds)
mvn test -pl addon -Ppit

# Report: addon/target/pit-reports/index.html
# Current: 551 mutations, 79% test strength (for covered code)
```

**Manual scripts (E2E + unit spot-checks):**
- `mutation-test-a.sh` — E2E mutations (requires app running in dev mode)
- `mutation-test-b.sh` — Unit test mutations (standalone, ~75 seconds)

```bash
# Category B (Unit Tests) — standalone
bash mutation-test-b.sh

# Category A (E2E) — requires app on port 8080
cd e2e-test-app && mvn spring-boot:run &  # start app first
bash mutation-test-a.sh
```

### What Gets Mutated

**PIT (automated):** Production addon source code. PIT generates ~551 mutations (conditional boundary changes, return value mutations, void method call removals, etc.) across Entry, RRule, BusinessHours, DisplayMode, Delta, FullCalendar, and InMemoryEntryProvider. Runs all unit tests against each mutation.

**E2E scripts (Category A):** Java test view files are mutated (entry colors, titles, options, listener registrations). The affected Playwright spec is run. If the spec still passes, the test is a false positive.

**Unit scripts (Category B):** Targeted production source mutations (field defaults, JSON keys, enum values). Supplements PIT for specific regression scenarios.

### Kill Rate Targets

| Tool | Target | Current |
|---|---|---|
| PIT (unit, test strength) | ≥ 80% | 79% |
| Manual E2E (Category A) | ≥ 95% | 100% |
| Manual Unit (Category B) | ≥ 80% | 83% |

### When to Run

- **PIT:** After adding or changing unit tests. Before releases as quality gate.
- **Manual scripts:** After adding E2E tests. After refactoring test assertions.
- **Both:** When investigating whether a test actually provides regression protection.

---

## 4. Visual Verification Process (Playwright MCP)

Use the Playwright MCP server to visually verify changes during development.

### When to Verify

- After implementing a use case
- After changing styles, theme, or layout
- After any change that affects rendering

### Default Browser Resolution

Unless the use case specifies otherwise, use **1920x1080**.

### Steps

1. Ensure the application is running (`cd demo && mvn spring-boot:run -Pproduction` or `cd e2e-test-app && mvn spring-boot:run`)
2. Navigate to the relevant view
3. Walk through the use case's main flow
4. Take screenshots at key interaction points
5. Check: layout, typography, interactive elements, responsive behavior
6. Record results in the per-use-case checklist below

---

## 5. Per-Use-Case Verification Checklist

> Copy this section for each use case.

### UC-[NNN]: [Feature Title]

**Use case spec:** [`use-case-NNN-name.md`](use-cases/use-case-NNN-name.md)
**Verified by:** [Name/Agent]
**Date:** [YYYY-MM-DD]

#### Automated Tests

- [ ] Unit test class exists and all tests pass
- [ ] E2E test spec covers the feature (if applicable)
- [ ] Acceptance criteria covered by tests

#### Functional

- [ ] Main flow works end-to-end as described in the spec
- [ ] All business rules are enforced
- [ ] Edge cases handled appropriately

#### Visual

- [ ] Calendar renders correctly in the relevant view(s)
- [ ] Entries display with correct colors, positions, and labels
- [ ] Interactive elements respond correctly (hover, focus, click, drag)
- [ ] Responsive at mobile and desktop widths

#### Result

- **Status:** [Pass / Fail / Partial]
- **Notes:** [Any issues found or follow-up items]
