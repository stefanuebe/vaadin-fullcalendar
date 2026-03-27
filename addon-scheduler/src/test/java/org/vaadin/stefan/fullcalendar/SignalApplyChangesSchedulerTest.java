package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that {@code applyEntryChangesFromEvent} correctly invokes the Consumer
 * for additional mutations (e.g., resource delta), and that
 * {@link EntryDroppedSchedulerEvent#applyChangesOnEntry()} routes resource
 * changes through the Consumer path.
 * <p>
 * These are unit tests (no UI context). Full signal integration (verifying
 * single {@code modify()} call) is covered by E2E tests
 * ({@code signal-drop-resize.spec.js}).
 */
public class SignalApplyChangesSchedulerTest {

    private FullCalendarScheduler calendar;
    private Resource resourceA;
    private Resource resourceB;

    @BeforeEach
    void setUp() {
        calendar = new FullCalendarScheduler();
        resourceA = new Resource("rA", "Room A", null);
        resourceB = new Resource("rB", "Room B", null);
        calendar.addResources(resourceA, resourceB);
    }

    @Test
    void applyEntryChangesFromEvent_consumerIsInvoked() {
        ResourceEntry entry = new ResourceEntry("e1");
        entry.setCalendar(calendar);
        entry.setStart(LocalDateTime.of(2026, 3, 1, 10, 0));
        entry.setEnd(LocalDateTime.of(2026, 3, 1, 12, 0));
        entry.addResources(resourceA);

        ObjectNode json = JsonFactory.createObject();
        json.put("id", "e1");
        json.put("start", JsonUtils.formatClientSideDateTimeString(LocalDateTime.of(2026, 3, 2, 9, 0)));
        json.put("end", JsonUtils.formatClientSideDateTimeString(LocalDateTime.of(2026, 3, 2, 11, 0)));
        json.put("allDay", false);

        AtomicReference<Entry> consumerReceived = new AtomicReference<>();
        calendar.applyEntryChangesFromEvent(entry, json, consumerReceived::set);

        // Consumer was called with the entry
        assertSame(entry, consumerReceived.get());
        // JSON changes were also applied
        assertEquals(LocalDateTime.of(2026, 3, 2, 9, 0), entry.getStart());
    }

    @Test
    void applyEntryChangesFromEvent_consumerNull_worksLikeOriginal() {
        ResourceEntry entry = new ResourceEntry("e2");
        entry.setCalendar(calendar);
        entry.setStart(LocalDateTime.of(2026, 3, 1, 10, 0));
        entry.setEnd(LocalDateTime.of(2026, 3, 1, 12, 0));

        ObjectNode json = JsonFactory.createObject();
        json.put("id", "e2");
        json.put("start", JsonUtils.formatClientSideDateTimeString(LocalDateTime.of(2026, 3, 3, 8, 0)));
        json.put("end", JsonUtils.formatClientSideDateTimeString(LocalDateTime.of(2026, 3, 3, 10, 0)));
        json.put("allDay", false);

        // No consumer — should not throw
        assertDoesNotThrow(() -> calendar.applyEntryChangesFromEvent(entry, json, null));
        assertEquals(LocalDateTime.of(2026, 3, 3, 8, 0), entry.getStart());
    }

    @Test
    void applyEntryChangesFromEvent_resourceDeltaViaConsumer() {
        ResourceEntry entry = new ResourceEntry("e3");
        entry.setCalendar(calendar);
        entry.setStart(LocalDateTime.of(2026, 3, 1, 10, 0));
        entry.setEnd(LocalDateTime.of(2026, 3, 1, 12, 0));
        entry.addResources(resourceA);

        ObjectNode json = JsonFactory.createObject();
        json.put("id", "e3");
        json.put("start", JsonUtils.formatClientSideDateTimeString(LocalDateTime.of(2026, 3, 1, 10, 0)));
        json.put("end", JsonUtils.formatClientSideDateTimeString(LocalDateTime.of(2026, 3, 1, 12, 0)));
        json.put("allDay", false);
        json.put("oldResource", "rA");
        json.put("newResource", "rB");

        // Simulate what EntryDroppedSchedulerEvent.applyChangesOnEntry() does
        calendar.applyEntryChangesFromEvent(entry, json,
                e -> EntryDroppedSchedulerEvent.updateResourcesFromEventResourceDelta(
                        (ResourceEntry) e, json));

        // Resource should have changed from A to B
        assertFalse(entry.getResourcesOrEmpty().contains(resourceA));
        assertTrue(entry.getResourcesOrEmpty().contains(resourceB));
    }

    @Test
    void applyEntryChangesFromEvent_noResourceChange_resourcesUnchanged() {
        ResourceEntry entry = new ResourceEntry("e4");
        entry.setCalendar(calendar);
        entry.setStart(LocalDateTime.of(2026, 3, 1, 10, 0));
        entry.setEnd(LocalDateTime.of(2026, 3, 1, 12, 0));
        entry.addResources(resourceA);

        ObjectNode json = JsonFactory.createObject();
        json.put("id", "e4");
        json.put("start", JsonUtils.formatClientSideDateTimeString(LocalDateTime.of(2026, 3, 2, 10, 0)));
        json.put("end", JsonUtils.formatClientSideDateTimeString(LocalDateTime.of(2026, 3, 2, 12, 0)));
        json.put("allDay", false);
        // No oldResource/newResource fields

        calendar.applyEntryChangesFromEvent(entry, json,
                e -> EntryDroppedSchedulerEvent.updateResourcesFromEventResourceDelta(
                        (ResourceEntry) e, json));

        // Time changed
        assertEquals(LocalDateTime.of(2026, 3, 2, 10, 0), entry.getStart());
        // Resource unchanged
        assertEquals(1, entry.getResourcesOrEmpty().size());
        assertTrue(entry.getResourcesOrEmpty().contains(resourceA));
    }
}
