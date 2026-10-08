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

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a resource. ResourceEntries contain these resources (a resource itself does not know anything about
 * the assigned entries). A resource can have sub resources / child resources.
 * <p>
 * Resources can carry per-resource entry style overrides ({@link #setColor(String)},
 * {@link #setEntryContrastColor(String)} and {@link #setEntryClassNames(java.util.Set)}) that apply to all
 * entries associated with the resource.
 */
@Getter
@EqualsAndHashCode(of = "id")
public class Resource implements Serializable {

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
     * The color of this resource, sent as FullCalendar's {@code eventColor}.
     * Entries associated with this resource use it. FullCalendar sets it on each entry's element as the CSS
     * variable {@code --fc-event-color}. The theme's styles read it, so where the color shows depends on the
     * theme. See {@link Entry#setColor(String)}.
     */
    private String color;

    /**
     * The BusinessHours array of this resource.
     * A businessHours[] declaration that will only apply to this resource.
     */
    private final BusinessHours[] businessHoursArray;

    /**
     * The children of the resource
     */
    private Set<Resource> children;
    /**
     * The parent of the current resource
     */
    private Resource parent;

    /**
     * The extended props, sent flat on the top level of the resource JSON
     */
    @Getter(AccessLevel.NONE)
    private final Map<String, Object> extendedProps = new HashMap<>();

    // Per-resource entry style overrides
    @Getter(AccessLevel.NONE)
    private String eventContrastColor;
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
     * Sets an extended prop. An extended prop is an additional value of your own, stored under a key of your
     * choice. Extended props do not change the regular properties of the resource, such as its title or color, and
     * FullCalendar does not use them itself. Setting a key again overwrites the extended prop stored under that key.
     * <p>
     * On the client, your own JavaScript callbacks can read it as {@code resource.extendedProps.<key>}. If this
     * resource has been added to a scheduler, the change is propagated to the client immediately.
     * Objects other than strings, numbers, booleans, maps, collections and arrays are serialized with Jackson.
     * Numbers other than {@code Integer} and {@code Long} are sent as doubles.
     *
     * @param key   property name
     * @param value property value, can be null
     */
    public void setExtendedProp(String key, Object value) {
        extendedProps.put(Objects.requireNonNull(key), value);
        pushUpdateToClient();
    }

    /**
     * Removes an extended prop from this resource. If this resource has been added to a scheduler, the change is
     * propagated to the client immediately. FullCalendar cannot delete an extended prop of a shown resource, so
     * the client keeps the key with the value {@code undefined}.
     *
     * @param key property name to remove
     */
    public void removeExtendedProp(String key) {
        extendedProps.remove(Objects.requireNonNull(key));
        pushUpdateToClient();
    }

    /**
     * Replaces the extended props of this resource with the entries of the given map. {@code null} clears them.
     * If this resource has been added to a scheduler, the change is propagated to the client immediately. Like
     * {@link #removeExtendedProp(String)}, a key that is no longer set stays on the client with the value
     * {@code undefined}.
     *
     * @param extendedProps extended props
     * @throws NullPointerException when the map contains a null key
     * @see #setExtendedProp(String, Object)
     */
    public void setExtendedProps(Map<String, Object> extendedProps) {
        Map<String, Object> copy = extendedProps != null ? new HashMap<>(extendedProps) : new HashMap<>();
        if (copy.containsKey(null)) {
            throw new NullPointerException("Extended props must not contain a null key");
        }

        this.extendedProps.clear();
        this.extendedProps.putAll(copy);
        pushUpdateToClient();
    }

    /**
     * Returns whether an extended prop is set under the given key, also when its value is null.
     *
     * @param key name of the extended prop
     * @return true if the key is set
     */
    public boolean hasExtendedProp(String key) {
        return extendedProps.containsKey(key);
    }

    /**
     * Returns the extended props of this resource as a read-only view. Never null. Change them with
     * {@link #setExtendedProp(String, Object)}, {@link #removeExtendedProp(String)} or
     * {@link #setExtendedProps(Map)}.
     *
     * @return extended props, not modifiable
     */
    public Map<String, Object> getExtendedProps() {
        return Collections.unmodifiableMap(extendedProps);
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
     * Sets the entry color for this resource ({@code eventColor}). Entries associated with this resource use it.
     * FullCalendar sets it on each entry's element as the CSS variable {@code --fc-event-color}, and the theme's
     * styles decide where it shows, see {@link Entry#setColor(String)}. If this resource has been added to a scheduler,
     * the change is sent to the client immediately. Entries do not repaint from it, see
     * {@link Scheduler#updateResource(Resource)}.
     *
     * @param color CSS color string (e.g., {@code "#3788d8"}, {@code "blue"})
     */
    public void setColor(String color) {
        this.color = color;
        pushUpdateToClient();
    }

    /**
     * Sets the contrast color for entries associated with this resource ({@code eventContrastColor}), used for
     * text and other content drawn on top of the entry color. FullCalendar sets it on each entry's element as the
     * CSS variable {@code --fc-event-contrast-color}. The theme's styles read it, so where the color shows
     * depends on the theme.
     * <p>
     * Unlike {@link #setTitle(String)} and {@link #setColor(String)}, this change is NOT
     * automatically propagated to the client. Call {@link Scheduler#updateResource(Resource)}
     * on the scheduler after modifying entry style properties.
     *
     * @param color CSS color string
     */
    public void setEntryContrastColor(String color) {
        this.eventContrastColor = color;
    }

    /**
     * Returns the entry contrast color override for this resource, or {@code null} if not set.
     *
     * @return CSS color string or null
     */
    public String getEntryContrastColor() {
        return eventContrastColor;
    }

    /**
     * Former name of {@link #setEntryContrastColor(String)}: sets the contrast color for entries associated
     * with this resource. Delegates to it, so the change is likewise not propagated to the client automatically.
     *
     * @param color CSS color string
     * @deprecated use {@link #setEntryContrastColor(String)}. FullCalendar 7 calls it the contrast color
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public void setEntryTextColor(String color) {
        setEntryContrastColor(color);
    }

    /**
     * Former name of {@link #getEntryContrastColor()}: returns the entry contrast color override for this
     * resource, or {@code null} if not set. Delegates to it.
     *
     * @return CSS color string or null
     * @deprecated use {@link #getEntryContrastColor()}. FullCalendar 7 calls it the contrast color
     */
    @Deprecated(since = "8.0.0", forRemoval = true)
    public String getEntryTextColor() {
        return getEntryContrastColor();
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
     * Sets CSS class names to be applied to entries associated with this resource. They are sent as one
     * space-separated {@code eventClass} string.
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
     * Returns all entries currently associated with this resource. Works with any
     * {@link org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider} type (in-memory,
     * callback, or signal-based).
     * <p>
     * Note: For callback-based providers this fetches all entries without a time range filter,
     * which may be expensive. Consider using the entry provider's {@code fetch()} method
     * with appropriate filters if performance is a concern.
     *
     * @return unmodifiable set of entries assigned to this resource; empty if not attached
     */
    public Set<ResourceEntry> getEntries() {
        if (scheduler == null) {
            return Collections.emptySet();
        }
        return scheduler.getEntryProvider().fetchAll()
                .filter(e -> e instanceof ResourceEntry)
                .map(e -> (ResourceEntry) e)
                .filter(e -> e.getResourcesOrEmpty().contains(this))
                .collect(Collectors.toCollection(LinkedHashSet::new));
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
     * Converts this resource to a JSON object. Recursively converts child resources. Please be aware that this method
     * <b>does not check</b> for potential hierarchical loops (e.g. infinite loops); this has to be prevented manually.
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

        if (eventContrastColor != null) jsonObject.put("eventContrastColor", eventContrastColor);
        if (eventConstraint != null) jsonObject.put("eventConstraint", eventConstraint);
        if (eventOverlap != null) jsonObject.put("eventOverlap", eventOverlap);
        if (eventClassNames != null && !eventClassNames.isEmpty()) {
            jsonObject.put("eventClass", String.join(" ", eventClassNames));
        }
        if (eventAllow != null) jsonObject.set("eventAllow", eventAllow.toMarkerJson());

        for (Map.Entry<String, Object> prop : extendedProps.entrySet()) {
            jsonObject.set(prop.getKey(), JsonUtils.toJsonNodeWithJackson(prop.getValue()));
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
