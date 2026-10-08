package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Every option of the FullCalendar version in use has a constant in {@link Option} or {@link SchedulerOption}, or is
 * left out on purpose. The key list is a test resource named after {@link FullCalendar#FC_CLIENT_VERSION}, so raising
 * the client version fails here until the list is regenerated from the new type definitions (see the resource's
 * header) and the new keys are handled.
 */
class OptionCompletenessTest {

    /** FullCalendar keys that get no constant, and why. */
    private record LeftOut(String reason, List<String> keys) {
    }

    private static final List<LeftOut> LEFT_OUT = List.of(
            new LeftOut("Listener. The add-on wires its own server-side events, others can be set as JsCallback via the String key",
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
            new LeftOut("Alias of class (CALENDAR_CLASS). The add-on sets it for its stable fc class, setting it would drop that class",
                    List.of("className")),
            new LeftOut("Objects of FullCalendar's JS framework integrations (React, Vue, ...) and internal flags",
                    List.of("controller", "customRenderingMetaMap", "handleCustomRendering", "needsResourceData")));

    @Test
    void everyFullCalendarOptionHasAConstantOrIsLeftOutOnPurpose() throws Exception {
        Set<String> fullCalendarKeys = readKeys();
        Set<String> constantKeys = Stream.concat(
                        Stream.of(Option.values()).map(Option::getOptionKey),
                        Stream.of(SchedulerOption.values()).map(SchedulerOption::getOptionKey))
                .collect(Collectors.toSet());
        Set<String> leftOut = LEFT_OUT.stream().flatMap(l -> l.keys().stream()).collect(Collectors.toSet());

        Set<String> uncovered = new TreeSet<>(fullCalendarKeys);
        uncovered.removeAll(constantKeys);
        uncovered.removeAll(leftOut);
        assertEquals(Set.of(), uncovered, "FullCalendar options without a constant");

        Set<String> leftOutButCovered = new HashSet<>(leftOut);
        leftOutButCovered.retainAll(constantKeys);
        assertEquals(Set.of(), leftOutButCovered, "left out, but a constant exists");

        Set<String> leftOutUnknown = new TreeSet<>(leftOut);
        leftOutUnknown.removeAll(fullCalendarKeys);
        assertEquals(Set.of(), leftOutUnknown, "left out, but not a FullCalendar option");
    }

    @Test
    void everyConstantIsAFullCalendarOption() throws Exception {
        Set<String> fullCalendarKeys = readKeys();
        // options of the separately loaded google calendar plugin, not part of the type definitions read above
        Set<String> pluginKeys = Set.of("googleCalendarApiKey");
        for (Option option : Option.values()) {
            String key = option.getOptionKey();
            assertTrue(fullCalendarKeys.contains(key) || pluginKeys.contains(key), option.name() + " -> " + key);
        }
        for (SchedulerOption option : SchedulerOption.values()) {
            assertTrue(fullCalendarKeys.contains(option.getOptionKey()), option.name() + " -> " + option.getOptionKey());
        }
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
