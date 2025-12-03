package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.function.ValueProvider;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.json.JsonName;

import java.lang.reflect.Field;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.vaadin.stefan.fullcalendar.Entry.*;

public class EntryTest {

    public static final LocalDateTime DEFAULT_START = LocalDate.of(2000, 1, 1).atStartOfDay();
    public static final LocalDateTime DEFAULT_END = LocalDate.of(2000, 1, 1).plusDays(1).atStartOfDay();

    public static final String DEFAULT_STRING = "test";
    public static final String DEFAULT_ID = DEFAULT_STRING + 1;
    public static final String DEFAULT_TITLE = DEFAULT_STRING + 2;
    public static final String DEFAULT_COLOR = DEFAULT_STRING + 3;
    public static final String DEFAULT_DESCRIPTION = DEFAULT_STRING + 4;
    public static final DisplayMode DEFAULT_DISPLAY_MODE_ALTERN = DisplayMode.BACKGROUND;
    public static final Timezone CUSTOM_TIMEZONE = new Timezone(ZoneId.of("Europe/Berlin"));

    public static final String FULL_CALENDAR_HTML = "fullcalendar/full-calendar.html";

    public static final Map<String, Object> DEFAULTS = new HashMap<>();
    public static final String JSON_UTC_TIMESTAMP = JsonUtils.formatClientSideDateTimeString(DEFAULT_START);

    static {
        DEFAULTS.put(Fields.TITLE, DEFAULT_STRING);
        DEFAULTS.put(Fields.START, DEFAULT_START);
        DEFAULTS.put(Fields.END, DEFAULT_END);
        DEFAULTS.put(Fields.COLOR, DEFAULT_COLOR);
        DEFAULTS.put(Fields.DISPLAY_MODE,
                DEFAULT_DISPLAY_MODE_ALTERN);
    }

    /**
     * Compares two entries
     *
     * @param expected expected entry
     * @param actual   actual entry
     */
    static void assertFullEqualsByJsonAttributes(Entry expected, Entry actual) {
        assertEquals(expected.getId(), actual.getId());
        // we use UTC, since LDT or ZoneDT can differ whether the entry has a calendar assigned or not

        expected.streamUpdateableProperties().forEach(def -> {
            ValueProvider<Entry, Object> getter = def.getGetter();
            assertEquals(getter.apply(expected), getter.apply(actual));
        });
    }

    @Test
    void testNoArgsConstructor() {
        Entry entry = new Entry();

        // test id generation
        String id = entry.getId();
        assertNotNull(id);
        assertFalse(id.isEmpty());

        //noinspection ResultOfMethodCallIgnored
        UUID.fromString(id);

        // test if is editable
        assertTrue(entry.isEditable());

        assertNull(entry.getStart());
        assertNull(entry.getEnd());
    }

    @Test
    void testIdArgConstructor() {
        Entry entry = new Entry(null);

        // test id generation
        String id = entry.getId();
        assertNotNull(id);
        assertFalse(id.isEmpty());
        assertEquals(id, UUID.fromString(id).toString());

        entry = new Entry("1");
        assertEquals("1", entry.getId());
    }

    @Test
    void testToJsonEmpty() {
        Entry entry = new Entry();
        JsonObject jsonObject = entry.toJson();


        Set<String> defaultKeys = new HashSet<>(Arrays.asList(
                        Fields.ID,
                        Fields.EDITABLE,
                        Fields.DURATION_EDITABLE,
                        Fields.START_EDITABLE,
                        Fields.ALL_DAY,
                        Fields.OVERLAP,
                        Fields.DISPLAY_MODE
                ))
                .stream()
                .map(EntryTest::getEntryFieldJsonName)
                .collect(Collectors.toSet());

        Assertions.assertEquals(entry.getId(), jsonObject.getString(Fields.ID));

        for (String key : jsonObject.keys()) {
            if (defaultKeys.contains(key)) {
                Assertions.assertTrue(jsonObject.hasKey(key), key);
            } else {
                Assertions.assertFalse(jsonObject.hasKey(key), key);
            }
        }
    }

    /**
     * Returns the json key to be used for the given field. Expects the field name as it is set in the class
     * and returns either the field name or the name given in the {@link JsonName} annotation.
     * @see Entry.Fields
     */
    private static String getEntryFieldJsonName(String fieldName) {
        Field field = FieldUtils.getField(Entry.class, fieldName, true);
        JsonName nameAnnotation = field.getAnnotation(JsonName.class);
        if (nameAnnotation != null) {
            return nameAnnotation.value();
        }
        return fieldName;
    }

