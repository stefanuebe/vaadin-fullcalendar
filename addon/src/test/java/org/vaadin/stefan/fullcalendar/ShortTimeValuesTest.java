package org.vaadin.stefan.fullcalendar;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * A {@link LocalTime} or {@link LocalDateTime} without converter of its own is sent like its {@code toString()},
 * so zero seconds are left out (#330), wherever the add-on serializes values with Jackson.
 */
public class ShortTimeValuesTest extends BrowserlessTest {

    private static final String KEY = "myOption";

    private FullCalendar calendar;

    @BeforeEach
    void setUp() {
        calendar = new FullCalendar();
    }

    /** Client value of the custom option in the initial options, which the client reads on attach. */
    private JsonNode initialValue() {
        return ((JsonNode) calendar.getElement().getPropertyRaw("initialOptions")).get(KEY);
    }

    /** Attaches the calendar and returns the client value sent by the next option sync. */
    private JsonNode valueAfterAttach(Object value) {
        UI ui = UI.getCurrent();
        ui.add(calendar);
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().dumpPendingJavaScriptInvocations();

        calendar.setOption(KEY, value);

        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        List<PendingJavaScriptInvocation> invocations = ui.getInternals().dumpPendingJavaScriptInvocations();
        for (PendingJavaScriptInvocation invocation : invocations) {
            List<Object> parameters = invocation.getInvocation().getParameters();
            if (invocation.getInvocation().getExpression().contains("setOptions")
                    && parameters.getLast() instanceof JsonNode changes && changes.has(KEY)) {
                return changes.get(KEY);
            }
        }

        return fail("no setOptions call for " + KEY);
    }

    @Test
    void localTime_beforeAttach_leavesOutZeroSeconds() {
        calendar.setOption(KEY, LocalTime.of(10, 0));
        assertEquals("10:00", initialValue().asString());

        calendar.setOption(KEY, LocalTime.of(10, 0, 30));
        assertEquals("10:00:30", initialValue().asString());
    }

    @Test
    void localTime_afterAttach_leavesOutZeroSeconds() {
        assertEquals("10:00", valueAfterAttach(LocalTime.of(10, 0)).asString());
    }

    @Test
    void localTimeWithSeconds_afterAttach_keepsSeconds() {
        assertEquals("10:00:30", valueAfterAttach(LocalTime.of(10, 0, 30)).asString());
    }

    @Test
    void localDateTime_beforeAndAfterAttach_leavesOutZeroSeconds() {
        LocalDateTime value = LocalDateTime.of(2025, 3, 10, 10, 0);

        calendar.setOption(KEY, value);
        assertEquals("2025-03-10T10:00", initialValue().asString());
        calendar.setOption(KEY, null); // only a change reaches an attached calendar
        assertEquals("2025-03-10T10:00", valueAfterAttach(value).asString());
    }

    @Test
    void localDateTimeWithSeconds_afterAttach_keepsSeconds() {
        assertEquals("2025-03-10T10:00:30", valueAfterAttach(LocalDateTime.of(2025, 3, 10, 10, 0, 30)).asString());
    }

    @Test
    void viewSpecificOption_leavesOutZeroSeconds() {
        calendar.setViewSpecificOption("dayGridMonth", KEY, LocalTime.of(10, 0));

        JsonNode views = ((JsonNode) calendar.getElement().getPropertyRaw("initialOptions")).get("views");
        assertEquals("10:00", views.get("dayGridMonth").get(KEY).asString());
    }

    @Test
    void nestedValues_leaveOutZeroSeconds() {
        calendar.setOption(KEY, Map.of("list", List.of(LocalTime.of(10, 0)),
                "record", new Slot(LocalTime.of(9, 0), LocalDateTime.of(2025, 3, 10, 9, 0))));

        JsonNode value = initialValue();
        assertEquals("10:00", value.get("list").get(0).asString());
        assertEquals("09:00", value.get("record").get("start").asString());
        assertEquals("2025-03-10T09:00", value.get("record").get("at").asString());
    }

    @Test
    void restoredOnReattach_leavesOutZeroSeconds() {
        UI ui = UI.getCurrent();
        ui.add(calendar);
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        calendar.setOption(KEY, LocalTime.of(10, 0));
        ui.remove(calendar);
        ui.getInternals().dumpPendingJavaScriptInvocations();

        ui.add(calendar);
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();

        JsonNode restored = ui.getInternals().dumpPendingJavaScriptInvocations().stream()
                .map(PendingJavaScriptInvocation::getInvocation)
                .filter(invocation -> invocation.getExpression().contains("setOptions"))
                .map(invocation -> (JsonNode) invocation.getParameters().getLast())
                .findFirst().orElseGet(() -> fail("no setOptions call"));
        assertEquals("10:00", restored.get(KEY).asString());
    }

    @Test
    void extendedPropOfEntry_leavesOutZeroSeconds() {
        Entry entry = new Entry();
        entry.setExtendedProp("time", LocalTime.of(10, 0));
        entry.setExtendedProp("at", LocalDateTime.of(2025, 3, 10, 10, 0));

        JsonNode props = entry.toJson().get("extendedProps");
        assertEquals("10:00", props.get("time").asString());
        assertEquals("2025-03-10T10:00", props.get("at").asString());
    }

    record Slot(LocalTime start, LocalDateTime at) {
    }
}
