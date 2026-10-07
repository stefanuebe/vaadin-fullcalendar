# UC-026: Scheduler — Virtualization and Printing

**As a** Vaadin application developer, **I want to** render only the visible resource rows of a large timeline and control how a scheduler prints **so that** calendars with many resources stay fast and printed output is usable.

**Status:** Implemented
**Date:** 2026-10-07

---

## Scope

**Addon module:** addon-scheduler
**Related Options:** `SchedulerOption.VIRTUALIZATION`, `SchedulerOption.ENTRY_PRINT_LAYOUT`, `SchedulerOption.PRINT_MAX_ROWS`
**Related Types:** `EntryPrintLayout`
**Related Events:** —

---

## User-Facing Behavior

- With virtualization on, a timeline view with resources keeps only the rows (and columns) in the scroll viewport in the DOM. Rows enter and leave the DOM while the user scrolls.
- Printing a scheduler (browser print dialog) uses FullCalendar's printer-friendly rendering. `FullCalendarScheduler` loads FullCalendar's `adaptive` premium plugin for this.
- In time grid views, the entry print layout decides whether entries are stacked or keep their grid position.
- When there are more rows than `PRINT_MAX_ROWS`, the extra rows are left out of the printout and the client logs a warning to the browser console.

---

## Java API Usage

```java
// Only render the visible resource rows (timeline views with resources)
scheduler.setOption(SchedulerOption.VIRTUALIZATION, true);

// Print layout of entries in time grid views
scheduler.setOption(SchedulerOption.ENTRY_PRINT_LAYOUT, EntryPrintLayout.STACK);

// Row limit of the printout (timeline views)
scheduler.setOption(SchedulerOption.PRINT_MAX_ROWS, 200);
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | `VIRTUALIZATION` takes a `boolean`, default `false`. It currently applies only to timeline views that show resources |
| BR-02 | `ENTRY_PRINT_LAYOUT` takes an `EntryPrintLayout` (`AUTO`, `STACK`, `GRID`), default `AUTO`: FullCalendar stacks in Firefox and uses the normal grid elsewhere |
| BR-03 | `PRINT_MAX_ROWS` takes a number, default 1000. It currently applies only to timeline views |
| BR-04 | `ENTRY_PRINT_LAYOUT` and `PRINT_MAX_ROWS` need the `adaptive` plugin. `FullCalendarScheduler` loads it, so both take effect without further setup. |
| BR-05 | Loading the `adaptive` plugin changes the print rendering of every scheduler compared to 7.x (see the migration guide) |

---

## Acceptance Criteria

- [x] `VIRTUALIZATION` and `PRINT_MAX_ROWS` reach the client as FullCalendar's `virtualization` and `printMaxRows`
- [x] `ENTRY_PRINT_LAYOUT` is sent as the client value of `EntryPrintLayout`
- [x] With `VIRTUALIZATION = true` and many resources, only the visible rows are in the DOM
- [x] The `adaptive` plugin is loaded by the scheduler client

---

## Tests

### Unit Tests
- [x] `SchedulerOptionsTest`: keys of the scheduler options, `EntryPrintLayout` set and read back
- [x] `OptionCompletenessTest`: every FullCalendar 7.1.0 option has a constant, or is left out with a reason

### E2E Tests
- [x] `scheduler-virtualization.spec.js`: with virtualization only the visible rows of a timeline with 300 resources are in the DOM, without it all of them; `RESOURCE_ROW_CLASS` reaches the rows. The browser's `beforeprint` event switches to FullCalendar's print rendering, which then holds `PRINT_MAX_ROWS` rows. That proves the adaptive plugin and the option reach the client

---

## Related FullCalendar Docs

- [virtualization](https://fullcalendar.io/docs/virtualization)
- [eventPrintLayout](https://fullcalendar.io/docs/eventPrintLayout)
- [printMaxRows](https://fullcalendar.io/docs/printMaxRows)
