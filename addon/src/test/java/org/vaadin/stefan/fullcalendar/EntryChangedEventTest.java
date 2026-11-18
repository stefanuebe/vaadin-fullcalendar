package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.node.ObjectNode;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntryChangedEventTest {

    private final FullCalendar calendar = mock(FullCalendar.class);
    private final Entry original = new Entry("test-1");

    private EntryDroppedEvent dropMakingTheEntryAllDay() {
        original.setAllDay(false);
        when(calendar.getCachedEntryFromFetch("test-1")).thenReturn(Optional.of(original));

        ObjectNode changes = JsonFactory.createObject();
        changes.put("id", "test-1");
        changes.put("allDay", true);

        ObjectNode delta = JsonFactory.createObject();
        delta.put("days", 0);
        delta.put("milliseconds", 0);
        return new EntryDroppedEvent(calendar, false, changes, delta);
    }

    @Test
    void getChangesAsEntry_returnsTheChangedCopy() {
        Entry changed = dropMakingTheEntryAllDay().getChangesAsEntry();

        assertTrue(changed.isAllDay());
        assertFalse(original.isAllDay(), "original entry must remain unchanged");
    }

    @Test
    void applyChangesOnEntry_writesTheChangeAndMarksItApplied() {
        EntryDroppedEvent event = dropMakingTheEntryAllDay();
        assertFalse(event.isChangesApplied());

        assertSame(original, event.applyChangesOnEntry());
        assertTrue(original.isAllDay());
        assertTrue(event.isChangesApplied());
    }
}
