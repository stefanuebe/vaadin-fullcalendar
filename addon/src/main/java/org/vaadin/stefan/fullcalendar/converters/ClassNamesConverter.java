package org.vaadin.stefan.fullcalendar.converters;

import org.vaadin.stefan.fullcalendar.JsonUtils;
import tools.jackson.databind.JsonNode;

import java.util.Collection;

/**
 * Converts a {@code Collection<String>} of CSS class names to a single space-separated
 * JSON string value. FullCalendar v7 expects {@code className: string} (clsx-compatible)
 * rather than the v6 {@code classNames: string[]}.
 */
public class ClassNamesConverter implements JsonItemPropertyConverter<Object, Object> {

    @Override
    public boolean supports(Object type) {
        if (type instanceof Collection<?> c) {
            return c.stream().allMatch(e -> e instanceof String);
        }
        return false;
    }

    @Override
    public JsonNode toClientModel(Object serverValue, Object currentInstance) {
        @SuppressWarnings("unchecked")
        Collection<String> col = (Collection<String>) serverValue;
        return JsonUtils.toJsonNode(String.join(" ", col));
    }
}
