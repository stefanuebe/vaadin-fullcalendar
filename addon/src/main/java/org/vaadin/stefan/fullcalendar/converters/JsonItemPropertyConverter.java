package org.vaadin.stefan.fullcalendar.converters;


import tools.jackson.databind.JsonNode;

/**
 * Converts a server side value to a json value and (optionally) vice versa. Also provides info
 * about the current parsed json item, if available.
 *
 * @param <SERVER_TYPE>
 */
public interface JsonItemPropertyConverter<SERVER_TYPE, T> {

    /**
     * Checks, if the given object is supported.
     * @param type object
     * @return
     */
    boolean supports(Object type);

    JsonNode toClientModel(SERVER_TYPE serverValue, T currentInstance);

    default SERVER_TYPE toServerModel(JsonNode clientValue, T currentInstance) {
        throw new UnsupportedOperationException("Conversion from client to server not implemented or supported");
    }
}
