package org.vaadin.stefan.fullcalendar;

import elemental.json.JsonObject;

/**
 * An event, that occur, when a entry has been changed on the client side. Returns the updated event.
 */
public abstract class EntryChangedEvent extends EntryEvent {

    private final JsonObject jsonObject;

    /**
     * New instance. Awaits the changed data object.
     * @param source source component
     * @param fromClient is from client
     * @param jsonObject json object with changed data
     */
    public EntryChangedEvent(FullCalendar source, boolean fromClient, JsonObject jsonObject) {
        super(source, fromClient, jsonObject.getString("id"));
        this.jsonObject = jsonObject;
    }

    /**
     * Returns the json object that contains the changes from client side. These can easily
     * be applied via {@link #applyChangesOnEntry()} or you can do that manually.
     *
     * @return changes as JsonObject
     */
    public JsonObject getJsonObject() {
        return jsonObject;
    }

    /**
     * Applies the contained changes on the refering entry and returns this instance.
     * @see Entry#update(JsonObject)
     * @return entry
     */
    public Entry applyChangesOnEntry() {
        Entry entry = getEntry();
        entry.update(getJsonObject());
        getSource().updateEntry(entry);
        return entry;
    }
}
