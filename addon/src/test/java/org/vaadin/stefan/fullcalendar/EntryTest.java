package org.vaadin.stefan.fullcalendar;

import elemental.json.Json;
import elemental.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class EntryTest {

    public static final LocalDateTime DEFAULT_START = LocalDate.of(2000, 1, 1).atStartOfDay();
    public static final LocalDateTime DEFAULT_END = DEFAULT_START.plusDays(1);
    public static final String DEFAULT_STRING = "test";
    public static final String DEFAULT_ID = DEFAULT_STRING + 1;
    public static final String DEFAULT_TITLE = DEFAULT_STRING + 2;
    public static final String DEFAULT_COLOR = DEFAULT_STRING + 3;
    public static final String DEFAULT_DESCRIPTION = DEFAULT_STRING + 4;

    @Test
    void testNoArgsConstructor() {
        Entry entry = new Entry();

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
        Entry entry = new Entry(null);

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
        entry = new Entry(null, null, null, null, false, false, null, null);

        // test id generation
        String id = entry.getId();
        Assertions.assertNotNull(id);
        Assertions.assertFalse(id.isEmpty());
        UUID.fromString(id);

        // test field values after construction - all params
        entry = new Entry(DEFAULT_ID, DEFAULT_TITLE, DEFAULT_START, DEFAULT_END, true, true, DEFAULT_COLOR, DEFAULT_DESCRIPTION);
        Assertions.assertEquals(DEFAULT_ID, entry.getId());
        Assertions.assertEquals(DEFAULT_TITLE, entry.getTitle());
        Assertions.assertEquals(DEFAULT_START, entry.getStart());
        Assertions.assertEquals(DEFAULT_END, entry.getEnd());
        Assertions.assertTrue(entry.isAllDay());
        Assertions.assertTrue(entry.isEditable());
        Assertions.assertEquals(DEFAULT_COLOR, entry.getColor());
        Assertions.assertEquals(DEFAULT_DESCRIPTION, entry.getDescription());

        // test null color when set empty
        Assertions.assertNull(new Entry(null, null, null, null, false, false, "", null).getColor());
    }

    @Test
    void testConstructionFromJson() {
        Entry entry;
        JsonObject jsonObject;

        LocalDateTime ref = LocalDate.now().atStartOfDay();

        // null
        JsonObject empty = Json.createObject();
        empty.put("id", Json.createNull());
        Assertions.assertThrows(IllegalArgumentException.class, () -> Entry.fromJson(empty));

        // day
        entry = new Entry("id", "title", ref, ref.plusDays(1), true, true, "color", "description");
        jsonObject = entry.toJson();

        Entry fromJson = Entry.fromJson(jsonObject);
        assertFullEqualsByJsonAttributes(entry, fromJson);

        // timed
        entry = new Entry("id", "title", ref, ref.plusHours(1), false, true, "color", "description");
        jsonObject = entry.toJson();

        fromJson = Entry.fromJson(jsonObject);
        assertFullEqualsByJsonAttributes(entry, fromJson);
    }

    /**
     * Checks an original entry and the json based variant for equal fields, that can be changed by json.
     * @param expected expected entry
     * @param actual actual entry
     */
    static void assertFullEqualsByJsonAttributes(Entry expected, Entry actual) {
        Assertions.assertEquals(expected.getId(), actual.getId());
        Assertions.assertEquals(expected.getTitle(), actual.getTitle());
        Assertions.assertEquals(expected.getStart(), actual.getStart());
        Assertions.assertEquals(expected.getEnd(), actual.getEnd());
        Assertions.assertEquals(expected.isAllDay(), actual.isAllDay());
        Assertions.assertEquals(expected.isEditable(), actual.isEditable());
        Assertions.assertEquals(expected.getColor(), actual.getColor());
    }

    @Test
    void testEqualsAndHashcodeOnlyDependOnId() {
        Entry entry = new Entry(DEFAULT_ID, null, null, null, false, false, null, null);
        Entry entry1 = new Entry(DEFAULT_ID, DEFAULT_TITLE, DEFAULT_START, DEFAULT_END, true, true, DEFAULT_COLOR, DEFAULT_DESCRIPTION);

        Assertions.assertEquals(entry, entry1);
        Assertions.assertEquals(entry.hashCode(), entry1.hashCode());

        Assertions.assertNotEquals(entry, new Entry());
        Assertions.assertNotEquals(entry.hashCode(), new Entry().hashCode());

        Entry entry2 = new Entry(null, DEFAULT_TITLE, DEFAULT_START, DEFAULT_END, true, true, DEFAULT_COLOR, DEFAULT_DESCRIPTION);
        Entry entry3 = new Entry(null, DEFAULT_TITLE, DEFAULT_START, DEFAULT_END, true, true, DEFAULT_COLOR, DEFAULT_DESCRIPTION);

        Assertions.assertNotEquals(entry2, entry3);
        Assertions.assertNotEquals(entry2.hashCode(), entry3.hashCode());
    }

    @Test
    void testToJson() {
        Entry entry = new Entry(DEFAULT_ID, DEFAULT_TITLE, DEFAULT_START, DEFAULT_END, true, true, DEFAULT_COLOR, DEFAULT_DESCRIPTION);

        JsonObject jsonObject = entry.toJson();

        Assertions.assertEquals(DEFAULT_ID, jsonObject.getString("id"));
        Assertions.assertEquals(DEFAULT_TITLE, jsonObject.getString("title"));
        Assertions.assertEquals(DEFAULT_START.toLocalDate().toString(), jsonObject.getString("start"));
        Assertions.assertEquals(DEFAULT_END.toLocalDate().toString(), jsonObject.getString("end"));
        Assertions.assertTrue(jsonObject.getBoolean("allDay"));
        Assertions.assertTrue(jsonObject.getBoolean("editable"));
        Assertions.assertEquals(DEFAULT_COLOR, jsonObject.getString("color"));
    }

    @Test
    void testIfUpdateFromJsonFailsOnNonMatchingId() {
        Entry entry = new Entry();

        JsonObject jsonObject = Json.createObject();
        jsonObject.put("id", "someNonUUID");

        Assertions.assertThrows(IllegalArgumentException.class, () -> entry.update(jsonObject));
    }

    @Test
    void testUpdateEntryFromJson() {
        Entry entry = new Entry();

        JsonObject jsonObject = Json.createObject();
        jsonObject.put("id", entry.getId());

        jsonObject.put("title", DEFAULT_TITLE);
        jsonObject.put("start", DEFAULT_START.toString());
        jsonObject.put("end", DEFAULT_END.toString());
        jsonObject.put("allDay", false);
        jsonObject.put("editable", true);
        jsonObject.put("color", DEFAULT_COLOR);

        jsonObject.put("description", DEFAULT_DESCRIPTION); // this should not affect the object

        entry.update(jsonObject);

        Assertions.assertEquals(jsonObject.getString("id"), entry.getId());

        Assertions.assertEquals(DEFAULT_TITLE, entry.getTitle());
        Assertions.assertFalse(entry.isAllDay());
        Assertions.assertEquals(DEFAULT_START, entry.getStart());
        Assertions.assertEquals(DEFAULT_END, entry.getEnd());
        Assertions.assertTrue(entry.isEditable());
        Assertions.assertEquals(DEFAULT_COLOR, entry.getColor());

        Assertions.assertNull(entry.getDescription()); // should not be affected by json
    }

    @Test
    void testUpdateAllDayEntryFromJson() {
        Entry entry = new Entry();

        JsonObject jsonObject = Json.createObject();
        jsonObject.put("id", entry.getId());

        jsonObject.put("start", DEFAULT_START.toLocalDate().toString());
        jsonObject.put("end", DEFAULT_END.toLocalDate().toString());
        jsonObject.put("allDay", true);

        entry.update(jsonObject);

        Assertions.assertTrue(entry.isAllDay());
        Assertions.assertEquals(DEFAULT_START.toLocalDate().atStartOfDay(), entry.getStart());
        Assertions.assertEquals(DEFAULT_END.toLocalDate().atStartOfDay(), entry.getEnd());
    }
}
