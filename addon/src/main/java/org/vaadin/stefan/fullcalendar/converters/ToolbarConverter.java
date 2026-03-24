package org.vaadin.stefan.fullcalendar.converters;

import elemental.json.JsonValue;
import org.vaadin.stefan.fullcalendar.model.AbstractHeaderFooter;

/**
 * Converts a {@link AbstractHeaderFooter} (Header or Footer) to its JSON representation
 * for the FullCalendar client side.
 */
public class ToolbarConverter<T> implements JsonItemPropertyConverter<AbstractHeaderFooter, T> {

    @Override
    public boolean supports(Object type) {
        return type instanceof AbstractHeaderFooter;
    }

    @Override
    public JsonValue toClientModel(AbstractHeaderFooter serverValue, T currentInstance) {
        return serverValue.toJson();
    }
}
