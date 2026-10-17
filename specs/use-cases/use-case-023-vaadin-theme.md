# UC-023: FullCalendar Vaadin Theme

**As a** Vaadin application developer, **I want** the calendar to use the FullCalendar Vaadin theme by default **so that** the calendar visually matches other Vaadin components (Lumo/Aura styling), in light and dark color scheme.

**Status:** Implemented
**Date:** 2026-10-08

---

## Scope

**Addon module:** addon (the scheduler inherits it)
**Related Options:** `colorScheme`
**Related Events:** —

---

## User-Facing Behavior

- By default, the calendar uses the FullCalendar Vaadin theme (`FullCalendarTheme.VAADIN`, name `vaadin`)
- The theme is FullCalendar's classic theme, colored and sized by the Vaadin application theme (Lumo or Aura)
- The calendar inherits the application's font size and line height. Small text (day headers, day numbers, time slot labels, week numbers) uses the application theme's small font size
- Entries take the accent color of the application theme. An own color of an entry or resource wins
- Today's day number and day header are shown as a badge in the accent color
- Hovering a list-item entry, a more link or an entry of the list view shows a light accent
- The application restyles the theme with `--vaadin-fc-*` variables named by purpose, on `html` or a class of one calendar
- Entries give visual feedback on hover, like with every theme (base styles of the add-on)
- The calendar follows the color scheme of the application. `colorScheme` overrides it for one calendar, its popover and a dragged entry
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
calendar.options().setColorScheme("dark");

