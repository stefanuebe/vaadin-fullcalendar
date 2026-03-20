package org.vaadin.stefan.fullcalendar.json;

import java.lang.annotation.*;

/**
 * Marks a field to be read directly via reflection instead of through a getter method.
 * <p>
 * By default, {@code BeanProperties} requires a public getter (e.g. {@code getFieldName()})
 * to access a field's value for JSON serialization. If this annotation is present, the field
 * is accessed directly — regardless of its visibility — so no public getter is needed.
 * <p>
 * Useful for fields that are part of the JSON model but intentionally have no public getter
 * in the Java API (e.g. internal serialization-only fields).
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JsonReadField {
}
