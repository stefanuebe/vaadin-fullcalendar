package org.vaadin.stefan.fullcalendar.converters;

import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.JsonFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;

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
    public JsonNode toClientModel(Collection<LocalDate> serverValue, T currentInstance) {
        if (serverValue == null || serverValue.isEmpty()) {
            return null;
        }
        ArrayNode array = JsonFactory.createArray();
        for (LocalDate date : serverValue) {
            array.add(date.toString());
        }
        return array;
    }
}
