package org.vaadin.stefan.fullcalendar.converters;

import org.vaadin.stefan.fullcalendar.JsonFactory;
import org.vaadin.stefan.fullcalendar.JsonUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Converts the extended props of an entry with {@link JsonUtils#toJsonNodeWithJackson(Object)}. An empty map is
 * not sent at all. The custom properties (the extended prop {@code customProperties}) are converted with
 * {@link JsonUtils#toJsonNode(Object)}, as before they became an extended prop.
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

        ObjectNode json = JsonFactory.createObject();
        serverValue.forEach((key, value) -> json.set(key, "customProperties".equals(key)
                ? JsonUtils.toJsonNode(value)
                : JsonUtils.toJsonNodeWithJackson(value)));
        return json;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> toServerModel(JsonNode clientValue, Object currentInstance) {
        Object value = JsonUtils.ofJsonNode(clientValue);

        return value instanceof Map ? new HashMap<>((Map<String, Object>) value) : new HashMap<>();
    }
}
