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

import elemental.json.*;

import javax.validation.constraints.NotNull;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
            Iterator<?> iterator = (Iterator) value;
            JsonArray array = Json.createArray();
            int i = 0;
            while (iterator.hasNext()) {
                array.set(i++, toJsonValue(iterator.next()));
            }
            return array;
        }
        
        if (value instanceof HashMap<?, ?>) {
        	HashMap<String, Object> hashmap = (HashMap<String, Object>) value;
        	JsonObject jsonObject = Json.createObject();
        	
        	for (Map.Entry<String, Object> prop : hashmap.entrySet()) {
            	jsonObject.put(prop.getKey(), JsonUtils.toJsonValue(prop.getValue()));
            }
            
            return jsonObject;
        }

        if (value instanceof Object[]) {
            return toJsonValue(Arrays.asList((Object[]) value).iterator());
        }

        if (value instanceof Iterable<?>) {
            return toJsonValue(((Iterable) value).iterator());
        }

        if (value instanceof Stream<?>) {
            return toJsonValue(((Stream) value).iterator());
        }

        return Json.create(String.valueOf(value));
    }

    /**
     * Reads the json property by key and tries to apply it as a string.
     *
     * @param object json object
     * @param key    json property key
     * @param setter setter to apply value
     * @throws NullPointerException when null is passed for not null parameters
     */
    public static void updateString(@NotNull JsonObject object, @NotNull String key, @NotNull Consumer<String> setter) {
        Objects.requireNonNull(object, "JsonObject");
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(setter, "setter");
        if (object.get(key) instanceof JsonString) {
            setter.accept(object.getString(key));
        }
    }

    /**
     * Reads the json property by key and tries to apply it as a boolean.
     *
     * @param object json object
     * @param key    json property key
     * @param setter setter to apply value
     * @throws NullPointerException when null is passed for not null parameters
     */
    public static void updateBoolean(@NotNull JsonObject object, @NotNull String key, @NotNull Consumer<Boolean> setter) {
        Objects.requireNonNull(object, "JsonObject");
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(setter, "setter");
        if (object.get(key) instanceof JsonBoolean) {
            setter.accept(object.getBoolean(key));
        }
    }

    /**
     * Reads the json property by key and tries to apply it as a temporal. Might use the timezone, if conversion to UTC is needed.
     *
     * @param object   json object
     * @param key      json property key
     * @param setter   setter to apply value
     * @param timezone timezone
     * @throws NullPointerException when null is passed for not null parameters
     */
    public static void updateDateTime(@NotNull JsonObject object, @NotNull String key, @NotNull Consumer<Instant> setter, @NotNull Timezone timezone) {
        Objects.requireNonNull(object, "JsonObject");
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(setter, "setter");
        Objects.requireNonNull(timezone, "timezone");
        if (object.get(key) instanceof JsonString) {
            Instant dateTime = parseDateTimeString(object.getString(key), timezone);

            setter.accept(dateTime);
        }
    }
    
    /**
     * Reads the json property by key and tries to apply it as a string.
     *
     * @param object json object
     * @param key    json property key
     * @param setter setter to apply value
     * @throws NullPointerException when null is passed for not null parameters
     */
    public static void updateHashMap(@NotNull JsonObject object, @NotNull String key, @NotNull Consumer<HashMap<String, Object>> setter) {
        Objects.requireNonNull(object, "JsonObject");
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(setter, "setter");
        if (object.get(key) instanceof JsonString) {
            setter.accept(toHashMap(object.get(key)));
        }
    }
    
    /**
     * Convert the JsonObject to HashMap
     *
     * @param object json object
     * 
     * @return HashMap<String, Object> The mapping
     */
    private static HashMap<String, Object> toHashMap(JsonObject jsonobj) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        Iterator<String> keys = Arrays.asList(jsonobj.keys()).iterator();
        
        while(keys.hasNext()) {
            String key = keys.next();
            Object value = jsonobj.get(key);
            
            if (value instanceof JsonArray)
                value = toList((JsonArray) value);
            else if (value instanceof JsonObject) 
                value = toHashMap((JsonObject) value);

            map.put(key, value);
        }
        
        return map;
    }

    /**
     * Convert the JsonArray to List
     *
     * @param array json array
     * 
     * @return List<Object The list
     */
    private static List<Object> toList(JsonArray array) {
    	List<Object> list = new ArrayList<Object>();
    	
        for(int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            
            if (value instanceof JsonArray)
                value = toList((JsonArray) value);
            else if (value instanceof JsonObject)
                value = toHashMap((JsonObject) value);
            
            list.add(value);
        }
        
        return list;
    }

    /**
     * Parses a date time string sent from the client side. This string may apply to ZonedDateTime, Instant, LocalDate
     * or LocalDateTime default parsers. The resulting temporal will be UTC based.
     * <br><br>
     * If no timezone is passed but is needed, the method will use the system's timezone.
     *
     * @param dateTimeString date time string
     * @param timezone       timezone
     * @return UTC based date time instance
     * @throws NullPointerException when null is passed for not null parameters
     */
    public static Instant parseDateTimeString(@NotNull String dateTimeString, Timezone timezone) {
        Objects.requireNonNull(dateTimeString, "dateTimeString");
        Instant dateTime;

        try {
            ZonedDateTime parse = ZonedDateTime.parse(dateTimeString);
            dateTime = parse.toInstant();
        } catch (DateTimeParseException e) {
            try {
                dateTime = Instant.parse(dateTimeString);
            } catch (DateTimeParseException e1) {
                if (timezone == null) {
                    timezone = Timezone.getSystem();
                }

                try {
                    dateTime = timezone.convertToUTC(LocalDateTime.parse(dateTimeString));
                } catch (DateTimeException e2) {
                    dateTime = timezone.convertToUTC(LocalDate.parse(dateTimeString));
                }
            }
        }
        return dateTime;
    }
}
