/*
 * Copyright 2026, Stefan Uebe
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

import com.vaadin.flow.component.Component;
import lombok.Getter;

import java.util.Objects;
import java.util.Optional;

/**
 * Wraps a Vaadin {@link Component} to make it draggable onto a FullCalendar.
 * Optionally carries {@link Entry} data that will be used to create a calendar entry on drop.
 * <p>
 * Register a Draggable via {@link FullCalendar#addDraggable(Draggable)}. The calendar must
 * have {@code droppable} set to {@code true} for drops to be accepted.
 * <p>
 * Entry data is immutable after construction — changes require creating a new Draggable instance
 * and re-registering it.
 *
 * @see FullCalendar#addDraggable(Draggable)
 * @see DropEvent
 */
public class Draggable {

    @Getter
    private final Component component;
    private final Entry entryData;

    /**
     * Creates a Draggable wrapping the given component without entry data.
     * When dropped, the {@link DropEvent} will have no entry information.
     *
     * @param component the component to make draggable
     * @throws NullPointerException if component is null
     */
    public Draggable(Component component) {
        this(component, null);
    }

    /**
     * Creates a Draggable wrapping the given component with optional entry data.
     * When dropped onto a calendar, the entry data is available via {@link DropEvent#getDraggedEntry()}.
     *
     * @param component the component to make draggable
     * @param entryData optional entry data to associate with the drop (may be null)
     * @throws NullPointerException if component is null
     */
    public Draggable(Component component, Entry entryData) {
        Objects.requireNonNull(component, "component");
        this.component = component;
        this.entryData = entryData;
    }

    /**
     * Returns the entry data associated with this draggable, if any.
     *
     * @return optional entry data
     */
    public Optional<Entry> getEntryData() {
        return Optional.ofNullable(entryData);
    }
}
