package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.converters.DateRangeConverter;
import tools.jackson.databind.JsonNode;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class DateRangeConverterTest {

    private final DateRangeConverter converter = new DateRangeConverter();

    @Test
    void supports_onlyDateRange() {
        assertTrue(converter.supports(new DateRange(null, null)));
        assertFalse(converter.supports("2025-03-01"));
        assertFalse(converter.supports(null));
    }

    @Test
    void toClientModel_bothBounds() {
        JsonNode json = converter.toClientModel(new DateRange(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 5, 1)), null);

        assertEquals("2025-03-01", json.get("start").asString());
        assertEquals("2025-05-01", json.get("end").asString());
    }

    @Test
    void toClientModel_openBoundsAreLeftOut() {
        JsonNode openStart = converter.toClientModel(new DateRange(null, LocalDate.of(2025, 5, 1)), null);
        JsonNode open = converter.toClientModel(new DateRange(null, null), null);

        assertFalse(openStart.has("start"));
        assertEquals(0, open.size());
    }
}
