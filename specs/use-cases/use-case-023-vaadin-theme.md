# UC-023: Vaadin FullCalendar Theme

**As a** Vaadin application developer, **I want** the calendar to use the Vaadin FullCalendar theme by default **so that** the calendar visually matches other Vaadin components (Lumo/Aura styling), in light and dark color scheme.

**Status:** Implemented
**Date:** 2026-10-08

---

## Scope

**Addon module:** addon (the scheduler inherits it)
**Related Options:** `Option.COLOR_SCHEME`
**Related Events:** —

---

## User-Facing Behavior

- By default, the calendar uses the Vaadin FullCalendar theme (`FullCalendarTheme.VAADIN`, name `vaadin`)
- The theme is FullCalendar's classic theme, colored and sized by the Vaadin application theme (Lumo or Aura)
- The calendar inherits the application's font size and line height. Small text (day headers, day numbers, time slot labels, week numbers) uses the application theme's small font size
- Entries take the accent color of the application theme. An own color of an entry or resource wins
- Today's day number and day header are shown as a badge in the accent color
- Entries give visual feedback on hover, like with every theme (base styles of the add-on)
- The calendar follows the color scheme of the application. `Option.COLOR_SCHEME` overrides it for one calendar, its popover and a dragged entry
- The default theme is shown at once, without loading a theme and without the invisible phase of UC-027
- A calendar with the Vaadin theme and a classic calendar can share a page and each keeps its own colors
- Another FullCalendar theme can be selected per calendar (UC-027)

---

## Java API Usage

```java
// default, no call needed
calendar.setTheme(FullCalendarTheme.VAADIN);

// follow the application's color scheme
UI.getCurrent().getPage().setColorScheme(ColorScheme.Value.DARK);

// dark for this calendar only
calendar.setOption(Option.COLOR_SCHEME, "dark");

// FullCalendar's classic look instead
calendar.setTheme(FullCalendarTheme.CLASSIC);
```

Restyling, in an application stylesheet:

```css
.fc-vaadin { --fc-classic-event: #2e7d32; }
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | `FullCalendarTheme.VAADIN` is the default theme of every calendar |
| BR-02 | It is a FullCalendar theme plugin (`vaadin-theme.ts`) that builds on classic and puts the class `fc-vaadin` on the calendar, its popover and its entries, a dragged entry included. The rules are in `vaadin-theme.css` |
| BR-03 | The theme comes with the main module of the add-on (no on-demand load). `vaadin-theme.ts` imports classic's stylesheet, on which it builds. `FullCalendar` names `vaadin-theme.ts` and `vaadin-theme.css` in its `@JsModule` / `@CssImport`, because Vaadin decides on a frontend bundle rebuild only from the files named there |
| BR-04 | Colors derive from three base colors: background (`--lumo-base-color` / `--aura-surface-color-solid`), text (`--lumo-body-text-color` / `--aura-neutral`), accent (`--lumo-primary-color` / `--aura-accent-color`). Without Lumo and Aura the base `--vaadin-*` colors are the fallback. Neutral backgrounds, borders and secondary text are mixed from the text color |
| BR-05 | The rules set classic's `--fc-classic-*` variables on the outermost `.fc-vaadin` element (calendar, popover, dragged entry) inside the cascade layer `fc-palette` (ADR 0002). Unlayered application CSS overrides them without extra specificity. Entries inherit them from the calendar, so a class of the application on one calendar sets them for that calendar. Entries carry `fc-vaadin` themselves, so a rule for one calendar also names `.<class> .fc-vaadin` to win over an application rule on `.fc-vaadin` |
| BR-06 | Lumo and Aura switch colors through `color-scheme`, so the calendar follows the application's color scheme. `Option.COLOR_SCHEME` sets `data-color-scheme` on calendar, popover and dragged entry, which switches `color-scheme` for them |
| BR-07 | Limitation: in a Lumo application set to dark, `Option.COLOR_SCHEME` cannot make a calendar light. Lumo's dark mode (`theme="dark"` on `html`) sets fixed dark colors instead of `light-dark()` values. A dark calendar in a light Lumo application and both directions with Aura work |
| BR-08 | The theme can be changed at runtime via `setTheme` |
| BR-09 | The 7.x theme variant, its stylesheet and its `--vaadin-fc-*` variables do not exist anymore |

---

## Acceptance Criteria

- [x] Entries take the accent color of Lumo and Aura, light and dark (E2E)
- [x] The calendar background follows the application's color scheme (E2E)
- [x] `Option.COLOR_SCHEME` makes one calendar and its popover dark in a light application, with the theme's own colors and not classic's dark palette, also with a classic calendar on the page (E2E)
- [x] An entry dragged out of such a calendar keeps the theme's class, color scheme and accent color (E2E)
- [x] With Aura, `Option.COLOR_SCHEME` also makes one calendar light in a dark application (E2E)
- [x] The default theme is present at once, the calendar never gets `visibility: hidden` (E2E)
- [x] A classic calendar on the same page keeps classic's colors (E2E)
- [x] A class of the application on one calendar sets the colors of its entries, also over an application rule for all calendars (E2E)
- [x] Entries of an entry source take the theme like the calendar's own entries (E2E)
- [x] Today is shown as a badge (E2E)
- [x] Vaadin builds on classic, classic still loads its own stylesheet and palette (E2E)
- [x] The playground theme and mode selects switch Lumo/Aura and color scheme (E2E)
- [x] Switching the theme at runtime updates the appearance (UC-027)
- [x] Layout, font and spacing match other Vaadin components *(checked by screenshots in the devcontainer: Lumo and Aura, light and dark, month, week and scheduler timeline views)*

---

## Tests

### Unit Tests
- [x] `ThemeTest` (UC-027) covers the default theme. No dedicated unit tests, the styling needs a browser

### E2E Tests
- [x] `vaadin-theme.spec.js` against `VaadinThemeTestView` (`/test/vaadin-theme`). The test app switches Lumo/Aura and color scheme with query parameters (`?theme=aura&scheme=dark`, `AppTheme`). Covers Lumo light/dark and Aura light/dark (entries take the accent color, background follows the scheme), the option `colorScheme` per Lumo/Aura (calendar, popover, dragged entry, and light in a dark Aura app), the default theme present at once, a classic calendar on the same page, a class on one calendar setting its entry colors over a rule for all calendars, an entry of a JSON feed entry source, today's badge
- [x] `calendar-toolbar.spec.js` "Vaadin Theme and Color Scheme": the playground selects switch Lumo/Aura and color scheme
- [x] `theme.spec.js` "vaadin builds on classic, classic still loads its own stylesheet and palette"

---

## Related FullCalendar Docs

- [Custom themes](https://fullcalendar.io/docs/custom-themes)
- [Color palettes](https://fullcalendar.io/docs/color-palettes)
