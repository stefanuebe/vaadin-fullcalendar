package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.*;

import java.time.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class JsonUtilsTest {

    @Test
    void testToJsonWithString() {
        JsonNode value = JsonUtils.toJsonNode("test");

        Assertions.assertTrue(value instanceof StringNode);
        Assertions.assertEquals("test", value.asString());
    }

    @Test
    void testToJsonWithJsonNode() {
        StringNode someTest = JsonFactory.create("test");

        Assertions.assertSame(someTest, JsonUtils.toJsonNode(someTest));
    }

    @Test
    void testToJsonWithNull() {
        JsonNode value = JsonUtils.toJsonNode(null);

        Assertions.assertTrue(value instanceof NullNode);
    }

    @Test
    void testToJsonWithBoolean() {
        JsonNode value = JsonUtils.toJsonNode(true);

        Assertions.assertTrue(value instanceof BooleanNode);
        Assertions.assertTrue(value.asBoolean());

    }

    @Test
    void testToJsonWithNumber() {
        JsonNode value = JsonUtils.toJsonNode(1);

        Assertions.assertTrue(value.isInt());
        Assertions.assertEquals(1, value.asInt());

        value = JsonUtils.toJsonNode(1.0);

        Assertions.assertTrue(value.isDouble());
        Assertions.assertEquals(1.0, value.asDouble());

        value = JsonUtils.toJsonNode(1L);

        Assertions.assertTrue(value.isLong());
        Assertions.assertEquals(1L, value.asLong());
    }

    @Test
    void testToJsonWithIterator() {
        List<Object> source = Arrays.asList(null, null, null);
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source.iterator()), NullNode.class, v -> null);

        source = Arrays.asList("1", "2", "3");
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source.iterator()), StringNode.class, JsonNode::asString);

        source = Arrays.asList(1, 2, 3);
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source.iterator()), IntNode.class, JsonNode::asInt);

        source = Arrays.asList(1.0, 2.0, 3.0);
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source.iterator()), DoubleNode.class, JsonNode::asDouble);

        source = Arrays.asList(true, false, true);
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source.iterator()), BooleanNode.class, JsonNode::asBoolean);

        List<CalendarViewImpl> eSource = Arrays.asList(CalendarViewImpl.values());

        assertEqualArray((Iterator) eSource.stream().map(ClientSideValue::getClientSideValue).iterator(), JsonUtils.toJsonNode(eSource.iterator()), StringNode.class, JsonNode::asString);
    }

    private <T extends JsonNode> void assertEqualArray(Iterator<Object> source, JsonNode converted, Class<T> type, Function<JsonNode, Object> convert) {
        Assertions.assertTrue(converted.isArray());

        ArrayNode array = (ArrayNode) converted;

        int i = 0;
        while (source.hasNext()) {
            Object next = source.next();
            JsonNode actual = array.get(i);
            Assertions.assertTrue(type.isAssignableFrom(actual.getClass()));
            Assertions.assertEquals(next, convert.apply(actual));

            i++;
        }

        Assertions.assertEquals(i, converted.size());
    }

    @Test
    void testToJsonWithIterable() {
        List<Object> source = Arrays.asList(null, null, null);
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source), NullNode.class, v -> null);

        source = Arrays.asList("1", "2", "3");
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source), StringNode.class, JsonNode::asString);

        source = Arrays.asList(1, 2, 3);
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source), IntNode.class, JsonNode::asInt);

        source = Arrays.asList(1.0, 2.0, 3.0);
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source), DoubleNode.class, JsonNode::asDouble);

        source = Arrays.asList(true, false, true);
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source), BooleanNode.class, JsonNode::asBoolean);


        Assertions.assertTrue(true); // prevent implemention change
        List<CalendarViewImpl> eSource = Arrays.asList(CalendarViewImpl.values());

        assertEqualArray((Iterator) eSource.stream().map(ClientSideValue::getClientSideValue).iterator(), JsonUtils.toJsonNode(eSource), StringNode.class, JsonNode::asString);

    }

    @Test
    void testToJsonWithStream() {
        List<Object> source = Arrays.asList(null, null, null);
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source.stream()), NullNode.class, v -> null);

        source = Arrays.asList("1", "2", "3");
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source.stream()), StringNode.class, JsonNode::asString);

        source = Arrays.asList(1, 2, 3);
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source.stream()), IntNode.class, JsonNode::asInt);

        source = Arrays.asList(1.0, 2.0, 3.0);
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source.stream()), DoubleNode.class, JsonNode::asDouble);

        source = Arrays.asList(true, false, true);
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source.stream()), BooleanNode.class, JsonNode::asBoolean);


        List<CalendarViewImpl> eSource = Arrays.asList(CalendarViewImpl.values());

        assertEqualArray((Iterator) eSource.stream().map(ClientSideValue::getClientSideValue).iterator(), JsonUtils.toJsonNode(eSource.stream()), StringNode.class, JsonNode::asString);
    }

    @Test
    void testToJsonWithArray() {
        List<Object> source = Arrays.asList(null, null, null);
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source.toArray()), NullNode.class, v -> null);

        source = Arrays.asList("1", "2", "3");
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source.toArray()), StringNode.class, JsonNode::asString);

        source = Arrays.asList(1, 2, 3);
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source.toArray()), IntNode.class, JsonNode::asInt);

        source = Arrays.asList(1.0, 2.0, 3.0);
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source.toArray()), DoubleNode.class, JsonNode::asDouble);

        source = Arrays.asList(true, false, true);
        assertEqualArray(source.iterator(), JsonUtils.toJsonNode(source.toArray()), BooleanNode.class, JsonNode::asBoolean);


        List<CalendarViewImpl> eSource = Arrays.asList(CalendarViewImpl.values());

        assertEqualArray((Iterator) eSource.stream().map(ClientSideValue::getClientSideValue).iterator(), JsonUtils.toJsonNode(eSource.toArray()), StringNode.class, JsonNode::asString);
    }

    @Test
    void testStringPropertyUpdate() {
        ObjectNode object = JsonFactory.createObject();
        object.put("title", "test");

        Entry entry = new Entry();
        TestUtils.updateString(object, "title", entry::setTitle);

        Assertions.assertEquals("test", entry.getTitle() );
    }

    @Test
    void testBooleanPropertyUpdate() {
        ObjectNode object = JsonFactory.createObject();
        object.put("allDay", true);

        Entry entry = new Entry();
        TestUtils.updateBoolean(object, "allDay", entry::setAllDay);

        Assertions.assertTrue(entry.isAllDay());

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
