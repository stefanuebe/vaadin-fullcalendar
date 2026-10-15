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

import org.vaadin.stefan.fullcalendar.DateFormat;
import org.vaadin.stefan.fullcalendar.DateFormatValue;
import org.vaadin.stefan.fullcalendar.JsCallback;
import org.vaadin.stefan.fullcalendar.JsonUtils;
import tools.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converts a {@link DateFormatValue} to its client form and back: the format object, or the callback marker.
 */
public class DateFormatValueConverter implements JsonItemPropertyConverter<DateFormatValue, Object> {

    @Override
    public boolean supports(Object type) {
        return type instanceof DateFormatValue;
    }

    @Override
    public JsonNode toClientModel(DateFormatValue serverValue, Object currentInstance) {
        return switch (serverValue) {
            case DateFormat format -> JsonUtils.toJsonNode(format.properties());
            case JsCallback callback -> callback.toMarkerJson();
        };
    }

    @Override
    public DateFormatValue toServerModel(JsonNode clientValue, Object currentInstance) {
        return JsCallback.isMarker(clientValue) ? JsCallback.fromMarkerJson(clientValue) : toDateFormat(clientValue);
    }

    /** Reads a format object as {@link #toClientModel} writes it. */
    static DateFormat toDateFormat(JsonNode json) {
        if (!json.isObject()) {
            throw new IllegalArgumentException("Not a format object: " + json);
        }
        Map<String, Object> properties = new LinkedHashMap<>();
        json.properties().forEach(property -> properties.put(property.getKey(), toValue(property.getValue())));
        return DateFormat.of(properties);
    }

    private static Object toValue(JsonNode value) {
        if (value.isString()) {
            return value.asString();
        }
        if (value.isBoolean()) {
            return value.asBoolean();
        }
        if (value.isInt()) {
            return value.asInt();
        }
        if (value.isNumber()) {
            return value.asDouble();
        }
        throw new IllegalArgumentException("Not a format property value: " + value);
    }
}
