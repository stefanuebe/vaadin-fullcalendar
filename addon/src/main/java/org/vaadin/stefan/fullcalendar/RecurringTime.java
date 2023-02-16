package org.vaadin.stefan.fullcalendar;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalTime;

/**
 * A simple time class, that allows times above 24 hours, since the FC allows recurring times to "bleed" into the
 * next day. Basically a simple variant of Duration with a specific purpose.
 */
@Getter
@EqualsAndHashCode
public class RecurringTime {
    private final int hours;
    private final int minutes;

    /**
     * New instance. Both parameters must not be negative. Hours has no limit (except for the technical one).
     * Passing 60 minutes or more will automatically increase the given hours value.
     *
     * @param hours   hours
     * @param minutes minutes
     */
    public RecurringTime(int hours, int minutes) {
        if (hours < 0) {
            throw new IllegalArgumentException("Hours must not be negative");
        }
        if (minutes < 0) {
            throw new IllegalArgumentException("Minutes must not be negative");
        }

        this.hours = hours + (minutes / 60);
        this.minutes = minutes % 60;
    }

    public RecurringTime(LocalTime time) {
        this(time.getHour(), time.getMinute());
    }

    public RecurringTime(String string) {
        String[] split = string.split(":");

        // add additional exception handling, if necessary
        hours = Integer.parseInt(split[0]);
        minutes = Integer.parseInt(split[1]);
    }

    /**
     * Converts this recurring time to a local time. Be careful, as LocalTime does not support times of 24h or
     * above and thus such an instance will lead to an exception.
     *
     * @return LocalTime instance
     * @throws DateTimeException if this instance represents a time of 24 hours or above.
     */
    public LocalTime toLocalTime() {
        return LocalTime.of(hours, minutes);
    }

    /**
     * Converts this instance to a {@link Duration} instance.
     *
     * @return duration instance
     */
    public Duration toDuration() {
        return Duration.ofHours(hours).plusMinutes(minutes);
    }

    /**
     * Returns this instance as a formatted string. The pattern is always "HH:mm".
     *
     * @return formatted string
     */
    public String toFormattedString() {
        return (hours < 10 ? "0" : "") + hours + ":" + (minutes < 10 ? "0" : "") + minutes;
    }

    /**
     * Returns a new instance with the given hours added on top. Negative values are allowd, but may lead
     * to an exception, if the resulting amount of hours is negative.
     *
     * @param hours hours
     * @return new instace with changed hours
     */
    public RecurringTime plusHours(int hours) {
        return new RecurringTime(this.hours + hours, minutes);
    }

    /**
     * Returns a new instance with the given minutes added on top. Negative values are allowd, but may lead
     * to an exception, if the resulting amount of minutes is negative. If the resulting amount of minutes
     * is 60 or greater, it will also increase the hours.
     *
     * @param minutes minutes
     * @return new instace with changed minutes
     */
    public RecurringTime plusMinutes(int minutes) {
        return new RecurringTime(hours, this.minutes + minutes);
    }
}
