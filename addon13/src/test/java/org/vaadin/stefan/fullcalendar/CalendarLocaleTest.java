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
        Locale locale = CalendarLocale.getDefault();
        Assertions.assertNotNull(locale);

        Locale[] locales = CalendarLocale.getAvailableLocales();
        Assertions.assertTrue(Arrays.asList(locales).contains(locale));
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
