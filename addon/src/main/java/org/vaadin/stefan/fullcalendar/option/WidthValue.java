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

/**
 * The value of an option that takes a width: in pixels or as a percentage. A {@code switch} over it needs no
 * {@code default} branch.
 * <p>
 * A sealed type rather than one Java type, because the library takes a number of pixels as well as a string with
 * {@code %}. The library reads no other CSS units here and ignores such a string, so they have no form. It shares no
 * forms with {@link HeightValue}, which takes any CSS length and the keyword {@code "auto"}.
 */
public sealed interface WidthValue extends Serializable {

    /**
     * Creates a width in pixels.
     *
     * @param pixels the width in pixels, not negative
     * @return the width
     * @throws IllegalArgumentException when the width is negative
     */
    static WidthValue ofPixels(int pixels) {
        return new Pixels(pixels);
    }

    /**
     * Creates a width as a percentage of the available width.
     *
     * @param percent the percentage, such as {@code 25}, not negative
     * @return the width
     * @throws IllegalArgumentException when the percentage is negative, infinite or not a number
     */
    static WidthValue ofPercent(double percent) {
        return new Percent(percent);
    }

    /**
     * A width in pixels.
     *
     * @param pixels the width in pixels, not negative
     */
    record Pixels(int pixels) implements WidthValue {

        public Pixels {
            if (pixels < 0) {
                throw new IllegalArgumentException("The width must not be negative, was " + pixels);
            }
        }
    }

    /**
     * A width as a percentage of the available width.
     *
     * @param percent the percentage, such as {@code 25}, not negative
     */
    record Percent(double percent) implements WidthValue {

        public Percent {
            if (!Double.isFinite(percent) || percent < 0) {
                throw new IllegalArgumentException("The percentage must be a finite number and not negative, was "
                        + percent);
            }
        }
    }
}
