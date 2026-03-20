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
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;

/**
 * Fires when an entry from a <em>client-managed</em> event source (JSON feed, Google Calendar, iCal) is resized.
 * This event fires instead of {@link EntryResizedEvent} when the resized entry's id is not in the server-side
 * entry cache (i.e. it came from a {@link ClientSideEventSource}).
 * <br><br>
 * The {@link #getEntry()} method returns a <em>transient</em> data carrier. NOT owned by any EntryProvider.
 * Use {@link #getOldEnd()} to get the end time before the resize.
 * <br><br>
 * Resize on client-managed source entries is opt-in. Set {@code withEditable(true)} or
 * {@code withDurationEditable(true)} on the {@link ClientSideEventSource} to enable it.
 * <br><br>
 * Client side name: externalEntryResize
 */
@DomEvent("externalEntryResize")
@Getter
@ToString
public class ExternalEntryResizedEvent extends ExternalEntryEvent {

    /**
     * The end-time delta by which the entry was resized.
     */
    private final Delta delta;

    /**
     * The end time before the resize, or {@code null} if the entry had no end time.
     */
    private final LocalDateTime oldEnd;

    /**
     * New instance.
     *
     * @param source      source component
     * @param fromClient  true if from client
     * @param entryData   JSON data of the resized entry (new end time)
     * @param jsonDelta   end delta JSON object
     * @param sourceId    id of the ClientSideEventSource
     */
    public ExternalEntryResizedEvent(FullCalendar source, boolean fromClient,
                                     @EventData("event.detail.data") ObjectNode entryData,
                                     @EventData("event.detail.delta") ObjectNode jsonDelta,
                                     @EventData("event.detail.sourceId") String sourceId) {
        super(source, fromClient, entryData, sourceId);
        this.delta = Delta.fromJson(jsonDelta);

        LocalDateTime newEnd = getEntry().getEnd();
        this.oldEnd = newEnd != null
                ? newEnd.minusYears(delta.getYears()).minusMonths(delta.getMonths()).minusDays(delta.getDays())
                       .minusHours(delta.getHours()).minusMinutes(delta.getMinutes()).minusSeconds(delta.getSeconds())
                : null;
    }
}
