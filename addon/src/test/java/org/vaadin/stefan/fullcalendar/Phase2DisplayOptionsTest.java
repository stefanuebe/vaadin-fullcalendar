package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.FullCalendar.Option;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.vaadin.stefan.fullcalendar.TestUtils.assertOptionalEquals;

/**
 * Tests for Phase 2: Missing display options and render hook callbacks.
 * Covers all new typed setters in FullCalendar and the ThemeSystem enum.
 */
public class Phase2DisplayOptionsTest {

    private FullCalendar calendar;

    @BeforeEach
    void setUp() {
        calendar = new FullCalendar();
    }

    // -------------------------------------------------------------------------
    // ThemeSystem enum
    // -------------------------------------------------------------------------

    @Test
    void themeSystem_clientSideValues() {
        assertEquals("standard", ThemeSystem.STANDARD.getClientSideValue());
        assertEquals("bootstrap5", ThemeSystem.BOOTSTRAP5.getClientSideValue());
        assertEquals("bootstrap", ThemeSystem.BOOTSTRAP.getClientSideValue());
    }

    // -------------------------------------------------------------------------
    // Option enum keys — Phase 2 additions
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
    void option_now_key() {
        assertEquals("now", Option.NOW.getOptionKey());
    }

    @Test
    void option_nowIndicatorSnap_key() {
        assertEquals("nowIndicatorSnap", Option.NOW_INDICATOR_SNAP.getOptionKey());
    }

    @Test
    void option_windowResizeDelay_key() {
        assertEquals("windowResizeDelay", Option.WINDOW_RESIZE_DELAY.getOptionKey());
    }

    @Test
    void option_initialDate_key() {
        assertEquals("initialDate", Option.INITIAL_DATE.getOptionKey());
    }

    @Test
    void option_initialView_key() {
        assertEquals("initialView", Option.INITIAL_VIEW.getOptionKey());
    }

    @Test
    void option_themeSystem_key() {
        assertEquals("themeSystem", Option.THEME_SYSTEM.getOptionKey());
    }

    @Test
    void option_defaultRangeSeparator_key() {
        assertEquals("defaultRangeSeparator", Option.DEFAULT_RANGE_SEPARATOR.getOptionKey());
    }

    @Test
    void option_buttonText_key() {
        assertEquals("buttonText", Option.BUTTON_TEXT.getOptionKey());
    }

