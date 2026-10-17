package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.vaadin.stefan.fullcalendar.option.AutoBoolean;
import org.vaadin.stefan.fullcalendar.option.DateFormat;
import org.vaadin.stefan.fullcalendar.option.EntryLimitValue;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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
    // setDisplayEventEnd
    // -------------------------------------------------------------------------

    @Test
    void setDisplayEventEnd_storesOption() {
        calendar.options().setDisplayEntryEnd(true);
        assertEquals(true, calendar.options().getDisplayEntryEnd());

        calendar.options().setDisplayEntryEnd(false);
        assertEquals(false, calendar.options().getDisplayEntryEnd());
    }

    // -------------------------------------------------------------------------
    // setDisplayEntryTime (DISPLAY_ENTRY_TIME)
    // -------------------------------------------------------------------------

    @Test
    void setDisplayEntryTime_setsDisplayEntryTimeOption() {
        calendar.options().setDisplayEntryTime(true);
        assertEquals(true, calendar.options().getDisplayEntryTime());

        calendar.options().setDisplayEntryTime(false);
        assertEquals(false, calendar.options().getDisplayEntryTime());
    }

    // -------------------------------------------------------------------------
    // setProgressiveEventRendering
    // -------------------------------------------------------------------------

    @Test
    void setProgressiveEventRendering_storesOption() {
        calendar.options().setProgressiveEntryRendering(true);
        assertEquals(true, calendar.options().getProgressiveEntryRendering());
    }

    // -------------------------------------------------------------------------
    // setRerenderDelay
    // -------------------------------------------------------------------------

    @Test
    void setRerenderDelay_storesOption() {
        calendar.options().setRerenderDelay(200);
        assertEquals(200, calendar.options().getRerenderDelay());
    }

    @Test
    void setRerenderDelay_negativeOneToDisable() {
        calendar.options().setRerenderDelay(-1);
        assertEquals(-1, calendar.options().getRerenderDelay());
    }

    // -------------------------------------------------------------------------
    // setNowIndicatorSnap
    // -------------------------------------------------------------------------

    @Test
    void setNowIndicatorSnap_storesOption() {
        calendar.options().setNowIndicatorSnap(AutoBoolean.TRUE);
        assertEquals(AutoBoolean.TRUE, calendar.options().getNowIndicatorSnap());

        calendar.options().setNowIndicatorSnap(AutoBoolean.FALSE);
        assertEquals(AutoBoolean.FALSE, calendar.options().getNowIndicatorSnap());
    }

    // -------------------------------------------------------------------------
    // popoverFormat
    // -------------------------------------------------------------------------

    @Test
    void setPopoverFormat_storesOption() {
        var format = DateFormat.of(Map.of("weekday", "long", "month", "long", "day", "numeric"));
        calendar.options().setPopoverFormat(format);
        assertEquals(format, calendar.options().getPopoverFormat());
    }

    // -------------------------------------------------------------------------
    // setDayMaxEventRows / setDayMaxEventRowsFitToCell
    // -------------------------------------------------------------------------

    @Test
    void setDayMaxEventRows_storesInt() {
        calendar.options().setDayMaxEntryRows(3);
        assertEquals(EntryLimitValue.of(3), calendar.options().getDayMaxEntryRows());
    }

    @Test
    void setDayMaxEventRowsFitToCell_storesTrue() {
        calendar.options().setDayMaxEntryRows(EntryLimitValue.CELL_HEIGHT);
        assertEquals(EntryLimitValue.CELL_HEIGHT, calendar.options().getDayMaxEntryRows());
    }

    @Test
    void setDayMaxEventRows_afterFitToCell_overridesWithInt() {
        // setDayMaxEventRowsFitToCell() stores true; a subsequent int call must overwrite it
        calendar.options().setDayMaxEntryRows(EntryLimitValue.CELL_HEIGHT);
        calendar.options().setDayMaxEntryRows(5);
        assertEquals(EntryLimitValue.of(5), calendar.options().getDayMaxEntryRows());
    }

    // -------------------------------------------------------------------------
    // Long-press delays
    // -------------------------------------------------------------------------

    @Test
    void setLongPressDelay_storesOption() {
        calendar.options().setLongPressDelay(800);
        assertEquals(800, calendar.options().getLongPressDelay());
    }

    @Test
    void setEventLongPressDelay_storesOption() {
        calendar.options().setEntryLongPressDelay(600);
        assertEquals(600, calendar.options().getEntryLongPressDelay());
    }

    @Test
    void setSelectLongPressDelay_storesOption() {
        calendar.options().setSelectLongPressDelay(700);
        assertEquals(700, calendar.options().getSelectLongPressDelay());
    }

    // -------------------------------------------------------------------------
    // Drag/drop options
    // -------------------------------------------------------------------------

    @Test
    void setDragRevertDuration_storesOption() {
        calendar.options().setDragRevertDuration(300);
        assertEquals(300, calendar.options().getDragRevertDuration());
    }

    @Test
    void setAllDayMaintainDuration_storesOption() {
        calendar.options().setAllDayMaintainDuration(true);
        assertEquals(true, calendar.options().getAllDayMaintainDuration());
    }

    @Test
    void setEventDragMinDistance_storesOption() {
        calendar.options().setEntryDragMinDistance(10);
        assertEquals(10, calendar.options().getEntryDragMinDistance());
    }

    // -------------------------------------------------------------------------
    // Fetch / rendering options
    // -------------------------------------------------------------------------

    @Test
    void setLazyFetching_storesOption() {
        calendar.options().setLazyFetching(false);
        assertEquals(false, calendar.options().getLazyFetching());
    }

    @Test
    void setForceEventDuration_storesOption() {
        calendar.options().setForceEntryDuration(true);
        assertEquals(true, calendar.options().getForceEntryDuration());
    }

    @Test
    void setDefaultAllDay_storesOption() {
        calendar.options().setDefaultAllDay(true);
        assertEquals(true, calendar.options().getDefaultAllDay());
    }

}
