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

import lombok.Getter;
import lombok.ToString;
import elemental.json.JsonObject;

/**
 * Extended entry event type, that provides also additional client side entry data, that can be interpreted on the
 * server side.
 */
@Getter
@ToString(callSuper = true)
public abstract class EntryDataEvent extends EntryEvent {

    /**
     * The json object that contains the changes from client side.
     */
    private final JsonObject jsonObject;

    /**
     * Whether {@link #applyChangesOnEntry()} has been called on this event.
     * Used by the auto-revert mechanism to determine if the client-side change
     * should be reverted.
     */
    @Getter
    private boolean changesApplied;

    /**
     * Whether a revert check has been scheduled via {@code beforeClientResponse}
     * for this event instance. Prevents duplicate scheduling when multiple
     * listeners are registered for the same event type.
     */
    @Getter(lombok.AccessLevel.NONE)
    private boolean revertCheckScheduled;

    /**
     * New instance. Awaits the changed data object.
     * @param source source component
     * @param fromClient is from client
     * @param jsonObject json object with changed data
     */
    public EntryDataEvent(FullCalendar source, boolean fromClient, JsonObject jsonObject) {
        super(source, fromClient, jsonObject.get(Entry.Fields.ID).asString());
        this.jsonObject = jsonObject;
    }

    /**
     * Applies the contained changes on the referring entry and returns this instance.
     * @see Entry#updateFromJson(elemental.json.JsonObject)
     * @return entry
     */
    public Entry applyChangesOnEntry() {
        Entry entry = getEntry();
        entry.updateFromJson(getJsonObject());
        this.changesApplied = true;
        return entry;
    }

    /**
     * Returns whether a revert check has already been scheduled for this event.
     * Package-private — used by {@link FullCalendar} auto-revert logic.
     */
    boolean isRevertCheckScheduled() {
        return revertCheckScheduled;
    }

    /**
     * Marks that a revert check has been scheduled for this event.
     * Package-private — used by {@link FullCalendar} auto-revert logic.
     */
    void markRevertCheckScheduled() {
        this.revertCheckScheduled = true;
    }

    /**
     * Returns a new {@link Entry} instance with the changes from this event applied on top of the
     * original entry. The original entry stored in {@link #getEntry()} is not modified.
     * <p>
     * This is the standard way to inspect "the entry as it would look after applying the change"
     * without touching server-side state — useful for validation before calling
     * {@link #applyChangesOnEntry()} or for rendering a dialog preview.
     *
     * @param <R> return type
     * @return a new entry reflecting the pending change
     */
    @SuppressWarnings("unchecked")
    public <R extends Entry> R getChangesAsEntry() {
        try {
            Entry copy = getEntry().copy();

            JsonObject jsonObject = getJsonObject();
            copy.updateFromJson(jsonObject);

            return (R) copy; // we use R here, since in most cases event listeners do not specify a generic type

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param <R> return type
     * @return copy reflecting the pending change
     * @deprecated use {@link #getChangesAsEntry()} — the new name is shorter and
     *             clearer about what the method returns.
     */
    @Deprecated
    public <R extends Entry> R createCopyBasedOnChanges() {
        return getChangesAsEntry();
    }


}
