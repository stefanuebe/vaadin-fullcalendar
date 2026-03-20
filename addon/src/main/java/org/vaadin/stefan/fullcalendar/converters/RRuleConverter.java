package org.vaadin.stefan.fullcalendar.converters;

import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.RRule;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.NullNode;

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
    public JsonNode toClientModel(RRule serverValue, Entry currentInstance) {
        if (serverValue == null) {
            return NullNode.getInstance();
        }
        return serverValue.toJson();
    }
}