    @Test
    void test_startMethods_nullSafety() {
        Entry entry = new Entry();

        assertNull(entry.getStart());
        assertNull(entry.getStartAsInstant());
        assertNull(entry.getStartAsLocalDate());
        assertNull(entry.getStartWithTimezone());
        assertNull(entry.getStartWithOffset());
        assertNull(entry.getStartWithOffset(Timezone.UTC));

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

        assertEquals(now, entry.getStart());
        assertEquals(nowInstant, entry.getStartAsInstant());
        assertEquals(nowZoned, entry.getStartWithTimezone());
        assertEquals(nowPlusOffset, entry.getStartWithTimezone().toLocalDateTime());
        assertEquals(nowPlusOffset, entry.getStartWithOffset());
        assertEquals(nowPlusOffset, entry.getStartWithOffset(Timezone.UTC));
        JsonObject json = entry.toJson();
        assertEquals(JSON_UTC_TIMESTAMP, json.getString(Fields.START));


        entry.setStart(nowInstant);
        assertEquals(now, entry.getStart());
        assertEquals(nowInstant, entry.getStartAsInstant());
        assertEquals(nowZoned, entry.getStartWithTimezone());
        assertEquals(nowPlusOffset, entry.getStartWithTimezone().toLocalDateTime());
        assertEquals(nowPlusOffset, entry.getStartWithOffset());
        assertEquals(nowPlusOffset, entry.getStartWithOffset(Timezone.UTC));
        json = entry.toJson();
        assertEquals(JSON_UTC_TIMESTAMP, json.getString(Fields.START));


        entry.setStartWithTimezone(nowZoned);
        assertEquals(now, entry.getStart());
        assertEquals(nowInstant, entry.getStartAsInstant());
        assertEquals(nowZoned, entry.getStartWithTimezone());
        assertEquals(nowPlusOffset, entry.getStartWithTimezone().toLocalDateTime());
        assertEquals(nowPlusOffset, entry.getStartWithOffset());
        assertEquals(nowPlusOffset, entry.getStartWithOffset(Timezone.UTC));
        json = entry.toJson();
        assertEquals(JSON_UTC_TIMESTAMP, json.getString(Fields.START));


        entry.setStartWithOffset(nowZoned.toLocalDateTime());
        assertEquals(now, entry.getStart());
        assertEquals(nowInstant, entry.getStartAsInstant());
        assertEquals(nowZoned, entry.getStartWithTimezone());
        assertEquals(nowPlusOffset, entry.getStartWithTimezone().toLocalDateTime());
        assertEquals(nowPlusOffset, entry.getStartWithOffset());
        assertEquals(nowPlusOffset, entry.getStartWithOffset(Timezone.UTC));
        json = entry.toJson();
        assertEquals(JSON_UTC_TIMESTAMP, json.getString(Fields.START));

        entry.setStartWithOffset(nowZoned.toLocalDateTime(), Timezone.UTC);
        assertEquals(now, entry.getStart());
        assertEquals(nowInstant, entry.getStartAsInstant());
        assertEquals(nowZoned, entry.getStartWithTimezone());
        assertEquals(nowPlusOffset, entry.getStartWithTimezone().toLocalDateTime());
        assertEquals(nowPlusOffset, entry.getStartWithOffset());
        assertEquals(nowPlusOffset, entry.getStartWithOffset(Timezone.UTC));
        json = entry.toJson();
        assertEquals(JSON_UTC_TIMESTAMP, json.getString(Fields.START));


        LocalDate nowDate = now.toLocalDate();
        entry.setStart(nowDate);

        assertEquals(nowDate.atStartOfDay(), entry.getStart());
        json = entry.toJson();
        assertEquals(JSON_UTC_TIMESTAMP, json.getString(Fields.START));
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
        calendar.getEntryProvider().asInMemory().addEntry(entry);

        assertEquals(now, entry.getStart());
        assertEquals(nowInstant, entry.getStartAsInstant());
        assertEquals(nowZoned, entry.getStartWithTimezone());
        assertEquals(nowPlusOffset, entry.getStartWithTimezone().toLocalDateTime());
        assertEquals(nowPlusOffset, entry.getStartWithOffset());
        assertEquals(nowPlusOffset, entry.getStartWithOffset(timezone));
        JsonObject json = entry.toJson();
        assertEquals(JSON_UTC_TIMESTAMP, json.getString(Fields.START));


        entry.setStart(nowInstant);
        assertEquals(now, entry.getStart());
        assertEquals(nowInstant, entry.getStartAsInstant());
        assertEquals(nowZoned, entry.getStartWithTimezone());
        assertEquals(nowPlusOffset, entry.getStartWithTimezone().toLocalDateTime());
        assertEquals(nowPlusOffset, entry.getStartWithOffset());
        assertEquals(nowPlusOffset, entry.getStartWithOffset(timezone));
        json = entry.toJson();
        assertEquals(JSON_UTC_TIMESTAMP, json.getString(Fields.START));


        entry.setStartWithTimezone(nowZoned);
        assertEquals(now, entry.getStart());
        assertEquals(nowInstant, entry.getStartAsInstant());
        assertEquals(nowZoned, entry.getStartWithTimezone());
        assertEquals(nowPlusOffset, entry.getStartWithTimezone().toLocalDateTime());
        assertEquals(nowPlusOffset, entry.getStartWithOffset());
        assertEquals(nowPlusOffset, entry.getStartWithOffset(timezone));
        json = entry.toJson();
        assertEquals(JSON_UTC_TIMESTAMP, json.getString(Fields.START));

        entry.setStartWithOffset(nowZoned.toLocalDateTime());
        assertEquals(now, entry.getStart());
        assertEquals(nowInstant, entry.getStartAsInstant());
        assertEquals(nowZoned, entry.getStartWithTimezone());
        assertEquals(nowPlusOffset, entry.getStartWithTimezone().toLocalDateTime());
        assertEquals(nowPlusOffset, entry.getStartWithOffset());
        assertEquals(nowPlusOffset, entry.getStartWithOffset(timezone));
        json = entry.toJson();
        assertEquals(JSON_UTC_TIMESTAMP, json.getString(Fields.START));

        entry.setStartWithOffset(nowZoned.toLocalDateTime(), timezone);
        assertEquals(now, entry.getStart());
        assertEquals(nowInstant, entry.getStartAsInstant());
        assertEquals(nowZoned, entry.getStartWithTimezone());
        assertEquals(nowPlusOffset, entry.getStartWithTimezone().toLocalDateTime());
        assertEquals(nowPlusOffset, entry.getStartWithOffset());
        assertEquals(nowPlusOffset, entry.getStartWithOffset(timezone));
        json = entry.toJson();
        assertEquals(JSON_UTC_TIMESTAMP, json.getString(Fields.START));

        LocalDate nowDate = now.toLocalDate();
        entry.setStart(nowDate);

        assertEquals(nowDate.atStartOfDay(), entry.getStart());
        json = entry.toJson();
        assertEquals(JSON_UTC_TIMESTAMP, json.getString(Fields.START));

    }

