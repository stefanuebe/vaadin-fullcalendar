package org.vaadin.stefan.fullcalendar;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

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
        calendar.setRawOption("weekends", false);
        attach();

        assertFalse(initialOptions().get("weekends").asBoolean());
    }

    @Test
    void afterAttach_onlyTheChangedOptionIsSent() {
        calendar.setRawOption("weekends", false);
        attach();

        calendar.setRawOption("weekends", false);
        calendar.setRawOption("dayCount", 3);
        List<JsonNode> calls = respond();

        assertEquals(1, calls.size());
        assertEquals(1, calls.getFirst().size());
        assertEquals(3, calls.getFirst().get("dayCount").asInt());
    }

    @Test
    void afterAttach_severalChangesArriveInOneCall() {
        attach();

        calendar.setRawOption("weekends", false);
        calendar.setRawOption("dayCount", 3);
        List<JsonNode> calls = respond();

        assertEquals(1, calls.size());
        assertFalse(calls.getFirst().get("weekends").asBoolean());
        assertEquals(3, calls.getFirst().get("dayCount").asInt());
    }

    @Test
    void afterAttach_removedOptionIsSentAsNull() {
        calendar.setRawOption("weekends", false);
        attach();

        calendar.setRawOption("weekends", null);
        List<JsonNode> calls = respond();

        assertEquals(1, calls.size());
        assertTrue(calls.getFirst().get("weekends").isNull());
    }

    @Test
    void afterAttach_setAndRemovedInOneRequest_sendsNothing() {
        attach();

        calendar.setRawOption("weekends", false);
        calendar.setRawOption("weekends", null);

        assertEquals(List.of(), respond());
    }

    @Test
    void afterAttach_changedCallerJson_isSentWhenSetAgain() {
        ObjectNode buttons = JsonFactory.createObject().put("text", "A");
        calendar.setRawOption("buttons", buttons);
        attach();

        buttons.put("text", "B");
        calendar.setRawOption("buttons", buttons);
        List<JsonNode> calls = respond();

        assertEquals("B", calls.getFirst().get("buttons").get("text").asString());
    }

    @Test
    void getRawOption_returnsACopy() {
        calendar.setRawOption("buttons", JsonFactory.createObject().put("text", "A"));

        ((ObjectNode) calendar.getRawOption("buttons").orElseThrow()).put("text", "B");

        assertEquals("A", calendar.getRawOption("buttons").orElseThrow().get("text").asString());
    }

    @Test
    void setRawOption_jsonNull_removesTheOption() {
        calendar.setRawOption("weekends", false);
        calendar.setRawOption("weekends", JsonFactory.createNull());

        assertTrue(calendar.getRawOption("weekends").isEmpty());
    }

    @Test
    void detachedBeforeTheResponse_optionArrivesAfterTheReattach() {
        attach();

        calendar.setRawOption("weekends", false);
        ui.remove(calendar);
        respond();
        calendar.setRawOption("dayCount", 3);
        ui.add(calendar);
        List<JsonNode> calls = respond();

        assertFalse(initialOptions().get("weekends").asBoolean());
        assertEquals(3, initialOptions().get("dayCount").asInt());
        assertFalse(calls.getLast().get("weekends").asBoolean());
        assertEquals(3, calls.getLast().get("dayCount").asInt());

        calendar.setRawOption("weekends", true);
        assertTrue(respond().getFirst().get("weekends").asBoolean());
    }

    @Test
    void attachedToAnotherUi_getsTheCurrentStateInTheProperty() {
        calendar.setRawOption("eventColor", "red");
        attach();
        calendar.setRawOption("eventColor", null);
        respond();

        // moves the component to a new UI as Flow does for @PreserveOnRefresh, the browser creates a new element
        calendar.getElement().removeFromTree();
        UI newUi = new UI();
        newUi.getInternals().setSession(ui.getSession());
        newUi.add(calendar);
        newUi.getInternals().getStateTree().runExecutionsBeforeClientResponse();

        assertFalse(initialOptions().has("eventColor"));
    }

    @Test
    void setRawOption_convertsCallbacksAndClientSideValues() {
        calendar.setRawOption("dayCellClass", JsCallback.of("() => 'x'"));
        calendar.setRawOption("initialView", CalendarViewImpl.TIME_GRID_WEEK);

        assertEquals("() => 'x'", calendar.getRawOption("dayCellClass").orElseThrow().get("__jsCallback").asString());
        assertEquals("timeGridWeek", calendar.getRawOption("initialView").orElseThrow().asString());
    }

    @Test
    void setRawOption_valueThatIsNoJson_throwsAtTheCall() {
        assertThrows(IllegalArgumentException.class, () -> calendar.setRawOption("weekends", new Broken()));
        assertTrue(calendar.getRawOption("weekends").isEmpty());
    }

    @Test
    void constructorJson_isRawOptions() {
        ObjectNode json = JsonFactory.createObject();
        json.put("weekends", false);
        calendar = new FullCalendar(json);

        assertFalse(calendar.getRawOption("weekends").orElseThrow().asBoolean());
    }

    @Test
    void constructorJson_reachesTheInitialOptions_withoutViewsAndTimeZone() {
        ObjectNode json = JsonFactory.createObject();
        json.put("weekends", false);
        json.put("timeZone", "Europe/Berlin");
        json.set("views", JsonFactory.createObject().set("myView", JsonFactory.createObject().put("type", "dayGrid")));
        calendar = new FullCalendar(json);
        attach();

        assertFalse(initialOptions().get("weekends").asBoolean());
        assertTrue(calendar.getRawOption("timeZone").isEmpty());
        assertTrue(calendar.getRawOption("views").isEmpty());
    }

    @Test
    void oldOptionApi_removalAfterAttach_isSentAsNull() {
        calendar.setOption(Option.WEEKENDS, false);
        attach();

        calendar.removeOption(Option.WEEKENDS);
        List<JsonNode> calls = respond();

        assertTrue(calls.getFirst().get("weekends").isNull());
    }

    @Test
    void oldOptionApi_writesRawOptions() {
        calendar.setOption(Option.WEEKENDS, false);

        assertFalse(calendar.getRawOption("weekends").orElseThrow().asBoolean());
        assertEquals(Boolean.FALSE, calendar.getOption(Option.WEEKENDS).orElseThrow());
    }

    /** A bean Jackson cannot convert, because its getter throws. */
    public static class Broken {
        public String getValue() {
            throw new IllegalStateException("broken");
        }
    }
}
