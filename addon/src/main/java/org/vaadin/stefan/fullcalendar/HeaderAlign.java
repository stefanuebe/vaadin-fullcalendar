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

/**
 * Horizontal alignment of header cell text. Used by {@link Option#DAY_HEADER_ALIGN}, {@link Option#SLOT_HEADER_ALIGN}
 * and the scheduler's resource day header alignment.
 *
 * @see <a href="https://fullcalendar.io/docs/day-header-render-hooks">dayHeaderAlign</a>
 */
public enum HeaderAlign implements ClientSideValue {
    /** Aligned to the start (left in left-to-right calendars). */
    START("start"),
    /** Centered. */
    CENTER("center"),
    /** Aligned to the end (right in left-to-right calendars). */
    END("end");

    private final String clientSideValue;

    HeaderAlign(String clientSideValue) {
        this.clientSideValue = clientSideValue;
    }

    @Override
    public String getClientSideValue() {
        return clientSideValue;
    }
}
