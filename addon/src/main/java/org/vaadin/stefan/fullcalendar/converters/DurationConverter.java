package org.vaadin.stefan.fullcalendar.converters;

import elemental.json.JsonValue;
import org.vaadin.stefan.fullcalendar.JsonFactory;
import org.vaadin.stefan.fullcalendar.JsonUtils;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Converts a {@link Duration} or {@link LocalTime} to a FullCalendar duration string in {@code "HH:mm:ss"} format
 * (e.g. {@code "00:30:00"}, {@code "06:00:00"}).
 * <p>
 * FullCalendar options like {@code slotDuration}, {@code snapDuration}, {@code slotLabelInterval},
 * {@code scrollTime}, {@code slotMinTime}, {@code slotMaxTime}, and {@code nextDayThreshold}
 * accept this format.
 */
public class DurationConverter implements JsonItemPropertyConverter<Object, Object> {

    private static final DateTimeFormatter LOCAL_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public boolean supports(Object value) {
        return value == null || value instanceof Duration || value instanceof LocalTime;
    }

    @Override
    public JsonValue toClientModel(Object serverValue, Object currentInstance) {
        if (serverValue == null) {
            return JsonFactory.createNull();
        }
        if (serverValue instanceof Duration) {
            Duration duration = (Duration) serverValue;
            return JsonUtils.toJsonValue(
                    String.format("%02d:%02d:%02d", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart())
            );
        }
        if (serverValue instanceof LocalTime) {
            LocalTime time = (LocalTime) serverValue;
            return JsonUtils.toJsonValue(time.format(LOCAL_TIME_FORMAT));
        }
        throw new IllegalArgumentException("Unsupported type: " + serverValue.getClass());
    }
}
