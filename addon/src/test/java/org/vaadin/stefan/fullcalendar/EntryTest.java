package org.vaadin.stefan.fullcalendar;

import elemental.json.Json;
import elemental.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.*;

import static org.vaadin.stefan.fullcalendar.Entry.*;

public class EntryTest {

    public static final LocalDateTime DEFAULT_START = LocalDate.of(2000, 1, 1).atStartOfDay();
    public static final LocalDateTime DEFAULT_END = LocalDate.of(2000, 1, 1).plusDays(1).atStartOfDay();

    public static final String DEFAULT_STRING = "test";
    public static final String DEFAULT_ID = DEFAULT_STRING + 1;
    public static final String DEFAULT_TITLE = DEFAULT_STRING + 2;
    public static final String DEFAULT_COLOR = DEFAULT_STRING + 3;
    public static final String DEFAULT_DESCRIPTION = DEFAULT_STRING + 4;
    public static final RenderingMode DEFAULT_RENDERING = RenderingMode.BACKGROUND;
    public static final Timezone CUSTOM_TIMEZONE = new Timezone(ZoneId.of("Europe/Berlin"));

    public static final String FULL_CALENDAR_HTML = "fullcalendar/full-calendar.html";

    public static final Map<Key, Object> DEFAULTS = new HashMap<>();
    public static final String JSON_UTC_TIMESTAMP = JsonUtils.formatClientSideDateTimeString(DEFAULT_START);

    static {
        DEFAULTS.put(EntryKey.TITLE, DEFAULT_STRING);
        DEFAULTS.put(EntryKey.START, DEFAULT_START);
        DEFAULTS.put(EntryKey.END, DEFAULT_END);
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
        Assertions.assertEquals(expected.getStart(), actual.getStart());
        Assertions.assertEquals(expected.getEnd(), actual.getEnd());
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
        Assertions.assertNull(entry.getEnd());
    }

    @Test
    void testIdArgConstructor() {
        Entry entry = new Entry(null);

        // test id generation
        String id = entry.getId();
        Assertions.assertNotNull(id);
        Assertions.assertFalse(id.isEmpty());
        Assertions.assertEquals(id, UUID.fromString(id).toString());

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
        Assertions.assertEquals(id, UUID.fromString(id).toString());
        // test editable parameter to be true by default
        Assertions.assertTrue(entry.isEditable());

        // test field values after construction - all params
        entry = new Entry(DEFAULT_ID);
        entry.setTitle(DEFAULT_TITLE);
        entry.setStart(DEFAULT_START);
        entry.setEnd(DEFAULT_END);
        entry.setAllDay(true);
        entry.setEditable(true);
        entry.setColor(DEFAULT_COLOR);
        entry.setDescription(DEFAULT_DESCRIPTION);

        Assertions.assertEquals(DEFAULT_ID, entry.getId());
        Assertions.assertEquals(DEFAULT_TITLE, entry.getTitle());
        Assertions.assertEquals(DEFAULT_START, entry.getStart());
        Assertions.assertEquals(DEFAULT_END, entry.getEnd());
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
        entry.setStart((LocalDateTime) null);
        entry.setEnd((LocalDateTime) null);
        entry.setAllDay(false);
        entry.setEditable(false);
        entry.setColor(null);
        entry.setDescription(null);

        Entry entry1 = new Entry(DEFAULT_ID);
        entry1.setTitle(DEFAULT_TITLE);
        entry1.setStart(DEFAULT_START);
        entry1.setEnd(DEFAULT_END);
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
        entry2.setStart(DEFAULT_START);
        entry2.setEnd(DEFAULT_END);
        entry2.setAllDay(true);
        entry2.setEditable(true);
        entry2.setColor(DEFAULT_COLOR);
        entry2.setDescription(DEFAULT_DESCRIPTION);
        Entry entry3 = new Entry();
        entry3.setTitle(DEFAULT_TITLE);
        entry3.setStart(DEFAULT_START);
        entry3.setEnd(DEFAULT_END);
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
        JsonObject jsonObject = entry.toJson(false);

        Set<Key> defaultKeys = new HashSet<>(Arrays.asList(EntryKey.ID, EntryKey.EDITABLE));

        Assertions.assertEquals(entry.getId(), jsonObject.getString("id"));
        Set<Key> keys = Key.readAndRegisterKeys(EntryKey.class);

        for (Key key : keys) {
            if (defaultKeys.contains(key)) {
                Assertions.assertTrue(jsonObject.hasKey(key.getName()), key.getName());
            } else {
                Assertions.assertFalse(jsonObject.hasKey(key.getName()), key.getName());
            }
        }
    }

