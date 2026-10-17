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
import org.vaadin.stefan.fullcalendar.converters.ClassNameValueConverter;
import org.vaadin.stefan.fullcalendar.converters.ConstraintValueConverter;
import org.vaadin.stefan.fullcalendar.converters.DayOfWeekConverter;
import org.vaadin.stefan.fullcalendar.converters.InitialDateValueConverter;
import org.vaadin.stefan.fullcalendar.converters.ListTextValueConverter;
import org.vaadin.stefan.fullcalendar.converters.LocaleConverter;
import org.vaadin.stefan.fullcalendar.converters.MoreLinkClickValueConverter;
import org.vaadin.stefan.fullcalendar.converters.TextValueConverter;
import org.vaadin.stefan.fullcalendar.converters.ToolbarElementsConverter;
import org.vaadin.stefan.fullcalendar.converters.ToolbarValueConverter;
import org.vaadin.stefan.fullcalendar.converters.ViewNameValueConverter;
import org.vaadin.stefan.fullcalendar.converters.WeekNumberCalculationValueConverter;
import org.vaadin.stefan.fullcalendar.json.JsonConverter;
import org.vaadin.stefan.fullcalendar.json.JsonName;
import org.vaadin.stefan.fullcalendar.option.BusinessHours;
import org.vaadin.stefan.fullcalendar.option.CalendarLocale;
import org.vaadin.stefan.fullcalendar.option.ClassName;
import org.vaadin.stefan.fullcalendar.option.ClassNameValue;
import org.vaadin.stefan.fullcalendar.option.ConstraintValue;
import org.vaadin.stefan.fullcalendar.option.ContentValue;
import org.vaadin.stefan.fullcalendar.option.Direction;
import org.vaadin.stefan.fullcalendar.option.DisplayMode;
import org.vaadin.stefan.fullcalendar.option.InitialDateValue;
import org.vaadin.stefan.fullcalendar.option.JsCallback;
import org.vaadin.stefan.fullcalendar.option.ListTextValue;
import org.vaadin.stefan.fullcalendar.option.MoreLinkClickValue;
import org.vaadin.stefan.fullcalendar.option.NativeToolbarButtonDisplay;
import org.vaadin.stefan.fullcalendar.option.NativeToolbarParts;
import org.vaadin.stefan.fullcalendar.option.TextContent;
import org.vaadin.stefan.fullcalendar.option.TextValue;
import org.vaadin.stefan.fullcalendar.option.Timezone;
import org.vaadin.stefan.fullcalendar.option.ToolbarValue;
import org.vaadin.stefan.fullcalendar.option.ViewName;
import org.vaadin.stefan.fullcalendar.option.WeekNumberCalculationValue;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
     * The library's built-in toolbar below the calendar, see {@link ToolbarValue} for its sections.
     * <p>
     * The getter returns null while the option is not set; then the library default, no toolbar, applies.
     *
     * @see <a href="https://fullcalendar.io/docs/footerToolbar">footerToolbar</a>
     */
    @JsonConverter(ToolbarValueConverter.class)
    private ToolbarValue footerToolbar;

    /**
     * The library's built-in toolbar above the calendar, see {@link ToolbarValue} for its sections.
     * <p>
     * The initial value is {@link ToolbarValue#NONE}, because navigation belongs to Vaadin components. The library's
     * toolbar navigates on the client, and the server learns of it only through events such as the
     * {@link DatesRenderedEvent}. Null removes the option. The library's default, no toolbar, then applies.
     *
     * @see <a href="https://fullcalendar.io/docs/headerToolbar">headerToolbar</a>
     */
    @JsonConverter(ToolbarValueConverter.class)
    private ToolbarValue headerToolbar = ToolbarValue.NONE;

    /**
     * The date the calendar shows when it is created. The default is the current date.
     * <p>
     * A date and time is read as UTC, like the start and end of entries, and the calendar shows it in its time zone.
     * The time matters for views shorter than a day, such as a timeline of six hours, whose range starts at the given
     * hour.
     * <p>
     * It has no effect on a calendar the browser already shows. Use {@link FullCalendar#gotoDate(LocalDate)} instead.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see FullCalendar#gotoDate(LocalDate)
     * @see <a href="https://fullcalendar.io/docs/initialDate">initialDate</a>
     */
    @JsonConverter(InitialDateValueConverter.class)
    private InitialDateValue initialDate;

    /**
     * Sets a date as the initial date, see {@link #getInitialDate()}.
     *
     * @param date the date, or null to remove the option
     */
    public void setInitialDate(LocalDate date) {
        this.initialDate = date == null ? null : InitialDateValue.ofDay(date);
    }

    /**
     * Sets a date and time as the initial date, see {@link #getInitialDate()}.
     *
     * @param dateTime the date and time, read as UTC like the start and end of entries, or null to remove the option
     */
    public void setInitialDate(LocalDateTime dateTime) {
        this.initialDate = dateTime == null ? null : InitialDateValue.ofDateTime(dateTime);
    }

    /**
     * Sets the initial date in any form, see {@link #getInitialDate()}.
     *
     * @param value the date, or null to remove the option
     */
    public void setInitialDate(InitialDateValue value) {
        this.initialDate = value;
    }

    /**
     * The language and the regional formats of the calendar, such as the texts, the first day of the week and the date
     * formats.
     * <p>
     * The initial value is {@link CalendarLocale#getDefaultLocale()}. The calendar always has a locale, so it cannot be
     * removed. This option is not the Vaadin locale of the component, which {@code calendar.getLocale()} still returns.
     *
     * @see <a href="https://fullcalendar.io/docs/locale">locale</a>
     */
    @JsonConverter(LocaleConverter.class)
    @Setter(AccessLevel.NONE)
    private Locale locale = CalendarLocale.getDefaultLocale();

    /**
     * Sets the locale, see {@link #getLocale()}.
     *
     * @param locale the locale
     * @throws NullPointerException when null is passed
     */
    public void setLocale(Locale locale) {
        this.locale = Objects.requireNonNull(locale);
    }

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
     * A callback that runs right after the root element of the view has been added to the DOM.
     * <p>
     * It gets an {@code info} object with {@code view}, {@code el} (the root element) and more, see the library
     * docs.
     * <p>
     * The calendar has its own callback for this option, which sends the {@link ViewSkeletonRenderedEvent}. This
     * callback runs after it, so the server still gets the event. A raw option with this name replaces both, and the
     * server then no longer gets the event.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered
     * elements, for example after navigating to another view. Existing elements are not updated.
     * <p>
     * The getter returns null while the option is not set; then only the calendar's own callback runs.
     *
     * @see <a href="https://fullcalendar.io/docs/view-render-hooks">viewDidMount</a>
     */
    private JsCallback viewDidMount;

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
     * Limits entry dragging and resizing to certain windows of time: recurring business hours, windows defined by an
     * {@link RRule} and fixed spans.
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
     * Limits entry dragging and resizing to windows of time, business hours, RRule windows and fixed spans mixed, see
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
     * A callback that runs when the user clicks a day link, see {@link ViewOptions#getNavLinks()}.
     * <p>
     * It gets the {@code date} and the {@code jsEvent}, see the library docs.
     * <p>
     * The calendar has its own callback for this option, which sends the {@link DayNumberClickedEvent}. This callback
     * runs after it, so the server still gets the event. A raw option with this name replaces both, also one with a
     * view name, which the library accepts too. The server then no longer gets the event.
     * <p>
     * The calendar does not change the view on such a click. To show another view, call
     * {@link FullCalendar#changeView(CalendarView)} in a listener of the server event.
     * <p>
     * The getter returns null while the option is not set; then only the calendar's own callback runs.
     *
     * @see <a href="https://fullcalendar.io/docs/navLinkDayClick">navLinkDayClick</a>
     */
    private JsCallback navLinkDayClick;

    /**
     * A callback that runs when the user clicks a week link, see {@link ViewOptions#getNavLinks()}.
     * <p>
     * It gets the {@code weekStart} and the {@code jsEvent}, see the library docs.
     * <p>
     * The calendar has its own callback for this option, which sends the {@link WeekNumberClickedEvent}. This callback
     * runs after it, so the server still gets the event. A raw option with this name replaces both, also one with a
     * view name, which the library accepts too. The server then no longer gets the event.
     * <p>
     * The calendar does not change the view on such a click. To show another view, call
     * {@link FullCalendar#changeView(CalendarView)} in a listener of the server event.
     * <p>
     * The getter returns null while the option is not set; then only the calendar's own callback runs.
     *
     * @see <a href="https://fullcalendar.io/docs/navLinkWeekClick">navLinkWeekClick</a>
     */
    private JsCallback navLinkWeekClick;

    /**
     * What happens when the user clicks a "+N more" link, which the entry limits create: an action such as
     * {@link MoreLinkClickValue.Action#POPUP}, a view to go to, or a callback. The library default is the popover.
     * <p>
     * A callback gets an {@code info} object with {@code date}, {@code allSegs}, {@code jsEvent} and more, see the
     * library docs. It can return an action or a view name, which then applies.
     * <p>
     * The calendar has its own callback for this option, which sends the {@link MoreLinkClickedEvent}, and applies this
     * value after it. The server gets the event with every value, also with a raw option.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/moreLinkClick">moreLinkClick</a>
     */
    @Setter(AccessLevel.NONE)
    @JsonConverter(MoreLinkClickValueConverter.class)
    private MoreLinkClickValue moreLinkClick;

    /**
     * Sets the view to go to, see {@link #getMoreLinkClick()}.
     *
     * @param viewName the client-side view name, such as {@code "timeGridDay"}, or null to remove the option
     */
    public void setMoreLinkClick(String viewName) {
        this.moreLinkClick = viewName == null ? null : ViewName.of(viewName);
    }

    /**
     * Sets the view to go to, see {@link #getMoreLinkClick()}.
     *
     * @param view the view, or null to remove the option
     */
    public void setMoreLinkClick(CalendarView view) {
        this.moreLinkClick = view == null ? null : ViewName.of(view);
    }

    /**
     * Sets the value in any form, see {@link #getMoreLinkClick()}.
     *
     * @param value the action, view name or callback, or null to remove the option
     */
    public void setMoreLinkClick(MoreLinkClickValue value) {
        this.moreLinkClick = value;
    }

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
     * Limits selections to windows of time, business hours, RRule windows and fixed spans mixed, see
     * {@link #getSelectConstraint()}.
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

    /**
     * CSS classes for the root element of the calendar. FullCalendar adds them to the classes the add-on sets there.
     * <p>
     * A callback gets an {@code info} object with {@code borderlessX}, {@code borderlessTop} and
     * {@code borderlessBottom}. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/className">className</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue className;

    /**
     * Sets fixed CSS classes for the root element, see {@link #getClassName()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setClassName(String classNames) {
        this.className = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the root element in any form, see {@link #getClassName()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setClassName(ClassNameValue value) {
        this.className = value;
    }

    /**
     * The milliseconds the calendar waits after an operation that may rerender it, such as changing the view or adding
     * an entry, before it rerenders everything at once. This can reduce the number of rerenders. Without a value, the
     * calendar rerenders at once.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/rerenderDelay">rerenderDelay</a>
     */
    private Integer rerenderDelay;

    /**
     * How the calendar renders all entries, for example as a block, as a list item with a dot, or as a background
     * entry. An entry with its own {@link Entry#setDisplayMode(DisplayMode) display mode} keeps it.
     * <p>
     * {@link DisplayMode#NONE} hides the entries in the browser only. They are still sent to the client with all their
     * data.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/eventDisplay">eventDisplay</a>
     */
    @JsonName("eventDisplay")
    private DisplayMode entryDisplay;

    // Texts, hints and formats

    /**
     * Text of the day view button of the built-in toolbar. Each locale defines this text, see the locale page.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/locale">dayText (locale object)</a>
     */
    private String dayText;

    /**
     * Text of the list view button of the built-in toolbar, or {@link ListTextValue#NONE} to show buttons named by
     * duration, such as "Week" or "Month", instead of a "List" button. Without a value, the locale's text shows.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/list-view">listText (List view: Toolbar Buttons)</a>
     */
    @JsonConverter(ListTextValueConverter.class)
    private ListTextValue listText;

    /**
     * Sets a fixed text, see {@link #getListText()}.
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setListText(String text) {
        this.listText = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the value in any form, see {@link #getListText()}.
     *
     * @param value the text or {@link ListTextValue#NONE}, or null to remove the option
     */
    public void setListText(ListTextValue value) {
        this.listText = value;
    }

    /**
     * Text of the month view button of the built-in toolbar. Each locale defines this text, see the locale page.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/locale">monthText (locale object)</a>
     */
    private String monthText;

    /**
     * Accessible label ({@code aria-label}) of the "next" button in the built-in toolbar. Without a value, the locale's
     * text is used.
     * <p>
     * A text may use {@code $0} as placeholder for the localized unit text, such as "week". A callback gets the
     * localized unit text and a canonical unit id such as {@code 'day'}, {@code 'week'} or {@code 'month'}. It returns
     * the text.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/locale">nextHint</a>
     */
    @JsonConverter(TextValueConverter.class)
    private TextValue nextHint;

    /**
     * Sets a fixed text, see {@link #getNextHint()}.
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setNextHint(String text) {
        this.nextHint = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the value in any form, see {@link #getNextHint()}.
     *
     * @param value the text or callback, or null to remove the option
     */
    public void setNextHint(TextValue value) {
        this.nextHint = value;
    }

    /**
     * Text of the "next" button of the built-in toolbar. Each locale defines this text, see the locale page.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/locale">nextText (locale object)</a>
     */
    private String nextText;

    /**
     * Text of the "next year" button of the built-in toolbar, see {@link NativeToolbarParts#NEXT_YEAR}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/headerToolbar">headerToolbar</a>
     */
    private String nextYearText;

    /**
     * Accessible label ({@code aria-label}) of the "prev" button in the built-in toolbar. Without a value, the locale's
     * text is used.
     * <p>
     * A text may use {@code $0} as placeholder for the localized unit text, such as "week". A callback gets the
     * localized unit text and a canonical unit id such as {@code 'day'}, {@code 'week'} or {@code 'month'}. It returns
     * the text.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/locale">prevHint</a>
     */
    @JsonConverter(TextValueConverter.class)
    private TextValue prevHint;

    /**
     * Sets a fixed text, see {@link #getPrevHint()}.
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setPrevHint(String text) {
        this.prevHint = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the value in any form, see {@link #getPrevHint()}.
     *
     * @param value the text or callback, or null to remove the option
     */
    public void setPrevHint(TextValue value) {
        this.prevHint = value;
    }

    /**
     * Text of the "previous" button of the built-in toolbar. Each locale defines this text, see the locale page.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/locale">prevText (locale object)</a>
     */
    private String prevText;

    /**
     * Text of the "previous year" button of the built-in toolbar, see {@link NativeToolbarParts#PREV_YEAR}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/headerToolbar">headerToolbar</a>
     */
    private String prevYearText;

    /**
     * Accessible label ({@code aria-label}) of the "today" button in the built-in toolbar. Without a value, the
     * locale's text is used.
     * <p>
     * A text may use {@code $0} as placeholder for the localized unit text, such as "week". A callback gets the
     * localized unit text and a canonical unit id such as {@code 'day'}, {@code 'week'} or {@code 'month'}. It returns
     * the text.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/locale">todayHint</a>
     */
    @JsonConverter(TextValueConverter.class)
    private TextValue todayHint;

    /**
     * Sets a fixed text, see {@link #getTodayHint()}.
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setTodayHint(String text) {
        this.todayHint = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the value in any form, see {@link #getTodayHint()}.
     *
     * @param value the text or callback, or null to remove the option
     */
    public void setTodayHint(TextValue value) {
        this.todayHint = value;
    }

    /**
     * Text of the "today" button of the built-in toolbar. Each locale defines this text, see the locale page.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/locale">todayText (locale object)</a>
     */
    private String todayText;

    /**
     * Accessible label ({@code aria-label}) for the view buttons of the built-in toolbar that have no hint of their
     * own. Without a value, the locale's text is used.
     * <p>
     * A text may use {@code $0} as placeholder for the localized button text, such as "$0 view". A callback gets the
     * localized button text and the button name from the toolbar, such as {@code 'listWeek'}, and returns the text.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/hints">viewHint</a>
     */
    @JsonConverter(TextValueConverter.class)
    private TextValue viewHint;

    /**
     * Sets a fixed text, see {@link #getViewHint()}.
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setViewHint(String text) {
        this.viewHint = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the value in any form, see {@link #getViewHint()}.
     *
     * @param value the text or callback, or null to remove the option
     */
    public void setViewHint(TextValue value) {
        this.viewHint = value;
    }

    /**
     * Like {@link #getWeekTextShort() weekTextShort}, but only used when the {@code week} setting of a date format is
     * {@code "long"}. Without a value, the locale's text shows (English: "Week").
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/weekTextLong">weekTextLong</a>
     */
    private String weekTextLong;

    /**
     * Short heading text for week numbers, such as "W" in "W1". It shows above the week number column in day grid views
     * and next to the week number in the top-left cell of time grid views. It is also used for weeks in date
     * formatting. Without a value, the locale's text shows (English: "W").
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/weekTextShort">weekTextShort</a>
     */
    private String weekTextShort;

    /**
     * Text of the year view button of the built-in toolbar. Each locale defines this text, see the locale page.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/locale">yearText (locale object)</a>
     */
    private String yearText;

    // Toolbar and entry sources

    /**
     * Custom buttons for the built-in toolbars, and settings that override those of the library's own buttons. The
     * key is the button name, which a toolbar section lists, see {@link #getHeaderToolbar()}. The built-in names are
     * the constants of {@link NativeToolbarParts}, such as {@link NativeToolbarParts#TODAY}. The value holds the
     * settings of the button by their library names, such as {@code text}, {@code hint}, {@code click},
     * {@code iconContent}, {@code isPrimary} and {@code display}, see the library docs.
     * <p>
     * A setting value is a {@code String}, a {@code Boolean}, a number, a {@link NativeToolbarButtonDisplay} or a
     * {@link JsCallback}. Content and class name types such as {@link TextContent} are not converted here, so
     * {@code iconContent} and {@code className} take a string or a callback.
     * <p>
     * A callback setting, such as {@code click}, is a {@link JsCallback} and runs in the browser only. A click handler
     * cannot call server code by itself. On a built-in button it runs before the button's own action, which
     * {@code ev.preventDefault()} cancels.
     * <p>
     * When set, the getter returns an unmodifiable map.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/buttons">buttons</a>
     */
    @Setter(AccessLevel.NONE)
    private Map<String, Map<String, Object>> buttons;

    /**
     * Sets the buttons, see {@link #getButtons()}. The add-on keeps an unmodifiable copy of the map and of the settings
     * of each button, so a later change of the passed maps has no effect.
     *
     * @param buttons the buttons by name, or null to remove the option
     * @throws NullPointerException when a name, settings or setting value is null
     */
    public void setButtons(Map<String, Map<String, Object>> buttons) {
        this.buttons = buttons == null ? null : buttons.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> Map.copyOf(entry.getValue())));
    }

    /**
     * Content of your own for the built-in toolbars. The key is the name a toolbar section lists, see
     * {@link #getHeaderToolbar()}, and the value is the content shown there.
     * <p>
     * A callback runs in the browser only.
     * <p>
     * When set, the getter returns an unmodifiable map.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/toolbarElements">toolbarElements</a>
     */
    @Setter(AccessLevel.NONE)
    @JsonConverter(ToolbarElementsConverter.class)
    private Map<String, ContentValue> toolbarElements;

    /**
     * Sets the toolbar elements, see {@link #getToolbarElements()}. The add-on keeps an unmodifiable copy of the map,
     * so a later change of the passed map has no effect.
     *
     * @param toolbarElements the content by name, or null to remove the option
     * @throws NullPointerException when a name or content is null
     */
    public void setToolbarElements(Map<String, ContentValue> toolbarElements) {
        this.toolbarElements = toolbarElements == null ? null : Map.copyOf(toolbarElements);
    }

    /**
     * A callback that transforms the raw data of each received entry into an entry object the library can parse. It
     * runs once per received entry, but not for entries added through the client-side API.
     * <p>
     * It gets the received entry data directly, there is no {@code info} object. It returns an entry object, or
     * {@code false} to discard the entry.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to entries received
     * afterwards, for example after navigating to another view or period. Existing entries are not updated.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/eventDataTransform">eventDataTransform</a>
     */
    @JsonName("eventDataTransform")
    private JsCallback entryDataTransform;

    /**
     * A callback that runs when fetching from an entry source succeeds. It can transform the response, for example
     * unwrap a wrapper object into the entry array. To transform each single entry, use
     * {@link #getEntryDataTransform() entryDataTransform}.
     * <p>
     * It gets the raw response content and, for a JSON feed, a {@code Response} object directly, there is no
     * {@code info} object. It returns a new array of entry objects, which is used instead of the response.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/eventSourceSuccess">eventSourceSuccess</a>
     */
    @JsonName("eventSourceSuccess")
    private JsCallback entrySourceSuccess;

    /**
     * Name of the query parameter that each JSON feed entry source gets for the start of the fetched range, an ISO
     * 8601 date string. A source overrides it with {@link JsonFeedEntrySource#withStartParam(String)}. The library
     * default is {@code "start"}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/startParam">startParam</a>
     */
    private String startParam;

    /**
     * Name of the query parameter that each JSON feed entry source gets for the exclusive end of the fetched range, an
     * ISO 8601 date string. A source overrides it with {@link JsonFeedEntrySource#withEndParam(String)}. The library
     * default is {@code "end"}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/endParam">endParam</a>
     */
    private String endParam;

    /**
     * Name of the query parameter that each JSON feed entry source gets for the time zone of the start and end values
     * and of the entries it should return, such as {@code "America/Chicago"} or {@code "UTC"}. It is not sent for the
     * local time zone. A source overrides it with {@link JsonFeedEntrySource#withTimeZoneParam(String)}. The library
     * default is {@code "timeZone"}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/timeZoneParam">timeZoneParam</a>
     */
    private String timeZoneParam;

    /**
     * The Google Calendar API key for every {@link GoogleCalendarEntrySource} that has no key of its own, see
     * {@link GoogleCalendarEntrySource#withApiKey(String)}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/google-calendar">googleCalendarApiKey</a>
     */
    private String googleCalendarApiKey;

    /**
     * Whether the calendar fetches entries only when it needs to. With {@code true}, it reuses the entries it already
     * has, for example when the user switches from the month view to a week view within the same month. With
     * {@code false}, it fetches on every view switch and every date change. The library default is {@code true}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/lazyFetching">lazyFetching</a>
     */
    private Boolean lazyFetching;

    /**
     * A callback that runs when fetching entries starts or stops, often to show or hide a loading indicator. With the
     * scheduler, it also runs when resources are fetched.
     * <p>
     * It gets {@code isLoading} directly, there is no {@code info} object: {@code true} when fetching begins and
     * {@code false} when it is done.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/loading">loading</a>
     */
    private JsCallback loading;
}
