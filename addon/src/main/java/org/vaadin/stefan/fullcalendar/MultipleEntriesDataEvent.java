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

import lombok.Getter;
import lombok.ToString;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Extended multple entries event type, that provides also additional client side entry data, that can be
 * interpreted on the server side.
 */
@Getter
@ToString(callSuper = true)
public abstract class MultipleEntriesDataEvent extends MultipleEntriesEvent {

    /**
     * A map containing each entry's json object, which represents the sent data from the client.
     */
    private final Map<Entry, ObjectNode> jsonObjects;

    /**
     * New instance. Awaits the changed data object.
     * @param source source component
     * @param fromClient is from client
     * @param jsonObjects json object with changed data
     */
    public MultipleEntriesDataEvent(FullCalendar source, boolean fromClient, ArrayNode jsonObjects) {
        super(source, fromClient, toCollection(Objects.requireNonNull(jsonObjects)));
        this.jsonObjects = toMap(jsonObjects);
    }

    private Map<Entry, ObjectNode> toMap(ArrayNode jsonObjects) {
        Map<String, Entry> entries = getEntries().stream().collect(Collectors.toMap(Entry::getId, Function.identity()));

        Map<Entry, ObjectNode> map = new HashMap<>(jsonObjects.size());
        for (JsonNode node : jsonObjects) {
            if(node instanceof ObjectNode jsonObject) {
                Entry entry = entries.get(jsonObject.get("id").asString());
                map.put(entry, jsonObject);
            }
        }

        return map;
    }

    private static Collection<String> toCollection(ArrayNode jsonObjects) {
        Set<String> ids = new HashSet<>(jsonObjects.size());
        for (JsonNode node : jsonObjects) {
            if (!(node instanceof ObjectNode objectNode)) {
                throw new IllegalArgumentException("Only json objects are allowed as direct children");
            }
            String id = (objectNode).get("id").asString();
            if (id == null) {
                throw new IllegalArgumentException("Only valid entry objects are allowed (must have an id)");
            }

            ids.add(id);

        }
        return ids;
    }
}
