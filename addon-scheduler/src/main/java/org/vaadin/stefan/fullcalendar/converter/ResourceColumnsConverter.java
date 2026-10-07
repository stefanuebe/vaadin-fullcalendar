package org.vaadin.stefan.fullcalendar.converter;

import org.vaadin.stefan.fullcalendar.JsonFactory;
import org.vaadin.stefan.fullcalendar.ResourceColumn;
import org.vaadin.stefan.fullcalendar.SchedulerOption;
import org.vaadin.stefan.fullcalendar.converters.JsonItemPropertyConverter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;

import java.util.List;

/**
 * Converts a list of {@link ResourceColumn} to the JSON array of {@link SchedulerOption#RESOURCE_COLUMNS}.
 */
public class ResourceColumnsConverter implements JsonItemPropertyConverter<List<? extends ResourceColumn>, Object> {
    @Override
    public boolean supports(Object type) {
        return type instanceof List<?> list && list.stream().allMatch(ResourceColumn.class::isInstance);
    }

    @Override
    public JsonNode toClientModel(List<? extends ResourceColumn> serverValue, Object currentInstance) {
        ArrayNode array = JsonFactory.createArray();
        serverValue.forEach(col -> array.add(col.toJson()));
        return array;
    }
}
