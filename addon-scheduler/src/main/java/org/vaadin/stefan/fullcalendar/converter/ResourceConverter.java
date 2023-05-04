package org.vaadin.stefan.fullcalendar.converter;

import elemental.json.JsonNull;
import elemental.json.JsonValue;
import org.vaadin.stefan.fullcalendar.*;
import org.vaadin.stefan.fullcalendar.converters.JsonItemPropertyConverter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Stefan Uebe
 */
public class ResourceConverter implements JsonItemPropertyConverter<Set<Resource>, ResourceEntry> {
    @Override
    public boolean supports(Object type) {
        return type instanceof Set && ((Set<?>) type).stream().allMatch(Resource.class::isInstance) ;
    }

    @Override
    public JsonValue toClientModel(Set<Resource> serverValue, ResourceEntry currentInstance) {
        List<String> ids = serverValue.stream()
                .map(Resource::getId)
                .collect(Collectors.toList());

        return JsonUtils.toJsonValue(ids);
    }

    @Override
    public Set<Resource> toServerModel(JsonValue clientValue, ResourceEntry currentInstance) {

        if (clientValue instanceof JsonNull) {
            return new LinkedHashSet<>();
        }

        FullCalendarScheduler calendar = (FullCalendarScheduler) currentInstance.getCalendar()
                .orElseThrow(() -> new IllegalStateException("Converting to server model requires an assigned scheduler instance"));

        Object value = JsonUtils.ofJsonValue(clientValue);
        if (value instanceof List) {
            return ((List<?>) value).stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .map(calendar::getResourceById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }

        if (value instanceof String || value instanceof Number) {
            Set<Resource> set = new LinkedHashSet<>();
            set.add(calendar.getResourceById(value.toString()).orElseThrow(() -> new NoSuchElementException("Id unknown: " + value)));
            return set;

        }

        throw new IllegalStateException("Value is not supported, as it is neither a string nor a list: " + value);
    }
}
