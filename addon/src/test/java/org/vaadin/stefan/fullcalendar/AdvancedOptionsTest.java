package org.vaadin.stefan.fullcalendar;

import org.vaadin.stefan.fullcalendar.option.BusinessHours;
import org.vaadin.stefan.fullcalendar.option.CalendarDuration;
import org.vaadin.stefan.fullcalendar.option.ConstraintValue;
import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.time.DayOfWeek;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for advanced and niche options.
 * Covers eventConstraint, dateIncrement/dateAlignment,
 * view-specific options, validRange/selectOverlap callbacks,
 * navigation methods, and getCurrentIntervalStart/End.
 */
public class AdvancedOptionsTest extends BrowserlessTest {

    private FullCalendar calendar;

    @BeforeEach
    void setUp() {
        calendar = new FullCalendar();
    }

    /** The initial options, which the client reads on attach. They reach the property with the response. */
    private JsonNode initialOptions() {
        UI ui = UI.getCurrent();
        ui.add(calendar);
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        return (JsonNode) calendar.getElement().getPropertyRaw("initialOptions");
    }

    // -------------------------------------------------------------------------
    // 7.6 entryConstraint
    // -------------------------------------------------------------------------

    @Test
    void setEntryConstraint_string_storesOption() {
        calendar.options().setEntryConstraint("myGroup");
        assertEquals(ConstraintValue.ofGroupId("myGroup"), calendar.options().getEntryConstraint());
        assertEquals("myGroup", initialOptions().get("eventConstraint").asString());
    }

