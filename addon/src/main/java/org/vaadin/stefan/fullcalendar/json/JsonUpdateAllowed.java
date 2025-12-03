package org.vaadin.stefan.fullcalendar.json;

import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.converters.JsonItemPropertyConverter;

import java.lang.annotation.*;

/**
 * Marks a property to be allowed to be updated, when calling {@link Entry#updateFromJson(tools.jackson.databind.node.ObjectNode)}.
 * Otherwise properties will be ignored. Please note, that any converters set for the marked property need to implement
 * their {@link JsonItemPropertyConverter#toServerModel(tools.jackson.databind.JsonNode, Object)}
 * method.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JsonUpdateAllowed {
}
