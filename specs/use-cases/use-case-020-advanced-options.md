# UC-020: Advanced Options (setOption API)

**As a** Vaadin application developer, **I want to** set arbitrary FullCalendar options **so that** I can access FC features beyond what dedicated Java methods provide.

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon + addon-scheduler
**Related Options:** All `Option` and `SchedulerOption` enum constants, plus `setOption(String, Object)` for raw options
**Related Events:** —

---

## User-Facing Behavior

- Every option in the `Option` enum can be set via `setOption(Option, value)`
- Raw FC option names can be passed as strings: `setOption("someOption", value)`
- View-specific options override global options for particular views
- Options can be retrieved via `getOption(Option)`

---

## Java API Usage

```java
// Typed option
calendar.setOption(Option.EDITABLE, true);
calendar.setOption(Option.SLOT_DURATION, Duration.ofMinutes(15));
calendar.setOption(Option.LOCALE, Locale.GERMAN);

// Raw option (not in enum)
calendar.setOption("someNewFcOption", "value");

// View-specific override
calendar.setViewSpecificOption("dayGridMonth", Option.DAY_MAX_EVENT_ROWS, 3);
calendar.setViewSpecificOption("timeGrid", Option.SLOT_DURATION, Duration.ofMinutes(30));

// Scheduler option
scheduler.setOption(SchedulerOption.RESOURCE_AREA_WIDTH, "200px");

// Read back
Optional<Boolean> editable = calendar.getOption(Option.EDITABLE);
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | Option enum constants auto-convert names to camelCase FC option names |
| BR-02 | Some options have explicit `@JsonName` overrides (e.g., `MAX_ENTRIES_PER_DAY` → `"dayMaxEvents"`) |
| BR-03 | `@JsonConverter` on option enums handles type conversion (Duration, DayOfWeek, BusinessHours, etc.) |
| BR-04 | View-specific options are scoped to FC view name prefixes |
| BR-05 | Raw string options bypass validation — incorrect values may cause client-side errors |
| BR-06 | Options set via constructor `initialOptions` are NOT cached server-side — `getOption()` will return empty for these values. To read back or later override an option, set it via `setOption()` instead. |

---

## Acceptance Criteria

- [ ] All Option enum values can be set and take effect
- [ ] Raw string option names work
- [ ] View-specific options override global options
- [ ] Type converters handle Duration, DayOfWeek, Locale, BusinessHours correctly
- [ ] `getOption()` returns previously set values
- [ ] `getOption()` returns empty for values set only via `initialOptions` constructor

---

## Tests

### Unit Tests
- [ ] `FullCalendarOptionsTest` — option set/get
- [ ] `AdvancedOptionsTest` — raw options, view-specific options
- [ ] `ConverterTest` — type conversion

### E2E Tests
- [ ] `advanced-options.spec.js` — advanced option effects

---

## Related FullCalendar Docs

- [Options](https://fullcalendar.io/docs)
