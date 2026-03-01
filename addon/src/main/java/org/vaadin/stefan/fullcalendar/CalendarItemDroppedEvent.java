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
import lombok.Getter;
import lombok.ToString;
import tools.jackson.databind.node.ObjectNode;

/**
 * Occurs when a calendar item's time has changed by drag and drop.
 * <p>
 * You can apply the changes to the referred item by calling {@link #applyChangesOnItem()}.
 * <p>
 * This is the CIP counterpart to {@link EntryDroppedEvent}. Use this event when the calendar is configured
 * with a CalendarItemProvider via {@link FullCalendar#setCalendarItemProvider}.
 * <br><br>
 * Client side name: eventDrop
 *
 * @param <T> the type of the calendar item POJO
 */
@DomEvent("eventDrop")
@Getter
@ToString(callSuper = true)
public class CalendarItemDroppedEvent<T> extends CalendarItemDataEvent<T> {

    /**
     * The delta information. Provides the amount of time by which the item was moved.
     * Note that the item data itself already reflects the updated times, so there is no need
     * to apply the delta manually.
     */
    private final Delta delta;

    /**
     * New instance.
     *
     * @param source     source component
     * @param fromClient is from client
     * @param jsonItem   JSON object with updated item data
     * @param jsonDelta  JSON object with delta information
     */
    public CalendarItemDroppedEvent(FullCalendar<T> source, boolean fromClient,
            @EventData("event.detail.data") ObjectNode jsonItem,
            @EventData("event.detail.delta") ObjectNode jsonDelta) {
        super(source, fromClient, jsonItem);
        this.delta = Delta.fromJson(jsonDelta);
    }
}
