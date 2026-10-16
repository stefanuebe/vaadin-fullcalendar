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
 * Pins the public methods of the scheduler's options objects, as the core OptionsApiTest does for the core ones. The
 * expected lists are the test resources {@code <class name>-api.txt}, one method per line, sorted. They list the core
 * accessors too, because the @Delegate of the scheduler classes generates them again as overrides. A new option field
 * adds its getter and setters there.
 */
class SchedulerOptionsApiTest {

    @ParameterizedTest
    @ValueSource(classes = {FullCalendarSchedulerOptions.class, FullCalendarSchedulerViewOptions.class})
    void publicMethodsArePinned(Class<?> type) throws IOException {
        String actual = Arrays.stream(type.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .map(SchedulerOptionsApiTest::signature)
                .sorted()
                .collect(Collectors.joining("\n"));

        assertEquals(expected(type), actual, "The public methods of " + type.getSimpleName()
                + " changed. If that is intended, check that each new field has a getter, a setter per form and one"
                + " taking its value type, then put the actual list into " + type.getSimpleName() + "-api.txt.");
    }

    @ParameterizedTest
    @ValueSource(classes = {FullCalendarSchedulerOptions.class, FullCalendarSchedulerViewOptions.class})
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
