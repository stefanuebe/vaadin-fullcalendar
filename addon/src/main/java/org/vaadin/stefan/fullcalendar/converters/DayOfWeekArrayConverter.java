package org.vaadin.stefan.fullcalendar.converters;

import elemental.json.JsonValue;
import org.vaadin.stefan.fullcalendar.JsonUtils;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Converts {@link DayOfWeek}{@code []} or {@link Collection}{@code <DayOfWeek>} to a JSON array
 * of FullCalendar day-of-week integers (Sunday=0, Monday=1, ..., Saturday=6).
 */
public class DayOfWeekArrayConverter implements JsonItemPropertyConverter<Object, Object> {

    @Override
    public boolean supports(Object type) {
        if (type instanceof DayOfWeek[]) return true;
        if (type instanceof Collection) {
            Collection<?> c = (Collection<?>) type;
            return c.stream().allMatch(e -> e instanceof DayOfWeek);
        }
        return false;
    }

    @Override
    public JsonValue toClientModel(Object serverValue, Object currentInstance) {
        Stream<DayOfWeek> stream;
        if (serverValue instanceof DayOfWeek[]) {
            DayOfWeek[] arr = (DayOfWeek[]) serverValue;
            stream = Arrays.stream(arr);
        } else {
            @SuppressWarnings("unchecked")
            Collection<DayOfWeek> col = (Collection<DayOfWeek>) serverValue;
            stream = col.stream();
        }
        return JsonUtils.toJsonValue(stream
                .map(dow -> dow == DayOfWeek.SUNDAY ? 0 : dow.getValue())
                .collect(java.util.stream.Collectors.toList()));
    }
}
