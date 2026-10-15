package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.converters.*;
import org.vaadin.stefan.fullcalendar.json.JsonConverter;
import org.vaadin.stefan.fullcalendar.model.Header;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.NullNode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the option converter classes.
 */
class ConverterTest {

    // -------------------------------------------------------------------------
    // Value-or-callback converters: client form and back
    // -------------------------------------------------------------------------

    /**
     * A value type with the converter its fields use, and values of each form with the JSON the library reads for them.
     */
    record ValueType(Class<? extends JsonItemPropertyConverter<?, ?>> converter, Map<Object, JsonNode> samples) {
    }

    private static final JsCallback CALLBACK = JsCallback.of("() => {}");

    /** Pairs of a value and its client form; a client form that is no {@link JsonNode} is converted to one. */
    private static Map<Object, JsonNode> forms(Object... valuesAndClientForms) {
        Map<Object, JsonNode> forms = new LinkedHashMap<>();
        for (int i = 0; i < valuesAndClientForms.length; i += 2) {
            Object clientForm = valuesAndClientForms[i + 1];
            forms.put(valuesAndClientForms[i],
                    clientForm instanceof JsonNode node ? node : JsonUtils.toJsonNode(clientForm));
        }
        return forms;
    }

    private static JsonNode array(BusinessHours... hours) {
        ArrayNode array = JsonFactory.createArray();
        Arrays.stream(hours).forEach(h -> array.add(h.toJson()));
        return array;
    }

