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

import org.vaadin.stefan.fullcalendar.option.ClassName;
import org.vaadin.stefan.fullcalendar.option.ClassNameValue;
import org.vaadin.stefan.fullcalendar.option.JsCallback;
import org.vaadin.stefan.fullcalendar.option.ListTextValue;
import org.vaadin.stefan.fullcalendar.option.NativeToolbarParts;
import org.vaadin.stefan.fullcalendar.option.TextContent;
import org.vaadin.stefan.fullcalendar.option.TextValue;
import org.vaadin.stefan.fullcalendar.option.Timezone;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.vaadin.stefan.fullcalendar.converters.ClassNameValueConverter;
import org.vaadin.stefan.fullcalendar.converters.DayOfWeekConverter;
import org.vaadin.stefan.fullcalendar.converters.ListTextValueConverter;
import org.vaadin.stefan.fullcalendar.converters.LocalDateConverter;
import org.vaadin.stefan.fullcalendar.converters.TextValueConverter;
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

    // Time grid and slots

    // Interaction

    // Entry display and layout

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
}
