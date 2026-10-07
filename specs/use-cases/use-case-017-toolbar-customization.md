# UC-017: Toolbar Customization

**As a** Vaadin application developer, **I want to** customize the calendar toolbar **so that** the navigation buttons, title, and view switchers match my application's needs.

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon
**Related Options:** `Option.HEADER_TOOLBAR`, `Option.FOOTER_TOOLBAR`, `Option.TODAY_HINT`, `Option.PREV_HINT`, `Option.NEXT_HINT`, `Option.VIEW_HINT`
**Related Events:** —

---

## User-Facing Behavior

- Header and footer toolbars can be configured with buttons for navigation, view switching, and title
- Button labels can be localized
- ARIA labels can be customized for accessibility
- Toolbar can be hidden entirely by setting it to `false`

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

---

## Acceptance Criteria

- [ ] Custom toolbar configuration renders correct buttons
- [ ] Button text localization works
- [ ] Toolbar can be hidden
- [ ] Footer toolbar works
- [ ] ARIA labels are applied to buttons

---

## Tests

### Unit Tests
- [x] `ToolbarOptionsTest`: toolbar maps, `false` and `null` reach the client, `ToolbarParts` builds the strings FullCalendar expects

### E2E Tests
- [x] `advanced-options.spec.js`: header and footer toolbar set as maps, view button switches the view

---

## Related FullCalendar Docs

- [headerToolbar](https://fullcalendar.io/docs/headerToolbar)
- [footerToolbar](https://fullcalendar.io/docs/footerToolbar)
- [buttons](https://fullcalendar.io/docs/buttons)
- [prevHint, nextHint, todayHint](https://fullcalendar.io/docs/locale)
