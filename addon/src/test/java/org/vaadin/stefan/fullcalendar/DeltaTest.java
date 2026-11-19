package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DeltaTest {

    @Test
    void testConstructor() {
        Delta delta = new Delta(30, 23, 59, 58);

        Assertions.assertEquals(30, delta.getDays());
        Assertions.assertEquals(23, delta.getHours());
        Assertions.assertEquals(59, delta.getMinutes());
        Assertions.assertEquals(58, delta.getSeconds());
    }

    @Test
    void testApplyOnLocalDate() {
        LocalDate reference = LocalDate.of(2000, 1, 1);

        Assertions.assertEquals(reference, new Delta(0, 0, 0, 0).applyOn(reference));
        // the time parts are ignored
        Assertions.assertEquals(LocalDate.of(2000, 2, 2), new Delta(32, 23, 59, 59).applyOn(reference));
    }

    @Test
    void testApplyOnLocalDateTime() {
        LocalDateTime reference = LocalDate.of(2000, 1, 1).atStartOfDay();

        Assertions.assertEquals(reference, new Delta(0, 0, 0, 0).applyOn(reference));
        Assertions.assertEquals(LocalDateTime.of(2000, 1, 2, 1, 1, 1), new Delta(1, 1, 1, 1).applyOn(reference));
    }

    @Test
    void testCreationFromObjectNode() {
        ObjectNode jsonObject = JsonFactory.createObject();
        jsonObject.put("years", 1);
        jsonObject.put("months", 2);
        jsonObject.put("days", 3);
        jsonObject.put("hours", 4);
        jsonObject.put("minutes", 5);
        jsonObject.put("seconds", 6);

        // years and months are ignored
        Assertions.assertEquals(new Delta(3, 4, 5, 6), Delta.fromJson(jsonObject));
    }

    @Test
    void testCreationFromObjectNodeWithMilliseconds() {
        ObjectNode jsonObject = JsonFactory.createObject();
        jsonObject.put("years", 0);
        jsonObject.put("months", 0);
        jsonObject.put("days", -2);
        jsonObject.put("milliseconds", ((4 * 60 + 5) * 60 + 6) * 1000L);

        Assertions.assertEquals(new Delta(-2, 4, 5, 6), Delta.fromJson(jsonObject));
    }

    @Test
    void testCreationFromObjectNodeWithNegativeMilliseconds() {
        // an entry dragged from Monday 23:00 to Tuesday 00:30
        ObjectNode jsonObject = JsonFactory.createObject();
        jsonObject.put("days", 1);
        jsonObject.put("milliseconds", -(22 * 60 + 30) * 60 * 1000L);

        Delta delta = Delta.fromJson(jsonObject);

        Assertions.assertEquals(new Delta(1, -22, -30, 0), delta);
        Assertions.assertEquals(LocalDateTime.of(2025, 1, 7, 0, 30), delta.applyOn(LocalDateTime.of(2025, 1, 6, 23, 0)));
    }

    @Test
    void fromLocalDates_acrossMonthEnd_isExpressedInDays() {
        LocalDateTime from = LocalDateTime.of(2025, 1, 31, 10, 0);
        LocalDateTime to = LocalDateTime.of(2025, 2, 1, 10, 0);

        Delta delta = Delta.fromLocalDates(from, to);

        Assertions.assertEquals(new Delta(1, 0, 0, 0), delta);
        Assertions.assertEquals(to, delta.applyOn(from));
    }

    @Test
    void fromLocalDates_backwardsAcrossYearEnd_isNegative() {
        LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 30);
        LocalDateTime to = LocalDateTime.of(2025, 12, 30, 23, 0, 15);

        Delta delta = Delta.fromLocalDates(from, to);

        Assertions.assertEquals(new Delta(-1, -1, -29, -45), delta);
        Assertions.assertEquals(to, delta.applyOn(from));
    }

    @Test
    void fromLocalDates_truncatesSpanToWholeSeconds() {
        LocalDateTime from = LocalDateTime.of(2025, 1, 1, 10, 0, 0, 900_000_000);
        LocalDateTime to = LocalDateTime.of(2025, 1, 1, 10, 0, 2, 100_000_000);

        Assertions.assertEquals(new Delta(0, 0, 0, 1), Delta.fromLocalDates(from, to));
    }

    @Test
    void subtractFrom_isInverseOfApplyOn() {
        Delta delta = Delta.builder().days(3).hours(4).minutes(5).seconds(6).build();
        LocalDateTime start = LocalDateTime.of(2026, 4, 19, 12, 0, 0);

        Assertions.assertEquals(LocalDateTime.of(2026, 4, 22, 16, 5, 6), delta.applyOn(start));
        Assertions.assertEquals(start, delta.subtractFrom(delta.applyOn(start)));
        Assertions.assertEquals(LocalDate.of(2026, 4, 16), delta.subtractFrom(start.toLocalDate()));
    }
}
