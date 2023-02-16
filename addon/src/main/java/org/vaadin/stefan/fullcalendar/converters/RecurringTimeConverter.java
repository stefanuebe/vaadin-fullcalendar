package org.vaadin.stefan.fullcalendar.converters;

import elemental.json.Json;
import elemental.json.JsonNull;
import elemental.json.JsonString;
import elemental.json.JsonValue;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.RecurringTime;

import java.time.LocalDate;

/**
 * @author Stefan Uebe
 */
public class RecurringTimeConverter<T extends Entry> implements JsonItemPropertyConverter<RecurringTime, T> {

    @Override
    public boolean supports(Object type) {
        return type == null || type instanceof RecurringTime;
    }

    @Override
    public JsonValue toJsonValue(RecurringTime serverValue, T currentInstance) {
        // recurring time must not be sent, when all day
        return serverValue == null || currentInstance.isAllDay() ? null : Json.create(serverValue.toFormattedString());
    }

    @Override
    public RecurringTime ofJsonValue(JsonValue clientValue, T currentInstance) {
        if (clientValue instanceof JsonNull) {
            return null;
        }

        if (clientValue instanceof JsonString) {
            String string = clientValue.asString();
            return new RecurringTime(string);
        }

        throw new IllegalArgumentException(clientValue + " must either be of type JsonNull or JsonString, but was " + (clientValue != null ? clientValue.getClass() : null) + ": " + clientValue);
    }
}
