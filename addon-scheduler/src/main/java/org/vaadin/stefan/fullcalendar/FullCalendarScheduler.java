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
import org.vaadin.stefan.fullcalendar.converter.ResourceColumnsConverter;
import org.vaadin.stefan.fullcalendar.converters.JsonItemPropertyConverter;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
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
@NpmPackage(value = "fullcalendar-scheduler", version = FullCalendarScheduler.FC_SCHEDULER_CLIENT_VERSION)
@JsModule("./vaadin-full-calendar/full-calendar-scheduler.ts")
@CssImport("./vaadin-full-calendar/full-calendar-scheduler-styles.css")
// full-calendar-scheduler.ts imports this module. It is named here as well, because Vaadin checks only the files named
// in these annotations to decide whether an application's frontend bundle needs a rebuild.
@JsModule("./vaadin-full-calendar/legacy-class-names-scheduler.ts")

@Tag("vaadin-full-calendar-scheduler")
public class FullCalendarScheduler extends FullCalendar implements Scheduler {

    /**
     * The FullCalendar Scheduler version used in this addon. Always the same as {@link FullCalendar#FC_CLIENT_VERSION},
     * because core and scheduler are released together.
     */
    public static final String FC_SCHEDULER_CLIENT_VERSION = FullCalendar.FC_CLIENT_VERSION;
    private final Map<String, Resource> resources = new HashMap<>();
    private final List<ComponentResourceColumn<?>> activeComponentColumns = new ArrayList<>();
    private Element hiddenContainer;

    // --- Batched resource-write pending state (see #231) ---
    // Pending resource ops accumulated within the current server request. All writes
    // (addResources / removeResources / updateResource / removeAllResources) mutate only
    // this state; the actual client-side JS calls fire once at beforeClientResponse in
    // a single, minimal op set. This avoids sending N inner FC updates for N writes —
    // which was the root cause of the scrollgrid sizing race previously papered over
    // by a retry loop on the client.
    // LinkedHashMap preserves insertion order so adds/updates reach the client in the
    // order the server code performed them.
    private final Map<String, Resource> pendingAdds = new LinkedHashMap<>();
    private final Map<String, Resource> pendingRemoves = new LinkedHashMap<>();
    private final Map<String, Resource> pendingUpdates = new LinkedHashMap<>();
    private boolean pendingRemoveAll = false;
    private boolean pendingScrollToLast = false;
    private boolean resourceFlushScheduled = false;

    /**
     * Creates a new instance without any settings beside the default locale ({@link CalendarLocale#getDefaultLocale()}).
     */
    public FullCalendarScheduler() {
        super();
    }

