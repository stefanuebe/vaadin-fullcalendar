package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins the public methods of the options objects, so no builder or helper method leaks into the public API. The
 * expected lists are the test resources {@code <class name>-api.txt}, one method per line, sorted. A new option field
 * adds its getter and setters there.
 */
class OptionsApiTest {

    @ParameterizedTest
    @ValueSource(classes = {FullCalendarOptions.class, FullCalendarViewOptions.class})
    void publicMethodsArePinned(Class<?> type) throws IOException {
        String actual = Arrays.stream(type.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .map(OptionsApiTest::signature)
                .sorted()
                .collect(Collectors.joining("\n"));

        assertEquals(expected(type), actual, "The public methods of " + type.getSimpleName()
                + " changed. If that is intended, put the actual list into " + type.getSimpleName() + "-api.txt.");
    }

    @ParameterizedTest
    @ValueSource(classes = {FullCalendarOptions.class, FullCalendarViewOptions.class})
    void hasNoPublicConstructor(Class<?> type) {
        assertEquals(List.of(), List.of(type.getConstructors()));
    }

    private static String signature(Method method) {
        return method.getName() + Arrays.stream(method.getParameterTypes())
                .map(Class::getSimpleName)
                .collect(Collectors.joining(", ", "(", "): ")) + method.getReturnType().getSimpleName();
    }

    private static String expected(Class<?> type) throws IOException {
        try (InputStream in = Objects.requireNonNull(type.getResourceAsStream(type.getSimpleName() + "-api.txt"))) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).strip();
        }
    }
}
