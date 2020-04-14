/*
 * Copyright 2018, Stefan Uebe
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

import java.util.*;

/**
 * Represents a resource. ResourceEntries contain these resources (a resource itself does not know anything about
 * the assigned entries). A resource can have sub resources / child resources.
 */
public class Resource {
    private final String id;
    private final String title;
    private final String color;
    private Set<Resource> children;
    private Resource parent;

    /**
     * New instance. ID will be generated.
     */
    public Resource() {
        this(null, null, null);
    }

    /**
     * New instance. Awaits id and title. If no id is provided, one will be generated.
     * Children list will be initialized to empty ArrayList<Resource>
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
     * <p/>
     * Adds the given resources as children using {@link #addChildren(Collection)} if a value != null is passed.
     *
     * @param id       id
     * @param title    title
     * @param color    color (optional)
     * @param children children (optional)
     */
    public Resource(String id, String title, String color, Collection<Resource> children) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.title = title;
        this.color = color;

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
     * Adds the given resources as children to this instance. Does not check, if the resources have been
     * added to other resources or entries before.
     * <p/>
     * Does also not update the resource instance on the client side. If you want to add child ressources to
     * already existing resources, you also have to register them manually in the client
     * using {@link Scheduler#addResources(Resource...)}
     * <p/>
     * Sets the parent for each child resource to this instance. If you move resource from another parent, remove
     * them there first.
     *
     * @param children resources to be added as children
     */
    public void addChildren(Collection<Resource> children) {
        if (this.children == null) {
            this.children = new HashSet<>(children);
        } else {
            this.children.addAll(children);
        }

        children.forEach(child -> child.setParent(this));
    }

    /**
     * Adds the given resource as child to this instance. Does not check, if the resource has been
     * added to other resources or entries before. Does also not update the resource instance on the client side.
     * If you want to add child ressources to
     * already existing resources, you also have to register them manually in the client
     * using {@link Scheduler#addResources(Resource...)}
     *
     * @param child resource to be added as child
     */
    public void addChild(Resource child) {
        addChildren(Collections.singleton(child));
    }

    /**
     * Removes the given resource from this instance. Does not update the resource instance on the client side. For
     * that you need to call {@link Scheduler#removeResources(Resource...)} manually for the given instance.
     *
     * @param child child resource to be removed
     */
    public void removeChild(Resource child) {
        removeChildren(Collections.singleton(child));
    }

    /**
     * Removes the given resources from this instance. Does not update the resource instance on the client side.
     * For that you need to call {@link Scheduler#removeResources(Resource...)} manually for the given instance.
     * <p/>
     * Unsets the parent, if it matches this instance.
     *
     * @param children child resources to be removed
     */
    public void removeChildren(Collection<Resource> children) {
        if (this.children != null) {
            children.stream()
                    .filter(child -> {
                        Optional<Resource> parent = child.getParent();
                        return parent.isPresent() && parent.get().equals(this);
                    })
                    .forEach(child -> child.setParent(null));

            this.children.removeAll(children);
        }
    }

    /**
     * Returns the id of this instance.
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the title.
     *
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the resource's color.
     *
     * @return color
     */
    public String getColor() {
        return color;
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Resource resource = (Resource) o;
        return Objects.equals(id, resource.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
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
        getParent().ifPresent(parent -> jsonObject.put("parentId", parent.getId()));

        Set<Resource> children = getChildren();
        if (!children.isEmpty()) {
            JsonArray jsonArray = Json.createArray();

            for (Resource child : children) {
                jsonArray.set(jsonArray.length(), child.toJson());
            }

            jsonObject.put("children", jsonArray);
        }

        return jsonObject;
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
