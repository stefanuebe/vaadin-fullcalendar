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

import lombok.Getter;
import tools.jackson.databind.node.ObjectNode;

import java.util.Objects;

/**
 * A client-managed event source that fetches events from a public iCalendar ({@code .ics}) feed URL.
 * <br><br>
 * Requires the {@code @fullcalendar/icalendar} and {@code ical.js} npm packages. The URL must be accessible
 * from the browser (CORS headers are required for cross-origin feeds).
 * <br><br>
 * iCalendar is a read-only format — drag/drop should remain disabled (the default).
 * <br><br>
 * Example:
 * <pre>
 * calendar.addEventSource(new ICalendarEventSource("https://example.com/holidays.ics")
 *     .withId("holiday-ics")
 *     .withColor("purple"));
 * </pre>
 *
 * @see <a href="https://fullcalendar.io/docs/icalendar">FullCalendar iCalendar documentation</a>
 */
@Getter
public class ICalendarEventSource extends ClientSideEventSource<ICalendarEventSource> {

    /** The URL of the iCalendar feed. */
    private final String url;

    /**
     * Creates a new iCalendar event source for the given URL.
     *
     * @param url the iCal feed URL; must not be null
     * @throws NullPointerException if url is null
     */
    public ICalendarEventSource(String url) {
        this.url = Objects.requireNonNull(url, "url must not be null");
    }

    @Override
    public ObjectNode toJson() {
        ObjectNode json = JsonFactory.createObject();
        addCommonToJson(json);
        json.put("url", url);
        json.put("format", "ics");
        return json;
    }
}
