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
import lombok.Getter;

/**
 * Fired when a custom toolbar button registered via {@link FullCalendar#addCustomButton} is clicked.
 * <p>
 * The event carries the {@link #buttonName} of the button that was clicked, which can be used to
 * distinguish between multiple custom buttons in a single listener.
 *
 * @see FullCalendar#addCustomButton(CustomButton, com.vaadin.flow.component.ComponentEventListener)
 * @see FullCalendar#addCustomButtonClickedListener(String, com.vaadin.flow.component.ComponentEventListener)
 */
@Getter
public class CustomButtonClickedEvent extends ComponentEvent<FullCalendar> {

    /**
     * The {@link CustomButton#getName() name} of the button that was clicked.
     */
    private final String buttonName;

    /**
     * Creates a new event.
     *
     * @param source     the calendar component that fired the event
     * @param fromClient {@code true} if the event originated on the client side
     * @param buttonName the name of the custom button that was clicked
     */
    public CustomButtonClickedEvent(FullCalendar source, boolean fromClient, String buttonName) {
        super(source, fromClient);
        this.buttonName = buttonName;
    }
}
