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
import org.vaadin.stefan.fullcalendar.converters.ClassNameValueConverter;
import org.vaadin.stefan.fullcalendar.converters.ContentValueConverter;
import org.vaadin.stefan.fullcalendar.converters.HeaderAlignValueConverter;
import org.vaadin.stefan.fullcalendar.converters.OrderValueConverter;
import org.vaadin.stefan.fullcalendar.converters.WidthValueConverter;
import org.vaadin.stefan.fullcalendar.json.JsonConverter;
import org.vaadin.stefan.fullcalendar.json.JsonName;
import org.vaadin.stefan.fullcalendar.option.ClassName;
import org.vaadin.stefan.fullcalendar.option.ClassNameValue;
import org.vaadin.stefan.fullcalendar.option.ContentValue;
import org.vaadin.stefan.fullcalendar.option.EntryPrintLayout;
import org.vaadin.stefan.fullcalendar.option.HeaderAlign;
import org.vaadin.stefan.fullcalendar.option.HeaderAlignValue;
import org.vaadin.stefan.fullcalendar.option.JsCallback;
import org.vaadin.stefan.fullcalendar.option.Order;
import org.vaadin.stefan.fullcalendar.option.OrderValue;
import org.vaadin.stefan.fullcalendar.option.TextContent;
import org.vaadin.stefan.fullcalendar.option.WidthValue;

/**
 * The scheduler options that can apply per view, besides those of {@link ViewOptions}. Null means not set. This class
 * is not public API. Users reach the fields through {@link FullCalendarScheduler#options()}.
 */
@Getter
@Setter
class SchedulerViewOptions extends ViewOptions {

    // FC-UPDATE: check the scheduler options of the new library version against these fields: new, renamed, removed
    //  or retyped options, and options that became view-specific or global. A renamed option changes only @JsonName.
    //  The test resource option-names.txt of this module lists the supported scheduler names as global or per view.

    /**
     * Groups the resources by a field each resource has, with a divider at the top of each group. The value is the
     * name of that field, for example {@code "groupId"}. The text of the divider comes from
     * {@code resourceGroupHeaderContent}.
     * <p>
     * The library ignores this option while a resource column is marked as a group column ({@code resourceColumns}).
     * It groups by that column then. The library docs do not say this.
     * <p>
     * The library has no default. Without a value, the resources are not grouped by a field.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resourceGroupField">resourceGroupField</a>
     */
    private String resourceGroupField;

    /**
     * Whether child resources are expanded when the view loads. Set it to {@code false} to start with them collapsed.
     * Only the timeline view supports it.
     * <p>
     * The library default is {@code true}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resourcesInitiallyExpanded">resourcesInitiallyExpanded</a>
     */
    private Boolean resourcesInitiallyExpanded;

    /**
     * The order of the resource list.
     * <p>
     * The two plain objects of a compare function hold the resource fields with the extended properties merged in,
     * so a function reads {@code a.rank}, not {@code a.extendedProps.rank}.
     * <p>
     * The library docs describe only the string form. The list and function forms come from the library code.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link Order}: a list of sort keys.<br>
     *                                  Each is a resource property name, ascending or descending, or a compare
     *                                  {@link JsCallback}.
     *                                  <p>{@link JsCallback}: one function that compares two resources.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setResourceOrder(String...)}: resource property names, a leading
     *                                  {@code -} sorts descending, as {@link Order}, at least one, none empty or
     *                                  containing a comma.
     *                                  <p>{@link #setResourceOrder(Order.Key...)}: sort keys that may mix properties
     *                                  and compare functions, as {@link Order}, at least one.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: {@code "id,title"}.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(a, b)}, two resources as plain objects.<br>
     *                                  It returns a negative number, zero or a positive number.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setResourceOrder("title");
     * calendar.options().setResourceOrder("-type", "title");
     * calendar.options().setResourceOrder(JsCallback.of("(a, b) => a.rank - b.rank"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resourceOrder">resourceOrder</a>
     */
    @JsonConverter(OrderValueConverter.class)
    private OrderValue resourceOrder;

    /**
     * Sets the order from resource property names, see {@link #getResourceOrder()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param properties the names, such as {@code "title"} or {@code "-title"} for descending, or null to remove the
     *        option
     */
    public void setResourceOrder(String... properties) {
        this.resourceOrder = properties == null ? null : Order.of(properties);
    }

    /**
     * Sets the order from sort keys, which may mix properties and compare functions, see {@link #getResourceOrder()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param keys the sort keys, at least one, or null to remove the option
     */
    public void setResourceOrder(Order.Key... keys) {
        this.resourceOrder = keys == null ? null : Order.of(keys);
    }

    /**
     * Sets the order in any form, see {@link #getResourceOrder()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the order or a compare callback, or null to remove the option
     */
    public void setResourceOrder(OrderValue value) {
        this.resourceOrder = value;
    }

    /**
     * In the vertical resource views ({@code resourceTimeGrid} and {@code resourceDayGrid}), puts the date headings
     * above the resource headings instead of the resource headings above the date headings.
     * <p>
     * The library default is {@code false}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/datesAboveResources">datesAboveResources</a>
     */
    private Boolean datesAboveResources;

    /**
     * Shows only resources that have entries assigned. The entries must be fetched before the resources can render.
     * <p>
     * The library default is {@code false}. The getter returns null while the option is not set; then the library
     * default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/filterResourcesWithEvents">filterResourcesWithEvents</a>
     */
    @JsonName("filterResourcesWithEvents")
    private Boolean filterResourcesWithEntries;

    /**
     * Allows the user to drag entries between resources. Without a value, it follows {@code editable}.
     * <p>
     * The library default is the value of {@code editable}, which is {@code false} by default. The getter returns null
     * while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/eventResourceEditable">eventResourceEditable</a>
     */
    @JsonName("eventResourceEditable")
    private Boolean entryResourceEditable;

