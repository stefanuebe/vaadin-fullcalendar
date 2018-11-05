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
    private String title;

    /**
     * New instance.
     */
    public Resource() {
        this(null);
    }

    /**
     * New instance. Awaits id and title.
     * @param id id
     * @param title title
     */
    public Resource(String id, String title) {
        this(id);
        this.title = title;
    }

    /**
     * New instance. Awaits id.
     *
     * @param id id
     */
    protected Resource(String id) {
        this.id = id != null ? id : UUID.randomUUID().toString();
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

    public void setTitle(String title) {
        this.title = title;
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
        jsonObject.put("title", getTitle());

        return jsonObject;
    }
}
