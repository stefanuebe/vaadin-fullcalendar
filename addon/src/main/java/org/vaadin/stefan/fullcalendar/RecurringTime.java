package org.vaadin.stefan.fullcalendar;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;

/**
 * A simple time class, that allows times above 24 hours, since the FC allows recurring times to "bleed" into the
 * next day. Basically a simple variant of Duration with a specific purpose.
 */
@Getter
@EqualsAndHashCode
public final class RecurringTime {
    private final int hour;
    private final int minute;

    /**
     * New instance. Both parameters must not be negative. Hours has no limit (except for the technical one).
     * Passing 60 minutes or more will automatically increase the given hours value.
     *
     * @param hour   hours
     * @param minute minutes
     */
    private RecurringTime(int hour, int minute) {
        if (hour < 0) {
            throw new IllegalArgumentException("Hours must not be negative");
        }
        if (minute < 0) {
            throw new IllegalArgumentException("Minutes must not be negative");
        }

        this.hour = hour + (minute / 60);
        this.minute = minute % 60;
    }

    /**
     * Creates a new instance based on the given integers. Both parameters must not be negative.
     * Hours has no limit (except for Integer.MAX). Passing 60 minutes or more will automatically increase the given
     * hours value.
     * @param hours hours
     * @param minutes minutes (values 60+ will be merged into hours)
     * @return new instance
     * @throws IllegalArgumentException when negative values are passed
     */
    public static RecurringTime of(int hours, int minutes) {
        return new RecurringTime(hours, minutes);
    }


    /**
     * Creates a new instance based on the given integer. The parameter must not be negative.
     * Hours has no limit (except for Integer.MAX).
     * @param hours hours
     * @return new instance
     * @throws IllegalArgumentException when negative values are passed
     */
    public static RecurringTime of(int hours) {
        return new RecurringTime(hours, 0);
    }

    /**
     * Creates an instance based on the given local time.
     * @param time local time
     * @return new instance
     */
    public static RecurringTime of(LocalTime time) {
        return of(time.getHour(), time.getMinute());
    }

    /**
     * Creates a new instance of the given string. The string must follow the same rules as the integer based constructor.
     * Expects hours and minutes in "HH:mm" format. Minutes can be left out (e.g. "1" for 1 hour), leading zeros are also
     * optional. Passing 60 minutes or more will automatically increase the given hours value.
     * @param string hours:minutes string
     * @return new instance
     * @throws NumberFormatException when the string cannot be parsed to integer(s)
     * @throws IllegalArgumentException when a blank string or negative values are passed
     */
    public static RecurringTime of(String string) {
        if (string.trim().isEmpty()) {
            throw new IllegalArgumentException("String must not be blank");
        }

        String[] split = string.split(":");

        // add additional exception handling, if necessary
        int hours = Integer.parseInt(split[0].trim());
        if (split.length == 1) {
            return of(hours);
        }

        return of(hours, Integer.parseInt(split[1].trim()));
    }

    /**
     * Converts this recurring time to a local time. Be careful, as LocalTime does not support times of 24h or
     * above and thus such an instance will lead to an exception.
     *
     * @return LocalTime instance
     * @throws DateTimeException if this instance represents a time of 24 hours or above.
     */
    public LocalTime toLocalTime() {
        return LocalTime.of(hour, minute);
    }

    /**
     * Converts this instance to a {@link Duration} instance.
     *
     * @return duration instance
     */
    public Duration toDuration() {
        return Duration.ofHours(hour).plusMinutes(minute);
    }

    /**
     * Returns this instance as a formatted string. The pattern is always "HH:mm".
     *
     * @return formatted string
     */
    public String toFormattedString() {
        return (hour < 10 ? "0" : "") + hour + ":" + (minute < 10 ? "0" : "") + minute;
    }

    /**
     * Returns a new instance with the given hours added on top. Negative values are allowd, but may lead
     * to an exception, if the resulting amount of hours is negative.
     *
     * @param hours hours
     * @return new instace with changed hours
     */
    public RecurringTime plusHours(int hours) {
        int totalMinutes = this.toTotalMinutes();
        totalMinutes += hours * 60;
        int[] times = fromTotalMinutes(totalMinutes);

        return new RecurringTime(times[0], times[1]);
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
        int totalMinutes = this.toTotalMinutes();
        totalMinutes += minutes;
        int[] times = fromTotalMinutes(totalMinutes);

        return new RecurringTime(times[0], times[1]);
    }

    public boolean isValidLocalTime() {
        return hour < 24;
    }

    public boolean equals(LocalTime localTime) {
        return equals(RecurringTime.of(localTime));
    }

    public boolean isAfter(RecurringTime recurringTime) {
        return toTotalMinutes() > recurringTime.toTotalMinutes();
    }

    public boolean isBefore(RecurringTime recurringTime) {
        return toTotalMinutes() < recurringTime.toTotalMinutes();
    }

    public boolean isAfter(LocalTime recurringTime) {
        return isAfter(RecurringTime.of(recurringTime));
    }

    public boolean isBefore(LocalTime recurringTime) {
        return isBefore(RecurringTime.of(recurringTime));
    }

    private int toTotalMinutes() {
        return hour * 60 + minute;
    }

    private int[] fromTotalMinutes(int totalMinutes) {
        return new int[] {totalMinutes / 60, totalMinutes % 60};
    }

}

