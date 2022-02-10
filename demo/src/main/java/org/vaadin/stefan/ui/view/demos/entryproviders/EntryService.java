package org.vaadin.stefan.ui.view.demos.entryproviders;

import lombok.NoArgsConstructor;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryQuery;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Simulates an service, that provides access to some kind of database, storing the entry data.
 *
 * @author Stefan Uebe
 */
public class EntryService {

    private final Map<String, EntryData> database = new HashMap<>();

    public EntryService() {
    }

    public static EntryService createInstance() {
        return new EntryService();
    }

    public static EntryService createRandomInstance() {
        EntryService instance = createInstance();
        instance.fillDatabaseWithRandomData();
        return instance;
    }

    public static EntryService createSimpleInstance() {
        EntryService instance = createInstance();
        instance.fillDatabaseWithSimpleData();
        return instance;
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
        entry.setEnd(entry.getStart().plusMinutes(minutes));
        entry.setAllDay(false);
        entry.setColor(color);
        return entry;
    }

    private EntryData createDay(int id, String title, LocalDate start, int days, String color) {
        EntryData entry = new EntryData(id);
        entry.setTitle(title);
        entry.setStart(start.atStartOfDay());
        entry.setEnd(entry.getStart().plusDays(days));
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

    public List<Entry> getEntries() {
        return streamEntries().collect(Collectors.toList());
    }

    public Stream<Entry> streamEntries() {
        return database.values().stream().map(this::toEntry);
    }

    public Stream<Entry> streamEntries(EntryQuery query) {
        Stream<EntryData> stream = database.values().stream();
        return  applyFilter(stream, query).map(this::toEntry);
    }

    public Stream<EntryData> applyFilter(Stream<EntryData> stream, EntryQuery query) {
        LocalDateTime start = query.getStart();
        LocalDateTime end = query.getEnd();

        if (start != null) {
            stream = stream.filter(e -> {
                if (e.isRecurring()) {
                    LocalDateTime recurringEnd = e.getRecurringEnd();

                    // recurring events, that have no end may go indefinitely to the future. So we return
                    // them always
                    return recurringEnd == null || recurringEnd.isAfter(start);
                }

                return e.getEnd() != null && e.getEnd().isAfter(start);
            });
        }

        if (end != null) {
            stream = stream.filter(e -> {
                if (e.isRecurring()) {
                    LocalDateTime recurringStart = e.getRecurringStart();

                    // recurring events, that have no start may go indefinitely to the past. So we return
                    // them always
                    return recurringStart == null || recurringStart.isBefore(end);
                }

                return e.getStart() != null && e.getStart().isBefore(end);
            });
        }
        return stream;
    }

    public void addEntry(Entry entries) {
        addEntries(Arrays.asList(entries));
    }

    public void addEntries(Collection<Entry> entries) {
        entries.forEach(entry -> database.put(entry.getId(), toEntryData(entry)));
    }

    public void updateEntry(Entry entry) {
        database.put(entry.getId(), toEntryData(entry));
    }

    public void removeEntry(Entry entry) {
        database.remove(entry.getId());
    }

    public void removeAll() {
        database.clear();
    }

    public void removeEntries(Collection<Entry> entries) {
        entries.forEach(entry -> database.remove(entry.getId()));
    }

    public Entry createNewInstance() {
        return new Entry(String.valueOf(database.size()));
    }

    public Entry toEntry(EntryData entryData) {
        return entryData.copy(Entry.class);
    }

    public EntryData toEntryData(Entry entry) {
        return entry.copy(EntryData.class);
    }

    /**
     * Simulates a database entry. We extend from entry here to not need writing everything twice.
     */
    @NoArgsConstructor
    public static class EntryData extends Entry {
        public EntryData(String id) {
            super(id);
        }

        public EntryData(int id) {
            this(String.valueOf(id));
        }

    }
}
