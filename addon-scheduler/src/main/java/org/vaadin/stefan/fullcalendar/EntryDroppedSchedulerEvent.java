package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import lombok.ToString;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.Collections;
import java.util.Optional;

@DomEvent("eventDrop")
@ToString(callSuper = true)
public class EntryDroppedSchedulerEvent extends EntryTimeChangedEvent {

    private final Resource oldResource;
    private final Resource newResource;

    /**
     * New instance. Awaits the changed data object for the entry plus the json object for the delta information.
     *
     * @param source     source component
     * @param fromClient is from client
     * @param jsonEntry  json object with changed data
     * @param jsonDelta  json object with delta information
     */
    public EntryDroppedSchedulerEvent(FullCalendarScheduler source, boolean fromClient,
                                      @EventData("event.detail.data") ObjectNode jsonEntry,
                                      @EventData("event.detail.delta") ObjectNode jsonDelta) {
        super(source, fromClient, jsonEntry, jsonDelta);

        if(jsonEntry.hasNonNull("oldResource")) {
            this.oldResource = source.getResourceById(jsonEntry.get("oldResource").asString()).orElseThrow(IllegalArgumentException::new);
        } else {
            this.oldResource = null;
        }

        if(jsonEntry.has("newResource")) {
            this.newResource = source.getResourceById(jsonEntry.get("newResource").asString()).orElseThrow(IllegalArgumentException::new);
        } else {
            this.newResource = null;
        }
    }

    /**
     * Applies the changes on the entry including updating a resource change.
     * @return this event's entry instance
     */
    @Override
    public Entry applyChangesOnEntry() {
        ResourceEntry entry = (ResourceEntry) super.applyChangesOnEntry();
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
     * If there has been a change in the resource assignments, this method returns the previous assigned resource.
     * @return previous resource or empty
     */
    public Optional<Resource> getOldResource() {
        return Optional.ofNullable(oldResource);
    }

    /**
     * If there has been a change in the resource assignments, this method returns the newly assigned resource.
     * @return newly resource or empty
     */
    public Optional<Resource> getNewResource() {
        return Optional.ofNullable(newResource);
    }
}
