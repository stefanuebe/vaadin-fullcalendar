# UC-017: Toolbar Customization

**As a** Vaadin application developer, **I want to** customize the calendar toolbar **so that** the navigation buttons, title, and view switchers match my application's needs.

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon
**Related Options:** `headerToolbar`, `footerToolbar`, `todayHint`, `prevHint`, `nextHint`, `viewHint`, `buttons`, `buttonDisplay`, `toolbarElements`, `todayText` and the other `*Text` fields, `toolbarClass`, `headerToolbarClass`, `footerToolbarClass`, `toolbarSectionClass`, `toolbarTitleClass`, `buttonClass`, `buttonGroupClass`, `headingLevel`
**Related Events:** —

---

## User-Facing Behavior

- Header and footer toolbars can be configured with buttons for navigation, view switching, and title
- Button labels can be localized
- ARIA labels can be customized for accessibility
- Toolbar can be hidden entirely by setting it to `false`
- Built-in buttons can get their own text, hint and icon/text display, and custom buttons can be added and placed in a toolbar
- A custom button can run a JavaScript click handler in the browser
- Custom toolbar content (not a button) can be defined with `toolbarElements`
- The toolbars, sections, title, buttons and button groups get CSS classes through their class options

---

## Java API Usage

```java
// Configure header
calendar.options().setHeaderToolbar(
    ToolbarValue.ofStartEnd("prev,next,today", "title",
                            "dayGridMonth,timeGridWeek,timeGridDay"));

// Hide header toolbar
calendar.options().setHeaderToolbar(ToolbarValue.NONE);

// Footer toolbar
calendar.options().setFooterToolbar(ToolbarValue.ofStartEnd("", "prev,next", ""));

// Localize button text (FullCalendar's buttons option, one map per button)
calendar.options().setButtons(Map.of(
    "today", Map.<String, Object>of("text", "Heute"),
    "dayGridMonth", Map.<String, Object>of("text", "Monat")));

// Built-in button text override and a custom button with a browser-side click handler.
// A custom button shows up only when its name is part of a toolbar string.
calendar.options().setButtons(Map.of(
    NativeToolbarParts.TODAY, Map.<String, Object>of("text", "Go to today", "display", NativeToolbarButtonDisplay.TEXT),
    "refresh", Map.<String, Object>of(
        "text", "Refresh",
        "click", JsCallback.of("function(ev) { console.log('refresh clicked'); }"))));
calendar.options().setHeaderToolbar(ToolbarValue.ofStartEnd(
    NativeToolbarParts.PREV + "," + NativeToolbarParts.NEXT + " " + NativeToolbarParts.TODAY + " refresh",
    NativeToolbarParts.TITLE, ""));

// Icon, text or both for all buttons (a button's own "display" wins)
calendar.options().setButtonDisplay(NativeToolbarButtonDisplay.TEXT);

// Custom (non-button) toolbar content, placed by its name
calendar.options().setToolbarElements(Map.of(
    "hint", JsCallback.of("function() { return { html: '<i>Drag to move</i>' }; }")));

// Classes for the toolbar parts
calendar.options().setToolbarClass("my-toolbar");
calendar.options().setButtonClass(JsCallback.of("function(info) { return info.isSelected ? 'active-button' : ''; }"));

// Level of the title in the document outline (default 2)
calendar.options().setHeadingLevel(3);

// ARIA labels for accessibility ($0 is the unit text of the current view)
calendar.options().setPrevHint("Previous $0");
calendar.options().setNextHint("Next $0");
calendar.options().setTodayHint("Go to today");
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | `ToolbarValue.ofStartEnd(start, center, end)` sets the three sections, which follow the text direction. `ToolbarValue.ofLeftRight(left, center, right)` keeps the outer sections on their screen side |
| BR-02 | Button names: `prev`, `next`, `today`, `prevYear`, `nextYear`, `title`, and any FullCalendar view name. `NativeToolbarParts` holds the positions and built-in names as constants |
| BR-03 | Buttons separated by commas appear as a group; space-separated buttons have spacing between them |
| BR-04 | `ToolbarValue.NONE` hides the toolbar entirely |
| BR-05 | The toolbar model (`Header`, `Footer`, `HeaderFooterPart`, …) is deprecated in favour of `ToolbarValue` |
| BR-06 | `buttons` is a `Map` of button name to a `Map` of button properties (`text`, `hint`, `click`, `iconClass`, `iconContent`, `class` or `className`, `isPrimary`, `display`, `didMount`, `willUnmount`). The built-in names are the `NativeToolbarParts` constants. A custom button is shown only when its name is used in `headerToolbar` or `footerToolbar` |
| BR-07 | `JsCallback` values inside the `buttons` map (and in any `Map` or `Collection` option value) are sent as callbacks, set before or after attach. They run in the browser only and cannot call server code by themselves. Before 8.0 such a value arrived as text |
| BR-08 | `NativeToolbarButtonDisplay` is the value of `buttonDisplay` and of a button's `display` property |
| BR-09 | `toolbarElements` is a `Map` of name to content: text, or a `JsCallback` returning content (text, `{html}` or `{domNodes}`). The element is placed through its name in a toolbar string |

---

## Acceptance Criteria

- [ ] Custom toolbar configuration renders correct buttons
- [ ] Button text localization works
- [ ] Toolbar can be hidden
- [ ] Footer toolbar works
- [ ] ARIA labels are applied to buttons
- [x] A button's text override and `display` reach the button
- [x] A custom button placed in a toolbar renders and its `click` callback runs in the browser, set before and after attach
- [x] `toolbarElements` content renders where its name is placed
- [x] Toolbar and button class options reach the elements

---

## Tests

### Unit Tests
- [x] `ToolbarOptionsTest`: toolbar values and `ToolbarValue.NONE` reach the client (`null` is tested in `AutoBrowserSettingsTest.headerToolbar_setToNull_isRemovedOnTheClient`), `NativeToolbarParts` builds the strings FullCalendar expects
- `TypedOptionValuesTest`: `buttons` with nested `JsCallback` values reaches the client as callbacks before and after attach, `NativeToolbarButtonDisplay` is sent as its client value
- `OptionCompletenessTest` (addon-scheduler): every FullCalendar option has a field, or is listed with a reason

### E2E Tests
- [x] `advanced-options.spec.js`: header and footer toolbar set, view button switches the view
- [x] `fc7-options.spec.js`: `buttons` text override, a button's `display`, custom button with click callback set before and after attach, `toolbarElements` with callback and plain text content, toolbar and button classes

---

## Related FullCalendar Docs

- [headerToolbar](https://fullcalendar.io/docs/headerToolbar)
- [footerToolbar](https://fullcalendar.io/docs/footerToolbar)
- [buttons](https://fullcalendar.io/docs/buttons)
- [buttonDisplay](https://fullcalendar.io/docs/buttonDisplay)
- [toolbarElements](https://fullcalendar.io/docs/toolbarElements)
- [Toolbar render hooks](https://fullcalendar.io/docs/toolbar-render-hooks)
- [prevHint, nextHint, todayHint](https://fullcalendar.io/docs/locale)
