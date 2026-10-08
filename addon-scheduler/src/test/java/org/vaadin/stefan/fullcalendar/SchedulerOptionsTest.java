package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.html.Span;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Scheduler option constants follow the FullCalendar Scheduler 7 names, and the license key constants.
 */
class SchedulerOptionsTest {

    /** Every deprecated constant and the constant that replaces it. */
    @SuppressWarnings("removal")
    private static final Map<SchedulerOption, SchedulerOption> DEPRECATED_ALIASES = Map.ofEntries(
            Map.entry(SchedulerOption.ENTRY_RESOURCES_EDITABLE, SchedulerOption.ENTRY_RESOURCE_EDITABLE),
            Map.entry(SchedulerOption.RESOURCE_AREA_COLUMNS, SchedulerOption.RESOURCE_COLUMNS),
            Map.entry(SchedulerOption.RESOURCE_AREA_WIDTH, SchedulerOption.RESOURCE_COLUMNS_WIDTH),
            Map.entry(SchedulerOption.RESOURCE_AREA_HEADER_CONTENT, SchedulerOption.RESOURCE_COLUMN_HEADER_CONTENT),
            Map.entry(SchedulerOption.RESOURCE_AREA_HEADER_CLASS_NAMES, SchedulerOption.RESOURCE_COLUMN_HEADER_CLASS),
            Map.entry(SchedulerOption.RESOURCE_AREA_HEADER_DID_MOUNT, SchedulerOption.RESOURCE_COLUMN_HEADER_DID_MOUNT),
            Map.entry(SchedulerOption.RESOURCE_AREA_HEADER_WILL_UNMOUNT, SchedulerOption.RESOURCE_COLUMN_HEADER_WILL_UNMOUNT),
            Map.entry(SchedulerOption.RESOURCE_LANE_CLASS_NAMES, SchedulerOption.RESOURCE_LANE_CLASS),
            Map.entry(SchedulerOption.RESOURCE_GROUP_LANE_CLASS_NAMES, SchedulerOption.RESOURCE_GROUP_LANE_CLASS),
            Map.entry(SchedulerOption.RESOURCE_GROUP_CLASS_NAMES, SchedulerOption.RESOURCE_GROUP_HEADER_CLASS),
            Map.entry(SchedulerOption.RESOURCE_GROUP_CONTENT, SchedulerOption.RESOURCE_GROUP_HEADER_CONTENT),
            Map.entry(SchedulerOption.RESOURCE_GROUP_DID_MOUNT, SchedulerOption.RESOURCE_GROUP_HEADER_DID_MOUNT),
            Map.entry(SchedulerOption.RESOURCE_GROUP_WILL_UNMOUNT, SchedulerOption.RESOURCE_GROUP_HEADER_WILL_UNMOUNT)
    );

    @Test
    void deprecatedAliases_shareTheKeyOfTheirSuccessor() {
        DEPRECATED_ALIASES.forEach((alias, successor) ->
                assertEquals(successor.getOptionKey(), alias.getOptionKey(), alias.name()));
    }

    @Test
    void everyDeprecatedConstantIsAKnownAlias() throws NoSuchFieldException {
        for (SchedulerOption option : SchedulerOption.values()) {
            boolean deprecated = SchedulerOption.class.getField(option.name()).isAnnotationPresent(Deprecated.class);
            assertEquals(deprecated, DEPRECATED_ALIASES.containsKey(option), option.name());
        }
    }

    @Test
    @SuppressWarnings("removal")
    void deprecatedAlias_isReadBackUnderTheRenamedConstant() {
        FullCalendarScheduler calendar = new FullCalendarScheduler();

        calendar.setOption(SchedulerOption.RESOURCE_AREA_WIDTH, "20%");

        assertEquals("20%", calendar.getOption(SchedulerOption.RESOURCE_COLUMNS_WIDTH).orElse(null));
    }

    @Test
    void setOption_withColumnList_bindsComponentColumns() {
        FullCalendarScheduler calendar = new FullCalendarScheduler();
        var column = new ComponentResourceColumn<Span>("status", resource -> new Span());

        calendar.setOption(SchedulerOption.RESOURCE_COLUMNS, List.of(new ResourceColumn("title"), column));

        assertTrue(column.isBound(), "the list went through setResourceColumns");
        ArrayNode sent = calendar.getOption(SchedulerOption.RESOURCE_COLUMNS, true)
                .map(ArrayNode.class::cast).orElseThrow();
        assertEquals(2, sent.size());
    }

