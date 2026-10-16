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

import java.util.Map;
import java.util.Objects;

/**
 * A date format as the library's format object, for an option that takes a date format. The properties are those of
 * the browser's {@code Intl.DateTimeFormat}, such as {@code year}, {@code month} and {@code day}, plus the library's
 * own, such as {@code omitCommas} or {@code meridiem}. The map is copied.
 * <p>
 * The library also takes a format string, but only with a formatting plugin, which the add-on does not load. So there
 * is no string form.
 *
 * @param properties the properties of the format object, with strings, booleans or numbers as values
 * @see <a href="https://fullcalendar.io/docs/date-formatting">Date formatting</a>
 */
public record DateFormat(Map<String, Object> properties) implements DateFormatValue, ListDayFormatValue {

    public DateFormat {
        properties = Map.copyOf(Objects.requireNonNull(properties));
        properties.forEach((key, value) -> {
            if (!(value instanceof String || value instanceof Boolean || value instanceof Number)) {
                throw new IllegalArgumentException("The value of " + key + " is no string, boolean or number: " + value);
            }
        });
    }

    /**
     * Creates the format.
     *
     * @param properties the properties of the format object, with strings, booleans or numbers as values
     * @return the format
     * @throws NullPointerException when null is passed or contained
     * @throws IllegalArgumentException when a value is no string, boolean or number
     */
    public static DateFormat of(Map<String, Object> properties) {
        return new DateFormat(properties);
    }
}
