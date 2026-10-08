import {joinClassNames, PluginInput} from 'fullcalendar';
import './demo-plain.css';

/**
 * A custom FullCalendar theme written from scratch, without a stock theme below it. It styles only what the month,
 * week and list views of the demo show, with the classes in demo-plain.css. The entries get the theme class too,
 * because FullCalendar adds the entry that follows the pointer while dragging to the body, outside the calendar. A
 * complete theme sets many more class name options, see https://fullcalendar.io/docs/render-hook-index.
 */
const demoPlainTheme: PluginInput = {
    name: 'demo-plain',
    optionDefaults: {
        className: 'demo-plain demo-plain-surface',
        viewClass: 'demo-plain-view',

        toolbarClass: 'demo-plain-toolbar',
        toolbarSectionClass: 'demo-plain-toolbar-section',
        toolbarTitleClass: 'demo-plain-title',
        buttonGroupClass: 'demo-plain-button-group',
        buttonClass: (info) => joinClassNames('demo-plain-button', info.isSelected && 'demo-plain-selected'),

        eventColor: 'var(--demo-plain-event)',
        eventContrastColor: 'var(--demo-plain-event-contrast)',
        eventClass: 'demo-plain',
        blockEventClass: 'demo-plain-block-entry',
        listItemEventClass: 'demo-plain-list-item-entry',
        listItemEventBeforeClass: 'demo-plain-dot',
        listItemEventInnerClass: 'demo-plain-list-item-inner',
        moreLinkClass: 'demo-plain-more',

        dayHeaderRowClass: 'demo-plain-lines',
        dayHeaderClass: 'demo-plain-lines',
        dayHeaderInnerClass: 'demo-plain-day-header',
        dayRowClass: 'demo-plain-lines',
        dayCellClass: (info) => joinClassNames('demo-plain-lines', info.isToday && 'demo-plain-today'),
        dayCellTopInnerClass: (info) => joinClassNames('demo-plain-day-number', info.isOther && 'demo-plain-other'),
        dayLaneClass: (info) => joinClassNames('demo-plain-lines', info.isToday && 'demo-plain-today'),
        slotLaneClass: 'demo-plain-lines',
        slotHeaderClass: 'demo-plain-lines',
        slotHeaderInnerClass: 'demo-plain-slot-label',
        allDayHeaderInnerClass: 'demo-plain-slot-label',
        weekNumberHeaderInnerClass: 'demo-plain-slot-label',
        allDayDividerClass: 'demo-plain-divider',
        nowIndicatorLineClass: 'demo-plain-now',
        inlineWeekNumberClass: 'demo-plain-week-number',

        popoverClass: 'demo-plain demo-plain-surface demo-plain-popover',
        popoverCloseClass: 'demo-plain-popover-close',
        popoverCloseContent: () => '×',

        listDayHeaderClass: 'demo-plain-list-day',
    },
};

export default demoPlainTheme;
