# UC-016: Custom JS Callbacks

**As a** Vaadin application developer, **I want to** define custom JavaScript callbacks for render hooks and constraints **so that** I can customize entry/resource rendering beyond what the Java API offers.

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon + addon-scheduler
**Related Options:** Various callback options (e.g., `ENTRY_ALLOW`, `SELECT_ALLOW`, `ENTRY_OVERLAP`, render hooks in `Option` and `SchedulerOption`, callbacks inside map values such as `Option.BUTTONS`)
**Related Events:** —

---

## User-Facing Behavior

- Developers can pass JavaScript functions as option values
- JS callbacks execute client-side for render customization, constraint validation, and formatting
- Entry extended props (`setExtendedProp`) are accessible in JS callbacks under `event.extendedProps`
- Scheduler render hooks customize resource label/lane rendering
- Every render hook FullCalendar 7.1.0 documents has a constant, grouped in families (see BR-08)
- A `JsCallback` can sit inside a `Map` or `Collection` option value, e.g. a button's `click` in `Option.BUTTONS`

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

// Scheduler: custom resource name in resource time grid / day grid headers
scheduler.setOption(SchedulerOption.RESOURCE_DAY_HEADER_CONTENT,
    JsCallback.of("function(info) { return { html: '<b>' + info.resource.title + '</b>' }; }"));

// Callback inside a map value: it is sent as a callback, not as text
calendar.setOption(Option.BUTTONS, Map.of("refresh", Map.<String, Object>of(
    "text", "Refresh",
    "click", JsCallback.of("function(ev) { console.log('refresh clicked'); }"))));

// One hook of another family: content of the "+more" link
calendar.setOption(Option.MORE_LINK_CONTENT,
    JsCallback.of("function(info) { return info.isNarrow ? info.numericText : info.longText; }"));

// Entry extended props (accessible in JS callbacks as event.extendedProps.priority etc.)
entry.setExtendedProp("priority", "high");
entry.setExtendedProp("department", "Engineering");
```

---

## Entry Render Hooks

In addition to constraint callbacks, the addon supports entry rendering hooks via options:

```java
// Custom entry content (e.g., HTML rendering)
calendar.setOption(Option.ENTRY_CONTENT,
    JsCallback.of("function(arg) { return { html: '<b>' + arg.event.title + '</b>' }; }"));

// CSS class names based on entry properties (one space-separated string, or a plain string without callback)
calendar.setOption(Option.ENTRY_CLASS,
    JsCallback.of("function(arg) { return arg.event.extendedProps.isUrgent ? 'urgent' : ''; }"));

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
| BR-03 | Extended props set via `setExtendedProp` are sent as the entry's `extendedProps` and are available as `event.extendedProps.<key>` in every entry callback, including the FullCalendar 7 split class hooks (`eventInnerClass`, `eventTitleClass`, …). No other key of the entry JSON lands in `extendedProps`, so none can overwrite an extended prop |
| BR-04 | Entry render hooks: `ENTRY_CONTENT`, `ENTRY_CLASS`, `ENTRY_DID_MOUNT`, `ENTRY_WILL_UNMOUNT`. They do not apply to background entries. Class options take a class name string or a callback returning one; FullCalendar 7 drops arrays. |
| BR-05 | Scheduler render hooks: `RESOURCE_CELL_*` (resource area cells, timeline views; also fires for group cells, then `info.resource` is absent), `RESOURCE_DAY_HEADER_*` (resource headers in resource time grid / day grid views), `RESOURCE_LANE_*` with `RESOURCE_LANE_TOP_CONTENT` / `RESOURCE_LANE_BOTTOM_CONTENT`, `RESOURCE_GROUP_HEADER_*` (the group value is `info.fieldValue`), `RESOURCE_COLUMN_HEADER_*`, etc. Class hooks take a class name string or a callback returning one. |
| BR-06 | Callbacks must be synchronous (no async/await) |
| BR-07 | Native DOM event listeners registered via `addEntryNativeEventListener(eventName, jsCode)` are automatically merged into `ENTRY_DID_MOUNT`. Example: `calendar.addEntryNativeEventListener("click", "console.log('clicked', e.target)")` registers a browser `click` handler on each entry's DOM element. |
| BR-08 | Render hook families with a constant each (`*_CLASS`, `*_INNER_CLASS` where FullCalendar has one, `*_CONTENT`, `*_DID_MOUNT`, `*_WILL_UNMOUNT`): block, row, column, list-item and background entries, "+more" links, the more-link popover, day cells, day headers, day rows and lanes, slot headers, tables, list days, single months, toolbar and buttons, now indicator, week numbers, no-entries message, and in the scheduler resource cells, headers, lanes, groups, expander, rows and the timeline. Which options exist and what each hook's `info` holds is in the Javadoc of `Option` / `SchedulerOption`. Not every hook has all four variants, and a few take a class name string only (no callback) |
| BR-09 | A `JsCallback` nested in a `Map` or `Collection` option value is sent as a callback, set before and after attach. Earlier versions sent it as text |
| BR-10 | Options without a constant (listeners the add-on wires itself, entry and resource data, plugins/locales/views, custom-view-only keys, framework-integration internals) are listed with reasons in `OptionCompletenessTest`. They can still be set with `setOption(String, …)` |

---

## Acceptance Criteria

- [ ] `ENTRY_ALLOW` callback can accept/reject drops
- [ ] `SELECT_ALLOW` callback can accept/reject selections
- [x] Extended props are accessible in JS callbacks via `extendedProps` (`entry-properties.spec.js`)
- [ ] Scheduler render hooks customize resource rendering
- [x] A representative hook of each new entry family and of the toolbar reaches the client and runs (`fc7-options.spec.js`). Day, slot and list-day hooks are covered by `display-options.spec.js`
- [x] A `JsCallback` inside the `BUTTONS` map runs on click
- [ ] Invalid JS does not crash the calendar — graceful degradation *(manual verification)*

---

## Tests

### Unit Tests
- [ ] `JsCallbackTest` — JsCallback construction, serialization
- [ ] `InteractionCallbacksTest` — callback options
- `TypedOptionValuesTest` — nested `JsCallback` values in map and collection options
- `OptionCompletenessTest` (addon-scheduler) — every FullCalendar 7.1.0 option and render hook has a constant, or is left out with a reason
- `SchedulerOptionsTest` — scheduler constants carry the FullCalendar 7 keys

### E2E Tests
- [ ] `interaction-callbacks.spec.js` — callback behavior
- [ ] `display-options.spec.js` (Render Hook Callbacks) — day cell, day header, inline week number and all-day header hooks (class, content, did-mount); a plain `eventClass` string set at runtime
- [x] `fc7-options.spec.js`: block, row, column, list-item and background entry classes, background entry content and did-mount, toolbar and button classes, `BUTTONS` click callbacks set before and after attach

---

## Related FullCalendar Docs

- [eventAllow](https://fullcalendar.io/docs/eventAllow)
- [selectAllow](https://fullcalendar.io/docs/selectAllow)
- [Render Hooks](https://fullcalendar.io/docs/content-injection)
- [buttons](https://fullcalendar.io/docs/buttons)
