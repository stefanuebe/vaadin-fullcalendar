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

import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import lombok.Getter;
import lombok.ToString;
import elemental.json.JsonObject;

import java.time.LocalDateTime;

/**
 * Fires when an entry from a <em>client-managed</em> event source (JSON feed, Google Calendar, iCal) is dragged
 * to a new time slot. This event fires instead of {@link EntryDroppedEvent} when the dropped entry's id is not
 * found in the server-side entry cache (i.e. it came from a {@link ClientSideEventSource}, not from the
 * server-side {@link org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider}).
 * <br><br>
 * The {@link #getEntry()} method returns a <em>transient</em> data carrier constructed from the dropped entry's
 * JS data. This entry is NOT part of any {@link org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider}.
 * Do not add it to a provider; instead, use it to read the entry id and new position, then call the appropriate
 * external API (e.g. Google Calendar API) to persist the change.
 * The entry's {@link Entry#getId()} matches the FullCalendar event id as set in the external source —
 * use this id to locate and update the corresponding record in the external system.
 * <br><br>
 * {@link #getOldStart()} and {@link #getOldEnd()} return the position before the drag (computed from the
 * delta applied in reverse).
 * <br><br>
 * Drag/drop on client-managed source entries is opt-in. Set {@code withEditable(true)} on the
 * {@link ClientSideEventSource} to enable it.
 * <br><br>
 * Client side name: externalEntryDrop
 */
@DomEvent("externalEntryDrop")
@Getter
@ToString
public class ExternalEntryDroppedEvent extends ExternalEntryEvent {

    /**
     * The delta by which the entry was moved.
     */
    private final Delta delta;

    /**
     * The start time before the drag, or {@code null} if the dropped entry had no start time.
     */
    private final LocalDateTime oldStart;

    /**
     * The end time before the drag, or {@code null} if the dropped entry had no end time.
     */
    private final LocalDateTime oldEnd;

    /**
     * New instance.
     *
     * @param source      source component
     * @param fromClient  true if from client
     * @param entryData   JSON data of the dropped entry (new position)
     * @param jsonDelta   delta JSON object
     * @param sourceId    id of the ClientSideEventSource
     */
    public ExternalEntryDroppedEvent(FullCalendar source, boolean fromClient,
                                     @EventData("event.detail.data") JsonObject entryData,
                                     @EventData("event.detail.delta") JsonObject jsonDelta,
                                     @EventData("event.detail.sourceId") String sourceId) {
        super(source, fromClient, entryData, sourceId);
        this.delta = Delta.fromJson(jsonDelta);

        LocalDateTime newStart = getEntry().getStart();
        LocalDateTime newEnd = getEntry().getEnd();
        this.oldStart = newStart != null
                ? newStart.minusYears(delta.getYears()).minusMonths(delta.getMonths()).minusDays(delta.getDays())
                         .minusHours(delta.getHours()).minusMinutes(delta.getMinutes()).minusSeconds(delta.getSeconds())
                : null;
        this.oldEnd = newEnd != null
                ? newEnd.minusYears(delta.getYears()).minusMonths(delta.getMonths()).minusDays(delta.getDays())
                       .minusHours(delta.getHours()).minusMinutes(delta.getMinutes()).minusSeconds(delta.getSeconds())
                : null;
    }
}
