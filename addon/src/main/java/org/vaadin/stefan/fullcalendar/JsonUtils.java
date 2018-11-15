/*
 * Copyright 2018, Stefan Uebe
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
     * Reads the json property by key and tries to apply it as a temporal. Might use the timezone, if conversion to UTC is needed.
     * @param object json object
     * @param key json property key
     * @param setter setter to apply value
     * @param timezone timezone
     */
    public static void updateDateTime(JsonObject object, String key, Consumer<Instant> setter, Timezone timezone) {
        if (object.get(key) instanceof JsonString) {
            Instant dateTime = parseDateTimeString(object.getString(key), timezone);

            setter.accept(dateTime);
        }
    }

    /**
     * Parses a date time string sent from the client side. This string may apply to ZonedDateTime, Instant, LocalDate
     * or LocalDateTime default parsers. The resulting temporal will be UTC based.
     * @param dateTimeString date time string
     * @param timezone timezone (might not be necessary)
     * @return UTC based date time instance
     */
    public static Instant parseDateTimeString(String dateTimeString, Timezone timezone) {
        Instant dateTime;

        try {
            ZonedDateTime parse = ZonedDateTime.parse(dateTimeString);
            dateTime = parse.toInstant();
        } catch (DateTimeParseException e) {
            try {
                dateTime = Instant.parse(dateTimeString);
            } catch (DateTimeParseException e1) {
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
