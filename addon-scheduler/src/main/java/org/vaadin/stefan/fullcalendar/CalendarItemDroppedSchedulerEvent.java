package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import lombok.Getter;
import lombok.ToString;
import tools.jackson.databind.node.ObjectNode;

import java.util.Optional;

/**
 * Occurs when a calendar item's time or resource assignment has changed by drag and drop
 * in a scheduler (resource) view.
 * <p>
 * This is the CIP counterpart to {@link EntryDroppedSchedulerEvent}. Use this event when
 * the calendar is configured with a CalendarItemProvider via {@link FullCalendar#setCalendarItemProvider}.
 * <p>
 * Extends {@link CalendarItemDroppedEvent} with resource change information (old/new resource).
 * <br><br>
 * Client side name: eventDrop
 *
 * @param <T> the type of the calendar item POJO
 * @author Stefan Uebe
 */
@DomEvent("eventDrop")
@Getter
@ToString(callSuper = true)
public class CalendarItemDroppedSchedulerEvent<T> extends CalendarItemDroppedEvent<T> {

    private final Resource oldResource;
    private final Resource newResource;

    /**
     * New instance.
     *
     * @param source     source component
     * @param fromClient is from client
     * @param jsonItem   JSON object with updated item data
     * @param jsonDelta  JSON object with delta information
     */
    public CalendarItemDroppedSchedulerEvent(FullCalendar<T> source, boolean fromClient,
            @EventData("event.detail.data") ObjectNode jsonItem,
            @EventData("event.detail.delta") ObjectNode jsonDelta) {
        super(source, fromClient, jsonItem, jsonDelta);

        Scheduler scheduler = (Scheduler) source;
        if (jsonItem.hasNonNull("oldResource")) {
            this.oldResource = scheduler.getResourceById(jsonItem.get("oldResource").asString()).orElse(null);
        } else {
            this.oldResource = null;
        }

        if (jsonItem.hasNonNull("newResource")) {
            this.newResource = scheduler.getResourceById(jsonItem.get("newResource").asString()).orElse(null);
        } else {
            this.newResource = null;
        }
    }

    private SchedulerCalendarItemChanges schedulerChanges;

    /**
     * If there has been a change in the resource assignments, this method returns the previous assigned resource.
     *
     * @return previous resource or empty
     */
    public Optional<Resource> getOldResource() {
        return Optional.ofNullable(oldResource);
    }

    /**
     * If there has been a change in the resource assignments, this method returns the newly assigned resource.
     *
     * @return new resource or empty
     */
    public Optional<Resource> getNewResource() {
        return Optional.ofNullable(newResource);
    }

    /**
     * Returns a typed accessor for the changed properties, including scheduler-specific resource changes.
     *
     * @return scheduler-specific changes accessor
     */
    public SchedulerCalendarItemChanges getSchedulerChanges() {
        if (schedulerChanges == null) {
            schedulerChanges = new SchedulerCalendarItemChanges(getJsonObject());
        }
        return schedulerChanges;
    }
}
