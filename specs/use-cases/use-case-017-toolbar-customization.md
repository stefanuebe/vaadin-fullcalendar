# UC-017: Toolbar Customization

**As a** Vaadin application developer, **I want to** customize the calendar toolbar **so that** the navigation buttons, title, and view switchers match my application's needs.

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon
**Related Options:** `Option.HEADER_TOOLBAR`, `Option.FOOTER_TOOLBAR`, `Option.TODAY_HINT`, `Option.PREV_HINT`, `Option.NEXT_HINT`, `Option.VIEW_HINT`, `Option.BUTTONS`, `Option.BUTTON_DISPLAY`, `Option.TOOLBAR_ELEMENTS`, `Option.TODAY_TEXT` and the other `*_TEXT` constants, `Option.TOOLBAR_CLASS`, `Option.HEADER_TOOLBAR_CLASS`, `Option.FOOTER_TOOLBAR_CLASS`, `Option.TOOLBAR_SECTION_CLASS`, `Option.TOOLBAR_TITLE_CLASS`, `Option.BUTTON_CLASS`, `Option.BUTTON_GROUP_CLASS`, `Option.HEADING_LEVEL`
**Related Events:** —

---

## User-Facing Behavior

- Header and footer toolbars can be configured with buttons for navigation, view switching, and title
- Button labels can be localized
- ARIA labels can be customized for accessibility
- Toolbar can be hidden entirely by setting it to `false`
- Built-in buttons can get their own text, hint and icon/text display, and custom buttons can be added and placed in a toolbar
- A custom button can run a JavaScript click handler in the browser
- Custom toolbar content (not a button) can be defined with `TOOLBAR_ELEMENTS`
- The toolbars, sections, title, buttons and button groups get CSS classes through their class options

---

## Java API Usage

```java
// Configure header
calendar.setOption(Option.HEADER_TOOLBAR,
    Map.of("start", "prev,next,today",
           "center", "title",
           "end", "dayGridMonth,timeGridWeek,timeGridDay"));

// Hide header toolbar
calendar.setOption(Option.HEADER_TOOLBAR, false);

// Footer toolbar
calendar.setOption(Option.FOOTER_TOOLBAR,
    Map.of("center", "prev,next"));

// Localize button text (FullCalendar's buttons option, one map per button)
calendar.setOption("buttons", Map.of(
    "today", Map.of("text", "Heute"),
    "dayGridMonth", Map.of("text", "Monat")));

// Built-in button text override and a custom button with a browser-side click handler.
// A custom button shows up only when its name is part of a toolbar string.
calendar.setOption(Option.BUTTONS, Map.of(
    ToolbarParts.TODAY, Map.<String, Object>of("text", "Go to today", "display", ButtonDisplay.TEXT),
    "refresh", Map.<String, Object>of(
        "text", "Refresh",
        "click", JsCallback.of("function(ev) { console.log('refresh clicked'); }"))));
calendar.setOption(Option.HEADER_TOOLBAR, Map.of(
    ToolbarParts.START, ToolbarParts.PREV + "," + ToolbarParts.NEXT + " " + ToolbarParts.TODAY + " refresh",
    ToolbarParts.CENTER, ToolbarParts.TITLE));

// Icon, text or both for all buttons (a button's own "display" wins)
calendar.setOption(Option.BUTTON_DISPLAY, ButtonDisplay.TEXT);

// Custom (non-button) toolbar content, placed by its name
calendar.setOption(Option.TOOLBAR_ELEMENTS, Map.of(
    "hint", JsCallback.of("function() { return { html: '<i>Drag to move</i>' }; }")));

// Classes for the toolbar parts
calendar.setOption(Option.TOOLBAR_CLASS, "my-toolbar");
calendar.setOption(Option.BUTTON_CLASS, JsCallback.of("function(info) { return info.isSelected ? 'active-button' : ''; }"));

// Level of the title in the document outline (default 2)
calendar.setOption(Option.HEADING_LEVEL, 3);

// ARIA labels for accessibility ($0 is the unit text of the current view)
calendar.setOption(Option.PREV_HINT, "Previous $0");
calendar.setOption(Option.NEXT_HINT, "Next $0");
calendar.setOption(Option.TODAY_HINT, "Go to today");
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | Toolbar maps accept keys `"start"`, `"center"`, `"end"` (FullCalendar also accepts `"left"` and `"right"`) |
| BR-02 | Button names: `prev`, `next`, `today`, `prevYear`, `nextYear`, `title`, and any FC view name. `ToolbarParts` holds the positions and built-in names as constants |
| BR-03 | Buttons separated by commas appear as a group; space-separated buttons have spacing between them |
| BR-04 | Setting toolbar to `false` hides it entirely |
| BR-05 | The toolbar model (`Header`, `Footer`, `HeaderFooterPart`, …) is deprecated in favour of the map |
| BR-06 | `BUTTONS` is a `Map` of button name to a `Map` of button properties (`text`, `hint`, `click`, `iconClass`, `iconContent`, `class` or `className`, `isPrimary`, `display`, `didMount`, `willUnmount`). The built-in names are the `ToolbarParts` constants. A custom button is shown only when its name is used in `HEADER_TOOLBAR` or `FOOTER_TOOLBAR` |
| BR-07 | `JsCallback` values inside the `BUTTONS` map (and in any `Map` or `Collection` option value) are sent as callbacks, set before or after attach. They run in the browser only and cannot call server code by themselves. Before 8.0 such a value arrived as text |
| BR-08 | `ButtonDisplay` is the value of `BUTTON_DISPLAY` and of a button's `display` property |
| BR-09 | `TOOLBAR_ELEMENTS` is a `Map` of name to `JsCallback` returning content (text, `{html}` or `{domNodes}`), placed through its name in a toolbar string |

---

## Acceptance Criteria

- [ ] Custom toolbar configuration renders correct buttons
- [ ] Button text localization works
- [ ] Toolbar can be hidden
- [ ] Footer toolbar works
- [ ] ARIA labels are applied to buttons
- [ ] A built-in button's text override and `display` reach the button
- [x] A custom button placed in a toolbar renders and its `click` callback runs in the browser, set before and after attach
- [ ] `TOOLBAR_ELEMENTS` content renders where its name is placed
- [x] Toolbar and button class options reach the elements

---

## Tests

### Unit Tests
- [x] `ToolbarOptionsTest`: toolbar maps, `false` and `null` reach the client, `ToolbarParts` builds the strings FullCalendar expects
- `TypedOptionValuesTest`: `BUTTONS` with nested `JsCallback` values reaches the client as callbacks before and after attach, `ButtonDisplay` is sent as its client value
- `OptionCompletenessTest` (addon-scheduler): every FullCalendar 7.1.0 option has a constant, or is listed with a reason

### E2E Tests
- [x] `advanced-options.spec.js`: header and footer toolbar set as maps, view button switches the view
- [x] `fc7-options.spec.js`: `BUTTONS` text override and custom button with click callback, set before and after attach; toolbar and button classes

---

## Related FullCalendar Docs

- [headerToolbar](https://fullcalendar.io/docs/headerToolbar)
- [footerToolbar](https://fullcalendar.io/docs/footerToolbar)
- [buttons](https://fullcalendar.io/docs/buttons)
- [buttonDisplay](https://fullcalendar.io/docs/buttonDisplay)
- [toolbarElements](https://fullcalendar.io/docs/toolbarElements)
- [Toolbar render hooks](https://fullcalendar.io/docs/toolbar-render-hooks)
- [prevHint, nextHint, todayHint](https://fullcalendar.io/docs/locale)
