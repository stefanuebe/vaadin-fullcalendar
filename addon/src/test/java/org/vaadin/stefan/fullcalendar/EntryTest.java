package org.vaadin.stefan.fullcalendar;

import elemental.json.Json;
import elemental.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.vaadin.stefan.fullcalendar.Entry.*;

public class EntryTest {

    public static final Instant DEFAULT_START_UTC = LocalDate.of(2000, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC);
    public static final Instant DEFAULT_END_UTC = LocalDate.of(2000, 1, 1).atStartOfDay().plusDays(1).toInstant(ZoneOffset.UTC);

    public static final String DEFAULT_STRING = "test";
    public static final String DEFAULT_ID = DEFAULT_STRING + 1;
    public static final String DEFAULT_TITLE = DEFAULT_STRING + 2;
    public static final String DEFAULT_COLOR = DEFAULT_STRING + 3;
    public static final String DEFAULT_DESCRIPTION = DEFAULT_STRING + 4;
    public static final RenderingMode DEFAULT_RENDERING = RenderingMode.BACKGROUND;
    public static final Timezone CUSTOM_TIMEZONE = new Timezone(ZoneId.of("Europe/Berlin"));

    public static final String FULL_CALENDAR_HTML = "fullcalendar/full-calendar.html";

    public static final Map<Key, Object> DEFAULTS = new HashMap<>();

    static {
        DEFAULTS.put(EntryKey.TITLE, DEFAULT_STRING);
        DEFAULTS.put(EntryKey.START, DEFAULT_START_UTC);
        DEFAULTS.put(EntryKey.END, DEFAULT_END_UTC);
        DEFAULTS.put(EntryKey.COLOR, DEFAULT_COLOR);
        DEFAULTS.put(EntryKey.RENDERING_MODE, DEFAULT_RENDERING);
    }

    /**
     * Checks an original entry and the json based variant for equal fields, that can be changed by json.
     *
     * @param expected expected entry
     * @param actual   actual entry
     */
    static void assertFullEqualsByJsonAttributes(Entry expected, Entry actual) {
        Assertions.assertEquals(expected.getId(), actual.getId());
        // we use UTC, since LDT or ZoneDT can differ whether the entry has a calendar assigned or not
        Assertions.assertEquals(expected.getStartUTC(), actual.getStartUTC());
        Assertions.assertEquals(expected.getEndUTC(), actual.getEndUTC());
        Assertions.assertEquals(expected.isAllDay(), actual.isAllDay());
    }

    @Test
    void testNoArgsConstructor() {
        Entry entry = new Entry();

        // test id generation
        String id = entry.getId();
        Assertions.assertNotNull(id);
        Assertions.assertFalse(id.isEmpty());

        //noinspection ResultOfMethodCallIgnored
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
        entry = new Entry();

        // test id generation
        String id = entry.getId();
        Assertions.assertNotNull(id);
        Assertions.assertFalse(id.isEmpty());
        UUID.fromString(id);
        // test editable parameter to be true by default
        Assertions.assertTrue(entry.isEditable());

        // test field values after construction - all params
        entry = new Entry(DEFAULT_ID);
        entry.setTitle(DEFAULT_TITLE);
        entry.setStart(DEFAULT_START_UTC);
        entry.setEnd(DEFAULT_END_UTC);
        entry.setAllDay(true);
        entry.setEditable(true);
        entry.setColor(DEFAULT_COLOR);
        entry.setDescription(DEFAULT_DESCRIPTION);

        Assertions.assertEquals(DEFAULT_ID, entry.getId());
        Assertions.assertEquals(DEFAULT_TITLE, entry.getTitle());
        Assertions.assertEquals(DEFAULT_START_UTC, entry.getStartUTC());
        Assertions.assertEquals(DEFAULT_END_UTC, entry.getEndUTC());
        Assertions.assertTrue(entry.isAllDay());
        Assertions.assertTrue(entry.isEditable());
        Assertions.assertEquals(DEFAULT_COLOR, entry.getColor());
        Assertions.assertEquals(DEFAULT_DESCRIPTION, entry.getDescription());

        // test null color when set empty
        Assertions.assertNull(new Entry().getColor());
    }

