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
        Assertions.assertEquals(CalendarLocale.GERMAN_AUSTRIA, CalendarLocale.findDefaultFor(Locale.of("de", "AT")));
        Assertions.assertEquals(CalendarLocale.PORTUGUESE_BRAZIL, CalendarLocale.findDefaultFor(Locale.of("pt", "BR")));
    }

    @Test
    void defaultLocaleFallsBackToLanguageOfUnsupportedRegion() {
        Assertions.assertEquals(CalendarLocale.FRENCH, CalendarLocale.findDefaultFor(Locale.FRANCE));
        Assertions.assertEquals(CalendarLocale.ITALIAN, CalendarLocale.findDefaultFor(Locale.ITALY));
        Assertions.assertEquals(CalendarLocale.SPANISH, CalendarLocale.findDefaultFor(Locale.of("es", "ES")));
        Assertions.assertEquals(CalendarLocale.ENGLISH, CalendarLocale.findDefaultFor(Locale.US));
    }

    @Test
    void defaultLocaleFallsBackToEnglishForUnsupportedLanguage() {
        Assertions.assertEquals(CalendarLocale.ENGLISH, CalendarLocale.findDefaultFor(Locale.of("xx", "YY")));
        Assertions.assertEquals(CalendarLocale.ENGLISH, CalendarLocale.findDefaultFor(Locale.ROOT));
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
