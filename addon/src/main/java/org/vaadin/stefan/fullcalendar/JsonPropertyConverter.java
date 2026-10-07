package org.vaadin.stefan.fullcalendar;


import tools.jackson.databind.JsonNode;

/**
 * Converts a server side value to a json value and (optionally) vice versa.
 *
 * @param <SERVER_TYPE>
 */
/**
 * Converts a server-side value to its JSON representation and back.
 *
 * @param <SERVER_TYPE> server-side type
 * @deprecated No method of the addon accepts this interface, and we see no use in keeping it. Use
 * {@link org.vaadin.stefan.fullcalendar.converters.JsonItemPropertyConverter} instead, which
 * {@link FullCalendar#setOption(String, Object, org.vaadin.stefan.fullcalendar.converters.JsonItemPropertyConverter...)}
 * accepts. If you need this interface, please open an issue at
 * <a href="https://github.com/stefanuebe/vaadin-fullcalendar/issues">https://github.com/stefanuebe/vaadin-fullcalendar/issues</a>.
 */
@Deprecated(since = "8.0.0", forRemoval = true)
public interface JsonPropertyConverter<SERVER_TYPE> {
    JsonNode toJson(SERVER_TYPE serverValue);

    default SERVER_TYPE ofJson(JsonNode clientValue) {
        throw new UnsupportedOperationException("Conversion from client to server not implemented or supported");
    }
}
