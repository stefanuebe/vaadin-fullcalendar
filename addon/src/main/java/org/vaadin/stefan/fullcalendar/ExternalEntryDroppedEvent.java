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

import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import tools.jackson.databind.node.ObjectNode;

/**
 * Former name of {@link EntrySourceEntryDroppedEvent}. Listeners registered for this class keep receiving the event.
 *
 * @deprecated use {@link EntrySourceEntryDroppedEvent} and
 * {@link FullCalendar#addEntrySourceEntryDroppedListener(com.vaadin.flow.component.ComponentEventListener)}. The entry
 * comes from an entry source, not from outside the calendar, which the former name suggested.
 */
@Deprecated(since = "8.0.0", forRemoval = true)
@DomEvent("externalEntryDrop")
public class ExternalEntryDroppedEvent extends EntrySourceEntryDroppedEvent {

    /**
     * New instance.
     *
     * @param source      source component
     * @param fromClient  true if from client
     * @param entryData   JSON data of the dropped entry (new position)
     * @param jsonDelta   delta JSON object
     * @param sourceId    id of the ClientSideEventSource
     */
    public ExternalEntryDroppedEvent(FullCalendar source, boolean fromClient,
                                     @EventData("event.detail.data") ObjectNode entryData,
                                     @EventData("event.detail.delta") ObjectNode jsonDelta,
                                     @EventData("event.detail.sourceId") String sourceId) {
        super(source, fromClient, entryData, jsonDelta, sourceId);
    }
}
