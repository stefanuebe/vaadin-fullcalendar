package org.vaadin.stefan.fullcalendar.json;

import java.lang.annotation.*;

/**
 * Marks a property to be completely ignored, when converting the object to or from json. Any other
 * json related annotation will be ignored.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JsonIgnore {
}
