/*
 * Copyright 2020, Stefan Uebe
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.vaadin.stefan.fullcalendar;

/**
 * FC options that accept a JavaScript function value. Use with
 * {@link FullCalendar#setCallbackOption(CallbackOption, String)}.
 * <p>
 * For plain (non-function) option values, use {@link FullCalendar.Option} with
 * {@link FullCalendar#setOption(FullCalendar.Option, Object)} instead.
 * <p>
 * Scheduler-specific callback options are defined in {@code SchedulerCallbackOption}
 * in the addon-scheduler module.
 */
public enum CallbackOption implements ClientSideValue {

    // ---- Render hooks: Entry ----
    /**
     * Add CSS classes to entry wrapper elements. Called when an entry is rendered.
     * Arguments: {@code {event, el, view}}. Returns: string array of CSS class names.
     * {@code info.event} has {@code getCustomProperty()} method available.
     */
    ENTRY_CLASS_NAMES("eventClassNames"),

    /**
     * Customize the inner content of entry elements. Called when an entry is rendered.
     * Arguments: {@code {event, timeText, isStart, isEnd, isMirror, isPast, isFuture, isToday, el, view}}.
     * Returns: content object or HTML string. {@code info.event} has {@code getCustomProperty()} available.
     */
    ENTRY_CONTENT("eventContent"),

    /**
     * Called after an entry element is added to the DOM. Use for post-render setup (e.g., popovers).
     * Arguments: {@code {event, el, view}}. {@code info.event} has {@code getCustomProperty()} available.
     */
    ENTRY_DID_MOUNT("eventDidMount"),

    /**
     * Called before an entry element is removed from the DOM. Use for cleanup.
     * Arguments: {@code {event, el, view}}. {@code info.event} has {@code getCustomProperty()} available.
     */
    ENTRY_WILL_UNMOUNT("eventWillUnmount"),

    // ---- Render hooks: Day Cell ----
    /**
     * Add CSS classes to day cell {@code <td>} elements. Called when a day cell is rendered.
     * Arguments: {@code {date, dayNumberText, isToday, isPast, isFuture, isOther, view}}.
     * Returns: string array of CSS class names.
     */
    DAY_CELL_CLASS_NAMES("dayCellClassNames"),

    /**
     * Customize the content inside day cells. Called when a day cell is rendered.
     * Arguments: {@code {date, dayNumberText, isToday, isPast, isFuture, isOther, view}}.
     * Returns: content object or HTML string.
     */
    DAY_CELL_CONTENT("dayCellContent"),

    /**
     * Called after a day cell element is added to the DOM.
     * Arguments: {@code {date, dayNumberText, isToday, isPast, isFuture, isOther, view, el}}.
     */
    DAY_CELL_DID_MOUNT("dayCellDidMount"),

    /**
     * Called before a day cell element is removed from the DOM.
     * Arguments: {@code {date, dayNumberText, isToday, isPast, isFuture, isOther, view, el}}.
     */
    DAY_CELL_WILL_UNMOUNT("dayCellWillUnmount"),

    // ---- Render hooks: Day Header ----
    /**
     * Add CSS classes to day header {@code <th>} elements. Called when a day header is rendered.
     * Arguments: {@code {date, text, isToday, isPast, isFuture, view}}.
     * Returns: string array of CSS class names.
     */
    DAY_HEADER_CLASS_NAMES("dayHeaderClassNames"),

    /**
     * Customize the content inside day header cells. Called when a day header is rendered.
     * Arguments: {@code {date, text, isToday, isPast, isFuture, view}}.
     * Returns: content object or HTML string.
     */
    DAY_HEADER_CONTENT("dayHeaderContent"),

    /**
     * Called after a day header element is added to the DOM.
     * Arguments: {@code {date, text, isToday, isPast, isFuture, view, el}}.
     */
    DAY_HEADER_DID_MOUNT("dayHeaderDidMount"),

    /**
     * Called before a day header element is removed from the DOM.
     * Arguments: {@code {date, text, isToday, isPast, isFuture, view, el}}.
     */
    DAY_HEADER_WILL_UNMOUNT("dayHeaderWillUnmount"),

    // ---- Render hooks: Slot Label ----
    /**
     * Add CSS classes to time slot label cells. Called when a slot label is rendered.
     * Arguments: {@code {date, text, view}}.
     * Returns: string array of CSS class names.
     */
    SLOT_LABEL_CLASS_NAMES("slotLabelClassNames"),

    /**
     * Customize the content inside time slot label cells.
     * Arguments: {@code {date, text, view}}.
     * Returns: content object or HTML string.
     */
    SLOT_LABEL_CONTENT("slotLabelContent"),

    /**
     * Called after a slot label element is added to the DOM.
     * Arguments: {@code {date, text, view, el}}.
     */
    SLOT_LABEL_DID_MOUNT("slotLabelDidMount"),

    /**
     * Called before a slot label element is removed from the DOM.
     * Arguments: {@code {date, text, view, el}}.
     */
    SLOT_LABEL_WILL_UNMOUNT("slotLabelWillUnmount"),

