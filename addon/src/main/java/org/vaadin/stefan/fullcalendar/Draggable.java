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
 * Wraps a Vaadin {@link Component} to make it (or its children) draggable onto a FullCalendar.
 * <p>
 * <b>Simple mode</b> (no itemSelector): The component itself is draggable. Optionally carries
 * static {@link Entry} data that will be used to create a calendar entry on drop.
 * <p>
 * <b>Container mode</b> (with itemSelector): The component acts as a container. Only children
 * matching the CSS selector are draggable. Use {@link #withEventDataCallback(JsCallback)} to
 * dynamically create entry data based on the dragged child element.
 * <p>
 * Register a Draggable via {@link FullCalendar#addDraggable(Draggable)}. The calendar must
 * have {@code droppable} set to {@code true} for drops to be accepted.
 *
 * @see FullCalendar#addDraggable(Draggable)
 * @see DropEvent
 */
public class Draggable {

    @Getter
    private final Component component;
    private final Entry entryData;

    @Getter
    private String itemSelector;

    @Getter
    private JsCallback eventDataCallback;

    /**
     * Creates a Draggable wrapping the given component without entry data.
     *
     * @param component the component to make draggable
     * @throws NullPointerException if component is null
     */
    public Draggable(Component component) {
        this(component, null);
    }

    /**
     * Creates a Draggable wrapping the given component with optional static entry data.
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
     * Returns the static entry data associated with this draggable, if any.
     *
     * @return optional entry data
     */
    public Optional<Entry> getEntryData() {
        return Optional.ofNullable(entryData);
    }

    /**
     * Sets a CSS selector to identify draggable children within the container component.
     * When set, the component itself is not draggable — only matching children are.
     * <p>
     * Example: {@code draggable.withItemSelector(".draggable-row")} makes only children
     * with the CSS class {@code draggable-row} draggable.
     *
     * @param itemSelector CSS selector for draggable children, or null to make the component itself draggable
     * @return this instance for chaining
     */
    public Draggable withItemSelector(String itemSelector) {
        this.itemSelector = itemSelector;
        return this;
    }

    /**
     * Sets a JavaScript callback that dynamically creates entry data based on the dragged element.
     * The callback receives the dragged DOM element and must return a FullCalendar event data object.
     * <p>
     * This is typically used together with {@link #withItemSelector(String)} for container-based dragging,
     * where each child may produce different entry data.
     * <p>
     * Example:
     * <pre>
     * draggable.withEventDataCallback(JsCallback.of(
     *     "function(el) { return { title: el.innerText, duration: '01:00' }; }"));
     * </pre>
     * <p>
     * When set, this takes precedence over static {@link #getEntryData()} for the client-side
     * event creation. The static entry data is still available server-side via the Draggable instance.
     *
     * @param callback JavaScript function receiving the dragged element, or null to use static entry data
     * @return this instance for chaining
     */
    public Draggable withEventDataCallback(JsCallback callback) {
        this.eventDataCallback = callback;
        return this;
    }
}
