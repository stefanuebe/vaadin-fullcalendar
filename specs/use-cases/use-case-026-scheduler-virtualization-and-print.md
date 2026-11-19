# UC-026: Scheduler — Virtualization and Printing

**As a** Vaadin application developer, **I want to** render only the visible resource rows of a large timeline and control how a scheduler prints **so that** calendars with many resources stay fast and printed output is usable.

**Status:** Implemented
**Date:** 2026-10-07

---

## Scope

**Addon module:** addon-scheduler
**Related Options:** `virtualization`, `entryPrintLayout`, `printMaxRows`
**Related Types:** `EntryPrintLayout`
**Related Events:** —

---

## User-Facing Behavior

- With virtualization on, a timeline view with resources keeps only the rows (and columns) in the scroll viewport in the DOM. Rows enter and leave the DOM while the user scrolls.
- Printing a scheduler (browser print dialog) uses FullCalendar's printer-friendly rendering. `FullCalendarScheduler` loads FullCalendar's `adaptive` premium plugin for this.
- In time grid views, the entry print layout decides whether entries are stacked or keep their grid position.
- When there are more rows than `printMaxRows`, the extra rows are left out of the printout and the client logs a warning to the browser console.

---

## Java API Usage

```java
// Only render the visible resource rows (timeline views with resources)
scheduler.options().setVirtualization(true);

// Print layout of entries in time grid views
scheduler.options().setEntryPrintLayout(EntryPrintLayout.STACK);

// Row limit of the printout (timeline views)
scheduler.options().setPrintMaxRows(200);
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | `virtualization` takes a `boolean`, default `false`. It currently applies only to timeline views that show resources |
| BR-02 | `entryPrintLayout` takes an `EntryPrintLayout` (`AUTO`, `STACK`, `GRID`), default `AUTO`: FullCalendar stacks in Firefox and uses the normal grid elsewhere |
| BR-03 | `printMaxRows` takes a number, default 1000. It currently applies only to timeline views |
| BR-04 | `entryPrintLayout` and `printMaxRows` need the `adaptive` plugin. `FullCalendarScheduler` loads it, so both take effect without further setup. |
| BR-05 | Loading the `adaptive` plugin changes the print rendering of every scheduler compared to 7.x (see the migration guide) |

---

## Acceptance Criteria

- [x] `virtualization` and `printMaxRows` reach the client as FullCalendar's `virtualization` and `printMaxRows`
- [x] `entryPrintLayout` is sent as the client value of `EntryPrintLayout`
- [x] With `virtualization = true` and many resources, only the visible rows are in the DOM
- [x] The `adaptive` plugin is loaded by the scheduler client

---

## Tests

### Unit Tests
- [x] `SchedulerOptionsTest`: `EntryPrintLayout` is sent as its client value
- [x] `OptionCompletenessTest`: every FullCalendar option has a field, or is left out with a reason

### E2E Tests
- [x] `scheduler-virtualization.spec.js`: with virtualization only the visible rows of a timeline with 300 resources are in the DOM, without it all of them; `resourceRowClass` reaches the rows. The browser's `beforeprint` event switches to FullCalendar's print rendering, which then holds `printMaxRows` rows. That proves the adaptive plugin and the option reach the client

---

## Related FullCalendar Docs

- [virtualization](https://fullcalendar.io/docs/virtualization)
- [eventPrintLayout](https://fullcalendar.io/docs/eventPrintLayout)
- [printMaxRows](https://fullcalendar.io/docs/printMaxRows)
