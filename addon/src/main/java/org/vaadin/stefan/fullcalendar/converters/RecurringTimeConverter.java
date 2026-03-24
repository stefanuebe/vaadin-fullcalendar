package org.vaadin.stefan.fullcalendar.converters;

import elemental.json.JsonType;
import elemental.json.JsonValue;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.JsonFactory;
import org.vaadin.stefan.fullcalendar.RecurringTime;

/**
 * @author Stefan Uebe
 */
public class RecurringTimeConverter<T extends Entry> implements JsonItemPropertyConverter<RecurringTime, T> {

    @Override
    public boolean supports(Object type) {
        return type == null || type instanceof RecurringTime;
    }

    @Override
    public JsonValue toClientModel(RecurringTime serverValue, T currentInstance) {
        // recurring time must not be sent, when all day
        return serverValue == null || currentInstance.isAllDay() ? null : JsonFactory.create(serverValue.toFormattedString());
    }

    @Override
    public RecurringTime toServerModel(JsonValue clientValue, T currentInstance) {
        if (clientValue.getType() == JsonType.NULL) {
            return null;
        }

        if (clientValue.getType() == JsonType.STRING) {
            String string = clientValue.asString();
            return RecurringTime.of(string);
        }

        throw new IllegalArgumentException(clientValue + " must either be of type NULL or STRING, but was " + (clientValue != null ? clientValue.getType() : null) + ": " + clientValue);
    }
}
