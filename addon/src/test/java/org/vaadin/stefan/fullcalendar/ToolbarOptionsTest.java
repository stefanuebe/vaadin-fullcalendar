package org.vaadin.stefan.fullcalendar;

import org.vaadin.stefan.fullcalendar.option.NativeToolbarParts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.BooleanNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The header and footer toolbar options take a map of positions to FullCalendar's button string.
 */
public class ToolbarOptionsTest {

    private FullCalendar calendar;

    @BeforeEach
    void setUp() {
        calendar = new FullCalendar();
    }

    private ObjectNode sentToClient() {
        return (ObjectNode) calendar.getElement().getPropertyRaw("initialOptions");
    }

    @Test
    void headerToolbar_map_reachesClientAsObject() {
        Map<String, String> toolbar = Map.of("start", "prev,next today", "center", "title", "end", "dayGridMonth,timeGridWeek");

        calendar.setOption(Option.HEADER_TOOLBAR, toolbar);

        ObjectNode json = (ObjectNode) sentToClient().get("headerToolbar");
        assertEquals("prev,next today", json.get("start").asString());
        assertEquals("title", json.get("center").asString());
        assertEquals("dayGridMonth,timeGridWeek", json.get("end").asString());
        assertEquals(toolbar, calendar.getOption(Option.HEADER_TOOLBAR).orElseThrow());
    }

    @Test
    void toolbarParts_buildTheButtonStringsFullCalendarExpects() {
        calendar.setOption(Option.HEADER_TOOLBAR, Map.of(
                NativeToolbarParts.START, NativeToolbarParts.PREV_YEAR + "," + NativeToolbarParts.PREV + "," + NativeToolbarParts.NEXT + ","
                        + NativeToolbarParts.NEXT_YEAR + " " + NativeToolbarParts.TODAY,
                NativeToolbarParts.CENTER, NativeToolbarParts.TITLE,
                NativeToolbarParts.END, CalendarViewImpl.DAY_GRID_MONTH.getClientSideValue()));

        ObjectNode json = (ObjectNode) sentToClient().get("headerToolbar");
        assertEquals("prevYear,prev,next,nextYear today", json.get("start").asString());
        assertEquals("title", json.get("center").asString());
        assertEquals("dayGridMonth", json.get("end").asString());
    }

    @Test
    void footerToolbar_map_reachesClientAsObject() {
        calendar.setOption(Option.FOOTER_TOOLBAR, Map.of("center", "prev,next"));

        assertEquals("prev,next", sentToClient().get("footerToolbar").get("center").asString());
    }

    @Test
    void headerToolbar_false_hidesToolbar() {
        calendar.setOption(Option.HEADER_TOOLBAR, false);

        assertTrue(sentToClient().get("headerToolbar").isBoolean());
        assertFalse(sentToClient().get("headerToolbar").booleanValue());
    }

    // setOption(…, null) drops the raw value, so the field's value applies again. The typed setter with null removes
    // the option instead, see AutoBrowserSettingsTest.headerToolbar_setToNull_isRemovedOnTheClient.
    @Test
    void headerToolbar_null_bringsTheFieldBack() {
        calendar.setOption(Option.HEADER_TOOLBAR, Map.of("center", "title"));

        calendar.setOption(Option.HEADER_TOOLBAR, null);

        assertEquals(BooleanNode.FALSE, sentToClient().get("headerToolbar"), "the field's initial value, no toolbar");
        assertTrue(calendar.getOption(Option.HEADER_TOOLBAR).isEmpty());
    }
}