    /**
     * Every value type, with at least one value per form and the client form of each. A new value type adds itself
     * here. Every value type has a converter (spec), so the scan below finds the fields of value types by the
     * converter.
     */
    static final Map<Class<?>, ValueType> VALUE_TYPES = Map.ofEntries(
            Map.entry(AutoBoolean.class, new ValueType(AutoBooleanConverter.class, forms(
                    AutoBoolean.TRUE, true, AutoBoolean.FALSE, false, AutoBoolean.AUTO, "auto"))),
            Map.entry(BusinessHoursValue.class, new ValueType(BusinessHoursValueConverter.class, forms(
                    BusinessHoursValue.NONE, false, BusinessHoursValue.DEFAULT, true,
                    BusinessHoursValue.of(BusinessHours.businessWeek().start(9).end(17),
                            BusinessHours.of(DayOfWeek.SATURDAY).start(LocalTime.of(10, 30)).end(24)),
                    array(BusinessHours.businessWeek().start(9).end(17),
                            BusinessHours.of(DayOfWeek.SATURDAY).start(LocalTime.of(10, 30)).end(24))))),
            Map.entry(ClassNameValue.class, new ValueType(ClassNameValueConverter.class, forms(
                    ClassName.of("a b"), "a b", CALLBACK, CALLBACK.toMarkerJson()))),
            Map.entry(ConstraintValue.class, new ValueType(ConstraintValueConverter.class, forms(
                    ConstraintValue.ofGroupId("meetings"), "meetings",
                    ConstraintValue.BUSINESS_HOURS, "businessHours",
                    ConstraintValue.ofWindows(BusinessHours.businessWeek().start(10).end(18)),
                    array(BusinessHours.businessWeek().start(10).end(18)),
                    ConstraintValue.ofWindows(BusinessHours.of(DayOfWeek.MONDAY), BusinessHours.of(DayOfWeek.FRIDAY)),
                    array(BusinessHours.of(DayOfWeek.MONDAY), BusinessHours.of(DayOfWeek.FRIDAY))))),
            Map.entry(ContentValue.class, new ValueType(ContentValueConverter.class, forms(
                    TextContent.of("all day"), "all day", CALLBACK, CALLBACK.toMarkerJson()))),
            Map.entry(CssSelectorValue.class, new ValueType(CssSelectorValueConverter.class, forms(
                    CssSelector.of(".item"), ".item", CALLBACK, CALLBACK.toMarkerJson()))),
            Map.entry(DateFormatValue.class, new ValueType(DateFormatValueConverter.class, forms(
                    DateFormat.of(Map.of("month", "short", "day", "numeric", "omitCommas", true, "hour12", false)),
                    JsonFactory.createObject().put("month", "short").put("day", "numeric").put("omitCommas", true)
                            .put("hour12", false),
                    CALLBACK, CALLBACK.toMarkerJson()))),
            Map.entry(EntryLimitValue.class, new ValueType(EntryLimitValueConverter.class, forms(
                    EntryLimitValue.NONE, false, EntryLimitValue.CELL_HEIGHT, true,
                    EntryLimitValue.of(0), 0, EntryLimitValue.of(3), 3))),
            Map.entry(HeightValue.class, new ValueType(HeightValueConverter.class, forms(
                    HeightValue.AUTO, "auto", HeightValue.ofPixels(0), 0, HeightValue.ofPixels(650), 650,
                    HeightValue.ofCss("50vh"), "50vh"))),
            Map.entry(HeaderAlignValue.class, new ValueType(HeaderAlignValueConverter.class, forms(
                    HeaderAlign.START, "start", HeaderAlign.CENTER, "center", HeaderAlign.END, "end",
                    CALLBACK, CALLBACK.toMarkerJson()))),
            Map.entry(ListDayFormatValue.class, new ValueType(ListDayFormatValueConverter.class, forms(
                    DateFormat.of(Map.of("weekday", "long")), JsonFactory.createObject().put("weekday", "long"),
                    CALLBACK, CALLBACK.toMarkerJson(), ListDayFormatValue.NONE, false))),
            Map.entry(ListTextValue.class, new ValueType(ListTextValueConverter.class, forms(
                    TextContent.of("Agenda"), "Agenda", ListTextValue.NONE, false))),
            Map.entry(NowValue.class, new ValueType(NowValueConverter.class, forms(
                    NowValue.ofDay(LocalDate.of(2026, 10, 15)), "2026-10-15",
                    NowValue.ofDateTime(LocalDateTime.of(2026, 10, 15, 9, 30)), "2026-10-15T09:30Z",
                    CALLBACK, CALLBACK.toMarkerJson()))),
            Map.entry(OverlapValue.class, new ValueType(OverlapValueConverter.class, forms(
                    Overlap.ALLOWED, true, Overlap.FORBIDDEN, false, CALLBACK, CALLBACK.toMarkerJson()))),
            Map.entry(StickyValue.class, new ValueType(StickyValueConverter.class, forms(
                    StickyValue.NOT_STICKY, false, StickyValue.STICKY, true, StickyValue.of("2em"), "2em"))),
            Map.entry(ViewNameValue.class, new ValueType(ViewNameValueConverter.class, forms(
                    ViewName.of("timeGridDay"), "timeGridDay", CALLBACK, CALLBACK.toMarkerJson()))));

    /**
     * Field types with a converter that are no value type, since their options take one form only, with the converter
     * their fields use.
     */
    static final Map<Class<?>, Class<?>> ONE_FORM_TYPES = Map.of(
            DayOfWeek.class, DayOfWeekConverter.class,
            LocalDate.class, LocalDateConverter.class);

    /**
     * Field types the sync writes without a converter: JSON scalars, a callback, and types with a client value
     * ({@link ClientSideValue}). Every other field type needs a converter. Add a type here only when the library
     * reads what {@link JsonUtils#toJsonNode(Object)} writes for it.
     */
    private static final Set<Class<?>> WRITTEN_AS_THEY_ARE =
            Set.of(Boolean.class, Integer.class, Double.class, String.class, JsCallback.class);

