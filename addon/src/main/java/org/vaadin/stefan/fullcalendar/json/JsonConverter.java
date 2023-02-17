package org.vaadin.stefan.fullcalendar.json;

import elemental.json.JsonValue;
import org.vaadin.stefan.fullcalendar.JsonUtils;
import org.vaadin.stefan.fullcalendar.converters.JsonItemPropertyConverter;

import java.lang.annotation.*;

/**
 * Defines a converter, that should be used to convert the property value to json and, if necessary, vice
 * versa. By default, the {@link JsonUtils#toJsonValue(Object)} and {@link JsonUtils#ofJsonValue(JsonValue)} methods
 * are used.<br>
 * <br>
 * Please note, that the conver needs to implement the {@link JsonItemPropertyConverter#toServerModel(JsonValue, Object)}
 * method only, when the @{@link JsonUpdateAllowed} annotation is used..
 *
 * @see JsonItemPropertyConverter
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JsonConverter {

    @SuppressWarnings("rawtypes")
    Class<? extends JsonItemPropertyConverter> value();

}
