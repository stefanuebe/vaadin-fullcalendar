package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.function.SerializableFunction;
import elemental.json.Json;
import elemental.json.JsonObject;
import elemental.json.JsonType;
import elemental.json.JsonValue;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Stefan Uebe
 */
public abstract class JsonItem<ID_TYPE> {
    /**
     * This flag indicates, if this instance is already known to the client. Depending on that,
     * any changes to the properties will be handled differently using a change set and sending only
     * that particular changes.
     */
    @Getter
    @Setter
    private boolean knownToTheClient;

    private static final Key _HARD_RESET = Key.builder().name("_hardReset").build();

    private final Map<Key, Object> properties = new HashMap<>();
    private final Set<Key> changedProperties = new HashSet<>();

    protected JsonItem(ID_TYPE id) {
        setId(Objects.requireNonNull(id, "ID must not be null!"));
    }

    /**
     * Returns the value of the property identified by the given key. Returned value might be null.
     *
     * @param key property key
     * @param <T> return type
     * @return property value (can be null)
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Key key) {
        if (!has(key)) {
            return (T) key.getDefaultValue();
        }

        return (T) properties.get(key);
    }

    /**
     * Returns the value of the property identified by the given key or a default. Returned value might be null.
     * <p></p>
     * For keys that define their own default value, this method will never return the given defaultValue argument.
     *
     * @param key          property key
     * @param defaultValue default value to return if no value has been found
     * @param <T>          return type
     * @return property value (can be null)
     */
    public <T> T get(Key key, T defaultValue) {
        T value = get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Returns the value of the property identified by the given key or a default. If the default has been used, it will
     * also be registered (calling {@link #set(Key, Object)}). Returned value might be null.
     * <p></p>
     * For keys that define their own default value, this method will never return the given defaultValue argument.
     *
     * @param key                  property key
     * @param defaultValueCallback default value to return if no value has been found
     * @param <T>                  return type
     * @return property value (can be null)
     */
    public <T> T getOrInit(Key key, SerializableFunction<JsonItem, T> defaultValueCallback) {
        if (!has(key)) {
            T defaultValue = defaultValueCallback.apply(this);
            set(key, defaultValue);
        }

        return get(key);
    }

    /**
     * Returns, if this instance has anything explicitly set for this property, including an empty or null value.
     * Will return {@code true} for the key, if {@link #set} has been called before. Will return {@code false} only,
     * if {@link #set} has not yet been called. Will also return {@code false}, if the key itself defines a default value,
     * but the property has not yet been initialized "manually".
     *
     * @param key key to check
     * @return has a key set
     */
    public boolean has(Key key) {
        return properties.containsKey(key);

    }

    /**
     * Removes the property value for this instance and returns it. Will also remove the "changed property" marker
     * for this property.
     * <p/>
     * This removal is server side only, the client side will not be informed. If you want to remove something for
     * the client, please use {@link #set} and pass an empty value matching the expected type.
     *
     * @param key key to check
     * @param <T> value type
     * @return removed value
     */
    @SuppressWarnings("unchecked")
    public <T> T remove(Key key) {
        changedProperties.remove(key);
        return (T) properties.remove(key);
    }

    /**
     * Returns the value of the property identified by the given key as a native boolean. Returns
     * false, if the value is null.
     *
     * @param key property key
     * @return property value (can be null)
     * @throws ClassCastException property type is not Boolean
     */
    public boolean getBoolean(Key key) {
        return getBoolean(key, false);
    }

    /**
     * Returns the value of the property identified by the given key as a native boolean. Returns
     * the given default value, if the value is null.
     *
     * @param key          property key
     * @param defaultValue default value to return if no value has been found
     * @return property value (can be null)
     * @throws ClassCastException property type is not Boolean
     */
    public boolean getBoolean(Key key, boolean defaultValue) {
        Boolean value = get(key, defaultValue);
        return value != null && value;
    }

    /**
     * Returns the value of the property identified by the given key as a native integer. Returns
     * 0, if the value is null. Supports any {@link Number} type (with their respective conversion rules).
     *
     * @param key property key
     * @return property value (can be null)
     * @throws ClassCastException property type is not Number
     */
    public int getInt(Key key) {
        return getInt(key, 0);
    }

    /**
     * Returns the value of the property identified by the given key as a native integer. Returns
     * the default value, if the value is null.
     * Supports any {@link Number} type (with their respective conversion rules).
     *
     * @param key          property key
     * @param defaultValue default value to return if no value has been found
     * @return property value (can be null)
     * @throws ClassCastException property type is not Number
     */
    public int getInt(Key key, int defaultValue) {
        return get(key, defaultValue);
    }


    /**
     * Returns the value of the property identified by the given key as a native double. Returns
     * 0, if the value is null. Supports any {@link Number} type (with their respective conversion rules).
     *
     * @param key property key
     * @return property value (can be null)
     * @throws ClassCastException property type is not Number
     */
    public double getDouble(Key key) {
        return getDouble(key, 0.0);
    }

    /**
     * Returns the value of the property identified by the given key as a native double. Returns
     * the default value, if the value is null. Supports any {@link Number} type (with their respective conversion rules).
     *
     * @param key          property key
     * @param defaultValue default value to return if no value has been found
     * @return property value (can be null)
     * @throws ClassCastException property type is not Number
     */
    public double getDouble(Key key, double defaultValue) {
        return get(key, defaultValue);
    }


    /**
     * Returns the value of the property identified by the given key as a native long. Returns
     * 0, if the value is null. Supports any {@link Number} type (with their respective conversion rules).
     *
     * @param key property key
     * @return property value (can be null)
     * @throws ClassCastException property type is not Number
     */
    public long getLong(Key key) {
        return getLong(key, 0L);
    }

    /**
     * Returns the value of the property identified by the given key as a native long. Returns
     * the default value, if the value is null. Supports any {@link Number} type (with their respective conversion rules).
     *
     * @param key          property key
     * @param defaultValue default value to return if no value has been found
     * @return property value (can be null)
     * @throws ClassCastException property type is not Number
     */
    public long getLong(Key key, long defaultValue) {
        return get(key, defaultValue);
    }


    /**
     * Checks if the property value identified by the given key is null.
     *
     * @param key key
     * @return is null
     */
    public boolean isNull(Key key) {
        Object serializable = get(key);
        return serializable == null;
    }

    /**
     * Checks if the property identified by the given key is an empty string.
     *
     * @param key key
     * @return is empty
     */
    public boolean isEmpty(Key key) {
        Object serializable = get(key);
        return serializable == null || serializable.toString().isEmpty();
    }

    /**
     * Checks if the property identified by the given key is empty. Since empty is
     * relative to the type, the second parameter has to determine the "default" empty
     * value (e.g. 0 or false). Must not be null.
     *
     * @param key   key
     * @param empty empty represent
     * @return is empty
     */
    public boolean isEmpty(Key key, Object empty) {
        Object serializable = get(key);
        return serializable == null || serializable.equals(empty);
    }

    /**
     * Sets a value for the given key. Throws an exception when the value is null.
     *
     * @param key   key
     * @param value value
     */
    public void setNotNull(Key key, @NotNull Object value) {
        Objects.requireNonNull(value, key.getName() + " does not allow null values");
        set(key, value);
    }


    /**
     * Sets a string for the given key. Throws an exception when the string is null or empty.
     *
     * @param key   key
     * @param value value
     * @see StringUtils#isEmpty(CharSequence)
     */
    public void setNotEmpty(Key key, @NotNull String value) {
        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException(key.getName() + " does not allow empty strings");
        }
        set(key, value);
    }

    /**
     * Sets a string for the given key. Throws an exception when the string is null or blank.
     *
     * @param key   key
     * @param value value
     * @see StringUtils#isBlank(CharSequence)
     */
    public void setNotBlank(Key key, @NotNull String value) {
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException(key.getName() + " does not allow blank strings");
        }
        set(key, value);
    }

    /**
     * Sets the value of the property identified by the given key, when the value differs to the current one.
     * Can be null to clear the value.
     * <p/>
     * When this instance is known to the client, the property is also registered as "has changed".
     *
     * @param key   key
     * @param value value
     */
    public void set(Key key, Object value) {
        if (!Objects.equals(value, get(key))) {
            setWithoutDirtyChange(key, value);
            if (isToBeMarkedAsChangedProperty(key)) {
                changedProperties.add(key);
            }
        }
    }

    /**
     * This method indicates, if the given key shall be marked as a changed property for the client
     * side, when it is changed on the server side via {@link #set}. This method shall assure, that for instance
     * for new items the concept of "changed properties" is irrelevant or there might be properties, that
     * do not need to be send on update.
     *
     * @param key property key
     * @return shall be marked as changed property
     */
    protected boolean isToBeMarkedAsChangedProperty(Key key) {
        return knownToTheClient;
    }

    public boolean isMarkedAsChangedProperty(Key key) {
        return changedProperties.contains(key);
    }

    /**
     * Sets the value of the property identified by the given key. Can be null to clear the value.
     * <p/>
     * The property is not marked as "dirty". This method should be used, when reading values from the client side.
     *
     * @param key   key
     * @param value value
     */
    public void setWithoutDirtyChange(Key key, Object value) {
        if (StringUtils.isBlank(key.getName())) {
            throw new IllegalArgumentException("Key id must not be null!");
        }
        properties.put(key, value);
    }

    /**
     * Checks whether this instance is dirty or not. Dirty means, that has changed property values. Always
     * false on items without an ID.
     *
     * @return is dirty
     */
    public boolean isDirty() {
        return !changedProperties.isEmpty();
    }


    /**
     * Clears the dirty state for this item and marks it thus as synced to the server.
     */
    public void clearDirtyState() {
        changedProperties.clear();
    }

    /**
     * Clears the dirty state for a specific property of this item and marks it thus as synced to the server.
     *
     * @param key property key to be marked as not dirty
     */
    public void clearDirtyState(Key key) {
        changedProperties.remove(key);
    }


    /**
     * Converts this instance to a json object. It writes all initialized properties. Properties, that are not used,
     * will not be written.
     *
     * @return json object representing this instance
     */
    public final JsonObject toJson() {
        return toJson(false);
    }

    /**
     * Converts this instance to a json object. Depending on the boolean parameter it writes either all initialized
     * or only changed properties. Properties, that are not used, will not be written.
     *
     * @param changedValuesOnly only write changed values
     * @return json object representing this instance
     */
    public final JsonObject toJson(boolean changedValuesOnly) {
        JsonObject jsonObject = Json.createObject();
        if (changedValuesOnly) {
            writeValuesToJsonWhenChanged(jsonObject);
        } else {
            writeValuesToJson(jsonObject, false);
        }
        return jsonObject;
    }

    /**
     * Converts this instance to a json object, which can be used, when an item has been added to the calendar.
     * Creates the json object and passes it to {@link #writeJsonOnAdd(JsonObject)}.
     *
     * @return json object representing this instance
     */
    public final JsonObject toJsonOnAdd() {
        JsonObject jsonObject = Json.createObject();
        writeJsonOnAdd(jsonObject);
        return jsonObject;
    }

    /**
     * This method is called when this instance is added to the calendar to convert and write all necessary properties
     * to the given json object, that are needed to inform the client side.
     * <p/>
     * Calls by default {@link #writeValuesToJson} with writeUnsetProperties == {@code false}.
     *
     * @param jsonObject json object to fill
     */
    protected void writeJsonOnAdd(JsonObject jsonObject) {
        writeValuesToJson(jsonObject, false);
    }

    /**
     * Converts this instance to a json object, which can be called, when an existing item shall
     * be updated in the calendar.
     * Creates the json object and passes it to {@link #writeJsonOnUpdate(JsonObject)}.
     * <p/>
     * It is recommended to call {@link #clearDirtyState()} afterwards.
     *
     * @return json object representing this instance
     */
    public final JsonObject toJsonOnUpdate() {
        JsonObject jsonObject = Json.createObject();
        writeJsonOnUpdate(jsonObject);
        return jsonObject;
    }

    /**
     * This method is called when this instance is updated to convert and write all necessary properties
     * to the given json object, that are needed to inform the client side.
     * <p/>
     * Calls by default {@link #writeValuesToJsonWhenChanged(JsonObject)} and also writes the id to the json object.
     * The id is always written after the changed values have been written.
     *
     * @param jsonObject json object to fill
     */
    protected void writeJsonOnUpdate(JsonObject jsonObject) {
        writeValuesToJsonWhenChanged(jsonObject);
        writeIdToJson(jsonObject);
    }

    /**
     * Converts this instance to a json object, which can be called, when an existing item shall
     * be removed from the calendar.
     * Creates the json object and passes it to {@link #writeJsonOnDelete(JsonObject)}.
     *
     * @return json object representing this instance
     */
    public final JsonObject toJsonOnDelete() {
        JsonObject jsonObject = Json.createObject();
        writeJsonOnDelete(jsonObject);
        return jsonObject;
    }

    /**
     * This method is called when this instance is deleted from the calendar to convert and write all necessary properties
     * to the given json object, that are needed to inform the client side.
     * <p/>
     * Writes by default only the id to the json object.
     *
     * @param jsonObject json object to fill
     */
    protected void writeJsonOnDelete(JsonObject jsonObject) {
        writeIdToJson(jsonObject);
    }

    protected void writeIdToJson(JsonObject jsonObject) {
        writeValueToJson(jsonObject, getIdKey());
    }

    protected void writeHardResetToJson(JsonObject jsonObject) {
        writeRawValueToJson(jsonObject, _HARD_RESET, JsonUtils.toJsonValue(true));
    }

    /**
     * Writes all property values to the json object, regardless of if they have changes or not. Recommended
     * for new items or on full redraw of an item.
     * <p/>
     * The second parameter allows to exclude unset properties from being written and send to the client. This may
     * spare unused overhead, since otherwise if an object has for instance 9 of 10 properties unset,
     * they would all be send as "null" by default. Passing {@code false} will leave these properties out.
     * Please note, that any property having explicitly set {@code null} or an "empty" value will still be sent
     * to the client.
     *
     * @param jsonObject           json object to write to
     * @param writeUnsetProperties write properties unset properties
     * @see #set
     * @see #has
     * @see #remove
     */
    protected void writeValuesToJson(JsonObject jsonObject, boolean writeUnsetProperties) {
        writeValuesToJson(jsonObject, writeUnsetProperties, getKeys());
    }

    /**
     * Returns the set of known keys of this instance.
     *
     * @return keys
     * @see Key
     */
    public abstract Set<Key> getKeys();

    /**
     * Returns the item's internal id. Every id must be unique for a calendar instance in the item's scope, otherwise
     * it will replace existing items.
     * <p/>
     * Never null.
     *
     * @return id
     */
    public ID_TYPE getId() {
        return get(Objects.requireNonNull(getIdKey()));
    }

    /**
     * Sets the id of this item. It is recommended to not use this method except for you know what you are doing.
     * Passing duplicates or null can and will lead to issues.
     *
     * @param id new id
     */
    protected void setId(ID_TYPE id) {
        set(Objects.requireNonNull(getIdKey()), id);
    }

    /**
     * Returns the ID key. Must not be null!
     *
     * @return id key
     */
    protected abstract Key getIdKey();

    /**
     * Writes all property values mapped by the given keys to the json object,
     * regardless of if they have changes or not. Recommended for new items or on full redraw of an item.
     * <p/>
     * The second parameter allows to exclude unset properties from being written and send to the client. This may
     * spare unused overhead, since otherwise if an object has for instance 9 of 10 properties unset,
     * they would all be send as "null" by default. Passing {@code false} will leave these properties out.
     * Please note, that any property having explicitly set {@code null} or an "empty" value will still be sent
     * to the client.
     * <p/>
     * Passing an empty keys object leads to an empty json object.
     *
     * @param jsonObject           json object to write to
     * @param writeUnsetProperties write properties unset properties
     * @param keysToWrite          keys to write
     * @see #set
     * @see #has
     * @see #remove
     */
    protected void writeValuesToJson(JsonObject jsonObject, boolean writeUnsetProperties, Key... keysToWrite) {
        writeValuesToJson(jsonObject, writeUnsetProperties, Arrays.asList(keysToWrite));
    }

    /**
     * Writes all property values mapped by the given keys to the json object,
     * regardless of if they have changes or not. Recommended for new items or on full redraw of an item.
     * <p/>
     * The second parameter allows to exclude unset properties from being written and send to the client. This may
     * spare unused overhead, since otherwise if an object has for instance 9 of 10 properties unset,
     * they would all be send as "null" by default. Passing {@code false} will leave these properties out.
     * Please note, that any property having explicitly set {@code null} or an "empty" value will still be sent
     * to the client. Same goes for properties, where the key has a default value.
     * <p/>
     * Passing an empty keys object leads to an empty json object.
     *
     * @param jsonObject           json object to write to
     * @param writeUnsetProperties write properties unset properties
     * @param keysToWrite          keys to write
     * @see #set
     * @see #has
     * @see #remove
     */
    protected void writeValuesToJson(JsonObject jsonObject, boolean writeUnsetProperties, Collection<Key> keysToWrite) {
        for (Key key : keysToWrite) {
            if (writeUnsetProperties || has(key) || key.getDefaultValue() != null) {
                writeValueToJson(jsonObject, key);
            }
        }
    }

    /**
     * Writes the property value identified by the given key to the json object. Will not check for changes or
     * unset properties - this has to be done by the caller.
     *
     * @param jsonObject json object
     * @param key        key of the property to write
     */
    protected void writeValueToJson(JsonObject jsonObject, Key key) {
        writeValueToJsonInternal(jsonObject, key);
    }

    /**
     * This method takes care of parsing the property value and setting it based on the key to the given
     * json object. If the key has a converter, the converter will be taken into account to convert the
     * value, otherwise the method {@link JsonUtils#toJsonValue} will be used.
     * Will not check for changes or unset properties - this has to be done by the caller.
     *
     * @param jsonObject json object to write to
     * @param key        key of the property to write
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void writeValueToJsonInternal(JsonObject jsonObject, Key key) {
        Object value = get(key);

        JsonValue jsonValue = Optional.ofNullable(key.getConverter())
                .map(c -> ((JsonPropertyConverter) c).toJsonValue(value, this))
                .orElseGet(() -> {
                    if (JsonUtils.isCollectable(value) && key.getCollectableItemConverter() != null) { // see docs of collectionItemConverter
                        SerializableFunction<Object, JsonValue> converter = key.getCollectableItemConverter();
                        return JsonUtils.toJsonValue(value, converter);
                    } else {
                        return JsonUtils.toJsonValue(value);
                    }
                });

        writeRawValueToJson(jsonObject, key, jsonValue);
    }

    /**
     * Writes the given json value to the json object with the given key.
     *
     * @param jsonObject json object to modify
     * @param key        key to be written
     * @param jsonValue  value to be written
     */
    protected void writeRawValueToJson(JsonObject jsonObject, Key key, JsonValue jsonValue) {
        jsonObject.put(key.getName(), jsonValue);
    }

    /**
     * Writes all changed values to the given json object. Changed also means that the property might have been set
     * to {@code null} or an empty default value.
     * Noop for items without ID or without changes or unset properties.
     * <p/>
     * Does NOT remove the dirty state - this has to be done by the caller.
     *
     * @param jsonObject object to write to
     * @see #clearDirtyState()
     */
    protected void writeValuesToJsonWhenChanged(JsonObject jsonObject) {
        for (Key key : changedProperties) {
            writeValueToJson(jsonObject, key);
        }
    }

    /**
     * Updates this instance based on the given json object. Calls {@link #updateFromJson(JsonObject, boolean)}
     * with "true" as second parameter. This may lead to a dirty object.
     *
     * @param jsonObject json object to read from
     * @see #isValidJsonSource(JsonObject)
     * @see #readJson(JsonObject, boolean)
     * @deprecated this method will be removed in one of the next versions, use {@link #updateFromJson(JsonObject)} instead
     */
    @Deprecated
    public final void update(JsonObject jsonObject) {
        updateFromJson(jsonObject);
    }

    /**
     * Updates this instance based on the given json object. Calls {@link #updateFromJson(JsonObject, boolean)}
     * with "true" as second parameter. This may lead to a dirty object.
     *
     * @param jsonObject json object to read from
     * @see #isValidJsonSource(JsonObject)
     * @see #readJson(JsonObject, boolean)
     */
    public final void updateFromJson(JsonObject jsonObject) {
        updateFromJson(jsonObject, true);
    }


    /**
     * Updates this instance based on the given json object. Checks, if the json object is a
     * valid source for this instance and then calls {@link #readJson(JsonObject, boolean)}.
     * <p></p>
     * The boolean parameter indicates, if changes written to this instance shall be marked
     * as dirty / changed properties. This can lead to additional load of data sent to the client
     * on the next update. If false, then any property, that was marked as dirty before and is now
     * overridden by the given json object data, will be reset to "not dirty".
     *
     * @param jsonObject         json object to read from
     * @param markAppliedAsDirty applied changes shall be marked as dirty (changed) values
     * @see #isValidJsonSource(JsonObject)
     * @see #readJson(JsonObject, boolean)
     */
    public final void updateFromJson(JsonObject jsonObject, boolean markAppliedAsDirty) {
        if (isValidJsonSource(jsonObject)) {
            readJson(jsonObject, markAppliedAsDirty);
        } else {
            throw new IllegalArgumentException("Not a valid json source. Cannot update this instance with the given json object.");
        }
    }

    /**
     * Checks whether the given json object is a valid source to update this instance.
     *
     * @param jsonObject json object to check
     * @return is a valid source
     */
    protected boolean isValidJsonSource(JsonObject jsonObject) {
        String name = getIdKey().getName();
        return jsonObject.hasKey(name) && Objects.equals(jsonObject.getString(name), getId());
    }

    /**
     * Updates this instance based on the given json object. Does not check prior, if the source object is a valid
     * source (use {@link #updateFromJson(JsonObject)} in that case).
     * <p/>
     * Each key will be mapped with the json object. For
     * each key (when the value shall be written, see details below), the value will either be converted by
     * the key's {@link JsonPropertyConverter} or the {@link JsonUtils}. Json arrays will be converted
     * to {@link ArrayList}.
     * <p></p>
     * The changes applied will not lead to a "dirty" object. For details see
     * {@link #readJson(JsonObject, boolean)}.
     *
     * @param jsonObject json object to read
     */
    public void readJson(@NotNull JsonObject jsonObject) {
        readJson(jsonObject, false);
    }

    /**
     * Updates this instance based on the given json object. Does not check prior, if the source object is a valid
     * source (use {@link #updateFromJson(JsonObject)} in that case).
     * <p/>
     * Each key will be mapped with the json object. The keys are obtained from {@link #getKeys()}. For
     * each key , the value will either be converted by
     * the key's {@link JsonPropertyConverter} or the {@link JsonUtils}. Json arrays will be converted
     * to {@link ArrayList}.
     * <p></p>
     * The boolean parameter indicates, if changes written to this instance shall be marked
     * as dirty / changed properties. This can lead to additional load of data sent to the client
     * on the next update. If false, then any property, that was marked as dirty before and is now
     * overridden by the given json object data, will be reset to "not dirty".
     *
     * @param jsonObject         json object to read
     * @param markAppliedAsDirty applied changes shall be marked as dirty (changed) values
     */
    public void readJson(@NotNull JsonObject jsonObject, boolean markAppliedAsDirty) {
        Objects.requireNonNull(jsonObject);

        Set<Key> keys = getKeys();
        for (Key key : keys) {
            readJsonValue(jsonObject, key, markAppliedAsDirty);
        }
    }

    /**
     * Reads the json value with the given key. When the value shall be written, see details below, the value will
     * either be converted by the key's {@link JsonPropertyConverter} or the {@link JsonUtils}.
     * Json arrays will be converted to {@link ArrayList}.
     * <p></p>
     * The boolean parameter indicates, if changes written to this instance shall be marked
     * as dirty / changed properties. This can lead to additional load of data sent to the client
     * on the next update. If false, then any property, that was marked as dirty before and is now
     * overridden by the given json object data, will be reset to "not dirty".
     *
     * @param jsonObject         json object to read
     * @param key                key to obtain
     * @param markAppliedAsDirty applied changes shall be marked as dirty (changed) values
     */
    protected void readJsonValue(@NotNull JsonObject jsonObject,
                                 @NotNull JsonItem.Key key,
                                 boolean markAppliedAsDirty) {

        Objects.requireNonNull(jsonObject);
        Objects.requireNonNull(key);

        if (key.isUpdateFromClientAllowed() && jsonObject.hasKey(key.getName())) {
            Object value = convertJsonValueToObject(jsonObject, key);
            if (markAppliedAsDirty) {
                set(key, value);
            } else {
                clearDirtyState(key);  // if there was some manual change before, that is now overridden,
                // so we prevent accidental resend of data to the client
                setWithoutDirtyChange(key, value);
            }
        }
    }

    /**
     * Converts the json property mapped by the given key in the json object to an Object by either using
     * the key's converter or passing the json value to {@link #convertJsonValueToObjectWithJsonUtils(JsonValue, Key)}.
     *
     * @param jsonObject json object
     * @param key        key to read
     * @return converted value
     */
    @SuppressWarnings("rawtypes")
    protected Object convertJsonValueToObject(JsonObject jsonObject, Key key) {
        Object value;
        JsonValue jsonValue = jsonObject.get(key.getName());
        value = Optional.ofNullable(key.getConverter())
                .map(c -> ((JsonPropertyConverter) c).ofJsonValue(jsonValue, this))
                .orElseGet(() -> convertJsonValueToObjectWithJsonUtils(jsonValue, key));
        return value;
    }

    /**
     * This method is used by {@link #convertJsonValueToObject(JsonObject, Key)}. It utilizes
     * the {@link JsonUtils} to convert the given json value to an Object. By default the methods
     * {@link JsonUtils#ofJsonValue(JsonValue, Class)} or
     * {@link JsonUtils#ofJsonValue(JsonValue, SerializableFunction, Collection, Class)} are called.
     * <p/>
     * The intention of this method is to allow easy change of the used {@link JsonUtils#ofJsonValue} method for
     * a custom json array conversion type.
     * <p/>
     * This method should not do anything else.
     *
     * @param jsonValue value to convert
     * @param key       current key
     * @return converted value
     */
    @SuppressWarnings("rawtypes")
    protected Object convertJsonValueToObjectWithJsonUtils(JsonValue jsonValue, Key key) {
        Optional<SerializableFunction<JsonValue, Object>> jsonObjectConverter = Optional.ofNullable(key.getJsonToObjectConverter());
        Class<? extends Collection> convertArrayToType = key.getJsonArrayToCollectionConversionType();

        return jsonObjectConverter.isPresent()
                ? JsonUtils.ofJsonValue(jsonValue, jsonObjectConverter.get(), key.getJsonObjectToConverterTypes(), convertArrayToType)
                : JsonUtils.ofJsonValue(jsonValue, convertArrayToType);
    }

    /**
     * Returns the map of the custom properties of this instance. This map is editable and any changes
     * will be sent to the client using {@link FullCalendar#updateEntry(Entry)}.
     * <p></p>
     * Might be null.
     * <p></p>
     * Be aware, that any non standard property you
     * set via "set(..., ...)" is not automatically put into this map, but this is done by the client later.
     *
     * @return Map
     * @see #getCustomPropertiesOrEmpty()
     * @see #getOrCreateCustomProperties()
     */
    public Map<String, Object> getCustomProperties() {
        return get(getCustomPropertiesKey());
    }

    /**
     * Returns the key to be used to assign custom properties. Throws an {@link UnsupportedOperationException} by
     * default. Only necessary to be overridden, when custom properties shall be usable.
     *
     * @return custom properties key.
     */
    protected Key getCustomPropertiesKey() {
        throw new UnsupportedOperationException("Override getCustomPropertiesKey to use custom properties.");
    }

    /**
     * Returns the custom properties map or an empty one, if none has yet been created. The map is not writable.
     * <p></p>
     * Be aware, that any non standard property you
     * set via "set(..., ...)" is not automatically put into this map, but this is done by the client later.
     *
     * @return map
     * @see #getCustomProperties()
     * @see #getOrCreateCustomProperties()
     */
    public Map<String, Object> getCustomPropertiesOrEmpty() {
        Map<String, Object> map = get(getCustomPropertiesKey());
        return map != null ? Collections.unmodifiableMap(map) : Collections.emptyMap();
    }

    /**
     * Returns the map of the custom properties of this instance. This map is editable and any changes
     * will be sent to the client using {@link FullCalendar#updateEntry(Entry)}.
     * <p/>
     * Creates and registers a new map, if none is there yet.
     * <p></p>
     * Be aware, that any non standard property you
     * set via "set(..., ...)" is not automatically put into this map, but this is done by the client later.
     *
     * @return Map
     * @see #getCustomPropertiesOrEmpty()
     * @see #getCustomProperties()
     */
    public Map<String, Object> getOrCreateCustomProperties() {
        Map<String, Object> map = get(getCustomPropertiesKey());
        if (map == null) {
            map = new HashMap<>();
            setCustomProperties(map);
        }
        return map;
    }

    /**
     * Sets custom properties. These will be passed as they are into the client side object as "extendedProps". Can
     * be used for custom event rendering.
     *
     * @param customProperties custom properties
     */
    public void setCustomProperties(Map<String, Object> customProperties) {
        set(getCustomPropertiesKey(), customProperties);
    }

    /**
     * Sets custom property for this entry. An existing property will be overwritten.
     *
     * @param key   the name of the property to set
     * @param value value to set
     */
    public void setCustomProperty(@NotNull String key, Object value) {
        Objects.requireNonNull(key);
        getOrCreateCustomProperties().put(key, value);
    }

    /**
     * Returns a custom property (or null if not defined).
     *
     * @param key name of the custom property
     * @param <T> return type
     * @return custom property value or null
     */
    @SuppressWarnings("unchecked")
    public <T> T getCustomProperty(@NotNull String key) {
        return (T) getCustomPropertiesOrEmpty().get(key);
    }

    /**
     * Remove the custom property based on the name.
     *
     * @param key the name of the property to remove
     */
    public void removeCustomProperty(@NotNull String key) {
        Map<String, Object> customProperties = getCustomProperties();
        if (customProperties != null) {
            // FIXME this will currently not remove the custom property from the client side!
            customProperties.remove(Objects.requireNonNull(key));
        }
    }

    /**
     * Remove specific custom property where the name and value match.
     *
     * @param key   the name of the property to remove
     * @param value the object to remove
     */
    public void removeCustomProperty(@NotNull String key, @NotNull Object value) {
        Map<String, Object> customProperties = getCustomProperties();
        if (customProperties != null) {
            // FIXME this will currently not remove the custom property from the client side!
            customProperties.remove(Objects.requireNonNull(key), Objects.requireNonNull(value));
        }
    }

    /**
     * Creates a copy of this instance. Unset properties stay uninitialized.
     *
     * @param <T> return type
     * @return copy
     */
    public <T extends JsonItem> T copy() {
        return copy(false);
    }

    /**
     * Creates a copy of this instance. Unset properties are initialized with a null value, when the boolean
     * parameter is set to true. Otherwise they will not be initialized.
     *
     * @param initializeUnsetProperties initialize unset properties
     * @param <T>                       return type
     * @return copy
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends JsonItem> T copy(boolean initializeUnsetProperties) {
        try {
            T copy = (T) getClass().newInstance();

            Set<Key> keys = getKeys();
            for (Key key : keys) {
                if (initializeUnsetProperties || has(key)) {
                    Object value = get(key);

                    if (value instanceof Iterable) {
                        value = copyIterable((Iterable) value, initializeUnsetProperties);
                    } else if (value instanceof Object[]) {
                        value = copyObjectArray((Object[]) value, initializeUnsetProperties);
                    }

                    copy.setWithoutDirtyChange(key, value);
                }
            }
            return copy;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected Iterable<Object> copyIterable(Iterable<Object> iterable, boolean writeUnsetProperties) {
        System.err.println("Iterable not yet implemented for deep copy");
        return iterable;
    }

    @SuppressWarnings("unchecked")
    protected Object[] copyObjectArray(Object[] array, boolean writeUnsetProperties) {
        Object[] newArray = new Object[array.length];
        for (int i = 0; i < array.length; i++) {
            Object value = array[i];

            if (value instanceof Iterable) {
                value = copyIterable((Iterable<Object>) value, writeUnsetProperties);
            } else if (value instanceof Object[]) {
                value = copyObjectArray((Object[]) value, writeUnsetProperties);
            }

            newArray[i] = value;
        }

        return newArray;
    }

    /**
     * Manually marks the given keys as "changed" without changing the value itself. This should be used,
     * if a property itself does not change, but is related to another property, that has changed.
     * @param keys keys to be marked as changed
     */
    public void markAsChangedProperty(Key... keys) {
        changedProperties.addAll(Arrays.asList(keys));
    }

    /**
     * Manually marks the given keys, where {@link #has(Key)} returns true, as "changed" without changing the value
     * itself. This should be used, if a property itself does not change, but is related to another property, that has changed.
     * @param keys keys to be marked as changed
     */

    public void markAsChangedPropertyWhenDefined(Key... keys) {
        for (Key key : keys) {
            if (has(key)) {
                changedProperties.add(key);
            }
        }
    }


    /**
     * @param <SERVER_TYPE>
     */
    public interface JsonPropertyConverter<SERVER_TYPE, T extends JsonItem> {
        JsonValue toJsonValue(SERVER_TYPE serverValue, T currentInstance);

        default SERVER_TYPE ofJsonValue(JsonValue clientValue, T currentInstance) {
            throw new UnsupportedOperationException("Conversion from client to server not implemented or supported");
        }
    }


    @Override
    public String toString() {
        final JsonObject jsonObject = Json.createObject();
        writeValuesToJson(jsonObject, false);
        return jsonObject.toString();
    }

    /**
     * Checks for equality by type and id.
     *
     * @param o object to check
     * @return equals
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entry entry = (Entry) o;
        return Objects.equals(getId(), entry.getId());
    }

    /**
     * Returns the hashcode of the id.
     *
     * @return hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Getter
    @RequiredArgsConstructor
    @EqualsAndHashCode(of = "name")
    @Builder
    public static class Key {
        /**
         * The name of the key. Must be unique inside its using scope.
         */
        private final String name;

        /**
         * Defines the allowed type for a property. Currently not used, might be removed later.
         */
        @Deprecated
        private final Class<?> allowedType; // TODO needed?

        /**
         * Sets a default value, that shall be send to the client or returned by the {@link JsonItem#get(Key)}
         * method, if the key is not initialized. Will not be used, when the key is initialized with null.
         * <p/>
         * Having a default value means, that this property will always be initialized or send to the client
         * for a new object.
         */
        private final Object defaultValue;

        /**
         * A general converter that should be applied, when a value for this key is converted from and to the
         * model. This converter is responsible for converting the whole object to its counter part (Java to Json
         * and vice versa).
         * <p></p>
         * If no converter is passed, then the keys will be converted using the JsonUtils and,
         * if necessary to update the server side part from the client, the {@link #jsonArrayToCollectionConversionType}
         * and {@link #jsonToObjectConverter}.
         */
        private final JsonPropertyConverter<?, ?> converter;

        /**
         * A convenience converter that can be applied, when a value for this key is a collectable (see
         * {@link JsonUtils#isCollectable(Object)}, that is to be
         * converted from the server side model. Other than the mode generalized {@link #converter}, this one will be
         * applied to every item of the mentioned collection. This allows to easily jsonify a collection of complex
         * objects to a client side readable version.
         * <p></p>
         * Please note, that this converter will only be applied to any object, that is not a collectable itself (those
         * will be traversed recursively and then call this converter) or basic objects, that are already handled
         * by the JsonUtils like {@link ClientSideValue}, {@link JsonValue}, {@link Boolean} or {@link Number} (plus
         * maybe others).
         * <p></p>
         * There is currently no version of that converter for client to server. That will instead be handled
         * by the {@link #jsonToObjectConverter}. Extend the {@link #jsonObjectToConverterTypes} to handle
         * any type you need to convert beside a normal JsonObject.
         *
         * @see JsonUtils#isCollectable(Object)
         */
        private final SerializableFunction<Object, JsonValue> collectableItemConverter;

        /**
         * Indicates, if this property might be changed from the client side in general. This could be for instance
         * the case on start and end time, but not the id.
         */
        private final boolean updateFromClientAllowed;

        /**
         * When defined, a json array coming from the client will be converted to this type. If not set,
         * an array will be converted to a list.
         * Only needed, when no converter is defined.
         */
        @SuppressWarnings("rawtypes")
        private final Class<? extends Collection> jsonArrayToCollectionConversionType;

        /**
         * Only needed, when no general json property converter is defined.
         * <p></p>
         * When defined, a json value coming from the client will be converter using this converter. By default
         * this will be applied to JsonObjects only. If not set, any json object will throw an exception.
         * To apply this converter to other json types, set the needed types into {@link #jsonObjectToConverterTypes}.
         *
         * @see #jsonObjectToConverterTypes
         * @see #converter
         */
        private final SerializableFunction<JsonValue, Object> jsonToObjectConverter;

        /**
         * Defines a set of json types for which the {@link #jsonToObjectConverter} shall be applied.
         * For {@link JsonType#OBJECT} the converter will always be applied. {@link JsonType#ARRAY}
         * will always be ignored. Be aware of not creating infinite loops or similar.
         */
        private final Set<JsonType> jsonObjectToConverterTypes;

        /**
         * When set, the value must not be null.
         */
        private final boolean nonNull;

        /**
         * Reads all static {@link Key} fields of the given type, including inherited and/or non public ones and returns
         * it as an unmodifiable set.
         *
         * @param type type to scan
         * @return set of keys
         * @throws DuplicateKeyException if any key or json key is registered twice
         */
        protected static Set<Key> readAndRegisterKeysAsUnmodifiable(Class<?> type) {
            return Collections.unmodifiableSet(readAndRegisterKeys(type));
        }

        /**
         * Reads all static {@link Key} fields of the given type, including inherited and/or non public ones.
         *
         * @param type type to scan
         * @return set of keys
         * @throws DuplicateKeyException if any key or json key is registered twice
         */
        protected static Set<Key> readAndRegisterKeys(Class<?> type) {
            List<Key> result = new ArrayList<>();

            while (type != Object.class) {
                result.addAll(readStaticKeyFields(type));
                type = type.getSuperclass();
            }

            // check for any duplicate keys
            Set<Key> keys = new HashSet<>();
            Set<String> duplicateValues = new HashSet<>();

            for (Key key : result) {
                if (!keys.add(key)) {
                    duplicateValues.add(key.getName());
                }
            }

            if (!duplicateValues.isEmpty()) {
                throw new DuplicateKeyException(duplicateValues);
            }

            return keys;
        }

        /**
         * Reads all static fields from the given type, that have Key as type.
         *
         * @param type class to read fields from
         * @return set of keys
         */
        private static List<Key> readStaticKeyFields(Class<?> type) {
            return Arrays.stream(type.getDeclaredFields())
                    .map(Key::readStaticField)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        private static Key readStaticField(Field f) {
            try {
                return (Key) FieldUtils.readStaticField(f, true);
            } catch (Exception e) {
                throw new ExceptionInInitializerError(e);
            }
        }

    }

    /**
     * Thrown, when {@link Key#readAndRegisterKeys(Class)} finds any duplicate keys.
     */
    @Getter
    public static class DuplicateKeyException extends RuntimeException {
        private final Set<String> duplicateKeys;

        public DuplicateKeyException(Set<String> duplicateKeys) {
            this.duplicateKeys = duplicateKeys;
        }
    }
}
