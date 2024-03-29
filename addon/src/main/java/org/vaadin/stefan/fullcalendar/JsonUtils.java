/*
 * Copyright 2020, Stefan Uebe
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.function.SerializableFunction;
import elemental.json.*;

import org.vaadin.stefan.fullcalendar.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * JsonUtils used for internally handling conversion of objects sent to or received from the client side.
 */
public final class JsonUtils {

    private JsonUtils() {
        // noop
    }

    /**
     * Converts the given object to a json value. Can be null.
     *
     * @param value value
     * @return object
     */
    public static JsonValue toJsonValue(Object value) {
        return toJsonValue(value, null);
    }

    /**
     * Converts the given object to a json value. Can be null. The given custom converter is applied, when
     * there is no default conversion found for the given value. Can be null to convert it to a simple string.
     *
     * @param value value
     * @param customConverter optional custom converter
     * @return object
     */
    @SuppressWarnings("unchecked")
    public static JsonValue toJsonValue(Object value, SerializableFunction<Object, JsonValue> customConverter) {
        if (value instanceof JsonValue) {
            return (JsonValue) value;
        }

        if (value instanceof ClientSideValue) {
            value = ((ClientSideValue) value).getClientSideValue();
        }

        if (value == null) {
            return Json.createNull();
        }
        if (value instanceof Boolean) {
            return Json.create((Boolean) value);
        }
        if (value instanceof Number) {
            return Json.create(((Number) value).doubleValue());
        }

        if (value instanceof Iterator<?>) {
            Iterator<?> iterator = (Iterator<?>) value;
            JsonArray array = Json.createArray();
            int i = 0;
            while (iterator.hasNext()) {
                array.set(i++, toJsonValue(iterator.next(), customConverter));
            }
            return array;
        }

        if (value instanceof Map<?, ?>) {
            Map<String, Object> map = (Map<String, Object>) value;
            JsonObject jsonObject = Json.createObject();
            for (Map.Entry<String, Object> prop : map.entrySet()) {
                jsonObject.put(prop.getKey(), toJsonValue(prop.getValue(), customConverter));
            }
            return jsonObject;
        }

        if (value instanceof Object[]) {
            return toJsonValue(Arrays.asList((Object[]) value).iterator(), customConverter);
        }

        if (value instanceof Iterable<?>) {
            return toJsonValue(((Iterable<?>) value).iterator(), customConverter);
        }

        if (value instanceof Stream<?>) {
            return toJsonValue(((Stream<?>) value).iterator(), customConverter);
        }

        return customConverter != null ? customConverter.apply(value) : Json.create(String.valueOf(value));
    }

    /**
     * Returns true, if this value would be converted to a json array (or iterable like) for the client, like
     * any Java iterable, stream, array, map or iterator.
     *
     * @param value value
     * @return value
     */
    public static boolean isCollectable(Object value) {
        return value instanceof Iterator<?>
                || value instanceof Map<?, ?>
                || value instanceof Object[]
                || value instanceof Iterable<?>
                || value instanceof Stream<?>;
    }

    public static String formatClientSideTimeString(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof LocalTime) {
            return value + "Z";
        }

        if (value instanceof LocalDateTime) {
            return formatClientSideTimeString(((LocalDateTime) value).toLocalTime());
        }

