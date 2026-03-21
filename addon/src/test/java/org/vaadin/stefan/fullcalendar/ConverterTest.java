package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.converters.*;
import org.vaadin.stefan.fullcalendar.model.Header;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the option converter classes.
 */
class ConverterTest {

    // -------------------------------------------------------------------------
    // DurationConverter
    // -------------------------------------------------------------------------

    @Test
    void duration_ofHours_convertsToString() {
        DurationConverter conv = new DurationConverter();
        assertTrue(conv.supports(Duration.ofHours(2)));
        JsonNode result = conv.toClientModel(Duration.ofHours(2), null);
        assertEquals("02:00:00", result.asString());
    }

    @Test
    void duration_ofMinutes_convertsToString() {
        DurationConverter conv = new DurationConverter();
        JsonNode result = conv.toClientModel(Duration.ofMinutes(30), null);
        assertEquals("00:30:00", result.asString());
    }

    @Test
    void duration_localTime_convertsToString() {
        DurationConverter conv = new DurationConverter();
        assertTrue(conv.supports(LocalTime.of(9, 30)));
        JsonNode result = conv.toClientModel(LocalTime.of(9, 30), null);
        assertEquals("09:30:00", result.asString());
    }

    @Test
    void duration_doesNotSupportString() {
        DurationConverter conv = new DurationConverter();
        // Strings bypass the converter — they go directly through JsonUtils.toJsonNode
        assertFalse(conv.supports("01:00:00"));
    }

    // -------------------------------------------------------------------------
    // DayOfWeekArrayConverter
    // -------------------------------------------------------------------------

    @Test
    void dayOfWeekArray_convertsToIntArray() {
        DayOfWeekArrayConverter conv = new DayOfWeekArrayConverter();
        assertTrue(conv.supports(new DayOfWeek[]{DayOfWeek.MONDAY}));
        JsonNode result = conv.toClientModel(new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.FRIDAY}, null);
        assertTrue(result.isArray());
        ArrayNode arr = (ArrayNode) result;
        assertEquals(2, arr.size());
        // FC uses 0=Sunday, 1=Monday, ..., 5=Friday
        assertEquals(1, arr.get(0).asInt());
        assertEquals(5, arr.get(1).asInt());
    }

    @Test
    void dayOfWeekArray_collection_converts() {
        DayOfWeekArrayConverter conv = new DayOfWeekArrayConverter();
        assertTrue(conv.supports(List.of(DayOfWeek.SATURDAY)));
        JsonNode result = conv.toClientModel(Set.of(DayOfWeek.SUNDAY), null);
        assertTrue(result.isArray());
        assertEquals(1, ((ArrayNode) result).size());
        assertEquals(0, ((ArrayNode) result).get(0).asInt()); // Sunday = 0
    }

    // -------------------------------------------------------------------------
    // StringArrayConverter
    // -------------------------------------------------------------------------

    @Test
    void stringArray_convertsToCommaJoinedString() {
        StringArrayConverter conv = new StringArrayConverter();
        assertTrue(conv.supports(new String[]{"a", "b"}));
        JsonNode result = conv.toClientModel(new String[]{"a", "b"}, null);
        assertTrue(result.isString());
        assertEquals("a,b", result.asString());
    }

    @Test
    void stringArray_collection_convertsToCommaJoinedString() {
        StringArrayConverter conv = new StringArrayConverter();
        assertTrue(conv.supports(List.of("x")));
        JsonNode result = conv.toClientModel(List.of("x", "y"), null);
        assertTrue(result.isString());
        assertEquals("x,y", result.asString());
    }

    // -------------------------------------------------------------------------
    // DayOfWeekConverter
    // -------------------------------------------------------------------------

    @Test
    void dayOfWeek_monday_converts() {
        DayOfWeekConverter conv = new DayOfWeekConverter();
        assertTrue(conv.supports(DayOfWeek.MONDAY));
        JsonNode result = conv.toClientModel(DayOfWeek.MONDAY, null);
        assertEquals(1, result.asInt());
    }

    @Test
    void dayOfWeek_sunday_converts() {
        DayOfWeekConverter conv = new DayOfWeekConverter();
        JsonNode result = conv.toClientModel(DayOfWeek.SUNDAY, null);
        assertEquals(0, result.asInt());
    }

    // -------------------------------------------------------------------------
    // LocaleConverter
    // -------------------------------------------------------------------------

    @Test
    void locale_converts() {
        LocaleConverter conv = new LocaleConverter();
        assertTrue(conv.supports(java.util.Locale.GERMAN));
        JsonNode result = conv.toClientModel(java.util.Locale.GERMAN, null);
        assertEquals("de", result.asString());
    }

    // -------------------------------------------------------------------------
    // BusinessHoursConverter
    // -------------------------------------------------------------------------

    @Test
    void businessHours_singleInstance_converts() {
        BusinessHoursConverter conv = new BusinessHoursConverter();
        BusinessHours bh = BusinessHours.businessWeek().start(9).end(17);
        assertTrue(conv.supports(bh));
        JsonNode result = conv.toClientModel(bh, null);
        assertTrue(result.isObject());
    }

    @Test
    void businessHours_array_converts() {
        BusinessHoursConverter conv = new BusinessHoursConverter();
        BusinessHours[] arr = {
                BusinessHours.businessWeek().start(9).end(17),
                BusinessHours.of(DayOfWeek.SATURDAY).start(10).end(14)
        };
        assertTrue(conv.supports(arr));
        JsonNode result = conv.toClientModel(arr, null);
        assertTrue(result.isArray());
        assertEquals(2, ((ArrayNode) result).size());
    }
}
