package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DeltaTest {

    @Test
    void testAssertLessThanThisWorks() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Delta.assertLessThan("test 1 < 1", 1, 1));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Delta.assertLessThan("test 2 < 1", 2, 1));
        Delta.assertLessThan("test 1 < 2", 1, 2);
    }

    @Test
    void testConstructorFieldInit() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Delta(0, 12, 0, 0, 0, 0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Delta(0, 0, 31, 0, 0, 0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Delta(0, 0, 0, 24, 0, 0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Delta(0, 0, 0, 0, 60, 0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Delta(0, 0, 0, 0, 0, 60));

        // max valid values
        Delta delta = new Delta(Integer.MAX_VALUE, 11, 30, 23, 59, 59);

        Assertions.assertEquals(Integer.MAX_VALUE, delta.getYears());
        Assertions.assertEquals(11, delta.getMonths());
        Assertions.assertEquals(30, delta.getDays());
        Assertions.assertEquals(23, delta.getHours());
        Assertions.assertEquals(59, delta.getMinutes());
        Assertions.assertEquals(59, delta.getSeconds());
    }

    @Test
    void testApplyOnLocalDate() {
        LocalDate reference = LocalDate.of(2000, 1, 1);

        LocalDate applied;
        applied = new Delta(0, 0, 0, 0, 0, 0).applyOn(reference);
        Assertions.assertEquals(reference, applied);

        applied = new Delta(1, 1, 1, 1, 1, 1).applyOn(reference);
        Assertions.assertTrue(reference.isBefore(applied));

        Assertions.assertEquals(reference.getYear() + 1, applied.getYear());
        Assertions.assertEquals(reference.getMonthValue() + 1, applied.getMonthValue());
        Assertions.assertEquals(reference.getDayOfMonth() + 1, applied.getDayOfMonth());
    }

    @Test
    void testApplyOnLocalDateTime() {
        LocalDateTime reference = LocalDate.of(2000, 1, 1).atStartOfDay();

        LocalDateTime applied;
        applied = new Delta(0, 0, 0, 0, 0, 0).applyOn(reference);
        Assertions.assertEquals(reference, applied);


        applied = new Delta(1, 1, 1, 1, 1, 1).applyOn(reference);
        Assertions.assertTrue(reference.isBefore(applied));

        Assertions.assertEquals(reference.getYear() + 1, applied.getYear());
        Assertions.assertEquals(reference.getMonthValue() + 1, applied.getMonthValue());
        Assertions.assertEquals(reference.getDayOfMonth() + 1, applied.getDayOfMonth());
        Assertions.assertEquals(reference.getHour() + 1, applied.getHour());
        Assertions.assertEquals(reference.getMinute() + 1, applied.getMinute());
        Assertions.assertEquals(reference.getSecond() + 1, applied.getSecond());
    }
}
