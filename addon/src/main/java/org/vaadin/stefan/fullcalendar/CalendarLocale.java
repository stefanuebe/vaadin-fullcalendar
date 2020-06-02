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

import java.util.Arrays;
import java.util.Locale;

/**
 * A list of all supported locales.
 */
public class CalendarLocale {
    public static final Locale AFRIKAANS = Locale.forLanguageTag("af");
    public static final Locale ARABIC = Locale.forLanguageTag("ar");
    public static final Locale ARABIC_ALGERIA = Locale.forLanguageTag("ar-dz");
    public static final Locale ARABIC_KUWAIT = Locale.forLanguageTag("ar-kw");
    public static final Locale ARABIC_LYBIA = Locale.forLanguageTag("ar-ly");
    public static final Locale ARABIC_MOROCCO = Locale.forLanguageTag("ar-ma");
    public static final Locale ARABIC_SAUDI_ARABIA = Locale.forLanguageTag("ar-sa");
    public static final Locale ARABIC_TUNESIA = Locale.forLanguageTag("ar-tn");
    public static final Locale BULGARIAN = Locale.forLanguageTag("bg");
    public static final Locale BOSNIAN = Locale.forLanguageTag("bs");
    public static final Locale CATALAN = Locale.forLanguageTag("ca");
    public static final Locale CZECH = Locale.forLanguageTag("cs");
    public static final Locale DANISH = Locale.forLanguageTag("da");
    public static final Locale GERMAN = Locale.forLanguageTag("de");
    public static final Locale GERMAN_AUSTRIA = Locale.forLanguageTag("de-at");
    public static final Locale GERMAN_SWITZERLAND = Locale.forLanguageTag("de-ch");
    public static final Locale GREEK = Locale.forLanguageTag("el");
    public static final Locale ENGLISH = Locale.forLanguageTag("en");
    public static final Locale ENGLISH_AUSTRALIA = Locale.forLanguageTag("en-au");
    public static final Locale ENGLISH_CANADA = Locale.forLanguageTag("en-ca");
    public static final Locale ENGLISH_UK = Locale.forLanguageTag("en-gb");
    public static final Locale ENGLISH_IRELAND = Locale.forLanguageTag("en-ie");
    public static final Locale ENGLISH_NEW_ZEALAND = Locale.forLanguageTag("en-nz");
    public static final Locale SPANISH = Locale.forLanguageTag("es");
    public static final Locale SPANICH_DOMINICAN_REPUBLIC = Locale.forLanguageTag("es-do");
    public static final Locale SPANISH_US = Locale.forLanguageTag("es-us");
    public static final Locale ESTONIAN = Locale.forLanguageTag("et");
    public static final Locale BASQUE = Locale.forLanguageTag("eu");
    public static final Locale PERSIAN = Locale.forLanguageTag("fa");
    public static final Locale FINNISH = Locale.forLanguageTag("fi");
    public static final Locale FRENCH = Locale.forLanguageTag("fr");
    public static final Locale FRENCH_CANADA = Locale.forLanguageTag("fr-ca");
    public static final Locale FRENCH_SWITZERLAND = Locale.forLanguageTag("fr-ch");
    public static final Locale GALICIAN = Locale.forLanguageTag("gl");
    public static final Locale HEBREW = Locale.forLanguageTag("he");
    public static final Locale HINDI = Locale.forLanguageTag("hi");
    public static final Locale CROATIAN = Locale.forLanguageTag("hr");
    public static final Locale HUNGARIAN = Locale.forLanguageTag("hu");
    public static final Locale INDONESIAN = Locale.forLanguageTag("id");
    public static final Locale ICELANDIC = Locale.forLanguageTag("is");
    public static final Locale ITALIAN = Locale.forLanguageTag("it");
    public static final Locale JAPANESE = Locale.forLanguageTag("ja");
    public static final Locale GEORGIAN = Locale.forLanguageTag("ka");
    public static final Locale KAZAKH = Locale.forLanguageTag("kk");
    public static final Locale KOREAN = Locale.forLanguageTag("ko");
    public static final Locale LUXEMBOURGISH = Locale.forLanguageTag("lb");
    public static final Locale LITHUNIAN = Locale.forLanguageTag("lt");
    public static final Locale LATVIAN = Locale.forLanguageTag("lv");
    public static final Locale MACEDONIAN = Locale.forLanguageTag("mk");
    public static final Locale MALAY = Locale.forLanguageTag("ms");
    public static final Locale MALAYSIA = Locale.forLanguageTag("ms-my");
    public static final Locale NORWEGIAN_BOKMAL = Locale.forLanguageTag("nb");
    public static final Locale DUTCH = Locale.forLanguageTag("nl");
    public static final Locale DUTCH_BELGIUM = Locale.forLanguageTag("nl-be");
    public static final Locale NORWEGIAN_NYNORSK = Locale.forLanguageTag("nn");
    public static final Locale POLISH = Locale.forLanguageTag("pl");
    public static final Locale PORTUGUESE = Locale.forLanguageTag("pt");
    public static final Locale PORTUGUESE_BRAZIL = Locale.forLanguageTag("pt-br");
    public static final Locale ROMANIAN = Locale.forLanguageTag("ro");
    public static final Locale RUSSIAN = Locale.forLanguageTag("ru");
    public static final Locale SLOVAK = Locale.forLanguageTag("sk");
    public static final Locale SLOVENIAN = Locale.forLanguageTag("sl");
    public static final Locale ALBANIAN = Locale.forLanguageTag("sq");
    public static final Locale SERBIAN = Locale.forLanguageTag("sr");
    public static final Locale SERBIAN_CYRILLIC = Locale.forLanguageTag("sr-cyrl");
    public static final Locale SWEDISH = Locale.forLanguageTag("sv");
    public static final Locale THAI = Locale.forLanguageTag("th");
    public static final Locale TURKISH = Locale.forLanguageTag("tr");
    public static final Locale UKRAINIAN = Locale.forLanguageTag("uk");
    public static final Locale VIETNAMESE = Locale.forLanguageTag("vi");
    public static final Locale CHINESE = Locale.forLanguageTag("zh-cn");
    public static final Locale CHINESE_TAIWAN = Locale.forLanguageTag("zh-tw");

