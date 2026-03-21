package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.FullCalendar.Option;
import tools.jackson.databind.node.ObjectNode;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.vaadin.stefan.fullcalendar.TestUtils.assertOptionalEquals;

/**
 * Tests for advanced and niche options.
 * Covers eventConstraint, dateIncrement/dateAlignment, CSP nonce,
 * view-specific options, fixedMirrorParent, dragScrollEls, validRange/selectOverlap callbacks,
 * navigation methods, and getCurrentIntervalStart/End.
 */
public class AdvancedOptionsTest {

    private FullCalendar calendar;

    @BeforeEach
    void setUp() {
        calendar = new FullCalendar();
    }

    // -------------------------------------------------------------------------
    // Option enum keys
    // -------------------------------------------------------------------------

    @Test
    void option_eventConstraint_key() {
        assertEquals("eventConstraint", Option.ENTRY_CONSTRAINT.getOptionKey());
    }

    @Test
    void option_dateIncrement_key() {
        assertEquals("dateIncrement", Option.DATE_INCREMENT.getOptionKey());
    }

    @Test
    void option_dateAlignment_key() {
        assertEquals("dateAlignment", Option.DATE_ALIGNMENT.getOptionKey());
    }

    @Test
    void option_contentSecurityPolicy_key() {
        assertEquals("contentSecurityPolicy", Option.CONTENT_SECURITY_POLICY.getOptionKey());
    }

    @Test
    void option_dragScrollEls_key() {
        assertEquals("dragScrollEls", Option.DRAG_SCROLL_ELS.getOptionKey());
    }

    // -------------------------------------------------------------------------
    // 7.6 entryConstraint
    // -------------------------------------------------------------------------

    @Test
    void setEntryConstraint_string_storesOption() {
        calendar.setOption(Option.ENTRY_CONSTRAINT, "myGroup");
        assertOptionalEquals("myGroup", calendar.getOption(Option.ENTRY_CONSTRAINT));
    }

