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
 * Fires when the user begins dragging an entry. Complements {@link EntryDroppedEvent}, which only fires when
 * the drag actually changed the entry's position. This event fires regardless of whether the drag results
 * in a position change or not.
 * <br><br>
 * Use this event to show UI feedback while a drag is in progress (e.g. disabling a "Delete" button).
 * Use {@link EntryDragStopEvent} to re-enable such feedback when dragging ends.
 * <br><br>
 * Note: {@link #getEntry()} returns the server-side entry at its <em>original</em> position.
 * Do not call {@link #applyChangesOnEntry()} on this event — drag start does not represent
 * a committed position change. Use it for UI feedback only.
 * <br><br>
 * Client side name: eventDragStart
 */
@DomEvent("eventDragStart")
@ToString(callSuper = true)
public class EntryDragStartEvent extends EntryDataEvent {

    /**
     * New instance.
     *
     * @param source     source component
     * @param fromClient is from client
     * @param entryData  entry data
     */
    public EntryDragStartEvent(FullCalendar source, boolean fromClient, @EventData("event.detail.data") ObjectNode entryData) {
        super(source, fromClient, entryData);
    }
}
