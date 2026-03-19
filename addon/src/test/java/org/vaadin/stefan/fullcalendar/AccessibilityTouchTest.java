package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.FullCalendar.Option;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.vaadin.stefan.fullcalendar.TestUtils.assertOptionalEquals;

/**
 * Tests for accessibility, touch, and print options.
 * Covers eventInteractive, accessibility hint setters, and Entry.interactive field.
 */
public class AccessibilityTouchTest {

    private FullCalendar calendar;

    @BeforeEach
    void setUp() {
        calendar = new FullCalendar();
    }

    // -------------------------------------------------------------------------
    // Option enum keys
    // -------------------------------------------------------------------------

    @Test
    void option_eventInteractive_key() {
        assertEquals("eventInteractive", Option.EVENT_INTERACTIVE.getOptionKey());
    }

    @Test
    void option_buttonHints_key() {
        assertEquals("buttonHints", Option.NATIVE_TOOLBAR_BUTTON_HINTS.getOptionKey());
    }

    @Test
    void option_viewHint_key() {
        assertEquals("viewHint", Option.NATIVE_TOOLBAR_VIEW_HINT.getOptionKey());
    }

    @Test
    void option_navLinkHint_key() {
        assertEquals("navLinkHint", Option.NAV_LINK_HINT.getOptionKey());
    }

    @Test
    void option_moreLinkHint_key() {
        assertEquals("moreLinkHint", Option.MORE_LINK_HINT.getOptionKey());
    }

    @Test
    void option_closeHint_key() {
        assertEquals("closeHint", Option.CLOSE_HINT.getOptionKey());
    }

    @Test
    void option_timeHint_key() {
        assertEquals("timeHint", Option.TIME_HINT.getOptionKey());
    }

    @Test
    void option_eventHint_key() {
        assertEquals("eventHint", Option.EVENT_HINT.getOptionKey());
    }

    // -------------------------------------------------------------------------
    // setEventInteractive
    // -------------------------------------------------------------------------

    @Test
    void setEventInteractive_true_storesOption() {
        calendar.setOption(Option.EVENT_INTERACTIVE, true);
        assertOptionalEquals(true, calendar.getOption(Option.EVENT_INTERACTIVE));
    }

    @Test
    void setEventInteractive_false_storesOption() {
        calendar.setOption(Option.EVENT_INTERACTIVE, false);
        assertOptionalEquals(false, calendar.getOption(Option.EVENT_INTERACTIVE));
    }

    // -------------------------------------------------------------------------
    // setButtonHints
    // -------------------------------------------------------------------------

    @Test
    void setButtonHints_storesMap() {
        Map<String, String> hints = Map.of("today", "Go to today", "prev", "Previous period", "next", "Next period");
        calendar.setOption(Option.NATIVE_TOOLBAR_BUTTON_HINTS, hints);
        assertOptionalEquals(hints, calendar.getOption(Option.NATIVE_TOOLBAR_BUTTON_HINTS));
    }

    @Test
    void setButtonHints_null_clearsOption() {
        calendar.setOption(Option.NATIVE_TOOLBAR_BUTTON_HINTS, Map.of("today", "Go to today"));
        calendar.setOption(Option.NATIVE_TOOLBAR_BUTTON_HINTS, null);
        // null clears the option from the map
        assertTrue(calendar.getOption(Option.NATIVE_TOOLBAR_BUTTON_HINTS).isEmpty());
    }

    // -------------------------------------------------------------------------
    // setViewHint
    // -------------------------------------------------------------------------

    @Test
    void setViewHint_storesOption() {
        calendar.setOption(Option.NATIVE_TOOLBAR_VIEW_HINT, "Switch to $0 view");
        assertOptionalEquals("Switch to $0 view", calendar.getOption(Option.NATIVE_TOOLBAR_VIEW_HINT));
    }

    @Test
    void setViewHint_null_clearsOption() {
        calendar.setOption(Option.NATIVE_TOOLBAR_VIEW_HINT, "Switch to $0 view");
        calendar.setOption(Option.NATIVE_TOOLBAR_VIEW_HINT, null);
        assertTrue(calendar.getOption(Option.NATIVE_TOOLBAR_VIEW_HINT).isEmpty());
    }