    /**
     * Horizontal alignment of the resource name in the column header cells of the vertical resource views
     * ({@code resourceTimeGrid} and {@code resourceDayGrid}). In a left-to-right locale, start means left and end
     * means right.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link HeaderAlign#START}, {@link HeaderAlign#CENTER} or
     *                                  {@link HeaderAlign#END}: a fixed alignment.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: decided by the theme.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has only {@code level} (the header row), {@code isNarrow} and
     *                                  {@code inPopover}, which is never true for a resource header.<br>
     *                                  It returns {@code "start"}, {@code "center"} or {@code "end"}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setResourceDayHeaderAlign(HeaderAlign.END);
     * calendar.options().setResourceDayHeaderAlign(JsCallback.of("info => info.isNarrow ? 'center' : 'start'"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-day-header-render-hooks">resourceDayHeaderAlign</a>
     */
    @JsonConverter(HeaderAlignValueConverter.class)
    private HeaderAlignValue resourceDayHeaderAlign;

    /**
     * The width of the area that contains the list of resources.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>     <dd>{@link WidthValue#ofPixels(int)}: pixels.
     *                                 <p>{@link WidthValue#ofPercent(double)}: a percentage.</dd>
     *   <dt><b>Shortcuts</b></dt> <dd>{@link #setResourceColumnsWidth(int)}: pixels, as
     *                                 {@link WidthValue#ofPixels(int)}, not negative.</dd>
     *   <dt><b>Default</b></dt>   <dd>library: {@code "30%"} in the resource timeline views.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setResourceColumnsWidth(200);
     * calendar.options().setResourceColumnsWidth(WidthValue.ofPercent(25));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resourceColumnsWidth">resourceColumnsWidth</a>
     */
    @JsonConverter(WidthValueConverter.class)
    private WidthValue resourceColumnsWidth;

    /**
     * Sets the width in pixels, see {@link #getResourceColumnsWidth()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param pixels the width in pixels, not negative
     */
    public void setResourceColumnsWidth(int pixels) {
        this.resourceColumnsWidth = WidthValue.ofPixels(pixels);
    }

    /**
     * Sets the width in any form, see {@link #getResourceColumnsWidth()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the width in pixels or as a percentage, or null to remove the option
     */
    public void setResourceColumnsWidth(WidthValue value) {
        this.resourceColumnsWidth = value;
    }

    /**
     * Minimum width of each slot of the time axis in timeline views, in pixels. Slots become wider when the view has
     * room to spare. The library docs speak of the exact width, but the used library version treats the value as a
     * minimum.
     * <p>
     * The library default is no value. Then the minimum is computed from the width of the slot labels.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/slotMinWidth">slotMinWidth</a>
     */
    private Integer slotMinWidth;

    /**
     * The minimum width of an entry in the timeline view, in pixels.
     * <p>
     * The library default is 30. The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/eventMinWidth">eventMinWidth</a>
     */
    @JsonName("eventMinWidth")
    private Integer entryMinWidth;

    /**
     * Renders only the rows and columns currently visible in the scroll viewport. This improves performance when a
     * calendar has a large number of resources. Rows and columns that leave the viewport are removed from the DOM and
     * newly visible ones are rendered while the user scrolls.
     * <p>
     * Currently it only applies to timeline views that display resources.
     * <p>
     * The library default is {@code false}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/virtualization">virtualization</a>
     */
    private Boolean virtualization;

    /**
     * How time grid views lay out entries when printing. Browsers, especially Firefox, have difficulties printing
     * absolutely positioned elements across pages, which affects time grid views most.
     * <p>
     * It applies while the calendar is in print mode, which the library's adaptive plugin switches on when the browser
     * prints. {@link FullCalendarScheduler} loads that plugin. The library default is {@link EntryPrintLayout#AUTO},
     * which stacks the entries in Firefox and keeps the grid in other browsers.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/eventPrintLayout">eventPrintLayout</a>
     */
    @JsonName("eventPrintLayout")
    private EntryPrintLayout entryPrintLayout;

    /**
     * The maximum number of rows rendered when printing. Printed output does not use virtual rendering, so a
     * calendar with very many resources could overwhelm the browser. If there are more rows, the extra rows are left
     * out and the client logs a warning to the browser console.
     * <p>
     * It applies while the calendar is in print mode, which the library's adaptive plugin switches on when the browser
     * prints. {@link FullCalendarScheduler} loads that plugin. Only timeline views read it.
     * <p>
     * The library default is 1000.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/printMaxRows">printMaxRows</a>
     */
    private Integer printMaxRows;

    /**
     * The FullCalendar Scheduler license key, needed to use the premium features. It is either a commercial key or
     * one of the free keys {@link Scheduler#AGPL_V3_LICENSE_KEY} and
     * {@link Scheduler#NON_COMMERCIAL_CREATIVE_COMMONS_LICENSE_KEY}.
     * <p>
     * The library has no default key. Without a valid key, the library shows a warning about the license key. The
     * add-on sets no key itself.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/schedulerLicenseKey">schedulerLicenseKey</a>
     */
    private String schedulerLicenseKey;

    /**
     * A callback that runs after the resource data is initialized or changed in any way.
     * <p>
     * It gets {@code resources}, an array of all resources, the same as the library's {@code Calendar.getResources}
     * returns.
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resourcesSet">resourcesSet</a>
     */
    private JsCallback resourcesSet;

    /**
     * A callback that runs after a resource has been added to the calendar, that is after
     * {@code Calendar.addResource} was called on the client. Resources the server adds reach the client this way, so it
     * runs for them too.
     * <p>
     * It gets an {@code addInfo} object with {@code resource} (the added resource) and {@code revert} (a function
     * that reverses the action).
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resourceAdd">resourceAdd</a>
     */
    private JsCallback resourceAdd;

    /**
     * A callback that runs after a resource has been modified, that is after {@code Resource.setProp} or
     * {@code Resource.setExtendedProp} was called on the client. A resource change the server sends reaches the
     * client this way, so it runs for it too.
     * <p>
     * It gets a {@code changeInfo} object with {@code resource} (with the changed data), {@code oldResource} (with
     * the data before the change) and {@code revert} (a function that reverses the action).
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resourceChange">resourceChange</a>
     */
    private JsCallback resourceChange;

