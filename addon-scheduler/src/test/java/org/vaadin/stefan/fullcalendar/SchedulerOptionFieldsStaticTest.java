package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.json.JsonName;

import javax.lang.model.SourceVersion;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The rules of the core OptionFieldsStaticTest for the fields the scheduler data classes declare: each is a scheduler
 * option of the right scope with its own JSON name, no field name says "event", no library name needs a keyword as
 * field name, no options method overrides an Object method, and each field has an @see tag. In the other direction,
 * every scheduler option name has a field. The scheduler option names come from the
 * test resource option-names.txt of this module. The core test covers the inherited fields, because the core resource
 * is not on this module's test classpath.
 */
class SchedulerOptionFieldsStaticTest {

    /** The scheduler classes whose fields the sync sends. */
    private static final List<Class<?>> DATA_CLASSES = List.of(SchedulerCalendarOptions.class,
            SchedulerViewOptions.class);
    /** The scheduler classes whose methods users reach through the options objects. */
    private static final List<Class<?>> OPTIONS_CLASSES = List.of(SchedulerCalendarOptions.class,
            SchedulerViewOptions.class, FullCalendarSchedulerOptions.class, FullCalendarSchedulerViewOptions.class);
    private static final Set<String> OBJECT_METHODS = Arrays.stream(Object.class.getDeclaredMethods())
            .map(SchedulerOptionFieldsStaticTest::signature)
            .collect(Collectors.toSet());

