# FullCalendar for Flow

A Vaadin Flow integration of the FullCalendar JavaScript library: Java components that let Vaadin developers configure and drive a FullCalendar instance from the server.

The domain glossary for this project. Class names, method names, test names and conversation all use these words for these things.

## Language

### Calendar content

**Entry**:
A single item shown on the calendar (a meeting, a booking, a holiday). FullCalendar JS calls it an "event".
_Avoid_: Event (reserved for Vaadin component events)

**Entry source**:
A provider of entries that the browser loads on its own, outside the server-side entry provider (for example a JSON feed or an iCalendar URL). Called a remote entry source where it is set apart from the entry provider. FullCalendar JS calls it an "event source".
_Avoid_: Event source, external entries, client-side source

**Resource**:
A thing entries can be assigned to in the scheduler views (a room, a person, a machine).

**Extended prop**:
A value the developer attaches to an entry or resource that FullCalendar JS does not know itself, read in JavaScript callbacks as `extendedProps`. Named after FullCalendar's own term.
_Avoid_: Custom property

### Configuration

**Option**:
A named FullCalendar setting, set on a calendar via the option API. Options are the primary way developers configure a calendar; option names follow the current FullCalendar JS names.
_Avoid_: Property, setting

### Styling

**FullCalendar theme**:
A complete visual design for the calendar provided by FullCalendar (classic, monarch, breezy, forma, pulse), or the addon's own Vaadin-aligned design built the same way. Chosen per calendar.
_Avoid_: Theme (alone, ambiguous with the Vaadin application theme), FC theme

**FullCalendar Vaadin theme**:
The addon's own FullCalendar theme and the default (`FullCalendarTheme.VAADIN`). It builds on classic and takes its look from the Vaadin application theme.
_Avoid_: Vaadin FullCalendar theme (reads as "the theme of the Vaadin add-on FullCalendar"), Vaadin theme

**Palette**:
A color set for a FullCalendar theme. One FullCalendar theme can offer several palettes.
_Avoid_: Color theme, skin

**Color scheme**:
Light or dark rendering of a calendar's palette.
_Avoid_: Dark theme, mode

**Vaadin application theme**:
The Vaadin theme of the surrounding application (Lumo or Aura). Not part of the calendar, but the FullCalendar Vaadin theme takes its look from it.

### Plugins

**Plugin**:
A plugin of FullCalendar JS, the wrapped library (a view plugin, a theme plugin, the interaction plugin).
_Avoid_: bare "plugin" for a Maven or Claude Code plugin, which are always written out in full

### Project and testing

**Demo view**:
A view in `demo/` that shows the add-on off. Its author must stay free to change it, so no test reads it.
_Avoid_: example view

**Test view**:
A view in `e2e-test-app/` that a browser test owns and is free to change.
_Avoid_: fixture view

**Browserless**:
The test tier that exercises the server-side component with no browser and no JavaScript, between unit tests and browser tests.
_Avoid_: integration test, UI unit test

## Relationships

- A **FullCalendar theme** may offer several **Palettes**.
- A **Color scheme** selects light or dark rendering of the active palette.
- Browser tests drive **Test views**.
- **Browserless** tests build the component directly.
- Nothing tests a **Demo view**.

## Flagged ambiguities

- "Event" meant both a FullCalendar calendar item and a Vaadin component event. A calendar item is now always an **Entry**, and "event" is reserved for component events.
- "Theme" meant the Vaadin application theme, a FullCalendar theme and the addon's former theme variant. **FullCalendar theme** and **Vaadin application theme** are now always qualified, and theme variants no longer exist.
