package org.vaadin.stefan.ui.view.tests;

import com.vaadin.flow.function.SerializableFunction;
import elemental.json.JsonArray;
import elemental.json.JsonValue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.JsonItem;
import org.vaadin.stefan.fullcalendar.JsonUtils;
import org.vaadin.stefan.fullcalendar.dataprovider.EagerInMemoryEntryProvider;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A eager in memory provider, that records temporary crud items and the resulting json arrays sent to the client.
 * @author Stefan Uebe
 */
@Getter
@NoArgsConstructor
class TestProvider extends EagerInMemoryEntryProvider<Entry> {
    private final Map<String, JsonArray> createdJsonArrays = new HashMap<>();
    private final Map<String, Set<String>> tmpItemSnapshots = new HashMap<>();
    private Consumer<TestProvider> afterClientSideUpdateCallback;
    private boolean recording;

    public TestProvider(Consumer<TestProvider> afterClientSideUpdateCallback) {
        this.afterClientSideUpdateCallback = afterClientSideUpdateCallback;
    }

    @Override
    protected JsonArray convertItemsAndSendToClient(String clientSideMethod, Collection<Entry> items, SerializableFunction<Entry, JsonValue> conversionCallback) {
        if (recording) {
            // snapshot the items at this point
            tmpItemSnapshots.put(clientSideMethod, items.stream().map(JsonItem::toString).collect(Collectors.toSet()));
        }
        JsonArray jsonArray = super.convertItemsAndSendToClient(clientSideMethod, items, conversionCallback);

        if (recording) {
            // snapshot the array
            createdJsonArrays.put(clientSideMethod, jsonArray);
        }

        return jsonArray;
    }

    @Override
    protected void executeClientSideUpdate() {
        super.executeClientSideUpdate();

        if (afterClientSideUpdateCallback != null) {
            afterClientSideUpdateCallback.accept(this);
        }

        recording = false;
    }

    @Override
    protected JsonArray convertItemsToJson(Collection<Entry> items, SerializableFunction<Entry, JsonValue> conversionCallback) {
        return super.convertItemsToJson(items, conversionCallback);
    }

    protected void startRecording() {
        this.recording = true;
    }

    public Map<String, ? extends Collection<Entry>> getCreatedJsonArraysAsSets() {
        return createdJsonArrays.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> JsonUtils.ofJsonValue(e.getValue())));
    }
}
