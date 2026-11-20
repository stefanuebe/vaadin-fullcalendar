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
 * The value of {@code listDayFormat} and {@code listDayAltFormat}, which format the day headings of list views. A
 * {@code switch} over it needs no {@code default} branch.
 * <dl>
 *   <dt><b>Forms</b></dt> <dd>{@link DateFormat}: the properties of a format object, such as {@code weekday} or
 *                             {@code month}.
 *                             <p>{@link #NONE}: no text.
 *                             <p>{@link JsCallback}: a function that returns the text.</dd>
 * </dl>
 * <p>
 * A sealed type rather than a {@link DateFormatValue}, because these options also take {@code false}.
 */
public sealed interface ListDayFormatValue extends NoneValue, Serializable
        permits DateFormat, JsCallback, NoneValue.None {
}
