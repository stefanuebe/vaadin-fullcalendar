package org.vaadin.stefan.fullcalendar.converters;

import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.RRule;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Converts an {@link RRule} instance to its JSON representation for the FC client.
 * Supports both structured (JSON object) and raw RRULE string forms.
 * <p>
 * If the structured RRule has no {@code dtstart} but the containing entry has a {@code start},
 * the entry's start is injected as {@code dtstart}. Without an anchor the rrule-js library
 * generates zero occurrences, so the entry would silently fail to render.
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
        JsonNode json = serverValue.toJson();
        if (json.isObject() && !json.has("dtstart") && currentInstance != null) {
            String dtstart = entryStartAsDtstart(currentInstance);
            if (dtstart != null) {
                // Copy before mutating: RRule.toJson() currently builds a fresh ObjectNode on
                // every call, but this converter must not depend on that — caching or reuse in
                // toJson() would otherwise silently leak injected dtstart back into the RRule.
                ObjectNode copy = ((ObjectNode) json).deepCopy();
                copy.put("dtstart", dtstart);
                return copy;
            }
        }
        return json;
    }

    /**
     * Formats the entry's start as a dtstart value for rrule-js. All-day entries emit a
     * date-only value so FC's parser treats the recurrence as all-day; timed entries emit
     * the full local datetime.
     */
    private static String entryStartAsDtstart(Entry entry) {
        LocalDateTime start = entry.getStart();
        if (start == null) {
            return null;
        }
        if (entry.isAllDay()) {
            return start.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        return start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
