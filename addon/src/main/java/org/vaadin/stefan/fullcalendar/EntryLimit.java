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

/**
 * The value of an option that limits the entries shown in a day cell: no limit, as many as fit into the cell's
 * height, or a number. A {@code switch} over it needs no {@code default} branch.
 * <p>
 * {@code dayMaxEvents} and {@code dayMaxEventRows} both take it, and the library combines them: {@link #CELL_HEIGHT}
 * on either wins, then a number on {@code dayMaxEvents}, then a number on {@code dayMaxEventRows}. So {@link #NONE} on
 * one of them still leaves the limit of the other.
 */
public sealed interface EntryLimit extends Serializable {

    /** No limit, the library's {@code false}. */
    EntryLimit NONE = new None();

    /** As many as fit into the height of the cell, the library's {@code true}. */
    EntryLimit CELL_HEIGHT = new CellHeight();

    /**
     * Creates a limit of the given number.
     *
     * @param count the number
     * @return the limit
     * @throws IllegalArgumentException when the number is negative
     */
    static EntryLimit of(int count) {
        return new Count(count);
    }

    /** No limit. Use {@link #NONE}. */
    record None() implements EntryLimit {
    }

    /** As many as fit into the height of the cell. Use {@link #CELL_HEIGHT}. */
    record CellHeight() implements EntryLimit {
    }

    /**
     * A fixed number.
     *
     * @param count the number, not negative
     */
    record Count(int count) implements EntryLimit {

        public Count {
            if (count < 0) {
                throw new IllegalArgumentException("The count must not be negative, was " + count);
            }
        }
    }
}
