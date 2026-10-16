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

import org.vaadin.stefan.fullcalendar.ClientSideValue;

import java.io.Serializable;

/**
 * What happens when the user clicks a "+N more" link: an {@link Action}, a {@link ViewName} to go to, or a
 * {@link JsCallback} that runs instead. A {@code switch} over it needs no {@code default} branch.
 * <p>
 * A sealed type rather than one Java type, because the library takes a fixed action, any view name or a function.
 */
public sealed interface MoreLinkClickValue extends Serializable permits MoreLinkClickValue.Action, ViewName, JsCallback {

    /**
     * The actions of the link that need no view name.
     */
    enum Action implements MoreLinkClickValue, ClientSideValue {
        /**
         * Shows a popover with all entries of the day. The entries in it send the same click events as the other
         * entries.
         */
        POPUP("popover"),

        /** Goes to a day view, as the views of the header toolbar determine. */
        DAY("day"),

        /** Goes to a week view, as the views of the header toolbar determine. */
        WEEK("week"),

        /**
         * Opens no popover and keeps the view type. The library only moves the calendar to the date of the link, which
         * switches the shown range when that date lies outside it, for example on a day of the next month. Use it to
         * handle the click on the server, for example with your own dialog, see
         * {@code FullCalendar.addMoreLinkClickedListener}.
         */
        NOTHING("function");

        private final String clientSideValue;

        Action(String clientSideValue) {
            this.clientSideValue = clientSideValue;
        }

        @Override
        public String getClientSideValue() {
            return clientSideValue;
        }
    }
}
