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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a resource.
 */
public class Resource {
    private final String id;
    private final String title;
    private final String color;
    private List<Resource> children;

    /**
     * New instance. ID will be generated.
     */
    public Resource() {
        this(null, null, null, new ArrayList<Resource>());
    }
    
    /**
     * New instance. Awaits id and title. If no id is provided, one will be generated.
     * Children list will be initialized to empty ArrayList<Resource>
     * 
     * @param id id
     * @param title title
     * @param color color (optional)
     */
    public Resource(String id, String title, String color) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.title = title;
        this.color = color;
        this.children = new ArrayList<Resource>();
    }
    
    /**
     * New instance. Awaits id and title. If no id is provided, one will be generated.
     * 
     * @param id id
     * @param title title
     * @param color color (optional)
     * @param children children (optional)
     */
    public Resource(String id, String title, String color, ArrayList<Resource> children) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.title = title;
        this.color = color;
        this.children = children;
    }
    
    /**
     * Returns the resource's children list.
     * @return children
     */
	public List<Resource> getChildren() {
		return this.children;
	}
	
	/**
     * Add the children to the childrens list
     * @param children
     */
	public void addChildren(Resource children) {
		this.children.add(children);
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
    
    /**
     * Convert the children list to JsonArray Object
     * @param childrens
     */
	protected JsonArray childrenListToJsonArray(List<Resource> children) {
		JsonArray jsonArray = Json.createArray();
		
		for(Resource child : children)
			jsonArray.set(jsonArray.length(), child.toJson());
		
		return jsonArray;
	}
    
    protected JsonObject toJson() {
        JsonObject jsonObject = Json.createObject();

        jsonObject.put("id", getId());
        jsonObject.put("title", JsonUtils.toJsonValue(getTitle()));
        jsonObject.put("eventColor", JsonUtils.toJsonValue(getColor()));
        jsonObject.put("children", childrenListToJsonArray(getChildren()));
        
        return jsonObject;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "title='" + title + '\'' + 
                ", color='" + color + '\'' +
                ", id='" + id + '\'' +
                ", children='" + children + '\'' +
                '}';
    }


}