    /**
     * A callback that runs after a resource has been removed from the calendar, that is after
     * {@code Resource.remove} was called on the client. Resources the server removes reach the client this way, so it
     * runs for them too.
     * <p>
     * It gets a {@code removeInfo} object with {@code resource} (the removed resource) and {@code revert} (a function
     * that reverses the action).
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resourceRemove">resourceRemove</a>
     */
    private JsCallback resourceRemove;

    /**
     * CSS classes for the cells of the resource area in the timeline view. Each cell shows one column field, the
     * resource title or a column of {@code resourceColumns}.
     * <p>
     * The hook also runs for the cells of a group column (a resource column with {@code group} set). The group rows
     * of {@code resourceGroupField} use the {@code resourceGroupHeader…} options instead.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setResourceCellClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes, a theme can set classes of its own.
     *                                  <p>add-on: the stable classes {@code fc-datagrid-cell} and
     *                                  {@code fc-resource}.<br>
     *                                  Classes set here are added to them.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code resource} (not in group columns), {@code field},
     *                                  {@code fieldValue} and {@code view}.<br>
     *                                  In group columns it has only {@code fieldValue} and {@code view}.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setResourceCellClass("my-class");
     * calendar.options().setResourceCellClass(
     *         JsCallback.of("info => info.field === 'title' ? 'my-title-cell' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-cell-render-hooks">resourceCellClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue resourceCellClass;

    /**
     * Sets fixed CSS classes, see {@link #getResourceCellClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setResourceCellClass(String classNames) {
        this.resourceCellClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getResourceCellClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setResourceCellClass(ClassNameValue value) {
        this.resourceCellClass = value;
    }

    /**
     * Custom content for the cells of the resource area in the timeline view. Branch on {@code info.field} to change
     * the title cell only.
     * <p>
     * The cell content of a resource column replaces it in that column. A callback of the column falls back to this
     * one by returning {@code true}.
     * <p>
     * The hook also runs for the cells of a group column (a resource column with {@code group} set). The group rows
     * of {@code resourceGroupField} use the {@code resourceGroupHeader…} options instead.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link TextContent}: shown as text.
     *                                  <p>{@link ContentValue#ofHtml(String)}: HTML, not escaped.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setResourceCellContent(String)}: a text, as {@link TextContent}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: the value of the column field as text.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code resource} (not in group columns), {@code field},
     *                                  {@code fieldValue} and {@code view}.<br>
     *                                  In group columns it has only {@code fieldValue} and {@code view}.<br>
     *                                  It returns a text, {@code {html: '...'}} or {@code {domNodes: [...]}}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setResourceCellContent("Name");
     * calendar.options().setResourceCellContent(ContentValue.ofHtml("<b>Name</b>"));
     * calendar.options().setResourceCellContent(
     *         JsCallback.of("info => info.field === 'title' ? '* ' + info.fieldValue : info.fieldValue"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-cell-render-hooks">resourceCellContent</a>
     */
    @JsonConverter(ContentValueConverter.class)
    private ContentValue resourceCellContent;

    /**
     * Sets a text as the content, see {@link #getResourceCellContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setResourceCellContent(String text) {
        this.resourceCellContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content in any form, see {@link #getResourceCellContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setResourceCellContent(ContentValue value) {
        this.resourceCellContent = value;
    }

    /**
     * A callback that runs right after a cell of the resource area has been added to the DOM. It gets an {@code info}
     * object with {@code el} (the element), {@code resource} (absent in group columns), {@code field} and more, see the
     * library docs.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-cell-render-hooks">resourceCellDidMount</a>
     */
    private JsCallback resourceCellDidMount;

    /**
     * CSS classes for the inner wrapper of a cell of the resource area in the timeline view. The inner wrapper holds
     * the content and is useful for adjusting padding.
     * <p>
     * The hook also runs for the cells of a group column (a resource column with {@code group} set). The group rows
     * of {@code resourceGroupField} use the {@code resourceGroupHeader…} options instead.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setResourceCellInnerClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes, a theme can set classes of its own.
     *                                  <p>add-on: the stable class {@code fc-datagrid-cell-cushion}.<br>
     *                                  Classes set here are added to it.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code resource} (not in group columns), {@code field},
     *                                  {@code fieldValue} and {@code view}.<br>
     *                                  In group columns it has only {@code fieldValue} and {@code view}.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setResourceCellInnerClass("my-class");
     * calendar.options().setResourceCellInnerClass(
     *         JsCallback.of("info => info.field === 'title' ? 'my-title-cell' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-cell-render-hooks">resourceCellInnerClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue resourceCellInnerClass;

    /**
     * Sets fixed CSS classes, see {@link #getResourceCellInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setResourceCellInnerClass(String classNames) {
        this.resourceCellInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getResourceCellInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setResourceCellInnerClass(ClassNameValue value) {
        this.resourceCellInnerClass = value;
    }

    /**
     * A callback that runs right before a cell of the resource area is removed from the DOM. It gets an {@code info}
     * object with {@code el} (the element), {@code resource} (absent in group columns), {@code field} and more, see the
     * library docs.
     * <p>
     * When this option is changed for an attached calendar, the callback that is set at the time an element is removed
     * runs for it. This includes elements that were rendered before the change.
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-cell-render-hooks">resourceCellWillUnmount</a>
     */
    private JsCallback resourceCellWillUnmount;

    /**
     * CSS classes for the divider line between the resource area and the timeline area in the timeline view.
     * <p>
     * It takes class names only, because the library accepts no callback for this option.
     * <p>
     * The library default is no classes. A theme can set classes of its own, and the classes set here are added to
     * them.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-column-divider-render-hooks">resourceColumnDividerClass</a>
     */
    private String resourceColumnDividerClass;

