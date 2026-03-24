package org.vaadin.stefan.fullcalendar;

import elemental.json.JsonArray;
import elemental.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class BusinessHoursTest {

    @Test
    void testConstruction() {
        BusinessHours hours = BusinessHours.allDays();
        Assertions.assertEquals(BusinessHours.ALL_DAYS, hours.getDayOfWeeks());
        Assertions.assertEquals(LocalTime.MIN, hours.getStart());
        Assertions.assertEquals(LocalTime.MAX, hours.getEnd());

        LocalTime start = LocalTime.of(5, 0);
        LocalTime end = start.plusHours(1);

        hours = hours.start(start);
        Assertions.assertEquals(start, hours.getStart());
        Assertions.assertEquals(LocalTime.MAX, hours.getEnd());

        hours = hours.end(end);
        Assertions.assertEquals(start, hours.getStart());
        Assertions.assertEquals(end, hours.getEnd());

        int startingHour = 2;
        int endingHour = 3;

        hours = hours.start(startingHour);
        Assertions.assertEquals(LocalTime.of(startingHour,0), hours.getStart());
        Assertions.assertEquals(end, hours.getEnd());

        hours = hours.end(endingHour);
        Assertions.assertEquals(LocalTime.of(startingHour,0), hours.getStart());
        Assertions.assertEquals(LocalTime.of(endingHour,0), hours.getEnd());
    }

    @Test
    void testConvertToClientSideDow() {
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            int clientSideDow = BusinessHours.convertToClientSideDow(dayOfWeek);

            if(dayOfWeek == DayOfWeek.SUNDAY) {
                Assertions.assertEquals(0, clientSideDow);
            } else {
                Assertions.assertEquals(dayOfWeek.getValue(), clientSideDow);
            }
        }
    }

    @Test
    void testToJson() {
        LocalTime start = LocalTime.of(5, 0);
        LocalTime end = start.plusHours(1);
        BusinessHours hours = BusinessHours.allDays().start(start).end(end);

        JsonObject object = hours.toJson();

        Assertions.assertEquals(start.toString(), object.get("startTime").asString());
        Assertions.assertEquals(end.toString(), object.get("endTime").asString());

        JsonArray array = (JsonArray) object.get("daysOfWeek");
        Set<Integer> days = new LinkedHashSet<>(array.length());
        for (int i = 0; i < array.length(); i++) {
            days.add((int) array.get(i).asNumber());
        }

        Assertions.assertEquals(BusinessHours.ALL_DAYS.stream().map(BusinessHours::convertToClientSideDow).collect(Collectors.toSet()), days);
    }


    @Test
    void testEmptyToJson() {
        BusinessHours hours = BusinessHours.allDays();

        JsonObject object = hours.toJson();

        Assertions.assertEquals("00:00", object.get("startTime").asString());
        Assertions.assertEquals("24:00", object.get("endTime").asString());

        Assertions.assertEquals(BusinessHours.ALL_DAYS.size(), ((JsonArray) object.get("daysOfWeek")).length());
    }

}
