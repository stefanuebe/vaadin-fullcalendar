package org.vaadin.stefan.fullcalendar;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@code setHeight} and the other {@code HasSize} methods set the library option {@code height}. A raw {@code height}
 * lies over it.
 */
public class HeightSyncTest extends BrowserlessTest {

    private FullCalendar calendar = new FullCalendar();

    private ObjectNode initialOptions() {
        return (ObjectNode) calendar.getElement().getPropertyRaw("initialOptions");
    }

    private void attach() {
        UI.getCurrent().add(calendar);
        respond();
    }

    /** Runs what the server does before its response and returns the option changes sent to the client. */
    private List<JsonNode> respond() {
        UI ui = UI.getCurrent();
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        return ui.getInternals().dumpPendingJavaScriptInvocations().stream()
                .map(PendingJavaScriptInvocation::getInvocation)
                .filter(invocation -> invocation.getExpression().contains("setOptions"))
                .map(invocation -> (JsonNode) invocation.getParameters().getLast())
                .toList();
    }

    @Test
    void newCalendar_hasFullHeight() {
        attach();

        assertEquals("100%", initialOptions().get("height").asString());
    }

    @Test
    void setHeight_beforeAttach_isInTheInitialOptions() {
        calendar.setHeight("500px");
        attach();

        assertEquals("500px", initialOptions().get("height").asString());
    }

    @Test
    void setHeight_afterAttach_reachesTheClient() {
        attach();

        calendar.setHeight("500px");

        assertEquals(List.of(JsonFactory.createObject().put("height", "500px")), respond());
    }

    @Test
    void setSizeUndefined_removesTheHeight() {
        attach();

        calendar.setSizeUndefined();

        assertEquals(List.of(JsonFactory.createObject().putNull("height")), respond());
    }

    @Test
    void heightInTheConstructorJson_skipsTheFullHeight() {
        calendar = new FullCalendar(JsonFactory.createObject().put("height", 500));
        attach();

        assertEquals(500, initialOptions().get("height").asInt());

        calendar.setHeight(null);
        assertTrue(respond().isEmpty(), "the height of the JSON stays");

        calendar.options().setRawOption("height", null);

        assertEquals(List.of(JsonFactory.createObject().putNull("height")), respond(), "no full height below it");
    }

    @Test
    void rawHeight_winsOverSetHeight_andRemovingItBringsSetHeightBack() {
        calendar.options().setRawOption("height", "300px");
        attach();

        calendar.setHeight("500px");

        assertEquals("300px", initialOptions().get("height").asString());
        assertTrue(respond().isEmpty());

        calendar.options().setRawOption("height", null);

        assertEquals(List.of(JsonFactory.createObject().put("height", "500px")), respond());
    }
}
