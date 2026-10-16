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
import org.vaadin.stefan.fullcalendar.option.Timezone;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.vaadin.stefan.fullcalendar.converters.DayOfWeekConverter;
import org.vaadin.stefan.fullcalendar.converters.LocalDateConverter;
import org.vaadin.stefan.fullcalendar.json.JsonConverter;
import org.vaadin.stefan.fullcalendar.json.JsonName;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * The options that apply to the whole calendar only, never per view. Null means not set. This class is not public
 * API. Users reach the fields through {@link FullCalendar#options()}.
 */
@Getter
@Setter
class CalendarOptions implements Serializable {

    // FC-UPDATE: check the global options of the new library version against these fields. The test resource
    //  option-names.txt lists the supported names as global or per view and says how to re-derive them.

    /**
     * First day of the week. Without a value, the locale decides.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/firstDay">firstDay</a>
     */
    @JsonConverter(DayOfWeekConverter.class)
    private DayOfWeek firstDay;

    /**
     * The date the calendar shows when it is created. The default is the current date.
     * <p>
     * It has no effect on a calendar the browser already shows. Use {@link FullCalendar#gotoDate(LocalDate)} instead.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see FullCalendar#gotoDate(LocalDate)
     * @see <a href="https://fullcalendar.io/docs/initialDate">initialDate</a>
     */
    @JsonConverter(LocalDateConverter.class)
    private LocalDate initialDate;

    /**
     * The time zone the calendar shows the entries in. The server converts entry times with it. The default is
     * {@link Timezone#LOCAL}, the browser's zone. While the calendar follows the browser, this returns the zone the
     * browser reported, and {@code LOCAL}, which converts with UTC, before the first report.
     * <p>
     * The public setter is {@link FullCalendarOptions#setTimeZone(Timezone)}. Besides the field, it sets whether the
     * calendar follows the browser.
     *
     * @see <a href="https://fullcalendar.io/docs/timeZone">timeZone</a>
     */
    @Setter(AccessLevel.NONE)
    private Timezone timeZone = Timezone.LOCAL;

    /** Sets the field only. The rules for {@link Timezone#LOCAL} are in {@link FullCalendar}. */
    void setTimeZone(Timezone timeZone) {
        this.timeZone = timeZone;
    }

    // Render hooks: day cells, day headers, day lanes, all-day

    // Render hooks: time slots, now indicator, week numbers, tables

    // Render hooks: entry layouts (block, column, row)

    // Render hooks: entries

    /**
     * A callback that runs when the element of an entry has been added to the DOM. It gets an {@code info} object with
     * {@code event}, {@code el}, {@code view} and more, see the library docs.
     * <p>
     * The calendar merges it with its native entry listeners and the automatic entry id into one callback, see
     * {@link FullCalendar#addEntryNativeEventListener(String, String)} and
     * {@link FullCalendar#setAutoProvideEntryIdOnClient(boolean)}. The merge needs a function with a body in braces.
     * A callback without one, such as {@code info => info.el.title = 'x'}, is sent as it is, without the native
     * listeners and the entry id.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/event-render-hooks">eventDidMount</a>
     */
    @JsonName("eventDidMount")
    private JsCallback entryDidMount;

    // Render hooks: view, toolbar, buttons, single month
}
