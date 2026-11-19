package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.option.ConstraintValue;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DropEventTest {

    private final FullCalendar calendar = new FullCalendar();

    @Test
    void timedDropWithoutEntryData_createsEntryAtTheTimeOfTheSlot() {
        DropEvent event = new DropEvent(calendar, true, "2025-03-12T10:00:00Z", false, null, null);

        assertEquals(LocalDateTime.of(2025, 3, 12, 10, 0), event.getDateTime());
        assertEquals(LocalDate.of(2025, 3, 12), event.getDate());
        assertEquals(LocalDateTime.of(2025, 3, 12, 10, 0), event.getCreatedEntry().getStart());
        assertEquals(LocalDateTime.of(2025, 3, 12, 11, 0), event.getCreatedEntry().getEnd());
    }

    @Test
    void dropWithEntryData_createsEntryFromThatData() {
        DropEvent event = new DropEvent(calendar, true, "2025-03-12T10:00:00Z", false,
                "{\"title\": \"Task A\", \"start\": \"2025-03-12T10:00:00Z\", \"end\": \"2025-03-12T11:30:00Z\"}", null);

        assertEquals("Task A", event.getCreatedEntry().getTitle());
        assertEquals(LocalDateTime.of(2025, 3, 12, 10, 0), event.getCreatedEntry().getStart());
        assertEquals(LocalDateTime.of(2025, 3, 12, 11, 30), event.getCreatedEntry().getEnd());
    }

    @Test
    void dropWithConstraintInEntryData_readsItOrSkipsItWhenNotRepresentable() {
        DropEvent withGroup = new DropEvent(calendar, true, "2025-03-12T10:00:00Z", false,
                "{\"title\": \"A\", \"constraint\": \"team-a\"}", null);
        assertEquals(ConstraintValue.ofGroupId("team-a"), withGroup.getCreatedEntry().getConstraint());

        // windows with different resources are valid for the library, but a ConstraintValue cannot hold them
        DropEvent withMixedWindows = new DropEvent(calendar, true, "2025-03-12T10:00:00Z", false,
                "{\"title\": \"B\", \"constraint\": [{\"start\": \"2025-03-12\", \"resourceIds\": [\"a\"]},"
                        + " {\"start\": \"2025-03-13\", \"resourceIds\": [\"b\"]}]}", null);
        assertEquals("B", withMixedWindows.getCreatedEntry().getTitle());
        assertNull(withMixedWindows.getCreatedEntry().getConstraint());
    }

    @Test
    void allDayDropWithoutEntryData_createsAllDayEntryForTheDay() {
        DropEvent event = new DropEvent(calendar, true, "2025-03-12", true, null, null);

        assertEquals(LocalDateTime.of(2025, 3, 12, 0, 0), event.getDateTime());
        assertTrue(event.getCreatedEntry().isAllDay());
        assertEquals(LocalDateTime.of(2025, 3, 12, 0, 0), event.getCreatedEntry().getStart());
        assertEquals(LocalDateTime.of(2025, 3, 13, 0, 0), event.getCreatedEntry().getEnd());
    }
}