    @Test
    void test_endMethods_nullSafety() {
        Entry entry = new Entry();

        assertNull(entry.getEnd());
        assertNull(entry.getEndAsInstant());
        assertNull(entry.getEndAsLocalDate());
        assertNull(entry.getEndWithTimezone());
        assertNull(entry.getEndWithOffset());
        assertNull(entry.getEndWithOffset(Timezone.UTC));

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

        assertEquals(now, entry.getEnd());
        assertEquals(nowInstant, entry.getEndAsInstant());
        assertEquals(nowZoned, entry.getEndWithTimezone());
        assertEquals(nowPlusOffset, entry.getEndWithTimezone().toLocalDateTime());
        assertEquals(nowPlusOffset, entry.getEndWithOffset());
        assertEquals(nowPlusOffset, entry.getEndWithOffset(Timezone.UTC));

        JsonObject json = entry.toJson();
        assertEquals(JSON_UTC_TIMESTAMP, json.getString(Fields.END));

        entry.setEnd(nowInstant);
        assertEquals(now, entry.getEnd());
        assertEquals(nowInstant, entry.getEndAsInstant());
        assertEquals(nowZoned, entry.getEndWithTimezone());
        assertEquals(nowPlusOffset, entry.getEndWithTimezone().toLocalDateTime());
        assertEquals(nowPlusOffset, entry.getEndWithOffset());
        assertEquals(nowPlusOffset, entry.getEndWithOffset(Timezone.UTC));

        json = entry.toJson();
        assertEquals(JSON_UTC_TIMESTAMP, json.getString(Fields.END));

        entry.setEndWithTimezone(nowZoned);
        assertEquals(now, entry.getEnd());
        assertEquals(nowInstant, entry.getEndAsInstant());
        assertEquals(nowZoned, entry.getEndWithTimezone());
        assertEquals(nowPlusOffset, entry.getEndWithTimezone().toLocalDateTime());
        assertEquals(nowPlusOffset, entry.getEndWithOffset());
        assertEquals(nowPlusOffset, entry.getEndWithOffset(Timezone.UTC));

        json = entry.toJson();
        assertEquals(JSON_UTC_TIMESTAMP, json.getString(Fields.END));

        entry.setEndWithOffset(nowZoned.toLocalDateTime());
        assertEquals(now, entry.getEnd());
        assertEquals(nowInstant, entry.getEndAsInstant());
        assertEquals(nowZoned, entry.getEndWithTimezone());
        assertEquals(nowPlusOffset, entry.getEndWithTimezone().toLocalDateTime());
        assertEquals(nowPlusOffset, entry.getEndWithOffset());
        assertEquals(nowPlusOffset, entry.getEndWithOffset(Timezone.UTC));

        json = entry.toJson();
        assertEquals(JSON_UTC_TIMESTAMP, json.getString(Fields.END));

        entry.setEndWithOffset(nowZoned.toLocalDateTime(), Timezone.UTC);
        assertEquals(now, entry.getEnd());
        assertEquals(nowInstant, entry.getEndAsInstant());
        assertEquals(nowZoned, entry.getEndWithTimezone());
        assertEquals(nowPlusOffset, entry.getEndWithTimezone().toLocalDateTime());
        assertEquals(nowPlusOffset, entry.getEndWithOffset());
        assertEquals(nowPlusOffset, entry.getEndWithOffset(Timezone.UTC));

        json = entry.toJson();
        assertEquals(JSON_UTC_TIMESTAMP, json.getString(Fields.END));

        LocalDate nowDate = now.toLocalDate();
        entry.setEnd(nowDate);
        assertEquals(nowDate.atStartOfDay(), entry.getEnd());

        json = entry.toJson();
        assertEquals(JSON_UTC_TIMESTAMP, json.getString(Fields.END));
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
        calendar.getEntryProvider().asInMemory().addEntry(entry);

        assertEquals(now, entry.getEnd());
        assertEquals(nowInstant, entry.getEndAsInstant());
        assertEquals(nowZoned, entry.getEndWithTimezone());
        assertEquals(nowPlusOffset, entry.getEndWithTimezone().toLocalDateTime());
        assertEquals(nowPlusOffset, entry.getEndWithOffset());
        assertEquals(nowPlusOffset, entry.getEndWithOffset(timezone));

        JsonObject json = entry.toJson();
        assertEquals(JSON_UTC_TIMESTAMP, json.getString(Fields.END));

        entry.setEnd(nowInstant);
        assertEquals(now, entry.getEnd());
        assertEquals(nowInstant, entry.getEndAsInstant());
        assertEquals(nowZoned, entry.getEndWithTimezone());
        assertEquals(nowPlusOffset, entry.getEndWithTimezone().toLocalDateTime());
        assertEquals(nowPlusOffset, entry.getEndWithOffset());
        assertEquals(nowPlusOffset, entry.getEndWithOffset(timezone));

        json = entry.toJson();
        assertEquals(JSON_UTC_TIMESTAMP, json.getString(Fields.END));

        entry.setEndWithTimezone(nowZoned);
        assertEquals(now, entry.getEnd());
        assertEquals(nowInstant, entry.getEndAsInstant());
        assertEquals(nowZoned, entry.getEndWithTimezone());
        assertEquals(nowPlusOffset, entry.getEndWithTimezone().toLocalDateTime());
        assertEquals(nowPlusOffset, entry.getEndWithOffset());
        assertEquals(nowPlusOffset, entry.getEndWithOffset(timezone));

        json = entry.toJson();
        assertEquals(JSON_UTC_TIMESTAMP, json.getString(Fields.END));

        entry.setEndWithOffset(nowZoned.toLocalDateTime());
        assertEquals(now, entry.getEnd());
        assertEquals(nowInstant, entry.getEndAsInstant());
        assertEquals(nowZoned, entry.getEndWithTimezone());
        assertEquals(nowPlusOffset, entry.getEndWithTimezone().toLocalDateTime());
        assertEquals(nowPlusOffset, entry.getEndWithOffset());
        assertEquals(nowPlusOffset, entry.getEndWithOffset(timezone));

        json = entry.toJson();
        assertEquals(JSON_UTC_TIMESTAMP, json.getString(Fields.END));

        entry.setEndWithOffset(nowZoned.toLocalDateTime(), timezone);
        assertEquals(now, entry.getEnd());
        assertEquals(nowInstant, entry.getEndAsInstant());
        assertEquals(nowZoned, entry.getEndWithTimezone());
        assertEquals(nowPlusOffset, entry.getEndWithTimezone().toLocalDateTime());
        assertEquals(nowPlusOffset, entry.getEndWithOffset());
        assertEquals(nowPlusOffset, entry.getEndWithOffset(timezone));

        json = entry.toJson();
        assertEquals(JSON_UTC_TIMESTAMP, json.getString(Fields.END));

        LocalDate nowDate = now.toLocalDate();
        entry.setEnd(nowDate);

        assertEquals(nowDate.atStartOfDay(), entry.getEnd());
        json = entry.toJson();
        assertEquals(JSON_UTC_TIMESTAMP, json.getString(Fields.END));

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
        entry.setDisplayMode(DEFAULT_DISPLAY_MODE_ALTERN);

        // TODO extend values

        JsonObject jsonObject = entry.toJson();

        assertEquals(DEFAULT_ID, jsonObject.getString(Fields.ID));
        assertEquals(DEFAULT_TITLE, jsonObject.getString(getEntryFieldJsonName(Fields.TITLE)));
        assertEquals(JsonUtils.formatClientSideDateTimeString(DEFAULT_START), jsonObject.getString(getEntryFieldJsonName(Fields.START)));
        assertEquals(JsonUtils.formatClientSideDateTimeString(DEFAULT_END), jsonObject.getString(getEntryFieldJsonName(Fields.END)));
        assertTrue(jsonObject.getBoolean(getEntryFieldJsonName(Fields.ALL_DAY)));
        assertTrue(jsonObject.getBoolean(getEntryFieldJsonName(Fields.EDITABLE)));
        assertEquals(DEFAULT_COLOR, jsonObject.getString(getEntryFieldJsonName(Fields.COLOR)));
        assertEquals(DEFAULT_DISPLAY_MODE_ALTERN.getClientSideValue(), jsonObject.getString(getEntryFieldJsonName(Fields.DISPLAY_MODE)));
    }

