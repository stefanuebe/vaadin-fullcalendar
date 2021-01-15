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

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * Represents a resource. ResourceEntries contain these resources (a resource itself does not know anything about
 * the assigned entries). A resource can have sub resources / child resources.
 */
@Getter
@EqualsAndHashCode(of = "id")
public class Resource {

    /**
     * The id of this resource.
     * 
     * Uniquely identifies this resource. 
     */
    private final String id;

    /**
     * The title/name of this resource.
     * 
     * Text that will be displayed on the resource when it is rendered.
     */
    private final String title;

    /**
     * The color of this resource.
     * 
     * Events associated with this resources will have their backgrounds and borders colored. 
     */
    private final String color;

    /**
     * The BusinessHours of this resource.
     * 
     * A businessHours declaration that will only apply to this resource.
     */
    private final BusinessHours businessHours;
    
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
    private HashMap<String, Object> extendedProps = new HashMap<String, Object>();

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
     */
    public Resource(String id, String title, String color, Collection<Resource> children) {
    	this(id, title, color, children, null);
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
    public Resource(String id, String title, String color, Collection<Resource> children, BusinessHours businessHours) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.title = title;
        this.color = color;
        this.businessHours = businessHours;

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
    public void addChild(@NotNull Resource child) {
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
    public void addChildren(@NotNull Collection<Resource> children) {
        Objects.requireNonNull(children);

        if (this.children == null) {
            this.children = new LinkedHashSet<>(children);
        } else {
            this.children.addAll(children);
        }

        children.forEach(child -> {
            child.getParent().ifPresent(p -> p.children.remove(child)); // faster, but keep an eye on the removal to not miss anything later here
            child.setParent(this);
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
    public void addChildren(@NotNull Resource... children) {
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
    public void removeChild(@NotNull Resource child) {
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
    public void removeChildren(@NotNull Resource... children) {
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
    public void removeChildren(@NotNull Collection<Resource> children) {
        children.stream()
                .filter(child -> {
                    Optional<Resource> parent = child.getParent();
                    return parent.isPresent() && parent.get().equals(this);
                })
                .forEach(child -> child.setParent(null));

        this.children.removeAll(children);
    }
    
    /**
     * Add custom element to the extendedProp HashMap. This allow to set custom property to the resource.
     *
     *@param key String the name of the property to add
     *@param value Object the object to add
     */
    public void addExtendedProps(@NotNull String key, @NotNull Object value) {
    	extendedProps.put(key, value);
    }
    
    /**
     * Remove the custom property based on the name.
     *
     *@param key String the name of the property to remove
     */
    public void removeExtendedProps(@NotNull String key) {
    	extendedProps.remove(key);
    }
    
    /**
     * remove specific custom property where the name and value match.
     *
     *@param key String the name of the property to remove
     *@param value Object the object to remove
     */
    public void removeExtendedProps(@NotNull String key, @NotNull Object value) {
    	extendedProps.remove(key, value);
    }

    /**
     * Converts the instance to a JsonObject. Calls itself also for child methods. Please be aware, that this method
     * <b>does not check</b> for potential hierarchial loops (e. g. infinite loops), this has to be done manually before.
     *
     * @return json object
     */
    protected JsonObject toJson() {
        JsonObject jsonObject = Json.createObject();

        jsonObject.put("id", getId());
        jsonObject.put("title", JsonUtils.toJsonValue(getTitle()));
        jsonObject.put("eventColor", JsonUtils.toJsonValue(getColor()));
        
        if(getBusinessHours() != null)
        	jsonObject.put("businessHours", getBusinessHours().toJson());
        
        getParent().ifPresent(parent -> jsonObject.put("parentId", parent.getId()));

        Set<Resource> children = getChildren();
        if (!children.isEmpty()) {
            JsonArray jsonArray = Json.createArray();

            for (Resource child : children) {
                jsonArray.set(jsonArray.length(), child.toJson());
            }

            jsonObject.put("children", jsonArray);
        }
        
        HashMap<String, Object> extendedProps = getExtendedProps();
        if (!extendedProps.isEmpty()) {
            for (Map.Entry<String, Object> prop : extendedProps.entrySet()) {
            	jsonObject.put(prop.getKey(), JsonUtils.toJsonValue(prop.getValue()));
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
