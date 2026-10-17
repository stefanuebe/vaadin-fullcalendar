/*
 * Copyright 2026, Stefan Uebe
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.vaadin.stefan.fullcalendar.converters;

import org.vaadin.stefan.fullcalendar.JsonFactory;
import org.vaadin.stefan.fullcalendar.JsonUtils;
import org.vaadin.stefan.fullcalendar.RRule;
import org.vaadin.stefan.fullcalendar.option.BusinessHours;
import org.vaadin.stefan.fullcalendar.option.ConstraintValue;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Converts a {@link ConstraintValue} to its client form and back: the group id, {@code "businessHours"}, or an array
 * of windows: business hours objects, objects with {@code start} and/or {@code end} for fixed spans, and objects with
 * {@code rrule}, {@code duration}, {@code exdate} and {@code exrule} as an entry has them. Resource ids go into each
 * window as {@code resourceIds}, or alone into one object for resources at any time. A single window object, which
 * the library takes as well, is read as one window.
 */
public class ConstraintValueConverter implements JsonItemPropertyConverter<ConstraintValue, Object> {

    private static final String BUSINESS_HOURS = "businessHours";
    private static final String START = "start";
    private static final String END = "end";
    private static final String RRULE = "rrule";
    private static final String DURATION = "duration";
    private static final String EXDATE = "exdate";
    private static final String EXRULE = "exrule";
    private static final String RESOURCE_IDS = "resourceIds";
    private static final Set<String> RRULE_KEYS = Set.of(RRULE, DURATION, EXDATE, EXRULE);
    private static final DurationConverter DURATION_CONVERTER = new DurationConverter();
    private static final int DATE_LENGTH = "2026-01-01".length();

    @Override
    public boolean supports(Object type) {
        return type instanceof ConstraintValue;
    }

    @Override
    public JsonNode toClientModel(ConstraintValue serverValue, Object currentInstance) {
        return switch (serverValue) {
            case ConstraintValue.GroupId groupId -> JsonUtils.toJsonNode(groupId.groupId());
            case ConstraintValue.CalendarBusinessHours ignored -> JsonUtils.toJsonNode(BUSINESS_HOURS);
            case ConstraintValue.Windows windows -> {
                ArrayNode array = JsonFactory.createArray();
                windows.windows().forEach(window -> array.add(withResourceIds(toJson(window), windows.resourceIds())));
                yield array;
            }
            case ConstraintValue.Resources resources ->
                    withResourceIds(JsonFactory.createObject(), resources.resourceIds());
        };
    }

    @Override
    public ConstraintValue toServerModel(JsonNode clientValue, Object currentInstance) {
        if (clientValue.isObject() && clientValue.size() == 1 && clientValue.has(RESOURCE_IDS)) {
            return new ConstraintValue.Resources(resourceIds(clientValue));
        }
        if (clientValue.isObject() || clientValue.isArray()) {
            List<JsonNode> windows = clientValue.isObject() ? List.of(clientValue) : clientValue.valueStream().toList();
            List<List<String>> resourceIds = windows.stream().map(ConstraintValueConverter::resourceIds).distinct()
                    .toList();
            if (resourceIds.size() > 1) {
                throw new IllegalArgumentException("Windows with different resource ids: " + clientValue);
            }

            return new ConstraintValue.Windows(windows.stream().map(ConstraintValueConverter::withoutResourceIds)
                    .map(ConstraintValueConverter::toWindow).toList(),
                    resourceIds.isEmpty() ? List.of() : resourceIds.getFirst());
        }
        if (!clientValue.isString()) {
            throw new IllegalArgumentException("Not a constraint value: " + clientValue);
        }
        String text = clientValue.asString();
        return text.equals(BUSINESS_HOURS) ? ConstraintValue.BUSINESS_HOURS : ConstraintValue.ofGroupId(text);
    }

    private static JsonNode toJson(ConstraintValue.Window window) {
        return switch (window) {
            case BusinessHours hours -> hours.toJson();
            case ConstraintValue.RRuleWindow rule -> rruleWindow(rule);
            case ConstraintValue.DateSpan span -> span(JsonUtils.formatClientSideDateString(span.start()),
                    JsonUtils.formatClientSideDateString(span.end()));
            case ConstraintValue.DateTimeSpan span -> span(JsonUtils.formatClientSideDateTimeString(span.start()),
                    JsonUtils.formatClientSideDateTimeString(span.end()));
        };
    }

