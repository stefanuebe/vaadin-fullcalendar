package org.vaadin.stefan.ui.view.demos.entryproviders;

import lombok.NoArgsConstructor;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.ResourceEntry;
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
public class EntryService<T extends Entry> {

    private final Map<String, EntryData> database = new HashMap<>();
    private final boolean resourceEntries;

    public EntryService(boolean resourceEntries) {
        this.resourceEntries = resourceEntries;
    }

    public static EntryService<Entry> createInstance() {
        return new EntryService<>(false);
    }

    public static EntryService<Entry> createRandomInstance() {
        EntryService<Entry> instance = createInstance();
        instance.fillDatabaseWithRandomData();
        return instance;
    }

    public static EntryService<Entry> createSimpleInstance() {
        EntryService<Entry> instance = createInstance();
        instance.fillDatabaseWithSimpleData();
        return instance;
    }

    public static EntryService<ResourceEntry> createResourceInstance() {
        return new EntryService<>(true);
    }

    public static EntryService<ResourceEntry> createRandomResourceInstance() {
        EntryService<ResourceEntry> instance = createResourceInstance();
        instance.fillDatabaseWithRandomData();
        return instance;
    }

    public static EntryService<ResourceEntry> createSimpleResourceInstance() {
        EntryService<ResourceEntry> instance = createResourceInstance();
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
                    addDefaultDay("T " + database.size(), start);
                } else {
                    addDefaultTimed("T " + database.size(), start.atTime(10, 0));
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

    /**
     * Creates a timed backend entry.
     */
    private EntryData createTimed(int id, String title, LocalDateTime start, int minutes, String color) {
        EntryData entry = new EntryData(id);
        entry.setTitle(title);
        entry.setStart(start);
        entry.setEnd(entry.getStart().plusMinutes(minutes));
        entry.setAllDay(false);
        entry.setColor(color);
        return entry;
    }

    /**
     * Creates an all day backend entry.
     */
    private EntryData createDay(int id, String title, LocalDate start, int days, String color) {
        EntryData entry = new EntryData(id);
        entry.setTitle(title);
        entry.setStart(start.atStartOfDay());
        entry.setEnd(entry.getStart().plusDays(days));
        entry.setAllDay(true);
        entry.setColor(color);
        return entry;
    }

    /**
     * Creates and adds an all day backend entry.
     */
    private void addDay(String title, LocalDate start, int days, String color) {
        EntryData data = createDay(database.size(), title, start, days, color);
        database.put(data.getId(), data);
    }

    /**
     * Creates and adds a timed backend entry.
     */
    private void addTimed(String title, LocalDateTime start, int minutes, String color) {
        EntryData data = createTimed(database.size(), title, start, minutes, color);
        database.put(data.getId(), data);
    }

    /**
     * Creates and adds an all day backend entry with a duration of 1 day.
     */
    private void addDefaultDay(String title, LocalDate start) {
        EntryData data = createDay(database.size(), title, start, 1, null);
        database.put(data.getId(), data);
    }

    /**
     * Creates and adds a timed backend entry with a duration of 60 minutes.
     */
    private void addDefaultTimed(String title, LocalDateTime start) {
        EntryData data = createTimed(database.size(), title, start, 60, null);
        database.put(data.getId(), data);
    }

    /**
     * Fetches a single calendar entry from the backend and converts it to an entry.
     * @param id id
     * @return entry or empty
     */
    public Optional<T> getEntry(String id) {
        return Optional.ofNullable(database.get(id)).map(this::toEntry);
    }

    /**
     * Same as {@link #getEntry(String)} but as non optional (returns null)
     * @param id id
     * @return entry or null
     */
    public T getEntryOrNull(String id) {
        return getEntry(id).orElse(null);
    }

    /**
     * Returns the amount of backend entries.
     * @return count
     */
    public int count() {
        return database.size();
    }

    /**
     * Returns {@link #streamEntries()} as list.
     * @return list of entries
     */
    public List<T> getEntries() {
        return streamEntries().collect(Collectors.toList());
    }

    /**
     * Reads all items from the backend and converts to entries.
     * @return entries
     */
    public Stream<T> streamEntries() {
        return database.values().stream().map(this::toEntry);
    }

    /**
     * Reads items from the backend, filters them by the given query and converts the matching items to entries.
     * @return filtered entries
     */
    public Stream<T> streamEntries(EntryQuery query) {
        Stream<EntryData> stream = database.values().stream();
        return  applyFilter(stream, query).map(this::toEntry);
    }

    /**
     * Applies the filter to the given stream. All entries, that do not cross the filter timespan (either
     * by recurrence or normal start/end) are filtered out. The {@link EntryQuery#getAllDay()} is not taken
     * into account. Query might be empty, but must not be null.
     * @param stream stream
     * @param query query
     * @return filtered stream
     */
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

    /**
     * Adds the given entries to the backend. Existing items might be replaced.
     * @param entries entries
     */
    @SafeVarargs
    public final void addEntries(T... entries) {
        addEntries(Arrays.asList(entries));
    }

    /**
     * Adds the given entries to the backend. Existing items might be replaced.
     * @param entries entries
     */
    public void addEntries(Collection<T> entries) {
        entries.forEach(entry -> database.put(entry.getId(), toEntryData(entry)));
    }

    /**
     * Updates the given entry in the backend.
     * @param entry entry
     */
    public void updateEntry(T entry) {
        database.put(entry.getId(), toEntryData(entry));
    }

    /**
     * Removes the given entries from the backend.
     * @param entries entries
     */
    @SafeVarargs
    public final void removeEntries(T... entries) {
        removeEntries(Arrays.asList(entries));
    }

    /**
     * Removes the given entries from the backend.
     * @param entries entries
     */
    public void removeEntries(Collection<T> entries) {
        entries.forEach(entry -> database.remove(entry.getId()));
    }

    /**
     * Removes everything from the backend.
     */
    public void removeAll() {
        database.clear();
    }

    /**
     * Creates a new entry instance with a valid id.
     * @return new instance
     */
    @SuppressWarnings("unchecked")
    public T createNewInstance() {
        return (T) (resourceEntries ? new ResourceEntry(String.valueOf(database.size())) : new Entry(String.valueOf(database.size())));
    }

    /**
     * Converts the backend item to an entry.
     * @param entryData backend item
     * @return entry
     */
    @SuppressWarnings("unchecked")
    private T toEntry(EntryData entryData) {
        return (T) (resourceEntries ? entryData.copy(ResourceEntry.class) : entryData.copy(Entry.class));
    }

    /**
     * Converts the entry to a backend item.
     * @param entry entry
     * @return backend item
     */
    private EntryData toEntryData(T entry) {
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
