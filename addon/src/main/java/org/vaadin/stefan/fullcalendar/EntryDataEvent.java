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
 * Entry event that also carries the entry data the client sent, for example the start and end of the clicked
 * occurrence. Events that carry a change of the entry extend {@link EntryChangedEvent}.
 */
@Getter
@ToString(callSuper = true)
public abstract class EntryDataEvent extends EntryEvent {

    /**
     * The json object with the entry data from the client side.
     */
    private final ObjectNode jsonObject;

    /**
     * New instance. Awaits the entry data object.
     * @param source source component
     * @param fromClient is from client
     * @param jsonObject json object with the entry data
     */
    public EntryDataEvent(FullCalendar source, boolean fromClient, ObjectNode jsonObject) {
        super(source, fromClient, jsonObject.get(Entry.Fields.ID).asString());
        this.jsonObject = jsonObject;
    }

    /**
     * Returns a new {@link Entry} instance with the data of this event applied on top of the original entry, for
     * example the start and end of the clicked occurrence of a recurring entry. The original entry stored in
     * {@link #getEntry()} is not modified.
     *
     * @param <R> return type
     * @return a new entry with the data of this event
     * @since 8.0.0
     */
    @SuppressWarnings("unchecked")
    public <R extends Entry> R getDetailsAsEntry() {
        Entry copy = getEntry().copy();
        copy.updateFromJson(getJsonObject());
        return (R) copy; // we use R here, since in most cases event listeners do not specify a generic type
    }

    /**
     * Returns a new {@link Entry} instance with the data of this event applied on top of the original entry.
     *
     * @param <R> return type
     * @return a new entry with the data of this event
     * @since 7.2.0
     * @deprecated events without a change carry no changes, use {@link #getDetailsAsEntry()}. On an
     * {@link EntryChangedEvent}, this method stays available.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public <R extends Entry> R getChangesAsEntry() {
        return getDetailsAsEntry();
    }

}
