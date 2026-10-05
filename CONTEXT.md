# FullCalendar for Flow

A Vaadin Flow integration of the FullCalendar JavaScript library: Java components that let Vaadin developers configure and drive a FullCalendar instance from the server.

## Language

### Calendar content

**Entry**:
A single item shown on the calendar (a meeting, a booking, a holiday). FullCalendar JS calls it an "event".
_Avoid_: Event (reserved for Vaadin component events)

**Entry source**:
A provider of entries that the calendar loads on its own, outside the server-side entry provider (for example a JSON feed or an iCalendar URL). FullCalendar JS calls it an "event source".
_Avoid_: Event source, external entries

**Resource**:
A thing entries can be assigned to in the scheduler views (a room, a person, a machine).

### Configuration

**Option**:
A named FullCalendar setting, set on a calendar via the option API. Options are the primary way developers configure a calendar; option names follow the current FullCalendar JS names.
_Avoid_: Property, setting

### Styling

**FC theme**:
A complete visual design for the calendar provided by FullCalendar (classic, monarch, breezy, forma, pulse), or the addon's own Vaadin-aligned design built the same way. Chosen per calendar.
_Avoid_: Theme (alone; ambiguous with the Vaadin application theme)

**Palette**:
A color set for an FC theme. One FC theme can offer several palettes.
_Avoid_: Color theme, skin

**Color scheme**:
Light or dark rendering of a calendar's palette.
_Avoid_: Dark theme, mode

**Variant**:
A small Vaadin-style modifier applied to a calendar via its `theme` attribute, layered on top of an FC theme.
_Avoid_: Theme variant (when an FC theme is meant)

**Vaadin application theme**:
The Vaadin theme of the surrounding application (Lumo or Aura). Not part of the calendar, but the Vaadin FC theme takes its look from it.
