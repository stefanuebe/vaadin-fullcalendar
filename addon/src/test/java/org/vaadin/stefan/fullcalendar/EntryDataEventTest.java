package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.ObjectNode;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EntryDataEventTest {

    @Test
    void getChangesAsEntry_keepsTheRRule() {
        // getChangesAsEntry is shared by all entry data events, drop and resize included
        FullCalendar calendar = mock(FullCalendar.class);
        Entry original = new Entry("test-1");
        original.setRRule(RRule.weekly());
        when(calendar.getCachedEntryFromFetch("test-1")).thenReturn(Optional.of(original));

        ObjectNode changes = JsonFactory.createObject();
        changes.put("id", "test-1");
        changes.put("allDay", true);

        Entry changed = new EntryClickedEvent(calendar, false, changes).getChangesAsEntry();

        assertSame(original.getRRule(), changed.getRRule());
    }
}
