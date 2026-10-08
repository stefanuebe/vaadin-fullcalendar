/*
   Copyright 2026, Stefan Uebe

   Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
   documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
   rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
   permit persons to whom the Software is furnished to do so, subject to the following conditions:

   The above copyright notice and this permission notice shall be included in all copies or substantial portions
   of the Software.

   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
   WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
   COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
   OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

   Exception of this license is the separately licensed part of the styles.
*/

import {joinClassNames, PluginInput} from 'fullcalendar';
import classicTheme from 'fullcalendar/themes/classic';
// @ts-ignore TypeScript knows no type of a plain CSS import
import 'fullcalendar/themes/classic/theme.css';

/**
 * Class names for the scheduler's resource views. Their options are typed by the scheduler package, which this module
 * does not import, so they are kept apart from the typed options below. A calendar without the scheduler ignores them.
 */
const schedulerClassNames = {
    resourceDayHeaderInnerClass: 'fc-vaadin-small',
    resourceColumnHeaderInnerClass: 'fc-vaadin-small',
};

/**
 * The FullCalendar Vaadin theme, the default theme of the add-on. It builds on FullCalendar's classic theme and adds
 * the class names that vaadin-theme.css styles. FullCalendar joins them with classic's class names.
 * <p>
 * Classic's stylesheet comes with this module. Its default palette is not needed, because vaadin-theme.css sets
 * classic's color variables.
 */
const vaadinTheme: PluginInput = {
    name: 'vaadin',
    deps: [classicTheme],
    optionDefaults: {
        className: 'fc-vaadin',
        popoverClass: 'fc-vaadin',
        // also reaches the entry a drag shows outside the calendar
        eventClass: 'fc-vaadin',
        dayHeaderInnerClass: (info) => joinClassNames('fc-vaadin-small', info.isToday && !info.inPopover && 'fc-vaadin-today'),
        dayCellTopInnerClass: (info) => joinClassNames('fc-vaadin-small', info.isToday && 'fc-vaadin-today'),
        slotHeaderInnerClass: 'fc-vaadin-small',
        weekNumberHeaderInnerClass: 'fc-vaadin-small',
        ...schedulerClassNames,
    },
};

export default vaadinTheme;

