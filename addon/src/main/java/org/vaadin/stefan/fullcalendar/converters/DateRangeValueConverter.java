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

import org.vaadin.stefan.fullcalendar.option.DateRange;
import org.vaadin.stefan.fullcalendar.option.DateRangeValue;
import org.vaadin.stefan.fullcalendar.option.JsCallback;
import tools.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Converts a {@link DateRangeValue} to its client form and back: an object with the set bounds as date strings, or
 * the callback marker.
 */
public class DateRangeValueConverter implements JsonItemPropertyConverter<DateRangeValue, Object> {

    @Override
    public boolean supports(Object type) {
        return type instanceof DateRangeValue;
    }

    @Override
    public JsonNode toClientModel(DateRangeValue serverValue, Object currentInstance) {
        return switch (serverValue) {
            case DateRange range -> new DateRangeConverter().toClientModel(range, null);
            case JsCallback callback -> callback.toMarkerJson();
        };
    }

    @Override
    public DateRangeValue toServerModel(JsonNode clientValue, Object currentInstance) {
        if (JsCallback.isMarker(clientValue)) {
            return JsCallback.fromMarkerJson(clientValue);
        }
        if (!clientValue.isObject()) {
            throw new IllegalArgumentException("Not a date range value: " + clientValue);
        }
        return new DateRange(date(clientValue, "start"), date(clientValue, "end"));
    }

    private static LocalDate date(JsonNode range, String bound) {
        JsonNode value = range.get(bound);
        if (value == null || value.isNull()) {
            return null;
        }
        try {
            return LocalDate.parse(value.asString());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Not a date: " + value, e);
        }
    }
}
