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

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.*;

/**
 * Represents a resource. ResourceEntries contain these resources (a resource itself does not know anything about
 * the assigned entries). A resource can have sub resources / child resources.
 * <p>
 * Resources can carry per-resource entry style overrides ({@link #setEntryBackgroundColor(String)},
 * {@link #setEntryBorderColor(String)}, {@link #setEntryTextColor(String)}, and
 * {@link #setEntryClassNames(java.util.Set)}) that apply to all entries associated with the resource.
 */
@Getter
@EqualsAndHashCode(of = "id")
public class Resource {

    /**
     * The id of this resource.
     * Uniquely identifies this resource.
     */
    private final String id;

    /**
     * The title/name of this resource.
     * Text that will be displayed on the resource when it is rendered.
     */
    private String title;

    /**
     * The color of this resource.
     * Events associated with this resources will have their backgrounds and borders colored.
     */
    private String color;

    /**
     * The BusinessHours array of this resource.
     * A businessHours[] declaration that will only apply to this resource.
     */
    private final BusinessHours[] businessHoursArray;

    /**
     * The childern's of the resource
     */
    private Set<Resource> children;
    /**
     * The parent of the current resource
     */
    private Resource parent;

    /**
     * The custom property list
     */
    private final HashMap<String, Object> extendedProps = new HashMap<>();

    // Per-resource event style overrides
    @Getter(AccessLevel.NONE)
    private String eventBackgroundColor;
    @Getter(AccessLevel.NONE)
    private String eventBorderColor;
    @Getter(AccessLevel.NONE)
    private String eventTextColor;
    @Getter(AccessLevel.NONE)
    private String eventConstraint;
    @Getter(AccessLevel.NONE)
    private Boolean eventOverlap;
    @Getter(AccessLevel.NONE)
    private Set<String> eventClassNames;
    @Getter(AccessLevel.NONE)
    private JsCallback eventAllow;

    // Scheduler back-reference for push updates (transient, excluded from equals/hashCode)
    @Getter(AccessLevel.NONE)
    private transient FullCalendarScheduler scheduler;

    /**
     * New instance. ID will be generated.
     */
    public Resource() {
        this(null, null, null);
    }

    /**
     * New instance. Awaits id and title. If no id is provided, one will be generated.
     *
     * @param id    id
     * @param title title
     * @param color color (optional)
     */
    public Resource(String id, String title, String color) {
        this(id, title, color, null);
    }

    /**
     * New instance. Awaits id and title. If no id is provided, one will be generated.
     * <br><br>
     * Adds the given resources as children using {@link #addChildren(Collection)} if a value != null is passed.
     *
     * @param id       id
     * @param title    title
     * @param color    color (optional)
     * @param children children (optional)
     * @param businessHours businessHours (optional)
     */
    public Resource(String id, String title, String color, Collection<Resource> children, BusinessHours... businessHours) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.title = title;
        this.color = color;
        this.businessHoursArray = (businessHours != null && businessHours.length == 0) ? null : businessHours;

