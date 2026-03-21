package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.FullCalendar.Option;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.vaadin.stefan.fullcalendar.TestUtils.assertOptionalEquals;

/**
 * Tests for display options and render hook callbacks.
 */
public class DisplayOptionsTest {

    private FullCalendar calendar;

    @BeforeEach
    void setUp() {
        calendar = new FullCalendar();
    }

    // -------------------------------------------------------------------------
    // Option enum keys
    // -------------------------------------------------------------------------

    @Test
    void option_displayEventEnd_key() {
        assertEquals("displayEventEnd", Option.DISPLAY_EVENT_END.getOptionKey());
    }

    @Test
    void option_progressiveEventRendering_key() {
        assertEquals("progressiveEventRendering", Option.PROGRESSIVE_EVENT_RENDERING.getOptionKey());
    }

    @Test
    void option_rerenderDelay_key() {
        assertEquals("rerenderDelay", Option.RERENDER_DELAY.getOptionKey());
    }

    @Test
    void option_nowIndicatorSnap_key() {
        assertEquals("nowIndicatorSnap", Option.NOW_INDICATOR_SNAP.getOptionKey());
    }

    @Test
    void option_defaultRangeSeparator_key() {
        assertEquals("defaultRangeSeparator", Option.NATIVE_TOOLBAR_DEFAULT_RANGE_SEPARATOR.getOptionKey());
    }

    @Test
    void option_buttonText_key() {
        assertEquals("buttonText", Option.NATIVE_TOOLBAR_BUTTON_TEXT.getOptionKey());
    }

    @Test
    void option_titleRangeSeparator_key() {
        assertEquals("titleRangeSeparator", Option.NATIVE_TOOLBAR_TITLE_RANGE_SEPARATOR.getOptionKey());
    }

    @Test
    void option_dayPopoverFormat_key() {
        assertEquals("dayPopoverFormat", Option.DAY_POPOVER_FORMAT.getOptionKey());
    }

    @Test
    void option_dayMaxEventRows_key() {
        assertEquals("dayMaxEventRows", Option.DAY_MAX_EVENT_ROWS.getOptionKey());
    }

    @Test
    void option_longPressDelay_key() {
        assertEquals("longPressDelay", Option.LONG_PRESS_DELAY.getOptionKey());
    }

    @Test
    void option_eventLongPressDelay_key() {
        assertEquals("eventLongPressDelay", Option.ENTRY_LONG_PRESS_DELAY.getOptionKey());
    }

    @Test
    void option_selectLongPressDelay_key() {
        assertEquals("selectLongPressDelay", Option.SELECT_LONG_PRESS_DELAY.getOptionKey());
    }

    @Test
    void option_dragRevertDuration_key() {
        assertEquals("dragRevertDuration", Option.DRAG_REVERT_DURATION.getOptionKey());
    }

    @Test
    void option_allDayMaintainDuration_key() {
        assertEquals("allDayMaintainDuration", Option.ALL_DAY_MAINTAIN_DURATION.getOptionKey());
    }

    @Test
    void option_eventDragMinDistance_key() {
        assertEquals("eventDragMinDistance", Option.ENTRY_DRAG_MIN_DISTANCE.getOptionKey());
    }

    @Test
    void option_lazyFetching_key() {
        assertEquals("lazyFetching", Option.LAZY_FETCHING.getOptionKey());
    }

    @Test
    void option_forceEventDuration_key() {
        assertEquals("forceEventDuration", Option.FORCE_EVENT_DURATION.getOptionKey());
    }

    @Test
    void option_defaultAllDay_key() {
        assertEquals("defaultAllDay", Option.DEFAULT_ALL_DAY.getOptionKey());
    }

    // -------------------------------------------------------------------------
    // setDisplayEventEnd
    // -------------------------------------------------------------------------

    @Test
    void setDisplayEventEnd_storesOption() {
        calendar.setOption(Option.DISPLAY_EVENT_END, true);
        assertOptionalEquals(true, calendar.getOption(Option.DISPLAY_EVENT_END));

        calendar.setOption(Option.DISPLAY_EVENT_END, false);
        assertOptionalEquals(false, calendar.getOption(Option.DISPLAY_EVENT_END));
    }

