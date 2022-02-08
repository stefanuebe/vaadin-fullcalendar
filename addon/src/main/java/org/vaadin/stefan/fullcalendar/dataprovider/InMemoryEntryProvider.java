package org.vaadin.stefan.fullcalendar.dataprovider;

import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.internal.JsonUtils;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
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
public class InMemoryEntryProvider<T extends Entry> extends AbstractEntryProvider<T> {

    // TODO implement lazy loading by client side, where only query matching entries are fetched

    private final Map<String, T> entries = new HashMap<>();

    private final boolean lazyLoading;

    private final Set<T> vItemsToAdd = new HashSet<>();
    private final Set<T> vItemsToChange = new HashSet<>();
    private final Set<T> vItemsToDelete = new HashSet<>();

    private boolean updateRegistered;
    private boolean detached;
    private boolean resendAll;

    public InMemoryEntryProvider(boolean lazyLoading) {
        this.lazyLoading = lazyLoading;

        if (isEagerLoading()) {
            addEntriesChangeListener(event -> {
                for (T value : entries.values()) {
                    value.markAsDirty();
                }
                triggerClientSideUpdate();
            });

            addEntryRefreshListener(event -> {
                event.getItemToRefresh().markAsDirty();
                triggerClientSideUpdate();
            });

        }
    }

    public InMemoryEntryProvider(boolean lazyLoading, Iterable<T> entries) {
        this(lazyLoading);
        addEntries(entries);
    }

    public InMemoryEntryProvider() {
        this(false);
    }

