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
import org.vaadin.stefan.fullcalendar.converters.HeaderAlignValueConverter;
import org.vaadin.stefan.fullcalendar.converters.OrderValueConverter;
import org.vaadin.stefan.fullcalendar.converters.WidthValueConverter;
import org.vaadin.stefan.fullcalendar.json.JsonConverter;
import org.vaadin.stefan.fullcalendar.json.JsonName;
import org.vaadin.stefan.fullcalendar.option.EntryPrintLayout;
import org.vaadin.stefan.fullcalendar.option.HeaderAlignValue;
import org.vaadin.stefan.fullcalendar.option.JsCallback;
import org.vaadin.stefan.fullcalendar.option.Order;
import org.vaadin.stefan.fullcalendar.option.OrderValue;
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
}
