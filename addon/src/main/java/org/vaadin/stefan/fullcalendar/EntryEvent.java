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
 * Simple event that occurred for a specific calendar item.
 */
@Getter
@ToString
public abstract class EntryEvent extends ComponentEvent<FullCalendar> {

    /**
     * The entry, for which the event occurred.
     */
    private final Entry entry;

    /**
     * New instance. Awaits the entry id.
     * @param source source component
     * @param fromClient from client
     * @param entryId affected entry id
     */
    public EntryEvent(FullCalendar source, boolean fromClient, String entryId) {
        super(source, fromClient);
        this.entry = source.getCachedEntryFromFetch(entryId).orElseThrow(IllegalArgumentException::new);
    }
}
