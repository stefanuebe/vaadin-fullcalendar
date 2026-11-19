package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;
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
 * expected lists are the test resources {@code <class name>-api.txt}, one method per line, sorted. They list only what
 * the scheduler classes declare: the scheduler accessors and the covariant {@code view(…)}, whose bridge method is the
 * second {@code view} line. The core accessors are inherited and pinned in the core module. A new scheduler option
 * field adds its getter and setters there.
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

    /**
     * The scheduler classes inherit the core accessors instead of generating them again, so a pinned list updated by
     * copying the actual one cannot bring the duplicates back unnoticed. Only the covariant view(…) overrides a core
     * method.
     */
    @Test
    void schedulerClassesOverrideNoCoreAccessor() throws NoSuchMethodException {
        Method view = FullCalendarSchedulerOptions.class.getMethod("view", CalendarView.class);
        for (Class<?> type : List.of(FullCalendarSchedulerOptions.class, FullCalendarSchedulerViewOptions.class)) {
            List<String> overrides = Arrays.stream(type.getDeclaredMethods())
                    .filter(method -> Modifier.isPublic(method.getModifiers()) && !method.isBridge())
                    .filter(method -> !method.equals(view))
                    .filter(method -> overridesCoreMethod(type.getSuperclass(), method))
                    .map(SchedulerOptionsApiTest::signature)
                    .toList();
            assertEquals(List.of(), overrides, type.getSimpleName());
        }
    }

    private static boolean overridesCoreMethod(Class<?> core, Method method) {
        try {
            core.getMethod(method.getName(), method.getParameterTypes());
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
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
