package org.vaadin.stefan.fullcalendar.dataprovider;

import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.internal.JsonUtils;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.Timezone;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Stefan Uebe
 */
@NoArgsConstructor
public class InMemoryLLEntryProvider<T extends Entry> extends AbstractEntryProvider<T> {

    private final Map<String, T> entries = new HashMap<>();

    private boolean updateRegistered;
    private boolean detached;
    private boolean resendAll;

    public InMemoryLLEntryProvider(Iterable<T> entries) {
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
        super.setCalendar(calendar);
        if (getCalendar() == null) {
            entries.values().forEach(e -> e.setCalendar(calendar));
        }
    }

    @Override
    public Stream<T> fetch(@NonNull EntryQuery query) {
        return query.applyFilter(entries.values().stream());
    }

    @Override
    public Optional<T> fetchById(@NonNull String id) {
        return Optional.ofNullable(entries.get(id));
    }

    public Optional<T> getEntryById(@NotNull String id) {
        Objects.requireNonNull(id);
        return fetchById(id);
    }

    public List<T> getEntries() {
        return fetchAll().collect(Collectors.toList());
    }

    public List<T> getEntries(LocalDateTime filterStart, LocalDateTime filterEnd) {
        return fetch(new EntryQuery(filterStart, filterEnd)).collect(Collectors.toList());
    }

    public List<T> getEntries(Instant filterStart, Instant filterEnd) {
        return getEntries(Timezone.UTC.convertToLocalDateTime(filterStart), Timezone.UTC.convertToLocalDateTime(filterEnd));
    }

    public List<T> getEntries(@NotNull Instant date) {
        return getEntries(Timezone.UTC.convertToLocalDateTime(date));
    }

    public List<T> getEntries(@NotNull LocalDate date) {
        Objects.requireNonNull(date);
        return getEntries(date.atStartOfDay());
    }

    public List<T> getEntries(@NotNull LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime);
        return getEntries(dateTime, dateTime.plusDays(1));
    }

    /**
     * Adds an entry to this calendar. Noop if the entry id is already registered.
     *
     * @param entry entry
     * @throws NullPointerException when null is passed
     */
    public void addEntry(@NotNull T entry) {
        Objects.requireNonNull(entry);
        addEntries(Collections.singletonList(entry));
    }

    /**
     * Adds an array of entries to the calendar. Noop for the entry id is already registered.
     *
     * @param arrayOfEntries array of entries
     * @throws NullPointerException when null is passed
     */
    @SafeVarargs
    public final void addEntries(@NotNull T... arrayOfEntries) {
        addEntries(Arrays.asList(arrayOfEntries));
    }

    /**
     * Adds a list of entries to the calendar. Noop for already registered entries.
     *
     * @param iterableEntries list of entries
     * @throws NullPointerException when null is passed
     */
    public void addEntries(@NotNull Iterable<T> iterableEntries) {
        Objects.requireNonNull(iterableEntries);

        FullCalendar calendar = getCalendar();
        iterableEntries.forEach(entry -> {
            String id = entry.getId();

            if (!entries.containsKey(id)) {
                entry.setCalendar(calendar);
                entries.put(id, entry);
            }
        });
    }

    /**
     * Removes the given entry. Noop if the id is not registered.
     *
     * @param entry entry
     * @throws NullPointerException when null is passed
     */
    public void removeEntry(@NotNull T entry) {
        Objects.requireNonNull(entry);
        removeEntries(Collections.singletonList(entry));
    }

    /**
     * Removes the given entries. Noop for not registered entries.
     *
     * @param arrayOfEntries entries to remove
     * @throws NullPointerException when null is passed
     */
    @SafeVarargs
    public final void removeEntries(@NotNull T... arrayOfEntries) {
        removeEntries(Arrays.asList(arrayOfEntries));
    }

    /**
     * Removes the given entries. Noop for not registered entries.
     *
     * @param iterableEntries entries to remove
     * @throws NullPointerException when null is passed
     */
    public void removeEntries(@NotNull Iterable<T> iterableEntries) {
        Objects.requireNonNull(iterableEntries);

        iterableEntries.forEach(entry -> {
            String id = entry.getId();
            if (entries.remove(id) != null) {
                entry.setCalendar(null);
            }
        });
    }

    /**
     * Remove all entries.
     */
    public void removeAllEntries() {
        removeEntries(new ArrayList<>(entries.values())); // prevent concurrent mod exception
    }

}
