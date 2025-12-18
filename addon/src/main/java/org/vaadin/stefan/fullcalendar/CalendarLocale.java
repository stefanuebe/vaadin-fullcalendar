/*
 * Copyright 2020, Stefan Uebe
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.vaadin.stefan.fullcalendar;

import lombok.Getter;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A list of all supported locales.
 */
public enum CalendarLocale {
    AFRIKAANS("af"),
    ARABIC("ar"),
    ARABIC_ALGERIA("ar-dz"),
    ARABIC_KUWAIT("ar-kw"),
    ARABIC_LYBIA("ar-ly"),
    ARABIC_MOROCCO("ar-ma"),
    ARABIC_SAUDI_ARABIA("ar-sa"),
    ARABIC_TUNESIA("ar-tn"),
    BULGARIAN("bg"),
    BOSNIAN("bs"),
    CATALAN("ca"),
    CZECH("cs"),
    DANISH("da"),
    GERMAN("de"),
    GERMAN_GERMANY("de-de"),
    GERMAN_AUSTRIA("de-at"),
    GERMAN_SWITZERLAND("de-ch"),
    GREEK("el"),
    ENGLISH("en"),
    ENGLISH_AUSTRALIA("en-au"),
    ENGLISH_CANADA("en-ca"),
    ENGLISH_UK("en-gb"),
    ENGLISH_IRELAND("en-ie"),
    ENGLISH_NEW_ZEALAND("en-nz"),
    SPANISH("es"),
    SPANICH_DOMINICAN_REPUBLIC("es-do"),
    SPANISH_US("es-us"),
    ESTONIAN("et"),
    BASQUE("eu"),
    PERSIAN("fa"),
    FINNISH("fi"),
    FRENCH("fr"),
    FRENCH_CANADA("fr-ca"),
    FRENCH_SWITZERLAND("fr-ch"),
    GALICIAN("gl"),
    HEBREW("he"),
    HINDI("hi"),
    CROATIAN("hr"),
    HUNGARIAN("hu"),
    INDONESIAN("id"),
    ICELANDIC("is"),
    ITALIAN("it"),
    JAPANESE("ja"),
    GEORGIAN("ka"),
    KAZAKH("kk"),
    KOREAN("ko"),
    LUXEMBOURGISH("lb"),
    LITHUNIAN("lt"),
    LATVIAN("lv"),
    MACEDONIAN("mk"),
    MALAY("ms"),
    MALAYSIA("ms-my"),
    NORWEGIAN_BOKMAL("nb"),
    DUTCH("nl"),
    DUTCH_BELGIUM("nl-be"),
    NORWEGIAN_NYNORSK("nn"),
    POLISH("pl"),
    PORTUGUESE("pt"),
    PORTUGUESE_BRAZIL("pt-br"),
    ROMANIAN("ro"),
    RUSSIAN("ru"),
    SLOVAK("sk"),
    SLOVENIAN("sl"),
    ALBANIAN("sq"),
    SERBIAN("sr"),
    SERBIAN_CYRILLIC("sr-cyrl"),
    SWEDISH("sv"),
    THAI("th"),
    TURKISH("tr"),
    UKRAINIAN("uk"),
    VIETNAMESE("vi"),
    CHINESE("zh-cn"),
    CHINESE_TAIWAN("zh-tw");

    private static final Locale[] availableLocales = Stream.of(values())
            .map(CalendarLocale::getLocale)
            .toArray(Locale[]::new);


    private static CalendarLocale defaultLocale = valueOf(Locale.getDefault()).orElse(ENGLISH);

    /**
     * The {@link Locale} instance representing this value
     */
    @Getter
    private final Locale locale;


    CalendarLocale(String languageTag) {
        locale = Objects.requireNonNull(Locale.forLanguageTag(languageTag),
                "Could not interprete language tag " + languageTag);
    }

    /**
     * Returns a matching instead for the given locale or empty, if no match has been found.
     *
     * @param locale locale to lookup
     * @return calendar locale instance
     */
    public static Optional<CalendarLocale> valueOf(Locale locale) {
        for (CalendarLocale calendarLocale : values()) {
            if (calendarLocale.getLocale().equals(locale)) {
                return Optional.of(calendarLocale);
            }
        }
        return Optional.empty();
    }


    /**
     * Get all available locales as an array of {@link Locale}.
     *
     * @return available locales
     */
    public static Locale[] getAvailableLocales() {
        return Arrays.copyOf(availableLocales, availableLocales.length);
    }

    /**
     * Returns the default locale. This is by default the system language. If the system language is not supported
     * by the calender, english will be used instead.
     * <p>
     * This value is used as a fallback in different places, if no locale could be
     * determined from the browser (or that should not be used).
     *
     * @return default locale
     */
    public static Locale getDefaultLocale() {
        return defaultLocale.getLocale();
    }

    /**
     * Sets the default locale. This is by default the system language. If the system language is not supported
     * by the calender, english will be used instead.
     * <p>
     * This value is used as a fallback in different places, if no locale could be
     * determined from the browser (or that should not be used).
     *
     * @param locale default calendar locale
     */
    public static void setDefault(CalendarLocale locale) {
        defaultLocale = Objects.requireNonNull(locale, "Parameter must not be null");
    }
}