    @Test
    void testEqualsAndHashcodeOnlyDependOnId() {
        Entry entry = new Entry(DEFAULT_ID);
        entry.setTitle(null);
        entry.setStart((Instant) null);
        entry.setEnd((Instant) null);
        entry.setAllDay(false);
        entry.setEditable(false);
        entry.setColor(null);
        entry.setDescription(null);

        Entry entry1 = new Entry(DEFAULT_ID);
        entry1.setTitle(DEFAULT_TITLE);
        entry1.setStart(DEFAULT_START_UTC);
        entry1.setEnd(DEFAULT_END_UTC);
        entry1.setAllDay(true);
        entry1.setEditable(true);
        entry1.setColor(DEFAULT_COLOR);
        entry1.setDescription(DEFAULT_DESCRIPTION);
        entry1.setRenderingMode(DEFAULT_RENDERING);

        Assertions.assertEquals(entry, entry1);
        Assertions.assertEquals(entry.hashCode(), entry1.hashCode());

        Assertions.assertNotEquals(entry, new Entry());
        Assertions.assertNotEquals(entry.hashCode(), new Entry().hashCode());

        Entry entry2 = new Entry();
        entry2.setTitle(DEFAULT_TITLE);
        entry2.setStart(DEFAULT_START_UTC);
        entry2.setEnd(DEFAULT_END_UTC);
        entry2.setAllDay(true);
        entry2.setEditable(true);
        entry2.setColor(DEFAULT_COLOR);
        entry2.setDescription(DEFAULT_DESCRIPTION);
        Entry entry3 = new Entry();
        entry3.setTitle(DEFAULT_TITLE);
        entry3.setStart(DEFAULT_START_UTC);
        entry3.setEnd(DEFAULT_END_UTC);
        entry3.setAllDay(true);
        entry3.setEditable(true);
        entry3.setColor(DEFAULT_COLOR);
        entry3.setDescription(DEFAULT_DESCRIPTION);
        entry3.setRenderingMode(DEFAULT_RENDERING);
        entry3.setRenderingMode(DEFAULT_RENDERING);

        Assertions.assertNotEquals(entry2, entry3);
        Assertions.assertNotEquals(entry2.hashCode(), entry3.hashCode());
    }

    @Test
    void testToJsonEmpty() {
        Entry entry = new Entry();
        JsonObject jsonObject = entry.toJsonOnAdd();

        Assertions.assertEquals(entry.getId(), jsonObject.getString("id"));
        Set<Key> keys = Key.readAndRegisterKeys(EntryKey.class);

        for (Key key : keys) {
            if (key != EntryKey.ID && key.getDefaultValue() == null) {
                Assertions.assertFalse(jsonObject.hasKey(key.getName()), key.getName());
            } else {
                Assertions.assertTrue(jsonObject.hasKey(key.getName()), key.getName());
            }
        }
    }


    @Test
    void testToJsonCustomTimezone() {
        FullCalendar calendar = new FullCalendar();
        calendar.setTimezone(CUSTOM_TIMEZONE);
        Entry entry = new Entry(DEFAULT_ID);
        entry.setTitle(DEFAULT_TITLE);
        entry.setStart(DEFAULT_START_UTC);
        entry.setEnd(DEFAULT_END_UTC);
        entry.setAllDay(true);
        entry.setEditable(true);
        entry.setColor(DEFAULT_COLOR);
        entry.setDescription(DEFAULT_DESCRIPTION);
        entry.setRenderingMode(DEFAULT_RENDERING);
        entry.setCalendar(calendar);

        JsonObject jsonObject = entry.toJson();

        Assertions.assertEquals(DEFAULT_ID, jsonObject.getString("id"));
        Assertions.assertEquals(DEFAULT_TITLE, jsonObject.getString(EntryKey.TITLE.getName()));
        Assertions.assertEquals(CUSTOM_TIMEZONE.formatWithZoneId(DEFAULT_START_UTC), jsonObject.getString(EntryKey.START.getName()));
        Assertions.assertEquals(CUSTOM_TIMEZONE.formatWithZoneId(DEFAULT_END_UTC), jsonObject.getString(EntryKey.END.getName()));
        Assertions.assertTrue(jsonObject.getBoolean(EntryKey.ALL_DAY.getName()));
        Assertions.assertTrue(jsonObject.getBoolean(EntryKey.EDITABLE.getName()));
        Assertions.assertEquals(DEFAULT_COLOR, jsonObject.getString(EntryKey.COLOR.getName()));
        Assertions.assertEquals(DEFAULT_RENDERING.getClientSideValue(), jsonObject.getString(EntryKey.RENDERING_MODE.getName()));
    }

    @Test
    void testToJsonUTC() {
        Entry entry = new Entry(DEFAULT_ID);
        entry.setTitle(DEFAULT_TITLE);
        entry.setStart(DEFAULT_START_UTC);
        entry.setEnd(DEFAULT_END_UTC);
        entry.setAllDay(true);
        entry.setEditable(true);
        entry.setColor(DEFAULT_COLOR);
        entry.setDescription(DEFAULT_DESCRIPTION);
        entry.setRenderingMode(DEFAULT_RENDERING);

        JsonObject jsonObject = entry.toJson();

        Assertions.assertEquals(DEFAULT_ID, jsonObject.getString("id"));
        Assertions.assertEquals(DEFAULT_TITLE, jsonObject.getString(EntryKey.TITLE.getName()));
        Assertions.assertEquals(DEFAULT_START_UTC.toString(), jsonObject.getString(EntryKey.START.getName()));
        Assertions.assertEquals(DEFAULT_END_UTC.toString(), jsonObject.getString(EntryKey.END.getName()));
        Assertions.assertTrue(jsonObject.getBoolean(EntryKey.ALL_DAY.getName()));
        Assertions.assertTrue(jsonObject.getBoolean(EntryKey.EDITABLE.getName()));
        Assertions.assertEquals(DEFAULT_COLOR, jsonObject.getString(EntryKey.COLOR.getName()));
        Assertions.assertEquals(DEFAULT_RENDERING.getClientSideValue(), jsonObject.getString(EntryKey.RENDERING_MODE.getName()));
    }

