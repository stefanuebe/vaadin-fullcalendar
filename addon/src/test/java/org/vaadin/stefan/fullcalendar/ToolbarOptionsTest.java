package org.vaadin.stefan.fullcalendar;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import org.vaadin.stefan.fullcalendar.option.NativeToolbarParts;
import org.vaadin.stefan.fullcalendar.option.ToolbarValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.BooleanNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The header and footer toolbar options take positions with FullCalendar's button string.
 */
public class ToolbarOptionsTest extends BrowserlessTest {

    private FullCalendar calendar;

    @BeforeEach
    void setUp() {
        calendar = new FullCalendar();
    }

    /** The initial options the client reads on attach, written when the calendar is attached. */
    private ObjectNode sentToClient() {
        UI.getCurrent().add(calendar);
        UI.getCurrent().getInternals().getStateTree().runExecutionsBeforeClientResponse();
        return (ObjectNode) calendar.getElement().getPropertyRaw("initialOptions");
    }

    @Test
    void headerToolbar_map_reachesClientAsObject() {
        ToolbarValue toolbar = ToolbarValue.ofStartEnd("prev,next today", "title", "dayGridMonth,timeGridWeek");

        calendar.options().setHeaderToolbar(toolbar);

        ObjectNode json = (ObjectNode) sentToClient().get("headerToolbar");
        assertEquals("prev,next today", json.get("start").asString());
        assertEquals("title", json.get("center").asString());
        assertEquals("dayGridMonth,timeGridWeek", json.get("end").asString());
        assertEquals(toolbar, calendar.options().getHeaderToolbar());
    }

    @Test
    void toolbarParts_buildTheButtonStringsFullCalendarExpects() {
        calendar.options().setHeaderToolbar(ToolbarValue.ofStartEnd(
                NativeToolbarParts.PREV_YEAR + "," + NativeToolbarParts.PREV + "," + NativeToolbarParts.NEXT + ","
                        + NativeToolbarParts.NEXT_YEAR + " " + NativeToolbarParts.TODAY,
                NativeToolbarParts.TITLE,
                CalendarViewImpl.DAY_GRID_MONTH.getClientSideValue()));

        ObjectNode json = (ObjectNode) sentToClient().get("headerToolbar");
        assertEquals("prevYear,prev,next,nextYear today", json.get("start").asString());
        assertEquals("title", json.get("center").asString());
        assertEquals("dayGridMonth", json.get("end").asString());
    }

    @Test
    void footerToolbar_map_reachesClientAsObject() {
        calendar.options().setFooterToolbar(ToolbarValue.ofStartEnd("", "prev,next", ""));

        assertEquals("prev,next", sentToClient().get("footerToolbar").get("center").asString());
    }

    @Test
    void headerToolbar_none_hidesToolbar() {
        calendar.options().setHeaderToolbar(ToolbarValue.ofStartEnd("prev", "title", "next"));
        calendar.options().setHeaderToolbar(ToolbarValue.NONE);

        assertTrue(sentToClient().get("headerToolbar").isBoolean());
        assertFalse(sentToClient().get("headerToolbar").booleanValue());
    }

    // Removing the raw option brings the field's value back. The field set to null removes the option instead, see
    // AutoBrowserSettingsTest.headerToolbar_setToNull_isRemovedOnTheClient.
    @Test
    void headerToolbar_rawRemoved_bringsTheFieldBack() {
        calendar.options().setRawOption("headerToolbar", Map.of("center", "title"));
        assertEquals(1, calendar.options().getRawOption("headerToolbar").orElseThrow().size());
        calendar.options().setRawOption("headerToolbar", null);

        assertEquals(BooleanNode.FALSE, sentToClient().get("headerToolbar"), "the field's initial value, no toolbar");
        assertTrue(calendar.options().getRawOption("headerToolbar").isEmpty());
    }
}
