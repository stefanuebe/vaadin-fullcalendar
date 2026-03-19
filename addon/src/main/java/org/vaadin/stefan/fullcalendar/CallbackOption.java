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
    ENTRY_CLASS_NAMES("eventClassNames"),
    /**
     * The eventContent hook. Note: this hook receives custom API injection automatically
     * (info.event has a {@code getCustomProperty} method available in the callback).
     */
    ENTRY_CONTENT("eventContent"),
    ENTRY_DID_MOUNT("eventDidMount"),
    ENTRY_WILL_UNMOUNT("eventWillUnmount"),

    // ---- Render hooks: Day Cell ----
    DAY_CELL_CLASS_NAMES("dayCellClassNames"),
    DAY_CELL_CONTENT("dayCellContent"),
    DAY_CELL_DID_MOUNT("dayCellDidMount"),
    DAY_CELL_WILL_UNMOUNT("dayCellWillUnmount"),

    // ---- Render hooks: Day Header ----
    DAY_HEADER_CLASS_NAMES("dayHeaderClassNames"),
    DAY_HEADER_CONTENT("dayHeaderContent"),
    DAY_HEADER_DID_MOUNT("dayHeaderDidMount"),
    DAY_HEADER_WILL_UNMOUNT("dayHeaderWillUnmount"),

    // ---- Render hooks: Slot Label ----
    SLOT_LABEL_CLASS_NAMES("slotLabelClassNames"),
    SLOT_LABEL_CONTENT("slotLabelContent"),
    SLOT_LABEL_DID_MOUNT("slotLabelDidMount"),
    SLOT_LABEL_WILL_UNMOUNT("slotLabelWillUnmount"),

    // ---- Render hooks: Slot Lane ----
    SLOT_LANE_CLASS_NAMES("slotLaneClassNames"),
    SLOT_LANE_CONTENT("slotLaneContent"),
    SLOT_LANE_DID_MOUNT("slotLaneDidMount"),
    SLOT_LANE_WILL_UNMOUNT("slotLaneWillUnmount"),

    // ---- Render hooks: View ----
    VIEW_CLASS_NAMES("viewClassNames"),
    VIEW_DID_MOUNT("viewDidMount"),
    VIEW_WILL_UNMOUNT("viewWillUnmount"),

    // ---- Render hooks: Now Indicator ----
    NOW_INDICATOR_CLASS_NAMES("nowIndicatorClassNames"),
    NOW_INDICATOR_CONTENT("nowIndicatorContent"),
    NOW_INDICATOR_DID_MOUNT("nowIndicatorDidMount"),
    NOW_INDICATOR_WILL_UNMOUNT("nowIndicatorWillUnmount"),

    // ---- Render hooks: Week Number ----
    WEEK_NUMBER_CLASS_NAMES("weekNumberClassNames"),
    WEEK_NUMBER_CONTENT("weekNumberContent"),
    WEEK_NUMBER_DID_MOUNT("weekNumberDidMount"),
    WEEK_NUMBER_WILL_UNMOUNT("weekNumberWillUnmount"),

    // ---- Render hooks: More Link ----
    MORE_LINK_CLASS_NAMES("moreLinkClassNames"),
    MORE_LINK_CONTENT("moreLinkContent"),
    MORE_LINK_DID_MOUNT("moreLinkDidMount"),
    MORE_LINK_WILL_UNMOUNT("moreLinkWillUnmount"),

    // ---- Render hooks: No Entries ----
    NO_ENTRIES_CLASS_NAMES("noEventsClassNames"),
    NO_ENTRIES_CONTENT("noEventsContent"),
    NO_ENTRIES_DID_MOUNT("noEventsDidMount"),
    NO_ENTRIES_WILL_UNMOUNT("noEventsWillUnmount"),

    // ---- Render hooks: All Day ----
    ALL_DAY_CLASS_NAMES("allDayClassNames"),
    ALL_DAY_CONTENT("allDayContent"),
    ALL_DAY_DID_MOUNT("allDayDidMount"),
    ALL_DAY_WILL_UNMOUNT("allDayWillUnmount"),

    // ---- Interaction callbacks ----
    SELECT_ALLOW("selectAllow"),
    ENTRY_ALLOW("eventAllow"),
    ENTRY_OVERLAP("eventOverlap"),
    SELECT_OVERLAP("selectOverlap"),
    DROP_ACCEPT("dropAccept"),
    VALID_RANGE("validRange"),
    ENTRY_ORDER("eventOrder"),

    // ---- Data transform / loading callbacks ----
    LOADING("loading"),
    ENTRY_DATA_TRANSFORM("eventDataTransform"),
    ENTRY_SOURCE_SUCCESS("eventSourceSuccess"),

    // ---- Navigation callbacks ----
    NAV_LINK_DAY_CLICK("navLinkDayClick"),
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
