package org.vaadin.stefan.fullcalendar.converters;

import org.vaadin.stefan.fullcalendar.JsonFactory;
import org.vaadin.stefan.fullcalendar.JsonUtils;
import tools.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Converts the extended props of an entry with {@link JsonUtils#toJsonNodeWithJackson(Object)}. An empty map is
 * not sent at all.
 */
public class ExtendedPropsConverter implements JsonItemPropertyConverter<Map<String, Object>, Object> {

    @Override
    public boolean supports(Object type) {
        return type instanceof Map;
    }

    @Override
    public JsonNode toClientModel(Map<String, Object> serverValue, Object currentInstance) {
        if (serverValue.isEmpty()) {
            return JsonFactory.createNull();
        }

        return JsonUtils.toJsonNodeWithJackson(serverValue);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> toServerModel(JsonNode clientValue, Object currentInstance) {
        Object value = JsonUtils.ofJsonNode(clientValue);

        return value instanceof Map ? new HashMap<>((Map<String, Object>) value) : new HashMap<>();
    }
}