    @Test
    void testIfUpdateFromJsonFailsOnNonMatchingId() {
        Entry entry = new Entry();

        JsonObject jsonObject = Json.createObject();
        jsonObject.put("id", "someNonUUID");

        Assertions.assertThrows(IllegalArgumentException.class, () -> entry.updateFromJson(jsonObject));
    }

    @Test
    void testUpdateEntryFromJsonWithUTC() {
        JsonObject jsonObject = Json.createObject();
        jsonObject.put("id", "1");

        jsonObject.put(EntryKey.TITLE.getName(), DEFAULT_TITLE);
        jsonObject.put(EntryKey.START.getName(), DEFAULT_START_UTC.toString());
        jsonObject.put(EntryKey.END.getName(), DEFAULT_END_UTC.toString());
        jsonObject.put(EntryKey.ALL_DAY.getName(), true);
        jsonObject.put(EntryKey.EDITABLE.getName(), false);
        jsonObject.put(EntryKey.DURATION_EDITABLE.getName(), false);
        jsonObject.put(EntryKey.START_EDITABLE.getName(), false);
        jsonObject.put(EntryKey.COLOR.getName(), DEFAULT_COLOR);
        jsonObject.put("description", DEFAULT_DESCRIPTION);
        jsonObject.put(EntryKey.RENDERING_MODE.getName(), DEFAULT_RENDERING.getClientSideValue());

        Entry entry = new Entry("1");
        Assertions.assertTrue(entry.isValidJsonSource(jsonObject));
        entry.updateFromJson(jsonObject);

        // affected properties
        Assertions.assertTrue(entry.isAllDay());
        Assertions.assertEquals(DEFAULT_START_UTC, entry.getStartUTC());
        Assertions.assertEquals(DEFAULT_END_UTC, entry.getEndUTC());

        // by json unaffected properties
        Assertions.assertTrue(entry.isEditable());
        Assertions.assertTrue(entry.isStartEditable());
        Assertions.assertTrue(entry.isDurationEditable());
        Assertions.assertFalse(entry.isRecurring());
        Assertions.assertNull(entry.getTitle());
        Assertions.assertNull(entry.getColor());
        Assertions.assertNull(entry.getBorderColor());
        Assertions.assertNull(entry.getBackgroundColor());
        Assertions.assertNull(entry.getTextColor());
        Assertions.assertNull(entry.getDescription());
        Assertions.assertEquals(RenderingMode.NONE, entry.getRenderingMode());
    }

    @Test
    void testUpdateEntryFromJsonWithCustomTimezone() {
        JsonObject jsonObject = Json.createObject();
        jsonObject.put("id", "1");

        ZonedDateTime start = DEFAULT_START_UTC.atZone(CUSTOM_TIMEZONE.getZoneId());
        ZonedDateTime end = DEFAULT_END_UTC.atZone(CUSTOM_TIMEZONE.getZoneId());
        jsonObject.put(EntryKey.START.getName(), start.toString());
        jsonObject.put(EntryKey.END.getName(), end.toString());
        jsonObject.put(EntryKey.ALL_DAY.getName(), true);

        jsonObject.put("description", DEFAULT_DESCRIPTION); // this should not affect the object
        jsonObject.put(EntryKey.RENDERING_MODE.getName(), DEFAULT_RENDERING.getClientSideValue()); // this should not affect the object

        Entry entry = new Entry("1");
        Assertions.assertTrue(entry.isValidJsonSource(jsonObject));
        entry.updateFromJson(jsonObject);

        // affected properties
        Assertions.assertTrue(entry.isAllDay());
        Assertions.assertEquals(DEFAULT_START_UTC, entry.getStartUTC());
        Assertions.assertEquals(DEFAULT_END_UTC, entry.getEndUTC());

        // by json unaffected properties
        Assertions.assertTrue(entry.isEditable());
        Assertions.assertTrue(entry.isStartEditable());
        Assertions.assertTrue(entry.isDurationEditable());
        Assertions.assertFalse(entry.isRecurring());
        Assertions.assertNull(entry.getTitle());
        Assertions.assertNull(entry.getColor());
        Assertions.assertNull(entry.getBorderColor());
        Assertions.assertNull(entry.getBackgroundColor());
        Assertions.assertNull(entry.getTextColor());
        Assertions.assertNull(entry.getDescription());
        Assertions.assertEquals(RenderingMode.NONE, entry.getRenderingMode());
    }

}