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

import java.util.List;

/**
 * Abstract base class for all client-managed event sources. Client-managed sources (JSON feed, Google Calendar, iCal)
 * fetch events directly in the browser — they do NOT go through the server-side {@link org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider}.
 *
 * @param <S> concrete subtype for fluent chaining
 * @deprecated Use {@link RemoteEntrySource}, the new name of this class. It has the same methods.
 */
@Deprecated(since = "7.3.0")
public abstract class ClientSideEventSource<S extends ClientSideEventSource<S>> extends RemoteEntrySource<S> {

    // The overrides keep the erased return type ClientSideEventSource, so code compiled against an earlier 7.x still
    // finds these methods.

    @Override
    protected S self() {
        return super.self();
    }

    @Override
    public S withId(String id) {
        return super.withId(id);
    }

    @Override
    public S withColor(String color) {
        return super.withColor(color);
    }

    @Override
    public S withBackgroundColor(String backgroundColor) {
        return super.withBackgroundColor(backgroundColor);
    }

    @Override
    public S withBorderColor(String borderColor) {
        return super.withBorderColor(borderColor);
    }

    @Override
    public S withTextColor(String textColor) {
        return super.withTextColor(textColor);
    }

    @Override
    public S withClassNames(List<String> classNames) {
        return super.withClassNames(classNames);
    }

    @Override
    public S withEditable(boolean editable) {
        return super.withEditable(editable);
    }

    @Override
    public S withStartEditable(boolean startEditable) {
        return super.withStartEditable(startEditable);
    }

    @Override
    public S withDurationEditable(boolean durationEditable) {
        return super.withDurationEditable(durationEditable);
    }

    @Override
    public S withConstraint(String constraint) {
        return super.withConstraint(constraint);
    }

    @Override
    public S withOverlap(boolean overlap) {
        return super.withOverlap(overlap);
    }

    @Override
    public S withDisplay(String display) {
        return super.withDisplay(display);
    }

    @Override
    public S withResourceEditable(boolean resourceEditable) {
        return super.withResourceEditable(resourceEditable);
    }

    @Override
    public S withDefaultAllDay(boolean defaultAllDay) {
        return super.withDefaultAllDay(defaultAllDay);
    }

    @Override
    public S withAllow(String jsFunction) {
        return super.withAllow(jsFunction);
    }

    @Override
    public S withAllow(JsCallback callback) {
        return super.withAllow(callback);
    }

    @Override
    public S withSuccess(String jsFunction) {
        return super.withSuccess(jsFunction);
    }

    @Override
    public S withSuccess(JsCallback callback) {
        return super.withSuccess(callback);
    }

    @Override
    public S withFailure(String jsFunction) {
        return super.withFailure(jsFunction);
    }

    @Override
    public S withFailure(JsCallback callback) {
        return super.withFailure(callback);
    }

    @Override
    public S withEventDataTransform(String jsFunction) {
        return super.withEventDataTransform(jsFunction);
    }

    @Override
    public S withEventDataTransform(JsCallback callback) {
        return super.withEventDataTransform(callback);
    }
}
