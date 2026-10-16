package org.vaadin.stefan.fullcalendar;

import org.vaadin.stefan.fullcalendar.option.DisplayMode;
import org.vaadin.stefan.fullcalendar.option.BusinessHours;
import org.vaadin.stefan.fullcalendar.option.ExtendedLocalTime;
import org.vaadin.stefan.fullcalendar.converters.DisplayModeConverter;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

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
    void recurringDuration_serializedAsHoursMinutesSeconds() {
        Entry entry = new Entry();
        entry.setRecurringDuration(Duration.ofDays(3).plusHours(12));
        ObjectNode json = entry.toJson();
        assertFalse(json.has("recurringDuration"), "Java field name must NOT appear in JSON");
        assertEquals("84:00:00", json.get("duration").asString());
    }

    @Test
    void recurringDuration_readBackFromJson() {
        // external drops rebuild the entry from its JSON with updateAllFromJson
        Entry source = new Entry();
        source.setRecurringDuration(Duration.ofMinutes(30));

        Entry target = new Entry();
        target.updateAllFromJson(source.toJson(), false);
        assertEquals(Duration.ofMinutes(30), target.getRecurringDuration());
    }

    @Test
    void recurringDuration_null_notInJson() {
        Entry entry = new Entry();
        entry.setRecurringDuration(Duration.ofHours(2));
        entry.setRecurringDuration(null);
        assertNull(entry.getRecurringDuration());
        assertFalse(entry.toJson().has("duration"), "duration should not be in JSON after clearing");
    }

    // -------------------------------------------------------------------------
    // built-in recurrence and RRule exclude each other
    // -------------------------------------------------------------------------

    private static Entry builtInRecurringEntry() {
        Entry entry = new Entry();
        entry.setRecurringDaysOfWeek(DayOfWeek.MONDAY);
        entry.setRecurringStartDate(LocalDate.of(2025, 3, 1));
        entry.setRecurringEndDate(LocalDate.of(2025, 4, 1));
        entry.setRecurringStartTime(LocalTime.of(9, 0));
        entry.setRecurringEndTime(LocalTime.of(10, 0));
        entry.setRecurringDuration(Duration.ofHours(48));
        return entry;
    }

    private static Entry rruleEntry() {
        Entry entry = new Entry();
        entry.setRRule(RRule.weekly()
                .excludeDates(LocalDate.of(2025, 3, 10))
                .excludeRules(RRule.monthly()));
        entry.setRecurringDuration(Duration.ofHours(48));
        return entry;
    }

    @Test
    void setRRule_clearsBuiltInRecurrence_keepsDuration() {
        Entry entry = builtInRecurringEntry();
        entry.setRRule(RRule.weekly());

        assertNull(entry.getRecurringDaysOfWeek());
        assertNull(entry.getRecurringStartDate());
        assertNull(entry.getRecurringEndDate());
        assertNull(entry.getRecurringStartTime());
        assertNull(entry.getRecurringEndTime());
        assertEquals(Duration.ofHours(48), entry.getRecurringDuration());

        ObjectNode json = entry.toJson();
        for (String key : List.of("daysOfWeek", "startRecur", "endRecur", "startTime", "endTime")) {
            assertFalse(json.has(key), key + " must not be sent next to an rrule");
        }
        assertTrue(json.has("rrule"));
        assertEquals("48:00:00", json.get("duration").asString());
    }

    @Test
    void setRRule_null_keepsBuiltInRecurrence() {
        Entry entry = builtInRecurringEntry();
        entry.setRRule(null);

        assertEquals(Set.of(DayOfWeek.MONDAY), entry.getRecurringDaysOfWeek());
        assertEquals(LocalDate.of(2025, 3, 1), entry.getRecurringStartDate());
        assertEquals(LocalDate.of(2025, 4, 1), entry.getRecurringEndDate());
        assertEquals(LocalTime.of(9, 0), entry.getRecurringStartTimeAsLocalTime());
        assertEquals(LocalTime.of(10, 0), entry.getRecurringEndTimeAsLocalTime());
    }

    /** Each built-in setter with a non-null value, mapped to the getter that must return that value afterwards. */
    private static Map<String, Map.Entry<Consumer<Entry>, Function<Entry, Object>>> builtInSetters() {
        Map<String, Map.Entry<Consumer<Entry>, Function<Entry, Object>>> setters = new LinkedHashMap<>();
        setters.put("setRecurringDaysOfWeek(DayOfWeek...)", Map.entry(
                e -> e.setRecurringDaysOfWeek(DayOfWeek.MONDAY), Entry::getRecurringDaysOfWeek));
        setters.put("setRecurringDaysOfWeek(Set)", Map.entry(
                e -> e.setRecurringDaysOfWeek(Set.of(DayOfWeek.MONDAY)), Entry::getRecurringDaysOfWeek));
        setters.put("setRecurringStartDate", Map.entry(
                e -> e.setRecurringStartDate(LocalDate.of(2025, 3, 1)), Entry::getRecurringStartDate));
        setters.put("setRecurringEndDate", Map.entry(
                e -> e.setRecurringEndDate(LocalDate.of(2025, 4, 1)), Entry::getRecurringEndDate));
        setters.put("setRecurringStartTime(LocalTime)", Map.entry(
                e -> e.setRecurringStartTime(LocalTime.of(9, 0)), Entry::getRecurringStartTime));
        setters.put("setRecurringStartTime(ExtendedLocalTime)", Map.entry(
                e -> e.setRecurringStartTime(ExtendedLocalTime.of(25, 0)), Entry::getRecurringStartTime));
        setters.put("setRecurringEndTime(LocalTime)", Map.entry(
                e -> e.setRecurringEndTime(LocalTime.of(10, 0)), Entry::getRecurringEndTime));
        setters.put("setRecurringEndTime(ExtendedLocalTime)", Map.entry(
                e -> e.setRecurringEndTime(ExtendedLocalTime.of(26, 0)), Entry::getRecurringEndTime));
        return setters;
    }

    @TestFactory
    Stream<DynamicTest> builtInSetter_nonNull_clearsRRule_keepsDuration() {
        return builtInSetters().entrySet().stream().map(setter -> DynamicTest.dynamicTest(setter.getKey(), () -> {
            Entry entry = rruleEntry();
            setter.getValue().getKey().accept(entry);

            assertNotNull(setter.getValue().getValue().apply(entry), "the built-in value must be stored");
            assertNull(entry.getRRule());
            assertEquals(Duration.ofHours(48), entry.getRecurringDuration());

            ObjectNode json = entry.toJson();
            assertFalse(json.has("rrule"));
            assertFalse(json.has("exdate"));
            assertFalse(json.has("exrule"));
            assertEquals("48:00:00", json.get("duration").asString());
        }));
    }

    private static Map<String, Consumer<Entry>> builtInNullSetters() {
        Map<String, Consumer<Entry>> setters = new LinkedHashMap<>();
        setters.put("setRecurringDaysOfWeek(Set) null", e -> e.setRecurringDaysOfWeek((Set<DayOfWeek>) null));
        setters.put("setRecurringDaysOfWeek(Set) empty", e -> e.setRecurringDaysOfWeek(Set.of()));
        setters.put("setRecurringDaysOfWeek() empty", e -> e.setRecurringDaysOfWeek());
        setters.put("setRecurringStartDate", e -> e.setRecurringStartDate(null));
        setters.put("setRecurringEndDate", e -> e.setRecurringEndDate(null));
        setters.put("setRecurringStartTime(LocalTime)", e -> e.setRecurringStartTime((LocalTime) null));
        setters.put("setRecurringStartTime(ExtendedLocalTime)", e -> e.setRecurringStartTime((ExtendedLocalTime) null));
        setters.put("setRecurringEndTime(LocalTime)", e -> e.setRecurringEndTime((LocalTime) null));
        setters.put("setRecurringEndTime(ExtendedLocalTime)", e -> e.setRecurringEndTime((ExtendedLocalTime) null));
        return setters;
    }

    @TestFactory
    Stream<DynamicTest> builtInSetter_null_keepsRRule() {
        return builtInNullSetters().entrySet().stream().map(setter -> DynamicTest.dynamicTest(setter.getKey(), () -> {
            Entry entry = rruleEntry();
            setter.getValue().accept(entry);

            assertNotNull(entry.getRRule());
            ObjectNode json = entry.toJson();
            assertTrue(json.has("rrule"));
            assertTrue(json.has("exdate"));
            assertTrue(json.has("exrule"));
        }));
    }

    // -------------------------------------------------------------------------
    // exdate — set via RRule.excludeDates(), transferred by Entry.setRRule()
    // -------------------------------------------------------------------------

    @Test
    void excludeDates_notInJson_whenNotSet() {
        Entry entry = new Entry();
        entry.setRRule(RRule.weekly().dtstart(LocalDate.of(2024, 1, 1)));
        assertFalse(entry.toJson().has("exdate"), "exdate should not be in JSON when no excludeDates set");
    }

    @Test
    void excludeDates_transferredToEntryOnSetRRule() {
        List<LocalDate> dates = List.of(LocalDate.of(2024, 1, 15), LocalDate.of(2024, 2, 20));
        Entry entry = new Entry();
        entry.setRRule(RRule.weekly().excludeDates(dates));
        // verify exdate is serialized into the event JSON
        ObjectNode json = entry.toJson();
        assertTrue(json.hasNonNull("exdate"), "exdate should be in JSON after setRRule with excludeDates");
        JsonNode exdateNode = json.get("exdate");
        assertInstanceOf(ArrayNode.class, exdateNode);
        ArrayNode arr = (ArrayNode) exdateNode;
        assertEquals(2, arr.size());
        assertEquals("2024-01-15", arr.get(0).asString());
        assertEquals("2024-02-20", arr.get(1).asString());
    }

    @Test
    void excludeDates_singleDate_serializedAsArrayWithOneElement() {
        Entry entry = new Entry();
        entry.setRRule(RRule.weekly().excludeDates(LocalDate.of(2024, 1, 15)));
        JsonNode exdateNode = entry.toJson().get("exdate");
        assertInstanceOf(ArrayNode.class, exdateNode);
        assertEquals(1, ((ArrayNode) exdateNode).size());
        assertEquals("2024-01-15", ((ArrayNode) exdateNode).get(0).asString());
    }

    @Test
    void excludeDates_notInRRuleJson() {
        // excludedDates must NOT appear inside the rrule string — only at event level
        RRule rrule = RRule.weekly().excludeDates(LocalDate.of(2024, 1, 15));
        String rruleStr = rrule.toRRuleString();
        assertFalse(rruleStr.contains("excludedDates"), "excludedDates must not be serialized inside RRULE string");
        assertFalse(rruleStr.contains("exdate"), "exdate must not be serialized inside RRULE string");
    }

    @Test
    void excludeDates_clearedWhenSetRRuleNull() {
        Entry entry = new Entry();
        entry.setRRule(RRule.weekly().excludeDates(LocalDate.of(2024, 1, 15)));
        entry.setRRule(null);
        assertNull(entry.getRRule());
        assertFalse(entry.toJson().has("exdate"), "exdate should not be in JSON after setRRule(null)");
    }

    @Test
    void excludeRules_clearedWhenSetRRuleNull() {
        Entry entry = new Entry();
        entry.setRRule(RRule.weekly().excludeRules(RRule.monthly()));
        entry.setRRule(null);
        assertFalse(entry.toJson().has("exrule"), "exrule should not be in JSON after setRRule(null)");
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
        String rruleStr = rrule.toRRuleString();
        assertTrue(rruleStr.contains("FREQ=WEEKLY"), "RRULE string must contain FREQ=WEEKLY");

        JsonNode json = rrule.toJson();
        assertTrue(json.isObject(), "toJson() must return an ObjectNode for structured form");
        assertEquals("weekly", json.get("freq").asString(), "freq must be lowercase 'weekly' for the rrule plugin");
    }

    @Test
    void rrule_byWeekday_DayOfWeek_convertedToAbbreviation() {
        RRule rrule = RRule.weekly().byWeekday(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);
        String rruleStr = rrule.toRRuleString();
        assertTrue(rruleStr.contains("BYDAY=MO,FR"), "RRULE string must contain BYDAY=MO,FR");
        assertTrue(rruleStr.contains("FREQ=WEEKLY"), "RRULE string must contain FREQ=WEEKLY");
    }

    @Test
    void rrule_byWeekday_allDays_correctAbbreviations() {
        RRule rrule = RRule.weekly().byWeekday(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
        String rruleStr = rrule.toRRuleString();
        assertTrue(rruleStr.contains("BYDAY=MO,TU,WE,TH,FR,SA,SU"),
                "RRULE string must contain all days in order: " + rruleStr);
    }

    @Test
    void rrule_byMonth_Month_convertedToIntegers() {
        RRule rrule = RRule.monthly().byMonth(Month.JANUARY, Month.MARCH, Month.DECEMBER);
        String rruleStr = rrule.toRRuleString();
        assertTrue(rruleStr.contains("BYMONTH=1,3,12"), "RRULE string must contain BYMONTH=1,3,12: " + rruleStr);
    }

    @Test
    void rrule_weekStart_DayOfWeek_convertedToAbbreviation() {
        String rruleStr1 = RRule.weekly().weekStart(DayOfWeek.MONDAY).toRRuleString();
        assertTrue(rruleStr1.contains("WKST=MO"), "RRULE string must contain WKST=MO: " + rruleStr1);

        String rruleStr2 = RRule.weekly().weekStart(DayOfWeek.SUNDAY).toRRuleString();
        assertTrue(rruleStr2.contains("WKST=SU"), "RRULE string must contain WKST=SU: " + rruleStr2);
    }

    @Test
    void rrule_count_inJson() {
        String rruleStr = RRule.weekly().count(10).toRRuleString();
        assertTrue(rruleStr.contains("COUNT=10"), "RRULE string must contain COUNT=10: " + rruleStr);
    }

    @Test
    void rrule_interval_inJson() {
        String rruleStr = RRule.daily().interval(2).toRRuleString();
        assertTrue(rruleStr.contains("INTERVAL=2"), "RRULE string must contain INTERVAL=2: " + rruleStr);
    }

    @Test
    void rrule_until_LocalDate_inJson() {
        String rruleStr = RRule.weekly().until(LocalDate.of(2025, 12, 31)).toRRuleString();
        assertTrue(rruleStr.contains("UNTIL=20251231"), "RRULE string must contain UNTIL=20251231: " + rruleStr);
        assertFalse(rruleStr.contains("2025-12-31"), "Date must not contain dashes in RRULE string");
    }

    @Test
    void rrule_until_LocalDateTime_inJson() {
        String rruleStr = RRule.weekly().until(LocalDateTime.of(2025, 12, 31, 23, 59, 59)).toRRuleString();
        assertTrue(rruleStr.contains("UNTIL=20251231T235959"),
                "RRULE string must contain UNTIL=20251231T235959 (no dashes or colons): " + rruleStr);
    }

    @Test
    void rrule_dtstart_LocalDate_inJson() {
        String rruleStr = RRule.weekly().dtstart(LocalDate.of(2025, 1, 1)).toRRuleString();
        assertTrue(rruleStr.contains("DTSTART=20250101"), "RRULE string must contain DTSTART=20250101: " + rruleStr);
        assertFalse(rruleStr.contains("2025-01-01"), "Date must not contain dashes in RRULE string");
    }

    @Test
    void rrule_byMonthday_inJson() {
        String rruleStr = RRule.monthly().byMonthday(1, 15).toRRuleString();
        assertTrue(rruleStr.contains("BYMONTHDAY=1,15"), "RRULE string must contain BYMONTHDAY=1,15: " + rruleStr);
    }

    @Test
    void rrule_byYearday_inJson() {
        String rruleStr = RRule.yearly().byYearday(1, 100, 365).toRRuleString();
        assertTrue(rruleStr.contains("BYYEARDAY=1,100,365"), "RRULE string must contain BYYEARDAY=1,100,365: " + rruleStr);
    }

    @Test
    void rrule_byHour_byMinute_inJson() {
        String rruleStr = RRule.daily().byHour(9, 17).byMinute(0, 30).toRRuleString();
        assertTrue(rruleStr.contains("BYHOUR=9,17"), "RRULE string must contain BYHOUR=9,17: " + rruleStr);
        assertTrue(rruleStr.contains("BYMINUTE=0,30"), "RRULE string must contain BYMINUTE=0,30: " + rruleStr);
    }

    @Test
    void rrule_byWeekday_strings_passedThrough() {
        String rruleStr = RRule.monthly().byWeekday("-1fr", "2mo").toRRuleString();
        assertTrue(rruleStr.contains("BYDAY=-1FR,2MO"), "RRULE string must contain BYDAY=-1FR,2MO: " + rruleStr);
    }

    @Test
    void rrule_positionalByWeekday_serializesAsString() {
        // Object form would break FC: it resolves weekday strings via RRule[day.toUpperCase()],
        // which only knows plain MO..SU. A positional token like "-1fr" would resolve to undefined
        // and crash the plugin. Keep string form so FC's iCal parser handles it.
        JsonNode json = RRule.monthly().byWeekday("-1fr").toJson();
        assertTrue(json.isString(), "positional byweekday must fall back to StringNode");
        assertTrue(json.asString().contains("BYDAY=-1FR"));
    }

    @Test
    void rrule_plainByWeekday_serializesAsObjectWithLowercaseDays() {
        JsonNode json = RRule.weekly().byWeekday(DayOfWeek.MONDAY, DayOfWeek.FRIDAY).toJson();
        assertTrue(json.isObject(), "plain byweekday must serialize as ObjectNode");
        JsonNode days = json.get("byweekday");
        assertTrue(days.isArray());
        assertEquals("mo", days.get(0).asString());
        assertEquals("fr", days.get(1).asString());
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
        assertTrue(RRule.weekly().toRRuleString().contains("FREQ=WEEKLY"));
        assertTrue(RRule.daily().toRRuleString().contains("FREQ=DAILY"));
        assertTrue(RRule.monthly().toRRuleString().contains("FREQ=MONTHLY"));
        assertTrue(RRule.yearly().toRRuleString().contains("FREQ=YEARLY"));
    }

    // -------------------------------------------------------------------------
    // rrule in Entry JSON
    // -------------------------------------------------------------------------

    @Test
    void entry_rrule_serializedToJson() {
        Entry entry = new Entry();
        entry.setRRule(RRule.weekly().byWeekday(DayOfWeek.MONDAY));

        ObjectNode json = entry.toJson();
        assertTrue(json.hasNonNull("rrule"), "rrule key must be present in entry JSON");
        JsonNode rruleNode = json.get("rrule");
        assertTrue(rruleNode.isObject(), "structured rrule must serialize as an ObjectNode");
        assertEquals("weekly", rruleNode.get("freq").asString());
        assertTrue(rruleNode.get("byweekday").isArray(), "byweekday must be an ArrayNode");
        assertEquals("mo", rruleNode.get("byweekday").get(0).asString());
    }

    @Test
    void entry_rrule_missingDtstart_injectedFromAllDayEntryStart() {
        // Without dtstart rrule-js generates no occurrences. If the entry has a start, use it.
        Entry entry = new Entry();
        entry.setAllDay(true);
        entry.setStart(LocalDate.of(2025, 3, 3).atStartOfDay());
        entry.setRRule(RRule.weekly().byWeekday(DayOfWeek.MONDAY));

        JsonNode rruleNode = entry.toJson().get("rrule");
        assertTrue(rruleNode.isObject());
        assertEquals("2025-03-03", rruleNode.get("dtstart").asString(),
                "all-day entries must emit a date-only dtstart");
    }

    @Test
    void entry_rrule_missingDtstart_injectedFromTimedEntryStart() {
        Entry entry = new Entry();
        entry.setAllDay(false);
        entry.setStart(LocalDateTime.of(2025, 3, 3, 10, 30));
        entry.setRRule(RRule.weekly().byWeekday(DayOfWeek.MONDAY));

        JsonNode rruleNode = entry.toJson().get("rrule");
        assertTrue(rruleNode.isObject());
        assertEquals("2025-03-03T10:30:00", rruleNode.get("dtstart").asString(),
                "timed entries must emit a datetime dtstart");
    }

    @Test
    void entry_rrule_existingDtstart_notOverridden() {
        Entry entry = new Entry();
        entry.setAllDay(true);
        entry.setStart(LocalDate.of(2025, 1, 1).atStartOfDay());
        entry.setRRule(RRule.weekly().dtstart(LocalDate.of(2025, 3, 3)).byWeekday(DayOfWeek.MONDAY));

        JsonNode rruleNode = entry.toJson().get("rrule");
        assertEquals("2025-03-03", rruleNode.get("dtstart").asString(),
                "explicit dtstart on the RRule must win over the entry's start");
    }

    @Test
    void entry_rrule_rawForm_dtstartNotInjected() {
        // Raw form is a StringNode; converter must not try to add dtstart.
        Entry entry = new Entry();
        entry.setAllDay(true);
        entry.setStart(LocalDate.of(2025, 3, 3).atStartOfDay());
        entry.setRRule(RRule.ofRaw("FREQ=WEEKLY;BYDAY=MO"));

        JsonNode rruleNode = entry.toJson().get("rrule");
        assertTrue(rruleNode.isString());
        assertEquals("FREQ=WEEKLY;BYDAY=MO", rruleNode.asString());
    }

    @Test
    void entry_rrule_allDay_nonMidnightStart_emitsDateOnlyDtstart() {
        // allDay flag must win over a stray time-of-day on entry.start; FC's parser would
        // otherwise interpret the dtstart as timed and shift occurrences.
        Entry entry = new Entry();
        entry.setAllDay(true);
        entry.setStart(LocalDateTime.of(2025, 3, 3, 10, 30));
        entry.setRRule(RRule.weekly().byWeekday(DayOfWeek.MONDAY));

        JsonNode rruleNode = entry.toJson().get("rrule");
        assertEquals("2025-03-03", rruleNode.get("dtstart").asString(),
                "all-day entries must emit a date-only dtstart regardless of the start's time part");
    }

    @Test
    void entry_rrule_raw_serializedAsString() {
        Entry entry = new Entry();
        entry.setRRule(RRule.ofRaw("FREQ=WEEKLY;BYDAY=MO"));

        ObjectNode json = entry.toJson();
        assertTrue(json.hasNonNull("rrule"));
        assertTrue(json.get("rrule").isString(), "raw RRule must be a JSON string in entry JSON");
        assertEquals("FREQ=WEEKLY;BYDAY=MO", json.get("rrule").asString());
    }

    @Test
    void entry_rrule_null_notInJson() {
        Entry entry = new Entry();
        entry.setRRule(RRule.weekly());
        entry.setRRule(null);
        assertNull(entry.getRRule(), "getRRule() should return null after clearing");
        assertFalse(entry.toJson().has("rrule"), "rrule should not be in JSON after clearing");
    }

    // -------------------------------------------------------------------------
    // exrule
    // -------------------------------------------------------------------------

    @Test
    void entry_exrule_notInJson_whenNotSet() {
        Entry entry = new Entry();
        entry.setRRule(RRule.weekly());
        assertFalse(entry.toJson().has("exrule"), "exrule should not be in JSON when not set");
    }

    @Test
    void entry_exrule_singleRule_serializedAsObject() {
        RRule exclusion = RRule.daily().count(3);
        Entry entry = new Entry();
        entry.setRRule(RRule.weekly().excludeRules(exclusion));

        ObjectNode json = entry.toJson();
        assertTrue(json.hasNonNull("exrule"), "exrule must be present");
        JsonNode exruleNode = json.get("exrule");
        assertTrue(exruleNode.isObject(), "single exrule must be serialized as an ObjectNode");
        assertEquals("daily", exruleNode.get("freq").asString());
        assertEquals(3, exruleNode.get("count").asInt());
    }

    @Test
    void entry_exrule_multipleRules_serializedAsArray() {
        RRule exclusion1 = RRule.daily().count(3);
        RRule exclusion2 = RRule.monthly().byMonthday(15);
        Entry entry = new Entry();
        entry.setRRule(RRule.weekly().excludeRules(exclusion1, exclusion2));

        ObjectNode json = entry.toJson();
        assertTrue(json.hasNonNull("exrule"), "exrule must be present");
        assertTrue(json.get("exrule").isArray(), "multiple exrules must be serialized as ArrayNode");
        ArrayNode array = (ArrayNode) json.get("exrule");
        assertEquals(2, array.size());
        assertTrue(array.get(0).isObject(), "each exrule element must be an ObjectNode");
        assertEquals("daily", array.get(0).get("freq").asString());
        assertTrue(array.get(1).isObject());
        assertEquals("monthly", array.get(1).get("freq").asString());
    }

    @Test
    void entry_exrule_transferredFromRRule() {
        RRule exclusion = RRule.daily();
        RRule rule = RRule.weekly().excludeRules(exclusion);
        Entry entry = new Entry();
        entry.setRRule(rule);

        // Verify it's transferred by checking JSON output
        assertTrue(entry.toJson().hasNonNull("exrule"));
    }

    @Test
    void entry_exrule_clearedWhenRRuleNull() {
        Entry entry = new Entry();
        entry.setRRule(RRule.weekly().excludeRules(RRule.daily()));
        entry.setRRule(null);

        assertFalse(entry.toJson().has("exrule"), "exrule should not be in JSON after clearing RRule");
    }

    @Test
    void excludeRules_rejectsRawRRule_varargs() {
        // FC's rrule plugin only accepts exrule as a structured object. An ofRaw rule would
        // serialize as a String and reintroduce the "Invalid options: 0, 1, ..." crash that
        // breaks all event rendering. Fail fast at the API boundary instead.
        RRule main = RRule.weekly();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> main.excludeRules(RRule.ofRaw("FREQ=DAILY;COUNT=3")));
        assertTrue(ex.getMessage().contains("ofRaw"),
                "message should mention ofRaw so the caller knows what to change: " + ex.getMessage());
    }

    @Test
    void excludeRules_rejectsRawRRule_list() {
        RRule main = RRule.weekly();
        assertThrows(IllegalArgumentException.class,
                () -> main.excludeRules(List.of(RRule.daily(), RRule.ofRaw("FREQ=DAILY"))));
    }

    @Test
    void excludeRules_acceptsStructuredRRules() {
        // Baseline: non-raw rules still work and are stored on the main RRule.
        RRule main = RRule.weekly().excludeRules(RRule.daily().count(3), RRule.monthly().byMonthday(15));
        assertEquals(2, main.getExcludedRules().size());
    }

    // -------------------------------------------------------------------------
    // isRecurring() with RRule
    // -------------------------------------------------------------------------

    @Test
    void entry_isRecurring_true_whenRRuleSet() {
        Entry entry = new Entry();
        entry.setRRule(RRule.weekly());
        assertTrue(entry.isRecurring(), "isRecurring should be true when RRule is set");
    }

    @Test
    void entry_isRecurring_false_afterRRuleCleared() {
        Entry entry = new Entry();
        entry.setRRule(RRule.weekly());
        entry.setRRule(null);
        assertFalse(entry.isRecurring(), "isRecurring should be false after RRule cleared");
    }

    @Test
    void entry_isRecurring_true_withBuiltinRecurrence() {
        Entry entry = new Entry();
        entry.setRecurringDaysOfWeek(java.util.Set.of(java.time.DayOfWeek.MONDAY));
        assertTrue(entry.isRecurring(), "isRecurring should be true for built-in recurrence");
    }

    // -------------------------------------------------------------------------
    // Entry.constraint — per-entry serialization
    // -------------------------------------------------------------------------

    @Test
    void constraint_default_notInJson() {
        Entry entry = new Entry();
        assertFalse(entry.toJson().has("constraint"), "constraint should not be in JSON when not set");
    }

    @Test
    void constraint_string_serializedToJson() {
        Entry entry = new Entry();
        entry.setConstraint("myGroupId");
        ObjectNode json = entry.toJson();
        assertTrue(json.has("constraint"), "constraint should be in JSON");
        assertEquals("myGroupId", json.get("constraint").asString());
    }

    @Test
    void constraint_businessHours_serializedToJson() {
        Entry entry = new Entry();
        entry.setConstraint(BusinessHours.businessWeek().start(9).end(17));
        ObjectNode json = entry.toJson();
        assertTrue(json.has("constraint"), "constraint should be in JSON");
        assertTrue(json.get("constraint").isObject(), "BusinessHours constraint should be an object");
    }

    @Test
    void constraint_setToBusinessHours_serializedAsString() {
        Entry entry = new Entry();
        entry.setConstraintToBusinessHours();
        ObjectNode json = entry.toJson();
        assertTrue(json.has("constraint"), "constraint should be in JSON");
        assertEquals("businessHours", json.get("constraint").asString());
    }

    @Test
    void constraint_null_notInJson() {
        Entry entry = new Entry();
        entry.setConstraint("myGroup");
        entry.setConstraint((String) null);
        assertFalse(entry.toJson().has("constraint"), "constraint should not be in JSON after clearing");
    }

    // --- Display mode JSON key tests (mutation testing revealed gap) ---

    @Test
    void displayMode_jsonKey_isDisplay() {
        // Verifies @JsonName("display") annotation produces correct key
        Entry entry = new Entry();
        entry.setDisplayMode(DisplayMode.BACKGROUND);
        ObjectNode json = entry.toJson();
        assertTrue(json.has("display"), "JSON key must be 'display' (from @JsonName)");
        assertEquals("background", json.get("display").asString(),
                "display value must be hardcoded 'background'");
    }

    @Test
    void displayMode_notSet_notInJson() {
        // an entry without its own display mode follows the calendar's eventDisplay, so it must not send one
        ObjectNode json = new Entry().toJson();
        assertFalse(json.has("display"));
    }

    @Test
    void displayModeConverter_readsAMissingOrUnknownValueAsNotSet() {
        DisplayModeConverter converter = new DisplayModeConverter();
        assertNull(converter.toServerModel(null, null));
        assertNull(converter.toServerModel(JsonFactory.createNull(), null));
        assertNull(converter.toServerModel(JsonFactory.create(""), null));
        assertNull(converter.toServerModel(JsonFactory.create("unknown"), null));
        assertEquals(DisplayMode.AUTO, converter.toServerModel(JsonFactory.create("auto"), null));
        assertEquals(DisplayMode.NONE, converter.toServerModel(JsonFactory.create("none"), null));
    }

    @Test
    void displayMode_setToNull_isNotSentAnymore() {
        Entry entry = new Entry();
        entry.setDisplayMode(DisplayMode.BACKGROUND);
        entry.setDisplayMode(null);
        assertFalse(entry.toJson().has("display"));
    }

    @Test
    void displayMode_auto_isSentAsAnExplicitValue() {
        Entry entry = new Entry();
        entry.setDisplayMode(DisplayMode.AUTO);
        assertEquals("auto", entry.toJson().get("display").asString());
    }

    @Test
    void displayMode_none_isSentAsNone() {
        Entry entry = new Entry();
        entry.setDisplayMode(DisplayMode.NONE);
        assertEquals("none", entry.toJson().get("display").asString());
    }
}
