package org.vaadin.stefan.fullcalendar;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link FullCalendar#withAutoBrowserTimezone()} and {@link FullCalendar#withAutoUiLocale()} (#273).
 */
public class AutoBrowserSettingsTest extends BrowserlessTest {

    private static final Timezone BERLIN = new Timezone(ZoneId.of("Europe/Berlin"));

    private FullCalendar calendar;

    @BeforeEach
    void setUp() {
        calendar = new FullCalendar();
        UI.getCurrent().setLocale(Locale.ENGLISH);
    }

    // -------------------------------------------------------------------------
    // Browser time zone
    // -------------------------------------------------------------------------

    @Test
    void autoBrowserTimezone_reportedTimezone_becomesOption() {
        calendar.withAutoBrowserTimezone();

        calendar.setBrowserTimezone("Europe/Berlin");

        assertEquals(BERLIN, calendar.getOption(Option.TIMEZONE).orElseThrow());
    }

    @Test
    void autoBrowserTimezone_alreadyReported_isAppliedAtOnce() {
        calendar.setBrowserTimezone("Europe/Berlin");

        calendar.withAutoBrowserTimezone();

        assertEquals(BERLIN, calendar.getOption(Option.TIMEZONE).orElseThrow());
    }

    @Test
    void withoutAutoBrowserTimezone_reportedTimezone_isNotApplied() {
        calendar.setBrowserTimezone("Europe/Berlin");

        assertTrue(calendar.getOption(Option.TIMEZONE).isEmpty());
        assertEquals(BERLIN, calendar.getBrowserTimezone().orElseThrow());
    }

    // -------------------------------------------------------------------------
    // UI locale
    // -------------------------------------------------------------------------

    @Test
    void autoUiLocale_onAttach_takesUiLocale() {
        UI.getCurrent().setLocale(Locale.GERMAN);
        calendar.withAutoUiLocale();

        UI.getCurrent().add(calendar);

        assertEquals(Locale.GERMAN, calendar.getOption(Option.LOCALE).orElseThrow());
    }

    @Test
    void autoUiLocale_followsLocaleChange() {
        calendar.withAutoUiLocale();
        UI.getCurrent().add(calendar);

        UI.getCurrent().setLocale(Locale.FRENCH);

        assertEquals(Locale.FRENCH, calendar.getOption(Option.LOCALE).orElseThrow());
    }

    @Test
    void autoUiLocale_enabledAfterAttach_isAppliedAtOnce() {
        UI.getCurrent().setLocale(Locale.GERMAN);
        UI.getCurrent().add(calendar);

        calendar.withAutoUiLocale();

        assertEquals(Locale.GERMAN, calendar.getOption(Option.LOCALE).orElseThrow());
    }

    @Test
    void withoutAutoUiLocale_localeChange_isIgnored() {
        Locale before = calendar.<Locale>getOption(Option.LOCALE).orElseThrow();
        UI.getCurrent().add(calendar);

        UI.getCurrent().setLocale(Locale.FRENCH);

        assertEquals(before, calendar.getOption(Option.LOCALE).orElseThrow());
    }
}
