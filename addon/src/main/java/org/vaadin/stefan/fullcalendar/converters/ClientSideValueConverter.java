package org.vaadin.stefan.fullcalendar.converters;

import elemental.json.JsonValue;
import org.vaadin.stefan.fullcalendar.ClientSideValue;
import org.vaadin.stefan.fullcalendar.JsonUtils;

/**
 * Converts any {@link ClientSideValue} to its client-side string representation.
 */
public class ClientSideValueConverter implements JsonItemPropertyConverter<ClientSideValue, Object> {

    @Override
    public boolean supports(Object type) {
        return type instanceof ClientSideValue;
    }

    @Override
    public JsonValue toClientModel(ClientSideValue serverValue, Object currentInstance) {
        return JsonUtils.toJsonValue(serverValue.getClientSideValue());
    }
}
