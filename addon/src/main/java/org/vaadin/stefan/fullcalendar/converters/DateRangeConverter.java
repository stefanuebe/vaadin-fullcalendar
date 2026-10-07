package org.vaadin.stefan.fullcalendar.converters;

import org.vaadin.stefan.fullcalendar.DateRange;
import org.vaadin.stefan.fullcalendar.JsonFactory;
import org.vaadin.stefan.fullcalendar.JsonUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Converts a {@link DateRange} to FullCalendar's range object with {@code start} and {@code end} date strings.
 * An open bound is left out.
 */
public class DateRangeConverter implements JsonItemPropertyConverter<DateRange, Object> {

    @Override
    public boolean supports(Object type) {
        return type instanceof DateRange;
    }

    @Override
    public JsonNode toClientModel(DateRange serverValue, Object currentInstance) {
        ObjectNode json = JsonFactory.createObject();
        if (serverValue.start() != null) {
            json.put("start", JsonUtils.formatClientSideDateString(serverValue.start()));
        }
        if (serverValue.end() != null) {
            json.put("end", JsonUtils.formatClientSideDateString(serverValue.end()));
        }
        return json;
    }
}
