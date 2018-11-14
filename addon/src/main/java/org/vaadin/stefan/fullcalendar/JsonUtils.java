package org.vaadin.stefan.fullcalendar;

import elemental.json.*;

import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class JsonUtils {
    private JsonUtils() {
        // noop
    }

    /**
     * Converts the given object to a json value.
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
     */
    public static void updateString(JsonObject object, String key, Consumer<String> setter) {
        if (object.get(key) instanceof JsonString) {
            setter.accept(object.getString(key));
        }
    }

    /**
     * Reads the json property by key and tries to apply it as a boolean.
     * @param object json object
     * @param key json property key
     * @param setter setter to apply value
     */
    public static void updateBoolean(JsonObject object, String key, Consumer<Boolean> setter) {
        if (object.get(key) instanceof JsonBoolean) {
            setter.accept(object.getBoolean(key));
        }
    }

    /**
     * Reads the json property by key and tries to apply it as a local date time.
     * @param object json object
     * @param key json property key
     * @param setter setter to apply value
     * @param zoneId
     */
    public static void updateDateTime(JsonObject object, String key, Consumer<Instant> setter, ZoneId zoneId) {
        if (object.get(key) instanceof JsonString) {
            String string = object.getString(key);

            // TODO to instance and zone id

            Instant dateTime;

            try {
                ZonedDateTime parse = ZonedDateTime.parse(string);
                dateTime = parse.toInstant();
            } catch (DateTimeParseException e) {
                try {
                    dateTime = Instant.parse(string);
                } catch (DateTimeParseException e1) {
                    LocalDateTime parse = LocalDateTime.parse(string);
                    ZoneOffset offset = zoneId.getRules().getOffset(parse);

                    dateTime = parse.toInstant(offset);
                    System.out.println(string);
                }
            }

            setter.accept(dateTime);
        }
    }
}
