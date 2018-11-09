package org.vaadin.stefan.fullcalendar;

import elemental.json.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.function.Consumer;

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
        if (value instanceof ClientSideValue) {
            value = ((ClientSideValue) value).getClientSideValue();
        }

        if (value == null) {
            return Json.createNull();
        }
        if (value instanceof Boolean) {
            return Json.create((Boolean) value);
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
     */
    public static void updateDateTime(JsonObject object, String key, Consumer<LocalDateTime> setter) {
        if (object.get(key) instanceof JsonString) {
            String string = object.getString(key);

            LocalDateTime dateTime;
            try {
                dateTime = LocalDateTime.parse(string);
            } catch (DateTimeParseException e) {
                dateTime = LocalDate.parse(string).atStartOfDay();
            }

            setter.accept(dateTime);
        }
    }
}
