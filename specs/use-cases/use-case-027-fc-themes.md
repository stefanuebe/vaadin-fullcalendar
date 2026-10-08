# UC-027: FullCalendar Themes

**As a** Vaadin application developer, **I want to** choose a FullCalendar theme per calendar, from FullCalendar's stock themes or my own, **so that** the calendar has the design I need without loading designs nobody uses.

**Status:** Implemented
**Date:** 2026-10-07

---

## Scope

**Addon module:** addon (the scheduler inherits it)
**Related Options:** — (the theme is not a FullCalendar option, FullCalendar takes it as a plugin)
**Related Events:** —

---

## User-Facing Behavior

- Each calendar renders with one FullCalendar theme. Default is the Vaadin FullCalendar theme (`FullCalendarTheme.VAADIN`, UC-023).
- The five FullCalendar stock themes can be selected: classic, monarch, breezy, forma, pulse. Each comes with its default palette (monarch purple, breezy indigo, forma blue, pulse red, classic its only palette).
- The browser loads monarch, breezy, forma and pulse only when a calendar on the page selects them. The Vaadin FullCalendar theme comes with the main module, and with it classic's plugin and stylesheet, on which it builds. Classic's palette loads when a calendar selects classic.
- Calendars with different themes can share a page.
- The theme can be changed while the calendar is shown. View, date and entries stay.
- A developer can register an own FullCalendar theme in the browser under a name and select it from Java like a stock theme.
- A name that is not registered logs an error to the browser console. The calendar keeps the theme it had, or shows without a theme if it had none yet.
- A theme that fails to load logs an error to the browser console. A calendar that was waiting for it shows without a theme.
- Until its initial theme has loaded, a calendar is rendered but invisible, so it never shows unstyled. The default theme needs no load and is shown at once. On a theme change it keeps the previous theme until the new one has loaded.

---

## Java API Usage

```java
FullCalendar calendar = new FullCalendar();   // Vaadin FullCalendar theme
calendar.setTheme(FullCalendarTheme.MONARCH);
calendar.getTheme();                          // "monarch"

calendar.setTheme("corporate");               // custom theme, registered in the browser
```

Registering a custom theme, in a frontend module the application loads with `@JsModule`:

```ts
import {FullCalendar} from 'Frontend/generated/jar-resources/vaadin-full-calendar/full-calendar';

// the loader returns the theme plugin, or a module whose default export is the plugin,
// and loads the theme's CSS itself
FullCalendar.registerTheme('corporate', () => import('./corporate-theme'));
```

---

## Business Rules

| ID | Rule |
|----|------|
| BR-01 | Theme names are plain strings. `FullCalendarTheme` holds the names of the themes that come with the add-on. It is no enum, so custom names work the same way. |
| BR-02 | `setTheme` rejects `null` (NullPointerException) and blank names (IllegalArgumentException). Any other name is passed to the browser unchanged. |
| BR-03 | The theme reaches the browser as the element property `fcTheme`. Flow restores it after detach and reattach. |
| BR-04 | Each theme is loaded once per page with a dynamic `import()`, so the frontend build puts it into its own chunk. Later calendars with the same theme reuse it. |
| BR-05 | A stock theme's `theme.css` and default palette are imported as text and added as one `<style data-fc-theme="<name>">` to the document head. The palette is wrapped in `@layer fc-palette`, so an unlayered palette or color variable of the application wins regardless of load order (ADR 0002). |
| BR-06 | FullCalendar reads plugins only when the calendar is created and in `resetOptions`. A theme that is known when the calendar is created is passed at creation. A theme that loads later, or a change of theme, is applied with `resetOptions({plugins})`, which keeps the view, the date and the options set via `setOption`. |
| BR-07 | A load result is used only while its theme is still the selected one. A theme that finishes loading or fails after the calendar switched to another theme changes nothing. |
| BR-08 | `FullCalendar.registerTheme(name, loader)` replaces a registration with the same name, also a stock theme's. Calendars that already show the replaced theme keep it until they select a theme again. A load of the replaced loader that is still running is discarded, and a calendar waiting for it loads with the new loader. |
| BR-09 | Theme plugins and the stable class names plugin (UC-025) both set `*Class` options. FullCalendar joins them, so the stable classes stay with every theme. |

---

## Acceptance Criteria

- [x] `setTheme(String)` / `getTheme()` on the calendar, constants `VAADIN`, `CLASSIC`, `MONARCH`, `BREEZY`, `FORMA`, `PULSE` in `FullCalendarTheme`.
- [x] Monarch, breezy, forma and pulse are loaded on demand. The production build of the e2e test app has one chunk per theme plugin, theme stylesheet and palette for them, and no code of them in the main bundle. Classic's plugin and stylesheet are in the main bundle with the Vaadin FullCalendar theme, its palette has its own chunk (checked by hand in the build output on 2026-10-08, no automated test).
- [x] A theme registered with `FullCalendar.registerTheme` can be selected from Java. An unknown name logs a console error.
- [x] Two calendars with different themes on one page render each with its own theme (E2E).
- [x] `HasTheme` and `FullCalendarVariant` are removed.

---

## Tests

### Unit Tests

- [x] `ThemeTest` (browserless): default theme, `setTheme` reaches the element property, custom names, `null` and blank rejected.

### E2E Tests

- [x] `theme.spec.js` against `ThemeTestView` (`/test/theme`): two themes on one page, lazy loading (`style[data-fc-theme]` appears only on selection, once), each stock theme with its palette, the application's palette override wins over the lazily loaded layered palette, a stock palette the application imports wins over the lazily loaded default palette, a theme switched to twice is loaded once, vaadin builds on classic, classic still loads its own stylesheet and palette, custom theme registration, console error for an unknown name, a theme change keeps an option set after attach, a theme change on a scheduler keeps its resource view, the theme is kept after detach and reattach, a calendar is invisible until its initial theme has loaded, a calendar waiting for its initial theme becomes visible when switched to an unknown name, calendars whose initial theme fails to load become visible and log the failure (two calendars waiting on one load, selecting the theme again loads it again), a loader replaced while it loads is the one that applies, also when the replaced loader fails afterwards, a theme that finishes loading after the calendar switched away changes nothing.

---

## Related FullCalendar Docs

- [Stock themes](https://fullcalendar.io/docs/stock-themes)
- [Color palettes](https://fullcalendar.io/docs/color-palettes)
- [Custom themes](https://fullcalendar.io/docs/custom-themes)
- ADR 0002 (`docs/adr/0002-default-palette-in-css-layer.md`)
