# UC-018: Accessibility

**As a** Vaadin application developer, **I want to** ensure the calendar is accessible **so that** keyboard-only users and screen reader users can interact with it.

**Status:** Implemented
**Date:** 2026-03-21

---

## Scope

**Addon module:** addon
**Related Options:** `Option.ENTRY_INTERACTIVE`, `Option.NATIVE_TOOLBAR_BUTTON_HINTS`, `Option.NAV_LINK_HINT`, `Option.MORE_LINK_HINT`, `Option.NATIVE_TOOLBAR_VIEW_HINT`, `Option.CLOSE_HINT`, `Option.TIME_HINT`, `Option.ENTRY_HINT`, `Option.LONG_PRESS_DELAY`, `Option.ENTRY_LONG_PRESS_DELAY`, `Option.SELECT_LONG_PRESS_DELAY`
**Related Events:** —

---

## User-Facing Behavior

- With `ENTRY_INTERACTIVE = true`, all entries receive `tabindex="0"` for keyboard focus
- Focused entries can be activated with Enter or Space
- Screen reader labels can be customized for toolbar buttons, nav links, more links, and entries
- Touch devices support long press for drag and selection (configurable delay)

---

## Java API Usage

```java
// Keyboard accessibility for entries
calendar.setOption(Option.ENTRY_INTERACTIVE, true);

// Screen reader labels
calendar.setOption(Option.NATIVE_TOOLBAR_BUTTON_HINTS,
    Map.of("prev", "Go to previous period", "next", "Go to next period"));
calendar.setOption(Option.NAV_LINK_HINT, "Navigate to $0");
calendar.setOption(Option.MORE_LINK_HINT, "$0 more events");
calendar.setOption(Option.ENTRY_HINT, "Event: $0");

// Touch device settings
calendar.setOption(Option.LONG_PRESS_DELAY, 500);
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | `ENTRY_INTERACTIVE = true` is recommended for WCAG 2.1 AA (SC 2.1.1 Keyboard) |
| BR-02 | Per-entry `interactive` overrides global setting |
| BR-03 | Hint strings use `$0` as placeholder for dynamic values |
| BR-04 | By default, only entries with a `url` are keyboard-focusable |
| BR-05 | Entry text on custom background colors must meet 4.5:1 contrast ratio (WCAG SC 1.4.3). The addon does NOT auto-calculate text color — developers must verify manually. |
| BR-06 | Focus indicators rely on FullCalendar's built-in styles. Verify the focus ring meets 3:1 contrast against the calendar background (WCAG SC 2.4.7 / 2.4.11). |
| BR-07 | The resize handle is 8px wide — this does not meet the 44x44px WCAG touch target recommendation. On touch devices, long press delay compensates partially. |
| BR-08 | Animations (drag revert, hover transitions) should respect `prefers-reduced-motion` media query. FullCalendar does not do this automatically. |

---

## Known Accessibility Gaps

- **No ARIA live regions**: When navigating to a new period, screen readers are not automatically notified of the content change (WCAG SC 4.1.3 Status Messages). This is a FullCalendar limitation.
- **"+N more" popover focus management**: FullCalendar's popover does not implement focus trapping or Escape-to-close-and-return-focus. This is a FullCalendar limitation.
- **No semantic landmark**: The calendar does not use `role="grid"` or ARIA landmarks — it relies on FC's internal ARIA implementation.

---

## Acceptance Criteria

- [ ] Entries are keyboard-focusable with `ENTRY_INTERACTIVE = true`
- [ ] Enter/Space activates focused entries
- [ ] Tab order follows logical reading order
- [ ] ARIA labels appear on toolbar buttons
- [ ] Screen reader hints work for nav links and more links
- [ ] Touch long press delay is configurable
- [ ] Focus indicator is visible on focused entries *(manual verification)*
- [ ] Entry text is readable against custom background colors — 4.5:1 contrast *(manual verification — see BR-05)*

---

## Tests

### Unit Tests
- [ ] `AccessibilityTouchTest` — touch/accessibility options

### E2E Tests
- [ ] `accessibility.spec.js` — keyboard navigation, ARIA attributes

---

## Related FullCalendar Docs

- [eventInteractive](https://fullcalendar.io/docs/eventInteractive)
- [Accessibility hints](https://fullcalendar.io/docs/hints)
