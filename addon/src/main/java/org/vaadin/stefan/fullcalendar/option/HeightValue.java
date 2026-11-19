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
 * The value of an option that takes a height: in pixels, as a CSS length, or {@code "auto"}. A {@code switch} over it
 * needs no {@code default} branch.
 * <p>
 * A sealed type rather than one Java type, because the forms differ in kind: a number, a string, and the keyword
 * {@code "auto"}, which the library treats apart from other strings.
 */
public sealed interface HeightValue extends Serializable {

    /** The natural height of the content, without scrollbars, the library's {@code "auto"}. */
    HeightValue AUTO = new Auto();

    /**
     * Creates a height in pixels.
     *
     * @param pixels the height in pixels, not negative
     * @return the height
     * @throws IllegalArgumentException when the height is negative
     */
    static HeightValue ofPixels(int pixels) {
        return new Pixels(pixels);
    }

    /**
     * Creates a height as a CSS length.
     *
     * @param length a CSS length, such as {@code "50vh"}
     * @return the height
     * @throws NullPointerException when null is passed
     * @throws IllegalArgumentException when {@code "auto"} is passed, use {@link #AUTO}
     */
    static HeightValue ofCss(String length) {
        return new Css(length);
    }

    /** The natural height of the content. Use {@link #AUTO}. */
    record Auto() implements HeightValue {
    }

    /**
     * A height in pixels.
     *
     * @param pixels the height in pixels, not negative
     */
    record Pixels(int pixels) implements HeightValue {

        public Pixels {
            if (pixels < 0) {
                throw new IllegalArgumentException("The height must not be negative, was " + pixels);
            }
        }
    }

    /**
     * A height as a CSS length.
     *
     * @param length a CSS length, such as {@code "50vh"}
     */
    record Css(String length) implements HeightValue {

        public Css {
            Objects.requireNonNull(length);
            if (length.equals("auto")) {
                throw new IllegalArgumentException("Use HeightValue.AUTO for \"auto\"");
            }
        }
    }
}
