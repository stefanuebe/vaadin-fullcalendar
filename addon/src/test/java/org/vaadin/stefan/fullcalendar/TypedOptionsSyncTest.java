package org.vaadin.stefan.fullcalendar;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.lang.reflect.Modifier;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

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

        calendar.setRawOption("firstDay", 3);
        assertEquals(3, respond().getFirst().get("firstDay").asInt());

        calendar.options().setFirstDay(DayOfWeek.TUESDAY);
        assertEquals(List.of(), respond());

        calendar.setRawOption("firstDay", null);
        assertEquals(2, respond().getFirst().get("firstDay").asInt());
        assertEquals(DayOfWeek.TUESDAY, calendar.options().getFirstDay());
    }

    @Test
    void beforeAttach_rawOptionLiesOverTheField() {
        calendar.options().setFirstDay(DayOfWeek.MONDAY);
        calendar.setRawOption("firstDay", 3);
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
    void viewValue_isSentOnlyUnderViews() {
        attach();

        calendar.options().view(CalendarViewImpl.TIME_GRID_WEEK).setEditable(false);
        JsonNode changes = respond().getFirst();

        assertFalse(changes.get("views").get("timeGridWeek").get("editable").asBoolean());
        assertFalse(changes.has("editable"));
    }

    @Test
    void viewGetter_isNull_whenOnlyACalendarWideValueExists() {
        calendar.options().setDayCellClass("calendar-wide");

        assertNull(calendar.options().view(CalendarViewImpl.TIME_GRID_WEEK).getDayCellClass());
    }

    @Test
    void viewGetterAlone_sendsNothing() {
        attach();

        calendar.options().view(CalendarViewImpl.TIME_GRID_WEEK).getEditable();

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
        week.setEditable(false);
        week.setDayCellClass("week-day");
        attach();

        week.setEditable(null);
        JsonNode weekValues = respond().getFirst().get("views").get("timeGridWeek");

        assertFalse(weekValues.has("editable"));
        assertEquals("week-day", weekValues.get("dayCellClass").asString());
    }

    @Test
    void viewClear_removesAllValuesOfThatView_andLeavesTheOthers() {
        calendar.options().setDayCellClass("calendar-wide");
        FullCalendarViewOptions week = calendar.options().view(CalendarViewImpl.TIME_GRID_WEEK);
        week.setEditable(false);
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
        week.setEditable(false);
        attach();

        week.clear();

        assertTrue(respond().getFirst().get("views").isNull());
    }

    @Test
    void keptViewReference_worksAfterClear() {
        FullCalendarViewOptions week = calendar.options().view(CalendarViewImpl.TIME_GRID_WEEK);
        week.setEditable(false);
        attach();
        week.clear();
        respond();

        week.setDayCellClass("week-day");

        assertEquals("week-day",
                respond().getFirst().get("views").get("timeGridWeek").get("dayCellClass").asString());
    }

    @Test
    void viewValuesClearedWhileDetached_areGoneAfterTheReattach() {
        calendar.options().view(CalendarViewImpl.TIME_GRID_WEEK).setEditable(false);
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
        calendar.options().view(CalendarViewImpl.TIME_GRID_WEEK).setEditable(false);
        calendar.setViewSpecificOption("listWeek", "weekends", false);
        calendar.setRawOption("views", JsonFactory.createObject()
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
