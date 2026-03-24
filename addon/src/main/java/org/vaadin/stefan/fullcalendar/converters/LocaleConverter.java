package org.vaadin.stefan.fullcalendar.converters;

import elemental.json.JsonValue;
import org.vaadin.stefan.fullcalendar.JsonUtils;

import java.util.Locale;

/**
 * Converts a {@link Locale} to a lowercase language tag string for the FullCalendar client side.
 */
public class LocaleConverter<T> implements JsonItemPropertyConverter<Locale, T> {

    @Override
    public boolean supports(Object type) {
        return type instanceof Locale;
    }

    @Override
    public JsonValue toClientModel(Locale serverValue, T currentInstance) {
        return JsonUtils.toJsonValue(serverValue.toLanguageTag().toLowerCase());
    }
}
