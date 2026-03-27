package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link FullCalendar#applyEntryChangesFromEvent(Entry, ObjectNode, java.util.function.Consumer)}.
 * <p>
 * This method is package-private. Since this test lives in the same package, it can access
 * it directly.
 * <p>
 * The tests here cover the <em>direct mutation path</em> (no signal binding active).
 * The signal-routing path (via {@code signal.modify()}) requires an active Vaadin UI session
 * and is therefore covered by E2E tests ({@code signal-drop-resize.spec.js}).
 */
public class SignalApplyChangesTest {

    private FullCalendar createCalendar() {
        return new FullCalendar();
    }

    private ObjectNode buildJsonForEntry(String id, LocalDateTime start, LocalDateTime end) {
        ObjectNode json = JsonFactory.createObject();
        json.put("id", id);
        json.put("start", JsonUtils.formatClientSideDateTimeString(start));
        json.put("end", JsonUtils.formatClientSideDateTimeString(end));
        json.put("allDay", false);
        return json;
    }

    // ---- Direct mutation (no signal binding) ----

    @Test
    void applyChangesWithNoSignalProviderMutatesEntryDirectly() {
        FullCalendar calendar = createCalendar();
        // No signal binding — signalEntryProvider is null → direct mutation path

        Entry entry = new Entry("e1");
        entry.setStart(LocalDateTime.of(2026, 1, 1, 10, 0));
        entry.setEnd(LocalDateTime.of(2026, 1, 1, 12, 0));

        LocalDateTime newStart = LocalDateTime.of(2026, 3, 1, 9, 0);
        LocalDateTime newEnd = LocalDateTime.of(2026, 3, 1, 11, 0);
        ObjectNode json = buildJsonForEntry("e1", newStart, newEnd);

        calendar.applyEntryChangesFromEvent(entry, json, null);

        assertEquals(newStart, entry.getStart());
        assertEquals(newEnd, entry.getEnd());
    }

    @Test
    void applyChangesWithNullConsumerDoesNotThrow() {
        FullCalendar calendar = createCalendar();

        Entry entry = new Entry("e2");
        entry.setStart(LocalDateTime.of(2026, 1, 1, 10, 0));
        entry.setEnd(LocalDateTime.of(2026, 1, 1, 12, 0));

        ObjectNode json = buildJsonForEntry("e2",
                LocalDateTime.of(2026, 2, 1, 9, 0),
                LocalDateTime.of(2026, 2, 1, 11, 0));

        // Passing null consumer must not throw
        assertDoesNotThrow(() -> calendar.applyEntryChangesFromEvent(entry, json, null));
    }

    @Test
    void applyChangesConsumerReceivesCorrectEntry() {
        FullCalendar calendar = createCalendar();

        Entry entry = new Entry("e3");
        entry.setStart(LocalDateTime.of(2026, 1, 1, 10, 0));
        entry.setEnd(LocalDateTime.of(2026, 1, 1, 12, 0));

        ObjectNode json = buildJsonForEntry("e3",
                LocalDateTime.of(2026, 3, 10, 8, 0),
                LocalDateTime.of(2026, 3, 10, 10, 0));

        AtomicReference<Entry> consumerArg = new AtomicReference<>();
        calendar.applyEntryChangesFromEvent(entry, json, consumerArg::set);

        // Consumer should receive exactly the entry that was passed in
        assertSame(entry, consumerArg.get());
    }

    @Test
    void applyChangesConsumerIsInvoked() {
        FullCalendar calendar = createCalendar();

        Entry entry = new Entry("e4");
        entry.setStart(LocalDateTime.of(2026, 1, 1, 10, 0));
        entry.setEnd(LocalDateTime.of(2026, 1, 1, 12, 0));

        ObjectNode json = buildJsonForEntry("e4",
                LocalDateTime.of(2026, 4, 1, 8, 0),
                LocalDateTime.of(2026, 4, 1, 10, 0));

        AtomicBoolean consumerCalled = new AtomicBoolean(false);
        calendar.applyEntryChangesFromEvent(entry, json, e -> consumerCalled.set(true));

        assertTrue(consumerCalled.get(), "Consumer should have been called");
    }

    @Test
    void applyChangesJsonMutationAndConsumerBothRun() {
        // Both the JSON update and the additional consumer mutation should be applied
        // when there is no signal binding (direct path).
        FullCalendar calendar = createCalendar();

        Entry entry = new Entry("e5");
        entry.setStart(LocalDateTime.of(2026, 1, 1, 10, 0));
        entry.setEnd(LocalDateTime.of(2026, 1, 1, 12, 0));
        entry.setTitle("Original");

        LocalDateTime newStart = LocalDateTime.of(2026, 5, 1, 9, 0);
        LocalDateTime newEnd   = LocalDateTime.of(2026, 5, 1, 11, 0);
        ObjectNode json = buildJsonForEntry("e5", newStart, newEnd);

        // Consumer sets title as an additional mutation
        calendar.applyEntryChangesFromEvent(entry, json, e -> e.setTitle("Modified"));

        assertEquals(newStart, entry.getStart(), "JSON start should be applied");
        assertEquals("Modified", entry.getTitle(), "Consumer title should be applied");
    }
}
