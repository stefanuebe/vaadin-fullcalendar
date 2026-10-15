package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.converters.*;
import org.vaadin.stefan.fullcalendar.json.JsonConverter;
import org.vaadin.stefan.fullcalendar.model.Header;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.NullNode;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
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

    /** A value type with the converter its fields use and values of each form. */
    private record ValueType(Class<? extends JsonItemPropertyConverter<?, ?>> converter, List<Object> samples) {
    }

    /**
     * Every value type, with at least one value per form. A new value type adds itself here. Every value type has a
     * converter (spec), so the scan below finds the fields of value types by the converter.
     */
    private static final Map<Class<?>, ValueType> VALUE_TYPES = Map.of(
            ClassNameValue.class, new ValueType(ClassNameValueConverter.class,
                    List.of(ClassName.of("a b"), JsCallback.of("info => 'a'"))),
            ContentValue.class, new ValueType(ContentValueConverter.class,
                    List.of(TextContent.of("all day"), JsCallback.of("info => info.text"))),
            CssSelectorValue.class, new ValueType(CssSelectorValueConverter.class,
                    List.of(CssSelector.of(".item"), JsCallback.of("el => true"))),
            HeaderAlignValue.class, new ValueType(HeaderAlignValueConverter.class,
                    Stream.<Object>concat(Arrays.stream(HeaderAlign.values()), Stream.of(JsCallback.of("() => 'end'")))
                            .toList()),
            OverlapValue.class, new ValueType(OverlapValueConverter.class,
                    List.of(Overlap.ALLOWED, Overlap.FORBIDDEN, JsCallback.of("(still, moving) => false"))),
            ViewNameValue.class, new ValueType(ViewNameValueConverter.class,
                    List.of(ViewName.of("timeGridDay"), JsCallback.of("(date, jsEvent) => {}"))));

    /** Field types with a converter that are no value type, since their options take one form only. */
    private static final Set<Class<?>> ONE_FORM_TYPES = Set.of(DayOfWeek.class, LocalDate.class);

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
    void everyValueType_roundTripsThroughItsConverter() throws ReflectiveOperationException {
        for (var entry : VALUE_TYPES.entrySet()) {
            JsonItemPropertyConverter converter = entry.getValue().converter().getConstructor().newInstance();
            for (Object value : entry.getValue().samples()) {
                assertTrue(entry.getKey().isInstance(value), entry.getKey() + ": " + value);
                assertTrue(converter.supports(value), entry.getKey() + ": " + value);
                assertEquals(value, converter.toServerModel(converter.toClientModel(value, null), null));
            }
        }
    }

    @Test
    void valueTypes_haveTheClientFormOfTheLibrary() {
        JsCallback callback = JsCallback.of("() => {}");
        assertEquals(JsonUtils.toJsonNode(true), new OverlapValueConverter().toClientModel(Overlap.ALLOWED, null));
        assertEquals(JsonUtils.toJsonNode(false), new OverlapValueConverter().toClientModel(Overlap.FORBIDDEN, null));
        assertEquals(JsonUtils.toJsonNode(".item"),
                new CssSelectorValueConverter().toClientModel(CssSelector.of(".item"), null));
        assertEquals(JsonUtils.toJsonNode("timeGridDay"),
                new ViewNameValueConverter().toClientModel(ViewName.of("timeGridDay"), null));
        assertEquals(callback.toMarkerJson(), new OverlapValueConverter().toClientModel(callback, null));
        assertEquals(callback.toMarkerJson(), new CssSelectorValueConverter().toClientModel(callback, null));
        assertEquals(callback.toMarkerJson(), new ViewNameValueConverter().toClientModel(callback, null));
    }

    /** A value-or-callback type that JsCallback implements is a value type, with or without fields yet. */
    @Test
    void everyTypeJsCallbackImplements_isAValueType() {
        assertEquals(Set.of(JsCallback.class.getInterfaces()), VALUE_TYPES.keySet(),
                "A value type JsCallback implements belongs into VALUE_TYPES");
    }

    /**
     * A field with a converter or of a sealed type is of a value type and uses its converter, unless its type is in
     * {@link #ONE_FORM_TYPES}.
     */
    @Test
    void everyValueTypeField_usesTheConverterOfItsType() {
        List<Field> valueTypeFields = optionFields()
                .filter(field -> field.isAnnotationPresent(JsonConverter.class) || field.getType().isSealed())
                .filter(field -> !ONE_FORM_TYPES.contains(field.getType()))
                .toList();

        assertFalse(valueTypeFields.isEmpty());
        for (Field field : valueTypeFields) {
            ValueType valueType = VALUE_TYPES.get(field.getType());
            assertNotNull(valueType, "Not in VALUE_TYPES: " + field.getType().getSimpleName() + " of " + field.getName());
            JsonConverter annotation = field.getAnnotation(JsonConverter.class);
            assertNotNull(annotation, "No converter on " + field.getName());
            assertEquals(valueType.converter(), annotation.value(), field.getName());
        }
    }

    @Test
    void valueTypeSamples_coverEveryForm() {
        VALUE_TYPES.forEach((type, valueType) -> {
            Set<Object> forms = valueType.samples().stream()
                    .map(value -> value instanceof Enum<?> constant ? constant : value.getClass())
                    .collect(Collectors.toSet());
            for (Class<?> form : type.getPermittedSubclasses()) {
                if (form.isEnum()) {
                    assertTrue(forms.containsAll(List.of(form.getEnumConstants())), type + ": " + form);
                } else {
                    assertTrue(forms.contains(form), type + ": " + form);
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
