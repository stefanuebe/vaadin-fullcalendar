package org.vaadin.stefan.fullcalendar.json;

import java.lang.annotation.*;

/**
 * Defines an alternative json key for the property, when mapping it to json. By default the property name will
 * be used.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JsonName {

    String value();

}
