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
import tools.jackson.databind.node.ObjectNode;

import java.util.Optional;

/**
 * Fires when an external element carrying event data ({@code data-event} attribute) has been dropped
 * onto the calendar and FullCalendar has created a new entry from it.
 * <br><br>
 * The {@link #getEntry()} method returns a freshly constructed {@link Entry} populated from the data
 * passed by the client. This entry is <em>not</em> added to the calendar's entry provider automatically;
 * the application must do this explicitly if it wants to persist the received entry
 * (e.g. via {@link org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider#addEntry(Entry)}).
 * <br><br>
 * If the dropped element was registered via {@link FullCalendar#addDraggable(Draggable)},
 * {@link #getDraggable()} provides access to the original Draggable (component + entry template).
 * <br><br>
 * Note: {@link DropEvent} fires for <em>all</em> external drops (including non-event elements), while
 * this event fires only when the dropped element produced a new calendar entry.
 * <br><br>
 * Client side name: eventReceive
 * @see DropEvent
 * @see Draggable
 */
@DomEvent("eventReceive")
@Getter
@ToString
public class EntryReceiveEvent extends ComponentEvent<FullCalendar> {

    /**
     * The newly created entry, constructed from the data reported by the client.
     * This entry is not tracked by the calendar's entry provider.
     */
    private final Entry entry;

    /**
     * The resolved {@link Draggable} instance, or {@code null} if the dropped element was not
     * registered via {@link FullCalendar#addDraggable(Draggable)} on this calendar.
     */
    private final Draggable draggable;

    /**
     * New instance.
     *
     * @param source      the source component
     * @param fromClient  {@code true} if the event originated from the client
     * @param entryData   JSON data for the received entry
     * @param draggableId the {@code data-draggable-id} attribute of the dropped element, or {@code null}
     */
    public EntryReceiveEvent(FullCalendar source, boolean fromClient,
                             @EventData("event.detail.data") ObjectNode entryData,
                             @EventData("event.detail.draggableId") String draggableId) {
        super(source, fromClient);
        Entry e = new Entry();
        e.updateAllFromJson(entryData, false);
        this.entry = e;
        this.draggable = source.resolveDraggable(draggableId).orElse(null);
    }

    /**
     * Returns the {@link Draggable} instance if the dropped element was registered via
     * {@link FullCalendar#addDraggable(Draggable)} on this calendar.
     *
     * @return optional draggable
     */
    public Optional<Draggable> getDraggable() {
        return Optional.ofNullable(draggable);
    }
}
