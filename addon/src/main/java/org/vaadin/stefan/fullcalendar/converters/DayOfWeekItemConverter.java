package org.vaadin.stefan.fullcalendar.converters;

import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.JsonFactory;
import org.vaadin.stefan.fullcalendar.JsonUtils;
import tools.jackson.databind.JsonNode;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Stefan Uebe
 */
public class DayOfWeekItemConverter<T extends Entry> implements JsonItemPropertyConverter<Set<DayOfWeek>, T> {
    @Override
    public boolean supports(Object type) {
        return type == null || (type instanceof Set && ((Set<?>) type).stream().allMatch(o -> o instanceof DayOfWeek));
    }

    @Override
    public JsonNode toClientModel(Set<DayOfWeek> serverValue, T currentInstance) {
        if (serverValue == null) {
            return JsonFactory.createNull();
        }

        return JsonUtils.toJsonNode(serverValue
                .stream()
                .map(dayOfWeek -> dayOfWeek == DayOfWeek.SUNDAY ? 0 : dayOfWeek.getValue())
                .collect(Collectors.toList()));
    }

    @Override
    public Set<DayOfWeek> toServerModel(JsonNode clientValue, T currentInstance) {
        Set<Number> daysOfWeek = JsonUtils.ofJsonNode(clientValue, HashSet.class);
        return daysOfWeek != null ? daysOfWeek.stream().map(n -> {
            int dayOfWeek = n.intValue();
            if (dayOfWeek == 0) {
                dayOfWeek = 7;
            }

            return DayOfWeek.of(dayOfWeek);
        }).collect(Collectors.toSet()) : null;
    }
}
