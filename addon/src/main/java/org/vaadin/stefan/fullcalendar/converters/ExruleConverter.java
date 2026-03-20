package org.vaadin.stefan.fullcalendar.converters;

import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.JsonFactory;
import org.vaadin.stefan.fullcalendar.RRule;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;

import java.util.Collection;

/**
 * Converts the {@code exrule} field of an {@link Entry} to the JSON format expected by
 * FullCalendar's RRule plugin. A single RRule is serialized as an object; multiple RRules
 * are serialized as an array.
 */
public class ExruleConverter<T extends Entry> implements JsonItemPropertyConverter<Collection<RRule>, T> {

    @Override
    public boolean supports(Object type) {
        return type == null || type instanceof Collection;
    }

    @Override
    public JsonNode toClientModel(Collection<RRule> serverValue, T currentInstance) {
        if (serverValue == null || serverValue.isEmpty()) {
            return null;
        }
        if (serverValue.size() == 1) {
            return serverValue.iterator().next().toJson();
        }
        ArrayNode array = JsonFactory.createArray();
        for (RRule rule : serverValue) {
            array.add(rule.toJson());
        }
        return array;
    }
}
