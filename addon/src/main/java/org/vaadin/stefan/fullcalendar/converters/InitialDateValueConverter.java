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

import org.vaadin.stefan.fullcalendar.JsonUtils;
import org.vaadin.stefan.fullcalendar.option.InitialDateValue;
import tools.jackson.databind.JsonNode;

/**
 * Converts an {@link InitialDateValue} to its client form and back: a date string, or a UTC date-time string like the
 * start and end of entries.
 */
public class InitialDateValueConverter implements JsonItemPropertyConverter<InitialDateValue, Object> {

    @Override
    public boolean supports(Object type) {
        return type instanceof InitialDateValue;
    }

    @Override
    public JsonNode toClientModel(InitialDateValue serverValue, Object currentInstance) {
        return switch (serverValue) {
            case InitialDateValue.Day day -> JsonUtils.toJsonNode(JsonUtils.formatClientSideDateString(day.date()));
            case InitialDateValue.DateTime dateTime ->
                    JsonUtils.toJsonNode(JsonUtils.formatClientSideDateTimeString(dateTime.dateTime()));
        };
    }

    @Override
    public InitialDateValue toServerModel(JsonNode clientValue, Object currentInstance) {
        if (!clientValue.isString()) {
            throw new IllegalArgumentException("Not an initial date value: " + clientValue);
        }

        String text = clientValue.asString();

        // a date alone is "yyyy-MM-dd", 10 characters
        return text.length() <= 10
                ? InitialDateValue.ofDay(JsonUtils.parseClientSideDate(text))
                : InitialDateValue.ofDateTime(JsonUtils.parseClientSideDateTime(text));
    }
}
