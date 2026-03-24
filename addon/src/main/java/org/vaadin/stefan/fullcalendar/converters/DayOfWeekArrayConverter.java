package org.vaadin.stefan.fullcalendar.converters;

import elemental.json.JsonValue;
import org.vaadin.stefan.fullcalendar.JsonUtils;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Converts {@link DayOfWeek}{@code []} or {@link Collection}{@code <DayOfWeek>} to a JSON array
 * of FullCalendar day-of-week integers (Sunday=0, Monday=1, ..., Saturday=6).
 */
public class DayOfWeekArrayConverter<T> implements JsonItemPropertyConverter<Object, T> {

    @Override
    public boolean supports(Object type) {
        if (type instanceof DayOfWeek[]) return true;
        if (type instanceof Collection<?>) {
            return ((Collection<?>) type).stream().allMatch(e -> e instanceof DayOfWeek);
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public JsonValue toClientModel(Object serverValue, T currentInstance) {
        Stream<DayOfWeek> stream;
        if (serverValue instanceof DayOfWeek[]) {
            stream = Arrays.stream((DayOfWeek[]) serverValue);
        } else {
            stream = ((Collection<DayOfWeek>) serverValue).stream();
        }
        return JsonUtils.toJsonValue(stream
                .map(dow -> dow == DayOfWeek.SUNDAY ? 0 : dow.getValue())
                .collect(Collectors.toList()));
    }
}