    public InMemoryEntryProvider(Iterable<T> entries) {
        this(false, entries);
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

            if (isEagerLoading()) {
                triggerClientSideUpdate();
            }
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

    public boolean isLazyLoading() {
        return lazyLoading;
    }

    public boolean isEagerLoading() {
        return isEagerLoading();
    }

    /**
     * Returns the entry with the given id. Is empty when the id is not registered.
     *
     * @param id id
     * @return entry or empty
     * @throws NullPointerException when null is passed
     */
    public Optional<T> getEntryById(@NotNull String id) {
        Objects.requireNonNull(id);
        return fetchById(id);
    }

    /**
     * Returns all entries registered in this instance. Changes in an entry instance is reflected in the
     * calendar instance on server side, but not client side. If you change an entry make sure to call
     * {@link #updateEntry(Entry)} afterwards.
     * <br><br>
     * Changes in the list are not reflected to the calendar's list instance. Also please note, that the content
     * of the list is <b>unsorted</b> and may vary with each call. The return of a list is due to presenting
     * a convenient way of using the returned values without the need to encapsulate them yourselves.
     * <br>
     * <b>This behavior may change in future.</b>
     *
     * @return entries entries
     */
    public List<T> getEntries() {
        return fetchAll().collect(Collectors.toList());
    }

    /**
     * // TODO OUTDATED DOCS
     * Returns all entries registered in this instance which timespan crosses the given time span. You may
     * pass null for the parameters to have the timespan search only on one side. Passing null for both
     * parameters return all entries. The times are converted
     * to UTC before searching. The conversion is done with the calendars timezone.
     * <br><br>
     * Changes in an entry instance is reflected in the
     * calendar instance on server side, but not client side. If you change an entry make sure to call
     * {@link #updateEntry(Entry)} afterwards.
     * <br><br>
     * Please be aware that the filter and entry times are exclusive due to the nature of the FC entries
     * to range from e.g. 07:00-08:00 or "day 1, 0:00" to "day 2, 0:00" where the end is a marker but somehow
     * exclusive to the date.
     * That means, that search for 06:00-07:00 or 08:00-09:00 will NOT include the given time example.
     * Searching for anything between these two timespans (like 06:00-07:01, 07:30-10:00, 07:59-09:00, etc.) will
     * include it.
     *
     * @param filterStart start point of filter timespan or null to have no limit
     * @param filterEnd   end point of filter timespan or null to have no limit
     * @return entries
     */
    public List<T> getEntries(LocalDateTime filterStart, LocalDateTime filterEnd) {
        return fetch(new EntryQuery(filterStart, filterEnd)).collect(Collectors.toList());
    }

    /**
     * Returns all entries registered in this instance which timespan crosses the given time span. You may
     * pass null for the parameters to have the timespan search only on one side. Passing null for both
     * parameters return all entries.
     * <br><br>
     * Changes in an entry instance is reflected in the
     * calendar instance on server side, but not client side. If you change an entry make sure to call
     * {@link #updateEntry(Entry)} afterwards.
     * <br><br>
     * Please be aware that the filter and entry times are exclusive due to the nature of the FC entries
     * to range from e.g. 07:00-08:00 or "day 1, 0:00" to "day 2, 0:00" where the end is a marker but somehow
     * exclusive to the date.
     * That means, that search for 06:00-07:00 or 08:00-09:00 will NOT include the given time example.
     * Searching for anything between these two timespans (like 06:00-07:01, 07:30-10:00, 07:59-09:00, etc.) will
     * include it.
     *
     * @param filterStart start point of filter timespan or null to have no limit
     * @param filterEnd   end point of filter timespan or null to have no limit
     * @return entries
     */
    public List<T> getEntries(Instant filterStart, Instant filterEnd) {
        return getEntries(Timezone.UTC.convertToLocalDateTime(filterStart), Timezone.UTC.convertToLocalDateTime(filterEnd));
    }

    /**
     * Returns all entries registered in this instance which timespan crosses the given date.
     * <br><br>
     * Changes in an entry instance is reflected in the
     * calendar instance on server side, but not client side. If you change an entry make sure to call
     * {@link #updateEntry(Entry)} afterwards.
     *
     * @param date end point of filter timespan
     * @return entries
     * @throws NullPointerException when null is passed
     */
    public List<T> getEntries(@NotNull Instant date) {
        return getEntries(Timezone.UTC.convertToLocalDateTime(date));
    }

    /**
     * // TODO OUTDATED DOCS
     * Returns all entries registered in this instance which timespan crosses the given date. The date is converted
     * to UTC before searching. The conversion is done with the calendars timezone.
     * <br><br>
     * Changes in an entry instance is reflected in the
     * calendar instance on server side, but not client side. If you change an entry make sure to call
     * {@link #updateEntry(Entry)} afterwards.
     *
     * @param date end point of filter timespan
     * @return entries
     * @throws NullPointerException when null is passed
     */
    public List<T> getEntries(@NotNull LocalDate date) {
        Objects.requireNonNull(date);
        return getEntries(date.atStartOfDay());
    }


    /**
     * // TODO OUTDATED DOCS
     * Returns all entries registered in this instance which timespan crosses the given date time. The date time is converted
     * to UTC before searching. The conversion is done with the calendars timezone.
     * <br><br>
     * Changes in an entry instance is reflected in the
     * calendar instance on server side, but not client side. If you change an entry make sure to call
     * {@link #updateEntry(Entry)} afterwards.
     *
     * @param dateTime end point of filter timespan
     * @return entries
     * @throws NullPointerException when null is passed
     */
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

        boolean eagerLoading = isEagerLoading();

        iterableEntries.forEach(entry -> {
            String id = entry.getId();

            if (!entries.containsKey(id)) {
                entry.setCalendar(getCalendar());
                entries.put(id, entry);

                if (eagerLoading) {
                    vItemsToAdd.add(entry);
                }
            }
        });

        triggerClientSideUpdate();
    }

    /**
     * Updates the given entry on the client side. Will check if the id is already registered, otherwise a noop.
     *
     * @param entry entry to update
     * @throws NullPointerException when null is passed
     */
    public void updateEntry(@NotNull T entry) {
        Objects.requireNonNull(entry);
        updateEntries(Collections.singletonList(entry));
    }

    /**
     * Updates the given entries on the client side. Ignores non-registered entries.
     *
     * @param arrayOfEntries entries to update
     * @throws NullPointerException when null is passed
     */
    @SafeVarargs
    public final void updateEntries(@NotNull T... arrayOfEntries) {
        updateEntries(Arrays.asList(arrayOfEntries));
    }


