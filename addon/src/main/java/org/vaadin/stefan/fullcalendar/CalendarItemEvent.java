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
import lombok.Getter;
import lombok.ToString;

/**
 * Simple event that occurred for a specific calendar item managed by a {@link org.vaadin.stefan.fullcalendar.dataprovider.CalendarItemProvider}.
 * <p>
 * This is the CIP counterpart to {@link EntryEvent}. Use this hierarchy when the calendar is configured
 * with a CalendarItemProvider via {@link FullCalendar#setCalendarItemProvider}.
 *
 * @param <T> the type of the calendar item POJO
 */
@Getter
@ToString
public abstract class CalendarItemEvent<T> extends ComponentEvent<FullCalendar<T>> {

    /**
     * The calendar item for which the event occurred.
     */
    private final T item;

    /**
     * The id of the calendar item.
     */
    private final String itemId;

    /**
     * New instance. Resolves the item from the calendar's fetch cache using the given id.
     *
     * @param source     source component
     * @param fromClient from client
     * @param itemId     the id of the affected calendar item
     * @throws IllegalArgumentException when no cached item is found for the given id
     */
    protected CalendarItemEvent(FullCalendar<T> source, boolean fromClient, String itemId) {
        super(source, fromClient);
        this.itemId = itemId;
        this.item = source.getCachedItemFromFetch(itemId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No cached item found for id '" + itemId + "'"));
    }
}
