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
import org.vaadin.stefan.fullcalendar.converters.LocaleValueConverter;
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
import org.vaadin.stefan.fullcalendar.option.EntryLimitValue;
import org.vaadin.stefan.fullcalendar.option.InitialDateValue;
import org.vaadin.stefan.fullcalendar.option.JsCallback;
import org.vaadin.stefan.fullcalendar.option.ListTextValue;
import org.vaadin.stefan.fullcalendar.option.LocaleValue;
import org.vaadin.stefan.fullcalendar.option.MoreLinkClickValue;
import org.vaadin.stefan.fullcalendar.option.NativeToolbarButtonDisplay;
import org.vaadin.stefan.fullcalendar.option.NativeToolbarParts;
import org.vaadin.stefan.fullcalendar.option.TextContent;
import org.vaadin.stefan.fullcalendar.option.TextValue;
import org.vaadin.stefan.fullcalendar.option.Timezone;
import org.vaadin.stefan.fullcalendar.option.ToolbarValue;
import org.vaadin.stefan.fullcalendar.option.ViewName;
import org.vaadin.stefan.fullcalendar.option.WeekNumberCalculation;
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
     * First day of the week.
     * <p>
     * The library default is the first day of the locale, Sunday in English, or Monday if {@code weekNumberCalculation}
     * is {@code ISO}. The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/firstDay">firstDay</a>
     */
    @JsonConverter(DayOfWeekConverter.class)
    private DayOfWeek firstDay;

    /**
     * The library's built-in toolbar below the calendar, see {@link ToolbarValue} for its sections.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>     <dd>{@link ToolbarValue#ofStartEnd(String, String, String)}: start, center and end
     *                                 section.
     *                                 <p>{@link ToolbarValue#ofLeftRight(String, String, String)}: left, center and
     *                                 right section.
     *                                 <p>{@link ToolbarValue#NONE}: no toolbar.</dd>
     *   <dt><b>Default</b></dt>   <dd>library: {@link ToolbarValue#NONE}, no toolbar.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setFooterToolbar(ToolbarValue.ofStartEnd("prev,next", "title", "today"));
     * calendar.options().setFooterToolbar(ToolbarValue.NONE);
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/footerToolbar">footerToolbar</a>
     */
    @JsonConverter(ToolbarValueConverter.class)
    private ToolbarValue footerToolbar;

    /**
     * The library's built-in toolbar above the calendar, see {@link ToolbarValue} for its sections.
     * <p>
     * The add-on switches the toolbar off, because navigation belongs to Vaadin components. The library's toolbar
     * navigates on the client, and the server learns of it only through events such as the {@link DatesRenderedEvent}.
     * <p>
     * The getter returns {@link ToolbarValue#NONE} until another value is set. Null removes the option. The getter then
     * returns null and the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>     <dd>{@link ToolbarValue#ofStartEnd(String, String, String)}: start, center and end
     *                                 section.
     *                                 <p>{@link ToolbarValue#ofLeftRight(String, String, String)}: left, center and
     *                                 right section.
     *                                 <p>{@link ToolbarValue#NONE}: no toolbar.</dd>
     *   <dt><b>Default</b></dt>   <dd>library: {@link ToolbarValue#NONE}, no toolbar.
     *                                 <p>add-on: {@link ToolbarValue#NONE}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setHeaderToolbar(ToolbarValue.ofStartEnd("prev,next today", "title", "dayGridMonth"));
     * calendar.options().setHeaderToolbar(ToolbarValue.NONE);
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/headerToolbar">headerToolbar</a>
     */
    @JsonConverter(ToolbarValueConverter.class)
    private ToolbarValue headerToolbar = ToolbarValue.NONE;

    /**
     * The date the calendar shows when it is created.
     * <p>
     * A date and time is read as UTC, like the start and end of entries, and the calendar shows it in its time zone.
     * The time matters for views shorter than a day, such as a timeline of six hours, whose range starts at the given
     * hour.
     * <p>
     * It has no effect on a calendar the browser already shows. Use {@link FullCalendar#gotoDate(LocalDate)} instead.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>     <dd>{@link InitialDateValue#ofDay(LocalDate)}: a date.
     *                                 <p>{@link InitialDateValue#ofDateTime(LocalDateTime)}: a date and time, read as
     *                                 UTC.</dd>
     *   <dt><b>Shortcuts</b></dt> <dd>{@link #setInitialDate(LocalDate)}: a date, as {@code ofDay}.
     *                                 <p>{@link #setInitialDate(LocalDateTime)}: a date and time, as
     *                                 {@code ofDateTime}.</dd>
     *   <dt><b>Default</b></dt>   <dd>library: the current date.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setInitialDate(LocalDate.of(2026, 1, 15));
     * calendar.options().setInitialDate(LocalDateTime.of(2026, 1, 15, 8, 0));
     * }</pre>
     *
     * @see FullCalendar#gotoDate(LocalDate)
     * @see <a href="https://fullcalendar.io/docs/initialDate">initialDate</a>
     */
    @JsonConverter(InitialDateValueConverter.class)
    private InitialDateValue initialDate;

    /**
     * Sets a date as the initial date, see {@link #getInitialDate()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param date the date, or null to remove the option
     */
    public void setInitialDate(LocalDate date) {
        this.initialDate = date == null ? null : InitialDateValue.ofDay(date);
    }

    /**
     * Sets a date and time as the initial date, see {@link #getInitialDate()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param dateTime the date and time, read as UTC like the start and end of entries, or null to remove the option
     */
    public void setInitialDate(LocalDateTime dateTime) {
        this.initialDate = dateTime == null ? null : InitialDateValue.ofDateTime(dateTime);
    }

    /**
     * Sets the initial date in any form, see {@link #getInitialDate()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
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
     * The library also takes a list of codes and uses the first one it brings. The field leaves it out, because the
     * calendar registers every locale of the library and {@link CalendarLocale} lists them, so the fallback can be
     * chosen on the server.
     * <p>
     * This option is independent of the Vaadin locale of the UI. To follow the UI locale, set this option in a view
     * that implements {@link com.vaadin.flow.i18n.LocaleChangeObserver}.
     * <p>
     * The calendar always has a locale, so it cannot be removed. Passing null throws a {@link NullPointerException}.
     * The getter returns the {@link LocaleValue} of {@link CalendarLocale#getDefaultLocale()} until another locale is
     * set.
     * <dl>
     *   <dt><b>Forms</b></dt>     <dd>{@link LocaleValue#of(Locale)}: a locale the library brings.
     *                                 <p>{@link LocaleValue#ofCustom(Locale, Map)}: a custom locale with its own
     *                                 options.</dd>
     *   <dt><b>Shortcuts</b></dt> <dd>{@link #setLocale(Locale)}: a locale the library brings, as
     *                                 {@code LocaleValue.of(Locale)}.</dd>
     *   <dt><b>Default</b></dt>   <dd>library: the first locale it is given, or English without one.
     *                                 <p>add-on: {@link CalendarLocale#getDefaultLocale()}.<br>
     *                                 The add-on gives the library every locale and always sets one, so the library
     *                                 default does not apply.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setLocale(Locale.GERMAN);
     * calendar.options().setLocale(LocaleValue.ofCustom(Locale.GERMAN, Map.of("todayText", "Heute")));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/locale">locale</a>
     */
    @JsonConverter(LocaleValueConverter.class)
    @Setter(AccessLevel.NONE)
    private LocaleValue locale = LocaleValue.of(CalendarLocale.getDefaultLocale());

    /**
     * Sets a locale the library brings, see {@link #getLocale()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param locale the locale
     * @throws NullPointerException when null is passed
     */
    public void setLocale(Locale locale) {
        this.locale = LocaleValue.of(locale);
    }

    /**
     * Sets the locale in any form, see {@link #getLocale()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the locale
     * @throws NullPointerException when null is passed
     */
    public void setLocale(LocaleValue value) {
        this.locale = Objects.requireNonNull(value);
    }

    /**
     * The time zone the calendar shows the entries in. The server converts entry times with it. The initial value is
     * {@link Timezone#UTC}. With {@link Timezone#LOCAL}, the calendar follows the browser's zone. This then returns the
     * zone the browser reported, or {@code LOCAL}, which converts with UTC, until the browser has reported.
     * <p>
     * The public setter is {@link FullCalendarOptions#setTimeZone(Timezone)}. Besides the field, it sets whether the
     * calendar follows the browser.
     *
     * @see <a href="https://fullcalendar.io/docs/timeZone">timeZone</a>
     */
    @Setter(AccessLevel.NONE)
    private Timezone timeZone = Timezone.UTC;

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
     * {@link FullCalendar#setAutoProvideEntryIdOnClient(boolean)}. The callback may have any function form, such as
     * {@code info => info.el.title = 'x'}.
     * <p>
     * With a number as the limit of entries per day ({@link ViewOptions#getDayMaxEntries() dayMaxEntries}), it runs
     * only for the entries the library renders, not for the ones behind the "+N more" link, until the popover renders
     * them. With {@link EntryLimitValue#CELL_HEIGHT} in day grid views, the library may render hidden entries invisibly
     * to measure them, so it can run for them too.
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/event-render-hooks">eventDidMount</a>
     */
    @JsonName("eventDidMount")
    private JsCallback entryDidMount;

    // Render hooks: view, toolbar, buttons, single month

    /**
     * CSS classes for the root element of the view.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setViewClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: stable classes {@code fc-view}, one per view type, such as
     *                                  {@code fc-dayGridMonth-view}, and one per view family, such as
     *                                  {@code fc-daygrid}.<br>
     *                                  Classes set here are added to them.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}, called whenever the view changes.<br>
     *                                  {@code info} has {@code view}, {@code borderlessX}, {@code borderlessTop},
     *                                  {@code borderlessBottom}, {@code options} (with {@code headerToolbar} and
     *                                  {@code footerToolbar}) and {@code isHeightAuto}.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setViewClass("my-view");
     * calendar.options().setViewClass(JsCallback.of("info => info.view.type.startsWith('list') ? 'my-list' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/view-render-hooks">viewClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue viewClass;

    /**
     * Sets fixed CSS classes, see {@link #getViewClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setViewClass(String classNames) {
        this.viewClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getViewClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
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
     * It gets an {@code info} object with {@code view}, {@code el} (the root element) and more, see the library docs.
     * <p>
     * When this option is changed for an attached calendar, the callback that is set at the time an element is removed
     * runs for it. This includes elements that were rendered before the change.
     * <p>
     * The library default is no callback.
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
     * The library default comes from the locale. Most locales are left-to-right. Some, such as Arabic and Hebrew, are
     * right-to-left. The library docs name {@code ltr} as the default. The getter returns null while the option is not
     * set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/direction">direction</a>
     */
    private Direction direction;

    /**
     * The view the calendar renders when it loads, a built-in or a custom one. The library reads it when the calendar
     * is created. Use {@link FullCalendar#changeView(CalendarView)} to change the view of a calendar that is already
     * shown.
     * <p>
     * The library default is {@code dayGridMonth}. It is the initial view of the day grid plugin, which the add-on
     * loads first. The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/initialView">initialView</a>
     */
    @JsonConverter(ViewNameValueConverter.class)
    private ViewName initialView;

    /**
     * Sets the view the calendar renders when it loads, by the name of a view, see {@link #getInitialView()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param name the view name, such as {@code "timeGridDay"}, or null to remove the option
     */
    public void setInitialView(String name) {
        this.initialView = name == null ? null : ViewName.of(name);
    }

    /**
     * Sets the view the calendar renders when it loads, see {@link #getInitialView()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param view the view, a built-in or a custom one, or null to remove the option
     */
    public void setInitialView(CalendarView view) {
        this.initialView = view == null ? null : ViewName.of(view);
    }

    /**
     * Sets the view the calendar renders when it loads, see {@link #getInitialView()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the view name, or null to remove the option
     */
    public void setInitialView(ViewName value) {
        this.initialView = value;
    }

    /**
     * How the week numbers are calculated. {@link WeekNumberCalculation#ISO} follows ISO 8601 and also makes Monday the
     * default first day of the week.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link WeekNumberCalculation#LOCAL}: the calculation of the locale.
     *                                  <p>{@link WeekNumberCalculation#ISO}: ISO 8601 week numbers.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: {@link WeekNumberCalculation#LOCAL}.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(date)}, the date as a JavaScript {@code Date}.<br>
     *                                  It returns the week number as an integer.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setWeekNumberCalculation(WeekNumberCalculation.ISO);
     * calendar.options().setWeekNumberCalculation(JsCallback.of("date => Math.ceil(date.getDate() / 7)"));
     * }</pre>
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
     * The library default is no callback. Without a callback, no further check is made.
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
     * A dragged or resized entry must lie fully inside the limit. For a group, that means inside at least one of its
     * entries.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>     <dd>{@link ConstraintValue#ofGroupId(String)}: the entries of a group.
     *                                 <p>{@link ConstraintValue#BUSINESS_HOURS}: the business hours of the calendar.
     *                                 <p>{@link ConstraintValue#ofWindows(BusinessHours...)}: recurring business hours.
     *                                 <p>{@link ConstraintValue#ofWindows(ConstraintValue.Window...)}: windows of time
     *                                 of any kind, built with {@link ConstraintValue.Window#ofRRule(RRule)},
     *                                 {@link ConstraintValue.Window#ofRRule(RRule, java.time.Duration)},
     *                                 {@link ConstraintValue.Window#span(LocalDate, LocalDate)} or
     *                                 {@link ConstraintValue.Window#span(LocalDateTime, LocalDateTime)}.
     *                                 <p>{@link ConstraintValue#ofResources(String...)}: resources, scheduler only.
     *                                 <p>{@link ConstraintValue.Windows#withResources(String...)}: windows of time
     *                                 limited to resources, scheduler only.</dd>
     *   <dt><b>Shortcuts</b></dt> <dd>{@link #setEntryConstraint(String)}: a group id, as {@code ofGroupId}.<br>
     *                                 The id {@code "businessHours"} throws an {@link IllegalArgumentException}.<br>
     *                                 Use {@link ConstraintValue#BUSINESS_HOURS} instead.
     *                                 <p>{@link #setEntryConstraint(BusinessHours...)}: recurring windows, as
     *                                 {@code ofWindows}.<br>
     *                                 No window throws an {@link IllegalArgumentException}.
     *                                 <p>{@link #setEntryConstraint(ConstraintValue.Window...)}: windows of time of any
     *                                 kind, as {@code ofWindows}.<br>
     *                                 No window throws an {@link IllegalArgumentException}.</dd>
     *   <dt><b>Default</b></dt>   <dd>library: none.<br>
     *                                 Dragging and resizing are not limited.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setEntryConstraint("groupA");
     * calendar.options().setEntryConstraint(BusinessHours.businessWeek().start(9).end(17));
     * calendar.options().setEntryConstraint(ConstraintValue.BUSINESS_HOURS);
     * calendar.options().setEntryConstraint(ConstraintValue.ofResources("a", "b")); // scheduler only
     * calendar.options().setEntryConstraint(
     *         ConstraintValue.ofWindows(BusinessHours.businessWeek()).withResources("a")); // scheduler only
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/eventConstraint">eventConstraint</a>
     */
    @JsonName("eventConstraint")
    @JsonConverter(ConstraintValueConverter.class)
    private ConstraintValue entryConstraint;

    /**
     * Limits entry dragging and resizing to the entries of a group, see {@link #getEntryConstraint()}. A dragged
     * entry must be fully contained by at least one entry of that group.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param groupId the group id, or null to remove the option
     */
    public void setEntryConstraint(String groupId) {
        this.entryConstraint = groupId == null ? null : ConstraintValue.ofGroupId(groupId);
    }

    /**
     * Limits entry dragging and resizing to recurring windows of time, see {@link #getEntryConstraint()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param windows the windows, at least one, or null to remove the option
     */
    public void setEntryConstraint(BusinessHours... windows) {
        this.entryConstraint = windows == null ? null : ConstraintValue.ofWindows(windows);
    }

    /**
     * Limits entry dragging and resizing to windows of time, business hours, RRule windows and fixed spans mixed, see
     * {@link #getEntryConstraint()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param windows the windows, at least one, or null to remove the option
     */
    public void setEntryConstraint(ConstraintValue.Window... windows) {
        this.entryConstraint = windows == null ? null : ConstraintValue.ofWindows(windows);
    }

    /**
     * Limits entry dragging and resizing in any form, see {@link #getEntryConstraint()}. This is the way to pass
     * {@link ConstraintValue#BUSINESS_HOURS}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
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
     * Without a value, the option follows {@code editable}. The library docs name {@code true} as the default, but the
     * used library version has no default for this option.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/eventDurationEditable">eventDurationEditable</a>
     */
    @JsonName("eventDurationEditable")
    private Boolean entryDurationEditable;

    /**
     * Whether entries can be dragged to change their start. It decides dragging on its own and takes precedence over
     * {@code editable}. A single entry can override it.
     * <p>
     * The library has no default for this option. Without a value, {@code editable} decides, whose library default is
     * {@code false}. The library docs name {@code true} as the default of this option, but the used library version
     * sets none. The getter returns null while the option is not set; then the library default applies.
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
     * What happens when the user clicks a "+N more" link, which the entry limits create.
     * <p>
     * The calendar has its own callback for this option, which sends the {@link MoreLinkClickedEvent}, and applies this
     * value after it. The server gets the event with every value, also with a raw option.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link MoreLinkClickValue.Action#POPUP}: a popover with all entries of the day.
     *                                  <p>{@link MoreLinkClickValue.Action#DAY}: a day view, as the views of the header
     *                                  toolbar determine.
     *                                  <p>{@link MoreLinkClickValue.Action#WEEK}: a week view, as the views of the
     *                                  header toolbar determine.
     *                                  <p>{@link MoreLinkClickValue.Action#NOTHING}: no popover, the view type stays.
     *                                  <p>{@link ViewName}: the view to go to.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setMoreLinkClick(String)}: a client-side view name, as {@link ViewName}.
     *                                  <p>{@link #setMoreLinkClick(CalendarView)}: a view, as {@link ViewName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: {@link MoreLinkClickValue.Action#POPUP}.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code date}, {@code allDay}, {@code allSegs},
     *                                  {@code hiddenSegs}, {@code jsEvent} and {@code view}.<br>
     *                                  It returns an action or a view name as a string, which then applies.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setMoreLinkClick(CalendarViewImpl.TIME_GRID_DAY);
     * calendar.options().setMoreLinkClick(MoreLinkClickValue.Action.WEEK);
     * calendar.options().setMoreLinkClick(JsCallback.of("info => info.date.getDay() === 0 ? 'week' : 'popover'"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/moreLinkClick">moreLinkClick</a>
     */
    @Setter(AccessLevel.NONE)
    @JsonConverter(MoreLinkClickValueConverter.class)
    private MoreLinkClickValue moreLinkClick;

    /**
     * Sets the view to go to, see {@link #getMoreLinkClick()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param viewName the client-side view name, such as {@code "timeGridDay"}, or null to remove the option
     */
    public void setMoreLinkClick(String viewName) {
        this.moreLinkClick = viewName == null ? null : ViewName.of(viewName);
    }

    /**
     * Sets the view to go to, see {@link #getMoreLinkClick()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param view the view, or null to remove the option
     */
    public void setMoreLinkClick(CalendarView view) {
        this.moreLinkClick = view == null ? null : ViewName.of(view);
    }

    /**
     * Sets the value in any form, see {@link #getMoreLinkClick()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
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
     * The library default is no callback. Without a callback, the selection is not checked.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/selectAllow">selectAllow</a>
     */
    private JsCallback selectAllow;

    /**
     * Limits where the user can select to certain windows of time. It applies when selecting is enabled.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>     <dd>{@link ConstraintValue#ofGroupId(String)}: the entries of a group.
     *                                 <p>{@link ConstraintValue#BUSINESS_HOURS}: the business hours of the calendar.
     *                                 <p>{@link ConstraintValue#ofWindows(BusinessHours...)}: recurring business hours.
     *                                 <p>{@link ConstraintValue#ofWindows(ConstraintValue.Window...)}: windows of time
     *                                 of any kind, built with {@link ConstraintValue.Window#ofRRule(RRule)},
     *                                 {@link ConstraintValue.Window#ofRRule(RRule, java.time.Duration)},
     *                                 {@link ConstraintValue.Window#span(LocalDate, LocalDate)} or
     *                                 {@link ConstraintValue.Window#span(LocalDateTime, LocalDateTime)}.
     *                                 <p>{@link ConstraintValue#ofResources(String...)}: resources, scheduler only.
     *                                 <p>{@link ConstraintValue.Windows#withResources(String...)}: windows of time
     *                                 limited to resources, scheduler only.</dd>
     *   <dt><b>Shortcuts</b></dt> <dd>{@link #setSelectConstraint(String)}: a group id, as {@code ofGroupId}.<br>
     *                                 The id {@code "businessHours"} throws an {@link IllegalArgumentException}.<br>
     *                                 Use {@link ConstraintValue#BUSINESS_HOURS}.
     *                                 <p>{@link #setSelectConstraint(BusinessHours...)}: recurring windows, as
     *                                 {@code ofWindows}.<br>
     *                                 No window throws an {@link IllegalArgumentException}.
     *                                 <p>{@link #setSelectConstraint(ConstraintValue.Window...)}: windows of time of
     *                                 any kind, as {@code ofWindows}.<br>
     *                                 No window throws an {@link IllegalArgumentException}.</dd>
     *   <dt><b>Default</b></dt>   <dd>library: none, the selection is not limited to windows of time.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setSelectConstraint("groupA");
     * calendar.options().setSelectConstraint(BusinessHours.businessWeek().start(9).end(17));
     * calendar.options().setSelectConstraint(ConstraintValue.BUSINESS_HOURS);
     * calendar.options().setSelectConstraint(ConstraintValue.ofResources("a", "b")); // scheduler only
     * calendar.options().setSelectConstraint(
     *         ConstraintValue.ofWindows(BusinessHours.businessWeek()).withResources("a")); // scheduler only
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/selectConstraint">selectConstraint</a>
     */
    @JsonConverter(ConstraintValueConverter.class)
    private ConstraintValue selectConstraint;

    /**
     * Limits selections to the entries of a group, see {@link #getSelectConstraint()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param groupId the group id, or null to remove the option
     */
    public void setSelectConstraint(String groupId) {
        this.selectConstraint = groupId == null ? null : ConstraintValue.ofGroupId(groupId);
    }

    /**
     * Limits selections to recurring windows of time, see {@link #getSelectConstraint()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param windows the windows, at least one, or null to remove the option
     */
    public void setSelectConstraint(BusinessHours... windows) {
        this.selectConstraint = windows == null ? null : ConstraintValue.ofWindows(windows);
    }

    /**
     * Limits selections to windows of time, business hours, RRule windows and fixed spans mixed, see
     * {@link #getSelectConstraint()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param windows the windows, at least one, or null to remove the option
     */
    public void setSelectConstraint(ConstraintValue.Window... windows) {
        this.selectConstraint = windows == null ? null : ConstraintValue.ofWindows(windows);
    }

    /**
     * Limits selections in any form, see {@link #getSelectConstraint()}. This is the way to pass
     * {@link ConstraintValue#BUSINESS_HOURS}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the constraint, or null to remove the option
     */
    public void setSelectConstraint(ConstraintValue value) {
        this.selectConstraint = value;
    }

    // Entry display and layout

    /**
     * CSS classes for the root element of the calendar.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setClassName(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: the stable class {@code fc}.<br>
     *                                  Classes set here are added to it.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code borderlessX}, {@code borderlessTop} and
     *                                  {@code borderlessBottom}.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setClassName("my-calendar");
     * calendar.options().setClassName(JsCallback.of("info => info.borderlessX ? 'my-borderless' : 'my-calendar'"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/className">className</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue className;

    /**
     * Sets fixed CSS classes for the root element, see {@link #getClassName()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setClassName(String classNames) {
        this.className = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the root element in any form, see {@link #getClassName()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setClassName(ClassNameValue value) {
        this.className = value;
    }

    /**
     * The milliseconds the calendar waits after an operation that may rerender it, such as changing the view or adding
     * an entry, before it rerenders everything at once. This can reduce the number of rerenders.
     * <p>
     * The delay does not apply while the user drags or resizes an entry. The calendar rerenders at once then.
     * <p>
     * The library default is no delay, so the calendar rerenders at once.
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
     * The library default is {@link DisplayMode#AUTO}. The getter returns null while the option is not set; then the
     * library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/eventDisplay">eventDisplay</a>
     */
    @JsonName("eventDisplay")
    private DisplayMode entryDisplay;

    // Texts, hints and formats

    /**
     * Text of the day view buttons of the built-in toolbar. The hints of the previous, next and today buttons use it as
     * the name of the unit. The list day view uses {@code listText} instead.
     * <p>
     * Each locale defines this text, see the locale page. The library docs describe this option only partly. They name
     * it in an example of the locale page.
     * <p>
     * The library default is the text of the locale. The getter returns null while the option is not set; then the
     * library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/locale">dayText (locale object)</a>
     */
    private String dayText;

    /**
     * Text of the list view button of the built-in toolbar.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>     <dd>{@link TextContent}: shown as text.
     *                                 <p>{@link ListTextValue#NONE}: buttons named by duration, such as "Week" or
     *                                 "Month", instead of a "List" button.</dd>
     *   <dt><b>Shortcuts</b></dt> <dd>{@link #setListText(String)}: a text, as {@link TextContent}.</dd>
     *   <dt><b>Default</b></dt>   <dd>library: the text of the locale, "List" in English.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setListText("Agenda");
     * calendar.options().setListText(ListTextValue.NONE);
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/list-view">listText (List view: Toolbar Buttons)</a>
     */
    @JsonConverter(ListTextValueConverter.class)
    private ListTextValue listText;

    /**
     * Sets a fixed text, see {@link #getListText()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setListText(String text) {
        this.listText = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the value in any form, see {@link #getListText()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the text or {@link ListTextValue#NONE}, or null to remove the option
     */
    public void setListText(ListTextValue value) {
        this.listText = value;
    }

    /**
     * Text of the month view button of the built-in toolbar. Each locale defines this text, see the locale page. The
     * library docs mention this option only as part of the locale object.
     * <p>
     * The library default is the text of the locale, "Month" in English. The getter returns null while the option is
     * not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/locale">monthText (locale object)</a>
     */
    private String monthText;

    /**
     * Accessible label ({@code aria-label}) of the "next" button in the built-in toolbar. It also labels the "next
     * year" button. For that button, the placeholder stands for the year text and the unit name is {@code 'year'}.
     * <p>
     * A text may use {@code $0} as placeholder for the localized unit text, such as "week", and {@code $1} for the
     * unit name in English, such as {@code week}.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link TextContent}: shown as text.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setNextHint(String)}: a text, as {@link TextContent}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: the hint of the locale, "Next $0" in English.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(unitText, unitName)}.<br>
     *                                  {@code unitText} is the localized text of the unit, {@code unitName} the unit
     *                                  name in English, such as {@code 'day'}, {@code 'week'} or {@code 'month'}.<br>
     *                                  It returns the text.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setNextHint("Next $0");
     * calendar.options().setNextHint(JsCallback.of("(unit, name) => 'Go forward one ' + unit"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/locale">nextHint</a>
     */
    @JsonConverter(TextValueConverter.class)
    private TextValue nextHint;

    /**
     * Sets a fixed text, see {@link #getNextHint()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setNextHint(String text) {
        this.nextHint = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the value in any form, see {@link #getNextHint()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the text or callback, or null to remove the option
     */
    public void setNextHint(TextValue value) {
        this.nextHint = value;
    }

    /**
     * Text of the "next" button of the built-in toolbar. Each locale defines this text, see the locale page.
     * The library docs mention this option only as part of the locale object.
     * <p>
     * The library default is the text of the locale, "Next" in English.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/locale">nextText (locale object)</a>
     */
    private String nextText;

    /**
     * Text of the "next year" button of the built-in toolbar, see {@link NativeToolbarParts#NEXT_YEAR}.
     * <p>
     * The library docs do not describe this option. The library default is the text of the locale, "Next year" in
     * English.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/headerToolbar">headerToolbar</a>
     */
    private String nextYearText;

    /**
     * Accessible label ({@code aria-label}) of the "prev" button in the built-in toolbar. It also labels the "previous
     * year" button. For that button, the placeholder stands for the year text and the unit name is {@code 'year'}.
     * <p>
     * A text may use {@code $0} as placeholder for the localized unit text, such as "week", and {@code $1} for the
     * unit name in English, such as {@code week}.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link TextContent}: shown as text.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setPrevHint(String)}: a text, as {@link TextContent}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: the hint of the locale, "Previous $0" in English.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(unitText, unitName)}.<br>
     *                                  {@code unitText} is the localized text of the unit, {@code unitName} the unit
     *                                  name in English, such as {@code 'day'}, {@code 'week'} or {@code 'month'}.<br>
     *                                  It returns the text.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setPrevHint("Previous $0");
     * calendar.options().setPrevHint(JsCallback.of("(unit, name) => 'Go back one ' + unit"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/locale">prevHint</a>
     */
    @JsonConverter(TextValueConverter.class)
    private TextValue prevHint;

    /**
     * Sets a fixed text, see {@link #getPrevHint()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setPrevHint(String text) {
        this.prevHint = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the value in any form, see {@link #getPrevHint()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the text or callback, or null to remove the option
     */
    public void setPrevHint(TextValue value) {
        this.prevHint = value;
    }

    /**
     * Text of the "previous" button of the built-in toolbar. Each locale defines this text, see the locale page.
     * The library docs mention this option only as part of the locale object.
     * <p>
     * The library default is the text of the locale, "Prev" in English.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/locale">prevText (locale object)</a>
     */
    private String prevText;

    /**
     * Text of the "previous year" button of the built-in toolbar, see {@link NativeToolbarParts#PREV_YEAR}.
     * <p>
     * The library docs do not describe this option. The library default is the text of the locale, "Prev year" in
     * English.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/headerToolbar">headerToolbar</a>
     */
    private String prevYearText;

    /**
     * Accessible label ({@code aria-label}) of the "today" button in the built-in toolbar.
     * <p>
     * A text may use {@code $0} as placeholder for the localized unit text, such as "week", and {@code $1} for the
     * unit name in English, such as {@code week}.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link TextContent}: shown as text.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setTodayHint(String)}: a text, as {@link TextContent}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: the hint of the locale.<br>
     *                                  In English, "Today" for the unit day and "This $0" otherwise, such as "This
     *                                  week".</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(unitText, unitName)}.<br>
     *                                  {@code unitText} is the localized text of the unit, {@code unitName} the unit
     *                                  name in English, such as {@code 'day'}, {@code 'week'} or {@code 'month'}.<br>
     *                                  It returns the text.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setTodayHint("Go to $0");
     * calendar.options().setTodayHint(JsCallback.of("(unit, name) => name === 'day' ? 'Today' : 'This ' + unit"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/locale">todayHint</a>
     */
    @JsonConverter(TextValueConverter.class)
    private TextValue todayHint;

    /**
     * Sets a fixed text, see {@link #getTodayHint()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setTodayHint(String text) {
        this.todayHint = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the value in any form, see {@link #getTodayHint()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the text or callback, or null to remove the option
     */
    public void setTodayHint(TextValue value) {
        this.todayHint = value;
    }

    /**
     * Text of the "today" button of the built-in toolbar. Each locale defines this text, see the locale page.
     * <p>
     * The library default is the text of the locale (English: "Today").
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/locale">todayText (locale object)</a>
     */
    private String todayText;

    /**
     * Accessible label ({@code aria-label}) for the view buttons of the built-in toolbar that have no hint of their
     * own.
     * <p>
     * A text may use {@code $0} as placeholder for the localized button text, such as "$0 view".
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link TextContent}: shown as text.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setViewHint(String)}: a text, as {@link TextContent}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: the hint of the locale, "$0 view" in English.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(buttonText, viewName)}.<br>
     *                                  {@code buttonText} is the localized button text, {@code viewName} the button
     *                                  name from the toolbar, such as {@code 'listWeek'}.<br>
     *                                  It returns the text.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setViewHint("Show $0");
     * calendar.options().setViewHint(JsCallback.of("(text, name) => name === 'listWeek' ? 'Agenda view' : text"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/hints">viewHint</a>
     */
    @JsonConverter(TextValueConverter.class)
    private TextValue viewHint;

    /**
     * Sets a fixed text, see {@link #getViewHint()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setViewHint(String text) {
        this.viewHint = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the value in any form, see {@link #getViewHint()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the text or callback, or null to remove the option
     */
    public void setViewHint(TextValue value) {
        this.viewHint = value;
    }

    /**
     * Like {@link #getWeekTextShort() weekTextShort}, but only used when the {@code week} setting of a date format is
     * {@code "long"}.
     * <p>
     * The library default is the text of the locale (English: "Week").
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/weekTextLong">weekTextLong</a>
     */
    private String weekTextLong;

    /**
     * Short heading text for week numbers, such as "W" in "W1". It shows above the week number column in day grid views
     * and next to the week number in the top-left cell of time grid views. It is also used for weeks in date
     * formatting.
     * <p>
     * The library default is the text of the locale (English: "W").
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/weekTextShort">weekTextShort</a>
     */
    private String weekTextShort;

    /**
     * Text of the year view button of the built-in toolbar. Each locale defines this text, see the locale page.
     * <p>
     * The library docs mention this option only in an example on the locale page. The library default is the text of
     * the locale (English: "Year").
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
     * A setting value is a {@code String}, a {@code Boolean}, a number, a {@link NativeToolbarButtonDisplay}, a
     * {@link JsCallback} or a {@code Map}. Content and class name types such as {@link TextContent} are not converted
     * here, so {@code iconContent} and {@code className} take a string or a callback. A string {@code iconContent} is
     * shown as text, not as markup. For SVG markup pass a {@code Map} with the key {@code html}, for example
     * {@code Map.of("html", "<svg>...</svg>")}, which the library sets as unescaped inner HTML, or a callback. Never
     * put unchecked user input into it.
     * <p>
     * A built-in button that normally shows an icon needs {@code display} set to {@code text}, {@code text-icon} or
     * {@code icon-text} before its {@code text} shows. The library docs say {@code hint} defaults to {@code text}.
     * Besides the built-in names, the names {@code year}, {@code month}, {@code week} and {@code day} can also be
     * overridden.
     * <p>
     * A callback setting, such as {@code click}, is a {@link JsCallback} and runs in the browser only. A click handler
     * cannot call server code by itself. A {@code click} callback gets only the mouse event. The library docs also name
     * the button element as a second argument, but the used library version does not pass it. On a built-in button it
     * runs before the button's own action, which {@code ev.preventDefault()} cancels.
     * <p>
     * When set, the getter returns an unmodifiable map.
     * <p>
     * The library default is no custom buttons. The theme sets the icons of the {@code prev}, {@code next},
     * {@code prevYear} and {@code nextYear} buttons.
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
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
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
     * The library default is no elements.
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
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
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
     * {@code false} to discard the entry. A callback that returns nothing keeps the entry data as received.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to entries received
     * afterwards, for example after navigating to another view or period. Existing entries are not updated.
     * <p>
     * The library default is no callback.
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
     * It runs for every entry source, also for the entries the calendar fetches from its own entry provider. Then it
     * gets the array of entries and no {@code Response}. A callback that returns nothing keeps the received data.
     * <p>
     * The library default is no callback.
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
     * The library has no default for this option. A Google Calendar source that has no key of its own fails to load
     * without it, with the library error "Specify a googleCalendarApiKey". The getter returns null while the option is
     * not set; then the library default applies.
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
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/loading">loading</a>
     */
    private JsCallback loading;
}
