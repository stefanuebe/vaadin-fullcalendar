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
 * The common part of the value types whose option the library switches off with {@code false}: the constant
 * {@link #NONE}. Each of these types takes it, for example {@code ListTextValue.NONE} or {@code EntryLimitValue.NONE},
 * and its Javadoc says what switching the option off means there.
 * <p>
 * A new value type with this form extends {@code NoneValue} and permits {@link None}, and {@link None} lists it
 * among the types it implements.
 */
public interface NoneValue {

    /** The option switched off, the library's {@code false}. */
    None NONE = new None();

    /** The option switched off, the library's {@code false}. Use {@link #NONE}. */
    record None() implements BusinessHoursValue, EntryLimitValue, ListTextValue, ToolbarValue {
    }
}
