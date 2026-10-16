package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.converters.ExdateConverter;
import org.vaadin.stefan.fullcalendar.converters.ExruleConverter;
import tools.jackson.databind.JsonNode;

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
