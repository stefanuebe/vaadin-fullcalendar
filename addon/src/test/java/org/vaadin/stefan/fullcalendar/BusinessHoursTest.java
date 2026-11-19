package org.vaadin.stefan.fullcalendar;

import org.vaadin.stefan.fullcalendar.option.BusinessHours;
import org.vaadin.stefan.fullcalendar.option.BusinessHoursValue;
import org.vaadin.stefan.fullcalendar.option.ConstraintValue;
import org.vaadin.stefan.fullcalendar.option.ExtendedLocalTime;
import org.vaadin.stefan.fullcalendar.converters.BusinessHoursValueConverter;
import org.vaadin.stefan.fullcalendar.converters.ConstraintValueConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class BusinessHoursTest {

    @Test
    void testConstruction() {
        BusinessHours hours = BusinessHours.allDays();
        Assertions.assertEquals(BusinessHours.ALL_DAYS, hours.getDayOfWeeks());
        Assertions.assertEquals(ExtendedLocalTime.of(0), hours.getStart());
        Assertions.assertEquals(ExtendedLocalTime.of(24), hours.getEnd());

        LocalTime start = LocalTime.of(5, 0);
        LocalTime end = start.plusHours(1);

        hours = hours.start(start);
        Assertions.assertEquals(ExtendedLocalTime.of(start), hours.getStart());
        Assertions.assertEquals(ExtendedLocalTime.of(24), hours.getEnd());

        hours = hours.end(end);
        Assertions.assertEquals(ExtendedLocalTime.of(start), hours.getStart());
        Assertions.assertEquals(ExtendedLocalTime.of(end), hours.getEnd());

        int startingHour = 2;
        int endingHour = 3;

        hours = hours.start(startingHour);
        Assertions.assertEquals(ExtendedLocalTime.of(startingHour), hours.getStart());
        Assertions.assertEquals(ExtendedLocalTime.of(end), hours.getEnd());

        hours = hours.end(endingHour);
        Assertions.assertEquals(ExtendedLocalTime.of(startingHour), hours.getStart());
        Assertions.assertEquals(ExtendedLocalTime.of(endingHour), hours.getEnd());
    }

    @Test
    void endPastMidnight_isWrittenPast24AndReadBack() {
        BusinessHours lateShift = BusinessHours.of(DayOfWeek.FRIDAY)
                .start(ExtendedLocalTime.of(22, 30)).end(ExtendedLocalTime.of(26, 15));

        ObjectNode json = lateShift.toJson();
        Assertions.assertEquals("22:30", json.get("startTime").asString());
        Assertions.assertEquals("26:15", json.get("endTime").asString());

        Assertions.assertEquals(BusinessHoursValue.of(lateShift),
                new BusinessHoursValueConverter().toServerModel(JsonFactory.createArray().add(json), null));
        Assertions.assertEquals(ConstraintValue.ofWindows(lateShift),
                new ConstraintValueConverter().toServerModel(JsonFactory.createArray().add(json), null));
    }

    @Test
    void endBeforeStart_isRejected() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> BusinessHours.allDays().start(22).end(2));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> BusinessHours.allDays().end(ExtendedLocalTime.of(30)).start(ExtendedLocalTime.of(26)).end(ExtendedLocalTime.of(22)));
    }

    @Test
    void endEqualToStart_isAllowed() {
        Assertions.assertEquals(ExtendedLocalTime.of(22), BusinessHours.allDays().start(22).end(22).getEnd());
    }

    @Test
    void endOfDay_isTheSameForEveryWayToSetIt() {
        BusinessHours byHour = BusinessHours.allDays().end(24);
        Assertions.assertEquals(byHour, BusinessHours.allDays().end(LocalTime.MAX));
        Assertions.assertEquals(byHour, BusinessHours.allDays());
        Assertions.assertEquals("24:00", byHour.toJson().get("endTime").asString());
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

        ObjectNode object = hours.toJson();

        Assertions.assertEquals(start.toString(), object.get("startTime").asString());
        Assertions.assertEquals(end.toString(), object.get("endTime").asString());

        ArrayNode array = (ArrayNode) object.get("daysOfWeek");
        Set<Integer> days = new LinkedHashSet<>(array.size());
        for (JsonNode arrayItem : array) {
            days.add(arrayItem.asInt());
        }

        Assertions.assertEquals(BusinessHours.ALL_DAYS.stream().map(BusinessHours::convertToClientSideDow).collect(Collectors.toSet()), days);
    }

    @Test
    void testEmptyToJson() {
        BusinessHours hours = BusinessHours.allDays();

        ObjectNode object = hours.toJson();

        Assertions.assertEquals("00:00", object.get("startTime").asString());
        Assertions.assertEquals("24:00", object.get("endTime").asString());

        Assertions.assertEquals(BusinessHours.ALL_DAYS.size(), object.get("daysOfWeek").size());
    }

}
