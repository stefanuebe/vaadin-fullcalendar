package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.function.SerializableFunction;
import elemental.json.*;
import org.junit.jupiter.api.Assertions;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestUtils {

    /**
     * Inits the vaadin service and mocks it to find your web components under "frontend://bower_components/XYZ" with
     * a file from "src/main/resources/META-INF/resources/frontend/bower_components/XYZ".
     *
     * @param webComponentUrlParts relative paths of web component html files
     */
    @Deprecated
    public static void initVaadinService(final String... webComponentUrlParts) {
//        if (!inited) {
//
//            VaadinService vaadinService = Mockito.mock(VaadinService.class);
//            VaadinService.setCurrent(vaadinService);
//
//            Mockito.when(vaadinService.getDeploymentConfiguration())
//                    .thenAnswer(invocation -> {
//                        DeploymentConfiguration config = Mockito.mock(DeploymentConfiguration.class);
//                        Mockito.when(config.isProductionMode()).thenReturn(false);
//                        return config;
//                    });
//
//            if (webComponentUrlParts != null) {
//                for (String part : webComponentUrlParts) {
//                    Mockito.when(vaadinService.
//                            getResourceAsStream(ArgumentMatchers.eq("frontend://bower_components/" + part), ArgumentMatchers.any(), ArgumentMatchers.any())).
//                            thenAnswer(invocation -> {
//                                Path path = Paths.get("src/main/resources/META-INF/resources/frontend/bower_components/" + part).toAbsolutePath();
//                                if (!Files.isRegularFile(path)) {
//                                    path = Paths.get("src/test/resources/META-INF/resources/frontend/bower_components/" + part).toAbsolutePath();
//                                }
//                                return Files.newInputStream(path);
//                            });
//
//                }
//            }
//
//            inited = true;
//        }
    }

    public static void assertJsonType(JsonObject object, String key, Class<? extends JsonValue> expectedType) {
        JsonValue jsonValue = object.get(key);
        Assertions.assertNotNull(jsonValue, "Json value for key '" + key + "' returned null, expected a json value being a sub type of " + expectedType);

        Class<? extends JsonValue> aClass = jsonValue.getClass();
        if (!expectedType.isAssignableFrom(aClass)) {
            Assertions.fail("Json value for key '" + key + "': Expected sub type of " + expectedType + ", but got " + aClass);
        }
    }

    public static void assertJsonMissingKey(JsonObject object, String key) {
        if (object.hasKey(key)) {
            Assertions.fail("Expected json object to not have key '" + key + "'");
        }
    }

    /**
     * Reads the json property by key and tries to apply it as a string.
     *
     * @param object json object
     * @param key    json property key
     * @param setter setter to apply value
     * @throws NullPointerException when null is passed for not null parameters
     */
    public static void updateString(@NotNull JsonObject object, @NotNull String key, @NotNull Consumer<String> setter) {
        Objects.requireNonNull(object, "JsonObject");
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(setter, "setter");
        if (object.get(key) instanceof JsonString) {
            setter.accept(object.getString(key));
        }
    }

    /**
     * Reads the json property by key and tries to apply it as a boolean.
     *
     * @param object json object
     * @param key    json property key
     * @param setter setter to apply value
     * @throws NullPointerException when null is passed for not null parameters
     */
    public static void updateBoolean(@NotNull JsonObject object, @NotNull String key, @NotNull Consumer<Boolean> setter) {
        Objects.requireNonNull(object, "JsonObject");
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(setter, "setter");
        if (object.get(key) instanceof JsonBoolean) {
            setter.accept(object.getBoolean(key));
        }
    }

//    /**
//     * Reads the json property by key and tries to apply it as a temporal. Might use the timezone, if conversion to UTC is needed.
//     *
//     * @param object   json object
//     * @param key      json property key
//     * @param setter   setter to apply value
//     * @param timezone timezone
//     * @throws NullPointerException when null is passed for not null parameters
//     */
//    public static void updateDateTime(@NotNull JsonObject object, @NotNull String key, @NotNull Consumer<Instant> setter, @NotNull Timezone timezone) {
//        Objects.requireNonNull(object, "JsonObject");
//        Objects.requireNonNull(key, "key");
//        Objects.requireNonNull(setter, "setter");
//        Objects.requireNonNull(timezone, "timezone");
//        if (object.get(key) instanceof JsonString) {
//            Instant dateTime = JsonUtils.parseClientSideDateTime(object.getString(key), timezone);
//
//            setter.accept(dateTime);
//        }
//    }

    public static <T> void assertOptionalEquals(T expected, Optional<T> value) {
        Assertions.assertTrue(value.isPresent());
        Assertions.assertEquals(expected, value.get());
    }

    public static <T> void assertOptionalEquals(T expected, Optional<T> value, String supplier) {
        Assertions.assertTrue(value.isPresent(), supplier);
        Assertions.assertEquals(expected, value.get(), supplier);
    }

    public static <T> void assertNPE(T testObject, Consumer<T> function) {
        Assertions.assertThrows(NullPointerException.class, () -> function.accept(testObject));
    }

    public static <T> void assertIAE(T testObject, Consumer<T> function) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> function.accept(testObject));
    }

    public static Entry createEntry(String id, String title) {
        return createEntry(id, title, null, null, false, true, null, null);
    }

    public static Entry createEntry(String id, String title, LocalDateTime start, LocalDateTime end, boolean allDay, boolean editable, String color, String description) {
        Entry entry = new Entry(id);

        entry.setTitle(title);
        entry.setStart(start);
        entry.setEnd(end);
        entry.setAllDay(allDay);
        entry.setEditable(editable);
        entry.setColor(color);
        entry.setDescription(description);

        return entry;
    }

    public static Entry createRecEntry(String id, String title, LocalDateTime start, LocalDateTime end, boolean allDay, boolean editable, String color, String description) {
        Entry entry = new Entry(id);

        entry.setTitle(title);
        entry.setRecurringStart(start);
        entry.setRecurringEnd(end);
        entry.setAllDay(allDay);
        entry.setEditable(editable);
        entry.setColor(color);
        entry.setDescription(description);

        return entry;
    }

    public static String buildListBasedErrorString(List<Entry> entriesMatching, Collection<Entry> entriesFound) {
        StringBuffer sb = new StringBuffer("Searched for:");
        entriesMatching.stream().map(Entry::getTitle).forEach(s -> sb.append(s).append("\n"));
        sb.append("\n\nbut found:");
        entriesFound.stream().map(Entry::getTitle).forEach(s -> sb.append(s).append("\n"));

        ArrayList<Entry> missingMatching = new ArrayList<>(entriesMatching);
        missingMatching.removeAll(entriesFound);

        ArrayList<Entry> missingFound = new ArrayList<>(entriesFound);
        missingFound.removeAll(entriesMatching);

        if (!missingMatching.isEmpty()) {
            sb.append("\n\nExpected these to be found, but we did not:\n");
            missingMatching.stream().map(Entry::getTitle).forEach(s -> sb.append(s).append("\n"));
        }

        if (!missingFound.isEmpty()) {
            sb.append("\n\nThese have been found, but should not match:\n");
            missingFound.stream().map(Entry::getTitle).forEach(s -> sb.append(s).append("\n"));
        }

        return sb.toString();
    }

    public static <T> void assertEqualAsSet(Set<T> expected, Collection<T> test) {
        assertEquals(expected, test instanceof Set ? test : new HashSet<>(test));
    }

    public static <T> void assertEqualAsSet(Set<T> expected, Collection<T> test, String message) {
        assertEquals(expected, test instanceof Set ? test : new HashSet<>(test), message);
    }

    public static <T> void assertEqualAsSet(Set<T> expected, Stream<T> test) {
        assertEquals(expected, test.collect(Collectors.toSet()));
    }

    public static <T> void assertEqualAsSet(Set<T> expected, Stream<T> test, String message) {
        assertEquals(expected, test.collect(Collectors.toSet()), message);
    }

    public static <T> void assertEqualAsSet(Stream<T> expected, Stream<T> test) {
        assertEquals(expected.collect(Collectors.toSet()), test.collect(Collectors.toSet()));
    }

    public static <T> void assertEqualAsSet(Stream<T> expected, Stream<T> test, String message) {
        assertEquals(expected.collect(Collectors.toSet()), test.collect(Collectors.toSet()), message);
    }

    public static <T> Set<T> toSet(JsonArray array, SerializableFunction<JsonValue, Object> converter) {
        return JsonUtils.ofJsonValue(array, converter, null, HashSet.class);
    }
}