    /**
     * Updates the given entries on the client side. Ignores non-registered entries.
     * NOOP for lazy loading variants.
     *
     * @param iterableEntries entries to update
     * @throws NullPointerException when null is passed
     */
    public void updateEntries(@NotNull Iterable<T> iterableEntries) {
        Objects.requireNonNull(iterableEntries);
        if (isEagerLoading()) {
            vItemsToChange.addAll(StreamSupport.stream(iterableEntries.spliterator(), true).collect(Collectors.toSet()));
            triggerClientSideUpdate();
        }
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

        boolean eagerLoading = isEagerLoading();

        iterableEntries.forEach(entry -> {
            String id = entry.getId();
            if (entries.remove(id) != null) {
                entry.setCalendar(null);

                if (eagerLoading) {
                    vItemsToDelete.add(entry);
                }
            }
        });

        triggerClientSideUpdate();
    }

    /**
     * Remove all entries.
     */
    public void removeAllEntries() {
        removeEntries(new ArrayList<>(entries.values())); // prevent concurrent mod exception
    }

    /**
     * This method is used to inform this component, that some data items have changed. Can be called multiple times
     * (continuing calls are noop).
     * <p/>
     * Registers a single time task before the response is sent to the client. This task will check for
     * added, updated and removed items, filter them depending on each other and then convert the items
     * to their respective json items. The resulting json objects are then send to the client.
     */
    private void triggerClientSideUpdate() {
        if (isEagerLoading() && !updateRegistered && getCalendar() != null) {
            updateRegistered = true;
            FullCalendar calendar = getCalendar();
            calendar.getElement().getNode().runWhenAttached(ui -> {
                ui.beforeClientResponse(calendar, pExecutionContext -> {
                    if (resendAll) {
                        vItemsToAdd.addAll(entries.values());
                    }

                    // items that are not yet on the client need no update
                    vItemsToChange.removeAll(vItemsToAdd);

                    // items to be deleted, need not to be added or updated on the client
                    vItemsToAdd.removeAll(vItemsToDelete);
                    vItemsToChange.removeAll(vItemsToDelete);

                    JsonArray entriesToAdd = convertItemsToJson(vItemsToAdd, item -> {
                        JsonObject json = item.toJson(false);
                        item.setKnownToTheClient(true);
                        item.clearDirtyState();
                        return json;
                    });

                    JsonArray entriesToUpdate = convertItemsToJson(vItemsToChange, item -> {
                        String id = item.getId();
                        if (item.isDirty() && entries.containsKey(id)) {
                            item.setKnownToTheClient(true);
                            JsonObject json = item.toJson(true);
                            item.clearDirtyState();
                            return json;
                        }
                        return null;
                    });

                    JsonArray entriesToRemove = convertItemsToJson(vItemsToDelete, item -> {
                        // only send some json to delete, if the item has not been created previously internally
                        JsonObject jsonObject = item.toJsonWithIdOnly();
                        item.setKnownToTheClient(false);
                        return jsonObject;
                    });

                    if (entriesToAdd.length() > 0) {
                        calendar.getElement().callJsFunction("addEvents", entriesToAdd);
                    }
                    if (entriesToUpdate.length() > 0) {
                        calendar.getElement().callJsFunction("updateEvents", entriesToUpdate);
                    }
                    if (entriesToRemove.length() > 0) {
                        calendar.getElement().callJsFunction("removeEvents", entriesToUpdate);
                    }

                    vItemsToAdd.clear();
                    vItemsToChange.clear();
                    vItemsToDelete.clear();

                    updateRegistered = false;

                    // if there is a request to resend all, it must be removed here
                    resendAll = false;
                });
            });
        }
    }

    protected JsonArray convertItemsToJson(@NotNull final Collection<T> items, final SerializableFunction<T, JsonValue> conversionCallback) {
        Objects.requireNonNull(items);

        JsonArray array = JsonUtils.createArray();
        for (T item : items) {
            JsonValue convertedItem = conversionCallback.apply(item);
            if (convertedItem != null) {
                array.set(array.length(), convertedItem);
            }
        }

        return array;
    }


}
