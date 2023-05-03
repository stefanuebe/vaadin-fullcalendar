package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.vaadin.stefan.fullcalendar.RecurringTime.of;

/**
 * @author Stefan Uebe
 */
public class RecurringTimeTest {

    @Test
    public void test_creation() {
        assertTime(of("00"), 0, 0, true);
        assertTime(of("0"), 0, 0, true);
        assertTime(of("1"), 1, 0, true);
        assertTime(of("1:0"), 1, 0, true);
        assertTime(of("00:00"), 0, 0, true);
        assertTime(of("23:59"), 23, 59, true);
        assertTime(of("24:00"), 24, 0, false);
        assertTime(of("100:30"), 100, 30, false);

        // also with spaces
        assertTime(of(" 1"), 1, 0, true);
        assertTime(of(" 1 : 0 "), 1, 0, true);

        assertTime(of(LocalTime.MIN), 0, 0, true);
        assertTime(of(LocalTime.MAX), 23, 59, true);

        assertTime(of(0, 0), 0, 0, true);
        assertTime(of(23, 59), 23, 59, true);
        assertTime(of(24, 0), 24, 0);
        assertTime(of(100, 30), 100, 30, false);

        assertTime(of(0), 0, 0, true);
        assertTime(of(23), 23, 0, true);
        assertTime(of(24), 24, 0, false);
        assertTime(of(100), 100, 0, false);

        // test invalid values
        assertThrows(NumberFormatException.class, () -> of("xyz"));
        assertThrows(NumberFormatException.class, () -> of("xyz:0"));
        assertThrows(NumberFormatException.class, () -> of("0:xyz"));
        assertThrows(IllegalArgumentException.class, () -> of("-1"));
        assertThrows(IllegalArgumentException.class, () -> of("-1:0"));
        assertThrows(IllegalArgumentException.class, () -> of("-01:00"));
        assertThrows(IllegalArgumentException.class, () -> of("0:-1"));
        assertThrows(IllegalArgumentException.class, () -> of("00:-01"));

        assertThrows(IllegalArgumentException.class, () -> of(-1));
        assertThrows(IllegalArgumentException.class, () -> of(-1,0));
        assertThrows(IllegalArgumentException.class, () -> of(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> of(0,-1));
        assertThrows(IllegalArgumentException.class, () -> of(0,-1));
    }

    @Test
    public void test_overtime() {
        assertTime(of(0, 60), 1, 0);
        assertTime(of(0, 120), 2, 0);
        assertTime(of(1, 60), 2, 0);


        assertTime(of("0: 60"), 1, 0);
        assertTime(of("0: 120"), 2, 0);
        assertTime(of("1: 60"), 2, 0);

        assertThrows(IllegalArgumentException.class, () -> of(1, -60));
        assertThrows(IllegalArgumentException.class, () -> of("1: -60"));
    }

    @Test
    public void test_plus_minus() {
        RecurringTime zeroHourTime = of(0, 0);
        assertTime(zeroHourTime.plusHours(1), 1, 0);
        assertTime(zeroHourTime.plusMinutes(1), 0, 1);
        assertTime(zeroHourTime.plusMinutes(60), 1, 0);
        assertTime(zeroHourTime.plusMinutes(61), 1, 1);
        assertTime(zeroHourTime.plusMinutes(120), 2, 0);

        assertThrows(IllegalArgumentException.class, () -> zeroHourTime.plusMinutes(-1));
        assertThrows(IllegalArgumentException.class, () -> zeroHourTime.plusHours(-1));
        assertThrows(IllegalArgumentException.class, () -> zeroHourTime.plusHours(-60));

        RecurringTime nonZeroHourTime = of(2, 0);
        assertTime(nonZeroHourTime.plusHours(-1), 1, 0);
        assertTime(nonZeroHourTime.plusHours(-2), 0, 0);
        assertTime(nonZeroHourTime.plusMinutes(-1), 1, 59);
        assertTime(nonZeroHourTime.plusMinutes(-60), 1, 0);
        assertTime(nonZeroHourTime.plusMinutes(-61), 0, 59);
        assertTime(nonZeroHourTime.plusMinutes(-120), 0, 0);

        assertThrows(IllegalArgumentException.class, () -> nonZeroHourTime.plusHours(-3));
        assertThrows(IllegalArgumentException.class, () -> nonZeroHourTime.plusHours(-121));
    }

    @Test
    public void test_before_after() {
        assertFalse(RecurringTime.of(1).isBefore(RecurringTime.of(1)));
        assertFalse(RecurringTime.of(1).isAfter(RecurringTime.of(1)));

        assertFalse(RecurringTime.of(1).isBefore(RecurringTime.of(0, 59)));
        assertTrue(RecurringTime.of(1).isAfter(RecurringTime.of(0, 59)));

        assertTrue(RecurringTime.of(0,59).isBefore(RecurringTime.of(1)));
        assertFalse(RecurringTime.of(0,59).isAfter(RecurringTime.of(1)));

        assertFalse(RecurringTime.of(1).isBefore(LocalTime.of(1, 0)));
        assertFalse(RecurringTime.of(1).isAfter(LocalTime.of(1, 0)));

        assertFalse(RecurringTime.of(1).isBefore(LocalTime.of(0, 59)));
        assertTrue(RecurringTime.of(1).isAfter(LocalTime.of(0, 59)));

        assertTrue(RecurringTime.of(0,59).isBefore(LocalTime.of(1, 0)));
        assertFalse(RecurringTime.of(0,59).isAfter(LocalTime.of(1, 0)));

    }

    private void assertTime(RecurringTime time, int expectedHour, int expectedMinute) {
        assertEquals(expectedHour, time.getHour());
        assertEquals(expectedMinute, time.getMinute());
    }

    private void assertTime(RecurringTime time, int expectedHour, int expectedMinute, boolean expectedValidLocalTime) {
        assertTime(time, expectedHour, expectedMinute);
        assertEquals(expectedValidLocalTime, time.isValidLocalTime());
    }

}
