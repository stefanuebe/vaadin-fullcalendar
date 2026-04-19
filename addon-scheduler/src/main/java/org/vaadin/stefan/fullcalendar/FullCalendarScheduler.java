/*
 * Copyright 2020, Stefan Uebe
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

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.shared.Registration;
import org.vaadin.stefan.fullcalendar.converters.JsonItemPropertyConverter;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.fullcalendar.json.JsonConverter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Flow implementation for the FullCalendar.
 * <p>
 * Please visit <a href="https://fullcalendar.io/">https://fullcalendar.io/</a> for details about the client side
 * component, API, functionality, etc.
 */
@NpmPackage(value = "@fullcalendar/resource", version = FullCalendarScheduler.FC_SCHEDULER_CLIENT_VERSION)
@NpmPackage(value = "@fullcalendar/resource-timeline", version = FullCalendarScheduler.FC_SCHEDULER_CLIENT_VERSION)
@NpmPackage(value = "@fullcalendar/resource-timegrid", version = FullCalendarScheduler.FC_SCHEDULER_CLIENT_VERSION)
@NpmPackage(value = "@fullcalendar/resource-daygrid", version = FullCalendarScheduler.FC_SCHEDULER_CLIENT_VERSION)
@NpmPackage(value = "@fullcalendar/scrollgrid", version = FullCalendarScheduler.FC_SCHEDULER_CLIENT_VERSION)
@JsModule("./vaadin-full-calendar/full-calendar-scheduler.ts")
@CssImport("./vaadin-full-calendar/full-calendar-scheduler-styles.css")

@Tag("vaadin-full-calendar-scheduler")
public class FullCalendarScheduler extends FullCalendar implements Scheduler {

    /**
     * The scheduler base version used in this addon. Some additional libraries might have a different version number due to
     * a different release cycle or known issues.
     */
    public static final String FC_SCHEDULER_CLIENT_VERSION = "6.1.9";
    private final Map<String, Resource> resources = new HashMap<>();
    private final List<ComponentResourceAreaColumn<?>> activeComponentColumns = new ArrayList<>();
    private Element hiddenContainer;

    /**
     * Creates a new instance without any settings beside the default locale ({@link CalendarLocale#getDefaultLocale()}).
     */
    public FullCalendarScheduler() {
        super();
    }

    /**
     * Creates a new instance.
     * <br><br>
     * Expects the default limit of entries shown per day. This does not affect basic or
     * list views. This value has to be set here and cannot be modified afterwards due to
     * technical reasons of FC. If set afterwards the entry limit would overwrite settings
     * and would show the limit also for basic views where it makes no sense (might change in future).
     * Passing a negative number or 0 disabled the entry limit (same as passing no number at all).
     * <br><br>
     * Sets the locale to {@link CalendarLocale#getDefault()}
     *
     *
     * @param entryLimit max entries to shown per day
     * @deprecated use the {@link FullCalendarBuilder#withEntryLimit(int)} instead
     */
    @Deprecated
    public FullCalendarScheduler(int entryLimit) {
        super(entryLimit);
    }

    /**
     * Creates a new instance with custom initial options. This allows a full override of the default
     * initial options, that the calendar would normally receive. Theoretically you can set all options,
     * as long as they are not based on a client side variable (as for instance "plugins" or "locales").
     * Complex objects are possible, too, for instance for view-specific settings.
     *  Please refer to the official FC documentation regarding potential options.
     * <br><br>
     * Client side event handlers, that are technically also a part of the options are still applied to
     * the options object. However you may set your own event handlers with the correct name. In that case
     * they will be taken into account instead of the default ones.
     * <br><br>
     * Plugins (key "plugins") will always be set on the client side (and thus override any key passed with this
     * object), since they are needed for a functional calendar. This may change in future. Same for locales
     * (key "locales").
     * <br><br>
     * Please be aware, that incorrect options or event handler overriding can lead to unpredictable errors,
     * which will NOT be supported in any case.
     * <br><br>
     * Also, options set this way are not cached in the server side state. Calling any of the
     * {@code getOption(...)} methods will result in {@code null} (or the respective native default).
     *
     * @see <a href="https://fullcalendar.io/docs">FullCalendar documentation</a>
     *
     * @param initialOptions initial options
     * @throws NullPointerException when null is passed
     */
    public FullCalendarScheduler(ObjectNode initialOptions) {
        super(initialOptions);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent); // Step 1: restoreStateFromServer (registers beforeClientResponse)

