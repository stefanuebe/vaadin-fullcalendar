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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.function.SerializableFunction;
import lombok.NonNull;
import elemental.json.JsonObject;

import java.util.*;

/**
 * A resource area column that renders a Vaadin {@link Component} per resource, analogous to
 * Vaadin Grid's {@code ComponentColumn}. The component is created by a callback that receives
 * the {@link Resource} and returns a component instance.
 * <p>
 * Components are managed via a "DOM teleportation" pattern: they live in the Vaadin component
 * tree (hidden container) but their DOM elements are physically moved into FullCalendar cells
 * via {@code cellDidMount} / {@code cellWillUnmount} lifecycle hooks.
 * <p>
 * The {@code cellContent}, {@code cellDidMount}, and {@code cellWillUnmount} properties are
 * managed internally and cannot be set manually — calling any of those {@code withXxx()} methods
 * throws {@link UnsupportedOperationException}.
 * <p>
 * Example:
 * <pre>{@code
 * var dateColumn = new ComponentResourceAreaColumn<DatePicker>("deadline", "Deadline",
 *     resource -> {
 *         DatePicker picker = new DatePicker();
 *         picker.setWidth("130px");
 *         return picker;
 *     });
 * scheduler.setResourceAreaColumns(
 *     new ResourceAreaColumn("title", "Name").withWidth("200px"),
 *     dateColumn.withWidth("160px")
 * );
 *
 * // Type-safe access at runtime
 * dateColumn.getComponent(resource).ifPresent(picker -> picker.setValue(LocalDate.now()));
 * }</pre>
 *
 * @param <T> the component type returned by the factory callback
 * @see ResourceAreaColumn
 * @see Scheduler#setResourceAreaColumns(java.util.List)
 */
public class ComponentResourceAreaColumn<T extends Component> extends ResourceAreaColumn {

    private final SerializableFunction<Resource, T> componentFactory;
    private final Map<String, T> components = new LinkedHashMap<>();
    private FullCalendarScheduler boundCalendar;

    /**
     * Creates a component column with a field key, header text, and component factory.
     *
     * @param field            unique column key (not displayed — cell content is suppressed)
     * @param headerContent    header text for the column
     * @param componentFactory callback that creates a component per resource; must not return null
     *                         and must return a unique instance per call
     */
    public ComponentResourceAreaColumn(@NonNull String field, String headerContent,
                                       @NonNull SerializableFunction<Resource, T> componentFactory) {
        super(field, headerContent);
        this.componentFactory = componentFactory;
    }

    /**
     * Creates a component column with a field key and component factory (no header text).
     *
     * @param field            unique column key
     * @param componentFactory callback that creates a component per resource
     */
    public ComponentResourceAreaColumn(@NonNull String field,
                                       @NonNull SerializableFunction<Resource, T> componentFactory) {
        super(field);
        this.componentFactory = componentFactory;
    }

    // ---- Bind / Unbind (package-private, called by FullCalendarScheduler) ----

    void bind(FullCalendarScheduler calendar) {
        if (this.boundCalendar != null && this.boundCalendar != calendar) {
            throw new IllegalStateException(
                    "ComponentResourceAreaColumn '" + getField() + "' is already bound to a different calendar");
        }
        this.boundCalendar = calendar;
    }

    void unbind() {
        this.boundCalendar = null;
    }

    /**
     * Returns whether this column is currently bound to a calendar.
     *
     * @return true if bound
     */
    public boolean isBound() {
        return boundCalendar != null;
    }

    // ---- Component lifecycle (package-private, called by FullCalendarScheduler) ----

    void createComponent(Resource resource) {
        Objects.requireNonNull(resource);
        String id = resource.getId();
        if (components.containsKey(id)) {
            return; // already created
        }

        T component;
        try {
            component = componentFactory.apply(resource);
        } catch (Exception e) {
            // BR-23: callback throws → resource has no component, map stays consistent
            return;
        }

        if (component == null) {
            throw new IllegalStateException(
                    "Component callback returned null for resource '" + id + "'");
        }
        if (component.getParent().isPresent()) {
            throw new IllegalStateException(
                    "Component returned by callback is already attached — each resource must receive a unique component instance");
        }

        component.getElement().setAttribute("data-rc-resource-id", id);
        component.getElement().setAttribute("data-rc-column-key", getField());
        component.getElement().getStyle().set("display", "none");

        if (boundCalendar != null) {
            // Append directly to calendar element. The TS-side cellDidMount callback
            // moves it into the FC cell and makes it visible. We don't use the hidden
            // container for initial attachment because FC's Calendar(el) wipes all
            // light DOM children during init. Vaadin re-sends components via UIDL.
            boundCalendar.getElement().appendChild(component.getElement());
        }

        components.put(id, component);
    }

