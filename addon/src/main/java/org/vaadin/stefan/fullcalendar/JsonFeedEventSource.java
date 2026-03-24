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
import elemental.json.JsonObject;

import java.util.Map;
import java.util.Objects;

/**
 * A client-managed event source that fetches events from a URL returning a JSON array of FullCalendar event objects.
 * <br><br>
 * The browser fetches events directly from the given URL — the server only configures the source, it does not
 * own or observe the individual entries.
 * <br><br>
 * The request includes {@code start} and {@code end} query parameters (names overridable per-source via
 * {@link #withStartParam(String)} / {@link #withEndParam(String)}, or globally via
 * {@code setOption(Option.EXTERNAL_EVENT_SOURCE_START_PARAM, ...)} / {@code setOption(Option.EXTERNAL_EVENT_SOURCE_END_PARAM, ...)})
 * plus optional extra parameters from {@link #withExtraParams(Map)}.
 * <br><br>
 * Example:
 * <pre>
 * calendar.addClientSideEventSource(new JsonFeedEventSource("/api/events")
 *     .withId("room-101")
 *     .withColor("steelblue")
 *     .withExtraParams(Map.of("roomId", "101")));
 * </pre>
 *
 * @see <a href="https://fullcalendar.io/docs/events-json-feed">FullCalendar JSON feed documentation</a>
 */
@Getter
public class JsonFeedEventSource extends ClientSideEventSource<JsonFeedEventSource> {

    /** The URL from which to fetch events. */
    private final String url;

    /** HTTP method to use for the request. Defaults to {@code "GET"}. */
    private String method = "GET";

    /** Extra query parameters appended to every request. */
    private Map<String, Object> extraParams;

    /** Per-source override for the start parameter name (default: inherits from calendar). */
    private String startParam;

    /** Per-source override for the end parameter name (default: inherits from calendar). */
    private String endParam;

    /** Per-source override for the timeZone parameter name (default: inherits from calendar). */
    private String timeZoneParam;

    /**
     * Creates a new JSON feed event source for the given URL.
     *
     * @param url the URL to fetch events from; must not be null
     * @throws NullPointerException if url is null
     */
    public JsonFeedEventSource(String url) {
        this.url = Objects.requireNonNull(url, "url must not be null");
    }

    /**
     * Sets the HTTP method for requests to this source.
     * @param method {@code "GET"} or {@code "POST"}
     * @return this
     */
    public JsonFeedEventSource withMethod(String method) {
        this.method = method;
        return this;
    }

    /**
     * Sets extra static query parameters appended to every request. The map keys are the parameter names
     * and the values are sent as-is alongside the automatically populated {@code start}, {@code end},
     * and {@code timeZone} parameters.
     * <br><br>
     * Example: {@code .withExtraParams(Map.of("roomId", "101", "building", "A"))} results in
     * {@code ?start=...&end=...&roomId=101&building=A}.
     *
     * @param extraParams map of parameter names to values; must not be null
     * @return this
     */
    public JsonFeedEventSource withExtraParams(Map<String, Object> extraParams) {
        this.extraParams = extraParams;
        return this;
    }

    /**
     * Overrides the <em>name</em> of the query parameter that carries the start of the visible date range.
     * FullCalendar populates the value automatically as an ISO 8601 datetime string
     * (e.g. {@code ?start=2025-03-01T00:00:00}). The default parameter name is {@code "start"}.
     * <br><br>
     * Use this when your backend expects a different name, e.g. {@code .withStartParam("from")} results in
     * {@code ?from=2025-03-01T00:00:00&end=...}.
     *
     * @param startParam query parameter name for the start date; must not be null
     * @return this
     */
    public JsonFeedEventSource withStartParam(String startParam) {
        this.startParam = startParam;
        return this;
    }

    /**
     * Overrides the <em>name</em> of the query parameter that carries the end of the visible date range.
     * FullCalendar populates the value automatically as an ISO 8601 datetime string
     * (e.g. {@code ?end=2025-04-01T00:00:00}). The default parameter name is {@code "end"}.
     *
     * @param endParam query parameter name for the end date; must not be null
     * @return this
     */
    public JsonFeedEventSource withEndParam(String endParam) {
        this.endParam = endParam;
        return this;
    }

    /**
     * Overrides the <em>name</em> of the query parameter that carries the calendar's current timezone.
     * FullCalendar populates the value automatically as a timezone string (e.g. {@code ?timeZone=UTC} or
     * {@code ?timeZone=Europe/Berlin}). The default parameter name is {@code "timeZone"}.
     * This parameter is only sent when the calendar's timezone is not set to {@code "local"}.
     *
     * @param timeZoneParam query parameter name for the timezone; must not be null
     * @return this
     */
    public JsonFeedEventSource withTimeZoneParam(String timeZoneParam) {
        this.timeZoneParam = timeZoneParam;
        return this;
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = JsonFactory.createObject();
        addCommonToJson(json);
        json.put("url", url);
        json.put("method", method);
        if (startParam != null) json.put("startParam", startParam);
        if (endParam != null) json.put("endParam", endParam);
        if (timeZoneParam != null) json.put("timeZoneParam", timeZoneParam);
        if (extraParams != null && !extraParams.isEmpty()) {
            JsonObject paramsNode = JsonFactory.createObject();
            extraParams.forEach((k, v) -> paramsNode.put(k, JsonUtils.toJsonValue(v)));
            json.put("extraParams", paramsNode);
        }
        return json;
    }
}
