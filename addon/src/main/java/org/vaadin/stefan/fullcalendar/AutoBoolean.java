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
package org.vaadin.stefan.fullcalendar;

/**
 * The value of an option that takes a boolean or {@code "auto"}, where the library decides. One enum covers all three
 * forms, so these options need no sealed value type.
 */
public enum AutoBoolean {
    /** The library's {@code true}. */
    TRUE,
    /** The library's {@code false}. */
    FALSE,
    /** The library decides, its {@code "auto"}. */
    AUTO;

    /**
     * Returns the constant for the library's boolean.
     *
     * @param value the boolean
     * @return {@link #TRUE} or {@link #FALSE}
     */
    public static AutoBoolean of(boolean value) {
        return value ? TRUE : FALSE;
    }
}
