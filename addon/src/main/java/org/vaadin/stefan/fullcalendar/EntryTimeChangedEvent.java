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
import lombok.Getter;
import lombok.ToString;

/**
 * This specialized version of the entry changed event gives additional information about the changed time as
 * a delta instance.
 * <br><br>
 * You can apply the changes to the referred entry by calling the method {@link #applyChangesOnEntry()}.
 */
@Getter
@ToString(callSuper = true)
public class EntryTimeChangedEvent extends EntryChangedEvent {

    /**
     * The delta information. Please note, that the entry itself already is up-to-date, so there is no need
     * to apply the delta on it.
     */
    private final Delta delta;

    /**
     * New instance. Awaits the changed data object for the entry plus the json object for the delta information.
     * @param source source component
     * @param fromClient is from client
     * @param jsonEntry json object with changed data
     * @param jsonDelta json object with delta information
     */
    public EntryTimeChangedEvent(FullCalendar source, boolean fromClient, JsonObject jsonEntry, JsonObject jsonDelta) {
        super(source, fromClient, jsonEntry);
        this.delta = Delta.fromJson(jsonDelta);
    }

}
