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
 * A client-managed event source that fetches events from a public Google Calendar.
 * <br><br>
 * Requires the {@code @fullcalendar/google-calendar} npm package and the global Google Calendar API key to be
 * set via {@link FullCalendar#setGoogleCalendarApiKey(String)} (or per-source via {@link #withApiKey(String)}).
 * <br><br>
 * <strong>Note:</strong> Only public Google Calendars are supported. Private calendars require OAuth and cannot
 * use this source. FullCalendar fetches but never writes back to Google Calendar — it is read-only from FC's
 * perspective.
 * <br><br>
 * Example:
 * <pre>
 * calendar.setGoogleCalendarApiKey("AIzaSy...");
 * calendar.addClientSideEventSource(new GoogleCalendarEventSource("holidays@group.calendar.google.com")
 *     .withId("holidays")
 *     .withColor("green"));
 * </pre>
 *
 * @see <a href="https://fullcalendar.io/docs/google-calendar">FullCalendar Google Calendar documentation</a>
 */
@Getter
public class GoogleCalendarEventSource extends ClientSideEventSource<GoogleCalendarEventSource> {

    /** The Google Calendar ID (e.g. {@code "abc@group.calendar.google.com"}). */
    private final String googleCalendarId;

    /**
     * Optional per-source API key. Falls back to the calendar-level key set via
     * {@link FullCalendar#setGoogleCalendarApiKey(String)}.
     */
    private String googleCalendarApiKey;

    /**
     * Creates a new Google Calendar event source for the given calendar ID.
     *
     * @param googleCalendarId the Google Calendar ID; must not be null
     * @throws NullPointerException if googleCalendarId is null
     */
    public GoogleCalendarEventSource(String googleCalendarId) {
        this.googleCalendarId = Objects.requireNonNull(googleCalendarId, "googleCalendarId must not be null");
    }

    /**
     * Sets a per-source Google Calendar API key. Overrides the calendar-level key for this source only.
     * @param apiKey Google Calendar API key
     * @return this
     */
    public GoogleCalendarEventSource withApiKey(String apiKey) {
        this.googleCalendarApiKey = apiKey;
        return this;
    }

    @Override
    public ObjectNode toJson() {
        ObjectNode json = JsonFactory.createObject();
        addCommonToJson(json);
        json.put("googleCalendarId", googleCalendarId);
        if (googleCalendarApiKey != null) json.put("googleCalendarApiKey", googleCalendarApiKey);
        return json;
    }
}
