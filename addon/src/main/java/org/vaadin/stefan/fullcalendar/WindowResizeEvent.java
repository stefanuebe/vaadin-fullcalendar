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

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import lombok.Getter;
import lombok.ToString;

import java.util.Optional;

/**
 * Fires after the browser window has been resized and FullCalendar has recalculated its layout.
 * <br><br>
 * Note: this event can fire frequently during a resize interaction. The {@code windowResizeDelay}
 * option (Phase 2) debounces FullCalendar's internal handling; this server event fires after that debounce.
 * <br><br>
 * Use this event to update server-side layout decisions when the calendar changes responsive breakpoints
 * (e.g. switching from timeGridWeek to timeGridDay on small screens).
 * <br><br>
 * Client side name: windowResize
 */
@DomEvent("windowResize")
@Getter
@ToString
public class WindowResizeEvent extends ComponentEvent<FullCalendar> {

    /**
     * The client-side view type name (e.g. "dayGridMonth", "timeGridWeek").
     */
    private final String viewName;

    /**
     * The current calendar view after resize, or {@code null} if the view name is not recognised
     * (e.g. a custom view that has not been registered on the server side).
     */
    private final CalendarView calendarView;

    /**
     * New instance.
     *
     * @param source     the source component
     * @param fromClient {@code true} if the event originated from the client
     * @param viewName   the current view type name as reported by FullCalendar
     */
    public WindowResizeEvent(FullCalendar source, boolean fromClient, @EventData("event.detail.name") String viewName) {
        super(source, fromClient);
        this.viewName = viewName;
        this.calendarView = source.lookupViewByClientSideValue(viewName).orElse(null);
    }

    /**
     * Returns the current calendar view after resize. Empty when the view name is not recognised
     * on the Java side (e.g. an unregistered custom view).
     *
     * @return the current calendar view
     */
    public Optional<CalendarView> getCalendarViewOptional() {
        return Optional.ofNullable(calendarView);
    }
}
