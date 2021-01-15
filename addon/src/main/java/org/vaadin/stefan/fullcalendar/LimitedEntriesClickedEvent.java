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

import java.time.LocalDate;
import java.util.List;

/**
 * Client side event: moreLinkClick.
 */
@DomEvent("moreLinkClick")
@Getter
@ToString
public class LimitedEntriesClickedEvent extends ComponentEvent<FullCalendar> {

    /**
     * The clicked date.
     */
    private final LocalDate clickedDate;
    
    /**
     * A List of all event objects for the clicked date. (This is not the Event Segment Object, but the Event Object)
     */
    private final List<Entry> events;

    /**
     * New instance. Awaits the clicked date as iso string (e.g. "2018-10-23").
     *
     * @param source     the source component
     * @param fromClient <code>true</code> if the event originated from the client
     * @param date       clicked time slot as iso string
     */
    public LimitedEntriesClickedEvent(FullCalendar source, boolean fromClient, @EventData("event.detail.date") String date, @EventData("event.detail.allSegs") List<Entry> allSegs) {
        super(source, fromClient);

        clickedDate = source.getTimezone().convertToLocalDate(JsonUtils.parseDateTimeString(date, source.getTimezone()));
        events = allSegs;
    }

}
