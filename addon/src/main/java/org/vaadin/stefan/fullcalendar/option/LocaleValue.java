/*
 * Copyright 2026, Stefan Uebe
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
package org.vaadin.stefan.fullcalendar.option;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * The value of the option that sets the language and the regional formats of the calendar: a locale the library
 * brings, or a custom locale with its own texts and formats. A {@code switch} over it needs no {@code default}
 * branch.
 * <p>
 * A sealed type rather than one Java type, because the library takes a locale code or a locale object with a
 * {@code code} and its own options.
 * <p>
 * The library also takes a list of codes and uses the first one it brings. That form is left out: the calendar
 * registers every locale of the library, and {@link CalendarLocale} lists them, so the fallback can be chosen on the
 * server.
 */
public sealed interface LocaleValue extends Serializable permits LocaleValue.Code, LocaleValue.Custom {

    /**
     * Creates the value for a locale the library brings, see {@link CalendarLocale}. A locale it does not bring falls
     * back to its language, then to English.
     *
     * @param locale the locale
     * @return the value
     * @throws NullPointerException when null is passed
     */
    static LocaleValue of(Locale locale) {
        return new Code(locale);
    }

    /**
     * Creates a custom locale with its own options, such as {@code todayText} or {@code week}. The library does not
     * merge them with the locale it brings for the same code: what the options leave out falls back to English.
     * <p>
     * The library passes the code to the browser's {@code Intl.NumberFormat}, which needs a valid language tag. The
     * code comes from a {@link Locale}, so it always is one.
     *
     * @param locale  the locale, sent as its language tag
     * @param options the options of the locale, as the library reads them, without {@code code}
     * @return the value
     * @throws NullPointerException when null is passed or contained
     * @throws IllegalArgumentException when the options contain {@code code}
     */
    static LocaleValue ofCustom(Locale locale, Map<String, Object> options) {
        return new Custom(locale, options);
    }

    /**
     * A locale the library brings.
     *
     * @param locale the locale
     */
    record Code(Locale locale) implements LocaleValue {

        public Code {
            Objects.requireNonNull(locale);
        }
    }

    /**
     * A custom locale with its own options.
     *
     * @param locale  the locale, sent as its language tag
     * @param options the options of the locale, without {@code code}
     */
    record Custom(Locale locale, Map<String, Object> options) implements LocaleValue {

        public Custom {
            Objects.requireNonNull(locale);
            options = Map.copyOf(Objects.requireNonNull(options));
            if (options.containsKey("code")) {
                throw new IllegalArgumentException("The code of a custom locale is its Locale, not an option");
            }
        }
    }
}
