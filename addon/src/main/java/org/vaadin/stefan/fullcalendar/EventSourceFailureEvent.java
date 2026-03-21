/*
 * Copyright 2026, Stefan Uebe
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

/**
 * Fires when a client-managed event source (JSON feed, Google Calendar, iCal) fails to load.
 * <br><br>
 * Use this to react on the server side (e.g. show an error {@code Notification}, log the failure, etc.).
 * The {@link #getSourceId()} identifies which configured source failed — this is the id set on the
 * {@link ClientSideEventSource} when it was added.
 * <br><br>
 * Client side name: eventSourceFailure
 */
@DomEvent("eventSourceFailure")
@Getter
@ToString
public class EventSourceFailureEvent extends ComponentEvent<FullCalendar> {

    /**
     * The id of the {@link ClientSideEventSource} that failed, as set when calling
     * {@link FullCalendar#addEventSource(ClientSideEventSource)}.
     */
    private final String sourceId;

    /**
     * The error message reported by FullCalendar.
     */
    private final String message;

    /**
     * New instance.
     *
     * @param source     the source component
     * @param fromClient {@code true} if the event originated from the client
     * @param sourceId   id of the event source that failed
     * @param message    error message
     */
    public EventSourceFailureEvent(FullCalendar source, boolean fromClient,
                                   @EventData("event.detail.sourceId") String sourceId,
                                   @EventData("event.detail.message") String message) {
        super(source, fromClient);
        this.sourceId = sourceId;
        this.message = message;
    }
}
