package org.vaadin.stefan.fullcalendar.json;

import java.lang.annotation.*;

/**
 * Container annotation for repeatable {@link JsonConverter} annotations.
 *
 * @see JsonConverter
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JsonConverters {
    JsonConverter[] value();
}