    @Test
    void setOption_withNull_clearsColumnsAndUnbindsComponentColumns() {
        FullCalendarScheduler calendar = new FullCalendarScheduler();
        var column = new ComponentResourceColumn<Span>("status", resource -> new Span());
        calendar.setResourceColumns(column);

        calendar.setOption(SchedulerOption.RESOURCE_COLUMNS, null);

        assertFalse(column.isBound());
        assertTrue(calendar.getOption(SchedulerOption.RESOURCE_COLUMNS).isEmpty());
    }

    @Test
    void setResourceColumns_sendsColumnsAndKeepsListServerSide() {
        FullCalendarScheduler calendar = new FullCalendarScheduler();
        List<ResourceColumn> columns = List.of(new ResourceColumn("title", "Resource"));

        calendar.setResourceColumns(columns);

        ArrayNode sent = calendar.getOption(SchedulerOption.RESOURCE_COLUMNS, true)
                .map(ArrayNode.class::cast).orElseThrow();
        assertEquals("title", sent.get(0).get("field").asString());
        assertEquals(columns, calendar.getOption(SchedulerOption.RESOURCE_COLUMNS).orElseThrow());

        calendar.setResourceColumns(List.of());

        assertTrue(calendar.getOption(SchedulerOption.RESOURCE_COLUMNS).isEmpty());
    }

    @Test
    @SuppressWarnings("removal")
    void setOption_deprecatedAliasWithColumnList_bindsAndConverts() {
        FullCalendarScheduler calendar = new FullCalendarScheduler();
        var column = new ComponentResourceColumn<Span>("status", resource -> new Span());

        calendar.setOption(SchedulerOption.RESOURCE_AREA_COLUMNS, List.of(column));

        assertTrue(column.isBound());
        assertTrue(calendar.getOption(SchedulerOption.RESOURCE_COLUMNS, true).orElseThrow() instanceof ArrayNode);
    }

    @Test
    void getOptionOrDefault_returnsValueOrDefault() {
        FullCalendarScheduler calendar = new FullCalendarScheduler();

        assertEquals("30%", calendar.getOptionOrDefault(SchedulerOption.RESOURCE_COLUMNS_WIDTH, "30%"));

        calendar.setOption(SchedulerOption.RESOURCE_COLUMNS_WIDTH, "20%");

        assertEquals("20%", calendar.getOptionOrDefault(SchedulerOption.RESOURCE_COLUMNS_WIDTH, "30%"));
    }

    @Test
    void setOption_withRawJson_unbindsComponentColumnsSetBefore() {
        FullCalendarScheduler calendar = new FullCalendarScheduler();
        var column = new ComponentResourceColumn<Span>("status", resource -> new Span());
        calendar.setResourceColumns(column);
        ArrayNode raw = JsonFactory.createArray();
        raw.add(JsonFactory.createObject().put("field", "title"));

        calendar.addResource(new Resource());
        assertEquals(1, column.getComponents().size());

        calendar.setOption(SchedulerOption.RESOURCE_COLUMNS, raw);

        assertFalse(column.isBound());
        assertTrue(column.getComponents().isEmpty(), "the components of the old column are destroyed");
        assertEquals(raw, calendar.getOption(SchedulerOption.RESOURCE_COLUMNS).orElse(null));
    }

    @Test
    void setOption_withNull_unbindsAndRemovesOption() {
        FullCalendarScheduler calendar = new FullCalendarScheduler();
        var column = new ComponentResourceColumn<Span>("status", resource -> new Span());
        calendar.setResourceColumns(column);

        calendar.setOption("resourceColumns", null);

        assertFalse(column.isBound());
        assertTrue(calendar.getOption(SchedulerOption.RESOURCE_COLUMNS).isEmpty());
    }

