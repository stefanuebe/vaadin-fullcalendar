package org.vaadin.stefan.fullcalendar.converters;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonValue;
import org.vaadin.stefan.fullcalendar.RRule;

import java.util.List;

/**
 * Converts the {@code exrule} field of an entry to the JSON format expected by
 * FullCalendar's RRule plugin. A single RRule is serialized directly; multiple RRules
 * are serialized as an array.
 *
 * @param <T> the entry type
 */
public class ExruleConverter<T> implements JsonItemPropertyConverter<List<RRule>, T> {

    @Override
    public boolean supports(Object type) {
        return type == null || type instanceof List;
    }

    @Override
    public JsonValue toClientModel(List<RRule> serverValue, T currentInstance) {
        if (serverValue == null || serverValue.isEmpty()) {
            return null;
        }
        if (serverValue.size() == 1) {
            return serverValue.get(0).toJson();
        }
        JsonArray array = Json.createArray();
        int i = 0;
        for (RRule rule : serverValue) {
            array.set(i++, rule.toJson());
        }
        return array;
    }
}
