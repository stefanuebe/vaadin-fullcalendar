package org.vaadin.stefan.fullcalendar;

import elemental.json.Json;
import elemental.json.JsonObject;
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
    void testConstructor() {
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

    @Test
    void testCreationFromJsonObject() {
        JsonObject jsonObject = Json.createObject();
        jsonObject.put("years", 1);
        jsonObject.put("months", 2);
        jsonObject.put("days", 3);
        jsonObject.put("hours", 4);
        jsonObject.put("minutes", 5);
        jsonObject.put("seconds", 6);

        Delta delta = Delta.fromJson(jsonObject);

        Assertions.assertEquals(1, delta.getYears());
        Assertions.assertEquals(2, delta.getMonths());
        Assertions.assertEquals(3, delta.getDays());
        Assertions.assertEquals(4, delta.getHours());
        Assertions.assertEquals(5, delta.getMinutes());
        Assertions.assertEquals(6, delta.getSeconds());
    }
}
