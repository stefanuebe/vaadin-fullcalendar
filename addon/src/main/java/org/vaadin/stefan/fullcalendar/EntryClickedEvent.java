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

import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import lombok.ToString;
import tools.jackson.databind.node.ObjectNode;

/**
 * Occurs when an entry has been clicked on the client side.
 * <br><br>
 * Client side event: eventClick
 *
 * @deprecated Use {@link CalendarItemClickedEvent} with {@link FullCalendar#addCalendarItemClickedListener} instead.
 */
@DomEvent("eventClick")
@ToString(callSuper = true)
@Deprecated
public class EntryClickedEvent extends CalendarItemClickedEvent<Entry> {

    /**
     * New instance. Awaits the id of the clicked entry.
     *
     * @param source     source component
     * @param fromClient from client
     * @param entryData  entry data
     */
    public EntryClickedEvent(FullCalendar<Entry> source, boolean fromClient, @EventData("event.detail.data") ObjectNode entryData) {
        super(source, fromClient, entryData);
    }

    /**
     * Returns the entry for which the event occurred.
     *
     * @return entry
     * @deprecated Use {@link #getItem()} instead.
     */
    @Deprecated
    public Entry getEntry() {
        return getItem();
    }

    /**
     * Creates a copy based on the referenced entry and the received data.
     *
     * @param <R> return type
     * @return copy
     * @deprecated Use the CIP event hierarchy with {@link CalendarItemClickedEvent} instead.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public <R extends Entry> R createCopyBasedOnChanges() {
        try {
            Entry copy = getEntry().copy();
            copy.updateFromJson(getJsonObject());
            return (R) copy;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
