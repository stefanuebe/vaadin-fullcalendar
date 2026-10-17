package org.vaadin.stefan.fullcalendar;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.option.ComponentResourceColumn;
import org.vaadin.stefan.fullcalendar.option.JsCallback;
import org.vaadin.stefan.fullcalendar.option.ResourceColumn;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The resource columns set through {@link FullCalendarScheduler#options()}: component columns keep their binding, and
 * resources reach the client under the current options.
 */
class SchedulerResourceColumnsOptionTest extends BrowserlessTest {

    private static final Pattern CALLED_FUNCTION = Pattern.compile("\\$0\\.(\\w+)\\(");

    private FullCalendarScheduler calendar;
    private UI ui;
    private Resource resource;
    private ComponentResourceColumn<TextField> componentColumn;

    @BeforeEach
    void setUp() {
        calendar = new FullCalendarScheduler();
        ui = UI.getCurrent();
        resource = new Resource("r1", "Room 1", null);
        calendar.addResource(resource);
        componentColumn = new ComponentResourceColumn<>("notes", "Notes", r -> new TextField());
    }

    /** Runs what the server does before its response and returns the names of the client calls. */
    private List<String> respond() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        List<String> calls = new ArrayList<>();
        for (PendingJavaScriptInvocation invocation : ui.getInternals().dumpPendingJavaScriptInvocations()) {
            Matcher function = CALLED_FUNCTION.matcher(invocation.getInvocation().getExpression());
            if (function.find()) {
                calls.add(function.group(1));
            }
        }
        return calls;
    }

    private JsonNode initialColumns() {
        return ((ObjectNode) calendar.getElement().getPropertyRaw("initialOptions")).get("resourceColumns");
    }

    @Test
    void componentColumn_setThroughTheOptions_isBoundAndReachesTheClient() {
        ResourceColumn title = new ResourceColumn("title");
        calendar.options().setResourceColumns(List.of(title, componentColumn));
        ui.add(calendar);
        respond();

        assertTrue(componentColumn.isBound());
        assertTrue(componentColumn.getComponent(resource).isPresent());
        assertEquals(List.of(title, componentColumn), calendar.options().getResourceColumns());
        JsonNode columns = initialColumns();
        assertEquals(2, columns.size());
        assertEquals("notes", columns.get(1).get("field").asString());
        assertTrue(JsCallback.isMarker(columns.get(1).get("cellDidMount")), columns.toString());
    }

    @Test
    void emptyListOrNull_unbindsTheComponentColumnAndRemovesTheOption() {
        for (List<ResourceColumn> removal : Arrays.asList(List.<ResourceColumn>of(), null)) {
            calendar.options().setResourceColumns(List.of(componentColumn));
            assertTrue(componentColumn.isBound());
            calendar.options().setResourceColumns(removal);

            assertFalse(componentColumn.isBound());
            assertNull(calendar.options().getResourceColumns());
        }
        ui.add(calendar);
        respond();
        assertNull(initialColumns());
    }

    @Test
    void list_isAnUnmodifiableCopy() {
        ResourceColumn title = new ResourceColumn("title");
        List<ResourceColumn> columns = new ArrayList<>(List.of(title));
        calendar.options().setResourceColumns(columns);
        columns.add(new ResourceColumn("other"));

        assertEquals(List.of(title), calendar.options().getResourceColumns());
        assertThrows(UnsupportedOperationException.class,
                () -> calendar.options().getResourceColumns().add(new ResourceColumn("x")));
    }

    @Test
    void duplicateField_throwsAndKeepsTheColumns() {
        calendar.options().setResourceColumns(List.of(componentColumn));

        assertThrows(IllegalArgumentException.class, () -> calendar.options()
                .setResourceColumns(List.of(new ResourceColumn("a"), new ResourceColumn("a"))));
        assertEquals(List.of(componentColumn), calendar.options().getResourceColumns());
        assertTrue(componentColumn.isBound());
    }

    @Test
    void rawResourceColumns_winOverTheField() {
        calendar.options().setResourceColumns(List.of(componentColumn));
        calendar.options().setRawOption("resourceColumns",
                JsonFactory.createArray().add(JsonFactory.createObject().put("field", "raw")));
        ui.add(calendar);
        respond();

        assertEquals("raw", initialColumns().get(0).get("field").asString());
        // the raw value only wins on the client, the field keeps its columns bound
        assertTrue(componentColumn.isBound());
    }

    @Test
    void componentColumn_setAfterAttach_reachesTheClient() {
        ui.add(calendar);
        respond();

        calendar.options().setResourceColumns(List.of(componentColumn));
        JsonNode columns = sentOptions().get("resourceColumns");

        assertEquals("notes", columns.get(0).get("field").asString());
        assertTrue(JsCallback.isMarker(columns.get(0).get("cellDidMount")), columns.toString());
        assertTrue(componentColumn.getComponent(resource).isPresent());
    }

    @Test
    void columnsRemovedAfterAttach_sendARemoval() {
        calendar.options().setResourceColumns(List.of(componentColumn));
        ui.add(calendar);
        respond();

        calendar.options().setResourceColumns(null);

        assertTrue(sentOptions().get("resourceColumns").isNull());
        assertFalse(componentColumn.isBound());
    }

    /** Runs what the server does before its response and returns the one option change it sends. */
    private JsonNode sentOptions() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        List<JsonNode> sent = ui.getInternals().dumpPendingJavaScriptInvocations().stream()
                .map(PendingJavaScriptInvocation::getInvocation)
                .filter(invocation -> invocation.getExpression().contains("setOptions"))
                .map(invocation -> (JsonNode) invocation.getParameters().getLast())
                .toList();
        assertEquals(1, sent.size());
        return sent.getFirst();
    }

    @Test
    void resources_reachTheClientAfterTheOptionsOfTheSameRequest() {
        ui.add(calendar);
        respond();

        // the resource flush is scheduled before the option sync
        calendar.addResource(new Resource("r2", "Room 2", null));
        calendar.options().setResourceColumns(List.of(componentColumn));

        assertEquals(List.of("setOptions", "addResources"), respond());
    }

    /** Flow keeps the client element when the calendar is detached and attached in one round trip. */
    @Test
    void reattachInOneRoundTrip_reAddsTheResourcesAfterTheOptions() {
        ui.add(calendar);
        respond();

        ui.remove(calendar);
        ui.add(calendar);
        List<String> calls = respond();

        assertTrue(calls.contains("setOptions") && calls.contains("addResources"), calls.toString());
        assertTrue(calls.indexOf("setOptions") < calls.indexOf("addResources"), calls.toString());
    }
}
