package org.vaadin.stefan.fullcalendar.dataprovider;

import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.internal.JsonUtils;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Stefan Uebe
 */
public class EagerInMemoryEntryProvider<T extends Entry> extends AbstractInMemoryEntryProvider<T> {
    private final Set<T> tmpAdd = new HashSet<>();
    private final Set<T> tmpUpdate = new HashSet<>();
    private final Set<T> tmpRemove = new HashSet<>();

    private boolean updateRegistered;

    private boolean resendAll;

    public EagerInMemoryEntryProvider() {
        addEntriesChangeListener(event -> {
            resendAll = true;
            triggerClientSideUpdate();
        });

        addEntryRefreshListener(event -> {
            event.getItemToRefresh().markAsDirty();
            triggerClientSideUpdate();
        });
    }

    public EagerInMemoryEntryProvider(Iterable<T> entries) {
        this();
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

        boolean hasChanged = oldCalendar != calendar;
        super.setCalendar(calendar);

        if (hasChanged) {
            triggerClientSideUpdate();
        }
    }

    /**
     * Adds a list of entries to the calendar. Noop for already registered entries.
     *
     * @param iterableEntries list of entries
     * @throws NullPointerException when null is passed
     */
    public void addEntries(@NotNull Iterable<T> iterableEntries) {
        super.addEntries(iterableEntries);
        triggerClientSideUpdate();
    }

    @Override
    protected void onEntryAdd(T entry) {
        tmpAdd.add(entry);
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
     *
     * @param iterableEntries entries to update
     * @throws NullPointerException when null is passed
     */
    public void updateEntries(@NotNull Iterable<T> iterableEntries) {
        Objects.requireNonNull(iterableEntries);
        Map<String, T> entriesMap = getEntriesMap();
        tmpUpdate.addAll(StreamSupport.stream(iterableEntries.spliterator(), true)
                .filter(entry -> entriesMap.containsKey(entry.getId()) && entry.isKnownToTheClient())
                .collect(Collectors.toSet()));
        triggerClientSideUpdate();
    }

    /**
     * Removes the given entries. Noop for not registered entries.
     *
     * @param iterableEntries entries to remove
     * @throws NullPointerException when null is passed
     */
    public void removeEntries(@NotNull Iterable<T> iterableEntries) {
        super.removeEntries(iterableEntries);
        triggerClientSideUpdate();
    }

    @Override
    protected void onEntryRemove(T entry) {
        tmpRemove.add(entry);
    }

    /**
     * This method is used to inform this component, that some data items have changed. Can be called multiple times
     * (continuing calls are noop).
     * <p/>
     * Registers a single time task before the response is sent to the client. This task will check for
     * added, updated and removed items, filter them depending on each other and then convert the items
     * to their respective json items. The resulting json objects are then send to the client.
     */
    protected void triggerClientSideUpdate() {
        FullCalendar calendar = getCalendar();
        if (!updateRegistered && calendar != null && calendar.isAttached()) { // not sure if calendar.isAttached() is really necessary
            updateRegistered = true;
            calendar.getElement().getNode().runWhenAttached(ui -> {
                ui.beforeClientResponse(calendar, pExecutionContext -> {
                    executeClientSideUpdate();
                });
            });
        }
    }

    /**
     * Executes the client side update. This method is not intended to be called manually, but by
     * {@link #triggerClientSideUpdate()} only (or for test purposes).
     */
    protected void executeClientSideUpdate() {
        Map<String, T> entriesMap = getEntriesMap();

        if (resendAll) {
            tmpRemove.addAll(entriesMap.values());
            tmpAdd.addAll(entriesMap.values());
        }

        // items that are not yet on the client need no update
        tmpUpdate.removeAll(tmpAdd);

        // items to be deleted, need not to be added or updated on the client
        tmpUpdate.removeAll(tmpRemove);

        convertItemsAndSendToClient("removeEvents", tmpRemove, item -> {
            // only send some json to delete, if the item has already been sent to the client once
            if (item.isKnownToTheClient()) {
                JsonObject jsonObject = item.toJsonWithIdOnly();
                item.setKnownToTheClient(false);
                return jsonObject;
            }
            return null;
        });

        convertItemsAndSendToClient("addEvents", tmpAdd, item -> {
            if (entriesMap.containsKey(item.getId())) { // prevent accidentally adding items, that have been removed after adding
                JsonObject json = item.toJson(false);
                item.setKnownToTheClient(true);
                item.clearDirtyState();
                return json;
            }
            return null;
        });

        convertItemsAndSendToClient("updateEvents", tmpUpdate, item -> {
            String id = item.getId();
            if (item.isDirty() && entriesMap.containsKey(id)) {
                JsonObject json = item.toJson(true);
                item.clearDirtyState();
                return json;
            }
            return null;
        });


        tmpAdd.clear();
        tmpUpdate.clear();
        tmpRemove.clear();

        updateRegistered = false;

        // if there is a request to resend all, it must be removed here
        resendAll = false;
    }

    /**
     * Converts the given set of items to a json array and sends it to the client using the given client side method.
     * Returns the created array for test purposes.
     * @param clientSideMethod client side method to call
     * @param items items to convert
     * @param conversionCallback conversion callback for each item
     * @return created json array
     */
    protected JsonArray convertItemsAndSendToClient(String clientSideMethod, @NotNull final Collection<T> items, final SerializableFunction<T, JsonValue> conversionCallback) {
        JsonArray entriesToSend = convertItemsToJson(items, conversionCallback);
        if (entriesToSend.length() > 0) {
            getCalendar().getElement().callJsFunction(clientSideMethod, entriesToSend);
        }

        return entriesToSend;
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

    /**
     * Refreshes all data based on currently available data in the underlying
     * provider.
     * <p></p>
     * <b>Be careful</b> as this method will lead to a full resend of all entries of this instance, not
     * just the ones for the current shown interval
     */
    @Override
    public void refreshAll() {
        super.refreshAll();
    }
}
