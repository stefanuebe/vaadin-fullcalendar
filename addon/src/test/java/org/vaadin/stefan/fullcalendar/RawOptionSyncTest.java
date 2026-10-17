package org.vaadin.stefan.fullcalendar;

import org.vaadin.stefan.fullcalendar.option.JsCallback;
import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The option sync: the client creates its calendar from the element property {@code initialOptions}, afterwards it
 * gets only the changed options, all of one response in one call.
 */
public class RawOptionSyncTest extends BrowserlessTest {

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
    void beforeAttach_rawOptionIsInTheInitialOptions() {
        calendar.options().setRawOption("weekends", false);
        attach();

        assertFalse(initialOptions().get("weekends").asBoolean());
    }

    @Test
    void afterAttach_onlyTheChangedOptionIsSent() {
        calendar.options().setRawOption("weekends", false);
        attach();

        calendar.options().setRawOption("weekends", false);
        calendar.options().setRawOption("dayCount", 3);
        List<JsonNode> calls = respond();

        assertEquals(1, calls.size());
        assertEquals(1, calls.getFirst().size());
        assertEquals(3, calls.getFirst().get("dayCount").asInt());
    }

    @Test
    void afterAttach_severalChangesArriveInOneCall() {
        attach();

        calendar.options().setRawOption("weekends", false);
        calendar.options().setRawOption("dayCount", 3);
        List<JsonNode> calls = respond();

        assertEquals(1, calls.size());
        assertFalse(calls.getFirst().get("weekends").asBoolean());
        assertEquals(3, calls.getFirst().get("dayCount").asInt());
    }

    @Test
    void afterAttach_removedOptionIsSentAsNull() {
        calendar.options().setRawOption("weekends", false);
        attach();

        calendar.options().setRawOption("weekends", null);
        List<JsonNode> calls = respond();

        assertEquals(1, calls.size());
        assertTrue(calls.getFirst().get("weekends").isNull());
    }

    @Test
    void afterAttach_setAndRemovedInOneRequest_sendsNothing() {
        attach();

        calendar.options().setRawOption("weekends", false);
        calendar.options().setRawOption("weekends", null);

        assertEquals(List.of(), respond());
    }

    @Test
    void afterAttach_changedCallerJson_isSentWhenSetAgain() {
        ObjectNode buttons = JsonFactory.createObject().put("text", "A");
        calendar.options().setRawOption("buttons", buttons);
        attach();

        buttons.put("text", "B");
        calendar.options().setRawOption("buttons", buttons);
        List<JsonNode> calls = respond();

        assertEquals("B", calls.getFirst().get("buttons").get("text").asString());
    }

    @Test
    void getRawOption_returnsACopy() {
        calendar.options().setRawOption("buttons", JsonFactory.createObject().put("text", "A"));

        ((ObjectNode) calendar.options().getRawOption("buttons").orElseThrow()).put("text", "B");

        assertEquals("A", calendar.options().getRawOption("buttons").orElseThrow().get("text").asString());
    }

    @Test
    void setRawOption_jsonNull_removesTheOption() {
        calendar.options().setRawOption("weekends", false);
        calendar.options().setRawOption("weekends", JsonFactory.createNull());

        assertTrue(calendar.options().getRawOption("weekends").isEmpty());
    }

    @Test
    void detachedOverAResponse_newClientElementGetsTheStateInTheProperty() {
        calendar.options().setRawOption("eventColor", "red");
        attach();

        calendar.options().setRawOption("weekends", false);
        ui.remove(calendar);
        respond();
        calendar.options().setRawOption("dayCount", 3);
        calendar.options().setRawOption("eventColor", null);
        ui.add(calendar);
        List<JsonNode> calls = respond();

        assertFalse(initialOptions().get("weekends").asBoolean());
        assertEquals(3, initialOptions().get("dayCount").asInt());
        assertFalse(initialOptions().has("eventColor"));
        // the new client element is created from the property, a call would apply the options a second time
        assertEquals(List.of(), calls);

        calendar.options().setRawOption("weekends", true);
        assertTrue(respond().getFirst().get("weekends").asBoolean());
    }

    @Test
    void detachedOverAResponse_reattachWithoutChange_writesTheCurrentState() {
        calendar.options().setRawOption("eventColor", "red");
        attach();
        calendar.options().setRawOption("weekends", false);
        respond();

        ui.remove(calendar);
        respond();
        // stands in for the browser, which creates the new element from whatever the property holds
        calendar.getElement().setPropertyJson("initialOptions", JsonFactory.createObject());
        ui.add(calendar);
        respond();

        assertEquals("red", initialOptions().get("eventColor").asString());
        assertFalse(initialOptions().get("weekends").asBoolean());
    }

