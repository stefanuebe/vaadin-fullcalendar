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
import lombok.Getter;
import elemental.json.JsonObject;

/**
 * Base class for events fired when an entry from a <em>client-managed</em> event source
 * (JSON feed, Google Calendar, iCal) is interacted with (dropped or resized).
 * <br><br>
 * The {@link #getEntry()} method returns a <em>transient</em> data carrier constructed from the entry's
 * JS data. This entry is NOT part of any {@link org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider}.
 * Use {@link Entry#getId()} to locate and update the corresponding record in the external system.
 */
@Getter
public abstract class ExternalEntryEvent extends ComponentEvent<FullCalendar> {

    /**
     * Transient entry data carrier. NOT owned by any EntryProvider.
     */
    private final Entry entry;

    /**
     * The id of the {@link ClientSideEventSource} the entry came from.
     */
    private final String sourceId;

    /**
     * Parses the entry data from JSON and stores the source id.
     *
     * @param source     source component
     * @param fromClient true if from client
     * @param entryData  JSON data of the entry (new position / state)
     * @param sourceId   id of the ClientSideEventSource
     */
    protected ExternalEntryEvent(FullCalendar source, boolean fromClient,
                                 JsonObject entryData, String sourceId) {
        super(source, fromClient);
        Entry e = new Entry();
        e.updateFromJson(entryData, false);
        this.entry = e;
        this.sourceId = sourceId;
    }
}
