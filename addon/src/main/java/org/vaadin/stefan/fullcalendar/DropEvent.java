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

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

/**
 * Fires when any external HTML element is dropped onto the calendar (requires {@code droppable} to be {@code true}).
 * This fires for <em>all</em> external drops, including non-event elements.
 * <br><br>
 * For drops of external elements that carry event data ({@code data-event} attribute) and are successfully
 * added to the calendar as entries, {@link EntryReceiveEvent} fires in addition to this event.
 * <br><br>
 * To make external elements droppable onto the calendar:
 * <ol>
 *   <li>Call {@link FullCalendar#setDroppable(boolean) setDroppable(true)} on the calendar.</li>
 *   <li>Attach a {@code data-event} JSON attribute to the draggable HTML element, e.g.
 *       {@code data-event='{"title":"Meeting","duration":"01:00"}'}.</li>
 *   <li>Register the element with FullCalendar's {@code Draggable} API on the client side, or add the
 *       {@code fc-event} CSS class to auto-register.
 *       See <a href="https://fullcalendar.io/docs/third-party-dragging">FullCalendar external dragging documentation</a>.</li>
 * </ol>
 * <br>
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
     * The raw JSON string from the dropped element's {@code data-event} attribute, or {@code null}
     * if the element had no such attribute.
     */
    private final String draggedElData;

    /**
     * New instance.
     *
     * @param source        the source component
     * @param fromClient    {@code true} if the event originated from the client
     * @param date          the drop date as ISO string
     * @param allDay        whether the drop occurred in the all-day area
     * @param draggedElData the {@code data-event} attribute content of the dropped element, or {@code null}
     */
    public DropEvent(FullCalendar source, boolean fromClient,
                     @EventData("event.detail.date") String date,
                     @EventData("event.detail.allDay") boolean allDay,
                     @EventData("event.detail.draggedElData") String draggedElData) {
        super(source, fromClient);
        this.date = JsonUtils.parseClientSideDate(date);
        this.allDay = allDay;
        this.draggedElData = draggedElData;
    }
}
