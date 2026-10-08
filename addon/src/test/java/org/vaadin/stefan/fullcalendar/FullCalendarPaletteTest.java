package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FullCalendarPaletteTest {

    @Test
    void stockTheme_listsItsPalettes_defaultFirst() {
        assertEquals(List.of("purple", "blue", "green", "red", "yellow"),
                FullCalendarPalette.availablePalettesFor(FullCalendarTheme.MONARCH));
        assertEquals(List.of("indigo", "amber", "emerald", "rose"),
                FullCalendarPalette.availablePalettesFor(FullCalendarTheme.BREEZY));
        assertEquals(List.of("blue", "green", "purple", "red"),
                FullCalendarPalette.availablePalettesFor(FullCalendarTheme.FORMA));
        assertEquals(List.of("red", "blue", "green", "purple"),
                FullCalendarPalette.availablePalettesFor(FullCalendarTheme.PULSE));
    }

    @Test
    void themeWithoutPalettesToChoose_isEmpty() {
        assertTrue(FullCalendarPalette.availablePalettesFor(FullCalendarTheme.VAADIN).isEmpty());
        assertTrue(FullCalendarPalette.availablePalettesFor(FullCalendarTheme.CLASSIC).isEmpty());
        assertTrue(FullCalendarPalette.availablePalettesFor("corporate").isEmpty());
    }

    @Test
    void null_throws() {
        assertThrows(NullPointerException.class, () -> FullCalendarPalette.availablePalettesFor(null));
    }

    @Test
    void list_isUnmodifiable() {
        assertThrows(UnsupportedOperationException.class,
                () -> FullCalendarPalette.availablePalettesFor(FullCalendarTheme.MONARCH).add("black"));
    }
}
