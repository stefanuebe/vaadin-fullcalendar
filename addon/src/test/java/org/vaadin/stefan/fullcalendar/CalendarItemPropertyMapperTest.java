package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.spike.SamplePojo;
import tools.jackson.databind.node.ObjectNode;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the production {@link CalendarItemPropertyMapper}.
 * Evolved from the spike CalendarItemPropertyMapperTest with additional coverage
 * for recurring time, getId(), display mode conversion, and applyChanges validation.
 */
class CalendarItemPropertyMapperTest {

    static final LocalDateTime START = LocalDateTime.of(2024, 3, 15, 14, 30);
    static final LocalDateTime END = LocalDateTime.of(2024, 3, 15, 15, 30);

    SamplePojo pojo;

    @BeforeEach
    void setUp() {
        pojo = new SamplePojo("meeting-1", "Team Standup", START, END);
        pojo.setCategoryColor("#3788d8");
        pojo.setCanEdit(true);
        pojo.setFullDay(false);
    }

    @Nested
    @DisplayName("Lambda-based read mapping")
    class LambdaReadMapping {

        @Test
        @DisplayName("should serialize basic properties to JSON")
        void basicSerialization() {
            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id(SamplePojo::getId)
                    .title(SamplePojo::getName)
                    .start(SamplePojo::getFrom)
                    .end(SamplePojo::getTo)
                    .color(SamplePojo::getCategoryColor)
                    .editable(SamplePojo::isCanEdit)
                    .allDay(SamplePojo::isFullDay);

            ObjectNode json = mapper.toJson(pojo);

            assertEquals("meeting-1", json.get("id").asString());
            assertEquals("Team Standup", json.get("title").asString());
            assertEquals(JsonUtils.formatClientSideDateTimeString(START), json.get("start").asString());
            assertEquals(JsonUtils.formatClientSideDateTimeString(END), json.get("end").asString());
            assertEquals("#3788d8", json.get("color").asString());
            assertTrue(json.get("editable").asBoolean());
            assertFalse(json.get("allDay").asBoolean());
        }

        @Test
        @DisplayName("should omit null properties from JSON")
        void nullHandling() {
            var pojo = new SamplePojo("id-1", null, null, null);

            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id(SamplePojo::getId)
                    .title(SamplePojo::getName)
                    .start(SamplePojo::getFrom)
                    .end(SamplePojo::getTo);

            ObjectNode json = mapper.toJson(pojo);

            assertEquals("id-1", json.get("id").asString());
            assertFalse(json.hasNonNull("title"));
            assertFalse(json.hasNonNull("start"));
            assertFalse(json.hasNonNull("end"));
        }

        @Test
        @DisplayName("should serialize computed properties via lambda")
        void computedProperties() {
            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id(SamplePojo::getId)
                    .title(p -> p.getName() + " (" + p.getRoom() + ")")
                    .start(SamplePojo::getFrom)
                    .end(p -> p.getFrom().plusHours(1));

            pojo.setRoom("Room A");

            ObjectNode json = mapper.toJson(pojo);

            assertEquals("Team Standup (Room A)", json.get("title").asString());
            assertEquals(
                    JsonUtils.formatClientSideDateTimeString(START.plusHours(1)),
                    json.get("end").asString()
            );
        }

        @Test
        @DisplayName("should serialize recurring properties")
        void recurringProperties() {
            pojo.setWeekDays(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY));
            pojo.setRepeatStart(LocalDate.of(2024, 1, 1));

            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id(SamplePojo::getId)
                    .title(SamplePojo::getName)
                    .recurringDaysOfWeek(SamplePojo::getWeekDays)
                    .recurringStartDate(SamplePojo::getRepeatStart);

            ObjectNode json = mapper.toJson(pojo);

            assertTrue(json.has("daysOfWeek"));
            assertTrue(json.get("daysOfWeek").isArray());
            assertEquals("2024-01-01", json.get("startRecur").asString());
        }

