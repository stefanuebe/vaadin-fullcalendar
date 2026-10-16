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
import org.vaadin.stefan.fullcalendar.option.NoneValue;
import org.vaadin.stefan.fullcalendar.option.ListTextValue;
import org.vaadin.stefan.fullcalendar.option.TextContent;
import tools.jackson.databind.JsonNode;

/**
 * Converts a {@link ListTextValue} to its client form and back: the text, or {@code false}.
 */
public class ListTextValueConverter implements JsonItemPropertyConverter<ListTextValue, Object> {

    @Override
    public boolean supports(Object type) {
        return type instanceof ListTextValue;
    }

    @Override
    public JsonNode toClientModel(ListTextValue serverValue, Object currentInstance) {
        return switch (serverValue) {
            case TextContent text -> JsonUtils.toJsonNode(text.text());
            case NoneValue.None ignored -> JsonUtils.toJsonNode(false);
        };
    }

    @Override
    public ListTextValue toServerModel(JsonNode clientValue, Object currentInstance) {
        if (clientValue.isBoolean() && !clientValue.asBoolean()) {
            return ListTextValue.NONE;
        }
        if (!clientValue.isString()) {
            throw new IllegalArgumentException("Not a list text value: " + clientValue);
        }
        return TextContent.of(clientValue.asString());
    }
}