// FullCalendar's classic look instead
calendar.setTheme(FullCalendarTheme.CLASSIC);
```

Restyling, in an application stylesheet:

```css
html { --vaadin-fc-accent-color: #2e7d32; }
.my-calendar { --vaadin-fc-entry-color: #c62828; }
```

The variables and their defaults are listed in `specs/design-system.md` and on the wiki page Themes.

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | `FullCalendarTheme.VAADIN` is the default theme of every calendar |
| BR-02 | It is a FullCalendar theme plugin (`vaadin-theme.ts`) that builds on classic and puts the class `fc-vaadin` on the calendar, its popover and its entries, a dragged entry included. The rules are in `vaadin-theme.css` |
| BR-03 | The theme comes with the main module of the add-on (no on-demand load). `vaadin-theme.ts` imports classic's stylesheet, on which it builds. `FullCalendar` names `vaadin-theme.ts` and `vaadin-theme.css` in its `@JsModule` / `@CssImport`, because Vaadin decides on a frontend bundle rebuild only from the files named there |
| BR-04 | Colors derive from four base colors, `--vaadin-fc-bg-color`, `--vaadin-fc-text-color`, `--vaadin-fc-accent-color` and `--vaadin-fc-accent-text-color`, whose defaults are: background (`--lumo-base-color` / `--aura-surface-color-solid`), text (`--lumo-body-text-color` / `--aura-neutral`), accent (`--lumo-primary-color` / `--aura-accent-color`), accent text (`--lumo-primary-text-color` / `--aura-accent-text-color`). Without Lumo and Aura the base `--vaadin-*` colors are the fallback. Neutral backgrounds, borders and secondary text are mixed from the text color |
| BR-05 | The rules set classic's `--fc-classic-*` variables on the outermost `.fc-vaadin` element (calendar, popover, dragged entry) inside the cascade layer `fc-palette` (ADR 0002). Unlayered application CSS overrides them without extra specificity. Entries inherit them from the calendar, so a class of the application on one calendar sets them for that calendar. Entries carry `fc-vaadin` themselves, so a rule that overrides `--fc-classic-*` for one calendar also names `.<class> .fc-vaadin` to win over an application rule on `.fc-vaadin`. The `--vaadin-fc-*` variables need no such second selector, because the theme does not set them on the entries |
| BR-06 | Lumo and Aura switch colors through `color-scheme`, so the calendar follows the application's color scheme. `colorScheme` sets `data-color-scheme` on calendar, popover and dragged entry, which switches `color-scheme` for them |
| BR-07 | Limitation: in a Lumo application set to dark, `colorScheme` cannot make a calendar light. Lumo's dark mode (`theme="dark"` on `html`) sets fixed dark colors instead of `light-dark()` values. A dark calendar in a light Lumo application and both directions with Aura work |
| BR-08 | The theme can be changed at runtime via `setTheme` |
| BR-09 | The 7.x theme variant and its stylesheet do not exist anymore. Of its 17 `--vaadin-fc-*` variables, three keep their name. The theme reads none of the names that changed, and there is no fallback |
| BR-10 | The theme reads the `--vaadin-fc-*` variables named by purpose (`specs/design-system.md`) and never defines them. It reads each with its default as fallback. The hover default is a light accent (10% accent). The other defaults are the colors the theme had before it read the variables |
| BR-11 | Hover, focus and active of list-item entries, more links and list view entries, and the non-business hours, are colored by classes the plugin adds under classic's conditions (`fc-vaadin-hover`, `fc-vaadin-muted-pressable`, `fc-vaadin-faint-pressable`, `fc-vaadin-non-business`), not by reassigning classic's faint, muted and strong tones, which classic also uses for surfaces |

---

## Acceptance Criteria

- [x] Entries take the accent color of Lumo and Aura, light and dark (E2E)
- [x] The calendar background follows the application's color scheme (E2E)
- [x] `colorScheme` makes one calendar and its popover dark in a light application, with the theme's own colors and not classic's dark palette, also with a classic calendar on the page (E2E)
- [x] An entry dragged out of such a calendar keeps the theme's class, color scheme and accent color (E2E)
- [x] With Aura, `colorScheme` also makes one calendar light in a dark application (E2E)
- [x] The default theme is present at once, the calendar never gets `visibility: hidden` (E2E)
- [x] A classic calendar on the same page keeps classic's colors (E2E)
- [x] A class of the application on one calendar sets the colors of its entries, also over an application rule for all calendars (E2E)
- [x] Entries of an entry source take the theme like the calendar's own entries (E2E)
- [x] Today is shown as a badge (E2E)
- [x] `--vaadin-fc-accent-color` set on `html` colors the entries, `--vaadin-fc-entry-color` set by a class on one calendar colors only that calendar's entries (E2E)
- [x] The hover color is a light accent and follows `--vaadin-fc-hover-color` (E2E)
- [x] Without any variable set, the computed colors of entries, background entries, selection, today, now indicator, borders, non-business hours, focus and active states and the small font size are unchanged, for Lumo and Aura, light and dark
- [x] Vaadin builds on classic, classic still loads its own stylesheet and palette (E2E)
- [x] The playground theme and mode selects switch Lumo/Aura and color scheme (E2E)
- [x] Switching the theme at runtime updates the appearance (UC-027)
- [x] Layout, font and spacing match other Vaadin components *(checked by screenshots in the devcontainer: Lumo and Aura, light and dark, month, week and scheduler timeline views)*

---

## Tests

### Unit Tests
- [x] `ThemeTest` (UC-027) covers the default theme. No dedicated unit tests, the styling needs a browser

### E2E Tests
- [x] `vaadin-theme.spec.js` against `VaadinThemeTestView` (`/test/vaadin-theme`). The test app switches Lumo/Aura and color scheme with query parameters (`?theme=aura&scheme=dark`, `AppTheme`). Covers Lumo light/dark and Aura light/dark (entries take the accent color, background follows the scheme), the option `colorScheme` per Lumo/Aura (calendar, popover, dragged entry, and light in a dark Aura app), the default theme present at once, a classic calendar on the same page, a class on one calendar setting its entry colors over a rule for all calendars, an entry of a JSON feed entry source, today's badge, the accent color on `html`, the entry color by a class on one calendar, the hover color
- [x] `calendar-toolbar.spec.js` "Vaadin Theme and Color Scheme": the playground selects switch Lumo/Aura and color scheme
- [x] `theme.spec.js` "vaadin builds on classic, classic still loads its own stylesheet and palette"

---

## Related FullCalendar Docs

- [Custom themes](https://fullcalendar.io/docs/custom-themes)
- [Color palettes](https://fullcalendar.io/docs/color-palettes)
