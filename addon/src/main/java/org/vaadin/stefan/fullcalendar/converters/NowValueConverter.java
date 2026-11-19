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

import org.vaadin.stefan.fullcalendar.option.JsCallback;
import org.vaadin.stefan.fullcalendar.JsonUtils;
import org.vaadin.stefan.fullcalendar.option.NowValue;
import tools.jackson.databind.JsonNode;

/**
 * Converts a {@link NowValue} to its client form and back: a date string, a UTC date-time string like the start and end
 * of entries, or the callback marker.
 */
public class NowValueConverter implements JsonItemPropertyConverter<NowValue, Object> {

    @Override
    public boolean supports(Object type) {
        return type instanceof NowValue;
    }

    @Override
    public JsonNode toClientModel(NowValue serverValue, Object currentInstance) {
        return switch (serverValue) {
            case NowValue.Day day -> JsonUtils.toJsonNode(JsonUtils.formatClientSideDateString(day.date()));
            case NowValue.DateTime dateTime ->
                    JsonUtils.toJsonNode(JsonUtils.formatClientSideDateTimeString(dateTime.dateTime()));
            case JsCallback callback -> callback.toMarkerJson();
        };
    }

    @Override
    public NowValue toServerModel(JsonNode clientValue, Object currentInstance) {
        if (clientValue.isObject()) {
            return JsCallback.fromMarkerJson(clientValue);
        }
        if (!clientValue.isString()) {
            throw new IllegalArgumentException("Not a now value: " + clientValue);
        }
        String text = clientValue.asString();
        // a date alone is "yyyy-MM-dd", 10 characters
        return text.length() <= 10
                ? NowValue.ofDay(JsonUtils.parseClientSideDate(text))
                : NowValue.ofDateTime(JsonUtils.parseClientSideDateTime(text));
    }
}
