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

import org.vaadin.stefan.fullcalendar.option.JsCallback;
import tools.jackson.databind.JsonNode;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The part both options objects share, {@link FullCalendarOptions} and {@link FullCalendarViewOptions}: options set
 * by their name in the FullCalendar library, bypassing the add-on's types.
 */
public abstract class AbstractOptions implements Serializable {

    AbstractOptions() {
    }

    /**
     * Sets an option by its name in the FullCalendar library, bypassing the add-on's types. Null removes it.
     * <p>
     * This is an expert feature for options the add-on has no typed way for. The value is passed to the client as
     * it is, converted to JSON: a {@link JsCallback} becomes a function, a {@link ClientSideValue} its client-side
     * value. The caller is responsible for a value FullCalendar understands.
     * <p>
     * A raw option wins over the field with the same name. Removing it brings the field's current value back. On the
     * options of a view, the raw option applies while that view is shown, like the view's fields.
     * <p>
     * For an option the add-on has its own handler for, such as {@code eventClick} or {@code viewDidMount}, a raw
     * callback replaces that handler, also when set before the calendar is shown. The server then no longer gets the
     * matching event, for example the {@link EntryClickedEvent}. The typed field
     * {@link FullCalendarOptions#setViewDidMount(org.vaadin.stefan.fullcalendar.option.JsCallback)} runs after the
     * handler instead.
     * <p>
     * The time zone cannot be a raw option, because the server converts the entry times with the zone of the field.
     * Set it with {@link FullCalendarOptions#setTimeZone(Timezone)}.
     *
     * @param option the option's name in the FullCalendar library
     * @param value  value, or null (also a JSON null) to remove the option
     * @throws NullPointerException            when the option is null
     * @throws IllegalArgumentException        when the option is {@code timeZone}
     * @throws OptionValueConversionException when the value cannot be converted to JSON
     * @see <a href="https://fullcalendar.io/docs">FullCalendar documentation</a>
     */
    public void setRawOption(String option, Object value) {
        Objects.requireNonNull(option);
        if (option.equals("timeZone")) {
            // the server converts entry times with the field, so the client must not show another zone
            throw new IllegalArgumentException("Set the time zone with options().setTimeZone(Timezone), not as a raw option");
        }
        JsonNode json = value == null ? null : toJson(value);
        if (json == null || json.isNull()) {
            rawOptions().remove(option);
        } else {
            rawOptions().put(option, json);
        }
        rawOptionsChanged();
    }

    /**
     * Returns the raw option with the given name, in the JSON form the client gets. The JSON is a deep copy, so
     * changing it does not change the option. For a large value, such as a long list, the copy costs time.
     *
     * @param option the option's name in the FullCalendar library
     * @return a copy of the option's JSON, or empty when it is not set
     * @throws NullPointerException when the option is null
     * @see #setRawOption(String, Object)
     */
    public Optional<JsonNode> getRawOption(String option) {
        Objects.requireNonNull(option);
        return Optional.ofNullable(rawOptions().get(option)).map(JsonNode::deepCopy);
    }

    /** The raw options this object writes to, in their client form by library name. */
    abstract Map<String, JsonNode> rawOptions();

    /** Schedules the option sync after a raw option changed. */
    abstract void rawOptionsChanged();

    /** Converts the value to JSON and copies it, so changing the caller's JSON later does not change the option. */
    private static JsonNode toJson(Object value) {
        try {
            return JsonUtils.toJsonNodeWithJackson(value).deepCopy();
        } catch (RuntimeException e) {
            throw new OptionValueConversionException(value, e);
        }
    }
}
