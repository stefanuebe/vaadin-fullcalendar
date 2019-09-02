/*
 * Copyright 2018, Stefan Uebe
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
 * Basic enumeration of possible calendar views.
 */
public enum CalendarViewImpl implements CalendarView {
    DAY_GRID_MONTH("dayGridMonth"), // was MONTH
    TIME_GRID_DAY("timeGridDay"), // was AGENDA_DAY
    TIME_GRID_WEEK("timeGridWeek"), // was AGENDA_WEEK
    DAY_GRID_DAY("dayGridDay"), // was BASIC_DAY
    DAY_GRID_WEEK("dayGridWeek"), // was BASIC_WEEK
    LIST_WEEK("listWeek"),
    LIST_DAY("listDay"),
    LIST_MONTH("listMonth"),
    LIST_YEAR("listYear"),
    ;

    private final String clientSideName;

    CalendarViewImpl(String clientSideName) {
        this.clientSideName = clientSideName;
    }

    @Override
    public String getClientSideValue() {
        return clientSideName;
    }

    @Override
    public String getName() {
        return name();
    }
}
