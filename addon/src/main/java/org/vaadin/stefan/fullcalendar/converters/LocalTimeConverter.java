package org.vaadin.stefan.fullcalendar.converters;

import elemental.json.JsonType;
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
        if (clientValue.getType() == JsonType.NULL) {
            return null;
        }

        if (clientValue.getType() == JsonType.STRING) {
            return JsonUtils.parseClientSideTime(clientValue.asString());
        }

        throw new IllegalArgumentException(clientValue + " must either be of type NULL or STRING, but was " + (clientValue != null ? clientValue.getType() : null) + ": " + clientValue);
    }
}
