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
 * Fires when the user stops dragging an entry, whether or not it was dropped somewhere else of a {@link
 * RemoteEntrySource}, such as a JSON feed, Google Calendar or iCal. It takes the place of {@link EntryDragStopEvent},
 * which fires for the entries of the entry provider only.
 * <br><br>
 * {@link #getEntry()} returns a <em>transient</em> data carrier built from the data of the dragged entry. It is not
 * part of any {@link org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider}. Its {@link Entry#getId()} is the id
 * the entry has in the data of its source. {@link #getSourceId()} returns the id of the source.
 * <br><br>
 * Client side name: externalEntryDragStop
 */
@DomEvent("externalEntryDragStop")
public class RemoteEntryDragStopEvent extends RemoteEntryEvent {

    /**
     * New instance.
     *
     * @param source     source component
     * @param fromClient true if from client
     * @param entryData  JSON data of the entry
     * @param sourceId   id of the {@link RemoteEntrySource}
     */
    public RemoteEntryDragStopEvent(FullCalendar source, boolean fromClient,
            @EventData("event.detail.data") ObjectNode entryData,
            @EventData("event.detail.sourceId") String sourceId) {
        super(source, fromClient, entryData, sourceId);
    }
}
