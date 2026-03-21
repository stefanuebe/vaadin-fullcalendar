package org.vaadin.stefan.fullcalendar.converters;

import org.vaadin.stefan.fullcalendar.JsonUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.NullNode;

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
    public JsonNode toClientModel(Object serverValue, Object currentInstance) {
        if (serverValue == null) {
            return NullNode.instance;
        }
        if (serverValue instanceof Duration duration) {
            return JsonUtils.toJsonNode(
                    String.format("%02d:%02d:%02d", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart())
            );
        }
        if (serverValue instanceof LocalTime time) {
            return JsonUtils.toJsonNode(time.format(LOCAL_TIME_FORMAT));
        }
        throw new IllegalArgumentException("Unsupported type: " + serverValue.getClass());
    }
}
