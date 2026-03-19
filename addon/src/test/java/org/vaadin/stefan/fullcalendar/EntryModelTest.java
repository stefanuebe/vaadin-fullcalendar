package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for entry/event model properties.
 * Covers url, interactive, recurringDuration, rrule, exdate, and the overlap null-semantics change.
 */
public class EntryModelTest {

    // -------------------------------------------------------------------------
    // url
    // -------------------------------------------------------------------------

    @Test
    void url_default_notInJson() {
        Entry entry = new Entry();
        assertFalse(entry.toJson().has("url"), "url should not be in JSON when not set");
    }

    @Test
    void url_setAndGet() {
        Entry entry = new Entry();
        entry.setUrl("https://example.com");
        assertEquals("https://example.com", entry.getUrl());
    }

    @Test
    void url_serializedToJson() {
        Entry entry = new Entry();
        entry.setUrl("https://example.com/event");
        ObjectNode json = entry.toJson();
        assertTrue(json.hasNonNull("url"), "url should be in JSON");
        assertEquals("https://example.com/event", json.get("url").asString());
    }

    @Test
    void url_null_removedFromJson() {
        Entry entry = new Entry();
        entry.setUrl("https://example.com");
        entry.setUrl(null);
        assertNull(entry.getUrl(), "url getter should return null after clearing");
        assertFalse(entry.toJson().has("url"), "url should not be in JSON after clearing");
    }

    // -------------------------------------------------------------------------
    // interactive
    // -------------------------------------------------------------------------

    @Test
    void interactive_default_notInJson() {
        Entry entry = new Entry();
        assertFalse(entry.toJson().has("interactive"), "interactive should not be in JSON when not set");
    }

    @Test
    void interactive_true_serializedToJson() {
        Entry entry = new Entry();
        entry.setInteractive(true);
        ObjectNode json = entry.toJson();
        assertTrue(json.hasNonNull("interactive"));
        assertTrue(json.get("interactive").asBoolean());
    }

    @Test
    void interactive_false_serializedToJson() {
        Entry entry = new Entry();
        entry.setInteractive(false);
        ObjectNode json = entry.toJson();
        assertTrue(json.hasNonNull("interactive"));
        assertFalse(json.get("interactive").asBoolean());
    }

    @Test
    void interactive_null_notInJson() {
        Entry entry = new Entry();
        entry.setInteractive(true);
        entry.setInteractive(null);
        assertFalse(entry.toJson().has("interactive"), "interactive should not be in JSON after clearing");
    }

    // -------------------------------------------------------------------------
    // recurringDuration (serializes as "duration" in JSON via @JsonName)
    // -------------------------------------------------------------------------

    @Test
    void recurringDuration_default_notInJson() {
        Entry entry = new Entry();
        assertFalse(entry.toJson().has("duration"), "duration should not be in JSON when not set");
    }

    @Test
    void recurringDuration_setAndGet() {
        Entry entry = new Entry();
        entry.setRecurringDuration("P1D");
        assertEquals("P1D", entry.getRecurringDuration());
    }

    @Test
    void recurringDuration_serializedAsDuration() {
        Entry entry = new Entry();
        entry.setRecurringDuration("PT2H");
        ObjectNode json = entry.toJson();
        assertFalse(json.has("recurringDuration"), "Java field name must NOT appear in JSON");
        assertTrue(json.hasNonNull("duration"), "FC key 'duration' must appear in JSON");
        assertEquals("PT2H", json.get("duration").asString());
    }

    @Test
    void recurringDuration_null_notInJson() {
        Entry entry = new Entry();
        entry.setRecurringDuration("P1D");
        entry.setRecurringDuration(null);
        assertNull(entry.getRecurringDuration(), "recurringDuration getter should return null after clearing");
        assertFalse(entry.toJson().has("duration"), "duration should not be in JSON after clearing");
    }

    // -------------------------------------------------------------------------
    // exdate
    // -------------------------------------------------------------------------

    @Test
    void exdate_default_notInJson() {
        Entry entry = new Entry();
        assertFalse(entry.toJson().has("exdate"), "exdate should not be in JSON when not set");
    }

    @Test
    void exdate_setAndGet() {
        Entry entry = new Entry();
        entry.setExdate("2024-01-15,2024-02-20");
        assertEquals("2024-01-15,2024-02-20", entry.getExdate());
    }