    void destroyComponent(Resource resource) {
        Objects.requireNonNull(resource);
        T component = components.remove(resource.getId());
        if (component != null) {
            component.getElement().removeFromParent();
        }
    }

    void destroyAllComponents() {
        for (T component : components.values()) {
            component.getElement().removeFromParent();
        }
        components.clear();
    }

    // ---- Public API ----

    /**
     * Returns the component for the given resource, or empty if none exists.
     *
     * @param resource the resource; must not be null
     * @return optional component
     */
    public Optional<T> getComponent(@NonNull Resource resource) {
        return Optional.ofNullable(components.get(resource.getId()));
    }

    /**
     * Returns an unmodifiable view of all resource-component mappings, keyed by resource ID.
     *
     * @return unmodifiable map
     */
    public Map<String, T> getComponents() {
        return Collections.unmodifiableMap(components);
    }

    /**
     * Destroys and re-creates the component for a single resource. The old component state is lost.
     * <p>
     * No-op if the resource is not registered or if the calendar is not attached.
     *
     * @param resource the resource to refresh; must not be null
     */
    public void refresh(@NonNull Resource resource) {
        if (boundCalendar == null || !boundCalendar.isAttached()) {
            return;
        }
        if (!components.containsKey(resource.getId())) {
            return;
        }

        // Step 1: client-side return to container
        boundCalendar.getElement().callJsFunction("returnComponentToContainer",
                resource.getId(), getField());

        // Step 2: server-side destroy + re-create in beforeClientResponse
        boundCalendar.getElement().getNode().runWhenAttached(ui ->
                ui.beforeClientResponse(boundCalendar, ctx -> {
                    destroyComponent(resource);
                    createComponent(resource);
                }));
    }

    /**
     * Destroys and re-creates all components. Component state is lost for all resources.
     * Continues on partial callback failures; throws {@link IllegalStateException} after
     * completing iteration if any callback failed.
     * <p>
     * No-op if the calendar is not attached.
     */
    public void refreshAll() {
        if (boundCalendar == null || !boundCalendar.isAttached()) {
            return;
        }
        if (components.isEmpty()) {
            return;
        }

        // Step 1: client-side return all to container
        boundCalendar.getElement().callJsFunction("returnAllComponentsToContainer");

        // Step 2: server-side destroy all + re-create in beforeClientResponse
        boundCalendar.getElement().getNode().runWhenAttached(ui ->
                ui.beforeClientResponse(boundCalendar, ctx -> {
                    // Snapshot resource IDs before clearing
                    List<String> resourceIds = new ArrayList<>(components.keySet());
                    destroyAllComponents();

                    RuntimeException firstFailure[] = {null};
                    for (String resourceId : resourceIds) {
                        boundCalendar.getResourceById(resourceId).ifPresent(resource -> {
                            try {
                                createComponent(resource);
                            } catch (RuntimeException e) {
                                if (firstFailure[0] == null) {
                                    firstFailure[0] = e;
                                }
                            }
                        });
                    }

                    // Step 3: trigger FC re-render
                    boundCalendar.getElement().callJsFunction("rerenderResources");

                    if (firstFailure[0] != null) {
                        throw new IllegalStateException(
                                "One or more component callbacks failed during refreshAll()",
                                firstFailure[0]);
                    }
                }));
    }

    // ---- Fluent method overrides (covariant return types) ----

    @Override
    public ComponentResourceAreaColumn<T> withWidth(String width) {
        super.withWidth(width);
        return this;
    }

    @Override
    public ComponentResourceAreaColumn<T> withGroup(boolean group) {
        super.withGroup(group);
        return this;
    }

    @Override
    public ComponentResourceAreaColumn<T> withHeaderClassNames(String classNames) {
        super.withHeaderClassNames(classNames);
        return this;
    }

    @Override
    public ComponentResourceAreaColumn<T> withHeaderClassNames(JsCallback callback) {
        super.withHeaderClassNames(callback);
        return this;
    }

    @Override
    public ComponentResourceAreaColumn<T> withHeaderDidMount(String jsFunction) {
        super.withHeaderDidMount(jsFunction);
        return this;
    }

    @Override
    public ComponentResourceAreaColumn<T> withHeaderDidMount(JsCallback callback) {
        super.withHeaderDidMount(callback);
        return this;
    }

    @Override
    public ComponentResourceAreaColumn<T> withHeaderWillUnmount(String jsFunction) {
        super.withHeaderWillUnmount(jsFunction);
        return this;
    }

