package org.vaadin.stefan.fullcalendar.option;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocaleValueTest {

    @Test
    void localeOfCodeIsTheGivenLocale() {
        LocaleValue value = LocaleValue.of(Locale.GERMAN);

        assertEquals(Locale.GERMAN, value.locale());
    }

    @Test
    void localeOfCustomIsTheGivenLocale() {
        Locale locale = Locale.of("nds");
        LocaleValue value = LocaleValue.ofCustom(locale, Map.of("todayText", "Vandaag"));

        assertEquals(locale, value.locale());
    }
}
