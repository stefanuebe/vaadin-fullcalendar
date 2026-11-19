package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.json.JsonName;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Every option of the FullCalendar version in use has a field in the options classes of core or scheduler, or is
 * left out on purpose. The key list is a test resource named after {@link FullCalendar#FC_CLIENT_VERSION}, so raising
 * the client version fails here until the list is regenerated from the new type definitions (see the resource's
 * header) and the new keys are handled. It lives in the scheduler module, because only here core and scheduler fields
 * are both on the classpath. Whether a field sits in the global or the view class is checked by
 * {@code OptionFieldsStaticTest} and {@link SchedulerOptionFieldsStaticTest}.
 */
class OptionCompletenessTest {

    /** The classes whose fields the option sync sends. */
    private static final List<Class<?>> DATA_CLASSES = List.of(CalendarOptions.class, ViewOptions.class,
            SchedulerCalendarOptions.class, SchedulerViewOptions.class);

    /** FullCalendar keys that get no field, and why. */
    private record LeftOut(String reason, List<String> keys) {
    }

    private static final List<LeftOut> LEFT_OUT = List.of(
            new LeftOut("Listener. The add-on wires its own server-side events, others can be set as raw options",
                    List.of("dateClick", "datesSet", "drop", "eventAdd", "eventChange", "eventClick", "eventDragStart",
                            "eventDragStop", "eventDrop", "eventLeave", "eventMouseEnter", "eventMouseLeave",
                            "eventReceive", "eventRemove", "eventResize", "eventResizeStart", "eventResizeStop",
                            "eventSourceFailure", "eventsSet", "select", "unselect")),
            new LeftOut("Data. Entry providers, remote entry sources and the resource API feed these",
                    List.of("events", "eventSources", "initialEvents", "initialResources", "resources")),
            new LeftOut("Set by the add-on itself, as plugins, locales and view-specific options",
                    List.of("plugins", "locales", "views")),
            new LeftOut("Only valid inside a custom view definition, see CustomCalendarView",
                    List.of("type", "component", "content", "didMount", "willUnmount", "buttonTextKey",
                            "dateProfileGeneratorClass", "usesMinMaxTime", "disallowAmbigTitle")),
            new LeftOut("Alias of className, which has the field", List.of("class")),
            new LeftOut("No field, the add-on sizes the calendar through HasSize, see the spec's Out of Scope",
                    List.of("height")),
            new LeftOut("Objects of FullCalendar's JS framework integrations (React, Vue, ...) and internal flags",
                    List.of("controller", "customRenderingMetaMap", "handleCustomRendering", "needsResourceData")));

    @Test
    void everyFullCalendarOptionHasAFieldOrIsLeftOutOnPurpose() throws Exception {
        Set<String> fullCalendarKeys = readKeys();
        Set<String> fieldKeys = fieldJsonNames();
        Set<String> leftOut = LEFT_OUT.stream().flatMap(l -> l.keys().stream()).collect(Collectors.toSet());

        Set<String> uncovered = new TreeSet<>(fullCalendarKeys);
        uncovered.removeAll(fieldKeys);
        uncovered.removeAll(leftOut);
        assertEquals(Set.of(), uncovered, "FullCalendar options without a field");

        Set<String> leftOutButCovered = new HashSet<>(leftOut);
        leftOutButCovered.retainAll(fieldKeys);
        assertEquals(Set.of(), leftOutButCovered, "left out, but a field exists");

        Set<String> leftOutUnknown = new TreeSet<>(leftOut);
        leftOutUnknown.removeAll(fullCalendarKeys);
        assertEquals(Set.of(), leftOutUnknown, "left out, but not a FullCalendar option");
    }

    @Test
    void everyFieldIsAFullCalendarOption() throws Exception {
        Set<String> fullCalendarKeys = readKeys();
        // options of the separately loaded google calendar plugin, not part of the type definitions read above
        Set<String> pluginKeys = Set.of("googleCalendarApiKey");

        Set<String> unknown = new TreeSet<>(fieldJsonNames());
        unknown.removeAll(fullCalendarKeys);
        unknown.removeAll(pluginKeys);
        assertEquals(Set.of(), unknown, "fields that are no FullCalendar option");
    }

    private static Set<String> fieldJsonNames() {
        return DATA_CLASSES.stream()
                .flatMap(type -> Arrays.stream(type.getDeclaredFields()))
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .map(OptionCompletenessTest::jsonName)
                .collect(Collectors.toSet());
    }

    private static String jsonName(Field field) {
        JsonName name = field.getAnnotation(JsonName.class);
        return name != null ? name.value() : field.getName();
    }

    private static Set<String> readKeys() throws Exception {
        String resource = "/fullcalendar-" + FullCalendar.FC_CLIENT_VERSION + "-option-keys.txt";
        try (InputStream in = OptionCompletenessTest.class.getResourceAsStream(resource)) {
            assertNotNull(in, resource + " is missing, regenerate it for the new FullCalendar version");
            return new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)).lines()
                    .map(String::strip)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .collect(Collectors.toSet());
        }
    }
}
