package org.vaadin.stefan.fullcalendar.converters;

import elemental.json.JsonValue;

/**
 * Converts a server side value to a json value and (optionally) vice versa. Also provides info
 * about the current parsed json item, if available.
 *
 * @param <SERVER_TYPE>
 */
public interface JsonItemPropertyConverter<SERVER_TYPE, T> {

    boolean supports(Object type);

    JsonValue toClientModel(SERVER_TYPE serverValue, T currentInstance);

    default SERVER_TYPE toServerModel(JsonValue clientValue, T currentInstance) {
        throw new UnsupportedOperationException("Conversion from client to server not implemented or supported");
    }
}
