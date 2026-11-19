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
import tools.jackson.databind.node.ObjectNode;

/**
 * An event that occurs when an entry has been changed on the client side.
 * <br><br>
 * You can apply the changes to the referred entry by calling the method {@link #applyChangesOnEntry()}.
 */
@ToString(callSuper = true)
public abstract class EntryChangedEvent extends EntryDataEvent {

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
    private boolean revertCheckScheduled;

    /**
     * New instance. Awaits the changed data object.
     * @param source source component
     * @param fromClient is from client
     * @param jsonObject json object with changed data
     */
    public EntryChangedEvent(FullCalendar source, boolean fromClient, ObjectNode jsonObject) {
        super(source, fromClient, jsonObject);
    }

    /**
     * Applies the contained changes on the referring entry and returns it.
     * @see Entry#updateFromJson(tools.jackson.databind.node.ObjectNode)
     * @return entry
     */
    public Entry applyChangesOnEntry() {
        Entry entry = getEntry();
        entry.updateFromJson(getJsonObject());
        this.changesApplied = true;
        return entry;
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
     * @since 7.2.0
     */
    @Override
    @SuppressWarnings("removal")
    public <R extends Entry> R getChangesAsEntry() {
        return getDetailsAsEntry();
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

}