    @Test
    void test_startMethods_nullSafety() {
        Entry entry = new Entry();

        Assertions.assertNull(entry.getStart());
        Assertions.assertNull(entry.getStartAsInstant());
        Assertions.assertNull(entry.getStartAsLocalDate());
        Assertions.assertNull(entry.getStartWithTimezone());
        Assertions.assertNull(entry.getStartWithOffset());
        Assertions.assertNull(entry.getStartWithOffset(Timezone.UTC));

        entry.clearStart();
        entry.setStart((LocalDateTime) null);
        entry.setStart((Instant) null);
        entry.setStart((LocalDate) null);
        entry.setStartWithTimezone(null);
        entry.setStartWithOffset(null);
        entry.setStartWithOffset(null, Timezone.UTC);
    }

    @Test
    void test_startMethods_utc() {
        LocalDateTime now = DEFAULT_START;
        Instant nowInstant = now.toInstant(ZoneOffset.UTC);
        ZonedDateTime nowZoned = nowInstant.atZone(Timezone.ZONE_ID_UTC);
        LocalDateTime nowPlusOffset = Timezone.UTC.applyTimezoneOffset(now);

        Entry entry = new Entry();
        entry.setStart(now);

        Assertions.assertEquals(now, entry.getStart());
        Assertions.assertEquals(nowInstant, entry.getStartAsInstant());
        Assertions.assertEquals(nowZoned, entry.getStartWithTimezone());
        Assertions.assertEquals(nowPlusOffset, entry.getStartWithTimezone().toLocalDateTime());
        Assertions.assertEquals(nowPlusOffset, entry.getStartWithOffset());
        Assertions.assertEquals(nowPlusOffset, entry.getStartWithOffset(Timezone.UTC));
        JsonObject json = entry.toJson();
        Assertions.assertEquals(JSON_UTC_TIMESTAMP, json.getString(EntryKey.START.getName()));


        entry.setStart(nowInstant);
        Assertions.assertEquals(now, entry.getStart());
        Assertions.assertEquals(nowInstant, entry.getStartAsInstant());
        Assertions.assertEquals(nowZoned, entry.getStartWithTimezone());
        Assertions.assertEquals(nowPlusOffset, entry.getStartWithTimezone().toLocalDateTime());
        Assertions.assertEquals(nowPlusOffset, entry.getStartWithOffset());
        Assertions.assertEquals(nowPlusOffset, entry.getStartWithOffset(Timezone.UTC));
        json = entry.toJson();
        Assertions.assertEquals(JSON_UTC_TIMESTAMP, json.getString(EntryKey.START.getName()));


        entry.setStartWithTimezone(nowZoned);
        Assertions.assertEquals(now, entry.getStart());
        Assertions.assertEquals(nowInstant, entry.getStartAsInstant());
        Assertions.assertEquals(nowZoned, entry.getStartWithTimezone());
        Assertions.assertEquals(nowPlusOffset, entry.getStartWithTimezone().toLocalDateTime());
        Assertions.assertEquals(nowPlusOffset, entry.getStartWithOffset());
        Assertions.assertEquals(nowPlusOffset, entry.getStartWithOffset(Timezone.UTC));
        json = entry.toJson();
        Assertions.assertEquals(JSON_UTC_TIMESTAMP, json.getString(EntryKey.START.getName()));


        entry.setStartWithOffset(nowZoned.toLocalDateTime());
        Assertions.assertEquals(now, entry.getStart());
        Assertions.assertEquals(nowInstant, entry.getStartAsInstant());
        Assertions.assertEquals(nowZoned, entry.getStartWithTimezone());
        Assertions.assertEquals(nowPlusOffset, entry.getStartWithTimezone().toLocalDateTime());
        Assertions.assertEquals(nowPlusOffset, entry.getStartWithOffset());
        Assertions.assertEquals(nowPlusOffset, entry.getStartWithOffset(Timezone.UTC));
        json = entry.toJson();
        Assertions.assertEquals(JSON_UTC_TIMESTAMP, json.getString(EntryKey.START.getName()));

        entry.setStartWithOffset(nowZoned.toLocalDateTime(), Timezone.UTC);
        Assertions.assertEquals(now, entry.getStart());
        Assertions.assertEquals(nowInstant, entry.getStartAsInstant());
        Assertions.assertEquals(nowZoned, entry.getStartWithTimezone());
        Assertions.assertEquals(nowPlusOffset, entry.getStartWithTimezone().toLocalDateTime());
        Assertions.assertEquals(nowPlusOffset, entry.getStartWithOffset());
        Assertions.assertEquals(nowPlusOffset, entry.getStartWithOffset(Timezone.UTC));
        json = entry.toJson();
        Assertions.assertEquals(JSON_UTC_TIMESTAMP, json.getString(EntryKey.START.getName()));


        LocalDate nowDate = now.toLocalDate();
        entry.setStart(nowDate);

        Assertions.assertEquals(nowDate.atStartOfDay(), entry.getStart());
        json = entry.toJson();
        Assertions.assertEquals(JSON_UTC_TIMESTAMP, json.getString(EntryKey.START.getName()));

    }

