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

import elemental.json.JsonObject;
import lombok.ToString;

/**
 * An event, that occur, when a entry has been changed on the client side.
 * <br><br>
 * You can apply the changes to the referred entry by calling the method {@link #applyChangesOnEntry()}.
 */
@ToString(callSuper = true)
public abstract class EntryChangedEvent extends EntryDataEvent {

    /**
     * New instance. Awaits the changed data object.
     * @param source source component
     * @param fromClient is from client
     * @param jsonObject json object with changed data
     */
    public EntryChangedEvent(FullCalendar source, boolean fromClient, JsonObject jsonObject) {
        super(source, fromClient, jsonObject);
    }

    /**
     * Applies the contained changes on the refering entry and returns this instance.
     * @see Entry#update(JsonObject)
     * @return entry
     */
    public Entry applyChangesOnEntry() {
        Entry entry = getEntry();
        entry.updateFromJson(getJsonObject(), true);//        getSource().updateEntry(entry); // TODO this is an extra roundtrip, not needed currently?
        return entry;
    }

    public <R extends Entry> R createCopyBasedOnChanges() {
        try {
            Entry copy = getEntry().copy();

            JsonObject jsonObject = getJsonObject();
            copy.update(jsonObject);

            return (R) copy; // we use R here, since in most cases event listeners do not specify a generic type

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
