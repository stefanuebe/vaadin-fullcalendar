package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.data.binder.Setter;
import com.vaadin.flow.function.ValueProvider;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Stefan Uebe
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class BeanProperties<T> {

    private final Field field;
    private final ValueProvider<T, Object> getter;
    private final Setter<T, Object> setter;

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
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
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
                            } catch (Throwable e) {
                                throw new RuntimeException(e);
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
}
