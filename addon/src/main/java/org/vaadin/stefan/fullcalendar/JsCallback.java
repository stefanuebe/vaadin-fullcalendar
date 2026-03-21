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

import tools.jackson.databind.node.ObjectNode;

import java.io.Serializable;
import java.util.Objects;

/**
 * Wraps a JavaScript function string for safe transport from Java to the client.
 * When the client encounters a JsCallback value in option JSON, it evaluates the
 * string via {@code new Function("return " + jsFunction)()} to produce a real
 * JS function object before passing it to FullCalendar.
 *
 * <p><b>Custom property injection:</b> For well-known entry callback keys
 * ({@code eventDidMount}, {@code eventContent}, {@code eventClassNames},
 * {@code eventWillUnmount}, {@code eventOverlap}, {@code eventAllow}, {@code selectOverlap}),
 * the client automatically injects {@code event.getCustomProperty(key, defaultValue)} on the
 * event objects passed to the callback. This mirrors data set on the server via
 * {@link Entry#setCustomProperty(String, Object)}.
 *
 * <p><b>CSP note:</b> This class uses {@code new Function()} on the client side, which requires
 * {@code unsafe-eval} in the Content Security Policy. This is compatible with Vaadin's default
 * CSP setup (which already requires {@code unsafe-eval}), but is <b>incompatible with Vaadin's
 * experimental strict CSP mode</b>. This is not a new limitation — the previous
 * callback API had the same requirement.
 *
 * <p>Example:
 * <pre>{@code
 * calendar.setOption(Option.ENTRY_DID_MOUNT,
 *     JsCallback.of("function(arg) { arg.el.title = arg.event.title; }"));
 * }</pre>
 */
public class JsCallback implements Serializable {

    private final String jsFunction;

    private JsCallback(String jsFunction) {
        Objects.requireNonNull(jsFunction);
        this.jsFunction = jsFunction.strip();
    }

    /**
     * Creates a JsCallback wrapping the given JavaScript function string.
     * Returns {@code null} if {@code jsFunction} is {@code null} or blank (whitespace-only),
     * as a convenience for "clear callback" patterns:
     * {@code setOption(Option.X, JsCallback.of(null))} and
     * {@code setOption(Option.X, JsCallback.of(""))} both clear the option.
     * <p>
     * The function string is passed to the browser as-is. No syntax validation is performed
     * server-side. Syntactically invalid JavaScript will produce a {@code SyntaxError} in the
     * browser console at evaluation time.
     *
     * @param jsFunction the JavaScript function string, or {@code null}/blank to clear
     * @return a new JsCallback, or {@code null} if the input is {@code null} or blank
     */
    public static JsCallback of(String jsFunction) {
        return jsFunction == null || jsFunction.isBlank() ? null : new JsCallback(jsFunction);
    }

    /**
     * Returns {@code null}, for use as a readable way to clear a previously set callback:
     * <pre>{@code
     * calendar.setOption(Option.ENTRY_DID_MOUNT, JsCallback.clearCallback());
     * }</pre>
     * Equivalent to passing {@code null} directly to {@code setOption}, but more expressive
     * about the intent.
     *
     * @return always {@code null}
     */
    public static JsCallback clearCallback() {
        return null;
    }

    /**
     * Returns the JavaScript function string.
     * @return JS function string
     */
    public String getJsFunction() {
        return jsFunction;
    }

    /**
     * Produces the JSON marker object that the client recognises as a JsCallback.
     * The marker has the form {@code {"__jsCallback": "function..."}}.
     *
     * @return ObjectNode marker
     */
    public ObjectNode toMarkerJson() {
        ObjectNode marker = JsonFactory.createObject();
        marker.put("__jsCallback", jsFunction);
        return marker;
    }

    @Override
    public String toString() {
        return "JsCallback{" + jsFunction + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsCallback that = (JsCallback) o;
        return jsFunction.equals(that.jsFunction);
    }

    @Override
    public int hashCode() {
        return jsFunction.hashCode();
    }
}
