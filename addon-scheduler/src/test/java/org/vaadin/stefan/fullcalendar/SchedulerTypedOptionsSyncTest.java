package org.vaadin.stefan.fullcalendar;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.converters.JsonItemPropertyConverter;
import org.vaadin.stefan.fullcalendar.json.JsonConverter;
import org.vaadin.stefan.fullcalendar.json.JsonName;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The typed options of {@link FullCalendarScheduler#options()}: the core and the scheduler options share one options
 * object and one view options object, and the fields the scheduler data classes declare reach the client as the core
 * TypedOptionsSyncTest checks it for the core fields.
 */
class SchedulerTypedOptionsSyncTest extends BrowserlessTest {

    private FullCalendarScheduler calendar;
    private UI ui;

    @BeforeEach
    void setUp() {
        calendar = new FullCalendarScheduler();
        ui = UI.getCurrent();
    }

    private ObjectNode initialOptions() {
        return (ObjectNode) calendar.getElement().getPropertyRaw("initialOptions");
    }

    private void attach() {
        ui.add(calendar);
        respond();
    }

    /** Runs what the server does before its response and returns the option changes sent to the client. */
    private List<JsonNode> respond() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        return ui.getInternals().dumpPendingJavaScriptInvocations().stream()
                .map(PendingJavaScriptInvocation::getInvocation)
                .filter(invocation -> invocation.getExpression().contains("setOptions"))
                .map(invocation -> (JsonNode) invocation.getParameters().getLast())
                .toList();
    }

    @Test
    void coreAndSchedulerOptions_throughOneOptionsObject_bothArrive() {
        FullCalendarSchedulerOptions options = calendar.options();
        options.setWeekends(false);
        options.setResourceGroupField("building");
        attach();

        assertFalse(initialOptions().get("weekends").asBoolean());
        assertEquals("building", initialOptions().get("resourceGroupField").asString());

        options.setWeekends(true);
        options.setResourceGroupField("floor");
        List<JsonNode> changes = respond();

        assertEquals(1, changes.size());
        assertTrue(changes.getFirst().get("weekends").asBoolean());
        assertEquals("floor", changes.getFirst().get("resourceGroupField").asString());
    }

    @Test
    void view_setsACoreAndASchedulerFieldWithoutACast() {
        FullCalendarSchedulerViewOptions week = calendar.options().view(SchedulerView.RESOURCE_TIMELINE_WEEK);
        week.setWeekends(false);
        week.setResourcesInitiallyExpanded(false);
        attach();

        JsonNode views = initialOptions().get("views").get("resourceTimelineWeek");
        assertFalse(views.get("weekends").asBoolean());
        assertFalse(views.get("resourcesInitiallyExpanded").asBoolean());
        assertNull(initialOptions().get("weekends"));
        assertNull(initialOptions().get("resourcesInitiallyExpanded"));
        assertFalse(calendar.options().view(SchedulerView.RESOURCE_TIMELINE_WEEK).getResourcesInitiallyExpanded());
        assertNull(calendar.options().getResourcesInitiallyExpanded());
    }

    @Test
    void viewGetterAlone_addsNoViewsEntry() {
        assertNull(calendar.options().view(SchedulerView.RESOURCE_TIMELINE_WEEK).getResourceGroupField());
        attach();

        assertNull(initialOptions().get("views"));
    }

    @Test
    void keptViewReference_changesTheClientInALaterRequest() {
        FullCalendarSchedulerViewOptions week = calendar.options().view(SchedulerView.RESOURCE_TIMELINE_WEEK);
        attach();

        week.setResourceGroupField("building");
        List<JsonNode> changes = respond();

        assertEquals(1, changes.size());
        assertEquals("building",
                changes.getFirst().get("views").get("resourceTimelineWeek").get("resourceGroupField").asString());
    }

    /**
     * A field for an option the core client has its own handler for runs after that handler, as the core
     * TypedOptionsSyncTest checks it for the core fields. The scheduler client adds no handlers of its own; one added
     * later needs a check here too.
     */
    @Test
    void everyFieldForAnOptionWithAClientHandler_runsAfterIt() throws Exception {
        // Surefire runs the tests in the module directory, so the path is relative to addon-scheduler/.
        String client = Files.readString(Path.of(
                "../addon/src/main/resources/META-INF/frontend/vaadin-full-calendar/full-calendar.ts"));
        int start = client.indexOf("protected createEventHandlers() {");
        String handlers = client.substring(start, client.indexOf("\n    }\n", start));
        Set<String> handled = Pattern.compile("(?m)^ {12}(\\w+):").matcher(handlers).results()
                .map(match -> match.group(1))
                .collect(Collectors.toSet());
        assertTrue(handled.containsAll(List.of("dateClick", "eventClick", "datesSet")), "handlers not found");
        handled.remove("moreLinkClick");

        SchedulerConverterTest.optionFields().forEach(field -> {
            String name = field.isAnnotationPresent(JsonName.class)
                    ? field.getAnnotation(JsonName.class).value() : field.getName();
            if (handled.contains(name)) {
                assertTrue(FullCalendar.CALLBACKS_AFTER_CLIENT_HANDLER.contains(name), name);
            }
        });
    }

    /** A core calendar's options objects have no accessor of a scheduler field, so no scheduler key reaches it. */
    @Test
    void coreOptionsObjects_haveNoSchedulerAccessors() {
        Set<String> coreMethods = Arrays.stream(FullCalendarOptions.class.getMethods())
                .map(Method::getName)
                .collect(Collectors.toSet());
        Arrays.stream(FullCalendarViewOptions.class.getMethods()).map(Method::getName).forEach(coreMethods::add);

        SchedulerConverterTest.optionFields().forEach(field -> {
            String suffix = suffix(field);
            assertFalse(coreMethods.contains("get" + suffix), "get" + suffix);
            assertFalse(coreMethods.contains("set" + suffix), "set" + suffix);
        });
    }

    /**
     * A value for the fields of a type that is no value type, with the JSON the library reads for it. A field of a new
     * one-form type adds its type here; a type with several forms is a value type and goes to
     * SchedulerConverterTest.VALUE_TYPES.
     */
    private static final Map<Class<?>, Map.Entry<Object, JsonNode>> ONE_FORM_SAMPLES = Map.ofEntries(
            sample(Boolean.class, false, JsonUtils.toJsonNode(false)),
            sample(Integer.class, 2, JsonUtils.toJsonNode(2)),
            sample(String.class, "x", JsonUtils.toJsonNode("x")));

    /**
     * A value for each parameter type of the setters per form, with the client JSON the field must have after the
     * setter took it. An enum parameter with client values ({@link ClientSideValue}) needs no entry: each constant is
     * tried, with its client value expected.
     */
    private static final Map<Class<?>, Map.Entry<Object, JsonNode>> PARAMETER_SAMPLES = Map.ofEntries(
            sample(String.class, "x", JsonUtils.toJsonNode("x")),
            sample(int.class, 1, JsonUtils.toJsonNode(1)),
            sample(boolean.class, true, JsonUtils.toJsonNode(true)));

    /**
     * The parameter types of the setters per form of each value type a scheduler field has, the setter table of the
     * README section "Value types" of the option-field tickets.
     */
    private static final Map<Class<?>, Set<Class<?>>> PER_FORM_PARAMETERS = Map.of();

    /**
     * A second value for the parameter types with two values only, so a setter that ignores its argument and writes
     * one fixed value fails.
     */
    private static final Map<Class<?>, Map.Entry<Object, JsonNode>> SECOND_PARAMETER_SAMPLES = Map.ofEntries(
            sample(boolean.class, false, JsonUtils.toJsonNode(false)));

    private static Map.Entry<Class<?>, Map.Entry<Object, JsonNode>> sample(Class<?> type, Object value, JsonNode json) {
        return Map.entry(type, Map.entry(value, json));
    }

    /**
     * Every field the scheduler data classes declare has a getter, a setter of its type and its setters per form on
     * the scheduler's options objects, and its value reaches the client under its library name: calendar-wide, and for
     * a view under {@code views}.
     */
    @Test
    void everyField_isSetAndReadThroughTheOptionsObjects_andReachesTheClient() throws Exception {
        FullCalendarSchedulerViewOptions weekOptions = calendar.options().view(SchedulerView.RESOURCE_TIMELINE_WEEK);
        Map<String, JsonNode> expectedGlobal = new LinkedHashMap<>();
        Map<String, JsonNode> expectedWeek = new LinkedHashMap<>();
        for (Field field : SchedulerConverterTest.optionFields().toList()) {
            Map.Entry<Object, JsonNode> sample = sampleOf(field);
            String name = field.isAnnotationPresent(JsonName.class)
                    ? field.getAnnotation(JsonName.class).value() : field.getName();
            setAndGet(calendar.options(), field, sample.getKey());
            expectedGlobal.put(name, sample.getValue());
            if (field.getDeclaringClass() == SchedulerViewOptions.class) {
                setAndGet(weekOptions, field, sample.getKey());
                expectedWeek.put(name, sample.getValue());
            }
        }
        assertFalse(expectedGlobal.isEmpty());
        attach();

        expectedGlobal.forEach((name, json) -> assertEquals(json, initialOptions().get(name), name));
        JsonNode week = initialOptions().get("views").get("resourceTimelineWeek");
        expectedGlobal.forEach((name, json) -> assertEquals(expectedWeek.get(name), week.get(name),
                "views.resourceTimelineWeek." + name));
    }

    private static String suffix(Field field) {
        return Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
    }

    /** A value for the field and its client form. */
    private static Map.Entry<Object, JsonNode> sampleOf(Field field) {
        Class<?> type = field.getType();
        if (SchedulerConverterTest.VALUE_TYPES.containsKey(type)) {
            return SchedulerConverterTest.VALUE_TYPES.get(type).samples().entrySet().iterator().next();
        }
        if (type.isEnum() && ClientSideValue.class.isAssignableFrom(type) && !ONE_FORM_SAMPLES.containsKey(type)) {
            Object constant = type.getEnumConstants()[0];
            return Map.entry(constant, JsonUtils.toJsonNode(constant));
        }
        Map.Entry<Object, JsonNode> sample = ONE_FORM_SAMPLES.get(type);
        assertNotNull(sample, "Add a sample of " + type.getSimpleName() + " to ONE_FORM_SAMPLES, for " + field.getName()
                + ", or to SchedulerConverterTest.VALUE_TYPES when its option takes several forms");
        return sample;
    }

    /**
     * Sets the field through each of its setters: a setter per form must write the client form of its argument, the
     * setter of its type must set the given value, which the getter returns.
     */
    private static void setAndGet(Object options, Field field, Object value) throws ReflectiveOperationException {
        String suffix = suffix(field);
        Method setter, getter;
        try {
            setter = options.getClass().getMethod("set" + suffix, field.getType());
            getter = options.getClass().getMethod("get" + suffix);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(options.getClass().getSimpleName() + " has no set" + suffix + "("
                    + field.getType().getSimpleName() + ") or get" + suffix + "()", e);
        }
        List<Method> perFormSetters = Arrays.stream(options.getClass().getMethods())
                .filter(m -> m.getName().equals(setter.getName()) && !m.equals(setter) && !m.isBridge()
                        && m.getParameterCount() == 1)
                .toList();
        if (SchedulerConverterTest.VALUE_TYPES.containsKey(field.getType())) {
            Set<Class<?>> expected = PER_FORM_PARAMETERS.get(field.getType());
            assertNotNull(expected, "Add a row for " + field.getType().getSimpleName() + " to PER_FORM_PARAMETERS");
            assertEquals(expected,
                    perFormSetters.stream().map(m -> m.getParameterTypes()[0]).collect(Collectors.toSet()),
                    "The setters per form of " + field.getName() + " differ from the setter table");
        }
        for (Method perForm : perFormSetters) {
            for (Map.Entry<Object, JsonNode> argument : parameterSamples(perForm.getParameterTypes()[0], perForm)) {
                setter.invoke(options, (Object) null);
                perForm.invoke(options, argument.getKey());
                Object result = getter.invoke(options);
                assertNotNull(result, perForm + " sets no value");
                assertEquals(argument.getValue(), clientForm(field, result), perForm.toString());
            }
        }
        setter.invoke(options, value);
        assertEquals(value, getter.invoke(options), field.getName());
    }

    /**
     * Values for the parameter, each with the client JSON the field must have after the setter took it. An enum with
     * client values and no sample is tried with each constant.
     */
    private static List<Map.Entry<Object, JsonNode>> parameterSamples(Class<?> type, Method setter) {
        if (type.isEnum() && ClientSideValue.class.isAssignableFrom(type) && !PARAMETER_SAMPLES.containsKey(type)) {
            return Arrays.stream(type.getEnumConstants())
                    .map(constant -> Map.entry(constant, JsonUtils.toJsonNode(constant)))
                    .toList();
        }
        Map.Entry<Object, JsonNode> second = SECOND_PARAMETER_SAMPLES.get(type);
        return second == null ? List.of(parameterSample(type, setter)) : List.of(parameterSample(type, setter), second);
    }

    private static Map.Entry<Object, JsonNode> parameterSample(Class<?> type, Method setter) {
        if (type.isArray()) {
            Map.Entry<Object, JsonNode> element = parameterSamples(type.getComponentType(), setter).getFirst();
            Object array = Array.newInstance(type.getComponentType(), 1);
            Array.set(array, 0, element.getKey());
            return Map.entry(array, JsonFactory.createArray().add(element.getValue()));
        }
        Map.Entry<Object, JsonNode> sample = PARAMETER_SAMPLES.get(type);
        assertNotNull(sample, "Add a sample of " + type.getSimpleName() + " to PARAMETER_SAMPLES, for " + setter);
        return sample;
    }

    /** The JSON the sync sends for the value of the field. */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static JsonNode clientForm(Field field, Object value) throws ReflectiveOperationException {
        JsonConverter converter = field.getAnnotation(JsonConverter.class);
        return converter == null ? JsonUtils.toJsonNode(value)
                : ((JsonItemPropertyConverter) converter.value().getConstructor().newInstance())
                        .toClientModel(value, null);
    }
}
