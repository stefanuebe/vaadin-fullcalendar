package org.vaadin.stefan.fullcalendar.converters;

import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.JsonFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;

/**
 * Converts the {@code exdate} field of an {@link Entry} to the JSON format expected by FullCalendar's
 * RRule plugin.
 * <p>
 * FullCalendar accepts {@code exdate} as either a single date string or a JSON array of date strings.
 * This converter handles the comma-separated multi-date input format used by the Java API:
 * <ul>
 *   <li>Single date (e.g. {@code "2025-03-10"}) → serialized as a JSON string</li>
 *   <li>Multiple dates (e.g. {@code "2025-03-10,2025-03-17"}) → serialized as a JSON array
 *       {@code ["2025-03-10","2025-03-17"]}</li>
 * </ul>
 */
public class ExdateConverter<T extends Entry> implements JsonItemPropertyConverter<String, T> {

    @Override
    public boolean supports(Object type) {
        return type == null || type instanceof String;
    }

    @Override
    public JsonNode toClientModel(String serverValue, T currentInstance) {
        if (serverValue == null) {
            return null;
        }
        if (serverValue.contains(",")) {
            ArrayNode array = JsonFactory.createArray();
            for (String date : serverValue.split(",")) {
                array.add(date.trim());
            }
            return array;
        }
        return JsonFactory.create(serverValue);
    }
}