    @Test
    void testIfUpdateFromJsonFailsOnNonMatchingId() {
        Entry entry = new Entry();

        JsonObject jsonObject = Json.createObject();
        jsonObject.put(Fields.ID, "someNonUUID");

        assertDoesNotThrow(() -> entry.updateFromJson(jsonObject, false));
        assertThrows(IllegalArgumentException.class, () -> entry.updateFromJson(jsonObject, true));
    }

    @Test
    void testUpdateEntryFromJsonWith() {
        JsonObject jsonObject = Json.createObject();
        jsonObject.put(Fields.ID, "1");

        jsonObject.put(Fields.TITLE, DEFAULT_TITLE);
        jsonObject.put(Fields.START, JsonUtils.formatClientSideDateTimeString(DEFAULT_START));
        jsonObject.put(Fields.END, JsonUtils.formatClientSideDateTimeString(DEFAULT_END));
        jsonObject.put(Fields.ALL_DAY, true);
        jsonObject.put(Fields.EDITABLE, false);
        jsonObject.put(Fields.DURATION_EDITABLE, false);
        jsonObject.put(Fields.START_EDITABLE, false);
        jsonObject.put(Fields.COLOR, DEFAULT_COLOR);
        jsonObject.put("description", DEFAULT_DESCRIPTION);
        jsonObject.put(Fields.DISPLAY_MODE, DEFAULT_DISPLAY_MODE_ALTERN.getClientSideValue());

        Entry entry = new Entry("1");
        assertTrue(entry.isValidJsonSource(jsonObject));
        entry.updateFromJson(jsonObject);

        // affected properties
        assertTrue(entry.isAllDay());
        assertEquals(DEFAULT_START, entry.getStart());
        assertEquals(DEFAULT_END, entry.getEnd());

        // by json unaffected properties
        assertTrue(entry.isEditable());
        assertTrue(entry.isStartEditable());
        assertTrue(entry.isDurationEditable());
        assertFalse(entry.isRecurring());
        assertNull(entry.getTitle());
        assertNull(entry.getColor());
        assertNull(entry.getBorderColor());
        assertNull(entry.getBackgroundColor());
        assertNull(entry.getTextColor());
        assertNull(entry.getDescription());
        assertEquals(DisplayMode.AUTO, entry.getDisplayMode());
    }

}