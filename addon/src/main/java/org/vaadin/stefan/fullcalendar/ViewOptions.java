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

    // Render hooks: entry layouts (block, column, row)

    // Render hooks: entries

    // Render hooks: view, toolbar, buttons, single month
}
