package org.vaadin.stefan.fullcalendar.converters;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonValue;

import java.time.LocalDate;
import java.util.List;

/**
 * Converts the {@code exdate} field of an entry to the JSON array format expected by
 * FullCalendar's RRule plugin ({@code exdate: ["2025-03-10", "2025-03-17"]}).
 *
 * @param <T> the entry type
 */
public class ExdateConverter<T> implements JsonItemPropertyConverter<List<LocalDate>, T> {

    @Override
    public boolean supports(Object type) {
        return type == null || type instanceof List;
    }

    @Override
    public JsonValue toClientModel(List<LocalDate> serverValue, T currentInstance) {
        if (serverValue == null || serverValue.isEmpty()) {
            return null;
        }
        JsonArray array = Json.createArray();
        int i = 0;
        for (LocalDate date : serverValue) {
            array.set(i++, date.toString());
        }
        return array;
    }
}
