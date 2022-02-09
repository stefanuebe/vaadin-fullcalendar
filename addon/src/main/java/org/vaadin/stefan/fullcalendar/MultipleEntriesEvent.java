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

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Simple event that occurred for multiple calendar items.
 */
@Getter
@ToString
public abstract class MultipleEntriesEvent extends ComponentEvent<FullCalendar> {

    /**
     * The entry, for which the event occurred.
     */
    private final Set<Entry> entries;

    /**
     * New instance. Awaits the entry id.
     * @param source source component
     * @param fromClient from client
     * @param entryIds affected entry id
     */
    public MultipleEntriesEvent(FullCalendar source, boolean fromClient, Collection<String> entryIds) {
        super(source, fromClient);

        if (entryIds == null || entryIds.isEmpty()) {
            throw new IllegalArgumentException("IDs parameter must not be null nor empty");
        }

        this.entries = entryIds
                .stream()
                .map(id -> {
                    Optional<Entry> entry = source.getCachedEntryFromFetch(id);
                    if (!entry.isPresent()) {
                        throw new IllegalArgumentException("No item found with id " + id);
                    }
                    return entry;
                })
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    /**
     * Returns the entries for which this event occurred. Never null nor empty.
     * @return entries
     */
    public Collection<Entry> getEntries() {
        return entries;
    }
}
