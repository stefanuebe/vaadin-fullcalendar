package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Locale;

public class CalendarLocaleTest {

    @Test
    void testAvailableLocalesDefined() {
        Locale[] locales = CalendarLocale.getAvailableLocales();

        Assertions.assertNotNull(locales);
        Assertions.assertNotEquals(0, locales.length);
    }

    @Test
    void testDefaultLocaleIsDefinedAndValidCalendarLocale() {
        Locale locale = CalendarLocale.getDefaultLocale();
        Assertions.assertNotNull(locale);

        Locale[] locales = CalendarLocale.getAvailableLocales();
        Assertions.assertTrue(Arrays.asList(locales).contains(locale));
    }

    @Test
    void defaultLocaleMatchesSupportedLocaleExactly() {
        Assertions.assertEquals(CalendarLocale.GERMAN_AUSTRIA, CalendarLocale.matchDefault(Locale.of("de", "AT")));
        Assertions.assertEquals(CalendarLocale.PORTUGUESE_BRAZIL, CalendarLocale.matchDefault(Locale.of("pt", "BR")));
    }

    @Test
    void defaultLocaleFallsBackToLanguageOfUnsupportedRegion() {
        Assertions.assertEquals(CalendarLocale.FRENCH, CalendarLocale.matchDefault(Locale.FRANCE));
        Assertions.assertEquals(CalendarLocale.ITALIAN, CalendarLocale.matchDefault(Locale.ITALY));
        Assertions.assertEquals(CalendarLocale.SPANISH, CalendarLocale.matchDefault(Locale.of("es", "ES")));
        Assertions.assertEquals(CalendarLocale.ENGLISH, CalendarLocale.matchDefault(Locale.US));
    }

    @Test
    void defaultLocaleFallsBackToEnglishForUnsupportedLanguage() {
        Assertions.assertEquals(CalendarLocale.ENGLISH, CalendarLocale.matchDefault(Locale.of("xx", "YY")));
        Assertions.assertEquals(CalendarLocale.ENGLISH, CalendarLocale.matchDefault(Locale.ROOT));
    }

    @Test
    void valueOfStaysExact() {
        Assertions.assertTrue(CalendarLocale.valueOf(Locale.FRANCE).isEmpty());
    }

    @Test
    void testNonEmptyClientTags() {
        for (Locale locale : CalendarLocale.getAvailableLocales()) {
            String languageTag = locale.toLanguageTag();
            Assertions.assertFalse(languageTag.isEmpty());
            Assertions.assertNotEquals("und", languageTag);
        }
    }
}
