package org.vaadin.stefan.fullcalendar.prototype;

import org.vaadin.stefan.fullcalendar.ClassName;
import org.vaadin.stefan.fullcalendar.ClassNameValue;
import org.vaadin.stefan.fullcalendar.JsCallback;
import org.vaadin.stefan.fullcalendar.JsonUtils;
import org.vaadin.stefan.fullcalendar.converters.JsonItemPropertyConverter;
import tools.jackson.databind.JsonNode;

/**
 * PROTOTYPE (option-fields-design.md), throwaway. Converts a {@link ClassNameValue} for the client.
 */
public class ClassNameValueConverter implements JsonItemPropertyConverter<ClassNameValue, Object> {

    @Override
    public boolean supports(Object type) {
        return type instanceof ClassNameValue;
    }

    @Override
    public JsonNode toClientModel(ClassNameValue serverValue, Object currentInstance) {
        return switch (serverValue) {
            case ClassName c -> JsonUtils.toJsonNode(c.names());
            case JsCallback j -> j.toMarkerJson();
        };
    }
}
