package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.converters.ExdateConverter;
import org.vaadin.stefan.fullcalendar.converters.ExruleConverter;
import tools.jackson.databind.JsonNode;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Reading an {@link RRule} back from the JSON it writes, and its equality. */
class RRuleJsonTest {

    private static RRule full() {
        return RRule.weekly().interval(2).byWeekday("mo", "we").byMonth(1, 2).byMonthday(1, -1).byYearday(5)
                .byHour(8).byMinute(30).weekStart("mo").dtstart(LocalDateTime.of(2026, 1, 5, 8, 0))
                .until(LocalDate.of(2026, 12, 31));
    }

    private static RRule roundTrip(RRule rule) {
        return RRule.fromJson(rule.toJson(), JsonUtils.toJsonNode(null), null);
    }

    @Test
    void objectForm_roundTrips() {
        assertEquals(full(), roundTrip(full()));
        assertEquals(RRule.daily().count(3), roundTrip(RRule.daily().count(3)));
        assertEquals(RRule.Frequency.WEEKLY, roundTrip(full()).getFreq());
        assertEquals(List.of("mo", "we"), roundTrip(full()).getByweekday());
        assertEquals("2026-01-05T08:00:00", roundTrip(full()).getDtstart());
    }

    @Test
    void stringForm_roundTrips() {
        RRule raw = RRule.ofRaw("FREQ=WEEKLY;BYDAY=MO,WE;UNTIL=20261231");
        assertEquals(raw, roundTrip(raw));
        assertEquals(raw.getRawRRule(), roundTrip(raw).getRawRRule());
        // a positional weekday is written as a string, which is read back as a raw rule
        RRule positional = RRule.monthly().byWeekday("-1fr").dtstart(LocalDate.of(2026, 1, 1));
        assertTrue(positional.toJson().isString());
        assertEquals(positional, roundTrip(positional));
    }

    @Test
    void excludedDatesAndRules_roundTrip() {
        RRule rule = RRule.daily().dtstart(LocalDate.of(2026, 1, 5))
                .excludeDates(LocalDate.of(2026, 1, 7), LocalDate.of(2026, 1, 9))
                .excludeRules(RRule.weekly().byWeekday("sa"), RRule.monthly().byWeekday("-1fr"));
        JsonNode exdate = new ExdateConverter<Entry>()
                .toClientModel(rule.getExcludedDates(), null);
        JsonNode exrule = new ExruleConverter<Entry>()
                .toClientModel(rule.getExcludedRules(), null);
        assertEquals(rule, RRule.fromJson(rule.toJson(), exdate, exrule));
        assertNotEquals(rule, RRule.fromJson(rule.toJson(), null, exrule));
        assertNotEquals(rule, RRule.fromJson(rule.toJson(), exdate, null));
    }

    @Test
    void excludedRule_withPositionalWeekday_isWrittenAsObject() {
        RRule rule = RRule.weekly().byWeekday(DayOfWeek.FRIDAY).dtstart(LocalDate.of(2026, 1, 2))
                .excludeRules(RRule.monthly().byWeekday("-1fr", "mo").dtstart(LocalDate.of(2026, 1, 1)));
        JsonNode exrule = new ExruleConverter<Entry>().toClientModel(rule.getExcludedRules(), null);

        assertTrue(exrule.isObject());
        JsonNode byweekday = exrule.get("byweekday");
        assertEquals(2, byweekday.size());
        assertEquals(4, byweekday.get(0).get("weekday").asInt());
        assertEquals(-1, byweekday.get(0).get("n").asInt());
        assertEquals("mo", byweekday.get(1).asString());
        // the main rule keeps its form
        assertTrue(rule.toJson().isObject());
        // read back to an equal rule
        assertEquals(rule, RRule.fromJson(rule.toJson(), null, exrule));
    }

    @Test
    void excludedRule_positionalWeekdays_mondaySundayAndPositivePosition() {
        assertEquals("{\"weekday\":6,\"n\":2}", weekdayOf("2su"));
        assertEquals("{\"weekday\":0,\"n\":1}", weekdayOf("1mo"));
    }

    private static String weekdayOf(String token) {
        RRule rule = RRule.weekly().excludeRules(RRule.monthly().byWeekday(token));
        JsonNode exrule = new ExruleConverter<Entry>().toClientModel(rule.getExcludedRules(), null);
        assertEquals(rule, RRule.fromJson(RRule.weekly().toJson(), null, exrule));
        return exrule.get("byweekday").get(0).toString();
    }

    @Test
    void excludedRule_malformedWeekdayObject_isRejected() {
        for (var weekday : List.of(JsonFactory.createObject().put("weekday", 7).put("n", 1),
                JsonFactory.createObject().put("weekday", 0),
                JsonFactory.createObject().put("weekday", 0).put("n", 1).put("x", 1),
                JsonFactory.createObject().put("weekday", -1).put("n", 1),
                JsonFactory.createObject().put("weekday", 0).put("n", "x"),
                JsonUtils.toJsonNode(3))) {
            JsonNode exrule = JsonFactory.createObject().put("freq", "monthly")
                    .set("byweekday", JsonFactory.createArray().add(weekday));
            assertThrows(IllegalArgumentException.class, () -> RRule.fromJson(RRule.weekly().toJson(), null, exrule),
                    weekday.toString());
        }
    }