        if (!attachEvent.isInitialAttach()) {
            // Step 2: re-append components to calendar element (registered BEFORE Step 3 for FIFO order).
            // Components are appended directly to the calendar element (not the hidden container)
            // because FC's Calendar(el) wipes all light DOM children during init. Vaadin re-sends
            // them via UIDL, and the TS-side ensureComponentContainer() + cellDidMount handle the rest.
            if (!activeComponentColumns.isEmpty()) {
                getElement().getNode().runWhenAttached(ui -> {
                    ui.beforeClientResponse(this, executionContext -> {
                        for (var col : activeComponentColumns) {
                            col.getComponents().values().forEach(comp ->
                                    getElement().appendChild(((com.vaadin.flow.component.Component) comp).getElement()));
                        }
                    });
                });
            }

            // Step 3: re-add resources to FC (existing logic)
            if (!resources.isEmpty()) {
                getElement().getNode().runWhenAttached(ui -> {
                    ui.beforeClientResponse(this, executionContext -> {
                        ArrayNode array = JsonFactory.createArray();
                        resources.values().forEach(resource -> {
                            // only add top-level resources; children are included via toJson() recursively
                            if (resource.getParent().isEmpty()) {
                                array.add(resource.toJson());
                            }
                        });
                        getElement().callJsFunction("addResources", array, false);
                    });
                });
            }
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (!activeComponentColumns.isEmpty()) {
            getElement().callJsFunction("returnAllComponentsToContainer");
        }
        super.onDetach(detachEvent);
    }

    @Deprecated
    @Override
    public void setSchedulerLicenseKey(String schedulerLicenseKey) {
        setOption(SchedulerOption.LICENSE_KEY, schedulerLicenseKey);
    }

    @Deprecated
    @Override
    public void setResourceAreaHeaderContent(String resourceAreaHeaderContent) {
        setOption(SchedulerOption.RESOURCE_AREA_HEADER_CONTENT, resourceAreaHeaderContent);
    }
    
    @Deprecated
    @Override
    public void setResourceAreaWidth(String resourceAreaWidth) {
        setOption(SchedulerOption.RESOURCE_AREA_WIDTH, resourceAreaWidth);
    }
    
    @Deprecated
    @Override
    public void setSlotMinWidth(String slotMinWidth) {
        setOption(SchedulerOption.SLOT_MIN_WIDTH, slotMinWidth);
    }
    
    @Deprecated
    @Override
    public void setResourcesInitiallyExpanded(boolean resourcesInitiallyExpanded) {
        setOption(SchedulerOption.RESOURCES_INITIALLY_EXPANDED, resourcesInitiallyExpanded);
    }
    
    @Deprecated
    @Override
    public void setFilterResourcesWithEvents(boolean filterResourcesWithEvents) {
        setOption(SchedulerOption.FILTER_RESOURCES_WITH_ENTRIES, filterResourcesWithEvents);
    }

    @Deprecated
    @Override
    public void setResourceOrder(String resourceOrder) {
        setOption(SchedulerOption.RESOURCE_ORDER, resourceOrder);
    }
    
    @Deprecated
    @Override
    public void setEntryResourceEditable(boolean eventResourceEditable) {
    	setOption(SchedulerOption.ENTRY_RESOURCES_EDITABLE, eventResourceEditable);
    }

    @Override
    public void addResources(Iterable<Resource> iterableResource) {
        addResources(iterableResource, true);
    }

    @Override
    public void addResources(Iterable<Resource> iterableResource, boolean scrollToLast) {
        Objects.requireNonNull(iterableResource);

        ArrayNode array = JsonFactory.createArray();
        iterableResource.forEach(resource -> {
            String id = resource.getId();
            if (!resources.containsKey(id)) {
                resources.put(id, resource);
                resource.attachScheduler(this);
                array.add(resource.toJson()); // this automatically sends sub resources to the client side

                // create components for active component columns
                for (var col : activeComponentColumns) {
                    col.createComponent(resource);
                }
            }

            // now also register child resources
            registerResourcesInternally(resource.getChildren());
        });
        getElement().callJsFunction("addResources", array, scrollToLast);
    }

