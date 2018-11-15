package org.vaadin.stefan.fullcalendar;

import elemental.json.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class JsonUtilsTest {

    @Test
    void testToJsonWithString() {
        JsonValue value = JsonUtils.toJsonValue("test");

        Assertions.assertTrue(value instanceof JsonString);
        Assertions.assertEquals("test", value.asString());
    }

    @Test
    void testToJsonWithJsonValue() {
        JsonString someTest = Json.create("test");

        Assertions.assertSame(someTest, JsonUtils.toJsonValue(someTest));
    }

    @Test
    void testToJsonWithNull() {
        JsonValue value = JsonUtils.toJsonValue(null);

        Assertions.assertTrue(value instanceof JsonNull);
    }

    @Test
    void testToJsonWithBoolean() {
        JsonValue value = JsonUtils.toJsonValue(true);

        Assertions.assertTrue(value instanceof JsonBoolean);
        Assertions.assertTrue(value.asBoolean());

    }

    @Test
    void testToJsonWithNumber() {
        JsonValue value = JsonUtils.toJsonValue(1);

        Assertions.assertTrue(value instanceof JsonNumber);
        Assertions.assertEquals(1, value.asNumber());

        value = JsonUtils.toJsonValue(1.0);

        Assertions.assertTrue(value instanceof JsonNumber);
        Assertions.assertEquals(1.0, value.asNumber());

        value = JsonUtils.toJsonValue(1.0f);

        Assertions.assertTrue(value instanceof JsonNumber);
        Assertions.assertEquals(1.0f, value.asNumber());

        value = JsonUtils.toJsonValue(1L);

        Assertions.assertTrue(value instanceof JsonNumber);
        Assertions.assertEquals(1L, value.asNumber());
    }

    @Test
    void testToJsonWithIterator() {
        JsonValue jsonValue;

        List<Object> source = Arrays.asList(null, null, null);
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source.iterator()), JsonNull.class, v -> null);

        source = Arrays.asList("1", "2", "3");
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source.iterator()), JsonString.class, JsonValue::asString);

        source = Arrays.asList(1, 2, 3);
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source.iterator()), JsonNumber.class, v -> (int) v.asNumber());

        source = Arrays.asList(1.0, 2.0, 3.0);
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source.iterator()), JsonNumber.class, JsonValue::asNumber);

        source = Arrays.asList(true, false, true);
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source.iterator()), JsonBoolean.class, JsonValue::asBoolean);

        Assertions.assertTrue(ClientSideValue.class.isAssignableFrom(CalendarViewImpl.class)); // prevent implemention change
        List<CalendarViewImpl> eSource = Arrays.asList(CalendarViewImpl.values());

        assertEqualArray((Iterator) eSource.stream().map(ClientSideValue::getClientSideValue).iterator(), JsonUtils.toJsonValue(eSource.iterator()), JsonString.class, JsonValue::asString);
    }

    private <T extends JsonValue> void assertEqualArray(Iterator<Object> source, JsonValue converted, Class<T> type, Function<JsonValue, Object> convert) {
        Assertions.assertTrue(converted instanceof JsonArray);

        JsonArray array = (JsonArray) converted;

        int i = 0;
        while (source.hasNext()) {
            Object next = source.next();
            JsonValue actual = array.get(i);
            Assertions.assertTrue(type.isAssignableFrom(actual.getClass()));
            Assertions.assertEquals(next, convert.apply(actual));

            i++;
        }

        Assertions.assertFalse(source.hasNext());
        Assertions.assertEquals(i, ((JsonArray) converted).length() );
    }

    @Test
    void testToJsonWithIterable() {
        List<Object> source = Arrays.asList(null, null, null);
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source), JsonNull.class, v -> null);

        source = Arrays.asList("1", "2", "3");
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source), JsonString.class, JsonValue::asString);

        source = Arrays.asList(1, 2, 3);
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source), JsonNumber.class, v -> (int) v.asNumber());

        source = Arrays.asList(1.0, 2.0, 3.0);
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source), JsonNumber.class, JsonValue::asNumber);

        source = Arrays.asList(true, false, true);
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source), JsonBoolean.class, JsonValue::asBoolean);


        Assertions.assertTrue(ClientSideValue.class.isAssignableFrom(CalendarViewImpl.class)); // prevent implemention change
        List<CalendarViewImpl> eSource = Arrays.asList(CalendarViewImpl.values());

        assertEqualArray((Iterator) eSource.stream().map(ClientSideValue::getClientSideValue).iterator(), JsonUtils.toJsonValue(eSource), JsonString.class, JsonValue::asString);

    }

    @Test
    void testToJsonWithStream() {
        List<Object> source = Arrays.asList(null, null, null);
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source.stream()), JsonNull.class, v -> null);

        source = Arrays.asList("1", "2", "3");
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source.stream()), JsonString.class, JsonValue::asString);

        source = Arrays.asList(1, 2, 3);
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source.stream()), JsonNumber.class, v -> (int) v.asNumber());

        source = Arrays.asList(1.0, 2.0, 3.0);
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source.stream()), JsonNumber.class, JsonValue::asNumber);

        source = Arrays.asList(true, false, true);
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source.stream()), JsonBoolean.class, JsonValue::asBoolean);


        Assertions.assertTrue(ClientSideValue.class.isAssignableFrom(CalendarViewImpl.class)); // prevent implemention change
        List<CalendarViewImpl> eSource = Arrays.asList(CalendarViewImpl.values());

        assertEqualArray((Iterator) eSource.stream().map(ClientSideValue::getClientSideValue).iterator(), JsonUtils.toJsonValue(eSource.stream()), JsonString.class, JsonValue::asString);
    }

    @Test
    void testToJsonWithArray() {
        List<Object> source = Arrays.asList(null, null, null);
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source.toArray()), JsonNull.class, v -> null);

        source = Arrays.asList("1", "2", "3");
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source.toArray()), JsonString.class, JsonValue::asString);

        source = Arrays.asList(1, 2, 3);
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source.toArray()), JsonNumber.class, v -> (int) v.asNumber());

        source = Arrays.asList(1.0, 2.0, 3.0);
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source.toArray()), JsonNumber.class, JsonValue::asNumber);

        source = Arrays.asList(true, false, true);
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source.toArray()), JsonBoolean.class, JsonValue::asBoolean);


        Assertions.assertTrue(ClientSideValue.class.isAssignableFrom(CalendarViewImpl.class)); // prevent implemention change
        List<CalendarViewImpl> eSource = Arrays.asList(CalendarViewImpl.values());

        assertEqualArray((Iterator) eSource.stream().map(ClientSideValue::getClientSideValue).iterator(), JsonUtils.toJsonValue(eSource.toArray()), JsonString.class, JsonValue::asString);
    }

    @Test
    void testStringPropertyUpdate() {
        JsonObject object = Json.createObject();
        object.put("title", "test");

        Entry entry = new Entry();
        JsonUtils.updateString(object, "title", entry::setTitle);

        Assertions.assertEquals("test", entry.getTitle() );
    }

    @Test
    void testBooleanPropertyUpdate() {
        JsonObject object = Json.createObject();
        object.put("allDay", true);

        Entry entry = new Entry();
        JsonUtils.updateBoolean(object, "allDay", entry::setAllDay);

        Assertions.assertEquals(true, entry.isAllDay() );

    }

    @Test
    void testLocalDatePropertyUpdate() {
        LocalDateTime now = LocalDateTime.now();

        JsonObject object = Json.createObject();
        object.put("date", now.toString());

        Entry entry = new Entry();
        JsonUtils.updateDateTime(object, "date", entry::setStart, Timezone.UTC);

        Assertions.assertEquals(now, entry.getStart());

    }

    @Test
    void testLocalDateTimePropertyUpdate() {
        LocalDate now = LocalDateTime.now().toLocalDate();

        JsonObject object = Json.createObject();
        object.put("date", now.toString());

        Entry entry = new Entry();
        JsonUtils.updateDateTime(object, "date", entry::setStart, Timezone.UTC);

        Assertions.assertEquals(now.atStartOfDay(), entry.getStart());
    }
}