    @Test
    void severalExcludedRules_eachInObjectForm_roundTrip() {
        RRule rule = RRule.weekly().byWeekday("fr").dtstart(LocalDate.of(2025, 3, 1))
                .excludeRules(RRule.monthly().byWeekday("-1fr"), RRule.weekly().byWeekday("sa"));
        JsonNode exrule = new ExruleConverter<Entry>().toClientModel(rule.getExcludedRules(), null);

        assertTrue(exrule.isArray());
        assertEquals(2, exrule.size());
        assertTrue(exrule.get(0).isObject());
        assertTrue(exrule.get(1).isObject());
        assertEquals(-1, exrule.get(0).get("byweekday").get(0).get("n").asInt());
        assertEquals(rule, RRule.fromJson(rule.toJson(), null, exrule));
    }

    @Test
    void excludedRule_asString_isRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> RRule.fromJson(RRule.weekly().toJson(), null, JsonUtils.toJsonNode("FREQ=MONTHLY")));
    }

    @Test
    void toExruleJson_ofRawRule_throws() {
        assertThrows(IllegalArgumentException.class, () -> RRule.ofRaw("FREQ=DAILY").toExruleJson());
    }

    @Test
    void mainRule_withPositionalWeekday_staysAString() {
        assertTrue(RRule.monthly().byWeekday("-1fr").toJson().isString());
    }

    @Test
    void equals_comparesWhatTheClientGets() {
        assertEquals(full(), full());
        assertEquals(full().hashCode(), full().hashCode());
        assertNotEquals(full(), full().interval(3));
        assertNotEquals(RRule.daily(), RRule.weekly());
        assertNotEquals(RRule.daily(), null);
        assertNotEquals(RRule.daily(), "x");
        // empty lists are not sent, so they equal none
        assertEquals(RRule.daily(), RRule.daily().excludeDates(List.of()).excludeRules(List.of()));
        assertEquals(RRule.daily().hashCode(), RRule.daily().excludeDates(List.of()).hashCode());
        // dtstart as a string or as a date writes the same
        assertEquals(RRule.daily().dtstart("2026-01-05"), RRule.daily().dtstart(LocalDate.of(2026, 1, 5)));
        assertNotEquals(RRule.daily().excludeDates(LocalDate.of(2026, 1, 5)), RRule.daily());
        assertNotEquals(RRule.daily().excludeRules(RRule.weekly()), RRule.daily());
    }

    @Test
    void badJson_isRejected() {
        assertThrows(IllegalArgumentException.class, () -> RRule.fromJson(null));
        assertThrows(IllegalArgumentException.class, () -> RRule.fromJson(JsonUtils.toJsonNode(5)));
        assertThrows(IllegalArgumentException.class, () -> RRule.fromJson(JsonFactory.createArray()));
        assertThrows(IllegalArgumentException.class,
                () -> RRule.fromJson(JsonFactory.createObject().put("freq", "hourly")));
        assertThrows(IllegalArgumentException.class,
                () -> RRule.fromJson(JsonFactory.createObject().put("freq", "daily").put("bysetpos", 1)));
        assertThrows(IllegalArgumentException.class,
                () -> RRule.fromJson(JsonFactory.createObject().put("count", "3")));
        assertThrows(IllegalArgumentException.class,
                () -> RRule.fromJson(JsonFactory.createObject().put("count", 3.5)));
        assertThrows(IllegalArgumentException.class,
                () -> RRule.fromJson(JsonFactory.createObject().put("byweekday", "mo")));
        assertThrows(IllegalArgumentException.class, () -> RRule.fromJson(JsonFactory.createObject()
                .set("bymonth", JsonFactory.createArray().add("x"))));
    }

    @Test
    void copy_keepsTheFieldsAndIsIndependent() {
        RRule positional = RRule.monthly().byWeekday("-1fr").dtstart(LocalDate.of(2026, 1, 1))
                .excludeDates(LocalDate.of(2026, 1, 30)).excludeRules(RRule.weekly().byWeekday("sa"));
        RRule copy = positional.copy();

        assertEquals(positional, copy);
        assertNotSame(positional, copy);
        // a positional weekday is written as a string, the copy still keeps the structured fields
        assertEquals(RRule.Frequency.MONTHLY, copy.getFreq());
        assertEquals(List.of("-1fr"), copy.getByweekday());

        copy.getExcludedDates().add(LocalDate.of(2026, 2, 27));
        copy.getExcludedRules().getFirst().interval(2);
        assertEquals(List.of(LocalDate.of(2026, 1, 30)), positional.getExcludedDates());
        assertNull(positional.getExcludedRules().getFirst().getInterval());
    }
}
