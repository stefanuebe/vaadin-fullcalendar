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

import lombok.Getter;
import lombok.Setter;
import org.vaadin.stefan.fullcalendar.converters.AutoBooleanConverter;
import org.vaadin.stefan.fullcalendar.converters.BusinessHoursValueConverter;
import org.vaadin.stefan.fullcalendar.converters.ClassNameValueConverter;
import org.vaadin.stefan.fullcalendar.converters.ContentValueConverter;
import org.vaadin.stefan.fullcalendar.converters.CssSelectorValueConverter;
import org.vaadin.stefan.fullcalendar.converters.DateFormatValueConverter;
import org.vaadin.stefan.fullcalendar.converters.DateRangeValueConverter;
import org.vaadin.stefan.fullcalendar.converters.DayOfWeekArrayConverter;
import org.vaadin.stefan.fullcalendar.converters.CalendarDurationConverter;
import org.vaadin.stefan.fullcalendar.converters.EntryLimitValueConverter;
import org.vaadin.stefan.fullcalendar.converters.HeaderAlignValueConverter;
import org.vaadin.stefan.fullcalendar.converters.HeightValueConverter;
import org.vaadin.stefan.fullcalendar.converters.ListDayFormatValueConverter;
import org.vaadin.stefan.fullcalendar.converters.NowValueConverter;
import org.vaadin.stefan.fullcalendar.converters.OrderValueConverter;
import org.vaadin.stefan.fullcalendar.converters.OverlapValueConverter;
import org.vaadin.stefan.fullcalendar.converters.SlotHeaderFormatValueConverter;
import org.vaadin.stefan.fullcalendar.converters.StickyValueConverter;
import org.vaadin.stefan.fullcalendar.converters.TextValueConverter;
import org.vaadin.stefan.fullcalendar.json.JsonConverter;
import org.vaadin.stefan.fullcalendar.json.JsonName;
import org.vaadin.stefan.fullcalendar.option.AutoBoolean;
import org.vaadin.stefan.fullcalendar.option.BusinessHours;
import org.vaadin.stefan.fullcalendar.option.BusinessHoursValue;
import org.vaadin.stefan.fullcalendar.option.ClassName;
import org.vaadin.stefan.fullcalendar.option.ClassNameValue;
import org.vaadin.stefan.fullcalendar.option.ContentValue;
import org.vaadin.stefan.fullcalendar.option.CssSelector;
import org.vaadin.stefan.fullcalendar.option.CssSelectorValue;
import org.vaadin.stefan.fullcalendar.option.DateFormat;
import org.vaadin.stefan.fullcalendar.option.DateFormatValue;
import org.vaadin.stefan.fullcalendar.option.DateRange;
import org.vaadin.stefan.fullcalendar.option.DateRangeValue;
import org.vaadin.stefan.fullcalendar.option.CalendarDuration;
import org.vaadin.stefan.fullcalendar.option.EntryLimitValue;
import org.vaadin.stefan.fullcalendar.option.HeaderAlign;
import org.vaadin.stefan.fullcalendar.option.HeaderAlignValue;
import org.vaadin.stefan.fullcalendar.option.HeightValue;
import org.vaadin.stefan.fullcalendar.option.JsCallback;
import org.vaadin.stefan.fullcalendar.option.ListDayFormatValue;
import org.vaadin.stefan.fullcalendar.option.NativeToolbarButtonDisplay;
import org.vaadin.stefan.fullcalendar.option.NowValue;
import org.vaadin.stefan.fullcalendar.option.Order;
import org.vaadin.stefan.fullcalendar.option.OrderValue;
import org.vaadin.stefan.fullcalendar.option.Overlap;
import org.vaadin.stefan.fullcalendar.option.OverlapValue;
import org.vaadin.stefan.fullcalendar.option.SlotHeaderFormatValue;
import org.vaadin.stefan.fullcalendar.option.StickyValue;
import org.vaadin.stefan.fullcalendar.option.TextContent;
import org.vaadin.stefan.fullcalendar.option.TextValue;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
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
     * can not be dragged or resized. Individual entries can override it with {@link Entry#setEditable(Boolean)}. To
     * allow only one of them use {@link CalendarOptions#getEntryStartEditable() entryStartEditable} or
     * {@link CalendarOptions#getEntryDurationEditable() entryDurationEditable}.
     * <p>
     * The library default is {@code false}. The getter returns null while the option is not set; then the library
     * default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/editable">editable</a>
     */
    private Boolean editable;

    /**
     * Whether Saturday and Sunday columns are included in the calendar views.
     * <p>
     * The library default is {@code true}.
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
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setDayCellClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: stable classes such as {@code fc-daygrid-day} and
     *                                  {@code fc-day-today}, and {@code fc-popover-body} in the popover.<br>
     *                                  Classes set here are added to them.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code date}, {@code text}, {@code isPast}, {@code isFuture},
     *                                  {@code isToday}, {@code isOther} and more.<br>
     *                                  See the library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setDayCellClass("my-day");
     * calendar.options().setDayCellClass(JsCallback.of("info => info.isToday ? 'my-today' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/day-cell-render-hooks">dayCellClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue dayCellClass;

    /**
     * Sets fixed CSS classes for day cells, see {@link #getDayCellClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setDayCellClass(String classNames) {
        this.dayCellClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for day cells in any form, see {@link #getDayCellClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setDayCellClass(ClassNameValue value) {
        this.dayCellClass = value;
    }

    /**
     * Content for the cell in the header area of time grid views that labels the all-day section.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link TextContent}: shown as text.
     *                                  <p>{@link ContentValue#ofHtml(String)}: HTML, not escaped.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setAllDayHeaderContent(String)}: a text, as {@link TextContent}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: the text of the locale for the all-day section, "All-day" in
     *                                  English.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code text}, {@code isNarrow} and {@code view}.<br>
     *                                  It returns a text, {@code {html: '...'}} or {@code {domNodes: [...]}}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setAllDayHeaderContent("Whole day");
     * calendar.options().setAllDayHeaderContent(ContentValue.ofHtml("<b>All day</b>"));
     * calendar.options().setAllDayHeaderContent(JsCallback.of("info => info.isNarrow ? 'All' : info.text"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/all-day-header-render-hooks">allDayHeaderContent</a>
     */
    @JsonConverter(ContentValueConverter.class)
    private ContentValue allDayHeaderContent;

    /**
     * Sets a text as the content of the all-day header cell, see {@link #getAllDayHeaderContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setAllDayHeaderContent(String text) {
        this.allDayHeaderContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content of the all-day header cell in any form, see {@link #getAllDayHeaderContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setAllDayHeaderContent(ContentValue value) {
        this.allDayHeaderContent = value;
    }

    /**
     * Horizontal text alignment of the day header cells in day grid and time grid views, and in the header of the entry
     * popover. In a left-to-right locale, start means left and end means right.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link HeaderAlign#START}, {@link HeaderAlign#CENTER} or
     *                                  {@link HeaderAlign#END}: a fixed alignment.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: {@link HeaderAlign#START}.<br>
     *                                  A theme can change it.
     *                                  <p>add-on: the theme of the add-on builds on the classic theme of the library,
     *                                  which centers the text outside the popover.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code level}, {@code inPopover} and {@code isNarrow}.<br>
     *                                  It returns {@code "start"}, {@code "center"} or {@code "end"}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setDayHeaderAlign(HeaderAlign.END);
     * calendar.options().setDayHeaderAlign(JsCallback.of("info => info.inPopover ? 'start' : 'center'"));
     * }</pre>
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
     * The library default is no classes. A theme can set classes of its own, and the classes set here are added to
     * them.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/all-day-divider-render-hooks">allDayDividerClass</a>
     */
    private String allDayDividerClass;

    /**
     * CSS classes for the cell in the header area of time grid views that labels the all-day section.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setAllDayHeaderClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: the stable class {@code fc-timegrid-axis}.<br>
     *                                  Classes set here are added to it.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code text}, {@code isNarrow} and {@code view}.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setAllDayHeaderClass("my-all-day");
     * calendar.options().setAllDayHeaderClass(JsCallback.of("info => info.isNarrow ? 'my-narrow' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/all-day-header-render-hooks">allDayHeaderClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue allDayHeaderClass;

    /**
     * Sets fixed CSS classes for the all-day header cell, see {@link #getAllDayHeaderClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setAllDayHeaderClass(String classNames) {
        this.allDayHeaderClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the all-day header cell in any form, see {@link #getAllDayHeaderClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setAllDayHeaderClass(ClassNameValue value) {
        this.allDayHeaderClass = value;
    }

    /**
     * A callback that runs right after the cell in the header area of time grid views that labels the all-day section
     * has been added to the DOM. It gets an {@code info} object with {@code text}, {@code isNarrow}, {@code view} and
     * {@code el}, the element, see the library docs.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The library default is no callback.
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
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setAllDayHeaderInnerClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code text}, {@code isNarrow} and {@code view}.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setAllDayHeaderInnerClass("my-all-day-inner");
     * calendar.options().setAllDayHeaderInnerClass(JsCallback.of("info => info.isNarrow ? 'my-narrow-inner' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/all-day-header-render-hooks">allDayHeaderInnerClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue allDayHeaderInnerClass;

    /**
     * Sets fixed CSS classes for the inner wrapper of the all-day header cell, see
     * {@link #getAllDayHeaderInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setAllDayHeaderInnerClass(String classNames) {
        this.allDayHeaderInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the inner wrapper of the all-day header cell in any form, see
     * {@link #getAllDayHeaderInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setAllDayHeaderInnerClass(ClassNameValue value) {
        this.allDayHeaderInnerClass = value;
    }

    /**
     * A callback that runs right before the cell in the header area of time grid views that labels the all-day section
     * is removed from the DOM. It gets an {@code info} object with {@code text}, {@code isNarrow}, {@code view} and
     * {@code el}, the element, see the library docs.
     * <p>
     * When this option is changed for an attached calendar, the callback that is set at the time an element is removed
     * runs for it. This includes elements that were rendered before the change.
     * <p>
     * The library default is no callback.
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
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setDayCellBottomClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code date}, {@code text}, {@code isToday} and more.<br>
     *                                  See the library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setDayCellBottomClass("my-day-bottom");
     * calendar.options().setDayCellBottomClass(JsCallback.of("info => info.isToday ? 'my-today-bottom' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/day-cell-render-hooks">dayCellBottomClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue dayCellBottomClass;

    /**
     * Sets fixed CSS classes for the bottom area of day cells, see {@link #getDayCellBottomClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setDayCellBottomClass(String classNames) {
        this.dayCellBottomClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the bottom area of day cells in any form, see {@link #getDayCellBottomClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setDayCellBottomClass(ClassNameValue value) {
        this.dayCellBottomClass = value;
    }

    /**
     * A callback that runs right after a day cell has been added to the DOM. It gets an {@code info} object with
     * {@code date}, {@code text}, {@code isToday}, {@code el}, the cell element, and more, see the library docs.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/day-cell-render-hooks">dayCellDidMount</a>
     */
    private JsCallback dayCellDidMount;

    /**
     * CSS classes for the middle area of a day cell in day grid views and in the all-day section of time grid views,
     * and for the body of the entry popover. This is where the entries appear.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setDayCellInnerClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: the stable class {@code fc-daygrid-day-events}.<br>
     *                                  Classes set here are added to it.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code date}, {@code text}, {@code isToday} and more.<br>
     *                                  See the library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setDayCellInnerClass("my-day-inner");
     * calendar.options().setDayCellInnerClass(JsCallback.of("info => info.isToday ? 'my-today-inner' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/day-cell-render-hooks">dayCellInnerClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue dayCellInnerClass;

    /**
     * Sets fixed CSS classes for the middle area of day cells, see {@link #getDayCellInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setDayCellInnerClass(String classNames) {
        this.dayCellInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the middle area of day cells in any form, see {@link #getDayCellInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
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
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setDayCellTopClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: the stable class {@code fc-daygrid-day-top}.<br>
     *                                  Classes set here are added to it.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code date}, {@code text}, {@code isToday} and more.<br>
     *                                  See the library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setDayCellTopClass("my-day-top");
     * calendar.options().setDayCellTopClass(JsCallback.of("info => info.isToday ? 'my-today-top' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/day-cell-render-hooks">dayCellTopClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue dayCellTopClass;

    /**
     * Sets fixed CSS classes for the top area of day cells, see {@link #getDayCellTopClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setDayCellTopClass(String classNames) {
        this.dayCellTopClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the top area of day cells in any form, see {@link #getDayCellTopClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
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
     * The cells show no day number in the all-day section of time grid views and in day grid views of a single row, so
     * this option has no effect there.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link TextContent}: shown as text.
     *                                  <p>{@link ContentValue#ofHtml(String)}: HTML, not escaped.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setDayCellTopContent(String)}: a text, as {@link TextContent}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: the day number text.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code date}, {@code text}, {@code isToday} and more.<br>
     *                                  See the library docs.<br>
     *                                  It returns a text, {@code {html: '...'}} or {@code {domNodes: [...]}}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setDayCellTopContent("Day");
     * calendar.options().setDayCellTopContent(ContentValue.ofHtml("<b>1</b>"));
     * calendar.options().setDayCellTopContent(JsCallback.of("info => info.isToday ? 'Today' : info.text"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/day-cell-render-hooks">dayCellTopContent</a>
     */
    @JsonConverter(ContentValueConverter.class)
    private ContentValue dayCellTopContent;

    /**
     * Sets a text as the content of the top area of a day cell, see {@link #getDayCellTopContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setDayCellTopContent(String text) {
        this.dayCellTopContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content of the top area of a day cell in any form, see {@link #getDayCellTopContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
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
     * The cells show no day number in the all-day section of time grid views and in day grid views of a single row, so
     * this option has no effect there.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setDayCellTopInnerClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: the stable class {@code fc-daygrid-day-number}.<br>
     *                                  Classes set here are added to it.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code date}, {@code text}, {@code isToday} and more, see the
     *                                  library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setDayCellTopInnerClass("my-class");
     * calendar.options().setDayCellTopInnerClass(JsCallback.of("info => info.isToday ? 'my-today-inner' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/day-cell-render-hooks">dayCellTopInnerClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue dayCellTopInnerClass;

    /**
     * Sets fixed CSS classes for the inner wrapper of the top area of day cells, see
     * {@link #getDayCellTopInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setDayCellTopInnerClass(String classNames) {
        this.dayCellTopInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the inner wrapper of the top area of day cells in any form, see
     * {@link #getDayCellTopInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setDayCellTopInnerClass(ClassNameValue value) {
        this.dayCellTopInnerClass = value;
    }

    /**
     * A callback that runs right before a day cell is removed from the DOM. It gets an {@code info} object with
     * {@code date}, {@code text}, {@code isToday}, {@code el}, the cell element, and more, see the library docs.
     * <p>
     * When this option is changed for an attached calendar, the callback that is set at the time an element is removed
     * runs for it. This includes elements that were rendered before the change.
     * <p>
     * The library default is no callback.
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
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setDayHeaderClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: stable classes such as {@code fc-col-header-cell} and
     *                                  {@code fc-day-today}.<br>
     *                                  Classes set here are added to them.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code date}, {@code text}, {@code isToday} and more, see the
     *                                  library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setDayHeaderClass("my-class");
     * calendar.options().setDayHeaderClass(JsCallback.of("info => info.isToday ? 'my-today-header' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/day-header-render-hooks">dayHeaderClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue dayHeaderClass;

    /**
     * Sets fixed CSS classes for day header cells, see {@link #getDayHeaderClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setDayHeaderClass(String classNames) {
        this.dayHeaderClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for day header cells in any form, see {@link #getDayHeaderClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
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
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link TextContent}: shown as text.
     *                                  <p>{@link ContentValue#ofHtml(String)}: HTML, not escaped.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setDayHeaderContent(String)}: a text, as {@link TextContent}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: the formatted date text.<br>
     *                                  Some stock themes set content of their own.
     *                                  <p>add-on: the Vaadin theme sets no content.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code date}, {@code text}, {@code isToday} and more, see the
     *                                  library docs.<br>
     *                                  It returns a text, {@code {html: '...'}} or {@code {domNodes: [...]}}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setDayHeaderContent("Text");
     * calendar.options().setDayHeaderContent(ContentValue.ofHtml("<b>Text</b>"));
     * calendar.options().setDayHeaderContent(JsCallback.of("info => info.isToday ? 'Today' : info.text"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/day-header-render-hooks">dayHeaderContent</a>
     */
    @JsonConverter(ContentValueConverter.class)
    private ContentValue dayHeaderContent;

    /**
     * Sets a text as the content of the day header cell, see {@link #getDayHeaderContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setDayHeaderContent(String text) {
        this.dayHeaderContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content of the day header cell in any form, see {@link #getDayHeaderContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setDayHeaderContent(ContentValue value) {
        this.dayHeaderContent = value;
    }

    /**
     * A callback that runs right after a day header cell has been added to the DOM. It gets an {@code info} object with
     * {@code date}, {@code text}, {@code el} and more, see the library docs.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/day-header-render-hooks">dayHeaderDidMount</a>
     */
    private JsCallback dayHeaderDidMount;

    /**
     * CSS classes for the divider line beneath the day header cells in day grid, time grid and multi-month views.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setDayHeaderDividerClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code isSticky}, {@code multiMonthColumns} and
     *                                  {@code options}, see the library docs.<br>
     *                                  The library docs say that {@code options} is the calendar options object.<br>
     *                                  The used library version passes only {@code allDaySlot} in it.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setDayHeaderDividerClass("my-class");
     * calendar.options().setDayHeaderDividerClass(JsCallback.of("info => info.isSticky ? 'my-sticky-divider' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/day-header-divider-render-hooks">dayHeaderDividerClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue dayHeaderDividerClass;

    /**
     * Sets fixed CSS classes for the day header divider, see {@link #getDayHeaderDividerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setDayHeaderDividerClass(String classNames) {
        this.dayHeaderDividerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the day header divider in any form, see {@link #getDayHeaderDividerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setDayHeaderDividerClass(ClassNameValue value) {
        this.dayHeaderDividerClass = value;
    }

    /**
     * CSS classes for the inner wrapper of the day header cells in day grid and time grid views, and in the header of
     * the entry popover. This wrapper may be a nav link.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setDayHeaderInnerClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: the stable class {@code fc-col-header-cell-cushion}, in the header of
     *                                  the entry popover {@code fc-popover-title}.<br>
     *                                  Classes set here are added to it.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code date}, {@code text}, {@code isToday} and more, see the
     *                                  library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setDayHeaderInnerClass("my-class");
     * calendar.options().setDayHeaderInnerClass(JsCallback.of("info => info.isToday ? 'my-today-inner' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/day-header-render-hooks">dayHeaderInnerClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue dayHeaderInnerClass;

    /**
     * Sets fixed CSS classes for the inner wrapper of day header cells, see {@link #getDayHeaderInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setDayHeaderInnerClass(String classNames) {
        this.dayHeaderInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the inner wrapper of day header cells in any form, see
     * {@link #getDayHeaderInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
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
     * The library default is no classes. A theme can set classes of its own, and the classes set here are added to
     * them.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/day-header-row-render-hooks">dayHeaderRowClass</a>
     */
    private String dayHeaderRowClass;

    /**
     * A callback that runs right before a day header cell is removed from the DOM. It gets an {@code info} object with
     * {@code date}, {@code text}, {@code el} and more, see the library docs.
     * <p>
     * When this option is changed for an attached calendar, the callback that is set at the time an element is removed
     * runs for it. This includes elements that were rendered before the change.
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/day-header-render-hooks">dayHeaderWillUnmount</a>
     */
    private JsCallback dayHeaderWillUnmount;

    /**
     * CSS classes for day lanes, the columns of time slots of a day in time grid views.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setDayLaneClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: stable classes such as {@code fc-timegrid-col} and
     *                                  {@code fc-day-today}.<br>
     *                                  Classes set here are added to them.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code date}, {@code isToday}, {@code isNarrow} and more, see
     *                                  the library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setDayLaneClass("my-class");
     * calendar.options().setDayLaneClass(JsCallback.of("info => info.isToday ? 'my-today-lane' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/day-lane-render-hooks">dayLaneClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue dayLaneClass;

    /**
     * Sets fixed CSS classes for day lanes, see {@link #getDayLaneClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setDayLaneClass(String classNames) {
        this.dayLaneClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for day lanes in any form, see {@link #getDayLaneClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setDayLaneClass(ClassNameValue value) {
        this.dayLaneClass = value;
    }

    /**
     * A callback that runs right after a day lane (column of time slots in time grid views) has been added to the DOM.
     * It gets an {@code info} object with {@code date}, {@code isToday}, {@code el} and more, see the library docs.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/day-lane-render-hooks">dayLaneDidMount</a>
     */
    private JsCallback dayLaneDidMount;

    /**
     * CSS classes for the inner wrapper of a day lane. Useful for adjusting padding.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setDayLaneInnerClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code date}, {@code isToday}, {@code isNarrow} and more, see
     *                                  the library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setDayLaneInnerClass("my-class");
     * calendar.options().setDayLaneInnerClass(JsCallback.of("info => info.isNarrow ? 'my-narrow-lane' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/day-lane-render-hooks">dayLaneInnerClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue dayLaneInnerClass;

    /**
     * Sets fixed CSS classes for the inner wrapper of day lanes, see {@link #getDayLaneInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setDayLaneInnerClass(String classNames) {
        this.dayLaneInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the inner wrapper of day lanes in any form, see {@link #getDayLaneInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setDayLaneInnerClass(ClassNameValue value) {
        this.dayLaneInnerClass = value;
    }

    /**
     * A callback that runs right before a day lane (column of time slots in time grid views) is removed from the DOM.
     * It gets an {@code info} object with {@code date}, {@code isToday}, {@code el} and more, see the library docs.
     * <p>
     * When this option is changed for an attached calendar, the callback that is set at the time an element is removed
     * runs for it. This includes elements that were rendered before the change.
     * <p>
     * The library default is no callback.
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
     * The library default is no classes. A theme can set classes of its own, and the classes set here are added to
     * them.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/day-row-render-hooks">dayRowClass</a>
     */
    private String dayRowClass;

    /**
     * CSS classes for the filler elements that keep the calendar layout aligned. They fill space in the all-day section
     * to offset the scrollbar width of the timed section, and in resource timeline views where the resource rows do not
     * fill the full height.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setFillerClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code inTableHeader}, see the library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setFillerClass("my-class");
     * calendar.options().setFillerClass(JsCallback.of("info => info.inTableHeader ? 'my-header-filler' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/filler-render-hooks">fillerClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue fillerClass;

    /**
     * Sets fixed CSS classes for filler elements, see {@link #getFillerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setFillerClass(String classNames) {
        this.fillerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for filler elements in any form, see {@link #getFillerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
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
     * The library default is no classes. A theme can set classes of its own, and the classes set here are added to
     * them.
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
     * The library default is no classes. A theme can set classes of its own, and the classes set here are added to
     * them.
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
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setInlineWeekNumberClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: the stable class {@code fc-daygrid-week-number}.<br>
     *                                  Classes set here are added to it.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code num}, {@code text}, {@code textParts}, {@code date},
     *                                  {@code isNarrow} and {@code hasNavLink}.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setInlineWeekNumberClass("my-class");
     * calendar.options().setInlineWeekNumberClass(JsCallback.of("info => info.num % 2 === 0 ? 'my-even-week' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/inline-week-number-render-hooks">inlineWeekNumberClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue inlineWeekNumberClass;

    /**
     * Sets fixed CSS classes, see {@link #getInlineWeekNumberClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setInlineWeekNumberClass(String classNames) {
        this.inlineWeekNumberClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getInlineWeekNumberClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setInlineWeekNumberClass(ClassNameValue value) {
        this.inlineWeekNumberClass = value;
    }

    /**
     * Custom content for the week number shown inline in day grid cells.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link TextContent}: shown as text.
     *                                  <p>{@link ContentValue#ofHtml(String)}: HTML, not escaped.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setInlineWeekNumberContent(String)}: a text, as
     *                                  {@link TextContent}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: the library's own week number text.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code num}, {@code text}, {@code textParts}, {@code date},
     *                                  {@code isNarrow} and {@code hasNavLink}.<br>
     *                                  It returns a text, {@code {html: '...'}} or {@code {domNodes: [...]}}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setInlineWeekNumberContent("Text");
     * calendar.options().setInlineWeekNumberContent(ContentValue.ofHtml("<b>Text</b>"));
     * calendar.options().setInlineWeekNumberContent(JsCallback.of("info => 'Week ' + info.num"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/inline-week-number-render-hooks">inlineWeekNumberContent</a>
     */
    @JsonConverter(ContentValueConverter.class)
    private ContentValue inlineWeekNumberContent;

    /**
     * Sets a text as the content, see {@link #getInlineWeekNumberContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setInlineWeekNumberContent(String text) {
        this.inlineWeekNumberContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content in any form, see {@link #getInlineWeekNumberContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setInlineWeekNumberContent(ContentValue value) {
        this.inlineWeekNumberContent = value;
    }

    /**
     * A callback that runs right after the week number shown inline in day grid cells has been added to the DOM.
     * <p>
     * The callback gets an {@code info} object with {@code el} (the element), {@code num}, {@code text} and more, see
     * the library docs.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/inline-week-number-render-hooks">inlineWeekNumberDidMount</a>
     */
    private JsCallback inlineWeekNumberDidMount;

    /**
     * A callback that runs right before the week number shown inline in day grid cells is removed from the DOM.
     * <p>
     * The callback gets an {@code info} object with {@code el} (the element), {@code num}, {@code text} and more, see
     * the library docs.
     * <p>
     * When this option is changed for an attached calendar, the callback that is set at the time an element is removed
     * runs for it. This includes elements that were rendered before the change.
     * <p>
     * The library default is no callback.
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
     * The library default is no classes. A theme can set classes of its own, and the classes set here are added to
     * them.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/now-indicator-dot-render-hooks">nowIndicatorDotClass</a>
     */
    private String nowIndicatorDotClass;

    /**
     * CSS classes for the arrow of the now indicator in the time axis (time grid and timeline views).
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setNowIndicatorHeaderClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: the stable class {@code fc-timegrid-now-indicator-arrow} in time grid
     *                                  views.<br>
     *                                  Classes set here are added to it.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code date} and {@code view}.<br>
     *                                  The library docs list only {@code date}, but the used library version also
     *                                  passes {@code view}.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setNowIndicatorHeaderClass("my-class");
     * calendar.options().setNowIndicatorHeaderClass(JsCallback.of(
     *         "info => info.date.getHours() < 12 ? 'my-morning' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/now-indicator-header-render-hooks">nowIndicatorHeaderClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue nowIndicatorHeaderClass;

    /**
     * Sets fixed CSS classes, see {@link #getNowIndicatorHeaderClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setNowIndicatorHeaderClass(String classNames) {
        this.nowIndicatorHeaderClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getNowIndicatorHeaderClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setNowIndicatorHeaderClass(ClassNameValue value) {
        this.nowIndicatorHeaderClass = value;
    }

    /**
     * Custom content for the arrow of the now indicator in the time axis (time grid and timeline views).
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link TextContent}: shown as text.
     *                                  <p>{@link ContentValue#ofHtml(String)}: HTML, not escaped.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setNowIndicatorHeaderContent(String)}: a text, as
     *                                  {@link TextContent}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no generated content.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code date} and {@code view}.<br>
     *                                  The library docs list only {@code date}, but the used library version also
     *                                  passes {@code view}.<br>
     *                                  It returns a text, {@code {html: '...'}} or {@code {domNodes: [...]}}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setNowIndicatorHeaderContent("Text");
     * calendar.options().setNowIndicatorHeaderContent(ContentValue.ofHtml("<b>Text</b>"));
     * calendar.options().setNowIndicatorHeaderContent(JsCallback.of("info => info.date.toLocaleTimeString()"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/now-indicator-header-render-hooks">nowIndicatorHeaderContent</a>
     */
    @JsonConverter(ContentValueConverter.class)
    private ContentValue nowIndicatorHeaderContent;

    /**
     * Sets a text as the content, see {@link #getNowIndicatorHeaderContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setNowIndicatorHeaderContent(String text) {
        this.nowIndicatorHeaderContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content in any form, see {@link #getNowIndicatorHeaderContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setNowIndicatorHeaderContent(ContentValue value) {
        this.nowIndicatorHeaderContent = value;
    }

    /**
     * A callback that runs right after the now indicator arrow has been added to the DOM.
     * <p>
     * The callback gets an {@code info} object with {@code date}, {@code view} and {@code el}. The library docs
     * list only {@code date} and {@code el}, but the used library version also passes {@code view}.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/now-indicator-header-render-hooks">nowIndicatorHeaderDidMount</a>
     */
    private JsCallback nowIndicatorHeaderDidMount;

    /**
     * A callback that runs right before the now indicator arrow is removed from the DOM.
     * <p>
     * The callback gets an {@code info} object with {@code date}, {@code view} and {@code el}. The library docs
     * list only {@code date} and {@code el}, but the used library version also passes {@code view}.
     * <p>
     * When this option is changed for an attached calendar, the callback that is set at the time an element is removed
     * runs for it. This includes elements that were rendered before the change.
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/now-indicator-header-render-hooks">nowIndicatorHeaderWillUnmount</a>
     */
    private JsCallback nowIndicatorHeaderWillUnmount;

    /**
     * CSS classes for the line of the now indicator over the day columns (time grid and timeline views).
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setNowIndicatorLineClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: the stable class {@code fc-timegrid-now-indicator-line} in time grid
     *                                  views.<br>
     *                                  Classes set here are added to it.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code date} and {@code view}.<br>
     *                                  The library docs list only {@code date}, but the used library version also
     *                                  passes {@code view}.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setNowIndicatorLineClass("my-class");
     * calendar.options().setNowIndicatorLineClass(JsCallback.of(
     *         "info => info.date.getHours() < 12 ? 'my-morning' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/now-indicator-line-render-hooks">nowIndicatorLineClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue nowIndicatorLineClass;

    /**
     * Sets fixed CSS classes, see {@link #getNowIndicatorLineClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setNowIndicatorLineClass(String classNames) {
        this.nowIndicatorLineClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getNowIndicatorLineClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setNowIndicatorLineClass(ClassNameValue value) {
        this.nowIndicatorLineClass = value;
    }

    /**
     * Custom content for the line of the now indicator over the day columns (time grid and timeline views).
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link TextContent}: shown as text.
     *                                  <p>{@link ContentValue#ofHtml(String)}: HTML, not escaped.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setNowIndicatorLineContent(String)}: a text, as
     *                                  {@link TextContent}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no generated content.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code date} and {@code view}.<br>
     *                                  The library docs list only {@code date}, but the used library version also
     *                                  passes {@code view}.<br>
     *                                  It returns a text, {@code {html: '...'}} or {@code {domNodes: [...]}}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setNowIndicatorLineContent("Text");
     * calendar.options().setNowIndicatorLineContent(ContentValue.ofHtml("<b>Text</b>"));
     * calendar.options().setNowIndicatorLineContent(JsCallback.of("info => info.date.toLocaleTimeString()"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/now-indicator-line-render-hooks">nowIndicatorLineContent</a>
     */
    @JsonConverter(ContentValueConverter.class)
    private ContentValue nowIndicatorLineContent;

    /**
     * Sets a text as the content, see {@link #getNowIndicatorLineContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setNowIndicatorLineContent(String text) {
        this.nowIndicatorLineContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content in any form, see {@link #getNowIndicatorLineContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setNowIndicatorLineContent(ContentValue value) {
        this.nowIndicatorLineContent = value;
    }

    /**
     * A callback that runs right after the now indicator line has been added to the DOM.
     * <p>
     * The callback gets an {@code info} object with {@code date}, {@code view} and {@code el}. The library docs
     * list only {@code date} and {@code el}, but the used library version also passes {@code view}.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/now-indicator-line-render-hooks">nowIndicatorLineDidMount</a>
     */
    private JsCallback nowIndicatorLineDidMount;

    /**
     * A callback that runs right before the now indicator line is removed from the DOM.
     * <p>
     * The callback gets an {@code info} object with {@code date}, {@code view} and {@code el}. The library docs
     * list only {@code date} and {@code el}, but the used library version also passes {@code view}.
     * <p>
     * When this option is changed for an attached calendar, the callback that is set at the time an element is removed
     * runs for it. This includes elements that were rendered before the change.
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/now-indicator-line-render-hooks">nowIndicatorLineWillUnmount</a>
     */
    private JsCallback nowIndicatorLineWillUnmount;

    /**
     * CSS classes for time slot headers, where time grid and timeline views show the date or time text of a slot.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setSlotHeaderClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: stable classes such as {@code fc-timegrid-slot} and
     *                                  {@code fc-timegrid-slot-label} in time grid views.<br>
     *                                  Classes set here are added to them.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code date}, {@code text}, {@code isMinor} and more, see the
     *                                  library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setSlotHeaderClass("my-class");
     * calendar.options().setSlotHeaderClass(JsCallback.of(
     *         "info => info.date.getHours() < 12 ? 'my-morning-slot' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/slot-header-render-hooks">slotHeaderClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue slotHeaderClass;

    /**
     * Sets fixed CSS classes, see {@link #getSlotHeaderClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setSlotHeaderClass(String classNames) {
        this.slotHeaderClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getSlotHeaderClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setSlotHeaderClass(ClassNameValue value) {
        this.slotHeaderClass = value;
    }

    /**
     * Custom content for a time slot header.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link TextContent}: shown as text.
     *                                  <p>{@link ContentValue#ofHtml(String)}: HTML, not escaped.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setSlotHeaderContent(String)}: a text, as {@link TextContent}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: the formatted text of the slot.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code date}, {@code text}, {@code isMinor} and more, see the
     *                                  library docs.<br>
     *                                  It returns a text, {@code {html: '...'}} or {@code {domNodes: [...]}}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setSlotHeaderContent("Text");
     * calendar.options().setSlotHeaderContent(ContentValue.ofHtml("<b>Text</b>"));
     * calendar.options().setSlotHeaderContent(JsCallback.of(
     *         "info => info.date.getHours() === 12 ? 'Noon' : info.text"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/slot-header-render-hooks">slotHeaderContent</a>
     */
    @JsonConverter(ContentValueConverter.class)
    private ContentValue slotHeaderContent;

    /**
     * Sets a text as the content, see {@link #getSlotHeaderContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setSlotHeaderContent(String text) {
        this.slotHeaderContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content in any form, see {@link #getSlotHeaderContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setSlotHeaderContent(ContentValue value) {
        this.slotHeaderContent = value;
    }

    /**
     * A callback that runs right after a time slot header has been added to the DOM.
     * <p>
     * The callback gets an {@code info} object with {@code date}, {@code text}, {@code el} (the slot header element)
     * and more, see the library docs.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/slot-header-render-hooks">slotHeaderDidMount</a>
     */
    private JsCallback slotHeaderDidMount;

    /**
     * CSS classes for the divider line between the time labels and the slot lanes, in time grid and timeline views.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setSlotHeaderDividerClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code inTableHeader} and {@code options} (with
     *                                  {@code dayMinWidth}).<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setSlotHeaderDividerClass("my-class");
     * calendar.options().setSlotHeaderDividerClass(JsCallback.of(
     *         "info => info.inTableHeader ? 'my-header-divider' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/slot-header-divider-render-hooks">slotHeaderDividerClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue slotHeaderDividerClass;

    /**
     * Sets fixed CSS classes, see {@link #getSlotHeaderDividerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setSlotHeaderDividerClass(String classNames) {
        this.slotHeaderDividerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getSlotHeaderDividerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
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
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setSlotHeaderInnerClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code date}, {@code text}, {@code isMinor} and more, see the
     *                                  library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setSlotHeaderInnerClass("my-class");
     * calendar.options().setSlotHeaderInnerClass(JsCallback.of(
     *         "info => info.date.getHours() < 12 ? 'my-morning-slot' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/slot-header-render-hooks">slotHeaderInnerClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue slotHeaderInnerClass;

    /**
     * Sets fixed CSS classes, see {@link #getSlotHeaderInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setSlotHeaderInnerClass(String classNames) {
        this.slotHeaderInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getSlotHeaderInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setSlotHeaderInnerClass(ClassNameValue value) {
        this.slotHeaderInnerClass = value;
    }

    /**
     * CSS classes for the row that contains the slot header cells in timeline views, which the Scheduler provides. The
     * library docs also name time grid views, but the used library version applies the option in timeline views only.
     * <p>
     * It takes class names only, because the library accepts no callback for this option.
     * <p>
     * The library default is no classes. A theme can set classes of its own, and the classes set here are added to
     * them.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/slot-header-row-render-hooks">slotHeaderRowClass</a>
     */
    private String slotHeaderRowClass;

    /**
     * A callback that runs right before a time slot header is removed from the DOM.
     * <p>
     * The callback gets an {@code info} object with {@code date}, {@code text}, {@code el} (the slot header element)
     * and more, see the library docs.
     * <p>
     * When this option is changed for an attached calendar, the callback that is set at the time an element is removed
     * runs for it. This includes elements that were rendered before the change.
     * <p>
     * The library default is no callback.
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
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setSlotLaneClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: stable classes such as {@code fc-timegrid-slot} and
     *                                  {@code fc-timegrid-slot-lane} in time grid views.<br>
     *                                  Classes set here are added to them.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code date}, {@code view} and more, and {@code time} in time
     *                                  grid views, see the library docs.<br>
     *                                  The library docs list {@code text} as well, which the used library version does
     *                                  not pass.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setSlotLaneClass("my-class");
     * calendar.options().setSlotLaneClass(JsCallback.of("info => info.isMinor ? 'my-minor-slot' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/slot-lane-render-hooks">slotLaneClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue slotLaneClass;

    /**
     * Sets fixed CSS classes, see {@link #getSlotLaneClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setSlotLaneClass(String classNames) {
        this.slotLaneClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getSlotLaneClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setSlotLaneClass(ClassNameValue value) {
        this.slotLaneClass = value;
    }

    /**
     * A callback that runs right after a time slot lane has been added to the DOM.
     * <p>
     * The callback gets an {@code info} object with {@code date}, {@code el} (the slot lane element) and more, and
     * {@code time} in time grid views, see the library docs. The library docs list {@code text} as well, which the used
     * library version does not pass.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/slot-lane-render-hooks">slotLaneDidMount</a>
     */
    private JsCallback slotLaneDidMount;

    /**
     * A callback that runs right before a time slot lane is removed from the DOM.
     * <p>
     * The callback gets an {@code info} object with {@code date}, {@code el} (the slot lane element) and more, and
     * {@code time} in time grid views, see the library docs. The library docs list {@code text} as well, which the used
     * library version does not pass.
     * <p>
     * When this option is changed for an attached calendar, the callback that is set at the time an element is removed
     * runs for it. This includes elements that were rendered before the change.
     * <p>
     * The library default is no callback.
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
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setTableBodyClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code borderlessX}, {@code borderlessTop},
     *                                  {@code borderlessBottom} and {@code multiMonthColumns}.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setTableBodyClass("my-class");
     * calendar.options().setTableBodyClass(JsCallback.of("info => info.borderlessX ? 'my-borderless-body' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/table-render-hooks">tableBodyClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue tableBodyClass;

    /**
     * Sets fixed CSS classes, see {@link #getTableBodyClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setTableBodyClass(String classNames) {
        this.tableBodyClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getTableBodyClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setTableBodyClass(ClassNameValue value) {
        this.tableBodyClass = value;
    }

    /**
     * CSS classes for the root element of the table-structured layout used across views.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setTableClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code borderlessX}, {@code borderlessTop},
     *                                  {@code borderlessBottom} and {@code multiMonthColumns}.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setTableClass("my-class");
     * calendar.options().setTableClass(JsCallback.of("info => info.borderlessX ? 'my-borderless-table' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/table-render-hooks">tableClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue tableClass;

    /**
     * Sets fixed CSS classes, see {@link #getTableClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setTableClass(String classNames) {
        this.tableClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getTableClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setTableClass(ClassNameValue value) {
        this.tableClass = value;
    }

    /**
     * CSS classes for the header row of the table-structured layout used across views, where the day names appear.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setTableHeaderClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code isSticky}, {@code borderlessX}, {@code borderlessTop},
     *                                  {@code borderlessBottom} and {@code multiMonthColumns}.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setTableHeaderClass("my-class");
     * calendar.options().setTableHeaderClass(JsCallback.of("info => info.isSticky ? 'my-sticky-header' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/table-render-hooks">tableHeaderClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue tableHeaderClass;

    /**
     * Sets fixed CSS classes, see {@link #getTableHeaderClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setTableHeaderClass(String classNames) {
        this.tableHeaderClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getTableHeaderClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setTableHeaderClass(ClassNameValue value) {
        this.tableHeaderClass = value;
    }

    /**
     * CSS classes for the week number in the header above the time axis of time grid views.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setWeekNumberHeaderClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: the stable class {@code fc-timegrid-axis}.<br>
     *                                  Classes set here are added to it.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code num}, {@code text}, {@code textParts}, {@code date},
     *                                  {@code isNarrow}, {@code hasNavLink} and {@code options}.<br>
     *                                  {@code options} holds only {@code dayMinWidth}.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setWeekNumberHeaderClass("my-class");
     * calendar.options().setWeekNumberHeaderClass(JsCallback.of("info => info.isNarrow ? 'my-narrow-week' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/week-number-header-render-hooks">weekNumberHeaderClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue weekNumberHeaderClass;

    /**
     * Sets fixed CSS classes, see {@link #getWeekNumberHeaderClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setWeekNumberHeaderClass(String classNames) {
        this.weekNumberHeaderClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getWeekNumberHeaderClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setWeekNumberHeaderClass(ClassNameValue value) {
        this.weekNumberHeaderClass = value;
    }

    /**
     * Custom content for the week number in the header above the time axis of time grid views.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link TextContent}: shown as text.
     *                                  <p>{@link ContentValue#ofHtml(String)}: HTML, not escaped.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setWeekNumberHeaderContent(String)}: a text, as
     *                                  {@link TextContent}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: the text of the week number, formatted as {@code weekNumberFormat}
     *                                  says.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code num}, {@code text}, {@code textParts}, {@code date},
     *                                  {@code isNarrow}, {@code hasNavLink} and {@code options}.<br>
     *                                  {@code options} holds only {@code dayMinWidth}.<br>
     *                                  It returns a text, {@code {html: '...'}} or {@code {domNodes: [...]}}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setWeekNumberHeaderContent("Week");
     * calendar.options().setWeekNumberHeaderContent(ContentValue.ofHtml("<b>W</b>"));
     * calendar.options().setWeekNumberHeaderContent(JsCallback.of("info => 'W' + info.num"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/week-number-header-render-hooks">weekNumberHeaderContent</a>
     */
    @JsonConverter(ContentValueConverter.class)
    private ContentValue weekNumberHeaderContent;

    /**
     * Sets a text as the content, see {@link #getWeekNumberHeaderContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setWeekNumberHeaderContent(String text) {
        this.weekNumberHeaderContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content in any form, see {@link #getWeekNumberHeaderContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setWeekNumberHeaderContent(ContentValue value) {
        this.weekNumberHeaderContent = value;
    }

    /**
     * A callback that runs right after the week number in the header above the time axis of time grid views has been
     * added to the DOM.
     * <p>
     * The callback gets an {@code info} object with {@code num}, {@code text}, {@code el} (the element) and more, see
     * the library docs. {@code options} holds only {@code dayMinWidth}.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The library default is no callback.
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
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setWeekNumberHeaderInnerClass(String)}: class names separated by spaces,
     *                                  as {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code num}, {@code date}, {@code text}, {@code textParts},
     *                                  {@code isNarrow}, {@code hasNavLink} and {@code options}.<br>
     *                                  {@code options} holds only {@code dayMinWidth}, see the library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setWeekNumberHeaderInnerClass("my-class");
     * calendar.options().setWeekNumberHeaderInnerClass(JsCallback.of("info => info.isNarrow ? 'my-narrow' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/week-number-header-render-hooks">weekNumberHeaderInnerClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue weekNumberHeaderInnerClass;

    /**
     * Sets fixed CSS classes, see {@link #getWeekNumberHeaderInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setWeekNumberHeaderInnerClass(String classNames) {
        this.weekNumberHeaderInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getWeekNumberHeaderInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
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
     * The callback gets an {@code info} object with {@code num}, {@code text}, {@code el} (the element) and more, see
     * the library docs. {@code options} holds only {@code dayMinWidth}.
     * <p>
     * When this option is changed for an attached calendar, the callback that is set at the time an element is removed
     * runs for it. This includes elements that were rendered before the change.
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/week-number-header-render-hooks">weekNumberHeaderWillUnmount</a>
     */
    private JsCallback weekNumberHeaderWillUnmount;

    // Render hooks: entry layouts (block, column, row)

    /**
     * CSS classes for the outermost element of block entries, the rectangular, color-filled entries (essentially all
     * entries except list entries).
     * <p>
     * It applies in addition to the general entry class option of the same part.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setBlockEntryClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code event}, {@code timeText}, {@code isStart} and more, see
     *                                  the library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setBlockEntryClass("my-class");
     * calendar.options().setBlockEntryClass(JsCallback.of("info => info.isPast ? 'my-past' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/block-event-render-hooks">blockEventClass</a>
     */
    @JsonName("blockEventClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue blockEntryClass;

    /**
     * Sets fixed CSS classes for the outermost element of block entries, see {@link #getBlockEntryClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setBlockEntryClass(String classNames) {
        this.blockEntryClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the outermost element of block entries in any form, see {@link #getBlockEntryClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setBlockEntryClass(ClassNameValue value) {
        this.blockEntryClass = value;
    }

    /**
     * CSS classes for the element before the "inner" wrapper of block entries, the rectangular, color-filled entries
     * (essentially all entries except list entries).
     * <p>
     * It applies in addition to the general entry class option of the same part.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setBlockEntryBeforeClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code event}, {@code timeText}, {@code isStart} and more, see
     *                                  the library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setBlockEntryBeforeClass("my-class");
     * calendar.options().setBlockEntryBeforeClass(JsCallback.of("info => info.isPast ? 'my-past' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/block-event-render-hooks">blockEventBeforeClass</a>
     */
    @JsonName("blockEventBeforeClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue blockEntryBeforeClass;

    /**
     * Sets fixed CSS classes for the element before the "inner" wrapper of block entries, see
     * {@link #getBlockEntryBeforeClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setBlockEntryBeforeClass(String classNames) {
        this.blockEntryBeforeClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the element before the "inner" wrapper of block entries in any form, see
     * {@link #getBlockEntryBeforeClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setBlockEntryBeforeClass(ClassNameValue value) {
        this.blockEntryBeforeClass = value;
    }

    /**
     * CSS classes for the element after the "inner" wrapper of block entries, the rectangular, color-filled entries
     * (essentially all entries except list entries).
     * <p>
     * It applies in addition to the general entry class option of the same part.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setBlockEntryAfterClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code event}, {@code timeText}, {@code isStart} and more, see
     *                                  the library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setBlockEntryAfterClass("my-class");
     * calendar.options().setBlockEntryAfterClass(JsCallback.of("info => info.isPast ? 'my-past' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/block-event-render-hooks">blockEventAfterClass</a>
     */
    @JsonName("blockEventAfterClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue blockEntryAfterClass;

    /**
     * Sets fixed CSS classes for the element after the "inner" wrapper of block entries, see
     * {@link #getBlockEntryAfterClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setBlockEntryAfterClass(String classNames) {
        this.blockEntryAfterClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the element after the "inner" wrapper of block entries in any form, see
     * {@link #getBlockEntryAfterClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setBlockEntryAfterClass(ClassNameValue value) {
        this.blockEntryAfterClass = value;
    }

    /**
     * CSS classes for the "inner" wrapper element of block entries, the rectangular, color-filled entries (essentially
     * all entries except list entries).
     * <p>
     * It applies in addition to the general entry class option of the same part.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setBlockEntryInnerClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code event}, {@code timeText}, {@code isStart} and more, see
     *                                  the library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setBlockEntryInnerClass("my-class");
     * calendar.options().setBlockEntryInnerClass(JsCallback.of("info => info.isPast ? 'my-past' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/block-event-render-hooks">blockEventInnerClass</a>
     */
    @JsonName("blockEventInnerClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue blockEntryInnerClass;

    /**
     * Sets fixed CSS classes for the "inner" wrapper element of block entries, see {@link #getBlockEntryInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setBlockEntryInnerClass(String classNames) {
        this.blockEntryInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the "inner" wrapper element of block entries in any form, see
     * {@link #getBlockEntryInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setBlockEntryInnerClass(ClassNameValue value) {
        this.blockEntryInnerClass = value;
    }

    /**
     * CSS classes for the time element within the "inner" wrapper of block entries, the rectangular, color-filled
     * entries (essentially all entries except list entries).
     * <p>
     * It applies in addition to the general entry class option of the same part.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setBlockEntryTimeClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has only {@code event}, {@code timeText}, {@code isNarrow},
     *                                  {@code isShort} and {@code resource} (resource views).<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setBlockEntryTimeClass("my-class");
     * calendar.options().setBlockEntryTimeClass(JsCallback.of("info => info.isNarrow ? 'my-narrow' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/block-event-render-hooks">blockEventTimeClass</a>
     */
    @JsonName("blockEventTimeClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue blockEntryTimeClass;

    /**
     * Sets fixed CSS classes for the time element within the "inner" wrapper of block entries, see
     * {@link #getBlockEntryTimeClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setBlockEntryTimeClass(String classNames) {
        this.blockEntryTimeClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the time element within the "inner" wrapper of block entries in any form, see
     * {@link #getBlockEntryTimeClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setBlockEntryTimeClass(ClassNameValue value) {
        this.blockEntryTimeClass = value;
    }

    /**
     * CSS classes for the title element within the "inner" wrapper of block entries, the rectangular, color-filled
     * entries (essentially all entries except list entries).
     * <p>
     * It applies in addition to the general entry class option of the same part.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setBlockEntryTitleClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has only {@code event}, {@code timeText}, {@code isNarrow},
     *                                  {@code isShort} and {@code resource} (resource views).<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setBlockEntryTitleClass("my-class");
     * calendar.options().setBlockEntryTitleClass(JsCallback.of("info => info.isNarrow ? 'my-narrow' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/block-event-render-hooks">blockEventTitleClass</a>
     */
    @JsonName("blockEventTitleClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue blockEntryTitleClass;

    /**
     * Sets fixed CSS classes for the title element within the "inner" wrapper of block entries, see
     * {@link #getBlockEntryTitleClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setBlockEntryTitleClass(String classNames) {
        this.blockEntryTitleClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the title element within the "inner" wrapper of block entries in any form, see
     * {@link #getBlockEntryTitleClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setBlockEntryTitleClass(ClassNameValue value) {
        this.blockEntryTitleClass = value;
    }

    /**
     * CSS classes for the outermost element of column entries, the color-filled entries that render vertically (timed
     * entries in time grid views).
     * <p>
     * It applies in addition to the general and the block entry class option of the same part.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setColumnEntryClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: the stable classes {@code fc-timegrid-event} and
     *                                  {@code fc-v-event}.<br>
     *                                  Classes set here are added to them.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code event}, {@code timeText}, {@code isStart} and more, see
     *                                  the library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setColumnEntryClass("my-class");
     * calendar.options().setColumnEntryClass(JsCallback.of("info => info.isPast ? 'my-past' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/column-event-render-hooks">columnEventClass</a>
     */
    @JsonName("columnEventClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue columnEntryClass;

    /**
     * Sets fixed CSS classes for the outermost element of column entries, see {@link #getColumnEntryClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setColumnEntryClass(String classNames) {
        this.columnEntryClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the outermost element of column entries in any form, see {@link #getColumnEntryClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setColumnEntryClass(ClassNameValue value) {
        this.columnEntryClass = value;
    }

    /**
     * CSS classes for the element before the "inner" wrapper of column entries, the color-filled entries that render
     * vertically (timed entries in time grid views).
     * <p>
     * It applies in addition to the general and the block entry class option of the same part.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setColumnEntryBeforeClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code event}, {@code timeText}, {@code isStart} and more, see
     *                                  the library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setColumnEntryBeforeClass("my-class");
     * calendar.options().setColumnEntryBeforeClass(JsCallback.of("info => info.isPast ? 'my-past' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/column-event-render-hooks">columnEventBeforeClass</a>
     */
    @JsonName("columnEventBeforeClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue columnEntryBeforeClass;

    /**
     * Sets fixed CSS classes for the element before the "inner" wrapper of column entries, see
     * {@link #getColumnEntryBeforeClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setColumnEntryBeforeClass(String classNames) {
        this.columnEntryBeforeClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the element before the "inner" wrapper of column entries in any form, see
     * {@link #getColumnEntryBeforeClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setColumnEntryBeforeClass(ClassNameValue value) {
        this.columnEntryBeforeClass = value;
    }

    /**
     * CSS classes for the element after the "inner" wrapper of column entries, the color-filled entries that render
     * vertically (timed entries in time grid views).
     * <p>
     * It applies in addition to the general and the block entry class option of the same part.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setColumnEntryAfterClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code event}, {@code timeText}, {@code isStart} and more, see
     *                                  the library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setColumnEntryAfterClass("my-class");
     * calendar.options().setColumnEntryAfterClass(JsCallback.of("info => info.isPast ? 'my-past' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/column-event-render-hooks">columnEventAfterClass</a>
     */
    @JsonName("columnEventAfterClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue columnEntryAfterClass;

    /**
     * Sets fixed CSS classes for the element after the "inner" wrapper of column entries, see
     * {@link #getColumnEntryAfterClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setColumnEntryAfterClass(String classNames) {
        this.columnEntryAfterClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the element after the "inner" wrapper of column entries in any form, see
     * {@link #getColumnEntryAfterClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setColumnEntryAfterClass(ClassNameValue value) {
        this.columnEntryAfterClass = value;
    }

    /**
     * CSS classes for the "inner" wrapper element of column entries, the color-filled entries that render vertically
     * (timed entries in time grid views).
     * <p>
     * It applies in addition to the general and the block entry class option of the same part.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setColumnEntryInnerClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code event}, {@code timeText}, {@code isStart} and more, see
     *                                  the library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setColumnEntryInnerClass("my-class");
     * calendar.options().setColumnEntryInnerClass(JsCallback.of("info => info.isPast ? 'my-past' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/column-event-render-hooks">columnEventInnerClass</a>
     */
    @JsonName("columnEventInnerClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue columnEntryInnerClass;

    /**
     * Sets fixed CSS classes for the "inner" wrapper element of column entries, see
     * {@link #getColumnEntryInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setColumnEntryInnerClass(String classNames) {
        this.columnEntryInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the "inner" wrapper element of column entries in any form, see
     * {@link #getColumnEntryInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setColumnEntryInnerClass(ClassNameValue value) {
        this.columnEntryInnerClass = value;
    }

    /**
     * CSS classes for the time element within the "inner" wrapper of column entries, the color-filled entries that
     * render vertically (timed entries in time grid views).
     * <p>
     * It applies in addition to the general and the block entry class option of the same part.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setColumnEntryTimeClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has only {@code event}, {@code timeText}, {@code isNarrow},
     *                                  {@code isShort} and {@code resource} (resource views).<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setColumnEntryTimeClass("my-class");
     * calendar.options().setColumnEntryTimeClass(JsCallback.of("info => info.isNarrow ? 'my-narrow' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/column-event-render-hooks">columnEventTimeClass</a>
     */
    @JsonName("columnEventTimeClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue columnEntryTimeClass;

    /**
     * Sets fixed CSS classes for the time element within the "inner" wrapper of column entries, see
     * {@link #getColumnEntryTimeClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setColumnEntryTimeClass(String classNames) {
        this.columnEntryTimeClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the time element within the "inner" wrapper of column entries in any form, see
     * {@link #getColumnEntryTimeClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setColumnEntryTimeClass(ClassNameValue value) {
        this.columnEntryTimeClass = value;
    }

    /**
     * CSS classes for the title element within the "inner" wrapper of column entries, the color-filled entries that
     * render vertically (timed entries in time grid views).
     * <p>
     * It applies in addition to the general and the block entry class option of the same part.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setColumnEntryTitleClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has only {@code event}, {@code timeText}, {@code isNarrow},
     *                                  {@code isShort} and {@code resource} (resource views).<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setColumnEntryTitleClass("my-class");
     * calendar.options().setColumnEntryTitleClass(JsCallback.of("info => info.isNarrow ? 'my-narrow' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/column-event-render-hooks">columnEventTitleClass</a>
     */
    @JsonName("columnEventTitleClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue columnEntryTitleClass;

    /**
     * Sets fixed CSS classes for the title element within the "inner" wrapper of column entries, see
     * {@link #getColumnEntryTitleClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setColumnEntryTitleClass(String classNames) {
        this.columnEntryTitleClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the title element within the "inner" wrapper of column entries in any form, see
     * {@link #getColumnEntryTitleClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setColumnEntryTitleClass(ClassNameValue value) {
        this.columnEntryTitleClass = value;
    }

    /**
     * CSS classes for the outermost element of row entries.
     * <p>
     * Row entries are the entries drawn as horizontal bars: all-day entries and entries spanning several days in day
     * grid views, all-day entries in time grid views, and entries in timeline views. This hook applies in addition to
     * the general and the block entry hook.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setRowEntryClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: stable classes such as {@code fc-h-event} and
     *                                  {@code fc-daygrid-block-event}.<br>
     *                                  Classes set here are added to them.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code event}, {@code timeText}, {@code isStart} and more, see
     *                                  the library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setRowEntryClass("my-class");
     * calendar.options().setRowEntryClass(JsCallback.of("info => info.isPast ? 'my-past' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/row-event-render-hooks">rowEventClass</a>
     */
    @JsonName("rowEventClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue rowEntryClass;

    /**
     * Sets fixed CSS classes for the outermost element of row entries, see {@link #getRowEntryClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setRowEntryClass(String classNames) {
        this.rowEntryClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the outermost element of row entries in any form, see {@link #getRowEntryClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setRowEntryClass(ClassNameValue value) {
        this.rowEntryClass = value;
    }

    /**
     * CSS classes for the element before the "inner" wrapper of row entries.
     * <p>
     * Row entries are the entries drawn as horizontal bars: all-day entries and entries spanning several days in day
     * grid views, all-day entries in time grid views, and entries in timeline views. This hook applies in addition to
     * the general and the block entry hook.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setRowEntryBeforeClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code event}, {@code timeText}, {@code isStart} and more, see
     *                                  the library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setRowEntryBeforeClass("my-class");
     * calendar.options().setRowEntryBeforeClass(JsCallback.of("info => info.isPast ? 'my-past' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/row-event-render-hooks">rowEventBeforeClass</a>
     */
    @JsonName("rowEventBeforeClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue rowEntryBeforeClass;

    /**
     * Sets fixed CSS classes for the element before the "inner" wrapper of row entries, see
     * {@link #getRowEntryBeforeClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setRowEntryBeforeClass(String classNames) {
        this.rowEntryBeforeClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the element before the "inner" wrapper of row entries in any form, see
     * {@link #getRowEntryBeforeClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setRowEntryBeforeClass(ClassNameValue value) {
        this.rowEntryBeforeClass = value;
    }

    /**
     * Generated content inserted inside the element before the "inner" wrapper of row entries. It is hidden from
     * assistive technology. Useful for decorations such as an arrow showing that the entry continues from an earlier
     * date.
     * <p>
     * Row entries are the entries drawn as horizontal bars: all-day entries and entries spanning several days in day
     * grid views, all-day entries in time grid views, and entries in timeline views.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link TextContent}: shown as text.
     *                                  <p>{@link ContentValue#ofHtml(String)}: HTML, not escaped.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setRowEntryBeforeContent(String)}: a text, as {@link TextContent}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no content.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code event}, {@code timeText}, {@code isStart} and more, see
     *                                  the library docs.<br>
     *                                  It returns a text, {@code {html: '...'}} or {@code {domNodes: [...]}}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setRowEntryBeforeContent("<");
     * calendar.options().setRowEntryBeforeContent(ContentValue.ofHtml("<b>&laquo;</b>"));
     * calendar.options().setRowEntryBeforeContent(JsCallback.of("info => info.isStart ? '' : '<'"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/row-event-render-hooks">rowEventBeforeContent</a>
     */
    @JsonName("rowEventBeforeContent")
    @JsonConverter(ContentValueConverter.class)
    private ContentValue rowEntryBeforeContent;

    /**
     * Sets a text as the content before the "inner" wrapper of row entries, see {@link #getRowEntryBeforeContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setRowEntryBeforeContent(String text) {
        this.rowEntryBeforeContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content before the "inner" wrapper of row entries in any form, see {@link #getRowEntryBeforeContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setRowEntryBeforeContent(ContentValue value) {
        this.rowEntryBeforeContent = value;
    }

    /**
     * CSS classes for the element after the "inner" wrapper of row entries.
     * <p>
     * Row entries are the entries drawn as horizontal bars: all-day entries and entries spanning several days in day
     * grid views, all-day entries in time grid views, and entries in timeline views. This hook applies in addition to
     * the general and the block entry hook.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setRowEntryAfterClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code event}, {@code timeText}, {@code isStart} and more, see
     *                                  the library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setRowEntryAfterClass("my-class");
     * calendar.options().setRowEntryAfterClass(JsCallback.of("info => info.isPast ? 'my-past' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/row-event-render-hooks">rowEventAfterClass</a>
     */
    @JsonName("rowEventAfterClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue rowEntryAfterClass;

    /**
     * Sets fixed CSS classes for the element after the "inner" wrapper of row entries, see
     * {@link #getRowEntryAfterClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setRowEntryAfterClass(String classNames) {
        this.rowEntryAfterClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the element after the "inner" wrapper of row entries in any form, see
     * {@link #getRowEntryAfterClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setRowEntryAfterClass(ClassNameValue value) {
        this.rowEntryAfterClass = value;
    }

    /**
     * Generated content inserted inside the element after the "inner" wrapper of row entries. It is hidden from
     * assistive technology. Useful for decorations such as an arrow showing that the entry continues past a later date.
     * <p>
     * Row entries are the entries drawn as horizontal bars: all-day entries and entries spanning several days in day
     * grid views, all-day entries in time grid views, and entries in timeline views.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link TextContent}: shown as text.
     *                                  <p>{@link ContentValue#ofHtml(String)}: HTML, not escaped.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setRowEntryAfterContent(String)}: a text, as {@link TextContent}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no content.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code event}, {@code timeText}, {@code isStart} and more, see
     *                                  the library docs.<br>
     *                                  It returns a text, {@code {html: '...'}} or {@code {domNodes: [...]}}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setRowEntryAfterContent("Text");
     * calendar.options().setRowEntryAfterContent(ContentValue.ofHtml("<b>Text</b>"));
     * calendar.options().setRowEntryAfterContent(JsCallback.of("info => info.isEnd ? '' : '>'"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/row-event-render-hooks">rowEventAfterContent</a>
     */
    @JsonName("rowEventAfterContent")
    @JsonConverter(ContentValueConverter.class)
    private ContentValue rowEntryAfterContent;

    /**
     * Sets a text as the content after the "inner" wrapper of row entries, see {@link #getRowEntryAfterContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setRowEntryAfterContent(String text) {
        this.rowEntryAfterContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content after the "inner" wrapper of row entries in any form, see {@link #getRowEntryAfterContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setRowEntryAfterContent(ContentValue value) {
        this.rowEntryAfterContent = value;
    }

    /**
     * CSS classes for the "inner" wrapper element of row entries.
     * <p>
     * Row entries are the entries drawn as horizontal bars: all-day entries and entries spanning several days in day
     * grid views, all-day entries in time grid views, and entries in timeline views. This hook applies in addition to
     * the general and the block entry hook.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setRowEntryInnerClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code event}, {@code timeText}, {@code isStart} and more, see
     *                                  the library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setRowEntryInnerClass("my-row-inner");
     * calendar.options().setRowEntryInnerClass(JsCallback.of("info => info.isStart ? 'my-start' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/row-event-render-hooks">rowEventInnerClass</a>
     */
    @JsonName("rowEventInnerClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue rowEntryInnerClass;

    /**
     * Sets fixed CSS classes for the "inner" wrapper element of row entries, see {@link #getRowEntryInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setRowEntryInnerClass(String classNames) {
        this.rowEntryInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the "inner" wrapper element of row entries in any form, see
     * {@link #getRowEntryInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setRowEntryInnerClass(ClassNameValue value) {
        this.rowEntryInnerClass = value;
    }

    /**
     * CSS classes for the time element within the "inner" wrapper of row entries.
     * <p>
     * Row entries are the entries drawn as horizontal bars: all-day entries and entries spanning several days in day
     * grid views, all-day entries in time grid views, and entries in timeline views. This hook applies in addition to
     * the general and the block entry hook.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setRowEntryTimeClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has only {@code event}, {@code timeText}, {@code isNarrow},
     *                                  {@code isShort} and {@code resource} (resource views).<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setRowEntryTimeClass("my-row-time");
     * calendar.options().setRowEntryTimeClass(JsCallback.of("info => info.isNarrow ? 'my-narrow' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/row-event-render-hooks">rowEventTimeClass</a>
     */
    @JsonName("rowEventTimeClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue rowEntryTimeClass;

    /**
     * Sets fixed CSS classes for the time element within the "inner" wrapper of row entries, see
     * {@link #getRowEntryTimeClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setRowEntryTimeClass(String classNames) {
        this.rowEntryTimeClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the time element within the "inner" wrapper of row entries in any form, see
     * {@link #getRowEntryTimeClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setRowEntryTimeClass(ClassNameValue value) {
        this.rowEntryTimeClass = value;
    }

    /**
     * CSS classes for the title element within the "inner" wrapper of row entries.
     * <p>
     * Row entries are the entries drawn as horizontal bars: all-day entries and entries spanning several days in day
     * grid views, all-day entries in time grid views, and entries in timeline views. This hook applies in addition to
     * the general and the block entry hook.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setRowEntryTitleClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has only {@code event}, {@code timeText}, {@code isNarrow},
     *                                  {@code isShort} and {@code resource} (resource views).<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setRowEntryTitleClass("my-row-title");
     * calendar.options().setRowEntryTitleClass(JsCallback.of("info => info.isNarrow ? 'my-narrow' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/row-event-render-hooks">rowEventTitleClass</a>
     */
    @JsonName("rowEventTitleClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue rowEntryTitleClass;

    /**
     * Sets fixed CSS classes for the title element within the "inner" wrapper of row entries, see
     * {@link #getRowEntryTitleClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setRowEntryTitleClass(String classNames) {
        this.rowEntryTitleClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes for the title element within the "inner" wrapper of row entries in any form, see
     * {@link #getRowEntryTitleClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
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
     * The general entry class options do not apply to background entries.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setBackgroundEntryClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: stable classes such as {@code fc-bg-event}.<br>
     *                                  Classes set here are added to them.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code event}, {@code timeText}, {@code isStart} and more, see
     *                                  the library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setBackgroundEntryClass("my-background");
     * calendar.options().setBackgroundEntryClass(JsCallback.of("info => info.isStart ? 'my-start' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/background-event-render-hooks">backgroundEventClass</a>
     */
    @JsonName("backgroundEventClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue backgroundEntryClass;

    /**
     * Sets fixed CSS classes, see {@link #getBackgroundEntryClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setBackgroundEntryClass(String classNames) {
        this.backgroundEntryClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getBackgroundEntryClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setBackgroundEntryClass(ClassNameValue value) {
        this.backgroundEntryClass = value;
    }

    /**
     * Custom content for background entries. The generated content is inserted inside the inner-most wrapper of the
     * entry element.
     * <p>
     * A callback is called every time the entry data changes.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link TextContent}: shown as text.
     *                                  <p>{@link ContentValue#ofHtml(String)}: HTML, not escaped.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setBackgroundEntryContent(String)}: a text, as {@link TextContent}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: the title of the entry, if it has one.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code event}, {@code timeText}, {@code isStart} and more, see
     *                                  the library docs.<br>
     *                                  It returns a text, {@code {html: '...'}} or {@code {domNodes: [...]}}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setBackgroundEntryContent("Text");
     * calendar.options().setBackgroundEntryContent(ContentValue.ofHtml("<b>Text</b>"));
     * calendar.options().setBackgroundEntryContent(JsCallback.of("info => 'Closed: ' + info.event.title"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/background-event-render-hooks">backgroundEventContent</a>
     */
    @JsonName("backgroundEventContent")
    @JsonConverter(ContentValueConverter.class)
    private ContentValue backgroundEntryContent;

    /**
     * Sets a text as the content, see {@link #getBackgroundEntryContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setBackgroundEntryContent(String text) {
        this.backgroundEntryContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content in any form, see {@link #getBackgroundEntryContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setBackgroundEntryContent(ContentValue value) {
        this.backgroundEntryContent = value;
    }

    /**
     * A callback that runs right after the element of a background entry has been added to the DOM. It is not called
     * again when the entry data changes. It gets an {@code info} object with {@code event}, {@code timeText},
     * {@code isStart}, {@code el}, the element, and more, see the library docs.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The library default is no callback.
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
     * The general entry class options do not apply to background entries.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setBackgroundEntryInnerClass(String)}: class names separated by spaces,
     *                                  as {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code event}, {@code timeText}, {@code isStart} and more, see
     *                                  the library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setBackgroundEntryInnerClass("my-background-inner");
     * calendar.options().setBackgroundEntryInnerClass(JsCallback.of("info => info.isStart ? 'my-start' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/background-event-render-hooks">backgroundEventInnerClass</a>
     */
    @JsonName("backgroundEventInnerClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue backgroundEntryInnerClass;

    /**
     * Sets fixed CSS classes, see {@link #getBackgroundEntryInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setBackgroundEntryInnerClass(String classNames) {
        this.backgroundEntryInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getBackgroundEntryInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setBackgroundEntryInnerClass(ClassNameValue value) {
        this.backgroundEntryInnerClass = value;
    }

    /**
     * CSS classes for the title element inside the inner wrapper of a background entry.
     * <p>
     * The general entry class options do not apply to background entries.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setBackgroundEntryTitleClass(String)}: class names separated by spaces,
     *                                  as {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code event}, {@code timeText} (always empty),
     *                                  {@code isNarrow}, {@code isShort} and {@code resource} (resource views).<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setBackgroundEntryTitleClass("my-background-title");
     * calendar.options().setBackgroundEntryTitleClass(JsCallback.of("info => info.isNarrow ? 'my-narrow' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/background-event-render-hooks">backgroundEventTitleClass</a>
     */
    @JsonName("backgroundEventTitleClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue backgroundEntryTitleClass;

    /**
     * Sets fixed CSS classes, see {@link #getBackgroundEntryTitleClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setBackgroundEntryTitleClass(String classNames) {
        this.backgroundEntryTitleClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getBackgroundEntryTitleClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setBackgroundEntryTitleClass(ClassNameValue value) {
        this.backgroundEntryTitleClass = value;
    }

    /**
     * A callback that runs right before the element of a background entry is removed from the DOM. It gets an
     * {@code info} object with {@code event}, {@code timeText}, {@code isStart}, {@code el}, the element, and more, see
     * the library docs.
     * <p>
     * When this option is changed for an attached calendar, the callback that is set at the time an element is removed
     * runs for it. This includes elements that were rendered before the change.
     * <p>
     * The library default is no callback.
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
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setEntryAfterClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: the stable classes {@code fc-event-resizer} and
     *                                  {@code fc-event-resizer-end}, for entries that can be resized at their end.<br>
     *                                  Classes set here are added to them.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code event}, {@code timeText}, {@code isStart} and more, see
     *                                  the library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setEntryAfterClass("my-after");
     * calendar.options().setEntryAfterClass(JsCallback.of("info => info.isStart ? 'my-start' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/event-render-hooks">eventAfterClass</a>
     */
    @JsonName("eventAfterClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue entryAfterClass;

    /**
     * Sets fixed CSS classes, see {@link #getEntryAfterClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setEntryAfterClass(String classNames) {
        this.entryAfterClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getEntryAfterClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setEntryAfterClass(ClassNameValue value) {
        this.entryAfterClass = value;
    }

    /**
     * CSS classes for the element before the inner wrapper of an entry. Background entries are not affected.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setEntryBeforeClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: the stable classes {@code fc-event-resizer} and
     *                                  {@code fc-event-resizer-start}, for entries that can be resized at their
     *                                  start.<br>
     *                                  Classes set here are added to them.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code event}, {@code timeText}, {@code isStart} and more, see
     *                                  the library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setEntryBeforeClass("my-before");
     * calendar.options().setEntryBeforeClass(JsCallback.of("info => info.isStart ? 'my-start' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/event-render-hooks">eventBeforeClass</a>
     */
    @JsonName("eventBeforeClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue entryBeforeClass;

    /**
     * Sets fixed CSS classes, see {@link #getEntryBeforeClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setEntryBeforeClass(String classNames) {
        this.entryBeforeClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getEntryBeforeClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setEntryBeforeClass(ClassNameValue value) {
        this.entryBeforeClass = value;
    }

    /**
     * CSS classes for the outermost element of an entry. Background entries are not affected.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setEntryClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: stable classes such as {@code fc-event} and
     *                                  {@code fc-event-draggable}, in day grid views also {@code fc-daygrid-event}.<br>
     *                                  Classes set here are added to them.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code event}, {@code timeText}, {@code isStart} and more, see
     *                                  the library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setEntryClass("my-entry");
     * calendar.options().setEntryClass(JsCallback.of("info => info.isStart ? 'my-start' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/event-render-hooks">eventClass</a>
     */
    @JsonName("eventClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue entryClass;

    /**
     * Sets fixed CSS classes, see {@link #getEntryClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setEntryClass(String classNames) {
        this.entryClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getEntryClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setEntryClass(ClassNameValue value) {
        this.entryClass = value;
    }

    /**
     * Custom content for entries. Background entries are not affected. The generated content is inserted inside the
     * inner-most wrapper of the entry element.
     * <p>
     * A callback is called every time the entry data changes.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link TextContent}: shown as text.
     *                                  <p>{@link ContentValue#ofHtml(String)}: HTML, not escaped.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setEntryContent(String)}: a text, as {@link TextContent}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: none, and the themes set no entry content.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code event}, {@code timeText}, {@code isStart} and more, see
     *                                  the library docs.<br>
     *                                  It returns a text, {@code {html: '...'}} or {@code {domNodes: [...]}}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setEntryContent("Text");
     * calendar.options().setEntryContent(ContentValue.ofHtml("<b>Text</b>"));
     * calendar.options().setEntryContent(JsCallback.of("info => info.timeText + ' ' + info.event.title"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/event-render-hooks">eventContent</a>
     */
    @JsonName("eventContent")
    @JsonConverter(ContentValueConverter.class)
    private ContentValue entryContent;

    /**
     * Sets a text as the content, see {@link #getEntryContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setEntryContent(String text) {
        this.entryContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content in any form, see {@link #getEntryContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setEntryContent(ContentValue value) {
        this.entryContent = value;
    }

    /**
     * CSS classes for the inner wrapper of an entry. Background entries are not affected.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setEntryInnerClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: the stable class {@code fc-event-main}.<br>
     *                                  Classes set here are added to it.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code event}, {@code timeText}, {@code isStart} and more, see
     *                                  the library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setEntryInnerClass("my-inner");
     * calendar.options().setEntryInnerClass(JsCallback.of("info => info.isStart ? 'my-start' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/event-render-hooks">eventInnerClass</a>
     */
    @JsonName("eventInnerClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue entryInnerClass;

    /**
     * Sets fixed CSS classes, see {@link #getEntryInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setEntryInnerClass(String classNames) {
        this.entryInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getEntryInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setEntryInnerClass(ClassNameValue value) {
        this.entryInnerClass = value;
    }

    /**
     * CSS classes for the time element inside the inner wrapper of an entry. Background entries are not affected.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setEntryTimeClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: the stable class {@code fc-event-time}.<br>
     *                                  Classes set here are added to it.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code event}, {@code timeText}, {@code isNarrow},
     *                                  {@code isShort} and {@code resource} (resource views).<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setEntryTimeClass("my-time");
     * calendar.options().setEntryTimeClass(JsCallback.of("info => info.isNarrow ? 'my-narrow' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/event-render-hooks">eventTimeClass</a>
     */
    @JsonName("eventTimeClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue entryTimeClass;

    /**
     * Sets fixed CSS classes, see {@link #getEntryTimeClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setEntryTimeClass(String classNames) {
        this.entryTimeClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getEntryTimeClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setEntryTimeClass(ClassNameValue value) {
        this.entryTimeClass = value;
    }

    /**
     * CSS classes for the title element inside the inner wrapper of an entry. Background entries are not affected.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setEntryTitleClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: the stable class {@code fc-event-title}.<br>
     *                                  Classes set here are added to it.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code event}, {@code timeText}, {@code isNarrow},
     *                                  {@code isShort} and {@code resource} (resource views).<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setEntryTitleClass("my-title");
     * calendar.options().setEntryTitleClass(JsCallback.of("info => info.isNarrow ? 'my-narrow' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/event-render-hooks">eventTitleClass</a>
     */
    @JsonName("eventTitleClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue entryTitleClass;

    /**
     * Sets fixed CSS classes, see {@link #getEntryTitleClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setEntryTitleClass(String classNames) {
        this.entryTitleClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getEntryTitleClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setEntryTitleClass(ClassNameValue value) {
        this.entryTitleClass = value;
    }

    /**
     * A callback that runs right before the element of an entry is removed from the DOM. Background entries are not
     * affected. It gets an {@code info} object with {@code el} (the element), {@code event}, {@code timeText} and more,
     * see the library docs.
     * <p>
     * The library calls the callback that is set when an element is removed. When this option is changed for an
     * attached calendar, that includes elements that were rendered before the change. With a number as the limit of
     * entries per day ({@code dayMaxEntries}), the library renders only the entries that fit. An entry behind a "+N
     * more" link has no element until the popover renders it, so no callback runs for it before. With
     * {@link EntryLimitValue#CELL_HEIGHT} in day grid views, the library may render hidden entries invisibly to measure
     * them, so the callback can run for them too.
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/event-render-hooks">eventWillUnmount</a>
     */
    @JsonName("eventWillUnmount")
    private JsCallback entryWillUnmount;

    /**
     * CSS classes for the element after the inner wrapper of a list item entry, that is a timed entry within one day in
     * day grid views and an entry in list views. {@code entryDisplay} can change which entries are list items.
     * <p>
     * The classes apply in addition to the general entry classes. A theme can set classes of its own, and the classes
     * set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setListItemEntryAfterClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code event}, {@code timeText}, {@code isStart} and more, see
     *                                  the library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setListItemEntryAfterClass("my-li-after");
     * calendar.options().setListItemEntryAfterClass(JsCallback.of("info => info.isStart ? 'my-start' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/list-item-event-render-hooks">listItemEventAfterClass</a>
     */
    @JsonName("listItemEventAfterClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue listItemEntryAfterClass;

    /**
     * Sets fixed CSS classes, see {@link #getListItemEntryAfterClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setListItemEntryAfterClass(String classNames) {
        this.listItemEntryAfterClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getListItemEntryAfterClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setListItemEntryAfterClass(ClassNameValue value) {
        this.listItemEntryAfterClass = value;
    }

    /**
     * CSS classes for the element before the inner wrapper of a list item entry, that is a timed entry within one day
     * in day grid views and an entry in list views. {@code entryDisplay} can change which entries are list items.
     * <p>
     * The classes apply in addition to the general entry classes. A theme can set classes of its own, and the classes
     * set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setListItemEntryBeforeClass(String)}: class names separated by spaces,
     *                                  as {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: the stable class {@code fc-daygrid-event-dot} in day grid views and
     *                                  {@code fc-list-event-dot} in list views.<br>
     *                                  Classes set here are added to it.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code event}, {@code timeText}, {@code isStart} and more, see
     *                                  the library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setListItemEntryBeforeClass("my-li-before");
     * calendar.options().setListItemEntryBeforeClass(JsCallback.of("info => info.isStart ? 'my-start' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/list-item-event-render-hooks">listItemEventBeforeClass</a>
     */
    @JsonName("listItemEventBeforeClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue listItemEntryBeforeClass;

    /**
     * Sets fixed CSS classes, see {@link #getListItemEntryBeforeClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setListItemEntryBeforeClass(String classNames) {
        this.listItemEntryBeforeClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getListItemEntryBeforeClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setListItemEntryBeforeClass(ClassNameValue value) {
        this.listItemEntryBeforeClass = value;
    }

    /**
     * CSS classes for the outermost element of a list item entry, that is a timed entry within one day in day grid
     * views and an entry in list views. {@code entryDisplay} can change which entries are list items.
     * <p>
     * The classes apply in addition to the general entry classes. A theme can set classes of its own, and the classes
     * set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setListItemEntryClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: the stable class {@code fc-daygrid-dot-event} in day grid views and
     *                                  {@code fc-list-event} in list views.<br>
     *                                  Classes set here are added to it.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code event}, {@code timeText}, {@code isStart} and more, see
     *                                  the library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setListItemEntryClass("my-li");
     * calendar.options().setListItemEntryClass(JsCallback.of("info => info.isStart ? 'my-start' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/list-item-event-render-hooks">listItemEventClass</a>
     */
    @JsonName("listItemEventClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue listItemEntryClass;

    /**
     * Sets fixed CSS classes, see {@link #getListItemEntryClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setListItemEntryClass(String classNames) {
        this.listItemEntryClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getListItemEntryClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setListItemEntryClass(ClassNameValue value) {
        this.listItemEntryClass = value;
    }

    /**
     * CSS classes for the inner wrapper of a list item entry, that is a timed entry within one day in day grid views
     * and an entry in list views. {@code entryDisplay} can change which entries are list items.
     * <p>
     * The classes apply in addition to the general entry classes. A theme can set classes of its own, and the classes
     * set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setListItemEntryInnerClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code event}, {@code timeText}, {@code isStart} and more, see
     *                                  the library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setListItemEntryInnerClass("my-li-inner");
     * calendar.options().setListItemEntryInnerClass(JsCallback.of("info => info.isStart ? 'my-start' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/list-item-event-render-hooks">listItemEventInnerClass</a>
     */
    @JsonName("listItemEventInnerClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue listItemEntryInnerClass;

    /**
     * Sets fixed CSS classes, see {@link #getListItemEntryInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setListItemEntryInnerClass(String classNames) {
        this.listItemEntryInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getListItemEntryInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setListItemEntryInnerClass(ClassNameValue value) {
        this.listItemEntryInnerClass = value;
    }

    /**
     * CSS classes for the time element inside the inner wrapper of a list item entry, that is a timed entry within one
     * day in day grid views and an entry in list views. {@code entryDisplay} can change which entries are list items.
     * <p>
     * The classes apply in addition to the general entry classes. A theme can set classes of its own, and the classes
     * set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setListItemEntryTimeClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: in list views the stable class {@code fc-list-event-time}.<br>
     *                                  Classes set here are added to it.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code event}, {@code timeText}, {@code isNarrow},
     *                                  {@code isShort} and {@code resource} (resource views).<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setListItemEntryTimeClass("my-li-time");
     * calendar.options().setListItemEntryTimeClass(JsCallback.of("info => info.isNarrow ? 'my-narrow' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/list-item-event-render-hooks">listItemEventTimeClass</a>
     */
    @JsonName("listItemEventTimeClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue listItemEntryTimeClass;

    /**
     * Sets fixed CSS classes, see {@link #getListItemEntryTimeClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setListItemEntryTimeClass(String classNames) {
        this.listItemEntryTimeClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getListItemEntryTimeClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setListItemEntryTimeClass(ClassNameValue value) {
        this.listItemEntryTimeClass = value;
    }

    /**
     * CSS classes for the title element inside the inner wrapper of a list item entry, that is a timed entry within one
     * day in day grid views and an entry in list views. {@code entryDisplay} can change which entries are list items.
     * <p>
     * The classes apply in addition to the general entry classes. A theme can set classes of its own, and the classes
     * set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setListItemEntryTitleClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: in list views the stable class {@code fc-list-event-title}.<br>
     *                                  Classes set here are added to it.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code event}, {@code timeText}, {@code isNarrow},
     *                                  {@code isShort} and {@code resource} (resource views).<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setListItemEntryTitleClass("my-class");
     * calendar.options().setListItemEntryTitleClass(JsCallback.of("info => info.isNarrow ? 'my-narrow-title' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/list-item-event-render-hooks">listItemEventTitleClass</a>
     */
    @JsonName("listItemEventTitleClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue listItemEntryTitleClass;

    /**
     * Sets fixed CSS classes, see {@link #getListItemEntryTitleClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setListItemEntryTitleClass(String classNames) {
        this.listItemEntryTitleClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getListItemEntryTitleClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
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
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setButtonClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: stable classes such as {@code fc-button} and
     *                                  {@code fc-button-active}.<br>
     *                                  Classes set here are added to them.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code name}, {@code text}, {@code isPrimary},
     *                                  {@code isSelected}, {@code isDisabled}, {@code isIconOnly} and
     *                                  {@code buttonGroup}.<br>
     *                                  The latter is null for a button outside a group, else an object with
     *                                  {@code hasSelection}.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setButtonClass("my-class");
     * calendar.options().setButtonClass(JsCallback.of("info => info.isPrimary ? 'my-primary-button' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/toolbar-render-hooks">buttonClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue buttonClass;

    /**
     * Sets fixed CSS classes, see {@link #getButtonClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setButtonClass(String classNames) {
        this.buttonClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getButtonClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setButtonClass(ClassNameValue value) {
        this.buttonClass = value;
    }

    /**
     * CSS classes for each group of buttons in a toolbar section. A group is a set of buttons separated by commas in
     * the toolbar layout.
     * <p>
     * A group with only one button has no group element, so the class is not applied to it.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setButtonGroupClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code hasSelection}, which is true when only one button of the
     *                                  group can be active at a time, like view buttons.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setButtonGroupClass("my-class");
     * calendar.options().setButtonGroupClass(JsCallback.of("info => info.hasSelection ? 'my-view-buttons' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/toolbar-render-hooks">buttonGroupClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue buttonGroupClass;

    /**
     * Sets fixed CSS classes, see {@link #getButtonGroupClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setButtonGroupClass(String classNames) {
        this.buttonGroupClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getButtonGroupClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
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
     * The library default is no classes. A theme can set classes of its own, and the classes set here are added to
     * them.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/navLinkClass">navLinkClass</a>
     */
    private String navLinkClass;

    /**
     * CSS classes for each single month of multi-month views.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setSingleMonthClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: the stable class {@code fc-multimonth-month}.<br>
     *                                  Classes set here are added to it.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code multiMonthColumns} (0 until the layout is measured),
     *                                  {@code isFirst} and {@code isLast}.<br>
     *                                  The library docs also list {@code date}, which the used library version does not
     *                                  pass.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setSingleMonthClass("my-class");
     * calendar.options().setSingleMonthClass(JsCallback.of("info => info.isFirst ? 'my-first-month' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/single-month-render-hooks">singleMonthClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue singleMonthClass;

    /**
     * Sets fixed CSS classes, see {@link #getSingleMonthClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setSingleMonthClass(String classNames) {
        this.singleMonthClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getSingleMonthClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
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
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/single-month-render-hooks">singleMonthDidMount</a>
     */
    private JsCallback singleMonthDidMount;

    /**
     * CSS classes for the month header (the title above each month grid) of multi-month views.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setSingleMonthHeaderClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: the stable class {@code fc-multimonth-title}.<br>
     *                                  Classes set here are added to it.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code multiMonthColumns}, {@code isSticky}, {@code isNarrow}
     *                                  and {@code hasNavLink}.<br>
     *                                  The library docs also list {@code date}, which the used library version does not
     *                                  pass.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setSingleMonthHeaderClass("my-class");
     * calendar.options().setSingleMonthHeaderClass(JsCallback.of("info => info.isSticky ? 'my-sticky-header' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/single-month-header-render-hooks">singleMonthHeaderClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue singleMonthHeaderClass;

    /**
     * Sets fixed CSS classes, see {@link #getSingleMonthHeaderClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setSingleMonthHeaderClass(String classNames) {
        this.singleMonthHeaderClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getSingleMonthHeaderClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setSingleMonthHeaderClass(ClassNameValue value) {
        this.singleMonthHeaderClass = value;
    }

    /**
     * CSS classes for the inner element of the month header of multi-month views. This element may be a nav link.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setSingleMonthHeaderInnerClass(String)}: class names separated by
     *                                  spaces, as {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code multiMonthColumns}, {@code isSticky}, {@code isNarrow}
     *                                  and {@code hasNavLink}.<br>
     *                                  The library docs also list {@code date}, which the used library version does not
     *                                  pass.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setSingleMonthHeaderInnerClass("my-class");
     * calendar.options().setSingleMonthHeaderInnerClass(JsCallback.of("info => info.hasNavLink ? 'my-nav' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/single-month-header-render-hooks">singleMonthHeaderInnerClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue singleMonthHeaderInnerClass;

    /**
     * Sets fixed CSS classes, see {@link #getSingleMonthHeaderInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setSingleMonthHeaderInnerClass(String classNames) {
        this.singleMonthHeaderInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getSingleMonthHeaderInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
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
     * When this option is changed for an attached calendar, the callback that is set at the time an element is removed
     * runs for it. This includes elements that were rendered before the change.
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/single-month-render-hooks">singleMonthWillUnmount</a>
     */
    private JsCallback singleMonthWillUnmount;

    /**
     * CSS classes for the container of the built-in toolbars (header and footer).
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setToolbarClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: the stable class {@code fc-toolbar}.<br>
     *                                  Classes set here are added to it.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code borderlessX}, {@code borderlessTop} and
     *                                  {@code borderlessBottom}.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setToolbarClass("my-class");
     * calendar.options().setToolbarClass(JsCallback.of("info => info.borderlessX ? 'my-flush-toolbar' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/toolbar-render-hooks">toolbarClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue toolbarClass;

    /**
     * Sets fixed CSS classes, see {@link #getToolbarClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setToolbarClass(String classNames) {
        this.toolbarClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getToolbarClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setToolbarClass(ClassNameValue value) {
        this.toolbarClass = value;
    }

    /**
     * CSS classes for each section (start, center, end) of the built-in toolbars.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setToolbarSectionClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code name}, the section: {@code 'start'}, {@code 'center'} or
     *                                  {@code 'end'}.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setToolbarSectionClass("my-class");
     * calendar.options().setToolbarSectionClass(JsCallback.of("info => info.name === 'center' ? 'my-title' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/toolbar-render-hooks">toolbarSectionClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue toolbarSectionClass;

    /**
     * Sets fixed CSS classes, see {@link #getToolbarSectionClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setToolbarSectionClass(String classNames) {
        this.toolbarSectionClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getToolbarSectionClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
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
     * The library default is no classes. A theme can set classes of its own, and the classes set here are added to
     * them.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/toolbar-render-hooks">toolbarTitleClass</a>
     */
    private String toolbarTitleClass;

    // Render hooks: list view, more link, no entries, popover

    /**
     * CSS classes for the column more-link element. A column more-link appears in time grid views when too many timed
     * entries overlap within a day column.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setColumnMoreLinkClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: the stable class {@code fc-timegrid-more-link}.<br>
     *                                  Classes set here are added to it.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code num}, {@code numericText}, {@code longText},
     *                                  {@code text}, {@code isNarrow}, {@code view}, {@code resource} (resource views)
     *                                  and more, see the library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setColumnMoreLinkClass("my-class");
     * calendar.options().setColumnMoreLinkClass(JsCallback.of("info => info.num > 5 ? 'my-many-hidden' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/column-more-link-render-hooks">columnMoreLinkClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue columnMoreLinkClass;

    /**
     * Sets fixed CSS classes, see {@link #getColumnMoreLinkClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setColumnMoreLinkClass(String classNames) {
        this.columnMoreLinkClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getColumnMoreLinkClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setColumnMoreLinkClass(ClassNameValue value) {
        this.columnMoreLinkClass = value;
    }

    /**
     * CSS classes for the inner wrapper of the column more-link. Useful for adjusting padding.
     * <p>
     * A column more-link appears in time grid views when too many timed entries overlap within a day column.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setColumnMoreLinkInnerClass(String)}: class names separated by spaces,
     *                                  as {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code num}, {@code numericText}, {@code longText},
     *                                  {@code text}, {@code isNarrow}, {@code view}, {@code resource} (resource views)
     *                                  and more, see the library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setColumnMoreLinkInnerClass("my-class");
     * calendar.options().setColumnMoreLinkInnerClass(JsCallback.of("info => info.num > 5 ? 'my-many-hidden' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/column-more-link-render-hooks">columnMoreLinkInnerClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue columnMoreLinkInnerClass;

    /**
     * Sets fixed CSS classes, see {@link #getColumnMoreLinkInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setColumnMoreLinkInnerClass(String classNames) {
        this.columnMoreLinkInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getColumnMoreLinkInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setColumnMoreLinkInnerClass(ClassNameValue value) {
        this.columnMoreLinkInnerClass = value;
    }

    /**
     * CSS classes for the element that wraps the day sections of list views.
     * <p>
     * It takes class names only, because the library accepts no callback for this option.
     * <p>
     * The library default is no classes. A theme can set classes of its own, and the classes set here are added to
     * them.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/list-day-render-hooks">listDaysClass</a>
     */
    private String listDaysClass;

    /**
     * CSS classes for the element that wraps the entries of a single day in list views.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setListDayBodyClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code date} (a JS Date), {@code dow}, {@code isDisabled},
     *                                  {@code isOther}, {@code isToday}, {@code isPast}, {@code isFuture} and
     *                                  {@code view}.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setListDayBodyClass("my-class");
     * calendar.options().setListDayBodyClass(JsCallback.of("info => info.isToday ? 'my-today-body' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/list-day-render-hooks">listDayBodyClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue listDayBodyClass;

    /**
     * Sets fixed CSS classes, see {@link #getListDayBodyClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setListDayBodyClass(String classNames) {
        this.listDayBodyClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getListDayBodyClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setListDayBodyClass(ClassNameValue value) {
        this.listDayBodyClass = value;
    }

    /**
     * CSS classes for the element that wraps a day section in list views. A day section consists of the day header row
     * and the entries.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setListDayClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code date}, {@code dow}, {@code isDisabled}, {@code isOther},
     *                                  {@code isToday}, {@code isPast}, {@code isFuture}, {@code isFirst},
     *                                  {@code isLast} and {@code view}.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setListDayClass("my-class");
     * calendar.options().setListDayClass(JsCallback.of("info => info.isFirst ? 'my-first-day' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/list-day-render-hooks">listDayClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue listDayClass;

    /**
     * Sets fixed CSS classes, see {@link #getListDayClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setListDayClass(String classNames) {
        this.listDayClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getListDayClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setListDayClass(ClassNameValue value) {
        this.listDayClass = value;
    }

    /**
     * CSS classes for list view day headings (the row that shows the date above a group of entries).
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setListDayHeaderClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: the stable class {@code fc-list-day}.<br>
     *                                  Classes set here are added to it.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code date}, {@code dow}, {@code isDisabled}, {@code isOther},
     *                                  {@code isToday}, {@code isPast}, {@code isFuture} and {@code view}.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setListDayHeaderClass("my-class");
     * calendar.options().setListDayHeaderClass(JsCallback.of("info => info.isToday ? 'my-today-heading' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/list-day-header-render-hooks">listDayHeaderClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue listDayHeaderClass;

    /**
     * Sets fixed CSS classes, see {@link #getListDayHeaderClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setListDayHeaderClass(String classNames) {
        this.listDayHeaderClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getListDayHeaderClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setListDayHeaderClass(ClassNameValue value) {
        this.listDayHeaderClass = value;
    }

    /**
     * Content for the texts of a list view day heading. The library calls it once per text, with {@code level} 0 for
     * the primary day format and 1 for the alternative day format.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link TextContent}: shown as text.
     *                                  <p>{@link ContentValue#ofHtml(String)}: HTML, not escaped.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setListDayHeaderContent(String)}: a text, as {@link TextContent}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: the text of the day formats, see {@link #getListDayFormat()} and
     *                                  {@link #getListDayAltFormat()}.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code date}, {@code dow}, {@code isDisabled}, {@code isOther},
     *                                  {@code isToday}, {@code isPast}, {@code isFuture}, {@code view}, {@code text},
     *                                  {@code textParts}, {@code weekdayText}, {@code dayNumberText},
     *                                  {@code monthText}, {@code hasNavLink} and {@code level}.<br>
     *                                  It returns a text, {@code {html: '...'}} or {@code {domNodes: [...]}}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setListDayHeaderContent("Day");
     * calendar.options().setListDayHeaderContent(ContentValue.ofHtml("<b>Day</b>"));
     * calendar.options().setListDayHeaderContent(JsCallback.of("info => info.level === 0 ? info.text : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/list-day-header-render-hooks">listDayHeaderContent</a>
     */
    @JsonConverter(ContentValueConverter.class)
    private ContentValue listDayHeaderContent;

    /**
     * Sets a text as the content of the texts of a list view day heading, see {@link #getListDayHeaderContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setListDayHeaderContent(String text) {
        this.listDayHeaderContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content of the texts of a list view day heading in any form, see {@link #getListDayHeaderContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setListDayHeaderContent(ContentValue value) {
        this.listDayHeaderContent = value;
    }

    /**
     * A callback that runs right after a list view day heading (the row that shows the date above a group of entries)
     * has been added to the DOM. It gets an {@code info} object with {@code el} (the element), {@code date},
     * {@code isToday} and more, see the library docs.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/list-day-header-render-hooks">listDayHeaderDidMount</a>
     */
    private JsCallback listDayHeaderDidMount;

    /**
     * CSS classes for each side of the day header in list views, the left and the right half.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setListDayHeaderInnerClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: the stable classes {@code fc-list-day-cushion} and, depending on the
     *                                  side, {@code fc-list-day-text} or {@code fc-list-day-side-text}.<br>
     *                                  Classes set here are added to them.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code date}, {@code dow}, {@code isDisabled}, {@code isOther},
     *                                  {@code isToday}, {@code isPast}, {@code isFuture}, {@code view}, {@code text},
     *                                  {@code textParts}, {@code weekdayText}, {@code dayNumberText},
     *                                  {@code monthText}, {@code hasNavLink} and {@code level}.<br>
     *                                  {@code level} is 0 for the primary day format side and 1 for the alternative
     *                                  format side.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setListDayHeaderInnerClass("my-class");
     * calendar.options().setListDayHeaderInnerClass(JsCallback.of("info => info.level === 0 ? 'my-left' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/list-day-header-render-hooks">listDayHeaderInnerClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue listDayHeaderInnerClass;

    /**
     * Sets fixed CSS classes, see {@link #getListDayHeaderInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setListDayHeaderInnerClass(String classNames) {
        this.listDayHeaderInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getListDayHeaderInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setListDayHeaderInnerClass(ClassNameValue value) {
        this.listDayHeaderInnerClass = value;
    }

    /**
     * A callback that runs right before a list view day heading (the row that shows the date above a group of entries)
     * is removed from the DOM. It gets an {@code info} object with {@code el} (the element), {@code date},
     * {@code isToday} and more, see the library docs.
     * <p>
     * When this option is changed for an attached calendar, the callback that is set at the time an element is removed
     * runs for it. This includes elements that were rendered before the change.
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/list-day-header-render-hooks">listDayHeaderWillUnmount</a>
     */
    private JsCallback listDayHeaderWillUnmount;

    /**
     * CSS classes for the "+N more" link in every view. {@link #getColumnMoreLinkClass() columnMoreLinkClass} and
     * {@link #getRowMoreLinkClass() rowMoreLinkClass} add classes for the links of one layout only.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setMoreLinkClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: the stable class {@code fc-more-link}.<br>
     *                                  Classes set here are added to it.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code num}, {@code numericText}, {@code longText},
     *                                  {@code text}, {@code isNarrow}, {@code view}, {@code resource} (resource views)
     *                                  and more, see the library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setMoreLinkClass("my-class");
     * calendar.options().setMoreLinkClass(JsCallback.of("info => info.num > 5 ? 'my-many-hidden' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/more-link-render-hooks">moreLinkClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue moreLinkClass;

    /**
     * Sets fixed CSS classes, see {@link #getMoreLinkClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setMoreLinkClass(String classNames) {
        this.moreLinkClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getMoreLinkClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setMoreLinkClass(ClassNameValue value) {
        this.moreLinkClass = value;
    }

    /**
     * Content for the "+N more" link.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link TextContent}: shown as text.
     *                                  <p>{@link ContentValue#ofHtml(String)}: HTML, not escaped.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setMoreLinkContent(String)}: a text, as {@link TextContent}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: the library's own text, see {@link #getMoreLinkText()}.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code num}, {@code numericText}, {@code longText},
     *                                  {@code text}, {@code isNarrow}, {@code view}, {@code resource} (resource views)
     *                                  and more, see the library docs.<br>
     *                                  It returns a text, {@code {html: '...'}} or {@code {domNodes: [...]}}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setMoreLinkContent("More");
     * calendar.options().setMoreLinkContent(ContentValue.ofHtml("<b>More</b>"));
     * calendar.options().setMoreLinkContent(JsCallback.of("info => info.numericText + ' more'"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/more-link-render-hooks">moreLinkContent</a>
     */
    @JsonConverter(ContentValueConverter.class)
    private ContentValue moreLinkContent;

    /**
     * Sets a text as the content of the "+N more" link, see {@link #getMoreLinkContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setMoreLinkContent(String text) {
        this.moreLinkContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content of the "+N more" link in any form, see {@link #getMoreLinkContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setMoreLinkContent(ContentValue value) {
        this.moreLinkContent = value;
    }

    /**
     * A callback that runs right after a "+N more" link has been added to the DOM. It gets an {@code info} object with
     * {@code el} (the element), {@code num}, {@code text}, {@code numericText} and more, see the library docs.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/more-link-render-hooks">moreLinkDidMount</a>
     */
    private JsCallback moreLinkDidMount;

    /**
     * CSS classes for the inner wrapper of the "+N more" link. Useful for adjusting padding.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setMoreLinkInnerClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code num}, {@code numericText}, {@code longText},
     *                                  {@code text}, {@code isNarrow}, {@code view}, {@code resource} (resource views)
     *                                  and more, see the library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setMoreLinkInnerClass("my-class");
     * calendar.options().setMoreLinkInnerClass(JsCallback.of("info => info.num > 5 ? 'my-many-hidden' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/more-link-render-hooks">moreLinkInnerClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue moreLinkInnerClass;

    /**
     * Sets fixed CSS classes, see {@link #getMoreLinkInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setMoreLinkInnerClass(String classNames) {
        this.moreLinkInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getMoreLinkInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setMoreLinkInnerClass(ClassNameValue value) {
        this.moreLinkInnerClass = value;
    }

    /**
     * A callback that runs right before a "+N more" link is removed from the DOM. It gets an {@code info} object with
     * {@code el} (the element), {@code num}, {@code text}, {@code numericText} and more, see the library docs.
     * <p>
     * When this option is changed for an attached calendar, the callback that is set at the time an element is removed
     * runs for it. This includes elements that were rendered before the change.
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/more-link-render-hooks">moreLinkWillUnmount</a>
     */
    private JsCallback moreLinkWillUnmount;

    /**
     * CSS classes for the "No events to display" message of list views.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setNoEntriesClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: the stable class {@code fc-list-empty}.<br>
     *                                  Classes set here are added to it.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code text} (the message text, from
     *                                  {@link #getNoEntriesText() noEntriesText}) and {@code view}.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setNoEntriesClass("my-class");
     * calendar.options().setNoEntriesClass(JsCallback.of("info => info.view.type === 'listWeek' ? 'my-week' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/no-events-render-hooks">noEventsClass</a>
     */
    @JsonName("noEventsClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue noEntriesClass;

    /**
     * Sets fixed CSS classes, see {@link #getNoEntriesClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setNoEntriesClass(String classNames) {
        this.noEntriesClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getNoEntriesClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setNoEntriesClass(ClassNameValue value) {
        this.noEntriesClass = value;
    }

    /**
     * Content for the "No events to display" message of list views.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link TextContent}: shown as text.
     *                                  <p>{@link ContentValue#ofHtml(String)}: HTML, not escaped.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setNoEntriesContent(String)}: a text, as {@link TextContent}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: the message text, see {@link #getNoEntriesText() noEntriesText}.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code text} (the message text, from
     *                                  {@link #getNoEntriesText() noEntriesText}) and {@code view}.<br>
     *                                  It returns a text, {@code {html: '...'}} or {@code {domNodes: [...]}}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setNoEntriesContent("Nothing here");
     * calendar.options().setNoEntriesContent(ContentValue.ofHtml("<b>Nothing here</b>"));
     * calendar.options().setNoEntriesContent(JsCallback.of("info => info.text.toUpperCase()"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/no-events-render-hooks">noEventsContent</a>
     */
    @JsonName("noEventsContent")
    @JsonConverter(ContentValueConverter.class)
    private ContentValue noEntriesContent;

    /**
     * Sets a text as the content of the "No events to display" message, see {@link #getNoEntriesContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setNoEntriesContent(String text) {
        this.noEntriesContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content of the "No events to display" message in any form, see {@link #getNoEntriesContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setNoEntriesContent(ContentValue value) {
        this.noEntriesContent = value;
    }

    /**
     * A callback that runs right after the "No events to display" message of list views has been added to the DOM. It
     * gets an {@code info} object with {@code el} (the element), {@code text} (the message text, from
     * {@link #getNoEntriesText() noEntriesText}) and {@code view}.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/no-events-render-hooks">noEventsDidMount</a>
     */
    @JsonName("noEventsDidMount")
    private JsCallback noEntriesDidMount;

    /**
     * CSS classes for the inner padding element of the "No events to display" message of list views.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setNoEntriesInnerClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code text} (the message text, from
     *                                  {@link #getNoEntriesText() noEntriesText}) and {@code view}.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setNoEntriesInnerClass("my-class");
     * calendar.options().setNoEntriesInnerClass(JsCallback.of("info => info.view.type === 'listWeek' ? 'wk' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/no-events-render-hooks">noEventsInnerClass</a>
     */
    @JsonName("noEventsInnerClass")
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue noEntriesInnerClass;

    /**
     * Sets fixed CSS classes, see {@link #getNoEntriesInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setNoEntriesInnerClass(String classNames) {
        this.noEntriesInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getNoEntriesInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setNoEntriesInnerClass(ClassNameValue value) {
        this.noEntriesInnerClass = value;
    }

    /**
     * A callback that runs right before the "No events to display" message of list views is removed from the DOM. It
     * gets an {@code info} object with {@code el} (the element), {@code text} (the message text, from
     * {@link #getNoEntriesText() noEntriesText}) and {@code view}.
     * <p>
     * When this option is changed for an attached calendar, the callback that is set at the time an element is removed
     * runs for it. This includes elements that were rendered before the change.
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/no-events-render-hooks">noEventsWillUnmount</a>
     */
    @JsonName("noEventsWillUnmount")
    private JsCallback noEntriesWillUnmount;

    /**
     * CSS classes for the outermost element of the popover that opens when the user clicks a more-link.
     * <p>
     * It takes class names only, because the library accepts no callback for this option.
     * <p>
     * The library default is no classes. A theme can set classes of its own, and the classes set here are added to
     * them.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/popover-render-hooks">popoverClass</a>
     */
    private String popoverClass;

    /**
     * CSS classes for the close button of the popover that opens when the user clicks a more-link.
     * <p>
     * It takes class names only, because the library accepts no callback for this option.
     * <p>
     * The library default is no classes. A theme can set classes of its own, and the classes set here are added to
     * them.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/popover-render-hooks">popoverCloseClass</a>
     */
    private String popoverCloseClass;

    /**
     * Content for the close button of the popover that opens when the user clicks a more-link. The generated content is
     * inserted inside the button, typically an icon. The accessible label of the button comes from
     * {@link #getCloseHint() closeHint}.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link TextContent}: shown as text.
     *                                  <p>{@link ContentValue#ofHtml(String)}: HTML, not escaped.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setPopoverCloseContent(String)}: a text, as {@link TextContent}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no content.<br>
     *                                  The themes of the library supply their own icon.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has no properties, because the library declares none.<br>
     *                                  It returns a text, {@code {html: '...'}} or {@code {domNodes: [...]}}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setPopoverCloseContent("x");
     * calendar.options().setPopoverCloseContent(ContentValue.ofHtml("<b>&times;</b>"));
     * calendar.options().setPopoverCloseContent(JsCallback.of("() => 'Close'"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/popover-render-hooks">popoverCloseContent</a>
     */
    @JsonConverter(ContentValueConverter.class)
    private ContentValue popoverCloseContent;

    /**
     * Sets a text as the content of the close button of the popover, see {@link #getPopoverCloseContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setPopoverCloseContent(String text) {
        this.popoverCloseContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content of the close button of the popover in any form, see {@link #getPopoverCloseContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setPopoverCloseContent(ContentValue value) {
        this.popoverCloseContent = value;
    }

    /**
     * CSS classes for the row more-link element. A row more-link appears in row-based layouts such as day grid and
     * timeline rows when too many entries would otherwise be shown.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setRowMoreLinkClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: the stable class {@code fc-daygrid-more-link} in day grid, time grid
     *                                  and multi-month views.<br>
     *                                  Classes set here are added to it.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code num}, {@code numericText}, {@code longText},
     *                                  {@code text}, {@code isNarrow}, {@code view}, {@code resource} (resource views)
     *                                  and more, see the library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setRowMoreLinkClass("my-more-link");
     * calendar.options().setRowMoreLinkClass(JsCallback.of("info => info.num > 5 ? 'many-hidden' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/row-more-link-render-hooks">rowMoreLinkClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue rowMoreLinkClass;

    /**
     * Sets fixed CSS classes, see {@link #getRowMoreLinkClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setRowMoreLinkClass(String classNames) {
        this.rowMoreLinkClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getRowMoreLinkClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setRowMoreLinkClass(ClassNameValue value) {
        this.rowMoreLinkClass = value;
    }

    /**
     * CSS classes for the inner wrapper of the row more-link. Useful for adjusting padding. A row more-link appears in
     * row-based layouts such as day grid and timeline rows when too many entries would otherwise be shown.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setRowMoreLinkInnerClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code num}, {@code numericText}, {@code longText},
     *                                  {@code text}, {@code isNarrow}, {@code view}, {@code resource} (resource views)
     *                                  and more, see the library docs.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setRowMoreLinkInnerClass("my-more-link");
     * calendar.options().setRowMoreLinkInnerClass(JsCallback.of("info => info.num > 5 ? 'many-hidden' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/row-more-link-render-hooks">rowMoreLinkInnerClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue rowMoreLinkInnerClass;

    /**
     * Sets fixed CSS classes, see {@link #getRowMoreLinkInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setRowMoreLinkInnerClass(String classNames) {
        this.rowMoreLinkInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getRowMoreLinkInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setRowMoreLinkInnerClass(ClassNameValue value) {
        this.rowMoreLinkInnerClass = value;
    }

    // Dates, ranges and durations

    /**
     * How the duration of an entry changes when it is dragged between the timed and the all-day section.
     * <p>
     * With {@code true}, the duration stays roughly the same (hourly durations are rounded down to whole days).
     * With {@code false}, the duration is reset to the default all-day or timed entry duration of the target
     * section.
     * <p>
     * The library default is {@code false}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/allDayMaintainDuration">allDayMaintainDuration</a>
     */
    private Boolean allDayMaintainDuration;

    /**
     * Determines the first visible day of a custom view, such as {@code "week"} or {@code "month"}, so the view starts
     * at the start of that interval. The library default is no value. Then the library generates it from the duration
     * of the view. The library ignores it when {@code visibleRange} defines the range of the view.
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
    @JsonConverter(CalendarDurationConverter.class)
    private CalendarDuration dateIncrement;

    /**
     * Sets a time-based duration, see {@link #getDateIncrement()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param duration the duration, days count as 24 hours, or null to remove the option
     */
    public void setDateIncrement(Duration duration) {
        this.dateIncrement = duration == null ? null : CalendarDuration.of(duration);
    }

    /**
     * Sets a calendar-based duration in years, months and days, see {@link #getDateIncrement()}.
     * <p>
     * A {@link Period} has no weeks, so {@code Period.ofWeeks(n)} arrives as {@code n * 7} days, without the
     * alignment to the start of the week. For weeks, use {@link CalendarDuration#ofWeeks(int)}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param period the years, months and days, or null to remove the option
     */
    public void setDateIncrement(Period period) {
        this.dateIncrement = period == null ? null : CalendarDuration.of(period);
    }

    /**
     * Sets a duration in the string format {@code [-][d.]hh:mm[:ss[.sss]]}, see {@link #getDateIncrement()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the duration, such as {@code "08:30"} or {@code "1.12:00"}, or null to remove the option
     */
    public void setDateIncrement(String text) {
        this.dateIncrement = text == null ? null : CalendarDuration.ofTimeString(text);
    }

    /**
     * Sets the duration, see {@link #getDateIncrement()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the duration, or null to remove the option
     */
    public void setDateIncrement(CalendarDuration value) {
        this.dateIncrement = value;
    }

    /**
     * The exact number of days a custom view shows, regardless of {@code weekends} and {@code hiddenDays}. With a
     * {@code duration} instead, hidden days are omitted and the view stretches to fill the space. It has no effect on a
     * view that has a duration, such as the built-in {@code timeGridWeek}.
     * <p>
     * The library default is no value.
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
     * The library default is {@code true}. The getter returns null while the option is not set; then the library
     * default applies.
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
     * The library has no default for this option. The getter returns null while the option is not set; then the library
     * default applies.
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
    @JsonConverter(CalendarDurationConverter.class)
    @JsonName("defaultAllDayEventDuration")
    private CalendarDuration defaultAllDayEntryDuration;

    /**
     * Sets a time-based duration, see {@link #getDefaultAllDayEntryDuration()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param duration the duration, days count as 24 hours, or null to remove the option
     */
    public void setDefaultAllDayEntryDuration(Duration duration) {
        this.defaultAllDayEntryDuration = duration == null ? null : CalendarDuration.of(duration);
    }

    /**
     * Sets a calendar-based duration in years, months and days, see {@link #getDefaultAllDayEntryDuration()}.
     * <p>
     * A {@link Period} has no weeks, so {@code Period.ofWeeks(n)} arrives as {@code n * 7} days, without the
     * alignment to the start of the week. For weeks, use {@link CalendarDuration#ofWeeks(int)}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param period the years, months and days, or null to remove the option
     */
    public void setDefaultAllDayEntryDuration(Period period) {
        this.defaultAllDayEntryDuration = period == null ? null : CalendarDuration.of(period);
    }

    /**
     * Sets a duration in the string format {@code [-][d.]hh:mm[:ss[.sss]]},
     * see {@link #getDefaultAllDayEntryDuration()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the duration, such as {@code "08:30"} or {@code "1.12:00"}, or null to remove the option
     */
    public void setDefaultAllDayEntryDuration(String text) {
        this.defaultAllDayEntryDuration = text == null ? null : CalendarDuration.ofTimeString(text);
    }

    /**
     * Sets the duration, see {@link #getDefaultAllDayEntryDuration()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the duration, or null to remove the option
     */
    public void setDefaultAllDayEntryDuration(CalendarDuration value) {
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
    @JsonConverter(CalendarDurationConverter.class)
    @JsonName("defaultTimedEventDuration")
    private CalendarDuration defaultTimedEntryDuration;

    /**
     * Sets a time-based duration, see {@link #getDefaultTimedEntryDuration()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param duration the duration, days count as 24 hours, or null to remove the option
     */
    public void setDefaultTimedEntryDuration(Duration duration) {
        this.defaultTimedEntryDuration = duration == null ? null : CalendarDuration.of(duration);
    }

    /**
     * Sets a calendar-based duration in years, months and days, see {@link #getDefaultTimedEntryDuration()}.
     * <p>
     * A {@link Period} has no weeks, so {@code Period.ofWeeks(n)} arrives as {@code n * 7} days, without the
     * alignment to the start of the week. For weeks, use {@link CalendarDuration#ofWeeks(int)}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param period the years, months and days, or null to remove the option
     */
    public void setDefaultTimedEntryDuration(Period period) {
        this.defaultTimedEntryDuration = period == null ? null : CalendarDuration.of(period);
    }

    /**
     * Sets a duration in the string format {@code [-][d.]hh:mm[:ss[.sss]]},
     * see {@link #getDefaultTimedEntryDuration()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the duration, such as {@code "08:30"} or {@code "1.12:00"}, or null to remove the option
     */
    public void setDefaultTimedEntryDuration(String text) {
        this.defaultTimedEntryDuration = text == null ? null : CalendarDuration.ofTimeString(text);
    }

    /**
     * Sets the duration, see {@link #getDefaultTimedEntryDuration()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the duration, or null to remove the option
     */
    public void setDefaultTimedEntryDuration(CalendarDuration value) {
        this.defaultTimedEntryDuration = value;
    }

    /**
     * The exact duration of a custom view. A duration in weeks, such as {@code CalendarDuration.ofWeeks(1)}, aligns
     * the view to the start of the week. A duration of seven days does not.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/duration">duration</a>
     */
    @JsonConverter(CalendarDurationConverter.class)
    private CalendarDuration duration;

    /**
     * Sets a time-based duration, see {@link #getDuration()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param duration the duration, days count as 24 hours, or null to remove the option
     */
    public void setDuration(Duration duration) {
        this.duration = duration == null ? null : CalendarDuration.of(duration);
    }

    /**
     * Sets a calendar-based duration in years, months and days, see {@link #getDuration()}.
     * <p>
     * A {@link Period} has no weeks, so {@code Period.ofWeeks(n)} arrives as {@code n * 7} days, without the
     * alignment to the start of the week. For weeks, use {@link CalendarDuration#ofWeeks(int)}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param period the years, months and days, or null to remove the option
     */
    public void setDuration(Period period) {
        this.duration = period == null ? null : CalendarDuration.of(period);
    }

    /**
     * Sets a duration in the string format {@code [-][d.]hh:mm[:ss[.sss]]}, see {@link #getDuration()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the duration, such as {@code "08:30"} or {@code "1.12:00"}, or null to remove the option
     */
    public void setDuration(String text) {
        this.duration = text == null ? null : CalendarDuration.ofTimeString(text);
    }

    /**
     * Sets the duration, see {@link #getDuration()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the duration, or null to remove the option
     */
    public void setDuration(CalendarDuration value) {
        this.duration = value;
    }

    /**
     * Whether a month view always shows six weeks. With {@code false}, it shows four, five or six weeks, depending on
     * the month.
     * <p>
     * The library default is {@code true} in the built-in month views, {@code dayGridMonth} and {@code multiMonthYear}.
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/fixedWeekCount">fixedWeekCount</a>
     */
    private Boolean fixedWeekCount;

    /**
     * Whether the library assigns an end to entries without one, using {@link #getDefaultTimedEntryDuration()} or
     * {@link #getDefaultAllDayEntryDuration()}. With {@code false}, the end of such an entry stays unset.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to entries received
     * afterwards, for example after navigating to another view or period. Existing entries are not updated.
     * <p>
     * The library default is {@code false}. The getter returns null while the option is not set; then the library
     * default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/forceEventDuration">forceEventDuration</a>
     */
    @JsonName("forceEventDuration")
    private Boolean forceEntryDuration;

    /**
     * The heading level of the title of the library's toolbar in the document outline. The title is rendered as a
     * {@code div} with an {@code aria-level} attribute, so the level does not change how it looks. Set it to match the
     * page around the calendar, so screen readers find the title at the right level.
     * <p>
     * The library default is {@code 2}. The getter returns null while the option is not set; then the library default
     * applies.
     *
     * @see <a href="https://fullcalendar.io/docs/headingLevel">headingLevel</a>
     */
    private Integer headingLevel;

    /**
     * The days of the week to hide. At least one day must stay visible. The setter rejects all seven days. Hiding the
     * five weekdays while {@code weekends} is {@code false} makes the library throw an error.
     * <p>
     * When set, the getter returns an unmodifiable set.
     * <p>
     * The library default is an empty set, so no day is hidden, unless {@code weekends} is {@code false}. The getter
     * returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/hiddenDays">hiddenDays</a>
     */
    @JsonConverter(DayOfWeekArrayConverter.class)
    private Set<DayOfWeek> hiddenDays;

    /**
     * Sets the days to hide, see {@link #getHiddenDays()}. The add-on keeps an unmodifiable copy in the order of the
     * week, so a later change of the passed set has no effect.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param hiddenDays the days to hide, or null to remove the option
     * @throws IllegalArgumentException when all seven days are passed
     */
    public void setHiddenDays(Set<DayOfWeek> hiddenDays) {
        if (hiddenDays != null && hiddenDays.containsAll(EnumSet.allOf(DayOfWeek.class))) {
            throw new IllegalArgumentException("At least one day must stay visible");
        }
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
    @JsonConverter(CalendarDurationConverter.class)
    private CalendarDuration nextDayThreshold;

    /**
     * Sets a time-based duration, see {@link #getNextDayThreshold()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param duration the duration, days count as 24 hours, or null to remove the option
     */
    public void setNextDayThreshold(Duration duration) {
        this.nextDayThreshold = duration == null ? null : CalendarDuration.of(duration);
    }

    /**
     * Sets a calendar-based duration in years, months and days, see {@link #getNextDayThreshold()}.
     * <p>
     * A {@link Period} has no weeks, so {@code Period.ofWeeks(n)} arrives as {@code n * 7} days, without the
     * alignment to the start of the week. For weeks, use {@link CalendarDuration#ofWeeks(int)}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param period the years, months and days, or null to remove the option
     */
    public void setNextDayThreshold(Period period) {
        this.nextDayThreshold = period == null ? null : CalendarDuration.of(period);
    }

    /**
     * Sets a duration in the string format {@code [-][d.]hh:mm[:ss[.sss]]}, see {@link #getNextDayThreshold()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the duration, such as {@code "08:30"} or {@code "1.12:00"}, or null to remove the option
     */
    public void setNextDayThreshold(String text) {
        this.nextDayThreshold = text == null ? null : CalendarDuration.ofTimeString(text);
    }

    /**
     * Sets the duration, see {@link #getNextDayThreshold()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the duration, or null to remove the option
     */
    public void setNextDayThreshold(CalendarDuration value) {
        this.nextDayThreshold = value;
    }

    /**
     * The current date of the calendar, the day it highlights as today. It is useful with a time zone other than the
     * one of the browser.
     * <p>
     * The library reads a date value only when the browser creates the calendar. So a change on a calendar the browser
     * shows has no effect. A calendar that is detached and attached again (not within one round trip) starts with the
     * current value, or without one if the option was removed. The same applies when the browser creates the calendar
     * again, for example on a refresh with {@code @PreserveOnRefresh}. A callback is read again when the time zone or
     * locale changes.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link NowValue#ofDay(LocalDate)}: a date.
     *                                  <p>{@link NowValue#ofDateTime(LocalDateTime)}: a date and time, read as UTC like
     *                                  the start and end of entries.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setNow(LocalDate)}: a date, as {@link NowValue#ofDay(LocalDate)}.
     *                                  <p>{@link #setNow(LocalDateTime)}: a date and time, as
     *                                  {@link NowValue#ofDateTime(LocalDateTime)}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: the current date of the browser.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function()}, runs on the client.<br>
     *                                  It returns a JavaScript {@code Date}, or any value the library parses as a
     *                                  date.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setNow(LocalDate.of(2026, 5, 17));
     * calendar.options().setNow(LocalDateTime.of(2026, 5, 17, 14, 30));
     * calendar.options().setNow(JsCallback.of("() => new Date()"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/now">now</a>
     */
    @JsonConverter(NowValueConverter.class)
    private NowValue now;

    /**
     * Sets a date as the current date, see {@link #getNow()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param date the date, or null to remove the option
     */
    public void setNow(LocalDate date) {
        this.now = date == null ? null : NowValue.ofDay(date);
    }

    /**
     * Sets a date and time as the current date, see {@link #getNow()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param dateTime the date and time, read as UTC like the start and end of entries, or null to remove the option
     */
    public void setNow(LocalDateTime dateTime) {
        this.now = dateTime == null ? null : NowValue.ofDateTime(dateTime);
    }

    /**
     * Sets the current date in any form, see {@link #getNow()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
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
     * The library default is {@code true}, except in the {@code multiMonthYear} view, which turns it off.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/showNonCurrentDates">showNonCurrentDates</a>
     */
    private Boolean showNonCurrentDates;

    /**
     * The date range the user can navigate to and where entries can go. Dates outside of the range are grayed out,
     * entries cannot be dragged or resized into them, and the prev and next buttons are disabled when they would leave
     * the range.
     * <p>
     * A {@link DateRange} with equal bounds is rejected, although the library reads such a range as one day.
     * <p>
     * The library calls a callback several times per view render, so keep it cheap.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link DateRange}: the end is exclusive.<br>
     *                                  One bound may be null for an open range, see
     *                                  {@link DateRange#withOpenEnd(LocalDate)} and
     *                                  {@link DateRange#withOpenStart(LocalDate)}.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no range, so no date is outside of it.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(todayDate)}, the start of the current day.<br>
     *                                  It returns an object with {@code start} and {@code end}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setValidRange(DateRange.withOpenEnd(LocalDate.of(2025, 3, 1)));
     * calendar.options().setValidRange(JsCallback.of("today => ({start: today})"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/validRange">validRange</a>
     */
    @JsonConverter(DateRangeValueConverter.class)
    private DateRangeValue validRange;

    /**
     * The exact date range a view shows. The end is exclusive. It applies only to views that define neither a duration
     * nor a day count.
     * <p>
     * A {@link DateRange} with equal bounds is rejected, although the library reads such a range as one day.
     * <p>
     * The library calls a callback several times per view render, so keep it cheap.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link DateRange}: needs both bounds.<br>
     *                                  The library ignores a range with an open bound.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no range, then the view shows the range its own duration gives.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(currentDate)}, the date the view is currently at.<br>
     *                                  It returns an object with {@code start} and {@code end}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setVisibleRange(new DateRange(LocalDate.of(2026, 3, 22), LocalDate.of(2026, 3, 25)));
     * calendar.options().setVisibleRange(
     *         JsCallback.of("d => ({start: d, end: new Date(d.valueOf() + 3 * 86400000)})"));
     * }</pre>
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
     * The library default is {@code true}.
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
     * The library has no default for this option. Without a value, day cells have no minimum width. The getter returns
     * null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/dayMinWidth">dayMinWidth</a>
     */
    private Integer dayMinWidth;

    /**
     * The pixel width of a day column below which the library switches to "narrow" date text and layouts.
     * Affects views with day columns (day grid, time grid, multi-month). The narrow state shows up as {@code isNarrow}
     * in the {@code info} of related render hooks.
     * <p>
     * The library default is 80 pixels. The library docs say that the default depends on the theme and is about 100.
     * The stock themes except classic set 100, and the Vaadin theme sets none.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/dayNarrowWidth">dayNarrowWidth</a>
     */
    private Integer dayNarrowWidth;

    /**
     * Expand the rows of a view to fill its height, if they do not take up the entire height by themselves. Applies to
     * time grid and timeline views. It has no effect while the calendar height is automatic.
     * <p>
     * The library default is {@code false}. The getter returns null while the option is not set; then the library
     * default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/expandRows">expandRows</a>
     */
    private Boolean expandRows;

    /**
     * Maximum number of month columns the multi-month grid tries to render. Fewer columns are shown if each month would
     * become smaller than {@link #getSingleMonthMinWidth()}. Use {@code 1} for a single column.
     * <p>
     * The library default is {@code 3}. The getter returns null while the option is not set; then the library default
     * applies.
     *
     * @see <a href="https://fullcalendar.io/docs/multiMonthMaxColumns">multiMonthMaxColumns</a>
     */
    private Integer multiMonthMaxColumns;

    /**
     * Show a marker for the current time. It repositions itself while the calendar is shown.
     * Applies to time grid and timeline views.
     * <p>
     * The library default is {@code false}.
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
     * The library default is {@link AutoBoolean#AUTO}.
     * <p>
     * The library docs do not describe this option on its own page, so the page of the now indicator is linked.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/nowIndicator">nowIndicator</a>
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
     * The library default is {@code "06:00:00"}, 6 am.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/scrollTime">scrollTime</a>
     */
    @JsonConverter(CalendarDurationConverter.class)
    private CalendarDuration scrollTime;

    /**
     * Sets a time-based duration, see {@link #getScrollTime()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param duration the duration, days count as 24 hours, or null to remove the option
     */
    public void setScrollTime(Duration duration) {
        this.scrollTime = duration == null ? null : CalendarDuration.of(duration);
    }

    /**
     * Sets a calendar-based duration in years, months and days, see {@link #getScrollTime()}.
     * <p>
     * A {@link Period} has no weeks, so {@code Period.ofWeeks(n)} arrives as {@code n * 7} days, without the
     * alignment to the start of the week. For weeks, use {@link CalendarDuration#ofWeeks(int)}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param period the years, months and days, or null to remove the option
     */
    public void setScrollTime(Period period) {
        this.scrollTime = period == null ? null : CalendarDuration.of(period);
    }

    /**
     * Sets the duration from the library's string format, such as {@code "01:00"}.
     * See {@link #getScrollTime()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the duration, or null to remove the option
     */
    public void setScrollTime(String text) {
        this.scrollTime = text == null ? null : CalendarDuration.ofTimeString(text);
    }

    /**
     * Sets the duration, see {@link #getScrollTime()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the duration, or null to remove the option
     */
    public void setScrollTime(CalendarDuration value) {
        this.scrollTime = value;
    }

    /**
     * Whether the view scrolls to {@link #getScrollTime()} every time the date range changes, via the API or by the
     * user navigating.
     * <p>
     * The library default is {@code true}.
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
     * The library default is {@code 350}, set by the multi-month views.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/singleMonthMinWidth">singleMonthMinWidth</a>
     */
    private Integer singleMonthMinWidth;

    /**
     * Duration of each time slot in time grid and timeline views.
     * <p>
     * The library default is 30 minutes ({@code "00:30:00"}) in the time grid views. A timeline view computes a
     * duration from its header interval when none is set.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/slotDuration">slotDuration</a>
     */
    @JsonConverter(CalendarDurationConverter.class)
    private CalendarDuration slotDuration;

    /**
     * Sets a time-based duration, see {@link #getSlotDuration()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param duration the duration, days count as 24 hours, or null to remove the option
     */
    public void setSlotDuration(Duration duration) {
        this.slotDuration = duration == null ? null : CalendarDuration.of(duration);
    }

    /**
     * Sets a calendar-based duration in years, months and days, see {@link #getSlotDuration()}.
     * <p>
     * A {@link Period} has no weeks, so {@code Period.ofWeeks(n)} arrives as {@code n * 7} days, without the
     * alignment to the start of the week. For weeks, use {@link CalendarDuration#ofWeeks(int)}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param period the years, months and days, or null to remove the option
     */
    public void setSlotDuration(Period period) {
        this.slotDuration = period == null ? null : CalendarDuration.of(period);
    }

    /**
     * Sets the duration from the library's string format, such as {@code "01:00"}.
     * See {@link #getSlotDuration()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the duration, or null to remove the option
     */
    public void setSlotDuration(String text) {
        this.slotDuration = text == null ? null : CalendarDuration.ofTimeString(text);
    }

    /**
     * Sets the duration, see {@link #getSlotDuration()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the duration, or null to remove the option
     */
    public void setSlotDuration(CalendarDuration value) {
        this.slotDuration = value;
    }

    /**
     * Whether timed entries in time grid views visually overlap. With {@code true}, at most half of each entry
     * is obscured. With {@code false}, entries never overlap.
     * <p>
     * The library default is {@code true}.
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
     * A theme can change the alignment. The classic theme, on which the Vaadin theme builds, centers the headers that
     * show no time.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link HeaderAlign#START}, {@link HeaderAlign#CENTER} or
     *                                  {@link HeaderAlign#END}: a fixed alignment.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: {@link HeaderAlign#START}.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code level} and {@code isTime}.<br>
     *                                  It returns {@code "start"}, {@code "center"} or {@code "end"}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setSlotHeaderAlign(HeaderAlign.CENTER);
     * calendar.options().setSlotHeaderAlign(JsCallback.of("info => info.isTime ? 'center' : 'start'"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/slot-header-render-hooks">slotHeaderAlign</a>
     */
    @JsonConverter(HeaderAlignValueConverter.class)
    private HeaderAlignValue slotHeaderAlign;

    /**
     * Interval at which time slots are labeled with a header text, such as one hour to show headers on the hour marks
     * even if {@link #getSlotDuration()} is 15 or 30 minutes.
     * <p>
     * The library default is computed from {@link #getSlotDuration()}, or in timeline views without one from the
     * duration of the view.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/slotHeaderInterval">slotHeaderInterval</a>
     */
    @JsonConverter(CalendarDurationConverter.class)
    private CalendarDuration slotHeaderInterval;

    /**
     * Sets a time-based duration, see {@link #getSlotHeaderInterval()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param duration the duration, days count as 24 hours, or null to remove the option
     */
    public void setSlotHeaderInterval(Duration duration) {
        this.slotHeaderInterval = duration == null ? null : CalendarDuration.of(duration);
    }

    /**
     * Sets a calendar-based duration in years, months and days, see {@link #getSlotHeaderInterval()}.
     * <p>
     * A {@link Period} has no weeks, so {@code Period.ofWeeks(n)} arrives as {@code n * 7} days, without the
     * alignment to the start of the week. For weeks, use {@link CalendarDuration#ofWeeks(int)}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param period the years, months and days, or null to remove the option
     */
    public void setSlotHeaderInterval(Period period) {
        this.slotHeaderInterval = period == null ? null : CalendarDuration.of(period);
    }

    /**
     * Sets the duration from the library's string format, such as {@code "01:00"}.
     * See {@link #getSlotHeaderInterval()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the duration, or null to remove the option
     */
    public void setSlotHeaderInterval(String text) {
        this.slotHeaderInterval = text == null ? null : CalendarDuration.ofTimeString(text);
    }

    /**
     * Sets the duration, see {@link #getSlotHeaderInterval()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the duration, or null to remove the option
     */
    public void setSlotHeaderInterval(CalendarDuration value) {
        this.slotHeaderInterval = value;
    }

    /**
     * Sticky positioning of the header rows above the lowest one in the slot headers of timeline views, which the
     * Scheduler provides. While the user scrolls horizontally, a sticky header keeps its text at the edge of the
     * scroll area. With an offset, that edge moves inward by the offset. With a centered slot header alignment, the
     * offset has no effect.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>     <dd>{@link StickyValue#STICKY}: the text stays visible.
     *                                 <p>{@link StickyValue#NOT_STICKY}: the text scrolls away.
     *                                 <p>{@link StickyValue#of(String)}: an offset as a CSS length, such as
     *                                 {@code "2em"} or {@code "10px"}.</dd>
     *   <dt><b>Shortcuts</b></dt> <dd>{@link #setSlotHeaderSticky(String)}: a CSS length, as
     *                                 {@link StickyValue#of(String)}.</dd>
     *   <dt><b>Default</b></dt>   <dd>library: {@link StickyValue#STICKY}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setSlotHeaderSticky("10px");
     * calendar.options().setSlotHeaderSticky(StickyValue.NOT_STICKY);
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/slot-header-render-hooks">slotHeaderSticky</a>
     */
    @JsonConverter(StickyValueConverter.class)
    private StickyValue slotHeaderSticky;

    /**
     * Sets the slot headers sticky at an offset from the edge, see {@link #getSlotHeaderSticky()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param offset a CSS length, such as {@code "2em"} or {@code "10px"}, or null to remove the option
     */
    public void setSlotHeaderSticky(String offset) {
        this.slotHeaderSticky = offset == null ? null : StickyValue.of(offset);
    }

    /**
     * Sets the sticky positioning of the slot headers in any form, see {@link #getSlotHeaderSticky()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
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
     * The library default is {@code "24:00:00"}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/slotMaxTime">slotMaxTime</a>
     */
    @JsonConverter(CalendarDurationConverter.class)
    private CalendarDuration slotMaxTime;

    /**
     * Sets a time-based duration, see {@link #getSlotMaxTime()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param duration the duration, days count as 24 hours, or null to remove the option
     */
    public void setSlotMaxTime(Duration duration) {
        this.slotMaxTime = duration == null ? null : CalendarDuration.of(duration);
    }

    /**
     * Sets a calendar-based duration in years, months and days, see {@link #getSlotMaxTime()}.
     * <p>
     * A {@link Period} has no weeks, so {@code Period.ofWeeks(n)} arrives as {@code n * 7} days, without the
     * alignment to the start of the week. For weeks, use {@link CalendarDuration#ofWeeks(int)}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param period the years, months and days, or null to remove the option
     */
    public void setSlotMaxTime(Period period) {
        this.slotMaxTime = period == null ? null : CalendarDuration.of(period);
    }

    /**
     * Sets the duration from the library's string format, such as {@code "01:00"}.
     * See {@link #getSlotMaxTime()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the duration, or null to remove the option
     */
    public void setSlotMaxTime(String text) {
        this.slotMaxTime = text == null ? null : CalendarDuration.ofTimeString(text);
    }

    /**
     * Sets the duration, see {@link #getSlotMaxTime()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the duration, or null to remove the option
     */
    public void setSlotMaxTime(CalendarDuration value) {
        this.slotMaxTime = value;
    }

    /**
     * Minimum height of each slot of the time axis, in pixels. A slot is taller when its label needs more room, or when
     * {@code expandRows} is on and the calendar has room to spare. The library docs speak of the exact height, but the
     * used library version treats the value as a minimum.
     * <p>
     * The library default is no value. Then the minimum is computed from the height of the slot label.
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
     * The library default is {@code "00:00:00"}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/slotMinTime">slotMinTime</a>
     */
    @JsonConverter(CalendarDurationConverter.class)
    private CalendarDuration slotMinTime;

    /**
     * Sets a time-based duration, see {@link #getSlotMinTime()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param duration the duration, days count as 24 hours, or null to remove the option
     */
    public void setSlotMinTime(Duration duration) {
        this.slotMinTime = duration == null ? null : CalendarDuration.of(duration);
    }

    /**
     * Sets a calendar-based duration in years, months and days, see {@link #getSlotMinTime()}.
     * <p>
     * A {@link Period} has no weeks, so {@code Period.ofWeeks(n)} arrives as {@code n * 7} days, without the
     * alignment to the start of the week. For weeks, use {@link CalendarDuration#ofWeeks(int)}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param period the years, months and days, or null to remove the option
     */
    public void setSlotMinTime(Period period) {
        this.slotMinTime = period == null ? null : CalendarDuration.of(period);
    }

    /**
     * Sets the duration from the library's string format, such as {@code "01:00"}.
     * See {@link #getSlotMinTime()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the duration, or null to remove the option
     */
    public void setSlotMinTime(String text) {
        this.slotMinTime = text == null ? null : CalendarDuration.ofTimeString(text);
    }

    /**
     * Sets the duration, see {@link #getSlotMinTime()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the duration, or null to remove the option
     */
    public void setSlotMinTime(CalendarDuration value) {
        this.slotMinTime = value;
    }

    /**
     * Time interval to which a dragged entry snaps on the time axis. Also sets the granularity of selections.
     * <p>
     * The library default is the value of {@link #getSlotDuration()}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/snapDuration">snapDuration</a>
     */
    @JsonConverter(CalendarDurationConverter.class)
    private CalendarDuration snapDuration;

    /**
     * Sets a time-based duration, see {@link #getSnapDuration()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param duration the duration, days count as 24 hours, or null to remove the option
     */
    public void setSnapDuration(Duration duration) {
        this.snapDuration = duration == null ? null : CalendarDuration.of(duration);
    }

    /**
     * Sets a calendar-based duration in years, months and days, see {@link #getSnapDuration()}.
     * <p>
     * A {@link Period} has no weeks, so {@code Period.ofWeeks(n)} arrives as {@code n * 7} days, without the
     * alignment to the start of the week. For weeks, use {@link CalendarDuration#ofWeeks(int)}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param period the years, months and days, or null to remove the option
     */
    public void setSnapDuration(Period period) {
        this.snapDuration = period == null ? null : CalendarDuration.of(period);
    }

    /**
     * Sets the duration from the library's string format, such as {@code "01:00"}.
     * See {@link #getSnapDuration()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the duration, or null to remove the option
     */
    public void setSnapDuration(String text) {
        this.snapDuration = text == null ? null : CalendarDuration.ofTimeString(text);
    }

    /**
     * Sets the duration, see {@link #getSnapDuration()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the duration, or null to remove the option
     */
    public void setSnapDuration(CalendarDuration value) {
        this.snapDuration = value;
    }

    // Interaction

    /**
     * Time in milliseconds an entry takes to move back to its original position after an unsuccessful drag.
     * <p>
     * The library default is 500. The getter returns null while the option is not set; then the library default
     * applies.
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
     * The library default is {@code true}. The getter returns null while the option is not set; then the library
     * default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/dragScroll">dragScroll</a>
     */
    private Boolean dragScroll;

    /**
     * Determines whether external elements or entries from other calendars can be dropped onto the calendar.
     * It has to be enabled to receive drops from external elements.
     * <p>
     * The library default is {@code false}. The getter returns null while the option is not set; then the library
     * default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/droppable">droppable</a>
     */
    private Boolean droppable;

    /**
     * Filters which external elements can be dropped onto the calendar. It takes effect when {@link #getDroppable()} is
     * true.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link CssSelector}: a CSS selector the dragged element must match.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setDropAccept(String)}: a CSS selector, as {@link CssSelector}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: {@code "*"}, which accepts every element.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(item)}, the dragged element.<br>
     *                                  It returns {@code true} if the item can be dropped.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setDropAccept(".cool-entry");
     * calendar.options().setDropAccept(JsCallback.of("item => item.classList.contains('cool-entry')"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/dropAccept">dropAccept</a>
     */
    @JsonConverter(CssSelectorValueConverter.class)
    private CssSelectorValue dropAccept;

    /**
     * Accepts the external elements that match a CSS selector, see {@link #getDropAccept()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param selector the CSS selector, such as {@code ".cool-entry"}, or null to remove the option
     */
    public void setDropAccept(String selector) {
        this.dropAccept = selector == null ? null : CssSelector.of(selector);
    }

    /**
     * Sets the filter for external elements in any form, see {@link #getDropAccept()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the selector or callback, or null to remove the option
     */
    public void setDropAccept(CssSelectorValue value) {
        this.dropAccept = value;
    }

    /**
     * Number of pixels the pointer must move before an entry drag or resize starts. It applies to dragging with the
     * mouse and to resizing. Dragging on touch ignores it. There, {@link #getEntryLongPressDelay()} applies. The
     * library docs say that it applies to touch too.
     * <p>
     * The library default is 5. The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/eventDragMinDistance">eventDragMinDistance</a>
     */
    @JsonName("eventDragMinDistance")
    private Integer entryDragMinDistance;

    /**
     * Whether all entries are focusable and tabbable for keyboard access. Without a value, every entry is focusable,
     * because the add-on registers an entry click handler. {@code false} turns that off for all entries without a
     * {@code url}. An entry with a {@code url} stays focusable.
     * <p>
     * The library docs name {@code false} as the default, so that only entries with a {@code url} are focusable. The
     * used library version has no default. Without a value, it makes every entry focusable when the calendar has an
     * entry click handler.
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
     * The library default is the value of {@link #getLongPressDelay() longPressDelay}, which is 1000. The getter
     * returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/eventLongPressDelay">eventLongPressDelay</a>
     */
    @JsonName("eventLongPressDelay")
    private Integer entryLongPressDelay;

    /**
     * Whether entries that are dragged or resized may overlap other entries. Background entries count like normal
     * entries.
     * <p>
     * A callback runs for every pair of intersecting entries. The entry being moved still has its original start and
     * end.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link Overlap#ALLOWED}: entries may overlap.
     *                                  <p>{@link Overlap#FORBIDDEN}: entries may not overlap.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setEntryOverlap(boolean)}: {@code true} = {@link Overlap#ALLOWED},
     *                                  {@code false} = {@link Overlap#FORBIDDEN}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: {@code true}.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(stillEvent, movingEvent)}, the entry underneath and the entry
     *                                  being moved.<br>
     *                                  It returns {@code true} to allow the overlap.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setEntryOverlap(false);
     * calendar.options().setEntryOverlap(JsCallback.of("(still, moving) => still.allDay && moving.allDay"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/eventOverlap">eventOverlap</a>
     */
    @JsonName("eventOverlap")
    @JsonConverter(OverlapValueConverter.class)
    private OverlapValue entryOverlap;

    /**
     * Sets whether dragged or resized entries may overlap entries, see {@link #getEntryOverlap()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param allowed true to allow the overlap, false to forbid it
     */
    public void setEntryOverlap(boolean allowed) {
        this.entryOverlap = Overlap.of(allowed);
    }

    /**
     * Sets whether dragged or resized entries may overlap entries in any form, see {@link #getEntryOverlap()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the overlap setting or callback, or null to remove the option
     */
    public void setEntryOverlap(OverlapValue value) {
        this.entryOverlap = value;
    }

    /**
     * Whether the user can resize entries from their start edge.
     * <p>
     * The library default is {@code false}, except in timeline views, which turn it on. The getter returns null while
     * the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/eventResizableFromStart">eventResizableFromStart</a>
     */
    @JsonName("eventResizableFromStart")
    private Boolean entryResizableFromStart;

    /**
     * Time in milliseconds a touch user must hold down before an entry can be dragged or a date selected.
     * {@link #getEntryLongPressDelay()} and {@link #getSelectLongPressDelay()} override it for one of the two. It has
     * no effect on non-touch devices.
     * <p>
     * The library default is {@code 1000}. The getter returns null while the option is not set; then the library
     * default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/longPressDelay">longPressDelay</a>
     */
    private Integer longPressDelay;

    /**
     * Whether day and week labels (day numbers, day headers, week numbers) are links. The add-on handles a click
     * itself and sends it to the server as a {@link DayNumberClickedEvent} or a {@link WeekNumberClickedEvent}.
     * The calendar does not change the view on its own.
     * <p>
     * The library default is {@code false}.
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
     * The library default is {@code false}.
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
     * The library default is the value of {@link #getLongPressDelay()}, which is {@code 1000} milliseconds by default.
     * The library docs name {@code 1000} as the default of this option.
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
     * The library default is {@code 0}.
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
     * The library default is {@code false}.
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
     * A callback runs for every entry the selection intersects. An all-day entry counts as intersecting in time grid
     * views, although it does not visually overlap the time slots.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link Overlap#ALLOWED}: the selection may overlap entries.
     *                                  <p>{@link Overlap#FORBIDDEN}: the selection may not overlap entries.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setSelectOverlap(boolean)}: {@code true} = {@link Overlap#ALLOWED},
     *                                  {@code false} = {@link Overlap#FORBIDDEN}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: {@code true}, which allows the selection.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(event)}, the intersected entry itself, not wrapped in an info
     *                                  object.<br>
     *                                  It returns {@code true} to allow the selection.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setSelectOverlap(false);
     * calendar.options().setSelectOverlap(JsCallback.of("event => event.display === 'background'"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/selectOverlap">selectOverlap</a>
     */
    @JsonConverter(OverlapValueConverter.class)
    private OverlapValue selectOverlap;

    /**
     * Sets whether a selection may overlap entries, see {@link #getSelectOverlap()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param allowed true to allow the overlap, false to forbid it
     */
    public void setSelectOverlap(boolean allowed) {
        this.selectOverlap = Overlap.of(allowed);
    }

    /**
     * Sets whether a selection may overlap entries in any form, see {@link #getSelectOverlap()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the overlap setting or callback, or null to remove the option
     */
    public void setSelectOverlap(OverlapValue value) {
        this.selectOverlap = value;
    }

    /**
     * Whether clicking elsewhere on the page clears the current selection. It applies when {@link #getSelectable()} is
     * true.
     * <p>
     * The library default is {@code true}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/unselectAuto">unselectAuto</a>
     */
    private Boolean unselectAuto;

    /**
     * CSS selector for elements that do not clear the current selection when clicked, see {@link #getUnselectAuto()}.
     * Useful for a form that opens after a selection.
     * <p>
     * The library docs name the empty string as default. The used library version has no default, and an empty string
     * acts like no value.
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
     * It is used only while neither {@code height} nor a {@link #getContentHeight() content height} is set. The add-on
     * sets the calendar to full height by default. A value below 0.5 is treated as 0.5 for the calendar height. Day
     * grid views also use the ratio for the minimum height of their rows when their height is automatic or they show
     * more than six rows, and multi-month views use it as well.
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
     * The library default is no color. A theme can set one.
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
     * The library default is {@code false}. A set {@code borderlessTop}, {@code borderlessBottom} or
     * {@code borderlessX} wins over this option, so {@code borderless} true with {@code borderlessTop} false keeps the
     * top border.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/borderless">borderless</a>
     */
    private Boolean borderless;

    /**
     * Whether the calendar removes only its bottom outer border.
     * <p>
     * The library default is {@code false}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/borderless">borderlessBottom</a>
     */
    private Boolean borderlessBottom;

    /**
     * Whether the calendar removes only its top outer border.
     * <p>
     * The library default is {@code false}.
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
     * The library default is {@code false}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/borderless">borderlessX</a>
     */
    private Boolean borderlessX;

    /**
     * The time slots the calendar emphasizes as business hours.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>     <dd>{@link BusinessHoursValue#DEFAULT}: Monday to Friday, 9am to 5pm.
     *                                 <p>{@link BusinessHoursValue#of(BusinessHours...)}: own hours, at least one
     *                                 {@link BusinessHours}.
     *                                 <p>{@link BusinessHoursValue#NONE}: no emphasis.</dd>
     *   <dt><b>Shortcuts</b></dt> <dd>{@link #setBusinessHours(BusinessHours...)}: own hours, as
     *                                 {@link BusinessHoursValue#of(BusinessHours...)}.<br>
     *                                 Without any {@link BusinessHours} it throws an
     *                                 {@link IllegalArgumentException}.</dd>
     *   <dt><b>Default</b></dt>   <dd>library: {@link BusinessHoursValue#NONE}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setBusinessHours(BusinessHours.businessWeek().start(8).end(18));
     * calendar.options().setBusinessHours(BusinessHoursValue.DEFAULT);
     * calendar.options().setBusinessHours(BusinessHoursValue.NONE);
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/businessHours">businessHours</a>
     */
    @JsonConverter(BusinessHoursValueConverter.class)
    private BusinessHoursValue businessHours;

    /**
     * Sets own business hours, see {@link #getBusinessHours()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param hours the business hours, at least one, or null to remove the option
     */
    public void setBusinessHours(BusinessHours... hours) {
        this.businessHours = hours == null ? null : BusinessHoursValue.of(hours);
    }

    /**
     * Sets the business hours in any form, see {@link #getBusinessHours()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
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
     * The library default is no value. Without this option, the FullCalendar Vaadin theme follows the CSS color-scheme
     * of the page, which {@code Page#setColorScheme} sets. A stock theme follows only a
     * {@code data-color-scheme="dark"} attribute on {@code <html>}, the body or another ancestor of the calendar, not
     * {@code Page#setColorScheme}.
     * <p>
     * With the attribute on an ancestor inside the body, the "+N more" popover and a dragged entry keep their light
     * colors, because the library renders them directly into the body. This option also sets the color scheme of the
     * popover and a dragged entry.
     * <p>
     * With Lumo, the option cannot make a calendar light while the application is dark. Lumo's dark mode sets fixed
     * dark colors on the page, and the calendar inherits them. A dark calendar in a light Lumo application works.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://github.com/stefanuebe/vaadin-fullcalendar/wiki/Themes-and-Styling#color-scheme">
     *     Themes and Styling: Color scheme</a>
     * @see <a href="https://fullcalendar.io/docs/color-palettes">colorScheme</a>
     */
    private String colorScheme;

    /**
     * Whether the title of column entries sticks to the top edge of the visible area while the user scrolls vertically
     * past a long timed entry, so the title stays readable. The library default is {@code true}. A theme can change
     * it. The monarch theme of the library sets it to {@code false}.
     * <p>
     * The library docs describe this option only partly or not at all.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/column-event-render-hooks">columnEventTitleSticky</a>
     */
    @JsonName("columnEventTitleSticky")
    private Boolean columnEntryTitleSticky;

    /**
     * The height of the view area of the calendar.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>     <dd>{@link HeightValue#ofPixels(int)}: exact pixels.<br>
     *                                 The view scrolls when its content does not fit.
     *                                 <p>{@link HeightValue#ofCss(String)}: a CSS length, such as {@code "50vh"}.
     *                                 <p>{@link HeightValue#AUTO}: the natural height of the content, without
     *                                 scrollbars.</dd>
     *   <dt><b>Shortcuts</b></dt> <dd>{@link #setContentHeight(int)}: pixels, as {@link HeightValue#ofPixels(int)}.<br>
     *                                 A negative number throws an {@link IllegalArgumentException}.
     *                                 <p>{@link #setContentHeight(String)}: a CSS length, as
     *                                 {@link HeightValue#ofCss(String)}.<br>
     *                                 {@code "auto"} throws an {@link IllegalArgumentException}.<br>
     *                                 Use {@link HeightValue#AUTO} instead.</dd>
     *   <dt><b>Default</b></dt>   <dd>library: no value, then the calendar height follows the
     *                                 {@link #getAspectRatio() aspect ratio}, as long as no {@code height} is set.
     *                                 <p>add-on: sets {@code height} to the full height by default.<br>
     *                                 While it is set, a pixel or CSS value has no effect, and {@link HeightValue#AUTO}
     *                                 still works.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setContentHeight(600);
     * calendar.options().setContentHeight("50vh");
     * calendar.options().setContentHeight(HeightValue.AUTO);
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/contentHeight">contentHeight</a>
     */
    @JsonConverter(HeightValueConverter.class)
    private HeightValue contentHeight;

    /**
     * Sets the height in pixels, see {@link #getContentHeight()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param pixels the height in pixels, not negative
     */
    public void setContentHeight(int pixels) {
        this.contentHeight = HeightValue.ofPixels(pixels);
    }

    /**
     * Sets the height as a CSS length, see {@link #getContentHeight()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param length a CSS length such as {@code "50vh"}, or null to remove the option
     */
    public void setContentHeight(String length) {
        this.contentHeight = length == null ? null : HeightValue.ofCss(length);
    }

    /**
     * Sets the height in any form, see {@link #getContentHeight()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the height, {@link HeightValue#AUTO} for the natural height of the content, or null to remove the
     *        option
     */
    public void setContentHeight(HeightValue value) {
        this.contentHeight = value;
    }

    /**
     * The maximum number of entries in a day cell of day grid views, not counting the "+N more" link. The rest is shown
     * in a popover. The all-day section of time grid views uses this option too, and
     * {@link EntryLimitValue#CELL_HEIGHT} limits it to 5 rows there. For the timed entries of time grid views and for
     * timeline views use {@link #getEntryMaxStack() entryMaxStack}.
     * <p>
     * The library combines this option with {@link #getDayMaxEntryRows() dayMaxEntryRows}, see {@link EntryLimitValue}.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>     <dd>{@link EntryLimitValue#of(int)}: a fixed number of entries.
     *                                 <p>{@link EntryLimitValue#CELL_HEIGHT}: as many as fit into the height of the
     *                                 cell.
     *                                 <p>{@link EntryLimitValue#NONE}: all entries.</dd>
     *   <dt><b>Shortcuts</b></dt> <dd>{@link #setDayMaxEntries(int)}: a number, as {@link EntryLimitValue#of(int)}.<br>
     *                                 A negative number throws an {@link IllegalArgumentException}.</dd>
     *   <dt><b>Default</b></dt>   <dd>library: {@link EntryLimitValue#NONE}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setDayMaxEntries(3);
     * calendar.options().setDayMaxEntries(EntryLimitValue.CELL_HEIGHT);
     * calendar.options().setDayMaxEntries(EntryLimitValue.NONE);
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/dayMaxEvents">dayMaxEvents</a>
     */
    @JsonConverter(EntryLimitValueConverter.class)
    @JsonName("dayMaxEvents")
    private EntryLimitValue dayMaxEntries;

    /**
     * Sets a fixed limit, see {@link #getDayMaxEntries()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param count the number, not negative
     */
    public void setDayMaxEntries(int count) {
        this.dayMaxEntries = EntryLimitValue.of(count);
    }

    /**
     * Sets the limit in any form, see {@link #getDayMaxEntries()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the limit, or null to remove the option
     */
    public void setDayMaxEntries(EntryLimitValue value) {
        this.dayMaxEntries = value;
    }

    /**
     * The maximum number of stacked entry levels in a day cell of day grid views, including the "+N more" link. The
     * rest is shown in a popover. The all-day section of time grid views uses this option too, and
     * {@link EntryLimitValue#CELL_HEIGHT} limits it to 5 rows there. For the timed entries of time grid views and for
     * timeline views use {@link #getEntryMaxStack() entryMaxStack}.
     * <p>
     * The library combines this option with {@link #getDayMaxEntries() dayMaxEntries}, see {@link EntryLimitValue}.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>     <dd>{@link EntryLimitValue#of(int)}: a fixed number of rows.
     *                                 <p>{@link EntryLimitValue#CELL_HEIGHT}: as many as fit into the height of the
     *                                 cell.
     *                                 <p>{@link EntryLimitValue#NONE}: all entries.</dd>
     *   <dt><b>Shortcuts</b></dt> <dd>{@link #setDayMaxEntryRows(int)}: a number, as
     *                                 {@link EntryLimitValue#of(int)}.<br>
     *                                 A negative number throws an {@link IllegalArgumentException}.</dd>
     *   <dt><b>Default</b></dt>   <dd>library: {@link EntryLimitValue#NONE}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setDayMaxEntryRows(3);
     * calendar.options().setDayMaxEntryRows(EntryLimitValue.CELL_HEIGHT);
     * calendar.options().setDayMaxEntryRows(EntryLimitValue.NONE);
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/dayMaxEventRows">dayMaxEventRows</a>
     */
    @JsonConverter(EntryLimitValueConverter.class)
    @JsonName("dayMaxEventRows")
    private EntryLimitValue dayMaxEntryRows;

    /**
     * Sets a fixed limit, see {@link #getDayMaxEntryRows()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param count the number, not negative
     */
    public void setDayMaxEntryRows(int count) {
        this.dayMaxEntryRows = EntryLimitValue.of(count);
    }

    /**
     * Sets the limit in any form, see {@link #getDayMaxEntryRows()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the limit, or null to remove the option
     */
    public void setDayMaxEntryRows(EntryLimitValue value) {
        this.dayMaxEntryRows = value;
    }

    /**
     * Whether the calendar shows the end time of an entry. An entry without an end, or a calendar that does not show
     * the time ({@link #getDisplayEntryTime() displayEntryTime}), shows no end time regardless. The library default
     * depends on the view. It shows no end time in the month and week day grid views, and an end time in the time grid
     * views and the day grid day view.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/displayEventEnd">displayEventEnd</a>
     */
    @JsonName("displayEventEnd")
    private Boolean displayEntryEnd;

    /**
     * Whether the calendar shows the time text of an entry. In list views, all-day entries show the all-day text as
     * their time text, which {@code false} hides too. The library default depends on the view. The time shows in all
     * day grid and time grid views. In timeline views it depends on the duration.
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
     * The library has no default for this option. The Vaadin theme and the stock themes set a color here. The getter
     * returns null while the option is not set; then the library default applies.
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
     * The library has no default for this option. The Vaadin theme and the stock themes set a color here. The getter
     * returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/eventContrastColor">eventContrastColor</a>
     */
    @JsonName("eventContrastColor")
    private String entryContrastColor;

    /**
     * The maximum number of entries that stack top to bottom in timeline views, and left to right in time grid views.
     * When more entries overlap, a more link stands for the hidden ones and opens a popover with them by default. The
     * library default is {@code null}, which shows all entries. For day grid views use
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
     * order. If a lower ranked entry fits into the layout more compactly, the library places it earlier, unless
     * {@link #getEntryOrderStrict() entryOrderStrict} is set.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link Order}: a list of sort keys.<br>
     *                                  Each is an entry property name, ascending or descending, or a compare
     *                                  {@link JsCallback}.
     *                                  <p>{@link JsCallback}: one function that compares two entries.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setEntryOrder(String...)}: entry property names, a leading {@code -}
     *                                  sorts descending, as {@link Order#of(String...)}.<br>
     *                                  No name, an empty name or a name with a comma throws an
     *                                  {@link IllegalArgumentException}.
     *                                  <p>{@link #setEntryOrder(Order.Key...)}: sort keys that may mix properties and
     *                                  compare functions, as {@link Order#of(Order.Key...)}.<br>
     *                                  No key throws an {@link IllegalArgumentException}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: {@code "start,-duration,allDay,title"} (earlier entries first, then
     *                                  longer ones, then all-day ones, then by title).</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(a, b)}.<br>
     *                                  {@code a} and {@code b} are plain objects, not the entries themselves.<br>
     *                                  They hold the fields of the entry definition such as {@code title} and
     *                                  {@code extendedProps}, the extended properties at top level, {@code id},
     *                                  {@code start} and {@code end} as numbers in milliseconds, {@code duration} in
     *                                  milliseconds and {@code allDay} as 0 or 1.<br>
     *                                  It returns a negative number, zero or a positive number.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setEntryOrder("title", "-start");
     * calendar.options().setEntryOrder(JsCallback.of("(a, b) => a.start - b.start"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/eventOrder">entryOrder</a>
     */
    @JsonConverter(OrderValueConverter.class)
    @JsonName("eventOrder")
    private OrderValue entryOrder;

    /**
     * Sets the order from entry property names, see {@link #getEntryOrder()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param properties the names, such as {@code "title"} or {@code "-start"} for descending, or null to remove the
     *        option
     */
    public void setEntryOrder(String... properties) {
        this.entryOrder = properties == null ? null : Order.of(properties);
    }

    /**
     * Sets the order from sort keys, which may mix properties and compare functions, see {@link #getEntryOrder()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param keys the sort keys, at least one, or null to remove the option
     */
    public void setEntryOrder(Order.Key... keys) {
        this.entryOrder = keys == null ? null : Order.of(keys);
    }

    /**
     * Sets the order in any form, see {@link #getEntryOrder()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the order or a compare callback, or null to remove the option
     */
    public void setEntryOrder(OrderValue value) {
        this.entryOrder = value;
    }

    /**
     * Whether the calendar follows {@link #getEntryOrder() entryOrder} strictly. Without it, the calendar prefers
     * compactness over the exact order.
     * <p>
     * The library default is {@code false}. The getter returns null while the option is not set; then the library
     * default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/eventOrderStrict">eventOrderStrict</a>
     */
    @JsonName("eventOrderStrict")
    private Boolean entryOrderStrict;

    /**
     * The height threshold in pixels for the "short" style of an entry in time grid views. The library default is 30.
     * The stock themes except classic set 50.
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
     * views that have one, such as timeline views. {@link AutoBoolean#AUTO} activates it when the calendar height is
     * automatic and so can be very tall. {@link AutoBoolean#TRUE} does the same, because the scrollbar sticks only
     * while the calendar height is automatic ({@code height} or {@code contentHeight} is {@code auto}).
     * {@link AutoBoolean#FALSE} turns it off.
     * <p>
     * The library default is {@link AutoBoolean#AUTO}. The getter returns null while the option is not set; then the
     * library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/footerScrollbarSticky">footerScrollbarSticky</a>
     */
    @JsonConverter(AutoBooleanConverter.class)
    private AutoBoolean footerScrollbarSticky;

    /**
     * Whether the calendar renders each asynchronous entry source as soon as it has been received ({@code true}, more
     * renders) or waits until all sources have been received and renders them at once ({@code false}).
     * <p>
     * The library default is {@code false}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/progressiveEventRendering">progressiveEventRendering</a>
     */
    @JsonName("progressiveEventRendering")
    private Boolean progressiveEntryRendering;

    /**
     * Whether the title of row entries sticks to the start edge of the visible area while the user scrolls horizontally
     * past a long entry, so the title stays readable.
     * <p>
     * The library default is {@code true}. The library docs do not describe this option.
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

    /**
     * Text of the label for the all-day section in time grid views. The library default is the locale's text.
     * <p>
     * The library docs describe this option only partly or not at all.
     * <p>
     * A newline in the text is shown as a line break in time grid views. List views show it as the time text of all-day
     * entries and of entries that span the whole day, unless entry times are hidden. For full control of the cell's
     * content use {@link #getAllDayHeaderContent() allDayHeaderContent}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/allDaySlot">allDayText</a>
     */
    private String allDayText;

    /**
     * Accessible hint ({@code aria-label}) for the close button of the entry popover. The library default is the
     * locale's text (English: "Close").
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/hints">closeHint</a>
     */
    private String closeHint;

    /**
     * Accessible label of the list of entries under each day in list views and in the timeline views of the Scheduler,
     * read by screen readers. The library docs do not describe this option. The behavior comes from the library code. A
     * value set here wins over the default.
     * <p>
     * The library default is the text of the locale, "Events" in English. The getter returns null while the option is
     * not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/hints">eventsHint</a>
     */
    @JsonName("eventsHint")
    private String entriesHint;

    /**
     * Accessible hint (the {@code title} attribute) for the "+N more" link. It describes what happens when the link
     * opens the entry popover.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link TextContent}: shown as text.<br>
     *                                  {@code $0} stands for the number of hidden entries, such as "Click to see $0
     *                                  more entries".
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setMoreLinkHint(String)}: a text, as {@link TextContent}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: the text of the locale, "Show 3 more events" for three entries in
     *                                  English.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(num)}, the number of hidden entries.<br>
     *                                  It returns the text.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setMoreLinkHint("Click to see $0 more entries");
     * calendar.options().setMoreLinkHint(JsCallback.of("num => 'Show ' + num + ' hidden entries'"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/hints">moreLinkHint</a>
     */
    @JsonConverter(TextValueConverter.class)
    private TextValue moreLinkHint;

    /**
     * Sets a fixed text, see {@link #getMoreLinkHint()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setMoreLinkHint(String text) {
        this.moreLinkHint = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the value in any form, see {@link #getMoreLinkHint()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the text or callback, or null to remove the option
     */
    public void setMoreLinkHint(TextValue value) {
        this.moreLinkHint = value;
    }

    /**
     * Text of the "+N more" link that shows when too many entries do not fit in a day.
     * <p>
     * Very small cells and the more links of time grid columns show only the number.
     * <p>
     * For full control of the link's content use {@code moreLinkContent}.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link TextContent}: shown as text, put after the number.<br>
     *                                  So "more" shows "+3 more".
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setMoreLinkText(String)}: a text, as {@link TextContent}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: the text of the locale, "more" in English.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(num)}, the number of hidden entries.<br>
     *                                  It returns the whole text of the link.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setMoreLinkText("more");
     * calendar.options().setMoreLinkText(JsCallback.of("num => '+' + num + ' hidden'"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/more-link-render-hooks">more-link render hooks</a>
     */
    @JsonConverter(TextValueConverter.class)
    private TextValue moreLinkText;

    /**
     * Sets a fixed text, see {@link #getMoreLinkText()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setMoreLinkText(String text) {
        this.moreLinkText = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the value in any form, see {@link #getMoreLinkText()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the text or callback, or null to remove the option
     */
    public void setMoreLinkText(TextValue value) {
        this.moreLinkText = value;
    }

    /**
     * Accessible hint ({@code aria-label}) for clickable day and week numbers (nav links). It describes what happens
     * after the nav link is clicked.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link TextContent}: shown as text.<br>
     *                                  {@code $0} stands for the localized date text, such as "Go to $0".
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setNavLinkHint(String)}: a text, as {@link TextContent}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: the hint of the locale, "Go to $0" in English.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(text, date)}, the localized date text and the JavaScript
     *                                  {@code Date} of the link.<br>
     *                                  It returns the text.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setNavLinkHint("Go to $0");
     * calendar.options().setNavLinkHint(JsCallback.of("(text, date) => 'Open ' + text"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/hints">navLinkHint</a>
     */
    @JsonConverter(TextValueConverter.class)
    private TextValue navLinkHint;

    /**
     * Sets a fixed text, see {@link #getNavLinkHint()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setNavLinkHint(String text) {
        this.navLinkHint = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the value in any form, see {@link #getNavLinkHint()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the text or callback, or null to remove the option
     */
    public void setNavLinkHint(TextValue value) {
        this.navLinkHint = value;
    }

    /**
     * Message shown in list views when no entries are in the shown range. It is the {@code text} of the {@code info} of
     * the no-entries render hooks.
     * <p>
     * The library default is the text of the locale, "No events to display" in English.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/no-events-render-hooks">noEventsText</a>
     */
    @JsonName("noEventsText")
    private String noEntriesText;

    /**
     * Accessibility label of the time axis next to the timed section of time grid views, read by screen readers. A
     * value set here wins over the text of the locale.
     * <p>
     * The library docs name this option only in their upgrade guide. The library default is the text of the locale
     * (English: "Timed").
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/upgrading-from-v6">timedText</a>
     */
    private String timedText;

    /**
     * Accessibility label of the button group in the built-in toolbar that switches the view, read by screen readers.
     * The library sets it only on a button group with more than one button, when all of them are view buttons. A value
     * set here wins over the text of the locale.
     * <p>
     * The library docs do not describe this option. The library default is the text of the locale (English: "Change
     * view").
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/hints">viewChangeHint</a>
     */
    private String viewChangeHint;

    /**
     * Format of the date text shown in each day cell of day grid views. It affects the {@code text} given to the day
     * cell render hooks.
     * <p>
     * For the first-of-month cells in day grid views that span several months and for the first cell of the view,
     * {@link #getMonthStartFormat() monthStartFormat} is used instead.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link DateFormat}: the properties of a format object, such as {@code weekday}
     *                                  or {@code month}.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setDayCellFormat(Map)}: the properties of the format object, as
     *                                  {@link DateFormat#of(Map)}.<br>
     *                                  A value that is no string, boolean or number throws an
     *                                  {@link IllegalArgumentException}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: the day number only.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code date}, {@code start}, {@code end}, {@code timeZone} and
     *                                  {@code localeCodes}.<br>
     *                                  {@code date} (the same as {@code start}) is a plain object with {@code year},
     *                                  {@code month} (zero-based), {@code day}, {@code hour}, {@code minute},
     *                                  {@code second}, {@code millisecond} and {@code marker}, not a JavaScript
     *                                  {@code Date}.<br>
     *                                  {@code end} is null here.<br>
     *                                  The library docs name a date as the argument, but the used library version
     *                                  passes the info object.<br>
     *                                  It returns the text.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setDayCellFormat(Map.of("day", "numeric"));
     * calendar.options().setDayCellFormat(JsCallback.of("info => info.date.day + '.'"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/dayCellFormat">dayCellFormat</a>
     */
    @JsonConverter(DateFormatValueConverter.class)
    private DateFormatValue dayCellFormat;

    /**
     * Sets a format from its properties, see {@link #getDayCellFormat()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param properties the properties of the format object, or null to remove the option
     */
    public void setDayCellFormat(Map<String, Object> properties) {
        this.dayCellFormat = properties == null ? null : DateFormat.of(properties);
    }

    /**
     * Sets the value in any form, see {@link #getDayCellFormat()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the format or callback, or null to remove the option
     */
    public void setDayCellFormat(DateFormatValue value) {
        this.dayCellFormat = value;
    }

    /**
     * Format of the text in the day headers (column headings).
     * <p>
     * The library docs show the default for views with several day columns as the short weekday with month and day, and
     * for views with one day column as the long weekday only. The used library version leaves out the month and shows
     * the day number in both. The Forma theme of the library sets the long weekday for month views.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link DateFormat}: the properties of a format object, such as {@code weekday}
     *                                  or {@code month}.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setDayHeaderFormat(Map)}: the properties of the format object, as
     *                                  {@link DateFormat#of(Map)}.<br>
     *                                  A value that is no string, boolean or number throws an
     *                                  {@link IllegalArgumentException}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: depends on the view (the short weekday in month views, the short
     *                                  weekday and day number in views with several day columns, and the long weekday
     *                                  and day number in views with one column).</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code date}, {@code start}, {@code end}, {@code timeZone} and
     *                                  {@code localeCodes}.<br>
     *                                  {@code date} (the same as {@code start}) is a plain object with {@code year},
     *                                  {@code month} (zero-based), {@code day}, {@code hour}, {@code minute},
     *                                  {@code second}, {@code millisecond} and {@code marker}, not a JavaScript
     *                                  {@code Date}.<br>
     *                                  {@code end} is null here.<br>
     *                                  The library docs name a date as the argument, but the used library version
     *                                  passes the info object.<br>
     *                                  It returns the text.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setDayHeaderFormat(Map.of("weekday", "long"));
     * calendar.options().setDayHeaderFormat(JsCallback.of("info => info.date.day + '/' + (info.date.month + 1)"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/dayHeaderFormat">dayHeaderFormat</a>
     */
    @JsonConverter(DateFormatValueConverter.class)
    private DateFormatValue dayHeaderFormat;

    /**
     * Sets a format from its properties, see {@link #getDayHeaderFormat()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param properties the properties of the format object, or null to remove the option
     */
    public void setDayHeaderFormat(Map<String, Object> properties) {
        this.dayHeaderFormat = properties == null ? null : DateFormat.of(properties);
    }

    /**
     * Sets the value in any form, see {@link #getDayHeaderFormat()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the format or callback, or null to remove the option
     */
    public void setDayHeaderFormat(DateFormatValue value) {
        this.dayHeaderFormat = value;
    }

    /**
     * Format of the time text shown on entries. It is shown for timed entries only.
     * <p>
     * For list views, the library docs give {@code 7pm}, which differs from the default of the used library version.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link DateFormat}: the properties of a format object, such as {@code hour} or
     *                                  {@code minute}.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setEntryTimeFormat(Map)}: the properties of the format object, as
     *                                  {@link DateFormat#of(Map)}.<br>
     *                                  A value that is no string, boolean or number throws an
     *                                  {@link IllegalArgumentException}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: depends on the view ({@code 7:00} in time grid views, {@code 7p} in day
     *                                  grid views and {@code 7:00pm} in list views).</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code date}, {@code start}, {@code end}, {@code timeZone} and
     *                                  {@code localeCodes}.<br>
     *                                  {@code date} (the same as {@code start}) is a plain object with {@code year},
     *                                  {@code month} (zero-based), {@code day}, {@code hour}, {@code minute},
     *                                  {@code second}, {@code millisecond} and {@code marker}, not a JavaScript
     *                                  {@code Date}.<br>
     *                                  {@code end} is set when the end time is shown, otherwise it is null.<br>
     *                                  The library docs name a date as the argument, but the used library version
     *                                  passes the info object.<br>
     *                                  It returns the text.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setEntryTimeFormat(Map.of("hour", "2-digit", "minute", "2-digit", "meridiem", false));
     * calendar.options().setEntryTimeFormat(JsCallback.of("info => info.start.hour + 'h'"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/eventTimeFormat">eventTimeFormat</a>
     */
    @JsonConverter(DateFormatValueConverter.class)
    @JsonName("eventTimeFormat")
    private DateFormatValue entryTimeFormat;

    /**
     * Sets a format from its properties, see {@link #getEntryTimeFormat()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param properties the properties of the format object, or null to remove the option
     */
    public void setEntryTimeFormat(Map<String, Object> properties) {
        this.entryTimeFormat = properties == null ? null : DateFormat.of(properties);
    }

    /**
     * Sets the value in any form, see {@link #getEntryTimeFormat()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the format or callback, or null to remove the option
     */
    public void setEntryTimeFormat(DateFormatValue value) {
        this.entryTimeFormat = value;
    }

    /**
     * Format of the text on the right side of the day headings in list views.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link DateFormat}: the properties of a format object, such as {@code weekday}
     *                                  or {@code month}.
     *                                  <p>{@link ListDayFormatValue#NONE}: no text.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setListDayAltFormat(Map)}: the properties of the format object, as
     *                                  {@link DateFormat#of(Map)}.<br>
     *                                  A value that is no string, boolean or number throws an
     *                                  {@link IllegalArgumentException}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: depends on the view (no text in views of one day, the full date, such
     *                                  as "January 1, 2026" in English, in views of one week or of a number of days,
     *                                  and the long weekday name in longer views).</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code date}, {@code start}, {@code end}, {@code timeZone} and
     *                                  {@code localeCodes}.<br>
     *                                  {@code date} (the same as {@code start}) is a plain object with {@code year},
     *                                  {@code month} (zero-based), {@code day}, {@code hour}, {@code minute},
     *                                  {@code second}, {@code millisecond} and {@code marker}, not a JavaScript
     *                                  {@code Date}.<br>
     *                                  {@code end} is null here.<br>
     *                                  The library docs name a date as the argument, but the used library version
     *                                  passes the info object.<br>
     *                                  It returns the text.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setListDayAltFormat(Map.of("weekday", "long"));
     * calendar.options().setListDayAltFormat(ListDayFormatValue.NONE);
     * calendar.options().setListDayAltFormat(JsCallback.of("info => info.date.day + '.'"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/listDayAltFormat">listDayAltFormat</a>
     */
    @JsonConverter(ListDayFormatValueConverter.class)
    private ListDayFormatValue listDayAltFormat;

    /**
     * Sets a format from its properties, see {@link #getListDayAltFormat()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param properties the properties of the format object, or null to remove the option
     */
    public void setListDayAltFormat(Map<String, Object> properties) {
        this.listDayAltFormat = properties == null ? null : DateFormat.of(properties);
    }

    /**
     * Sets the value in any form, see {@link #getListDayAltFormat()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the format or callback, or null to remove the option
     */
    public void setListDayAltFormat(ListDayFormatValue value) {
        this.listDayAltFormat = value;
    }

    /**
     * Format of the text on the left side of the day headings in list views.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link DateFormat}: the properties of a format object, such as {@code weekday}
     *                                  or {@code month}.
     *                                  <p>{@link ListDayFormatValue#NONE}: no text.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setListDayFormat(Map)}: the properties of the format object, as
     *                                  {@link DateFormat#of(Map)}.<br>
     *                                  A value that is no string, boolean or number throws an
     *                                  {@link IllegalArgumentException}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: depends on the view (the long weekday name in views of one day, of one
     *                                  week or of a number of days, and the full date, such as "January 1, 2026" in
     *                                  English, in longer views).</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code date}, {@code start}, {@code end}, {@code timeZone} and
     *                                  {@code localeCodes}.<br>
     *                                  {@code date} (the same as {@code start}) is a plain object with {@code year},
     *                                  {@code month} (zero-based), {@code day}, {@code hour}, {@code minute},
     *                                  {@code second}, {@code millisecond} and {@code marker}, not a JavaScript
     *                                  {@code Date}.<br>
     *                                  {@code end} is null here.<br>
     *                                  The library docs name a date as the argument, but the used library version
     *                                  passes the info object.<br>
     *                                  It returns the text.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setListDayFormat(Map.of("weekday", "long"));
     * calendar.options().setListDayFormat(ListDayFormatValue.NONE);
     * calendar.options().setListDayFormat(JsCallback.of("info => info.date.day + '.'"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/listDayFormat">listDayFormat</a>
     */
    @JsonConverter(ListDayFormatValueConverter.class)
    private ListDayFormatValue listDayFormat;

    /**
     * Sets a format from its properties, see {@link #getListDayFormat()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param properties the properties of the format object, or null to remove the option
     */
    public void setListDayFormat(Map<String, Object> properties) {
        this.listDayFormat = properties == null ? null : DateFormat.of(properties);
    }

    /**
     * Sets the value in any form, see {@link #getListDayFormat()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the format or callback, or null to remove the option
     */
    public void setListDayFormat(ListDayFormatValue value) {
        this.listDayFormat = value;
    }

    /**
     * Format of the first cell of each month and of the first cell of the view, when a day grid view spans several
     * months.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link DateFormat}: the properties of a format object, such as {@code month} or
     *                                  {@code day}.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setMonthStartFormat(Map)}: the properties of the format object, as
     *                                  {@link DateFormat#of(Map)}.<br>
     *                                  A value that is no string, boolean or number throws an
     *                                  {@link IllegalArgumentException}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: the long month and the day, such as "January 1" in English.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code date}, {@code start}, {@code end}, {@code timeZone} and
     *                                  {@code localeCodes}.<br>
     *                                  {@code date} (the same as {@code start}) is a plain object with {@code year},
     *                                  {@code month} (zero-based), {@code day}, {@code hour}, {@code minute},
     *                                  {@code second}, {@code millisecond} and {@code marker}, not a JavaScript
     *                                  {@code Date}.<br>
     *                                  {@code end} is null here.<br>
     *                                  The library docs name a date as the argument, but the used library version
     *                                  passes the info object.<br>
     *                                  It returns the text.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setMonthStartFormat(Map.of("month", "short", "day", "numeric"));
     * calendar.options().setMonthStartFormat(
     *         JsCallback.of("info => info.date.day + '.' + (info.date.month + 1) + '.'"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/monthStartFormat">monthStartFormat</a>
     */
    @JsonConverter(DateFormatValueConverter.class)
    private DateFormatValue monthStartFormat;

    /**
     * Sets a format from its properties, see {@link #getMonthStartFormat()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param properties the properties of the format object, or null to remove the option
     */
    public void setMonthStartFormat(Map<String, Object> properties) {
        this.monthStartFormat = properties == null ? null : DateFormat.of(properties);
    }

    /**
     * Sets the value in any form, see {@link #getMonthStartFormat()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the format or callback, or null to remove the option
     */
    public void setMonthStartFormat(DateFormatValue value) {
        this.monthStartFormat = value;
    }

    /**
     * Format of the date in the title of the "+N more" popover.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link DateFormat}: the properties of a format object, such as {@code month} or
     *                                  {@code day}.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setPopoverFormat(Map)}: the properties of the format object, as
     *                                  {@link DateFormat#of(Map)}.<br>
     *                                  A value that is no string, boolean or number throws an
     *                                  {@link IllegalArgumentException}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: the long month, the day and the year.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code date}, {@code start}, {@code end}, {@code timeZone} and
     *                                  {@code localeCodes}.<br>
     *                                  {@code date} (the same as {@code start}) is a plain object with {@code year},
     *                                  {@code month} (zero-based), {@code day}, {@code hour}, {@code minute},
     *                                  {@code second}, {@code millisecond} and {@code marker}, not a JavaScript
     *                                  {@code Date}.<br>
     *                                  {@code end} is null here.<br>
     *                                  The library docs name a date as the argument, but the used library version
     *                                  passes the info object.<br>
     *                                  It returns the text.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setPopoverFormat(Map.of("month", "short", "day", "numeric"));
     * calendar.options().setPopoverFormat(
     *         JsCallback.of("info => info.date.day + '.' + (info.date.month + 1) + '.' + info.date.year"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/popoverFormat">popoverFormat</a>
     */
    @JsonConverter(DateFormatValueConverter.class)
    private DateFormatValue popoverFormat;

    /**
     * Sets a format from its properties, see {@link #getPopoverFormat()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param properties the properties of the format object, or null to remove the option
     */
    public void setPopoverFormat(Map<String, Object> properties) {
        this.popoverFormat = properties == null ? null : DateFormat.of(properties);
    }

    /**
     * Sets the value in any form, see {@link #getPopoverFormat()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the format or callback, or null to remove the option
     */
    public void setPopoverFormat(DateFormatValue value) {
        this.popoverFormat = value;
    }

    /**
     * Format of the text above each month in multi-month views.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link DateFormat}: the properties of a format object, such as {@code month} or
     *                                  {@code day}.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setSingleMonthTitleFormat(Map)}: the properties of the format object, as
     *                                  {@link DateFormat#of(Map)}.<br>
     *                                  A value that is no string, boolean or number throws an
     *                                  {@link IllegalArgumentException}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: the long month if the calendar spans a single year, otherwise the long
     *                                  month and the year.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code date}, {@code start}, {@code end}, {@code timeZone} and
     *                                  {@code localeCodes}.<br>
     *                                  {@code date} (the same as {@code start}) is a plain object with {@code year},
     *                                  {@code month} (zero-based), {@code day}, {@code hour}, {@code minute},
     *                                  {@code second}, {@code millisecond} and {@code marker}, not a JavaScript
     *                                  {@code Date}.<br>
     *                                  {@code end} is null here.<br>
     *                                  The library docs name a date as the argument, but the used library version
     *                                  passes the info object.<br>
     *                                  It returns the text.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setSingleMonthTitleFormat(Map.of("month", "short"));
     * calendar.options().setSingleMonthTitleFormat(
     *         JsCallback.of("info => info.date.year + '/' + (info.date.month + 1)"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/singleMonthTitleFormat">singleMonthTitleFormat</a>
     */
    @JsonConverter(DateFormatValueConverter.class)
    private DateFormatValue singleMonthTitleFormat;

    /**
     * Sets a format from its properties, see {@link #getSingleMonthTitleFormat()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param properties the properties of the format object, or null to remove the option
     */
    public void setSingleMonthTitleFormat(Map<String, Object> properties) {
        this.singleMonthTitleFormat = properties == null ? null : DateFormat.of(properties);
    }

    /**
     * Sets the value in any form, see {@link #getSingleMonthTitleFormat()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the format or callback, or null to remove the option
     */
    public void setSingleMonthTitleFormat(DateFormatValue value) {
        this.singleMonthTitleFormat = value;
    }

    /**
     * Format of the time slot headers.
     * <p>
     * With one format per header row, the top row comes first, for example the month above the weekday. The time
     * grid uses the first format only.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link DateFormat}: the properties of a format object, such as {@code hour} or
     *                                  {@code minute}.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.
     *                                  <p>{@link SlotHeaderFormatValue#of(DateFormatValue...)}: one format per header
     *                                  row, timeline views, scheduler only.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setSlotHeaderFormat(Map)}: the properties of the format object, as
     *                                  {@link DateFormat#of(Map)}.<br>
     *                                  A value that is no string, boolean or number throws an
     *                                  {@link IllegalArgumentException}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: in the time grid
     *                                  {@code {hour: 'numeric', minute: '2-digit', omitZeroMinute: true,
     *                                  meridiem: 'short'}},
     *                                  which shows times like "5pm" and "5:30pm" in English.<br>
     *                                  A timeline view picks its header formats from its slot duration.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code date}, {@code start}, {@code end}, {@code timeZone} and
     *                                  {@code localeCodes}.<br>
     *                                  {@code date} (the same as {@code start}) is a plain object with {@code year},
     *                                  {@code month} (zero-based), {@code day}, {@code hour}, {@code minute},
     *                                  {@code second}, {@code millisecond} and {@code marker}, not a JavaScript
     *                                  {@code Date}.<br>
     *                                  {@code end} is null here.<br>
     *                                  The library docs name a date as the argument, but the used library version
     *                                  passes the info object.<br>
     *                                  It returns the text.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setSlotHeaderFormat(Map.of("hour", "2-digit", "minute", "2-digit", "hour12", false));
     * calendar.options().setSlotHeaderFormat(JsCallback.of("info => info.date.hour + ' h'"));
     * calendar.options().setSlotHeaderFormat(SlotHeaderFormatValue.of(
     *         DateFormat.of(Map.of("month", "long")), DateFormat.of(Map.of("weekday", "short"))));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/slotHeaderFormat">slotHeaderFormat</a>
     */
    @JsonConverter(SlotHeaderFormatValueConverter.class)
    private SlotHeaderFormatValue slotHeaderFormat;

    /**
     * Sets a format from its properties, see {@link #getSlotHeaderFormat()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param properties the properties of the format object, or null to remove the option
     */
    public void setSlotHeaderFormat(Map<String, Object> properties) {
        this.slotHeaderFormat = properties == null ? null : DateFormat.of(properties);
    }

    /**
     * Sets the value in any form, see {@link #getSlotHeaderFormat()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the format, callback or formats per header row, or null to remove the option
     */
    public void setSlotHeaderFormat(SlotHeaderFormatValue value) {
        this.slotHeaderFormat = value;
    }

    /**
     * Format of the title text in the built-in header toolbar.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link DateFormat}: the properties of a format object, such as {@code month} or
     *                                  {@code day}.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setTitleFormat(Map)}: the properties of the format object, as
     *                                  {@link DateFormat#of(Map)}.<br>
     *                                  A value that is no string, boolean or number throws an
     *                                  {@link IllegalArgumentException}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: depends on the view.<br>
     *                                  Month and week views show the year and the long month ("September 2009"), day
     *                                  views also the day ("September 8 2009").<br>
     *                                  When the range spans two months, the months are short.<br>
     *                                  List views of several days show the days as well.<br>
     *                                  The library docs show the day in week views too ("Sep 13 2009"), which the used
     *                                  library version does not.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code date}, {@code start}, {@code end}, {@code timeZone} and
     *                                  {@code localeCodes}.<br>
     *                                  {@code date} (the same as {@code start}) is a plain object with {@code year},
     *                                  {@code month} (zero-based), {@code day}, {@code hour}, {@code minute},
     *                                  {@code second}, {@code millisecond} and {@code marker}, not a JavaScript
     *                                  {@code Date}.<br>
     *                                  {@code end} is the end of the shown range.<br>
     *                                  The library docs name a date as the argument, but the used library version
     *                                  passes the info object.<br>
     *                                  It returns the text.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setTitleFormat(Map.of("year", "numeric", "month", "short"));
     * calendar.options().setTitleFormat(JsCallback.of("info => info.start.year + ' / ' + (info.start.month + 1)"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/titleFormat">titleFormat</a>
     */
    @JsonConverter(DateFormatValueConverter.class)
    private DateFormatValue titleFormat;

    /**
     * Sets a format from its properties, see {@link #getTitleFormat()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param properties the properties of the format object, or null to remove the option
     */
    public void setTitleFormat(Map<String, Object> properties) {
        this.titleFormat = properties == null ? null : DateFormat.of(properties);
    }

    /**
     * Sets the value in any form, see {@link #getTitleFormat()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the format or callback, or null to remove the option
     */
    public void setTitleFormat(DateFormatValue value) {
        this.titleFormat = value;
    }

    /**
     * Text format of the week numbers. The text depends on {@code weekTextShort}: with {@code "W"}, {@code narrow}
     * gives {@code "W6"}, {@code short} gives {@code "W 6"} and {@code numeric} gives {@code "6"}.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link DateFormat}: the properties of a format object, such as {@code month} or
     *                                  {@code day}.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setWeekNumberFormat(Map)}: the properties of the format object, as
     *                                  {@link DateFormat#of(Map)}.<br>
     *                                  A value that is no string, boolean or number throws an
     *                                  {@link IllegalArgumentException}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: {@code week: 'narrow'} in day grid views and {@code week: 'short'} in
     *                                  time grid views.<br>
     *                                  The library docs name {@code week: 'narrow'} for all views.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code date}, {@code start}, {@code end}, {@code timeZone} and
     *                                  {@code localeCodes}.<br>
     *                                  {@code date} (the same as {@code start}) is a plain object with {@code year},
     *                                  {@code month} (zero-based), {@code day}, {@code hour}, {@code minute},
     *                                  {@code second}, {@code millisecond} and {@code marker}, not a JavaScript
     *                                  {@code Date}.<br>
     *                                  {@code end} is null here.<br>
     *                                  The library docs name a date as the argument, but the used library version
     *                                  passes the info object.<br>
     *                                  It returns the text.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setWeekNumberFormat(Map.of("week", "numeric"));
     * calendar.options().setWeekNumberFormat(
     *         JsCallback.of("info => 'Week of ' + info.date.day + '.' + (info.date.month + 1) + '.'"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/weekNumberFormat">weekNumberFormat</a>
     */
    @JsonConverter(DateFormatValueConverter.class)
    private DateFormatValue weekNumberFormat;

    /**
     * Sets a format from its properties, see {@link #getWeekNumberFormat()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param properties the properties of the format object, or null to remove the option
     */
    public void setWeekNumberFormat(Map<String, Object> properties) {
        this.weekNumberFormat = properties == null ? null : DateFormat.of(properties);
    }

    /**
     * Sets the value in any form, see {@link #getWeekNumberFormat()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the format or callback, or null to remove the option
     */
    public void setWeekNumberFormat(DateFormatValue value) {
        this.weekNumberFormat = value;
    }

    // Toolbar and entry sources

    /**
     * Whether the buttons of the built-in toolbars show an icon, text, or both. A single button overrides it with the
     * {@code display} setting of its entry in {@link FullCalendarOptions#getButtons() buttons}. The library default is
     * {@link NativeToolbarButtonDisplay#AUTO}, the icon if the theme defines one, otherwise the text.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/buttonDisplay">buttonDisplay</a>
     */
    private NativeToolbarButtonDisplay buttonDisplay;

    /**
     * CSS classes for the built-in header toolbar only. {@link #getToolbarClass() toolbarClass} applies to both
     * toolbars.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setHeaderToolbarClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes, a theme can set classes of its own.
     *                                  <p>add-on: the stable class {@code fc-header-toolbar}.<br>
     *                                  Classes set here are added to it.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code borderlessX}, {@code borderlessTop} and
     *                                  {@code borderlessBottom}.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setHeaderToolbarClass("my-toolbar");
     * calendar.options().setHeaderToolbarClass(JsCallback.of("info => info.borderlessTop ? 'my-flat-toolbar' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/toolbar-render-hooks">headerToolbarClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue headerToolbarClass;

    /**
     * Sets fixed CSS classes, see {@link #getHeaderToolbarClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setHeaderToolbarClass(String classNames) {
        this.headerToolbarClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getHeaderToolbarClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setHeaderToolbarClass(ClassNameValue value) {
        this.headerToolbarClass = value;
    }

    /**
     * CSS classes for the built-in footer toolbar only. {@link #getToolbarClass() toolbarClass} applies to both
     * toolbars.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setFooterToolbarClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes, a theme can set classes of its own.
     *                                  <p>add-on: the stable class {@code fc-footer-toolbar}.<br>
     *                                  Classes set here are added to it.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code borderlessX}, {@code borderlessTop} and
     *                                  {@code borderlessBottom}.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setFooterToolbarClass("my-toolbar");
     * calendar.options().setFooterToolbarClass(JsCallback.of("info => info.borderlessTop ? 'my-flat-toolbar' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/toolbar-render-hooks">footerToolbarClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue footerToolbarClass;

    /**
     * Sets fixed CSS classes, see {@link #getFooterToolbarClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setFooterToolbarClass(String classNames) {
        this.footerToolbarClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getFooterToolbarClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setFooterToolbarClass(ClassNameValue value) {
        this.footerToolbarClass = value;
    }
}