    // -------------------------------------------------------------------------
    // setDisplayEntryTime (DISPLAY_ENTRY_TIME)
    // -------------------------------------------------------------------------

    @Test
    void setDisplayEntryTime_setsDisplayEntryTimeOption() {
        // DISPLAY_ENTRY_TIME uses ENTRY→EVENT key replacement → FC option key is "displayEventTime"
        assertEquals("displayEventTime", Option.DISPLAY_ENTRY_TIME.getOptionKey());

        calendar.setOption(Option.DISPLAY_ENTRY_TIME, true);
        assertOptionalEquals(true, calendar.getOption(Option.DISPLAY_ENTRY_TIME));

        calendar.setOption(Option.DISPLAY_ENTRY_TIME, false);
        assertOptionalEquals(false, calendar.getOption(Option.DISPLAY_ENTRY_TIME));
    }

    // -------------------------------------------------------------------------
    // setProgressiveEventRendering
    // -------------------------------------------------------------------------

    @Test
    void setProgressiveEventRendering_storesOption() {
        calendar.setOption(Option.PROGRESSIVE_EVENT_RENDERING, true);
        assertOptionalEquals(true, calendar.getOption(Option.PROGRESSIVE_EVENT_RENDERING));
    }

    // -------------------------------------------------------------------------
    // setRerenderDelay
    // -------------------------------------------------------------------------

    @Test
    void setRerenderDelay_storesOption() {
        calendar.setOption(Option.RERENDER_DELAY, 200);
        assertOptionalEquals(200, calendar.getOption(Option.RERENDER_DELAY));
    }

    @Test
    void setRerenderDelay_negativeOneToDisable() {
        calendar.setOption(Option.RERENDER_DELAY, -1);
        assertOptionalEquals(-1, calendar.getOption(Option.RERENDER_DELAY));
    }

    // -------------------------------------------------------------------------
    // setNowIndicatorSnap
    // -------------------------------------------------------------------------

    @Test
    void setNowIndicatorSnap_storesOption() {
        calendar.setOption(Option.NOW_INDICATOR_SNAP, true);
        assertOptionalEquals(true, calendar.getOption(Option.NOW_INDICATOR_SNAP));

        calendar.setOption(Option.NOW_INDICATOR_SNAP, false);
        assertOptionalEquals(false, calendar.getOption(Option.NOW_INDICATOR_SNAP));
    }

    // -------------------------------------------------------------------------
    // setDefaultRangeSeparator / setTitleRangeSeparator
    // -------------------------------------------------------------------------

    @Test
    void setDefaultRangeSeparator_storesOption() {
        calendar.setOption(Option.NATIVE_TOOLBAR_DEFAULT_RANGE_SEPARATOR, " – ");
        assertOptionalEquals(" – ", calendar.getOption(Option.NATIVE_TOOLBAR_DEFAULT_RANGE_SEPARATOR));
    }

    @Test
    void setTitleRangeSeparator_storesOption() {
        calendar.setOption(Option.NATIVE_TOOLBAR_TITLE_RANGE_SEPARATOR, " / ");
        assertOptionalEquals(" / ", calendar.getOption(Option.NATIVE_TOOLBAR_TITLE_RANGE_SEPARATOR));
    }

    // -------------------------------------------------------------------------
    // buttonText
    // -------------------------------------------------------------------------

    @Test
    void setButtonText_storesMap() {
        Map<String, String> labels = Map.of("today", "Jetzt", "month", "Monat");
        calendar.setOption(Option.NATIVE_TOOLBAR_BUTTON_TEXT, labels);
        Optional<Object> opt = calendar.getOption(Option.NATIVE_TOOLBAR_BUTTON_TEXT);
        assertTrue(opt.isPresent());
        assertTrue(opt.get() instanceof Map, "buttonText option should be stored as a Map");
        @SuppressWarnings("unchecked")
        Map<String, String> stored = (Map<String, String>) opt.get();
        assertEquals("Jetzt", stored.get("today"));
        assertEquals("Monat", stored.get("month"));
    }

    // -------------------------------------------------------------------------
    // setDayPopoverFormat
    // -------------------------------------------------------------------------