    /**
     * Simulating how the entry works, when the calendar has a timezone different than UTC set.
     */
    @Test
    void test_startMethods_timezone() {
        Timezone timezone = Timezone.getSystem();

        LocalDateTime now = DEFAULT_START;
        Instant nowInstant = now.toInstant(ZoneOffset.UTC);
        ZonedDateTime nowZoned = nowInstant.atZone(timezone.getZoneId());
        LocalDateTime nowPlusOffset = timezone.applyTimezoneOffset(now);

        Entry entry = new Entry();
        entry.setStart(now);

        FullCalendar calendar = new FullCalendar();
        calendar.setTimezone(timezone);
        calendar.addEntry(entry);

        Assertions.assertEquals(now, entry.getStart());
        Assertions.assertEquals(nowInstant, entry.getStartAsInstant());
        Assertions.assertEquals(nowZoned, entry.getStartWithTimezone());
        Assertions.assertEquals(nowPlusOffset, entry.getStartWithTimezone().toLocalDateTime());
        Assertions.assertEquals(nowPlusOffset, entry.getStartWithOffset());
        Assertions.assertEquals(nowPlusOffset, entry.getStartWithOffset(timezone));
        JsonObject json = entry.toJson();
        Assertions.assertEquals(JSON_UTC_TIMESTAMP, json.getString(EntryKey.START.getName()));


        entry.setStart(nowInstant);
        Assertions.assertEquals(now, entry.getStart());
        Assertions.assertEquals(nowInstant, entry.getStartAsInstant());
        Assertions.assertEquals(nowZoned, entry.getStartWithTimezone());
        Assertions.assertEquals(nowPlusOffset, entry.getStartWithTimezone().toLocalDateTime());
        Assertions.assertEquals(nowPlusOffset, entry.getStartWithOffset());
        Assertions.assertEquals(nowPlusOffset, entry.getStartWithOffset(timezone));
        json = entry.toJson();
        Assertions.assertEquals(JSON_UTC_TIMESTAMP, json.getString(EntryKey.START.getName()));


        entry.setStartWithTimezone(nowZoned);
        Assertions.assertEquals(now, entry.getStart());
        Assertions.assertEquals(nowInstant, entry.getStartAsInstant());
        Assertions.assertEquals(nowZoned, entry.getStartWithTimezone());
        Assertions.assertEquals(nowPlusOffset, entry.getStartWithTimezone().toLocalDateTime());
        Assertions.assertEquals(nowPlusOffset, entry.getStartWithOffset());
        Assertions.assertEquals(nowPlusOffset, entry.getStartWithOffset(timezone));
        json = entry.toJson();
        Assertions.assertEquals(JSON_UTC_TIMESTAMP, json.getString(EntryKey.START.getName()));

        entry.setStartWithOffset(nowZoned.toLocalDateTime());
        Assertions.assertEquals(now, entry.getStart());
        Assertions.assertEquals(nowInstant, entry.getStartAsInstant());
        Assertions.assertEquals(nowZoned, entry.getStartWithTimezone());
        Assertions.assertEquals(nowPlusOffset, entry.getStartWithTimezone().toLocalDateTime());
        Assertions.assertEquals(nowPlusOffset, entry.getStartWithOffset());
        Assertions.assertEquals(nowPlusOffset, entry.getStartWithOffset(timezone));
        json = entry.toJson();
        Assertions.assertEquals(JSON_UTC_TIMESTAMP, json.getString(EntryKey.START.getName()));

        entry.setStartWithOffset(nowZoned.toLocalDateTime(), timezone);
        Assertions.assertEquals(now, entry.getStart());
        Assertions.assertEquals(nowInstant, entry.getStartAsInstant());
        Assertions.assertEquals(nowZoned, entry.getStartWithTimezone());
        Assertions.assertEquals(nowPlusOffset, entry.getStartWithTimezone().toLocalDateTime());
        Assertions.assertEquals(nowPlusOffset, entry.getStartWithOffset());
        Assertions.assertEquals(nowPlusOffset, entry.getStartWithOffset(timezone));
        json = entry.toJson();
        Assertions.assertEquals(JSON_UTC_TIMESTAMP, json.getString(EntryKey.START.getName()));

        LocalDate nowDate = now.toLocalDate();
        entry.setStart(nowDate);

        Assertions.assertEquals(nowDate.atStartOfDay(), entry.getStart());
        json = entry.toJson();
        Assertions.assertEquals(JSON_UTC_TIMESTAMP, json.getString(EntryKey.START.getName()));

    }

