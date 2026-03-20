package org.vaadin.stefan.fullcalendar.converters;

import org.vaadin.stefan.fullcalendar.JsonUtils;
import tools.jackson.databind.JsonNode;

import java.time.DayOfWeek;

/**
 * Converts a single {@link DayOfWeek} to a FullCalendar day-of-week integer
 * (Sunday=0, Monday=1, ..., Saturday=6).
 */
public class DayOfWeekConverter implements JsonItemPropertyConverter<DayOfWeek, Object> {

    @Override
    public boolean supports(Object type) {
        return type instanceof DayOfWeek;
    }

    @Override
    public JsonNode toClientModel(DayOfWeek serverValue, Object currentInstance) {
        int fcValue = serverValue == DayOfWeek.SUNDAY ? 0 : serverValue.getValue();
        return JsonUtils.toJsonNode(fcValue);
    }
}
