# UC-023: Vaadin Theme Variant

**As a** Vaadin application developer, **I want to** apply the Vaadin theme variant to the calendar **so that** the calendar visually matches other Vaadin components (Lumo/Aura styling).

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon
**Related Options:** —
**Related Events:** —

---

## User-Facing Behavior

- By default, the calendar uses FullCalendar's native styling
- When the Vaadin theme variant is applied, the calendar adopts Vaadin Lumo/Aura colors, fonts, and spacing
- The variant affects toolbar buttons, entry styling, grid lines, and day headers to align with the surrounding Vaadin UI

---

## Java API Usage

```java
// Apply Vaadin theme
calendar.addThemeVariants(FullCalendarVariant.VAADIN);

// Remove Vaadin theme
calendar.removeThemeVariants(FullCalendarVariant.VAADIN);
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | `FullCalendarVariant.VAADIN` is the only available variant |
| BR-02 | The variant applies `full-calendar-theme-vaadin.css` via `@CssImport` |
| BR-03 | The variant can be toggled at runtime via `addThemeVariants` / `removeThemeVariants` |
| BR-04 | Custom CSS applied by the developer takes precedence over the variant styles |

---

## Acceptance Criteria

- [ ] Calendar with `VAADIN` variant visually matches other Vaadin components *(manual verification)*
- [ ] Calendar without variant uses default FC styling *(manual verification)*
- [ ] Toggling the variant at runtime updates the appearance
- [ ] Custom CSS overrides variant styles *(manual verification)*

---

## Tests

### Unit Tests
- [ ] No dedicated unit tests — visual theming requires browser

### E2E Tests
- [ ] No dedicated E2E tests for theme variant — coverage gap. Visual verification via Playwright MCP recommended.

---

## Related FullCalendar Docs

- N/A (addon-specific feature)
