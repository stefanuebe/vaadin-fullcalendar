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

import org.vaadin.stefan.fullcalendar.option.BusinessHours;
import org.vaadin.stefan.fullcalendar.option.ClassName;
import org.vaadin.stefan.fullcalendar.option.ClassNameValue;
import org.vaadin.stefan.fullcalendar.option.ConstraintValue;
import org.vaadin.stefan.fullcalendar.option.JsCallback;
import org.vaadin.stefan.fullcalendar.option.Timezone;
import org.vaadin.stefan.fullcalendar.option.ViewName;
import lombok.AccessLevel;
import org.vaadin.stefan.fullcalendar.option.Direction;
import org.vaadin.stefan.fullcalendar.option.WeekNumberCalculationValue;
import org.vaadin.stefan.fullcalendar.converters.WeekNumberCalculationValueConverter;
import lombok.Getter;
import lombok.Setter;
import org.vaadin.stefan.fullcalendar.converters.ClassNameValueConverter;
import org.vaadin.stefan.fullcalendar.converters.ConstraintValueConverter;
import org.vaadin.stefan.fullcalendar.converters.DayOfWeekConverter;
import org.vaadin.stefan.fullcalendar.converters.LocalDateConverter;
import org.vaadin.stefan.fullcalendar.converters.ViewNameValueConverter;
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

    /**
     * CSS classes for the root element of the view. A callback runs whenever the view changes.
     * <p>
     * It gets an {@code info} object with {@code view} and more, see the library docs. It returns the class names
     * as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/view-render-hooks">viewClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue viewClass;

    /**
     * Sets fixed CSS classes, see {@link #getViewClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setViewClass(String classNames) {
        this.viewClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getViewClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setViewClass(ClassNameValue value) {
        this.viewClass = value;
    }

    /**
     * A callback that runs right before the root element of the view is removed from the DOM.
     * <p>
     * It gets an {@code info} object with {@code view}, {@code el} (the root element) and more, see the library
     * docs.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered
     * elements, for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/view-render-hooks">viewWillUnmount</a>
     */
    private JsCallback viewWillUnmount;

    // Dates, ranges and durations

    /**
     * The direction in which the calendar renders its elements, left-to-right or right-to-left. It is useful for
     * right-to-left languages such as Arabic and Hebrew.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/direction">direction</a>
     */
    private Direction direction;

    /**
     * The view the calendar renders when it loads, a built-in or a custom one. The library reads it when the
     * calendar is created. Use {@link FullCalendar#changeView(CalendarView)} to change the view of a calendar that
     * is already shown.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/initialView">initialView</a>
     */
    @JsonConverter(ViewNameValueConverter.class)
    private ViewName initialView;

    /**
     * Sets the view the calendar renders when it loads, by the name of a view, see {@link #getInitialView()}.
     *
     * @param name the view name, such as {@code "timeGridDay"}, or null to remove the option
     */
    public void setInitialView(String name) {
        this.initialView = name == null ? null : ViewName.of(name);
    }

    /**
     * Sets the view the calendar renders when it loads, see {@link #getInitialView()}.
     *
     * @param view the view, a built-in or a custom one, or null to remove the option
     */
    public void setInitialView(CalendarView view) {
        this.initialView = view == null ? null : ViewName.of(view);
    }

    /**
     * Sets the view the calendar renders when it loads, see {@link #getInitialView()}.
     *
     * @param value the view name, or null to remove the option
     */
    public void setInitialView(ViewName value) {
        this.initialView = value;
    }

    /**
     * How the week numbers are calculated: by the locale, by ISO 8601 (which also makes Monday the default first
     * day of the week), or by a callback that gets a date and returns its week number.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/weekNumberCalculation">weekNumberCalculation</a>
     */
    @JsonConverter(WeekNumberCalculationValueConverter.class)
    private WeekNumberCalculationValue weekNumberCalculation;

    // Time grid and slots

    // Interaction

    /**
     * Exact programmatic control over where an entry can be dropped. The callback must return a boolean synchronously.
     * <p>
     * The callback runs for every new potential drop position while the user is dragging, after
     * the overlap and constraint settings of entries have allowed the position. It gets a
     * {@code dropInfo} with {@code start}, {@code end}, {@code allDay} and more, and the dragged entry. See the library
     * docs.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/eventAllow">eventAllow</a>
     */
    @JsonName("eventAllow")
    private JsCallback entryAllow;

    /**
     * Limits entry dragging and resizing to certain windows of time.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/eventConstraint">eventConstraint</a>
     */
    @JsonName("eventConstraint")
    @JsonConverter(ConstraintValueConverter.class)
    private ConstraintValue entryConstraint;

    /**
     * Limits entry dragging and resizing to the entries of a group, see {@link #getEntryConstraint()}. A dragged
     * entry must be fully contained by at least one entry of that group.
     *
     * @param groupId the group id, or null to remove the option
     */
    public void setEntryConstraint(String groupId) {
        this.entryConstraint = groupId == null ? null : ConstraintValue.ofGroupId(groupId);
    }

    /**
     * Limits entry dragging and resizing to recurring windows of time, see {@link #getEntryConstraint()}.
     *
     * @param windows the windows, at least one, or null to remove the option
     */
    public void setEntryConstraint(BusinessHours... windows) {
        this.entryConstraint = windows == null ? null : ConstraintValue.ofWindows(windows);
    }

    /**
     * Limits entry dragging and resizing to windows of time, recurring ones and fixed spans mixed, see
     * {@link #getEntryConstraint()}.
     *
     * @param windows the windows, at least one, or null to remove the option
     */
    public void setEntryConstraint(ConstraintValue.Window... windows) {
        this.entryConstraint = windows == null ? null : ConstraintValue.ofWindows(windows);
    }

    /**
     * Limits entry dragging and resizing in any form, see {@link #getEntryConstraint()}. This is the way to pass
     * {@link ConstraintValue#BUSINESS_HOURS}.
     *
     * @param value the constraint, or null to remove the option
     */
    public void setEntryConstraint(ConstraintValue value) {
        this.entryConstraint = value;
    }

    /**
     * Whether entries can be resized to change their duration. It decides resizing on its own and takes precedence
     * over {@code editable}. A single entry can override it.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/eventDurationEditable">eventDurationEditable</a>
     */
    @JsonName("eventDurationEditable")
    private Boolean entryDurationEditable;

    /**
     * Whether entries can be dragged to change their start. It decides dragging on its own and takes precedence
     * over {@code editable}. A single entry can override it.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/eventStartEditable">eventStartEditable</a>
     */
    @JsonName("eventStartEditable")
    private Boolean entryStartEditable;

    /**
     * Exact programmatic control over where the user can select. The callback must return a boolean synchronously.
     * <p>
     * The callback runs for every new potential selection while the user is dragging. It gets a {@code selectInfo}
     * with {@code start}, {@code end}, {@code allDay} and more. See the library docs.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/selectAllow">selectAllow</a>
     */
    private JsCallback selectAllow;

    /**
     * Limits where the user can select to certain windows of time. It takes the same values as
     * {@link #getEntryConstraint()} and applies when selecting is enabled.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/selectConstraint">selectConstraint</a>
     */
    @JsonConverter(ConstraintValueConverter.class)
    private ConstraintValue selectConstraint;

    /**
     * Limits selections to the entries of a group, see {@link #getSelectConstraint()}.
     *
     * @param groupId the group id, or null to remove the option
     */
    public void setSelectConstraint(String groupId) {
        this.selectConstraint = groupId == null ? null : ConstraintValue.ofGroupId(groupId);
    }

    /**
     * Limits selections to recurring windows of time, see {@link #getSelectConstraint()}.
     *
     * @param windows the windows, at least one, or null to remove the option
     */
    public void setSelectConstraint(BusinessHours... windows) {
        this.selectConstraint = windows == null ? null : ConstraintValue.ofWindows(windows);
    }

    /**
     * Limits selections to windows of time, recurring ones and fixed spans mixed, see {@link #getSelectConstraint()}.
     *
     * @param windows the windows, at least one, or null to remove the option
     */
    public void setSelectConstraint(ConstraintValue.Window... windows) {
        this.selectConstraint = windows == null ? null : ConstraintValue.ofWindows(windows);
    }

    /**
     * Limits selections in any form, see {@link #getSelectConstraint()}. This is the way to pass
     * {@link ConstraintValue#BUSINESS_HOURS}.
     *
     * @param value the constraint, or null to remove the option
     */
    public void setSelectConstraint(ConstraintValue value) {
        this.selectConstraint = value;
    }

    // Entry display and layout

    // Texts, hints and formats
}
