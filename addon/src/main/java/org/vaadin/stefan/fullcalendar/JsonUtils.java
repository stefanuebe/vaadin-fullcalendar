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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeType;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
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
    public static JsonNode toJsonNode(Object value) {
        return toJsonNode(value, null);
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
    public static JsonNode toJsonNode(Object value, SerializableFunction<Object, JsonNode> customConverter) {
        if (value instanceof JsonNode node) {
            return node;
        }

        if (value instanceof ClientSideValue clientSideValue) {
            value = clientSideValue.getClientSideValue();
        }

        if (value == null) {
            return JsonFactory.createNull();
        }
        if (value instanceof Boolean b) {
            return JsonFactory.create(b);
        }
        if (value instanceof Integer i) {
            return JsonFactory.create(i);
        }
        if (value instanceof Long l) {
            return JsonFactory.create(l);
        }
        if (value instanceof Number n) {
            return JsonFactory.create(n.doubleValue());
        }

        if (value instanceof Iterator<?> iterator) {
            var array = JsonFactory.createArray();
            while (iterator.hasNext()) {
                array.add(toJsonNode(iterator.next(), customConverter));
            }
            return array;
        }

        if (value instanceof Map<?, ?>) {
            var map = (Map<String, Object>) value;
            var jsonObject = JsonFactory.createObject();
            for (var prop : map.entrySet()) {
                jsonObject.set(prop.getKey(), toJsonNode(prop.getValue(), customConverter));
            }
            return jsonObject;
        }

        if (value instanceof Object[] objects) {
            return toJsonNode(Arrays.asList(objects).iterator(), customConverter);
        }

        if (value instanceof Iterable<?> iterable) {
            return toJsonNode(iterable.iterator(), customConverter);
        }

        if (value instanceof Stream<?> stream) {
            return toJsonNode(stream.iterator(), customConverter);
        }

        return customConverter != null ? customConverter.apply(value) : JsonFactory.create(String.valueOf(value));
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
        return switch (value) {
            case null -> null;
            case LocalTime ignored -> value + "Z";
            case LocalDateTime localDateTime -> formatClientSideTimeString(localDateTime.toLocalTime());
            default -> throw new IllegalArgumentException("Unsupported class: " + value.getClass());
        };

    }


    public static String formatClientSideDateString(Object value) {
        return switch (value) {
            case null -> null;
            case LocalDate ignored -> value.toString();
            case LocalDateTime localDateTime -> formatClientSideDateString(localDateTime.toLocalDate());
            default -> throw new IllegalArgumentException("Unsupported class: " + value.getClass());
        };

    }

    public static String formatClientSideDateTimeString(Object value) {
        return switch (value) {
            case null -> null;
            case LocalDate localDate -> formatClientSideDateTimeString(localDate.atStartOfDay());
            case LocalDateTime ignored -> value + "Z";
            default -> throw new IllegalArgumentException("Unsupported class: " + value.getClass());
        };

    }

    /**
     * Parses a date string sent from the client side.
     *
     * @return date instance
     * @throws NullPointerException when null is passed for not null parameters
     */
    public static LocalDate parseClientSideDate(String dateString) {
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
    public static LocalDateTime parseClientSideDateTime(String dateTimeString) {
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
    public static LocalTime parseClientSideTime(String timeString) {
        Objects.requireNonNull(timeString, "timeString");

        if (!timeString.endsWith("Z")) {
            throw new IllegalArgumentException("Parsing non utc time string: " + timeString);
        }

        return LocalTime.parse(timeString.substring(0, timeString.length() - 1));
    }

    /**
     * Shortcut method for {@link #ofJsonNode(JsonNode, SerializableFunction, Collection, Class)}. Reads a json value object and
     * tries to parse it to a Java object.
     * <p></p>
     * Most basic types are automatically converted. Since Json objects represent a more complex structure, the
     * given callback can be used to convert them to their Java representation. This method converts them automatically
     * into a Map, using {@link #convertObjectToMap(tools.jackson.databind.node.ObjectNode, Class)}.
     * <p></p>
     * Json arrays are automatically converted into a Java {@link ArrayList}.
     *
     * @param jsonValue value to parse
     * @param <T>       return type
     * @return parsed / converted value
     * @see #ofJsonNode(JsonNode, Class)
     * @see #ofJsonNode(JsonNode, SerializableFunction, Collection, Class)
     */
    public static <T> T ofJsonNode(JsonNode jsonValue) {
        return ofJsonNode(jsonValue, o -> convertObjectToMap(checkForObjectOrThrow(o), ArrayList.class), null, ArrayList.class);
    }

    /**
     * Shortcut method for {@link #ofJsonNode(JsonNode, SerializableFunction, Collection, Class)}. Reads a json value object and
     * tries to parse it to a Java object.
     * <p></p>
     * Most basic types are automatically converted. Since Json objects represent a more complex structure, the
     * given callback can be used to convert them to their Java representation. This method converts them automatically
     * into a Map, using {@link #convertObjectToMap(tools.jackson.databind.node.ObjectNode, Class)}.
     * <p></p>
     * Json arrays are converted to the given
     * collection type, where for each element of the json array, this method is called recursively. Please check,
     * if the given collection type may lead to eliminated duplicates (e.g. Set)
     *
     * @param jsonValue          json value to read
     * @param convertArrayToType target collection type json arrays shall be converted to.
     * @param <T>                return type
     * @return converted Java object
     * @see #ofJsonNode(JsonNode, SerializableFunction, Collection, Class)
     * @see #convertObjectToMap(tools.jackson.databind.node.ObjectNode, Class)
     */
    @SuppressWarnings("rawtypes")
    public static <T> T ofJsonNode(JsonNode jsonValue, Class<? extends Collection> convertArrayToType) {
        return ofJsonNode(jsonValue, o -> convertObjectToMap(checkForObjectOrThrow(o), convertArrayToType), null, convertArrayToType);
    }

    /**
     * Reads a json value object and tries to parse it to a Java object.
     * <p></p>
     * Most basic types are automatically converted. Since Json objects represent a more complex structure, the
     * given callback can be used to convert them to their Java representation. Please also check
     * {@link #convertObjectToMap(tools.jackson.databind.node.ObjectNode, Class)} for a simple "to Map" converter.
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
     * @see #convertObjectToMap(tools.jackson.databind.node.ObjectNode, Class)
     */
    @SuppressWarnings("unchecked")
    public static <T> T ofJsonNode(JsonNode jsonValue,
                                   SerializableFunction<JsonNode, Object> toObjectCallback,
                                   Collection<JsonNodeType> toObjectJsonTypes,
                                   @SuppressWarnings("rawtypes") Class<? extends Collection> convertArrayToType) {
        if (jsonValue == null) {
            return null;
        }

        var type = jsonValue.getNodeType();

        if (jsonValue.isObject()) {
            return (T) toObjectCallback.apply(jsonValue);
        }

        if (jsonValue.isArray() && jsonValue instanceof ArrayNode array) {
            try {
                Collection<?> collection = convertArrayToType.getConstructor().newInstance();
                for (JsonNode item : array) {
                    collection.add(JsonUtils.ofJsonNode(item, toObjectCallback, toObjectJsonTypes, convertArrayToType));
                }
                return (T) collection;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (toObjectJsonTypes != null && toObjectJsonTypes.contains(type)) {
            return (T) toObjectCallback.apply(jsonValue);
        }

        return switch (type) {
            case STRING -> (T) jsonValue.asString();
            case NUMBER -> (T) (Double) jsonValue.asDouble();
            case BOOLEAN -> (T) (Boolean) jsonValue.asBoolean();
            case NULL -> null;
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    /**
     * Simple method, that converts a json object to a map. Calls {@link #ofJsonNode(JsonNode, Class)} for each
     * read value.
     *
     * @param object             object to read
     * @param convertArrayToType target type json arrays shall be converted to
     * @return map
     */
    @SuppressWarnings({"rawtypes"})
    public static Map<String, Object> convertObjectToMap(ObjectNode object, Class<? extends Collection> convertArrayToType) {
        Map<String, Object> map = new HashMap<>();
        for (String property : object.propertyNames()) {
            map.put(property, ofJsonNode(object.get(property), convertArrayToType));
        }
        return map;
    }

    private static ObjectNode checkForObjectOrThrow(JsonNode value) {
        if (!(value instanceof ObjectNode objectNode)) {
            throw new IllegalArgumentException("Only ObjectNode) is supported. Given type is " + (value != null ? value.getNodeType() : " null"));
        }

        return objectNode;
    }

}
