package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.converter.ResourceColumnsConverter;
import org.vaadin.stefan.fullcalendar.converters.JsonItemPropertyConverter;
import org.vaadin.stefan.fullcalendar.json.JsonConverter;
import org.vaadin.stefan.fullcalendar.option.JsCallback;
import tools.jackson.databind.JsonNode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The converter checks of the core ConverterTest for the fields the scheduler data classes declare: each field uses the
 * converter of its type, and each value type of these fields writes the client form of the library and reads it back.
 * The core test covers the inherited fields, because the core test classes are not on this module's test classpath.
 */
class SchedulerConverterTest {

    /**
     * A value type with the converter its fields use, and values of each form with the JSON the library reads for them.
     */
    record ValueType(Class<? extends JsonItemPropertyConverter<?, ?>> converter, Map<Object, JsonNode> samples) {
    }

    /**
     * The value type of each field of the scheduler data classes whose option takes several forms, with at least one
     * value per form and the client form of each. A field of a value type adds the type here, also a core value type.
     */
    static final Map<Class<?>, ValueType> VALUE_TYPES = Map.of();

    /**
     * Field types with a converter that are no value type, since their options take one form only, with the converter
     * their fields use.
     */
    static final Map<Class<?>, Class<?>> ONE_FORM_TYPES = Map.of(
            List.class, ResourceColumnsConverter.class);

    /**
     * Field types the sync writes without a converter: JSON scalars, a callback, and types with a client value
     * ({@link ClientSideValue}). Every other field type needs a converter.
     */
    private static final Set<Class<?>> WRITTEN_AS_THEY_ARE =
            Set.of(Boolean.class, Integer.class, Double.class, String.class, JsCallback.class);

    /** The fields the scheduler data classes declare. */
    static Stream<Field> optionFields() {
        return Stream.of(SchedulerCalendarOptions.class, SchedulerViewOptions.class)
                .flatMap(type -> Arrays.stream(type.getDeclaredFields()))
                .filter(field -> !Modifier.isStatic(field.getModifiers()));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void everyValueType_hasTheClientFormOfTheLibrary_andRoundTrips() throws Exception {
        for (var entry : VALUE_TYPES.entrySet()) {
            JsonItemPropertyConverter converter = entry.getValue().converter().getConstructor().newInstance();
            for (var sample : entry.getValue().samples().entrySet()) {
                Object value = sample.getKey();
                String label = entry.getKey().getSimpleName() + ": " + value;
                assertTrue(entry.getKey().isInstance(value), label);
                assertTrue(converter.supports(value), label);
                assertEquals(sample.getValue(), converter.toClientModel(value, null), label);
                assertEquals(value, converter.toServerModel(sample.getValue(), null), label);
                assertEquals(value, serializedAndBack(value), label);
            }
        }
    }

    private static Object serializedAndBack(Object value) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(value);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return in.readObject();
        }
    }

    /**
     * Every field whose type the sync cannot write as it is uses the converter of its type from {@link #VALUE_TYPES}
     * or {@link #ONE_FORM_TYPES}.
     */
    @Test
    void everyValueTypeField_usesTheConverterOfItsType() {
        List<Field> convertedFields = optionFields()
                .filter(field -> !WRITTEN_AS_THEY_ARE.contains(field.getType())
                        && !ClientSideValue.class.isAssignableFrom(field.getType()))
                .toList();

        for (Field field : convertedFields) {
            Class<?> type = field.getType();
            Class<?> expected = VALUE_TYPES.containsKey(type) ? VALUE_TYPES.get(type).converter()
                    : ONE_FORM_TYPES.get(type);
            assertNotNull(expected, "Add " + type.getSimpleName() + " of " + field.getName()
                    + " to VALUE_TYPES, or to ONE_FORM_TYPES when its option takes one form only");
            JsonConverter annotation = field.getAnnotation(JsonConverter.class);
            assertNotNull(annotation, "No converter on " + field.getName());
            assertEquals(expected, annotation.value(), field.getName());
        }
    }

    @Test
    void valueTypeSamples_coverEveryForm() {
        VALUE_TYPES.forEach((type, valueType) -> {
            Set<Object> forms = valueType.samples().keySet().stream()
                    .map(value -> value instanceof Enum<?> constant ? constant : value.getClass())
                    .collect(Collectors.toSet());
            Class<?>[] permitted = type.isEnum() ? new Class<?>[]{type} : type.getPermittedSubclasses();
            for (Class<?> form : permitted) {
                if (form.isEnum()) {
                    assertTrue(forms.containsAll(List.of(form.getEnumConstants())),
                            "Add a sample of each constant of " + form.getSimpleName() + " to " + type.getSimpleName());
                } else {
                    assertTrue(forms.contains(form),
                            "Add a sample of " + form.getSimpleName() + " to " + type.getSimpleName());
                }
            }
        });
    }
}
