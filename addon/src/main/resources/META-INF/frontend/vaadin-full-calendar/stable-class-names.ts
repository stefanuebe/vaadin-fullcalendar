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

import {DayCellInfo, DayHeaderInfo, DayLaneInfo, EventDisplayInfo, joinClassNames, PluginInput} from 'fullcalendar';

/*
 * Re-adds the documented stable v6 class names (see docs/adr/0001-stable-v6-class-names.md).
 * FullCalendar joins these with theme and user classes, so they never replace them.
 * The option for each class follows FullCalendar's CSS migration guide (upgrading-from-v6-css).
 */

// indexed by "dow", 0 (Sunday) to 6
const WEEKDAYS = ['sun', 'mon', 'tue', 'wed', 'thu', 'fri', 'sat'];

// Day classes shared by header cells, body cells and timegrid lanes.
const dayClasses = (info: DayHeaderInfo | DayCellInfo | DayLaneInfo) =>
    joinClassNames(
        'fc-day',
        info.isToday && 'fc-day-today',
        info.isPast && 'fc-day-past',
        info.isFuture && 'fc-day-future',
        info.isOther && 'fc-day-other',
        'fc-day-' + WEEKDAYS[info.dow]
    );

// Classes shared by foreground and background entries.
const eventTimingClasses = (info: EventDisplayInfo) =>
    joinClassNames(
        info.isStart && 'fc-event-start',
        info.isEnd && 'fc-event-end',
        info.isPast && 'fc-event-past',
        info.isFuture && 'fc-event-future',
        info.isToday && 'fc-event-today'
    );

const plugin: PluginInput = {
    name: 'vaadin-stable-class-names',
    optionDefaults: {
        // root and view container
        className: 'fc',
        viewClass: (info) => joinClassNames('fc-view', `fc-${info.view.type}-view`),

        // toolbar and buttons (fc-<name>-button covers prev, next, today and the view buttons)
        toolbarClass: 'fc-toolbar',
        headerToolbarClass: 'fc-header-toolbar',
        footerToolbarClass: 'fc-footer-toolbar',
        toolbarTitleClass: 'fc-toolbar-title',
        buttonClass: (info) => joinClassNames('fc-button', `fc-${info.name}-button`, info.isSelected && 'fc-button-active'),

        // foreground entries (all views)
        eventClass: (info) => joinClassNames('fc-event', eventTimingClasses(info),
            info.isSelected && 'fc-event-selected',
            info.isDraggable && 'fc-event-draggable',
            info.isDragging && 'fc-event-dragging',
            (info.isStartResizable || info.isEndResizable) && 'fc-event-resizable',
            info.isResizing && 'fc-event-resizing',
            info.isMirror && 'fc-event-mirror'),
        eventBeforeClass: (info) => info.isStartResizable && 'fc-event-resizer fc-event-resizer-start',
        eventAfterClass: (info) => info.isEndResizable && 'fc-event-resizer fc-event-resizer-end',
        eventInnerClass: 'fc-event-main',
        eventTimeClass: 'fc-event-time',
        eventTitleClass: 'fc-event-title',

        // background entries
        backgroundEventClass: (info) => joinClassNames('fc-bg-event', eventTimingClasses(info)),

        // horizontal (daygrid, timeline) and vertical (timegrid) entries
        rowEventClass: 'fc-h-event',
        columnEventClass: 'fc-timegrid-event fc-v-event',

        // day header cells (the popover header is a day header, too)
        dayHeaderClass: (info) => joinClassNames(info.inPopover ? 'fc-popover-header' : 'fc-col-header-cell', dayClasses(info)),
        dayHeaderInnerClass: (info) => info.inPopover ? 'fc-popover-title' : 'fc-col-header-cell-cushion',

        // day body cells (the popover body is a day cell, too)
        dayCellClass: (info) => joinClassNames(info.inPopover ? 'fc-popover-body' : 'fc-daygrid-day', dayClasses(info)),
        dayCellTopClass: 'fc-daygrid-day-top',
        // FC renders this element only when the cell shows a day number, also for "3日" (CJK) and month-start texts
        dayCellTopInnerClass: 'fc-daygrid-day-number',
        dayCellInnerClass: 'fc-daygrid-day-events',
        inlineWeekNumberClass: 'fc-daygrid-week-number',

        // timegrid day lane, axis cells (slot header cells are set in the timeGrid view below)
        dayLaneClass: (info) => joinClassNames('fc-timegrid-col', dayClasses(info)),
        weekNumberHeaderClass: 'fc-timegrid-axis',
        allDayHeaderClass: 'fc-timegrid-axis',

        // more-link and popover
        moreLinkClass: 'fc-more-link',
        popoverClass: 'fc-popover fc-more-popover',
        popoverCloseClass: 'fc-popover-close',

        // list view (list items are set in the list view below)
        listDayHeaderClass: 'fc-list-day',
        // level 0 is the primary text (left), higher levels the secondary text (right)
        listDayHeaderInnerClass: (info) => joinClassNames('fc-list-day-cushion',
            info.level === 0 ? 'fc-list-day-text' : 'fc-list-day-side-text'),
        noEventsClass: 'fc-list-empty',

        // multimonth view
        singleMonthClass: 'fc-multimonth-month',
        singleMonthHeaderClass: 'fc-multimonth-title',

        // misc
        highlightClass: 'fc-highlight',
        nonBusinessHoursClass: 'fc-non-business',
    },

    // view specific classes. Plugin views are merged with the built-in view definitions.
    views: {
        dayGrid: {
            className: 'fc-daygrid',
            eventClass: 'fc-daygrid-event',
            rowEventClass: 'fc-daygrid-block-event',
            listItemEventClass: 'fc-daygrid-dot-event',
            listItemEventBeforeClass: 'fc-daygrid-event-dot',
            rowMoreLinkClass: 'fc-daygrid-more-link',
        },
        timeGrid: {
            className: 'fc-timegrid',
            // all-day entries and more-links are daygrid ones
            rowEventClass: 'fc-daygrid-event fc-daygrid-block-event',
            rowMoreLinkClass: 'fc-daygrid-more-link',
            columnMoreLinkClass: 'fc-timegrid-more-link',
            slotHeaderClass: (info) => joinClassNames('fc-timegrid-axis', 'fc-timegrid-slot', 'fc-timegrid-slot-label',
                info.isMinor && 'fc-timegrid-slot-minor'),
            slotLaneClass: (info) => joinClassNames('fc-timegrid-slot', 'fc-timegrid-slot-lane',
                info.isMinor && 'fc-timegrid-slot-minor'),
            nowIndicatorHeaderClass: 'fc-timegrid-now-indicator-arrow',
            nowIndicatorLineClass: 'fc-timegrid-now-indicator-line',
        },
        list: {
            className: 'fc-list',
            listItemEventClass: 'fc-list-event',
            listItemEventBeforeClass: 'fc-list-event-dot',
            listItemEventTimeClass: 'fc-list-event-time',
            listItemEventTitleClass: 'fc-list-event-title',
        },
        multiMonth: {
            className: 'fc-multimonth',
        },
    },
};

export default plugin;
