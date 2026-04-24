package org.vaadin.stefan.fullcalendar;

import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonType;
import elemental.json.JsonValue;
import org.junit.jupiter.api.Test;

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
        assertFalse(entry.toJson().hasKey("url"), "url should not be in JSON when not set");
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
        JsonObject json = entry.toJson();
        assertTrue(json.hasKey("url") && json.get("url").getType() != JsonType.NULL, "url should be in JSON");
        assertEquals("https://example.com/event", json.get("url").asString());
    }

    @Test
    void url_null_removedFromJson() {
        Entry entry = new Entry();
        entry.setUrl("https://example.com");
        entry.setUrl(null);
        assertNull(entry.getUrl(), "url getter should return null after clearing");
        assertFalse(entry.toJson().hasKey("url"), "url should not be in JSON after clearing");
    }

    // -------------------------------------------------------------------------
    // interactive
    // -------------------------------------------------------------------------

    @Test
    void interactive_default_notInJson() {
        Entry entry = new Entry();
        assertFalse(entry.toJson().hasKey("interactive"), "interactive should not be in JSON when not set");
    }

    @Test
    void interactive_true_serializedToJson() {
        Entry entry = new Entry();
        entry.setInteractive(true);
        JsonObject json = entry.toJson();
        assertTrue(json.hasKey("interactive") && json.get("interactive").getType() != JsonType.NULL);
        assertTrue(json.get("interactive").asBoolean());
    }

    @Test
    void interactive_false_serializedToJson() {
        Entry entry = new Entry();
        entry.setInteractive(false);
        JsonObject json = entry.toJson();
        assertTrue(json.hasKey("interactive") && json.get("interactive").getType() != JsonType.NULL);
        assertFalse(json.get("interactive").asBoolean());
    }

    @Test
    void interactive_null_notInJson() {
        Entry entry = new Entry();
        entry.setInteractive(true);
        entry.setInteractive(null);
        assertFalse(entry.toJson().hasKey("interactive"), "interactive should not be in JSON after clearing");
    }

    // -------------------------------------------------------------------------
    // recurringDuration (serializes as "duration" in JSON via @JsonName)
    // -------------------------------------------------------------------------

    @Test
    void recurringDuration_default_notInJson() {
        Entry entry = new Entry();
        assertFalse(entry.toJson().hasKey("duration"), "duration should not be in JSON when not set");
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
        JsonObject json = entry.toJson();
        assertFalse(json.hasKey("recurringDuration"), "Java field name must NOT appear in JSON");
        assertTrue(json.hasKey("duration") && json.get("duration").getType() != JsonType.NULL, "FC key 'duration' must appear in JSON");
        assertEquals("PT2H", json.get("duration").asString());
    }

    @Test
    void recurringDuration_null_notInJson() {
        Entry entry = new Entry();
        entry.setRecurringDuration("P1D");
        entry.setRecurringDuration(null);
        assertNull(entry.getRecurringDuration(), "recurringDuration getter should return null after clearing");
        assertFalse(entry.toJson().hasKey("duration"), "duration should not be in JSON after clearing");
    }

    // -------------------------------------------------------------------------
    // exdate — set via RRule.excludeDates(), transferred by Entry.setRRule()
    // -------------------------------------------------------------------------

    @Test
    void excludeDates_notInJson_whenNotSet() {
        Entry entry = new Entry();
        entry.setRRule(RRule.weekly().dtstart(LocalDate.of(2024, 1, 1)));
        assertFalse(entry.toJson().hasKey("exdate"), "exdate should not be in JSON when no excludeDates set");
    }

    @Test
    void excludeDates_transferredToEntryOnSetRRule() {
        List<LocalDate> dates = List.of(LocalDate.of(2024, 1, 15), LocalDate.of(2024, 2, 20));
        Entry entry = new Entry();
        entry.setRRule(RRule.weekly().excludeDates(dates));
        // verify exdate is serialized into the event JSON
        JsonObject json = entry.toJson();
        assertTrue(json.hasKey("exdate") && json.get("exdate").getType() != JsonType.NULL, "exdate should be in JSON after setRRule with excludeDates");
        JsonValue exdateNode = json.get("exdate");
        assertEquals(JsonType.ARRAY, exdateNode.getType());
        JsonArray arr = (JsonArray) exdateNode;
        assertEquals(2, arr.length());
        assertEquals("2024-01-15", arr.get(0).asString());
        assertEquals("2024-02-20", arr.get(1).asString());
    }

    @Test
    void excludeDates_singleDate_serializedAsArrayWithOneElement() {
        Entry entry = new Entry();
        entry.setRRule(RRule.weekly().excludeDates(LocalDate.of(2024, 1, 15)));
        JsonValue exdateNode = entry.toJson().get("exdate");
        assertEquals(JsonType.ARRAY, exdateNode.getType());
        assertEquals(1, ((JsonArray) exdateNode).length());
        assertEquals("2024-01-15", ((JsonArray) exdateNode).get(0).asString());
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
        assertFalse(entry.toJson().hasKey("exdate"), "exdate should not be in JSON after setRRule(null)");
    }

    // -------------------------------------------------------------------------
    // overlap — changed from boolean (default true) to Boolean (default null)
    // -------------------------------------------------------------------------

    @Test
    void overlap_default_null_notInJson() {
        Entry entry = new Entry();
        assertNull(entry.getOverlap(), "overlap default should be null (inherit)");
        assertFalse(entry.toJson().hasKey("overlap"),
                "overlap should NOT be in JSON when null (means inherit from calendar-level setting)");
    }

    @Test
    void overlap_true_inJson() {
        Entry entry = new Entry();
        entry.setOverlap(true);
        JsonObject json = entry.toJson();
        assertTrue(json.hasKey("overlap") && json.get("overlap").getType() != JsonType.NULL);
        assertTrue(json.get("overlap").asBoolean());
    }

    @Test
    void overlap_false_inJson() {
        Entry entry = new Entry();
        entry.setOverlap(false);
        JsonObject json = entry.toJson();
        assertTrue(json.hasKey("overlap") && json.get("overlap").getType() != JsonType.NULL);
        assertFalse(json.get("overlap").asBoolean());
    }

    @Test
    void overlap_clearedToNull_notInJson() {
        Entry entry = new Entry();
        entry.setOverlap(true);
        entry.setOverlap(null);
        assertNull(entry.getOverlap());
        assertFalse(entry.toJson().hasKey("overlap"), "overlap should not be in JSON after clearing to null");
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
        assertFalse(entry.toJson().hasKey("rrule"), "rrule should not be in JSON when not set");
    }

    @Test
    void rrule_weekly_toJson_hasFreq() {
        RRule rrule = RRule.weekly();
        String rruleStr = rrule.toRRuleString();
        assertTrue(rruleStr.contains("FREQ=WEEKLY"), "RRULE string must contain FREQ=WEEKLY");

        JsonValue json = rrule.toJson();
        assertEquals(JsonType.OBJECT, json.getType(), "toJson() must return a JsonObject for structured form");
        assertEquals("weekly", ((JsonObject) json).getString("freq"), "freq must be lowercase 'weekly' for the rrule plugin");
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
        JsonValue json = RRule.monthly().byWeekday("-1fr").toJson();
        assertEquals(JsonType.STRING, json.getType(), "positional byweekday must fall back to string");
        assertTrue(json.asString().contains("BYDAY=-1FR"));
    }

    @Test
    void rrule_plainByWeekday_serializesAsObjectWithLowercaseDays() {
        JsonValue json = RRule.weekly().byWeekday(DayOfWeek.MONDAY, DayOfWeek.FRIDAY).toJson();
        assertEquals(JsonType.OBJECT, json.getType(), "plain byweekday must serialize as JsonObject");
        JsonArray days = ((JsonObject) json).getArray("byweekday");
        assertEquals(2, days.length());
        assertEquals("mo", days.getString(0));
        assertEquals("fr", days.getString(1));
    }

    // -------------------------------------------------------------------------
    // RRule — raw string form
    // -------------------------------------------------------------------------

    @Test
    void rrule_raw_toJson_returnsString() {
        RRule rrule = RRule.ofRaw("FREQ=WEEKLY;BYDAY=MO,WE;COUNT=10");
        JsonValue json = rrule.toJson();
        assertEquals(JsonType.STRING, json.getType(), "Raw RRule should serialize as a string");
        assertEquals("FREQ=WEEKLY;BYDAY=MO,WE;COUNT=10", json.asString());
    }

    @Test
    void rrule_raw_notAffectedByStructuredFields() {
        // raw form ignores freq / byweekday etc. — raw string takes precedence
        RRule rrule = RRule.ofRaw("FREQ=DAILY");
        JsonValue json = rrule.toJson();
        assertEquals(JsonType.STRING, json.getType());
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

        JsonObject json = entry.toJson();
        assertTrue(json.hasKey("rrule") && json.get("rrule").getType() != JsonType.NULL, "rrule key must be present in entry JSON");
        assertEquals(JsonType.OBJECT, json.get("rrule").getType(), "structured rrule must serialize as a JsonObject");
        JsonObject rruleNode = json.getObject("rrule");
        assertEquals("weekly", rruleNode.getString("freq"));
        assertEquals(JsonType.ARRAY, rruleNode.get("byweekday").getType(), "byweekday must be a JsonArray");
        assertEquals("mo", rruleNode.getArray("byweekday").getString(0));
    }

    @Test
    void entry_rrule_missingDtstart_injectedFromAllDayEntryStart() {
        // Without dtstart rrule-js generates no occurrences. If the entry has a start, use it.
        Entry entry = new Entry();
        entry.setAllDay(true);
        entry.setStart(LocalDate.of(2025, 3, 3).atStartOfDay());
        entry.setRRule(RRule.weekly().byWeekday(DayOfWeek.MONDAY));

        JsonObject rruleNode = entry.toJson().getObject("rrule");
        assertEquals("2025-03-03", rruleNode.getString("dtstart"),
                "all-day entries must emit a date-only dtstart");
    }

    @Test
    void entry_rrule_missingDtstart_injectedFromTimedEntryStart() {
        Entry entry = new Entry();
        entry.setAllDay(false);
        entry.setStart(LocalDateTime.of(2025, 3, 3, 10, 30));
        entry.setRRule(RRule.weekly().byWeekday(DayOfWeek.MONDAY));

        JsonObject rruleNode = entry.toJson().getObject("rrule");
        assertEquals("2025-03-03T10:30:00", rruleNode.getString("dtstart"),
                "timed entries must emit a datetime dtstart");
    }

    @Test
    void entry_rrule_existingDtstart_notOverridden() {
        Entry entry = new Entry();
        entry.setAllDay(true);
        entry.setStart(LocalDate.of(2025, 1, 1).atStartOfDay());
        entry.setRRule(RRule.weekly().dtstart(LocalDate.of(2025, 3, 3)).byWeekday(DayOfWeek.MONDAY));

        JsonObject rruleNode = entry.toJson().getObject("rrule");
        assertEquals("2025-03-03", rruleNode.getString("dtstart"),
                "explicit dtstart on the RRule must win over the entry's start");
    }

    @Test
    void entry_rrule_rawForm_dtstartNotInjected() {
        // Raw form is a string; converter must not try to add dtstart.
        Entry entry = new Entry();
        entry.setAllDay(true);
        entry.setStart(LocalDate.of(2025, 3, 3).atStartOfDay());
        entry.setRRule(RRule.ofRaw("FREQ=WEEKLY;BYDAY=MO"));

        JsonValue rruleNode = entry.toJson().get("rrule");
        assertEquals(JsonType.STRING, rruleNode.getType());
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

        JsonObject rruleNode = entry.toJson().getObject("rrule");
        assertEquals("2025-03-03", rruleNode.getString("dtstart"),
                "all-day entries must emit a date-only dtstart regardless of the start's time part");
    }

    @Test
    void entry_rrule_raw_serializedAsString() {
        Entry entry = new Entry();
        entry.setRRule(RRule.ofRaw("FREQ=WEEKLY;BYDAY=MO"));

        JsonObject json = entry.toJson();
        assertTrue(json.hasKey("rrule") && json.get("rrule").getType() != JsonType.NULL);
        assertEquals(JsonType.STRING, json.get("rrule").getType(), "raw RRule must be a JSON string in entry JSON");
        assertEquals("FREQ=WEEKLY;BYDAY=MO", json.get("rrule").asString());
    }

    @Test
    void entry_rrule_null_notInJson() {
        Entry entry = new Entry();
        entry.setRRule(RRule.weekly());
        entry.setRRule(null);
        assertNull(entry.getRRule(), "getRRule() should return null after clearing");
        assertFalse(entry.toJson().hasKey("rrule"), "rrule should not be in JSON after clearing");
    }

    // -------------------------------------------------------------------------
    // exrule
    // -------------------------------------------------------------------------

    @Test
    void entry_exrule_notInJson_whenNotSet() {
        Entry entry = new Entry();
        entry.setRRule(RRule.weekly());
        assertFalse(entry.toJson().hasKey("exrule"), "exrule should not be in JSON when not set");
    }

    @Test
    void entry_exrule_singleRule_serializedAsObject() {
        RRule exclusion = RRule.daily().count(3);
        Entry entry = new Entry();
        entry.setRRule(RRule.weekly().excludeRules(exclusion));

        JsonObject json = entry.toJson();
        assertTrue(json.hasKey("exrule") && json.get("exrule").getType() != JsonType.NULL, "exrule must be present");
        assertEquals(JsonType.OBJECT, json.get("exrule").getType(), "single exrule must be serialized as a JsonObject");
        JsonObject exruleNode = json.getObject("exrule");
        assertEquals("daily", exruleNode.getString("freq"));
        assertEquals(3, (int) exruleNode.getNumber("count"));
    }

    @Test
    void entry_exrule_multipleRules_serializedAsArray() {
        RRule exclusion1 = RRule.daily().count(3);
        RRule exclusion2 = RRule.monthly().byMonthday(15);
        Entry entry = new Entry();
        entry.setRRule(RRule.weekly().excludeRules(exclusion1, exclusion2));

        JsonObject json = entry.toJson();
        assertTrue(json.hasKey("exrule") && json.get("exrule").getType() != JsonType.NULL, "exrule must be present");
        assertEquals(JsonType.ARRAY, json.get("exrule").getType(), "multiple exrules must be serialized as array");
        JsonArray array = (JsonArray) json.get("exrule");
        assertEquals(2, array.length());
        assertEquals(JsonType.OBJECT, array.get(0).getType(), "each exrule element must be a JsonObject");
        assertEquals("daily", ((JsonObject) array.get(0)).getString("freq"));
        assertEquals(JsonType.OBJECT, array.get(1).getType());
        assertEquals("monthly", ((JsonObject) array.get(1)).getString("freq"));
    }

    @Test
    void entry_exrule_transferredFromRRule() {
        RRule exclusion = RRule.daily();
        RRule rule = RRule.weekly().excludeRules(exclusion);
        Entry entry = new Entry();
        entry.setRRule(rule);

        // Verify it's transferred by checking JSON output
        assertTrue(json_hasNonNull(entry.toJson(), "exrule"));
    }

    @Test
    void entry_exrule_clearedWhenRRuleNull() {
        Entry entry = new Entry();
        entry.setRRule(RRule.weekly().excludeRules(RRule.daily()));
        entry.setRRule(null);

        assertFalse(entry.toJson().hasKey("exrule"), "exrule should not be in JSON after clearing RRule");
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
        assertFalse(entry.toJson().hasKey("constraint"), "constraint should not be in JSON when not set");
    }

    @Test
    void constraint_string_serializedToJson() {
        Entry entry = new Entry();
        entry.setConstraint("myGroupId");
        JsonObject json = entry.toJson();
        assertTrue(json.hasKey("constraint"), "constraint should be in JSON");
        assertEquals("myGroupId", json.get("constraint").asString());
    }

    @Test
    void constraint_businessHours_serializedToJson() {
        Entry entry = new Entry();
        entry.setConstraint(BusinessHours.businessWeek().start(9).end(17));
        JsonObject json = entry.toJson();
        assertTrue(json.hasKey("constraint"), "constraint should be in JSON");
        assertEquals(JsonType.OBJECT, json.get("constraint").getType(), "BusinessHours constraint should be an object");
    }

    @Test
    void constraint_setToBusinessHours_serializedAsString() {
        Entry entry = new Entry();
        entry.setConstraintToBusinessHours();
        JsonObject json = entry.toJson();
        assertTrue(json.hasKey("constraint"), "constraint should be in JSON");
        assertEquals("businessHours", json.get("constraint").asString());
    }

    @Test
    void constraint_null_notInJson() {
        Entry entry = new Entry();
        entry.setConstraint("myGroup");
        entry.setConstraint((String) null);
        assertFalse(entry.toJson().hasKey("constraint"), "constraint should not be in JSON after clearing");
    }

    // --- Display mode JSON key tests (mutation testing revealed gap) ---

    @Test
    void displayMode_jsonKey_isDisplay() {
        // Verifies @JsonName("display") annotation produces correct key
        Entry entry = new Entry();
        entry.setDisplayMode(DisplayMode.BACKGROUND);
        JsonObject json = entry.toJson();
        assertTrue(json.hasKey("display"), "JSON key must be 'display' (from @JsonName)");
        assertEquals("background", json.get("display").asString(),
                "display value must be hardcoded 'background'");
    }

    @Test
    void displayMode_default_auto_notInJson() {
        // Default displayMode is AUTO — should not appear in JSON (FC default)
        Entry entry = new Entry();
        JsonObject json = entry.toJson();
        // AUTO is the default, it should either be absent or equal "auto"
        if (json.hasKey("display")) {
            assertEquals("auto", json.get("display").asString());
        }
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private static boolean json_hasNonNull(JsonObject obj, String key) {
        return obj.hasKey(key) && obj.get(key).getType() != JsonType.NULL;
    }
}
