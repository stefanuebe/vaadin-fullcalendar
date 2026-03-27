package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.ObjectNode;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link EntryDroppedSchedulerEvent#updateResourcesFromEventResourceDelta(ResourceEntry, ObjectNode)}.
 * Ported from the commented-out tests in ResourceEntryTest.java (elemental.json → Jackson 3).
 */
public class ResourceDeltaTest {

    private FullCalendarScheduler calendar;
    private Resource resource1;
    private Resource resource2;
    private Resource resource3;

    @BeforeEach
    void setUp() {
        calendar = new FullCalendarScheduler();
        resource1 = new Resource("1", "Resource 1", null);
        resource2 = new Resource("2", "Resource 2", null);
        resource3 = new Resource("3", "Resource 3", null);
        calendar.addResources(resource1, resource2, resource3);
    }

    private ResourceEntry createEntryWithResources(Resource... resources) {
        ResourceEntry entry = new ResourceEntry();
        entry.setCalendar(calendar);
        entry.assignResources(resources);
        return entry;
    }

    @Test
    void assignResourceViaNewResourceField() {
        ResourceEntry entry = createEntryWithResources(resource1, resource2);

        ObjectNode json = JsonFactory.createObject();
        json.put("id", entry.getId());
        json.put("newResource", "3");

        EntryDroppedSchedulerEvent.updateResourcesFromEventResourceDelta(entry, json);

        assertEquals(
                new LinkedHashSet<>(Arrays.asList(resource1, resource2, resource3)),
                entry.getOrCreateResources());
    }

    @Test
    void unassignResourceViaOldResourceField() {
        ResourceEntry entry = createEntryWithResources(resource1, resource2);

        ObjectNode json = JsonFactory.createObject();
        json.put("id", entry.getId());
        json.put("oldResource", resource2.getId());

        EntryDroppedSchedulerEvent.updateResourcesFromEventResourceDelta(entry, json);

        assertEquals(Collections.singleton(resource1), entry.getOrCreateResources());
    }

    @Test
    void reassignResourceViaBothFields() {
        ResourceEntry entry = createEntryWithResources(resource1, resource2);

        ObjectNode json = JsonFactory.createObject();
        json.put("id", entry.getId());
        json.put("oldResource", "2");
        json.put("newResource", "3");

        EntryDroppedSchedulerEvent.updateResourcesFromEventResourceDelta(entry, json);

        assertEquals(
                new LinkedHashSet<>(Arrays.asList(resource1, resource3)),
                entry.getOrCreateResources());
    }

    @Test
    void reassignWithoutCalendar_doesNotThrow() {
        ResourceEntry entry = new ResourceEntry();
        // No setCalendar() — getCalendar() returns empty, ifPresent guard should prevent NPE
        entry.addResources(resource1);

        ObjectNode json = JsonFactory.createObject();
        json.put("id", entry.getId());
        json.put("oldResource", "1");
        json.put("newResource", "2");

        assertDoesNotThrow(() ->
                EntryDroppedSchedulerEvent.updateResourcesFromEventResourceDelta(entry, json));
        // Resources unchanged because no calendar means no resource lookup
        assertTrue(entry.getResourcesOrEmpty().contains(resource1));
    }

    @Test
    void noResourceFieldsDoesNotChangeResources() {
        ResourceEntry entry = createEntryWithResources(resource1, resource2);

        ObjectNode json = JsonFactory.createObject();
        json.put("id", entry.getId());

        EntryDroppedSchedulerEvent.updateResourcesFromEventResourceDelta(entry, json);

        assertEquals(
                new LinkedHashSet<>(Arrays.asList(resource1, resource2)),
                entry.getOrCreateResources());
    }
}
