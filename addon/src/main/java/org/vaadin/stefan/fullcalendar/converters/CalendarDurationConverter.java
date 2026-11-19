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
import org.vaadin.stefan.fullcalendar.option.CalendarDuration;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.time.Duration;
import java.util.Set;

/**
 * Converts a {@link CalendarDuration} to its client form and back: an object with the units {@code years},
 * {@code months}, {@code weeks}, {@code days}, {@code hours}, {@code minutes}, {@code seconds} and
 * {@code milliseconds}, each only when not zero. Reading back also takes a number of milliseconds and every string
 * {@link CalendarDuration#ofTimeString(String)} reads.
 */
public class CalendarDurationConverter implements JsonItemPropertyConverter<CalendarDuration, Object> {

    private static final String YEARS = "years";
    private static final String MONTHS = "months";
    private static final String WEEKS = "weeks";
    private static final String DAYS = "days";
    private static final String HOURS = "hours";
    private static final String MINUTES = "minutes";
    private static final String SECONDS = "seconds";
    private static final String MILLISECONDS = "milliseconds";
    private static final Set<String> KEYS = Set.of(YEARS, MONTHS, WEEKS, DAYS, HOURS, MINUTES, SECONDS, MILLISECONDS);

    private static void putUnlessZero(ObjectNode json, String unit, int value) {
        if (value != 0) {
            json.put(unit, value);
        }
    }

    @Override
    public boolean supports(Object type) {
        return type instanceof CalendarDuration;
    }

    @Override
    public JsonNode toClientModel(CalendarDuration serverValue, Object currentInstance) {
        ObjectNode json = JsonFactory.createObject();
        putUnlessZero(json, YEARS, serverValue.years());
        putUnlessZero(json, MONTHS, serverValue.months());
        putUnlessZero(json, WEEKS, serverValue.weeks());
        putUnlessZero(json, DAYS, serverValue.days());
        putUnlessZero(json, HOURS, serverValue.hours());
        putUnlessZero(json, MINUTES, serverValue.minutes());
        putUnlessZero(json, SECONDS, serverValue.seconds());
        putUnlessZero(json, MILLISECONDS, serverValue.milliseconds());

        return json;
    }

    @Override
    public CalendarDuration toServerModel(JsonNode clientValue, Object currentInstance) {
        if (clientValue.isString()) {
            return CalendarDuration.ofTimeString(clientValue.asString());
        }
        if (clientValue.isIntegralNumber()) {
            return CalendarDuration.of(Duration.ofMillis(clientValue.asLong()));
        }
        if (!clientValue.isObject() || !KEYS.containsAll(clientValue.propertyNames())
                || !clientValue.valueStream().allMatch(JsonNode::isInt)) {
            throw new IllegalArgumentException("Not a duration value: " + clientValue);
        }

        return new CalendarDuration(clientValue.path(YEARS).asInt(0), clientValue.path(MONTHS).asInt(0),
                clientValue.path(WEEKS).asInt(0), clientValue.path(DAYS).asInt(0), clientValue.path(HOURS).asInt(0),
                clientValue.path(MINUTES).asInt(0), clientValue.path(SECONDS).asInt(0),
                clientValue.path(MILLISECONDS).asInt(0));
    }
}
