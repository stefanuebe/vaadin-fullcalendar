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
import org.vaadin.stefan.fullcalendar.option.ContentValue;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Converts the custom toolbar elements, a map of element name to {@link ContentValue}, to their client form and back:
 * an object with the client form of each content. The library's {@code toolbarElements} takes each content in the
 * forms of a render hook, so one map of {@link ContentValue} covers it and needs no value type of its own.
 */
public class ToolbarElementsConverter implements JsonItemPropertyConverter<Map<String, ContentValue>, Object> {

    private static final ContentValueConverter CONTENT = new ContentValueConverter();

    @Override
    public boolean supports(Object type) {
        return type instanceof Map<?, ?> map
                && map.entrySet().stream().allMatch(e -> e.getKey() instanceof String && e.getValue() instanceof ContentValue);
    }

    @Override
    public JsonNode toClientModel(Map<String, ContentValue> serverValue, Object currentInstance) {
        ObjectNode elements = JsonFactory.createObject();
        serverValue.forEach((name, content) -> elements.set(name, CONTENT.toClientModel(content, null)));
        return elements;
    }

    @Override
    public Map<String, ContentValue> toServerModel(JsonNode clientValue, Object currentInstance) {
        if (!clientValue.isObject()) {
            throw new IllegalArgumentException("Not a map of toolbar elements: " + clientValue);
        }
        Map<String, ContentValue> elements = new HashMap<>();
        clientValue.properties().forEach(e -> elements.put(e.getKey(), CONTENT.toServerModel(e.getValue(), null)));
        return elements;
    }
}
