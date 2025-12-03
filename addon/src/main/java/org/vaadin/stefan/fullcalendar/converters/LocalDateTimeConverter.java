package org.vaadin.stefan.fullcalendar.converters;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.JsonUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.StringNode;

import java.time.LocalDateTime;

/**
 * @author Stefan Uebe
 */
@Getter
@RequiredArgsConstructor
public class LocalDateTimeConverter<T extends Entry> implements JsonItemPropertyConverter<LocalDateTime, T> {

    @Override
    public boolean supports(Object type) {
        return type == null || type instanceof LocalDateTime;
    }

    @Override
    public JsonNode toClientModel(LocalDateTime serverValue, T currentInstance) {
        return JsonUtils.toJsonNode(JsonUtils.formatClientSideDateTimeString(serverValue));
    }

    @Override
    public LocalDateTime toServerModel(JsonNode clientValue, T currentInstance) {
        if (clientValue instanceof NullNode) {
            return null;
        }

        if (clientValue instanceof StringNode) {
            return JsonUtils.parseClientSideDateTime(clientValue.asString());
        }

        throw new IllegalArgumentException(clientValue + " must either be of type NullNode or StringNode, but was " + (clientValue != null ? clientValue.getClass() : null) + ": " + clientValue);
    }
}
