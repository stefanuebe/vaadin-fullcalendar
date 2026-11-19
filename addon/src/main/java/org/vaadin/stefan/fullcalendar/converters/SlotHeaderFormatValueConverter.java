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
import org.vaadin.stefan.fullcalendar.option.DateFormat;
import org.vaadin.stefan.fullcalendar.option.DateFormatValue;
import org.vaadin.stefan.fullcalendar.option.JsCallback;
import org.vaadin.stefan.fullcalendar.option.SlotHeaderFormatValue;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts a {@link SlotHeaderFormatValue} to its client form and back: the format object, the callback marker, or an
 * array of both kinds, one per header row.
 */
public class SlotHeaderFormatValueConverter implements JsonItemPropertyConverter<SlotHeaderFormatValue, Object> {

    private final DateFormatValueConverter tierConverter = new DateFormatValueConverter();

    @Override
    public boolean supports(Object type) {
        return type instanceof SlotHeaderFormatValue;
    }

    @Override
    public JsonNode toClientModel(SlotHeaderFormatValue serverValue, Object currentInstance) {
        return switch (serverValue) {
            case DateFormat format -> JsonUtils.toJsonNode(format.properties());
            case JsCallback callback -> callback.toMarkerJson();
            case SlotHeaderFormatValue.Tiers tiers -> {
                ArrayNode array = JsonFactory.createArray();
                tiers.tiers().forEach(tier -> array.add(tierConverter.toClientModel(tier, currentInstance)));
                yield array;
            }
        };
    }

    @Override
    public SlotHeaderFormatValue toServerModel(JsonNode clientValue, Object currentInstance) {
        if (clientValue.isArray()) {
            List<DateFormatValue> tiers = new ArrayList<>();
            clientValue.forEach(tier -> tiers.add(tierConverter.toServerModel(tier, currentInstance)));
            return new SlotHeaderFormatValue.Tiers(tiers);
        }
        return JsCallback.isMarker(clientValue)
                ? JsCallback.fromMarkerJson(clientValue)
                : DateFormatValueConverter.toDateFormat(clientValue);
    }
}
