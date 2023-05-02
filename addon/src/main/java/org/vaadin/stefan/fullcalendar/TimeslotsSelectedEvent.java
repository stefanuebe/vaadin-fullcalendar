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

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import lombok.Getter;
import lombok.ToString;

import java.time.*;

/**
 * Occurs when the user selects one or multiple timeslots on the calendar. The selected timeslots may contain
 * entries.
 * <br><br>
 * Client side event: select
 *
 */
@DomEvent("select")
@Getter
@ToString
public class TimeslotsSelectedEvent extends ComponentEvent<FullCalendar> {

    /**
     * If the selection has been for day slots. False means, it has been a selection on time slots inside a day.
     */
    private final boolean allDay;

    /**
     * Returns the start of the event as local date time. Represents the UTC date time this event starts, which
     * means the time is the same as when calling {@link #getStartAsInstant()}.
     */
    private final LocalDateTime start;

    /**
     * Returns the end of the event as local date time. Represents the UTC date time this event ends, which
     * means the time is the same as when calling {@link #getEndAsInstant()}.
     */
    private final LocalDateTime end;

    /**
     * New instance. Awaits the selected dates (time) as iso string (e.g. "2018-10-23" or "2018-10-23T13:30").
     *
     * @param source     the source component
     * @param fromClient <code>true</code> if the event originated from the client
     * @param start start time slot as iso string
     * @param end end time slot as iso string
     * @param allDay all day event
     */
    public TimeslotsSelectedEvent(FullCalendar source, boolean fromClient, @EventData("event.detail.start") String start, @EventData("event.detail.end") String end, @EventData("event.detail.allDay") boolean allDay) {
        super(source, fromClient);

        Timezone timezone = source.getTimezone();
        this.allDay = allDay;
        this.start = JsonUtils.parseClientSideDateTime(start);
        this.end = JsonUtils.parseClientSideDateTime(end);
    }

    /**
     * Returns the entry's start as an {@link Instant}. The contained time is the same as when calling
     * {@link #getStart()}.
     *
     * @return start as Instant
     */
    public Instant getStartAsInstant() {
        return Timezone.UTC.convertToInstant(start);
    }

    /**
     * Returns the entry's end as an {@link Instant}. The contained time is the same as when calling
     * {@link #getEnd()}.
     *
     * @return end as Instant
     */
    public Instant getEndAsInstant() {
        return Timezone.UTC.convertToInstant(end);
    }

    /**
     * Returns the start time as a local date time after applying the timezone's offset to
     * the utc based start date ({@link #getStart()}). By default the timezone is
     * the calendar's timezone or, if no calendar is set yet, UTC.
     * <p></p>
     * @return start with offset
     */
    public LocalDateTime getStartWithOffset() {
        return getSource().getTimezone().applyTimezoneOffset(start);
    }

    /**
     * Returns the end time as a local date time after applying the timezone's offset to
     * the utc based end date ({@link #getEnd()}). By default the timezone is
     * the calendar's timezone or, if no calendar is set yet, UTC.
     * @return end with offset
     */
    public LocalDateTime getEndWithOffset() {
        return getSource().getTimezone().applyTimezoneOffset(end);
    }

    /**
     * Returns the start of the event as local date.
     * @return start as local date
     */
    public LocalDate getStartDate() {
        return start.toLocalDate();
    }

    /**
     * Returns the end of the event as local date.
     * @return end as local date
     */
    public LocalDate getEndDate() {
        return end.toLocalDate();
    }

}
