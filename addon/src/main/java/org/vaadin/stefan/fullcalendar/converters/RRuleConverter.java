package org.vaadin.stefan.fullcalendar.converters;

import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.RRule;
import org.vaadin.stefan.fullcalendar.option.Timezone;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.NullNode;

/**
 * Converts an {@link RRule} instance to its JSON representation for the client.
 * Supports both structured (JSON object) and raw RRULE string forms.
 * <p>
 * If the rule has no {@code dtstart} but the containing entry has a {@code start}, the entry's start becomes the
 * {@code dtstart}. Without an anchor the rrule-js library generates zero occurrences, so the entry would silently
 * fail to render. A raw rule is sent as it is.
 */
public class RRuleConverter implements JsonItemPropertyConverter<RRule, Entry> {

    /**
     * Converts the rule for a calendar in the given time zone. The library reads a {@code dtstart} without zone in
     * the time zone of the calendar, so the UTC start of the entry is converted to that zone before it becomes the
     * {@code dtstart}. An all-day entry gives a date-only {@code dtstart}.
     *
     * @param rule     the rule, may be null
     * @param entry    the entry that holds the rule, may be null
     * @param timezone the time zone of the calendar
     * @return the JSON of the rule
     */
    public static JsonNode toClientModelInZone(RRule rule, Entry entry, Timezone timezone) {
        if (rule == null) {
            return NullNode.getInstance();
        }
        if (rule.getRawRRule() == null && rule.getDtstart() == null && entry != null && entry.getStart() != null) {
            // a copy, so the entry's rule keeps having no dtstart and follows later changes of the entry start
            RRule anchored = rule.copy();
            if (entry.isAllDay()) {
                anchored.dtstart(entry.getStart().toLocalDate());
            } else {
                anchored.dtstart(timezone.applyTimezoneOffset(entry.getStart()));
            }

            return anchored.toJson();
        }

        return rule.toJson();
    }

    @Override
    public boolean supports(Object type) {
        return type == null || type instanceof RRule;
    }

    /**
     * Converts the rule for a calendar in UTC. A calendar in another time zone uses
     * {@link #toClientModelInZone(RRule, Entry, Timezone)}.
     */
    @Override
    public JsonNode toClientModel(RRule serverValue, Entry currentInstance) {
        return toClientModelInZone(serverValue, currentInstance, Timezone.UTC);
    }
}