    // ---- Render hooks: Slot Lane ----
    /**
     * Add CSS classes to time slot lane cells (columns in timegrid view).
     * Arguments: {@code {date, time, view}}. ({@code time} is duration from start of day).
     * Returns: string array of CSS class names.
     */
    SLOT_LANE_CLASS_NAMES("slotLaneClassNames"),

    /**
     * Customize the content inside time slot lane cells.
     * Arguments: {@code {date, time, view}}.
     * Returns: content object or HTML string.
     */
    SLOT_LANE_CONTENT("slotLaneContent"),

    /**
     * Called after a slot lane element is added to the DOM.
     * Arguments: {@code {date, time, view, el}}.
     */
    SLOT_LANE_DID_MOUNT("slotLaneDidMount"),

    /**
     * Called before a slot lane element is removed from the DOM.
     * Arguments: {@code {date, time, view, el}}.
     */
    SLOT_LANE_WILL_UNMOUNT("slotLaneWillUnmount"),

    // ---- Render hooks: View ----
    /**
     * Add CSS classes to the view root element.
     * Arguments: {@code {view}}.
     * Returns: string array of CSS class names.
     */
    VIEW_CLASS_NAMES("viewClassNames"),

    /**
     * Called after the view root element is added to the DOM.
     * Arguments: {@code {view, el}}.
     */
    VIEW_DID_MOUNT("viewDidMount"),

    /**
     * Called before the view root element is removed from the DOM.
     * Arguments: {@code {view, el}}.
     */
    VIEW_WILL_UNMOUNT("viewWillUnmount"),

    // ---- Render hooks: Now Indicator ----
    /**
     * Add CSS classes to now indicator elements.
     * Arguments: {@code {date, isAxis, view}}. ({@code isAxis} is true for time label).
     * Returns: string array of CSS class names.
     */
    NOW_INDICATOR_CLASS_NAMES("nowIndicatorClassNames"),

    /**
     * Customize the content inside now indicator elements.
     * Arguments: {@code {date, isAxis, view}}.
     * Returns: content object or HTML string.
     */
    NOW_INDICATOR_CONTENT("nowIndicatorContent"),

    /**
     * Called after a now indicator element is added to the DOM.
     * Arguments: {@code {date, isAxis, view, el}}.
     */
    NOW_INDICATOR_DID_MOUNT("nowIndicatorDidMount"),

    /**
     * Called before a now indicator element is removed from the DOM.
     * Arguments: {@code {date, isAxis, view, el}}.
     */
    NOW_INDICATOR_WILL_UNMOUNT("nowIndicatorWillUnmount"),

    // ---- Render hooks: Week Number ----
    /**
     * Add CSS classes to week number cells.
     * Arguments: {@code {date, num, text, view}}.
     * Returns: string array of CSS class names.
     */
    WEEK_NUMBER_CLASS_NAMES("weekNumberClassNames"),

    /**
     * Customize the content inside week number cells.
     * Arguments: {@code {date, num, text, view}}.
     * Returns: content object or HTML string.
     */
    WEEK_NUMBER_CONTENT("weekNumberContent"),

    /**
     * Called after a week number element is added to the DOM.
     * Arguments: {@code {date, num, text, view, el}}.
     */
    WEEK_NUMBER_DID_MOUNT("weekNumberDidMount"),

    /**
     * Called before a week number element is removed from the DOM.
     * Arguments: {@code {date, num, text, view, el}}.
     */
    WEEK_NUMBER_WILL_UNMOUNT("weekNumberWillUnmount"),

    // ---- Render hooks: More Link ----
    /**
     * Add CSS classes to the "+N more" link element in month view.
     * Arguments: {@code {num, text, shortText, view}}.
     * Returns: string array of CSS class names.
     */
    MORE_LINK_CLASS_NAMES("moreLinkClassNames"),

    /**
     * Customize the content of the "+N more" link.
     * Arguments: {@code {num, text, shortText, view}}.
     * Returns: content object or HTML string.
     */
    MORE_LINK_CONTENT("moreLinkContent"),

    /**
     * Called after a more link element is added to the DOM.
     * Arguments: {@code {num, text, shortText, view, el}}.
     */
    MORE_LINK_DID_MOUNT("moreLinkDidMount"),

    /**
     * Called before a more link element is removed from the DOM.
     * Arguments: {@code {num, text, shortText, view, el}}.
     */
    MORE_LINK_WILL_UNMOUNT("moreLinkWillUnmount"),

    // ---- Render hooks: No Entries ----
    /**
     * Add CSS classes to the "No events" message in list view.
     * Arguments: {@code {view}}.
     * Returns: string array of CSS class names.
     */
    NO_ENTRIES_CLASS_NAMES("noEventsClassNames"),

    /**
     * Customize the "No events" message in list view.
     * Arguments: {@code {view}}.
     * Returns: content object or HTML string.
     */
    NO_ENTRIES_CONTENT("noEventsContent"),