    /**
     * Adds resources to the internal resources map. Does not update the client side. This method is mainly intended
     * to be used for child resources of registered resources, as the toJson method takes care for recursive child registration
     * on the client side, thus no separate call of toJson for children is needed.
     * @param resources resources
     */
    private void registerResourcesInternally(Collection<Resource> resources) {
        for (Resource resource : resources) {
            this.resources.put(resource.getId(), resource);
            resource.attachScheduler(this);

            for (var col : activeComponentColumns) {
                col.createComponent(resource);
            }

            registerResourcesInternally(resource.getChildren());
        }
    }

    @Override
    public void removeResources(Iterable<Resource> iterableResources) {
        Objects.requireNonNull(iterableResources);

        removeFromEntries(iterableResources);

        // create registry of removed items to send to client
        ArrayNode array = JsonFactory.createArray();
        iterableResources.forEach(resource -> {
            String id = resource.getId();
            if (this.resources.containsKey(id)) {
                // recursively remove children from resources map and destroy their components
                unregisterResourcesInternally(resource.getChildren());

                // destroy component for this resource
                for (var col : activeComponentColumns) {
                    col.destroyComponent(resource);
                }

                this.resources.remove(id);
                resource.detachScheduler();
                array.add(resource.toJson());
            }
        });

        getElement().callJsFunction("removeResources", array);
    }

    /**
     * Recursively removes child resources from the internal map and destroys their components.
     */
    private void unregisterResourcesInternally(Set<Resource> children) {
        for (Resource child : children) {
            unregisterResourcesInternally(child.getChildren());
            for (var col : activeComponentColumns) {
                col.destroyComponent(child);
            }
            this.resources.remove(child.getId());
            child.detachScheduler();
        }
    }

    /**
     * Removes the given resources from the known entries of this calendar.
     * @param iterableResources resources
     */
    private void removeFromEntries(Iterable<Resource> iterableResources) {
        List<Resource> resources = StreamSupport.stream(iterableResources.spliterator(), false).collect(Collectors.toList());
        // TODO integrate in memory resource provider
        EntryProvider<Entry> entryProvider = getEntryProvider();
        if (entryProvider.isInMemory()) {
            entryProvider.asInMemory()
                    .getEntries()
                    .stream()
                    .filter(e -> e instanceof ResourceEntry)
                    .forEach(e -> ((ResourceEntry) e).removeResources(resources));
        }
    }

    @Override
    public Optional<Resource> getResourceById(String id) {
        Objects.requireNonNull(id);
        return Optional.ofNullable(resources.get(id));
    }

    @Override
    public Set<Resource> getResources() {
        return new LinkedHashSet<>(resources.values());
    }

    @Override
    public void removeAllResources() {
        removeFromEntries(resources.values());

        for (var col : activeComponentColumns) {
            col.destroyAllComponents();
        }

        resources.values().forEach(Resource::detachScheduler);
    	resources.clear();
        getElement().callJsFunction("removeAllResources");
    }

    @Override
    @Deprecated
    public void setResourceLabelClassNamesCallback(String s) {
        setOption(SchedulerOption.RESOURCE_LABEL_CLASS_NAMES, JsCallback.of(s));
    }

    @Override
    @Deprecated
    public void setResourceLabelContentCallback(String s) {
        setOption(SchedulerOption.RESOURCE_LABEL_CONTENT, JsCallback.of(s));
    }

    @Override
    @Deprecated
    public void setResourceLabelDidMountCallback(String s) {
        setOption(SchedulerOption.RESOURCE_LABEL_DID_MOUNT, JsCallback.of(s));
    }

    @Override
    @Deprecated
    public void setResourceLablelWillUnmountCallback(String s) {
        setOption(SchedulerOption.RESOURCE_LABEL_WILL_UNMOUNT, JsCallback.of(s));
    }

    @Override
    @Deprecated
    public void setResourceLaneClassNamesCallback(String s) {
        setOption(SchedulerOption.RESOURCE_LANE_CLASS_NAMES, JsCallback.of(s));
    }

    @Override
    @Deprecated
    public void setResourceLaneContentCallback(String s) {
        setOption(SchedulerOption.RESOURCE_LANE_CONTENT, JsCallback.of(s));
    }

    @Override
    @Deprecated
    public void setResourceLaneDidMountCallback(String s) {
        setOption(SchedulerOption.RESOURCE_LANE_DID_MOUNT, JsCallback.of(s));
    }

    @Override
    @Deprecated
    public void setResourceLaneWillUnmountCallback(String s) {
        setOption(SchedulerOption.RESOURCE_LANE_WILL_UNMOUNT, JsCallback.of(s));
    }

