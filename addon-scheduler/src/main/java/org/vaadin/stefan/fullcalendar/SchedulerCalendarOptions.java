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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.vaadin.stefan.fullcalendar.converter.ResourceColumnsConverter;
import org.vaadin.stefan.fullcalendar.json.JsonConverter;
import org.vaadin.stefan.fullcalendar.option.AbstractResourceColumn;
import org.vaadin.stefan.fullcalendar.option.ComponentResourceColumn;

import java.util.List;

/**
 * The scheduler options that apply to the whole calendar only, never per view, besides those of
 * {@link CalendarOptions}. Null means not set. This class is not public API. Users reach the fields through
 * {@link FullCalendarScheduler#options()}.
 */
@Getter
@Setter
class SchedulerCalendarOptions extends CalendarOptions {

    /**
     * The columns of the resource area. They turn it from a plain list of resource titles into a grid of data. Each
     * column shows a property of the resource and can be grouped, sized and customized. A
     * {@link ComponentResourceColumn} shows a Vaadin component per resource.
     * <p>
     * The public setter is {@link FullCalendarSchedulerOptions#setResourceColumns(List)}. Besides the field, it binds
     * the component columns to the calendar. The list is unmodifiable.
     * <p>
     * The library default is no columns. The resource area is then a plain list of resource titles.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resourceColumns">resourceColumns</a>
     */
    @Setter(AccessLevel.NONE)
    @JsonConverter(ResourceColumnsConverter.class)
    private List<AbstractResourceColumn<?>> resourceColumns;

    /** Sets the field only. The binding of the component columns is in {@link FullCalendarScheduler}. */
    void setResourceColumns(List<AbstractResourceColumn<?>> resourceColumns) {
        this.resourceColumns = resourceColumns;
    }

    /**
     * Refetches and rerenders the resources when the user changes the date or the view. The resources function or
     * JSON feed then receives {@code start}, {@code end} and {@code timezone}.
     * <p>
     * The add-on sends the resources it manages as a fixed list, for which this option has no effect. It only matters
     * for a resources function or feed that is set on the client itself.
     * <p>
     * The library default is {@code false}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/refetchResourcesOnNavigate">refetchResourcesOnNavigate</a>
     */
    private Boolean refetchResourcesOnNavigate;

    // FC-UPDATE: check the global scheduler options of the new library version against these fields. The test
    //  resource option-names.txt of this module lists the supported scheduler names as global or per view and says
    //  how to re-derive them.
}