    /**
     * CSS classes for the resource column header, shown above the resource data in the timeline view. With resource
     * columns set (see {@link FullCalendarSchedulerOptions#setResourceColumns(java.util.List)}), they apply to the
     * header of every column.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setResourceColumnHeaderClass(String)}: class names separated by spaces,
     *                                  as {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: the stable class {@code fc-datagrid-cell}.<br>
     *                                  Classes set here are added to it.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code view}.<br>
     *                                  The library docs do not list it, but the used library version passes it.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setResourceColumnHeaderClass("my-class");
     * calendar.options().setResourceColumnHeaderClass(
     *         JsCallback.of("info => info.view.type.startsWith('resourceTimeline') ? 'my-header' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-column-header-render-hooks">resourceColumnHeaderClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue resourceColumnHeaderClass;

    /**
     * Sets fixed CSS classes, see {@link #getResourceColumnHeaderClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setResourceColumnHeaderClass(String classNames) {
        this.resourceColumnHeaderClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getResourceColumnHeaderClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setResourceColumnHeaderClass(ClassNameValue value) {
        this.resourceColumnHeaderClass = value;
    }

    /**
     * Custom content for the resource column header, shown above the resource data in the timeline view.
     * <p>
     * With {@code resourceColumns} set, it is shown as a header above all columns, and it is the content of a column
     * header whose column has no header content of its own.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link TextContent}: shown as text.
     *                                  <p>{@link ContentValue#ofHtml(String)}: HTML, not escaped.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setResourceColumnHeaderContent(String)}: a text, as
     *                                  {@link TextContent}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: the text "Resources", shown while no resource columns are set.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code view}.<br>
     *                                  The library docs do not list it, but the used library version passes it.<br>
     *                                  It returns a text, {@code {html: '...'}} or {@code {domNodes: [...]}}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setResourceColumnHeaderContent("Name");
     * calendar.options().setResourceColumnHeaderContent(ContentValue.ofHtml("<b>Name</b>"));
     * calendar.options().setResourceColumnHeaderContent(
     *         JsCallback.of("info => 'Rooms'"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-column-header-render-hooks">resourceColumnHeaderContent</a>
     */
    @JsonConverter(ContentValueConverter.class)
    private ContentValue resourceColumnHeaderContent;

    /**
     * Sets a text as the content, see {@link #getResourceColumnHeaderContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setResourceColumnHeaderContent(String text) {
        this.resourceColumnHeaderContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content in any form, see {@link #getResourceColumnHeaderContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setResourceColumnHeaderContent(ContentValue value) {
        this.resourceColumnHeaderContent = value;
    }

    /**
     * A callback that runs right after the resource column header has been added to the DOM. It gets an {@code info}
     * object with {@code el} (the element) and {@code view}. The library docs list only {@code el}, but the
     * used library version also passes {@code view}.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-column-header-render-hooks">resourceColumnHeaderDidMount</a>
     */
    private JsCallback resourceColumnHeaderDidMount;

    /**
     * CSS classes for the inner wrapper of the resource column header in the timeline view. The resource column header
     * is the header above the resource data. With resource columns set (see
     * {@link FullCalendarSchedulerOptions#setResourceColumns(java.util.List)}), they apply to the inner wrapper of
     * every column header.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setResourceColumnHeaderInnerClass(String)}: class names separated by
     *                                  spaces, as {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: the stable class {@code fc-datagrid-cell-cushion}.<br>
     *                                  Classes set here are added to it.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code view}.<br>
     *                                  The library docs do not list it, but the used library version passes it.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setResourceColumnHeaderInnerClass("my-class");
     * calendar.options().setResourceColumnHeaderInnerClass(
     *         JsCallback.of("info => info.view.type.startsWith('resourceTimeline') ? 'my-header' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-column-header-render-hooks">
     *     resourceColumnHeaderInnerClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue resourceColumnHeaderInnerClass;

    /**
     * Sets fixed CSS classes, see {@link #getResourceColumnHeaderInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setResourceColumnHeaderInnerClass(String classNames) {
        this.resourceColumnHeaderInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getResourceColumnHeaderInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setResourceColumnHeaderInnerClass(ClassNameValue value) {
        this.resourceColumnHeaderInnerClass = value;
    }

    /**
     * A callback that runs right before the resource column header is removed from the DOM. It gets an {@code info}
     * object with {@code el} (the element) and {@code view}. The library docs list only {@code el}, but the
     * used library version also passes {@code view}.
     * <p>
     * When this option is changed for an attached calendar, the callback that is set at the time an element is removed
     * runs for it. This includes elements that were rendered before the change.
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-column-header-render-hooks">
     *     resourceColumnHeaderWillUnmount</a>
     */
    private JsCallback resourceColumnHeaderWillUnmount;

    /**
     * CSS classes for the handle the user drags to resize a resource column in the timeline view. The handle sits at
     * the end edge of every column header except the last one and is not rendered when printing.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setResourceColumnResizerClass(String)}: class names separated by spaces,
     *                                  as {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code view}.<br>
     *                                  The library docs do not list it, but the used library version passes it.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setResourceColumnResizerClass("my-class");
     * calendar.options().setResourceColumnResizerClass(
     *         JsCallback.of("info => info.view.type.startsWith('resourceTimeline') ? 'my-resizer' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-column-header-render-hooks">resourceColumnResizerClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue resourceColumnResizerClass;

    /**
     * Sets fixed CSS classes, see {@link #getResourceColumnResizerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setResourceColumnResizerClass(String classNames) {
        this.resourceColumnResizerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getResourceColumnResizerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setResourceColumnResizerClass(ClassNameValue value) {
        this.resourceColumnResizerClass = value;
    }

    /**
     * CSS classes for the column header cells that show the resource names in the vertical resource views
     * ({@code resourceTimeGrid} and {@code resourceDayGrid}).
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setResourceDayHeaderClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: the stable class {@code fc-resource}.<br>
     *                                  Classes set here are added to it.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code resource}, {@code text}, {@code level}, {@code view},
     *                                  {@code isDisabled}, {@code isNarrow} and {@code isMajor}.<br>
     *                                  When the column is scoped to a date, it also has {@code date}, {@code dow},
     *                                  {@code isOther}, {@code isToday}, {@code isPast} and {@code isFuture}.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setResourceDayHeaderClass("my-resource-header");
     * calendar.options().setResourceDayHeaderClass(JsCallback.of("info => info.isToday ? 'my-today' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-day-header-render-hooks">resourceDayHeaderClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue resourceDayHeaderClass;

    /**
     * Sets fixed CSS classes, see {@link #getResourceDayHeaderClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setResourceDayHeaderClass(String classNames) {
        this.resourceDayHeaderClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getResourceDayHeaderClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setResourceDayHeaderClass(ClassNameValue value) {
        this.resourceDayHeaderClass = value;
    }

    /**
     * Custom content for the column header cells that show the resource names in the vertical resource views
     * ({@code resourceTimeGrid} and {@code resourceDayGrid}).
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link TextContent}: shown as text.
     *                                  <p>{@link ContentValue#ofHtml(String)}: HTML, not escaped.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setResourceDayHeaderContent(String)}: a text, as
     *                                  {@link TextContent}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: the title of the resource as text, or its id when it has no title.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code resource}, {@code text}, {@code level}, {@code view},
     *                                  {@code isDisabled}, {@code isNarrow} and {@code isMajor}.<br>
     *                                  When the column is scoped to a date, it also has {@code date}, {@code dow},
     *                                  {@code isOther}, {@code isToday}, {@code isPast} and {@code isFuture}.<br>
     *                                  It returns a text, {@code {html: '...'}} or {@code {domNodes: [...]}}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setResourceDayHeaderContent("Resource");
     * calendar.options().setResourceDayHeaderContent(ContentValue.ofHtml("<b>Resource</b>"));
     * calendar.options().setResourceDayHeaderContent(JsCallback.of("info => info.text.toUpperCase()"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-day-header-render-hooks">resourceDayHeaderContent</a>
     */
    @JsonConverter(ContentValueConverter.class)
    private ContentValue resourceDayHeaderContent;

