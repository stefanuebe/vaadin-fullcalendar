package org.vaadin.stefan.fullcalendar;

import elemental.json.JsonValue;

/**
 * Converts a server side value to a json value and (optionally) vice versa. Also provides info
 * about the current parsed json item, if available.
 *
 * @param <SERVER_TYPE>
 */
public interface JsonItemPropertyConverter<SERVER_TYPE, T extends JsonItem> {
    JsonValue toJsonValue(SERVER_TYPE serverValue, T currentInstance);

    default SERVER_TYPE ofJsonValue(JsonValue clientValue, T currentInstance) {
        throw new UnsupportedOperationException("Conversion from client to server not implemented or supported");
    }
}
