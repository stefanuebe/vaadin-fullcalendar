package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that ResourceEntry.copy(), copyAsType(), and updateFromJson() work correctly,
 * especially that no NPE occurs due to type mismatches between
 * Entry (Boolean) and ResourceEntry (boolean) fields during reflection-based copy.
 * <p>
 * These tests cover the same code paths as {@code EntryDataEvent.applyChangesOnEntry()}
 * and {@code EntryDataEvent.createCopyBasedOnChanges()}.
 */
public class ResourceEntryCopyTest {

    @Test
    void copyAsType_withDefaults_shouldNotThrow() {
        ResourceEntry entry = new ResourceEntry("test-1");
        entry.setTitle("Test");
        entry.setStart(LocalDateTime.of(2026, 3, 21, 10, 0));
        entry.setEnd(LocalDateTime.of(2026, 3, 21, 12, 0));

        ResourceEntry copy = entry.copyAsType(ResourceEntry.class);

        assertEquals("test-1", copy.getId());
        assertEquals("Test", copy.getTitle());
        assertEquals(entry.getStart(), copy.getStart());
        assertEquals(entry.getEnd(), copy.getEnd());
        assertEquals(entry.isResourceEditable(), copy.isResourceEditable());
    }

    @Test
    void copyAsType_withAllBooleanFieldsSet_shouldCopyCorrectly() {
        ResourceEntry entry = new ResourceEntry("test-2");
        entry.setEditable(true);
        entry.setStartEditable(true);
        entry.setDurationEditable(false);
        entry.setResourceEditable(false);
        entry.setOverlap(true);
        entry.setInteractive(false);
        entry.setAllDay(true);

        ResourceEntry copy = entry.copyAsType(ResourceEntry.class);

        assertTrue(copy.isEditable());
        assertEquals(true, copy.getStartEditable());
        assertEquals(false, copy.getDurationEditable());
        assertFalse(copy.isResourceEditable());
        assertEquals(true, copy.getOverlap());
        assertEquals(false, copy.getInteractive());
        assertTrue(copy.isAllDay());
    }

    @Test
    void copyAsType_withNullableBooleanFieldsNull_shouldNotThrow() {
        ResourceEntry entry = new ResourceEntry("test-3");
        // Leave nullable Boolean fields at their default (null)
        entry.setStartEditable(null);
        entry.setDurationEditable(null);
        entry.setOverlap(null);
        entry.setInteractive(null);

        ResourceEntry copy = entry.copyAsType(ResourceEntry.class);

        assertNull(copy.getStartEditable());
        assertNull(copy.getDurationEditable());
        assertNull(copy.getOverlap());
        assertNull(copy.getInteractive());
    }

    @Test
    void copy_shouldNotThrow() {
        ResourceEntry entry = new ResourceEntry("test-4");
        entry.setTitle("Copy Test");
        entry.setStart(LocalDateTime.of(2026, 3, 4, 0, 0));
        entry.setEnd(LocalDateTime.of(2026, 3, 5, 0, 0));
        entry.setAllDay(true);

        ResourceEntry copy = entry.copy();

        assertEquals("test-4", copy.getId());
        assertEquals("Copy Test", copy.getTitle());
    }

    @Test
    void copyAsType_withResources_shouldCopyResources() {
        ResourceEntry entry = new ResourceEntry("test-5");
        Resource r1 = new Resource("r1", "Room 1", null);
        Resource r2 = new Resource("r2", "Room 2", null);
        entry.addResources(r1, r2);

        ResourceEntry copy = entry.copyAsType(ResourceEntry.class);

        assertEquals(2, copy.getResourcesOrEmpty().size());
        // Should be a different set instance (deep copy)
        assertNotSame(entry.getResources(), copy.getResources());
    }

    // --- updateFromJson tests (same code path as EntryDataEvent.applyChangesOnEntry) ---

    @Test
    void updateFromJson_shouldApplyChangesToResourceEntry() {
        ResourceEntry entry = new ResourceEntry("e1");
        entry.setTitle("Original");
        entry.setStart(LocalDateTime.of(2026, 3, 1, 10, 0));
        entry.setEnd(LocalDateTime.of(2026, 3, 1, 12, 0));
        entry.setAllDay(false);

        ObjectNode json = JsonFactory.createObject();
        json.put("id", "e1");
        json.put("start", JsonUtils.formatClientSideDateTimeString(LocalDateTime.of(2026, 3, 2, 9, 0)));
        json.put("end", JsonUtils.formatClientSideDateTimeString(LocalDateTime.of(2026, 3, 2, 11, 0)));
        json.put("allDay", true);

        entry.updateFromJson(json);

        assertEquals(LocalDateTime.of(2026, 3, 2, 9, 0), entry.getStart());
        assertEquals(LocalDateTime.of(2026, 3, 2, 11, 0), entry.getEnd());
        assertTrue(entry.isAllDay());
        // title is not @JsonUpdateAllowed, should remain unchanged
        assertEquals("Original", entry.getTitle());
    }

    @Test
    void updateFromJson_shouldNotThrowOnResourceEntry() {
        ResourceEntry entry = new ResourceEntry("e2");
        entry.setResourceEditable(false);

        ObjectNode json = JsonFactory.createObject();
        json.put("id", "e2");
        json.put("allDay", false);

        assertDoesNotThrow(() -> entry.updateFromJson(json));
    }

    // --- copy + updateFromJson (same code path as EntryDataEvent.createCopyBasedOnChanges) ---

    @Test
    void copyThenUpdateFromJson_shouldNotThrow() {
        ResourceEntry entry = new ResourceEntry("e3");
        entry.setTitle("Meeting");
        entry.setStart(LocalDateTime.of(2026, 3, 10, 14, 0));
        entry.setEnd(LocalDateTime.of(2026, 3, 10, 15, 0));
        entry.setAllDay(false);
        entry.setResourceEditable(true);

        Resource room = new Resource("r1", "Room A", null);
        entry.addResources(room);

        // simulate createCopyBasedOnChanges
        ResourceEntry copy = entry.copy();

        ObjectNode json = JsonFactory.createObject();
        json.put("id", "e3");
        json.put("start", JsonUtils.formatClientSideDateTimeString(LocalDateTime.of(2026, 3, 11, 14, 0)));
        json.put("end", JsonUtils.formatClientSideDateTimeString(LocalDateTime.of(2026, 3, 11, 16, 0)));
        json.put("allDay", true);

        copy.updateFromJson(json);

        // copy should have updated values
        assertEquals(LocalDateTime.of(2026, 3, 11, 14, 0), copy.getStart());
        assertEquals(LocalDateTime.of(2026, 3, 11, 16, 0), copy.getEnd());
        assertTrue(copy.isAllDay());

        // original should be unchanged
        assertEquals(LocalDateTime.of(2026, 3, 10, 14, 0), entry.getStart());
        assertFalse(entry.isAllDay());
    }
}
