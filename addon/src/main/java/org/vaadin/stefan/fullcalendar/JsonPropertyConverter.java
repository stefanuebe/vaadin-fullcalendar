package org.vaadin.stefan.fullcalendar;


import tools.jackson.databind.JsonNode;

/**
 * Converts a server side value to a json value and (optionally) vice versa.
 *
 * @param <SERVER_TYPE>
 */
public interface JsonPropertyConverter<SERVER_TYPE> {
    JsonNode toJson(SERVER_TYPE serverValue);

    default SERVER_TYPE ofJson(JsonNode clientValue) {
        throw new UnsupportedOperationException("Conversion from client to server not implemented or supported");
    }
}
