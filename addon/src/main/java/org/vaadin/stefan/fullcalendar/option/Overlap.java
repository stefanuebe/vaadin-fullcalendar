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

/**
 * Whether entries may overlap, for an option that takes a boolean or a callback.
 */
public enum Overlap implements OverlapValue {
    /** Overlapping is allowed, the library's {@code true}. */
    ALLOWED,
    /** Overlapping is not allowed, the library's {@code false}. */
    FORBIDDEN;

    /**
     * Returns the constant for the library's boolean.
     *
     * @param allowed whether overlapping is allowed
     * @return {@link #ALLOWED} for true, {@link #FORBIDDEN} for false
     */
    public static Overlap of(boolean allowed) {
        return allowed ? ALLOWED : FORBIDDEN;
    }
}