    @Test
    @SuppressWarnings("removal")
    void deprecatedSetOptionWithServerValue_goesThroughTheColumnHandling() {
        FullCalendarScheduler calendar = new FullCalendarScheduler();
        var column = new ComponentResourceColumn<Span>("status", resource -> new Span());
        List<ResourceColumn> columns = List.of(column);

        calendar.setOption(SchedulerOption.RESOURCE_COLUMNS, columns, columns);

        assertTrue(column.isBound());
        assertEquals("status", calendar.<ArrayNode>getOption(SchedulerOption.RESOURCE_COLUMNS, true)
                .orElseThrow().get(0).get("field").asString());
        assertEquals(columns, calendar.getOption(SchedulerOption.RESOURCE_COLUMNS).orElseThrow());

        calendar.setOption(SchedulerOption.RESOURCE_COLUMNS, List.of(), "ignored");

        assertFalse(column.isBound());
        assertTrue(calendar.getOption(SchedulerOption.RESOURCE_COLUMNS).isEmpty());
    }

    @Test
    void setOption_withDuplicateFields_throwsAndKeepsTheColumnsSetBefore() {
        FullCalendarScheduler calendar = new FullCalendarScheduler();
        var column = new ComponentResourceColumn<Span>("status", resource -> new Span());
        calendar.setResourceColumns(column);

        assertThrows(IllegalArgumentException.class, () -> calendar.setOption(SchedulerOption.RESOURCE_COLUMNS,
                List.of(new ResourceColumn("title"), new ResourceColumn("title"))));

        assertTrue(column.isBound());
        assertEquals(List.of(column), calendar.getOption(SchedulerOption.RESOURCE_COLUMNS).orElseThrow());
    }

    @Test
    void setOption_stringKeyWithColumnList_bindsAndEmptyListRemoves() {
        FullCalendarScheduler calendar = new FullCalendarScheduler();
        var column = new ComponentResourceColumn<Span>("status", resource -> new Span());

        calendar.setOption("resourceColumns", List.of(column));

        assertTrue(column.isBound());
        assertEquals("status", calendar.<ArrayNode>getOption(SchedulerOption.RESOURCE_COLUMNS, true)
                .orElseThrow().get(0).get("field").asString());

        calendar.setOption("resourceColumns", List.of());

        assertFalse(column.isBound());
        assertTrue(calendar.getOption(SchedulerOption.RESOURCE_COLUMNS).isEmpty());
    }

    @Test
    void setOption_withRawJson_isSentAsItIs() {
        FullCalendarScheduler calendar = new FullCalendarScheduler();
        ArrayNode raw = JsonFactory.createArray();
        raw.add(JsonFactory.createObject().put("field", "title"));

        calendar.setOption(SchedulerOption.RESOURCE_COLUMNS, raw);

        assertEquals(raw, calendar.getOption(SchedulerOption.RESOURCE_COLUMNS).orElse(null));
    }

    @Test
    void agplLicenseKey_isTheFullCalendar7PresetKey() {
        assertEquals("AGPL-My-Frontend-And-Backend-Are-Open-Source", Scheduler.AGPL_V3_LICENSE_KEY);
    }

    @Test
    void gplLicenseKey_isDeprecated() throws NoSuchFieldException {
        assertTrue(Scheduler.class.getField("GPL_V3_LICENSE_KEY").isAnnotationPresent(Deprecated.class));
    }

    @Test
    void developerLicenseKey_isTheNonCommercialPresetKey() {
        assertEquals("CC-Attribution-NonCommercial-NoDerivatives", Scheduler.NON_COMMERCIAL_CREATIVE_COMMONS_LICENSE_KEY);
        assertEquals(Scheduler.NON_COMMERCIAL_CREATIVE_COMMONS_LICENSE_KEY, Scheduler.DEVELOPER_LICENSE_KEY);
    }

    @Test
    void entryPrintLayout_sendsClientValue() {
        FullCalendarScheduler calendar = new FullCalendarScheduler();

        calendar.setOption(SchedulerOption.ENTRY_PRINT_LAYOUT, EntryPrintLayout.STACK);

        ObjectNode initialOptions = (ObjectNode) calendar.getElement().getPropertyRaw("initialOptions");
        assertEquals("stack", initialOptions.get("eventPrintLayout").asString());
        assertEquals(EntryPrintLayout.STACK, calendar.getOption(SchedulerOption.ENTRY_PRINT_LAYOUT).orElseThrow());
    }
}
