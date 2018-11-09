package org.vaadin.stefan.fullcalendar;

import elemental.json.Json;
import elemental.json.JsonObject;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a resource.
 */
public class Resource {
    private final String id;
    private final String title;
    private final String color;

    /**
     * New instance. ID will be generated.
     */
    public Resource() {
        this(null, null, null);
    }

    /**
     * New instance. Awaits id and title. If no id is provided, one will be generated.
     * @param id id
     * @param title title
     * @param color color (optional)
     */
    public Resource(String id, String title, String color) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.title = title;
        this.color = color;
    }

    /**
     * Returns the id of this instance.
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the title.
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the resource's color.
     * @return color
     */
    public String getColor() {
        return color;
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

    protected JsonObject toJson() {
        JsonObject jsonObject = Json.createObject();

        jsonObject.put("id", getId());
        jsonObject.put("title", JsonUtils.toJsonValue(getTitle()));
        jsonObject.put("eventColor", JsonUtils.toJsonValue(getColor()));

        return jsonObject;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "title='" + title + '\'' +
                ", color='" + color + '\'' +
                ", id='" + id + '\'' +
                '}';
    }


}
