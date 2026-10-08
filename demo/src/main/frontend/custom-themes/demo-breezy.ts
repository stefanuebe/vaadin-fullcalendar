import {joinClassNames, PluginInput} from 'fullcalendar';
import breezyTheme from 'fullcalendar/themes/breezy';
// @ts-ignore TypeScript knows no type of a plain CSS import
import 'fullcalendar/themes/breezy/theme.css';
import './demo-breezy.css';

/**
 * A custom FullCalendar theme built on FullCalendar's breezy theme. FullCalendar joins the class names below with
 * breezy's. The theme imports breezy's theme.css but none of breezy's palettes, demo-breezy.css sets breezy's color
 * variables instead. The popover and the entries get the theme class too, because FullCalendar adds the popover
 * and the entry that follows the pointer while dragging to the body, outside the calendar.
 */
const demoBreezyTheme: PluginInput = {
    name: 'demo-breezy',
    deps: [breezyTheme],
    optionDefaults: {
        className: 'demo-breezy',
        popoverClass: 'demo-breezy',
        eventClass: 'demo-breezy demo-breezy-entry',
        dayCellTopInnerClass: (info) => joinClassNames(info.isToday && 'demo-breezy-today'),
    },
};

export default demoBreezyTheme;
