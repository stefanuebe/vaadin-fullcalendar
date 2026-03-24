package org.vaadin.stefan.fullcalendar.converters;

import elemental.json.JsonArray;
import elemental.json.JsonValue;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.JsonFactory;

import java.time.LocalDate;
import java.util.Collection;

/**
 * Converts the {@code exdate} field of an {@link Entry} to the JSON array format expected by
 * FullCalendar's RRule plugin ({@code exdate: ["2025-03-10", "2025-03-17"]}).
 */
public class ExdateConverter<T extends Entry> implements JsonItemPropertyConverter<Collection<LocalDate>, T> {

    @Override
    public boolean supports(Object type) {
        return type == null || type instanceof Collection;
    }

    @Override
    public JsonValue toClientModel(Collection<LocalDate> serverValue, T currentInstance) {
        if (serverValue == null || serverValue.isEmpty()) {
            return null;
        }
        JsonArray array = JsonFactory.createArray();
        for (LocalDate date : serverValue) {
            array.set(array.length(), JsonFactory.create(date.toString()));
        }
        return array;
    }
}
