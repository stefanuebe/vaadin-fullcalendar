package org.vaadin.stefan.fullcalendar;

import org.vaadin.stefan.fullcalendar.option.CalendarDuration;
import org.vaadin.stefan.fullcalendar.option.AutoBoolean;
import org.vaadin.stefan.fullcalendar.option.BusinessHours;
import org.vaadin.stefan.fullcalendar.option.BusinessHoursValue;
import org.vaadin.stefan.fullcalendar.option.ClassName;
import org.vaadin.stefan.fullcalendar.option.ClassNameValue;
import org.vaadin.stefan.fullcalendar.option.ConstraintValue;
import org.vaadin.stefan.fullcalendar.option.ContentValue;
import org.vaadin.stefan.fullcalendar.option.CssSelector;
import org.vaadin.stefan.fullcalendar.option.CssSelectorValue;
import org.vaadin.stefan.fullcalendar.option.DateFormat;
import org.vaadin.stefan.fullcalendar.option.DateFormatValue;
import org.vaadin.stefan.fullcalendar.option.DateRange;
import org.vaadin.stefan.fullcalendar.option.DateRangeValue;
import org.vaadin.stefan.fullcalendar.option.EntryLimitValue;
import org.vaadin.stefan.fullcalendar.option.ExtendedLocalTime;
import org.vaadin.stefan.fullcalendar.option.HeaderAlign;
import org.vaadin.stefan.fullcalendar.option.HeaderAlignValue;
import org.vaadin.stefan.fullcalendar.option.HeightValue;
import org.vaadin.stefan.fullcalendar.option.JsCallback;
import org.vaadin.stefan.fullcalendar.option.ListTextValue;
import org.vaadin.stefan.fullcalendar.option.InitialDateValue;
import org.vaadin.stefan.fullcalendar.option.LocaleValue;
import org.vaadin.stefan.fullcalendar.option.NowValue;
import org.vaadin.stefan.fullcalendar.option.Order;
import org.vaadin.stefan.fullcalendar.option.OrderValue;
import org.vaadin.stefan.fullcalendar.option.Overlap;
import org.vaadin.stefan.fullcalendar.option.OverlapValue;
import org.vaadin.stefan.fullcalendar.option.SlotHeaderFormatValue;
import org.vaadin.stefan.fullcalendar.option.StickyValue;
import org.vaadin.stefan.fullcalendar.option.TextContent;
import org.vaadin.stefan.fullcalendar.option.TextValue;
import org.vaadin.stefan.fullcalendar.option.ToolbarValue;
import org.vaadin.stefan.fullcalendar.option.ViewName;
import org.vaadin.stefan.fullcalendar.option.ViewNameValue;
import org.vaadin.stefan.fullcalendar.option.MoreLinkClickValue;
import org.vaadin.stefan.fullcalendar.option.WeekNumberCalculation;
import org.vaadin.stefan.fullcalendar.option.WeekNumberCalculationValue;
import org.vaadin.stefan.fullcalendar.option.WidthValue;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.converters.*;
import org.vaadin.stefan.fullcalendar.json.JsonConverter;
import org.vaadin.stefan.fullcalendar.model.Header;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.ObjectNode;

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
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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
                            BusinessHours.of(DayOfWeek.SATURDAY).start(LocalTime.of(10, 30)).end(24)),
                    BusinessHoursValue.of(BusinessHours.of(DayOfWeek.FRIDAY)
                            .start(ExtendedLocalTime.of(22, 30)).end(ExtendedLocalTime.of(26, 15))),
                    array(BusinessHours.of(DayOfWeek.FRIDAY)
                            .start(ExtendedLocalTime.of(22, 30)).end(ExtendedLocalTime.of(26, 15)))))),
            Map.entry(ToolbarValue.class, new ValueType(ToolbarValueConverter.class, forms(
                    ToolbarValue.NONE, false,
                    ToolbarValue.ofStartEnd("prev,next today", "title", ""),
                    JsonFactory.createObject().put("start", "prev,next today").put("center", "title").put("end", ""),
                    ToolbarValue.ofLeftRight("prev", "", "dayGridMonth"),
                    JsonFactory.createObject().put("left", "prev").put("center", "").put("right", "dayGridMonth")))),
            Map.entry(ClassNameValue.class, new ValueType(ClassNameValueConverter.class, forms(
                    ClassName.of("a b"), "a b", CALLBACK, CALLBACK.toMarkerJson()))),
            Map.entry(ConstraintValue.class, new ValueType(ConstraintValueConverter.class, forms(
                    ConstraintValue.ofGroupId("meetings"), "meetings",
                    ConstraintValue.BUSINESS_HOURS, "businessHours",
                    ConstraintValue.ofResources("room-a", "room-b"),
                    JsonFactory.createObject()
                            .set("resourceIds", JsonFactory.createArray().add("room-a").add("room-b")),
                    ConstraintValue.ofWindows(BusinessHours.businessWeek().start(10).end(18),
                                    ConstraintValue.Window.span(LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 7)))
                            .withResources("room-a"),
                    JsonFactory.createArray()
                            .add(BusinessHours.businessWeek().start(10).end(18).toJson()
                                    .set("resourceIds", JsonFactory.createArray().add("room-a")))
                            .add(JsonFactory.createObject().put("start", "2026-11-02").put("end", "2026-11-07")
                                    .set("resourceIds", JsonFactory.createArray().add("room-a"))),
                    ConstraintValue.ofWindows(BusinessHours.businessWeek().start(10).end(18)),
                    array(BusinessHours.businessWeek().start(10).end(18)),
                    ConstraintValue.ofWindows(BusinessHours.of(DayOfWeek.MONDAY), BusinessHours.of(DayOfWeek.FRIDAY)),
                    array(BusinessHours.of(DayOfWeek.MONDAY), BusinessHours.of(DayOfWeek.FRIDAY)),
                    ConstraintValue.ofWindows(
                            BusinessHours.businessWeek().recurringStartDate(LocalDate.of(2026, 3, 1))
                                    .recurringEndDate(LocalDate.of(2026, 7, 1)),
                            ConstraintValue.Window.span(LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 7)),
                            ConstraintValue.Window.span(LocalDate.of(2026, 12, 1), null),
                            ConstraintValue.Window.span(LocalDateTime.of(2026, 11, 9, 8, 0), LocalDateTime.of(2026, 11, 9, 17, 0)),
                            ConstraintValue.Window.span(null, LocalDateTime.of(2026, 1, 1, 12, 0))),
                    JsonFactory.createArray()
                            .add(BusinessHours.businessWeek().toJson().put("startRecur", "2026-03-01").put("endRecur", "2026-07-01"))
                            .add(JsonFactory.createObject().put("start", "2026-11-02").put("end", "2026-11-07"))
                            .add(JsonFactory.createObject().put("start", "2026-12-01"))
                            .add(JsonFactory.createObject().put("start", "2026-11-09T08:00Z").put("end", "2026-11-09T17:00Z"))
                            .add(JsonFactory.createObject().put("end", "2026-01-01T12:00Z")),
                    ConstraintValue.ofWindows(
                            ConstraintValue.Window.ofRRule(RRule.weekly().byWeekday(DayOfWeek.MONDAY)
                                    .dtstart(LocalDate.of(2026, 1, 5))),
                            ConstraintValue.Window.ofRRule(RRule.ofRaw("FREQ=DAILY;DTSTART=20260105"),
                                    Duration.ofHours(8)),
                            ConstraintValue.Window.ofRRule(RRule.daily().dtstart(LocalDate.of(2026, 1, 5))
                                    .excludeDates(LocalDate.of(2026, 1, 7))
                                    .excludeRules(RRule.weekly().byWeekday("sa", "su")),
                                    Duration.ofMinutes(90)),
                            ConstraintValue.Window.ofRRule(RRule.ofRaw("FREQ=DAILY;DTSTART=20260105")
                                    .excludeDates(LocalDate.of(2026, 1, 7))),
                            ConstraintValue.Window.ofRRule(RRule.daily().dtstart(LocalDate.of(2026, 1, 5))
                                    .excludeRules(RRule.monthly().byWeekday("-1fr")))),
                    JsonFactory.createArray()
                            .add(JsonFactory.createObject().set("rrule",
                                    RRule.weekly().byWeekday(DayOfWeek.MONDAY).dtstart(LocalDate.of(2026, 1, 5)).toJson()))
                            .add(JsonFactory.createObject().put("rrule", "FREQ=DAILY;DTSTART=20260105")
                                    .put("duration", "08:00:00"))
                            .add(JsonFactory.createObject()
                                    .set("rrule", RRule.daily().dtstart(LocalDate.of(2026, 1, 5)).toJson())
                                    .put("duration", "01:30:00")
                                    .set("exdate", JsonFactory.createArray().add("2026-01-07"))
                                    .set("exrule", RRule.weekly().byWeekday("sa", "su").toJson()))
                            .add(JsonFactory.createObject().put("rrule", "FREQ=DAILY;DTSTART=20260105")
                                    .set("exdate", JsonFactory.createArray().add("2026-01-07")))
                            .add(JsonFactory.createObject()
                                    .set("rrule", RRule.daily().dtstart(LocalDate.of(2026, 1, 5)).toJson())
                                    .set("exrule", JsonFactory.createObject().put("freq", "monthly")
                                            .set("byweekday", JsonFactory.createArray().add(
                                                    JsonFactory.createObject().put("weekday", 4).put("n", -1)))))))),
            Map.entry(ContentValue.class, new ValueType(ContentValueConverter.class, forms(
                    TextContent.of("all day"), "all day", ContentValue.ofHtml("<b>all</b> day"),
                    JsonFactory.createObject().put("html", "<b>all</b> day"), CALLBACK, CALLBACK.toMarkerJson()))),
            Map.entry(CssSelectorValue.class, new ValueType(CssSelectorValueConverter.class, forms(
                    CssSelector.of(".item"), ".item", CALLBACK, CALLBACK.toMarkerJson()))),
            Map.entry(DateFormatValue.class, new ValueType(DateFormatValueConverter.class, forms(
                    DateFormat.of(Map.of("month", "short", "day", "numeric", "omitCommas", true, "hour12", false)),
                    JsonFactory.createObject().put("month", "short").put("day", "numeric").put("omitCommas", true)
                            .put("hour12", false),
                    CALLBACK, CALLBACK.toMarkerJson()))),
            Map.entry(DateRangeValue.class, new ValueType(DateRangeValueConverter.class, forms(
                    new DateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 7, 1)),
                    JsonFactory.createObject().put("start", "2026-01-01").put("end", "2026-07-01"),
                    new DateRange(LocalDate.of(2026, 1, 1), null), JsonFactory.createObject().put("start", "2026-01-01"),
                    new DateRange(null, LocalDate.of(2026, 7, 1)), JsonFactory.createObject().put("end", "2026-07-01"),
                    CALLBACK, CALLBACK.toMarkerJson()))),
            Map.entry(OrderValue.class, new ValueType(OrderValueConverter.class, forms(
                    Order.of("title", "-start"), JsonFactory.createArray().add("title").add("-start"),
                    Order.of(Order.descending("duration"), CALLBACK),
                    JsonFactory.createArray().add("-duration").add(CALLBACK.toMarkerJson()),
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
            Map.entry(SlotHeaderFormatValue.class, new ValueType(SlotHeaderFormatValueConverter.class, forms(
                    DateFormat.of(Map.of("hour", "numeric")), JsonFactory.createObject().put("hour", "numeric"),
                    CALLBACK, CALLBACK.toMarkerJson(),
                    SlotHeaderFormatValue.of(DateFormat.of(Map.of("month", "long")), CALLBACK),
                    JsonFactory.createArray().add(JsonFactory.createObject().put("month", "long"))
                            .add(CALLBACK.toMarkerJson())))),
            Map.entry(ListTextValue.class, new ValueType(ListTextValueConverter.class, forms(
                    TextContent.of("Agenda"), "Agenda", ListTextValue.NONE, false))),
            Map.entry(InitialDateValue.class, new ValueType(InitialDateValueConverter.class, forms(
                    InitialDateValue.ofDay(LocalDate.of(2026, 10, 15)), "2026-10-15",
                    InitialDateValue.ofDateTime(LocalDateTime.of(2026, 10, 15, 6, 0)), "2026-10-15T06:00Z"))),
            Map.entry(LocaleValue.class, new ValueType(LocaleValueConverter.class, forms(
                    LocaleValue.of(Locale.GERMANY), "de-de",
                    LocaleValue.ofCustom(Locale.of("de", "CH"), Map.of("todayText", "Heute",
                            "week", Map.of("dow", 1, "doy", 4), "moreLinkText", CALLBACK)),
                    JsonFactory.createObject().put("code", "de-ch").put("todayText", "Heute")
                            .<ObjectNode>set("week", JsonFactory.createObject().put("dow", 1).put("doy", 4))
                            .set("moreLinkText", CALLBACK.toMarkerJson())))),
            Map.entry(NowValue.class, new ValueType(NowValueConverter.class, forms(
                    NowValue.ofDay(LocalDate.of(2026, 10, 15)), "2026-10-15",
                    NowValue.ofDateTime(LocalDateTime.of(2026, 10, 15, 9, 30)), "2026-10-15T09:30Z",
                    CALLBACK, CALLBACK.toMarkerJson()))),
            Map.entry(OverlapValue.class, new ValueType(OverlapValueConverter.class, forms(
                    Overlap.ALLOWED, true, Overlap.FORBIDDEN, false, CALLBACK, CALLBACK.toMarkerJson()))),
            Map.entry(TextValue.class, new ValueType(TextValueConverter.class, forms(
                    TextContent.of("+2 more"), "+2 more", CALLBACK, CALLBACK.toMarkerJson()))),
            Map.entry(StickyValue.class, new ValueType(StickyValueConverter.class, forms(
                    StickyValue.NOT_STICKY, false, StickyValue.STICKY, true, StickyValue.of("2em"), "2em"))),
            Map.entry(MoreLinkClickValue.class, new ValueType(MoreLinkClickValueConverter.class, forms(
                    MoreLinkClickValue.Action.POPUP, "popover", MoreLinkClickValue.Action.DAY, "day",
                    MoreLinkClickValue.Action.WEEK, "week", MoreLinkClickValue.Action.NOTHING, "function",
                    ViewName.of("timeGridDay"), "timeGridDay", CALLBACK, CALLBACK.toMarkerJson()))),
            Map.entry(ViewNameValue.class, new ValueType(ViewNameValueConverter.class, forms(
                    ViewName.of("timeGridDay"), "timeGridDay", CALLBACK, CALLBACK.toMarkerJson()))),
            Map.entry(WeekNumberCalculationValue.class, new ValueType(WeekNumberCalculationValueConverter.class, forms(
                    WeekNumberCalculation.LOCAL, "local", WeekNumberCalculation.ISO, "ISO",
                    CALLBACK, CALLBACK.toMarkerJson()))),
            Map.entry(WidthValue.class, new ValueType(WidthValueConverter.class, forms(
                    WidthValue.ofPixels(0), 0, WidthValue.ofPixels(250), 250, WidthValue.ofPercent(25), "25%",
                    WidthValue.ofPercent(33.5), "33.5%"))));

    /**
     * Field types with a converter that are no value type, since their options take one form only, with the converter
     * their fields use.
     */
    static final Map<Class<?>, Class<?>> ONE_FORM_TYPES = Map.of(
            CalendarDuration.class, CalendarDurationConverter.class,
            DayOfWeek.class, DayOfWeekConverter.class,
            LocalDate.class, LocalDateConverter.class,
            Locale.class, LocaleConverter.class,
            Set.class, DayOfWeekArrayConverter.class,
            ViewName.class, ViewNameValueConverter.class);

    /**
     * The converters of the map fields, by field name, because maps with different values need different converters.
     * A map field not listed here is in {@link #MAPS_WRITTEN_AS_THEY_ARE}.
     */
    private static final Map<String, Class<?>> MAP_CONVERTERS = Map.of(
            "toolbarElements", ToolbarElementsConverter.class);

    /**
     * Map fields, by name, whose values {@link JsonUtils#toJsonNode(Object)} writes as the library reads them: plain
     * data, callbacks and types with a client value.
     */
    private static final Set<String> MAPS_WRITTEN_AS_THEY_ARE = Set.of("buttons");

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
    void moreLinkClickValue_otherJsonKinds_throw() {
        MoreLinkClickValueConverter converter = new MoreLinkClickValueConverter();

        assertThrows(IllegalArgumentException.class, () -> converter.toServerModel(JsonUtils.toJsonNode(5), null));
        assertThrows(IllegalArgumentException.class, () -> converter.toServerModel(JsonUtils.toJsonNode(false), null));
    }

    @Test
    void toolbarValue_unknownClientForms_throw() {
        ToolbarValueConverter converter = new ToolbarValueConverter();

        assertThrows(IllegalArgumentException.class, () -> converter.toServerModel(JsonUtils.toJsonNode(true), null));
        assertThrows(IllegalArgumentException.class, () -> converter.toServerModel(JsonUtils.toJsonNode("title"), null));
        assertThrows(IllegalArgumentException.class, () -> converter.toServerModel(
                JsonFactory.createObject().put("left", "prev").put("end", "next"), null));
        assertThrows(IllegalArgumentException.class, () -> converter.toServerModel(
                JsonFactory.createObject().put("start", "prev").put("right", "next"), null));
        assertThrows(IllegalArgumentException.class, () -> converter.toServerModel(
                JsonFactory.createObject().put("middle", "title"), null));
        assertThrows(IllegalArgumentException.class, () -> converter.toServerModel(
                JsonFactory.createObject().put("start", 5), null));
    }

    @Test
    void toolbarElements_writeEachContentInItsClientForm_andRoundTrip() {
        Map<String, ContentValue> elements = Map.of("info", TextContent.of("Info"),
                "logo", ContentValue.ofHtml("<img src='logo.svg'>"), "clock", CALLBACK);
        JsonNode expected = JsonFactory.createObject().put("info", "Info")
                .<JsonNode>set("logo", JsonFactory.createObject().put("html", "<img src='logo.svg'>"))
                .set("clock", CALLBACK.toMarkerJson());
        ToolbarElementsConverter converter = new ToolbarElementsConverter();

        assertTrue(converter.supports(elements));
        assertEquals(expected, converter.toClientModel(elements, null));
        assertEquals(elements, converter.toServerModel(expected, null));
    }

    @Test
    void widthValue_readsPixelsAsString() {
        assertEquals(WidthValue.ofPixels(250), new WidthValueConverter().toServerModel(JsonUtils.toJsonNode("250px"), null));
    }

    @Test
    void orderValue_readsTheLibraryString() {
        assertEquals(Order.of("title", "-start"),
                new OrderValueConverter().toServerModel(JsonUtils.toJsonNode("title, -start"), null));
    }

    @Test
    void calendarDuration_sendsEachUnitThatIsNotZero() {
        CalendarDurationConverter converter = new CalendarDurationConverter();
        assertEquals(JsonFactory.createObject().put("weeks", 1),
                converter.toClientModel(CalendarDuration.ofWeeks(1), null));
        assertEquals(JsonFactory.createObject().put("days", 7),
                converter.toClientModel(CalendarDuration.ofDays(7), null));
        assertEquals(JsonFactory.createObject().put("years", 1).put("months", 2).put("weeks", 3).put("days", 4)
                        .put("hours", 5).put("minutes", 6).put("seconds", 7).put("milliseconds", 8),
                converter.toClientModel(CalendarDuration.ofYears(1).plusMonths(2).plusWeeks(3).plusDays(4).plusHours(5)
                        .plusMinutes(6).plusSeconds(7).plusMillis(8), null));
        assertEquals(JsonFactory.createObject(), converter.toClientModel(CalendarDuration.ZERO, null));
    }

    @Test
    void calendarDuration_takesTheUnitsOfDurationAndPeriod() {
        assertEquals(CalendarDuration.ofHours(26).plusMinutes(30).plusSeconds(5).plusMillis(500),
                CalendarDuration.of(Duration.ofDays(1).plusHours(2).plusMinutes(30).plusSeconds(5).plusMillis(500)));
        assertEquals(CalendarDuration.ofSeconds(-1).plusMillis(-500), CalendarDuration.of(Duration.ofMillis(-1500)));
        assertEquals(CalendarDuration.ofHours(-1).plusMinutes(-30), CalendarDuration.of(Duration.ofMinutes(-90)));
        assertEquals(CalendarDuration.ofYears(1).plusMonths(2).plusDays(3), CalendarDuration.of(Period.of(1, 2, 3)));
        // the library aligns weeks to the start of the week, but Period.ofWeeks is days
        assertEquals(CalendarDuration.ofDays(14), CalendarDuration.of(Period.ofWeeks(2)));
    }

    @Test
    void calendarDuration_readsTheLibraryStringsAndMilliseconds() {
        assertEquals(CalendarDuration.ofHours(8).plusMinutes(30), CalendarDuration.ofTimeString("08:30"));
        assertEquals(CalendarDuration.ofDays(1).plusHours(12), CalendarDuration.ofTimeString("1.12:00:00"));
        assertEquals(CalendarDuration.ofDays(-1).plusHours(-2).plusMinutes(-5).plusSeconds(-3).plusMillis(-4),
                CalendarDuration.ofTimeString("-1.02:05:03.004"));
        assertEquals(CalendarDuration.ZERO, CalendarDuration.ofTimeString("0.00:00"));
        // like the library, a match at the start is enough
        assertEquals(CalendarDuration.ofHours(1), CalendarDuration.ofTimeString("01:00:00 extra"));

        CalendarDurationConverter converter = new CalendarDurationConverter();
        assertEquals(CalendarDuration.ofSeconds(1).plusMillis(500),
                converter.toServerModel(JsonUtils.toJsonNode(1500), null));
        assertEquals(CalendarDuration.ofDays(2), converter.toServerModel(JsonUtils.toJsonNode("2.00:00"), null));
        CalendarDuration all = CalendarDuration.ofYears(1).plusMonths(2).plusWeeks(3).plusDays(4).plusHours(5)
                .plusMinutes(6).plusSeconds(7).plusMillis(8);
        assertEquals(all, converter.toServerModel(converter.toClientModel(all, null), null));
        CalendarDuration negative = CalendarDuration.ofWeeks(-1).plusMinutes(-30);
        assertEquals(negative, converter.toServerModel(converter.toClientModel(negative, null), null));
    }

    @Test
    void businessHoursValue_writesTheEndOfTheDayAs24() {
        JsonNode endOfDay = new BusinessHoursValueConverter().toClientModel(
                BusinessHoursValue.of(BusinessHours.of(DayOfWeek.SUNDAY).start(LocalTime.of(10, 30)).end(24)), null);
        assertEquals("10:30", endOfDay.get(0).get("startTime").asString());
        assertEquals("24:00", endOfDay.get(0).get("endTime").asString());
        assertEquals(0, endOfDay.get(0).get("daysOfWeek").get(0).asInt());
    }

    /** Every type a user passes as an option value lives in the package {@code option}, the forms of a value type too. */
    @Test
    void everyOptionValueType_andItsForms_liveInTheOptionPackage() {
        String optionPackage = AutoBoolean.class.getPackageName();
        Stream.concat(optionFields().map(Field::getType),
                        VALUE_TYPES.values().stream().flatMap(type -> type.samples().keySet().stream()).map(Object::getClass))
                .filter(type -> !type.getPackageName().startsWith("java."))
                .map(type -> type.isMemberClass() ? type.getEnclosingClass() : type)
                .distinct()
                .forEach(type -> assertEquals(optionPackage, type.getPackageName(), type.getName()));
    }

    /**
     * A value-or-callback type that JsCallback implements is a value type, with or without fields yet. The sort key of
     * an order is a part of a form, no option value.
     */
    @Test
    void everyTypeJsCallbackImplements_isAValueType() {
        Set<Class<?>> interfaces = new HashSet<>(Set.of(JsCallback.class.getInterfaces()));
        interfaces.remove(Order.Key.class);
        assertTrue(VALUE_TYPES.keySet().containsAll(interfaces),
                "A value type JsCallback implements belongs into VALUE_TYPES");
    }

    @Test
    void valueTypes_rejectValuesTheLibraryReadsDifferently() {
        assertThrows(IllegalArgumentException.class, () -> ConstraintValue.ofGroupId("businessHours"));
        assertThrows(IllegalArgumentException.class, () -> EntryLimitValue.of(-1));
        assertThrows(IllegalArgumentException.class,
                () -> LocaleValue.ofCustom(Locale.GERMAN, Map.of("code", "de", "todayText", "Heute")));
        assertThrows(IllegalArgumentException.class, ConstraintValue::ofWindows);
        assertThrows(IllegalArgumentException.class, ConstraintValue::ofResources);
        assertThrows(IllegalArgumentException.class,
                () -> ConstraintValue.ofWindows(BusinessHours.businessWeek()).withResources());
        assertThrows(IllegalArgumentException.class, BusinessHoursValue::of);
        assertThrows(IllegalArgumentException.class, SlotHeaderFormatValue::of);
        assertThrows(NullPointerException.class, () -> SlotHeaderFormatValue.of((DateFormatValue) null));
        assertThrows(IllegalArgumentException.class, () -> DateFormat.of(Map.of("month", List.of("short"))));
        assertThrows(IllegalArgumentException.class, () -> HeightValue.ofPixels(-1));
        assertThrows(IllegalArgumentException.class, () -> HeightValue.ofCss("auto"));
        assertThrows(IllegalArgumentException.class, () -> WidthValue.ofPixels(-1));
        assertThrows(IllegalArgumentException.class, () -> WidthValue.ofPercent(-1));
        assertThrows(IllegalArgumentException.class, () -> WidthValue.ofPercent(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> WidthValue.ofPercent(Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> Order.ascending("-start"));
        assertThrows(IllegalArgumentException.class, () -> Order.of("title", ""));
        // the library splits only the string form at commas, not a name in a list
        assertThrows(IllegalArgumentException.class, () -> Order.of("title,start"));
        assertThrows(IllegalArgumentException.class, () -> Order.of(new String[0]));
        assertThrows(IllegalArgumentException.class, () -> ConstraintValue.Window.span((LocalDate) null, null));
        assertThrows(IllegalArgumentException.class,
                () -> ConstraintValue.Window.span(LocalDate.of(2026, 1, 2), LocalDate.of(2026, 1, 2)));
        assertThrows(IllegalArgumentException.class, () -> ConstraintValue.Window.span(
                LocalDateTime.of(2026, 1, 2, 10, 0), LocalDateTime.of(2026, 1, 2, 9, 0)));
        assertThrows(IllegalArgumentException.class, () -> BusinessHours.allDays()
                .recurringStartDate(LocalDate.of(2026, 3, 1)).recurringEndDate(LocalDate.of(2026, 3, 1)));
        assertThrows(IllegalArgumentException.class, () -> CalendarDuration.ofTimeString("PT1H"));
        assertThrows(IllegalArgumentException.class, () -> CalendarDuration.ofTimeString("8"));
    }

    @Test
    void constraintValueConverter_readsResourceIds_andRejectsIdsThatDifferPerWindow() {
        ConstraintValueConverter converter = new ConstraintValueConverter();
        ObjectNode window = JsonFactory.createObject().put("start", "2026-11-02");
        assertEquals(ConstraintValue.ofWindows(ConstraintValue.Window.span(LocalDate.of(2026, 11, 2), null))
                        .withResources("room-a"),
                converter.toServerModel(window.deepCopy().set("resourceIds", JsonFactory.createArray().add("room-a")),
                        null));
        assertThrows(IllegalArgumentException.class, () -> converter.toServerModel(JsonFactory.createArray()
                .add(window.deepCopy().set("resourceIds", JsonFactory.createArray().add("room-a")))
                .add(window), null));
        assertThrows(IllegalArgumentException.class, () -> converter.toServerModel(
                JsonFactory.createObject().set("resourceIds", JsonFactory.createArray().add(1)), null));
        assertEquals(List.of("room-b"), ConstraintValue.ofWindows(BusinessHours.businessWeek()).withResources("room-a")
                .withResources("room-b").resourceIds());
    }

    @Test
    void constraintValueConverter_rejectsABadRuleWindow() {
        ConstraintValueConverter converter = new ConstraintValueConverter();
        ObjectNode rrule = JsonFactory.createObject().put("freq", "daily");
        assertThrows(IllegalArgumentException.class, () -> converter.toServerModel(
                JsonFactory.createObject().set("rrule", JsonUtils.toJsonNode(1)), null));
        assertThrows(IllegalArgumentException.class, () -> converter.toServerModel(
                JsonFactory.createObject().set("rrule", rrule).put("start", "2026-01-01"), null));
        assertThrows(IllegalArgumentException.class, () -> converter.toServerModel(
                JsonFactory.createObject().set("rrule", rrule).put("duration", "PT1H"), null));
        assertThrows(IllegalArgumentException.class, () -> converter.toServerModel(
                JsonFactory.createObject().set("rrule", rrule).put("exdate", "2026-01-01"), null));
        assertThrows(IllegalArgumentException.class, () -> converter.toServerModel(
                JsonFactory.createObject().set("rrule", rrule).set("exdate", JsonFactory.createArray().add("x")),
                null));
        assertThrows(IllegalArgumentException.class, () -> converter.toServerModel(
                JsonFactory.createObject().set("rrule", rrule).set("exrule", JsonFactory.createObject().put("x", 1)),
                null));
        assertThrows(IllegalArgumentException.class, () -> converter.toServerModel(
                JsonFactory.createObject().set("rrule", rrule).set("exrule", JsonFactory.createArray().add(1)),
                null));
        assertThrows(NullPointerException.class, () -> ConstraintValue.Window.ofRRule(null));
    }

    @Test
    void ruleWindow_isNotChangedByALaterChangeToTheRule() {
        RRule rule = RRule.daily().dtstart(LocalDate.of(2026, 1, 5)).excludeDates(new ArrayList<>(
                        List.of(LocalDate.of(2026, 1, 7))))
                .excludeRules(new ArrayList<>(List.of(RRule.weekly().byWeekday("sa"))));
        ConstraintValue.Window window = ConstraintValue.Window.ofRRule(rule);
        ConstraintValueConverter converter = new ConstraintValueConverter();
        JsonNode before = converter.toClientModel(ConstraintValue.ofWindows(window), null);

        rule.interval(5);
        rule.getExcludedDates().add(LocalDate.of(2026, 1, 8));
        rule.getExcludedRules().get(0).interval(2);
        rule.getExcludedRules().add(RRule.weekly());
        assertEquals(before, converter.toClientModel(ConstraintValue.ofWindows(window), null));

        RRule returned = ((ConstraintValue.RRuleWindow) window).rrule();
        returned.interval(9).excludeDates(LocalDate.of(2026, 2, 1));
        assertEquals(before, converter.toClientModel(ConstraintValue.ofWindows(window), null));
        assertNotSame(returned, ((ConstraintValue.RRuleWindow) window).rrule());
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
        assertThrows(IllegalArgumentException.class, () -> new ConstraintValueConverter()
                .toServerModel(JsonFactory.createObject().put("start", "2026-11-09T08:00"), null));
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
                () -> new InitialDateValueConverter().toServerModel(JsonUtils.toJsonNode(1), null));
        assertThrows(IllegalArgumentException.class,
                () -> new LocaleValueConverter().toServerModel(JsonUtils.toJsonNode(1), null));
        assertThrows(IllegalArgumentException.class, () -> new LocaleValueConverter()
                .toServerModel(JsonFactory.createObject().put("todayText", "Heute"), null));
        assertThrows(IllegalArgumentException.class, () -> new LocaleValueConverter()
                .toServerModel(JsonFactory.createObject().put("code", 5), null));
        assertThrows(IllegalArgumentException.class,
                () -> new StickyValueConverter().toServerModel(JsonFactory.createArray(), null));
        assertThrows(IllegalArgumentException.class,
                () -> new SlotHeaderFormatValueConverter().toServerModel(JsonUtils.toJsonNode(true), null));
        assertThrows(IllegalArgumentException.class,
                () -> new SlotHeaderFormatValueConverter().toServerModel(JsonUtils.toJsonNode("MMM d"), null));
        assertThrows(IllegalArgumentException.class,
                () -> new SlotHeaderFormatValueConverter().toServerModel(JsonFactory.createArray(), null));
        assertThrows(IllegalArgumentException.class, () -> new SlotHeaderFormatValueConverter()
                .toServerModel(JsonFactory.createArray().add(true), null));
        assertThrows(IllegalArgumentException.class, () -> new SlotHeaderFormatValueConverter()
                .toServerModel(JsonFactory.createArray().add(JsonFactory.createArray()), null));
        assertThrows(IllegalArgumentException.class, () -> new ContentValueConverter()
                .toServerModel(JsonFactory.createObject().put("domNodes", "x"), null));
        assertThrows(IllegalArgumentException.class,
                () -> new TextValueConverter().toServerModel(JsonUtils.toJsonNode(1), null));
        assertThrows(IllegalArgumentException.class,
                () -> new WidthValueConverter().toServerModel(JsonUtils.toJsonNode(true), null));
        assertThrows(IllegalArgumentException.class,
                () -> new WidthValueConverter().toServerModel(JsonUtils.toJsonNode("20em"), null));
        assertThrows(IllegalArgumentException.class,
                () -> new WidthValueConverter().toServerModel(JsonUtils.toJsonNode("x%"), null));
        assertThrows(IllegalArgumentException.class,
                () -> new DateRangeValueConverter().toServerModel(JsonUtils.toJsonNode("2026-01-01"), null));
        assertThrows(IllegalArgumentException.class, () -> new DateRangeValueConverter()
                .toServerModel(JsonFactory.createObject().put("start", "2026-01-01T10:00"), null));
        assertThrows(IllegalArgumentException.class,
                () -> new WeekNumberCalculationValueConverter().toServerModel(JsonUtils.toJsonNode("x"), null));
        assertThrows(IllegalArgumentException.class,
                () -> new CalendarDurationConverter().toServerModel(JsonUtils.toJsonNode(true), null));
        assertThrows(IllegalArgumentException.class, () -> new CalendarDurationConverter()
                .toServerModel(JsonFactory.createObject().put("fortnights", 1), null));
        assertThrows(IllegalArgumentException.class, () -> new CalendarDurationConverter()
                .toServerModel(JsonFactory.createObject().put("hours", 1.5), null));
        assertThrows(IllegalArgumentException.class,
                () -> new OrderValueConverter().toServerModel(JsonUtils.toJsonNode(1), null));
        assertThrows(IllegalArgumentException.class,
                () -> new OrderValueConverter().toServerModel(JsonFactory.createArray().add(1), null));
        assertThrows(IllegalArgumentException.class, () -> new ToolbarElementsConverter()
                .toServerModel(JsonUtils.toJsonNode("x"), null));
    }

    @Test
    void valueTypes_withListsKeepTheirValueWhenThePassedListChanges() {
        List<BusinessHours> hours = new ArrayList<>(List.of(BusinessHours.businessWeek()));
        BusinessHoursValue value = new BusinessHoursValue.Hours(hours);
        hours.add(BusinessHours.allDays());
        assertEquals(List.of(BusinessHours.businessWeek()), ((BusinessHoursValue.Hours) value).hours());
        assertThrows(UnsupportedOperationException.class,
                () -> ((BusinessHoursValue.Hours) value).hours().add(BusinessHours.allDays()));

        List<ConstraintValue.Window> windows = new ArrayList<>(List.of(BusinessHours.businessWeek()));
        ConstraintValue constraint = new ConstraintValue.Windows(windows);
        windows.add(BusinessHours.allDays());
        assertEquals(ConstraintValue.ofWindows(BusinessHours.businessWeek()), constraint);

        DateFormat month = DateFormat.of(Map.of("month", "long"));
        List<DateFormatValue> tiers = new ArrayList<>(List.of(month));
        SlotHeaderFormatValue.Tiers slotHeaderFormat = new SlotHeaderFormatValue.Tiers(tiers);
        tiers.add(DateFormat.of(Map.of("weekday", "short")));
        assertEquals(List.of(month), slotHeaderFormat.tiers());
        assertThrows(UnsupportedOperationException.class, () -> slotHeaderFormat.tiers().add(month));

        Set<DayOfWeek> days = new HashSet<>(Set.of(DayOfWeek.MONDAY));
        BusinessHours monday = BusinessHours.of(days);
        days.add(DayOfWeek.TUESDAY);
        assertEquals(Set.of(DayOfWeek.MONDAY), monday.getDayOfWeeks());
    }

    /**
     * Every field whose type the sync cannot write as it is uses the converter of its type from {@link #VALUE_TYPES}
     * or {@link #ONE_FORM_TYPES}, a map field the one of {@link #MAP_CONVERTERS}.
     */
    @Test
    void everyValueTypeField_usesTheConverterOfItsType() {
        List<Field> convertedFields = optionFields()
                .filter(field -> !WRITTEN_AS_THEY_ARE.contains(field.getType())
                        && !ClientSideValue.class.isAssignableFrom(field.getType())
                        && !MAPS_WRITTEN_AS_THEY_ARE.contains(field.getName()))
                .toList();

        assertFalse(convertedFields.isEmpty());
        Set<String> mapFields = optionFields().filter(field -> field.getType() == Map.class).map(Field::getName)
                .collect(Collectors.toSet());
        Set<String> listedMaps = new HashSet<>(MAP_CONVERTERS.keySet());
        listedMaps.addAll(MAPS_WRITTEN_AS_THEY_ARE);
        assertEquals(mapFields, listedMaps, "MAP_CONVERTERS and MAPS_WRITTEN_AS_THEY_ARE list the map fields");
        for (Field field : convertedFields) {
            Class<?> type = field.getType();
            Class<?> expected = VALUE_TYPES.containsKey(type) ? VALUE_TYPES.get(type).converter()
                    : type == Map.class ? MAP_CONVERTERS.get(field.getName()) : ONE_FORM_TYPES.get(type);
            assertNotNull(expected, "Add " + type.getSimpleName() + " of " + field.getName()
                    + " to VALUE_TYPES, to ONE_FORM_TYPES when its option takes one form only, or to MAP_CONVERTERS");
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
