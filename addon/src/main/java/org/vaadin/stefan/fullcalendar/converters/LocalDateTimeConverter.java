package org.vaadin.stefan.fullcalendar.converters;

import elemental.json.JsonType;
import elemental.json.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.JsonUtils;

import java.time.LocalDateTime;

/**
 * @author Stefan Uebe
 */
@Getter
@RequiredArgsConstructor
public class LocalDateTimeConverter<T extends Entry> implements JsonItemPropertyConverter<LocalDateTime, T> {

    @Override
    public boolean supports(Object type) {
        return type == null || type instanceof LocalDateTime;
    }

    @Override
    public JsonValue toClientModel(LocalDateTime serverValue, T currentInstance) {
        return JsonUtils.toJsonValue(JsonUtils.formatClientSideDateTimeString(serverValue));
    }

    @Override
    public LocalDateTime toServerModel(JsonValue clientValue, T currentInstance) {
        if (clientValue.getType() == JsonType.NULL) {
            return null;
        }

        if (clientValue.getType() == JsonType.STRING) {
            return JsonUtils.parseClientSideDateTime(clientValue.asString());
        }

        throw new IllegalArgumentException(clientValue + " must either be of type NULL or STRING, but was " + (clientValue != null ? clientValue.getType() : null) + ": " + clientValue);
    }
}
