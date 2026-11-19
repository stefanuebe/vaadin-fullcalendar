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
import java.util.List;
import java.util.Objects;

/**
 * The value of {@code slotHeaderFormat}: a {@link DateFormat}, a {@link JsCallback} that returns the text, or
 * {@link Tiers}, one format per header row of a timeline view. A sealed type rather than a {@link DateFormatValue},
 * because only this option takes a list, and the other format options would accept it otherwise. A {@code switch}
 * over it needs no {@code default} branch.
 */
public sealed interface SlotHeaderFormatValue extends Serializable permits DateFormat, JsCallback,
        SlotHeaderFormatValue.Tiers {

    /**
     * Creates the value for several header rows, the top row first.
     *
     * @param tiers the format of each row, at least one
     * @return the value
     * @throws NullPointerException when null is passed or contained
     * @throws IllegalArgumentException when no format is passed
     */
    static SlotHeaderFormatValue of(DateFormatValue... tiers) {
        return new Tiers(List.of(tiers));
    }

    /**
     * One format per header row of a timeline view, the top row first. The list is copied, so a later change to the
     * passed list does not change the value.
     *
     * @param tiers the format of each row, at least one
     */
    record Tiers(List<DateFormatValue> tiers) implements SlotHeaderFormatValue {

        public Tiers {
            tiers = List.copyOf(Objects.requireNonNull(tiers));
            if (tiers.isEmpty()) {
                throw new IllegalArgumentException("At least one format is needed");
            }
        }
    }
}
