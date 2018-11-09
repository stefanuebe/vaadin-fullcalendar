package org.vaadin.stefan.fullcalendar;

import elemental.json.JsonArray;
import elemental.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class BusinessHoursTest {
    @Test
    void testEmptyConstructors() {
        new BusinessHours((LocalTime) null);
        new BusinessHours((Set<DayOfWeek>) null);
        new BusinessHours((LocalTime) null, null);
        new BusinessHours((Set<DayOfWeek>) null, null);
        new BusinessHours(null, null, null);

        Assertions.assertEquals(5, BusinessHours.class.getDeclaredConstructors().length, "There are untested constructors!");
    }

    @Test
    void testEmptyReturnValues() {
        BusinessHours hours = new BusinessHours(null, null, null);

        Assertions.assertEquals(Optional.empty(), hours.getStart());
        Assertions.assertEquals(Optional.empty(), hours.getEnd());
        Assertions.assertEquals(Collections.emptySet(), hours.getDayOfWeeks());
    }

    @Test
    void testConstructors() {
        LocalTime start = LocalTime.of(5, 0);
        LocalTime end = start.plusHours(1);
        BusinessHours hours;

        hours = new BusinessHours(start);
        Assertions.assertEquals(start, hours.getStart().get());

        hours = new BusinessHours(BusinessHours.ALL_DAYS);
        Assertions.assertNotSame(BusinessHours.ALL_DAYS, hours.getDayOfWeeks());
        Assertions.assertEquals(BusinessHours.ALL_DAYS, hours.getDayOfWeeks());

        hours = new BusinessHours(start, end);
        Assertions.assertEquals(start, hours.getStart().get());
        Assertions.assertEquals(end, hours.getEnd().get());

        hours = new BusinessHours(BusinessHours.ALL_DAYS, start);
        Assertions.assertNotSame(BusinessHours.ALL_DAYS, hours.getDayOfWeeks());
        Assertions.assertEquals(BusinessHours.ALL_DAYS, hours.getDayOfWeeks());
        Assertions.assertEquals(start, hours.getStart().get());

        hours = new BusinessHours(BusinessHours.ALL_DAYS, start, end);
        Assertions.assertNotSame(BusinessHours.ALL_DAYS, hours.getDayOfWeeks());
        Assertions.assertEquals(BusinessHours.ALL_DAYS, hours.getDayOfWeeks());
        Assertions.assertEquals(start, hours.getStart().get());
        Assertions.assertEquals(end, hours.getEnd().get());

        Assertions.assertEquals(5, BusinessHours.class.getDeclaredConstructors().length, "There are untested constructors!");
    }

    @Test
    void testToJson() {
        LocalTime start = LocalTime.of(5, 0);
        LocalTime end = start.plusHours(1);
        BusinessHours hours = new BusinessHours(BusinessHours.ALL_DAYS, start, end);

        JsonObject object = hours.toJson();

        Assertions.assertEquals(start.toString(), object.getString("start"));
        Assertions.assertEquals(end.toString(), object.getString("end"));

        JsonArray array = object.get("dow");
        Set<Integer> days = new HashSet<>(array.length());
        for (int i = 0; i < array.length(); i++) {
            days.add((int) array.getNumber(i));
        }
        Assertions.assertEquals(BusinessHours.ALL_DAYS.stream().map(DayOfWeek::getValue).collect(Collectors.toSet()), days);
    }


    @Test
    void testEmptyToJson() {
        BusinessHours hours = new BusinessHours(null, null, null);

        JsonObject object = hours.toJson();

        Assertions.assertEquals("00:00", object.getString("start"));
        Assertions.assertEquals("1.00:00", object.getString("end"));

        Assertions.assertEquals(0, object.<JsonArray>get("dow").length());
    }

}
