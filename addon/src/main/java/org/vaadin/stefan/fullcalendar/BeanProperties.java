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
import org.vaadin.stefan.fullcalendar.json.JsonReadField;
import org.vaadin.stefan.fullcalendar.json.JsonUpdateAllowed;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Caches reflection metadata for a single field of a bean type, including getter, setter,
 * JSON annotation data, and converter instances. Created once at application startup via
 * {@link #read(Class)} and reused for all subsequent serialization/deserialization calls.
 *
 * @author Stefan Uebe
 */
@Getter
public class BeanProperties<T> {

    private final Field field;
    private final ValueProvider<T, Object> getter;
    private final Setter<T, Object> setter;

    // Cached annotation data (read once at construction time)
    private final boolean jsonIgnored;
    private final boolean jsonUpdateAllowed;
    private final String jsonName;
    @SuppressWarnings("rawtypes")
    private final JsonItemPropertyConverter converter;

    @SuppressWarnings("rawtypes")
    private BeanProperties(Field field, ValueProvider<T, Object> getter, Setter<T, Object> setter,
                           boolean jsonIgnored, boolean jsonUpdateAllowed, String jsonName,
                           JsonItemPropertyConverter converter) {
        this.field = field;
        this.getter = getter;
        this.setter = setter;
        this.jsonIgnored = jsonIgnored;
        this.jsonUpdateAllowed = jsonUpdateAllowed;
        this.jsonName = jsonName;
        this.converter = converter;
    }

    /**
     * Reads the fields of the given type (and its superclasses). Any field that has a valid bean-styled
     * getter ("get" or "is", or a method named like the field) will be included in the set.
     * Fields annotated with {@link JsonReadField} are included even without a public getter.
     * <br>
     * Setters are optional. If no setter is available, invoking the setter will lead to an
     * {@link UnsupportedOperationException}.
     * <p>
     * Annotation data ({@link JsonIgnore}, {@link JsonUpdateAllowed}, {@link JsonName},
     * {@link JsonConverter}) is read once and cached in the returned instances.
     *
     * @param type type
     * @param <T>  analyzed type
     * @return set of bean properties
     */
    public static <T> Set<BeanProperties<T>> read(Class<T> type) {
        return Stream.of(type.getDeclaredFields())
                .map(field -> {
                    String fieldName = field.getName();
                    boolean directFieldAccess = field.getAnnotation(JsonReadField.class) != null;

                    Method getterMethod = MethodUtils.getAccessibleMethod(type, "get" + StringUtils.capitalize(fieldName));
                    if (getterMethod == null) {
                        getterMethod = MethodUtils.getAccessibleMethod(type, "is" + StringUtils.capitalize(fieldName));
                    }

                    if (getterMethod == null) {
                        getterMethod = MethodUtils.getAccessibleMethod(type, fieldName);
                    }

                    if (getterMethod == null && !directFieldAccess) {
                        return null; // ignore any fields that have no getter and no @JsonReadField
                    }

                    // Build getter
                    ValueProvider<T, Object> getter;
                    if (getterMethod != null) {
                        Method finalGetterMethod = getterMethod;
                        getter = item -> {
                            try {
                                return finalGetterMethod.invoke(item);
                            } catch (Throwable e) {
                                throw new RuntimeException(e);
                            }
                        };
                    } else {
                        // Direct field access for @JsonReadField
                        field.setAccessible(true);
                        getter = item -> {
                            try {
                                return field.get(item);
                            } catch (Throwable e) {
                                throw new RuntimeException(e);
                            }
                        };
                    }

                    // Build setter (optional)
                    Class<?> fieldType = field.getType();
                    Method setterMethod = MethodUtils.getAccessibleMethod(type, "set" + StringUtils.capitalize(fieldName), fieldType);
                    if (setterMethod == null) {
                        setterMethod = MethodUtils.getAccessibleMethod(type, fieldName, fieldType);
                    }

                    Setter<T, Object> setter;
                    if (setterMethod != null) {
                        Method finalSetterMethod = setterMethod;
                        setter = (item, value) -> {
                            Object valueToWrite = value;
                            if (value instanceof Optional) {
                                valueToWrite = ((Optional<?>) value).orElse(null);
                            }
                            try {
                                finalSetterMethod.invoke(item, valueToWrite);
                            } catch (Throwable e) {
                                throw new RuntimeException(e);
                            }
                        };
                    } else {
                        setter = null;
                    }

                    // Cache annotation data
                    boolean jsonIgnored = field.getAnnotation(JsonIgnore.class) != null;
                    boolean jsonUpdateAllowed = field.getAnnotation(JsonUpdateAllowed.class) != null;

                    String jsonName = fieldName;
                    JsonName nameAnnotation = field.getAnnotation(JsonName.class);
                    if (nameAnnotation != null) {
                        jsonName = nameAnnotation.value();
                    }

                    JsonItemPropertyConverter<?, ?> converter = null;
                    JsonConverter converterAnnotation = field.getAnnotation(JsonConverter.class);
                    if (converterAnnotation != null) {
                        try {
                            converter = converterAnnotation.value().getConstructor().newInstance();
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to instantiate converter for field " + fieldName, e);
                        }
                    }

                    return new BeanProperties<>(field, getter, setter, jsonIgnored, jsonUpdateAllowed, jsonName, converter);
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
}