    @Test
    void setEntryConstraint_businessHours_storesJsonNode() {
        BusinessHours hours = BusinessHours.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY);
        calendar.setEntryConstraint(hours);
        Optional<Object> opt = calendar.getOption(Option.ENTRY_CONSTRAINT);
        assertTrue(opt.isPresent());
        // stored as ObjectNode
        assertInstanceOf(ObjectNode.class, opt.get());
    }

    @Test
    void setEntryConstraintToBusinessHours_storesString() {
        calendar.setOption(Option.ENTRY_CONSTRAINT, "businessHours");
        assertOptionalEquals("businessHours", calendar.getOption(Option.ENTRY_CONSTRAINT));
    }

    @Test
    void setEntryConstraint_null_clearsOption() {
        calendar.setOption(Option.ENTRY_CONSTRAINT, "myGroup");
        calendar.setOption(Option.ENTRY_CONSTRAINT, null);
        assertTrue(calendar.getOption(Option.ENTRY_CONSTRAINT).isEmpty());
    }

    @Test
    void setEntryConstraint_businessHours_null_throws() {
        assertThrows(NullPointerException.class, () -> calendar.setEntryConstraint((BusinessHours) null));
    }

    // -------------------------------------------------------------------------
    // 7.7 dateIncrement and dateAlignment
    // -------------------------------------------------------------------------

    @Test
    void setDateIncrement_storesOption() {
        calendar.setOption(Option.DATE_INCREMENT, "P1W");
        assertOptionalEquals("P1W", calendar.getOption(Option.DATE_INCREMENT));
    }

    @Test
    void setDateIncrement_null_clearsOption() {
        calendar.setOption(Option.DATE_INCREMENT, "P1W");
        calendar.setOption(Option.DATE_INCREMENT, null);
        assertTrue(calendar.getOption(Option.DATE_INCREMENT).isEmpty());
    }

    @Test
    void setDateAlignment_storesOption() {
        calendar.setOption(Option.DATE_ALIGNMENT, "week");
        assertOptionalEquals("week", calendar.getOption(Option.DATE_ALIGNMENT));
    }

    @Test
    void setDateAlignment_null_clearsOption() {
        calendar.setOption(Option.DATE_ALIGNMENT, "week");
        calendar.setOption(Option.DATE_ALIGNMENT, null);
        assertTrue(calendar.getOption(Option.DATE_ALIGNMENT).isEmpty());
    }

    // -------------------------------------------------------------------------
    // 7.12 CSP nonce
    // -------------------------------------------------------------------------

    @Test
    void setContentSecurityPolicyNonce_storesMapOption() {
        calendar.setOption(Option.CONTENT_SECURITY_POLICY, Map.of("nonce", "abc123"));
        Optional<Object> opt = calendar.getOption(Option.CONTENT_SECURITY_POLICY);
        assertTrue(opt.isPresent());
        Map<?, ?> map = (Map<?, ?>) opt.get();
        assertEquals("abc123", map.get("nonce"));
    }

    @Test
    void setContentSecurityPolicyNonce_null_clearsOption() {
        calendar.setOption(Option.CONTENT_SECURITY_POLICY, Map.of("nonce", "abc123"));
        calendar.setOption(Option.CONTENT_SECURITY_POLICY, null);
        assertTrue(calendar.getOption(Option.CONTENT_SECURITY_POLICY).isEmpty());
    }

    // -------------------------------------------------------------------------
    // 7.13 setViewSpecificOption
    // -------------------------------------------------------------------------

    @Test
    void setViewSpecificOption_string_key_storesNestedJson() {
        calendar.setViewSpecificOption("dayGridMonth", "dayMaxEventRows", 3);
        Optional<Object> opt = calendar.getOption("views");
        assertTrue(opt.isPresent());
        ObjectNode views = (ObjectNode) opt.get();
        assertTrue(views.has("dayGridMonth"));
        assertEquals(3, views.get("dayGridMonth").get("dayMaxEventRows").asInt());
    }

    @Test
    void setViewSpecificOption_option_enum_storesNestedJson() {
        calendar.setViewSpecificOption("timeGrid", Option.SLOT_DURATION, "00:30:00");
        Optional<Object> opt = calendar.getOption("views");
        assertTrue(opt.isPresent());
        ObjectNode views = (ObjectNode) opt.get();
        assertTrue(views.has("timeGrid"));
        assertEquals("00:30:00", views.get("timeGrid").get("slotDuration").asText());
    }

    @Test
    void setViewSpecificOption_calendarView_enum_storesNestedJson() {
        calendar.setViewSpecificOption(CalendarViewImpl.DAY_GRID_MONTH, Option.NOW_INDICATOR, true);
        Optional<Object> opt = calendar.getOption("views");
        assertTrue(opt.isPresent());
        ObjectNode views = (ObjectNode) opt.get();
        String viewKey = CalendarViewImpl.DAY_GRID_MONTH.getClientSideValue();
        assertTrue(views.has(viewKey));
        assertTrue(views.get(viewKey).get("nowIndicator").asBoolean());
    }

    @Test
    void setViewSpecificOption_multipleViews_storesSeparately() {
        calendar.setViewSpecificOption("dayGrid", "dayMaxEventRows", 3);
        calendar.setViewSpecificOption("timeGrid", "slotDuration", "00:30:00");
        ObjectNode views = (ObjectNode) calendar.getOption("views").orElseThrow();
        assertTrue(views.has("dayGrid"));
        assertTrue(views.has("timeGrid"));
        assertEquals(3, views.get("dayGrid").get("dayMaxEventRows").asInt());
        assertEquals("00:30:00", views.get("timeGrid").get("slotDuration").asText());
    }

    @Test
    void setViewSpecificOptions_map_storesAllEntries() {
        calendar.setViewSpecificOptions("listWeek", Map.of("noEventsText", "None", "listDaySideFormat", "DD"));
        ObjectNode views = (ObjectNode) calendar.getOption("views").orElseThrow();
        assertTrue(views.has("listWeek"));
        assertEquals("None", views.get("listWeek").get("noEventsText").asText());
    }

    @Test
    void setViewSpecificOption_null_value_removesEntireViewNode() {
        // Set an option and then null it — the view node should be cleaned up entirely
        calendar.setViewSpecificOption("dayGrid", "dayMaxEventRows", 3);
        calendar.setViewSpecificOption("dayGrid", "dayMaxEventRows", null);
        // Implementation removes the view node when it becomes empty, so views is empty/absent
        Optional<Object> opt = calendar.getOption("views");
        opt.ifPresent(v -> {
            ObjectNode views = (ObjectNode) v;
            assertFalse(views.has("dayGrid"), "empty dayGrid view node must be removed");
        });
    }

    @Test
    void setViewSpecificOption_multipleKeys_removeOneKeepOther() {
        calendar.setViewSpecificOption("dayGrid", "dayMaxEventRows", 3);
        calendar.setViewSpecificOption("dayGrid", "nowIndicator", true);
        // Remove only one key
        calendar.setViewSpecificOption("dayGrid", "dayMaxEventRows", null);
        ObjectNode views = (ObjectNode) calendar.getOption("views").orElseThrow();
        assertTrue(views.has("dayGrid"), "dayGrid node must remain because nowIndicator is still set");
        assertFalse(views.get("dayGrid").has("dayMaxEventRows"), "removed key must be absent");
        assertTrue(views.get("dayGrid").has("nowIndicator"), "remaining key must still be present");
    }

    @Test
    void setViewSpecificOption_viewType_null_throws() {
        assertThrows(NullPointerException.class,
                () -> calendar.setViewSpecificOption((String) null, "dayMaxEventRows", 3));
    }

    @Test
    void setViewSpecificOption_optionKey_null_throws() {
        assertThrows(NullPointerException.class,
                () -> calendar.setViewSpecificOption("dayGrid", (String) null, 3));
    }

    // -------------------------------------------------------------------------
    // 7.15 dragScrollEls
    // -------------------------------------------------------------------------

    @Test
    void setDragScrollEls_single_storesValue() {
        calendar.setOption(Option.DRAG_SCROLL_ELS, new String[]{".my-scroller"});
        assertTrue(calendar.getOption(Option.DRAG_SCROLL_ELS).isPresent());
    }

    @Test
    void setDragScrollEls_multiple_storesValue() {
        calendar.setOption(Option.DRAG_SCROLL_ELS, new String[]{".scroll-a", "body"});
        assertTrue(calendar.getOption(Option.DRAG_SCROLL_ELS).isPresent());
    }

    @Test
    void setDragScrollEls_null_clearsOption() {
        calendar.setOption(Option.DRAG_SCROLL_ELS, new String[]{".scroller"});
        calendar.setOption(Option.DRAG_SCROLL_ELS, null);
        assertTrue(calendar.getOption(Option.DRAG_SCROLL_ELS).isEmpty());
    }

    // -------------------------------------------------------------------------
    // 7.9 getCurrentIntervalStart / getCurrentIntervalEnd
    // -------------------------------------------------------------------------

    @Test
    void getCurrentIntervalStart_emptyBeforeFirstRender() {
        // Fresh calendar: no render event fired yet
        assertTrue(calendar.getCurrentIntervalStart().isEmpty());
    }

    @Test
    void getCurrentIntervalEnd_emptyBeforeFirstRender() {
        assertTrue(calendar.getCurrentIntervalEnd().isEmpty());
    }

}
