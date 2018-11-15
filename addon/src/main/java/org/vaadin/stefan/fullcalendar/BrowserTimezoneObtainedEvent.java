package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

/**
 * This event gets fired when the client side reported the browser's timezone to the server. Since this is
 * done after the element has been attached to the client, it will be fired after all UI attach events.
 */
@DomEvent("browser-timezone-obtained")
public class BrowserTimezoneObtainedEvent extends ComponentEvent<FullCalendar> {

    private final Timezone timezone;

    /**
     * Creates a new event using the given source and indicator whether the
     * event originated from the client side or the server side.
     *
     * @param source     the source component
     * @param fromClient <code>true</code> if the event originated from the client
     */
    public BrowserTimezoneObtainedEvent(FullCalendar source, boolean fromClient, @EventData("dummy") Timezone timezone) {
        super(source, fromClient);
        this.timezone = timezone;
    }

    public Timezone getTimezone() {
        return timezone;
    }
}
