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
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resourceGroupField">resourceGroupField</a>
     */
    private String resourceGroupField;

    /**
     * Whether child resources are expanded when the view loads. Set it to {@code false} to start with them collapsed.
     * Only the timeline view supports it.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resourcesInitiallyExpanded">resourcesInitiallyExpanded</a>
     */
    private Boolean resourcesInitiallyExpanded;

    /**
     * The order of the resource list. The library default is {@code "id,title"}.
     * <p>
     * It takes a list of resource property names, where a leading {@code -} sorts descending, a mix with compare
     * functions ({@link Order}), or a single {@link JsCallback} that compares two resources.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resourceOrder">resourceOrder</a>
     */
    @JsonConverter(OrderValueConverter.class)
    private OrderValue resourceOrder;

    /**
     * Sets the order from resource property names, see {@link #getResourceOrder()}.
     *
     * @param properties the names, such as {@code "title"} or {@code "-title"} for descending, or null to remove the
     *        option
     */
    public void setResourceOrder(String... properties) {
        this.resourceOrder = properties == null ? null : Order.of(properties);
    }

    /**
     * Sets the order from sort keys, which may mix properties and compare functions, see {@link #getResourceOrder()}.
     *
     * @param keys the sort keys, at least one, or null to remove the option
     */
    public void setResourceOrder(Order.Key... keys) {
        this.resourceOrder = keys == null ? null : Order.of(keys);
    }

    /**
     * Sets the order in any form, see {@link #getResourceOrder()}.
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
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/datesAboveResources">datesAboveResources</a>
     */
    private Boolean datesAboveResources;

    /**
     * Shows only resources that have entries assigned. The entries must be fetched before the resources can
     * render.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/filterResourcesWithEvents">filterResourcesWithEvents</a>
     */
    @JsonName("filterResourcesWithEvents")
    private Boolean filterResourcesWithEntries;

    /**
     * Allows the user to drag entries between resources. Without a value, it follows {@code editable}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/eventResourceEditable">eventResourceEditable</a>
     */
    @JsonName("eventResourceEditable")
    private Boolean entryResourceEditable;

    /**
     * Horizontal alignment of the resource name in the column header cells of the vertical resource views
     * ({@code resourceTimeGrid} and {@code resourceDayGrid}). In a left-to-right locale, start means left and end
     * means right. Without a value, the theme decides.
     * <p>
     * A callback gets an {@code info} object with {@code level}, the header row. It returns {@code "start"},
     * {@code "center"} or {@code "end"}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-day-header-render-hooks">resourceDayHeaderAlign</a>
     */
    @JsonConverter(HeaderAlignValueConverter.class)
    private HeaderAlignValue resourceDayHeaderAlign;

    /**
     * The width of the area that contains the list of resources, in pixels or as a percentage.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resourceColumnsWidth">resourceColumnsWidth</a>
     */
    @JsonConverter(WidthValueConverter.class)
    private WidthValue resourceColumnsWidth;

    /**
     * Sets the width in pixels, see {@link #getResourceColumnsWidth()}.
     *
     * @param pixels the width in pixels, not negative
     */
    public void setResourceColumnsWidth(int pixels) {
        this.resourceColumnsWidth = WidthValue.ofPixels(pixels);
    }

    /**
     * Sets the width in any form, see {@link #getResourceColumnsWidth()}.
     *
     * @param value the width in pixels or as a percentage, or null to remove the option
     */
    public void setResourceColumnsWidth(WidthValue value) {
        this.resourceColumnsWidth = value;
    }

    /**
     * The width of each slot of the time axis in the timeline view, in pixels. Without a value, the library computes
     * a reasonable one.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/slotMinWidth">slotMinWidth</a>
     */
    private Integer slotMinWidth;

    /**
     * The minimum width of an entry in the timeline view, in pixels.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
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
     * prints. {@link FullCalendarScheduler} loads that plugin. Without a value, the library stacks the entries in
     * Firefox and keeps the grid in other browsers.
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
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resourceRemove">resourceRemove</a>
     */
    private JsCallback resourceRemove;

    /**
     * CSS classes for the cells of the resource area in the timeline view. Each cell shows one column field, the
     * resource title or a column of {@code resourceColumns}.
     * <p>
     * The hook also runs for the cells of group rows, which have no resource.
     * <p>
     * A callback gets an {@code info} object with {@code resource} (absent in group rows), {@code field},
     * {@code fieldValue} and more, see the library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-cell-render-hooks">resourceCellClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue resourceCellClass;

    /**
     * Sets fixed CSS classes, see {@link #getResourceCellClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setResourceCellClass(String classNames) {
        this.resourceCellClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getResourceCellClass()}.
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
     * A callback gets an {@code info} object with {@code resource} (absent in group rows), {@code field},
     * {@code fieldValue} and more, see the library docs. It returns a text, or {@code {html: '...'}} or
     * {@code {domNodes: [...]}}.
     * <p>
     * The cell content of a resource column replaces it in that column. A callback of the column falls back to this
     * one by returning {@code true}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-cell-render-hooks">resourceCellContent</a>
     */
    @JsonConverter(ContentValueConverter.class)
    private ContentValue resourceCellContent;

    /**
     * Sets a text as the content, see {@link #getResourceCellContent()}.
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setResourceCellContent(String text) {
        this.resourceCellContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content in any form, see {@link #getResourceCellContent()}.
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setResourceCellContent(ContentValue value) {
        this.resourceCellContent = value;
    }

    /**
     * A callback that runs right after a cell of the resource area has been added to the DOM. It gets an {@code info}
     * object with {@code resource} (absent in group rows), {@code field}, {@code fieldValue} and more, see the library
     * docs.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
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
     * The hook also runs for the cells of group rows, which have no resource.
     * <p>
     * A callback gets an {@code info} object with {@code resource} (absent in group rows), {@code field},
     * {@code fieldValue} and more, see the library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-cell-render-hooks">resourceCellInnerClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue resourceCellInnerClass;

    /**
     * Sets fixed CSS classes, see {@link #getResourceCellInnerClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setResourceCellInnerClass(String classNames) {
        this.resourceCellInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getResourceCellInnerClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setResourceCellInnerClass(ClassNameValue value) {
        this.resourceCellInnerClass = value;
    }

    /**
     * A callback that runs right before a cell of the resource area is removed from the DOM. It gets an {@code info}
     * object with {@code resource} (absent in group rows), {@code field}, {@code fieldValue} and more, see the library
     * docs.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
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
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-column-divider-render-hooks">resourceColumnDividerClass</a>
     */
    private String resourceColumnDividerClass;

    /**
     * CSS classes for the resource column header, shown above the resource data in the timeline view.
     * <p>
     * A callback gets an {@code info} object with {@code view}, see the library docs. It returns the class names as a
     * string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-column-header-render-hooks">resourceColumnHeaderClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue resourceColumnHeaderClass;

    /**
     * Sets fixed CSS classes, see {@link #getResourceColumnHeaderClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setResourceColumnHeaderClass(String classNames) {
        this.resourceColumnHeaderClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getResourceColumnHeaderClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setResourceColumnHeaderClass(ClassNameValue value) {
        this.resourceColumnHeaderClass = value;
    }

    /**
     * Custom content for the resource column header, shown above the resource data in the timeline view. The library
     * shows the text "Resources" by default.
     * <p>
     * With {@code resourceColumns} set, it is shown as a header above all columns, and it is the content of a column
     * header whose column has no header content of its own.
     * <p>
     * A callback gets an {@code info} object with {@code view}, see the library docs. It returns a text, or
     * {@code {html: '...'}} or {@code {domNodes: [...]}}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-column-header-render-hooks">resourceColumnHeaderContent</a>
     */
    @JsonConverter(ContentValueConverter.class)
    private ContentValue resourceColumnHeaderContent;

    /**
     * Sets a text as the content, see {@link #getResourceColumnHeaderContent()}.
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setResourceColumnHeaderContent(String text) {
        this.resourceColumnHeaderContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content in any form, see {@link #getResourceColumnHeaderContent()}.
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setResourceColumnHeaderContent(ContentValue value) {
        this.resourceColumnHeaderContent = value;
    }

    /**
     * A callback that runs right after the resource column header has been added to the DOM. It gets an {@code info}
     * object with {@code view}, see the library docs.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-column-header-render-hooks">resourceColumnHeaderDidMount</a>
     */
    private JsCallback resourceColumnHeaderDidMount;

    /**
     * CSS classes for the inner wrapper of the resource column header in the timeline view. The resource column header
     * is the header above the resource data.
     * <p>
     * A callback gets an {@code info} object with {@code view}, see the library docs. It returns the class names as a
     * string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-column-header-render-hooks">
     *     resourceColumnHeaderInnerClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue resourceColumnHeaderInnerClass;

    /**
     * Sets fixed CSS classes, see {@link #getResourceColumnHeaderInnerClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setResourceColumnHeaderInnerClass(String classNames) {
        this.resourceColumnHeaderInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getResourceColumnHeaderInnerClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setResourceColumnHeaderInnerClass(ClassNameValue value) {
        this.resourceColumnHeaderInnerClass = value;
    }

    /**
     * A callback that runs right before the resource column header is removed from the DOM. It gets an {@code info}
     * object with {@code view}, see the library docs.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
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
     * A callback gets an {@code info} object with {@code view}, see the library docs. It returns the class names as a
     * string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-column-header-render-hooks">resourceColumnResizerClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue resourceColumnResizerClass;

    /**
     * Sets fixed CSS classes, see {@link #getResourceColumnResizerClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setResourceColumnResizerClass(String classNames) {
        this.resourceColumnResizerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getResourceColumnResizerClass()}.
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
     * A callback gets an {@code info} object with {@code resource}, {@code text}, {@code level} and more, see the
     * library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-day-header-render-hooks">resourceDayHeaderClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue resourceDayHeaderClass;

    /**
     * Sets fixed CSS classes, see {@link #getResourceDayHeaderClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setResourceDayHeaderClass(String classNames) {
        this.resourceDayHeaderClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getResourceDayHeaderClass()}.
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
     * A callback gets an {@code info} object with {@code resource}, {@code text}, {@code level} and more, see the
     * library docs. It returns a text, or {@code {html: '...'}} or {@code {domNodes: [...]}}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-day-header-render-hooks">resourceDayHeaderContent</a>
     */
    @JsonConverter(ContentValueConverter.class)
    private ContentValue resourceDayHeaderContent;

    /**
     * Sets a text as the content, see {@link #getResourceDayHeaderContent()}.
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setResourceDayHeaderContent(String text) {
        this.resourceDayHeaderContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content in any form, see {@link #getResourceDayHeaderContent()}.
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setResourceDayHeaderContent(ContentValue value) {
        this.resourceDayHeaderContent = value;
    }

    /**
     * A callback that runs right after a resource day header cell has been added to the DOM. It gets an {@code info}
     * object with {@code resource}, {@code text}, {@code level} and more, see the library docs.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
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
     * A callback gets an {@code info} object with {@code resource}, {@code text}, {@code level} and more, see the
     * library docs. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-day-header-render-hooks">resourceDayHeaderInnerClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue resourceDayHeaderInnerClass;

    /**
     * Sets fixed CSS classes, see {@link #getResourceDayHeaderInnerClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setResourceDayHeaderInnerClass(String classNames) {
        this.resourceDayHeaderInnerClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getResourceDayHeaderInnerClass()}.
     *
     * @param value the classes or callback, or null to remove the option
     */
    public void setResourceDayHeaderInnerClass(ClassNameValue value) {
        this.resourceDayHeaderInnerClass = value;
    }

    /**
     * A callback that runs right before a resource day header cell is removed from the DOM. It gets an {@code info}
     * object with {@code resource}, {@code text}, {@code level} and more, see the library docs.
     * <p>
     * When this option is changed for an attached calendar, the new setting only applies to newly rendered elements,
     * for example after navigating to another view or period. Existing elements are not updated.
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
     * A callback gets an {@code info} object with {@code isExpanded}. It returns the class names as a string.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-expander-render-hooks">resourceExpanderClass</a>
     */
    @JsonConverter(ClassNameValueConverter.class)
    private ClassNameValue resourceExpanderClass;

    /**
     * Sets fixed CSS classes, see {@link #getResourceExpanderClass()}.
     *
     * @param classNames the class names, separated by spaces, or null to remove the option
     */
    public void setResourceExpanderClass(String classNames) {
        this.resourceExpanderClass = classNames == null ? null : ClassName.of(classNames);
    }

    /**
     * Sets the CSS classes in any form, see {@link #getResourceExpanderClass()}.
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
     * A callback gets an {@code info} object with {@code isExpanded}. It returns a text, or {@code {html: '...'}} or
     * {@code {domNodes: [...]}}.
     * <p>
     * The getter returns null while the option is not set; then the library default applies.
     *
     * @see <a href="https://fullcalendar.io/docs/resource-expander-render-hooks">resourceExpanderContent</a>
     */
    @JsonConverter(ContentValueConverter.class)
    private ContentValue resourceExpanderContent;

    /**
     * Sets a text as the content, see {@link #getResourceExpanderContent()}.
     *
     * @param text the text, shown as text, or null to remove the option
     */
    public void setResourceExpanderContent(String text) {
        this.resourceExpanderContent = text == null ? null : TextContent.of(text);
    }

    /**
     * Sets the content in any form, see {@link #getResourceExpanderContent()}.
     *
     * @param value the content or callback, or null to remove the option
     */
    public void setResourceExpanderContent(ContentValue value) {
        this.resourceExpanderContent = value;
    }
}
