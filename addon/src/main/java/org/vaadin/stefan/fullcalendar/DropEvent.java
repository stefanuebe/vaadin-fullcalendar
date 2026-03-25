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
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Fires when any external HTML element is dropped onto the calendar (requires {@code droppable} to be {@code true}).
 * This fires for <em>all</em> external drops, including non-event elements.
 * <br><br>
 * The created entry can be obtained via {@link #getCreatedEntry()}.
 * <br><br>
 * If the dropped element was registered via {@link FullCalendar#addDraggable(Draggable)},
 * use {@link #getDraggable()} for typed access.
 * <br><br>
 * For drops of external elements that carry event data ({@code data-event} attribute) and are successfully
 * added to the calendar as entries, {@link EntryReceiveEvent} fires in addition to this event.
 * <br><br>
 * Client side name: drop
 */
@DomEvent("drop")
@Getter
@ToString
public class DropEvent extends ComponentEvent<FullCalendar> {

    /**
     * The calendar date onto which the element was dropped.
     */
    private final LocalDate date;

    /**
     * Whether the drop occurred in the all-day area.
     */
    private final boolean allDay;

    /**
     * The resolved {@link Draggable} instance, or {@code null} if the dropped element was not
     * registered via {@link FullCalendar#addDraggable(Draggable)} on this calendar.
     */
    private final Draggable draggable;
    private final Entry createdEntry;

    /**
     * New instance.
     *
     * @param source        the source component
     * @param fromClient    {@code true} if the event originated from the client
     * @param date          the drop date as ISO string
     * @param allDay        whether the drop occurred in the all-day area
     * @param draggedElData the {@code data-event} attribute content of the dropped element, or {@code null}
     * @param draggableId   the {@code data-draggable-id} attribute of the dropped element, or {@code null}
     */
    public DropEvent(FullCalendar source, boolean fromClient,
                     @EventData("event.detail.date") String date,
                     @EventData("event.detail.allDay") boolean allDay,
                     @EventData("event.detail.draggedElData") String draggedElData,
                     @EventData("event.detail.draggableId") String draggableId) {
        super(source, fromClient);
        this.date = JsonUtils.parseClientSideDate(date);
        this.allDay = allDay;
        this.draggable = source.resolveDraggable(draggableId).orElse(null);

        createdEntry = new Entry();
        if (draggedElData != null) {
            createdEntry.updateAllFromJson((ObjectNode) JsonMapper.shared().readTree(draggedElData), false);
        } else {
            createdEntry.setAllDay(allDay);
            createdEntry.setStart(JsonUtils.parseClientSideDate(date));
            LocalDateTime start = createdEntry.getStart();
            createdEntry.setEnd(allDay ? start.plusDays(1) : start.plusHours(1));
        }
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

    /**
     * Returns the entry, that has been created by the client side as a result of the drop operation. The content
     * of this entry may differ from the draggable entry data, depending on restrictions or limitations of the calendar.
     * <p>
     *     Please note, that this entry will NOT be automatically added to your entry provider in any case.
     * </p>
     * @return created entry
     */
    public Entry getCreatedEntry() {
        return createdEntry;
    }
}
