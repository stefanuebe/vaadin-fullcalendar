package org.vaadin.stefan.ui.view.playground;

import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryQuery;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Simplified simulated backend service for entry provider test views.
 * Based on the demo's EntryService but self-contained.
 */
public class EntryService {

    private final Map<String, Entry> database = new LinkedHashMap<>();
    private int idCounter = 0;

    /**
     * Creates a new instance pre-filled with random data spanning +-3 years.
     */
    public static EntryService createRandomInstance() {
        EntryService service = new EntryService();
        service.fillWithRandomData();
        return service;
    }

    /**
     * Creates a new empty instance.
     */
    public static EntryService createInstance() {
        return new EntryService();
    }

    private void fillWithRandomData() {
        database.clear();
        idCounter = 0;
        LocalDate date = LocalDate.now().minusYears(3).withDayOfYear(1);
        LocalDate end = LocalDate.now().plusYears(3);
        Random random = new Random(123); // deterministic seed for reproducibility

        while (date.isBefore(end)) {
            int maxDays = date.lengthOfMonth();
            for (int i = 0; i < 8; i++) {
                LocalDate start = date.withDayOfMonth(random.nextInt(maxDays) + 1);
                Entry entry = new Entry(String.valueOf(idCounter++));
                entry.setTitle("T " + entry.getId());
                if (random.nextBoolean()) {
                    entry.setStart(start);
                    entry.setAllDay(true);
                    entry.setEnd(start.plusDays(1));
                } else {
                    entry.setStart(start.atTime(10, 0));
                    entry.setEnd(start.atTime(11, 0));
                }
                database.put(entry.getId(), entry);
            }
            date = date.plusMonths(1);
        }
    }

    public Entry createNewInstance() {
        return new Entry(String.valueOf(idCounter++));
    }

    public Optional<Entry> getEntry(String id) {
        Entry e = database.get(id);
        return Optional.ofNullable(e != null ? e.copyAsType(Entry.class) : null);
    }

    public List<Entry> getEntries() {
        return streamEntries().collect(Collectors.toList());
    }

    public Stream<Entry> streamEntries() {
        return database.values().stream().map(e -> e.copyAsType(Entry.class));
    }

    public Stream<Entry> streamEntries(EntryQuery query) {
        Stream<Entry> stream = streamEntries();
        LocalDateTime start = query.getStart();
        LocalDateTime end = query.getEnd();

        if (start != null) {
            stream = stream.filter(e -> {
                if (e.isRecurring()) {
                    LocalDate recurringEnd = e.getRecurringEndDate();
                    return recurringEnd == null || recurringEnd.atStartOfDay().isAfter(start);
                }
                return e.getEndWithOffset() != null && e.getEndWithOffset().isAfter(start);
            });
        }

        if (end != null) {
            stream = stream.filter(e -> {
                if (e.isRecurring()) {
                    LocalDate recurringStart = e.getRecurringStartDate();
                    return recurringStart == null || recurringStart.atStartOfDay().isBefore(end);
                }
                return e.getStartWithOffset() != null && e.getStartWithOffset().isBefore(end);
            });
        }

        return stream;
    }

    public void addEntries(Collection<Entry> entries) {
        for (Entry entry : entries) {
            if (entry.getId() == null) {
                // Assign an id if missing
                entry = entry.copyAsType(Entry.class);
            }
            database.put(entry.getId(), entry);
        }
    }

    public void updateEntry(Entry entry) {
        database.put(entry.getId(), entry);
    }

    public void removeEntries(Collection<Entry> entries) {
        for (Entry entry : entries) {
            database.remove(entry.getId());
        }
    }

    public void removeAll() {
        database.clear();
    }
}
