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

import java.io.Serializable;
import java.util.Objects;

/**
 * The value of an option that makes headers sticky: not sticky, sticky at the edge, or sticky at an offset from the
 * edge. A {@code switch} over it needs no {@code default} branch.
 * <p>
 * The library takes the offset as a number or a string and uses it as a CSS length, where a number means pixels. So
 * one string form covers both: {@code of("10px")} for a number of pixels.
 * <p>
 * A sealed type rather than one Java type, because the library takes a boolean as well as an offset.
 */
public sealed interface StickyValue extends Serializable {

    /** Not sticky, the library's {@code false}. */
    StickyValue NOT_STICKY = new NotSticky();

    /** Sticky at the edge, the library's {@code true}. */
    StickyValue STICKY = new Sticky();

    /**
     * Creates the value for sticky at an offset from the edge.
     *
     * @param offset a CSS length, such as {@code "2em"} or {@code "10px"}
     * @return the value
     * @throws NullPointerException when null is passed
     */
    static StickyValue of(String offset) {
        return new Offset(offset);
    }

    /** Not sticky. Use {@link #NOT_STICKY}. */
    record NotSticky() implements StickyValue {
    }

    /** Sticky at the edge. Use {@link #STICKY}. */
    record Sticky() implements StickyValue {
    }

    /**
     * Sticky at an offset from the edge.
     *
     * @param offset a CSS length, such as {@code "2em"} or {@code "10px"}
     */
    record Offset(String offset) implements StickyValue {

        public Offset {
            Objects.requireNonNull(offset);
        }
    }
}