    /**
     * Sets a text as the content, see {@link #getResourceDayHeaderContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setResourceDayHeaderContent(String text) {
        this.resourceDayHeaderContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content in any form, see {@link #getResourceDayHeaderContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setResourceDayHeaderContent(ContentValue value) {
        this.resourceDayHeaderContent = value;
    }

    /**
     * A callback that runs right after a resource day header cell has been added to the DOM. It gets an {@code info}
     * object with {@code el} (the element), {@code resource}, {@code text} and more, see the library docs.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-day-header-render-hooks">resourceDayHeaderDidMount</a>
     */
    private JsCallback resourceDayHeaderDidMount;

    /**
     * CSS classes for the inner wrapper of the resource day header cells in the vertical resource views
     * ({@code resourceTimeGrid} and {@code resourceDayGrid}). The inner wrapper is useful for adjusting padding.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setResourceDayHeaderInnerClass(String)}: class names separated by
     *                                  spaces, as {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code resource}, {@code text}, {@code level}, {@code view},
     *                                  {@code isDisabled}, {@code isNarrow} and {@code isMajor}.<br>
     *                                  When the column is scoped to a date, it also has {@code date}, {@code dow},
     *                                  {@code isOther}, {@code isToday}, {@code isPast} and {@code isFuture}.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setResourceDayHeaderInnerClass("my-header-inner");
     * calendar.options().setResourceDayHeaderInnerClass(JsCallback.of("info => info.isToday ? 'my-today' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-day-header-render-hooks">resourceDayHeaderInnerClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue resourceDayHeaderInnerClass;

    /**
     * Sets fixed CSS classes, see {@link #getResourceDayHeaderInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setResourceDayHeaderInnerClass(String classNames) {
        this.resourceDayHeaderInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getResourceDayHeaderInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setResourceDayHeaderInnerClass(ClassNameValue value) {
        this.resourceDayHeaderInnerClass = value;
    }

    /**
     * A callback that runs right before a resource day header cell is removed from the DOM. It gets an {@code info}
     * object with {@code el} (the element), {@code resource}, {@code text} and more, see the library docs.
     * <p>
     * When this option is changed for an attached calendar, the callback that is set at the time an element is removed
     * runs for it. This includes elements that were rendered before the change.
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-day-header-render-hooks">resourceDayHeaderWillUnmount</a>
     */
    private JsCallback resourceDayHeaderWillUnmount;

    /**
     * CSS classes for the expand/collapse toggle in front of expandable resource rows in the hierarchical timeline
     * view.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setResourceExpanderClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code isExpanded}.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setResourceExpanderClass("my-expander");
     * calendar.options().setResourceExpanderClass(JsCallback.of("info => info.isExpanded ? 'my-open' : 'my-closed'"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-expander-render-hooks">resourceExpanderClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue resourceExpanderClass;

    /**
     * Sets fixed CSS classes, see {@link #getResourceExpanderClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setResourceExpanderClass(String classNames) {
        this.resourceExpanderClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getResourceExpanderClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setResourceExpanderClass(ClassNameValue value) {
        this.resourceExpanderClass = value;
    }

    /**
     * Custom content for the expand/collapse toggle in front of expandable resource rows in the hierarchical timeline
     * view. The content is inserted inside the toggle, typically as an icon, and is hidden from assistive technology.
     * <p>
     * Content set here takes the place of the content of the active theme.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link TextContent}: shown as text.
     *                                  <p>{@link ContentValue#ofHtml(String)}: HTML, not escaped.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setResourceExpanderContent(String)}: a text, as
     *                                  {@link TextContent}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no content, but the active theme sets an icon for it.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code isExpanded}.<br>
     *                                  It returns a text, {@code {html: '...'}} or {@code {domNodes: [...]}}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setResourceExpanderContent("+");
     * calendar.options().setResourceExpanderContent(ContentValue.ofHtml("<b>+</b>"));
     * calendar.options().setResourceExpanderContent(JsCallback.of("info => info.isExpanded ? '-' : '+'"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-expander-render-hooks">resourceExpanderContent</a>
     */
    @JsonConverter(ContentValueConverter.class)
    private ContentValue resourceExpanderContent;