    @Test
    void detachAndAttachInOneRoundTrip_keptClientElementGetsEveryOptionAsCall() {
        calendar.options().setRawOption("eventColor", "red");
        calendar.options().setRawOption("weekends", false);
        attach();

        ui.remove(calendar);
        calendar.options().setRawOption("weekends", null);
        calendar.options().setRawOption("dayCount", 3);
        ui.add(calendar);
        List<JsonNode> calls = respond();

        // Flow discards the calls queued before the detach, so the unchanged option goes out too
        assertEquals(1, calls.size());
        assertEquals("red", calls.getFirst().get("eventColor").asString());
        assertTrue(calls.getFirst().get("weekends").isNull());
        assertEquals(3, calls.getFirst().get("dayCount").asInt());
        // the kept element ignores the property, so it is not rewritten
        assertFalse(initialOptions().has("dayCount"));
    }

    @Test
    void attachedToAnotherUi_getsTheCurrentStateInTheProperty() {
        calendar.options().setRawOption("eventColor", "red");
        attach();
        calendar.options().setRawOption("eventColor", null);
        respond();

        // moves the component to a new UI as Flow does for @PreserveOnRefresh, the browser creates a new element
        calendar.getElement().getNode().removeFromTree(false);
        UI newUi = new UI();
        newUi.getInternals().setSession(ui.getSession());
        newUi.add(calendar);
        newUi.getInternals().getStateTree().runExecutionsBeforeClientResponse();

        assertFalse(initialOptions().has("eventColor"));
        assertTrue(newUi.getInternals().dumpPendingJavaScriptInvocations().stream()
                .noneMatch(call -> call.getInvocation().getExpression().contains("setOptions")));
    }

    @Test
    void clientResync_getsTheCurrentStateInTheProperty() {
        calendar.options().setRawOption("eventColor", "red");
        attach();
        calendar.options().setRawOption("eventColor", null);
        respond();

        // on a resync the client rebuilds every element, and Flow attaches the calendar again in the same UI
        ui.getInternals().getStateTree().prepareForResync();
        calendar.options().setRawOption("dayCount", 3);
        List<JsonNode> calls = respond();

        assertFalse(initialOptions().has("eventColor"));
        assertEquals(3, initialOptions().get("dayCount").asInt());
        assertEquals(List.of(), calls);
    }

    @Test
    void viewRawOption_reachesTheClientUnderTheView_andWinsOverTheViewField() {
        FullCalendarViewOptions month = calendar.options().view(CalendarViewImpl.DAY_GRID_MONTH);
        month.setWeekends(true);
        month.setRawOption("weekends", false);
        month.setRawOption("dayCount", 3);
        attach();

        JsonNode view = initialOptions().get("views").get("dayGridMonth");
        assertFalse(view.get("weekends").asBoolean());
        assertEquals(3, view.get("dayCount").asInt());
        assertFalse(initialOptions().has("dayCount"));
        assertEquals(3, month.getRawOption("dayCount").orElseThrow().asInt());
        assertTrue(calendar.options().getRawOption("dayCount").isEmpty());
    }

    @Test
    void viewRawOption_removedOrCleared_isGoneFromTheView() {
        FullCalendarViewOptions month = calendar.options().view(CalendarViewImpl.DAY_GRID_MONTH);
        month.setWeekends(true);
        month.setRawOption("weekends", false);
        month.setRawOption("dayCount", 3);
        attach();

        month.setRawOption("weekends", null);
        List<JsonNode> calls = respond();
        assertTrue(calls.getFirst().get("views").get("dayGridMonth").get("weekends").asBoolean());

        month.clear();
        calls = respond();
        assertTrue(calls.getFirst().get("views").isNull());
        assertTrue(month.getRawOption("dayCount").isEmpty());
    }

    @Test
    void setRawOption_convertsCallbacksAndClientSideValues() {
        calendar.options().setRawOption("dayCellClass", JsCallback.of("() => 'x'"));
        calendar.options().setRawOption("initialView", CalendarViewImpl.TIME_GRID_WEEK);

        assertEquals("() => 'x'", calendar.options().getRawOption("dayCellClass").orElseThrow().get("__jsCallback").asString());
        assertEquals("timeGridWeek", calendar.options().getRawOption("initialView").orElseThrow().asString());
    }

    @Test
    void setRawOption_valueThatIsNoJson_throwsAtTheCall() {
        assertThrows(OptionValueConversionException.class, () -> calendar.options().setRawOption("weekends", new Broken()));
        assertTrue(calendar.options().getRawOption("weekends").isEmpty());

        FullCalendarViewOptions month = calendar.options().view(CalendarViewImpl.DAY_GRID_MONTH);
        assertThrows(OptionValueConversionException.class, () -> month.setRawOption("weekends", new Broken()));
        assertTrue(month.getRawOption("weekends").isEmpty());
    }

    @Test
    void constructorJson_isRawOptions() {
        ObjectNode json = JsonFactory.createObject();
        json.put("weekends", false);
        calendar = new FullCalendar(json);

        assertFalse(calendar.options().getRawOption("weekends").orElseThrow().asBoolean());
    }