    /** The fields of the option data classes and their superclasses. */
    private static Stream<Field> optionFields() {
        return Stream.of(CalendarOptions.class, ViewOptions.class)
                .<Class<?>>mapMulti((type, downstream) -> {
                    for (Class<?> c = type; c != Object.class; c = c.getSuperclass()) {
                        downstream.accept(c);
                    }
                })
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

    @Test
    void valueTypeConverters_readFurtherClientForms() {
        assertEquals(StickyValue.of("10px"),
                new StickyValueConverter().toServerModel(JsonUtils.toJsonNode(10), null));
        assertEquals(StickyValue.of("10.5px"),
                new StickyValueConverter().toServerModel(JsonUtils.toJsonNode(10.5), null));
    }

    @Test
    void businessHoursValue_writesTheEndOfTheDayAs24() {
        JsonNode endOfDay = new BusinessHoursValueConverter().toClientModel(
                BusinessHoursValue.of(BusinessHours.of(DayOfWeek.SUNDAY).start(LocalTime.of(10, 30)).end(24)), null);
        assertEquals("10:30", endOfDay.get(0).get("startTime").asString());
        assertEquals("24:00", endOfDay.get(0).get("endTime").asString());
        assertEquals(0, endOfDay.get(0).get("daysOfWeek").get(0).asInt());
    }

    /** A value-or-callback type that JsCallback implements is a value type, with or without fields yet. */
    @Test
    void everyTypeJsCallbackImplements_isAValueType() {
        assertTrue(VALUE_TYPES.keySet().containsAll(Set.of(JsCallback.class.getInterfaces())),
                "A value type JsCallback implements belongs into VALUE_TYPES");
    }

    @Test
    void valueTypes_rejectValuesTheLibraryReadsDifferently() {
        assertThrows(IllegalArgumentException.class, () -> ConstraintValue.ofGroupId("businessHours"));
        assertThrows(IllegalArgumentException.class, () -> EntryLimitValue.of(-1));
        assertThrows(IllegalArgumentException.class, ConstraintValue::ofWindows);
        assertThrows(IllegalArgumentException.class, BusinessHoursValue::of);
        assertThrows(IllegalArgumentException.class, () -> DateFormat.of(Map.of("month", List.of("short"))));
        assertThrows(IllegalArgumentException.class, () -> HeightValue.ofPixels(-1));
        assertThrows(IllegalArgumentException.class, () -> HeightValue.ofCss("auto"));
    }

    @Test
    void constraintValueConverter_readsASingleObjectAsOneWindow() {
        assertEquals(ConstraintValue.ofWindows(BusinessHours.businessWeek()),
                new ConstraintValueConverter().toServerModel(BusinessHours.businessWeek().toJson(), null));
    }

    @Test
    void valueTypeConverters_rejectOtherClientValues() {
        assertThrows(IllegalArgumentException.class,
                () -> new BusinessHoursValueConverter().toServerModel(JsonUtils.toJsonNode("x"), null));
        JsonNode incomplete = JsonFactory.createArray().add(JsonFactory.createObject().put("startTime", "10:00"));
        assertThrows(IllegalArgumentException.class,
                () -> new BusinessHoursValueConverter().toServerModel(incomplete, null));
        assertThrows(IllegalArgumentException.class,
                () -> new ConstraintValueConverter().toServerModel(JsonUtils.toJsonNode(1), null));
        assertThrows(IllegalArgumentException.class,
                () -> new ConstraintValueConverter().toServerModel(JsonFactory.createArray().add(1), null));
        assertThrows(IllegalArgumentException.class,
                () -> new ConstraintValueConverter().toServerModel(JsonFactory.createArray(), null));
        assertThrows(IllegalArgumentException.class,
                () -> new EntryLimitValueConverter().toServerModel(JsonUtils.toJsonNode(-1), null));
        assertThrows(IllegalArgumentException.class,
                () -> new EntryLimitValueConverter().toServerModel(JsonUtils.toJsonNode(3.5), null));
        assertThrows(IllegalArgumentException.class,
                () -> new EntryLimitValueConverter().toServerModel(JsonUtils.toJsonNode("x"), null));
        assertThrows(IllegalArgumentException.class,
                () -> new OverlapValueConverter().toServerModel(JsonUtils.toJsonNode("x"), null));
        assertThrows(IllegalArgumentException.class,
                () -> new AutoBooleanConverter().toServerModel(JsonUtils.toJsonNode("x"), null));
        assertThrows(IllegalArgumentException.class,
                () -> new HeightValueConverter().toServerModel(JsonUtils.toJsonNode(true), null));
        assertThrows(IllegalArgumentException.class,
                () -> new ListTextValueConverter().toServerModel(JsonUtils.toJsonNode(true), null));
        assertThrows(IllegalArgumentException.class, () -> new DateFormatValueConverter()
                .toServerModel(JsonFactory.createObject().set("month", JsonFactory.createArray()), null));
        assertThrows(IllegalArgumentException.class,
                () -> new NowValueConverter().toServerModel(JsonUtils.toJsonNode(1), null));
        assertThrows(IllegalArgumentException.class,
                () -> new StickyValueConverter().toServerModel(JsonFactory.createArray(), null));
        assertThrows(IllegalArgumentException.class,
                () -> new ListDayFormatValueConverter().toServerModel(JsonUtils.toJsonNode(true), null));
        assertThrows(IllegalArgumentException.class,
                () -> new ListDayFormatValueConverter().toServerModel(JsonUtils.toJsonNode("MMM d"), null));
    }

    @Test
    void valueTypes_withListsKeepTheirValueWhenThePassedListChanges() {
        List<BusinessHours> hours = new ArrayList<>(List.of(BusinessHours.businessWeek()));
        BusinessHoursValue value = new BusinessHoursValue.Hours(hours);
        hours.add(BusinessHours.allDays());
        assertEquals(List.of(BusinessHours.businessWeek()), ((BusinessHoursValue.Hours) value).hours());
        assertThrows(UnsupportedOperationException.class,
                () -> ((BusinessHoursValue.Hours) value).hours().add(BusinessHours.allDays()));

        List<BusinessHours> windows = new ArrayList<>(List.of(BusinessHours.businessWeek()));
        ConstraintValue constraint = new ConstraintValue.Windows(windows);
        windows.add(BusinessHours.allDays());
        assertEquals(ConstraintValue.ofWindows(BusinessHours.businessWeek()), constraint);

        Set<DayOfWeek> days = new HashSet<>(Set.of(DayOfWeek.MONDAY));
        BusinessHours monday = BusinessHours.of(days);
        days.add(DayOfWeek.TUESDAY);
        assertEquals(Set.of(DayOfWeek.MONDAY), monday.getDayOfWeeks());
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

        assertFalse(convertedFields.isEmpty());
        for (Field field : convertedFields) {
            Class<?> type = field.getType();
            Class<?> expected = VALUE_TYPES.containsKey(type) ? VALUE_TYPES.get(type).converter() : ONE_FORM_TYPES.get(type);
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

    // -------------------------------------------------------------------------
    // ClassNameConverter
    // -------------------------------------------------------------------------

    @Test
    void className_joinsWithSpaces() {
        ClassNameConverter conv = new ClassNameConverter();
        assertTrue(conv.supports(List.of("a", "b")));
        assertEquals("a b", conv.toClientModel(List.of("a", "b"), null).asString());
    }

    @Test
    void className_splitsOnWhitespace() {
        ClassNameConverter conv = new ClassNameConverter();
        assertEquals(Set.of("a", "b"), conv.toServerModel(JsonUtils.toJsonNode(" a \tb "), null));
    }

    // -------------------------------------------------------------------------
    // DurationConverter
    // -------------------------------------------------------------------------

    @Test
    void duration_ofHours_convertsToString() {
        DurationConverter conv = new DurationConverter();
        assertTrue(conv.supports(Duration.ofHours(2)));
        JsonNode result = conv.toClientModel(Duration.ofHours(2), null);
        assertEquals("02:00:00", result.asString());
    }

    @Test
    void duration_ofMinutes_convertsToString() {
        DurationConverter conv = new DurationConverter();
        JsonNode result = conv.toClientModel(Duration.ofMinutes(30), null);
        assertEquals("00:30:00", result.asString());
    }

    @Test
    void duration_negative_hasOneLeadingSign() {
        DurationConverter conv = new DurationConverter();
        assertEquals("-00:30:00", conv.toClientModel(Duration.ofMinutes(-30), null).asString());
        assertEquals("-01:30:00", conv.toClientModel(Duration.ofMinutes(-90), null).asString());
    }

    @Test
    void duration_withMillis_appendsThreeDigits() {
        DurationConverter conv = new DurationConverter();
        assertEquals("00:00:01.500", conv.toClientModel(Duration.ofMillis(1500), null).asString());
        assertEquals("00:00:00.005", conv.toClientModel(Duration.ofMillis(5), null).asString());
        assertEquals("-00:00:01.500", conv.toClientModel(Duration.ofMillis(-1500), null).asString());
        assertEquals("-00:00:00.005", conv.toClientModel(Duration.ofMillis(-5), null).asString());
        assertEquals("25:00:00.007", conv.toClientModel(Duration.ofHours(25).plusMillis(7), null).asString());
        assertEquals("00:00:01", conv.toClientModel(Duration.ofMillis(1000), null).asString());
    }

    @Test
    void duration_fromClient_readsClientFormat() {
        DurationConverter conv = new DurationConverter();
        assertEquals(Duration.ofMinutes(30), conv.toServerModel(JsonUtils.toJsonNode("00:30"), null));
        assertEquals(Duration.ofHours(84), conv.toServerModel(JsonUtils.toJsonNode("84:00:00"), null));
        assertEquals(Duration.ofDays(3).plusHours(12), conv.toServerModel(JsonUtils.toJsonNode("3.12:00:00"), null));
        assertEquals(Duration.ofMinutes(-90), conv.toServerModel(JsonUtils.toJsonNode("-01:30:00"), null));
        assertEquals(Duration.ofMillis(1500), conv.toServerModel(JsonUtils.toJsonNode("00:00:01.500"), null));
        assertEquals(Duration.ofHours(-26), conv.toServerModel(JsonUtils.toJsonNode("-1.02:00:00"), null));
        // like the client, a match at the start is enough, the rest is ignored
        assertEquals(Duration.ofHours(1), conv.toServerModel(JsonUtils.toJsonNode("01:00:00 extra"), null));
    }

    @Test
    void duration_fromClient_otherFormatIsNull() {
        DurationConverter conv = new DurationConverter();
        assertNull(conv.toServerModel(JsonUtils.toJsonNode("P3D"), null));
        assertNull(conv.toServerModel(NullNode.instance, null));
    }

    @Test
    void duration_roundTrip() {
        DurationConverter conv = new DurationConverter();
        for (Duration d : List.of(Duration.ofMinutes(-30), Duration.ofHours(25).plusMillis(7), Duration.ofDays(3))) {
            assertEquals(d, conv.toServerModel(conv.toClientModel(d, null), null));
        }
    }

    @Test
    void duration_belowMillis_isDropped() {
        DurationConverter conv = new DurationConverter();
        assertEquals("00:00:01", conv.toClientModel(Duration.ofSeconds(1).plusNanos(999), null).asString());
    }

    @Test
    void duration_localTime_convertsToString() {
        DurationConverter conv = new DurationConverter();
        assertTrue(conv.supports(LocalTime.of(9, 30)));
        JsonNode result = conv.toClientModel(LocalTime.of(9, 30), null);
        assertEquals("09:30:00", result.asString());
    }

    @Test
    void duration_doesNotSupportString() {
        DurationConverter conv = new DurationConverter();
        // Strings bypass the converter — they go directly through JsonUtils.toJsonNode
        assertFalse(conv.supports("01:00:00"));
    }

    // -------------------------------------------------------------------------
    // DayOfWeekArrayConverter
    // -------------------------------------------------------------------------

    @Test
    void dayOfWeekArray_convertsToIntArray() {
        DayOfWeekArrayConverter conv = new DayOfWeekArrayConverter();
        assertTrue(conv.supports(new DayOfWeek[]{DayOfWeek.MONDAY}));
        JsonNode result = conv.toClientModel(new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.FRIDAY}, null);
        assertTrue(result.isArray());
        ArrayNode arr = (ArrayNode) result;
        assertEquals(2, arr.size());
        // FC uses 0=Sunday, 1=Monday, ..., 5=Friday
        assertEquals(1, arr.get(0).asInt());
        assertEquals(5, arr.get(1).asInt());
    }

    @Test
    void dayOfWeekArray_collection_converts() {
        DayOfWeekArrayConverter conv = new DayOfWeekArrayConverter();
        assertTrue(conv.supports(List.of(DayOfWeek.SATURDAY)));
        JsonNode result = conv.toClientModel(Set.of(DayOfWeek.SUNDAY), null);
        assertTrue(result.isArray());
        assertEquals(1, ((ArrayNode) result).size());
        assertEquals(0, ((ArrayNode) result).get(0).asInt()); // Sunday = 0
    }

    // -------------------------------------------------------------------------
    // StringArrayConverter
    // -------------------------------------------------------------------------

    @Test
    void stringArray_convertsToCommaJoinedString() {
        StringArrayConverter conv = new StringArrayConverter();
        assertTrue(conv.supports(new String[]{"a", "b"}));
        JsonNode result = conv.toClientModel(new String[]{"a", "b"}, null);
        assertTrue(result.isString());
        assertEquals("a,b", result.asString());
    }

    @Test
    void stringArray_collection_convertsToCommaJoinedString() {
        StringArrayConverter conv = new StringArrayConverter();
        assertTrue(conv.supports(List.of("x")));
        JsonNode result = conv.toClientModel(List.of("x", "y"), null);
        assertTrue(result.isString());
        assertEquals("x,y", result.asString());
    }

    // -------------------------------------------------------------------------
    // DayOfWeekConverter
    // -------------------------------------------------------------------------

    @Test
    void dayOfWeek_monday_converts() {
        DayOfWeekConverter conv = new DayOfWeekConverter();
        assertTrue(conv.supports(DayOfWeek.MONDAY));
        JsonNode result = conv.toClientModel(DayOfWeek.MONDAY, null);
        assertEquals(1, result.asInt());
    }

    @Test
    void dayOfWeek_sunday_converts() {
        DayOfWeekConverter conv = new DayOfWeekConverter();
        JsonNode result = conv.toClientModel(DayOfWeek.SUNDAY, null);
        assertEquals(0, result.asInt());
    }

    // -------------------------------------------------------------------------
    // LocaleConverter
    // -------------------------------------------------------------------------

    @Test
    void locale_converts() {
        LocaleConverter conv = new LocaleConverter();
        assertTrue(conv.supports(java.util.Locale.GERMAN));
        JsonNode result = conv.toClientModel(java.util.Locale.GERMAN, null);
        assertEquals("de", result.asString());
    }

    // -------------------------------------------------------------------------
    // BusinessHoursConverter
    // -------------------------------------------------------------------------

    @Test
    void businessHours_singleInstance_converts() {
        BusinessHoursConverter conv = new BusinessHoursConverter();
        BusinessHours bh = BusinessHours.businessWeek().start(9).end(17);
        assertTrue(conv.supports(bh));
        JsonNode result = conv.toClientModel(bh, null);
        assertTrue(result.isObject());
    }

    @Test
    void businessHours_array_converts() {
        BusinessHoursConverter conv = new BusinessHoursConverter();
        BusinessHours[] arr = {
                BusinessHours.businessWeek().start(9).end(17),
                BusinessHours.of(DayOfWeek.SATURDAY).start(10).end(14)
        };
        assertTrue(conv.supports(arr));
        JsonNode result = conv.toClientModel(arr, null);
        assertTrue(result.isArray());
        assertEquals(2, ((ArrayNode) result).size());
    }
}
