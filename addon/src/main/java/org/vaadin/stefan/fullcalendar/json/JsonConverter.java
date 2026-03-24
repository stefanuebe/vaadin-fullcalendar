package org.vaadin.stefan.fullcalendar.json;

import org.vaadin.stefan.fullcalendar.JsonUtils;
import org.vaadin.stefan.fullcalendar.converters.JsonItemPropertyConverter;
import elemental.json.JsonValue;

import java.lang.annotation.*;

/**
 * Defines a converter, that should be used to convert the property value to json and, if necessary, vice
 * versa. By default, the {@link JsonUtils#toJsonValue(Object)} and {@link JsonUtils#ofJsonValue(JsonValue)} methods
 * are used.<br>
 * <br>
 * Please note, that the converter needs to implement the {@link JsonItemPropertyConverter#toServerModel(JsonValue, Object)}
 * method only, when the @{@link JsonUpdateAllowed} annotation is used.
 *
 * @see JsonItemPropertyConverter
 */
@Repeatable(JsonConverters.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JsonConverter {

    @SuppressWarnings("rawtypes")
    Class<? extends JsonItemPropertyConverter> value();

}
