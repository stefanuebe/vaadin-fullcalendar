package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import lombok.ToString;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.Collections;
import java.util.Optional;

/**
 * Occurs when an entry's time or resource assignment has changed by drag and drop
 * in a scheduler (resource) view.
 * <br><br>
 * Client side name: eventDrop
 *
 * @deprecated Use {@link CalendarItemDroppedSchedulerEvent} with
 * {@link FullCalendarScheduler#addCalendarItemDroppedSchedulerListener} instead.
 */
@DomEvent("eventDrop")
@ToString(callSuper = true)
@Deprecated
public class EntryDroppedSchedulerEvent extends CalendarItemDroppedSchedulerEvent<Entry> {

    /**
     * New instance. Awaits the changed data object for the entry plus the json object for the delta information.
     *
     * @param source     source component
     * @param fromClient is from client
     * @param jsonEntry  json object with changed data
     * @param jsonDelta  json object with delta information
     */
    public EntryDroppedSchedulerEvent(FullCalendar<Entry> source, boolean fromClient,
                                      @EventData("event.detail.data") ObjectNode jsonEntry,
                                      @EventData("event.detail.delta") ObjectNode jsonDelta) {
        super(source, fromClient, jsonEntry, jsonDelta);
    }

    /**
     * Returns the entry for which the event occurred.
     *
     * @return entry
     * @deprecated Use {@link #getItem()} instead.
     */
    @Deprecated
    public Entry getEntry() {
        return getItem();
    }

    /**
     * Applies the changes on the entry including updating a resource change.
     *
     * @return this event's entry instance
     * @deprecated Use {@link #applyChangesOnItem()} instead. Note that resource changes
     * need to be handled separately when using the CIP approach.
     */
    @Deprecated
    public Entry applyChangesOnEntry() {
        ResourceEntry entry = (ResourceEntry) applyChangesOnItem();
        ObjectNode object = getJsonObject();

        updateResourcesFromEventResourceDelta(entry, object);

        return entry;
    }

    public static void updateResourcesFromEventResourceDelta(ResourceEntry entry, ObjectNode object) {
        entry.getCalendar().map(c -> (Scheduler) c).ifPresent(calendar -> {
            Optional.ofNullable(object.get("oldResource"))
                    .filter(JsonNode::isString)
                    .map(JsonNode::asString)
                    .flatMap(calendar::getResourceById)
                    .map(Collections::singleton)
                    .ifPresent(entry::removeResources);

            Optional.ofNullable(object.get("newResource"))
                    .filter(JsonNode::isString)
                    .map(JsonNode::asString)
                    .flatMap(calendar::getResourceById)
                    .map(Collections::singleton)
                    .ifPresent(entry::addResources);
        });
    }

    /**
     * Creates a copy based on the referenced entry and the received data.
     *
     * @param <R> return type
     * @return copy
     * @deprecated Use the CIP event hierarchy with {@link CalendarItemDroppedSchedulerEvent} instead.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public <R extends Entry> R createCopyBasedOnChanges() {
        try {
            Entry copy = getEntry().copy();
            copy.updateFromJson(getJsonObject());
            return (R) copy;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
