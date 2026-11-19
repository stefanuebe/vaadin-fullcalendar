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

import org.vaadin.stefan.fullcalendar.CalendarView;
import java.util.Objects;

/**
 * The client-side name of a view, for an option that takes a view name, see {@link ViewNameValue} and
 * {@link MoreLinkClickValue}.
 *
 * @param name the view name, such as {@code "timeGridDay"}
 * @see CalendarView#getClientSideValue()
 */
public record ViewName(String name) implements ViewNameValue, MoreLinkClickValue {

    public ViewName {
        Objects.requireNonNull(name);
    }

    /**
     * Creates the view name.
     *
     * @param name the view name, such as {@code "timeGridDay"}
     * @return the view name
     * @throws NullPointerException when null is passed
     */
    public static ViewName of(String name) {
        return new ViewName(name);
    }

    /**
     * Creates the name of the given view.
     *
     * @param view the view, a built-in or a custom one
     * @return the view name
     * @throws NullPointerException when null is passed
     */
    public static ViewName of(CalendarView view) {
        return new ViewName(view.getClientSideValue());
    }
}