        @Test
        @DisplayName("should serialize classNames and customProperties")
        void collectionsAndMaps() {
            pojo.setTags(Set.of("important", "recurring"));
            pojo.setMetadata(Map.of("priority", 1, "department", "engineering"));

            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id(SamplePojo::getId)
                    .classNames(SamplePojo::getTags)
                    .customProperties(SamplePojo::getMetadata);

            ObjectNode json = mapper.toJson(pojo);

            assertTrue(json.get("classNames").isArray());
            assertTrue(json.get("customProperties").isObject());
        }
    }

    @Nested
    @DisplayName("String-based (reflection) mapping")
    class StringMapping {

        @Test
        @DisplayName("should map POJO fields by name via reflection")
        void basicReflection() {
            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id(SamplePojo::getId)
                    .map("title", "name")
                    .map("color", "categoryColor");

            ObjectNode json = mapper.toJson(pojo);

            assertEquals("Team Standup", json.get("title").asString());
            assertEquals("#3788d8", json.get("color").asString());
        }

        @Test
        @DisplayName("should throw for non-existent field")
        void invalidField() {
            assertThrows(IllegalArgumentException.class, () ->
                    CalendarItemPropertyMapper.of(SamplePojo.class)
                            .map("title", "nonExistentField")
            );
        }

        @Test
        @DisplayName("string-based id mapping works via reflection")
        void stringBasedId() {
            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id("id")
                    .title("name");

            ObjectNode json = mapper.toJson(pojo);
            assertEquals("meeting-1", json.get("id").asString());
            assertEquals("Team Standup", json.get("title").asString());
        }
    }

    @Nested
    @DisplayName("getId() top-level method")
    class GetId {

        @Test
        @DisplayName("getId extracts the mapped ID")
        void extractsId() {
            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id(SamplePojo::getId)
                    .title(SamplePojo::getName);

            assertEquals("meeting-1", mapper.getId(pojo));
        }

        @Test
        @DisplayName("getId throws if no id mapping")
        void throwsWithoutIdMapping() {
            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .title(SamplePojo::getName);

            assertThrows(IllegalStateException.class, () -> mapper.getId(pojo));
        }
    }

    @Nested
    @DisplayName("Bidirectional mapping (Strategy A: setters)")
    class BidirectionalMapping {

        @Test
        @DisplayName("should apply changes to POJO via setters")
        void applyChanges() {
            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id(SamplePojo::getId)
                    .title(SamplePojo::getName)
                    .start(SamplePojo::getFrom, SamplePojo::setFrom)
                    .end(SamplePojo::getTo, SamplePojo::setTo)
                    .allDay(SamplePojo::isFullDay, SamplePojo::setFullDay);

            LocalDateTime newStart = LocalDateTime.of(2024, 3, 16, 10, 0);
            LocalDateTime newEnd = LocalDateTime.of(2024, 3, 16, 11, 0);

            ObjectNode changes = JsonFactory.createObject();
            changes.put("start", JsonUtils.formatClientSideDateTimeString(newStart));
            changes.put("end", JsonUtils.formatClientSideDateTimeString(newEnd));
            changes.put("allDay", true);

            mapper.applyChanges(pojo, changes);

            assertEquals(newStart, pojo.getFrom());
            assertEquals(newEnd, pojo.getTo());
            assertTrue(pojo.isFullDay());
        }

        @Test
        @DisplayName("should only apply properties present in JSON")
        void partialChanges() {
            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id(SamplePojo::getId)
                    .start(SamplePojo::getFrom, SamplePojo::setFrom)
                    .end(SamplePojo::getTo, SamplePojo::setTo);

            LocalDateTime newStart = LocalDateTime.of(2024, 3, 16, 10, 0);

            ObjectNode changes = JsonFactory.createObject();
            changes.put("start", JsonUtils.formatClientSideDateTimeString(newStart));

            mapper.applyChanges(pojo, changes);

            assertEquals(newStart, pojo.getFrom());
            assertEquals(END, pojo.getTo()); // unchanged
        }

        @Test
        @DisplayName("applyChanges throws if no setters registered")
        void applyChangesThrowsWithoutSetters() {
            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id(SamplePojo::getId)
                    .start(SamplePojo::getFrom);

            ObjectNode changes = JsonFactory.createObject();
            changes.put("start", JsonUtils.formatClientSideDateTimeString(START));

            assertThrows(IllegalStateException.class,
                    () -> mapper.applyChanges(pojo, changes));
        }

        @Test
        @DisplayName("hasSetters() should return true when setters registered")
        void hasSettersTrue() {
            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id(SamplePojo::getId)
                    .start(SamplePojo::getFrom, SamplePojo::setFrom);

            assertTrue(mapper.hasSetters());
        }

        @Test
        @DisplayName("hasSetters() should return false when no setters registered")
        void hasSettersFalse() {
            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id(SamplePojo::getId)
                    .start(SamplePojo::getFrom);

            assertFalse(mapper.hasSetters());
        }
    }

    @Nested
    @DisplayName("forItem() bound mapper pattern")
    class BoundMapperPattern {

        @Test
        @DisplayName("should access properties via bound mapper")
        void boundAccess() {
            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id(SamplePojo::getId)
                    .title(SamplePojo::getName)
                    .start(SamplePojo::getFrom)
                    .end(SamplePojo::getTo);

            var bound = mapper.forItem(pojo);

            assertEquals("meeting-1", bound.getId());
            assertEquals("Team Standup", bound.getTitle());
            assertEquals(START, bound.getStart());
            assertEquals(END, bound.getEnd());
        }

        @Test
        @DisplayName("should produce same JSON as direct toJson()")
        void boundJsonEquivalence() {
            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id(SamplePojo::getId)
                    .title(SamplePojo::getName)
                    .start(SamplePojo::getFrom)
                    .end(SamplePojo::getTo);

            ObjectNode directJson = mapper.toJson(pojo);
            ObjectNode boundJson = mapper.forItem(pojo).toJson();

            assertEquals(directJson.toString(), boundJson.toString());
        }

        @Test
        @DisplayName("bound mapper rebinds to different items")
        void rebinding() {
            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id(SamplePojo::getId)
                    .title(SamplePojo::getName);

            var bound1 = mapper.forItem(pojo);
            assertEquals("Team Standup", bound1.getTitle());

            var pojo2 = new SamplePojo("meeting-2", "Sprint Review", null, null);
            var bound2 = mapper.forItem(pojo2);
            assertEquals("Sprint Review", bound2.getTitle());
        }

        @Test
        @DisplayName("getItem returns the bound item")
        void getItem() {
            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id(SamplePojo::getId);

            var bound = mapper.forItem(pojo);
            assertSame(pojo, bound.getItem());
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("validate() throws if id mapping missing")
        void missingIdThrows() {
            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .title(SamplePojo::getName);

            assertThrows(IllegalStateException.class, mapper::validate);
        }

        @Test
        @DisplayName("validate() passes when id is mapped")
        void withIdPasses() {
            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id(SamplePojo::getId);

            assertDoesNotThrow(mapper::validate);
        }

        @Test
        @DisplayName("toJson() auto-validates and throws if id missing")
        void toJsonAutoValidates() {
            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .title(SamplePojo::getName);

            assertThrows(IllegalStateException.class, () -> mapper.toJson(pojo));
        }
    }

    @Nested
    @DisplayName("Immutability after first use (freeze)")
    class Freezing {

        @Test
        @DisplayName("mapper is frozen after first toJson() call")
        void frozenAfterToJson() {
            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id(SamplePojo::getId);

            mapper.toJson(pojo);

            assertThrows(IllegalStateException.class, () ->
                    mapper.title(SamplePojo::getName)
            );
        }

        @Test
        @DisplayName("mapper is frozen after first forItem() call")
        void frozenAfterForItem() {
            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id(SamplePojo::getId);

            mapper.forItem(pojo);

            assertThrows(IllegalStateException.class, () ->
                    mapper.title(SamplePojo::getName)
            );
        }

        @Test
        @DisplayName("mapper is frozen after validate() call")
        void frozenAfterValidate() {
            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id(SamplePojo::getId);

            mapper.validate();

            assertThrows(IllegalStateException.class, () ->
                    mapper.title(SamplePojo::getName)
            );
        }
    }

    @Nested
    @DisplayName("Type conversions")
    class TypeConversions {

        @Test
        @DisplayName("LocalDateTime serializes to ISO UTC string")
        void localDateTimeSerialization() {
            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id(SamplePojo::getId)
                    .start(SamplePojo::getFrom);

            ObjectNode json = mapper.toJson(pojo);

            String expected = JsonUtils.formatClientSideDateTimeString(START);
            assertEquals(expected, json.get("start").asString());
            assertTrue(expected.endsWith("Z"), "Should end with Z for UTC");
        }

        @Test
        @DisplayName("LocalDate serializes to date-only string")
        void localDateSerialization() {
            pojo.setRepeatStart(LocalDate.of(2024, 1, 15));

            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id(SamplePojo::getId)
                    .recurringStartDate(SamplePojo::getRepeatStart);

            ObjectNode json = mapper.toJson(pojo);

            assertEquals("2024-01-15", json.get("startRecur").asString());
        }

        @Test
        @DisplayName("DayOfWeek set serializes to int array (Sunday=0)")
        void dayOfWeekSerialization() {
            pojo.setWeekDays(new LinkedHashSet<>(List.of(DayOfWeek.MONDAY, DayOfWeek.SUNDAY)));

            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id(SamplePojo::getId)
                    .recurringDaysOfWeek(SamplePojo::getWeekDays);

            ObjectNode json = mapper.toJson(pojo);

            var array = json.get("daysOfWeek");
            assertTrue(array.isArray());

            Set<Integer> values = new HashSet<>();
            array.forEach(node -> values.add(node.asInt()));
            assertTrue(values.contains(0), "Sunday should map to 0");
            assertTrue(values.contains(1), "Monday should map to 1");
        }

        @Test
        @DisplayName("RecurringTime serializes to formatted string")
        void recurringTimeSerialization() {
            // Use a custom POJO with RecurringTime to test
            var mapper = CalendarItemPropertyMapper.of(RecurringTimePojo.class)
                    .id(RecurringTimePojo::getId)
                    .recurringStartTime(RecurringTimePojo::getStartTime)
                    .recurringEndTime(RecurringTimePojo::getEndTime);

            var rtPojo = new RecurringTimePojo("rt-1",
                    RecurringTime.of(9, 30),
                    RecurringTime.of(17, 0));

            ObjectNode json = mapper.toJson(rtPojo);

            assertEquals("09:30", json.get("startTime").asString());
            assertEquals("17:00", json.get("endTime").asString());
        }

        @Test
        @DisplayName("RecurringTime above 24h serializes correctly")
        void recurringTimeAbove24h() {
            var mapper = CalendarItemPropertyMapper.of(RecurringTimePojo.class)
                    .id(RecurringTimePojo::getId)
                    .recurringStartTime(RecurringTimePojo::getStartTime);

            var rtPojo = new RecurringTimePojo("rt-2", RecurringTime.of(25, 30), null);

            ObjectNode json = mapper.toJson(rtPojo);

            assertEquals("25:30", json.get("startTime").asString());
        }

        @Test
        @DisplayName("DisplayMode serializes to client-side value")
        void displayModeSerialization() {
            var mapper = CalendarItemPropertyMapper.of(DisplayModePojo.class)
                    .id(DisplayModePojo::getId)
                    .display(DisplayModePojo::getDisplayMode);

            var dmPojo = new DisplayModePojo("dm-1", DisplayMode.BACKGROUND);

            ObjectNode json = mapper.toJson(dmPojo);

            assertEquals("background", json.get("display").asString());
        }

        @Test
        @DisplayName("DisplayMode.NONE serializes to null (omitted)")
        void displayModeNone() {
            var mapper = CalendarItemPropertyMapper.of(DisplayModePojo.class)
                    .id(DisplayModePojo::getId)
                    .display(DisplayModePojo::getDisplayMode);

            var dmPojo = new DisplayModePojo("dm-2", DisplayMode.NONE);

            ObjectNode json = mapper.toJson(dmPojo);

            assertFalse(json.hasNonNull("display"));
        }

        @Test
        @DisplayName("LocalDateTime roundtrip: serialize then deserialize via applyChanges")
        void localDateTimeRoundtrip() {
            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id(SamplePojo::getId)
                    .start(SamplePojo::getFrom, SamplePojo::setFrom)
                    .end(SamplePojo::getTo, SamplePojo::setTo);

            ObjectNode json = mapper.toJson(pojo);

            var newPojo = new SamplePojo("meeting-1", "Test", null, null);
            mapper.applyChanges(newPojo, json);

            assertEquals(START, newPojo.getFrom());
            assertEquals(END, newPojo.getTo());
        }
    }

    @Nested
    @DisplayName("Resource mappings (scheduler)")
    class ResourceMappings {

        @Test
        @DisplayName("resourceIds read-only serializes to JSON array")
        void resourceIdsReadOnly() {
            pojo.setResourceIds(new LinkedHashSet<>(List.of("r1", "r2", "r3")));

            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id(SamplePojo::getId)
                    .resourceIds(SamplePojo::getResourceIds);

            ObjectNode json = mapper.toJson(pojo);

            assertTrue(json.has("resourceIds"));
            assertTrue(json.get("resourceIds").isArray());
            var ids = new LinkedHashSet<String>();
            json.get("resourceIds").forEach(node -> ids.add(node.asString()));
            assertEquals(Set.of("r1", "r2", "r3"), ids);
        }

        @Test
        @DisplayName("resourceIds bidirectional applies changes from JSON")
        void resourceIdsBidirectional() {
            pojo.setResourceIds(new LinkedHashSet<>(List.of("r1")));

            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id(SamplePojo::getId)
                    .resourceIds(SamplePojo::getResourceIds, SamplePojo::setResourceIds);

            // Verify serialization
            ObjectNode json = mapper.toJson(pojo);
            assertTrue(json.get("resourceIds").isArray());

            // Apply changes
            ObjectNode changes = JsonFactory.createObject();
            var newIds = JsonFactory.createArray();
            newIds.add("r4");
            newIds.add("r5");
            changes.set("resourceIds", newIds);

            mapper.applyChanges(pojo, changes);

            assertEquals(Set.of("r4", "r5"), pojo.getResourceIds());
        }

        @Test
        @DisplayName("resourceEditable read-only serializes to JSON boolean")
        void resourceEditableReadOnly() {
            pojo.setResourceEditable(true);

            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id(SamplePojo::getId)
                    .resourceEditable(SamplePojo::isResourceEditable);

            ObjectNode json = mapper.toJson(pojo);

            assertTrue(json.has("resourceEditable"));
            assertTrue(json.get("resourceEditable").asBoolean());
        }

        @Test
        @DisplayName("null resourceIds are omitted from JSON")
        void nullResourceIdsOmitted() {
            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id(SamplePojo::getId)
                    .resourceIds(SamplePojo::getResourceIds);

            ObjectNode json = mapper.toJson(pojo);

            assertFalse(json.hasNonNull("resourceIds"));
        }

        @Test
        @DisplayName("BoundMapper getResourceIds() returns mapped value")
        void boundMapperResourceIds() {
            pojo.setResourceIds(Set.of("r1", "r2"));

            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id(SamplePojo::getId)
                    .resourceIds(SamplePojo::getResourceIds);

            var bound = mapper.forItem(pojo);
            assertEquals(Set.of("r1", "r2"), bound.getResourceIds());
        }

        @Test
        @DisplayName("BoundMapper getResourceIds() returns null when not mapped")
        void boundMapperResourceIdsNotMapped() {
            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id(SamplePojo::getId);

            var bound = mapper.forItem(pojo);
            assertNull(bound.getResourceIds());
        }
    }

    @Nested
    @DisplayName("Comparison to Entry.toJson() baseline")
    class EntryBaseline {

        @Test
        @DisplayName("mapper output matches Entry.toJson() for equivalent data")
        void outputMatchesEntry() {
            Entry entry = new Entry("entry-1");
            entry.setTitle("Meeting");
            entry.setStart(START);
            entry.setEnd(END);
            entry.setColor("#3788d8");
            entry.setAllDay(false);

            ObjectNode entryJson = entry.toJson();

            var mappedPojo = new SamplePojo("entry-1", "Meeting", START, END);
            mappedPojo.setCategoryColor("#3788d8");
            mappedPojo.setFullDay(false);

            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id(SamplePojo::getId)
                    .title(SamplePojo::getName)
                    .start(SamplePojo::getFrom)
                    .end(SamplePojo::getTo)
                    .color(SamplePojo::getCategoryColor)
                    .allDay(SamplePojo::isFullDay);

            ObjectNode mapperJson = mapper.toJson(mappedPojo);

            assertEquals(entryJson.get("id").asString(), mapperJson.get("id").asString());
            assertEquals(entryJson.get("title").asString(), mapperJson.get("title").asString());
            assertEquals(entryJson.get("start").asString(), mapperJson.get("start").asString());
            assertEquals(entryJson.get("end").asString(), mapperJson.get("end").asString());
            assertEquals(entryJson.get("color").asString(), mapperJson.get("color").asString());
        }
    }

    // ---- Test helper POJOs ----

    /**
     * Helper POJO for testing RecurringTime mapping.
     */
    static class RecurringTimePojo {
        private final String id;
        private final RecurringTime startTime;
        private final RecurringTime endTime;

        RecurringTimePojo(String id, RecurringTime startTime, RecurringTime endTime) {
            this.id = id;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public String getId() { return id; }
        public RecurringTime getStartTime() { return startTime; }
        public RecurringTime getEndTime() { return endTime; }
    }

    /**
     * Helper POJO for testing DisplayMode mapping.
     */
    static class DisplayModePojo {
        private final String id;
        private final DisplayMode displayMode;

        DisplayModePojo(String id, DisplayMode displayMode) {
            this.id = id;
            this.displayMode = displayMode;
        }

        public String getId() { return id; }
        public DisplayMode getDisplayMode() { return displayMode; }
    }

    @Nested
    @DisplayName("jsonSerializer hook")
    class JsonSerializerHook {

        @Test
        @DisplayName("should delegate toJson to custom serializer")
        void delegatesToCustomSerializer() {
            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id(SamplePojo::getId)
                    .jsonSerializer(item -> {
                        ObjectNode json = JsonFactory.createObject();
                        json.put("id", item.getId());
                        json.put("customField", "custom-" + item.getName());
                        return json;
                    });

            ObjectNode json = mapper.toJson(pojo);
            assertEquals("meeting-1", json.get("id").asString());
            assertEquals("custom-Team Standup", json.get("customField").asString());
            // title/start/end should NOT be present since serializer overrides readMappings
            assertNull(json.get("title"));
            assertNull(json.get("start"));
        }

        @Test
        @DisplayName("getId should still work with jsonSerializer")
        void getIdStillWorks() {
            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id(SamplePojo::getId)
                    .jsonSerializer(item -> {
                        ObjectNode json = JsonFactory.createObject();
                        json.put("id", item.getId());
                        return json;
                    });

            assertEquals("meeting-1", mapper.getId(pojo));
        }

        @Test
        @DisplayName("should not allow setting serializer after freeze")
        void cannotSetAfterFreeze() {
            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id(SamplePojo::getId);

            // Freeze by calling toJson
            mapper.toJson(pojo);

            assertThrows(IllegalStateException.class, () ->
                    mapper.jsonSerializer(item -> JsonFactory.createObject()));
        }
    }
}
