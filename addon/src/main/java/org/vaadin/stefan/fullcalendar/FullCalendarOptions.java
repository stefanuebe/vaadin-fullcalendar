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

import org.vaadin.stefan.fullcalendar.option.Timezone;
import lombok.experimental.Delegate;
import org.vaadin.stefan.fullcalendar.converters.JsonItemPropertyConverter;
import org.vaadin.stefan.fullcalendar.json.JsonConverter;
import org.vaadin.stefan.fullcalendar.json.JsonName;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The options of a calendar, as typed fields. Get it with {@link FullCalendar#options()}. A setter changes the
 * calendar, and the change reaches the browser with the next response. Null means not set, then FullCalendar's default
 * applies.
 * <p>
 * The object is live, not a copy. A reference kept for later works in a later request too.
 */
public class FullCalendarOptions extends AbstractOptions {

    private static final Map<Class<?>, List<OptionField>> FIELDS = new ConcurrentHashMap<>();

    private final FullCalendar calendar;

    FullCalendarOptions(FullCalendar calendar) {
        this.calendar = Objects.requireNonNull(calendar);
    }

    /**
     * Sets the time zone the calendar shows the entries in, see {@link #getTimeZone()}.
     * <p>
     * {@link Timezone#LOCAL} lets the calendar follow the browser: each report of the browser's zone sets the zone the
     * server converts with, while the browser keeps showing its own zone. If the browser has reported already, its
     * zone applies at once. Any other zone is shown as it is and ends following the browser.
     *
     * @param timeZone the time zone
     * @throws NullPointerException when null is passed
     */
    public void setTimeZone(Timezone timeZone) {
        calendar.applyTimeZone(timeZone);
    }

    /**
     * Returns the options of one view type. A value set there overrides the calendar-wide value while that view is
     * shown. The returned object is live, like this one.
     * <p>
     * The values of all views reach the client as the library option {@code views}. A raw option {@code views}
     * replaces them all.
     *
     * @param view the view type, a built-in or a custom one
     * @return the options of the view
     * @throws NullPointerException when null is passed
     */
    public FullCalendarViewOptions view(CalendarView view) {
        Objects.requireNonNull(view);
        return new FullCalendarViewOptions(calendar, view.getClientSideValue());
    }

    /** The fields of one data class with a value, in their client form, by library name. */
    static Map<String, JsonNode> toJson(Object data) {
        Map<String, JsonNode> json = new LinkedHashMap<>();
        for (OptionField field : FIELDS.computeIfAbsent(data.getClass(), OptionField::read)) {
            Object value = field.get(data);
            if (value != null) {
                json.put(field.name(), field.converter() != null
                        ? field.converter().toClientModel(value, null)
                        : JsonUtils.toJsonNode(value));
            }
        }

        return json;
    }

    // Every call, getters included, schedules the option sync. A getter costs one sync that finds no change.

    @Delegate
    private CalendarOptions calendarOptions() {
        calendar.optionsChanged();
        return calendar.calendarOptions;
    }

    @Delegate
    private ViewOptions viewOptions() {
        calendar.optionsChanged();
        return calendar.viewOptions;
    }

    @Override
    Map<String, JsonNode> rawOptions() {
        return calendar.rawOptions;
    }

    @Override
    void rawOptionsChanged() {
        calendar.optionsChanged();
    }

    /** The values of the single view types as the library's {@code views} object, or empty when none is set. */
    static Optional<ObjectNode> toViewsJson(Map<String, ViewOptions> perViewOptions) {
        ObjectNode views = JsonFactory.createObject();
        perViewOptions.forEach((view, options) -> {
            Map<String, JsonNode> json = toJson(options);
            if (!json.isEmpty()) {
                views.set(view, JsonFactory.createObject().setAll(json));
            }
        });

        return views.isEmpty() ? Optional.empty() : Optional.of(views);
    }

    /** A field of a data class with its library name and converter, read once per class. */
    @SuppressWarnings("rawtypes")
    private record OptionField(Field field, String name, JsonItemPropertyConverter converter) {

        static List<OptionField> read(Class<?> type) {
            List<OptionField> fields = new ArrayList<>();
            for (Class<?> c = type; c != Object.class; c = c.getSuperclass()) {
                for (Field field : c.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }

                    field.setAccessible(true);
                    JsonName name = field.getAnnotation(JsonName.class);
                    JsonConverter converter = field.getAnnotation(JsonConverter.class);
                    try {
                        fields.add(new OptionField(field, name != null ? name.value() : field.getName(),
                                converter != null ? converter.value().getConstructor().newInstance() : null));
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException("Cannot create the converter of " + field, e);
                    }
                }
            }

            return List.copyOf(fields);
        }

        Object get(Object data) {
            try {
                return field.get(data);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
