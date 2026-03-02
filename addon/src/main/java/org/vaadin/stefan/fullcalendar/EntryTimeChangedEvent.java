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
 * Specialized entry event for time changes with delta information.
 * <p>
 * <b>Note:</b> Concrete entry events ({@link EntryDroppedEvent}, {@link EntryResizedEvent}) no longer
 * extend this class. They now extend the corresponding CIP event types directly
 * ({@link CalendarItemDroppedEvent}, {@link CalendarItemResizedEvent}).
 * <p>
 * Use {@code instanceof CalendarItemTimeChangedEvent} instead of {@code instanceof EntryTimeChangedEvent}
 * to match concrete entry events.
 *
 * @deprecated Use {@link CalendarItemTimeChangedEvent} instead. This class has no concrete subclasses.
 */
@Deprecated
@ToString(callSuper = true)
public class EntryTimeChangedEvent extends CalendarItemTimeChangedEvent<Entry> {

    /**
     * New instance. Awaits the changed data object for the entry plus the json object for the delta information.
     * @param source source component
     * @param fromClient is from client
     * @param jsonEntry json object with changed data
     * @param jsonDelta json object with delta information
     */
    public EntryTimeChangedEvent(FullCalendar<Entry> source, boolean fromClient, ObjectNode jsonEntry, ObjectNode jsonDelta) {
        super(source, fromClient, jsonEntry, jsonDelta);
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
     *
     * @return entry
     * @deprecated Use {@link #applyChangesOnItem()} instead.
     */
    @Deprecated
    public Entry applyChangesOnEntry() {
        return applyChangesOnItem();
    }

    /**
     * Creates a copy based on the referenced entry and the received data.
     *
     * @param <R> return type
     * @return copy
     * @deprecated Use the CIP event hierarchy instead.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public <R extends Entry> R createCopyBasedOnChanges() {
        try {
            Entry copy = getEntry().copy();
            copy.updateFromJson(getJsonObject());
            return (R) copy;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
