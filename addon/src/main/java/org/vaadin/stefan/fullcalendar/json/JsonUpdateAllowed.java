package org.vaadin.stefan.fullcalendar.json;

import elemental.json.JsonObject;
import elemental.json.JsonValue;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.converters.JsonItemPropertyConverter;

import java.lang.annotation.*;

/**
 * Marks a property to be allowed to be updated, when calling {@link Entry#updateFromJson(JsonObject)}. Otherwise
 * properties will be ignored. Please note, that any converters set for the marked property need to implement
 * their {@link JsonItemPropertyConverter#toServerModel(JsonValue, Object)}
 * method.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JsonUpdateAllowed {
}
