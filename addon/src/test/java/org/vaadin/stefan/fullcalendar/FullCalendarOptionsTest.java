package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.FullCalendar.Option;

import java.util.stream.Stream;

public class FullCalendarOptionsTest {
    @Test
    void testNonEmptyOptionKeys() {
        Assertions.assertFalse(Stream.of(Option.values())
                .map(Option::getOptionKey)
                .anyMatch(s -> s == null || s.trim().isEmpty()));
    }

    @Test
    void getOptionOrDefault_returnsValueOrDefault() {
        FullCalendar calendar = new FullCalendar();
        Timezone berlin = new Timezone(java.time.ZoneId.of("Europe/Berlin"));

        Assertions.assertEquals(Timezone.UTC, calendar.getOptionOrDefault(Option.TIMEZONE, Timezone.UTC));
        Assertions.assertEquals("fallback", calendar.getOptionOrDefault("someUnsetOption", "fallback"));

        calendar.setOption(Option.TIMEZONE, berlin);
        calendar.setOption(Option.EDITABLE, false);

        Assertions.assertEquals(berlin, calendar.getOptionOrDefault(Option.TIMEZONE, Timezone.UTC));
        Assertions.assertEquals(false, calendar.getOptionOrDefault(Option.EDITABLE, true));
        Assertions.assertEquals(berlin, calendar.getOptionOrDefault(Option.TIMEZONE.getOptionKey(), Timezone.UTC));
    }

    @Test
    @SuppressWarnings("deprecation")
    void renamedOptionConstants_areTheirDeprecatedAliases() {
        Assertions.assertSame(Option.DAY_MAX_ENTRIES, Option.MAX_ENTRIES_PER_DAY);
        Assertions.assertSame(Option.DAY_MAX_ENTRY_ROWS, Option.DAY_MAX_EVENT_ROWS);
        Assertions.assertSame(Option.DISPLAY_ENTRY_END, Option.DISPLAY_EVENT_END);
        Assertions.assertSame(Option.FORCE_ENTRY_DURATION, Option.FORCE_EVENT_DURATION);
        Assertions.assertSame(Option.PROGRESSIVE_ENTRY_RENDERING, Option.PROGRESSIVE_EVENT_RENDERING);
        Assertions.assertSame(Option.ENTRY_SOURCE_START_PARAM, Option.EXTERNAL_EVENT_SOURCE_START_PARAM);
        Assertions.assertSame(Option.ENTRY_SOURCE_END_PARAM, Option.EXTERNAL_EVENT_SOURCE_END_PARAM);
        Assertions.assertSame(Option.ENTRY_SOURCE_TIME_ZONE_PARAM, Option.EXTERNAL_EVENT_SOURCE_TIME_ZONE_PARAM);
        Assertions.assertSame(Option.ENTRY_SOURCE_GOOGLE_CALENDAR_API_KEY, Option.EXTERNAL_EVENT_SOURCE_GOOGLE_CALENDAR_API_KEY);
        Assertions.assertSame(Option.VIEW_HINT, Option.NATIVE_TOOLBAR_VIEW_HINT);

        Assertions.assertEquals("dayMaxEvents", Option.DAY_MAX_ENTRIES.getOptionKey());
        Assertions.assertEquals("dayMaxEventRows", Option.DAY_MAX_ENTRY_ROWS.getOptionKey());
    }

    @Test
    void validRange_dateRangeIsSentAsStartAndEnd() {
        FullCalendar calendar = new FullCalendar();
        DateRange range = new DateRange(java.time.LocalDate.of(2025, 3, 1), null);

        calendar.setOption(Option.VALID_RANGE, range);

        tools.jackson.databind.JsonNode json = calendar.<tools.jackson.databind.JsonNode>getOption(Option.VALID_RANGE, true).orElseThrow();
        Assertions.assertEquals("2025-03-01", json.get("start").asString());
        Assertions.assertFalse(json.has("end"));
        Assertions.assertEquals(range, calendar.getOption(Option.VALID_RANGE).orElseThrow());
    }
}
