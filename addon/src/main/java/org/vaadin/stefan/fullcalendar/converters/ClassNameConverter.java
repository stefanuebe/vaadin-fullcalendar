package org.vaadin.stefan.fullcalendar.converters;

import org.vaadin.stefan.fullcalendar.JsonUtils;
import tools.jackson.databind.JsonNode;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Converts a collection of CSS class names to the single space-separated string FullCalendar 7 expects
 * for {@code className}, and back to a set.
 */
public class ClassNameConverter implements JsonItemPropertyConverter<Collection<String>, Object> {

    @Override
    public boolean supports(Object type) {
        return type instanceof Collection<?> c && c.stream().allMatch(e -> e instanceof String);
    }

    @Override
    public JsonNode toClientModel(Collection<String> serverValue, Object currentInstance) {
        return JsonUtils.toJsonNode(String.join(" ", serverValue));
    }

    @Override
    public Set<String> toServerModel(JsonNode clientValue, Object currentInstance) {
        Set<String> classNames = new LinkedHashSet<>();
        if (clientValue != null && !clientValue.isNull()) {
            Arrays.stream(clientValue.asString().split("\\s+"))
                    .filter(s -> !s.isEmpty())
                    .forEach(classNames::add);
        }
        return classNames;
    }
}