    @Override
    public void setResourceAreaColumns(List<ResourceAreaColumn> columns) {
        Objects.requireNonNull(columns);

        // validate no duplicate field keys
        Set<String> fieldKeys = new HashSet<>();
        for (ResourceAreaColumn col : columns) {
            if (!fieldKeys.add(col.getField())) {
                throw new IllegalArgumentException("Duplicate column field key: '" + col.getField() + "'");
            }
        }

        // unbind + destroy old component columns
        for (var col : activeComponentColumns) {
            col.destroyAllComponents();
            col.unbind();
        }
        activeComponentColumns.clear();

        // bind new component columns
        for (ResourceAreaColumn col : columns) {
            if (col instanceof ComponentResourceAreaColumn<?> compCol) {
                compCol.bind(this);
                activeComponentColumns.add(compCol);
            }
        }

        // ensure hidden container exists if needed
        if (!activeComponentColumns.isEmpty()) {
            ensureHiddenContainer();

            // create components for all existing resources
            for (Resource resource : resources.values()) {
                for (var col : activeComponentColumns) {
                    col.createComponent(resource);
                }
            }
        }

        // send to client
        if (columns.isEmpty()) {
            setOption(SchedulerOption.RESOURCE_AREA_COLUMNS, null, null);
        } else {
            ArrayNode array = JsonFactory.createArray();
            columns.forEach(col -> array.add(col.toJson()));
            setOption(SchedulerOption.RESOURCE_AREA_COLUMNS, array, columns);
        }
    }

    private void ensureHiddenContainer() {
        if (hiddenContainer == null) {
            hiddenContainer = new Element("div");
            hiddenContainer.setAttribute("data-fc-component-container", "");
            hiddenContainer.getStyle().set("display", "none");
            getElement().appendChild(hiddenContainer);
        }
    }

    /**
     * Returns the hidden container element for component teleportation.
     * Package-private — used by {@link ComponentResourceAreaColumn}.
     */
    Element getHiddenContainer() {
        ensureHiddenContainer();
        return hiddenContainer;
    }


    @Override
    public void updateResource(Resource resource) {
        Objects.requireNonNull(resource);
        getElement().callJsFunction("updateResource", resource.toJson().toString());
    }


