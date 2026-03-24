package org.vaadin.stefan.fullcalendar.converters;

import elemental.json.JsonValue;
import org.vaadin.stefan.fullcalendar.JsonUtils;

import java.util.Collection;

/**
 * Converts {@code String[]} or {@code Collection<String>} to a single comma-joined
 * JSON string value. Used for FullCalendar options that accept comma-separated
 * CSS selectors (e.g. {@code dragScrollEls}).
 */
public class StringArrayConverter implements JsonItemPropertyConverter<Object, Object> {

    @Override
    public boolean supports(Object type) {
        if (type instanceof String[]) return true;
        if (type instanceof Collection) {
            Collection<?> c = (Collection<?>) type;
            return c.stream().allMatch(e -> e instanceof String);
        }
        return false;
    }

    @Override
    public JsonValue toClientModel(Object serverValue, Object currentInstance) {
        String joined;
        if (serverValue instanceof String[]) {
            String[] arr = (String[]) serverValue;
            joined = String.join(",", arr);
        } else {
            @SuppressWarnings("unchecked")
            Collection<String> col = (Collection<String>) serverValue;
            joined = String.join(",", col);
        }
        return JsonUtils.toJsonValue(joined);
    }
}
