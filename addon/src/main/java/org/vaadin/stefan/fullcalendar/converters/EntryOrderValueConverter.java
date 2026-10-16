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
import org.vaadin.stefan.fullcalendar.option.EntryOrder;
import org.vaadin.stefan.fullcalendar.option.EntryOrderValue;
import org.vaadin.stefan.fullcalendar.option.JsCallback;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;

/**
 * Converts an {@link EntryOrderValue} to its client form and back: an order as an array of property names, with a
 * leading {@code -} for descending, and callback markers; a single callback as its marker. Reading back also takes the
 * library's comma-separated string.
 */
public class EntryOrderValueConverter implements JsonItemPropertyConverter<EntryOrderValue, Object> {

    @Override
    public boolean supports(Object type) {
        return type instanceof EntryOrderValue;
    }

    @Override
    public JsonNode toClientModel(EntryOrderValue serverValue, Object currentInstance) {
        return switch (serverValue) {
            case EntryOrder order -> {
                ArrayNode array = JsonFactory.createArray();
                order.keys().forEach(key -> array.add(switch (key) {
                    case EntryOrder.Property property ->
                            JsonUtils.toJsonNode((property.descending() ? "-" : "") + property.name());
                    case JsCallback callback -> callback.toMarkerJson();
                }));
                yield array;
            }
            case JsCallback callback -> callback.toMarkerJson();
        };
    }

    @Override
    public EntryOrderValue toServerModel(JsonNode clientValue, Object currentInstance) {
        if (clientValue.isString()) {
            return EntryOrder.of(clientValue.asString().split("\\s*,\\s*"));
        }
        if (clientValue.isArray()) {
            return EntryOrder.of(clientValue.valueStream().map(EntryOrderValueConverter::toKey).toArray(EntryOrder.Key[]::new));
        }
        if (JsCallback.isMarker(clientValue)) {
            return JsCallback.fromMarkerJson(clientValue);
        }
        throw new IllegalArgumentException("Not an entry order value: " + clientValue);
    }

    private static EntryOrder.Key toKey(JsonNode key) {
        if (key.isString()) {
            return EntryOrder.of(key.asString()).keys().getFirst();
        }
        if (JsCallback.isMarker(key)) {
            return JsCallback.fromMarkerJson(key);
        }
        throw new IllegalArgumentException("Not a sort key: " + key);
    }
}