    @Override
    public void setGroupEntriesBy(GroupEntriesBy groupEntriesBy) {
        switch (groupEntriesBy) {
            case NONE -> {
                setOption(SchedulerOption.GROUP_BY_RESOURCE, false);
                setOption(SchedulerOption.GROUP_BY_DATE_AND_RESOURCE, false);
            }
            case RESOURCE_DATE -> {
                setOption(SchedulerOption.GROUP_BY_DATE_AND_RESOURCE, false);
                setOption(SchedulerOption.GROUP_BY_RESOURCE, true);
            }
            case DATE_RESOURCE -> {
                setOption(SchedulerOption.GROUP_BY_RESOURCE, false);
                setOption(SchedulerOption.GROUP_BY_DATE_AND_RESOURCE, true);
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Registration addTimeslotsSelectedListener(ComponentEventListener<? extends TimeslotsSelectedEvent> listener) {
        return addTimeslotsSelectedSchedulerListener((ComponentEventListener) listener);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Registration addTimeslotsSelectedSchedulerListener(ComponentEventListener<? extends TimeslotsSelectedSchedulerEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(TimeslotsSelectedSchedulerEvent.class, (ComponentEventListener) listener);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Registration addTimeslotClickedListener(ComponentEventListener<? extends TimeslotClickedEvent> listener) {
        return addTimeslotClickedSchedulerListener((ComponentEventListener) listener);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Registration addTimeslotClickedSchedulerListener(ComponentEventListener<? extends TimeslotClickedSchedulerEvent> listener) {
        Objects.requireNonNull(listener);
        return addListener(TimeslotClickedSchedulerEvent.class, (ComponentEventListener) listener);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Registration addEntryDroppedSchedulerListener(ComponentEventListener<? extends EntryDroppedSchedulerEvent> listener) {
        Objects.requireNonNull(listener);
        return addAutoRevertAwareListener(EntryDroppedSchedulerEvent.class, (ComponentEventListener) listener);
    }

    /**
     * Sets a option for this instance. Passing a null value removes the option.
     * <br><br>
     * Please be aware that this method does not check the passed value. Use the typed
     * {@link SchedulerOption} constants for type safety.
     *
     * @param option option
     * @param value  value
     * @throws NullPointerException when null is passed
     */
    public void setOption(SchedulerOption option, Object value) {
        setOption(option.getOptionKey(), value, null, option.getConverters());
    }

    /**
     * Sets a option for this instance. Passing a null value removes the option. The third parameter
     * might be used to explicitly store a "more complex" variant of the option's value to be returned
     * by {@link #getOption(SchedulerOption)}. It is always stored when not equal to the value except for null.
     * If it is equal to the value or null it will not be stored (old version will be removed from internal cache).
     * <pre>
     * Please be aware that this method does not check the passed value. Use the typed
     * {@link SchedulerOption} constants for type safety.
     *
     * @param option             option
     * @param value              value
     * @param valueForServerSide value to be stored on server side
     * @throws NullPointerException when null is passed
     */
    public void setOption(SchedulerOption option, Object value, Object valueForServerSide) {
        setOption(option.getOptionKey(), value, valueForServerSide, option.getConverters());
    }

    /**
     * Returns an optional option value or empty, that has been set for that key via one of the setOptions methods.
     * If a server side version of the value has been set
     * via {@link #setOption(SchedulerOption, Serializable, Object)}, that will be returned instead.
     * <br><br>
     * If there is a explicit getter method, it is recommended to use these instead (e.g. {@link #getLocale()}).
     *
     * @param option option
     * @param <T>    type of value
     * @return optional value or empty
     * @throws NullPointerException when null is passed
     */
    public <T> Optional<T> getOption(SchedulerOption option) {
        return getOption(option, false);
    }

    /**
     * Returns an optional option value or empty, that has been set for that key via one of the setOptions methods.
     * If the second parameter is false and a server side version of the
     * value has been set via {@link #setOption(SchedulerOption, Serializable, Object)}, that will be returned instead.
     * <br><br>
     * If there is a explicit getter method, it is recommended to use these instead (e.g. {@link #getLocale()}).
     *
     * @param option               option
     * @param forceClientSideValue explicitly return the value that has been sent to client
     * @param <T>                  type of value
     * @return optional value or empty
     * @throws NullPointerException when null is passed
     */
    public <T> Optional<T> getOption(SchedulerOption option, boolean forceClientSideValue) {
        return getOption(option.getOptionKey(), forceClientSideValue);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends CalendarView> Optional<T> lookupViewName(String clientSideValue) {
        Optional<T> optional = super.lookupViewName(clientSideValue);
        if (optional.isEmpty()) {
            optional = (Optional<T>) SchedulerView.ofClientSideValue(clientSideValue);
        }
        return optional;
    }

    /**
     * Enumeration of possible scheduler options, that can be applied to the calendar.
     * Contains only options, that affect the client side library, but not internal options.
     * Also this list may not contain all options, but the most common used ones.
     * Any missing option can be set manually using one of the {@link FullCalendar#setOption} methods
     * using a string key.
     * <br><br>
     * Please refer to the FullCalendar client library documentation for possible options:
     * <a href="https://fullcalendar.io/docs">https://fullcalendar.io/docs</a>
     */
    public enum SchedulerOption {
        /**
         * In vertical resource view, display dates above resources instead of resources above dates.
         * <dl>
         *   <dt>Type</dt>    <dd>{@code boolean}</dd>
         *   <dt>Default</dt> <dd>{@code false}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/datesAboveResources">datesAboveResources</a>
         */
        DATES_ABOVE_RESOURCES("datesAboveResources"),

        /**
         * Allow dragging entries between resources.
         * <dl>
         *   <dt>Type</dt>    <dd>{@code boolean}</dd>
         *   <dt>Default</dt> <dd>inherits from {@link FullCalendar.Option#EDITABLE}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/eventResourceEditable">eventResourceEditable</a>
         */
        ENTRY_RESOURCES_EDITABLE("eventResourceEditable"),

        /**
         * Minimum pixel width of entries in timeline view.
         * <dl>
         *   <dt>Type</dt> <dd>{@code number} (pixels)</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/eventMinWidth">eventMinWidth</a>
         */
        ENTRY_MIN_WIDTH("eventMinWidth"),

        /**
         * Only show resources that have entries assigned.
         * <dl>
         *   <dt>Type</dt>    <dd>{@code boolean}</dd>
         *   <dt>Default</dt> <dd>{@code false}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/filterResourcesWithEvents">filterResourcesWithEvents</a>
         */
        FILTER_RESOURCES_WITH_ENTRIES("filterResourcesWithEvents"),

        /**
         * Group the calendar view by date first, then by resource.
         * <dl>
         *   <dt>Type</dt>    <dd>{@code boolean}</dd>
         *   <dt>Default</dt> <dd>{@code false}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/groupByDateAndResource">groupByDateAndResource</a>
         */
        GROUP_BY_DATE_AND_RESOURCE("groupByDateAndResource"),

        /**
         * Group the calendar view by resource.
         * <dl>
         *   <dt>Type</dt>    <dd>{@code boolean}</dd>
         *   <dt>Default</dt> <dd>{@code false}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/groupByResource">groupByResource</a>
         */
        GROUP_BY_RESOURCE("groupByResource"),

        /**
         * FullCalendar Scheduler license key required for scheduler views to work.
         * <dl>
         *   <dt>Type</dt> <dd>{@code string}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/schedulerLicenseKey">schedulerLicenseKey</a>
         */
        LICENSE_KEY("schedulerLicenseKey"),

        /**
         * Re-fetch resources from the data provider when navigating to a different period.
         * <dl>
         *   <dt>Type</dt>    <dd>{@code boolean}</dd>
         *   <dt>Default</dt> <dd>{@code false}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/refetchResourcesOnNavigate">refetchResourcesOnNavigate</a>
         */
        REFETCH_RESOURCES_ON_NAVIGATE("refetchResourcesOnNavigate"),

        /**
         * Column definitions for the resource area (left side in timeline views).
         * <dl>
         *   <dt>Type</dt>    <dd>array of column configuration objects</dd>
         *   <dt>Default</dt> <dd>single column with resource name</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/resourceAreaColumns">resourceAreaColumns</a>
         */
        RESOURCE_AREA_COLUMNS("resourceAreaColumns"),

        /**
         * Custom content for the resource area header cell (top-left corner in timeline views).
         * <dl>
         *   <dt>Type</dt> <dd>{@code string} | HTML string | content object</dd>
         * </dl>
         * To use a JS function callback, use {@link FullCalendarScheduler#setOption(String, Object)}
         * with a {@link JsCallback} value.
         *
         * @see <a href="https://fullcalendar.io/docs/resource-area-header-render-hooks">resourceAreaHeaderContent</a>
         */
        RESOURCE_AREA_HEADER_CONTENT("resourceAreaHeaderContent"),

        /**
         * Width of the resource area (left column in timeline/vertical-resource views).
         * <dl>
         *   <dt>Type</dt>    <dd>CSS width string (e.g., {@code "200px"}, {@code "20%"})</dd>
         *   <dt>Default</dt> <dd>auto-calculated by FullCalendar</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/resourceAreaWidth">resourceAreaWidth</a>
         */
        RESOURCE_AREA_WIDTH("resourceAreaWidth"),

        /**
         * Field name in resource data used to group resources.
         * <dl>
         *   <dt>Type</dt>    <dd>{@code string} (field name)</dd>
         *   <dt>Default</dt> <dd>none (no grouping)</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/resourceGroupField">resourceGroupField</a>
         */
        RESOURCE_GROUP_FIELD("resourceGroupField"),

        /**
         * Whether resource groups start in an expanded state.
         * <dl>
         *   <dt>Type</dt>    <dd>{@code boolean}</dd>
         *   <dt>Default</dt> <dd>{@code true}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/resourcesInitiallyExpanded">resourcesInitiallyExpanded</a>
         */
        RESOURCES_INITIALLY_EXPANDED("resourcesInitiallyExpanded"),

        /**
         * Default sort order for resources.
         * <dl>
         *   <dt>Type</dt>    <dd>{@code string} | array of sort keys | {@code -1} for reverse order</dd>
         *   <dt>Default</dt> <dd>alphabetical by name</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/resourceOrder">resourceOrder</a>
         */
        RESOURCE_ORDER("resourceOrder"),

        /**
         * Minimum pixel width of each time slot column in timeline view.
         * <dl>
         *   <dt>Type</dt>    <dd>{@code number} (pixels)</dd>
         *   <dt>Default</dt> <dd>auto-calculated</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/slotMinWidth">slotMinWidth</a>
         */
        SLOT_MIN_WIDTH("slotMinWidth"),


        // ---- Callback options (merged from SchedulerCallbackOption) ----

        // ---- Render hooks: Resource Label ----
        /**
         * Add CSS classes to resource name label cells. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {resource, el, view}}</dd>
         *   <dt>Returns</dt>   <dd>string array of CSS class names</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/resource-render-hooks">resourceLabelClassNames</a>
         */
        RESOURCE_LABEL_CLASS_NAMES("resourceLabelClassNames"),

        /**
         * Customize the content inside a resource name label cell. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {resource, el, view}}</dd>
         *   <dt>Returns</dt>   <dd>content object or HTML string</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/resource-render-hooks">resourceLabelContent</a>
         */
        RESOURCE_LABEL_CONTENT("resourceLabelContent"),

        /**
         * Called after a resource label element is added to the DOM. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {resource, el, view}}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/resource-render-hooks">resourceLabelDidMount</a>
         */
        RESOURCE_LABEL_DID_MOUNT("resourceLabelDidMount"),

        /**
         * Called before a resource label element is removed from the DOM. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {resource, el, view}}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/resource-render-hooks">resourceLabelWillUnmount</a>
         */
        RESOURCE_LABEL_WILL_UNMOUNT("resourceLabelWillUnmount"),

        // ---- Render hooks: Resource Lane ----
        /**
         * Add CSS classes to a resource lane. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {resource, el, view}}</dd>
         *   <dt>Returns</dt>   <dd>string array of CSS class names</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/resource-render-hooks">resourceLaneClassNames</a>
         */
        RESOURCE_LANE_CLASS_NAMES("resourceLaneClassNames"),

        /**
         * Customize the content inside a resource lane. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {resource, el, view}}</dd>
         *   <dt>Returns</dt>   <dd>content object or HTML string</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/resource-render-hooks">resourceLaneContent</a>
         */
        RESOURCE_LANE_CONTENT("resourceLaneContent"),

        /**
         * Called after a resource lane element is added to the DOM. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {resource, el, view}}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/resource-render-hooks">resourceLaneDidMount</a>
         */
        RESOURCE_LANE_DID_MOUNT("resourceLaneDidMount"),

        /**
         * Called before a resource lane element is removed from the DOM. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {resource, el, view}}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/resource-render-hooks">resourceLaneWillUnmount</a>
         */
        RESOURCE_LANE_WILL_UNMOUNT("resourceLaneWillUnmount"),

        // ---- Render hooks: Resource Group ----
        /**
         * Add CSS classes to a resource group header row. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {groupValue, el, view}}</dd>
         *   <dt>Returns</dt>   <dd>string array of CSS class names</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/resource-group-render-hooks">resourceGroupClassNames</a>
         */
        RESOURCE_GROUP_CLASS_NAMES("resourceGroupClassNames"),

        /**
         * Customize the content inside a resource group header row. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {groupValue, el, view}}</dd>
         *   <dt>Returns</dt>   <dd>content object or HTML string</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/resource-group-render-hooks">resourceGroupContent</a>
         */
        RESOURCE_GROUP_CONTENT("resourceGroupContent"),

        /**
         * Called after a resource group header element is added to the DOM. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {groupValue, el, view}}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/resource-group-render-hooks">resourceGroupDidMount</a>
         */
        RESOURCE_GROUP_DID_MOUNT("resourceGroupDidMount"),

        /**
         * Called before a resource group header element is removed from the DOM. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {groupValue, el, view}}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/resource-group-render-hooks">resourceGroupWillUnmount</a>
         */
        RESOURCE_GROUP_WILL_UNMOUNT("resourceGroupWillUnmount"),

        // ---- Render hooks: Resource Group Lane ----
        /**
         * Add CSS classes to a resource group lane row. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {groupValue, el, view}}</dd>
         *   <dt>Returns</dt>   <dd>string array of CSS class names</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/resource-group-render-hooks">resourceGroupLaneClassNames</a>
         */
        RESOURCE_GROUP_LANE_CLASS_NAMES("resourceGroupLaneClassNames"),

        /**
         * Customize the content inside a resource group lane row. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {groupValue, el, view}}</dd>
         *   <dt>Returns</dt>   <dd>content object or HTML string</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/resource-group-render-hooks">resourceGroupLaneContent</a>
         */
        RESOURCE_GROUP_LANE_CONTENT("resourceGroupLaneContent"),

        /**
         * Called after a resource group lane element is added to the DOM. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {groupValue, el, view}}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/resource-group-render-hooks">resourceGroupLaneDidMount</a>
         */
        RESOURCE_GROUP_LANE_DID_MOUNT("resourceGroupLaneDidMount"),

        /**
         * Called before a resource group lane element is removed from the DOM. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {groupValue, el, view}}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/resource-group-render-hooks">resourceGroupLaneWillUnmount</a>
         */
        RESOURCE_GROUP_LANE_WILL_UNMOUNT("resourceGroupLaneWillUnmount"),

        // ---- Render hooks: Resource Area Header ----
        /**
         * Add CSS classes to the resource area header cell. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {el, view}}</dd>
         *   <dt>Returns</dt>   <dd>string array of CSS class names</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/resource-area-header-render-hooks">resourceAreaHeaderClassNames</a>
         */
        RESOURCE_AREA_HEADER_CLASS_NAMES("resourceAreaHeaderClassNames"),

        /**
         * Called after the resource area header element is added to the DOM. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {el, view}}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/resource-area-header-render-hooks">resourceAreaHeaderDidMount</a>
         */
        RESOURCE_AREA_HEADER_DID_MOUNT("resourceAreaHeaderDidMount"),

        /**
         * Called before the resource area header element is removed from the DOM. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {el, view}}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/resource-area-header-render-hooks">resourceAreaHeaderWillUnmount</a>
         */
        RESOURCE_AREA_HEADER_WILL_UNMOUNT("resourceAreaHeaderWillUnmount"),

        // ---- Resource lifecycle callbacks ----
        /**
         * Called when a resource is added to the calendar. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {resource}}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/resourceAdd">resourceAdd</a>
         */
        RESOURCE_ADD("resourceAdd"),

        /**
         * Called when a resource is modified. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {oldResource, resource, revert}}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/resourceChange">resourceChange</a>
         */
        RESOURCE_CHANGE("resourceChange"),

        /**
         * Called when a resource is removed from the calendar. Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {resource}}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/resourceRemove">resourceRemove</a>
         */
        RESOURCE_REMOVE("resourceRemove"),

        /**
         * Called after all resources are set (bulk). Accepts a {@link JsCallback}.
         * <dl>
         *   <dt>Arguments</dt> <dd>{@code {resources}}</dd>
         * </dl>
         *
         * @see <a href="https://fullcalendar.io/docs/resourcesSet">resourcesSet</a>
         */
        RESOURCES_SET("resourcesSet"),

        ;


        private final String optionKey;

        private static final Map<SchedulerOption, List<JsonItemPropertyConverter<?, ?>>> CONVERTER_CACHE;

        static {
            Map<SchedulerOption, List<JsonItemPropertyConverter<?, ?>>> map = new EnumMap<>(SchedulerOption.class);
            for (SchedulerOption opt : values()) {
                try {
                    JsonConverter[] annotations = SchedulerOption.class.getField(opt.name())
                            .getAnnotationsByType(JsonConverter.class);
                    if (annotations.length > 0) {
                        List<JsonItemPropertyConverter<?, ?>> list = new ArrayList<>();
                        for (JsonConverter ann : annotations) {
                            list.add(ann.value().getConstructor().newInstance());
                        }
                        map.put(opt, Collections.unmodifiableList(list));
                    }
                } catch (ReflectiveOperationException e) {
                    throw new ExceptionInInitializerError(e);
                }
            }
            CONVERTER_CACHE = Collections.unmodifiableMap(map);
        }

        SchedulerOption(String optionKey) {
            this.optionKey = optionKey;
        }

        String getOptionKey() {
            return optionKey;
        }

        /**
         * Returns the converters registered for this option via {@link JsonConverter} annotations, in order.
         */
        public List<JsonItemPropertyConverter<?, ?>> getConverters() {
            return CONVERTER_CACHE.getOrDefault(this, List.of());
        }

        /**
         * If this option has one or more {@link JsonConverter} annotations and the given value is
         * supported by one of them, returns the converted {@link JsonNode}. Otherwise returns empty.
         */
        @SuppressWarnings("unchecked")
        public Optional<JsonNode> convertValue(Object value) {
            for (JsonItemPropertyConverter<?, ?> c : getConverters()) {
                if (c.supports(value)) {
                    return Optional.of(((JsonItemPropertyConverter<Object, Object>) c).toClientModel(value, null));
                }
            }
            return Optional.empty();
        }
    }

    // SchedulerCallbackOption enum removed — all constants merged into SchedulerOption.
    // Use setOption(SchedulerOption.X, JsCallback.of("function...")) instead.

}
