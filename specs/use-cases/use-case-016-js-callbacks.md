# UC-016: Custom JS Callbacks

**As a** Vaadin application developer, **I want to** define custom JavaScript callbacks for render hooks and constraints **so that** I can customize entry/resource rendering beyond what the Java API offers.

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon + addon-scheduler
**Related Options:** Various callback options (e.g., `ENTRY_ALLOW`, `SELECT_ALLOW`, `ENTRY_OVERLAP`, render hooks in SchedulerOption)
**Related Events:** —

---

## User-Facing Behavior

- Developers can pass JavaScript functions as option values
- JS callbacks execute client-side for render customization, constraint validation, and formatting
- Entry custom properties (`setCustomProperty`) are accessible in JS callbacks
- Scheduler render hooks customize resource label/lane rendering

---

## Java API Usage

```java
// Custom allow callback
calendar.setOption(Option.ENTRY_ALLOW,
    JsCallback.of("function(dropInfo, draggedEvent) { return dropInfo.start.getDay() !== 0; }"));

// Custom overlap callback
calendar.setOption(Option.ENTRY_OVERLAP,
    JsCallback.of("function(stillEvent, movingEvent) { return stillEvent.extendedProps.allowOverlap !== false; }"));

// Custom select allow
calendar.setOption(Option.SELECT_ALLOW,
    JsCallback.of("function(selectInfo) { return selectInfo.start.getDay() !== 0; }"));

// Scheduler: custom resource label
scheduler.setOption(SchedulerOption.RESOURCE_LABEL_CONTENT,
    JsCallback.of("function(arg) { return { html: '<b>' + arg.resource.title + '</b>' }; }"));

// Entry custom properties (accessible in JS callbacks)
entry.setCustomProperty("priority", "high");
entry.setCustomProperty("department", "Engineering");
```

---

## Entry Render Hooks

In addition to constraint callbacks, the addon supports entry rendering hooks via options:

```java
// Custom entry content (e.g., HTML rendering)
calendar.setOption(Option.ENTRY_CONTENT,
    JsCallback.of("function(arg) { return { html: '<b>' + arg.event.title + '</b>' }; }"));

// CSS class names based on entry properties
calendar.setOption(Option.ENTRY_CLASS_NAMES,
    JsCallback.of("function(arg) { if (arg.event.extendedProps.isUrgent) return ['urgent']; }"));

// Post-render setup (e.g., tooltips)
calendar.setOption(Option.ENTRY_DID_MOUNT,
    JsCallback.of("function(arg) { arg.el.title = arg.event.title; }"));

// Cleanup before removal
calendar.setOption(Option.ENTRY_WILL_UNMOUNT,
    JsCallback.of("function(arg) { /* cleanup */ }"));
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | `JsCallback.of(string)` wraps a JS function for client-side execution |
| BR-02 | JS callbacks use `new Function()` intentionally for dynamic evaluation |
| BR-03 | Custom properties set via `setCustomProperty` are available as `event.extendedProps` in JS |
| BR-04 | Entry render hooks: `ENTRY_CONTENT`, `ENTRY_CLASS_NAMES`, `ENTRY_DID_MOUNT`, `ENTRY_WILL_UNMOUNT` |
| BR-05 | Scheduler render hooks: `RESOURCE_LABEL_CONTENT`, `RESOURCE_LABEL_CLASS_NAMES`, `RESOURCE_LABEL_DID_MOUNT`, `RESOURCE_LANE_CONTENT`, etc. |
| BR-06 | Callbacks must be synchronous (no async/await) |
| BR-07 | Native DOM event listeners registered via `addEntryNativeEventListener(eventName, jsCode)` are automatically merged into `ENTRY_DID_MOUNT`. Example: `calendar.addEntryNativeEventListener("click", "console.log('clicked', e.target)")` registers a browser `click` handler on each entry's DOM element. |

---

## Acceptance Criteria

- [ ] `ENTRY_ALLOW` callback can accept/reject drops
- [ ] `SELECT_ALLOW` callback can accept/reject selections
- [ ] Custom properties are accessible in JS callbacks via `extendedProps`
- [ ] Scheduler render hooks customize resource rendering
- [ ] Invalid JS does not crash the calendar — graceful degradation *(manual verification)*

---

## Tests

### Unit Tests
- [ ] `JsCallbackTest` — JsCallback construction, serialization
- [ ] `InteractionCallbacksTest` — callback options

### E2E Tests
- [ ] `interaction-callbacks.spec.js` — callback behavior

---

## Related FullCalendar Docs

- [eventAllow](https://fullcalendar.io/docs/eventAllow)
- [selectAllow](https://fullcalendar.io/docs/selectAllow)
- [Render Hooks](https://fullcalendar.io/docs/content-injection)