    @Test
    void exdate_serializedToJson() {
        Entry entry = new Entry();
        entry.setExdate("2024-01-15");
        ObjectNode json = entry.toJson();
        assertTrue(json.hasNonNull("exdate"));
        assertEquals("2024-01-15", json.get("exdate").asString());
    }

    @Test
    void exdate_multipleDates_serializedAsJsonArray() {
        // When multiple dates are comma-separated, FC expects a JSON array, not a plain string.
        Entry entry = new Entry();
        entry.setExdate("2024-01-15,2024-02-20");
        ObjectNode json = entry.toJson();
        assertTrue(json.hasNonNull("exdate"), "exdate should be in JSON");
        JsonNode exdateNode = json.get("exdate");
        assertInstanceOf(ArrayNode.class, exdateNode, "Multiple exdates should serialize as a JSON array");
        ArrayNode arr = (ArrayNode) exdateNode;
        assertEquals(2, arr.size());
        assertEquals("2024-01-15", arr.get(0).asString());
        assertEquals("2024-02-20", arr.get(1).asString());
    }

    @Test
    void exdate_singleDate_serializedAsString() {
        // A single exdate is sent as a plain string (FC accepts "a single string or array")
        Entry entry = new Entry();
        entry.setExdate("2024-01-15");
        ObjectNode json = entry.toJson();
        JsonNode exdateNode = json.get("exdate");
        assertFalse(exdateNode instanceof ArrayNode, "Single exdate should serialize as a string, not an array");
        assertEquals("2024-01-15", exdateNode.asString());
    }

    @Test
    void exdate_null_removedFromJson() {
        Entry entry = new Entry();
        entry.setExdate("2024-01-15");
        entry.setExdate(null);
        assertNull(entry.getExdate(), "exdate getter should return null after clearing");
        assertFalse(entry.toJson().has("exdate"), "exdate should not be in JSON after clearing");
    }

    // -------------------------------------------------------------------------
    // overlap — changed from boolean (default true) to Boolean (default null)
    // -------------------------------------------------------------------------

    @Test
    void overlap_default_null_notInJson() {
        Entry entry = new Entry();
        assertNull(entry.getOverlap(), "overlap default should be null (inherit)");
        assertFalse(entry.toJson().has("overlap"),
                "overlap should NOT be in JSON when null (means inherit from calendar-level setting)");
    }

    @Test
    void overlap_true_inJson() {
        Entry entry = new Entry();
        entry.setOverlap(true);
        ObjectNode json = entry.toJson();
        assertTrue(json.hasNonNull("overlap"));
        assertTrue(json.get("overlap").asBoolean());
    }

    @Test
    void overlap_false_inJson() {
        Entry entry = new Entry();
        entry.setOverlap(false);
        ObjectNode json = entry.toJson();
        assertTrue(json.hasNonNull("overlap"));
        assertFalse(json.get("overlap").asBoolean());
    }

    @Test
    void overlap_clearedToNull_notInJson() {
        Entry entry = new Entry();
        entry.setOverlap(true);
        entry.setOverlap(null);
        assertNull(entry.getOverlap());
        assertFalse(entry.toJson().has("overlap"), "overlap should not be in JSON after clearing to null");
    }

    @Test
    void overlapAllowed_aliasWorksWithBoolean() {
        Entry entry = new Entry();
        entry.setOverlapAllowed(false);
        assertEquals(Boolean.FALSE, entry.isOverlapAllowed());
        entry.setOverlapAllowed(null);
        assertNull(entry.isOverlapAllowed());
    }

    @Test
    void setOverlap_andSetOverlapAllowed_affectSameField() {
        // setOverlap() and setOverlapAllowed() are aliases for the same underlying field
        Entry entry = new Entry();
        entry.setOverlap(true);
        assertEquals(Boolean.TRUE, entry.isOverlapAllowed(), "setOverlap(true) should be visible via isOverlapAllowed()");
        assertEquals(Boolean.TRUE, entry.getOverlap(), "setOverlapAllowed should be visible via getOverlap()");

        entry.setOverlapAllowed(false);
        assertEquals(Boolean.FALSE, entry.getOverlap(), "setOverlapAllowed(false) should be visible via getOverlap()");
    }