    @Test
    void option_titleRangeSeparator_key() {
        assertEquals("titleRangeSeparator", Option.TITLE_RANGE_SEPARATOR.getOptionKey());
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
        assertEquals("eventLongPressDelay", Option.EVENT_LONG_PRESS_DELAY.getOptionKey());
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
        assertEquals("eventDragMinDistance", Option.EVENT_DRAG_MIN_DISTANCE.getOptionKey());
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

    @Test
    void option_handleWindowResize_key() {
        assertEquals("handleWindowResize", Option.HANDLE_WINDOW_RESIZE.getOptionKey());
    }

    // -------------------------------------------------------------------------
    // setDisplayEventEnd
    // -------------------------------------------------------------------------

    @Test
    void setDisplayEventEnd_storesOption() {
        calendar.setDisplayEventEnd(true);
        assertOptionalEquals(true, calendar.getOption(Option.DISPLAY_EVENT_END));

        calendar.setDisplayEventEnd(false);
        assertOptionalEquals(false, calendar.getOption(Option.DISPLAY_EVENT_END));
    }

    // -------------------------------------------------------------------------
    // setDisplayEventTime — delegates to setDisplayEntryTime (DISPLAY_ENTRY_TIME)
    // -------------------------------------------------------------------------

    @Test
    void setDisplayEventTime_delegatesToDisplayEntryTime() {
        calendar.setDisplayEventTime(true);
        // DISPLAY_ENTRY_TIME key = "displayEventTime" (ENTRY → EVENT replacement)
        assertOptionalEquals(true, calendar.getOption(Option.DISPLAY_ENTRY_TIME));

        calendar.setDisplayEventTime(false);
        assertOptionalEquals(false, calendar.getOption(Option.DISPLAY_ENTRY_TIME));
    }

    // -------------------------------------------------------------------------
    // setProgressiveEventRendering
    // -------------------------------------------------------------------------

    @Test
    void setProgressiveEventRendering_storesOption() {
        calendar.setProgressiveEventRendering(true);
        assertOptionalEquals(true, calendar.getOption(Option.PROGRESSIVE_EVENT_RENDERING));
    }

    // -------------------------------------------------------------------------
    // setRerenderDelay
    // -------------------------------------------------------------------------

    @Test
    void setRerenderDelay_storesOption() {
        calendar.setRerenderDelay(200);
        assertOptionalEquals(200, calendar.getOption(Option.RERENDER_DELAY));
    }

    @Test
    void setRerenderDelay_negativeOneToDisable() {
        calendar.setRerenderDelay(-1);
        assertOptionalEquals(-1, calendar.getOption(Option.RERENDER_DELAY));
    }

    // -------------------------------------------------------------------------
    // setNow
    // -------------------------------------------------------------------------

    @Test
    void setNow_LocalDate_serializedAsIsoDate() {
        calendar.setNow(LocalDate.of(2025, 3, 15));
        assertOptionalEquals("2025-03-15", calendar.getOption(Option.NOW));
    }

    @Test
    void setNow_LocalDateTime_serializedAsIsoDateTime() {
        calendar.setNow(LocalDateTime.of(2025, 3, 15, 10, 30, 0));
        assertOptionalEquals("2025-03-15T10:30:00", calendar.getOption(Option.NOW));
    }

    @Test
    void setNow_null_clearsOption() {
        calendar.setNow(LocalDate.of(2025, 1, 1));
        calendar.setNow((LocalDate) null);
        assertFalse(calendar.getOption(Option.NOW).isPresent(), "now option should be cleared");
    }

    // -------------------------------------------------------------------------
    // setNowIndicatorSnap
    // -------------------------------------------------------------------------

    @Test
    void setNowIndicatorSnap_storesOption() {
        calendar.setNowIndicatorSnap(true);
        assertOptionalEquals(true, calendar.getOption(Option.NOW_INDICATOR_SNAP));

        calendar.setNowIndicatorSnap(false);
        assertOptionalEquals(false, calendar.getOption(Option.NOW_INDICATOR_SNAP));
    }

    // -------------------------------------------------------------------------
    // setWindowResizeDelay
    // -------------------------------------------------------------------------

    @Test
    void setWindowResizeDelay_storesOption() {
        calendar.setWindowResizeDelay(250);
        assertOptionalEquals(250, calendar.getOption(Option.WINDOW_RESIZE_DELAY));
    }

    // -------------------------------------------------------------------------
    // setInitialDate
    // -------------------------------------------------------------------------

    @Test
    void setInitialDate_serializedAsIsoDate() {
        calendar.setInitialDate(LocalDate.of(2025, 6, 1));
        assertOptionalEquals("2025-06-01", calendar.getOption(Option.INITIAL_DATE));
    }

    @Test
    void setInitialDate_null_clearsOption() {
        calendar.setInitialDate(LocalDate.of(2025, 1, 1));
        calendar.setInitialDate(null);
        assertFalse(calendar.getOption(Option.INITIAL_DATE).isPresent());
    }

    // -------------------------------------------------------------------------
    // setInitialView
    // -------------------------------------------------------------------------

    @Test
    void setInitialView_storesClientSideValue() {
        calendar.setInitialView(CalendarViewImpl.DAY_GRID_MONTH);
        Optional<String> opt = calendar.getOption(Option.INITIAL_VIEW);
        assertTrue(opt.isPresent());
        assertEquals(CalendarViewImpl.DAY_GRID_MONTH.getClientSideValue(), opt.get());
    }

    @Test
    void setInitialView_null_clearsOption() {
        calendar.setInitialView(CalendarViewImpl.TIME_GRID_WEEK);
        calendar.setInitialView(null);
        assertFalse(calendar.getOption(Option.INITIAL_VIEW).isPresent());
    }

    // -------------------------------------------------------------------------
    // setThemeSystem
    // -------------------------------------------------------------------------

    @Test
    void setThemeSystem_storesClientSideValue() {
        calendar.setThemeSystem(ThemeSystem.BOOTSTRAP5);
        assertOptionalEquals("bootstrap5", calendar.getOption(Option.THEME_SYSTEM));
    }

    @Test
    void setThemeSystem_standard() {
        calendar.setThemeSystem(ThemeSystem.STANDARD);
        assertOptionalEquals("standard", calendar.getOption(Option.THEME_SYSTEM));
    }

    @Test
    void setThemeSystem_null_clearsOption() {
        calendar.setThemeSystem(ThemeSystem.BOOTSTRAP5);
        calendar.setThemeSystem(null);
        assertFalse(calendar.getOption(Option.THEME_SYSTEM).isPresent());
    }

    // -------------------------------------------------------------------------
    // setDefaultRangeSeparator / setTitleRangeSeparator
    // -------------------------------------------------------------------------

    @Test
    void setDefaultRangeSeparator_storesOption() {
        calendar.setDefaultRangeSeparator(" – ");
        assertOptionalEquals(" – ", calendar.getOption(Option.DEFAULT_RANGE_SEPARATOR));
    }

    @Test
    void setTitleRangeSeparator_storesOption() {
        calendar.setTitleRangeSeparator(" / ");
        assertOptionalEquals(" / ", calendar.getOption(Option.TITLE_RANGE_SEPARATOR));
    }

    // -------------------------------------------------------------------------
    // setButtonText
    // -------------------------------------------------------------------------

    @Test
    void setButtonText_storesMap() {
        Map<String, String> labels = Map.of("today", "Jetzt", "month", "Monat");
        calendar.setButtonText(labels);
        Optional<Object> opt = calendar.getOption(Option.BUTTON_TEXT);
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
        calendar.setDayPopoverFormat("{ weekday: 'long', month: 'long', day: 'numeric' }");
        Optional<String> opt = calendar.getOption(Option.DAY_POPOVER_FORMAT);
        assertTrue(opt.isPresent());
    }

    // -------------------------------------------------------------------------
    // setDayMaxEventRows / setDayMaxEventRowsFitToCell
    // -------------------------------------------------------------------------

    @Test
    void setDayMaxEventRows_storesInt() {
        calendar.setDayMaxEventRows(3);
        assertOptionalEquals(3, calendar.getOption(Option.DAY_MAX_EVENT_ROWS));
    }

    @Test
    void setDayMaxEventRowsFitToCell_storesTrue() {
        calendar.setDayMaxEventRowsFitToCell();
        assertOptionalEquals(true, calendar.getOption(Option.DAY_MAX_EVENT_ROWS));
    }

    @Test
    void setDayMaxEventRows_overridesFitToCell() {
        calendar.setDayMaxEventRowsFitToCell();
        calendar.setDayMaxEventRows(5);
        assertOptionalEquals(5, calendar.getOption(Option.DAY_MAX_EVENT_ROWS));
    }

    // -------------------------------------------------------------------------
    // Long-press delays
    // -------------------------------------------------------------------------

    @Test
    void setLongPressDelay_storesOption() {
        calendar.setLongPressDelay(800);
        assertOptionalEquals(800, calendar.getOption(Option.LONG_PRESS_DELAY));
    }

    @Test
    void setEventLongPressDelay_storesOption() {
        calendar.setEventLongPressDelay(600);
        assertOptionalEquals(600, calendar.getOption(Option.EVENT_LONG_PRESS_DELAY));
    }

    @Test
    void setSelectLongPressDelay_storesOption() {
        calendar.setSelectLongPressDelay(700);
        assertOptionalEquals(700, calendar.getOption(Option.SELECT_LONG_PRESS_DELAY));
    }

    // -------------------------------------------------------------------------
    // Drag/drop options
    // -------------------------------------------------------------------------

    @Test
    void setDragRevertDuration_storesOption() {
        calendar.setDragRevertDuration(300);
        assertOptionalEquals(300, calendar.getOption(Option.DRAG_REVERT_DURATION));
    }

    @Test
    void setAllDayMaintainDuration_storesOption() {
        calendar.setAllDayMaintainDuration(true);
        assertOptionalEquals(true, calendar.getOption(Option.ALL_DAY_MAINTAIN_DURATION));
    }

    @Test
    void setEventDragMinDistance_storesOption() {
        calendar.setEventDragMinDistance(10);
        assertOptionalEquals(10, calendar.getOption(Option.EVENT_DRAG_MIN_DISTANCE));
    }

    // -------------------------------------------------------------------------
    // Fetch / rendering options
    // -------------------------------------------------------------------------

    @Test
    void setLazyFetching_storesOption() {
        calendar.setLazyFetching(false);
        assertOptionalEquals(false, calendar.getOption(Option.LAZY_FETCHING));
    }

    @Test
    void setForceEventDuration_storesOption() {
        calendar.setForceEventDuration(true);
        assertOptionalEquals(true, calendar.getOption(Option.FORCE_EVENT_DURATION));
    }

    @Test
    void setDefaultAllDay_storesOption() {
        calendar.setDefaultAllDay(true);
        assertOptionalEquals(true, calendar.getOption(Option.DEFAULT_ALL_DAY));
    }

    @Test
    void setHandleWindowResize_storesOption() {
        calendar.setHandleWindowResize(false);
        assertOptionalEquals(false, calendar.getOption(Option.HANDLE_WINDOW_RESIZE));
    }

    // -------------------------------------------------------------------------
    // Render hook callbacks — verify option key is set correctly
    // -------------------------------------------------------------------------

    @Test
    void setDayCellClassNamesCallback_storesOptionByKey() {
        String fn = "function(info) { return ['test-class']; }";
        calendar.setDayCellClassNamesCallback(fn);
        assertOptionalEquals(fn, calendar.getOption("dayCellClassNames"));
    }

    @Test
    void setDayCellContentCallback_storesOptionByKey() {
        String fn = "function(info) { return { html: '<b>' + info.dayNumberText + '</b>' }; }";
        calendar.setDayCellContentCallback(fn);
        assertOptionalEquals(fn, calendar.getOption("dayCellContent"));
    }

    @Test
    void setDayCellDidMountCallback_storesOptionByKey() {
        String fn = "function(info) {}";
        calendar.setDayCellDidMountCallback(fn);
        assertOptionalEquals(fn, calendar.getOption("dayCellDidMount"));
    }

    @Test
    void setDayCellWillUnmountCallback_storesOptionByKey() {
        String fn = "function(info) {}";
        calendar.setDayCellWillUnmountCallback(fn);
        assertOptionalEquals(fn, calendar.getOption("dayCellWillUnmount"));
    }

    @Test
    void setDayHeaderClassNamesCallback_storesOptionByKey() {
        String fn = "function(info) { return ['hdr']; }";
        calendar.setDayHeaderClassNamesCallback(fn);
        assertOptionalEquals(fn, calendar.getOption("dayHeaderClassNames"));
    }

    @Test
    void setDayHeaderContentCallback_storesOptionByKey() {
        String fn = "function(info) { return { html: info.text }; }";
        calendar.setDayHeaderContentCallback(fn);
        assertOptionalEquals(fn, calendar.getOption("dayHeaderContent"));
    }

    @Test
    void setDayHeaderDidMountCallback_storesOptionByKey() {
        String fn = "function(info) {}";
        calendar.setDayHeaderDidMountCallback(fn);
        assertOptionalEquals(fn, calendar.getOption("dayHeaderDidMount"));
    }

    @Test
    void setDayHeaderWillUnmountCallback_storesOptionByKey() {
        String fn = "function(info) {}";
        calendar.setDayHeaderWillUnmountCallback(fn);
        assertOptionalEquals(fn, calendar.getOption("dayHeaderWillUnmount"));
    }

    @Test
    void setSlotLabelClassNamesCallback_storesOptionByKey() {
        String fn = "function(info) { return []; }";
        calendar.setSlotLabelClassNamesCallback(fn);
        assertOptionalEquals(fn, calendar.getOption("slotLabelClassNames"));
    }

    @Test
    void setSlotLabelContentCallback_storesOptionByKey() {
        String fn = "function(info) { return { text: info.text }; }";
        calendar.setSlotLabelContentCallback(fn);
        assertOptionalEquals(fn, calendar.getOption("slotLabelContent"));
    }

    @Test
    void setSlotLaneClassNamesCallback_storesOptionByKey() {
        calendar.setSlotLaneClassNamesCallback("function(info) { return []; }");
        assertTrue(calendar.getOption("slotLaneClassNames").isPresent());
    }

    @Test
    void setSlotLaneContentCallback_storesOptionByKey() {
        calendar.setSlotLaneContentCallback("function(info) { return {}; }");
        assertTrue(calendar.getOption("slotLaneContent").isPresent());
    }

    @Test
    void setSlotLaneDidMountCallback_storesOptionByKey() {
        calendar.setSlotLaneDidMountCallback("function(info) {}");
        assertTrue(calendar.getOption("slotLaneDidMount").isPresent());
    }

    @Test
    void setSlotLaneWillUnmountCallback_storesOptionByKey() {
        calendar.setSlotLaneWillUnmountCallback("function(info) {}");
        assertTrue(calendar.getOption("slotLaneWillUnmount").isPresent());
    }

    @Test
    void setViewClassNamesCallback_storesOptionByKey() {
        calendar.setViewClassNamesCallback("function(info) { return []; }");
        assertTrue(calendar.getOption("viewClassNames").isPresent());
    }

    @Test
    void setViewDidMountCallback_storesOptionByKey() {
        calendar.setViewDidMountCallback("function(info) {}");
        assertTrue(calendar.getOption("viewDidMount").isPresent());
    }

    @Test
    void setViewWillUnmountCallback_storesOptionByKey() {
        calendar.setViewWillUnmountCallback("function(info) {}");
        assertTrue(calendar.getOption("viewWillUnmount").isPresent());
    }

    @Test
    void setNowIndicatorClassNamesCallback_storesOptionByKey() {
        calendar.setNowIndicatorClassNamesCallback("function(info) { return []; }");
        assertTrue(calendar.getOption("nowIndicatorClassNames").isPresent());
    }

    @Test
    void setNowIndicatorContentCallback_storesOptionByKey() {
        calendar.setNowIndicatorContentCallback("function(info) { return {}; }");
        assertTrue(calendar.getOption("nowIndicatorContent").isPresent());
    }

    @Test
    void setNowIndicatorDidMountCallback_storesOptionByKey() {
        calendar.setNowIndicatorDidMountCallback("function(info) {}");
        assertTrue(calendar.getOption("nowIndicatorDidMount").isPresent());
    }

    @Test
    void setNowIndicatorWillUnmountCallback_storesOptionByKey() {
        calendar.setNowIndicatorWillUnmountCallback("function(info) {}");
        assertTrue(calendar.getOption("nowIndicatorWillUnmount").isPresent());
    }

    @Test
    void setWeekNumberClassNamesCallback_storesOptionByKey() {
        calendar.setWeekNumberClassNamesCallback("function(info) { return []; }");
        assertTrue(calendar.getOption("weekNumberClassNames").isPresent());
    }

    @Test
    void setWeekNumberContentCallback_storesOptionByKey() {
        String fn = "function(info) { return { html: '<span>' + info.text + '</span>' }; }";
        calendar.setWeekNumberContentCallback(fn);
        assertOptionalEquals(fn, calendar.getOption("weekNumberContent"));
    }

    @Test
    void setWeekNumberDidMountCallback_storesOptionByKey() {
        calendar.setWeekNumberDidMountCallback("function(info) {}");
        assertTrue(calendar.getOption("weekNumberDidMount").isPresent());
    }

    @Test
    void setWeekNumberWillUnmountCallback_storesOptionByKey() {
        calendar.setWeekNumberWillUnmountCallback("function(info) {}");
        assertTrue(calendar.getOption("weekNumberWillUnmount").isPresent());
    }

    @Test
    void setMoreLinkClassNamesCallback_storesOptionByKey() {
        calendar.setMoreLinkClassNamesCallback("function(info) { return []; }");
        assertTrue(calendar.getOption("moreLinkClassNames").isPresent());
    }

    @Test
    void setMoreLinkContentCallback_storesOptionByKey() {
        calendar.setMoreLinkContentCallback("function(info) { return {}; }");
        assertTrue(calendar.getOption("moreLinkContent").isPresent());
    }

    @Test
    void setMoreLinkDidMountCallback_storesOptionByKey() {
        calendar.setMoreLinkDidMountCallback("function(info) {}");
        assertTrue(calendar.getOption("moreLinkDidMount").isPresent());
    }

    @Test
    void setMoreLinkWillUnmountCallback_storesOptionByKey() {
        calendar.setMoreLinkWillUnmountCallback("function(info) {}");
        assertTrue(calendar.getOption("moreLinkWillUnmount").isPresent());
    }

    @Test
    void setNoEventsClassNamesCallback_storesOptionByKey() {
        calendar.setNoEventsClassNamesCallback("function(info) { return []; }");
        assertTrue(calendar.getOption("noEventsClassNames").isPresent());
    }

    @Test
    void setNoEventsContentCallback_storesOptionByKey() {
        calendar.setNoEventsContentCallback("function(info) { return {}; }");
        assertTrue(calendar.getOption("noEventsContent").isPresent());
    }

    @Test
    void setNoEventsDidMountCallback_storesOptionByKey() {
        calendar.setNoEventsDidMountCallback("function(info) {}");
        assertTrue(calendar.getOption("noEventsDidMount").isPresent());
    }

    @Test
    void setNoEventsWillUnmountCallback_storesOptionByKey() {
        calendar.setNoEventsWillUnmountCallback("function(info) {}");
        assertTrue(calendar.getOption("noEventsWillUnmount").isPresent());
    }

    @Test
    void setAllDayClassNamesCallback_storesOptionByKey() {
        calendar.setAllDayClassNamesCallback("function(info) { return []; }");
        assertTrue(calendar.getOption("allDayClassNames").isPresent());
    }

    @Test
    void setAllDayContentCallback_storesOptionByKey() {
        String fn = "function(info) { return { text: 'todo el día' }; }";
        calendar.setAllDayContentCallback(fn);
        assertOptionalEquals(fn, calendar.getOption("allDayContent"));
    }

    @Test
    void setAllDayDidMountCallback_storesOptionByKey() {
        calendar.setAllDayDidMountCallback("function(info) {}");
        assertTrue(calendar.getOption("allDayDidMount").isPresent());
    }

    @Test
    void setAllDayWillUnmountCallback_storesOptionByKey() {
        calendar.setAllDayWillUnmountCallback("function(info) {}");
        assertTrue(calendar.getOption("allDayWillUnmount").isPresent());
    }
}
