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

    // Render hooks: time slots, now indicator, week numbers, tables

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

    // Render hooks: view, toolbar, buttons, single month
}
