package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CalendarItemChanges}.
 */
class CalendarItemChangesTest {

    static final LocalDateTime START = LocalDateTime.of(2024, 3, 15, 14, 30);
    static final LocalDateTime END = LocalDateTime.of(2024, 3, 15, 15, 30);

    @Test
    @DisplayName("getChangedStart returns parsed LocalDateTime from JSON")
    void getChangedStart() {
        ObjectNode json = JsonFactory.createObject();
        json.put("start", JsonUtils.formatClientSideDateTimeString(START));

        CalendarItemChanges changes = new CalendarItemChanges(json);

        assertTrue(changes.getChangedStart().isPresent());
        assertEquals(START, changes.getChangedStart().get());
    }

    @Test
    @DisplayName("getChangedEnd returns parsed LocalDateTime from JSON")
    void getChangedEnd() {
        ObjectNode json = JsonFactory.createObject();
        json.put("end", JsonUtils.formatClientSideDateTimeString(END));

        CalendarItemChanges changes = new CalendarItemChanges(json);

        assertTrue(changes.getChangedEnd().isPresent());
        assertEquals(END, changes.getChangedEnd().get());
    }

    @Test
    @DisplayName("getChangedAllDay returns boolean from JSON")
    void getChangedAllDay() {
        ObjectNode json = JsonFactory.createObject();
        json.put("allDay", true);

        CalendarItemChanges changes = new CalendarItemChanges(json);

        assertTrue(changes.getChangedAllDay().isPresent());
        assertTrue(changes.getChangedAllDay().get());
    }

    @Test
    @DisplayName("missing properties return empty Optional")
    void missingProperties() {
        ObjectNode json = JsonFactory.createObject();

        CalendarItemChanges changes = new CalendarItemChanges(json);

        assertTrue(changes.getChangedStart().isEmpty());
        assertTrue(changes.getChangedEnd().isEmpty());
        assertTrue(changes.getChangedAllDay().isEmpty());
    }

    @Test
    @DisplayName("getRawJson returns original ObjectNode")
    void getRawJson() {
        ObjectNode json = JsonFactory.createObject();
        json.put("customProp", "customValue");

        CalendarItemChanges changes = new CalendarItemChanges(json);

        assertSame(json, changes.getRawJson());
        assertEquals("customValue", changes.getRawJson().get("customProp").asString());
    }

    @Test
    @DisplayName("constructor rejects null")
    void rejectsNull() {
        assertThrows(NullPointerException.class, () -> new CalendarItemChanges(null));
    }
}
