package org.vaadin.stefan.fullcalendar.converters;

import elemental.json.Json;
import elemental.json.JsonValue;
import org.vaadin.stefan.fullcalendar.RRule;

/**
 * Converts an {@link RRule} instance to its JSON representation for the FC client.
 * Supports both structured (JSON object) and raw RRULE string forms.
 *
 * @param <T> the entry type
 */
public class RRuleConverter<T> implements JsonItemPropertyConverter<RRule, T> {

    @Override
    public boolean supports(Object type) {
        return type == null || type instanceof RRule;
    }

    @Override
    public JsonValue toClientModel(RRule serverValue, T currentInstance) {
        if (serverValue == null) {
            return Json.createNull();
        }
        return serverValue.toJson();
    }
}
