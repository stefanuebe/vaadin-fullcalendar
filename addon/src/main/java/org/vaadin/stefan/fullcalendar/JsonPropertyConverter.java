package org.vaadin.stefan.fullcalendar;

import elemental.json.JsonValue;

/**
 * Converts a server side value to a json value and (optionally) vice versa.
 *
 * @param <SERVER_TYPE>
 */
public interface JsonPropertyConverter<SERVER_TYPE> {
    JsonValue toJsonValue(SERVER_TYPE serverValue);

    default SERVER_TYPE ofJsonValue(JsonValue clientValue) {
        throw new UnsupportedOperationException("Conversion from client to server not implemented or supported");
    }
}
