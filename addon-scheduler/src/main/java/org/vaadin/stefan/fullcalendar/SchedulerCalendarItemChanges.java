package org.vaadin.stefan.fullcalendar;

import tools.jackson.databind.node.ObjectNode;

import java.util.Optional;

/**
 * Extends {@link CalendarItemChanges} with scheduler-specific resource change information.
 * <p>
 * When a calendar item is dropped onto a different resource in a scheduler view, the JSON delta
 * contains {@code "oldResource"} and {@code "newResource"} fields with the respective resource IDs.
 *
 * @author Stefan Uebe
 */
public class SchedulerCalendarItemChanges extends CalendarItemChanges {

    private final ObjectNode jsonDelta;

    /**
     * Creates a new instance wrapping the given JSON delta.
     *
     * @param jsonDelta the JSON object containing changed properties
     */
    public SchedulerCalendarItemChanges(ObjectNode jsonDelta) {
        super(jsonDelta);
        this.jsonDelta = jsonDelta;
    }

    /**
     * Returns the ID of the resource the item was previously assigned to, if present.
     *
     * @return optional containing the old resource ID
     */
    public Optional<String> getOldResourceId() {
        if (jsonDelta.hasNonNull("oldResource")) {
            return Optional.of(jsonDelta.get("oldResource").asString());
        }
        return Optional.empty();
    }

    /**
     * Returns the ID of the resource the item was dropped onto, if present.
     *
     * @return optional containing the new resource ID
     */
    public Optional<String> getNewResourceId() {
        if (jsonDelta.hasNonNull("newResource")) {
            return Optional.of(jsonDelta.get("newResource").asString());
        }
        return Optional.empty();
    }

    /**
     * Returns the resource the item was previously assigned to, resolved from the given scheduler.
     *
     * @param scheduler the scheduler to resolve the resource from
     * @return optional containing the old resource
     */
    public Optional<Resource> getOldResource(Scheduler scheduler) {
        return getOldResourceId().flatMap(scheduler::getResourceById);
    }

    /**
     * Returns the resource the item was dropped onto, resolved from the given scheduler.
     *
     * @param scheduler the scheduler to resolve the resource from
     * @return optional containing the new resource
     */
    public Optional<Resource> getNewResource(Scheduler scheduler) {
        return getNewResourceId().flatMap(scheduler::getResourceById);
    }
}
