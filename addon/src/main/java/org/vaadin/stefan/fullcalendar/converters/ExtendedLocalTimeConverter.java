package org.vaadin.stefan.fullcalendar.converters;

import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.JsonFactory;
import org.vaadin.stefan.fullcalendar.option.ExtendedLocalTime;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.StringNode;

/**
 * Converts the recurring start and end times of an Entry to "HH:mm" and back.
 *
 * @author Stefan Uebe
 */
public class ExtendedLocalTimeConverter<T extends Entry> implements JsonItemPropertyConverter<ExtendedLocalTime, T> {

    @Override
    public boolean supports(Object type) {
        return type == null || type instanceof ExtendedLocalTime;
    }

    @Override
    public JsonNode toClientModel(ExtendedLocalTime serverValue, T currentInstance) {
        // recurring time must not be sent, when all day
        return serverValue == null || currentInstance.isAllDay() ? null : JsonFactory.create(serverValue.toFormattedString());
    }

    @Override
    public ExtendedLocalTime toServerModel(JsonNode clientValue, T currentInstance) {
        if (clientValue instanceof NullNode) {
            return null;
        }

        if (clientValue instanceof StringNode) {
            String string = clientValue.asString();
            return ExtendedLocalTime.of(string);
        }

        throw new IllegalArgumentException(clientValue + " must either be of type NullNode or StringNode, but was "
                + (clientValue != null ? clientValue.getClass() : null) + ": " + clientValue);
    }
}
