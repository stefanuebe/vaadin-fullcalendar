package org.vaadin.stefan.fullcalendar.json;

import org.vaadin.stefan.fullcalendar.converters.JsonItemPropertyConverter;

import java.lang.annotation.*;

/**
 * @author Stefan Uebe
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JsonConverter {
    Class<? extends JsonItemPropertyConverter> value();

}