    @Test
    void test_endMethods_nullSafety() {
        Entry entry = new Entry();

        Assertions.assertNull(entry.getEnd());
        Assertions.assertNull(entry.getEndAsInstant());
        Assertions.assertNull(entry.getEndAsLocalDate());
        Assertions.assertNull(entry.getEndWithTimezone());
        Assertions.assertNull(entry.getEndWithOffset());
        Assertions.assertNull(entry.getEndWithOffset(Timezone.UTC));

        entry.clearEnd();
        entry.setEnd((LocalDateTime) null);
        entry.setEnd((Instant) null);
        entry.setEnd((LocalDate) null);
        entry.setEndWithTimezone(null);
        entry.setEndWithOffset(null);
        entry.setEndWithOffset(null, Timezone.UTC);
    }

    @Test
    void test_endMethods_utc() {
        LocalDateTime now = DEFAULT_START;
        Instant nowInstant = now.toInstant(ZoneOffset.UTC);
        ZonedDateTime nowZoned = nowInstant.atZone(Timezone.ZONE_ID_UTC);
        LocalDateTime nowPlusOffset = Timezone.UTC.applyTimezoneOffset(now);

        Entry entry = new Entry();
        entry.setEnd(now);

        Assertions.assertEquals(now, entry.getEnd());
        Assertions.assertEquals(nowInstant, entry.getEndAsInstant());
        Assertions.assertEquals(nowZoned, entry.getEndWithTimezone());
        Assertions.assertEquals(nowPlusOffset, entry.getEndWithTimezone().toLocalDateTime());
        Assertions.assertEquals(nowPlusOffset, entry.getEndWithOffset());
        Assertions.assertEquals(nowPlusOffset, entry.getEndWithOffset(Timezone.UTC));

        JsonObject json = entry.toJson();
        Assertions.assertEquals(JSON_UTC_TIMESTAMP, json.getString(EntryKey.END.getName()));

        entry.setEnd(nowInstant);
        Assertions.assertEquals(now, entry.getEnd());
        Assertions.assertEquals(nowInstant, entry.getEndAsInstant());
        Assertions.assertEquals(nowZoned, entry.getEndWithTimezone());
        Assertions.assertEquals(nowPlusOffset, entry.getEndWithTimezone().toLocalDateTime());
        Assertions.assertEquals(nowPlusOffset, entry.getEndWithOffset());
        Assertions.assertEquals(nowPlusOffset, entry.getEndWithOffset(Timezone.UTC));

        json = entry.toJson();
        Assertions.assertEquals(JSON_UTC_TIMESTAMP, json.getString(EntryKey.END.getName()));

        entry.setEndWithTimezone(nowZoned);
        Assertions.assertEquals(now, entry.getEnd());
        Assertions.assertEquals(nowInstant, entry.getEndAsInstant());
        Assertions.assertEquals(nowZoned, entry.getEndWithTimezone());
        Assertions.assertEquals(nowPlusOffset, entry.getEndWithTimezone().toLocalDateTime());
        Assertions.assertEquals(nowPlusOffset, entry.getEndWithOffset());
        Assertions.assertEquals(nowPlusOffset, entry.getEndWithOffset(Timezone.UTC));

        json = entry.toJson();
        Assertions.assertEquals(JSON_UTC_TIMESTAMP, json.getString(EntryKey.END.getName()));

        entry.setEndWithOffset(nowZoned.toLocalDateTime());
        Assertions.assertEquals(now, entry.getEnd());
        Assertions.assertEquals(nowInstant, entry.getEndAsInstant());
        Assertions.assertEquals(nowZoned, entry.getEndWithTimezone());
        Assertions.assertEquals(nowPlusOffset, entry.getEndWithTimezone().toLocalDateTime());
        Assertions.assertEquals(nowPlusOffset, entry.getEndWithOffset());
        Assertions.assertEquals(nowPlusOffset, entry.getEndWithOffset(Timezone.UTC));

        json = entry.toJson();
        Assertions.assertEquals(JSON_UTC_TIMESTAMP, json.getString(EntryKey.END.getName()));

        entry.setEndWithOffset(nowZoned.toLocalDateTime(), Timezone.UTC);
        Assertions.assertEquals(now, entry.getEnd());
        Assertions.assertEquals(nowInstant, entry.getEndAsInstant());
        Assertions.assertEquals(nowZoned, entry.getEndWithTimezone());
        Assertions.assertEquals(nowPlusOffset, entry.getEndWithTimezone().toLocalDateTime());
        Assertions.assertEquals(nowPlusOffset, entry.getEndWithOffset());
        Assertions.assertEquals(nowPlusOffset, entry.getEndWithOffset(Timezone.UTC));

        json = entry.toJson();
        Assertions.assertEquals(JSON_UTC_TIMESTAMP, json.getString(EntryKey.END.getName()));

        LocalDate nowDate = now.toLocalDate();
        entry.setEnd(nowDate);
        Assertions.assertEquals(nowDate.atStartOfDay(), entry.getEnd());

        json = entry.toJson();
        Assertions.assertEquals(JSON_UTC_TIMESTAMP, json.getString(EntryKey.END.getName()));
    }

