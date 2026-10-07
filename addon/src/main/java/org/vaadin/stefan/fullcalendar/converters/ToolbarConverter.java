package org.vaadin.stefan.fullcalendar.converters;

import org.vaadin.stefan.fullcalendar.model.AbstractHeaderFooter;
import tools.jackson.databind.JsonNode;

/**
 * Converts a {@link AbstractHeaderFooter} (Header or Footer) to its JSON representation
 * for the FullCalendar client side.
 *
 * @deprecated goes together with the deprecated toolbar model ({@link org.vaadin.stefan.fullcalendar.model.Header},
 * {@link org.vaadin.stefan.fullcalendar.model.Footer}). Set the toolbar options with a {@code Map} instead.
 * @see org.vaadin.stefan.fullcalendar.Option#HEADER_TOOLBAR
 * @see org.vaadin.stefan.fullcalendar.Option#FOOTER_TOOLBAR
 */
@Deprecated(since = "8.0.0", forRemoval = true)
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
