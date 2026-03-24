package org.vaadin.stefan.fullcalendar.converters;

import elemental.json.JsonType;
import elemental.json.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.JsonUtils;

import java.time.LocalDate;

/**
 * @author Stefan Uebe
 */
@Getter
@RequiredArgsConstructor
public class LocalDateConverter<T extends Entry> implements JsonItemPropertyConverter<LocalDate, T> {

    @Override
    public boolean supports(Object type) {
        return type == null || type instanceof LocalDate;
    }

    @Override
    public JsonValue toClientModel(LocalDate serverValue, T currentInstance) {
        return JsonUtils.toJsonValue(JsonUtils.formatClientSideDateString(serverValue));
    }

    @Override
    public LocalDate toServerModel(JsonValue clientValue, T currentInstance) {
        if (clientValue.getType() == JsonType.NULL) {
            return null;
        }

        if (clientValue.getType() == JsonType.STRING) {
            return JsonUtils.parseClientSideDate(clientValue.asString());
        }

        throw new IllegalArgumentException(clientValue + " must either be of type NULL or STRING, but was " + (clientValue != null ? clientValue.getType() : null) + ": " + clientValue);
    }
}