    /**
     * Simulating how the entry works, when the calendar has a timezone different than UTC set.
     */
    @Test
    void test_endMethods_timezone() {
        Timezone timezone = Timezone.getSystem();

        LocalDateTime now = DEFAULT_START;
        Instant nowInstant = now.toInstant(ZoneOffset.UTC);
        ZonedDateTime nowZoned = nowInstant.atZone(timezone.getZoneId());
        LocalDateTime nowPlusOffset = timezone.applyTimezoneOffset(now);

        Entry entry = new Entry();
        entry.setEnd(now);

        FullCalendar calendar = new FullCalendar();
        calendar.setTimezone(timezone);
        calendar.addEntry(entry);

        Assertions.assertEquals(now, entry.getEnd());
        Assertions.assertEquals(nowInstant, entry.getEndAsInstant());
        Assertions.assertEquals(nowZoned, entry.getEndWithTimezone());
        Assertions.assertEquals(nowPlusOffset, entry.getEndWithTimezone().toLocalDateTime());
        Assertions.assertEquals(nowPlusOffset, entry.getEndWithOffset());
        Assertions.assertEquals(nowPlusOffset, entry.getEndWithOffset(timezone));

        JsonObject json = entry.toJson();
        Assertions.assertEquals(JSON_UTC_TIMESTAMP, json.getString(EntryKey.END.getName()));

        entry.setEnd(nowInstant);
        Assertions.assertEquals(now, entry.getEnd());
        Assertions.assertEquals(nowInstant, entry.getEndAsInstant());
        Assertions.assertEquals(nowZoned, entry.getEndWithTimezone());
        Assertions.assertEquals(nowPlusOffset, entry.getEndWithTimezone().toLocalDateTime());
        Assertions.assertEquals(nowPlusOffset, entry.getEndWithOffset());
        Assertions.assertEquals(nowPlusOffset, entry.getEndWithOffset(timezone));

        json = entry.toJson();
        Assertions.assertEquals(JSON_UTC_TIMESTAMP, json.getString(EntryKey.END.getName()));

        entry.setEndWithTimezone(nowZoned);
        Assertions.assertEquals(now, entry.getEnd());
        Assertions.assertEquals(nowInstant, entry.getEndAsInstant());
        Assertions.assertEquals(nowZoned, entry.getEndWithTimezone());
        Assertions.assertEquals(nowPlusOffset, entry.getEndWithTimezone().toLocalDateTime());
        Assertions.assertEquals(nowPlusOffset, entry.getEndWithOffset());
        Assertions.assertEquals(nowPlusOffset, entry.getEndWithOffset(timezone));

        json = entry.toJson();
        Assertions.assertEquals(JSON_UTC_TIMESTAMP, json.getString(EntryKey.END.getName()));

        entry.setEndWithOffset(nowZoned.toLocalDateTime());
        Assertions.assertEquals(now, entry.getEnd());
        Assertions.assertEquals(nowInstant, entry.getEndAsInstant());
        Assertions.assertEquals(nowZoned, entry.getEndWithTimezone());
        Assertions.assertEquals(nowPlusOffset, entry.getEndWithTimezone().toLocalDateTime());
        Assertions.assertEquals(nowPlusOffset, entry.getEndWithOffset());
        Assertions.assertEquals(nowPlusOffset, entry.getEndWithOffset(timezone));

        json = entry.toJson();
        Assertions.assertEquals(JSON_UTC_TIMESTAMP, json.getString(EntryKey.END.getName()));

        entry.setEndWithOffset(nowZoned.toLocalDateTime(), timezone);
        Assertions.assertEquals(now, entry.getEnd());
        Assertions.assertEquals(nowInstant, entry.getEndAsInstant());
        Assertions.assertEquals(nowZoned, entry.getEndWithTimezone());
        Assertions.assertEquals(nowPlusOffset, entry.getEndWithTimezone().toLocalDateTime());
        Assertions.assertEquals(nowPlusOffset, entry.getEndWithOffset());
        Assertions.assertEquals(nowPlusOffset, entry.getEndWithOffset(timezone));

        json = entry.toJson();
        Assertions.assertEquals(JSON_UTC_TIMESTAMP, json.getString(EntryKey.END.getName()));

        LocalDate nowDate = now.toLocalDate();
        entry.setEnd(nowDate);

        Assertions.assertEquals(nowDate.atStartOfDay(), entry.getEnd());
        json = entry.toJson();
        Assertions.assertEquals(JSON_UTC_TIMESTAMP, json.getString(EntryKey.END.getName()));

    }


