package org.vaadin.stefan.fullcalendar.converters;

import elemental.json.JsonNull;
import elemental.json.JsonString;
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
public class ClientDateConverter<T extends Entry> implements JsonItemPropertyConverter<LocalDate, T> {

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
        if (clientValue instanceof JsonNull) {
            return null;
        }

        if (clientValue instanceof JsonString) {
            return JsonUtils.parseClientSideDate(clientValue.asString());
        }

        throw new IllegalArgumentException(clientValue + " must either be of type JsonNull or JsonString, but was " + (clientValue != null ? clientValue.getClass() : null) + ": " + clientValue);
    }
}
