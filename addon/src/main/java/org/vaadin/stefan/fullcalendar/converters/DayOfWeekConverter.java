package org.vaadin.stefan.fullcalendar.converters;

import elemental.json.JsonValue;
import org.vaadin.stefan.fullcalendar.JsonUtils;

import java.time.DayOfWeek;

/**
 * Converts a single {@link DayOfWeek} to a FullCalendar day-of-week integer
 * (Sunday=0, Monday=1, ..., Saturday=6).
 */
public class DayOfWeekConverter<T> implements JsonItemPropertyConverter<DayOfWeek, T> {

    @Override
    public boolean supports(Object type) {
        return type instanceof DayOfWeek;
    }

    @Override
    public JsonValue toClientModel(DayOfWeek serverValue, T currentInstance) {
        int fcValue = serverValue == DayOfWeek.SUNDAY ? 0 : serverValue.getValue();
        return JsonUtils.toJsonValue(fcValue);
    }
}
