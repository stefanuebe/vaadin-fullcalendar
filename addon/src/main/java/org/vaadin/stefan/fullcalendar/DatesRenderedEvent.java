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
import elemental.json.JsonObject;

import java.time.Instant;
import java.time.LocalDate;

import static org.vaadin.stefan.fullcalendar.JsonUtils.parseDateTimeString;

/**
 * Occurs when the calendar view has been rendered. Provides information about the shown timespan.
 * <br><br>
 * The values are always daybased, regardless of the current view.
 */
@DomEvent("datesRender")
public class DatesRenderedEvent extends ComponentEvent<FullCalendar> {

    private final LocalDate intervalStart;
    private final LocalDate intervalEnd;
    private final LocalDate start;
    private final LocalDate end;

    /**
     * Creates a new event using the given source and indicator whether the
     * event originated from the client side or the server side.
     *
     * @param source     the source component
     * @param fromClient <code>true</code> if the event originated from the client
     */
    public DatesRenderedEvent(FullCalendar source, boolean fromClient, @EventData("event.detail") JsonObject eventData) {
        super(source, fromClient);

        this.intervalStart = getLocalDate(eventData, "intervalStart", source);
        this.intervalEnd = getLocalDate(eventData, "intervalEnd", source);
        this.start = getLocalDate(eventData, "start", source);
        this.end = getLocalDate(eventData, "end", source);
    }

    /**
     * Returns the current shown interval's start date.
     *
     * @return interval start
     */
    public LocalDate getIntervalStart() {
        return intervalStart;
    }

    /**
     * Returns the current shown interval's exclusive end date. This means, this date is not part of the interval.
     *
     * @return interval end (exclusive)
     */
    public LocalDate getIntervalEnd() {
        return intervalEnd;
    }

    /**
     * Returns the first visible date. In month-view, this value is often before
     * the 1st day of the month, because most months do not begin on the first
     * day-of-week.
     *
     * @return first visible date
     */
    public LocalDate getStart() {
        return start;
    }

    /**
     * Returns the last visible date. In month-view, this value is often after
     * the last day of the month, because most months do not end on the last day
     * of the week.
     *
     * @return last visible date
     */
    public LocalDate getEnd() {
        return end;
    }

    private LocalDate getLocalDate(final JsonObject eventData, final String key, final FullCalendar source) {
        final Instant instant = parseDateTimeString(eventData.getString(key), source.getTimezone());
        return source.getTimezone().convertToLocalDate(instant);
    }

}
