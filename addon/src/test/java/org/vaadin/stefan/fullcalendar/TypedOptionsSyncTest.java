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
