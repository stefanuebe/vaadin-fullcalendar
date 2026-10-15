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

import lombok.experimental.Delegate;

import java.io.Serializable;
import java.util.Objects;

/**
 * The options of one view type, as typed fields. Get it with {@code calendar.options().view(view)}. It works like
 * {@link FullCalendarOptions}, a kept reference included, but has only the options that can apply per view.
 * <p>
 * A value set here overrides the calendar-wide value while that view is shown. A getter returns only the value set for
 * this view, so it is null when only a calendar-wide value exists. Null removes the view's value, so the view falls
 * back to the calendar-wide value.
 */
public class FullCalendarViewOptions implements Serializable {

    private final FullCalendar calendar;
    private final String view;

    FullCalendarViewOptions(FullCalendar calendar, String view) {
        this.calendar = Objects.requireNonNull(calendar);
        this.view = Objects.requireNonNull(view);
    }

    /**
     * Removes all values set for this view, so the view falls back to the calendar-wide values.
     */
    public void clear() {
        calendar.perViewOptions.remove(view);
        calendar.optionsChanged();
    }

    // Every call, getters included, schedules the option sync. A getter creates an empty instance, which adds no
    // entry to views.
    @Delegate
    private ViewOptions viewOptions() {
        calendar.optionsChanged();
        return calendar.perViewOptions.computeIfAbsent(view, key -> new ViewOptions());
    }
}
