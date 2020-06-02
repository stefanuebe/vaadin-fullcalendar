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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Basic enumeration of possible calendar views.
 */
@Getter
@RequiredArgsConstructor
public enum CalendarViewImpl implements CalendarView {

    /** Day based grid view of a month */
    DAY_GRID_MONTH("dayGridMonth"), // was MONTH

    /** Timegrid day view */
    TIME_GRID_DAY("timeGridDay"), // was AGENDA_DAY
    /** Timegrid week view */
    TIME_GRID_WEEK("timeGridWeek"), // was AGENDA_WEEK

    /** Day based grid view of a day (simply a single rectangle showing the day) */
    DAY_GRID_DAY("dayGridDay"), // was BASIC_DAY
    /** Day based grid view of a week */
    DAY_GRID_WEEK("dayGridWeek"), // was BASIC_WEEK

    /** Entries list for a week */
    LIST_WEEK("listWeek"),
    /** Entries list for a day */
    LIST_DAY("listDay"),
    /** Entries list for a month */
    LIST_MONTH("listMonth"),
    /** Entries list for a year */
    LIST_YEAR("listYear"),
    ;

    private final String clientSideValue;

    @Override
    public String getName() {
        return name();
    }
}
