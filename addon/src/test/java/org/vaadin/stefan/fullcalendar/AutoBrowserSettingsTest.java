package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link FullCalendar#withAutoBrowserTimezone()} and {@link FullCalendar#withAutoUiLocale()}.
 */
public class AutoBrowserSettingsTest {

    private static final Timezone BERLIN = new Timezone(ZoneId.of("Europe/Berlin"));

    private FullCalendar calendar;
    private UI ui;

    @BeforeEach
    void setUp() {
        ui = new UI();
        UI.setCurrent(ui);
        ui.setLocale(Locale.ENGLISH);
        calendar = new FullCalendar();
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
    }

    // -------------------------------------------------------------------------
    // Browser time zone
    // -------------------------------------------------------------------------

    @Test
    void autoBrowserTimezone_reportedTimezone_becomesOption() {
        calendar.withAutoBrowserTimezone();

        calendar.setBrowserTimezone("Europe/Berlin");

        assertEquals(BERLIN, calendar.getOption(FullCalendar.Option.TIMEZONE).orElseThrow());
    }

    @Test
    void autoBrowserTimezone_alreadyReported_isAppliedAtOnce() {
        calendar.setBrowserTimezone("Europe/Berlin");

        calendar.withAutoBrowserTimezone();

        assertEquals(BERLIN, calendar.getOption(FullCalendar.Option.TIMEZONE).orElseThrow());
    }

    @Test
    void withoutAutoBrowserTimezone_reportedTimezone_isNotApplied() {
        calendar.setBrowserTimezone("Europe/Berlin");

        assertTrue(calendar.getOption(FullCalendar.Option.TIMEZONE).isEmpty());
        assertEquals(BERLIN, calendar.getBrowserTimezone().orElseThrow());
    }

    // -------------------------------------------------------------------------
    // UI locale
    // -------------------------------------------------------------------------

    @Test
    void autoUiLocale_onAttach_takesUiLocale() {
        ui.setLocale(Locale.GERMAN);
        calendar.withAutoUiLocale();

        ui.add(calendar);

        assertEquals(Locale.GERMAN, calendar.getOption(FullCalendar.Option.LOCALE).orElseThrow());
    }

    @Test
    void autoUiLocale_followsLocaleChange() {
        calendar.withAutoUiLocale();
        ui.add(calendar);

        ui.setLocale(Locale.FRENCH);

        assertEquals(Locale.FRENCH, calendar.getOption(FullCalendar.Option.LOCALE).orElseThrow());
    }

    @Test
    void autoUiLocale_enabledAfterAttach_isAppliedAtOnce() {
        ui.setLocale(Locale.GERMAN);
        ui.add(calendar);

        calendar.withAutoUiLocale();

        assertEquals(Locale.GERMAN, calendar.getOption(FullCalendar.Option.LOCALE).orElseThrow());
    }

    @Test
    void withoutAutoUiLocale_localeChange_isIgnored() {
        ui.add(calendar);

        ui.setLocale(Locale.FRENCH);

        // unlike in 8.0, the constructor sets no locale option on 7.x
        assertTrue(calendar.getOption(FullCalendar.Option.LOCALE).isEmpty());
    }
}
