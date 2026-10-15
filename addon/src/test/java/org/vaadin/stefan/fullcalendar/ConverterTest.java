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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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
    record ValueType(Class<? extends JsonItemPropertyConverter<?, ?>> converter, List<Object> samples) {
    }

    /**
     * Every value type, with at least one value per form. A new value type adds itself here. Every value type has a
     * converter (spec), so the scan below finds the fields of value types by the converter.
     */
    static final Map<Class<?>, ValueType> VALUE_TYPES = Map.ofEntries(
            Map.entry(AutoBoolean.class, new ValueType(AutoBooleanConverter.class, List.of(AutoBoolean.values()))),
            Map.entry(BusinessHoursValue.class, new ValueType(BusinessHoursValueConverter.class, List.of(
                    BusinessHoursValue.NONE, BusinessHoursValue.DEFAULT,
                    BusinessHoursValue.of(BusinessHours.businessWeek().start(9).end(17),
                            BusinessHours.of(DayOfWeek.SATURDAY).start(LocalTime.of(10, 30)).end(24))))),
            Map.entry(ClassNameValue.class, new ValueType(ClassNameValueConverter.class,
                    List.of(ClassName.of("a b"), JsCallback.of("info => 'a'")))),
            Map.entry(ConstraintValue.class, new ValueType(ConstraintValueConverter.class, List.of(
                    ConstraintValue.groupId("meetings"), ConstraintValue.BUSINESS_HOURS,
                    ConstraintValue.windows(BusinessHours.businessWeek().start(10).end(18)),
                    ConstraintValue.windows(BusinessHours.of(DayOfWeek.MONDAY), BusinessHours.of(DayOfWeek.FRIDAY))))),
            Map.entry(ContentValue.class, new ValueType(ContentValueConverter.class,
                    List.of(TextContent.of("all day"), JsCallback.of("info => info.text")))),
            Map.entry(CssSelectorValue.class, new ValueType(CssSelectorValueConverter.class,
                    List.of(CssSelector.of(".item"), JsCallback.of("el => true")))),
            Map.entry(DateFormatValue.class, new ValueType(DateFormatValueConverter.class, List.of(
                    DateFormat.of(Map.of("month", "short", "day", "numeric", "omitCommas", true, "hour12", false)),
                    JsCallback.of("arg => arg.date.year")))),
            Map.entry(EntryLimit.class, new ValueType(EntryLimitConverter.class,
                    List.of(EntryLimit.NONE, EntryLimit.CELL_HEIGHT, EntryLimit.of(0), EntryLimit.of(3)))),
            Map.entry(HeightValue.class, new ValueType(HeightValueConverter.class,
                    List.of(HeightValue.AUTO, HeightValue.ofPixels(0), HeightValue.ofPixels(650),
                            HeightValue.ofCss("50vh")))),
            Map.entry(HeaderAlignValue.class, new ValueType(HeaderAlignValueConverter.class,
                    Stream.<Object>concat(Arrays.stream(HeaderAlign.values()), Stream.of(JsCallback.of("() => 'end'")))
                            .toList())),
            Map.entry(ListDayFormatValue.class, new ValueType(ListDayFormatValueConverter.class, List.of(
                    DateFormat.of(Map.of("weekday", "long")), JsCallback.of("arg => 'day'"), Hidden.INSTANCE))),
            Map.entry(ListTextValue.class, new ValueType(ListTextValueConverter.class,
                    List.of(TextContent.of("Agenda"), Hidden.INSTANCE))),
            Map.entry(NowValue.class, new ValueType(NowValueConverter.class, List.of(
                    NowValue.of(LocalDate.of(2026, 10, 15)), NowValue.of(LocalDateTime.of(2026, 10, 15, 9, 30)),
                    JsCallback.of("() => new Date()")))),
            Map.entry(OverlapValue.class, new ValueType(OverlapValueConverter.class,
                    List.of(Overlap.ALLOWED, Overlap.FORBIDDEN, JsCallback.of("(still, moving) => false")))),
            Map.entry(StickyValue.class, new ValueType(StickyValueConverter.class,
                    List.of(StickyValue.NOT_STICKY, StickyValue.STICKY, StickyValue.offset("2em")))),
            Map.entry(ViewNameValue.class, new ValueType(ViewNameValueConverter.class,
                    List.of(ViewName.of("timeGridDay"), JsCallback.of("(date, jsEvent) => {}")))));

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
        assertEquals(JsonUtils.toJsonNode(false),
                new BusinessHoursValueConverter().toClientModel(BusinessHoursValue.NONE, null));
        assertEquals(JsonUtils.toJsonNode(true),
                new BusinessHoursValueConverter().toClientModel(BusinessHoursValue.DEFAULT, null));
        assertEquals(JsonFactory.createArray().add(BusinessHours.businessWeek().toJson()),
                new BusinessHoursValueConverter()
                        .toClientModel(BusinessHoursValue.of(BusinessHours.businessWeek()), null));
        assertEquals(JsonUtils.toJsonNode("businessHours"),
                new ConstraintValueConverter().toClientModel(ConstraintValue.BUSINESS_HOURS, null));
        assertEquals(JsonUtils.toJsonNode("meetings"),
                new ConstraintValueConverter().toClientModel(ConstraintValue.groupId("meetings"), null));
        assertEquals(JsonFactory.createArray().add(BusinessHours.businessWeek().toJson()),
                new ConstraintValueConverter()
                        .toClientModel(ConstraintValue.windows(BusinessHours.businessWeek()), null));
        assertEquals(JsonUtils.toJsonNode("auto"), new AutoBooleanConverter().toClientModel(AutoBoolean.AUTO, null));
        assertEquals(JsonUtils.toJsonNode(true), new AutoBooleanConverter().toClientModel(AutoBoolean.TRUE, null));
        assertEquals(JsonUtils.toJsonNode("auto"), new HeightValueConverter().toClientModel(HeightValue.AUTO, null));
        assertEquals(JsonUtils.toJsonNode(650),
                new HeightValueConverter().toClientModel(HeightValue.ofPixels(650), null));
        assertEquals(JsonUtils.toJsonNode(false),
                new StickyValueConverter().toClientModel(StickyValue.NOT_STICKY, null));
        assertEquals(StickyValue.offset("10px"),
                new StickyValueConverter().toServerModel(JsonUtils.toJsonNode(10), null));
        assertEquals(JsonUtils.toJsonNode("2em"),
                new StickyValueConverter().toClientModel(StickyValue.offset("2em"), null));
        assertEquals(JsonUtils.toJsonNode(false), new ListTextValueConverter().toClientModel(Hidden.INSTANCE, null));
        assertEquals(JsonUtils.toJsonNode(false),
                new ListDayFormatValueConverter().toClientModel(Hidden.INSTANCE, null));
        assertEquals(JsonFactory.createObject().put("weekday", "long"),
                new DateFormatValueConverter().toClientModel(DateFormat.of(Map.of("weekday", "long")), null));
        assertEquals(JsonUtils.toJsonNode("50vh"),
                new HeightValueConverter().toClientModel(HeightValue.ofCss("50vh"), null));
        assertEquals(StickyValue.offset("10.5px"),
                new StickyValueConverter().toServerModel(JsonUtils.toJsonNode(10.5), null));
        assertEquals(JsonUtils.toJsonNode("Agenda"),
                new ListTextValueConverter().toClientModel(TextContent.of("Agenda"), null));
        assertEquals(JsonFactory.createObject().put("hour12", false).put("minute", 2),
                new DateFormatValueConverter()
                        .toClientModel(DateFormat.of(Map.of("hour12", false, "minute", 2)), null));
        assertEquals(callback.toMarkerJson(), new NowValueConverter().toClientModel(callback, null));
        assertEquals(callback.toMarkerJson(), new ListDayFormatValueConverter().toClientModel(callback, null));
        assertEquals(JsonUtils.toJsonNode("2026-10-15"),
                new NowValueConverter().toClientModel(NowValue.of(LocalDate.of(2026, 10, 15)), null));
        assertEquals(JsonUtils.toJsonNode("2026-10-15T09:30Z"), new NowValueConverter()
                .toClientModel(NowValue.of(LocalDateTime.of(2026, 10, 15, 9, 30)), null));
        JsonNode endOfDay = new BusinessHoursValueConverter().toClientModel(
                BusinessHoursValue.of(BusinessHours.of(DayOfWeek.SUNDAY).start(LocalTime.of(10, 30)).end(24)), null);
        assertEquals("10:30", endOfDay.get(0).get("startTime").asString());
        assertEquals("24:00", endOfDay.get(0).get("endTime").asString());
        assertEquals(0, endOfDay.get(0).get("daysOfWeek").get(0).asInt());
        assertEquals(JsonUtils.toJsonNode(false), new EntryLimitConverter().toClientModel(EntryLimit.NONE, null));
        assertEquals(JsonUtils.toJsonNode(true), new EntryLimitConverter().toClientModel(EntryLimit.CELL_HEIGHT, null));
        assertEquals(JsonUtils.toJsonNode(3), new EntryLimitConverter().toClientModel(EntryLimit.of(3), null));
    }

    /** A value-or-callback type that JsCallback implements is a value type, with or without fields yet. */
    @Test
    void everyTypeJsCallbackImplements_isAValueType() {
        assertTrue(VALUE_TYPES.keySet().containsAll(Set.of(JsCallback.class.getInterfaces())),
                "A value type JsCallback implements belongs into VALUE_TYPES");
    }

    @Test
    void valueTypes_rejectValuesTheLibraryReadsDifferently() {
        assertThrows(IllegalArgumentException.class, () -> ConstraintValue.groupId("businessHours"));
        assertThrows(IllegalArgumentException.class, () -> EntryLimit.of(-1));
        assertThrows(IllegalArgumentException.class, ConstraintValue::windows);
        assertThrows(IllegalArgumentException.class, BusinessHoursValue::of);
        assertThrows(IllegalArgumentException.class, () -> DateFormat.of(Map.of("month", List.of("short"))));
        assertThrows(IllegalArgumentException.class, () -> HeightValue.ofPixels(-1));
        assertThrows(IllegalArgumentException.class, () -> HeightValue.ofCss("auto"));
    }

    @Test
    void constraintValueConverter_readsASingleObjectAsOneWindow() {
        assertEquals(ConstraintValue.windows(BusinessHours.businessWeek()),
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
                () -> new EntryLimitConverter().toServerModel(JsonUtils.toJsonNode(-1), null));
        assertThrows(IllegalArgumentException.class,
                () -> new EntryLimitConverter().toServerModel(JsonUtils.toJsonNode(3.5), null));
        assertThrows(IllegalArgumentException.class,
                () -> new EntryLimitConverter().toServerModel(JsonUtils.toJsonNode("x"), null));
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
        assertEquals(ConstraintValue.windows(BusinessHours.businessWeek()), constraint);

        Set<DayOfWeek> days = new HashSet<>(Set.of(DayOfWeek.MONDAY));
        BusinessHours monday = BusinessHours.of(days);
        days.add(DayOfWeek.TUESDAY);
        assertEquals(Set.of(DayOfWeek.MONDAY), monday.getDayOfWeeks());
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
            Class<?>[] permitted = type.isEnum() ? new Class<?>[]{type} : type.getPermittedSubclasses();
            for (Class<?> form : permitted) {
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
