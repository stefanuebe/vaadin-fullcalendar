package org.vaadin.stefan.fullcalendar;

import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonType;
import elemental.json.JsonValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class JsonUtilsTest {

    @Test
    void testToJsonWithString() {
        JsonValue value = JsonUtils.toJsonValue("test");

        Assertions.assertEquals(JsonType.STRING, value.getType());
        Assertions.assertEquals("test", value.asString());
    }

    @Test
    void testToJsonWithJsonValue() {
        JsonValue someTest = JsonFactory.create("test");

        Assertions.assertSame(someTest, JsonUtils.toJsonValue(someTest));
    }

    @Test
    void testToJsonWithNull() {
        JsonValue value = JsonUtils.toJsonValue(null);

        Assertions.assertEquals(JsonType.NULL, value.getType());
    }

    @Test
    void testToJsonWithBoolean() {
        JsonValue value = JsonUtils.toJsonValue(true);

        Assertions.assertEquals(JsonType.BOOLEAN, value.getType());
        Assertions.assertTrue(value.asBoolean());
    }

    @Test
    void testToJsonWithNumber() {
        JsonValue value = JsonUtils.toJsonValue(1);

        Assertions.assertEquals(JsonType.NUMBER, value.getType());
        Assertions.assertEquals(1, (int) value.asNumber());

        value = JsonUtils.toJsonValue(1.0);

        Assertions.assertEquals(JsonType.NUMBER, value.getType());
        Assertions.assertEquals(1.0, value.asNumber());

        value = JsonUtils.toJsonValue(1L);

        Assertions.assertEquals(JsonType.NUMBER, value.getType());
        Assertions.assertEquals(1L, (long) value.asNumber());
    }

    @Test
    void testToJsonWithIterator() {
        List<Object> source = Arrays.asList(null, null, null);
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source.iterator()), JsonType.NULL, v -> null);

        source = Arrays.asList("1", "2", "3");
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source.iterator()), JsonType.STRING, JsonValue::asString);

        source = Arrays.asList(1, 2, 3);
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source.iterator()), JsonType.NUMBER, v -> (int) v.asNumber());

        source = Arrays.asList(1.0, 2.0, 3.0);
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source.iterator()), JsonType.NUMBER, JsonValue::asNumber);

        source = Arrays.asList(true, false, true);
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source.iterator()), JsonType.BOOLEAN, JsonValue::asBoolean);

        List<CalendarViewImpl> eSource = Arrays.asList(CalendarViewImpl.values());

        assertEqualArray((Iterator) eSource.stream().map(ClientSideValue::getClientSideValue).iterator(), JsonUtils.toJsonValue(eSource.iterator()), JsonType.STRING, JsonValue::asString);
    }

    private void assertEqualArray(Iterator<Object> source, JsonValue converted, JsonType type, Function<JsonValue, Object> convert) {
        Assertions.assertEquals(JsonType.ARRAY, converted.getType());

        JsonArray array = (JsonArray) converted;

        int i = 0;
        while (source.hasNext()) {
            Object next = source.next();
            JsonValue actual = array.get(i);
            Assertions.assertEquals(type, actual.getType());
            Assertions.assertEquals(next, convert.apply(actual));

            i++;
        }

        Assertions.assertEquals(i, array.length());
    }

    @Test
    void testToJsonWithIterable() {
        List<Object> source = Arrays.asList(null, null, null);
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source), JsonType.NULL, v -> null);

        source = Arrays.asList("1", "2", "3");
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source), JsonType.STRING, JsonValue::asString);

        source = Arrays.asList(1, 2, 3);
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source), JsonType.NUMBER, v -> (int) v.asNumber());

        source = Arrays.asList(1.0, 2.0, 3.0);
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source), JsonType.NUMBER, JsonValue::asNumber);

        source = Arrays.asList(true, false, true);
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source), JsonType.BOOLEAN, JsonValue::asBoolean);


        Assertions.assertTrue(true); // prevent implementation change
        List<CalendarViewImpl> eSource = Arrays.asList(CalendarViewImpl.values());

        assertEqualArray((Iterator) eSource.stream().map(ClientSideValue::getClientSideValue).iterator(), JsonUtils.toJsonValue(eSource), JsonType.STRING, JsonValue::asString);
    }

    @Test
    void testToJsonWithStream() {
        List<Object> source = Arrays.asList(null, null, null);
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source.stream()), JsonType.NULL, v -> null);

        source = Arrays.asList("1", "2", "3");
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source.stream()), JsonType.STRING, JsonValue::asString);

        source = Arrays.asList(1, 2, 3);
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source.stream()), JsonType.NUMBER, v -> (int) v.asNumber());

        source = Arrays.asList(1.0, 2.0, 3.0);
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source.stream()), JsonType.NUMBER, JsonValue::asNumber);

        source = Arrays.asList(true, false, true);
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source.stream()), JsonType.BOOLEAN, JsonValue::asBoolean);


        List<CalendarViewImpl> eSource = Arrays.asList(CalendarViewImpl.values());

        assertEqualArray((Iterator) eSource.stream().map(ClientSideValue::getClientSideValue).iterator(), JsonUtils.toJsonValue(eSource.stream()), JsonType.STRING, JsonValue::asString);
    }

    @Test
    void testToJsonWithArray() {
        List<Object> source = Arrays.asList(null, null, null);
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source.toArray()), JsonType.NULL, v -> null);

        source = Arrays.asList("1", "2", "3");
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source.toArray()), JsonType.STRING, JsonValue::asString);

        source = Arrays.asList(1, 2, 3);
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source.toArray()), JsonType.NUMBER, v -> (int) v.asNumber());

        source = Arrays.asList(1.0, 2.0, 3.0);
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source.toArray()), JsonType.NUMBER, JsonValue::asNumber);

        source = Arrays.asList(true, false, true);
        assertEqualArray(source.iterator(), JsonUtils.toJsonValue(source.toArray()), JsonType.BOOLEAN, JsonValue::asBoolean);


        List<CalendarViewImpl> eSource = Arrays.asList(CalendarViewImpl.values());

        assertEqualArray((Iterator) eSource.stream().map(ClientSideValue::getClientSideValue).iterator(), JsonUtils.toJsonValue(eSource.toArray()), JsonType.STRING, JsonValue::asString);
    }

    @Test
    void testStringPropertyUpdate() {
        JsonObject object = JsonFactory.createObject();
        object.put("title", "test");

        Entry entry = new Entry();
        TestUtils.updateString(object, "title", entry::setTitle);

        Assertions.assertEquals("test", entry.getTitle());
    }

    @Test
    void testBooleanPropertyUpdate() {
        JsonObject object = JsonFactory.createObject();
        object.put("allDay", true);

        Entry entry = new Entry();
        TestUtils.updateBoolean(object, "allDay", entry::setAllDay);

        Assertions.assertTrue(entry.isAllDay());
    }

    @Test
    void testParseDateTimeString() {
        LocalDate date = LocalDate.now();
        LocalDateTime dateTime = date.atStartOfDay();

        Assertions.assertEquals(date.toString(), JsonUtils.formatClientSideDateString(date));
        Assertions.assertEquals(dateTime + "Z", JsonUtils.formatClientSideDateTimeString(date));

        Assertions.assertEquals(date.toString(), JsonUtils.formatClientSideDateString(dateTime));
        Assertions.assertEquals(dateTime + "Z", JsonUtils.formatClientSideDateTimeString(dateTime));

        Assertions.assertEquals(date, JsonUtils.parseClientSideDate(JsonUtils.formatClientSideDateString(date)));
        Assertions.assertEquals(dateTime, JsonUtils.parseClientSideDateTime(JsonUtils.formatClientSideDateTimeString(date)));

        Assertions.assertEquals(date, JsonUtils.parseClientSideDate(JsonUtils.formatClientSideDateString(dateTime)));
        Assertions.assertEquals(dateTime, JsonUtils.parseClientSideDateTime(JsonUtils.formatClientSideDateTimeString(dateTime)));
    }
}
