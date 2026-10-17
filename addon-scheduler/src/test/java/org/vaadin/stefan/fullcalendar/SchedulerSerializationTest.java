package org.vaadin.stefan.fullcalendar;

import org.vaadin.stefan.fullcalendar.option.ComponentResourceColumn;
import org.vaadin.stefan.fullcalendar.option.BusinessHours;
import org.vaadin.stefan.fullcalendar.option.JsCallback;
import com.vaadin.flow.component.html.Span;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
        entry.addResources(child);
        calendar.getEntryProvider().asInMemory().addEntry(entry);

        FullCalendarScheduler restored = roundtrip(calendar);
        assertNotNull(restored);
        assertTrue(restored.getResources().size() >= 2);
    }

    @Test
    void resourceWithBusinessHoursIsSerializable() throws Exception {
        Resource resource = new Resource("r1", "Room", "#123456", null, BusinessHours.businessWeek());
        resource.setExtendedProp("capacity", 42);
        assertNotNull(roundtrip(resource));
    }

    @Test
    void schedulerWithComponentResourceColumnIsSerializable() throws Exception {
        FullCalendarScheduler calendar = new FullCalendarScheduler();
        calendar.addResource(new Resource("r1", "Room A", "#ff0000"));

        // component column keeps a SerializableFunction factory + a back-ref to the calendar and,
        // once a resource exists, a created Vaadin component in the hidden container
        calendar.options().setResourceColumns(List.of(
                new ComponentResourceColumn<>("title", "Title",
                        resource -> new Span(resource.getTitle()))));

        assertNotNull(roundtrip(calendar));
    }

    @Test
    void schedulerWithSchedulerViewIsSerializable() throws Exception {
        FullCalendarScheduler calendar = new FullCalendarScheduler();
        calendar.changeView(SchedulerView.RESOURCE_TIMELINE_DAY);
        assertNotNull(roundtrip(calendar));
    }

    @Test
    void schedulerWithOptionsAndViewOptionsIsSerializable() throws Exception {
        FullCalendarScheduler calendar = new FullCalendarScheduler();
        calendar.options().setResourceGroupField("building");
        calendar.options().setWeekends(false);
        FullCalendarSchedulerViewOptions week = calendar.options().view(SchedulerView.RESOURCE_TIMELINE_WEEK);
        week.setResourcesInitiallyExpanded(false);
        week.setWeekends(true);
        week.setDayCellDidMount(JsCallback.of("info => {}"));

        FullCalendarScheduler restored = roundtrip(calendar);
        assertEquals("building", restored.options().getResourceGroupField());
        assertFalse(restored.options().getWeekends());
        assertFalse(restored.options().view(SchedulerView.RESOURCE_TIMELINE_WEEK).getResourcesInitiallyExpanded());
        assertTrue(restored.options().view(SchedulerView.RESOURCE_TIMELINE_WEEK).getWeekends());
        assertEquals(JsCallback.of("info => {}"),
                restored.options().view(SchedulerView.RESOURCE_TIMELINE_WEEK).getDayCellDidMount());

        FullCalendarSchedulerViewOptions restoredWeek = roundtrip(week);
        assertFalse(restoredWeek.getResourcesInitiallyExpanded());
    }
}
