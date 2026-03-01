package org.vaadin.stefan.fullcalendar.spike;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.JsonFactory;
import org.vaadin.stefan.fullcalendar.JsonUtils;
import tools.jackson.databind.node.ObjectNode;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Spike 0.1: Tests for CalendarItemPropertyMapper prototype.
 * <p>
 * Tests:
 * 1. Lambda-based read mapping
 * 2. String-based (reflection) read mapping
 * 3. Bidirectional mapping (getter + setter)
 * 4. toJson serialization
 * 5. applyChanges deserialization
 * 6. forItem bound pattern
 * 7. validate() for missing id
 * 8. hasSetters() check
 * 9. Type conversions: LocalDateTime, LocalDate, DayOfWeek
 * 10. Null handling
 * 11. Benchmark: toJson vs forItem with 10,000 items
 * 12. Comparison to Entry.toJson baseline
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

            // Simulate client-side drag to new time
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
            // end is NOT in the changes

            mapper.applyChanges(pojo, changes);

            assertEquals(newStart, pojo.getFrom());
            assertEquals(END, pojo.getTo()); // unchanged
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
        @DisplayName("LocalDateTime roundtrip: serialize then deserialize via applyChanges")
        void localDateTimeRoundtrip() {
            var mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id(SamplePojo::getId)
                    .start(SamplePojo::getFrom, SamplePojo::setFrom)
                    .end(SamplePojo::getTo, SamplePojo::setTo);

            // Serialize
            ObjectNode json = mapper.toJson(pojo);

            // Create a new pojo and apply the serialized JSON to it
            var newPojo = new SamplePojo("meeting-1", "Test", null, null);
            mapper.applyChanges(newPojo, json);

            assertEquals(START, newPojo.getFrom());
            assertEquals(END, newPojo.getTo());
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

            // Both should have the same key properties with matching values
            assertEquals(entryJson.get("id").asString(), mapperJson.get("id").asString());
            assertEquals(entryJson.get("title").asString(), mapperJson.get("title").asString());
            assertEquals(entryJson.get("start").asString(), mapperJson.get("start").asString());
            assertEquals(entryJson.get("end").asString(), mapperJson.get("end").asString());
            assertEquals(entryJson.get("color").asString(), mapperJson.get("color").asString());

            // Entry includes defaults for editable, overlap, display etc. - mapper only includes mapped properties
            // This is by design: unmapped properties use FullCalendar JS defaults
        }
    }

    @Nested
    @DisplayName("Benchmark: 10,000 items")
    class Benchmark {

        static final int ITEM_COUNT = 10_000;

        List<SamplePojo> items;
        CalendarItemPropertyMapper<SamplePojo> mapper;

        @BeforeEach
        void setup() {
            items = new ArrayList<>(ITEM_COUNT);
            for (int i = 0; i < ITEM_COUNT; i++) {
                var p = new SamplePojo("id-" + i, "Meeting " + i,
                        START.plusMinutes(i), END.plusMinutes(i));
                p.setCategoryColor("#" + String.format("%06x", i % 0xFFFFFF));
                p.setCanEdit(i % 2 == 0);
                items.add(p);
            }

            mapper = CalendarItemPropertyMapper.of(SamplePojo.class)
                    .id(SamplePojo::getId)
                    .title(SamplePojo::getName)
                    .start(SamplePojo::getFrom)
                    .end(SamplePojo::getTo)
                    .color(SamplePojo::getCategoryColor)
                    .editable(SamplePojo::isCanEdit)
                    .allDay(SamplePojo::isFullDay);
        }

        @Test
        @DisplayName("direct toJson() for 10,000 items completes in reasonable time")
        void directToJsonBenchmark() {
            // Warmup
            for (int i = 0; i < 100; i++) {
                mapper.toJson(items.get(i));
            }

            long startTime = System.nanoTime();
            for (SamplePojo item : items) {
                mapper.toJson(item);
            }
            long durationMs = (System.nanoTime() - startTime) / 1_000_000;

            System.out.println("Direct toJson() for " + ITEM_COUNT + " items: " + durationMs + " ms");
            // Should complete well under 1 second
            assertTrue(durationMs < 5000, "toJson for 10K items took " + durationMs + "ms (expected <5000ms)");
        }

        @Test
        @DisplayName("forItem().toJson() for 10,000 items completes in reasonable time")
        void boundMapperBenchmark() {
            // Warmup
            for (int i = 0; i < 100; i++) {
                mapper.forItem(items.get(i)).toJson();
            }

            long startTime = System.nanoTime();
            for (SamplePojo item : items) {
                mapper.forItem(item).toJson();
            }
            long durationMs = (System.nanoTime() - startTime) / 1_000_000;

            System.out.println("forItem().toJson() for " + ITEM_COUNT + " items: " + durationMs + " ms");
            assertTrue(durationMs < 5000, "forItem().toJson() for 10K items took " + durationMs + "ms (expected <5000ms)");
        }

        @Test
        @DisplayName("Entry.toJson() baseline for 10,000 items")
        void entryBaselineBenchmark() {
            List<Entry> entries = new ArrayList<>(ITEM_COUNT);
            for (int i = 0; i < ITEM_COUNT; i++) {
                Entry e = new Entry("id-" + i);
                e.setTitle("Meeting " + i);
                e.setStart(START.plusMinutes(i));
                e.setEnd(END.plusMinutes(i));
                e.setColor("#" + String.format("%06x", i % 0xFFFFFF));
                e.setEditable(i % 2 == 0);
                entries.add(e);
            }

            // Warmup
            for (int i = 0; i < 100; i++) {
                entries.get(i).toJson();
            }

            long startTime = System.nanoTime();
            for (Entry entry : entries) {
                entry.toJson();
            }
            long durationMs = (System.nanoTime() - startTime) / 1_000_000;

            System.out.println("Entry.toJson() baseline for " + ITEM_COUNT + " items: " + durationMs + " ms");
            assertTrue(durationMs < 5000, "Entry.toJson() for 10K items took " + durationMs + "ms (expected <5000ms)");
        }
    }
}