    private static JsonNode withResourceIds(JsonNode window, List<String> resourceIds) {
        if (resourceIds.isEmpty()) {
            return window;
        }

        return ((ObjectNode) window.deepCopy()).set(RESOURCE_IDS, JsonUtils.toJsonNode(resourceIds));
    }

    /** The resource ids of a window, empty when it has none. */
    private static List<String> resourceIds(JsonNode window) {
        JsonNode ids = window.path(RESOURCE_IDS);
        if (ids.isMissingNode()) {
            return List.of();
        }

        if (!ids.isArray() || !ids.valueStream().allMatch(JsonNode::isString)) {
            throw new IllegalArgumentException("Not a list of resource ids: " + ids);
        }

        return ids.valueStream().map(JsonNode::asString).toList();
    }

    private static JsonNode withoutResourceIds(JsonNode window) {
        if (!window.has(RESOURCE_IDS)) {
            return window;
        }

        ObjectNode copy = (ObjectNode) window.deepCopy();
        copy.remove(RESOURCE_IDS);

        return copy;
    }

    private static ObjectNode rruleWindow(ConstraintValue.RRuleWindow window) {
        RRule rrule = window.rrule();
        ObjectNode json = JsonFactory.createObject();
        json.set(RRULE, rrule.toJson());
        if (window.duration() != null) {
            json.set(DURATION, DURATION_CONVERTER.toClientModel(window.duration(), null));
        }
        // the same JSON as an entry writes for these keys
        JsonNode exdate = new ExdateConverter<>().toClientModel(rrule.getExcludedDates(), null);
        if (exdate != null) {
            json.set(EXDATE, exdate);
        }
        JsonNode exrule = new ExruleConverter<>().toClientModel(rrule.getExcludedRules(), null);
        if (exrule != null) {
            json.set(EXRULE, exrule);
        }
        return json;
    }

    private static ObjectNode span(String start, String end) {
        ObjectNode json = JsonFactory.createObject();
        if (start != null) {
            json.put(START, start);
        }
        if (end != null) {
            json.put(END, end);
        }
        return json;
    }

    private static ConstraintValue.Window toRRuleWindow(JsonNode json) {
        if (!RRULE_KEYS.containsAll(json.propertyNames())) {
            throw new IllegalArgumentException("Not a window with a rule: " + json);
        }
        Duration duration = null;
        if (json.has(DURATION)) {
            duration = (Duration) DURATION_CONVERTER.toServerModel(json.get(DURATION), null);
            if (duration == null) {
                throw new IllegalArgumentException("Not a duration: " + json.get(DURATION));
            }
        }
        return ConstraintValue.Window.ofRRule(RRule.fromJson(json.get(RRULE), json.get(EXDATE), json.get(EXRULE)),
                duration);
    }

    /**
     * Reads a window: an object with an rrule is a rule window, one with only a start and an end a fixed span, any
     * other one business hours.
     */
    private static ConstraintValue.Window toWindow(JsonNode json) {
        if (json.isObject() && json.has(RRULE)) {
            return toRRuleWindow(json);
        }
        boolean span = json.isObject() && !json.isEmpty() && Set.of(START, END).containsAll(json.propertyNames());
        if (!span) {
            return BusinessHoursValueConverter.toBusinessHours(json);
        }
        String start = json.path(START).asString(null);
        String end = json.path(END).asString(null);
        try {
            if ((start == null || start.length() == DATE_LENGTH) && (end == null || end.length() == DATE_LENGTH)) {
                return ConstraintValue.Window.span(start == null ? null : LocalDate.parse(start),
                        end == null ? null : LocalDate.parse(end));
            }
            return ConstraintValue.Window.span(start == null ? null : JsonUtils.parseClientSideDateTime(start),
                    end == null ? null : JsonUtils.parseClientSideDateTime(end));
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("Not a span: " + json, e);
        }
    }
}