        throw new IllegalArgumentException("Unsupported class: " + value.getClass());
    }


    public static String formatClientSideDateString(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof LocalDate) {
            return value.toString();
        }

        if (value instanceof LocalDateTime) {
            return formatClientSideDateString(((LocalDateTime) value).toLocalDate());
        }

        throw new IllegalArgumentException("Unsupported class: " + value.getClass());
    }

    public static String formatClientSideDateTimeString(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof LocalDate) {
            return formatClientSideDateTimeString(((LocalDate) value).atStartOfDay());
        }

        if (value instanceof LocalDateTime) {
            return value + "Z";
        }

        throw new IllegalArgumentException("Unsupported class: " + value.getClass());
    }

    /**
     * Parses a date string sent from the client side.
     *
     * @return date instance
     * @throws NullPointerException when null is passed for not null parameters
     */
    public static LocalDate parseClientSideDate(@NotNull String dateString) {
        Objects.requireNonNull(dateString, "dateString");

        if (dateString.length() > 10) {
            dateString = dateString.substring(0, 10);
        }

        return LocalDate.parse(dateString);
    }

    /**
     * Parses a date string sent from the client side. Will be converted to a UTC.
     *
     * @return UTC based date time instance
     * @throws NullPointerException when null is passed for not null parameters
     */
    public static LocalDateTime parseClientSideDateTime(@NotNull String dateTimeString) {
        if (dateTimeString.length() <= 10) {
            return parseClientSideDate(dateTimeString).atStartOfDay();
        }

        if (dateTimeString.endsWith("Z")) {
            return LocalDateTime.parse(dateTimeString.substring(0, dateTimeString.length() - 1));
        }

        throw new IllegalArgumentException("Parsing non utc date time string: " + dateTimeString);
    }

    /**
     * Parses a time string sent from the client side. Will be converted to a UTC.
     *
     * @return UTC based date time instance
     * @throws NullPointerException when null is passed for not null parameters
     */
    public static LocalTime parseClientSideTime(@NotNull String timeString) {
        Objects.requireNonNull(timeString, "timeString");

        if (!timeString.endsWith("Z")) {
            throw new IllegalArgumentException("Parsing non utc time string: " + timeString);
        }

        return LocalTime.parse(timeString.substring(0, timeString.length() - 1));
    }

    /**
     * Shortcut method for {@link #ofJsonValue(JsonValue, SerializableFunction, Collection, Class)}. Reads a json value object and
     * tries to parse it to a Java object.
     * <p></p>
     * Most basic types are automatically converted. Since Json objects represent a more complex structure, the
     * given callback can be used to convert them to their Java representation. This method converts them automatically
     * into a Map, using {@link #convertObjectToMap(JsonObject, Class)}.
     * <p></p>
     * Json arrays are automatically converted into a Java {@link ArrayList}.
     *
     * @param jsonValue value to parse
     * @param <T>       return type
     * @return parsed / converted value
     * @see #ofJsonValue(JsonValue, Class)
     * @see #ofJsonValue(JsonValue, SerializableFunction, Collection, Class)
     */
    public static <T> T ofJsonValue(JsonValue jsonValue) {
        return ofJsonValue(jsonValue, o -> convertObjectToMap(checkForObjectOrThrow(o), ArrayList.class), null, ArrayList.class);
    }

    /**
     * Shortcut method for {@link #ofJsonValue(JsonValue, SerializableFunction, Collection, Class)}. Reads a json value object and
     * tries to parse it to a Java object.
     * <p></p>
     * Most basic types are automatically converted. Since Json objects represent a more complex structure, the
     * given callback can be used to convert them to their Java representation. This method converts them automatically
     * into a Map, using {@link #convertObjectToMap(JsonObject, Class)}.
     * <p></p>
     * Json arrays are converted to the given
     * collection type, where for each element of the json array, this method is called recursively. Please check,
     * if the given collection type may lead to eliminated duplicates (e.g. Set)
     *
     * @param jsonValue          json value to read
     * @param convertArrayToType target collection type json arrays shall be converted to.
     * @param <T>                return type
     * @return converted Java object
     * @see #ofJsonValue(JsonValue, SerializableFunction, Collection, Class)
     * @see #convertObjectToMap(JsonObject, Class)
     */
    @SuppressWarnings("rawtypes")
    public static <T> T ofJsonValue(JsonValue jsonValue, Class<? extends Collection> convertArrayToType) {
        return ofJsonValue(jsonValue, o -> convertObjectToMap(checkForObjectOrThrow(o), convertArrayToType), null, convertArrayToType);
    }

    /**
     * Reads a json value object and tries to parse it to a Java object.
     * <p></p>
     * Most basic types are automatically converted. Since Json objects represent a more complex structure, the
     * given callback can be used to convert them to their Java representation. Please also check
     * {@link #convertObjectToMap(JsonObject, Class)} for a simple "to Map" converter.
     * <p></p>
     * Json arrays are converted to the given
     * collection type, where for each element of the json array, this method is called recursively. Please check,
     * if the given collection type may lead to eliminated duplicates (e.g. Set)
     *
     * @param jsonValue          json value to read
     * @param toObjectCallback   callback to convert json objects
     * @param toObjectJsonTypes  optional collection of additional types beside OBJECT the object converter callback shall be applied on
     * @param convertArrayToType target collection type json arrays shall be converted to.
     * @param <T>                return type
     * @return converted Java object
     * @see #convertObjectToMap(JsonObject, Class)
     */
    @SuppressWarnings("unchecked")
    public static <T> T ofJsonValue(JsonValue jsonValue,
                                    SerializableFunction<JsonValue, Object> toObjectCallback,
                                    Collection<JsonType> toObjectJsonTypes,
                                    @SuppressWarnings("rawtypes") Class<? extends Collection> convertArrayToType) {
        if (jsonValue == null) {
            return null;
        }

        JsonType type = jsonValue.getType();

        if (type == JsonType.OBJECT) {
            return (T) toObjectCallback.apply(jsonValue);
        }

        if (type == JsonType.ARRAY) {
            try {
                Collection<?> collection = convertArrayToType.newInstance();
                JsonArray array = (JsonArray) jsonValue;
                for (int i = 0; i < array.length(); i++) {
                    collection.add(JsonUtils.ofJsonValue(array.get(i), toObjectCallback, toObjectJsonTypes, convertArrayToType));
                }
                return (T) collection;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (toObjectJsonTypes != null && toObjectJsonTypes.contains(type)) {
            return (T) toObjectCallback.apply(jsonValue);
        }

        switch (type) {
            case STRING:
                return (T) jsonValue.asString();
            case NUMBER:
                return (T) (Double) jsonValue.asNumber();
            case BOOLEAN:
                return (T) (Boolean) jsonValue.asBoolean();
            case NULL:
                return null;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }

    /**
     * Simple method, that converts a json object to a map. Calls {@link #ofJsonValue(JsonValue, Class)} for each
     * read value.
     *
     * @param object             object to read
     * @param convertArrayToType target type json arrays shall be converted to
     * @return map
     */
    @SuppressWarnings({"rawtypes"})
    public static Map<String, Object> convertObjectToMap(JsonObject object, Class<? extends Collection> convertArrayToType) {
        Map<String, Object> map = new HashMap<>();
        for (String key : object.keys()) {
            map.put(key, ofJsonValue(object.get(key), convertArrayToType));
        }
        return map;
    }

    private static JsonObject checkForObjectOrThrow(JsonValue value) {
        if (!(value instanceof JsonObject)) {
            throw new IllegalArgumentException("Only JsonObject is supported. Given type is " + (value != null ? value.getType() : " null"));
        }

        return (JsonObject) value;
    }

}
