package org.vaadin.stefan.fullcalendar;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.text.NumberFormat;
import java.time.LocalTime;

import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;

/**
 * @author Stefan Uebe
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RecurrenceTime {

    private static final NumberFormat formatter = NumberFormat.getInstance();
    private final int hour;
    private final int minute;

    private static final RecurrenceTime[] HOURS_24 = new RecurrenceTime[24];
    static {
        for (int i = 0; i < HOURS_24.length; i++) {
            HOURS_24[i] = new RecurrenceTime(i, 0);
        }
        formatter.setMinimumIntegerDigits(2);
        formatter.setMaximumFractionDigits(0);
    }


    public static RecurrenceTime of(int hour, int minute) {
        if (hour < 0) {
            throw new IllegalArgumentException("Hour must not be negative");
        }

        if (minute == 0 && hour < 24) {
            return HOURS_24[hour]; // reuse "normal" hours
        }

        MINUTE_OF_HOUR.checkValidValue(minute);

        return new RecurrenceTime(hour, minute);
    }

    public static RecurrenceTime of(int hour) {
        return of(hour, 0);
    }

    public RecurrenceTime plusHours(int hours) {
        if (hours == 0) {
            return this;
        }
        return of(hours, minute);
    }

    public RecurrenceTime minusHours(int hours) {
        return plusHours(-hours);
    }

    public RecurrenceTime plusMinutes(int minutes) {
        if (minutes == 0) {
            return this;
        }
        return of(hour, minutes);
    }

    public RecurrenceTime minusMinutes(int minutes) {
        return plusMinutes(-minutes);
    }

    /**
     * Outputs this time as a {@code String}, such as {@code 10:15}.
     * <p>
     * The output will be in an ISO-8601 like format: {@code HH:mm}. There is no day represenation. That means,
     * that a
     *
     * @return a string representation of this time, not null
     */
    @Override
    public String toString() {
        return (hour < 10 ? "0" : "") + hour + (minute < 10 ? ":0" : ":") + minute;
    }
}
