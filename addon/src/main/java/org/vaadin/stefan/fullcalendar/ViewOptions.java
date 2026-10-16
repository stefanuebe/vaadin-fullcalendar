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

    // Render hooks: entries

    // Render hooks: view, toolbar, buttons, single month
}
