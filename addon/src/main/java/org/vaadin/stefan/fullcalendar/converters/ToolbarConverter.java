package org.vaadin.stefan.fullcalendar.converters;

import org.vaadin.stefan.fullcalendar.model.AbstractHeaderFooter;
import tools.jackson.databind.JsonNode;

/**
 * Converts a {@link AbstractHeaderFooter} (Header or Footer) to its JSON representation
 * for the FullCalendar client side.
 */
public class ToolbarConverter implements JsonItemPropertyConverter<AbstractHeaderFooter, Object> {

    @Override
    public boolean supports(Object type) {
        return type instanceof AbstractHeaderFooter;
    }

    @Override
    public JsonNode toClientModel(AbstractHeaderFooter serverValue, Object currentInstance) {
        return serverValue.toJson();
    }
}
