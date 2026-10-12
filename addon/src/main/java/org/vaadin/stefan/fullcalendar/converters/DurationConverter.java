package org.vaadin.stefan.fullcalendar.converters;

import org.vaadin.stefan.fullcalendar.JsonUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.NullNode;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts a {@link Duration} or {@link LocalTime} to a FullCalendar duration string in {@code "HH:mm:ss"} format
 * (e.g. {@code "00:30:00"}, {@code "06:00:00"}).
 * <p>
 * FullCalendar options like {@code slotDuration}, {@code snapDuration}, {@code slotHeaderInterval},
 * {@code scrollTime}, {@code slotMinTime}, {@code slotMaxTime}, and {@code nextDayThreshold}
 * accept this format, and so does the recurring duration of an entry. Hours may exceed 24, for example
 * {@code "72:00:00"} for three days.
 * <p>
 * A negative duration gets one leading sign ({@code "-00:30:00"}). Milliseconds are appended when present
 * ({@code "00:00:01.500"}). Anything below a millisecond is dropped.
 * <p>
 * A string from the client is read back as {@link Duration} with the client's own duration format
 * ({@code [-][d.]hh:mm[:ss[.sss]]}). A string in another format, such as ISO 8601, gives {@code null}, as it does
 * on the client.
 */
public class DurationConverter implements JsonItemPropertyConverter<Object, Object> {

    private static final DateTimeFormatter LOCAL_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    /** The client's duration string format, matched from the start like the client does. */
    private static final Pattern CLIENT_FORMAT = Pattern.compile("(-?)(?:(\\d+)\\.)?(\\d+):(\\d\\d)(?::(\\d\\d)(?:\\.(\\d\\d\\d))?)?");

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
            // the client reads one leading sign for the whole value, so format the absolute value
            Duration abs = duration.abs();
            String formatted = String.format("%s%02d:%02d:%02d", duration.isNegative() ? "-" : "",
                    abs.toHours(), abs.toMinutesPart(), abs.toSecondsPart());
            if (abs.toMillisPart() > 0) {
                formatted += String.format(".%03d", abs.toMillisPart());
            }
            return JsonUtils.toJsonNode(formatted);
        }
        if (serverValue instanceof LocalTime time) {
            return JsonUtils.toJsonNode(time.format(LOCAL_TIME_FORMAT));
        }
        throw new IllegalArgumentException("Unsupported type: " + serverValue.getClass());
    }

    @Override
    public Object toServerModel(JsonNode clientValue, Object currentInstance) {
        if (clientValue == null || clientValue.isNull()) {
            return null;
        }

        Matcher m = CLIENT_FORMAT.matcher(clientValue.asString());
        if (!m.lookingAt()) {
            return null;
        }

        Duration duration = Duration.ofDays(m.group(2) != null ? Long.parseLong(m.group(2)) : 0)
                .plusHours(Long.parseLong(m.group(3)))
                .plusMinutes(Long.parseLong(m.group(4)))
                .plusSeconds(m.group(5) != null ? Long.parseLong(m.group(5)) : 0)
                .plusMillis(m.group(6) != null ? Long.parseLong(m.group(6)) : 0);
        return m.group(1).isEmpty() ? duration : duration.negated();
    }
}
