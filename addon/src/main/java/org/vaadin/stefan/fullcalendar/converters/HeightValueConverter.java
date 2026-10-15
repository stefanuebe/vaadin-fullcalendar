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

import org.vaadin.stefan.fullcalendar.HeightValue;
import org.vaadin.stefan.fullcalendar.JsonUtils;
import tools.jackson.databind.JsonNode;

/**
 * Converts a {@link HeightValue} to its client form and back: {@code "auto"}, the number of pixels, or the CSS length.
 */
public class HeightValueConverter implements JsonItemPropertyConverter<HeightValue, Object> {

    private static final String AUTO = "auto";

    @Override
    public boolean supports(Object type) {
        return type instanceof HeightValue;
    }

    @Override
    public JsonNode toClientModel(HeightValue serverValue, Object currentInstance) {
        return switch (serverValue) {
            case HeightValue.Auto ignored -> JsonUtils.toJsonNode(AUTO);
            case HeightValue.Pixels pixels -> JsonUtils.toJsonNode(pixels.pixels());
            case HeightValue.Css css -> JsonUtils.toJsonNode(css.length());
        };
    }

    @Override
    public HeightValue toServerModel(JsonNode clientValue, Object currentInstance) {
        if (clientValue.isInt()) {
            return HeightValue.ofPixels(clientValue.asInt());
        }
        if (!clientValue.isString()) {
            throw new IllegalArgumentException("Not a height value: " + clientValue);
        }
        return clientValue.asString().equals(AUTO) ? HeightValue.AUTO : HeightValue.ofCss(clientValue.asString());
    }
}
