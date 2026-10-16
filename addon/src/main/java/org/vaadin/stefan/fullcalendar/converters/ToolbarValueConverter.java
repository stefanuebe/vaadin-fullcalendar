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
package org.vaadin.stefan.fullcalendar.converters;

import org.vaadin.stefan.fullcalendar.JsonFactory;
import org.vaadin.stefan.fullcalendar.JsonUtils;
import org.vaadin.stefan.fullcalendar.option.NoneValue;
import org.vaadin.stefan.fullcalendar.option.ToolbarValue;
import tools.jackson.databind.JsonNode;

import java.util.Set;

/**
 * Converts a {@link ToolbarValue} to its client form and back: an object with the keys {@code start}, {@code center}
 * and {@code end}, or {@code left}, {@code center} and {@code right}, or {@code false}.
 */
public class ToolbarValueConverter implements JsonItemPropertyConverter<ToolbarValue, Object> {

    private static final Set<String> SECTIONS = Set.of("start", "center", "end", "left", "right");

    @Override
    public boolean supports(Object type) {
        return type instanceof ToolbarValue;
    }

    @Override
    public JsonNode toClientModel(ToolbarValue serverValue, Object currentInstance) {
        return switch (serverValue) {
            case ToolbarValue.StartEnd sections -> JsonFactory.createObject()
                    .put("start", sections.start()).put("center", sections.center()).put("end", sections.end());
            case ToolbarValue.LeftRight sections -> JsonFactory.createObject()
                    .put("left", sections.left()).put("center", sections.center()).put("right", sections.right());
            case NoneValue.None ignored -> JsonUtils.toJsonNode(false);
        };
    }

    @Override
    public ToolbarValue toServerModel(JsonNode clientValue, Object currentInstance) {
        if (clientValue.isBoolean() && !clientValue.asBoolean()) {
            return ToolbarValue.NONE;
        }
        if (!clientValue.isObject()) {
            throw new IllegalArgumentException("Not a toolbar value: " + clientValue);
        }
        for (var property : clientValue.properties()) {
            if (!SECTIONS.contains(property.getKey()) || !property.getValue().isString()) {
                throw new IllegalArgumentException("Not a toolbar section: " + property);
            }
        }
        boolean leftRight = clientValue.has("left") || clientValue.has("right");
        if (leftRight && (clientValue.has("start") || clientValue.has("end"))) {
            throw new IllegalArgumentException("A toolbar has either start and end or left and right: " + clientValue);
        }
        String center = clientValue.path("center").asString("");
        return leftRight
                ? ToolbarValue.ofLeftRight(clientValue.path("left").asString(""), center,
                        clientValue.path("right").asString(""))
                : ToolbarValue.ofStartEnd(clientValue.path("start").asString(""), center,
                        clientValue.path("end").asString(""));
    }
}
