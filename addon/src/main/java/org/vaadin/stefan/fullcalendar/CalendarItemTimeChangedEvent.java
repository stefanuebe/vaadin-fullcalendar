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

import lombok.Getter;
import lombok.ToString;
import tools.jackson.databind.node.ObjectNode;

/**
 * Abstract event for calendar items whose time has changed (by drag-and-drop or resize).
 * Provides a {@link Delta} with the amount of time the item was moved or resized.
 * <p>
 * Concrete subclasses: {@link CalendarItemDroppedEvent}, {@link CalendarItemResizedEvent}.
 * <p>
 * This is the CIP counterpart to {@link EntryTimeChangedEvent}.
 *
 * @param <T> the type of the calendar item POJO
 */
@Getter
@ToString(callSuper = true)
public abstract class CalendarItemTimeChangedEvent<T> extends CalendarItemDataEvent<T> {

    /**
     * The delta information. Provides the amount of time by which the item was moved or resized.
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
    protected CalendarItemTimeChangedEvent(FullCalendar<T> source, boolean fromClient,
            ObjectNode jsonItem, ObjectNode jsonDelta) {
        super(source, fromClient, jsonItem);
        this.delta = Delta.fromJson(jsonDelta);
    }
}
