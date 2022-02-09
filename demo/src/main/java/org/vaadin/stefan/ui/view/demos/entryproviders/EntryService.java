package org.vaadin.stefan.ui.view.demos.entryproviders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryQuery;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Stefan Uebe
 */
public class EntryService {

    private final Map<Integer, EntryData> database = new HashMap<>();

    public EntryService() {
    }

    public static EntryService createInstance() {
        return new EntryService();
    }

    /**
     * Deletes all current values and (re)fills the database with new entries in a range of -3 years to +3 years
     * (related to today). The entry ids will always start with 0.
     */
    public void fillDatabaseWithRandomData() {
        database.clear();
        LocalDate date = LocalDate.now().minusYears(3).withDayOfYear(1);
        LocalDate end = LocalDate.now().plusYears(3);

        Random random = new Random();

        while (date.isBefore(end)) {
            int maxDays = date.lengthOfMonth();
            for (int i = 0; i < 8; i++) {
                LocalDate start = date.withDayOfMonth(random.nextInt(maxDays) + 1);
                if (random.nextBoolean()) {
                    addDay("Entry " + database.size(), start);
                } else {
                    addTimed("Entry " + database.size(), start.atTime(10, 0));
                }
            }

            date = date.plusMonths(1);
        }
    }

    /**
     * Deletes all current values and (re)fills the database with a few entries for this month.
     */
    public void fillDatabaseWithSimpleData() {
        database.clear();
        LocalDate now = LocalDate.now();
        addTimed("Grocery Store", now.withDayOfMonth(7).atTime(17, 30), 45, "blue");
        addTimed("Dentist", now.withDayOfMonth(20).atTime(11, 30), 60, "red");
        addTimed("Cinema", now.withDayOfMonth(10).atTime(20, 30), 140, "green");
        addDay("Short trip", now.withDayOfMonth(17), 2, "green");
        addDay("John's Birthday", now.withDayOfMonth(23), 1, "violet");
        addDay("This special holiday", now.withDayOfMonth(4), 1, "gray");
    }

    private EntryData createTimed(int id, String title, LocalDateTime start, int minutes, String color) {
        EntryData entry = new EntryData(id);
        entry.setTitle(title);
        entry.setStart(start);
        entry.setEnd(entry.getStart().plusMinutes(1));
        entry.setAllDay(false);
        entry.setColor(color);
        return entry;
    }

    private EntryData createDay(int id, String title, LocalDate start, int days, String color) {
        EntryData entry = new EntryData(id);
        entry.setTitle(title);
        entry.setStart(start.atStartOfDay());
        entry.setEnd(entry.getStart().plusDays(1));
        entry.setAllDay(true);
        entry.setColor(color);
        return entry;
    }

    public void addDay(String title, LocalDate start, int days, String color) {
        EntryData data = createDay(database.size(), title, start, days, color);
        database.put(data.getId(), data);
    }

    public void addDay(String title, LocalDate start) {
        addDay(title, start, 1, null);
    }

    public void addTimed(String title, LocalDateTime start, int minutes, String color) {
        EntryData data = createTimed(database.size(), title, start, minutes, color);
        database.put(data.getId(), data);
    }

    public void addTimed(String title, LocalDateTime start) {
        addTimed(title, start, 60, null);
    }

    public Optional<Entry> getEntry(String id) {
        return Optional.ofNullable(database.get(id)).map(this::toEntry);
    }

    public Entry getEntryOrNull(String id) {
        return getEntry(id).orElse(null);
    }

    public int count() {
        return database.size();
    }

    public Stream<Entry> streamEntries() {
        return database.values().stream().map(this::toEntry);
    }

    public Stream<Entry> streamEntries(EntryQuery query) {
        Stream<EntryData> stream = database.values().stream();
        return applyFilter(stream, query).map(this::toEntry)/*.peek(e -> e.setCalendar(query.getSource()))*/;
    }

    public Stream<EntryData> applyFilter(Stream<EntryData> stream, EntryQuery query) {
        LocalDateTime start = query.getStart();
        LocalDateTime end = query.getEnd();

        if (start != null) {
            stream = stream.filter(e -> e.getEnd() != null && e.getEnd().isAfter(start));
        }

        if (end != null) {
            stream = stream.filter(e -> e.getStart() != null && e.getStart().isBefore(end));
        }

        return stream;
    }

    public Entry toEntry(EntryData entryData) {
        Entry entry = new Entry("" + entryData.getId());
        entry.setTitle(entryData.getTitle());
        entry.setStart(entryData.getStart());
        entry.setEnd(entryData.getEnd());
        entry.setAllDay(entryData.isAllDay());
        entry.setColor(entryData.getColor());
        entry.setDescription("Description of " + entry.getTitle());
        return entry;
    }

    public void addEntry(Entry entries) {
        addEntries(Arrays.asList(entries));
    }

    public void addEntries(Collection<Entry> entries) {
        entries.forEach(entry -> database.put(getDatabaseId(entry), new EntryData(getDatabaseId(entry), entry.getTitle(), entry.getStart(), entry.getEnd(), entry.isAllDay(), null)));
    }

    public void updateEntry(Entry entry) {
        database.put(getDatabaseId(entry), new EntryData(getDatabaseId(entry), entry.getTitle(), entry.getStart(), entry.getEnd(), entry.isAllDay(), null));
    }

    public void removeEntry(Entry entry) {
        database.remove(getDatabaseId(entry));
    }

    private int getDatabaseId(Entry entry) {
        return Integer.parseInt(entry.getId());
    }

    public void removeAll() {
        database.clear();
    }

    @Data
    @RequiredArgsConstructor
    @AllArgsConstructor
    private static class EntryData {
        private final int id;
        private String title;
        private LocalDateTime start;
        private LocalDateTime end;
        private boolean allDay;
        private String color;
    }
}
