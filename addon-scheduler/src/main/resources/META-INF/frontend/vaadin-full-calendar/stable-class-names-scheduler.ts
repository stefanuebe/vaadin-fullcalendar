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

import {joinClassNames} from 'fullcalendar';

/*
 * Re-adds the documented stable v6 class names of the scheduler views (see docs/adr/0001-stable-v6-class-names.md).
 * FullCalendar joins these with theme and user classes, so they never replace them.
 * The option for each class follows FullCalendar's CSS migration guide (upgrading-from-v6-css).
 */

// Typed loosely, because the scheduler options are not part of the core CalendarOptions type.
const plugin: any = {
    name: 'vaadin-stable-class-names-scheduler',
    optionDefaults: {
        // datagrid cells: column header, group header and resource cell
        resourceColumnHeaderClass: 'fc-datagrid-cell',
        resourceColumnHeaderInnerClass: 'fc-datagrid-cell-cushion',
        resourceGroupHeaderClass: 'fc-datagrid-cell fc-resource-group',
        resourceGroupHeaderInnerClass: 'fc-datagrid-cell-cushion',
        resourceCellClass: 'fc-datagrid-cell fc-resource',
        resourceCellInnerClass: 'fc-datagrid-cell-cushion',
        resourceColumnDividerClass: 'fc-resource-timeline-divider',

        // timeline lanes
        resourceLaneClass: 'fc-timeline-lane fc-resource',
        resourceGroupLaneClass: 'fc-timeline-lane fc-resource-group',

        // resource day headers, cells and lanes in resource daygrid and timegrid views
        resourceDayHeaderClass: 'fc-resource',
        dayHeaderClass: (info: any) => info.resource && 'fc-resource',
        dayCellClass: (info: any) => info.resource && 'fc-resource',
        dayLaneClass: (info: any) => info.resource && 'fc-resource',

        // timeline slot header row
        slotHeaderRowClass: 'fc-timeline-header-row',
    },

    // view specific classes. Plugin views are merged with the built-in view definitions.
    views: {
        // resourceTimeline inherits from timeline
        timeline: {
            className: 'fc-timeline',
            slotHeaderClass: (info: any) => joinClassNames('fc-timeline-slot', 'fc-timeline-slot-label',
                info.isMinor && 'fc-timeline-slot-minor'),
            slotLaneClass: (info: any) => joinClassNames('fc-timeline-slot', 'fc-timeline-slot-lane',
                info.isMinor && 'fc-timeline-slot-minor'),
            rowEventClass: 'fc-timeline-event',
            rowMoreLinkClass: 'fc-timeline-more-link',
            nowIndicatorHeaderClass: 'fc-timeline-now-indicator-arrow',
            nowIndicatorLineClass: 'fc-timeline-now-indicator-line',
        },
        resourceTimeline: {
            className: 'fc-resource-timeline',
        },
    },
};

export default plugin;
