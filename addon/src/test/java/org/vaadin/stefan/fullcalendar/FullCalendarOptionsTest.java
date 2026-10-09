package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
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
            Map.entry(Option.NATIVE_TOOLBAR_VIEW_HINT, Option.VIEW_HINT),
            // FullCalendar 7 renames
            Map.entry(Option.ENTRY_TEXT_COLOR, Option.ENTRY_CONTRAST_COLOR),
            Map.entry(Option.LIST_DAY_SIDE_FORMAT, Option.LIST_DAY_ALT_FORMAT),
            Map.entry(Option.MULTI_MONTH_MIN_WIDTH, Option.SINGLE_MONTH_MIN_WIDTH),
            Map.entry(Option.MULTI_MONTH_TITLE_FORMAT, Option.SINGLE_MONTH_TITLE_FORMAT),
            Map.entry(Option.SLOT_LABEL_FORMAT, Option.SLOT_HEADER_FORMAT),
            Map.entry(Option.SLOT_LABEL_INTERVAL, Option.SLOT_HEADER_INTERVAL),
            Map.entry(Option.STICKY_FOOTER_SCROLLBAR, Option.FOOTER_SCROLLBAR_STICKY),
            Map.entry(Option.STICKY_HEADER_DATES, Option.TABLE_HEADER_STICKY),
            Map.entry(Option.WEEK_TEXT, Option.WEEK_TEXT_SHORT),
            Map.entry(Option.DAY_POPOVER_FORMAT, Option.POPOVER_FORMAT),
            Map.entry(Option.ENTRY_CLASS_NAMES, Option.ENTRY_CLASS),
            Map.entry(Option.DAY_CELL_CLASS_NAMES, Option.DAY_CELL_CLASS),
            Map.entry(Option.DAY_CELL_CONTENT, Option.DAY_CELL_TOP_CONTENT),
            Map.entry(Option.DAY_HEADER_CLASS_NAMES, Option.DAY_HEADER_CLASS),
            Map.entry(Option.SLOT_LABEL_CLASS_NAMES, Option.SLOT_HEADER_CLASS),
            Map.entry(Option.SLOT_LABEL_CONTENT, Option.SLOT_HEADER_CONTENT),
            Map.entry(Option.SLOT_LABEL_DID_MOUNT, Option.SLOT_HEADER_DID_MOUNT),
            Map.entry(Option.SLOT_LABEL_WILL_UNMOUNT, Option.SLOT_HEADER_WILL_UNMOUNT),
            Map.entry(Option.SLOT_LANE_CLASS_NAMES, Option.SLOT_LANE_CLASS),
            Map.entry(Option.VIEW_CLASS_NAMES, Option.VIEW_CLASS),
            Map.entry(Option.MORE_LINK_CLASS_NAMES, Option.MORE_LINK_CLASS),
            Map.entry(Option.NO_ENTRIES_CLASS_NAMES, Option.NO_ENTRIES_CLASS));

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
    @SuppressWarnings("removal")
    void deprecatedAlias_isReadBackUnderTheRenamedConstant() {
        FullCalendar calendar = new FullCalendar();

        calendar.setOption(Option.ENTRY_CLASS_NAMES, "urgent");

        assertEquals("urgent", calendar.getOption(Option.ENTRY_CLASS).orElse(null));
    }

    @Test
    void renamedConstants_useTheFullCalendar7Keys() {
        assertEquals("eventClass", Option.ENTRY_CLASS.getOptionKey());
        assertEquals("eventContrastColor", Option.ENTRY_CONTRAST_COLOR.getOptionKey());
        assertEquals("dayCellTopContent", Option.DAY_CELL_TOP_CONTENT.getOptionKey());
        assertEquals("slotHeaderInterval", Option.SLOT_HEADER_INTERVAL.getOptionKey());
        assertEquals("tableHeaderSticky", Option.TABLE_HEADER_STICKY.getOptionKey());
        assertEquals("noEventsClass", Option.NO_ENTRIES_CLASS.getOptionKey());
    }

    @Test
    void splitHooks_haveOneConstantPerPart() {
        assertEquals("nowIndicatorHeaderClass", Option.NOW_INDICATOR_HEADER_CLASS.getOptionKey());
        assertEquals("nowIndicatorLineContent", Option.NOW_INDICATOR_LINE_CONTENT.getOptionKey());
        assertEquals("inlineWeekNumberClass", Option.INLINE_WEEK_NUMBER_CLASS.getOptionKey());
        assertEquals("weekNumberHeaderDidMount", Option.WEEK_NUMBER_HEADER_DID_MOUNT.getOptionKey());
        assertEquals("allDayHeaderWillUnmount", Option.ALL_DAY_HEADER_WILL_UNMOUNT.getOptionKey());
        assertEquals("dayLaneClass", Option.DAY_LANE_CLASS.getOptionKey());
        assertEquals("listDayHeaderContent", Option.LIST_DAY_HEADER_CONTENT.getOptionKey());
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

    @Test
    void getOptionOrDefault_returnsValueOrDefault() {
        FullCalendar calendar = new FullCalendar();
        Timezone berlin = new Timezone(java.time.ZoneId.of("Europe/Berlin"));

        assertEquals(Timezone.UTC, calendar.getOptionOrDefault(Option.TIMEZONE, Timezone.UTC));
        assertEquals("fallback", calendar.getOptionOrDefault("someUnsetOption", "fallback"));

        calendar.setOption(Option.TIMEZONE, berlin);
        calendar.setOption(Option.EDITABLE, false);

        assertEquals(berlin, calendar.getOptionOrDefault(Option.TIMEZONE, Timezone.UTC));
        assertEquals(false, calendar.getOptionOrDefault(Option.EDITABLE, true));
        assertEquals(berlin, calendar.getOptionOrDefault(Option.TIMEZONE.getOptionKey(), Timezone.UTC));
    }

    @Test
    void removedOptionReturnsToTheAddonDefault() {
        FullCalendar calendar = new FullCalendar();

        calendar.setOption(Option.EDITABLE, false);
        calendar.setOption(Option.EDITABLE, null);
        assertEquals(Optional.of(true), calendar.getOption(Option.EDITABLE));

        calendar.setOption(Option.LOCALE, Locale.GERMAN);
        calendar.setOption(Option.LOCALE, null);
        assertEquals(Optional.of(CalendarLocale.getDefaultLocale()), calendar.getOption(Option.LOCALE));

        calendar.setOption(Option.DAY_MAX_ENTRIES, 3);
        calendar.setOption(Option.DAY_MAX_ENTRIES, null);
        assertEquals(Optional.of(false), calendar.getOption(Option.DAY_MAX_ENTRIES));
    }

    @Test
    void removeOptionRemovesTheOption() {
        FullCalendar calendar = new FullCalendar();

        calendar.setOption(Option.WEEKENDS, false);
        calendar.removeOption(Option.WEEKENDS);
        assertEquals(Optional.empty(), calendar.getOption(Option.WEEKENDS));

        calendar.setOption(Option.EDITABLE, false);
        calendar.removeOption(Option.EDITABLE);
        assertEquals(Optional.of(true), calendar.getOption(Option.EDITABLE));
    }

    @Test
    void removeOptionWithStringKeyRemovesTheOption() {
        FullCalendar calendar = new FullCalendar();

        calendar.setOption("weekends", false);
        calendar.removeOption("weekends");
        assertEquals(Optional.empty(), calendar.getOption("weekends"));

        calendar.setOption(Option.EDITABLE, false);
        calendar.removeOption("editable");
        assertEquals(Optional.of(true), calendar.getOption(Option.EDITABLE));
    }

    @Test
    void removedOptionWithoutAddonDefaultStaysRemoved() {
        FullCalendar calendar = new FullCalendar();

        calendar.setOption(Option.WEEKENDS, false);
        calendar.setOption(Option.WEEKENDS, null);
        assertEquals(Optional.empty(), calendar.getOption(Option.WEEKENDS));
    }

    @Test
    void removedHeightReturnsToTheAddonDefault() {
        FullCalendar calendar = new FullCalendar();

        calendar.setOption(Option.HEIGHT, "500px");
        calendar.setOption(Option.HEIGHT, null);
        assertEquals(Optional.of("100%"), calendar.getOption(Option.HEIGHT));
        ObjectNode clientOptions = (ObjectNode) calendar.getElement().getPropertyRaw("initialOptions");
        assertEquals("100%", clientOptions.get("height").asString());
    }

    @Test
    void removedHeightReturnsToTheLastVaadinHeight() {
        FullCalendar calendar = new FullCalendar();

        calendar.setSizeUndefined();
        calendar.setHeightFull();
        calendar.setOption(Option.HEIGHT, "500px");
        calendar.setOption(Option.HEIGHT, null);
        assertEquals(Optional.of("100%"), calendar.getOption(Option.HEIGHT));

        calendar.setHeight("400px");
        calendar.setOption(Option.HEIGHT, "500px");
        calendar.setOption(Option.HEIGHT, null);
        assertEquals(Optional.of("400px"), calendar.getOption(Option.HEIGHT));
    }

    @Test
    void heightOfTheInitialJsonOptionsIsNoAddonDefault() {
        ObjectNode initialOptions = JsonFactory.createObject();
        initialOptions.put("height", 500);
        FullCalendar calendar = new FullCalendar(initialOptions);
        assertEquals(Optional.empty(), calendar.getOption(Option.HEIGHT));

        calendar.setOption(Option.HEIGHT, "300px");
        calendar.setOption(Option.HEIGHT, null);
        // the client falls back to the initial JSON options
        assertEquals(Optional.empty(), calendar.getOption(Option.HEIGHT));
    }

    @Test
    void undefinedSizeRemovesTheHeight() {
        FullCalendar calendar = new FullCalendar();

        calendar.setSizeUndefined();
        assertEquals(Optional.empty(), calendar.getOption(Option.HEIGHT));

        calendar.setOption(Option.HEIGHT, "500px");
        calendar.setOption(Option.HEIGHT, null);
        assertEquals(Optional.empty(), calendar.getOption(Option.HEIGHT));
    }

    @Test
    void optionOfTheInitialJsonOptionsIsNoAddonDefault() {
        ObjectNode initialOptions = JsonFactory.createObject();
        initialOptions.put("editable", false);
        FullCalendar calendar = new FullCalendar(initialOptions);

        calendar.setOption(Option.EDITABLE, true);
        calendar.setOption(Option.EDITABLE, null);
        // the client falls back to the initial JSON options
        assertEquals(Optional.empty(), calendar.getOption(Option.EDITABLE));
    }
}
