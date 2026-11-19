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
import lombok.Getter;
import lombok.ToString;
import org.vaadin.stefan.fullcalendar.converters.ConstraintValueConverter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Fires when a registered {@link Draggable} or an entry from another calendar is dropped onto the calendar.
 * Requires {@code droppable} to be {@code true}. For entries from another calendar, both calendars also need
 * {@code editable}.
 * <br><br>
 * {@link #getDateTime()} returns where the element was dropped, at the start of the day for a drop in the all-day
 * area. The created entry can be obtained via {@link #getCreatedEntry()}.
 * <br><br>
 * If the dropped element was registered via {@link FullCalendar#addDraggable(Draggable)},
 * use {@link #getDraggable()} for typed access.
 * <br><br>
 * {@link EntryReceiveEvent} also fires when the drop carries entry data: a {@link Draggable} with entry data or with
 * an entry data callback, or an entry from another calendar. A {@link Draggable} without entry data fires only this
 * event.
 * <br><br>
 * Client side name: drop
 */
@DomEvent("drop")
@Getter
@ToString(callSuper = true)
public class DropEvent extends DateTimeEvent {

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
     * @param date          the drop date or date time as ISO string
     * @param allDay        whether the drop occurred in the all-day area
     * @param draggedElData the entry data of the dropped element as JSON, or {@code null}
     * @param draggableId   the {@code data-draggable-id} attribute of the registered draggable, or {@code null}
     */
    public DropEvent(FullCalendar source, boolean fromClient,
                     @EventData("event.detail.date") String date,
                     @EventData("event.detail.allDay") boolean allDay,
                     @EventData("event.detail.draggedElData") String draggedElData,
                     @EventData("event.detail.draggableId") String draggableId) {
        super(source, fromClient, date, allDay);
        this.draggable = source.resolveDraggable(draggableId).orElse(null);

        createdEntry = new Entry();
        if (draggedElData != null) {
            ObjectNode data = (ObjectNode) JsonMapper.shared().readTree(draggedElData);
            // The entry data may come from a callback of your own. The library takes constraints there that a
            // ConstraintValue cannot hold, such as windows with different resources, so such a constraint is skipped
            // instead of failing the whole drop.
            JsonNode constraint = data.remove("constraint");
            createdEntry.updateAllFromJson(data, false);
            if (constraint != null && !constraint.isNull()) {
                try {
                    createdEntry.setConstraint(new ConstraintValueConverter().toServerModel(constraint, createdEntry));
                } catch (IllegalArgumentException e) {
                    // not representable, the created entry has no constraint
                }
            }
        } else {
            LocalDateTime start = getDateTime();
            createdEntry.setAllDay(allDay);
            createdEntry.setStart(start);
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
     * <p>
     *     <b>Please note</b> that the entry has no {@link RRule}, even when the draggable's entry data has one.
     *     Use {@link #getDraggable()} and {@link Draggable#getEntryData()} to get it.
     * </p>
     * @return created entry
     */
    public Entry getCreatedEntry() {
        return createdEntry;
    }
}
