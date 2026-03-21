package org.vaadin.stefan.fullcalendar.converters;

import org.vaadin.stefan.fullcalendar.ClientSideValue;
import org.vaadin.stefan.fullcalendar.JsonUtils;
import tools.jackson.databind.JsonNode;

/**
 * Converts any {@link ClientSideValue} to its client-side string representation.
 */
public class ClientSideValueConverter implements JsonItemPropertyConverter<ClientSideValue, Object> {

    @Override
    public boolean supports(Object type) {
        return type instanceof ClientSideValue;
    }

    @Override
    public JsonNode toClientModel(ClientSideValue serverValue, Object currentInstance) {
        return JsonUtils.toJsonNode(serverValue.getClientSideValue());
    }
}