    @Override
    public ComponentResourceAreaColumn<T> withHeaderWillUnmount(JsCallback callback) {
        super.withHeaderWillUnmount(callback);
        return this;
    }

    @Override
    public ComponentResourceAreaColumn<T> withCellClassNames(String classNames) {
        super.withCellClassNames(classNames);
        return this;
    }

    @Override
    public ComponentResourceAreaColumn<T> withCellClassNames(JsCallback callback) {
        super.withCellClassNames(callback);
        return this;
    }

    // ---- Blocked methods (managed internally) ----

    /**
     * @throws UnsupportedOperationException always — cell content is managed internally
     */
    @Override
    public ComponentResourceAreaColumn<T> withCellContent(String staticContent) {
        throw new UnsupportedOperationException(
                "cellContent is managed internally by ComponentResourceAreaColumn");
    }

    /**
     * @throws UnsupportedOperationException always — cell content is managed internally
     */
    @Override
    public ComponentResourceAreaColumn<T> withCellContent(JsCallback callback) {
        throw new UnsupportedOperationException(
                "cellContent is managed internally by ComponentResourceAreaColumn");
    }

    /**
     * @throws UnsupportedOperationException always — cellDidMount is managed internally
     */
    @Override
    public ComponentResourceAreaColumn<T> withCellDidMount(String jsFunction) {
        throw new UnsupportedOperationException(
                "cellDidMount is managed internally by ComponentResourceAreaColumn");
    }

    /**
     * @throws UnsupportedOperationException always — cellDidMount is managed internally
     */
    @Override
    public ComponentResourceAreaColumn<T> withCellDidMount(JsCallback callback) {
        throw new UnsupportedOperationException(
                "cellDidMount is managed internally by ComponentResourceAreaColumn");
    }

    /**
     * @throws UnsupportedOperationException always — cellWillUnmount is managed internally
     */
    @Override
    public ComponentResourceAreaColumn<T> withCellWillUnmount(String jsFunction) {
        throw new UnsupportedOperationException(
                "cellWillUnmount is managed internally by ComponentResourceAreaColumn");
    }

    /**
     * @throws UnsupportedOperationException always — cellWillUnmount is managed internally
     */
    @Override
    public ComponentResourceAreaColumn<T> withCellWillUnmount(JsCallback callback) {
        throw new UnsupportedOperationException(
                "cellWillUnmount is managed internally by ComponentResourceAreaColumn");
    }

    // ---- JSON serialization ----

    @Override
    public JsonObject toJson() {
        JsonObject json = super.toJson();

        // Add auto-generated cellContent, cellDidMount, cellWillUnmount
        // (parent fields are null since withXxx throws, so super.toJson() didn't write them)
        String columnKey = getField();

        json.put("cellContent", JsCallback.of(
                "function() { return { domNodes: [] } }"
        ).toMarkerJson());

        json.put("cellDidMount", JsCallback.of(
                "function(info) {" +
                "  var calendarEl = info.el.closest('vaadin-full-calendar-scheduler');" +
                "  if (!calendarEl) return;" +
                "  var resourceId = CSS.escape(info.resource.id);" +
                // Search the entire calendar element for the component (it may be a direct
                // child of the calendar element or parked in the hidden container)
                "  var component = calendarEl.querySelector(" +
                "    '[data-rc-resource-id=\"' + resourceId + '\"][data-rc-column-key=\"" + columnKey + "\"]'" +
                "  );" +
                "  if (component) {" +
                // Inject into FC's cell cushion (the content area inside the cell frame)
                // so the component sits alongside/replacing the cell text, not below the frame
                "    var cushion = info.el.querySelector('.fc-datagrid-cell-cushion');" +
                "    var target = cushion || info.el;" +
                "    target.appendChild(component);" +
                "    component.style.display = '';" +
                "  }" +
                "}"
        ).toMarkerJson());

        json.put("cellWillUnmount", JsCallback.of(
                "function(info) {" +
                "  var calendarEl = info.el.closest('vaadin-full-calendar-scheduler');" +
                "  if (!calendarEl) return;" +
                "  var container = calendarEl.querySelector('[data-fc-component-container]');" +
                "  if (!container) return;" +
                // Search within the entire cell (component may be in cushion or directly in td)
                "  var component = info.el.querySelector(" +
                "    '[data-rc-resource-id][data-rc-column-key=\"" + columnKey + "\"]'" +
                "  );" +
                "  if (component) {" +
                "    component.style.display = 'none';" +
                "    container.appendChild(component);" +
                "  }" +
                "}"
        ).toMarkerJson());

        return json;
    }
}
