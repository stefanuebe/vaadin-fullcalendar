package org.vaadin.stefan.fullcalendar.converter;

import org.vaadin.stefan.fullcalendar.JsonFactory;
import org.vaadin.stefan.fullcalendar.option.AbstractResourceColumn;
import org.vaadin.stefan.fullcalendar.FullCalendarSchedulerOptions;
import org.vaadin.stefan.fullcalendar.converters.JsonItemPropertyConverter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;

import java.util.List;

/**
 * Converts a list of {@link AbstractResourceColumn} to the JSON array of
 * {@link FullCalendarSchedulerOptions#setResourceColumns(List) resourceColumns}.
 */
public class ResourceColumnsConverter
        implements JsonItemPropertyConverter<List<? extends AbstractResourceColumn<?>>, Object> {
    @Override
    public boolean supports(Object type) {
        return type instanceof List<?> list && list.stream().allMatch(AbstractResourceColumn.class::isInstance);
    }

    @Override
    public JsonNode toClientModel(List<? extends AbstractResourceColumn<?>> serverValue, Object currentInstance) {
        ArrayNode array = JsonFactory.createArray();
        serverValue.forEach(col -> array.add(col.toJson()));
        return array;
    }
}
