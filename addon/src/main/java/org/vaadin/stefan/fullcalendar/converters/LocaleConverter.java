package org.vaadin.stefan.fullcalendar.converters;

import org.vaadin.stefan.fullcalendar.JsonUtils;
import tools.jackson.databind.JsonNode;

import java.util.Locale;

/**
 * Converts a {@link Locale} to a lowercase language tag string for the FullCalendar client side.
 */
public class LocaleConverter implements JsonItemPropertyConverter<Locale, Object> {

    @Override
    public boolean supports(Object type) {
        return type instanceof Locale;
    }

    @Override
    public JsonNode toClientModel(Locale serverValue, Object currentInstance) {
        return JsonUtils.toJsonNode(serverValue.toLanguageTag().toLowerCase());
    }
}
