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
import elemental.json.JsonObject;
import lombok.ToString;

/**
 * Occurs when an entry time has changed by resizing.
 * <br><br>
 * You can apply the changes to the referred entry by calling the method {@link #applyChangesOnEntry()}.
 * <br><br>
 * Client side name: eventResize
 *
 */
@DomEvent("eventResize")
@ToString(callSuper = true)
public class EntryResizedEvent extends EntryTimeChangedEvent {

    /**
     * New instance. Awaits the changed data object for the entry plus the json object for the delta information.
     * @param source source component
     * @param fromClient is from client
     * @param jsonEntry json object with changed data
     * @param jsonDelta json object with delta information
     */
    public EntryResizedEvent(FullCalendar source, boolean fromClient, @EventData("event.detail.data") JsonObject jsonEntry, @EventData("event.detail.delta") JsonObject jsonDelta) {
        super(source, fromClient, jsonEntry, jsonDelta);
    }
}
