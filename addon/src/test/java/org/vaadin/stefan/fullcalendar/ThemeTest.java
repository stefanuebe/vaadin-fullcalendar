package org.vaadin.stefan.fullcalendar;

import com.vaadin.browserless.BrowserlessTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link FullCalendar#setTheme(String)} (#265). The browser reads the theme from the element property
 * {@code fcTheme}, so that property is what reaches the client.
 */
public class ThemeTest extends BrowserlessTest {

    @Test
    void newCalendar_hasVaadinTheme() {
        FullCalendar calendar = new FullCalendar();

        assertEquals(FullCalendarTheme.VAADIN, calendar.getTheme());
        assertEquals(FullCalendarTheme.VAADIN, calendar.getElement().getProperty("fcTheme"));
    }

    @Test
    void setTheme_reachesTheElement() {
        FullCalendar calendar = new FullCalendar();

        calendar.setTheme(FullCalendarTheme.MONARCH);

        assertEquals(FullCalendarTheme.MONARCH, calendar.getTheme());
        assertEquals(FullCalendarTheme.MONARCH, calendar.getElement().getProperty("fcTheme"));
    }

    @Test
    void setTheme_customName_isPassedUnchanged() {
        FullCalendar calendar = new FullCalendar();

        calendar.setTheme("corporate");

        assertEquals("corporate", calendar.getElement().getProperty("fcTheme"));
    }

    @Test
    void setTheme_null_throws() {
        FullCalendar calendar = new FullCalendar();

        assertThrows(NullPointerException.class, () -> calendar.setTheme(null));
    }

    @Test
    void setTheme_blank_throws() {
        FullCalendar calendar = new FullCalendar();

        assertThrows(IllegalArgumentException.class, () -> calendar.setTheme(" "));
        assertEquals(FullCalendarTheme.VAADIN, calendar.getTheme());
    }

    @Test
    void jsonOptionsConstructor_hasVaadinTheme() {
        FullCalendar calendar = new FullCalendar(JsonFactory.createObject());

        assertEquals(FullCalendarTheme.VAADIN, calendar.getTheme());
    }
}