    @Test
    void setDayPopoverFormat_storesOption() {
        calendar.setOption(Option.DAY_POPOVER_FORMAT, "{ weekday: 'long', month: 'long', day: 'numeric' }");
        Optional<String> opt = calendar.getOption(Option.DAY_POPOVER_FORMAT);
        assertTrue(opt.isPresent());
    }

    // -------------------------------------------------------------------------
    // setDayMaxEventRows / setDayMaxEventRowsFitToCell
    // -------------------------------------------------------------------------

    @Test
    void setDayMaxEventRows_storesInt() {
        calendar.setOption(Option.DAY_MAX_EVENT_ROWS, 3);
        assertOptionalEquals(3, calendar.getOption(Option.DAY_MAX_EVENT_ROWS));
    }

    @Test
    void setDayMaxEventRowsFitToCell_storesTrue() {
        calendar.setOption(Option.DAY_MAX_EVENT_ROWS, true);
        assertOptionalEquals(true, calendar.getOption(Option.DAY_MAX_EVENT_ROWS));
    }

    @Test
    void setDayMaxEventRows_afterFitToCell_overridesWithInt() {
        // setDayMaxEventRowsFitToCell() stores true; a subsequent int call must overwrite it
        calendar.setOption(Option.DAY_MAX_EVENT_ROWS, true);
        calendar.setOption(Option.DAY_MAX_EVENT_ROWS, 5);
        assertOptionalEquals(5, calendar.getOption(Option.DAY_MAX_EVENT_ROWS));
    }

    // -------------------------------------------------------------------------
    // Long-press delays
    // -------------------------------------------------------------------------

    @Test
    void setLongPressDelay_storesOption() {
        calendar.setOption(Option.LONG_PRESS_DELAY, 800);
        assertOptionalEquals(800, calendar.getOption(Option.LONG_PRESS_DELAY));
    }

    @Test
    void setEventLongPressDelay_storesOption() {
        calendar.setOption(Option.ENTRY_LONG_PRESS_DELAY, 600);
        assertOptionalEquals(600, calendar.getOption(Option.ENTRY_LONG_PRESS_DELAY));
    }

    @Test
    void setSelectLongPressDelay_storesOption() {
        calendar.setOption(Option.SELECT_LONG_PRESS_DELAY, 700);
        assertOptionalEquals(700, calendar.getOption(Option.SELECT_LONG_PRESS_DELAY));
    }

    // -------------------------------------------------------------------------
    // Drag/drop options
    // -------------------------------------------------------------------------

    @Test
    void setDragRevertDuration_storesOption() {
        calendar.setOption(Option.DRAG_REVERT_DURATION, 300);
        assertOptionalEquals(300, calendar.getOption(Option.DRAG_REVERT_DURATION));
    }

    @Test
    void setAllDayMaintainDuration_storesOption() {
        calendar.setOption(Option.ALL_DAY_MAINTAIN_DURATION, true);
        assertOptionalEquals(true, calendar.getOption(Option.ALL_DAY_MAINTAIN_DURATION));
    }

    @Test
    void setEventDragMinDistance_storesOption() {
        calendar.setOption(Option.ENTRY_DRAG_MIN_DISTANCE, 10);
        assertOptionalEquals(10, calendar.getOption(Option.ENTRY_DRAG_MIN_DISTANCE));
    }

    // -------------------------------------------------------------------------
    // Fetch / rendering options
    // -------------------------------------------------------------------------

    @Test
    void setLazyFetching_storesOption() {
        calendar.setOption(Option.LAZY_FETCHING, false);
        assertOptionalEquals(false, calendar.getOption(Option.LAZY_FETCHING));
    }

    @Test
    void setForceEventDuration_storesOption() {
        calendar.setOption(Option.FORCE_EVENT_DURATION, true);
        assertOptionalEquals(true, calendar.getOption(Option.FORCE_EVENT_DURATION));
    }

    @Test
    void setDefaultAllDay_storesOption() {
        calendar.setOption(Option.DEFAULT_ALL_DAY, true);
        assertOptionalEquals(true, calendar.getOption(Option.DEFAULT_ALL_DAY));
    }

}
