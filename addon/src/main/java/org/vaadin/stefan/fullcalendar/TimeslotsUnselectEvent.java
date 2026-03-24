/*
 * Copyright 2020, Stefan Uebe
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

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import lombok.ToString;

/**
 * Fires when the current timeslot selection is cleared. This complements {@link TimeslotsSelectedEvent}.
 * <br><br>
 * Unselect can be triggered by:
 * <ul>
 *   <li>The user clicking elsewhere on the calendar (when {@code unselectAuto} is {@code true}, which is the default)</li>
 *   <li>The user pressing Escape</li>
 *   <li>A new selection being made (which replaces the old one)</li>
 *   <li>Code calling {@link FullCalendar#clearSelection()} programmatically</li>
 * </ul>
 * <br>
 * Client side name: unselect
 */
@DomEvent("unselect")
@ToString
public class TimeslotsUnselectEvent extends ComponentEvent<FullCalendar> {

    /**
     * New instance.
     *
     * @param source     the source component
     * @param fromClient {@code true} if the event originated from the client
     */
    public TimeslotsUnselectEvent(FullCalendar source, boolean fromClient) {
        super(source, fromClient);
    }
}