    /**
     * Sets a text as the content, see {@link #getResourceExpanderContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setResourceExpanderContent(String text) {
        this.resourceExpanderContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content in any form, see {@link #getResourceExpanderContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setResourceExpanderContent(ContentValue value) {
        this.resourceExpanderContent = value;
    }

    /**
     * CSS classes for the resource group header, the cell that shows the group name at the top of each row group in the
     * resource timeline view.
     * <p>
     * The library ignores this option while a resource column is marked as a group column ({@code resourceColumns}).
     * The hooks of that column apply then. The library docs do not say this.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setResourceGroupHeaderClass(String)}: class names separated by spaces,
     *                                  as {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: the stable classes {@code fc-datagrid-cell} and
     *                                  {@code fc-resource-group}.<br>
     *                                  Classes set here are added to them.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code fieldValue} and {@code view}.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setResourceGroupHeaderClass("my-group-header");
     * calendar.options().setResourceGroupHeaderClass(JsCallback.of("i => i.fieldValue == 'A' ? 'my-group' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-group-header-render-hooks">resourceGroupHeaderClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue resourceGroupHeaderClass;

    /**
     * Sets fixed CSS classes, see {@link #getResourceGroupHeaderClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setResourceGroupHeaderClass(String classNames) {
        this.resourceGroupHeaderClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getResourceGroupHeaderClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setResourceGroupHeaderClass(ClassNameValue value) {
        this.resourceGroupHeaderClass = value;
    }

    /**
     * Custom content for the resource group header, the cell that shows the group name at the top of each row group in
     * the resource timeline view. The groups come from {@code resourceGroupField}.
     * <p>
     * The library ignores this option while a resource column is marked as a group column ({@code resourceColumns}).
     * The hooks of that column apply then. The library docs do not say this.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link TextContent}: shown as text.
     *                                  <p>{@link ContentValue#ofHtml(String)}: HTML, not escaped.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setResourceGroupHeaderContent(String)}: a text, as
     *                                  {@link TextContent}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: the value of the group field, or a non-breaking space when it is
     *                                  empty.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code fieldValue} and {@code view}.<br>
     *                                  It returns a text, {@code {html: '...'}} or {@code {domNodes: [...]}}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setResourceGroupHeaderContent("Group");
     * calendar.options().setResourceGroupHeaderContent(ContentValue.ofHtml("<b>Group</b>"));
     * calendar.options().setResourceGroupHeaderContent(JsCallback.of("info => 'Group ' + info.fieldValue"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-group-header-render-hooks">resourceGroupHeaderContent</a>
     */
    @JsonConverter(ContentValueConverter.class)
    private ContentValue resourceGroupHeaderContent;

    /**
     * Sets a text as the content, see {@link #getResourceGroupHeaderContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setResourceGroupHeaderContent(String text) {
        this.resourceGroupHeaderContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content in any form, see {@link #getResourceGroupHeaderContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setResourceGroupHeaderContent(ContentValue value) {
        this.resourceGroupHeaderContent = value;
    }

    /**
     * A callback that runs right after a resource group header has been added to the DOM. It gets an {@code info}
     * object with {@code fieldValue}, {@code view} and {@code el}, the element.
     * <p>
     * The library ignores this option while a resource column is marked as a group column ({@code resourceColumns}).
     * The hooks of that column apply then. The library docs do not say this.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-group-header-render-hooks">resourceGroupHeaderDidMount</a>
     */
    private JsCallback resourceGroupHeaderDidMount;

    /**
     * CSS classes for the inner wrapper of a resource group header cell in the resource timeline view. A group's header
     * is where its name is displayed, at the top of each row group.
     * <p>
     * The library ignores this option while a resource column is marked as a group column ({@code resourceColumns}).
     * The hooks of that column apply then. The library docs do not say this.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setResourceGroupHeaderInnerClass(String)}: class names separated by
     *                                  spaces, as {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: the stable class {@code fc-datagrid-cell-cushion}.<br>
     *                                  Classes set here are added to it.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code fieldValue} and {@code view}.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setResourceGroupHeaderInnerClass("my-group-inner");
     * calendar.options().setResourceGroupHeaderInnerClass(JsCallback.of("i => i.fieldValue == 'A' ? 'my-group' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-group-header-render-hooks">resourceGroupHeaderInnerClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue resourceGroupHeaderInnerClass;

    /**
     * Sets fixed CSS classes, see {@link #getResourceGroupHeaderInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setResourceGroupHeaderInnerClass(String classNames) {
        this.resourceGroupHeaderInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getResourceGroupHeaderInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setResourceGroupHeaderInnerClass(ClassNameValue value) {
        this.resourceGroupHeaderInnerClass = value;
    }

    /**
     * A callback that runs right before a resource group header is removed from the DOM. It gets an {@code info} object
     * with {@code fieldValue}, {@code view} and {@code el}, the element.
     * <p>
     * The library ignores this option while a resource column is marked as a group column ({@code resourceColumns}).
     * The hooks of that column apply then. The library docs do not say this.
     * <p>
     * When this option is changed for an attached calendar, the callback that is set at the time an element is removed
     * runs for it. This includes elements that were rendered before the change.
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-group-header-render-hooks">resourceGroupHeaderWillUnmount</a>
     */
    private JsCallback resourceGroupHeaderWillUnmount;

    /**
     * CSS classes for the resource group lane, the horizontal area of a group along the time slots in the resource
     * timeline view.
     * <p>
     * While a resource column is marked as a group column ({@code resourceColumns}), the library renders no group
     * lanes, so this option has no effect. The library docs do not say this.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setResourceGroupLaneClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: the stable classes {@code fc-timeline-lane} and
     *                                  {@code fc-resource-group}.<br>
     *                                  Classes set here are added to them.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code fieldValue} and {@code view}.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setResourceGroupLaneClass("my-group-lane");
     * calendar.options().setResourceGroupLaneClass(JsCallback.of("i => i.fieldValue == 'A' ? 'my-group' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-group-lane-render-hooks">resourceGroupLaneClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue resourceGroupLaneClass;

    /**
     * Sets fixed CSS classes, see {@link #getResourceGroupLaneClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setResourceGroupLaneClass(String classNames) {
        this.resourceGroupLaneClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getResourceGroupLaneClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setResourceGroupLaneClass(ClassNameValue value) {
        this.resourceGroupLaneClass = value;
    }

    /**
     * Custom content for the resource group lane, the horizontal area of a group along the time slots in the resource
     * timeline view.
     * <p>
     * While a resource column is marked as a group column ({@code resourceColumns}), the library renders no group
     * lanes, so this option has no effect. The library docs do not say this.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link TextContent}: shown as text.
     *                                  <p>{@link ContentValue#ofHtml(String)}: HTML, not escaped.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setResourceGroupLaneContent(String)}: a text, as
     *                                  {@link TextContent}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no content.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code fieldValue} and {@code view}.<br>
     *                                  It returns a text, {@code {html: '...'}} or {@code {domNodes: [...]}}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setResourceGroupLaneContent("Lane");
     * calendar.options().setResourceGroupLaneContent(ContentValue.ofHtml("<b>Lane</b>"));
     * calendar.options().setResourceGroupLaneContent(JsCallback.of("info => 'Group ' + info.fieldValue"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-group-lane-render-hooks">resourceGroupLaneContent</a>
     */
    @JsonConverter(ContentValueConverter.class)
    private ContentValue resourceGroupLaneContent;

