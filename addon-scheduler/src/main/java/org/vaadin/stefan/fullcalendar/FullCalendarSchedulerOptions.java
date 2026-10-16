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
import org.vaadin.stefan.fullcalendar.option.ComponentResourceColumn;
import org.vaadin.stefan.fullcalendar.option.ResourceColumn;

import java.util.List;
import java.util.Objects;

/**
 * The options of a scheduler, as typed fields: those of {@link FullCalendarOptions} and the scheduler's own. Get it
 * with {@link FullCalendarScheduler#options()}. It works like {@link FullCalendarOptions}.
 */
public class FullCalendarSchedulerOptions extends FullCalendarOptions {

    // The core field is private, and view(…) needs the scheduler type.
    private final FullCalendarScheduler scheduler;

    FullCalendarSchedulerOptions(FullCalendarScheduler calendar) {
        super(calendar);
        this.scheduler = calendar;
    }

    /**
     * Returns the options of one view type, with the core and the scheduler options that can apply per view, see
     * {@link FullCalendarOptions#view(CalendarView)}.
     *
     * @param view the view type, a built-in or a custom one
     * @return the options of the view
     * @throws NullPointerException when null is passed
     */
    @Override
    public FullCalendarSchedulerViewOptions view(CalendarView view) {
        Objects.requireNonNull(view);
        return new FullCalendarSchedulerViewOptions(scheduler, view.getClientSideValue());
    }

    /**
     * Sets the columns of the resource area, see {@link #getResourceColumns()}. The component columns are bound to
     * this calendar, and the columns set before are unbound. An empty list removes the option, as null does.
     *
     * @param resourceColumns the columns, or null to remove the option
     * @throws IllegalArgumentException when two columns have the same field
     * @throws IllegalStateException when a {@link ComponentResourceColumn} is bound to another calendar
     */
    public void setResourceColumns(List<? extends ResourceColumn> resourceColumns) {
        scheduler.applyResourceColumns(resourceColumns);
    }

    // The same instances as the core accessors. The core methods are inherited, so the delegates exclude them.

    @Delegate(excludes = CalendarOptions.class)
    private SchedulerCalendarOptions schedulerCalendarOptions() {
        return (SchedulerCalendarOptions) calendarOptions();
    }

    @Delegate(excludes = ViewOptions.class)
    private SchedulerViewOptions schedulerViewOptions() {
        return (SchedulerViewOptions) viewOptions();
    }
}
