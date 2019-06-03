package org.vaadin.stefan.fullcalendar;

import elemental.json.Json;
import elemental.json.JsonNull;
import elemental.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.UUID;

public class EntryTest {

    public static final LocalDateTime DEFAULT_START = LocalDate.of(2000, 1, 1).atStartOfDay();
    public static final Instant DEFAULT_START_UTC = DEFAULT_START.toInstant(ZoneOffset.UTC);
    public static final LocalDateTime DEFAULT_END = DEFAULT_START.plusDays(1);
    public static final Instant DEFAULT_END_UTC = DEFAULT_END.toInstant(ZoneOffset.UTC);

    public static final String DEFAULT_STRING = "test";
    public static final String DEFAULT_ID = DEFAULT_STRING + 1;
    public static final String DEFAULT_TITLE = DEFAULT_STRING + 2;
    public static final String DEFAULT_COLOR = DEFAULT_STRING + 3;
    public static final String DEFAULT_DESCRIPTION = DEFAULT_STRING + 4;
    public static final Entry.RenderingMode DEFAULT_RENDERING = Entry.RenderingMode.BACKGROUND;
    public static final Timezone CUSTOM_TIMEZONE = new Timezone(ZoneId.of("Europe/Berlin"));

    public static final String FULL_CALENDAR_HTML = "fullcalendar/full-calendar.html";

    @BeforeAll
    static void beforeAll() {
        TestUtils.initVaadinService(FULL_CALENDAR_HTML);
    }

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

        Assertions.assertNull(entry.getStart());
        Assertions.assertNull(entry.getStart(Timezone.UTC));
        Assertions.assertNull(entry.getEnd());
        Assertions.assertNull(entry.getEnd(Timezone.UTC));
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
        entry = new Entry(null, null, (Instant) null, null, false, false, null, null);

        // test id generation
        String id = entry.getId();
        Assertions.assertNotNull(id);
        Assertions.assertFalse(id.isEmpty());
        UUID.fromString(id);

        // test field values after construction - all params
        entry = new Entry(DEFAULT_ID, DEFAULT_TITLE, DEFAULT_START_UTC, DEFAULT_END_UTC, true, true, DEFAULT_COLOR, DEFAULT_DESCRIPTION);
        Assertions.assertEquals(DEFAULT_ID, entry.getId());
        Assertions.assertEquals(DEFAULT_TITLE, entry.getTitle());
        Assertions.assertEquals(DEFAULT_START_UTC, entry.getStartUTC());
        Assertions.assertEquals(DEFAULT_END_UTC, entry.getEndUTC());
        Assertions.assertTrue(entry.isAllDay());
        Assertions.assertTrue(entry.isEditable());
        Assertions.assertEquals(DEFAULT_COLOR, entry.getColor());
        Assertions.assertEquals(DEFAULT_DESCRIPTION, entry.getDescription());

