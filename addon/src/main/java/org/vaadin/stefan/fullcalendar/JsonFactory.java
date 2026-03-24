package org.vaadin.stefan.fullcalendar;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;

/**
 * Factory to create new objects. Wraps the current json framework.
 */
public class JsonFactory {
    private JsonFactory() {

    }

    /**
     * Creates an empty array node.
     * @return array node
     */
    public static JsonArray createArray() {
        return Json.createArray();
    }

    /**
     * Creates an empty object node.
     * @return object node
     */
    public static JsonObject createObject() {
        return Json.createObject();
    }

    /**
     * Creates a number value with the given double.
     * @param value double value
     * @return json number
     */
    public static JsonValue create(double value) {
        return Json.create(value);
    }

    /**
     * Creates a number value with the given integer.
     * @param value int value
     * @return json number
     */
    public static JsonValue create(int value) {
        return Json.create((double) value);
    }

    /**
     * Creates a number value with the given long.
     * @param value long value
     * @return json number
     */
    public static JsonValue create(long value) {
        return Json.create((double) value);
    }

    /**
     * Creates a string value with the given text.
     * @param value text
     * @return json string
     */
    public static JsonValue create(String value) {
        return Json.create(value);
    }

    /**
     * Creates a boolean value.
     * @param value value
     * @return json boolean
     */
    public static JsonValue create(boolean value) {
        return Json.create(value);
    }

    /**
     * Creates a value that represents null.
     * @return json null
     */
    public static JsonValue createNull() {
        return Json.createNull();
    }
}
