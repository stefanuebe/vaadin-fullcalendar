package org.vaadin.stefan.fullcalendar;

import org.vaadin.stefan.fullcalendar.option.JsCallback;
import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.BooleanNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The protected sync hook {@link FullCalendar#contributeOptions(Map)} and its first user, {@code entryDidMount}: the
 * callback merged with the native entry listeners and the automatic entry id. Also the fields of options the client has
 * its own handler for, such as {@code viewDidMount} and {@code navLinkDayClick}, which the hook marks to run after
 * that handler.
 */
public class OptionSyncHookTest extends BrowserlessTest {

    private static final String USER_CALLBACK = "function(info) { info.el.title = 'user'; }";

    private static ObjectNode initialOptions(FullCalendar calendar) {
        return (ObjectNode) calendar.getElement().getPropertyRaw("initialOptions");
    }

    /** The eventDidMount function the client gets first, or null when it gets none. */
    private static String entryDidMountOf(ObjectNode options) {
        JsonNode marker = options.get("eventDidMount");
        return marker == null ? null : marker.get("__jsCallback").asString();
    }

    private static void attach(FullCalendar calendar) {
        UI.getCurrent().add(calendar);
        respond();
    }

    /** Runs what the server does before its response and returns the client calls it made. */
    private static List<PendingJavaScriptInvocation> respond() {
        UI ui = UI.getCurrent();
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        return ui.getInternals().dumpPendingJavaScriptInvocations();
    }

    /** The option changes among the given client calls. */
    private static List<ObjectNode> optionChanges(List<PendingJavaScriptInvocation> calls) {
        return calls.stream()
                .map(PendingJavaScriptInvocation::getInvocation)
                .filter(invocation -> invocation.getExpression().contains("setOptions"))
                .map(invocation -> (ObjectNode) invocation.getParameters().getLast())
                .toList();
    }

    @Test
    void entryDidMount_isMergedWithTheEntryIdAndTheNativeListeners() {
        FullCalendar calendar = new FullCalendar();
        calendar.options().setEntryDidMount(JsCallback.of(USER_CALLBACK));
        calendar.addEntryNativeEventListener("click", "e => console.log(e)");

        String merged = entryDidMountOf(initialOptions(calendar));

        assertTrue(merged.contains("info.el.title = 'user';"), merged);
        assertTrue(merged.contains("'entry-' + arguments[0].event.id"), merged);
        assertTrue(merged.contains("addEventListener('click', e => console.log(e))"), merged);
    }

    @Test
    void withoutCallback_theEntryIdIsSentByDefault() {
        FullCalendar calendar = new FullCalendar();

        assertTrue(entryDidMountOf(initialOptions(calendar)).contains("'entry-' + arguments[0].event.id"));
    }

    @Test
    void nothingToMerge_sendsNoEntryDidMount() {
        FullCalendar calendar = new FullCalendar();
        calendar.setAutoProvideEntryIdOnClient(false);

        assertNull(entryDidMountOf(initialOptions(calendar)));
    }

    @Test
    void nativeListenerAddedAfterAttach_goesThroughTheSync() {
        FullCalendar calendar = new FullCalendar();
        attach(calendar);

        calendar.addEntryNativeEventListener("click", "e => console.log(e)");
        List<PendingJavaScriptInvocation> calls = respond();

        List<ObjectNode> changes = optionChanges(calls);
        assertEquals(1, changes.size());
        assertTrue(entryDidMountOf(changes.getFirst()).contains("addEventListener('click'"));
        assertTrue(calls.stream().noneMatch(call -> call.getInvocation().getExpression().contains("setOption(")),
                "no separate call for eventDidMount");
    }

    @Test
    void removingTheLastPart_sendsARemoval() {
        FullCalendar calendar = new FullCalendar();
        calendar.setAutoProvideEntryIdOnClient(false);
        var registration = calendar.addEntryNativeEventListener("click", "e => console.log(e)");
        attach(calendar);

        registration.remove();

        assertEquals(List.of(JsonFactory.createObject().putNull("eventDidMount")), optionChanges(respond()));
    }

    @Test
    void rawEventDidMount_winsOverTheMerge() {
        FullCalendar calendar = new FullCalendar();
        attach(calendar);
        calendar.options().setRawOption("eventDidMount", JsCallback.of(USER_CALLBACK));
        respond();

        calendar.addEntryNativeEventListener("click", "e => console.log(e)");

        // the merged value changed under the raw option, which the client keeps
        assertEquals(List.of(), optionChanges(respond()));

        calendar.options().setRawOption("eventDidMount", null);

        List<ObjectNode> changes = optionChanges(respond());
        assertEquals(1, changes.size());
        assertTrue(entryDidMountOf(changes.getFirst()).contains("addEventListener('click'"),
                "removing the raw option brings the merged callback back");
    }

    @Test
    void subclass_addsAndReplacesKeys() {
        FullCalendar calendar = new FullCalendar() {
            @Override
            protected void contributeOptions(Map<String, JsonNode> options) {
                super.contributeOptions(options);
                options.put("computed", StringNode.valueOf("value"));
                options.put("weekends", BooleanNode.FALSE);
            }
        };
        calendar.options().setWeekends(true);

        ObjectNode options = initialOptions(calendar);

        assertEquals("value", options.get("computed").asString());
        assertFalse(options.get("weekends").asBoolean(), "the hook replaces the field value");
        assertNotNull(options.get("eventDidMount"), "super keeps the merged callback");
    }

    @Test
    void subclass_rawOptionWinsOverTheHook() {
        FullCalendar calendar = new FullCalendar() {
            @Override
            protected void contributeOptions(Map<String, JsonNode> options) {
                options.put("weekends", BooleanNode.FALSE);
            }
        };
        calendar.options().setRawOption("weekends", true);

        assertTrue(initialOptions(calendar).get("weekends").asBoolean());
    }

    @Test
    void viewDidMountField_isMarkedToRunAfterTheClientHandler() {
        FullCalendar calendar = new FullCalendar();
        calendar.options().setViewDidMount(JsCallback.of(USER_CALLBACK));
        attach(calendar);

        JsonNode value = initialOptions(calendar).get("viewDidMount");

        assertEquals(USER_CALLBACK, value.get("__jsCallback").asString());
        assertTrue(value.get(FullCalendar.AFTER_CLIENT_HANDLER_KEY).asBoolean());
    }

    @Test
    void viewDidMountField_setAfterAttach_isSentMarked_andClearingRemovesIt() {
        FullCalendar calendar = new FullCalendar();
        attach(calendar);

        calendar.options().setViewDidMount(JsCallback.of(USER_CALLBACK));
        List<ObjectNode> changes = optionChanges(respond());

        assertEquals(1, changes.size());
        assertTrue(changes.getFirst().get("viewDidMount").get(FullCalendar.AFTER_CLIENT_HANDLER_KEY).asBoolean());

        calendar.options().setViewDidMount(null);

        assertEquals(List.of(JsonFactory.createObject().putNull("viewDidMount")), optionChanges(respond()),
                "the removal gives the client its own handler back");
    }

    @Test
    void rawViewDidMount_isNotMarked_andWinsOverTheField() {
        FullCalendar calendar = new FullCalendar();
        calendar.options().setViewDidMount(JsCallback.of(USER_CALLBACK));
        calendar.options().setRawOption("viewDidMount", JsCallback.of("function(info) {}"));

        JsonNode value = initialOptions(calendar).get("viewDidMount");

        assertEquals("function(info) {}", value.get("__jsCallback").asString());
        assertFalse(value.has(FullCalendar.AFTER_CLIENT_HANDLER_KEY), "a raw option replaces the client's handler");
    }

    @Test
    void removingRawViewDidMount_bringsTheMarkedFieldBack() {
        FullCalendar calendar = new FullCalendar();
        calendar.options().setViewDidMount(JsCallback.of(USER_CALLBACK));
        calendar.options().setRawOption("viewDidMount", JsCallback.of("function(info) {}"));
        attach(calendar);

        calendar.options().setRawOption("viewDidMount", null);
        List<ObjectNode> changes = optionChanges(respond());

        assertEquals(1, changes.size());
        JsonNode value = changes.getFirst().get("viewDidMount");
        assertEquals(USER_CALLBACK, value.get("__jsCallback").asString());
        assertTrue(value.get(FullCalendar.AFTER_CLIENT_HANDLER_KEY).asBoolean());
    }

    @Test
    void navLinkClickFields_areMarkedToRunAfterTheClientHandler() {
        FullCalendar calendar = new FullCalendar();
        calendar.options().setNavLinkDayClick(JsCallback.of(USER_CALLBACK));
        calendar.options().setNavLinkWeekClick(JsCallback.of(USER_CALLBACK));
        attach(calendar);

        for (String key : List.of("navLinkDayClick", "navLinkWeekClick")) {
            JsonNode value = initialOptions(calendar).get(key);
            assertEquals(USER_CALLBACK, value.get("__jsCallback").asString(), key);
            assertTrue(value.get(FullCalendar.AFTER_CLIENT_HANDLER_KEY).asBoolean(), key);
        }
    }

    @Test
    void navLinkClickFields_setAfterAttach_areSentMarked_andClearingRemovesThem() {
        FullCalendar calendar = new FullCalendar();
        attach(calendar);

        calendar.options().setNavLinkDayClick(JsCallback.of(USER_CALLBACK));
        calendar.options().setNavLinkWeekClick(JsCallback.of(USER_CALLBACK));
        List<ObjectNode> changes = optionChanges(respond());

        assertEquals(1, changes.size());
        for (String key : List.of("navLinkDayClick", "navLinkWeekClick")) {
            assertTrue(changes.getFirst().get(key).get(FullCalendar.AFTER_CLIENT_HANDLER_KEY).asBoolean(), key);
        }

        calendar.options().setNavLinkDayClick(null);
        calendar.options().setNavLinkWeekClick(null);

        assertEquals(List.of(JsonFactory.createObject().putNull("navLinkDayClick").putNull("navLinkWeekClick")),
                optionChanges(respond()), "the removal gives the client its own handler back");
    }

    @Test
    void rawNavLinkDayClick_isNotMarked() {
        FullCalendar calendar = new FullCalendar();
        calendar.options().setNavLinkDayClick(JsCallback.of(USER_CALLBACK));
        calendar.options().setRawOption("navLinkDayClick", JsCallback.of("function(date) {}"));
        attach(calendar);

        JsonNode value = initialOptions(calendar).get("navLinkDayClick");

        assertEquals("function(date) {}", value.get("__jsCallback").asString());
        assertFalse(value.has(FullCalendar.AFTER_CLIENT_HANDLER_KEY), "a raw option replaces the client's handler");
    }
}
