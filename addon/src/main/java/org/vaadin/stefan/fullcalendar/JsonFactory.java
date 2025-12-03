package org.vaadin.stefan.fullcalendar;

import tools.jackson.databind.node.*;

/**
 * Factory to create new objects. Wraps the current json framework.
 */
public class JsonFactory {
    // TODO should this be an inner class, that is only used by the json utils to have a single point of creating json nodes?
    private JsonFactory() {

    }

    /**
     * Returns the factory instance, that is used to create json nodes.
     * @return factory
     */
    public static JsonNodeFactory factory() {
        return JsonNodeFactory.instance;
    }

    /**
     * Creates an empty array node.
     * @return array node
     */
    public static ArrayNode createArray() {
        return factory().arrayNode();
    }

    /**
     * Creates an empty object node.
     * @return object node
     */
    public static ObjectNode createObject() {
        return factory().objectNode();
    }

    /**
     * Creates a number node with the given double.
     * @param value double value
     * @return number node
     */
    public static NumericNode create(double value) {
        return factory().numberNode(value);
    }

    /**
     * Creates a number node with the given integer.
     * @param value int value
     * @return number node
     */
    public static NumericNode create(int value) {
        return factory().numberNode(value);
    }

    /**
     * Creates a number node with the given long.
     * @param value long value
     * @return number node
     */
    public static NumericNode create(long value) {
        return factory().numberNode(value);
    }

    /**
     * Creates a string node with the given text.
     * @param value text
     * @return string node
     */
    public static StringNode create(String value) {
        return factory().stringNode(value);
    }

    /**
     * Creates a boolean node with the value.
     * @param value value
     * @return boolean node
     */
    public static BooleanNode create(boolean value) {
        return factory().booleanNode(value);
    }

    /**
     * Creates a node, that represents a null value.
     * @return null node
     */
    public static NullNode createNull() {
        return factory().nullNode();
    }
}