    /**
     * Creates a new instance with custom initial options. This allows a full override of the default
     * initial options, that the calendar would normally receive. Theoretically you can set all options,
     * as long as they are not based on a client side variable (as for instance "plugins" or "locales").
     * Complex objects are possible, too, for instance for view-specific settings.
     *  Please refer to the official FullCalendar documentation regarding potential options.
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

            // Step 3: re-add resources to FC. Route through the pending-flush pipeline so
            // any user-code add/remove/update calls in the same reattach request are
            // merged into one client dispatch. Filter to top-level resources here because
            // the resources map also contains children (registered via
            // registerResourcesInternally); children are serialised recursively via
            // each root's toJson() and must not be sent separately.
            if (!resources.isEmpty()) {
                resources.values().stream()
                        .filter(r -> r.getParent().isEmpty())
                        .forEach(r -> pendingAdds.put(r.getId(), r));
                scheduleResourceFlush();
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

    @Override
    public void addResources(Iterable<Resource> iterableResource) {
        addResources(iterableResource, true);
    }

    @Override
    public void addResources(Iterable<Resource> iterableResource, boolean scrollToLast) {
        Objects.requireNonNull(iterableResource);

        iterableResource.forEach(resource -> {
            String id = resource.getId();
            if (!resources.containsKey(id)) {
                resources.put(id, resource);
                resource.attachScheduler(this);
                // Queue for client dispatch in beforeClientResponse. Sub-resources are
                // serialised recursively by toJson() at flush time, so no separate add.
                pendingAdds.put(id, resource);
                // Any earlier pending update for this id is collapsed — the add payload
                // carries the latest state.
                pendingUpdates.remove(id);

                // create components for active component columns
                for (var col : activeComponentColumns) {
                    col.createComponent(resource);
                }
            }

            // now also register child resources
            registerResourcesInternally(resource.getChildren());
        });
        pendingScrollToLast = pendingScrollToLast || scrollToLast;
        scheduleResourceFlush();
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

                // Collapse: if this resource was added earlier in the same request, the
                // client never heard about it — just cancel the pending add.
                if (pendingAdds.remove(id) == null) {
                    pendingRemoves.put(id, resource);
                }
                // Drop any pending update for a resource we're about to remove anyway.
                pendingUpdates.remove(id);
            }
        });

        scheduleResourceFlush();
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

        // The client will receive a single removeAllResources; any earlier piecewise
        // pending ops in this same request are now moot.
        pendingAdds.clear();
        pendingRemoves.clear();
        pendingUpdates.clear();
        pendingRemoveAll = true;
        pendingScrollToLast = false;
        scheduleResourceFlush();
    }

    @Override
    public void setResourceColumns(List<? extends ResourceColumn> columns) {
        Objects.requireNonNull(columns);
        setOption(SchedulerOption.RESOURCE_COLUMNS, columns);
    }

    /**
     * Validates the columns and binds the component columns, so their components exist before the columns
     * reach the client.
     */
    private void bindResourceColumns(List<? extends ResourceColumn> columns) {
        // validate no duplicate field keys
        Set<String> fieldKeys = new HashSet<>();
        for (ResourceColumn col : columns) {
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
        for (ResourceColumn col : columns) {
            if (col instanceof ComponentResourceColumn<?> compCol) {
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
     * Package-private — used by {@link ComponentResourceColumn}.
     */
    Element getHiddenContainer() {
        ensureHiddenContainer();
        return hiddenContainer;
    }

    @Override
    public void updateResource(Resource resource) {
        Objects.requireNonNull(resource);
        String id = resource.getId();
        if (pendingAdds.containsKey(id)) {
            // Same-request add + update: the pending add will already carry the
            // latest toJson() state at flush time; no separate update needed.
            pendingAdds.put(id, resource);
        } else if (pendingRemoves.containsKey(id)) {
            // Updating a resource that's being removed in the same request — the
            // remove wins; the update is moot.
            return;
        } else {
            pendingUpdates.put(id, resource);
        }
        scheduleResourceFlush();
    }

    /**
     * Registers a single {@code beforeClientResponse} callback that flushes all
     * pending resource ops. Subsequent calls within the same request are idempotent;
     * the callback's {@code finally} block resets both the pending state and the
     * {@code resourceFlushScheduled} guard so the next request re-schedules fresh.
     * <p>
     * <b>Detach mid-request:</b> if the component is detached after a pending write
     * but before the callback fires, the {@code runWhenAttached} closure is held by
     * the Vaadin node and re-invoked when the component reattaches, at which point
     * {@link #onAttach(AttachEvent)} has already (re-)populated {@code pendingAdds}
     * with the current {@code resources} map. The flush then fires with the correct
     * state. The guard is intentionally NOT reset on detach — that would cause a
     * double-registration on reattach because {@code onAttach} also calls this
     * method.
     */
    protected void scheduleResourceFlush() {
        if (resourceFlushScheduled) {
            return;
        }
        resourceFlushScheduled = true;
        getElement().getNode().runWhenAttached(ui ->
                ui.beforeClientResponse(this, ctx -> {
                    try {
                        flushResourceOps();
                    } finally {
                        resourceFlushScheduled = false;
                        pendingAdds.clear();
                        pendingRemoves.clear();
                        pendingUpdates.clear();
                        pendingRemoveAll = false;
                        pendingScrollToLast = false;
                    }
                }));
    }

    /**
     * Dispatches the minimal set of client-side resource ops that matches the pending
     * state. Order: {@code removeAllResources} → {@code removeResources} →
     * {@code addResources} → {@code updateResource} (per entry). A single request with
     * any number of resource writes fires at most four JS calls plus one per lingering
     * update.
     */
    protected void flushResourceOps() {
        if (pendingRemoveAll) {
            getElement().callJsFunction("removeAllResources");
        } else if (!pendingRemoves.isEmpty()) {
            // The client-side removeResources handler only reads array[i].id, but we
            // keep sending full resource.toJson() for wire-shape consistency with
            // addResources above.
            ArrayNode array = JsonFactory.createArray();
            pendingRemoves.values().forEach(r -> array.add(r.toJson()));
            getElement().callJsFunction("removeResources", array);
        }
        if (!pendingAdds.isEmpty()) {
            // pendingAdds is expected to contain only top-level resources; children
            // are registered via registerResourcesInternally (which does NOT touch
            // pendingAdds) and serialised recursively via each root's toJson(). The
            // onAttach replay filters to top-level before populating pendingAdds.
            ArrayNode array = JsonFactory.createArray();
            pendingAdds.values().forEach(r -> array.add(r.toJson()));
            getElement().callJsFunction("addResources", array, pendingScrollToLast);
        }
        if (!pendingUpdates.isEmpty()) {
            // The client-side updateResource takes a single resource JSON. Keep that shape
            // to avoid a TS-side API addition; the loop still emits at most one JS call per
            // pending update, batched together in the same request.
            pendingUpdates.values().forEach(r ->
                    getElement().callJsFunction("updateResource", r.toJson().toString()));
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
     * Every way of setting an option ends here, so the resource columns are handled once. A column list
     * binds its component columns and goes through {@link ResourceColumnsConverter}, an empty list removes
     * the option. Any other value (raw JSON, null) unbinds the component columns set before.
     * A column list always uses the column converter, also when the caller passed other converters.
     */
    @Override
    protected void setOption(String option, Object value, Object valueForServerSide,
                             List<JsonItemPropertyConverter<?, ?>> converters) {
        if (SchedulerOption.RESOURCE_COLUMNS.getOptionKey().equals(option)) {
            if (value instanceof List<?> list && list.stream().allMatch(ResourceColumn.class::isInstance)) {
                bindResourceColumns(list.stream().map(ResourceColumn.class::cast).toList());
                value = list.isEmpty() ? null : list;
                converters = SchedulerOption.RESOURCE_COLUMNS.getConverters();
            } else {
                bindResourceColumns(List.of());
            }
        }
        super.setOption(option, value, valueForServerSide, converters);
    }

    @Override
    public FullCalendarScheduler withAutoBrowserTimezone() {
        super.withAutoBrowserTimezone();
        return this;
    }

    @Override
    public FullCalendarScheduler withAutoUiLocale() {
        super.withAutoUiLocale();
        return this;
    }

    /**
     * Sets a option for this instance. Passing a null value removes the option. The third parameter
     * might be used to explicitly store a "more complex" variant of the option's value to be returned
     * by {@link #getOption(SchedulerOption)}. It is always stored when not equal to the value except for null.
     * If it is equal to the value or null it will not be stored (old version will be removed from internal cache).
     * <br><br>
     * Please be aware that this method does not check the passed value. Use the typed
     * {@link SchedulerOption} constants for type safety.
     *
     * @param option             option
     * @param value              value
     * @param valueForServerSide value to be stored on server side
     * @throws NullPointerException when null is passed
     * @deprecated use {@link #setOption(SchedulerOption, Object)} with the typed value. The option's converter creates
     * the client-side value from it, so the client and {@link #getOption(SchedulerOption)} cannot get out of step.
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public void setOption(SchedulerOption option, Object value, Object valueForServerSide) {
        setOption(option.getOptionKey(), value, valueForServerSide, option.getConverters());
    }

    /**
     * Returns an optional option value or empty, that has been set for that key via one of the setOptions methods.
     * If a server side version of the value has been set
     * via {@link #setOption(SchedulerOption, Serializable, Object)}, that will be returned instead.
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

    /**
     * Returns the value of the given option like {@link #getOption(SchedulerOption)}, or the given default when the
     * option is not set.
     *
     * @param option       option
     * @param defaultValue value to return when the option is not set
     * @param <T>          type of value
     * @return the option's value or the default
     * @throws NullPointerException when null is passed as option
     * @see #getOptionOrDefault(Option, Object)
     */
    public <T> T getOptionOrDefault(SchedulerOption option, T defaultValue) {
        return this.<T>getOption(option).orElse(defaultValue);
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

    // SchedulerCallbackOption enum removed — all constants merged into SchedulerOption.
    // Use setOption(SchedulerOption.X, JsCallback.of("function...")) instead.

}
