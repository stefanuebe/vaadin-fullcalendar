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
import java.util.Objects;

/**
 * The value of the toolbar options {@code headerToolbar} and {@code footerToolbar}: the three sections of the toolbar,
 * or {@link #NONE} for no toolbar. A {@code switch} over it needs no {@code default} branch.
 * <p>
 * Each section is a string of names. Names separated by a comma are shown adjacent, names separated by a space with a
 * small gap. A name is one of {@link NativeToolbarParts}, a view name such as {@code "dayGridMonth"}, or the name of a
 * button or element defined with the options {@code buttons} or {@code toolbarElements}. An empty string shows nothing
 * in that section.
 * <pre>{@code
 * options.setHeaderToolbar(ToolbarValue.ofStartEnd("prev,next today", "title", "dayGridMonth,timeGridWeek"));
 * }</pre>
 * <p>
 * The library names the outer sections in two ways, so there are two forms with data:
 * <ul>
 *   <li>{@link StartEnd}, the usual form: {@code start} and {@code end} follow the text direction. In a right-to-left
 *   calendar, {@code start} is on the right.</li>
 *   <li>{@link LeftRight}: {@code left} and {@code right} are the screen sides and stay there in a right-to-left
 *   calendar. Use it only for a toolbar whose sections must not swap sides with the text direction.</li>
 * </ul>
 * A sealed type rather than one record, because the two forms write different keys, and {@link #NONE} is the boolean
 * {@code false}, not an object.
 */
public sealed interface ToolbarValue extends NoneValue, Serializable
        permits ToolbarValue.StartEnd, ToolbarValue.LeftRight, NoneValue.None {

    /**
     * Creates a toolbar whose outer sections follow the text direction, see {@link StartEnd}.
     *
     * @param start the section at the start, left in a left-to-right calendar
     * @param center the section in the center
     * @param end the section at the end, right in a left-to-right calendar
     * @return the toolbar
     * @throws NullPointerException when null is passed, use an empty string for an empty section
     */
    static ToolbarValue ofStartEnd(String start, String center, String end) {
        return new StartEnd(start, center, end);
    }

    /**
     * Creates a toolbar whose outer sections stay on their screen side, see {@link LeftRight}.
     *
     * @param left the section on the left, also in a right-to-left calendar
     * @param center the section in the center
     * @param right the section on the right, also in a right-to-left calendar
     * @return the toolbar
     * @throws NullPointerException when null is passed, use an empty string for an empty section
     */
    static ToolbarValue ofLeftRight(String left, String center, String right) {
        return new LeftRight(left, center, right);
    }

    /**
     * A toolbar whose outer sections follow the text direction, the library's {@code start}, {@code center} and
     * {@code end}. In a right-to-left calendar, {@code start} is on the right.
     *
     * @param start the section at the start
     * @param center the section in the center
     * @param end the section at the end
     */
    record StartEnd(String start, String center, String end) implements ToolbarValue {

        public StartEnd {
            Objects.requireNonNull(start);
            Objects.requireNonNull(center);
            Objects.requireNonNull(end);
        }
    }

    /**
     * A toolbar whose outer sections are screen sides, the library's {@code left}, {@code center} and {@code right}.
     * Unlike {@link StartEnd}, the sections do not swap sides in a right-to-left calendar.
     *
     * @param left the section on the left
     * @param center the section in the center
     * @param right the section on the right
     */
    record LeftRight(String left, String center, String right) implements ToolbarValue {

        public LeftRight {
            Objects.requireNonNull(left);
            Objects.requireNonNull(center);
            Objects.requireNonNull(right);
        }
    }
}