    // -------------------------------------------------------------------------
    // RRule — structured form
    // -------------------------------------------------------------------------

    @Test
    void rrule_default_notInJson() {
        Entry entry = new Entry();
        assertFalse(entry.toJson().has("rrule"), "rrule should not be in JSON when not set");
    }

    @Test
    void rrule_weekly_toJson_hasFreq() {
        RRule rrule = RRule.weekly();
        ObjectNode json = (ObjectNode) rrule.toJson();
        assertEquals("weekly", json.get("freq").asString());
    }

    @Test
    void rrule_byWeekday_DayOfWeek_convertedToAbbreviation() {
        RRule rrule = RRule.weekly().byWeekday(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);
        ObjectNode json = (ObjectNode) rrule.toJson();

        ArrayNode byweekday = (ArrayNode) json.get("byweekday");
        assertNotNull(byweekday);
        assertEquals(2, byweekday.size());
        assertEquals("mo", byweekday.get(0).asString());
        assertEquals("fr", byweekday.get(1).asString());
    }

    @Test
    void rrule_byWeekday_allDays_correctAbbreviations() {
        RRule rrule = RRule.weekly().byWeekday(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
        ObjectNode json = (ObjectNode) rrule.toJson();
        ArrayNode bwd = (ArrayNode) json.get("byweekday");
        List<String> days = List.of("mo", "tu", "we", "th", "fr", "sa", "su");
        for (int i = 0; i < days.size(); i++) {
            assertEquals(days.get(i), bwd.get(i).asString(), "Day at index " + i);
        }
    }

    @Test
    void rrule_byMonth_Month_convertedToIntegers() {
        RRule rrule = RRule.monthly().byMonth(Month.JANUARY, Month.MARCH, Month.DECEMBER);
        ObjectNode json = (ObjectNode) rrule.toJson();

        ArrayNode bymonth = (ArrayNode) json.get("bymonth");
        assertNotNull(bymonth);
        assertEquals(3, bymonth.size());
        assertEquals(1, bymonth.get(0).asInt());
        assertEquals(3, bymonth.get(1).asInt());
        assertEquals(12, bymonth.get(2).asInt());
    }

    @Test
    void rrule_weekStart_DayOfWeek_convertedToAbbreviation() {
        RRule rrule = RRule.weekly().weekStart(DayOfWeek.MONDAY);
        ObjectNode json = (ObjectNode) rrule.toJson();
        assertEquals("mo", json.get("wkst").asString());

        RRule rrule2 = RRule.weekly().weekStart(DayOfWeek.SUNDAY);
        ObjectNode json2 = (ObjectNode) rrule2.toJson();
        assertEquals("su", json2.get("wkst").asString());
    }

    @Test
    void rrule_count_inJson() {
        RRule rrule = RRule.weekly().count(10);
        ObjectNode json = (ObjectNode) rrule.toJson();
        assertEquals(10, json.get("count").asInt());
    }

    @Test
    void rrule_interval_inJson() {
        RRule rrule = RRule.daily().interval(2);
        ObjectNode json = (ObjectNode) rrule.toJson();
        assertEquals(2, json.get("interval").asInt());
    }

    @Test
    void rrule_until_LocalDate_inJson() {
        RRule rrule = RRule.weekly().until(LocalDate.of(2025, 12, 31));
        ObjectNode json = (ObjectNode) rrule.toJson();
        assertEquals("2025-12-31", json.get("until").asString());
    }

    @Test
    void rrule_until_LocalDateTime_inJson() {
        RRule rrule = RRule.weekly().until(LocalDateTime.of(2025, 12, 31, 23, 59, 59));
        ObjectNode json = (ObjectNode) rrule.toJson();
        assertEquals("2025-12-31T23:59:59", json.get("until").asString());
    }

    @Test
    void rrule_dtstart_LocalDate_inJson() {
        RRule rrule = RRule.weekly().dtstart(LocalDate.of(2025, 1, 1));
        ObjectNode json = (ObjectNode) rrule.toJson();
        assertEquals("2025-01-01", json.get("dtstart").asString());
    }

    @Test
    void rrule_byMonthday_inJson() {
        RRule rrule = RRule.monthly().byMonthday(1, 15);
        ObjectNode json = (ObjectNode) rrule.toJson();
        ArrayNode bmd = (ArrayNode) json.get("bymonthday");
        assertNotNull(bmd);
        assertEquals(2, bmd.size());
        assertEquals(1, bmd.get(0).asInt());
        assertEquals(15, bmd.get(1).asInt());
    }

    @Test
    void rrule_byYearday_inJson() {
        RRule rrule = RRule.yearly().byYearday(1, 100, 365);
        ObjectNode json = (ObjectNode) rrule.toJson();
        ArrayNode byd = (ArrayNode) json.get("byyearday");
        assertNotNull(byd);
        assertEquals(3, byd.size());
    }

    @Test
    void rrule_byHour_byMinute_inJson() {
        RRule rrule = RRule.daily().byHour(9, 17).byMinute(0, 30);
        ObjectNode json = (ObjectNode) rrule.toJson();
        assertEquals(2, ((ArrayNode) json.get("byhour")).size());
        assertEquals(2, ((ArrayNode) json.get("byminute")).size());
    }

    @Test
    void rrule_byWeekday_strings_passedThrough() {
        RRule rrule = RRule.monthly().byWeekday("-1fr", "2mo");
        ObjectNode json = (ObjectNode) rrule.toJson();
        ArrayNode bwd = (ArrayNode) json.get("byweekday");
        assertEquals("-1fr", bwd.get(0).asString());
        assertEquals("2mo", bwd.get(1).asString());
    }

    // -------------------------------------------------------------------------
    // RRule — raw string form
    // -------------------------------------------------------------------------

    @Test
    void rrule_raw_toJson_returnsString() {
        RRule rrule = RRule.ofRaw("FREQ=WEEKLY;BYDAY=MO,WE;COUNT=10");
        JsonNode json = rrule.toJson();
        assertTrue(json.isString(), "Raw RRule should serialize as a StringNode");
        assertEquals("FREQ=WEEKLY;BYDAY=MO,WE;COUNT=10", json.asString());
    }

    @Test
    void rrule_raw_notAffectedByStructuredFields() {
        // raw form ignores freq / byweekday etc. — raw string takes precedence
        RRule rrule = RRule.ofRaw("FREQ=DAILY");
        JsonNode json = rrule.toJson();
        assertTrue(json.isString());
    }

    // -------------------------------------------------------------------------
    // RRule — factory methods
    // -------------------------------------------------------------------------

    @Test
    void rrule_factoryMethods_produceCorrectFrequency() {
        assertEquals("weekly", ((ObjectNode) RRule.weekly().toJson()).get("freq").asString());
        assertEquals("daily", ((ObjectNode) RRule.daily().toJson()).get("freq").asString());
        assertEquals("monthly", ((ObjectNode) RRule.monthly().toJson()).get("freq").asString());
        assertEquals("yearly", ((ObjectNode) RRule.yearly().toJson()).get("freq").asString());
    }

    // -------------------------------------------------------------------------
    // rrule in Entry JSON
    // -------------------------------------------------------------------------

    @Test
    void entry_rrule_serializedToJson() {
        Entry entry = new Entry();
        entry.setRrule(RRule.weekly().byWeekday(DayOfWeek.MONDAY));

        ObjectNode json = entry.toJson();
        assertTrue(json.hasNonNull("rrule"), "rrule key must be present in entry JSON");
        assertTrue(json.get("rrule").isObject(), "rrule value must be a JSON object for structured form");
    }

    @Test
    void entry_rrule_raw_serializedAsString() {
        Entry entry = new Entry();
        entry.setRrule(RRule.ofRaw("FREQ=WEEKLY;BYDAY=MO"));

        ObjectNode json = entry.toJson();
        assertTrue(json.hasNonNull("rrule"));
        assertTrue(json.get("rrule").isString(), "raw RRule must be a JSON string in entry JSON");
    }

    @Test
    void entry_rrule_null_notInJson() {
        Entry entry = new Entry();
        entry.setRrule(RRule.weekly());
        entry.setRrule(null);
        assertNull(entry.getRrule(), "getRrule() should return null after clearing");
        assertFalse(entry.toJson().has("rrule"), "rrule should not be in JSON after clearing");
    }
}
