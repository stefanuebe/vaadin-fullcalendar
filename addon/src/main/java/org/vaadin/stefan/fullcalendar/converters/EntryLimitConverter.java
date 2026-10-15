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

import org.vaadin.stefan.fullcalendar.EntryLimit;
import org.vaadin.stefan.fullcalendar.JsonUtils;
import tools.jackson.databind.JsonNode;

/**
 * Converts an {@link EntryLimit} to its client form and back: {@code false}, {@code true} or the number.
 */
public class EntryLimitConverter implements JsonItemPropertyConverter<EntryLimit, Object> {

    @Override
    public boolean supports(Object type) {
        return type instanceof EntryLimit;
    }

    @Override
    public JsonNode toClientModel(EntryLimit serverValue, Object currentInstance) {
        return switch (serverValue) {
            case EntryLimit.None ignored -> JsonUtils.toJsonNode(false);
            case EntryLimit.CellHeight ignored -> JsonUtils.toJsonNode(true);
            case EntryLimit.Count count -> JsonUtils.toJsonNode(count.count());
        };
    }

    @Override
    public EntryLimit toServerModel(JsonNode clientValue, Object currentInstance) {
        if (clientValue.isBoolean()) {
            return clientValue.asBoolean() ? EntryLimit.CELL_HEIGHT : EntryLimit.NONE;
        }
        if (!clientValue.isInt()) {
            throw new IllegalArgumentException("Not an entry limit: " + clientValue);
        }
        return EntryLimit.of(clientValue.asInt());
    }
}