    @Test
    void constructorJson_reachesTheInitialOptions_viewsAsCustomViewsInTheGeneratedViews() {
        ObjectNode json = JsonFactory.createObject();
        json.put("weekends", false);
        json.put("timeZone", "Europe/Berlin");
        json.set("views", JsonFactory.createObject().set("myView", JsonFactory.createObject().put("type", "dayGrid")));
        calendar = new FullCalendar(json);
        attach();

        assertFalse(initialOptions().get("weekends").asBoolean());
        assertEquals("Europe/Berlin", initialOptions().get("timeZone").asString(),
                "the time zone comes from the field, not from the raw JSON");
        assertEquals("dayGrid", initialOptions().get("views").get("myView").get("type").asString());
        assertTrue(calendar.getCustomCalendarViews().containsKey("myView"));
    }

    @Test
    void constructorJsonViews_customViewsCanStillBeSetOnce() {
        ObjectNode json = JsonFactory.createObject();
        json.set("views", JsonFactory.createObject().set("myView", JsonFactory.createObject().put("type", "dayGrid")));
        calendar = new FullCalendar(json);

        calendar.setCustomCalendarViews(new CustomCalendarView.AnonymousCustomCalendarView("other",
                JsonFactory.createObject().put("type", "timeGrid")));

        attach();

        assertTrue(initialOptions().get("views").has("myView"));
        assertTrue(initialOptions().get("views").has("other"));
        assertThrows(UnsupportedOperationException.class, () -> calendar.setCustomCalendarViews());
    }

    /**
     * Runs what the server does before its response and returns the client functions called, in order. Flow writes a
     * function call as {@code return $0.name(...)}; other scripts are returned as they are.
     */
    private List<String> respondWithCalledFunctions() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        return ui.getInternals().dumpPendingJavaScriptInvocations().stream()
                .map(invocation -> invocation.getInvocation().getExpression())
                .map(expression -> expression.replaceFirst("(?s)^.*?\\$0\\.(\\w+)\\(.*$", "$1"))
                .toList();
    }

    private static void assertCalledBefore(List<String> functions, String first, String second) {
        assertTrue(functions.contains(first) && functions.contains(second), functions.toString());
        assertTrue(functions.indexOf(first) < functions.indexOf(second), functions.toString());
    }

    @Test
    void optionSetBeforeGotoDate_reachesTheClientBeforeTheCall() {
        attach();

        calendar.options().setRawOption("validRange", JsonFactory.createObject().put("start", "2025-03-01"));
        calendar.gotoDate(LocalDate.of(2025, 2, 1));
        List<String> functions = respondWithCalledFunctions();

        assertCalledBefore(functions, "setOptions", "gotoDate");
    }

    @Test
    void optionSetBeforeGotoDate_inTheRequestOfTheFirstAttach_isInTheProperty() {
        ui.add(calendar);
        calendar.options().setRawOption("validRange", JsonFactory.createObject().put("start", "2025-03-01"));
        calendar.gotoDate(LocalDate.of(2025, 2, 1));
        List<String> functions = respondWithCalledFunctions();

        assertEquals("2025-03-01", initialOptions().get("validRange").get("start").asString());
        assertFalse(functions.contains("setOptions"), functions.toString());
        assertTrue(functions.contains("gotoDate"), functions.toString());
    }

    @Test
    void optionSetBeforeGotoDate_whileDetached_isNotSentToTheNewClientElement() {
        attach();
        ui.remove(calendar);

        calendar.options().setRawOption("validRange", JsonFactory.createObject().put("start", "2025-03-01"));
        calendar.gotoDate(LocalDate.of(2025, 2, 1));
        respond();
        ui.add(calendar);
        List<String> functions = respondWithCalledFunctions();

        assertEquals("2025-03-01", initialOptions().get("validRange").get("start").asString());
        assertFalse(functions.contains("setOptions"), functions.toString());
        assertTrue(functions.contains("gotoDate"), functions.toString());
    }

    @Test
    void optionSetBeforeRender_reachesTheClientBeforeTheCall() {
        attach();

        calendar.options().setRawOption("weekends", false);
        calendar.render();
        List<String> functions = respondWithCalledFunctions();

        assertCalledBefore(functions, "setOptions", "renderCalendar");
    }

    @Test
    void optionSetWhileDetached_reachesTheKeptClientElementBeforeTheRestore() {
        attach();

        ui.remove(calendar);
        calendar.options().setRawOption("weekends", false);
        ui.add(calendar);
        List<String> functions = respondWithCalledFunctions();

        assertCalledBefore(functions, "setOptions", "restoreStateFromServer");
    }

    /** A bean Jackson cannot convert, because its getter throws. */
    public static class Broken {
        public String getValue() {
            throw new IllegalStateException("broken");
        }
    }
}
