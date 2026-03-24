package org.vaadin.stefan.fullcalendar.converters;

import elemental.json.JsonValue;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.JsonFactory;
import org.vaadin.stefan.fullcalendar.RRule;

/**
 * Converts an {@link RRule} instance to its JSON representation for the FC client.
 * Supports both structured (JSON object) and raw RRULE string forms.
 */
public class RRuleConverter implements JsonItemPropertyConverter<RRule, Entry> {

    @Override
    public boolean supports(Object type) {
        return type == null || type instanceof RRule;
    }

    @Override
    public JsonValue toClientModel(RRule serverValue, Entry currentInstance) {
        if (serverValue == null) {
            return JsonFactory.createNull();
        }
        return serverValue.toJson();
    }
}
