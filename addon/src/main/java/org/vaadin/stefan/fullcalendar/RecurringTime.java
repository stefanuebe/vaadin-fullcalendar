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

    public static RecurringTime of(int hours, int minutes) {
        return new RecurringTime(hours, minutes);
    }

    public static RecurringTime of(LocalTime time) {
        return of(time.getHour(), time.getMinute());
    }

    public static RecurringTime of(String string) {
        String[] split = string.split(":");

        // add additional exception handling, if necessary
        return of(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
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
        return new RecurringTime(this.hour + hours, minute);
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
        return new RecurringTime(hour, this.minute + minutes);
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



}

