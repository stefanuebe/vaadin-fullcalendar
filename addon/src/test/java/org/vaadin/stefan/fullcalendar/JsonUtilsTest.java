package org.vaadin.stefan.fullcalendar;

import elemental.json.*;
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
        JsonValue value = JsonUtils.toJsonNode("test");

        Assertions.assertTrue(value instanceof JsonString);
        Assertions.assertEquals("test", value.asString());
    }

    @Test
    void testToJsonWithJsonValue() {
        JsonString someTest = Json.create("test");

        Assertions.assertSame(someTest, JsonUtils.toJsonNode(someTest));
    }

    @Test
    void testToJsonWithNull() {
        JsonValue value = JsonUtils.toJsonNode(null);

        Assertions.assertTrue(value instanceof JsonNull);
    }

    @Test
    void testToJsonWithBoolean() {
        JsonValue value = JsonUtils.toJsonNode(true);

        Assertions.assertTrue(value instanceof JsonBoolean);
        Assertions.assertTrue(value.asBoolean());

    }

    @Test
    void testToJsonWithNumber() {
        JsonValue value = JsonUtils.toJsonNode(1);

        Assertions.assertTrue(value instanceof JsonNumber);
        Assertions.assertEquals(1, value.asNumber());

        value = JsonUtils.toJsonNode(1.0);

        Assertions.assertTrue(value instanceof JsonNumber);
        Assertions.assertEquals(1.0, value.asNumber());

        value = JsonUtils.toJsonNode(1.0f);

        Assertions.assertTrue(value instanceof JsonNumber);
        Assertions.assertEquals(1.0f, value.asNumber());

        value = JsonUtils.toJsonNode(1L);

        Assertions.assertTrue(value instanceof JsonNumber);
        Assertions.assertEquals(1L, value.asNumber());
    }

    @Test
    void testToJsonWithIterator() {
        JsonValue jsonValue;

        List<Object> source = Arrays.asList(null, null, null);
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source.iterator()), JsonNull.class, v -> null);

        source = Arrays.asList("1", "2", "3");
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source.iterator()), JsonString.class, JsonValue::asString);

        source = Arrays.asList(1, 2, 3);
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source.iterator()), JsonNumber.class, v -> (int) v.asNumber());

        source = Arrays.asList(1.0, 2.0, 3.0);
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source.iterator()), JsonNumber.class, JsonValue::asNumber);

        source = Arrays.asList(true, false, true);
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source.iterator()), JsonBoolean.class, JsonValue::asBoolean);

        Assertions.assertTrue(ClientSideValue.class.isAssignableFrom(CalendarViewImpl.class)); // prevent implemention change
        List<CalendarViewImpl> eSource = Arrays.asList(CalendarViewImpl.values());

        assertEqualArray((Iterator) eSource.stream().map(ClientSideValue::getClientSideValue).iterator(), JsonUtils.toJsonNode(eSource.iterator()), JsonString.class, JsonValue::asString);
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
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source), JsonNull.class, v -> null);

        source = Arrays.asList("1", "2", "3");
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source), JsonString.class, JsonValue::asString);

        source = Arrays.asList(1, 2, 3);
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source), JsonNumber.class, v -> (int) v.asNumber());

        source = Arrays.asList(1.0, 2.0, 3.0);
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source), JsonNumber.class, JsonValue::asNumber);

        source = Arrays.asList(true, false, true);
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source), JsonBoolean.class, JsonValue::asBoolean);


        Assertions.assertTrue(ClientSideValue.class.isAssignableFrom(CalendarViewImpl.class)); // prevent implemention change
        List<CalendarViewImpl> eSource = Arrays.asList(CalendarViewImpl.values());

        assertEqualArray((Iterator) eSource.stream().map(ClientSideValue::getClientSideValue).iterator(), JsonUtils.toJsonNode(eSource), JsonString.class, JsonValue::asString);

    }

    @Test
    void testToJsonWithStream() {
        List<Object> source = Arrays.asList(null, null, null);
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source.stream()), JsonNull.class, v -> null);

        source = Arrays.asList("1", "2", "3");
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source.stream()), JsonString.class, JsonValue::asString);

        source = Arrays.asList(1, 2, 3);
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source.stream()), JsonNumber.class, v -> (int) v.asNumber());

        source = Arrays.asList(1.0, 2.0, 3.0);
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source.stream()), JsonNumber.class, JsonValue::asNumber);

        source = Arrays.asList(true, false, true);
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source.stream()), JsonBoolean.class, JsonValue::asBoolean);


        Assertions.assertTrue(ClientSideValue.class.isAssignableFrom(CalendarViewImpl.class)); // prevent implemention change
        List<CalendarViewImpl> eSource = Arrays.asList(CalendarViewImpl.values());

        assertEqualArray((Iterator) eSource.stream().map(ClientSideValue::getClientSideValue).iterator(), JsonUtils.toJsonNode(eSource.stream()), JsonString.class, JsonValue::asString);
    }

    @Test
    void testToJsonWithArray() {
        List<Object> source = Arrays.asList(null, null, null);
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source.toArray()), JsonNull.class, v -> null);

        source = Arrays.asList("1", "2", "3");
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source.toArray()), JsonString.class, JsonValue::asString);

        source = Arrays.asList(1, 2, 3);
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source.toArray()), JsonNumber.class, v -> (int) v.asNumber());

        source = Arrays.asList(1.0, 2.0, 3.0);
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source.toArray()), JsonNumber.class, JsonValue::asNumber);

        source = Arrays.asList(true, false, true);
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source.toArray()), JsonBoolean.class, JsonValue::asBoolean);


        Assertions.assertTrue(ClientSideValue.class.isAssignableFrom(CalendarViewImpl.class)); // prevent implemention change
        List<CalendarViewImpl> eSource = Arrays.asList(CalendarViewImpl.values());

        assertEqualArray((Iterator) eSource.stream().map(ClientSideValue::getClientSideValue).iterator(), JsonUtils.toJsonNode(eSource.toArray()), JsonString.class, JsonValue::asString);
    }

    @Test
    void testStringPropertyUpdate() {
        JsonObject object = Json.createObject();
        object.put("title", "test");

        Entry entry = new Entry();
        TestUtils.updateString(object, "title", entry::setTitle);

        Assertions.assertEquals("test", entry.getTitle() );
    }

    @Test
    void testBooleanPropertyUpdate() {
        JsonObject object = Json.createObject();
        object.put("allDay", true);

        Entry entry = new Entry();
        TestUtils.updateBoolean(object, "allDay", entry::setAllDay);

        Assertions.assertEquals(true, entry.isAllDay() );

    }

//    @Test
//    void testLocalDatePropertyUpdate() {
//        LocalDateTime now = LocalDateTime.now();
//
//        JsonObject object = Json.createObject();
//        object.put("date", now.toString());
//
//        Entry entry = new Entry();
//        TestUtils.updateDateTime(object, "date", entry::setStart, Timezone.getSystem());
//
//        Assertions.assertEquals(now, entry.getStart());
//    }
//
//    @Test
//    void testLocalDateTimePropertyUpdate() {
//        LocalDate now = LocalDateTime.now().toLocalDate();
//
//        JsonObject object = Json.createObject();
//        object.put("date", now.toString());
//
//        Entry entry = new Entry();
//        TestUtils.updateDateTime(object, "date", entry::setStartUTC, Timezone.getSystem());
//
//        Assertions.assertEquals(now.atStartOfDay(), entry.getStart());
//    }

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
