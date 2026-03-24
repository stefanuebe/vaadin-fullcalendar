package org.vaadin.stefan.fullcalendar;

import elemental.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EntryModelExtensionTest {

    // --- New simple properties ---

    @Test
    void urlSerializedToJson() {
        Entry entry = new Entry();
        entry.setUrl("https://example.com");
        JsonObject json = entry.toJson();
        assertEquals("https://example.com", json.getString("url"));
    }

    @Test
    void interactiveNullNotSerialized() {
        Entry entry = new Entry();
        entry.setInteractive(null);
        JsonObject json = entry.toJson();
        assertFalse(json.hasKey("interactive"));
    }

    @Test
    void interactiveTrueSerialized() {
        Entry entry = new Entry();
        entry.setInteractive(true);
        JsonObject json = entry.toJson();
        assertTrue(json.getBoolean("interactive"));
    }

    @Test
    void recurringDurationSerialized() {
        Entry entry = new Entry();
        entry.setRecurringDuration("P2D");
        JsonObject json = entry.toJson();
        assertEquals("P2D", json.getString("duration"));
    }

    // --- Overlap migration ---

    @Test
    void overlapDefaultIsNull() {
        Entry entry = new Entry();
        assertNull(entry.getOverlap());
    }

    @Test
    void overlapNullNotSerialized() {
        Entry entry = new Entry();
        JsonObject json = entry.toJson();
        assertFalse(json.hasKey("overlap"));
    }

    @Test
    void isOverlapReturnsTrueWhenNull() {
        Entry entry = new Entry();
        assertTrue(entry.isOverlap());
    }

    @Test
    void isOverlapReturnsFalseWhenSetToFalse() {
        Entry entry = new Entry();
        entry.setOverlap(Boolean.FALSE);
        assertFalse(entry.isOverlap());
    }

    @Test
    void overlapFalseSerializedAsBoolean() {
        Entry entry = new Entry();
        entry.setOverlap(false);
        JsonObject json = entry.toJson();
        assertTrue(json.hasKey("overlap"));
        assertFalse(json.getBoolean("overlap"));
    }

    @Test
    void overlapTrueSerializedAsBoolean() {
        Entry entry = new Entry();
        entry.setOverlap(true);
        JsonObject json = entry.toJson();
        assertTrue(json.getBoolean("overlap"));
    }

    @SuppressWarnings("deprecation")
    @Test
    void isOverlapAllowedDelegatesToIsOverlap() {
        Entry entry = new Entry();
        entry.setOverlap(Boolean.FALSE);
        assertFalse(entry.isOverlapAllowed());
    }

    @SuppressWarnings("deprecation")
    @Test
    void setOverlapAllowedDelegatesToSetOverlap() {
        Entry entry = new Entry();
        entry.setOverlapAllowed(false);
        assertEquals(Boolean.FALSE, entry.getOverlap());
    }

    // --- RRule ---

    @Test
    void rruleStructuredFormSerializesToJsonObject() {
        Entry entry = new Entry();
        RRule rule = RRule.weekly().byWeekday(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);
        entry.setRrule(rule);
        JsonObject json = entry.toJson();
        assertTrue(json.hasKey("rrule"));
        // Structured form must be a JSON object (not a string) for exdate/exrule support
        JsonObject rruleJson = json.getObject("rrule");
        assertEquals("weekly", rruleJson.getString("freq"));
    }

    @Test
    void rruleRawFormSerializesToString() {
        Entry entry = new Entry();
        RRule rule = RRule.ofRaw("FREQ=MONTHLY;BYDAY=-1FR");
        entry.setRrule(rule);
        JsonObject json = entry.toJson();
        assertTrue(json.hasKey("rrule"));
        assertEquals("FREQ=MONTHLY;BYDAY=-1FR", json.getString("rrule"));
    }

    @Test
    void rruleWithExdateTransfersToEntryAndSerializes() {
        Entry entry = new Entry();
        RRule rule = RRule.daily()
                .dtstart(LocalDate.of(2026, 1, 1))
                .excludeDates(LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 10));
        entry.setRrule(rule);
        assertNotNull(entry.getExdate());
        assertEquals(2, entry.getExdate().size());

        // Verify exdate serializes to JSON
        JsonObject json = entry.toJson();
        assertTrue(json.hasKey("exdate"));
    }

    @Test
    void rruleNullClearsRruleAndExclusions() {
        Entry entry = new Entry();
        entry.setRrule(RRule.daily().excludeDates(LocalDate.of(2026, 1, 5)));
        assertNotNull(entry.getExdate());

        entry.setRrule(null);
        assertNull(entry.getRrule());
        assertNull(entry.getExdate());
        assertNull(entry.getExrule());
    }

    @Test
    void rruleFrequencyFactories() {
        assertEquals(RRule.Frequency.DAILY, RRule.daily().getFreq());
        assertEquals(RRule.Frequency.WEEKLY, RRule.weekly().getFreq());
        assertEquals(RRule.Frequency.MONTHLY, RRule.monthly().getFreq());
        assertEquals(RRule.Frequency.YEARLY, RRule.yearly().getFreq());
    }

    @Test
    void rruleWithCountAndInterval() {
        RRule rule = RRule.weekly().count(10).interval(2);
        String str = rule.toRRuleString();
        assertTrue(str.contains("COUNT=10"));
        assertTrue(str.contains("INTERVAL=2"));
    }
}
