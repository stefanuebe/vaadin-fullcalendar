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
import org.vaadin.stefan.fullcalendar.option.ContentValue;
import org.vaadin.stefan.fullcalendar.option.HeaderAlignValue;
import org.vaadin.stefan.fullcalendar.option.JsCallback;
import org.vaadin.stefan.fullcalendar.option.TextContent;
import lombok.Getter;
import lombok.Setter;
import org.vaadin.stefan.fullcalendar.converters.ClassNameValueConverter;
import org.vaadin.stefan.fullcalendar.converters.ContentValueConverter;
import org.vaadin.stefan.fullcalendar.converters.HeaderAlignValueConverter;
import org.vaadin.stefan.fullcalendar.json.JsonConverter;
import org.vaadin.stefan.fullcalendar.json.JsonName;

import java.io.Serializable;

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
}
