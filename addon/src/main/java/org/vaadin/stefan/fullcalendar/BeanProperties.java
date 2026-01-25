package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.data.binder.Setter;
import com.vaadin.flow.function.ValueProvider;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.vaadin.stefan.fullcalendar.converters.JsonItemPropertyConverter;
import org.vaadin.stefan.fullcalendar.json.JsonConverter;
import org.vaadin.stefan.fullcalendar.json.JsonIgnore;
import org.vaadin.stefan.fullcalendar.json.JsonName;
import org.vaadin.stefan.fullcalendar.json.JsonUpdateAllowed;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Stefan Uebe
 */
@Getter
public class BeanProperties<T> {

    private final Field field;
    private final ValueProvider<T, Object> getter;
    private final Setter<T, Object> setter;

    // Cached annotation information for performance
    private final boolean jsonIgnored;
    private final boolean jsonUpdateAllowed;
    private final String jsonName;
    private final JsonItemPropertyConverter<?, ?> converter;

    @SuppressWarnings("unchecked")
    private BeanProperties(Field field, ValueProvider<T, Object> getter, Setter<T, Object> setter) {
        this.field = field;
        this.getter = getter;
        this.setter = setter;

        // Cache annotation information at construction time
        this.jsonIgnored = field.getAnnotation(JsonIgnore.class) != null;
        this.jsonUpdateAllowed = field.getAnnotation(JsonUpdateAllowed.class) != null;

        JsonName nameAnnotation = field.getAnnotation(JsonName.class);
        this.jsonName = nameAnnotation != null ? nameAnnotation.value() : field.getName();

        JsonConverter converterAnnotation = field.getAnnotation(JsonConverter.class);
        if (converterAnnotation != null) {
            try {
                this.converter = converterAnnotation.value().getConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to instantiate converter for field " + field.getName(), e);
            }
        } else {
            this.converter = null;
        }
    }

    /**
     * Reads the field of the given type (and its superclasses). Any field, that has a valid bean-styled
     * getter ("get" or "is", alternative a method named like the field) will be included in the set.<br>
     * Setters are optional. If no setter is available, invoking the setter will lead to an
     * {@link UnsupportedOperationException}.
     *
     * @param type type
     * @param <T>  analyzed type
     * @return set of bean properties
     */
    public static <T> Set<BeanProperties<T>> read(Class<T> type) {
        return Stream.of(type.getDeclaredFields())
                .map(field -> {
                    String fieldName = field.getName();

                    Method getterMethod = MethodUtils.getAccessibleMethod(type, "get" + StringUtils.capitalize(fieldName));
                    if (getterMethod == null) {
                        getterMethod = MethodUtils.getAccessibleMethod(type, "is" + StringUtils.capitalize(fieldName));
                    }

                    if (getterMethod == null) {
                        getterMethod = MethodUtils.getAccessibleMethod(type, fieldName);
                    }

                    if (getterMethod == null) {
                        return null; // ignore any fields, that have no getter
                    }

                    Class<?> fieldType = field.getType();
                    Method setterMethod = MethodUtils.getAccessibleMethod(type, "set" + StringUtils.capitalize(fieldName), fieldType);

                    if (setterMethod == null) {
                        setterMethod = MethodUtils.getAccessibleMethod(type, fieldName, fieldType);
                    }

                    Method finalGetterMethod = getterMethod;
                    Method finalSetterMethod = setterMethod;
                    ValueProvider<T, Object> getter = item -> {
                        try {
                            return finalGetterMethod.invoke(item);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException("Failed to invoke getter for field " + fieldName, e);
                        }
                    };

                    // setter is optional
                    Setter<T, Object> setter;
                    if (finalSetterMethod != null) {
                        setter = (item, value) -> {
                            Object valueToWrite = value;
                            if(value instanceof Optional) { // special handling for getters, that return optional
                                valueToWrite = ((Optional<?>) value).orElse(null);
                            }

                            try {
                                finalSetterMethod.invoke(item, valueToWrite);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                throw new RuntimeException("Failed to invoke setter for field " + fieldName, e);
                            }
                        };
                    } else {
                        setter = null;
                    }
                    return new BeanProperties<T>(field, getter, setter);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public Optional<Setter<T, Object>> getSetter() {
        return Optional.ofNullable(setter);
    }

    public String getName() {
        return field.getName();
    }

    /**
     * Returns whether this field should be ignored for JSON serialization.
     * @return true if field has @JsonIgnore annotation
     */
    public boolean isJsonIgnored() {
        return jsonIgnored;
    }

    /**
     * Returns whether this field can be updated from JSON.
     * @return true if field has @JsonUpdateAllowed annotation
     */
    public boolean isJsonUpdateAllowed() {
        return jsonUpdateAllowed;
    }

    /**
     * Returns the JSON property name for this field.
     * @return the JSON name (from @JsonName annotation or field name)
     */
    public String getJsonName() {
        return jsonName;
    }

    /**
     * Returns the cached converter instance for this field, if any.
     * @return the converter or null if none configured
     */
    @SuppressWarnings("unchecked")
    public <V, E> JsonItemPropertyConverter<V, E> getConverter() {
        return (JsonItemPropertyConverter<V, E>) converter;
    }
}