    @Test
    void testToJson() {
        Entry entry = new Entry(DEFAULT_ID);
        entry.setTitle(DEFAULT_TITLE);
        entry.setStart(DEFAULT_START);
        entry.setEnd(DEFAULT_END);
        entry.setAllDay(true);
        entry.setEditable(true);
        entry.setColor(DEFAULT_COLOR);
        entry.setDescription(DEFAULT_DESCRIPTION);
        entry.setRenderingMode(DEFAULT_RENDERING);

        JsonObject jsonObject = entry.toJson();

        Assertions.assertEquals(DEFAULT_ID, jsonObject.getString("id"));
        Assertions.assertEquals(DEFAULT_TITLE, jsonObject.getString(EntryKey.TITLE.getName()));
        Assertions.assertEquals(JsonUtils.formatClientSideDateTimeString(DEFAULT_START), jsonObject.getString(EntryKey.START.getName()));
        Assertions.assertEquals(JsonUtils.formatClientSideDateTimeString(DEFAULT_END), jsonObject.getString(EntryKey.END.getName()));
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
    void testUpdateEntryFromJsonWith() {
        JsonObject jsonObject = Json.createObject();
        jsonObject.put("id", "1");

        jsonObject.put(EntryKey.TITLE.getName(), DEFAULT_TITLE);
        jsonObject.put(EntryKey.START.getName(), JsonUtils.formatClientSideDateTimeString(DEFAULT_START));
        jsonObject.put(EntryKey.END.getName(), JsonUtils.formatClientSideDateTimeString(DEFAULT_END));
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
        Assertions.assertEquals(DEFAULT_START, entry.getStart());
        Assertions.assertEquals(DEFAULT_END, entry.getEnd());

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
        Assertions.assertEquals(RenderingMode.AUTO, entry.getRenderingMode());
    }

}