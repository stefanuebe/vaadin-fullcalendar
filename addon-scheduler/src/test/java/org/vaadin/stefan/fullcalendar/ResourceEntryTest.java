package org.vaadin.stefan.fullcalendar;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ResourceEntryTest {

    public static final LocalDateTime DEFAULT_START = LocalDate.of(2000, 1, 1).atStartOfDay();
    public static final LocalDateTime DEFAULT_END = DEFAULT_START.plusDays(1);
    public static final String DEFAULT_STRING = "test";
    public static final String DEFAULT_ID = DEFAULT_STRING + 1;
    public static final String DEFAULT_TITLE = DEFAULT_STRING + 2;
    public static final String DEFAULT_COLOR = DEFAULT_STRING + 3;
    public static final String DEFAULT_DESCRIPTION = DEFAULT_STRING + 4;

    @BeforeAll
    static void beforeAll() {
        TestUtils.initVaadinService(FullCalendarSchedulerTest.COMPONENT_HTMLS);
    }

    @Test
    void testNoArgsConstructor() {
        Entry entry = new ResourceEntry();

        // test id generation
        String id = entry.getId();
        Assertions.assertNotNull(id);
        Assertions.assertFalse(id.isEmpty());
        UUID.fromString(id);

        // test if is editable
        Assertions.assertTrue(entry.isEditable());
    }

    @Test
    void testIdArgConstructor() {
        Entry entry = new ResourceEntry(null);

        // test id generation
        String id = entry.getId();
        Assertions.assertNotNull(id);
        Assertions.assertFalse(id.isEmpty());
        UUID.fromString(id);

        entry = new Entry("1");
        Assertions.assertEquals("1", entry.getId());
    }

    @Test
    void testFullArgsConstructor() {
        Entry entry;

        // test optional parameters
        entry = new ResourceEntry(null, null, null, null, false, false, null, null);

        // test id generation
        String id = entry.getId();
        Assertions.assertNotNull(id);
        Assertions.assertFalse(id.isEmpty());
        UUID.fromString(id);

        // test field values after construction - all params
        entry = new ResourceEntry(DEFAULT_ID, DEFAULT_TITLE, DEFAULT_START, DEFAULT_END, true, true, DEFAULT_COLOR, DEFAULT_DESCRIPTION);
        Assertions.assertEquals(DEFAULT_ID, entry.getId());
        Assertions.assertEquals(DEFAULT_TITLE, entry.getTitle());
        Assertions.assertEquals(DEFAULT_START, entry.getStart());
        Assertions.assertEquals(DEFAULT_END, entry.getEnd());
        Assertions.assertTrue(entry.isAllDay());
        Assertions.assertTrue(entry.isEditable());
        Assertions.assertEquals(DEFAULT_COLOR, entry.getColor());
        Assertions.assertEquals(DEFAULT_DESCRIPTION, entry.getDescription());

        // test null color when set empty
        Assertions.assertNull(new ResourceEntry(null, null, null, null, false, false, "", null).getColor());
    }

    /**
     * Checks an original entry and the json based variant for equal fields, that can be changed by json.
     * @param expected expected entry
     * @param actual actual entry
     */
    static void assertFullEqualsByJsonAttributes(ResourceEntry expected, ResourceEntry actual) {
        Assertions.assertEquals(expected.getId(), actual.getId());
        Assertions.assertEquals(expected.getTitle(), actual.getTitle());
        Assertions.assertEquals(expected.getStart(), actual.getStart());
        Assertions.assertEquals(expected.getEnd(), actual.getEnd());
        Assertions.assertEquals(expected.isAllDay(), actual.isAllDay());
        Assertions.assertEquals(expected.isEditable(), actual.isEditable());
        Assertions.assertEquals(expected.getColor(), actual.getColor());
        Assertions.assertEquals(expected.getResources(), actual.getResources());
    }

    @Test
    void testToJson() {
        ResourceEntry entry = new ResourceEntry(DEFAULT_ID, DEFAULT_TITLE, DEFAULT_START, DEFAULT_END, true, true, DEFAULT_COLOR, DEFAULT_DESCRIPTION);
        Set<Resource> resources = new HashSet<>(Arrays.asList(new Resource(), new Resource(), new Resource()));
        entry.addResources(resources);

        JsonObject jsonObject = entry.toJson();

        Assertions.assertEquals(DEFAULT_ID, jsonObject.getString("id"));
        Assertions.assertEquals(DEFAULT_TITLE, jsonObject.getString("title"));
        Assertions.assertEquals(DEFAULT_START.toLocalDate().toString(), jsonObject.getString("start"));
        Assertions.assertEquals(DEFAULT_END.toLocalDate().toString(), jsonObject.getString("end"));
        Assertions.assertTrue(jsonObject.getBoolean("allDay"));
        Assertions.assertTrue(jsonObject.getBoolean("editable"));
        Assertions.assertEquals(DEFAULT_COLOR, jsonObject.getString("color"));
        Assertions.assertTrue(jsonObject.get("resourceIds") instanceof JsonArray);

        JsonArray array = jsonObject.get("resourceIds");
        Set<String> jsonResourceIds = new HashSet<>(array.length());
        for (int i = 0; i < array.length(); i++) {
            jsonResourceIds.add(array.getString(i));
        }
        Assertions.assertEquals(resources.stream().map(Resource::getId).collect(Collectors.toSet()), jsonResourceIds);
    }

    @Test
    void testUpdateResourceEntryFromJson() {
        FullCalendarScheduler calendar = new FullCalendarScheduler();
        calendar.addResource(new Resource("1", "1", null));
        calendar.addResource(new Resource("2", "2", null));
        calendar.addResource(new Resource("3", "3", null));

        ResourceEntry entry = new ResourceEntry();
        entry.setCalendar(calendar);

        JsonObject jsonObject = Json.createObject();
        jsonObject.put("id", entry.getId());

        jsonObject.put("title", DEFAULT_TITLE);
        jsonObject.put("start", DEFAULT_START.toString());
        jsonObject.put("end", DEFAULT_END.toString());
        jsonObject.put("allDay", false);
        jsonObject.put("editable", true);
        jsonObject.put("color", DEFAULT_COLOR);

        JsonArray array = Json.createArray();
        array.set(0, "1");
        array.set(1, "2");
        array.set(2, "3");
        jsonObject.put("resourceIds", array);

        jsonObject.put("description", DEFAULT_DESCRIPTION); // this should not affect the object

        entry.update(jsonObject);

        Assertions.assertEquals(jsonObject.getString("id"), entry.getId());

        Assertions.assertEquals(DEFAULT_TITLE, entry.getTitle());
        Assertions.assertFalse(entry.isAllDay());
        Assertions.assertEquals(DEFAULT_START, entry.getStart());
        Assertions.assertEquals(DEFAULT_END, entry.getEnd());
        Assertions.assertTrue(entry.isEditable());
        Assertions.assertEquals(DEFAULT_COLOR, entry.getColor());

        Assertions.assertTrue(jsonObject.get("resourceIds") instanceof JsonArray);

        array = jsonObject.get("resourceIds");
        Set<String> jsonResourceIds = new HashSet<>(array.length());
        for (int i = 0; i < array.length(); i++) {
            jsonResourceIds.add(array.getString(i));
        }
        Assertions.assertEquals(calendar.getResources().stream().map(Resource::getId).collect(Collectors.toSet()), jsonResourceIds);

        Assertions.assertNull(entry.getDescription()); // should not be affected by json
    }
}
