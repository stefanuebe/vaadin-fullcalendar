package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.option.TextContent;

import static org.junit.jupiter.api.Assertions.*;

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
    // setEventInteractive
    // -------------------------------------------------------------------------

    @Test
    void setEventInteractive_true_storesOption() {
        calendar.options().setEntryInteractive(true);
        assertEquals(true, calendar.options().getEntryInteractive());
    }

    @Test
    void setEventInteractive_false_storesOption() {
        calendar.options().setEntryInteractive(false);
        assertEquals(false, calendar.options().getEntryInteractive());
    }

    // -------------------------------------------------------------------------
    // button hints
    // -------------------------------------------------------------------------

    @Test
    void setTodayHint_storesOption() {
        calendar.options().setTodayHint("Go to today");
        assertEquals(TextContent.of("Go to today"), calendar.options().getTodayHint());
    }

    @Test
    void setTodayHint_null_clearsOption() {
        calendar.options().setTodayHint("Go to today");
        calendar.options().setTodayHint((String) null);
        // null removes the option
        assertNull(calendar.options().getTodayHint());
    }

    // -------------------------------------------------------------------------
    // setViewHint
    // -------------------------------------------------------------------------

    @Test
    void setViewHint_storesOption() {
        calendar.options().setViewHint("Switch to $0 view");
        assertEquals(TextContent.of("Switch to $0 view"), calendar.options().getViewHint());
    }

    @Test
    void setViewHint_null_clearsOption() {
        calendar.options().setViewHint("Switch to $0 view");
        calendar.options().setViewHint((String) null);
        assertNull(calendar.options().getViewHint());
    }

    // -------------------------------------------------------------------------
    // setNavLinkHint
    // -------------------------------------------------------------------------

    @Test
    void setNavLinkHint_storesOption() {
        calendar.options().setNavLinkHint("Go to $0");
        assertEquals(TextContent.of("Go to $0"), calendar.options().getNavLinkHint());
    }

    // -------------------------------------------------------------------------
    // setMoreLinkHint
    // -------------------------------------------------------------------------

    @Test
    void setMoreLinkHint_storesOption() {
        calendar.options().setMoreLinkHint("$0 more events. Click to expand");
        assertEquals(TextContent.of("$0 more events. Click to expand"), calendar.options().getMoreLinkHint());
    }

    // -------------------------------------------------------------------------
    // setCloseHint
    // -------------------------------------------------------------------------

    @Test
    void setCloseHint_storesOption() {
        calendar.options().setCloseHint("Close");
        assertEquals("Close", calendar.options().getCloseHint());
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

    @Test
    void setLongPressDelay_storesOption() {
        calendar.options().setLongPressDelay(500);
        assertEquals(500, calendar.options().getLongPressDelay());
    }

    @Test
    void setEventLongPressDelay_storesOption() {
        calendar.options().setEntryLongPressDelay(300);
        assertEquals(300, calendar.options().getEntryLongPressDelay());
    }

    @Test
    void setSelectLongPressDelay_storesOption() {
        calendar.options().setSelectLongPressDelay(400);
        assertEquals(400, calendar.options().getSelectLongPressDelay());
    }
}
