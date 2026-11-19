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
import org.vaadin.stefan.fullcalendar.option.WidthValue;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;

/**
 * Converts a {@link WidthValue} to its client form and back: the number of pixels, or the percentage as a string such
 * as {@code "25%"}. Reading back also takes a string of whole pixels such as {@code "250px"}.
 */
public class WidthValueConverter implements JsonItemPropertyConverter<WidthValue, Object> {

    @Override
    public boolean supports(Object type) {
        return type instanceof WidthValue;
    }

    @Override
    public JsonNode toClientModel(WidthValue serverValue, Object currentInstance) {
        return switch (serverValue) {
            case WidthValue.Pixels pixels -> JsonUtils.toJsonNode(pixels.pixels());
            case WidthValue.Percent percent ->
                    JsonUtils.toJsonNode(BigDecimal.valueOf(percent.percent()).stripTrailingZeros().toPlainString() + "%");
        };
    }

    @Override
    public WidthValue toServerModel(JsonNode clientValue, Object currentInstance) {
        try {
            if (clientValue.isInt()) {
                return WidthValue.ofPixels(clientValue.asInt());
            }
            String text = clientValue.isString() ? clientValue.asString() : "";
            if (text.endsWith("%")) {
                return WidthValue.ofPercent(Double.parseDouble(text.substring(0, text.length() - 1)));
            }
            if (text.endsWith("px")) {
                return WidthValue.ofPixels(Integer.parseInt(text.substring(0, text.length() - 2)));
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Not a width value: " + clientValue, e);
        }
        throw new IllegalArgumentException("Not a width value: " + clientValue);
    }
}
