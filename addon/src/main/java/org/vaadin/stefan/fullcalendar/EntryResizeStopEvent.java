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
import lombok.ToString;
import tools.jackson.databind.node.ObjectNode;

/**
 * Fires when the user stops resizing an entry, regardless of whether the duration changed.
 * Complements {@link EntryResizedEvent}, which only fires when the resize actually changed the entry's duration.
 * <br><br>
 * Use this event to clean up UI feedback that was shown in response to {@link EntryResizeStartEvent}
 * (e.g. re-enabling controls after any resize, regardless of outcome).
 * <br><br>
 * Note: {@link #getEntry()} returns the server-side entry at its <em>original</em> duration.
 * Do not call {@link #applyChangesOnEntry()} on this event — resize stop does not represent
 * a committed duration change. Use it for UI feedback only.
 * <br><br>
 * Client side name: eventResizeStop
 */
@DomEvent("eventResizeStop")
@ToString(callSuper = true)
public class EntryResizeStopEvent extends EntryDataEvent {

    /**
     * New instance.
     *
     * @param source     source component
     * @param fromClient is from client
     * @param entryData  entry data
     */
    public EntryResizeStopEvent(FullCalendar source, boolean fromClient, @EventData("event.detail.data") ObjectNode entryData) {
        super(source, fromClient, entryData);
    }
}
