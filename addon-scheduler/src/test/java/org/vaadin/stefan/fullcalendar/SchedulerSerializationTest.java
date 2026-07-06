package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that the scheduler component and its resources survive Java serialization
 * (session passivation / cluster replication). Regression test for issue #239.
 */
@SuppressWarnings("ALL")
public class SchedulerSerializationTest {

    @SuppressWarnings("unchecked")
    private static <T> T roundtrip(T object) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(object);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (T) ois.readObject();
        }
    }

    @Test
    void plainSchedulerIsSerializable() throws Exception {
        assertNotNull(roundtrip(new FullCalendarScheduler()));
    }

    @Test
    void schedulerWithResourcesAndEntriesIsSerializable() throws Exception {
        FullCalendarScheduler calendar = new FullCalendarScheduler();

        Resource parent = new Resource("r-parent", "Room A", "#ff0000");
        Resource child = new Resource("r-child", "Desk 1", "#00ff00");
        parent.addChild(child);
        calendar.addResource(parent);
        calendar.addResource(child);

        ResourceEntry entry = new ResourceEntry("e1");
        entry.setTitle("Booking");
        entry.setStart(LocalDateTime.now());
        entry.setEnd(LocalDateTime.now().plusHours(2));
        entry.assignResource(child);
        calendar.getEntryProvider().asInMemory().addEntry(entry);

        FullCalendarScheduler restored = roundtrip(calendar);
        assertNotNull(restored);
        assertTrue(restored.getResources().size() >= 2);
    }

    @Test
    void resourceWithBusinessHoursIsSerializable() throws Exception {
        Resource resource = new Resource("r1", "Room", "#123456", null, BusinessHours.businessWeek());
        resource.addExtendedProps("capacity", 42);
        assertNotNull(roundtrip(resource));
    }

    @Test
    void schedulerWithComponentResourceAreaColumnIsSerializable() throws Exception {
        FullCalendarScheduler calendar = new FullCalendarScheduler();
        calendar.addResource(new Resource("r1", "Room A", "#ff0000"));

        // component column keeps a SerializableFunction factory + a back-ref to the calendar and,
        // once a resource exists, a created Vaadin component in the hidden container
        calendar.setResourceAreaColumns(
                new ComponentResourceAreaColumn<>("title", "Title",
                        resource -> new com.vaadin.flow.component.html.Span(resource.getTitle())));

        assertNotNull(roundtrip(calendar));
    }

    @Test
    void schedulerWithSchedulerViewIsSerializable() throws Exception {
        FullCalendarScheduler calendar = new FullCalendarScheduler();
        calendar.changeView(SchedulerView.RESOURCE_TIMELINE_DAY);
        assertNotNull(roundtrip(calendar));
    }
}
