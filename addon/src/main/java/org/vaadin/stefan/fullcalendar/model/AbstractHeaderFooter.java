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

package org.vaadin.stefan.fullcalendar.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.vaadin.stefan.fullcalendar.JsonFactory;
import tools.jackson.databind.node.ObjectNode;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Abstract base class for FullCalendar header and footer
 *
 * @deprecated set the toolbar with {@link org.vaadin.stefan.fullcalendar.Option#HEADER_TOOLBAR} or
 * {@link org.vaadin.stefan.fullcalendar.Option#FOOTER_TOOLBAR} and a {@code Map} of the positions
 * ({@code start}, {@code center}, {@code end}) to FullCalendar's button string, for example
 * {@code Map.of("start", "prev,next today", "center", "title", "end", "dayGridMonth,timeGridWeek")}.
 * {@link org.vaadin.stefan.fullcalendar.NativeToolbarParts} has constants for the positions and the built-in buttons.
 * This model knows only some of the buttons. View and custom buttons cannot be expressed with it, therefore it is deprecated.
 */
@Deprecated(since = "8.0.0", forRemoval = true)
@EqualsAndHashCode
@ToString
public class AbstractHeaderFooter {
    private final Map<HeaderFooterPartPosition, HeaderFooterPart> parts;

    /**
     * Creates a new instance.
     */
    protected AbstractHeaderFooter() {
        this.parts = new HashMap<>();
    }

    /**
     * Creates a new instance with the given parts.
     *
     * @param parts parts
     */
    protected AbstractHeaderFooter(Collection<HeaderFooterPart> parts) {
        this.parts = Objects.requireNonNull(parts).stream().collect(Collectors.toMap(HeaderFooterPart::getPosition, Function.identity()));
    }

    /**
     * Returns all parts. Never null
     *
     * @return parts
     */
    public Set<HeaderFooterPart> getParts() {
        return new HashSet<>(parts.values());
    }

    /**
     * Convenience method to get the left part of this instance. Creates a new instance on the first call.
     * @return left part
     */
    public HeaderFooterPart getStart() {
        return parts.computeIfAbsent(HeaderFooterPartPosition.START, HeaderFooterPart::new);
    }

    /**
     * Convenience method to get the center part of this instance. Creates a new instance on the first call.
     * @return center part
     */
    public HeaderFooterPart getCenter() {
        return parts.computeIfAbsent(HeaderFooterPartPosition.CENTER, HeaderFooterPart::new);
    }

    /**
     * Convenience method to get the right part of this instance. Creates a new instance on the first call.
     * @return right part
     */
    public HeaderFooterPart getEnd() {
        return parts.computeIfAbsent(HeaderFooterPartPosition.END, HeaderFooterPart::new);
    }

    /**
     * Converts the given object into a json object.
     *
     * @return json object
     */
    public ObjectNode toJson() {
        ObjectNode jsonObject = JsonFactory.createObject();

        for (HeaderFooterPart position : parts.values())
            jsonObject.put(
                    position.getPosition().getCode(),
                    position.getItems()
                            .stream()
                            .map(HeaderFooterItem::getCode)
                            .collect(Collectors.joining(",")));

        return jsonObject;
    }

    /**
     * Registers the given part. Overrides any previous set definitions.
     * @param part part
     */
    public void addPart(HeaderFooterPart part) {
        parts.put(part.getPosition(), part);
    }
}