    /** The scheduler option names of the resource, mapped to "global" or "view". */
    private static Map<String, String> readOptionNames() throws IOException {
        Map<String, String> names = new LinkedHashMap<>();
        try (var reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(
                SchedulerOptionFieldsStaticTest.class.getResourceAsStream("option-names.txt")),
                StandardCharsets.UTF_8))) {
            for (String line : reader.lines().map(String::strip).toList()) {
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split("\\s+");
                assertTrue(parts.length == 2 && (parts[1].equals("global") || parts[1].equals("view")),
                        "Malformed line in option-names.txt: " + line);
                assertNull(names.put(parts[0], parts[1]), "Listed twice in option-names.txt: " + parts[0]);
            }
        }
        return names;
    }

    /** The instance fields the class declares itself. */
    private static List<Field> declaredFields(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .toList();
    }

    private static List<Field> dataFields() {
        return DATA_CLASSES.stream().flatMap(type -> declaredFields(type).stream()).toList();
    }

    private static Set<String> fieldJsonNames() {
        return dataFields().stream().map(SchedulerOptionFieldsStaticTest::jsonName).collect(Collectors.toSet());
    }

    private static String jsonName(Field field) {
        JsonName name = field.getAnnotation(JsonName.class);
        return name != null ? name.value() : field.getName();
    }

    private static String label(Member member) {
        return member.getDeclaringClass().getSimpleName() + "." + member.getName();
    }

    private static String signature(Method method) {
        return method.getName() + Arrays.toString(method.getParameterTypes());
    }

    /**
     * The Javadoc right before the field declaration, or null if it has none. Annotations and comments other than
     * Javadoc may stand between them, another member may not.
     */
    private static String javadocOf(String source, String fieldName) {
        Matcher matcher = Pattern.compile("private\\s+[\\w<>,.?\\[\\]\\s]+\\s" + fieldName + "\\s*[;=]")
                .matcher(source);
        assertTrue(matcher.find(), "No declaration found for " + fieldName);
        String before = source.substring(0, matcher.start());
        int start = before.lastIndexOf("/**");
        if (start < 0) {
            return null;
        }
        int end = before.indexOf("*/", start);
        // A semicolon, a line with only a closing brace or a line ending with an opening brace belongs to another
        // member or the class declaration. Line comments are left out, because they may contain either.
        String between = before.substring(end).replaceAll("//[^\\n]*", "");
        return Pattern.compile(";|(?m)^\\s*}\\s*$|(?m)\\{\\s*$").matcher(between).find()
                ? null : before.substring(start, end);
    }

    @Test
    void everyFieldIsASchedulerOptionWithTheMatchingScope() throws IOException {
        Map<String, String> names = readOptionNames();
        List<String> problems = new ArrayList<>();
        for (Class<?> type : DATA_CLASSES) {
            String expected = CalendarOptions.class.isAssignableFrom(type) ? "global" : "view";
            for (Field field : declaredFields(type)) {
                String scope = names.get(jsonName(field));
                if (scope == null) {
                    problems.add(label(field) + ": " + jsonName(field) + " is not in option-names.txt");
                } else if (!scope.equals(expected)) {
                    problems.add(label(field) + ": the resource says " + scope);
                }
            }
        }
        assertEquals(List.of(), problems);
    }

    /** The other direction of everyFieldIsASchedulerOptionWithTheMatchingScope. */
    @Test
    void everyResourceNameHasAField() throws IOException {
        Set<String> fieldNames = fieldJsonNames();
        List<String> problems = readOptionNames().keySet().stream()
                .filter(name -> !fieldNames.contains(name))
                .toList();
        assertEquals(List.of(), problems);
    }

    /** The core fields are on the classpath, only the core resource is not, so this checks against them too. */
    @Test
    void noTwoFieldsHaveTheSameJsonName_coreFieldsIncluded() {
        Set<String> seen = new HashSet<>();
        List<String> problems = new ArrayList<>();
        for (Class<?> type : List.of(CalendarOptions.class, ViewOptions.class, SchedulerCalendarOptions.class,
                SchedulerViewOptions.class)) {
            declaredFields(type).stream()
                    .filter(field -> !seen.add(jsonName(field)))
                    .map(SchedulerOptionFieldsStaticTest::label)
                    .forEach(problems::add);
        }
        assertEquals(List.of(), problems, "The sync would send only one of them.");
    }

    @Test
    void noFieldNameContainsEvent() {
        List<String> problems = dataFields().stream()
                .filter(field -> field.getName().toLowerCase().contains("event"))
                .map(SchedulerOptionFieldsStaticTest::label)
                .toList();
        assertEquals(List.of(), problems, "Field names say entry, not event. @JsonName keeps the library name.");
    }

    /**
     * The compiler already rejects a field named like a keyword, so this checks the library names instead, with
     * "event" written as "entry" as the field would be named.
     */
    @Test
    void noSchedulerOptionNameIsAJavaKeyword() throws IOException {
        Set<String> renamed = new HashSet<>();
        dataFields().stream()
                .filter(field -> field.isAnnotationPresent(JsonName.class))
                .forEach(field -> renamed.add(jsonName(field)));
        List<String> problems = readOptionNames().keySet().stream()
                .filter(name -> !renamed.contains(name))
                .filter(name -> SourceVersion.isKeyword(name.replace("event", "entry").replace("Event", "Entry")))
                .toList();
        assertEquals(List.of(), problems);
    }

    @Test
    void noOptionsMethodOverridesAnObjectMethod() {
        List<String> problems = OPTIONS_CLASSES.stream()
                .flatMap(type -> Arrays.stream(type.getDeclaredMethods()))
                .filter(method -> OBJECT_METHODS.contains(signature(method)))
                .map(SchedulerOptionFieldsStaticTest::label)
                .toList();
        assertEquals(List.of(), problems);
    }

    @Test
    void everyFieldHasAnSeeTag() throws IOException {
        List<String> problems = new ArrayList<>();
        for (Field field : dataFields()) {
            // Surefire runs the tests in the module directory, so the path is relative to addon-scheduler/.
            String source = Files.readString(
                    Path.of("src/main/java", field.getDeclaringClass().getName().replace('.', '/') + ".java"));
            String javadoc = javadocOf(source, field.getName());
            if (javadoc == null || !javadoc.matches("(?s).*\\n\\s*\\*\\s*@see\\s.*")) {
                problems.add(label(field));
            }
        }
        assertEquals(List.of(), problems, "Fields without @see in their Javadoc.");
    }
}
