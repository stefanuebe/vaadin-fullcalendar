package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.FullCalendar.Option;
import tools.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FullCalendarOptionsTest {

    @SuppressWarnings("removal")
    private static final Map<Option, Option> DEPRECATED_ALIASES = Map.ofEntries(
            Map.entry(Option.MAX_ENTRIES_PER_DAY, Option.DAY_MAX_ENTRIES),
            Map.entry(Option.DAY_MAX_EVENT_ROWS, Option.DAY_MAX_ENTRY_ROWS),
            Map.entry(Option.DISPLAY_EVENT_END, Option.DISPLAY_ENTRY_END),
            Map.entry(Option.FORCE_EVENT_DURATION, Option.FORCE_ENTRY_DURATION),
            Map.entry(Option.PROGRESSIVE_EVENT_RENDERING, Option.PROGRESSIVE_ENTRY_RENDERING),
            Map.entry(Option.EXTERNAL_EVENT_SOURCE_START_PARAM, Option.ENTRY_SOURCE_START_PARAM),
            Map.entry(Option.EXTERNAL_EVENT_SOURCE_END_PARAM, Option.ENTRY_SOURCE_END_PARAM),
            Map.entry(Option.EXTERNAL_EVENT_SOURCE_TIME_ZONE_PARAM, Option.ENTRY_SOURCE_TIME_ZONE_PARAM),
            Map.entry(Option.EXTERNAL_EVENT_SOURCE_GOOGLE_CALENDAR_API_KEY, Option.ENTRY_SOURCE_GOOGLE_CALENDAR_API_KEY),
            Map.entry(Option.NATIVE_TOOLBAR_VIEW_HINT, Option.VIEW_HINT));

    @Test
    void testNonEmptyOptionKeys() {
        Assertions.assertFalse(Stream.of(Option.values())
                .map(Option::getOptionKey)
                .anyMatch(s -> s == null || s.trim().isEmpty()));
    }

    @Test
    void deprecatedAliases_setSameOptionAsRenamedConstant() {
        DEPRECATED_ALIASES.forEach((alias, renamed) -> {
            assertEquals(renamed.getOptionKey(), alias.getOptionKey(), alias.name());
            assertEquals(converterTypes(renamed), converterTypes(alias), alias.name());
        });
    }

    @Test
    void everyDeprecatedConstantIsAKnownAlias() throws NoSuchFieldException {
        for (Option option : Option.values()) {
            boolean deprecated = Option.class.getField(option.name()).isAnnotationPresent(Deprecated.class);
            assertEquals(deprecated, DEPRECATED_ALIASES.containsKey(option), option.name());
        }
    }

    @Test
    void initialDate_isSentAsIsoDate() {
        FullCalendar calendar = new FullCalendar();
        LocalDate date = LocalDate.of(2025, 3, 1);

        calendar.setOption(Option.INITIAL_DATE, date);

        assertEquals(date, calendar.getOption(Option.INITIAL_DATE).orElse(null));
        assertEquals("2025-03-01", calendar.<JsonNode>getOption(Option.INITIAL_DATE, true).orElseThrow().asString());
    }

    private static Set<Class<?>> converterTypes(Option option) {
        return option.getConverters().stream().map(Object::getClass).collect(Collectors.toSet());
    }
}