    /**
     * Sets a text as the content, see {@link #getResourceGroupLaneContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setResourceGroupLaneContent(String text) {
        this.resourceGroupLaneContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content in any form, see {@link #getResourceGroupLaneContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setResourceGroupLaneContent(ContentValue value) {
        this.resourceGroupLaneContent = value;
    }

    /**
     * A callback that runs right after a resource group lane has been added to the DOM. It gets an {@code info} object
     * with {@code fieldValue}, {@code view} and {@code el}, the element.
     * <p>
     * While a resource column is marked as a group column ({@code resourceColumns}), the library renders no group
     * lanes, so this option has no effect. The library docs do not say this.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-group-lane-render-hooks">resourceGroupLaneDidMount</a>
     */
    private JsCallback resourceGroupLaneDidMount;

    /**
     * CSS classes for the inner wrapper of a resource group lane cell in the resource timeline view. A group's lane is
     * the horizontal area running along the time slots.
     * <p>
     * While a resource column is marked as a group column ({@code resourceColumns}), the library renders no group
     * lanes, so this option has no effect. The library docs do not say this.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setResourceGroupLaneInnerClass(String)}: class names separated by
     *                                  spaces, as {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code fieldValue} and {@code view}.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setResourceGroupLaneInnerClass("my-lane-inner");
     * calendar.options().setResourceGroupLaneInnerClass(JsCallback.of("i => i.fieldValue == 'A' ? 'my-group' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-group-lane-render-hooks">resourceGroupLaneInnerClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue resourceGroupLaneInnerClass;

    /**
     * Sets fixed CSS classes, see {@link #getResourceGroupLaneInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setResourceGroupLaneInnerClass(String classNames) {
        this.resourceGroupLaneInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getResourceGroupLaneInnerClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setResourceGroupLaneInnerClass(ClassNameValue value) {
        this.resourceGroupLaneInnerClass = value;
    }

    /**
     * A callback that runs right before a resource group lane is removed from the DOM. It gets an {@code info} object
     * with {@code fieldValue}, {@code view} and {@code el}, the element.
     * <p>
     * While a resource column is marked as a group column ({@code resourceColumns}), the library renders no group
     * lanes, so this option has no effect. The library docs do not say this.
     * <p>
     * When this option is changed for an attached calendar, the callback that is set at the time an element is removed
     * runs for it. This includes elements that were rendered before the change.
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-group-lane-render-hooks">resourceGroupLaneWillUnmount</a>
     */
    private JsCallback resourceGroupLaneWillUnmount;

    /**
     * CSS classes for the header rows of the resource area in the timeline view.
     * <p>
     * It takes class names only, because the library accepts no callback for this option.
     * <p>
     * The library default is no classes. A theme can set classes of its own, and the classes set here are added to
     * them.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-header-row-render-hooks">resourceHeaderRowClass</a>
     */
    private String resourceHeaderRowClass;

    /**
     * CSS classes for the indentation space in front of each resource row in the hierarchical resource timeline view.
     * <p>
     * It takes class names only, because the library accepts no callback for this option.
     * <p>
     * The library default is no classes. A theme can set classes of its own, and the classes set here are added to
     * them.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-expander-render-hooks">resourceIndentClass</a>
     */
    private String resourceIndentClass;

    /**
     * CSS classes for the element below the entries of a resource lane.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setResourceLaneBottomClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code resource} and {@code options}, which holds only
     *                                  {@code eventOverlap}.<br>
     *                                  The library docs list {@code resource} only.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setResourceLaneBottomClass("my-lane-bottom");
     * calendar.options().setResourceLaneBottomClass(JsCallback.of("info => info.resource.id == 'a' ? 'my-lane' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-lane-render-hooks">resourceLaneBottomClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue resourceLaneBottomClass;

    /**
     * Sets fixed CSS classes, see {@link #getResourceLaneBottomClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setResourceLaneBottomClass(String classNames) {
        this.resourceLaneBottomClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getResourceLaneBottomClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setResourceLaneBottomClass(ClassNameValue value) {
        this.resourceLaneBottomClass = value;
    }

    /**
     * Content inserted at the bottom of a resource lane, below its entries.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link TextContent}: shown as text.
     *                                  <p>{@link ContentValue#ofHtml(String)}: HTML, not escaped.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setResourceLaneBottomContent(String)}: a text, as
     *                                  {@link TextContent}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no content.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code resource} and {@code options}, which holds only
     *                                  {@code eventOverlap}.<br>
     *                                  The library docs list {@code resource} only.<br>
     *                                  It returns a text, {@code {html: '...'}} or {@code {domNodes: [...]}}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setResourceLaneBottomContent("Bottom");
     * calendar.options().setResourceLaneBottomContent(ContentValue.ofHtml("<b>Bottom</b>"));
     * calendar.options().setResourceLaneBottomContent(JsCallback.of("info => info.resource.title"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-lane-render-hooks">resourceLaneBottomContent</a>
     */
    @JsonConverter(ContentValueConverter.class)
    private ContentValue resourceLaneBottomContent;

