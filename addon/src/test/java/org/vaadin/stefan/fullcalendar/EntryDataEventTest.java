package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.node.ObjectNode;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntryDataEventTest {

    @Test
    void getChangesAsEntry_appliesChangesOnACopy() {
        // EntryClickedEvent resolves the entry from the calendar's fetch cache
        FullCalendar calendar = mock(FullCalendar.class);
        Entry original = new Entry("test-1");
        original.setTitle("Original");
        original.setAllDay(false);
        when(calendar.getCachedEntryFromFetch("test-1")).thenReturn(Optional.of(original));

        // updateFromJson only applies to fields tagged @JsonUpdateAllowed (allDay is one of them)
        ObjectNode changes = JsonFactory.createObject();
        changes.put("id", "test-1");
        changes.put("allDay", true);

        EntryClickedEvent event = new EntryClickedEvent(calendar, false, changes);
        Entry changed = event.getChangesAsEntry();

        assertEquals("test-1", changed.getId());
        assertTrue(changed.isAllDay());
        assertNotSame(original, changed);
        assertFalse(original.isAllDay(), "original entry must remain unchanged");
    }
}
