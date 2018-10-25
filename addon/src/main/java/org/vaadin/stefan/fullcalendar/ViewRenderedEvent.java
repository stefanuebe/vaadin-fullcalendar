package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import elemental.json.JsonObject;
import elemental.json.JsonValue;

import java.time.LocalDate;

/**
 * Occurs when the calendar view has been rendered. Provides information about the shown timespan.
 */
@DomEvent("viewRender")
public class ViewRenderedEvent extends ComponentEvent<FullCalendar> {

    private final LocalDate intervalStart;
    private final LocalDate intervalEnd;

    /**
     * Creates a new event using the given source and indicator whether the
     * event originated from the client side or the server side.
     *
     * @param source     the source component
     * @param fromClient <code>true</code> if the event originated from the client
     */
    public ViewRenderedEvent(FullCalendar source, boolean fromClient, @EventData("event.detail") JsonObject eventData) {
        super(source, fromClient);

        intervalStart = LocalDate.parse(eventData.getString("intervalStart"));
        intervalEnd = LocalDate.parse(eventData.getString("intervalEnd"));
    }

    /**
     * Returns the current shown interval's start date.
     * @return interval start
     */
    public LocalDate getIntervalStart() {
        return intervalStart;
    }

    /**
     * Returns the current shown interval's exclusive end date. This means, this date is not part of the interval.
     * @return interval end (exclusive)
     */
    public LocalDate getIntervalEnd() {
        return intervalEnd;
    }
}