        // test null color when set empty
        Assertions.assertNull(new Entry(null, null, (Instant) null, null, false, false, "", null).getColor());
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
        Assertions.assertEquals(expected.getRenderingMode(), actual.getRenderingMode());
    }

    @Test
    void testEqualsAndHashcodeOnlyDependOnId() {
        Entry entry = new Entry(DEFAULT_ID, null, (Instant) null, null, false, false, null, null);
        Entry entry1 = new Entry(DEFAULT_ID, DEFAULT_TITLE, DEFAULT_START_UTC, DEFAULT_END_UTC, true, true, DEFAULT_COLOR, DEFAULT_DESCRIPTION);
        entry1.setRenderingMode(DEFAULT_RENDERING);

        Assertions.assertEquals(entry, entry1);
        Assertions.assertEquals(entry.hashCode(), entry1.hashCode());

        Assertions.assertNotEquals(entry, new Entry());
        Assertions.assertNotEquals(entry.hashCode(), new Entry().hashCode());

        Entry entry2 = new Entry(null, DEFAULT_TITLE, DEFAULT_START_UTC, DEFAULT_END_UTC, true, true, DEFAULT_COLOR, DEFAULT_DESCRIPTION);
        Entry entry3 = new Entry(null, DEFAULT_TITLE, DEFAULT_START_UTC, DEFAULT_END_UTC, true, true, DEFAULT_COLOR, DEFAULT_DESCRIPTION);
        entry2.setRenderingMode(DEFAULT_RENDERING);
        entry3.setRenderingMode(DEFAULT_RENDERING);

        Assertions.assertNotEquals(entry2, entry3);
        Assertions.assertNotEquals(entry2.hashCode(), entry3.hashCode());
    }

    @Test
    void testToJsonEmpty() {
        Entry entry = new Entry();
        JsonObject jsonObject = entry.toJson();

        Assertions.assertEquals(entry.getId(), jsonObject.getString("id"));
        Assertions.assertTrue(jsonObject.get("title") instanceof JsonNull);
        Assertions.assertTrue(jsonObject.get("start") instanceof JsonNull);
        Assertions.assertTrue(jsonObject.get("end") instanceof JsonNull);
        Assertions.assertFalse(jsonObject.getBoolean("allDay"));
        Assertions.assertTrue(jsonObject.getBoolean("editable"));
        Assertions.assertTrue(jsonObject.get("color") instanceof JsonNull);
        Assertions.assertTrue(jsonObject.get("rendering") instanceof JsonNull);
    }


    @Test
    void testToJsonCustomTimezone() {
        FullCalendar calendar = new FullCalendar();
        calendar.setTimezone(CUSTOM_TIMEZONE);
        Entry entry = new Entry(DEFAULT_ID, DEFAULT_TITLE, DEFAULT_START_UTC, DEFAULT_END_UTC, true, true, DEFAULT_COLOR, DEFAULT_DESCRIPTION);
        entry.setRenderingMode(DEFAULT_RENDERING);
        entry.setCalendar(calendar);

        JsonObject jsonObject = entry.toJson();

        Assertions.assertEquals(DEFAULT_ID, jsonObject.getString("id"));
        Assertions.assertEquals(DEFAULT_TITLE, jsonObject.getString("title"));
        Assertions.assertEquals(CUSTOM_TIMEZONE.formatWithZoneId(DEFAULT_START_UTC), jsonObject.getString("start"));
        Assertions.assertEquals(CUSTOM_TIMEZONE.formatWithZoneId(DEFAULT_END_UTC), jsonObject.getString("end"));
        Assertions.assertTrue(jsonObject.getBoolean("allDay"));
        Assertions.assertTrue(jsonObject.getBoolean("editable"));
        Assertions.assertEquals(DEFAULT_COLOR, jsonObject.getString("color"));
        Assertions.assertEquals(DEFAULT_RENDERING.getClientSideValue(), jsonObject.getString("rendering"));
    }

    @Test
    void testToJsonUTC() {

        Entry entry = new Entry(DEFAULT_ID, DEFAULT_TITLE, DEFAULT_START_UTC, DEFAULT_END_UTC, true, true, DEFAULT_COLOR, DEFAULT_DESCRIPTION);
        entry.setRenderingMode(DEFAULT_RENDERING);

        JsonObject jsonObject = entry.toJson();

        Assertions.assertEquals(DEFAULT_ID, jsonObject.getString("id"));
        Assertions.assertEquals(DEFAULT_TITLE, jsonObject.getString("title"));
        Assertions.assertEquals(DEFAULT_START_UTC.toString(), jsonObject.getString("start"));
        Assertions.assertEquals(DEFAULT_END_UTC.toString(), jsonObject.getString("end"));
        Assertions.assertTrue(jsonObject.getBoolean("allDay"));
        Assertions.assertTrue(jsonObject.getBoolean("editable"));
        Assertions.assertEquals(DEFAULT_COLOR, jsonObject.getString("color"));
        Assertions.assertEquals(DEFAULT_RENDERING.getClientSideValue(), jsonObject.getString("rendering"));
    }

    @Test
    void testIfUpdateFromJsonFailsOnNonMatchingId() {
        Entry entry = new Entry();

        JsonObject jsonObject = Json.createObject();
        jsonObject.put("id", "someNonUUID");

        Assertions.assertThrows(IllegalArgumentException.class, () -> entry.update(jsonObject));
    }

    @Test
    void testUpdateEntryFromJsonWithUTC() {
        JsonObject jsonObject = Json.createObject();
        jsonObject.put("id", "1");

        jsonObject.put("title", DEFAULT_TITLE);
        jsonObject.put("start", DEFAULT_START_UTC.toString());
        jsonObject.put("end", DEFAULT_END_UTC.toString());
        jsonObject.put("allDay", false);
        jsonObject.put("editable", true);
        jsonObject.put("color", DEFAULT_COLOR);

        jsonObject.put("description", DEFAULT_DESCRIPTION); // this should not affect the object
        jsonObject.put("rendering", DEFAULT_RENDERING.getClientSideValue()); // this should not affect the object

        Entry entry = new Entry("1");
        entry.update(jsonObject);

        Assertions.assertEquals(jsonObject.getString("id"), entry.getId());

        Assertions.assertEquals(DEFAULT_TITLE, entry.getTitle());
        Assertions.assertFalse(entry.isAllDay());
        Assertions.assertEquals(DEFAULT_START_UTC, entry.getStartUTC());
        Assertions.assertEquals(DEFAULT_END_UTC, entry.getEndUTC());
        Assertions.assertTrue(entry.isEditable());
        Assertions.assertEquals(DEFAULT_COLOR, entry.getColor());

        Assertions.assertNull(entry.getDescription()); // should not be affected by json
        Assertions.assertEquals(Entry.RenderingMode.NORMAL, entry.getRenderingMode()); // should not be affected by json
    }

    @Test
    void testUpdateEntryFromJsonWithCustomTimezone() {
        JsonObject jsonObject = Json.createObject();
        jsonObject.put("id", "1");

        jsonObject.put("title", DEFAULT_TITLE);
        ZonedDateTime start = DEFAULT_START_UTC.atZone(CUSTOM_TIMEZONE.getZoneId());
        ZonedDateTime end = DEFAULT_END_UTC.atZone(CUSTOM_TIMEZONE.getZoneId());
        jsonObject.put("start", start.toString());
        jsonObject.put("end", end.toString());
        jsonObject.put("allDay", false);
        jsonObject.put("editable", true);
        jsonObject.put("color", DEFAULT_COLOR);

        jsonObject.put("description", DEFAULT_DESCRIPTION); // this should not affect the object
        jsonObject.put("rendering", DEFAULT_RENDERING.getClientSideValue()); // this should not affect the object

        Entry entry = new Entry("1");
        entry.update(jsonObject);

        Assertions.assertEquals(jsonObject.getString("id"), entry.getId());

        Assertions.assertEquals(DEFAULT_TITLE, entry.getTitle());
        Assertions.assertFalse(entry.isAllDay());
        Assertions.assertEquals(DEFAULT_START_UTC, entry.getStartUTC());
        Assertions.assertEquals(DEFAULT_END_UTC, entry.getEndUTC());
        Assertions.assertTrue(entry.isEditable());
        Assertions.assertEquals(DEFAULT_COLOR, entry.getColor());

        Assertions.assertNull(entry.getDescription()); // should not be affected by json
        Assertions.assertEquals(Entry.RenderingMode.NORMAL, entry.getRenderingMode()); // should not be affected by json
    }

}