    /**
     * Called after a no-entries element is added to the DOM.
     * Arguments: {@code {view, el}}.
     */
    NO_ENTRIES_DID_MOUNT("noEventsDidMount"),

    /**
     * Called before a no-entries element is removed from the DOM.
     * Arguments: {@code {view, el}}.
     */
    NO_ENTRIES_WILL_UNMOUNT("noEventsWillUnmount"),

    // ---- Render hooks: All Day ----
    /**
     * Add CSS classes to the all-day section header cell in timegrid views.
     * Arguments: {@code {text, view}}.
     * Returns: string array of CSS class names.
     */
    ALL_DAY_CLASS_NAMES("allDayClassNames"),

    /**
     * Customize the content inside the all-day header cell.
     * Arguments: {@code {text, view}}.
     * Returns: content object or HTML string.
     */
    ALL_DAY_CONTENT("allDayContent"),

    /**
     * Called after the all-day header element is added to the DOM.
     * Arguments: {@code {text, view, el}}.
     */
    ALL_DAY_DID_MOUNT("allDayDidMount"),

    /**
     * Called before the all-day header element is removed from the DOM.
     * Arguments: {@code {text, view, el}}.
     */
    ALL_DAY_WILL_UNMOUNT("allDayWillUnmount"),

    // ---- Interaction callbacks ----
    /**
     * Control whether a time range selection is allowed. Called on every mouse move during selection drag.
     * Must be synchronous (no server round-trip possible).
     * Arguments: {@code (selectInfo)}. Returns: boolean.
     */
    SELECT_ALLOW("selectAllow"),

    /**
     * Control whether an entry drop is allowed. Called during entry drag to allow/deny a drop.
     * Arguments: {@code (dropInfo, draggedEvent)}. Returns: boolean.
     * {@code draggedEvent} has {@code getCustomProperty()} available.
     */
    ENTRY_ALLOW("eventAllow"),

    /**
     * Per-combination entry overlap control. Called for each pair of overlapping entries during drag.
     * Arguments: {@code (stillEvent, movingEvent)}. Returns: boolean.
     * Both events have {@code getCustomProperty()} available.
     */
    ENTRY_OVERLAP("eventOverlap"),

    /**
     * Control whether a time selection can overlap an existing entry. Called when selection overlaps an entry.
     * Arguments: {@code (event)}. Returns: boolean.
     * Also paired with {@link FullCalendar.Option#SELECT_OVERLAP}} for non-function variant.
     */
    SELECT_OVERLAP("selectOverlap"),

    /**
     * Filter which external DOM elements can be dropped onto the calendar.
     * Arguments: {@code (draggable)}. Returns: boolean.
     * Also paired with {@link FullCalendar.Option#DROP_ACCEPT}} for CSS selector variant.
     */
    DROP_ACCEPT("dropAccept"),

    /**
     * Dynamic valid date range. Returns the allowed date range based on the current time.
     * Arguments: {@code (nowDate)}. Returns: {@code {start, end}}} with date strings.
     * Also paired with {@link FullCalendar.Option#VALID_RANGE}} for static-range variant.
     */
    VALID_RANGE("validRange"),

    /**
     * Custom sort order for entries. Compare function for sorting entries within a slot.
     * Arguments: {@code (a, b)}}. Returns: number (-1, 0, or 1).
     * Also paired with {@link FullCalendar.Option#ENTRY_ORDER}} for string/array variant.
     */
    ENTRY_ORDER("eventOrder"),

    // ---- Data transform / loading callbacks ----
    /**
     * Called when async entry fetching starts or stops. No return value.
     * Arguments: {@code (isLoading)}} (boolean).
     */
    LOADING("loading"),

    /**
     * Transform raw entry data before FullCalendar parses it.
     * Arguments: {@code (eventData)}}. Returns: transformed event data object.
     */
    ENTRY_DATA_TRANSFORM("eventDataTransform"),

    /**
     * Called after an entry source fetches successfully.
     * Arguments: {@code (rawEvents, response)}}. Returns: array of event objects (or undefined to keep original).
     */
    ENTRY_SOURCE_SUCCESS("eventSourceSuccess"),

    // ---- Navigation callbacks ----
    /**
     * Custom handler for clickable day nav links (dates in month/week view).
     * Overrides default navigation behavior if defined.
     * Arguments: {@code (date, jsEvent)}}.
     */
    NAV_LINK_DAY_CLICK("navLinkDayClick"),

    /**
     * Custom handler for clickable week nav links (week numbers).
     * Overrides default navigation behavior if defined.
     * Arguments: {@code (weekStart, jsEvent)}}. {@code weekStart} is the Monday of that week.
     */
    NAV_LINK_WEEK_CLICK("navLinkWeekClick");

    private final String clientSideValue;

    CallbackOption(String clientSideValue) {
        this.clientSideValue = clientSideValue;
    }

    @Override
    public String getClientSideValue() {
        return clientSideValue;
    }
}
