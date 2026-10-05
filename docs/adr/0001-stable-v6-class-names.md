# Stable v6 class names via plugin defaults

FullCalendar 7 renders no semantic `.fc-*` class names anymore (only hashed, build-generated ones), which breaks every user stylesheet and our E2E selectors. We decided to re-add a curated subset of the v6 class names (e.g. `fc-event`, `fc-daygrid-day`, `fc-day-today`) through FullCalendar's `*Class` options, delivered as plugin option defaults, and to document them as stable public API. FullCalendar joins `*Class` values from theme plugins, plugin defaults and user options instead of overriding them, so these classes coexist with any FC theme and with user-supplied classes.

## Considered Options

- New addon-specific naming scheme (e.g. `fc-entry`): consistent with the entry vocabulary, but forces every user to rewrite all selectors.
- No own classes, only `data-date` / `data-time` / ARIA roles: no maintenance, but leaves users without a reasonable styling hook.

## Consequences

The curated class list becomes API we maintain across FullCalendar upgrades. Selectors relying on v6 DOM structure (e.g. `table td`) still break. Only class-based selectors survive.
