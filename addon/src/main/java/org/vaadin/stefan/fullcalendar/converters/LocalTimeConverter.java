package org.vaadin.stefan.fullcalendar.converters;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.JsonUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.StringNode;

import java.time.LocalTime;

/**
 * @author Stefan Uebe
 */
@Getter
@RequiredArgsConstructor
public class LocalTimeConverter<T extends Entry> implements JsonItemPropertyConverter<LocalTime, T> {

    @Override
    public boolean supports(Object type) {
        return type == null || type instanceof LocalTime;
    }


    @Override
    public JsonNode toClientModel(LocalTime serverValue, T currentInstance) {
        return JsonUtils.toJsonNode(JsonUtils.formatClientSideTimeString(serverValue));
    }

    @Override
    public LocalTime toServerModel(JsonNode clientValue, T currentInstance) {
        if (clientValue instanceof NullNode) {
            return null;
        }

        if (clientValue instanceof StringNode) {
            return JsonUtils.parseClientSideTime(clientValue.asString());
        }

        throw new IllegalArgumentException(clientValue + " must either be of type NullNode or StringNode, but was " + (clientValue != null ? clientValue.getClass() : null) + ": " + clientValue);
    }
}