    // -------------------------------------------------------------------------
    // setNavLinkHint
    // -------------------------------------------------------------------------

    @Test
    void setNavLinkHint_storesOption() {
        calendar.setOption(Option.NAV_LINK_HINT, "Go to $0");
        assertOptionalEquals("Go to $0", calendar.getOption(Option.NAV_LINK_HINT));
    }

    // -------------------------------------------------------------------------
    // setMoreLinkHint
    // -------------------------------------------------------------------------

    @Test
    void setMoreLinkHint_storesOption() {
        calendar.setOption(Option.MORE_LINK_HINT, "$0 more events. Click to expand");
        assertOptionalEquals("$0 more events. Click to expand", calendar.getOption(Option.MORE_LINK_HINT));
    }

    // -------------------------------------------------------------------------
    // setCloseHint
    // -------------------------------------------------------------------------

    @Test
    void setCloseHint_storesOption() {
        calendar.setOption(Option.CLOSE_HINT, "Close");
        assertOptionalEquals("Close", calendar.getOption(Option.CLOSE_HINT));
    }

    // -------------------------------------------------------------------------
    // setTimeHint
    // -------------------------------------------------------------------------

    @Test
    void setTimeHint_storesOption() {
        calendar.setOption(Option.TIME_HINT, "Time");
        assertOptionalEquals("Time", calendar.getOption(Option.TIME_HINT));
    }

    // -------------------------------------------------------------------------
    // setEventHint
    // -------------------------------------------------------------------------

    @Test
    void setEventHint_storesOption() {
        calendar.setOption(Option.EVENT_HINT, "Event");
        assertOptionalEquals("Event", calendar.getOption(Option.EVENT_HINT));
    }

    // -------------------------------------------------------------------------
    // Entry.interactive (Boolean field)
    // -------------------------------------------------------------------------

    @Test
    void entry_interactive_defaultIsNull() {
        Entry entry = new Entry();
        assertNull(entry.getInteractive(), "interactive should default to null (inherit from calendar)");
    }

    @Test
    void entry_interactive_setTrue() {
        Entry entry = new Entry();
        entry.setInteractive(true);
        assertEquals(Boolean.TRUE, entry.getInteractive());
    }

    @Test
    void entry_interactive_setFalse() {
        Entry entry = new Entry();
        entry.setInteractive(false);
        assertEquals(Boolean.FALSE, entry.getInteractive());
    }

    @Test
    void entry_interactive_setNullResetsToInherit() {
        Entry entry = new Entry();
        entry.setInteractive(true);
        entry.setInteractive(null);
        assertNull(entry.getInteractive(), "setting null should restore inherit-from-calendar semantics");
    }

    // -------------------------------------------------------------------------
    // Touch delay option keys
    // -------------------------------------------------------------------------

    @Test
    void option_longPressDelay_key() {
        assertEquals("longPressDelay", Option.LONG_PRESS_DELAY.getOptionKey());
    }

    @Test
    void option_eventLongPressDelay_key() {
        assertEquals("eventLongPressDelay", Option.EVENT_LONG_PRESS_DELAY.getOptionKey());
    }

    @Test
    void option_selectLongPressDelay_key() {
        assertEquals("selectLongPressDelay", Option.SELECT_LONG_PRESS_DELAY.getOptionKey());
    }

    @Test
    void setLongPressDelay_storesOption() {
        calendar.setOption(Option.LONG_PRESS_DELAY, 500);
        assertOptionalEquals(500, calendar.getOption(Option.LONG_PRESS_DELAY));
    }

    @Test
    void setEventLongPressDelay_storesOption() {
        calendar.setOption(Option.EVENT_LONG_PRESS_DELAY, 300);
        assertOptionalEquals(300, calendar.getOption(Option.EVENT_LONG_PRESS_DELAY));
    }

    @Test
    void setSelectLongPressDelay_storesOption() {
        calendar.setOption(Option.SELECT_LONG_PRESS_DELAY, 400);
        assertOptionalEquals(400, calendar.getOption(Option.SELECT_LONG_PRESS_DELAY));
    }
}
