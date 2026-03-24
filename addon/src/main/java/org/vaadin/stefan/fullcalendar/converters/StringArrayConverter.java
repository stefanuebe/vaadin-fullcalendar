package org.vaadin.stefan.fullcalendar.converters;

import elemental.json.JsonValue;
import org.vaadin.stefan.fullcalendar.JsonUtils;

import java.util.Collection;

/**
 * Converts {@code String[]} or {@code Collection<String>} to a single comma-joined
 * JSON string value. Used for FullCalendar options that accept comma-separated
 * CSS selectors (e.g. {@code dragScrollEls}).
 */
public class StringArrayConverter<T> implements JsonItemPropertyConverter<Object, T> {

    @Override
    public boolean supports(Object type) {
        if (type instanceof String[]) return true;
        if (type instanceof Collection<?>) {
            return ((Collection<?>) type).stream().allMatch(e -> e instanceof String);
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public JsonValue toClientModel(Object serverValue, T currentInstance) {
        String joined;
        if (serverValue instanceof String[]) {
            joined = String.join(",", (String[]) serverValue);
        } else {
            joined = String.join(",", (Collection<String>) serverValue);
        }
        return JsonUtils.toJsonValue(joined);
    }
}
