# UC-017: Toolbar Customization

**As a** Vaadin application developer, **I want to** customize the calendar toolbar **so that** the navigation buttons, title, and view switchers match my application's needs.

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon
**Related Options:** `Option.HEADER_TOOLBAR`, `Option.FOOTER_TOOLBAR`, `Option.NATIVE_TOOLBAR_BUTTON_TEXT`, `Option.NATIVE_TOOLBAR_BUTTON_HINTS`, `Option.NATIVE_TOOLBAR_DEFAULT_RANGE_SEPARATOR`, `Option.NATIVE_TOOLBAR_TITLE_RANGE_SEPARATOR`, `Option.NATIVE_TOOLBAR_VIEW_HINT`
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
    Map.of("left", "prev,next,today",
           "center", "title",
           "right", "dayGridMonth,timeGridWeek,timeGridDay"));

// Hide header toolbar
calendar.setOption(Option.HEADER_TOOLBAR, false);

// Footer toolbar
calendar.setOption(Option.FOOTER_TOOLBAR,
    Map.of("center", "prev,next"));

// Localize button text
calendar.setOption(Option.NATIVE_TOOLBAR_BUTTON_TEXT,
    Map.of("today", "Heute", "month", "Monat", "week", "Woche", "day", "Tag"));

// ARIA labels for accessibility
calendar.setOption(Option.NATIVE_TOOLBAR_BUTTON_HINTS,
    Map.of("prev", "Previous period", "next", "Next period", "today", "Go to today"));
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | Toolbar maps accept keys `"left"`, `"center"`, `"right"` |
| BR-02 | Button names: `prev`, `next`, `today`, `prevYear`, `nextYear`, `title`, and any FC view name |
| BR-03 | Buttons separated by commas appear as a group; space-separated buttons have spacing between them |
| BR-04 | Setting toolbar to `false` hides it entirely |

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
- [ ] No dedicated unit tests — toolbar is covered by E2E tests

### E2E Tests
- [ ] `calendar-toolbar.spec.js` — toolbar rendering and customization

---

## Related FullCalendar Docs

- [headerToolbar](https://fullcalendar.io/docs/headerToolbar)
- [footerToolbar](https://fullcalendar.io/docs/footerToolbar)
- [buttonText](https://fullcalendar.io/docs/buttonText)
