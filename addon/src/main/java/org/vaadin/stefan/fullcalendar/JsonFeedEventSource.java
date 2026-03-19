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
     * Sets extra query parameters sent on every request.
     * @param extraParams map of parameter names to values
     * @return this
     */
    public JsonFeedEventSource withExtraParams(Map<String, Object> extraParams) {
        this.extraParams = extraParams;
        return this;
    }

    /**
     * Overrides the start parameter name for this source.
     * @param startParam parameter name
     * @return this
     */
    public JsonFeedEventSource withStartParam(String startParam) {
        this.startParam = startParam;
        return this;
    }

    /**
     * Overrides the end parameter name for this source.
     * @param endParam parameter name
     * @return this
     */
    public JsonFeedEventSource withEndParam(String endParam) {
        this.endParam = endParam;
        return this;
    }

    /**
     * Overrides the timeZone parameter name for this source.
     * @param timeZoneParam parameter name
     * @return this
     */
    public JsonFeedEventSource withTimeZoneParam(String timeZoneParam) {
        this.timeZoneParam = timeZoneParam;
        return this;
    }

    @Override
    public ObjectNode toJson() {
        ObjectNode json = JsonFactory.createObject();
        addCommonToJson(json);
        json.put("url", url);
        json.put("method", method);
        if (startParam != null) json.put("startParam", startParam);
        if (endParam != null) json.put("endParam", endParam);
        if (timeZoneParam != null) json.put("timeZoneParam", timeZoneParam);
        if (extraParams != null && !extraParams.isEmpty()) {
            ObjectNode paramsNode = JsonFactory.createObject();
            extraParams.forEach((k, v) -> paramsNode.set(k, JsonUtils.toJsonNode(v)));
            json.set("extraParams", paramsNode);
        }
        return json;
    }
}
