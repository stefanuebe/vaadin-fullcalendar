package org.vaadin.stefan.fullcalendar.converters;

import elemental.json.JsonNull;
import elemental.json.JsonString;
import elemental.json.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.JsonUtils;

import java.time.LocalTime;

/**
 * @author Stefan Uebe
 */
@Getter
@RequiredArgsConstructor
public class LocalTimeConverter<T extends Entry> implements JsonItemPropertyConverter<LocalTime, T> {

    @Override
    public boolean supports(Object type) {
        return type == null || type instanceof LocalTime;
    }


    @Override
    public JsonValue toClientModel(LocalTime serverValue, T currentInstance) {
        return JsonUtils.toJsonValue(JsonUtils.formatClientSideTimeString(serverValue));
    }

    @Override
    public LocalTime toServerModel(JsonValue clientValue, T currentInstance) {
        if (clientValue instanceof JsonNull) {
            return null;
        }

        if (clientValue instanceof JsonString) {
            return JsonUtils.parseClientSideTime(clientValue.asString());
        }

        throw new IllegalArgumentException(clientValue + " must either be of type JsonNull or JsonString, but was " + (clientValue != null ? clientValue.getClass() : null) + ": " + clientValue);
    }
}