        if (children != null) {
            addChildren(children);
        }
    }

    /**
     * Returns the resource's children as unmodifiable set. Empty, when not children are set.
     *
     * @return children
     */
    public Set<Resource> getChildren() {
        return children != null ? Collections.unmodifiableSet(children) : Collections.emptySet();
    }

    /**
     * Adds the given resource as children to this instance. If the given resource has been added to
     * other resources before, it will be removed from there. Also the parent is replaced.
     * <br><br>
     * Does not update the resource instances on the client side when this instance has been added to the calendar
     * before. In that case you need to add the child resources manually via {@link Scheduler#addResource(Resource)}.
     *
     * @param child resources to be added as children
     * @throws NullPointerException when null is passed
     */
    public void addChild(Resource child) {
        addChildren(Objects.requireNonNull(child));
    }

    /**
     * Adds the given resources as children to this instance. If the given resources have been added to
     * other resources before, they will be removed from there. Also the parent is replaced.
     * <br><br>
     * Does not update the resource instances on the client side when this instance has been added to the calendar
     * before. In that case you need to add the child resources manually via {@link Scheduler#addResources(Iterable)}.
     *
     * @param children resources to be added as children
     * @throws NullPointerException when null is passed
     */
    public void addChildren(Collection<Resource> children) {
        Objects.requireNonNull(children);

        if (this.children == null) {
            this.children = new LinkedHashSet<>(children);
        } else {
            this.children.addAll(children);
        }

        children.forEach(child -> {
            child.getParent().ifPresent(p -> p.children.remove(child)); // faster, but keep an eye on the removal to not miss anything later here
            child.setParent(this);
            if (this.scheduler != null) {
                child.attachScheduler(this.scheduler);
            }
        });
    }

    /**
     * Adds the given resources as children to this instance. If the given resources have been added to
     * other resources before, they will be removed from there. Also the parent is replaced.
     * <br><br>
     * Does not update the resource instances on the client side when this instance has been added to the calendar
     * before. In that case you need to add the child resources manually via {@link Scheduler#addResources(Resource...)}.
     *
     * @param children resources to be added as children
     * @throws NullPointerException when null is passed
     */
    public void addChildren(Resource... children) {
        addChildren(Arrays.asList(children));
    }

    /**
     * Removes the given resource from this instance. Does not update the resource instance on the client side.
     * For that you need to call {@link Scheduler#removeResource(Resource)} manually for the given instance.
     * <br><br>
     * Unsets the parent, if it matches this instance.
     *
     * @param child child resources to be removed
     * @throws NullPointerException when null is passed
     */
    public void removeChild(Resource child) {
        removeChildren(Objects.requireNonNull(child));
    }

    /**
     * Removes the given resources from this instance. Does not update the resource instance on the client side.
     * For that you need to call {@link Scheduler#removeResources(Resource...)} manually for the given instance.
     * <br><br>
     * Unsets the parent, if it matches this instance.
     *
     * @param children child resources to be removed
     * @throws NullPointerException when null is passed
     */
    public void removeChildren(Resource... children) {
        removeChildren(Arrays.asList(children));
    }

    /**
     * Removes the given resources from this instance. Does not update the resource instance on the client side.
     * For that you need to call {@link Scheduler#removeResources(Resource...)} manually for the given instance.
     * <br><br>
     * Unsets the parent, if it matches this instance.
     *
     * @param children child resources to be removed
     * @throws NullPointerException when null is passed
     */
    public void removeChildren(Collection<Resource> children) {
        if (this.children == null || this.children.isEmpty()) {
            return;
        }

        children.stream()
                .filter(child -> {
                    Optional<Resource> parent = child.getParent();
                    return parent.isPresent() && parent.get().equals(this);
                })
                .forEach(child -> {
                    child.setParent(null);
                    child.detachScheduler();
                });

        this.children.removeAll(children);
    }

    /**
     * Adds or updates a custom extended property on this resource. If this resource has been
     * added to a scheduler, the change is propagated to the client immediately.
     *
     * @param key   property name
     * @param value property value
     */
    public void addExtendedProps(String key, Object value) {
        extendedProps.put(key, value);
        pushUpdateToClient();
    }

    /**
     * Removes a custom extended property from this resource by key. If this resource has been
     * added to a scheduler, the change is propagated to the client immediately.
     *
     * @param key property name to remove
     */
    public void removeExtendedProps(String key) {
        extendedProps.remove(key);
        pushUpdateToClient();
    }

    /**
     * Removes a custom extended property from this resource only if it matches both key and value.
     * If this resource has been added to a scheduler, the change is propagated to the client immediately.
     *
     * @param key   property name to remove
     * @param value value that must match
     */
    public void removeExtendedProps(String key, Object value) {
        extendedProps.remove(key, value);
        pushUpdateToClient();
    }

    /**
     * Sets the display title of this resource. If this resource has been added to a scheduler,
     * the change is propagated to the client immediately.
     *
     * @param title new title
     */
    public void setTitle(String title) {
        this.title = title;
        pushUpdateToClient();
    }

    /**
     * Sets the event color shorthand for this resource (sets both background and border color of
     * events associated with this resource). If this resource has been added to a scheduler,
     * the change is propagated to the client immediately.
     * <p>
     * To control background and border colors independently, use
     * {@link #setEntryBackgroundColor(String)} and {@link #setEntryBorderColor(String)}.
     *
     * @param color CSS color string (e.g., {@code "#3788d8"}, {@code "blue"})
     */
    public void setColor(String color) {
        this.color = color;
        pushUpdateToClient();
    }

    /**
     * Sets the background color for entries associated with this resource.
     * Overrides the {@link #setColor(String) eventColor} shorthand for background color.
     * <p>
     * Unlike {@link #setTitle(String)} and {@link #setColor(String)}, this change is NOT
     * automatically propagated to the client. Call {@link Scheduler#updateResource(Resource)}
     * on the scheduler after modifying entry style properties.
     *
     * @param color CSS color string
     */
    public void setEntryBackgroundColor(String color) {
        this.eventBackgroundColor = color;
    }

    /**
     * Returns the entry background color override for this resource, or {@code null} if not set.
     *
     * @return CSS color string or null
     */
    public String getEntryBackgroundColor() {
        return eventBackgroundColor;
    }

    /**
     * Sets the border color for entries associated with this resource.
     * Overrides the {@link #setColor(String) eventColor} shorthand for border color.
     * <p>
     * Unlike {@link #setTitle(String)} and {@link #setColor(String)}, this change is NOT
     * automatically propagated to the client. Call {@link Scheduler#updateResource(Resource)}
     * on the scheduler after modifying entry style properties.
     *
     * @param color CSS color string
     */
    public void setEntryBorderColor(String color) {
        this.eventBorderColor = color;
    }

    /**
     * Returns the entry border color override for this resource, or {@code null} if not set.
     *
     * @return CSS color string or null
     */
    public String getEntryBorderColor() {
        return eventBorderColor;
    }

    /**
     * Sets the text color for entries associated with this resource.
     * <p>
     * Unlike {@link #setTitle(String)} and {@link #setColor(String)}, this change is NOT
     * automatically propagated to the client. Call {@link Scheduler#updateResource(Resource)}
     * on the scheduler after modifying entry style properties.
     *
     * @param color CSS color string
     */
    public void setEntryTextColor(String color) {
        this.eventTextColor = color;
    }

    /**
     * Returns the entry text color override for this resource, or {@code null} if not set.
     *
     * @return CSS color string or null
     */
    public String getEntryTextColor() {
        return eventTextColor;
    }

    /**
     * Sets a constraint for entries associated with this resource. Accepts a business hours
     * object ID, a named entry groupId, or a special constraint object string.
     *
     * @param constraint constraint string
     * @see <a href="https://fullcalendar.io/docs/eventConstraint">FullCalendar eventConstraint</a>
     */
    public void setEntryConstraint(String constraint) {
        this.eventConstraint = constraint;
    }

    /**
     * Returns the entry constraint for this resource, or {@code null} if not set.
     *
     * @return constraint string or null
     */
    public String getEntryConstraint() {
        return eventConstraint;
    }

    /**
     * Sets whether entries associated with this resource can overlap other entries.
     * Use {@code null} to inherit the calendar-level default.
     *
     * @param overlap {@code true} to allow overlap, {@code false} to prevent, {@code null} to inherit
     * @see <a href="https://fullcalendar.io/docs/eventOverlap">FullCalendar eventOverlap</a>
     */
    public void setEntryOverlap(Boolean overlap) {
        this.eventOverlap = overlap;
    }

    /**
     * Returns the entry overlap setting for this resource, or {@code null} if not set (inherits calendar default).
     *
     * @return Boolean overlap setting or null
     */
    public Boolean getEntryOverlap() {
        return eventOverlap;
    }

    /**
     * Sets CSS class names to be applied to entries associated with this resource.
     * <p>
     * Unlike {@link #setTitle(String)} and {@link #setColor(String)}, this change is NOT
     * automatically propagated to the client. Call {@link Scheduler#updateResource(Resource)}
     * on the scheduler after modifying entry style properties.
     *
     * @param classNames set of CSS class names; {@code null} clears the setting
     */
    public void setEntryClassNames(Set<String> classNames) {
        this.eventClassNames = classNames != null ? new LinkedHashSet<>(classNames) : null;
    }

    /**
     * Returns an unmodifiable view of the entry class names for this resource,
     * or {@code null} if not set.
     *
     * @return unmodifiable set of class names or null
     */
    public Set<String> getEntryClassNames() {
        return eventClassNames != null ? Collections.unmodifiableSet(eventClassNames) : null;
    }

    /**
     * Sets a per-resource {@code eventAllow} JS callback that controls where entries associated with
     * this resource can be dropped. Receives {@code (dropInfo, draggedEvent)} and returns a boolean.
     *
     * @param jsFunction JS function string, or {@code null} to clear
     * @see <a href="https://fullcalendar.io/docs/eventAllow">FullCalendar eventAllow</a>
     */
    public void setEntryAllow(String jsFunction) {
        this.eventAllow = JsCallback.of(jsFunction);
    }

    /**
     * Sets a per-resource {@code eventAllow} JS callback.
     *
     * @param callback JsCallback, or {@code null} to clear
     * @see <a href="https://fullcalendar.io/docs/eventAllow">FullCalendar eventAllow</a>
     */
    public void setEntryAllow(JsCallback callback) {
        this.eventAllow = callback;
    }

    /**
     * Returns the per-resource {@code eventAllow} JS callback, or {@code null} if not set.
     *
     * @return JsCallback or null
     */
    public JsCallback getEntryAllow() {
        return eventAllow;
    }

    /**
     * Returns all entries currently associated with this resource. Only works when this resource
     * has been added to a {@link FullCalendarScheduler} that uses an
     * {@link org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider}; returns an empty
     * set for callback-based providers (entries are fetched lazily and cannot be enumerated).
     *
     * @return unmodifiable set of entries assigned to this resource; empty if not attached or provider is not in-memory
     */
    public Set<ResourceEntry> getEvents() {
        if (scheduler == null || !scheduler.getEntryProvider().isInMemory()) {
            return Collections.emptySet();
        }
        Set<ResourceEntry> result = new LinkedHashSet<>();
        scheduler.getEntryProvider().asInMemory().getEntries().stream()
                .filter(e -> e instanceof ResourceEntry)
                .map(e -> (ResourceEntry) e)
                .filter(e -> e.getResourcesOrEmpty().contains(this))
                .forEach(result::add);
        return Collections.unmodifiableSet(result);
    }

    /**
     * Attaches this resource to the given scheduler for automatic client-side push updates.
     * Called when the resource is added to a {@link FullCalendarScheduler}.
     * <p>
     * {@apiNote Internal — called by {@link FullCalendarScheduler} during resource registration.
     * Do not call from application code.}
     *
     * @param scheduler the scheduler this resource belongs to
     */
    void attachScheduler(FullCalendarScheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Detaches this resource from its scheduler. Called when the resource is removed
     * from a {@link FullCalendarScheduler}.
     * <p>
     * {@apiNote Internal — called by {@link FullCalendarScheduler} during resource removal.
     * Do not call from application code.}
     */
    void detachScheduler() {
        this.scheduler = null;
    }

    /**
     * Pushes a full resource update to the client if this resource is currently attached
     * to a scheduler that is also attached to the UI.
     */
    private void pushUpdateToClient() {
        if (scheduler != null && scheduler.isAttached()) {
            scheduler.updateResource(this);
        }
    }

    /**
     * Converts the instance to a JsonObject. Calls itself also for child methods. Please be aware, that this method
     * <b>does not check</b> for potential hierarchial loops (e. g. infinite loops), this has to be done manually before.
     *
     * @return json object
     */
    protected ObjectNode toJson() {
        ObjectNode jsonObject = JsonFactory.createObject();

        jsonObject.put("id", getId());
        jsonObject.set("title", JsonUtils.toJsonNode(getTitle()));
        jsonObject.set("eventColor", JsonUtils.toJsonNode(getColor()));

        BusinessHours[] businessHours = getBusinessHoursArray();
        if(businessHours != null && businessHours.length > 0) {
            ArrayNode businessHoursJsonArray = JsonFactory.createArray();
            for (BusinessHours hour : businessHours) {
                businessHoursJsonArray.add(hour.toJson());
            }
            jsonObject.set("businessHours", businessHoursJsonArray);
        }

        getParent().ifPresent(parent -> jsonObject.put("parentId", parent.getId()));

        Set<Resource> children = getChildren();
        if (!children.isEmpty()) {
            ArrayNode jsonArray = JsonFactory.createArray();

            for (Resource child : children) {
                jsonArray.add(child.toJson());
            }

            jsonObject.set("children", jsonArray);
        }

        if (eventBackgroundColor != null) jsonObject.put("eventBackgroundColor", eventBackgroundColor);
        if (eventBorderColor != null) jsonObject.put("eventBorderColor", eventBorderColor);
        if (eventTextColor != null) jsonObject.put("eventTextColor", eventTextColor);
        if (eventConstraint != null) jsonObject.put("eventConstraint", eventConstraint);
        if (eventOverlap != null) jsonObject.put("eventOverlap", eventOverlap);
        if (eventClassNames != null && !eventClassNames.isEmpty()) {
            ArrayNode classNamesArray = JsonFactory.createArray();
            eventClassNames.forEach(classNamesArray::add);
            jsonObject.set("eventClassNames", classNamesArray);
        }
        if (eventAllow != null) jsonObject.set("eventAllow", eventAllow.toMarkerJson());

        HashMap<String, Object> extendedProps = getExtendedProps();
        if (!extendedProps.isEmpty()) {
            for (Map.Entry<String, Object> prop : extendedProps.entrySet()) {
            	jsonObject.set(prop.getKey(), JsonUtils.toJsonNode(prop.getValue()));
            }
        }

        return jsonObject;
    }

    /**
     * Returns the parent resource (or empty if top level).
     *
     * @return parent or empty
     */
    public Optional<Resource> getParent() {
        return Optional.ofNullable(parent);
    }

    /**
     * Used by {@link #addChildren(Collection)}. Currently no need to use it elsewhere.
     *
     * @param parent parent
     */
    private void setParent(Resource parent) {
        this.parent = parent;
    }

    /**
     * Returns the first item from businessHoursArray or null if array is empty
     *
     * @return BusinessHours or null
     */
    public BusinessHours getBusinessHours() {
        if(businessHoursArray == null || businessHoursArray.length == 0) return null;

        return businessHoursArray[0];
    }

    @Override
    public String toString() {
        String s = "Resource{" +
                "title='" + title + '\'' +
                ", color='" + color + '\'' +
                ", id='" + id + '\'' +
                ", children='" + children + '\'';

        if (parent != null) {
            s += "parentId = '" + parent.getId() + '\'';
        }

        return s + '}';
    }


}
