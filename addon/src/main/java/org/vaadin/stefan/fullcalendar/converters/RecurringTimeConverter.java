package org.vaadin.stefan.fullcalendar.converters;

import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.JsonFactory;
import org.vaadin.stefan.fullcalendar.RecurringTime;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.StringNode;

/**
 * @author Stefan Uebe
 */
public class RecurringTimeConverter<T extends Entry> implements JsonItemPropertyConverter<RecurringTime, T> {

    @Override
    public boolean supports(Object type) {
        return type == null || type instanceof RecurringTime;
    }

    @Override
    public JsonNode toClientModel(RecurringTime serverValue, T currentInstance) {
        // recurring time must not be sent, when all day
        return serverValue == null || currentInstance.isAllDay() ? null : JsonFactory.create(serverValue.toFormattedString());
    }

    @Override
    public RecurringTime toServerModel(JsonNode clientValue, T currentInstance) {
        if (clientValue instanceof NullNode) {
            return null;
        }

        if (clientValue instanceof StringNode) {
            String string = clientValue.asString();
            return RecurringTime.of(string);
        }

        throw new IllegalArgumentException(clientValue + " must either be of type NullNode or StringNode, but was " + (clientValue != null ? clientValue.getClass() : null) + ": " + clientValue);
    }
}
