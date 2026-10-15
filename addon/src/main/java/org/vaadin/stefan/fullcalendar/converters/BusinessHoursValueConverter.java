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

import org.vaadin.stefan.fullcalendar.BusinessHours;
import org.vaadin.stefan.fullcalendar.BusinessHoursValue;
import org.vaadin.stefan.fullcalendar.JsonFactory;
import org.vaadin.stefan.fullcalendar.JsonUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Converts a {@link BusinessHoursValue} to its client form and back: {@code false}, {@code true}, or an array of
 * business hours objects.
 */
public class BusinessHoursValueConverter implements JsonItemPropertyConverter<BusinessHoursValue, Object> {

    @Override
    public boolean supports(Object type) {
        return type instanceof BusinessHoursValue;
    }

    @Override
    public JsonNode toClientModel(BusinessHoursValue serverValue, Object currentInstance) {
        return switch (serverValue) {
            case BusinessHoursValue.None ignored -> JsonUtils.toJsonNode(false);
            case BusinessHoursValue.Default ignored -> JsonUtils.toJsonNode(true);
            case BusinessHoursValue.Hours hours -> {
                ArrayNode array = JsonFactory.createArray();
                hours.hours().forEach(entry -> array.add(entry.toJson()));
                yield array;
            }
        };
    }

    @Override
    public BusinessHoursValue toServerModel(JsonNode clientValue, Object currentInstance) {
        if (clientValue.isBoolean()) {
            return clientValue.asBoolean() ? BusinessHoursValue.DEFAULT : BusinessHoursValue.NONE;
        }
        if (!clientValue.isArray()) {
            throw new IllegalArgumentException("Not a business hours value: " + clientValue);
        }
        return new BusinessHoursValue.Hours(
                clientValue.valueStream().map(BusinessHoursValueConverter::toBusinessHours).toList());
    }

    /** Reads one business hours object as {@link BusinessHours#toJson()} writes it. */
    static BusinessHours toBusinessHours(JsonNode json) {
        if (!json.isObject() || !json.has("daysOfWeek") || !json.has("startTime") || !json.has("endTime")) {
            throw new IllegalArgumentException("Not a business hours object: " + json);
        }
        Set<DayOfWeek> days = json.get("daysOfWeek").valueStream()
                .map(day -> day.asInt() == 0 ? DayOfWeek.SUNDAY : DayOfWeek.of(day.asInt()))
                .collect(Collectors.toSet());
        String end = json.get("endTime").asString();
        return BusinessHours.of(days)
                .start(LocalTime.parse(json.get("startTime").asString()))
                .end(end.equals("24:00") ? LocalTime.MAX : LocalTime.parse(end));
    }
}
