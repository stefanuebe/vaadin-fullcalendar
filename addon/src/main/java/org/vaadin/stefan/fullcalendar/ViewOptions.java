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

import org.vaadin.stefan.fullcalendar.option.AutoBoolean;
import org.vaadin.stefan.fullcalendar.option.ClassName;
import org.vaadin.stefan.fullcalendar.option.ClassNameValue;
import org.vaadin.stefan.fullcalendar.option.ContentValue;
import org.vaadin.stefan.fullcalendar.option.CssSelector;
import org.vaadin.stefan.fullcalendar.option.CssSelectorValue;
import org.vaadin.stefan.fullcalendar.option.DurationValue;
import org.vaadin.stefan.fullcalendar.option.HeaderAlignValue;
import org.vaadin.stefan.fullcalendar.option.JsCallback;
import org.vaadin.stefan.fullcalendar.option.Overlap;
import org.vaadin.stefan.fullcalendar.option.OverlapValue;
import org.vaadin.stefan.fullcalendar.option.StickyValue;
import org.vaadin.stefan.fullcalendar.option.TextContent;
import org.vaadin.stefan.fullcalendar.converters.DateRangeValueConverter;
import org.vaadin.stefan.fullcalendar.converters.DayOfWeekArrayConverter;
import org.vaadin.stefan.fullcalendar.converters.DurationValueConverter;
import org.vaadin.stefan.fullcalendar.converters.NowValueConverter;
import org.vaadin.stefan.fullcalendar.option.AutoBoolean;
import org.vaadin.stefan.fullcalendar.option.BusinessHours;
import org.vaadin.stefan.fullcalendar.option.BusinessHoursValue;
import org.vaadin.stefan.fullcalendar.option.DateRange;
import org.vaadin.stefan.fullcalendar.option.DateRangeValue;
import org.vaadin.stefan.fullcalendar.option.DurationValue;
import org.vaadin.stefan.fullcalendar.option.EntryLimitValue;
import org.vaadin.stefan.fullcalendar.option.EntryOrder;
import org.vaadin.stefan.fullcalendar.option.EntryOrderValue;
import org.vaadin.stefan.fullcalendar.option.HeightValue;
import org.vaadin.stefan.fullcalendar.option.NowValue;
import lombok.Getter;
import lombok.Setter;
import org.vaadin.stefan.fullcalendar.converters.AutoBooleanConverter;
import org.vaadin.stefan.fullcalendar.converters.ClassNameValueConverter;
import org.vaadin.stefan.fullcalendar.converters.ContentValueConverter;
import org.vaadin.stefan.fullcalendar.converters.CssSelectorValueConverter;
import org.vaadin.stefan.fullcalendar.converters.DurationValueConverter;
import org.vaadin.stefan.fullcalendar.converters.HeaderAlignValueConverter;
import org.vaadin.stefan.fullcalendar.converters.AutoBooleanConverter;
import org.vaadin.stefan.fullcalendar.converters.BusinessHoursValueConverter;
import org.vaadin.stefan.fullcalendar.converters.EntryLimitValueConverter;
import org.vaadin.stefan.fullcalendar.converters.EntryOrderValueConverter;
import org.vaadin.stefan.fullcalendar.converters.HeightValueConverter;
import org.vaadin.stefan.fullcalendar.converters.OverlapValueConverter;
import org.vaadin.stefan.fullcalendar.converters.StickyValueConverter;
import org.vaadin.stefan.fullcalendar.json.JsonConverter;
import org.vaadin.stefan.fullcalendar.json.JsonName;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * The options that can apply per view. Null means not set. This class is not public API. Users reach the fields through
 * {@link FullCalendar#options()}.
 */
@Getter
@Setter
class ViewOptions implements Serializable {

    // FC-UPDATE: check the options of the new library version against these fields: new, renamed, removed or retyped
    //  options, and options that became view-specific or global. A renamed option changes only @JsonName. The test
    //  resource option-names.txt lists the supported names as global or per view.

    /**
     * Master switch for dragging and resizing entries. Enables or disables both at the same time. Background entries
     * can not be dragged or resized. Individual entries can override it with {@link Entry#setEditable(boolean)}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/editable">editable</a>
     */
    private Boolean editable;

    /**
     * Whether Saturday and Sunday columns are included in the calendar views.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/weekends">weekends</a>
     */
    private Boolean weekends;

    // Render hooks: day cells, day headers, day lanes, all-day

    /**
     * CSS classes for day cells in day grid views (including the all-day section of time grid views) and the body of
     * the "+N more" popover. Time grid day columns use {@code dayLaneClass} instead.
     * <p>
     * A callback gets an {@code info} object with {@code date}, {@code text}, {@code isPast}, {@code isFuture},
     * {@code isToday}, {@code isOther} and more, see the library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/day-cell-render-hooks">dayCellClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue dayCellClass;

    /**
     * Sets fixed CSS classes for day cells, see {@link #getDayCellClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setDayCellClass(String classNames) {
        this.dayCellClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for day cells in any form, see {@link #getDayCellClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setDayCellClass(ClassNameValue value) {
        this.dayCellClass = value;
    }

    /**
     * Content for the cell in the header area of time grid views that labels the all-day section. Without a value, the
     * locale's text shows ("all-day" in English).
     * <p>
     * A callback gets an {@code info} object with {@code text}, {@code isNarrow} and {@code view}. It returns a text,
     * or {@code {html: '...'}} or {@code {domNodes: [...]}}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/all-day-header-render-hooks">allDayHeaderContent</a>
     */
    @JsonConverter(ContentValueConverter.class)
    private ContentValue allDayHeaderContent;

    /**
     * Sets a text as the content of the all-day header cell, see {@link #getAllDayHeaderContent()}.
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setAllDayHeaderContent(String text) {
        this.allDayHeaderContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content of the all-day header cell in any form, see {@link #getAllDayHeaderContent()}.
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setAllDayHeaderContent(ContentValue value) {
        this.allDayHeaderContent = value;
    }

    /**
     * Horizontal text alignment of the day header cells in day grid and time grid views, and in the header of the entry
     * popover. In a left-to-right locale, start means left and end means right. Without a value, the theme decides.
     * <p>
     * A callback gets an {@code info} object with {@code level}, {@code inPopover} and {@code isNarrow}. It returns
     * {@code "start"}, {@code "center"} or {@code "end"}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/day-header-render-hooks">dayHeaderAlign</a>
     */
    @JsonConverter(HeaderAlignValueConverter.class)
    private HeaderAlignValue dayHeaderAlign;

    /**
     * CSS classes for the divider line beneath the all-day section in time grid views.
     * <p>
     * It takes class names only, because the library accepts no callback for this option.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/all-day-divider-render-hooks">allDayDividerClass</a>
     */
    private String allDayDividerClass;

    /**
     * CSS classes for the cell in the header area of time grid views that labels the all-day section.
     * <p>
     * A callback gets an {@code info} object with {@code text}, {@code isNarrow} and {@code view}, see the library
     * docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/all-day-header-render-hooks">allDayHeaderClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue allDayHeaderClass;

    /**
     * Sets fixed CSS classes for the all-day header cell, see {@link #getAllDayHeaderClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setAllDayHeaderClass(String classNames) {
        this.allDayHeaderClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the all-day header cell in any form, see {@link #getAllDayHeaderClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setAllDayHeaderClass(ClassNameValue value) {
        this.allDayHeaderClass = value;
    }

    /**
     * A callback that runs right after the cell in the header area of time grid views that labels the all-day section
     * has been added to the DOM. It gets an {@code info} object with {@code text}, {@code isNarrow}, {@code view} and
     * more, see the library docs.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/all-day-header-render-hooks">allDayHeaderDidMount</a>
     */
    private JsCallback allDayHeaderDidMount;

    /**
     * CSS classes for the inner wrapper of the cell in the header area of time grid views that labels the all-day
     * section. Useful for adjusting padding.
     * <p>
     * A callback gets an {@code info} object with {@code text}, {@code isNarrow} and {@code view}, see the library
     * docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/all-day-header-render-hooks">allDayHeaderInnerClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue allDayHeaderInnerClass;

    /**
     * Sets fixed CSS classes for the inner wrapper of the all-day header cell, see
     * {@link #getAllDayHeaderInnerClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setAllDayHeaderInnerClass(String classNames) {
        this.allDayHeaderInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the inner wrapper of the all-day header cell in any form, see
     * {@link #getAllDayHeaderInnerClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setAllDayHeaderInnerClass(ClassNameValue value) {
        this.allDayHeaderInnerClass = value;
    }

    /**
     * A callback that runs right before the cell in the header area of time grid views that labels the all-day section
     * is removed from the DOM. It gets an {@code info} object with {@code text}, {@code isNarrow}, {@code view} and
     * more, see the library docs.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/all-day-header-render-hooks">allDayHeaderWillUnmount</a>
     */
    private JsCallback allDayHeaderWillUnmount;

    /**
     * CSS classes for the bottom area of a day cell in day grid views and in the all-day section of time grid views.
     * This is where the more-link appears.
     * <p>
     * A callback gets an {@code info} object with {@code date}, {@code text}, {@code isToday} and more, see the library
     * docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/day-cell-render-hooks">dayCellBottomClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue dayCellBottomClass;

    /**
     * Sets fixed CSS classes for the bottom area of day cells, see {@link #getDayCellBottomClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setDayCellBottomClass(String classNames) {
        this.dayCellBottomClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the bottom area of day cells in any form, see {@link #getDayCellBottomClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setDayCellBottomClass(ClassNameValue value) {
        this.dayCellBottomClass = value;
    }

    /**
     * A callback that runs right after a day cell has been added to the DOM. It gets an {@code info} object with
     * {@code date}, {@code text}, {@code isToday} and more, see the library docs.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/day-cell-render-hooks">dayCellDidMount</a>
     */
    private JsCallback dayCellDidMount;

    /**
     * CSS classes for the middle area of a day cell in day grid views and in the all-day section of time grid views.
     * This is where the entries appear.
     * <p>
     * A callback gets an {@code info} object with {@code date}, {@code text}, {@code isToday} and more, see the library
     * docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/day-cell-render-hooks">dayCellInnerClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue dayCellInnerClass;

    /**
     * Sets fixed CSS classes for the middle area of day cells, see {@link #getDayCellInnerClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setDayCellInnerClass(String classNames) {
        this.dayCellInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the middle area of day cells in any form, see {@link #getDayCellInnerClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setDayCellInnerClass(ClassNameValue value) {
        this.dayCellInnerClass = value;
    }

    /**
     * CSS classes for the top area of a day cell in day grid views and in the all-day section of time grid views. This
     * is where the day number appears.
     * <p>
     * A callback gets an {@code info} object with {@code date}, {@code text}, {@code isToday} and more, see the library
     * docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/day-cell-render-hooks">dayCellTopClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue dayCellTopClass;

    /**
     * Sets fixed CSS classes for the top area of day cells, see {@link #getDayCellTopClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setDayCellTopClass(String classNames) {
        this.dayCellTopClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the top area of day cells in any form, see {@link #getDayCellTopClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setDayCellTopClass(ClassNameValue value) {
        this.dayCellTopClass = value;
    }

    /**
     * Custom content for the top area of day cells, where the day number appears.
     * <p>
     * The generated content is inserted inside the inner-most wrapper of the day cell. It does not replace the cell
     * element itself.
     * <p>
     * A callback gets an {@code info} object with {@code date}, {@code text}, {@code isToday} and more, see the library
     * docs. It returns a text, or {@code {html: '...'}} or {@code {domNodes: [...]}}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/day-cell-render-hooks">dayCellTopContent</a>
     */
    @JsonConverter(ContentValueConverter.class)
    private ContentValue dayCellTopContent;

    /**
     * Sets a text as the content of the top area of a day cell, see {@link #getDayCellTopContent()}.
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setDayCellTopContent(String text) {
        this.dayCellTopContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content of the top area of a day cell in any form, see {@link #getDayCellTopContent()}.
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setDayCellTopContent(ContentValue value) {
        this.dayCellTopContent = value;
    }

    /**
     * CSS classes for the inner wrapper of the top area of a day cell (see {@link #getDayCellTopClass()}). This wrapper
     * may be a nav link.
     * <p>
     * A callback gets an {@code info} object with {@code date}, {@code text}, {@code isToday} and more, see the library
     * docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/day-cell-render-hooks">dayCellTopInnerClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue dayCellTopInnerClass;

    /**
     * Sets fixed CSS classes for the inner wrapper of the top area of day cells, see
     * {@link #getDayCellTopInnerClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setDayCellTopInnerClass(String classNames) {
        this.dayCellTopInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the inner wrapper of the top area of day cells in any form, see
     * {@link #getDayCellTopInnerClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setDayCellTopInnerClass(ClassNameValue value) {
        this.dayCellTopInnerClass = value;
    }

    /**
     * A callback that runs right before a day cell is removed from the DOM. It gets an {@code info} object with
     * {@code date}, {@code text}, {@code isToday} and more, see the library docs.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/day-cell-render-hooks">dayCellWillUnmount</a>
     */
    private JsCallback dayCellWillUnmount;

    /**
     * CSS classes for day header cells (above the day cells in day grid and time grid views) and the header of the
     * entry popover.
     * <p>
     * A callback gets an {@code info} object with {@code date}, {@code text}, {@code isToday} and more, see the library
     * docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/day-header-render-hooks">dayHeaderClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue dayHeaderClass;

    /**
     * Sets fixed CSS classes for day header cells, see {@link #getDayHeaderClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setDayHeaderClass(String classNames) {
        this.dayHeaderClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for day header cells in any form, see {@link #getDayHeaderClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setDayHeaderClass(ClassNameValue value) {
        this.dayHeaderClass = value;
    }

    /**
     * Custom content for day header cells.
     * <p>
     * The generated content is inserted inside the inner-most wrapper of the day header cell. It does not replace the
     * element itself.
     * <p>
     * A callback gets an {@code info} object with {@code date}, {@code text}, {@code isToday} and more, see the library
     * docs. It returns a text, or {@code {html: '...'}} or {@code {domNodes: [...]}}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/day-header-render-hooks">dayHeaderContent</a>
     */
    @JsonConverter(ContentValueConverter.class)
    private ContentValue dayHeaderContent;

    /**
     * Sets a text as the content of the day header cell, see {@link #getDayHeaderContent()}.
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setDayHeaderContent(String text) {
        this.dayHeaderContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content of the day header cell in any form, see {@link #getDayHeaderContent()}.
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setDayHeaderContent(ContentValue value) {
        this.dayHeaderContent = value;
    }

    /**
     * A callback that runs right after a day header cell has been added to the DOM. It gets an {@code info} object with
     * {@code date}, {@code text}, {@code view} and more, see the library docs.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/day-header-render-hooks">dayHeaderDidMount</a>
     */
    private JsCallback dayHeaderDidMount;

    /**
     * CSS classes for the divider line beneath the day header cells in day grid and time grid views.
     * <p>
     * A callback gets an {@code info} object with {@code isSticky}, {@code multiMonthColumns} and {@code options}, see
     * the library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/day-header-divider-render-hooks">dayHeaderDividerClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue dayHeaderDividerClass;

    /**
     * Sets fixed CSS classes for the day header divider, see {@link #getDayHeaderDividerClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setDayHeaderDividerClass(String classNames) {
        this.dayHeaderDividerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the day header divider in any form, see {@link #getDayHeaderDividerClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setDayHeaderDividerClass(ClassNameValue value) {
        this.dayHeaderDividerClass = value;
    }

    /**
     * CSS classes for the inner wrapper of the day header cells in day grid and time grid views. This wrapper may be a
     * nav link.
     * <p>
     * A callback gets an {@code info} object with {@code date}, {@code text}, {@code isToday} and more, see the library
     * docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/day-header-render-hooks">dayHeaderInnerClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue dayHeaderInnerClass;

    /**
     * Sets fixed CSS classes for the inner wrapper of day header cells, see {@link #getDayHeaderInnerClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setDayHeaderInnerClass(String classNames) {
        this.dayHeaderInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the inner wrapper of day header cells in any form, see
     * {@link #getDayHeaderInnerClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setDayHeaderInnerClass(ClassNameValue value) {
        this.dayHeaderInnerClass = value;
    }

    /**
     * CSS classes for the row that contains the day header cells in day grid and time grid views.
     * <p>
     * It takes class names only, because the library accepts no callback for this option.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/day-header-row-render-hooks">dayHeaderRowClass</a>
     */
    private String dayHeaderRowClass;

    /**
     * A callback that runs right before a day header cell is removed from the DOM. It gets an {@code info} object with
     * {@code date}, {@code text}, {@code view} and more, see the library docs.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/day-header-render-hooks">dayHeaderWillUnmount</a>
     */
    private JsCallback dayHeaderWillUnmount;

    /**
     * CSS classes for day lanes, the columns of time slots of a day in time grid views.
     * <p>
     * A callback gets an {@code info} object with {@code date}, {@code isToday}, {@code isNarrow} and more, see the
     * library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/day-lane-render-hooks">dayLaneClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue dayLaneClass;

    /**
     * Sets fixed CSS classes for day lanes, see {@link #getDayLaneClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setDayLaneClass(String classNames) {
        this.dayLaneClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for day lanes in any form, see {@link #getDayLaneClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setDayLaneClass(ClassNameValue value) {
        this.dayLaneClass = value;
    }

    /**
     * A callback that runs right after a day lane (column of time slots in time grid views) has been added to the DOM.
     * It gets an {@code info} object with {@code date}, {@code isToday}, {@code isStack} and more, see the library
     * docs.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/day-lane-render-hooks">dayLaneDidMount</a>
     */
    private JsCallback dayLaneDidMount;

    /**
     * CSS classes for the inner wrapper of a day lane. Useful for adjusting padding.
     * <p>
     * A callback gets an {@code info} object with {@code date}, {@code isToday}, {@code isNarrow} and more, see the
     * library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/day-lane-render-hooks">dayLaneInnerClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue dayLaneInnerClass;

    /**
     * Sets fixed CSS classes for the inner wrapper of day lanes, see {@link #getDayLaneInnerClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setDayLaneInnerClass(String classNames) {
        this.dayLaneInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the inner wrapper of day lanes in any form, see {@link #getDayLaneInnerClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setDayLaneInnerClass(ClassNameValue value) {
        this.dayLaneInnerClass = value;
    }

    /**
     * A callback that runs right before a day lane (column of time slots in time grid views) is removed from the DOM.
     * It gets an {@code info} object with {@code date}, {@code isToday}, {@code isStack} and more, see the library
     * docs.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/day-lane-render-hooks">dayLaneWillUnmount</a>
     */
    private JsCallback dayLaneWillUnmount;

    /**
     * CSS classes for the horizontal day row in table-based date layouts.
     * <p>
     * It takes class names only, because the library accepts no callback for this option.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/day-row-render-hooks">dayRowClass</a>
     */
    private String dayRowClass;

    /**
     * CSS classes for the filler elements that keep the calendar layout aligned. They fill space in the all-day section
     * to offset the scrollbar width of the timed section, and in resource timeline views where the resource rows do
     * not fill the full height.
     * <p>
     * A callback gets an {@code info} object with {@code inTableHeader}, see the library docs. It returns the class
     * names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/filler-render-hooks">fillerClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue fillerClass;

    /**
     * Sets fixed CSS classes for filler elements, see {@link #getFillerClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setFillerClass(String classNames) {
        this.fillerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for filler elements in any form, see {@link #getFillerClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setFillerClass(ClassNameValue value) {
        this.fillerClass = value;
    }

    /**
     * CSS classes for the highlight element that appears while the user selects a date range.
     * <p>
     * It takes class names only, because the library accepts no callback for this option.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/highlight-render-hooks">highlightClass</a>
     */
    private String highlightClass;

    /**
     * CSS classes for the elements that shade the non-business hours.
     * <p>
     * It takes class names only, because the library accepts no callback for this option.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/business-hours-render-hooks">nonBusinessHoursClass</a>
     */
    private String nonBusinessHoursClass;

    // Render hooks: time slots, now indicator, week numbers, tables

    /**
     * CSS classes for the week number shown inline in day grid cells.
     * <p>
     * A callback gets an {@code info} object with {@code num}, {@code text}, {@code textParts} and more, see the
     * library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/inline-week-number-render-hooks">inlineWeekNumberClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue inlineWeekNumberClass;

    /**
     * Sets fixed CSS classes, see {@link #getInlineWeekNumberClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setInlineWeekNumberClass(String classNames) {
        this.inlineWeekNumberClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getInlineWeekNumberClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setInlineWeekNumberClass(ClassNameValue value) {
        this.inlineWeekNumberClass = value;
    }

    /**
     * Custom content for the week number shown inline in day grid cells.
     * <p>
     * A callback gets an {@code info} object with {@code num}, {@code text}, {@code textParts} and more, see the
     * library docs. It returns a text, or {@code {html: '...'}} or {@code {domNodes: [...]}}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/inline-week-number-render-hooks">inlineWeekNumberContent</a>
     */
    @JsonConverter(ContentValueConverter.class)
    private ContentValue inlineWeekNumberContent;

    /**
     * Sets a text as the content, see {@link #getInlineWeekNumberContent()}.
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setInlineWeekNumberContent(String text) {
        this.inlineWeekNumberContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content in any form, see {@link #getInlineWeekNumberContent()}.
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setInlineWeekNumberContent(ContentValue value) {
        this.inlineWeekNumberContent = value;
    }

    /**
     * A callback that runs right after the week number shown inline in day grid cells has been added to the DOM.
     * <p>
     * The callback gets an {@code info} object with {@code num}, {@code text}, {@code textParts} and more, see the
     * library docs.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/inline-week-number-render-hooks">inlineWeekNumberDidMount</a>
     */
    private JsCallback inlineWeekNumberDidMount;

    /**
     * A callback that runs right before the week number shown inline in day grid cells is removed from the DOM.
     * <p>
     * The callback gets an {@code info} object with {@code num}, {@code text}, {@code textParts} and more, see the
     * library docs.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/inline-week-number-render-hooks">inlineWeekNumberWillUnmount</a>
     */
    private JsCallback inlineWeekNumberWillUnmount;

    /**
     * CSS classes for the dot of the now indicator. The dot sits alongside the indicator line in the time axis of time
     * grid and timeline views.
     * <p>
     * It takes class names only, because the library accepts no callback for this option.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/now-indicator-dot-render-hooks">nowIndicatorDotClass</a>
     */
    private String nowIndicatorDotClass;

    /**
     * CSS classes for the arrow of the now indicator in the time axis (time grid and timeline views).
     * <p>
     * A callback gets an {@code info} object with {@code date} and {@code view}. It returns the class names as a
     * string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/now-indicator-header-render-hooks">nowIndicatorHeaderClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue nowIndicatorHeaderClass;

    /**
     * Sets fixed CSS classes, see {@link #getNowIndicatorHeaderClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setNowIndicatorHeaderClass(String classNames) {
        this.nowIndicatorHeaderClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getNowIndicatorHeaderClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setNowIndicatorHeaderClass(ClassNameValue value) {
        this.nowIndicatorHeaderClass = value;
    }

    /**
     * Custom content for the arrow of the now indicator in the time axis (time grid and timeline views).
     * <p>
     * A callback gets an {@code info} object with {@code date} and {@code view}. It returns a text, or
     * {@code {html: '...'}} or {@code {domNodes: [...]}}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/now-indicator-header-render-hooks">nowIndicatorHeaderContent</a>
     */
    @JsonConverter(ContentValueConverter.class)
    private ContentValue nowIndicatorHeaderContent;

    /**
     * Sets a text as the content, see {@link #getNowIndicatorHeaderContent()}.
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setNowIndicatorHeaderContent(String text) {
        this.nowIndicatorHeaderContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content in any form, see {@link #getNowIndicatorHeaderContent()}.
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setNowIndicatorHeaderContent(ContentValue value) {
        this.nowIndicatorHeaderContent = value;
    }

    /**
     * A callback that runs right after the now indicator arrow has been added to the DOM.
     * <p>
     * The callback gets an {@code info} object with {@code date}, {@code view} and {@code el}.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/now-indicator-header-render-hooks">nowIndicatorHeaderDidMount</a>
     */
    private JsCallback nowIndicatorHeaderDidMount;

    /**
     * A callback that runs right before the now indicator arrow is removed from the DOM.
     * <p>
     * The callback gets an {@code info} object with {@code date}, {@code view} and {@code el}.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/now-indicator-header-render-hooks">nowIndicatorHeaderWillUnmount</a>
     */
    private JsCallback nowIndicatorHeaderWillUnmount;

    /**
     * CSS classes for the line of the now indicator over the day columns (time grid and timeline views).
     * <p>
     * A callback gets an {@code info} object with {@code date} and {@code view}. It returns the class names as a
     * string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/now-indicator-line-render-hooks">nowIndicatorLineClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue nowIndicatorLineClass;

    /**
     * Sets fixed CSS classes, see {@link #getNowIndicatorLineClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setNowIndicatorLineClass(String classNames) {
        this.nowIndicatorLineClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getNowIndicatorLineClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setNowIndicatorLineClass(ClassNameValue value) {
        this.nowIndicatorLineClass = value;
    }

    /**
     * Custom content for the line of the now indicator over the day columns (time grid and timeline views).
     * <p>
     * A callback gets an {@code info} object with {@code date} and {@code view}. It returns a text, or
     * {@code {html: '...'}} or {@code {domNodes: [...]}}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/now-indicator-line-render-hooks">nowIndicatorLineContent</a>
     */
    @JsonConverter(ContentValueConverter.class)
    private ContentValue nowIndicatorLineContent;

    /**
     * Sets a text as the content, see {@link #getNowIndicatorLineContent()}.
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setNowIndicatorLineContent(String text) {
        this.nowIndicatorLineContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content in any form, see {@link #getNowIndicatorLineContent()}.
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setNowIndicatorLineContent(ContentValue value) {
        this.nowIndicatorLineContent = value;
    }

    /**
     * A callback that runs right after the now indicator line has been added to the DOM.
     * <p>
     * The callback gets an {@code info} object with {@code date}, {@code view} and {@code el}.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/now-indicator-line-render-hooks">nowIndicatorLineDidMount</a>
     */
    private JsCallback nowIndicatorLineDidMount;

    /**
     * A callback that runs right before the now indicator line is removed from the DOM.
     * <p>
     * The callback gets an {@code info} object with {@code date}, {@code view} and {@code el}.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/now-indicator-line-render-hooks">nowIndicatorLineWillUnmount</a>
     */
    private JsCallback nowIndicatorLineWillUnmount;

    /**
     * CSS classes for time slot headers, where time grid and timeline views show the date or time text of a slot.
     * <p>
     * A callback gets an {@code info} object with {@code date}, {@code text}, {@code isPast} and more, see the library
     * docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/slot-header-render-hooks">slotHeaderClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue slotHeaderClass;

    /**
     * Sets fixed CSS classes, see {@link #getSlotHeaderClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setSlotHeaderClass(String classNames) {
        this.slotHeaderClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getSlotHeaderClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setSlotHeaderClass(ClassNameValue value) {
        this.slotHeaderClass = value;
    }

    /**
     * Custom content for a time slot header.
     * <p>
     * A callback gets an {@code info} object with {@code date}, {@code text}, {@code isPast} and more, see the library
     * docs. It returns a text, or {@code {html: '...'}} or {@code {domNodes: [...]}}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/slot-header-render-hooks">slotHeaderContent</a>
     */
    @JsonConverter(ContentValueConverter.class)
    private ContentValue slotHeaderContent;

    /**
     * Sets a text as the content, see {@link #getSlotHeaderContent()}.
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setSlotHeaderContent(String text) {
        this.slotHeaderContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content in any form, see {@link #getSlotHeaderContent()}.
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setSlotHeaderContent(ContentValue value) {
        this.slotHeaderContent = value;
    }

    /**
     * A callback that runs right after a time slot header has been added to the DOM.
     * <p>
     * The callback gets an {@code info} object with {@code date}, {@code text}, {@code isPast} and more, see the
     * library docs.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/slot-header-render-hooks">slotHeaderDidMount</a>
     */
    private JsCallback slotHeaderDidMount;

    /**
     * CSS classes for the divider line between the time labels and the slot lanes, in time grid and timeline views.
     * <p>
     * A callback gets an {@code info} object with {@code inTableHeader}, {@code options} (with {@code dayMinWidth}). It
     * returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/slot-header-divider-render-hooks">slotHeaderDividerClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue slotHeaderDividerClass;

    /**
     * Sets fixed CSS classes, see {@link #getSlotHeaderDividerClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setSlotHeaderDividerClass(String classNames) {
        this.slotHeaderDividerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getSlotHeaderDividerClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setSlotHeaderDividerClass(ClassNameValue value) {
        this.slotHeaderDividerClass = value;
    }

    /**
     * CSS classes for the inner wrapper of the slot headers, where time grid and timeline views display their date and
     * time text. Useful for adjusting padding.
     * <p>
     * A callback gets an {@code info} object with {@code date}, {@code text}, {@code isPast} and more, see the library
     * docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/slot-header-render-hooks">slotHeaderInnerClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue slotHeaderInnerClass;

    /**
     * Sets fixed CSS classes, see {@link #getSlotHeaderInnerClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setSlotHeaderInnerClass(String classNames) {
        this.slotHeaderInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getSlotHeaderInnerClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setSlotHeaderInnerClass(ClassNameValue value) {
        this.slotHeaderInnerClass = value;
    }

    /**
     * CSS classes for the row that contains the slot header cells in time grid and timeline views.
     * <p>
     * It takes class names only, because the library accepts no callback for this option.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/slot-header-row-render-hooks">slotHeaderRowClass</a>
     */
    private String slotHeaderRowClass;

    /**
     * A callback that runs right before a time slot header is removed from the DOM.
     * <p>
     * The callback gets an {@code info} object with {@code date}, {@code text}, {@code isPast} and more, see the
     * library docs.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/slot-header-render-hooks">slotHeaderWillUnmount</a>
     */
    private JsCallback slotHeaderWillUnmount;

    /**
     * CSS classes for the lane of a time slot. In time grid views this is the horizontal space passing under all days,
     * in timeline views the vertical space passing through the resources.
     * <p>
     * A callback gets an {@code info} object with {@code date}, {@code time}, {@code view} and more, see the library
     * docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/slot-lane-render-hooks">slotLaneClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue slotLaneClass;

    /**
     * Sets fixed CSS classes, see {@link #getSlotLaneClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setSlotLaneClass(String classNames) {
        this.slotLaneClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getSlotLaneClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setSlotLaneClass(ClassNameValue value) {
        this.slotLaneClass = value;
    }

    /**
     * A callback that runs right after a time slot lane has been added to the DOM.
     * <p>
     * The callback gets an {@code info} object with {@code date}, {@code time}, {@code view} and more, see the library
     * docs.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/slot-lane-render-hooks">slotLaneDidMount</a>
     */
    private JsCallback slotLaneDidMount;

    /**
     * A callback that runs right before a time slot lane is removed from the DOM.
     * <p>
     * The callback gets an {@code info} object with {@code date}, {@code time}, {@code view} and more, see the library
     * docs.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/slot-lane-render-hooks">slotLaneWillUnmount</a>
     */
    private JsCallback slotLaneWillUnmount;

    /**
     * CSS classes for the body of the table-structured layout used across views, where the day cells and entries
     * appear.
     * <p>
     * A callback gets an {@code info} object with {@code borderlessX}, {@code borderlessTop}, {@code borderlessBottom}
     * and more, see the library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/table-render-hooks">tableBodyClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue tableBodyClass;

    /**
     * Sets fixed CSS classes, see {@link #getTableBodyClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setTableBodyClass(String classNames) {
        this.tableBodyClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getTableBodyClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setTableBodyClass(ClassNameValue value) {
        this.tableBodyClass = value;
    }

    /**
     * CSS classes for the root element of the table-structured layout used across views.
     * <p>
     * A callback gets an {@code info} object with {@code borderlessX}, {@code borderlessTop}, {@code borderlessBottom}
     * and more, see the library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/table-render-hooks">tableClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue tableClass;

    /**
     * Sets fixed CSS classes, see {@link #getTableClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setTableClass(String classNames) {
        this.tableClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getTableClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setTableClass(ClassNameValue value) {
        this.tableClass = value;
    }

    /**
     * CSS classes for the header row of the table-structured layout used across views, where the day names appear.
     * <p>
     * A callback gets an {@code info} object with {@code borderlessX}, {@code borderlessTop}, {@code borderlessBottom}
     * and more, see the library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/table-render-hooks">tableHeaderClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue tableHeaderClass;

    /**
     * Sets fixed CSS classes, see {@link #getTableHeaderClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setTableHeaderClass(String classNames) {
        this.tableHeaderClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getTableHeaderClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setTableHeaderClass(ClassNameValue value) {
        this.tableHeaderClass = value;
    }

    /**
     * CSS classes for the week number in the header above the time axis of time grid views.
     * <p>
     * A callback gets an {@code info} object with {@code num}, {@code text}, {@code textParts} and more, see the
     * library docs. {@code options} holds only {@code dayMinWidth}. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/week-number-header-render-hooks">weekNumberHeaderClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue weekNumberHeaderClass;

    /**
     * Sets fixed CSS classes, see {@link #getWeekNumberHeaderClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setWeekNumberHeaderClass(String classNames) {
        this.weekNumberHeaderClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getWeekNumberHeaderClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setWeekNumberHeaderClass(ClassNameValue value) {
        this.weekNumberHeaderClass = value;
    }

    /**
     * Custom content for the week number in the header above the time axis of time grid views.
     * <p>
     * A callback gets an {@code info} object with {@code num}, {@code text}, {@code textParts} and more, see the
     * library docs. {@code options} holds only {@code dayMinWidth}. It returns a text, or {@code {html: '...'}} or
     * {@code {domNodes: [...]}}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/week-number-header-render-hooks">weekNumberHeaderContent</a>
     */
    @JsonConverter(ContentValueConverter.class)
    private ContentValue weekNumberHeaderContent;

    /**
     * Sets a text as the content, see {@link #getWeekNumberHeaderContent()}.
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setWeekNumberHeaderContent(String text) {
        this.weekNumberHeaderContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content in any form, see {@link #getWeekNumberHeaderContent()}.
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setWeekNumberHeaderContent(ContentValue value) {
        this.weekNumberHeaderContent = value;
    }

    /**
     * A callback that runs right after the week number in the header above the time axis of time grid views has
     * been added to the DOM.
     * <p>
     * The callback gets an {@code info} object with {@code num}, {@code text}, {@code textParts} and more, see the
     * library docs. {@code options} holds only {@code dayMinWidth}.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/week-number-header-render-hooks">weekNumberHeaderDidMount</a>
     */
    private JsCallback weekNumberHeaderDidMount;

    /**
     * CSS classes for the inner wrapper of the week number header in time grid views, the top-left cell above the time
     * axis. Useful for adjusting padding.
     * <p>
     * A callback gets an {@code info} object with {@code num}, {@code text}, {@code textParts} and more, see the
     * library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/week-number-header-render-hooks">weekNumberHeaderInnerClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue weekNumberHeaderInnerClass;

    /**
     * Sets fixed CSS classes, see {@link #getWeekNumberHeaderInnerClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setWeekNumberHeaderInnerClass(String classNames) {
        this.weekNumberHeaderInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getWeekNumberHeaderInnerClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setWeekNumberHeaderInnerClass(ClassNameValue value) {
        this.weekNumberHeaderInnerClass = value;
    }

    /**
     * A callback that runs right before the week number in the header above the time axis of time grid views is removed
     * from the DOM.
     * <p>
     * The callback gets an {@code info} object with {@code num}, {@code text}, {@code textParts} and more, see the
     * library docs. {@code options} holds only {@code dayMinWidth}.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/week-number-header-render-hooks">weekNumberHeaderWillUnmount</a>
     */
    private JsCallback weekNumberHeaderWillUnmount;

    // Render hooks: entry layouts (block, column, row)

    /**
     * CSS classes for the outermost element of block entries. Without a value, no classes are added.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code timeText}, {@code isStart} and more, see the
     * library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/block-event-render-hooks">blockEventClass</a>
     */
    @JsonName("blockEventClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue blockEntryClass;

    /**
     * Sets fixed CSS classes for the outermost element of block entries, see {@link #getBlockEntryClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setBlockEntryClass(String classNames) {
        this.blockEntryClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the outermost element of block entries in any form, see {@link #getBlockEntryClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setBlockEntryClass(ClassNameValue value) {
        this.blockEntryClass = value;
    }

    /**
     * CSS classes for the element before the "inner" wrapper of block entries. Without a value, no classes are added.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code timeText}, {@code isStart} and more, see the
     * library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/block-event-render-hooks">blockEventBeforeClass</a>
     */
    @JsonName("blockEventBeforeClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue blockEntryBeforeClass;

    /**
     * Sets fixed CSS classes for the element before the "inner" wrapper of block entries, see
     * {@link #getBlockEntryBeforeClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setBlockEntryBeforeClass(String classNames) {
        this.blockEntryBeforeClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the element before the "inner" wrapper of block entries in any form, see
     * {@link #getBlockEntryBeforeClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setBlockEntryBeforeClass(ClassNameValue value) {
        this.blockEntryBeforeClass = value;
    }

    /**
     * CSS classes for the element after the "inner" wrapper of block entries. Without a value, no classes are added.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code timeText}, {@code isStart} and more, see the
     * library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/block-event-render-hooks">blockEventAfterClass</a>
     */
    @JsonName("blockEventAfterClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue blockEntryAfterClass;

    /**
     * Sets fixed CSS classes for the element after the "inner" wrapper of block entries, see
     * {@link #getBlockEntryAfterClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setBlockEntryAfterClass(String classNames) {
        this.blockEntryAfterClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the element after the "inner" wrapper of block entries in any form, see
     * {@link #getBlockEntryAfterClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setBlockEntryAfterClass(ClassNameValue value) {
        this.blockEntryAfterClass = value;
    }

    /**
     * CSS classes for the "inner" wrapper element of block entries. Without a value, no classes are added.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code timeText}, {@code isStart} and more, see the
     * library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/block-event-render-hooks">blockEventInnerClass</a>
     */
    @JsonName("blockEventInnerClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue blockEntryInnerClass;

    /**
     * Sets fixed CSS classes for the "inner" wrapper element of block entries, see {@link #getBlockEntryInnerClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setBlockEntryInnerClass(String classNames) {
        this.blockEntryInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the "inner" wrapper element of block entries in any form, see
     * {@link #getBlockEntryInnerClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setBlockEntryInnerClass(ClassNameValue value) {
        this.blockEntryInnerClass = value;
    }

    /**
     * CSS classes for the time element within the "inner" wrapper of block entries. Without a value, no classes are
     * added.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code isNarrow} and {@code isShort}, see the library
     * docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/block-event-render-hooks">blockEventTimeClass</a>
     */
    @JsonName("blockEventTimeClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue blockEntryTimeClass;

    /**
     * Sets fixed CSS classes for the time element within the "inner" wrapper of block entries, see
     * {@link #getBlockEntryTimeClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setBlockEntryTimeClass(String classNames) {
        this.blockEntryTimeClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the time element within the "inner" wrapper of block entries in any form, see
     * {@link #getBlockEntryTimeClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setBlockEntryTimeClass(ClassNameValue value) {
        this.blockEntryTimeClass = value;
    }

    /**
     * CSS classes for the title element within the "inner" wrapper of block entries. Without a value, no classes are
     * added.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code isNarrow} and {@code isShort}, see the library
     * docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/block-event-render-hooks">blockEventTitleClass</a>
     */
    @JsonName("blockEventTitleClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue blockEntryTitleClass;

    /**
     * Sets fixed CSS classes for the title element within the "inner" wrapper of block entries, see
     * {@link #getBlockEntryTitleClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setBlockEntryTitleClass(String classNames) {
        this.blockEntryTitleClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the title element within the "inner" wrapper of block entries in any form, see
     * {@link #getBlockEntryTitleClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setBlockEntryTitleClass(ClassNameValue value) {
        this.blockEntryTitleClass = value;
    }

    /**
     * CSS classes for the outermost element of column entries. Without a value, no classes are added.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code timeText}, {@code isStart} and more, see the
     * library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/column-event-render-hooks">columnEventClass</a>
     */
    @JsonName("columnEventClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue columnEntryClass;

    /**
     * Sets fixed CSS classes for the outermost element of column entries, see {@link #getColumnEntryClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setColumnEntryClass(String classNames) {
        this.columnEntryClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the outermost element of column entries in any form, see {@link #getColumnEntryClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setColumnEntryClass(ClassNameValue value) {
        this.columnEntryClass = value;
    }

    /**
     * CSS classes for the element before the "inner" wrapper of column entries. Without a value, no classes are added.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code timeText}, {@code isStart} and more, see the
     * library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/column-event-render-hooks">columnEventBeforeClass</a>
     */
    @JsonName("columnEventBeforeClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue columnEntryBeforeClass;

    /**
     * Sets fixed CSS classes for the element before the "inner" wrapper of column entries, see
     * {@link #getColumnEntryBeforeClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setColumnEntryBeforeClass(String classNames) {
        this.columnEntryBeforeClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the element before the "inner" wrapper of column entries in any form, see
     * {@link #getColumnEntryBeforeClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setColumnEntryBeforeClass(ClassNameValue value) {
        this.columnEntryBeforeClass = value;
    }

    /**
     * CSS classes for the element after the "inner" wrapper of column entries. Without a value, no classes are added.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code timeText}, {@code isStart} and more, see the
     * library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/column-event-render-hooks">columnEventAfterClass</a>
     */
    @JsonName("columnEventAfterClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue columnEntryAfterClass;

    /**
     * Sets fixed CSS classes for the element after the "inner" wrapper of column entries, see
     * {@link #getColumnEntryAfterClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setColumnEntryAfterClass(String classNames) {
        this.columnEntryAfterClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the element after the "inner" wrapper of column entries in any form, see
     * {@link #getColumnEntryAfterClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setColumnEntryAfterClass(ClassNameValue value) {
        this.columnEntryAfterClass = value;
    }

    /**
     * CSS classes for the "inner" wrapper element of column entries. Without a value, no classes are added.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code timeText}, {@code isStart} and more, see the
     * library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/column-event-render-hooks">columnEventInnerClass</a>
     */
    @JsonName("columnEventInnerClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue columnEntryInnerClass;

    /**
     * Sets fixed CSS classes for the "inner" wrapper element of column entries, see
     * {@link #getColumnEntryInnerClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setColumnEntryInnerClass(String classNames) {
        this.columnEntryInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the "inner" wrapper element of column entries in any form, see
     * {@link #getColumnEntryInnerClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setColumnEntryInnerClass(ClassNameValue value) {
        this.columnEntryInnerClass = value;
    }

    /**
     * CSS classes for the time element within the "inner" wrapper of column entries. Without a value, no classes are
     * added.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code isNarrow} and {@code isShort}, see the library
     * docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/column-event-render-hooks">columnEventTimeClass</a>
     */
    @JsonName("columnEventTimeClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue columnEntryTimeClass;

    /**
     * Sets fixed CSS classes for the time element within the "inner" wrapper of column entries, see
     * {@link #getColumnEntryTimeClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setColumnEntryTimeClass(String classNames) {
        this.columnEntryTimeClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the time element within the "inner" wrapper of column entries in any form, see
     * {@link #getColumnEntryTimeClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setColumnEntryTimeClass(ClassNameValue value) {
        this.columnEntryTimeClass = value;
    }

    /**
     * CSS classes for the title element within the "inner" wrapper of column entries. Without a value, no classes are
     * added.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code isNarrow} and {@code isShort}, see the library
     * docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/column-event-render-hooks">columnEventTitleClass</a>
     */
    @JsonName("columnEventTitleClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue columnEntryTitleClass;

    /**
     * Sets fixed CSS classes for the title element within the "inner" wrapper of column entries, see
     * {@link #getColumnEntryTitleClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setColumnEntryTitleClass(String classNames) {
        this.columnEntryTitleClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the title element within the "inner" wrapper of column entries in any form, see
     * {@link #getColumnEntryTitleClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setColumnEntryTitleClass(ClassNameValue value) {
        this.columnEntryTitleClass = value;
    }

    /**
     * CSS classes for the outermost element of row entries. Without a value, no classes are added.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code timeText}, {@code isStart} and more, see the
     * library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/row-event-render-hooks">rowEventClass</a>
     */
    @JsonName("rowEventClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue rowEntryClass;

    /**
     * Sets fixed CSS classes for the outermost element of row entries, see {@link #getRowEntryClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setRowEntryClass(String classNames) {
        this.rowEntryClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the outermost element of row entries in any form, see {@link #getRowEntryClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setRowEntryClass(ClassNameValue value) {
        this.rowEntryClass = value;
    }

    /**
     * CSS classes for the element before the "inner" wrapper of row entries. Without a value, no classes are added.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code timeText}, {@code isStart} and more, see the
     * library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/row-event-render-hooks">rowEventBeforeClass</a>
     */
    @JsonName("rowEventBeforeClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue rowEntryBeforeClass;

    /**
     * Sets fixed CSS classes for the element before the "inner" wrapper of row entries, see
     * {@link #getRowEntryBeforeClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setRowEntryBeforeClass(String classNames) {
        this.rowEntryBeforeClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the element before the "inner" wrapper of row entries in any form, see
     * {@link #getRowEntryBeforeClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setRowEntryBeforeClass(ClassNameValue value) {
        this.rowEntryBeforeClass = value;
    }

    /**
     * Generated content inserted inside the element before the "inner" wrapper of row entries. It is hidden from
     * assistive
     * technology. Useful for decorations such as an arrow showing that the entry continues from an earlier date.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code timeText}, {@code isStart} and more, see the
     * library docs. It returns a text, or {@code {html: '...'}} or {@code {domNodes: [...]}}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/row-event-render-hooks">rowEventBeforeContent</a>
     */
    @JsonName("rowEventBeforeContent")
    @JsonConverter(ContentValueConverter.class)
    private ContentValue rowEntryBeforeContent;

    /**
     * Sets a text as the content before the "inner" wrapper of row entries, see {@link #getRowEntryBeforeContent()}.
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setRowEntryBeforeContent(String text) {
        this.rowEntryBeforeContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content before the "inner" wrapper of row entries in any form, see {@link #getRowEntryBeforeContent()}.
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setRowEntryBeforeContent(ContentValue value) {
        this.rowEntryBeforeContent = value;
    }

    /**
     * CSS classes for the element after the "inner" wrapper of row entries. Without a value, no classes are added.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code timeText}, {@code isStart} and more, see the
     * library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/row-event-render-hooks">rowEventAfterClass</a>
     */
    @JsonName("rowEventAfterClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue rowEntryAfterClass;

    /**
     * Sets fixed CSS classes for the element after the "inner" wrapper of row entries, see
     * {@link #getRowEntryAfterClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setRowEntryAfterClass(String classNames) {
        this.rowEntryAfterClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the element after the "inner" wrapper of row entries in any form, see
     * {@link #getRowEntryAfterClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setRowEntryAfterClass(ClassNameValue value) {
        this.rowEntryAfterClass = value;
    }

    /**
     * Generated content inserted inside the element after the "inner" wrapper of row entries. It is hidden from
     * assistive
     * technology. Useful for decorations such as an arrow showing that the entry continues past a later date.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code timeText}, {@code isStart} and more, see the
     * library docs. It returns a text, or {@code {html: '...'}} or {@code {domNodes: [...]}}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/row-event-render-hooks">rowEventAfterContent</a>
     */
    @JsonName("rowEventAfterContent")
    @JsonConverter(ContentValueConverter.class)
    private ContentValue rowEntryAfterContent;

    /**
     * Sets a text as the content after the "inner" wrapper of row entries, see {@link #getRowEntryAfterContent()}.
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setRowEntryAfterContent(String text) {
        this.rowEntryAfterContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content after the "inner" wrapper of row entries in any form, see {@link #getRowEntryAfterContent()}.
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setRowEntryAfterContent(ContentValue value) {
        this.rowEntryAfterContent = value;
    }

    /**
     * CSS classes for the "inner" wrapper element of row entries. Without a value, no classes are added.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code timeText}, {@code isStart} and more, see the
     * library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/row-event-render-hooks">rowEventInnerClass</a>
     */
    @JsonName("rowEventInnerClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue rowEntryInnerClass;

    /**
     * Sets fixed CSS classes for the "inner" wrapper element of row entries, see {@link #getRowEntryInnerClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setRowEntryInnerClass(String classNames) {
        this.rowEntryInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the "inner" wrapper element of row entries in any form, see
     * {@link #getRowEntryInnerClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setRowEntryInnerClass(ClassNameValue value) {
        this.rowEntryInnerClass = value;
    }

    /**
     * CSS classes for the time element within the "inner" wrapper of row entries. Without a value, no classes are
     * added.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code isNarrow} and {@code isShort}, see the library
     * docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/row-event-render-hooks">rowEventTimeClass</a>
     */
    @JsonName("rowEventTimeClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue rowEntryTimeClass;

    /**
     * Sets fixed CSS classes for the time element within the "inner" wrapper of row entries, see
     * {@link #getRowEntryTimeClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setRowEntryTimeClass(String classNames) {
        this.rowEntryTimeClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the time element within the "inner" wrapper of row entries in any form, see
     * {@link #getRowEntryTimeClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setRowEntryTimeClass(ClassNameValue value) {
        this.rowEntryTimeClass = value;
    }

    /**
     * CSS classes for the title element within the "inner" wrapper of row entries. Without a value, no classes are
     * added.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code isNarrow} and {@code isShort}, see the library
     * docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/row-event-render-hooks">rowEventTitleClass</a>
     */
    @JsonName("rowEventTitleClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue rowEntryTitleClass;

    /**
     * Sets fixed CSS classes for the title element within the "inner" wrapper of row entries, see
     * {@link #getRowEntryTitleClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setRowEntryTitleClass(String classNames) {
        this.rowEntryTitleClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the title element within the "inner" wrapper of row entries in any form, see
     * {@link #getRowEntryTitleClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setRowEntryTitleClass(ClassNameValue value) {
        this.rowEntryTitleClass = value;
    }

    // Render hooks: entries

    /**
     * CSS classes for the outermost element of a background entry.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code timeText}, {@code isStart} and more, see the
     * library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/background-event-render-hooks">backgroundEventClass</a>
     */
    @JsonName("backgroundEventClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue backgroundEntryClass;

    /**
     * Sets fixed CSS classes, see {@link #getBackgroundEntryClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setBackgroundEntryClass(String classNames) {
        this.backgroundEntryClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getBackgroundEntryClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setBackgroundEntryClass(ClassNameValue value) {
        this.backgroundEntryClass = value;
    }

    /**
     * Custom content for background entries. The generated content is inserted inside the inner-most wrapper of
     * the entry element.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code timeText}, {@code isStart} and more, see the
     * library docs. It returns a text, or {@code {html: '...'}} or {@code {domNodes: [...]}}. It is called every time
     * the entry data changes.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/background-event-render-hooks">backgroundEventContent</a>
     */
    @JsonName("backgroundEventContent")
    @JsonConverter(ContentValueConverter.class)
    private ContentValue backgroundEntryContent;

    /**
     * Sets a text as the content, see {@link #getBackgroundEntryContent()}.
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setBackgroundEntryContent(String text) {
        this.backgroundEntryContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content in any form, see {@link #getBackgroundEntryContent()}.
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setBackgroundEntryContent(ContentValue value) {
        this.backgroundEntryContent = value;
    }

    /**
     * A callback that runs right after the element of a background entry has been added to the DOM. It is not called
     * again when the entry data changes. It gets an {@code info} object with {@code event}, {@code timeText},
     * {@code isStart} and more, see the library docs.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/background-event-render-hooks">backgroundEventDidMount</a>
     */
    @JsonName("backgroundEventDidMount")
    private JsCallback backgroundEntryDidMount;

    /**
     * CSS classes for the inner wrapper of a background entry.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code timeText}, {@code isStart} and more, see the
     * library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/background-event-render-hooks">backgroundEventInnerClass</a>
     */
    @JsonName("backgroundEventInnerClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue backgroundEntryInnerClass;

    /**
     * Sets fixed CSS classes, see {@link #getBackgroundEntryInnerClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setBackgroundEntryInnerClass(String classNames) {
        this.backgroundEntryInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getBackgroundEntryInnerClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setBackgroundEntryInnerClass(ClassNameValue value) {
        this.backgroundEntryInnerClass = value;
    }

    /**
     * CSS classes for the title element inside the inner wrapper of a background entry.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code isNarrow} and {@code isShort}, see the
     * library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/background-event-render-hooks">backgroundEventTitleClass</a>
     */
    @JsonName("backgroundEventTitleClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue backgroundEntryTitleClass;

    /**
     * Sets fixed CSS classes, see {@link #getBackgroundEntryTitleClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setBackgroundEntryTitleClass(String classNames) {
        this.backgroundEntryTitleClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getBackgroundEntryTitleClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setBackgroundEntryTitleClass(ClassNameValue value) {
        this.backgroundEntryTitleClass = value;
    }

    /**
     * A callback that runs right before the element of a background entry is removed from the DOM. It gets an
     * {@code info} object with {@code event}, {@code timeText}, {@code isStart} and more, see the library docs.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/background-event-render-hooks">backgroundEventWillUnmount</a>
     */
    @JsonName("backgroundEventWillUnmount")
    private JsCallback backgroundEntryWillUnmount;

    /**
     * CSS classes for the element after the inner wrapper of an entry. Background entries are not affected.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code timeText}, {@code isStart} and more, see the
     * library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/event-render-hooks">eventAfterClass</a>
     */
    @JsonName("eventAfterClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue entryAfterClass;

    /**
     * Sets fixed CSS classes, see {@link #getEntryAfterClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setEntryAfterClass(String classNames) {
        this.entryAfterClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getEntryAfterClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setEntryAfterClass(ClassNameValue value) {
        this.entryAfterClass = value;
    }

    /**
     * CSS classes for the element before the inner wrapper of an entry. Background entries are not affected.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code timeText}, {@code isStart} and more, see the
     * library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/event-render-hooks">eventBeforeClass</a>
     */
    @JsonName("eventBeforeClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue entryBeforeClass;

    /**
     * Sets fixed CSS classes, see {@link #getEntryBeforeClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setEntryBeforeClass(String classNames) {
        this.entryBeforeClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getEntryBeforeClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setEntryBeforeClass(ClassNameValue value) {
        this.entryBeforeClass = value;
    }

    /**
     * CSS classes for the outermost element of an entry. Background entries are not affected.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code timeText}, {@code isStart} and more, see the
     * library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/event-render-hooks">eventClass</a>
     */
    @JsonName("eventClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue entryClass;

    /**
     * Sets fixed CSS classes, see {@link #getEntryClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setEntryClass(String classNames) {
        this.entryClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getEntryClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setEntryClass(ClassNameValue value) {
        this.entryClass = value;
    }

    /**
     * Custom content for entries. Background entries are not affected. The generated content is inserted inside
     * the inner-most wrapper of the entry element.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code timeText}, {@code isStart} and more, see the
     * library docs. It returns a text, or {@code {html: '...'}} or {@code {domNodes: [...]}}. It is called every time
     * the entry data changes.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/event-render-hooks">eventContent</a>
     */
    @JsonName("eventContent")
    @JsonConverter(ContentValueConverter.class)
    private ContentValue entryContent;

    /**
     * Sets a text as the content, see {@link #getEntryContent()}.
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setEntryContent(String text) {
        this.entryContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content in any form, see {@link #getEntryContent()}.
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setEntryContent(ContentValue value) {
        this.entryContent = value;
    }

    /**
     * CSS classes for the inner wrapper of an entry. Background entries are not affected.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code timeText}, {@code isStart} and more, see the
     * library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/event-render-hooks">eventInnerClass</a>
     */
    @JsonName("eventInnerClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue entryInnerClass;

    /**
     * Sets fixed CSS classes, see {@link #getEntryInnerClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setEntryInnerClass(String classNames) {
        this.entryInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getEntryInnerClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setEntryInnerClass(ClassNameValue value) {
        this.entryInnerClass = value;
    }

    /**
     * CSS classes for the time element inside the inner wrapper of an entry. Background entries are not affected.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code isNarrow} and {@code isShort}, see the
     * library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/event-render-hooks">eventTimeClass</a>
     */
    @JsonName("eventTimeClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue entryTimeClass;

    /**
     * Sets fixed CSS classes, see {@link #getEntryTimeClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setEntryTimeClass(String classNames) {
        this.entryTimeClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getEntryTimeClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setEntryTimeClass(ClassNameValue value) {
        this.entryTimeClass = value;
    }

    /**
     * CSS classes for the title element inside the inner wrapper of an entry. Background entries are not affected.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code isNarrow} and {@code isShort}, see the
     * library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/event-render-hooks">eventTitleClass</a>
     */
    @JsonName("eventTitleClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue entryTitleClass;

    /**
     * Sets fixed CSS classes, see {@link #getEntryTitleClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setEntryTitleClass(String classNames) {
        this.entryTitleClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getEntryTitleClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setEntryTitleClass(ClassNameValue value) {
        this.entryTitleClass = value;
    }

    /**
     * A callback that runs right before the element of an entry is removed from the DOM. Background entries are not
     * affected. It gets an {@code info} object with {@code event}, {@code timeText}, {@code isStart} and more, see the
     * library docs.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/event-render-hooks">eventWillUnmount</a>
     */
    @JsonName("eventWillUnmount")
    private JsCallback entryWillUnmount;

    /**
     * CSS classes for the element after the inner wrapper of a list item entry, that is a timed entry in day
     * grid views and an entry in list views. They apply in addition to the general entry classes.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code timeText}, {@code isStart} and more, see the
     * library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/list-item-event-render-hooks">listItemEventAfterClass</a>
     */
    @JsonName("listItemEventAfterClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue listItemEntryAfterClass;

    /**
     * Sets fixed CSS classes, see {@link #getListItemEntryAfterClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setListItemEntryAfterClass(String classNames) {
        this.listItemEntryAfterClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getListItemEntryAfterClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setListItemEntryAfterClass(ClassNameValue value) {
        this.listItemEntryAfterClass = value;
    }

    /**
     * CSS classes for the element before the inner wrapper of a list item entry, that is a timed entry in day
     * grid views and an entry in list views. They apply in addition to the general entry classes.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code timeText}, {@code isStart} and more, see the
     * library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/list-item-event-render-hooks">listItemEventBeforeClass</a>
     */
    @JsonName("listItemEventBeforeClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue listItemEntryBeforeClass;

    /**
     * Sets fixed CSS classes, see {@link #getListItemEntryBeforeClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setListItemEntryBeforeClass(String classNames) {
        this.listItemEntryBeforeClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getListItemEntryBeforeClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setListItemEntryBeforeClass(ClassNameValue value) {
        this.listItemEntryBeforeClass = value;
    }

    /**
     * CSS classes for the outermost element of a list item entry, that is a timed entry in day grid views and an
     * entry in list views. They apply in addition to the general entry classes.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code timeText}, {@code isStart} and more, see the
     * library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/list-item-event-render-hooks">listItemEventClass</a>
     */
    @JsonName("listItemEventClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue listItemEntryClass;

    /**
     * Sets fixed CSS classes, see {@link #getListItemEntryClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setListItemEntryClass(String classNames) {
        this.listItemEntryClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getListItemEntryClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setListItemEntryClass(ClassNameValue value) {
        this.listItemEntryClass = value;
    }

    /**
     * CSS classes for the inner wrapper of a list item entry, that is a timed entry in day grid views and an
     * entry in list views. They apply in addition to the general entry classes.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code timeText}, {@code isStart} and more, see the
     * library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/list-item-event-render-hooks">listItemEventInnerClass</a>
     */
    @JsonName("listItemEventInnerClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue listItemEntryInnerClass;

    /**
     * Sets fixed CSS classes, see {@link #getListItemEntryInnerClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setListItemEntryInnerClass(String classNames) {
        this.listItemEntryInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getListItemEntryInnerClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setListItemEntryInnerClass(ClassNameValue value) {
        this.listItemEntryInnerClass = value;
    }

    /**
     * CSS classes for the time element inside the inner wrapper of a list item entry, that is a timed entry in
     * day grid views and an entry in list views. They apply in addition to the general entry classes.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code isNarrow} and {@code isShort}, see the
     * library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/list-item-event-render-hooks">listItemEventTimeClass</a>
     */
    @JsonName("listItemEventTimeClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue listItemEntryTimeClass;

    /**
     * Sets fixed CSS classes, see {@link #getListItemEntryTimeClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setListItemEntryTimeClass(String classNames) {
        this.listItemEntryTimeClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getListItemEntryTimeClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setListItemEntryTimeClass(ClassNameValue value) {
        this.listItemEntryTimeClass = value;
    }

    /**
     * CSS classes for the title element inside the inner wrapper of a list item entry, that is a timed entry in
     * day grid views and an entry in list views. They apply in addition to the general entry classes.
     * <p>
     * A callback gets an {@code info} object with {@code event}, {@code timeText}, {@code isNarrow} and more, see the
     * library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/list-item-event-render-hooks">listItemEventTitleClass</a>
     */
    @JsonName("listItemEventTitleClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue listItemEntryTitleClass;

    /**
     * Sets fixed CSS classes, see {@link #getListItemEntryTitleClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setListItemEntryTitleClass(String classNames) {
        this.listItemEntryTitleClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getListItemEntryTitleClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setListItemEntryTitleClass(ClassNameValue value) {
        this.listItemEntryTitleClass = value;
    }

    // Render hooks: view, toolbar, buttons, single month

    /**
     * CSS classes for each button of the built-in toolbars (header and footer).
     * <p>
     * A callback gets an {@code info} object with {@code name}, {@code text}, {@code isPrimary} and more, see the
     * library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/toolbar-render-hooks">buttonClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue buttonClass;

    /**
     * Sets fixed CSS classes, see {@link #getButtonClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setButtonClass(String classNames) {
        this.buttonClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getButtonClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setButtonClass(ClassNameValue value) {
        this.buttonClass = value;
    }

    /**
     * CSS classes for each group of buttons in a toolbar section. A group is a set of buttons separated by
     * commas in the toolbar layout.
     * <p>
     * A callback gets an {@code info} object with {@code hasSelection}, which is true when only one button of
     * the group can be active at a time, like view buttons. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/toolbar-render-hooks">buttonGroupClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue buttonGroupClass;

    /**
     * Sets fixed CSS classes, see {@link #getButtonGroupClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setButtonGroupClass(String classNames) {
        this.buttonGroupClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getButtonGroupClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setButtonGroupClass(ClassNameValue value) {
        this.buttonGroupClass = value;
    }

    /**
     * CSS classes for each nav link, the clickable day numbers and weekday names that navigate to another view.
     * <p>
     * It takes class names only, because the library accepts no callback for this option.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/navLinkClass">navLinkClass</a>
     */
    private String navLinkClass;

    /**
     * CSS classes for each single month of multi-month views.
     * <p>
     * A callback gets an {@code info} object with {@code multiMonthColumns} (0 until the layout is measured),
     * {@code isFirst} and {@code isLast}. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/single-month-render-hooks">singleMonthClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue singleMonthClass;

    /**
     * Sets fixed CSS classes, see {@link #getSingleMonthClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setSingleMonthClass(String classNames) {
        this.singleMonthClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getSingleMonthClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setSingleMonthClass(ClassNameValue value) {
        this.singleMonthClass = value;
    }

    /**
     * A callback that runs right after the element of a single month of multi-month views has been added to the
     * DOM. It has no effect in the used library version, because the library never calls it.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/single-month-render-hooks">singleMonthDidMount</a>
     */
    private JsCallback singleMonthDidMount;

    /**
     * CSS classes for the month header (the title above each month grid) of multi-month views.
     * <p>
     * A callback gets an {@code info} object with {@code hasNavLink}, {@code isSticky}, {@code isNarrow} and more, see
     * the library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/single-month-header-render-hooks">singleMonthHeaderClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue singleMonthHeaderClass;

    /**
     * Sets fixed CSS classes, see {@link #getSingleMonthHeaderClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setSingleMonthHeaderClass(String classNames) {
        this.singleMonthHeaderClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getSingleMonthHeaderClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setSingleMonthHeaderClass(ClassNameValue value) {
        this.singleMonthHeaderClass = value;
    }

    /**
     * CSS classes for the inner element of the month header of multi-month views. This element may be a nav link.
     * <p>
     * A callback gets an {@code info} object with {@code hasNavLink}, {@code isSticky}, {@code isNarrow} and more, see
     * the library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/single-month-header-render-hooks">singleMonthHeaderInnerClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue singleMonthHeaderInnerClass;

    /**
     * Sets fixed CSS classes, see {@link #getSingleMonthHeaderInnerClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setSingleMonthHeaderInnerClass(String classNames) {
        this.singleMonthHeaderInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getSingleMonthHeaderInnerClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setSingleMonthHeaderInnerClass(ClassNameValue value) {
        this.singleMonthHeaderInnerClass = value;
    }

    /**
     * A callback that runs right before the element of a single month of multi-month views is removed from the
     * DOM. In the used library version it gets an {@code info} object without data (its {@code el} is undefined),
     * so it cannot reach the element.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered
     * elements, for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/single-month-render-hooks">singleMonthWillUnmount</a>
     */
    private JsCallback singleMonthWillUnmount;

    /**
     * CSS classes for the container of the built-in toolbars (header and footer).
     * <p>
     * A callback gets an {@code info} object with {@code borderlessX}, {@code borderlessTop} and
     * {@code borderlessBottom}. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/toolbar-render-hooks">toolbarClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue toolbarClass;

    /**
     * Sets fixed CSS classes, see {@link #getToolbarClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setToolbarClass(String classNames) {
        this.toolbarClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getToolbarClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setToolbarClass(ClassNameValue value) {
        this.toolbarClass = value;
    }

    /**
     * CSS classes for each section (start, center, end) of the built-in toolbars.
     * <p>
     * A callback gets an {@code info} object with {@code name}, the name of the section. It returns the class
     * names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/toolbar-render-hooks">toolbarSectionClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue toolbarSectionClass;

    /**
     * Sets fixed CSS classes, see {@link #getToolbarSectionClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setToolbarSectionClass(String classNames) {
        this.toolbarSectionClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getToolbarSectionClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setToolbarSectionClass(ClassNameValue value) {
        this.toolbarSectionClass = value;
    }

    /**
     * CSS classes for the title text element of the built-in toolbars.
     * <p>
     * It takes class names only, because the library accepts no callback for this option.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/toolbar-render-hooks">toolbarTitleClass</a>
     */
    private String toolbarTitleClass;

    // Dates, ranges and durations

    /**
     * How the duration of an entry changes when it is dragged between the timed and the all-day section.
     * <p>
     * With {@code true}, the duration stays roughly the same (hourly durations are rounded down to whole days).
     * With {@code false}, the duration is reset to the default all-day or timed entry duration of the target
     * section.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/allDayMaintainDuration">allDayMaintainDuration</a>
     */
    private Boolean allDayMaintainDuration;

    /**
     * Determines the first visible day of a custom view, such as {@code "week"} or {@code "month"}, so the view
     * starts at the start of that interval. Without a value, the library generates it from the duration of the
     * view. The library ignores it when {@code visibleRange} defines the range of the view.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/dateAlignment">dateAlignment</a>
     */
    private String dateAlignment;

    /**
     * How far the calendar navigates when the user presses prev or next, or when the calendar's prev or next
     * method runs. For a custom view with a {@code duration}, that duration is the default.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/dateIncrement">dateIncrement</a>
     */
    @JsonConverter(DurationValueConverter.class)
    private DurationValue dateIncrement;

    /**
     * Sets a time-based duration, see {@link #getDateIncrement()}.
     *
     * @param duration the duration, days count as 24 hours, or null to remove the option
     */
    public void setDateIncrement(Duration duration) {
        this.dateIncrement = duration == null ? null : DurationValue.ofTime(duration);
    }

    /**
     * Sets a calendar-based duration, see {@link #getDateIncrement()}.
     *
     * @param period the years, months and days, not zero, or null to remove the option
     */
    public void setDateIncrement(Period period) {
        this.dateIncrement = period == null ? null : DurationValue.ofCalendar(period);
    }

    /**
     * Sets a duration in the string format {@code [-][d.]hh:mm[:ss[.sss]]}, see {@link #getDateIncrement()}.
     *
     * @param text the duration, such as {@code "08:30"} or {@code "1.12:00"}, or null to remove the option
     */
    public void setDateIncrement(String text) {
        this.dateIncrement = text == null ? null : DurationValue.ofTimeString(text);
    }

    /**
     * Sets the duration in any form, see {@link #getDateIncrement()}.
     *
     * @param value the duration, or null to remove the option
     */
    public void setDateIncrement(DurationValue value) {
        this.dateIncrement = value;
    }

    /**
     * The exact number of days a custom view shows, regardless of {@code weekends} and {@code hiddenDays}. With a
     * {@code duration} instead, hidden days are omitted and the view stretches to fill the space.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/dayCount">dayCount</a>
     */
    private Integer dayCount;

    /**
     * Whether the day headers appear in the month, day grid and time grid views. Set it to {@code false} to hide
     * them, for example in a single-day view.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/dayHeaders">dayHeaders</a>
     */
    private Boolean dayHeaders;

    /**
     * The default value of the all-day flag for entries that do not specify one. Without a value, the library
     * guesses the flag from the entry data.
     * <p>
     * It applies to entries received after the change, for example after navigating to another view or period.
     * Existing entries are not updated.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/defaultAllDay">defaultAllDay</a>
     */
    private Boolean defaultAllDay;

    /**
     * A fallback duration for all-day entries without an end. The end of the entry stays unset unless
     * {@link #getForceEntryDuration()} is {@code true}. For timed entries, see
     * {@link #getDefaultTimedEntryDuration()}.
     * <p>
     * It applies to entries received after the change, for example after navigating to another view or period.
     * Single entries already shown are not updated.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/defaultAllDayEventDuration">defaultAllDayEventDuration</a>
     */
    @JsonConverter(DurationValueConverter.class)
    @JsonName("defaultAllDayEventDuration")
    private DurationValue defaultAllDayEntryDuration;

    /**
     * Sets a time-based duration, see {@link #getDefaultAllDayEntryDuration()}.
     *
     * @param duration the duration, days count as 24 hours, or null to remove the option
     */
    public void setDefaultAllDayEntryDuration(Duration duration) {
        this.defaultAllDayEntryDuration = duration == null ? null : DurationValue.ofTime(duration);
    }

    /**
     * Sets a calendar-based duration, see {@link #getDefaultAllDayEntryDuration()}.
     *
     * @param period the years, months and days, not zero, or null to remove the option
     */
    public void setDefaultAllDayEntryDuration(Period period) {
        this.defaultAllDayEntryDuration = period == null ? null : DurationValue.ofCalendar(period);
    }

    /**
     * Sets a duration in the string format {@code [-][d.]hh:mm[:ss[.sss]]},
     * see {@link #getDefaultAllDayEntryDuration()}.
     *
     * @param text the duration, such as {@code "08:30"} or {@code "1.12:00"}, or null to remove the option
     */
    public void setDefaultAllDayEntryDuration(String text) {
        this.defaultAllDayEntryDuration = text == null ? null : DurationValue.ofTimeString(text);
    }

    /**
     * Sets the duration in any form, see {@link #getDefaultAllDayEntryDuration()}.
     *
     * @param value the duration, or null to remove the option
     */
    public void setDefaultAllDayEntryDuration(DurationValue value) {
        this.defaultAllDayEntryDuration = value;
    }

    /**
     * A fallback duration for timed entries without an end. The end of the entry stays unset unless
     * {@link #getForceEntryDuration()} is {@code true}. For all-day entries, see
     * {@link #getDefaultAllDayEntryDuration()}.
     * <p>
     * It applies to entries received after the change, for example after navigating to another view or period.
     * Single entries already shown are not updated.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/defaultTimedEventDuration">defaultTimedEventDuration</a>
     */
    @JsonConverter(DurationValueConverter.class)
    @JsonName("defaultTimedEventDuration")
    private DurationValue defaultTimedEntryDuration;

    /**
     * Sets a time-based duration, see {@link #getDefaultTimedEntryDuration()}.
     *
     * @param duration the duration, days count as 24 hours, or null to remove the option
     */
    public void setDefaultTimedEntryDuration(Duration duration) {
        this.defaultTimedEntryDuration = duration == null ? null : DurationValue.ofTime(duration);
    }

    /**
     * Sets a calendar-based duration, see {@link #getDefaultTimedEntryDuration()}.
     *
     * @param period the years, months and days, not zero, or null to remove the option
     */
    public void setDefaultTimedEntryDuration(Period period) {
        this.defaultTimedEntryDuration = period == null ? null : DurationValue.ofCalendar(period);
    }

    /**
     * Sets a duration in the string format {@code [-][d.]hh:mm[:ss[.sss]]},
     * see {@link #getDefaultTimedEntryDuration()}.
     *
     * @param text the duration, such as {@code "08:30"} or {@code "1.12:00"}, or null to remove the option
     */
    public void setDefaultTimedEntryDuration(String text) {
        this.defaultTimedEntryDuration = text == null ? null : DurationValue.ofTimeString(text);
    }

    /**
     * Sets the duration in any form, see {@link #getDefaultTimedEntryDuration()}.
     *
     * @param value the duration, or null to remove the option
     */
    public void setDefaultTimedEntryDuration(DurationValue value) {
        this.defaultTimedEntryDuration = value;
    }

    /**
     * The exact duration of a custom view. A duration in weeks, such as one week, aligns the view to the start of
     * the week, a duration of seven days does not.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/duration">duration</a>
     */
    @JsonConverter(DurationValueConverter.class)
    private DurationValue duration;

    /**
     * Sets a time-based duration, see {@link #getDuration()}.
     *
     * @param duration the duration, days count as 24 hours, or null to remove the option
     */
    public void setDuration(Duration duration) {
        this.duration = duration == null ? null : DurationValue.ofTime(duration);
    }

    /**
     * Sets a calendar-based duration, see {@link #getDuration()}.
     *
     * @param period the years, months and days, not zero, or null to remove the option
     */
    public void setDuration(Period period) {
        this.duration = period == null ? null : DurationValue.ofCalendar(period);
    }

    /**
     * Sets a duration in the string format {@code [-][d.]hh:mm[:ss[.sss]]}, see {@link #getDuration()}.
     *
     * @param text the duration, such as {@code "08:30"} or {@code "1.12:00"}, or null to remove the option
     */
    public void setDuration(String text) {
        this.duration = text == null ? null : DurationValue.ofTimeString(text);
    }

    /**
     * Sets the duration in any form, see {@link #getDuration()}.
     *
     * @param value the duration, or null to remove the option
     */
    public void setDuration(DurationValue value) {
        this.duration = value;
    }

    /**
     * Whether a month view always shows six weeks. With {@code false}, it shows four, five or six weeks, depending
     * on the month.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/fixedWeekCount">fixedWeekCount</a>
     */
    private Boolean fixedWeekCount;

    /**
     * Whether the library assigns an end to entries without one, using {@link #getDefaultTimedEntryDuration()} or
     * {@link #getDefaultAllDayEntryDuration()}. With {@code false}, the end of such an entry stays unset.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/forceEventDuration">forceEventDuration</a>
     */
    @JsonName("forceEventDuration")
    private Boolean forceEntryDuration;

    /**
     * The heading level of the title of the library's toolbar in the document outline. The title is rendered as a
     * {@code div} with an {@code aria-level} attribute, so the level does not change how it looks. Set it to match
     * the page around the calendar, so screen readers find the title at the right level.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/headingLevel">headingLevel</a>
     */
    private Integer headingLevel;

    /**
     * The days of the week to hide. Without a value, no day is hidden, unless {@code weekends} is {@code false}.
     * <p>
     * When set, the getter returns an unmodifiable set.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/hiddenDays">hiddenDays</a>
     */
    @JsonConverter(DayOfWeekArrayConverter.class)
    private Set<DayOfWeek> hiddenDays;

    /**
     * Sets the days to hide, see {@link #getHiddenDays()}. The add-on keeps an unmodifiable copy in the order of the
     * week, so a later change of the passed set has no effect.
     *
     * @param hiddenDays the days to hide, or null to remove the option
     */
    public void setHiddenDays(Set<DayOfWeek> hiddenDays) {
        this.hiddenDays = hiddenDays == null ? null
                : hiddenDays.isEmpty() ? Set.of() : Collections.unmodifiableSet(EnumSet.copyOf(hiddenDays));
    }

    /**
     * How far a timed entry must run into the next day before it renders on that day. It applies only to timed
     * entries shown on whole days, such as in day grid, multi-month and list views, and in timeline views with
     * day-sized slots. With a threshold of 9am, an entry from 8pm to 2am appears on one day only.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/nextDayThreshold">nextDayThreshold</a>
     */
    @JsonConverter(DurationValueConverter.class)
    private DurationValue nextDayThreshold;

    /**
     * Sets a time-based duration, see {@link #getNextDayThreshold()}.
     *
     * @param duration the duration, days count as 24 hours, or null to remove the option
     */
    public void setNextDayThreshold(Duration duration) {
        this.nextDayThreshold = duration == null ? null : DurationValue.ofTime(duration);
    }

    /**
     * Sets a calendar-based duration, see {@link #getNextDayThreshold()}.
     *
     * @param period the years, months and days, not zero, or null to remove the option
     */
    public void setNextDayThreshold(Period period) {
        this.nextDayThreshold = period == null ? null : DurationValue.ofCalendar(period);
    }

    /**
     * Sets a duration in the string format {@code [-][d.]hh:mm[:ss[.sss]]}, see {@link #getNextDayThreshold()}.
     *
     * @param text the duration, such as {@code "08:30"} or {@code "1.12:00"}, or null to remove the option
     */
    public void setNextDayThreshold(String text) {
        this.nextDayThreshold = text == null ? null : DurationValue.ofTimeString(text);
    }

    /**
     * Sets the duration in any form, see {@link #getNextDayThreshold()}.
     *
     * @param value the duration, or null to remove the option
     */
    public void setNextDayThreshold(DurationValue value) {
        this.nextDayThreshold = value;
    }

    /**
     * The current date of the calendar, the day it highlights as today. Without a value, the date of the
     * browser is used. It is useful with a time zone other than the one of the browser.
     * <p>
     * A callback runs on the client and returns the date.
     * <p>
     * The library reads a date value only when the browser creates the calendar. So a change on a calendar the
     * browser shows has no effect. A calendar that is detached and attached again (not within one round trip) starts
     * with the current value, or without one if the option was removed. The same applies when the browser creates
     * the calendar again, for example on a refresh with {@code @PreserveOnRefresh}. A callback is read again when
     * the time zone or locale changes.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/now">now</a>
     */
    @JsonConverter(NowValueConverter.class)
    private NowValue now;

    /**
     * Sets a date as the current date, see {@link #getNow()}.
     *
     * @param date the date, or null to remove the option
     */
    public void setNow(LocalDate date) {
        this.now = date == null ? null : NowValue.ofDay(date);
    }

    /**
     * Sets a date and time as the current date, see {@link #getNow()}.
     *
     * @param dateTime the date and time, read as UTC like the start and end of entries, or null to remove the option
     */
    public void setNow(LocalDateTime dateTime) {
        this.now = dateTime == null ? null : NowValue.ofDateTime(dateTime);
    }

    /**
     * Sets the current date in any form, see {@link #getNow()}.
     *
     * @param value the date or callback, or null to remove the option
     */
    public void setNow(NowValue value) {
        this.now = value;
    }

    /**
     * Whether a month view renders the dates of the previous and the next month. Dates that are disabled do not
     * render entries.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/showNonCurrentDates">showNonCurrentDates</a>
     */
    private Boolean showNonCurrentDates;

    /**
     * The date range the user can navigate to and where entries can go. Dates outside of the range are grayed
     * out, entries cannot be dragged or resized into them, and the prev and next buttons are disabled when they
     * would leave the range. One bound may stay open (a {@link DateRange} with a null start or end).
     * <p>
     * A callback gets the start of the current day and returns the range. The library calls it several times per
     * view render, so keep it cheap.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/validRange">validRange</a>
     */
    @JsonConverter(DateRangeValueConverter.class)
    private DateRangeValue validRange;

    /**
     * The exact date range a view shows. The end is exclusive. The library ignores a range with an open bound.
     * It applies only to views that define neither a duration nor a day count.
     * <p>
     * A callback gets the date the view is currently at and returns the range. The library calls it several times
     * per view render, so keep it cheap.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/visibleRange">visibleRange</a>
     */
    @JsonConverter(DateRangeValueConverter.class)
    private DateRangeValue visibleRange;

    /**
     * Whether week numbers are displayed next to each row of days in month and day grid views, and in the top-left
     * corner of time grid views. The week number calculation follows the locale, see {@code weekNumberCalculation}.
     * <p>
     * The library default is {@code false}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/weekNumbers">weekNumbers</a>
     */
    private Boolean weekNumbers;

    // Time grid and slots

    /**
     * Show or hide the all-day row at the top of the calendar. When hidden, all-day entries are not displayed
     * in time grid views.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/allDaySlot">allDaySlot</a>
     */
    private Boolean allDaySlot;

    /**
     * Minimum pixel width of each day cell. When the calendar gets too narrow for it, horizontal scrollbars
     * appear. Applies to vertical resource views, day grid views and time grid views.
     * <p>
     * The option needs the premium {@code scrollgrid} plugin of the library, which only {@code FullCalendarScheduler}
     * loads.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/dayMinWidth">dayMinWidth</a>
     */
    private Integer dayMinWidth;

    /**
     * The pixel width of a day column below which the library switches to "narrow" date text and layouts.
     * Affects views with day columns (day grid, time grid, multi-month). The narrow state shows up as {@code isNarrow}
     * in the {@code info} of related render hooks.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/dayNarrowWidth">dayNarrowWidth</a>
     */
    private Integer dayNarrowWidth;

    /**
     * Expand the rows of a view to fill its height, if they do not take up the entire height by themselves.
     * Applies to time grid and timeline views.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/expandRows">expandRows</a>
     */
    private Boolean expandRows;

    /**
     * Maximum number of month columns the multi-month grid tries to render. Fewer columns are shown if each
     * month would become smaller than {@link #getSingleMonthMinWidth()}. Use {@code 1} for a single column.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/multiMonthMaxColumns">multiMonthMaxColumns</a>
     */
    private Integer multiMonthMaxColumns;

    /**
     * Show a marker for the current time. It repositions itself while the calendar is shown.
     * Applies to time grid and timeline views.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/nowIndicator">nowIndicator</a>
     */
    private Boolean nowIndicator;

    /**
     * Whether the now indicator aligns with the start of its slot.
     * <p>
     * With {@link AutoBoolean#AUTO}, the indicator snaps to the slot when the slot unit is a day or larger or its
     * value is 1. Otherwise it shows the exact current time. With {@link AutoBoolean#TRUE}, it always snaps to the
     * slot. With {@link AutoBoolean#FALSE}, it always shows the exact current time.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/nowIndicatorSnap">nowIndicatorSnap</a>
     */
    @JsonConverter(AutoBooleanConverter.class)
    private AutoBoolean nowIndicatorSnap;

    /**
     * Initial scroll position of the view, as time of day. The user can still scroll back to
     * earlier times. Use {@link #getSlotMinTime()} to prevent that.
     * <p>
     * The position is applied again whenever the date range changes, unless {@link #getScrollTimeReset()} is
     * {@code false}.
     * <p>
     * A change on an attached calendar does not scroll it at once. The new value applies from the next change of the
     * date range.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/scrollTime">scrollTime</a>
     */
    @JsonConverter(DurationValueConverter.class)
    private DurationValue scrollTime;

    /**
     * Sets a time-based duration, see {@link #getScrollTime()}.
     *
     * @param duration the duration, or null to remove the option
     */
    public void setScrollTime(Duration duration) {
        this.scrollTime = duration == null ? null : DurationValue.ofTime(duration);
    }

    /**
     * Sets a calendar-based duration in years, months and days, see {@link #getScrollTime()}.
     *
     * @param period the years, months and days, not zero, or null to remove the option
     */
    public void setScrollTime(Period period) {
        this.scrollTime = period == null ? null : DurationValue.ofCalendar(period);
    }

    /**
     * Sets the duration from the library's string format, such as {@code "01:00"}.
     * See {@link #getScrollTime()}.
     *
     * @param text the duration, or null to remove the option
     */
    public void setScrollTime(String text) {
        this.scrollTime = text == null ? null : DurationValue.ofTimeString(text);
    }

    /**
     * Sets the duration in any form, see {@link #getScrollTime()}.
     *
     * @param value the duration, or null to remove the option
     */
    public void setScrollTime(DurationValue value) {
        this.scrollTime = value;
    }

    /**
     * Whether the view scrolls to {@link #getScrollTime()} every time the date range changes, via the API or by the
     * user navigating.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/scrollTimeReset">scrollTimeReset</a>
     */
    private Boolean scrollTimeReset;

    /**
     * Minimum pixel width of each mini-month in the multi-month grid, padding included. If the available
     * width would make a month smaller, the months wrap to the next row.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/singleMonthMinWidth">singleMonthMinWidth</a>
     */
    private Integer singleMonthMinWidth;

    /**
     * Duration of each time slot in time grid and timeline views.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/slotDuration">slotDuration</a>
     */
    @JsonConverter(DurationValueConverter.class)
    private DurationValue slotDuration;

    /**
     * Sets a time-based duration, see {@link #getSlotDuration()}.
     *
     * @param duration the duration, or null to remove the option
     */
    public void setSlotDuration(Duration duration) {
        this.slotDuration = duration == null ? null : DurationValue.ofTime(duration);
    }

    /**
     * Sets a calendar-based duration in years, months and days, see {@link #getSlotDuration()}.
     *
     * @param period the years, months and days, not zero, or null to remove the option
     */
    public void setSlotDuration(Period period) {
        this.slotDuration = period == null ? null : DurationValue.ofCalendar(period);
    }

    /**
     * Sets the duration from the library's string format, such as {@code "01:00"}.
     * See {@link #getSlotDuration()}.
     *
     * @param text the duration, or null to remove the option
     */
    public void setSlotDuration(String text) {
        this.slotDuration = text == null ? null : DurationValue.ofTimeString(text);
    }

    /**
     * Sets the duration in any form, see {@link #getSlotDuration()}.
     *
     * @param value the duration, or null to remove the option
     */
    public void setSlotDuration(DurationValue value) {
        this.slotDuration = value;
    }

    /**
     * Whether timed entries in time grid views visually overlap. With {@code true}, at most half of each entry
     * is obscured. With {@code false}, entries never overlap.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/slotEventOverlap">slotEventOverlap</a>
     */
    @JsonName("slotEventOverlap")
    private Boolean slotEntryOverlap;

    /**
     * Horizontal text alignment of the slot headers of timeline views, which the Scheduler provides. It accepts the
     * same values as {@link #getDayHeaderAlign()}.
     * <p>
     * A callback gets an {@code info} object with {@code level} and {@code isTime}. It returns {@code "start"},
     * {@code "center"} or {@code "end"}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/slot-header-render-hooks">slotHeaderAlign</a>
     */
    @JsonConverter(HeaderAlignValueConverter.class)
    private HeaderAlignValue slotHeaderAlign;

    /**
     * Interval at which time slots are labeled with a header text, such as one hour to show headers on the hour
     * marks even if {@link #getSlotDuration()} is 15 or 30 minutes.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/slotHeaderInterval">slotHeaderInterval</a>
     */
    @JsonConverter(DurationValueConverter.class)
    private DurationValue slotHeaderInterval;

    /**
     * Sets a time-based duration, see {@link #getSlotHeaderInterval()}.
     *
     * @param duration the duration, or null to remove the option
     */
    public void setSlotHeaderInterval(Duration duration) {
        this.slotHeaderInterval = duration == null ? null : DurationValue.ofTime(duration);
    }

    /**
     * Sets a calendar-based duration in years, months and days, see {@link #getSlotHeaderInterval()}.
     *
     * @param period the years, months and days, not zero, or null to remove the option
     */
    public void setSlotHeaderInterval(Period period) {
        this.slotHeaderInterval = period == null ? null : DurationValue.ofCalendar(period);
    }

    /**
     * Sets the duration from the library's string format, such as {@code "01:00"}.
     * See {@link #getSlotHeaderInterval()}.
     *
     * @param text the duration, or null to remove the option
     */
    public void setSlotHeaderInterval(String text) {
        this.slotHeaderInterval = text == null ? null : DurationValue.ofTimeString(text);
    }

    /**
     * Sets the duration in any form, see {@link #getSlotHeaderInterval()}.
     *
     * @param value the duration, or null to remove the option
     */
    public void setSlotHeaderInterval(DurationValue value) {
        this.slotHeaderInterval = value;
    }

    /**
     * Sticky positioning of the header rows above the lowest one in the slot headers of timeline views, which the
     * Scheduler provides. With {@link StickyValue#STICKY} their text stays visible at the start and end edge of the
     * horizontal scroll area, with {@link StickyValue#NOT_STICKY} it scrolls away. With an offset, a number or a CSS
     * length, that edge moves inward by the offset.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/slot-header-render-hooks">slotHeaderSticky</a>
     */
    @JsonConverter(StickyValueConverter.class)
    private StickyValue slotHeaderSticky;

    /**
     * Sets the slot headers sticky at an offset from the edge, see {@link #getSlotHeaderSticky()}.
     *
     * @param offset a CSS length, such as {@code "2em"} or {@code "10px"}, or null to remove the option
     */
    public void setSlotHeaderSticky(String offset) {
        this.slotHeaderSticky = offset == null ? null : StickyValue.of(offset);
    }

    /**
     * Sets the sticky positioning of the slot headers in any form, see {@link #getSlotHeaderSticky()}.
     *
     * @param value the positioning, or null to remove the option
     */
    public void setSlotHeaderSticky(StickyValue value) {
        this.slotHeaderSticky = value;
    }

    /**
     * Last time slot displayed for each day, as an exclusive end time. {@code "24:00:00"} ends at midnight. The slot
     * limit also applies when the view is scrolled back all the way.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/slotMaxTime">slotMaxTime</a>
     */
    @JsonConverter(DurationValueConverter.class)
    private DurationValue slotMaxTime;

    /**
     * Sets a time-based duration, see {@link #getSlotMaxTime()}.
     *
     * @param duration the duration, or null to remove the option
     */
    public void setSlotMaxTime(Duration duration) {
        this.slotMaxTime = duration == null ? null : DurationValue.ofTime(duration);
    }

    /**
     * Sets a calendar-based duration in years, months and days, see {@link #getSlotMaxTime()}.
     *
     * @param period the years, months and days, not zero, or null to remove the option
     */
    public void setSlotMaxTime(Period period) {
        this.slotMaxTime = period == null ? null : DurationValue.ofCalendar(period);
    }

    /**
     * Sets the duration from the library's string format, such as {@code "01:00"}.
     * See {@link #getSlotMaxTime()}.
     *
     * @param text the duration, or null to remove the option
     */
    public void setSlotMaxTime(String text) {
        this.slotMaxTime = text == null ? null : DurationValue.ofTimeString(text);
    }

    /**
     * Sets the duration in any form, see {@link #getSlotMaxTime()}.
     *
     * @param value the duration, or null to remove the option
     */
    public void setSlotMaxTime(DurationValue value) {
        this.slotMaxTime = value;
    }

    /**
     * Height of each slot of the time axis, in pixels. Without it, a value is computed.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/slotMinHeight">slotMinHeight</a>
     */
    private Integer slotMinHeight;

    /**
     * First time slot displayed for each day. {@code "00:00:00"} starts at midnight. The slot limit also applies when
     * the view is scrolled back all the way.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/slotMinTime">slotMinTime</a>
     */
    @JsonConverter(DurationValueConverter.class)
    private DurationValue slotMinTime;

    /**
     * Sets a time-based duration, see {@link #getSlotMinTime()}.
     *
     * @param duration the duration, or null to remove the option
     */
    public void setSlotMinTime(Duration duration) {
        this.slotMinTime = duration == null ? null : DurationValue.ofTime(duration);
    }

    /**
     * Sets a calendar-based duration in years, months and days, see {@link #getSlotMinTime()}.
     *
     * @param period the years, months and days, not zero, or null to remove the option
     */
    public void setSlotMinTime(Period period) {
        this.slotMinTime = period == null ? null : DurationValue.ofCalendar(period);
    }

    /**
     * Sets the duration from the library's string format, such as {@code "01:00"}.
     * See {@link #getSlotMinTime()}.
     *
     * @param text the duration, or null to remove the option
     */
    public void setSlotMinTime(String text) {
        this.slotMinTime = text == null ? null : DurationValue.ofTimeString(text);
    }

    /**
     * Sets the duration in any form, see {@link #getSlotMinTime()}.
     *
     * @param value the duration, or null to remove the option
     */
    public void setSlotMinTime(DurationValue value) {
        this.slotMinTime = value;
    }

    /**
     * Time interval to which a dragged entry snaps on the time axis. Also sets the granularity of selections.
     * Without a value, it is the same as {@link #getSlotDuration()}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/snapDuration">snapDuration</a>
     */
    @JsonConverter(DurationValueConverter.class)
    private DurationValue snapDuration;

    /**
     * Sets a time-based duration, see {@link #getSnapDuration()}.
     *
     * @param duration the duration, or null to remove the option
     */
    public void setSnapDuration(Duration duration) {
        this.snapDuration = duration == null ? null : DurationValue.ofTime(duration);
    }

    /**
     * Sets a calendar-based duration in years, months and days, see {@link #getSnapDuration()}.
     *
     * @param period the years, months and days, not zero, or null to remove the option
     */
    public void setSnapDuration(Period period) {
        this.snapDuration = period == null ? null : DurationValue.ofCalendar(period);
    }

    /**
     * Sets the duration from the library's string format, such as {@code "01:00"}.
     * See {@link #getSnapDuration()}.
     *
     * @param text the duration, or null to remove the option
     */
    public void setSnapDuration(String text) {
        this.snapDuration = text == null ? null : DurationValue.ofTimeString(text);
    }

    /**
     * Sets the duration in any form, see {@link #getSnapDuration()}.
     *
     * @param value the duration, or null to remove the option
     */
    public void setSnapDuration(DurationValue value) {
        this.snapDuration = value;
    }

    // Interaction

    /**
     * Time in milliseconds an entry takes to move back to its original position after an unsuccessful drag.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/dragRevertDuration">dragRevertDuration</a>
     */
    private Integer dragRevertDuration;

    /**
     * Whether the scroll containers scroll automatically while the user drags entries or selects dates,
     * once the mouse gets close to the edge.
     * <p>
     * A change on an attached calendar takes effect when the view is rendered again, for example after a view
     * change.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/dragScroll">dragScroll</a>
     */
    private Boolean dragScroll;

    /**
     * Determines whether external elements or entries from other calendars can be dropped onto the calendar.
     * It has to be enabled to receive drops from external elements.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/droppable">droppable</a>
     */
    private Boolean droppable;

    /**
     * Filters which external elements can be dropped onto the calendar. It takes effect when {@link #getDroppable()}
     * is true.
     * <p>
     * A callback gets the dragged item as its argument and returns true if the item can be dropped.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/dropAccept">dropAccept</a>
     */
    @JsonConverter(CssSelectorValueConverter.class)
    private CssSelectorValue dropAccept;

    /**
     * Accepts the external elements that match a CSS selector, see {@link #getDropAccept()}.
     *
     * @param selector the CSS selector, such as {@code ".cool-entry"}, or null to remove the option
     */
    public void setDropAccept(String selector) {
        this.dropAccept = selector == null ? null : CssSelector.of(selector);
    }

    /**
     * Sets the filter for external elements in any form, see {@link #getDropAccept()}.
     *
     * @param value the selector or callback, or null to remove the option
     */
    public void setDropAccept(CssSelectorValue value) {
        this.dropAccept = value;
    }

    /**
     * Number of pixels the pointer must move before an entry drag or resize starts. It applies to dragging with the
     * mouse and to resizing. Dragging on touch ignores it. There, {@link #getEntryLongPressDelay()} applies.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/eventDragMinDistance">eventDragMinDistance</a>
     */
    @JsonName("eventDragMinDistance")
    private Integer entryDragMinDistance;

    /**
     * Whether all entries are focusable and tabbable for keyboard access. Without a value, every entry is focusable,
     * because the add-on registers an entry click handler. {@code false} turns that off for all entries.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/eventInteractive">eventInteractive</a>
     */
    @JsonName("eventInteractive")
    private Boolean entryInteractive;

    /**
     * Time in milliseconds the user must hold down on a touch device before an entry becomes draggable.
     * Without it, {@link #getLongPressDelay()} applies. It has no effect on non-touch devices.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/eventLongPressDelay">eventLongPressDelay</a>
     */
    @JsonName("eventLongPressDelay")
    private Integer entryLongPressDelay;

    /**
     * Whether entries that are dragged or resized may overlap other entries. Background entries count like normal
     * entries.
     * <p>
     * A callback runs for every pair of intersecting entries and returns true if the overlap is allowed. It gets the
     * entry underneath and the entry being moved, which still has its original start and end.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/eventOverlap">eventOverlap</a>
     */
    @JsonName("eventOverlap")
    @JsonConverter(OverlapValueConverter.class)
    private OverlapValue entryOverlap;

    /**
     * Sets whether dragged or resized entries may overlap entries, see {@link #getEntryOverlap()}.
     *
     * @param allowed true to allow the overlap, false to forbid it
     */
    public void setEntryOverlap(boolean allowed) {
        this.entryOverlap = Overlap.of(allowed);
    }

    /**
     * Sets whether dragged or resized entries may overlap entries in any form, see {@link #getEntryOverlap()}.
     *
     * @param value the overlap setting or callback, or null to remove the option
     */
    public void setEntryOverlap(OverlapValue value) {
        this.entryOverlap = value;
    }

    /**
     * Whether the user can resize entries from their start edge.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/eventResizableFromStart">eventResizableFromStart</a>
     */
    @JsonName("eventResizableFromStart")
    private Boolean entryResizableFromStart;

    /**
     * Time in milliseconds a touch user must hold down before an entry can be dragged or a date selected.
     * {@link #getEntryLongPressDelay()} and {@link #getSelectLongPressDelay()} override it for one of the two.
     * It has no effect on non-touch devices.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/longPressDelay">longPressDelay</a>
     */
    private Integer longPressDelay;

    /**
     * Whether day and week labels (day numbers, day headers, week numbers) are links. The add-on handles a click
     * itself and sends it to the server as a {@link DayNumberClickedEvent} or a {@link WeekNumberClickedEvent}.
     * The calendar does not change the view on its own.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/navLinks">navLinks</a>
     */
    private Boolean navLinks;

    /**
     * Whether the user can select days or time slots by clicking and dragging. A selection fires a
     * {@link TimeslotsSelectedEvent} on the server.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/selectable">selectable</a>
     */
    private Boolean selectable;

    /**
     * Time in milliseconds a touch user must hold down before a date becomes selectable.
     * Without it, {@link #getLongPressDelay()} applies. It has no effect on non-touch devices.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/selectLongPressDelay">selectLongPressDelay</a>
     */
    private Integer selectLongPressDelay;

    /**
     * Number of pixels the mouse must travel after a mouse down before a selection starts. 0 puts no
     * restriction on the distance. A higher value tells a selection from a date click. It applies to the mouse only.
     * <p>
     * A change on an attached calendar takes effect when the view is rendered again, for example after a view
     * change.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/selectMinDistance">selectMinDistance</a>
     */
    private Integer selectMinDistance;

    /**
     * Whether a placeholder entry is drawn while the user drags a selection, instead of the standard highlighting
     * of each cell. It applies to time grid views only.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/selectMirror">selectMirror</a>
     */
    private Boolean selectMirror;

    /**
     * Whether the user can select periods of time that are occupied by entries. It applies when
     * {@link #getSelectable()} is true.
     * <p>
     * A callback runs for every entry the selection intersects and returns true to allow the selection. It gets the
     * intersected entry itself, not wrapped in an info object.
     * <p>
     * An all-day entry counts as intersecting in time grid views, although it does not visually overlap the time
     * slots.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/selectOverlap">selectOverlap</a>
     */
    @JsonConverter(OverlapValueConverter.class)
    private OverlapValue selectOverlap;

    /**
     * Sets whether a selection may overlap entries, see {@link #getSelectOverlap()}.
     *
     * @param allowed true to allow the overlap, false to forbid it
     */
    public void setSelectOverlap(boolean allowed) {
        this.selectOverlap = Overlap.of(allowed);
    }

    /**
     * Sets whether a selection may overlap entries in any form, see {@link #getSelectOverlap()}.
     *
     * @param value the overlap setting or callback, or null to remove the option
     */
    public void setSelectOverlap(OverlapValue value) {
        this.selectOverlap = value;
    }

    /**
     * Whether clicking elsewhere on the page clears the current selection. It applies when
     * {@link #getSelectable()} is true.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/unselectAuto">unselectAuto</a>
     */
    private Boolean unselectAuto;

    /**
     * CSS selector for elements that do not clear the current selection when clicked, see
     * {@link #getUnselectAuto()}. Useful for a form that opens after a selection.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/unselectCancel">unselectCancel</a>
     */
    private String unselectCancel;

    // Entry display and layout

    /**
     * The width-to-height ratio of the calendar. The calendar fills its available width, and this ratio sets its
     * height, so a larger number makes a smaller height. It applies to the content area. The library default is 1.35.
     * <p>
     * It is used only while neither {@code height} nor a {@link #getContentHeight() content height} is set. The
     * add-on sets the calendar to full height by default.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/aspectRatio">aspectRatio</a>
     */
    private Double aspectRatio;

    /**
     * Default color for all background entries, a CSS color such as {@code "#ff9f89"}. FullCalendar sets it on each
     * background entry's outermost element as the CSS variable {@code --fc-event-color}, so own styles can read it.
     * <p>
     * The color of an entry or of its entry source takes precedence, see {@link Entry#setColor(String)}. For foreground
     * entries use {@link #getEntryColor() entryColor}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/backgroundEventColor">backgroundEventColor</a>
     */
    @JsonName("backgroundEventColor")
    private String backgroundEntryColor;

    /**
     * Whether the calendar removes all its outer borders. Use {@code borderlessTop}, {@code borderlessBottom} or
     * {@code borderlessX} to remove only some of them.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/borderless">borderless</a>
     */
    private Boolean borderless;

    /**
     * Whether the calendar removes only its bottom outer border.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/borderless">borderlessBottom</a>
     */
    private Boolean borderlessBottom;

    /**
     * Whether the calendar removes only its top outer border.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/borderless">borderlessTop</a>
     */
    private Boolean borderlessTop;

    /**
     * Whether the calendar removes its left and right outer borders. It also indents the built-in toolbar to align with
     * the calendar body.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/borderless">borderlessX</a>
     */
    private Boolean borderlessX;

    /**
     * The time slots the calendar emphasizes as business hours. {@link BusinessHoursValue#DEFAULT} uses the library's
     * default, Monday to Friday from 9am to 5pm. Own hours come from {@link BusinessHoursValue#of(BusinessHours...)}.
     * {@link BusinessHoursValue#NONE} switches the emphasis off, which is also the library's default.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/businessHours">businessHours</a>
     */
    @JsonConverter(BusinessHoursValueConverter.class)
    private BusinessHoursValue businessHours;

    /**
     * Sets own business hours, see {@link #getBusinessHours()}.
     *
     * @param hours the business hours, at least one, or null to remove the option
     */
    public void setBusinessHours(BusinessHours... hours) {
        this.businessHours = hours == null ? null : BusinessHoursValue.of(hours);
    }

    /**
     * Sets the business hours in any form, see {@link #getBusinessHours()}.
     *
     * @param value the business hours, {@link BusinessHoursValue#DEFAULT} or {@link BusinessHoursValue#NONE}, or null
     *        to remove the option
     */
    public void setBusinessHours(BusinessHoursValue value) {
        this.businessHours = value;
    }

    /**
     * Sets the color scheme of this calendar only, {@code "light"}, {@code "dark"} or another value a theme
     * understands.
     * <p>
     * Without this option, the FullCalendar Vaadin theme follows the CSS color-scheme of the page, which
     * {@code Page#setColorScheme} sets. A stock theme follows only a {@code data-color-scheme="dark"} attribute on
     * {@code <html>}, the body or another ancestor of the calendar, not {@code Page#setColorScheme}.
     * <p>
     * With Lumo, the option cannot make a calendar light while the application is dark. Lumo's dark mode sets fixed
     * dark colors on the page, and the calendar inherits them. A dark calendar in a light Lumo application works.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://github.com/stefanuebe/vaadin-fullcalendar/wiki/Themes#color-scheme">
     *     Themes: Color scheme</a>
     * @see <a href="https://fullcalendar.io/docs/color-palettes">colorScheme</a>
     */
    private String colorScheme;

    /**
     * Whether the title of column entries sticks to the top edge of the visible area while the user scrolls vertically
     * past a long timed entry, so the title stays readable. Themes can change the default.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/column-event-render-hooks">columnEventTitleSticky</a>
     */
    @JsonName("columnEventTitleSticky")
    private Boolean columnEntryTitleSticky;

    /**
     * The height of the view area of the calendar. Without a value, the calendar height follows the
     * {@link #getAspectRatio() aspect ratio}, as long as no {@code height} is set.
     * <p>
     * A pixel height is exact, and the view scrolls when its content does not fit. {@link HeightValue#AUTO} gives the
     * view the natural height of its content, without scrollbars. A CSS length such as {@code "50vh"} sets the height
     * in that unit.
     * <p>
     * A pixel or CSS value has no effect while {@code height} is set, which the add-on does by default (full
     * height). {@link HeightValue#AUTO} still works.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/contentHeight">contentHeight</a>
     */
    @JsonConverter(HeightValueConverter.class)
    private HeightValue contentHeight;

    /**
     * Sets the height in pixels, see {@link #getContentHeight()}.
     *
     * @param pixels the height in pixels, not negative
     */
    public void setContentHeight(int pixels) {
        this.contentHeight = HeightValue.ofPixels(pixels);
    }

    /**
     * Sets the height as a CSS length, see {@link #getContentHeight()}.
     *
     * @param length a CSS length such as {@code "50vh"}, or null to remove the option
     */
    public void setContentHeight(String length) {
        this.contentHeight = length == null ? null : HeightValue.ofCss(length);
    }

    /**
     * Sets the height in any form, see {@link #getContentHeight()}.
     *
     * @param value the height, {@link HeightValue#AUTO} for the natural height of the content, or null to remove the
     *        option
     */
    public void setContentHeight(HeightValue value) {
        this.contentHeight = value;
    }

    /**
     * The maximum number of entries in a day cell of day grid views, not counting the "+N more" link. The rest is shown
     * in a popover. For time grid and timeline views use {@link #getEntryMaxStack() entryMaxStack}.
     * <p>
     * A number limits it to that many. {@link EntryLimitValue#CELL_HEIGHT} limits it to what fits into the height of
     * the cell. {@link EntryLimitValue#NONE} shows all entries, which is the library's default.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/dayMaxEvents">dayMaxEvents</a>
     */
    @JsonConverter(EntryLimitValueConverter.class)
    @JsonName("dayMaxEvents")
    private EntryLimitValue dayMaxEntries;

    /**
     * Sets a fixed limit, see {@link #getDayMaxEntries()}.
     *
     * @param count the number, not negative
     */
    public void setDayMaxEntries(int count) {
        this.dayMaxEntries = EntryLimitValue.of(count);
    }

    /**
     * Sets the limit in any form, see {@link #getDayMaxEntries()}.
     *
     * @param value the limit, or null to remove the option
     */
    public void setDayMaxEntries(EntryLimitValue value) {
        this.dayMaxEntries = value;
    }

    /**
     * The maximum number of stacked entry levels in a day cell of day grid views, including the "+N more" link. The
     * rest is shown in a popover. For time grid and timeline views use {@link #getEntryMaxStack() entryMaxStack}.
     * <p>
     * A number limits it to that many. {@link EntryLimitValue#CELL_HEIGHT} limits it to what fits into the height of
     * the cell. {@link EntryLimitValue#NONE} shows all entries, which is the library's default.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/dayMaxEventRows">dayMaxEventRows</a>
     */
    @JsonConverter(EntryLimitValueConverter.class)
    @JsonName("dayMaxEventRows")
    private EntryLimitValue dayMaxEntryRows;

    /**
     * Sets a fixed limit, see {@link #getDayMaxEntryRows()}.
     *
     * @param count the number, not negative
     */
    public void setDayMaxEntryRows(int count) {
        this.dayMaxEntryRows = EntryLimitValue.of(count);
    }

    /**
     * Sets the limit in any form, see {@link #getDayMaxEntryRows()}.
     *
     * @param value the limit, or null to remove the option
     */
    public void setDayMaxEntryRows(EntryLimitValue value) {
        this.dayMaxEntryRows = value;
    }

    /**
     * Whether the calendar shows the end time of an entry. An entry without an end, or a calendar that does not show
     * the time ({@link #getDisplayEntryTime() displayEntryTime}), shows no end time regardless. Without a value, the
     * view decides. It shows no end time in the month and week day grid views, and an end time in the time grid
     * views and the day grid day view.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/displayEventEnd">displayEventEnd</a>
     */
    @JsonName("displayEventEnd")
    private Boolean displayEntryEnd;

    /**
     * Whether the calendar shows the time text of an entry. It applies to entries that have times only. All-day entries
     * show no time text anyway. Without a value, the view decides.
     * The time shows in all day grid and time grid views. In timeline views it depends on the duration.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/displayEventTime">displayEventTime</a>
     */
    @JsonName("displayEventTime")
    private Boolean displayEntryTime;

    /**
     * The color for all foreground entries, any CSS color such as {@code "#378006"}. It does not apply to background
     * entries, use {@link #getBackgroundEntryColor() backgroundEntryColor} for them. FullCalendar sets the color on
     * each entry's element as the CSS variable {@code --fc-event-color}. The theme's styles read it, so where the
     * color shows depends on the theme. Own styles can read the variable as well.
     * <p>
     * The color of an entry or of its entry source takes precedence, see {@link Entry#setColor(String)}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/eventColor">eventColor</a>
     */
    @JsonName("eventColor")
    private String entryColor;

    /**
     * The contrast color for all foreground entries, any CSS color. It colors the text and other elements that have to
     * be visible against the background of the entry. FullCalendar sets it on each entry's element as the CSS variable
     * {@code --fc-event-contrast-color}, which the theme's styles and own styles can read.
     * <p>
     * The color of an entry or of its entry source takes precedence, see {@link Entry#setContrastColor(String)}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/eventContrastColor">eventContrastColor</a>
     */
    @JsonName("eventContrastColor")
    private String entryContrastColor;

    /**
     * The maximum number of entries that stack top to bottom in timeline views, and left to right in time grid views.
     * When more entries overlap, a more link stands for the hidden ones and opens a popover with them by default.
     * Without a value, all entries show. For day grid views use
     * {@link #getDayMaxEntryRows() dayMaxEntryRows} or {@link #getDayMaxEntries() dayMaxEntries}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/eventMaxStack">eventMaxStack</a>
     */
    @JsonName("eventMaxStack")
    private Integer entryMaxStack;

    /**
     * The minimum height in pixels an entry may have in time grid views. The library default is 15.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/eventMinHeight">eventMinHeight</a>
     */
    @JsonName("eventMinHeight")
    private Integer entryMinHeight;

    /**
     * The order of entries within the same day. In time grid views it is the horizontal order, otherwise the vertical
     * order. The library default is {@code "start,-duration,allDay,title"}: earlier entries first, then longer ones,
     * then all-day ones, then by title.
     * <p>
     * It takes a list of entry property names, where a leading {@code -} sorts descending, a mix with compare functions
     * ({@link EntryOrder}), or a single {@link JsCallback} that compares two entries. If a lower ranked entry fits into
     * the layout more compactly, the library places it earlier, unless {@link #getEntryOrderStrict() entryOrderStrict}
     * is set.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/eventOrder">entryOrder</a>
     */
    @JsonConverter(EntryOrderValueConverter.class)
    @JsonName("eventOrder")
    private EntryOrderValue entryOrder;

    /**
     * Sets the order from entry property names, see {@link #getEntryOrder()}.
     *
     * @param properties the names, such as {@code "title"} or {@code "-start"} for descending, or null to remove the
     *        option
     */
    public void setEntryOrder(String... properties) {
        this.entryOrder = properties == null ? null : EntryOrder.of(properties);
    }

    /**
     * Sets the order from sort keys, which may mix properties and compare functions, see {@link #getEntryOrder()}.
     *
     * @param keys the sort keys, at least one, or null to remove the option
     */
    public void setEntryOrder(EntryOrder.Key... keys) {
        this.entryOrder = keys == null ? null : EntryOrder.of(keys);
    }

    /**
     * Sets the order in any form, see {@link #getEntryOrder()}.
     *
     * @param value the order or a compare callback, or null to remove the option
     */
    public void setEntryOrder(EntryOrderValue value) {
        this.entryOrder = value;
    }

    /**
     * Whether the calendar follows {@link #getEntryOrder() entryOrder} strictly. Without it, the calendar prefers
     * compactness over the exact order.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/eventOrderStrict">eventOrderStrict</a>
     */
    @JsonName("eventOrderStrict")
    private Boolean entryOrderStrict;

    /**
     * The height threshold in pixels for the "short" style of an entry in time grid views. The library default is 30.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/eventShortHeight">eventShortHeight</a>
     */
    @JsonName("eventShortHeight")
    private Integer entryShortHeight;

    /**
     * Whether day grid views may visually split a multi-day entry around a "+more" link. When a day cell overflows (see
     * {@link #getDayMaxEntries() dayMaxEntries}), a "+more" link replaces the excess entries.
     * <p>
     * With {@code true}, a multi-day entry that spans such a cell is split. The parts before and after the overflowing
     * cell are still shown and the cell itself shows the link. With {@code false}, the multi-day entry is treated as a
     * unit and is hidden completely, absorbed into the "+more" link, if any cell it occupies overflows. The library
     * default is {@code true}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/eventSlicing">eventSlicing</a>
     */
    @JsonName("eventSlicing")
    private Boolean entrySlicing;

    /**
     * Whether the horizontal scrollbar of the view sticks to the bottom of the viewport while the user scrolls, for
     * views that have one, such as timeline views. {@link AutoBoolean#AUTO}, the library default, activates it when the
     * calendar height is automatic and so can be very tall.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/footerScrollbarSticky">footerScrollbarSticky</a>
     */
    @JsonConverter(AutoBooleanConverter.class)
    private AutoBoolean footerScrollbarSticky;

    /**
     * Whether the calendar renders each asynchronous entry source as soon as it has been received ({@code true}, more
     * renders) or waits until all sources have been received and renders them at once ({@code false}, the library
     * default).
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/progressiveEventRendering">progressiveEventRendering</a>
     */
    @JsonName("progressiveEventRendering")
    private Boolean progressiveEntryRendering;

    /**
     * Whether the title of row entries sticks to the start edge of the visible area while the user scrolls horizontally
     * past a long entry, so the title stays readable. Themes can change the default.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/row-event-render-hooks">rowEventTitleSticky</a>
     */
    @JsonName("rowEventTitleSticky")
    private Boolean rowEntryTitleSticky;

    /**
     * Whether the date headers stick to the top of the viewport while the user scrolls. {@link AutoBoolean#AUTO}, the
     * library default, activates it when the calendar height is automatic and so can be very tall.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/tableHeaderSticky">tableHeaderSticky</a>
     */
    @JsonConverter(AutoBooleanConverter.class)
    private AutoBoolean tableHeaderSticky;

    // Texts, hints and formats
}
