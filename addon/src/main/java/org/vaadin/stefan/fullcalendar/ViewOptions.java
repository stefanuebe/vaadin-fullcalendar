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

    // Render hooks: time slots, now indicator, week numbers, tables

    // Render hooks: entry layouts (block, column, row)

    // Render hooks: entries

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
}
