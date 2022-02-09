package org.vaadin.stefan.fullcalendar.dataprovider;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Basic abstract implementation of an in memory entry provider utilizing a hashmap.
 *
 * @author Stefan Uebe
 */
public abstract class AbstractInMemoryEntryProvider<T extends Entry> extends AbstractEntryProvider<T> implements InMemoryEntryProvider<T> {

    /**
     * Maps the entry ids to their respective entry instance. Any change to this map reflects directly
     * to this instance.
     */
    @Getter(AccessLevel.PROTECTED)
    private final Map<String, T> entriesMap = new HashMap<>();

    public AbstractInMemoryEntryProvider() {
    }

    public AbstractInMemoryEntryProvider(Iterable<T> entries) {
        addEntries(entries);
    }


    /**
     * Connects this instance with the calendar. Not intended to be called manually, the FC will take care of this.
     * NOOP when called for the same calendar instance multiple times.
     *
     * @param calendar calendar to "connect" to.
     */
    @Override
    public void setCalendar(FullCalendar calendar) {
        FullCalendar oldCalendar = getCalendar();
        super.setCalendar(calendar);

        if (oldCalendar != calendar) {
            entriesMap.values().forEach(e -> e.setCalendar(calendar));
        }
    }

    @Override
    public Stream<T> fetch(@NonNull EntryQuery query) {
        return query.applyFilter(entriesMap.values().stream());
    }

    @Override
    public Optional<T> fetchById(@NonNull String id) {
        return Optional.ofNullable(entriesMap.get(id));
    }

    /**
     * Adds a list of entries to the calendar. Noop for already registered entries.
     *
     * @param iterableEntries list of entries
     * @throws NullPointerException when null is passed
     */
    @Override
    public void addEntries(@NotNull Iterable<T> iterableEntries) {
        Objects.requireNonNull(iterableEntries);

        iterableEntries.forEach(entry -> {
            String id = entry.getId();

            if (!entriesMap.containsKey(id)) {
                entriesMap.put(id, entry);
                entry.setCalendar(getCalendar());
                onEntryAdd(entry);
            }
        });
    }

    protected void onEntryAdd(T entry) {

    }


    /**
     * Removes the given entries. Noop for not registered entries.
     *
     * @param iterableEntries entries to remove
     * @throws NullPointerException when null is passed
     */
    @Override
    public void removeEntries(@NotNull Iterable<T> iterableEntries) {
        Objects.requireNonNull(iterableEntries);

        iterableEntries.forEach(entry -> {
            String id = entry.getId();
            if (entriesMap.remove(id) != null) {
                entry.setCalendar(null);

                onEntryRemove(entry);
            }
        });
    }

    protected void onEntryRemove(T entry) {

    }


}
