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
package org.vaadin.stefan.fullcalendar.model;

import elemental.json.Json;
import elemental.json.JsonObject;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.vaadin.stefan.fullcalendar.JsonUtils;

import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * Definition of a part of the header and footer.
 */
@EqualsAndHashCode
@ToString
public class HeaderFooterPart {
	private final HeaderFooterPartPosition position;
	private final LinkedHashSet<HeaderFooterItem> items;

	/**
	 * Creates a new instance for the given position.
	 * @param position position where this part shall be shown
	 */
	public HeaderFooterPart(@NotNull HeaderFooterPartPosition position) {
		this(position, null);
	}

	/**
	 * Creates a new instance for the given position with the given items to show.
	 * @param position position where this part shall be shown
	 * @param items items to show
	 */
	public HeaderFooterPart(@NotNull HeaderFooterPartPosition position, Collection<HeaderFooterItem> items) {
		this.position = Objects.requireNonNull(position);
		this.items = items != null ? new LinkedHashSet<>(items) : new LinkedHashSet<>();
	}

	/**
	 * Item to add to this part.
	 * @param item item
	 */
	public void addItem(@NotNull HeaderFooterItem item) {
		items.add(Objects.requireNonNull(item));
	}

	/**
	 * Removes the given item from this part. Noop if the item has not been added.
	 * @param item item to be removed
	 */
	public void removeItem(HeaderFooterItem item) {
		items.remove(item);
	}

	/**
	 * Removes all items
	 */
	public void removeItems() {
		items.clear();
	}

	/**
	 * Returns the position of this instance.
	 * @return
	 */
	public HeaderFooterPartPosition getPosition() {
		return position;
	}

	/**
	 * Returns all added items.
	 * @return items
	 */
	public Set<HeaderFooterItem> getItems() {
		return Collections.unmodifiableSet(items);
	}
	
	/**
     * Converts the given object into a json object.
     * @return json object
     */
    protected JsonObject toJson() {
        JsonObject jsonObject = Json.createObject();

        jsonObject.put(position.getCode(), JsonUtils.toJsonValue(items.stream().map(option -> option.getCode())));

        return jsonObject;
    }
}
