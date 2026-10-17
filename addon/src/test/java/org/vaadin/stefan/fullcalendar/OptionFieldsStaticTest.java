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
 * Rules for the fields of the option data classes, checked without a calendar instance: each field is a library option
 * of the right scope with its own JSON name, no field name says "event", no library name needs a keyword as field
 * name, no options method overrides an Object method, and each field has an @see tag. In the other direction, every
 * library option name has a field, apart from a short list of exceptions. The library option
 * names come from the test resource option-names.txt.
 */
class OptionFieldsStaticTest {

    /** The classes whose fields the sync sends. */
    private static final List<Class<?>> DATA_CLASSES = List.of(CalendarOptions.class, ViewOptions.class);
    /** The classes whose methods users reach through the options objects. */
    private static final List<Class<?>> OPTIONS_CLASSES = List.of(CalendarOptions.class, ViewOptions.class,
            FullCalendarOptions.class, FullCalendarViewOptions.class);
    private static final Set<String> OBJECT_METHODS = Arrays.stream(Object.class.getDeclaredMethods())
            .map(OptionFieldsStaticTest::signature)
            .collect(Collectors.toSet());
    /**
     * The library option names that have no field, mapped to the reason. The resource lists the same names. The
     * reasons are for the reader; the tests use the keys only.
     */
    private static final Map<String, String> KEYS_WITHOUT_A_FIELD = Map.of(
            "height", "HasSize sets it (setHeight, setHeightFull), so the options have no height field; a raw "
                    + "height through setRawOption wins over it");
    /** The library option names of the resource, mapped to "global" or "view". */
    private static Map<String, String> readOptionNames() throws IOException {
        Map<String, String> names = new LinkedHashMap<>();
        try (var reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(
                OptionFieldsStaticTest.class.getResourceAsStream("option-names.txt")), StandardCharsets.UTF_8))) {
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

    /** The instance fields of the class and its superclasses, as the sync reads them. */
    private static List<Field> fields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = type; c != Object.class; c = c.getSuperclass()) {
            Arrays.stream(c.getDeclaredFields())
                    .filter(field -> !Modifier.isStatic(field.getModifiers()))
                    .forEach(fields::add);
        }
        return fields;
    }

    private static List<Field> dataFields() {
        return DATA_CLASSES.stream().flatMap(type -> fields(type).stream()).toList();
    }

    private static Set<String> fieldJsonNames() {
        return dataFields().stream().map(OptionFieldsStaticTest::jsonName).collect(Collectors.toSet());
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
    void everyFieldIsALibraryOptionWithTheMatchingScope() throws IOException {
        Map<String, String> names = readOptionNames();
        List<String> problems = new ArrayList<>();
        for (Class<?> type : DATA_CLASSES) {
            String expected = CalendarOptions.class.isAssignableFrom(type) ? "global" : "view";
            for (Field field : fields(type)) {
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

    /** The other direction of everyFieldIsALibraryOptionWithTheMatchingScope. */
    @Test
    void everyResourceNameHasAField() throws IOException {
        Set<String> fieldNames = fieldJsonNames();
        List<String> problems = readOptionNames().keySet().stream()
                .filter(name -> !fieldNames.contains(name))
                .filter(name -> !KEYS_WITHOUT_A_FIELD.containsKey(name))
                .toList();
        assertEquals(List.of(), problems);
    }

    /**
     * Each entry of KEYS_WITHOUT_A_FIELD is a library option name of the resource without a field. An entry that no
     * longer applies fails here, so the list cannot hide a later gap.
     */
    @Test
    void everyExceptionStillApplies() throws IOException {
        Set<String> fieldNames = fieldJsonNames();
        Set<String> resourceNames = readOptionNames().keySet();
        List<String> problems = KEYS_WITHOUT_A_FIELD.keySet().stream()
                .filter(key -> !resourceNames.contains(key) || fieldNames.contains(key))
                .toList();
        assertEquals(List.of(), problems);
    }

    @Test
    void noTwoFieldsHaveTheSameJsonName() {
        Set<String> seen = new HashSet<>();
        List<String> problems = dataFields().stream()
                .filter(field -> !seen.add(jsonName(field)))
                .map(OptionFieldsStaticTest::label)
                .toList();
        assertEquals(List.of(), problems, "The sync would send only one of them.");
    }

    @Test
    void noFieldNameContainsEvent() {
        List<String> problems = dataFields().stream()
                .filter(field -> field.getName().toLowerCase().contains("event"))
                .map(OptionFieldsStaticTest::label)
                .toList();
        assertEquals(List.of(), problems, "Field names say entry, not event. @JsonName keeps the library name.");
    }

    /**
     * The compiler already rejects a field named like a keyword, so this checks the library names instead, with
     * "event" written as "entry" as the field would be named. It fails when a library update brings a name that needs
     * another field name and an @JsonName, until a field with that @JsonName exists.
     */
    @Test
    void noLibraryOptionNameIsAJavaKeyword() throws IOException {
        Set<String> renamed = new HashSet<>();
        dataFields().stream()
                .filter(field -> field.isAnnotationPresent(JsonName.class))
                .forEach(field -> renamed.add(jsonName(field)));
        List<String> problems = readOptionNames().keySet().stream()
                .filter(name -> !renamed.contains(name))
                .filter(name -> SourceVersion.isKeyword(asFieldName(name)))
                .toList();
        assertEquals(List.of(), problems);
    }

    /** The library name as a field would be named: "event" written as "entry". */
    private static String asFieldName(String libraryName) {
        return libraryName.replace("event", "entry").replace("Event", "Entry");
    }

    @Test
    void noOptionsMethodOverridesAnObjectMethod() {
        List<String> problems = OPTIONS_CLASSES.stream()
                .flatMap(type -> Arrays.stream(type.getDeclaredMethods()))
                .filter(method -> OBJECT_METHODS.contains(signature(method)))
                .map(OptionFieldsStaticTest::label)
                .toList();
        assertEquals(List.of(), problems);
    }

    @Test
    void everyFieldHasAnSeeTag() throws IOException {
        List<String> problems = new ArrayList<>();
        for (Field field : dataFields()) {
            // Surefire runs the tests in the module directory, so the path is relative to addon/.
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
