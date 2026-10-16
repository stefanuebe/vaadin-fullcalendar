package org.vaadin.stefan.fullcalendar;

import org.vaadin.stefan.fullcalendar.option.ClassName;
import org.vaadin.stefan.fullcalendar.option.ConstraintValue;
import org.vaadin.stefan.fullcalendar.option.JsCallback;
import org.vaadin.stefan.fullcalendar.option.Timezone;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.dataprovider.CallbackEntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    void calendarWithRawOptionsIsSerializable() throws Exception {
        FullCalendar calendar = new FullCalendar();
        calendar.options().setRawOption("weekends", false);
        calendar.options().setRawOption("dayCellClass", JsCallback.of("() => 'x'"));
        calendar.options().view(CalendarViewImpl.DAY_GRID_MONTH).setRawOption("dayCount", 3);

        FullCalendar copy = roundtrip(calendar);
        assertTrue(copy.options().getRawOption("dayCellClass").isPresent());
        assertEquals(3, copy.options().view(CalendarViewImpl.DAY_GRID_MONTH).getRawOption("dayCount").orElseThrow().asInt());
    }

    @Test
    void calendarWithTypedOptionsIsSerializable() throws Exception {
        FullCalendar calendar = new FullCalendar();
        calendar.options().setFirstDay(DayOfWeek.MONDAY);
        calendar.options().setInitialDate(LocalDate.of(2025, 3, 3));
        calendar.options().setDayCellClass(JsCallback.of("() => 'x'"));

        FullCalendar copy = roundtrip(calendar);
        assertEquals(DayOfWeek.MONDAY, copy.options().getFirstDay());
        assertEquals(Boolean.TRUE, copy.options().getEditable());
        assertEquals(JsCallback.of("() => 'x'"), copy.options().getDayCellClass());

        // the options object of the copy changes the copy, not the original
        copy.options().setFirstDay(DayOfWeek.TUESDAY);
        assertEquals(DayOfWeek.TUESDAY, copy.options().getFirstDay());
        assertEquals(DayOfWeek.MONDAY, calendar.options().getFirstDay());
    }

    @Test
    void calendarWithViewOptionsAndAKeptViewReferenceIsSerializable() throws Exception {
        FullCalendar calendar = new FullCalendar();
        FullCalendarViewOptions week = calendar.options().view(CalendarViewImpl.TIME_GRID_WEEK);
        week.setWeekends(false);

        // serialized together, as in a session, so the copied reference points to the copied calendar
        List<?> copy = roundtrip(new ArrayList<>(List.of(calendar, week)));
        FullCalendar calendarCopy = (FullCalendar) copy.get(0);
        FullCalendarViewOptions weekCopy = (FullCalendarViewOptions) copy.get(1);

        assertEquals(Boolean.FALSE, calendarCopy.options().view(CalendarViewImpl.TIME_GRID_WEEK).getWeekends());
        weekCopy.setDayCellClass("week-day");
        assertEquals(ClassName.of("week-day"),
                calendarCopy.options().view(CalendarViewImpl.TIME_GRID_WEEK).getDayCellClass());
        assertNull(week.getDayCellClass());
    }

    @Test
    void calendarWithInMemoryEntriesIsSerializable() throws Exception {
        FullCalendar calendar = new FullCalendar();
        calendar.setOption(Option.TIMEZONE, Timezone.getSystem());
        calendar.setOption(Option.LOCALE, Locale.ENGLISH);

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
        calendar.setOption(Option.EDITABLE, true);
        calendar.setMaxEntriesPerDay(5);
        calendar.changeView(CalendarViewImpl.TIME_GRID_WEEK);
        assertNotNull(roundtrip(calendar));
    }

    @Test
    void calendarWithARuleWindowConstraintIsSerializable() throws Exception {
        FullCalendar calendar = new FullCalendar();
        calendar.options().setEntryConstraint(ConstraintValue.Window.ofRRule(
                RRule.weekly().dtstart(LocalDateTime.of(2026, 1, 5, 9, 0)).excludeDates(LocalDate.of(2026, 1, 12)),
                Duration.ofHours(8)));
        FullCalendar restored = roundtrip(calendar);
        assertEquals(calendar.options().getEntryConstraint(), restored.options().getEntryConstraint());
    }

    @Test
    void entryIsSerializable() throws Exception {
        Entry entry = new Entry();
        entry.setTitle("Standalone");
        entry.setStart(LocalDateTime.now());
        entry.setExtendedProp("foo", "bar");
        assertNotNull(roundtrip(entry));
    }

    @Test
    void calendarWithRemoteEntrySourcesIsSerializable() throws Exception {
        FullCalendar calendar = new FullCalendar();
        calendar.addRemoteEntrySource(new JsonFeedEntrySource("/api/events").withEditable(true));
        calendar.addRemoteEntrySource(new ICalendarEntrySource("/feed.ics"));
        calendar.addRemoteEntrySource(new GoogleCalendarEntrySource("cal-id").withApiKey("api-key"));
        assertNotNull(roundtrip(calendar));
    }

    @Test
    void calendarWithDraggableIsSerializable() throws Exception {
        FullCalendar calendar = new FullCalendar();
        calendar.addDraggable(new Draggable(new com.vaadin.flow.component.html.Span("drag me")));
        assertNotNull(roundtrip(calendar));
    }
}
