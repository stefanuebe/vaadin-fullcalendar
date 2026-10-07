# UC-023: Vaadin FC Theme

**As a** Vaadin application developer, **I want** the calendar to use the Vaadin FC theme by default **so that** the calendar visually matches other Vaadin components (Lumo/Aura styling).

**Status:** Draft. Since #265 the theme is selected through `setTheme` (UC-027) and the theme variant is removed. The Vaadin FC theme itself, with light and dark color scheme, is built in #266, which rewrites this spec. Until then `FullCalendarTheme.VAADIN` renders as the classic theme.
**Date:** 2026-10-07

---

## Scope

**Addon module:** addon
**Related Options:** —
**Related Events:** —

---

## User-Facing Behavior

- By default, the calendar uses the Vaadin FC theme
- The Vaadin FC theme adopts Vaadin Lumo/Aura colors, fonts, and spacing
- It affects toolbar buttons, entry styling, grid lines, and day headers to align with the surrounding Vaadin UI
- Another FC theme can be selected per calendar (UC-027)

---

## Java API Usage

```java
// default, no call needed
calendar.setTheme(FullCalendarTheme.VAADIN);

// FullCalendar's classic look instead
calendar.setTheme(FullCalendarTheme.CLASSIC);
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | `FullCalendarTheme.VAADIN` is the default theme of every calendar |
| BR-02 | It is an FC theme like the stock themes: a theme plugin plus CSS (#266) |
| BR-03 | The theme can be changed at runtime via `setTheme` |
| BR-04 | Custom CSS applied by the developer takes precedence over the theme styles |

---

## Acceptance Criteria

- [ ] Calendar with the `VAADIN` theme visually matches other Vaadin components *(manual verification)*
- [ ] Switching the theme at runtime updates the appearance
- [ ] Custom CSS overrides theme styles *(manual verification)*

---

## Tests

### Unit Tests
- [ ] No dedicated unit tests — visual theming requires browser

### E2E Tests
- [ ] No dedicated E2E tests for the Vaadin FC theme yet (#266).

---

## Related FullCalendar Docs

- N/A (addon-specific feature)