    private static final Locale[] availableLocales = {AFRIKAANS, ARABIC, ARABIC_ALGERIA, ARABIC_KUWAIT, ARABIC_LYBIA,
            ARABIC_MOROCCO, ARABIC_SAUDI_ARABIA, ARABIC_TUNESIA, BULGARIAN, BOSNIAN, CATALAN, CZECH, DANISH, GERMAN,
            GERMAN_AUSTRIA, GERMAN_SWITZERLAND, GREEK, ENGLISH, ENGLISH_AUSTRALIA, ENGLISH_CANADA, ENGLISH_UK,
            ENGLISH_IRELAND, ENGLISH_NEW_ZEALAND, SPANISH, SPANICH_DOMINICAN_REPUBLIC, SPANISH_US, ESTONIAN, BASQUE,
            PERSIAN, FINNISH, FRENCH, FRENCH_CANADA, FRENCH_SWITZERLAND, GALICIAN, HEBREW, HINDI, CROATIAN, HUNGARIAN,
            INDONESIAN, ICELANDIC, ITALIAN, JAPANESE, GEORGIAN, KAZAKH, KOREAN, LUXEMBOURGISH, LITHUNIAN, LATVIAN,
            MACEDONIAN, MALAY, MALAYSIA, NORWEGIAN_BOKMAL, DUTCH, DUTCH_BELGIUM, NORWEGIAN_NYNORSK, POLISH, PORTUGUESE,
            PORTUGUESE_BRAZIL, ROMANIAN, RUSSIAN, SLOVAK, SLOVENIAN, ALBANIAN, SERBIAN, SERBIAN_CYRILLIC, SWEDISH,
            THAI, TURKISH, UKRAINIAN, VIETNAMESE, CHINESE, CHINESE_TAIWAN};

    public static Locale[] getAvailableLocales() {
        return Arrays.copyOf(availableLocales, availableLocales.length);
    }

    public static Locale getDefault() {
        return CalendarLocale.ENGLISH;
    }
}