    /**
     * Sets a text as the content, see {@link #getResourceLaneBottomContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setResourceLaneBottomContent(String text) {
        this.resourceLaneBottomContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content in any form, see {@link #getResourceLaneBottomContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setResourceLaneBottomContent(ContentValue value) {
        this.resourceLaneBottomContent = value;
    }

    /**
     * CSS classes for a resource lane. A resource lane runs horizontally across the timeline slots of a resource in the
     * resource timeline view.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setResourceLaneClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.
     *                                  <p>add-on: the stable classes {@code fc-timeline-lane} and
     *                                  {@code fc-resource}.<br>
     *                                  Classes set here are added to them.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code resource} and {@code options}, which holds only
     *                                  {@code eventOverlap}.<br>
     *                                  The library docs list {@code resource} only.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setResourceLaneClass("my-lane");
     * calendar.options().setResourceLaneClass(JsCallback.of("info => info.resource.id == 'a' ? 'my-lane' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-lane-render-hooks">resourceLaneClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue resourceLaneClass;

    /**
     * Sets fixed CSS classes, see {@link #getResourceLaneClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setResourceLaneClass(String classNames) {
        this.resourceLaneClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getResourceLaneClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setResourceLaneClass(ClassNameValue value) {
        this.resourceLaneClass = value;
    }

    /**
     * A callback that runs right after a resource lane element has been added to the DOM. It gets an {@code info}
     * object with {@code resource}, {@code options} and {@code el}, the element. {@code options} holds only
     * {@code eventOverlap}. The library docs list {@code resource} and {@code el} only.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-lane-render-hooks">resourceLaneDidMount</a>
     */
    private JsCallback resourceLaneDidMount;

    /**
     * CSS classes for the element above the entries of a resource lane.
     * <p>
     * A theme can set classes of its own, and the classes set here are added to them.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link ClassName}: class names.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setResourceLaneTopClass(String)}: class names separated by spaces, as
     *                                  {@link ClassName}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no classes.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code resource} and {@code options}, which holds only
     *                                  {@code eventOverlap}.<br>
     *                                  The library docs list {@code resource} only.<br>
     *                                  It returns the class names as a string.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setResourceLaneTopClass("my-lane-top");
     * calendar.options().setResourceLaneTopClass(JsCallback.of("info => info.resource.id == 'a' ? 'my-lane' : ''"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-lane-render-hooks">resourceLaneTopClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue resourceLaneTopClass;

    /**
     * Sets fixed CSS classes, see {@link #getResourceLaneTopClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setResourceLaneTopClass(String classNames) {
        this.resourceLaneTopClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getResourceLaneTopClass()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setResourceLaneTopClass(ClassNameValue value) {
        this.resourceLaneTopClass = value;
    }

    /**
     * Content inserted at the top of a resource lane, above its entries.
     * <p>
     * The getter returns null while the option is not set. Then the library default applies.
     * <dl>
     *   <dt><b>Forms</b></dt>      <dd>{@link TextContent}: shown as text.
     *                                  <p>{@link ContentValue#ofHtml(String)}: HTML, not escaped.
     *                                  <p>{@link JsCallback}: a function, see JsCallback.</dd>
     *   <dt><b>Shortcuts</b></dt>  <dd>{@link #setResourceLaneTopContent(String)}: a text, as {@link TextContent}.</dd>
     *   <dt><b>Default</b></dt>    <dd>library: no content.</dd>
     *   <dt><b>JsCallback</b></dt> <dd>{@code function(info)}.<br>
     *                                  {@code info} has {@code resource} and {@code options}, which holds only
     *                                  {@code eventOverlap}.<br>
     *                                  The library docs list {@code resource} only.<br>
     *                                  It returns a text, {@code {html: '...'}} or {@code {domNodes: [...]}}.</dd>
     * </dl>
     * <pre>{@code
     * calendar.options().setResourceLaneTopContent("Top");
     * calendar.options().setResourceLaneTopContent(ContentValue.ofHtml("<b>Top</b>"));
     * calendar.options().setResourceLaneTopContent(JsCallback.of("info => info.resource.title"));
     * }</pre>
     *
     * @see <a href="https://fullcalendar.io/docs/resource-lane-render-hooks">resourceLaneTopContent</a>
     */
    @JsonConverter(ContentValueConverter.class)
    private ContentValue resourceLaneTopContent;

    /**
     * Sets a text as the content, see {@link #getResourceLaneTopContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setResourceLaneTopContent(String text) {
        this.resourceLaneTopContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content in any form, see {@link #getResourceLaneTopContent()}.
     * <!-- OPTION DESCRIPTION PLACEHOLDER - DO NOT REMOVE. build-tools/SourcesJavadoc.java inserts it here. -->
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setResourceLaneTopContent(ContentValue value) {
        this.resourceLaneTopContent = value;
    }

    /**
     * A callback that runs right before a resource lane element is removed from the DOM. It gets an {@code info} object
     * with {@code resource}, {@code options} and {@code el}, the element. {@code options} holds only
     * {@code eventOverlap}. The library docs list {@code resource} and {@code el} only.
     * <p>
     * When this option is changed for an attached calendar, the callback that is set at the time an element is removed
     * runs for it. This includes elements that were rendered before the change.
     * <p>
     * The library default is no callback.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-lane-render-hooks">resourceLaneWillUnmount</a>
     */
    private JsCallback resourceLaneWillUnmount;

    /**
     * CSS classes for the body rows of the resource area in the timeline view. Does not apply to the resource group
     * header rows within the body.
     * <p>
     * It takes class names only, because the library accepts no callback for this option.
     * <p>
     * The library default is no classes. A theme can set classes of its own, and the classes set here are added to
     * them.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-row-render-hooks">resourceRowClass</a>
     */
    private String resourceRowClass;

    /**
     * CSS classes for the element below the entries in the timeline view without resources.
     * <p>
     * It takes class names only, because the library accepts no callback for this option.
     * <p>
     * The library default is no classes. A theme can set classes of its own, and the classes set here are added to
     * them.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/timeline-render-hooks">timelineBottomClass</a>
     */
    private String timelineBottomClass;

    /**
     * CSS classes for the element above the entries in the timeline view without resources.
     * <p>
     * It takes class names only, because the library accepts no callback for this option.
     * <p>
     * The library default is no classes. A theme can set classes of its own, and the classes set here are added to
     * them.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/timeline-render-hooks">timelineTopClass</a>
     */
    private String timelineTopClass;
}
