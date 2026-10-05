# Default palette in a CSS cascade layer, no palette API

FullCalendar 7 palettes are CSS files that set the theme's color variables on `:root`, so a palette always applies page-wide, and popovers and drag mirrors are rendered outside the calendar element. A per-calendar palette API would require rewriting all palette files to scoped selectors and injecting a class into every element FullCalendar renders outside the calendar. We decided to offer no palette API: the addon loads each theme's default palette inside a CSS cascade layer, so any palette or variable override a user loads unlayered always wins, regardless of load order. If Vaadin's frontend build cannot preserve the layer, 8.0 ships without palette support.

## Considered Options

- Per-calendar `setTheme(theme, palette)` with build-time rewritten, class-scoped palettes: possible later as a non-breaking overload if requested.
- A "disable default palette" flag on the calendar: rejected because palettes act page-wide, so a per-calendar flag promises more than it can deliver.
