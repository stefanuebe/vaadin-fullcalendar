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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.vaadin.stefan.fullcalendar.JsonUtils;

import elemental.json.Json;
import elemental.json.JsonObject;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Definition of position for the header and footer.
 */
@EqualsAndHashCode
@ToString
public class Position {
	private Positions position;
	
	private Set<Options> options;
	
	public Position(Positions position) {
		this(position, new HashSet<>());
	}
	
	public Position(Positions position, Set<Options> options) {
		this.position = position;
		this.options = options;
	}
	
	public void addOption(Options option) {
		options.add(option);
	}
	
	public void removeOption(Options option) {
		options.remove(option);
	}
	
	public void clearOption(Options option) {
		options.clear();
	}
	
	public Positions getPosition() {
		return position;
	}
	
	public Set<Options> getOptions() {
		return Collections.unmodifiableSet(options);
	}
	
	/**
     * Converts the given object into a json object.
     * @return json object
     */
    protected JsonObject toJson() {
        JsonObject jsonObject = Json.createObject();

        jsonObject.put(position.getCode(), JsonUtils.toJsonValue(options.stream().map(option -> option.getCode())));

        return jsonObject;
    }
}
