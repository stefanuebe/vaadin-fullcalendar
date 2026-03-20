package org.vaadin.stefan.fullcalendar.converters;

import org.vaadin.stefan.fullcalendar.JsonUtils;
import tools.jackson.databind.JsonNode;

import java.util.Arrays;
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
        if (type instanceof Collection<?> c) return c.stream().allMatch(e -> e instanceof String);
        return false;
    }

    @Override
    public JsonNode toClientModel(Object serverValue, Object currentInstance) {
        String joined;
        if (serverValue instanceof String[] arr) {
            joined = String.join(",", arr);
        } else {
            @SuppressWarnings("unchecked")
            Collection<String> col = (Collection<String>) serverValue;
            joined = String.join(",", col);
        }
        return JsonUtils.toJsonNode(joined);
    }
}
