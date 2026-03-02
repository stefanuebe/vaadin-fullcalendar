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

import lombok.ToString;
import tools.jackson.databind.node.ObjectNode;

/**
 * Extended entry event type that provides additional client-side entry data.
 * <p>
 * <b>Note:</b> Concrete entry events (e.g. {@link EntryClickedEvent}, {@link EntryDroppedEvent}) no longer
 * extend this class. They now extend the corresponding CIP event types directly
 * (e.g. {@link CalendarItemClickedEvent}, {@link CalendarItemDroppedEvent}).
 * <p>
 * Use {@code instanceof CalendarItemDataEvent} instead of {@code instanceof EntryDataEvent} to match
 * concrete entry events.
 *
 * @deprecated Use {@link CalendarItemDataEvent} instead. This class has no concrete subclasses.
 */
@Deprecated
@ToString(callSuper = true)
public abstract class EntryDataEvent extends CalendarItemDataEvent<Entry> {

    /**
     * New instance. Awaits the changed data object.
     * @param source source component
     * @param fromClient is from client
     * @param jsonObject json object with changed data
     */
    public EntryDataEvent(FullCalendar<Entry> source, boolean fromClient, ObjectNode jsonObject) {
        super(source, fromClient, jsonObject);
    }

    /**
     * Returns the entry for which the event occurred.
     *
     * @return entry
     * @deprecated Use {@link #getItem()} instead.
     */
    @Deprecated
    public Entry getEntry() {
        return getItem();
    }

    /**
     * Applies the contained changes on the referring entry and returns this instance.
     * @see Entry#updateFromJson(tools.jackson.databind.node.ObjectNode)
     * @return entry
     * @deprecated Use {@link #applyChangesOnItem()} instead.
     */
    @Deprecated
    public Entry applyChangesOnEntry() {
        return applyChangesOnItem();
    }

    /**
     * Creates a copy based on the referenced entry and the received data.
     * @param <R> return type
     * @return copy
     * @deprecated Use the CIP event hierarchy instead.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public <R extends Entry> R createCopyBasedOnChanges() {
        try {
            Entry copy = getEntry().copy();

            ObjectNode jsonObject = getJsonObject();
            copy.updateFromJson(jsonObject);

            return (R) copy; // we use R here, since in most cases event listeners do not specify a generic type

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
