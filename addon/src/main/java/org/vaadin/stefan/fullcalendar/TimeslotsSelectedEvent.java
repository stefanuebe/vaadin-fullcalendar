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

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Occurs when the user selects one or multiple timeslots on the calendar. The selected timeslots may contain
 * entries.
 * <br><br>
 * Client side event: select
 *
 */
@DomEvent("select")
public class TimeslotsSelectedEvent extends ComponentEvent<FullCalendar> {

    private final boolean allDay;
    private final Instant startDateTime;
    private final Instant endDateTime;

    /**
     * New instance. Awaits the selected dates (time) as iso string (e.g. "2018-10-23" or "2018-10-23T13:30").
     *
     * @param source     the source component
     * @param fromClient <code>true</code> if the event originated from the client
     * @param start start time slot as iso string
     * @param end end time slot as iso string
     */
    public TimeslotsSelectedEvent(FullCalendar source, boolean fromClient, @EventData("event.detail.start") String start, @EventData("event.detail.end") String end, @EventData("event.detail.allDay") boolean allDay) {
        super(source, fromClient);

        Timezone timezone = source.getTimezone();

        this.allDay = allDay;
        this.startDateTime = JsonUtils.parseDateTimeString(start, timezone);
        this.endDateTime = JsonUtils.parseDateTimeString(end, timezone);
    }

    /**
     * Returns, if the selection has been for day slots. False means, it has been a selection on time slots inside a day.
     * @return all day click
     */
    public boolean isAllDay() {
        return allDay;
    }

    /**
     * Returns the selected start date time. For day slots the time will be at start of the day.
     * @return date time
     */
    public LocalDateTime getStartDateTime() {
        return getSource().getTimezone().convertToLocalDateTime(startDateTime);
    }

    /**
     * Returns the selected end date time. For day slots the time will be at start of the day.
     * @return date time
     */
    public LocalDateTime getEndDateTime() {
        return getSource().getTimezone().convertToLocalDateTime(endDateTime);
    }

    /**
     * Returns the selected start date time as UTC. For day slots the time will be at start of the day.
     * @return date time
     */
    public Instant getStartDateTimeUTC() {
        return startDateTime;
    }

    /**
     * Returns the selected end date time as UTC. For day slots the time will be at start of the day.
     * @return date time
     */
    public Instant getEndDateTimeUTC() {
        return endDateTime;
    }
}
