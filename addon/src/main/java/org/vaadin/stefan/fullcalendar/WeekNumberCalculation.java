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

/**
 * Determines how week numbers are calculated. Corresponds to the FullCalendar {@code weekNumberCalculation} option.
 *
 * @see <a href="https://fullcalendar.io/docs/weekNumberCalculation">weekNumberCalculation</a>
 */
public enum WeekNumberCalculation implements ClientSideValue {
    /** Uses the locale's default week number calculation. */
    LOCAL("local"),
    /** ISO 8601 week numbers (week starts on Monday, first week contains Jan 4th). */
    ISO("ISO"),
    /** Alias for ISO. */
    ISO8601("ISO");

    private final String clientSideValue;

    WeekNumberCalculation(String clientSideValue) {
        this.clientSideValue = clientSideValue;
    }

    @Override
    public String getClientSideValue() {
        return clientSideValue;
    }
}
