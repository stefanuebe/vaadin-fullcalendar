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
 * Extended calendar item event type that provides additional client-side data, which can be interpreted on the
 * server side.
 * <p>
 * This is the CIP counterpart to {@link EntryDataEvent}. Use this hierarchy when the calendar is configured
 * with a CalendarItemProvider via {@link FullCalendar#setCalendarItemProvider}.
 *
 * @param <T> the type of the calendar item POJO
 */
@Getter
@ToString(callSuper = true)
public abstract class CalendarItemDataEvent<T> extends CalendarItemEvent<T> {

    /**
     * The JSON object containing the current state / changes from the client side.
     */
    private final ObjectNode jsonObject;

    /**
     * New instance. Extracts the item id from the JSON object and resolves the item from cache.
     *
     * @param source     source component
     * @param fromClient is from client
     * @param jsonObject JSON object with current item data
     * @throws IllegalArgumentException when no cached item is found for the id contained in the JSON object
     */
    protected CalendarItemDataEvent(FullCalendar<T> source, boolean fromClient, ObjectNode jsonObject) {
        super(source, fromClient, jsonObject.get("id").asString());
        this.jsonObject = jsonObject;
    }

    private CalendarItemChanges changes;

    /**
     * Returns a typed accessor for the changed properties sent by the client.
     *
     * @return typed changes accessor
     */
    public CalendarItemChanges getChanges() {
        if (changes == null) {
            changes = new CalendarItemChanges(jsonObject);
        }
        return changes;
    }

    /**
     * Applies the contained changes on the referring calendar item using the configured update strategy
     * (mapper setters or update handler) and returns the updated item.
     *
     * @return the updated calendar item
     * @throws IllegalStateException if no update strategy is configured on the calendar
     */
    public T applyChangesOnItem() {
        T item = getItem();
        getSource().applyCalendarItemChanges(item, jsonObject);
        return item;
    }
}
