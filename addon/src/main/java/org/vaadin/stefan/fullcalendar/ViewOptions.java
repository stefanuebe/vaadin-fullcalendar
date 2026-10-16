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

    // Render hooks: time slots, now indicator, week numbers, tables

    // Render hooks: entry layouts (block, column, row)

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
