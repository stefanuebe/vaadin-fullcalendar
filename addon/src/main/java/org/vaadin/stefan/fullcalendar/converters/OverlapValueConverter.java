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

import org.vaadin.stefan.fullcalendar.JsCallback;
import org.vaadin.stefan.fullcalendar.JsonUtils;
import org.vaadin.stefan.fullcalendar.Overlap;
import org.vaadin.stefan.fullcalendar.OverlapValue;
import tools.jackson.databind.JsonNode;

/**
 * Converts an {@link OverlapValue} to its client form and back: a boolean, or the callback marker.
 */
public class OverlapValueConverter implements JsonItemPropertyConverter<OverlapValue, Object> {

    @Override
    public boolean supports(Object type) {
        return type instanceof OverlapValue;
    }

    @Override
    public JsonNode toClientModel(OverlapValue serverValue, Object currentInstance) {
        return switch (serverValue) {
            case Overlap overlap -> JsonUtils.toJsonNode(overlap == Overlap.ALLOWED);
            case JsCallback callback -> callback.toMarkerJson();
        };
    }

    @Override
    public OverlapValue toServerModel(JsonNode clientValue, Object currentInstance) {
        if (clientValue.isObject()) {
            return JsCallback.fromMarkerJson(clientValue);
        }
        if (!clientValue.isBoolean()) {
            throw new IllegalArgumentException("Not an overlap value: " + clientValue);
        }
        return Overlap.of(clientValue.asBoolean());
    }
}
