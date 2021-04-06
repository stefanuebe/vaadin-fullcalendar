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

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

/**
 * Definition of header for a calendar instance.
 */
@EqualsAndHashCode
@ToString
public class Footer {
	private Set<Position> positions;
	
    /**
     * Creates a new instance.
     */
    public Footer() {
    	this(new HashSet<>());
    }
    
    public Footer(Set<Position> positions) {
    	this.positions = positions;
    }

    /**
     * Returns the end time or empty if none was set.
     *
     * @return end time or empty
     */
    public void addPosition(@NotNull Position position) {
        Objects.requireNonNull(position);
        
        if(positions.contains(position))
        	return;
        
        positions.add(position);
    }
    
    public Set<Position> getPositions() {
    	return Collections.unmodifiableSet(positions);
    }
    
    /**
     * Converts the given object into a json object.
     * @return json object
     */
    public JsonObject toJson() {
        JsonObject jsonObject = Json.createObject();
        
        for (Position position : positions)
        	jsonObject.put(position.getPosition().getCode(), position.getOptions().stream().map(o -> o.getCode()).collect(Collectors.joining(",")));

        return jsonObject;
    }
}
