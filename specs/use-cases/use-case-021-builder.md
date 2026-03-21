# UC-021: Calendar Builder

**As a** Vaadin application developer, **I want to** create and configure a calendar using a fluent builder **so that** I can set up the calendar with a clear, readable configuration.

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon + addon-scheduler
**Related Options:** —
**Related Events:** —

---

## User-Facing Behavior

- `FullCalendarBuilder` provides a fluent API to create and pre-configure a calendar
- Supports creating both `FullCalendar` and `FullCalendarScheduler` instances
- Entry limit, initial options, and scheduler license key can be set during construction

---

## Java API Usage

```java
// Basic calendar
FullCalendar calendar = FullCalendarBuilder.create().build();

// With entry limit
FullCalendar calendar = FullCalendarBuilder.create()
    .withEntryLimit(5)
    .build();

// Scheduler with license key
FullCalendarScheduler scheduler = FullCalendarBuilder.create()
    .withScheduler(Scheduler.GPL_V3_LICENSE_KEY)
    .build();

// Auto-detect browser timezone and locale
FullCalendar calendar = FullCalendarBuilder.create()
    .withAutoBrowserTimezone()
    .withAutoBrowserLocale()
    .build();

// Pre-populate with entries
FullCalendar calendar = FullCalendarBuilder.create()
    .withInitialEntries(List.of(entry1, entry2))
    .build();

// Custom entry content rendering (prefer setOption after build — withEntryContent is @Deprecated)
FullCalendar calendar = FullCalendarBuilder.create().build();
calendar.setOption(Option.ENTRY_CONTENT,
    JsCallback.of("function(arg) { return { html: arg.event.title }; }"));

// Custom calendar views (implement the CustomCalendarView interface — see UC-006)
FullCalendar calendar = FullCalendarBuilder.create()
    .withCustomCalendarViews(myThreeDayView)
    .build();

// With initial options (raw JSON)
ObjectNode options = JsonFactory.createObject();
options.put("editable", true);
FullCalendar calendar = FullCalendarBuilder.create()
    .withInitialOptions(options)
    .build();
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | `withScheduler()` configures the builder so that `build()` returns a `FullCalendarScheduler` instance directly — no cast needed |
| BR-02 | Without `withScheduler()`, a plain `FullCalendar` is created |
| BR-03 | Initial options bypass server-side caching |
| BR-04 | Entry limit is a constructor-time-only setting |
| BR-05 | `withAutoBrowserTimezone()` listens for `BrowserTimezoneObtainedEvent` and auto-sets the calendar timezone |
| BR-06 | `withAutoBrowserLocale()` auto-sets the locale from the browser |
| BR-07 | `withInitialEntries()` adds entries to the InMemoryEntryProvider after build. Throws `ClassCastException` if provider is not InMemoryEntryProvider (see UC-009 BR-07). |
| BR-08 | `withEntryContent(String)` is `@Deprecated` — use `setOption(Option.ENTRY_CONTENT, JsCallback.of(...))` after build instead |

---

## Acceptance Criteria

- [ ] Builder creates `FullCalendar` by default
- [ ] `withScheduler()` creates `FullCalendarScheduler`
- [ ] `withScheduler()` without license key also works (no-arg overload)
- [ ] Entry limit is applied
- [ ] Initial options are passed to the client
- [ ] `withAutoBrowserTimezone()` detects and applies browser timezone
- [ ] `withAutoBrowserLocale()` applies browser locale
- [ ] `withInitialEntries()` populates calendar with entries
- [ ] `withEntryContent()` sets custom entry rendering (deprecated — prefer `setOption`)
- [ ] `withCustomCalendarViews()` registers custom views

---

## Tests

### Unit Tests
- [ ] `FullCalendarBuilderTest` — builder configuration

---

## Related FullCalendar Docs

- N/A (addon-specific pattern)
