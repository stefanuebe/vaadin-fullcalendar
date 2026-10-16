package org.vaadin.stefan.fullcalendar;

import org.vaadin.stefan.fullcalendar.option.DateRangeValue;
import org.vaadin.stefan.fullcalendar.option.TextValue;
import org.vaadin.stefan.fullcalendar.option.WeekNumberCalculationValue;
import org.vaadin.stefan.fullcalendar.option.WidthValue;
import org.vaadin.stefan.fullcalendar.option.DurationValue;
import org.vaadin.stefan.fullcalendar.option.EntryOrder;
import org.vaadin.stefan.fullcalendar.option.EntryOrderValue;
import org.vaadin.stefan.fullcalendar.option.AutoBoolean;
import org.vaadin.stefan.fullcalendar.option.BusinessHours;
import org.vaadin.stefan.fullcalendar.option.BusinessHoursValue;
import org.vaadin.stefan.fullcalendar.option.ClassName;
import org.vaadin.stefan.fullcalendar.option.ClassNameValue;
import org.vaadin.stefan.fullcalendar.option.ConstraintValue;
import org.vaadin.stefan.fullcalendar.option.ContentValue;
import org.vaadin.stefan.fullcalendar.option.CssSelectorValue;
import org.vaadin.stefan.fullcalendar.option.DateFormatValue;
import org.vaadin.stefan.fullcalendar.option.EntryLimitValue;
import org.vaadin.stefan.fullcalendar.option.HeaderAlign;
import org.vaadin.stefan.fullcalendar.option.HeaderAlignValue;
import org.vaadin.stefan.fullcalendar.option.HeightValue;
import org.vaadin.stefan.fullcalendar.option.JsCallback;
import org.vaadin.stefan.fullcalendar.option.ListDayFormatValue;
import org.vaadin.stefan.fullcalendar.option.ListTextValue;
import org.vaadin.stefan.fullcalendar.option.NowValue;
import org.vaadin.stefan.fullcalendar.option.OverlapValue;
import org.vaadin.stefan.fullcalendar.option.StickyValue;
import org.vaadin.stefan.fullcalendar.option.TextContent;
import org.vaadin.stefan.fullcalendar.option.ViewName;
import org.vaadin.stefan.fullcalendar.option.ViewNameValue;
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
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
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
 * The typed options of {@link FullCalendar#options()}: they reach the client like raw options, and a raw option with
 * the same name lies over them.
 */
public class TypedOptionsSyncTest extends BrowserlessTest {

    private static final CustomCalendarView THREE_DAYS = new CustomCalendarView.AnonymousCustomCalendarView(
            "threeDays", JsonFactory.createObject().put("type", "dayGrid").put("dayCount", 3));

    private FullCalendar calendar;
    private UI ui;

    @BeforeEach
    void setUp() {
        calendar = new FullCalendar();
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
    void beforeAttach_fieldsAreInTheInitialOptions() {
        calendar.options().setFirstDay(DayOfWeek.MONDAY);
        calendar.options().setInitialDate(LocalDate.of(2025, 3, 3));
        attach();

        assertEquals(1, initialOptions().get("firstDay").asInt());
        assertEquals("2025-03-03", initialOptions().get("initialDate").asString());
        assertTrue(initialOptions().get("editable").asBoolean());
    }

    @Test
    void afterAttach_setEditable_reachesTheClient_nullSendsARemoval() {
        attach();

        calendar.options().setEditable(false);
        assertFalse(respond().getFirst().get("editable").asBoolean());

        calendar.options().setEditable(null);
        assertTrue(respond().getFirst().get("editable").isNull());
    }

    @Test
    void hiddenDays_laterChangeOfThePassedSet_doesNotChangeTheOption() {
        Set<DayOfWeek> days = new HashSet<>(Set.of(DayOfWeek.SUNDAY, DayOfWeek.MONDAY));
        calendar.options().setHiddenDays(days);
        days.add(DayOfWeek.FRIDAY);

        assertEquals(Set.of(DayOfWeek.SUNDAY, DayOfWeek.MONDAY), calendar.options().getHiddenDays());
        assertThrows(UnsupportedOperationException.class, () -> calendar.options().getHiddenDays().clear());

        calendar.options().setHiddenDays(null);
        assertNull(calendar.options().getHiddenDays());
        calendar.options().setHiddenDays(new HashSet<>());
        assertEquals(Set.of(), calendar.options().getHiddenDays());
    }

    @Test
    void keptReference_changesTheClientInALaterRequest() {
        FullCalendarOptions options = calendar.options();
        attach();

        options.setFirstDay(DayOfWeek.TUESDAY);

        assertEquals(2, respond().getFirst().get("firstDay").asInt());
    }

    @Test
    void rawOption_winsOverTheField_removingItBringsTheCurrentFieldValueBack() {
        calendar.options().setFirstDay(DayOfWeek.MONDAY);
        attach();

        calendar.options().setRawOption("firstDay", 3);
        assertEquals(3, respond().getFirst().get("firstDay").asInt());

        calendar.options().setFirstDay(DayOfWeek.TUESDAY);
        assertEquals(List.of(), respond());

        calendar.options().setRawOption("firstDay", null);
        assertEquals(2, respond().getFirst().get("firstDay").asInt());
        assertEquals(DayOfWeek.TUESDAY, calendar.options().getFirstDay());
    }

    @Test
    void beforeAttach_rawOptionLiesOverTheField() {
        calendar.options().setFirstDay(DayOfWeek.MONDAY);
        calendar.options().setRawOption("firstDay", 3);
        attach();

        assertEquals(3, initialOptions().get("firstDay").asInt());
    }

    @Test
    void constructorJsonEditable_keepsTheAddonDefaultOut() {
        ObjectNode json = JsonFactory.createObject();
        json.put("editable", false);
        calendar = new FullCalendar(json);
        attach();

        assertFalse(initialOptions().get("editable").asBoolean());
        assertNull(calendar.options().getEditable());
    }

    @Test
    void valueOrCallback_bothFormsReachTheClient_nullRemovesTheOption() {
        attach();

        calendar.options().setDayCellClass("typed-day");
        assertEquals("typed-day", respond().getFirst().get("dayCellClass").asString());

        calendar.options().setDayCellClass(JsCallback.of("() => 'callback-day'"));
        assertEquals("() => 'callback-day'", respond().getFirst().get("dayCellClass").get("__jsCallback").asString());

        calendar.options().setDayCellClass((String) null);
        assertTrue(respond().getFirst().get("dayCellClass").isNull());
    }

    @Test
    void valueOrCallback_getterCoversBothFormsWithoutDefaultBranch() {
        calendar.options().setDayCellClass("typed-day");
        assertEquals("names typed-day", dayCellClassForm());

        calendar.options().setDayCellClass(JsCallback.of("() => 'x'"));
        assertEquals("callback", dayCellClassForm());
    }

    private String dayCellClassForm() {
        // compiles only while the switch covers every permitted type
        return switch (calendar.options().getDayCellClass()) {
            case ClassName className -> "names " + className.names();
            case JsCallback callback -> "callback";
        };
    }

    @Test
    void contentValue_bothFormsReachTheClient() {
        attach();

        calendar.options().setAllDayHeaderContent("Ganztags");
        assertEquals("Ganztags", respond().getFirst().get("allDayHeaderContent").asString());

        calendar.options().setAllDayHeaderContent(JsCallback.of("info => info.text"));
        assertEquals("info => info.text",
                respond().getFirst().get("allDayHeaderContent").get("__jsCallback").asString());

        calendar.options().setAllDayHeaderContent((String) null);
        assertTrue(respond().getFirst().get("allDayHeaderContent").isNull());
    }

    @Test
    void headerAlignValue_bothFormsReachTheClient() {
        attach();

        calendar.options().setDayHeaderAlign(HeaderAlign.CENTER);
        assertEquals("center", respond().getFirst().get("dayHeaderAlign").asString());

        calendar.options().setDayHeaderAlign(JsCallback.of("() => 'end'"));
        assertEquals("() => 'end'", respond().getFirst().get("dayHeaderAlign").get("__jsCallback").asString());

        calendar.options().setDayHeaderAlign(null);
        assertTrue(respond().getFirst().get("dayHeaderAlign").isNull());
    }

    @Test
    void contentAndHeaderAlign_gettersCoverBothFormsWithoutDefaultBranch() {
        calendar.options().setAllDayHeaderContent("Ganztags");
        calendar.options().setDayHeaderAlign(HeaderAlign.END);
        assertEquals("text Ganztags, align END", contentAndAlignForms());

        calendar.options().setAllDayHeaderContent(ContentValue.ofHtml("<b>Ganztags</b>"));
        assertEquals("html <b>Ganztags</b>, align END", contentAndAlignForms());

        calendar.options().setAllDayHeaderContent(JsCallback.of("info => info.text"));
        calendar.options().setDayHeaderAlign(JsCallback.of("() => 'end'"));
        assertEquals("callback, callback", contentAndAlignForms());
    }

    private String contentAndAlignForms() {
        // compiles only while the switches cover every permitted type
        String content = switch (calendar.options().getAllDayHeaderContent()) {
            case TextContent text -> "text " + text.text();
            case ContentValue.Html html -> "html " + html.html();
            case JsCallback callback -> "callback";
        };
        String align = switch (calendar.options().getDayHeaderAlign()) {
            case HeaderAlign headerAlign -> "align " + headerAlign;
            case JsCallback callback -> "callback";
        };
        return content + ", " + align;
    }

    @Test
    void viewValue_isSentOnlyUnderViews() {
        attach();

        calendar.options().view(CalendarViewImpl.TIME_GRID_WEEK).setWeekends(false);
        JsonNode changes = respond().getFirst();

        assertFalse(changes.get("views").get("timeGridWeek").get("weekends").asBoolean());
        assertFalse(changes.has("weekends"));
    }

    @Test
    void viewGetter_isNull_whenOnlyACalendarWideValueExists() {
        calendar.options().setDayCellClass("calendar-wide");

        assertNull(calendar.options().view(CalendarViewImpl.TIME_GRID_WEEK).getDayCellClass());
    }

    @Test
    void viewGetterAlone_sendsNothing() {
        attach();

        calendar.options().view(CalendarViewImpl.TIME_GRID_WEEK).getWeekends();

        assertEquals(List.of(), respond());
        assertFalse(initialOptions().has("views"));
    }

    @Test
    void viewValue_beforeAttach_isInTheInitialOptionsOnlyUnderViews() {
        calendar.options().view(CalendarViewImpl.TIME_GRID_WEEK).setDayCellClass("week-day");
        attach();

        assertEquals("week-day", initialOptions().get("views").get("timeGridWeek").get("dayCellClass").asString());
        assertFalse(initialOptions().has("dayCellClass"));
    }

    @Test
    void viewValue_null_removesOnlyThatValue() {
        FullCalendarViewOptions week = calendar.options().view(CalendarViewImpl.TIME_GRID_WEEK);
        week.setWeekends(false);
        week.setDayCellClass("week-day");
        attach();

        week.setWeekends(null);
        JsonNode weekValues = respond().getFirst().get("views").get("timeGridWeek");

        assertFalse(weekValues.has("weekends"));
        assertEquals("week-day", weekValues.get("dayCellClass").asString());
    }

    @Test
    void viewClear_removesAllValuesOfThatView_andLeavesTheOthers() {
        calendar.options().setDayCellClass("calendar-wide");
        FullCalendarViewOptions week = calendar.options().view(CalendarViewImpl.TIME_GRID_WEEK);
        week.setWeekends(false);
        week.setDayCellClass("week-day");
        calendar.options().view(CalendarViewImpl.DAY_GRID_MONTH).setDayCellClass("month-day");
        attach();

        week.clear();
        JsonNode changes = respond().getFirst();

        assertFalse(changes.get("views").has("timeGridWeek"));
        assertEquals("month-day", changes.get("views").get("dayGridMonth").get("dayCellClass").asString());
        assertFalse(changes.has("dayCellClass"));
        assertNull(week.getDayCellClass());
        assertEquals(ClassName.of("calendar-wide"), calendar.options().getDayCellClass());
    }

    @Test
    void viewClear_ofTheOnlyView_removesViews() {
        FullCalendarViewOptions week = calendar.options().view(CalendarViewImpl.TIME_GRID_WEEK);
        week.setWeekends(false);
        attach();

        week.clear();

        assertTrue(respond().getFirst().get("views").isNull());
    }

    @Test
    void keptViewReference_worksAfterClear() {
        FullCalendarViewOptions week = calendar.options().view(CalendarViewImpl.TIME_GRID_WEEK);
        week.setWeekends(false);
        attach();
        week.clear();
        respond();

        week.setDayCellClass("week-day");

        assertEquals("week-day",
                respond().getFirst().get("views").get("timeGridWeek").get("dayCellClass").asString());
    }

    @Test
    void viewValuesClearedWhileDetached_areGoneAfterTheReattach() {
        calendar.options().view(CalendarViewImpl.TIME_GRID_WEEK).setWeekends(false);
        attach();

        ui.remove(calendar);
        respond();
        calendar.options().view(CalendarViewImpl.TIME_GRID_WEEK).clear();
        ui.add(calendar);
        respond();

        assertFalse(initialOptions().has("views"));
    }

    @Test
    void keptViewReference_changesTheClientInALaterRequest() {
        FullCalendarViewOptions week = calendar.options().view(CalendarViewImpl.TIME_GRID_WEEK);
        attach();

        week.setDayCellClass("week-day");

        assertEquals("week-day",
                respond().getFirst().get("views").get("timeGridWeek").get("dayCellClass").asString());
    }

    @Test
    void customView_keepsItsDefinition_underALaterViewValue() {
        calendar.setCustomCalendarViews(THREE_DAYS);
        attach();

        calendar.options().view(THREE_DAYS).setDayCellClass("three-day");
        JsonNode threeDays = respond().getFirst().get("views").get("threeDays");

        assertEquals("dayGrid", threeDays.get("type").asString());
        assertEquals(3, threeDays.get("dayCount").asInt());
        assertEquals("three-day", threeDays.get("dayCellClass").asString());
    }

    @Test
    void customView_clear_removesOnlyTheViewValues() {
        calendar.setCustomCalendarViews(THREE_DAYS);
        calendar.options().view(THREE_DAYS).setDayCellClass("three-day");
        attach();

        calendar.options().view(THREE_DAYS).clear();
        JsonNode threeDays = respond().getFirst().get("views").get("threeDays");

        assertEquals("dayGrid", threeDays.get("type").asString());
        assertFalse(threeDays.has("dayCellClass"));
    }

    @Test
    void viewValue_overridesTheSameKeyOfTheCustomViewDefinition() {
        calendar.setCustomCalendarViews(new CustomCalendarView.AnonymousCustomCalendarView("threeDays",
                JsonFactory.createObject().put("type", "dayGrid").put("dayCellClass", "definition")));
        calendar.options().view(THREE_DAYS).setDayCellClass("view-value");
        attach();

        assertEquals("view-value", initialOptions().get("views").get("threeDays").get("dayCellClass").asString());
    }

    @Test
    void rawViews_replaceTheGeneratedViews() {
        calendar.setCustomCalendarViews(THREE_DAYS);
        calendar.options().view(CalendarViewImpl.TIME_GRID_WEEK).setWeekends(false);
        calendar.setViewSpecificOption("listWeek", "weekends", false);
        calendar.options().setRawOption("views", JsonFactory.createObject()
                .set("dayGridMonth", JsonFactory.createObject().put("weekends", false)));
        attach();

        JsonNode views = initialOptions().get("views");
        assertEquals(1, views.size());
        assertFalse(views.get("dayGridMonth").get("weekends").asBoolean());
    }

    @Test
    void oldViewSpecificOption_winsOverTheViewValueOfTheSameKey() {
        calendar.options().view(THREE_DAYS).setDayCellClass("view-value");
        calendar.setViewSpecificOption("threeDays", "dayCellClass", "old-api");
        attach();

        assertEquals("old-api", initialOptions().get("views").get("threeDays").get("dayCellClass").asString());
    }

    @Test
    void oldViewSpecificOption_isMergedIntoTheGeneratedViews() {
        calendar.setCustomCalendarViews(THREE_DAYS);
        calendar.setViewSpecificOption("threeDays", "weekends", false);
        attach();

        JsonNode threeDays = initialOptions().get("views").get("threeDays");
        assertEquals("dayGrid", threeDays.get("type").asString());
        assertFalse(threeDays.get("weekends").asBoolean());
    }

    /**
     * Fields the sync handles apart, each with tests of its own. Adding a field here needs those tests, never only a
     * red test of this class turned green.
     */
    private static final Map<String, String> SYNCED_APART = Map.of(
            "entryDidMount", "OptionSyncHookTest",
            "timeZone", "AutoBrowserSettingsTest");

    /**
     * A value for the fields of a type that is no value type, with the JSON the library reads for it. A field of a new
     * one-form type adds its type here; a type with several forms is a value type and goes to ConverterTest.VALUE_TYPES.
     */
    private static final Map<Class<?>, Map.Entry<Object, JsonNode>> ONE_FORM_SAMPLES = Map.ofEntries(
            sample(Boolean.class, false, JsonUtils.toJsonNode(false)),
            sample(Integer.class, 2, JsonUtils.toJsonNode(2)),
            sample(Double.class, 1.5, JsonUtils.toJsonNode(1.5)),
            sample(String.class, "x", JsonUtils.toJsonNode("x")),
            sample(DayOfWeek.class, DayOfWeek.WEDNESDAY, JsonUtils.toJsonNode(3)),
            sample(LocalDate.class, LocalDate.of(2026, 1, 2), JsonUtils.toJsonNode("2026-01-02")),
            sample(ViewName.class, ViewName.of("timeGridDay"), JsonUtils.toJsonNode("timeGridDay")),
            sample(Set.class, Set.of(DayOfWeek.WEDNESDAY), JsonFactory.createArray().add(3)),
            sample(JsCallback.class, JsCallback.of("() => {}"), JsCallback.of("() => {}").toMarkerJson()));

    /**
     * A value for each parameter type of the setters per form, with the client JSON the field must have after the
     * setter took it. An enum parameter with client values ({@link ClientSideValue}) needs no entry: each constant is
     * tried, with its client value expected.
     */
    private static final Map<Class<?>, Map.Entry<Object, JsonNode>> PARAMETER_SAMPLES = Map.ofEntries(
            sample(String.class, "x", JsonUtils.toJsonNode("x")),
            sample(int.class, 1, JsonUtils.toJsonNode(1)),
            sample(Integer.class, 1, JsonUtils.toJsonNode(1)),
            sample(boolean.class, true, JsonUtils.toJsonNode(true)),
            sample(Boolean.class, true, JsonUtils.toJsonNode(true)),
            sample(BusinessHours.class, BusinessHours.businessWeek(), BusinessHours.businessWeek().toJson()),
            sample(EntryOrder.Key.class, EntryOrder.ascending("x"), JsonUtils.toJsonNode("x")),
            sample(LocalDate.class, LocalDate.of(2026, 1, 2), JsonUtils.toJsonNode("2026-01-02")),
            sample(LocalDateTime.class, LocalDateTime.of(2026, 1, 2, 3, 4), JsonUtils.toJsonNode("2026-01-02T03:04Z")),
            sample(CalendarView.class, CalendarViewImpl.TIME_GRID_DAY, JsonUtils.toJsonNode("timeGridDay")),
            sample(Duration.class, Duration.ofMinutes(90), JsonUtils.toJsonNode("01:30:00")),
            sample(Period.class, Period.ofMonths(1), JsonFactory.createObject().put("months", 1)),
            sample(ConstraintValue.Window.class,
                    ConstraintValue.Window.span(LocalDate.of(2026, 1, 2), LocalDate.of(2026, 1, 5)),
                    JsonFactory.createObject().put("start", "2026-01-02").put("end", "2026-01-05")),
            sample(DayOfWeek.class, DayOfWeek.WEDNESDAY, JsonUtils.toJsonNode(3)),
            sample(Map.class, Map.of("weekday", "long"), JsonFactory.createObject().put("weekday", "long")));

    /**
     * The parameter types of the setters per form of each value type, the setter table of the README section
     * "Value types". A new value type adds its row there and here.
     */
    private static final Map<Class<?>, Set<Class<?>>> PER_FORM_PARAMETERS = Map.ofEntries(
            Map.entry(AutoBoolean.class, Set.of()),
            Map.entry(ClassNameValue.class, Set.of(String.class)),
            Map.entry(ContentValue.class, Set.of(String.class)),
            Map.entry(CssSelectorValue.class, Set.of(String.class)),
            Map.entry(HeaderAlignValue.class, Set.of()),
            Map.entry(ViewNameValue.class, Set.of(String.class, CalendarView.class)),
            Map.entry(OverlapValue.class, Set.of(boolean.class)),
            Map.entry(DateFormatValue.class, Set.of(Map.class)),
            Map.entry(ListDayFormatValue.class, Set.of(Map.class)),
            Map.entry(ListTextValue.class, Set.of(String.class)),
            Map.entry(HeightValue.class, Set.of(int.class, String.class)),
            Map.entry(StickyValue.class, Set.of(String.class)),
            Map.entry(EntryLimitValue.class, Set.of(int.class)),
            Map.entry(BusinessHoursValue.class, Set.of(BusinessHours[].class)),
            Map.entry(ConstraintValue.class, Set.of(String.class, BusinessHours[].class, ConstraintValue.Window[].class)),
            Map.entry(NowValue.class, Set.of(LocalDate.class, LocalDateTime.class)),
            Map.entry(TextValue.class, Set.of(String.class)),
            Map.entry(EntryOrderValue.class, Set.of(String[].class, EntryOrder.Key[].class)),
            Map.entry(DurationValue.class, Set.of(Duration.class, Period.class, String.class)),
            Map.entry(WidthValue.class, Set.of(int.class)),
            Map.entry(DateRangeValue.class, Set.of()),
            Map.entry(WeekNumberCalculationValue.class, Set.of()));

    /**
     * A value and client JSON for one setter whose parameter type has a different sample, keyed by the setter name and
     * parameter type, such as {@code "setSlotDuration(String)"}. A setter not listed here uses PARAMETER_SAMPLES.
     */
    private static final Map<String, Map.Entry<Object, JsonNode>> SETTER_SAMPLES = Map.ofEntries(
            Map.entry("setDateIncrement(String)", Map.entry("08:30", JsonUtils.toJsonNode("08:30:00"))),
            Map.entry("setDefaultAllDayEntryDuration(String)", Map.entry("08:30", JsonUtils.toJsonNode("08:30:00"))),
            Map.entry("setDefaultTimedEntryDuration(String)", Map.entry("08:30", JsonUtils.toJsonNode("08:30:00"))),
            Map.entry("setDuration(String)", Map.entry("08:30", JsonUtils.toJsonNode("08:30:00"))),
            Map.entry("setNextDayThreshold(String)", Map.entry("08:30", JsonUtils.toJsonNode("08:30:00"))),
            Map.entry("setScrollTime(String)", Map.entry("01:00", JsonUtils.toJsonNode("01:00:00"))),
            Map.entry("setSlotDuration(String)", Map.entry("01:00", JsonUtils.toJsonNode("01:00:00"))),
            Map.entry("setSlotHeaderInterval(String)", Map.entry("01:00", JsonUtils.toJsonNode("01:00:00"))),
            Map.entry("setSlotMaxTime(String)", Map.entry("01:00", JsonUtils.toJsonNode("01:00:00"))),
            Map.entry("setSlotMinTime(String)", Map.entry("01:00", JsonUtils.toJsonNode("01:00:00"))),
            Map.entry("setSnapDuration(String)", Map.entry("01:00", JsonUtils.toJsonNode("01:00:00"))));

    /**
     * A second value for the parameter types with two values only, so a setter that ignores its argument and writes
     * one fixed value fails.
     */
    private static final Map<Class<?>, Map.Entry<Object, JsonNode>> SECOND_PARAMETER_SAMPLES = Map.ofEntries(
            sample(boolean.class, false, JsonUtils.toJsonNode(false)),
            sample(Boolean.class, false, JsonUtils.toJsonNode(false)));

    private static Map.Entry<Class<?>, Map.Entry<Object, JsonNode>> sample(Class<?> type, Object value, JsonNode json) {
        return Map.entry(type, Map.entry(value, json));
    }

    @Test
    void syncedApart_namesFieldsWithTheirOwnTests() {
        SYNCED_APART.forEach((name, test) -> {
            assertTrue(Stream.of(CalendarOptions.class, ViewOptions.class)
                    .anyMatch(type -> Arrays.stream(type.getDeclaredFields()).anyMatch(f -> f.getName().equals(name))),
                    "No field " + name);
            Path source = Path.of("src/test/java", getClass().getPackageName().replace('.', '/'), test + ".java");
            assertTrue(assertDoesNotThrow(() -> Files.readString(source)).contains(name),
                    test + " does not test " + name);
        });
    }

    /**
     * Every field has a getter, a setter of its type and its setters per form on the options objects, and its value
     * reaches the client under its library name: calendar-wide, and for a view under {@code views}.
     */
    @Test
    void everyField_isSetAndReadThroughTheOptionsObjects_andReachesTheClient() throws Exception {
        FullCalendarViewOptions weekOptions = calendar.options().view(CalendarViewImpl.TIME_GRID_WEEK);
        Map<String, JsonNode> expectedGlobal = new LinkedHashMap<>();
        Map<String, JsonNode> expectedWeek = new LinkedHashMap<>();
        for (Class<?> dataClass : List.of(CalendarOptions.class, ViewOptions.class)) {
            for (Field field : dataClass.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || SYNCED_APART.containsKey(field.getName())) {
                    continue;
                }
                Map.Entry<Object, JsonNode> sample = sampleOf(field);
                String name = field.isAnnotationPresent(JsonName.class)
                        ? field.getAnnotation(JsonName.class).value() : field.getName();
                setAndGet(calendar.options(), field, sample.getKey());
                expectedGlobal.put(name, sample.getValue());
                if (dataClass == ViewOptions.class) {
                    setAndGet(weekOptions, field, sample.getKey());
                    expectedWeek.put(name, sample.getValue());
                }
            }
        }
        attach();

        expectedGlobal.forEach((name, json) -> assertEquals(json, initialOptions().get(name), name));
        JsonNode week = initialOptions().get("views").get("timeGridWeek");
        expectedWeek.forEach((name, json) -> assertEquals(json, week.get(name), "views.timeGridWeek." + name));
    }

    /** A value for the field and its client form. */
    private static Map.Entry<Object, JsonNode> sampleOf(Field field) {
        Class<?> type = field.getType();
        if (ConverterTest.VALUE_TYPES.containsKey(type)) {
            return ConverterTest.VALUE_TYPES.get(type).samples().entrySet().iterator().next();
        }
        if (type.isEnum() && ClientSideValue.class.isAssignableFrom(type) && !ONE_FORM_SAMPLES.containsKey(type)) {
            Object constant = type.getEnumConstants()[0];
            return Map.entry(constant, JsonUtils.toJsonNode(constant));
        }
        Map.Entry<Object, JsonNode> sample = ONE_FORM_SAMPLES.get(type);
        assertNotNull(sample, "Add a sample of " + type.getSimpleName() + " to ONE_FORM_SAMPLES, for " + field.getName()
                + ", or to ConverterTest.VALUE_TYPES when its option takes several forms");
        return sample;
    }

    /**
     * Sets the field through each of its setters: a setter per form must write the client form of its argument, the
     * setter of its type must set the given value, which the getter returns.
     */
    private static void setAndGet(Object options, Field field, Object value) throws ReflectiveOperationException {
        String suffix = Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
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
        if (ConverterTest.VALUE_TYPES.containsKey(field.getType())) {
            Set<Class<?>> expected = PER_FORM_PARAMETERS.get(field.getType());
            assertNotNull(expected, "Add a row for " + field.getType().getSimpleName() + " to PER_FORM_PARAMETERS");
            assertEquals(expected, perFormSetters.stream().map(m -> m.getParameterTypes()[0]).collect(Collectors.toSet()),
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
        Map.Entry<Object, JsonNode> override = SETTER_SAMPLES.get(setter.getName() + "(" + type.getSimpleName() + ")");
        if (override != null) {
            return List.of(override);
        }
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
                : ((JsonItemPropertyConverter) converter.value().getConstructor().newInstance()).toClientModel(value, null);
    }

    @Test
    void unsetField_isNull() {
        assertNull(calendar.options().getFirstDay());
        assertNull(calendar.options().getInitialDate());
    }

    @Test
    void optionsObject_hasNoPublicConstructor() {
        assertTrue(Arrays.stream(FullCalendarOptions.class.getDeclaredConstructors())
                .noneMatch(constructor -> Modifier.isPublic(constructor.getModifiers())));
    }
}
