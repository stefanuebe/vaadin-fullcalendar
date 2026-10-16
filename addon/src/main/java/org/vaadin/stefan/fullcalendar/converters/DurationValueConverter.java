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
import org.vaadin.stefan.fullcalendar.option.DurationValue;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.time.Duration;
import java.time.Period;
import java.util.Set;

/**
 * Converts a {@link DurationValue} to its client form and back: a time-based duration as {@code "hh:mm:ss"}, a
 * calendar-based one as an object with {@code years}, {@code months}, {@code days} and {@code milliseconds}, each
 * only when not zero. Reading back also takes a number of milliseconds and every string
 * {@link DurationValue#ofTimeString(String)} reads.
 */
public class DurationValueConverter implements JsonItemPropertyConverter<DurationValue, Object> {

    private static final String YEARS = "years";
    private static final String MONTHS = "months";
    private static final String DAYS = "days";
    private static final String MILLISECONDS = "milliseconds";
    private static final Set<String> KEYS = Set.of(YEARS, MONTHS, DAYS, MILLISECONDS);

    private static final DurationConverter TIME = new DurationConverter();

    @Override
    public boolean supports(Object type) {
        return type instanceof DurationValue;
    }

    @Override
    public JsonNode toClientModel(DurationValue serverValue, Object currentInstance) {
        return switch (serverValue) {
            case DurationValue.Time time -> TIME.toClientModel(time.time(), null);
            case DurationValue.Calendar calendar -> {
                ObjectNode json = JsonFactory.createObject();
                Period period = calendar.period();
                if (period.getYears() != 0) {
                    json.put(YEARS, period.getYears());
                }
                if (period.getMonths() != 0) {
                    json.put(MONTHS, period.getMonths());
                }
                if (period.getDays() != 0) {
                    json.put(DAYS, period.getDays());
                }
                if (!calendar.time().isZero()) {
                    json.put(MILLISECONDS, calendar.time().toMillis());
                }
                yield json;
            }
        };
    }

    @Override
    public DurationValue toServerModel(JsonNode clientValue, Object currentInstance) {
        if (clientValue.isString()) {
            return DurationValue.ofTimeString(clientValue.asString());
        }
        if (clientValue.isIntegralNumber()) {
            return DurationValue.ofTime(Duration.ofMillis(clientValue.asLong()));
        }
        if (!clientValue.isObject() || !KEYS.containsAll(clientValue.propertyNames())) {
            throw new IllegalArgumentException("Not a duration value: " + clientValue);
        }
        Period period = Period.of(clientValue.path(YEARS).asInt(0), clientValue.path(MONTHS).asInt(0),
                clientValue.path(DAYS).asInt(0));
        Duration time = Duration.ofMillis(clientValue.path(MILLISECONDS).asLong(0));
        return period.isZero() ? DurationValue.ofTime(time) : DurationValue.ofCalendar(period, time);
    }
}
