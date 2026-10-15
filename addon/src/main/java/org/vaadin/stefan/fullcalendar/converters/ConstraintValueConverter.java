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

import org.vaadin.stefan.fullcalendar.ConstraintValue;
import org.vaadin.stefan.fullcalendar.JsonFactory;
import org.vaadin.stefan.fullcalendar.JsonUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;

/**
 * Converts a {@link ConstraintValue} to its client form and back: the group id, {@code "businessHours"}, or an array
 * of business hours objects. A single business hours object, which the library takes as well, is read as one window.
 */
public class ConstraintValueConverter implements JsonItemPropertyConverter<ConstraintValue, Object> {

    private static final String BUSINESS_HOURS = "businessHours";

    @Override
    public boolean supports(Object type) {
        return type instanceof ConstraintValue;
    }

    @Override
    public JsonNode toClientModel(ConstraintValue serverValue, Object currentInstance) {
        return switch (serverValue) {
            case ConstraintValue.GroupId groupId -> JsonUtils.toJsonNode(groupId.groupId());
            case ConstraintValue.CalendarBusinessHours ignored -> JsonUtils.toJsonNode(BUSINESS_HOURS);
            case ConstraintValue.Windows windows -> {
                ArrayNode array = JsonFactory.createArray();
                windows.windows().forEach(window -> array.add(window.toJson()));
                yield array;
            }
        };
    }

    @Override
    public ConstraintValue toServerModel(JsonNode clientValue, Object currentInstance) {
        if (clientValue.isObject()) {
            return ConstraintValue.windows(BusinessHoursValueConverter.toBusinessHours(clientValue));
        }
        if (clientValue.isArray()) {
            return new ConstraintValue.Windows(
                    clientValue.valueStream().map(BusinessHoursValueConverter::toBusinessHours).toList());
        }
        if (!clientValue.isString()) {
            throw new IllegalArgumentException("Not a constraint value: " + clientValue);
        }
        String text = clientValue.asString();
        return text.equals(BUSINESS_HOURS) ? ConstraintValue.BUSINESS_HOURS : ConstraintValue.groupId(text);
    }
}
