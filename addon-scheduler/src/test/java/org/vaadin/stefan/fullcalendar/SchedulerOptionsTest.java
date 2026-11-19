package org.vaadin.stefan.fullcalendar;

import org.vaadin.stefan.fullcalendar.option.ComponentResourceColumn;
import org.vaadin.stefan.fullcalendar.option.ResourceColumn;
import org.vaadin.stefan.fullcalendar.option.EntryPrintLayout;
import org.vaadin.stefan.fullcalendar.option.Timezone;
import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Span;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The scheduler option fields that need more than a getter and setter (the resource columns, the print layout) and
 * the license key constants.
 */
class SchedulerOptionsTest extends BrowserlessTest {

    private static ObjectNode initialOptions(FullCalendarScheduler calendar) {
        return (ObjectNode) calendar.getElement().getPropertyRaw("initialOptions");
    }

    @Test
    void noArgConstructor_startsWithUtc_andSetsNoDroppedDefaults() {
        FullCalendarScheduler calendar = new FullCalendarScheduler();
        UI.getCurrent().add(calendar);
        UI.getCurrent().getInternals().getStateTree().runExecutionsBeforeClientResponse();

        assertEquals(Timezone.UTC, calendar.options().getTimeZone());
        for (String name : List.of("editable", "dayMaxEvents", "weekNumbers", "tableHeaderSticky",
                "footerScrollbarSticky")) {
            assertFalse(initialOptions(calendar).has(name), name);
        }
    }

    @Test
    void setResourceColumns_withColumnList_bindsComponentColumns() {
        FullCalendarScheduler calendar = new FullCalendarScheduler();
        var column = new ComponentResourceColumn<Span>("status", resource -> new Span());

        calendar.options().setResourceColumns(List.of(new ResourceColumn("title"), column));

        assertTrue(column.isBound());
        assertEquals(2, initialOptions(calendar).get("resourceColumns").size());
    }

    @Test
    void setResourceColumns_withNull_clearsColumnsAndUnbindsComponentColumns() {
        FullCalendarScheduler calendar = new FullCalendarScheduler();
        var column = new ComponentResourceColumn<Span>("status", resource -> new Span());
        calendar.options().setResourceColumns(List.of(column));

        calendar.options().setResourceColumns(null);

        assertFalse(column.isBound());
        assertNull(calendar.options().getResourceColumns());
    }

    @Test
    void setResourceColumns_sendsColumnsAndKeepsListServerSide() {
        FullCalendarScheduler calendar = new FullCalendarScheduler();
        List<ResourceColumn> columns = List.of(new ResourceColumn("title", "Resource"));

        calendar.options().setResourceColumns(columns);

        ArrayNode sent = (ArrayNode) initialOptions(calendar).get("resourceColumns");
        assertEquals("title", sent.get(0).get("field").asString());
        assertEquals(columns, calendar.options().getResourceColumns());

        calendar.options().setResourceColumns(List.of());

        assertNull(calendar.options().getResourceColumns());
    }

    @Test
    void setResourceColumns_withDuplicateFields_throwsAndKeepsTheColumnsSetBefore() {
        FullCalendarScheduler calendar = new FullCalendarScheduler();
        var column = new ComponentResourceColumn<Span>("status", resource -> new Span());
        calendar.options().setResourceColumns(List.of(column));

        assertThrows(IllegalArgumentException.class, () -> calendar.options().setResourceColumns(
                List.of(new ResourceColumn("title"), new ResourceColumn("title"))));

        assertTrue(column.isBound());
        assertEquals(List.of(column), calendar.options().getResourceColumns());
    }

    @Test
    void rawResourceColumns_areSentAsTheyAre() {
        FullCalendarScheduler calendar = new FullCalendarScheduler();
        ArrayNode raw = JsonFactory.createArray();
        raw.add(JsonFactory.createObject().put("field", "title"));

        calendar.options().setRawOption("resourceColumns", raw);

        assertEquals(raw, calendar.options().getRawOption("resourceColumns").orElse(null));
        assertEquals(raw, initialOptions(calendar).get("resourceColumns"));
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

        calendar.options().setEntryPrintLayout(EntryPrintLayout.STACK);
        UI.getCurrent().add(calendar);
        UI.getCurrent().getInternals().getStateTree().runExecutionsBeforeClientResponse();

        ObjectNode initialOptions = (ObjectNode) calendar.getElement().getPropertyRaw("initialOptions");
        assertEquals("stack", initialOptions.get("eventPrintLayout").asString());
        assertEquals(EntryPrintLayout.STACK, calendar.options().getEntryPrintLayout());
    }
}