    @Test
    void setEntryConstraint_businessHours_sendsJsonObject() {
        BusinessHours hours = BusinessHours.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY);
        calendar.options().setEntryConstraint(hours);
        assertEquals(ConstraintValue.ofWindows(hours), calendar.options().getEntryConstraint());
        assertTrue(initialOptions().has("eventConstraint"));
        assertFalse(initialOptions().get("eventConstraint").isString());
    }

    @Test
    void setEntryConstraintToBusinessHours_storesString() {
        calendar.options().setEntryConstraint(ConstraintValue.BUSINESS_HOURS);
        assertEquals(ConstraintValue.BUSINESS_HOURS, calendar.options().getEntryConstraint());
        assertEquals("businessHours", initialOptions().get("eventConstraint").asString());
    }

    @Test
    void setEntryConstraint_null_clearsOption() {
        calendar.options().setEntryConstraint("myGroup");
        calendar.options().setEntryConstraint((String) null);
        assertNull(calendar.options().getEntryConstraint());
        assertFalse(initialOptions().has("eventConstraint"));
    }

    // -------------------------------------------------------------------------
    // 7.7 dateIncrement and dateAlignment
    // -------------------------------------------------------------------------

    @Test
    void setDateIncrement_storesOption() {
        calendar.options().setDateIncrement(CalendarDuration.ofWeeks(1));
        assertEquals(CalendarDuration.ofWeeks(1), calendar.options().getDateIncrement());
        assertEquals(1, initialOptions().get("dateIncrement").get("weeks").asInt());
    }

    @Test
    void setDateIncrement_null_clearsOption() {
        calendar.options().setDateIncrement(CalendarDuration.ofWeeks(1));
        calendar.options().setDateIncrement((CalendarDuration) null);
        assertNull(calendar.options().getDateIncrement());
        assertFalse(initialOptions().has("dateIncrement"));
    }

    @Test
    void setDateAlignment_storesOption() {
        calendar.options().setDateAlignment("week");
        assertEquals("week", calendar.options().getDateAlignment());
        assertEquals("week", initialOptions().get("dateAlignment").asString());
    }

    @Test
    void setDateAlignment_null_clearsOption() {
        calendar.options().setDateAlignment("week");
        calendar.options().setDateAlignment(null);
        assertNull(calendar.options().getDateAlignment());
        assertFalse(initialOptions().has("dateAlignment"));
    }

    // -------------------------------------------------------------------------
    // 7.13 view options
    // -------------------------------------------------------------------------

    @Test
    void viewOptions_field_storesNestedJson() {
        calendar.options().view(CalendarViewImpl.DAY_GRID_MONTH).setDayMaxEntries(3);
        ObjectNode views = (ObjectNode) initialOptions().get("views");
        assertTrue(views.has("dayGridMonth"));
        assertEquals(3, views.get("dayGridMonth").get("dayMaxEvents").asInt());
    }

    @Test
    void viewOptions_duration_storesNestedJson() {
        calendar.options().view(CalendarViewImpl.TIME_GRID_WEEK).setSlotDuration(Duration.ofMinutes(30));
        ObjectNode views = (ObjectNode) initialOptions().get("views");
        assertTrue(views.has("timeGridWeek"));
        assertEquals(30, views.get("timeGridWeek").get("slotDuration").get("minutes").asInt());
    }

    @Test
    void viewOptions_boolean_storesNestedJson() {
        calendar.options().view(CalendarViewImpl.DAY_GRID_MONTH).setNowIndicator(true);
        ObjectNode views = (ObjectNode) initialOptions().get("views");
        String viewKey = CalendarViewImpl.DAY_GRID_MONTH.getClientSideValue();
        assertTrue(views.has(viewKey));
        assertTrue(views.get(viewKey).get("nowIndicator").asBoolean());
    }

    @Test
    void viewOptions_multipleViews_storesSeparately() {
        calendar.options().view(CalendarViewImpl.DAY_GRID_MONTH).setDayMaxEntries(3);
        calendar.options().view(CalendarViewImpl.TIME_GRID_WEEK).setSlotDuration(Duration.ofMinutes(30));
        ObjectNode views = (ObjectNode) initialOptions().get("views");
        assertTrue(views.has("dayGridMonth"));
        assertTrue(views.has("timeGridWeek"));
        assertEquals(3, views.get("dayGridMonth").get("dayMaxEvents").asInt());
        assertEquals(30, views.get("timeGridWeek").get("slotDuration").get("minutes").asInt());
    }

    @Test
    void viewOptions_fieldAndRawOption_storeAllEntries() {
        FullCalendarViewOptions list = calendar.options().view(CalendarViewImpl.LIST_WEEK);
        list.setNoEntriesText("None");
        list.setRawOption("listDaySideFormat", "DD");
        ObjectNode views = (ObjectNode) initialOptions().get("views");
        assertTrue(views.has("listWeek"));
        assertEquals("None", views.get("listWeek").get("noEventsText").asString());
        assertEquals("DD", views.get("listWeek").get("listDaySideFormat").asString());
    }

    @Test
    void viewOptions_null_value_removesEntireViewNode() {
        // Set an option and then null it - the view node must not stay behind empty
        calendar.options().view(CalendarViewImpl.DAY_GRID_MONTH).setDayMaxEntries(3);
        calendar.options().view(CalendarViewImpl.DAY_GRID_MONTH).setDayMaxEntries(null);
        JsonNode views = initialOptions().get("views");
        assertTrue(views == null || !views.has("dayGridMonth"), "empty dayGridMonth view node must be removed");
    }

    @Test
    void viewOptions_multipleKeys_removeOneKeepOther() {
        FullCalendarViewOptions month = calendar.options().view(CalendarViewImpl.DAY_GRID_MONTH);
        month.setDayMaxEntries(3);
        month.setNowIndicator(true);
        // Remove only one key
        month.setDayMaxEntries(null);
        ObjectNode views = (ObjectNode) initialOptions().get("views");
        assertTrue(views.has("dayGridMonth"), "dayGridMonth node must remain because nowIndicator is still set");
        assertFalse(views.get("dayGridMonth").has("dayMaxEvents"), "removed key must be absent");
        assertTrue(views.get("dayGridMonth").has("nowIndicator"), "remaining key must still be present");
    }

    @Test
    void viewOptions_view_null_throws() {
        assertThrows(NullPointerException.class, () -> calendar.options().view(null));
    }

    @Test
    void viewOptions_rawOptionName_null_throws() {
        assertThrows(NullPointerException.class,
                () -> calendar.options().view(CalendarViewImpl.DAY_GRID_MONTH).setRawOption(null, 3));
    }

    // -------------------------------------------------------------------------
    // 7.9 getCurrentIntervalStart / getCurrentIntervalEnd
    // -------------------------------------------------------------------------

    @Test
    void getCurrentIntervalStart_emptyBeforeFirstRender() {
        // Fresh calendar: no render event fired yet
        assertTrue(calendar.getCurrentIntervalStart().isEmpty());
    }

    @Test
    void getCurrentIntervalEnd_emptyBeforeFirstRender() {
        assertTrue(calendar.getCurrentIntervalEnd().isEmpty());
    }

}
