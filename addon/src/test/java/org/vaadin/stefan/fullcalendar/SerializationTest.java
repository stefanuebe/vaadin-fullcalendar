package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.dataprovider.CallbackEntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that the calendar and its collaborators survive Java serialization.
 * Vaadin views are serialized by the servlet container (session passivation /
 * cluster replication), so every field of a {@link com.vaadin.flow.component.Component}
 * must be serializable or transient. Regression test for issue #239.
 */
@SuppressWarnings("ALL")
public class SerializationTest {

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
    void plainCalendarIsSerializable() throws Exception {
        FullCalendar calendar = new FullCalendar();
        assertNotNull(roundtrip(calendar));
    }

    @Test
    void calendarWithInMemoryEntriesIsSerializable() throws Exception {
        FullCalendar calendar = new FullCalendar();
        calendar.setTimezone(Timezone.getSystem());
        calendar.setLocale(java.util.Locale.ENGLISH);

        InMemoryEntryProvider<Entry> provider = calendar.getEntryProvider().asInMemory();
        Entry entry = new Entry();
        entry.setTitle("Meeting");
        entry.setStart(LocalDateTime.now());
        entry.setEnd(LocalDateTime.now().plusHours(1));
        entry.setColor("#ff0000");
        provider.addEntry(entry);

        Entry recurring = new Entry();
        recurring.setRecurringDaysOfWeek(java.time.DayOfWeek.MONDAY, java.time.DayOfWeek.FRIDAY);
        recurring.setRecurringStartDate(LocalDate.now());
        provider.addEntry(recurring);

        FullCalendar restored = roundtrip(calendar);
        assertNotNull(restored);
        assertTrue(restored.getEntryProvider().asInMemory().fetchAll().count() >= 2);
    }

    @Test
    void calendarWithCallbackProviderIsSerializable() throws Exception {
        FullCalendar calendar = new FullCalendar();
        CallbackEntryProvider<Entry> provider = org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider.fromCallbacks(
                query -> List.<Entry>of().stream(),
                id -> null);
        calendar.setEntryProvider(provider);
        assertNotNull(roundtrip(calendar));
    }

    @Test
    void calendarWithOptionsIsSerializable() throws Exception {
        FullCalendar calendar = new FullCalendar();
        calendar.setOption(FullCalendar.Option.EDITABLE, true);
        calendar.setMaxEntriesPerDay(5);
        calendar.changeView(CalendarViewImpl.TIME_GRID_WEEK);
        assertNotNull(roundtrip(calendar));
    }

    @Test
    void entryIsSerializable() throws Exception {
        Entry entry = new Entry();
        entry.setTitle("Standalone");
        entry.setStart(LocalDateTime.now());
        entry.setCustomProperty("foo", "bar");
        assertNotNull(roundtrip(entry));
    }
}
