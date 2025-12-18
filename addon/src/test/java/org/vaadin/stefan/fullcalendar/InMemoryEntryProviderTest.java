package org.vaadin.stefan.fullcalendar;

import lombok.Getter;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryQuery;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;
import tools.jackson.databind.node.ArrayNode;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.vaadin.stefan.fullcalendar.TestUtils.*;

/**
 * @author Stefan Uebe
 */
public class InMemoryEntryProviderTest {

    public Entry entry1 = new Entry("1");
    public Entry entry2 = new Entry("2");
    public Entry entry3 = new Entry("3");
    private Set<Entry> entries = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(entry1, entry2, entry3)));

    @BeforeEach
    void beforeEach() {
        entry1 = new Entry("1");
        entry2 = new Entry("2");
        entry3 = new Entry("3");
        entries = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(entry1, entry2, entry3)));
    }

    @Test
    void test_StaticMethods() {
        assertEquals(0, EntryProvider.emptyInMemory().fetchAll().count());
        assertEquals(1, EntryProvider.inMemoryFrom(entry1).fetchAll().count());
        assertEquals(entries.size(), EntryProvider.inMemoryFrom(entries).fetchAll().count());
    }

    @Test
    void test_fetchAndGetVariants() {

        entry1.setStart(LocalDate.of(2000, 1, 1).atTime(10, 0));
        entry1.setEnd(LocalDate.of(2000, 1, 1).atTime(11, 0));
        entry2.setStart(LocalDate.of(2000, 2, 1).atTime(10, 0));
        entry2.setEnd(LocalDate.of(2000, 2, 1).atTime(11, 0));
        entry3.setStart(LocalDate.of(2000, 3, 1).atTime(10, 0));
        entry3.setEnd(LocalDate.of(2000, 3, 1).atTime(11, 0));

        LocalDateTime filterStart = LocalDate.of(2000, 1, 1).atStartOfDay();
        LocalDateTime filterEnd = LocalDate.of(2000, 1, 1).atStartOfDay().plusDays(1);

        InMemoryEntryProvider<Entry> provider = EntryProvider.emptyInMemory();
        provider.addEntries(entries);

        Set<Entry> expected = entries;

        // fetch all
        assertEqualAsSet(expected, provider.fetchAll());
        assertEqualAsSet(expected, provider.fetch(new EntryQuery()));
        assertEqualAsSet(expected, provider.fetch(new EntryQuery((LocalDateTime) null, null)));
        assertEqualAsSet(expected, provider.fetch((LocalDateTime) null, null));
        assertEqualAsSet(expected, provider.fetch((Instant) null, null));

        // test date time related shortcut methods
        // if the filtering itself works, is tested in other methods
        assertEqualAsSet(provider.fetch(new EntryQuery(filterStart, filterEnd)), provider.fetch(filterStart, filterEnd));
        assertEqualAsSet(provider.fetch(new EntryQuery(filterStart.toInstant(ZoneOffset.UTC), filterEnd.toInstant(ZoneOffset.UTC))), provider.fetch(filterStart.toInstant(ZoneOffset.UTC), filterEnd.toInstant(ZoneOffset.UTC)));

        // get all
        assertEqualAsSet(expected, provider.getEntries());
    }

    @Test
    void test_AddEntriesArray() {
        InMemoryEntryProvider<Entry> provider = EntryProvider.emptyInMemory();

        assertNPE(provider, c -> c.addEntries((Entry[]) null));

        provider.addEntries(entries);
        provider.addEntries(entry3);

        Collection<Entry> entries = provider.getEntries();
        assertEquals(3, entries.size());

        assertTrue(entries.contains(entry1));
        assertTrue(entries.contains(entry2));
        assertTrue(entries.contains(entry3));

        assertOptionalEquals(entry1, provider.getEntryById(entry1.getId()));
        assertOptionalEquals(entry2, provider.getEntryById(entry2.getId()));
        assertOptionalEquals(entry3, provider.getEntryById(entry3.getId()));
    }

    @Test
    void test_AddEntriesIterable() {
        InMemoryEntryProvider<Entry> provider = EntryProvider.emptyInMemory();
        assertNPE(provider, c -> c.addEntries((Iterable<Entry>) null));

        provider.addEntries(entries);
        provider.addEntries(entry3);

        Collection<Entry> entries = provider.getEntries();
        assertEquals(3, entries.size());

        assertTrue(entries.contains(entry1));
        assertTrue(entries.contains(entry2));
        assertTrue(entries.contains(entry3));

        assertOptionalEquals(entry1, provider.getEntryById(entry1.getId()));
        assertOptionalEquals(entry2, provider.getEntryById(entry2.getId()));
        assertOptionalEquals(entry3, provider.getEntryById(entry3.getId()));
    }

//    @Test
//    void test_UpdateEntries() {
//        // checks only for exceptions
//        InMemoryEntryProvider<Entry> provider = EntryProvider.emptyInMemory();
//
//        assertNPE(provider, c -> c.updateEntries((Entry[]) null));
//        assertNPE(provider, c -> c.updateEntries((Iterable<Entry>) null));
//
//        provider.addEntries(entries);
//        provider.addEntries(entry3);
//
//        provider.updateEntries(entries);
//        provider.updateEntries(entry3);
//    }

    @Test
    void test_RemoveEntriesArray() {
        InMemoryEntryProvider<Entry> provider = EntryProvider.emptyInMemory();

        assertNPE(provider, c -> c.removeEntries((Entry[]) null));


        provider.addEntries(entries);
        provider.removeEntries(entry1, entry2);

        Collection<Entry> entries = provider.getEntries();
        assertEquals(1, entries.size());

        assertFalse(entries.contains(entry1));
        assertFalse(entries.contains(entry2));
        assertTrue(entries.contains(entry3));
    }

    @Test
    void test_RemoveEntriesIterable() {
        InMemoryEntryProvider<Entry> provider = EntryProvider.emptyInMemory();

        assertNPE(provider, c -> c.removeEntries((Iterable<Entry>) null));

        provider.addEntries(entries);
        provider.removeEntries(Arrays.asList(entry1, entry2));

        Collection<Entry> entries = provider.getEntries();
        assertEquals(1, entries.size());

        assertFalse(entries.contains(entry1));
        assertFalse(entries.contains(entry2));
        assertTrue(entries.contains(entry3));
    }

    @Test
    void test_FetchEntriesByClosedDateTimeInterval() {
        InMemoryEntryProvider<Entry> provider = EntryProvider.emptyInMemory();

        LocalDate ref = LocalDate.of(2000, 1, 1);
        LocalDateTime refStartOfDay = ref.atStartOfDay();
        LocalDateTime refEndOfDay = ref.atTime(23, 0);

        LocalDateTime filterStart = ref.atTime(7, 0);
        LocalDateTime filterEnd = ref.atTime(8, 0);

        List<Entry> entriesNotMatching = new ArrayList<>();
        List<Entry> entriesMatching = new ArrayList<>();

        // completely out
        entriesNotMatching.add(createEntry(null, "NM: Start / end at start of day", refStartOfDay, refStartOfDay, false, true, null, null));
        entriesNotMatching.add(createEntry(null, "NM: Start / end at end of day", refEndOfDay, refEndOfDay, false, true, null, null));

        // matching only with exclusive start filter time so not matching at all
        entriesNotMatching.add(createEntry(null, "NM: Start of day to filter start", refStartOfDay, filterStart, false, true, null, null));

        // matching only with exclusive end filter time so not matching at all
        entriesNotMatching.add(createEntry(null, "NM: Filter end to end of day", filterEnd, refEndOfDay, false, true, null, null));

        // 0 timespan - matching only with exclusive start filter time so not matching at all
        entriesNotMatching.add(createEntry(null, "NM: Filter start to filter start", filterStart, filterStart, false, true, null, null));

        // 0 timespan - matching only with exclusive end filter time so not matching at all
        entriesNotMatching.add(createEntry(null, "NM: Filter end to filter end", filterEnd, filterEnd, false, true, null, null));

        // crossing filter timespan (match by 1 nanosecond)
        entriesMatching.add(createEntry(null, "M: Start of day to filter start + 1ns", refStartOfDay, filterStart.plusNanos(1), false, true, null, null));
        entriesMatching.add(createEntry(null, "M: Filter end - 1ns to end of day", filterEnd.minusNanos(1), refEndOfDay, false, true, null, null));

        // matches filter timespan completely
        entriesMatching.add(createEntry(null, "M: Filter start to filter end", filterStart, filterEnd, false, true, null, null));

        // inner filter period match
        entriesMatching.add(createEntry(null, "M: Filter start + 1ns to filter end", filterStart.plusNanos(1), filterEnd, false, true, null, null));
        entriesMatching.add(createEntry(null, "M: Filter start to filter end - 1ns", filterStart, filterEnd.minusNanos(1), false, true, null, null));
        entriesMatching.add(createEntry(null, "M: Inside of filter timespan", filterStart.plus(29, ChronoUnit.MINUTES), filterStart.plus(31, ChronoUnit.MINUTES), false, true, null, null));

        provider.addEntries(entriesNotMatching);
        provider.addEntries(entriesMatching);

        List<Entry> entriesFound = provider.fetch(filterStart, filterEnd).collect(Collectors.toList());

        // sort so that we have matching lists
        entriesMatching.sort(Comparator.comparing(Entry::getTitle));
        entriesFound.sort(Comparator.comparing(Entry::getTitle));

        Assertions.assertEquals(entriesMatching.size(), entriesFound.size(), () -> buildListBasedErrorString(entriesMatching, entriesFound));
        Assertions.assertEquals(entriesMatching, new ArrayList<>(entriesFound), () -> buildListBasedErrorString(entriesMatching, entriesFound));
    }

    @Test
    void test_FetchEntriesByDateTimeIntervalWithoutFilterStart() {
        InMemoryEntryProvider<Entry> provider = EntryProvider.emptyInMemory();

        LocalDate ref = LocalDate.of(2000, 1, 1);
        LocalDateTime refStartOfDay = ref.atStartOfDay();
        LocalDateTime refEndOfDay = ref.atTime(23, 0);

        LocalDateTime filterStart = ref.atTime(7, 0);
        LocalDateTime filterEnd = ref.atTime(8, 0);

        List<Entry> entriesNotMatching = new ArrayList<>();
        List<Entry> entriesMatching = new ArrayList<>();

        // completely out
        entriesNotMatching.add(createEntry(null, "NM: Start / end at end of day", refEndOfDay, refEndOfDay, false, true, null, null));

        // matching only with exclusive end filter time so not matching at all
        entriesNotMatching.add(createEntry(null, "NM: Filter end to end of day", filterEnd, refEndOfDay, false, true, null, null));

        // 0 timespan - matching only with exclusive end filter time so not matching at all
        entriesNotMatching.add(createEntry(null, "NM: Filter end to filter end", filterEnd, filterEnd, false, true, null, null));

        // these three are now matching since open filter start (@see test_FetchEntriesByDateTimeInterval())
        entriesMatching.add(createEntry(null, "M: Start / end at start of day", refStartOfDay, refStartOfDay, false, true, null, null));
        entriesMatching.add(createEntry(null, "M: Start of day to filter start", refStartOfDay, filterStart, false, true, null, null));
        entriesMatching.add(createEntry(null, "M: Filter start to filter start", filterStart, filterStart, false, true, null, null));

        // crossing filter timespan (match by 1 nanosecond)
        entriesMatching.add(createEntry(null, "M: Start of day to filter start + 1ns", refStartOfDay, filterStart.plusNanos(1), false, true, null, null));
        entriesMatching.add(createEntry(null, "M: Filter end - 1ns to end of day", filterEnd.minusNanos(1), refEndOfDay, false, true, null, null));

        // matches filter timespan completely
        entriesMatching.add(createEntry(null, "M: Filter start to filter end", filterStart, filterEnd, false, true, null, null));

        // inner filter period match
        entriesMatching.add(createEntry(null, "M: Filter start + 1ns to filter end", filterStart.plusNanos(1), filterEnd, false, true, null, null));
        entriesMatching.add(createEntry(null, "M: Filter start to filter end - 1ns", filterStart, filterEnd.minusNanos(1), false, true, null, null));
        entriesMatching.add(createEntry(null, "M: Inside of filter timespan", filterStart.plus(29, ChronoUnit.MINUTES), filterStart.plus(31, ChronoUnit.MINUTES), false, true, null, null));

        provider.addEntries(entriesNotMatching);
        provider.addEntries(entriesMatching);

        List<Entry> entriesFound = provider.fetch(null, filterEnd).collect(Collectors.toList());

        // sort so that we have matching lists
        entriesMatching.sort(Comparator.comparing(Entry::getTitle));
        entriesFound.sort(Comparator.comparing(Entry::getTitle));

        Assertions.assertEquals(entriesMatching.size(), entriesFound.size(), () -> buildListBasedErrorString(entriesMatching, entriesFound));
        Assertions.assertEquals(entriesMatching, new ArrayList<>(entriesFound), () -> buildListBasedErrorString(entriesMatching, entriesFound));
    }

    @Test
    void test_FetchEntriesByClosedDateTimeIntervalWithoutFilterEnd() {
        InMemoryEntryProvider<Entry> provider = EntryProvider.emptyInMemory();

        LocalDate ref = LocalDate.of(2000, 1, 1);
        LocalDateTime refStartOfDay = ref.atStartOfDay();
        LocalDateTime refEndOfDay = ref.atTime(23, 0);

        LocalDateTime filterStart = ref.atTime(7, 0);
        LocalDateTime filterEnd = ref.atTime(8, 0);

        List<Entry> entriesNotMatching = new ArrayList<>();
        List<Entry> entriesMatching = new ArrayList<>();

        // completely out
        entriesNotMatching.add(createEntry(null, "NM: Start / end at start of day", refStartOfDay, refStartOfDay, false, true, null, null));

        // matching only with exclusive start filter time so not matching at all
        entriesNotMatching.add(createEntry(null, "NM: Start of day to filter start", refStartOfDay, filterStart, false, true, null, null));


        // 0 timespan - matching only with exclusive start filter time so not matching at all
        entriesNotMatching.add(createEntry(null, "NM: Filter start to filter start", filterStart, filterStart, false, true, null, null));

        // these three are now matching since open filter start (@see test_FetchEntriesByDateTimeInterval())
        entriesMatching.add(createEntry(null, "M: Start / end at end of day", refEndOfDay, refEndOfDay, false, true, null, null));
        entriesMatching.add(createEntry(null, "M: Filter end to end of day", filterEnd, refEndOfDay, false, true, null, null));
        entriesMatching.add(createEntry(null, "M: Filter end to filter end", filterEnd, filterEnd, false, true, null, null));

        // crossing filter timespan (match by 1 nanosecond)
        entriesMatching.add(createEntry(null, "M: Start of day to filter start + 1ns", refStartOfDay, filterStart.plusNanos(1), false, true, null, null));
        entriesMatching.add(createEntry(null, "M: Filter end - 1ns to end of day", filterEnd.minusNanos(1), refEndOfDay, false, true, null, null));

        // matches filter timespan completely
        entriesMatching.add(createEntry(null, "M: Filter start to filter end", filterStart, filterEnd, false, true, null, null));

        // inner filter period match
        entriesMatching.add(createEntry(null, "M: Filter start + 1ns to filter end", filterStart.plusNanos(1), filterEnd, false, true, null, null));
        entriesMatching.add(createEntry(null, "M: Filter start to filter end - 1ns", filterStart, filterEnd.minusNanos(1), false, true, null, null));
        entriesMatching.add(createEntry(null, "M: Inside of filter timespan", filterStart.plus(29, ChronoUnit.MINUTES), filterStart.plus(31, ChronoUnit.MINUTES), false, true, null, null));

        provider.addEntries(entriesNotMatching);
        provider.addEntries(entriesMatching);

        List<Entry> entriesFound = provider.fetch(filterStart, null).collect(Collectors.toList());

        // sort so that we have matching lists
        entriesMatching.sort(Comparator.comparing(Entry::getTitle));
        entriesFound.sort(Comparator.comparing(Entry::getTitle));

        Assertions.assertEquals(entriesMatching.size(), entriesFound.size(), () -> buildListBasedErrorString(entriesMatching, entriesFound));
        Assertions.assertEquals(entriesMatching, new ArrayList<>(entriesFound), () -> buildListBasedErrorString(entriesMatching, entriesFound));
    }

    @Test
    void test_FetchEntriesByClosedDateTimeIntervalWithoutParameters() {
        InMemoryEntryProvider<Entry> provider = EntryProvider.emptyInMemory();

        LocalDate ref = LocalDate.of(2000, 1, 1);
        LocalDateTime refStartOfDay = ref.atStartOfDay();
        LocalDateTime refEndOfDay = ref.atTime(23, 0);

        LocalDateTime filterStart = ref.atTime(7, 0);
        LocalDateTime filterEnd = ref.atTime(8, 0);

        List<Entry> entriesNotMatching = new ArrayList<>();
        List<Entry> entriesMatching = new ArrayList<>();

        // completely out
        entriesMatching.add(createEntry(null, "M: Start / end at start of day", refStartOfDay, refStartOfDay, false, true, null, null));
        entriesMatching.add(createEntry(null, "M: Start / end at end of day", refEndOfDay, refEndOfDay, false, true, null, null));

        // matching only with exclusive start filter time so not matching at all
        entriesMatching.add(createEntry(null, "M: Start of day to filter start", refStartOfDay, filterStart, false, true, null, null));

        // matching only with exclusive end filter time so not matching at all
        entriesMatching.add(createEntry(null, "M: Filter end to end of day", filterEnd, refEndOfDay, false, true, null, null));

        // 0 timespan - matching only with exclusive start filter time so not matching at all
        entriesMatching.add(createEntry(null, "M: Filter start to filter start", filterStart, filterStart, false, true, null, null));

        // 0 timespan - matching only with exclusive end filter time so not matching at all
        entriesMatching.add(createEntry(null, "M: Filter end to filter end", filterEnd, filterEnd, false, true, null, null));

        // crossing filter timespan (match by 1 nanosecond)
        entriesMatching.add(createEntry(null, "M: Start of day to filter start + 1ns", refStartOfDay, filterStart.plusNanos(1), false, true, null, null));
        entriesMatching.add(createEntry(null, "M: Filter end - 1ns to end of day", filterEnd.minusNanos(1), refEndOfDay, false, true, null, null));

        // matches filter timespan completely
        entriesMatching.add(createEntry(null, "M: Filter start to filter end", filterStart, filterEnd, false, true, null, null));

        // inner filter period match
        entriesMatching.add(createEntry(null, "M: Filter start + 1ns to filter end", filterStart.plusNanos(1), filterEnd, false, true, null, null));
        entriesMatching.add(createEntry(null, "M: Filter start to filter end - 1ns", filterStart, filterEnd.minusNanos(1), false, true, null, null));
        entriesMatching.add(createEntry(null, "M: Inside of filter timespan", filterStart.plus(29, ChronoUnit.MINUTES), filterStart.plus(31, ChronoUnit.MINUTES), false, true, null, null));

        provider.addEntries(entriesNotMatching);
        provider.addEntries(entriesMatching);

        List<Entry> entriesFound = provider.fetch((LocalDateTime) null, null).collect(Collectors.toList());
        List<Entry> allEntries = provider.fetchAll().collect(Collectors.toList());

        // sort so that we have matching lists
        entriesMatching.sort(Comparator.comparing(Entry::getTitle));
        entriesFound.sort(Comparator.comparing(Entry::getTitle));
        allEntries.sort(Comparator.comparing(Entry::getTitle));

        Assertions.assertEquals(entriesMatching.size(), entriesFound.size(), () -> buildListBasedErrorString(entriesMatching, entriesFound));
        Assertions.assertEquals(entriesMatching, new ArrayList<>(entriesFound), () -> buildListBasedErrorString(entriesMatching, entriesFound));

        Assertions.assertEquals(allEntries.size(), entriesFound.size(), () -> buildListBasedErrorString(allEntries, entriesFound));
        Assertions.assertEquals(allEntries, new ArrayList<>(entriesFound), () -> buildListBasedErrorString(allEntries, entriesFound));

    }

    @Test
    void test_GetEntriesByDate() {
        InMemoryEntryProvider<Entry> provider = EntryProvider.emptyInMemory();
        assertNPE(provider, c -> c.getEntries((LocalDateTime) null));

        LocalDateTime ref = LocalDate.of(2000, 1, 1).atStartOfDay();
        LocalDateTime filterEnd = ref.plusDays(1);

        List<Entry> entriesNotMatching = new ArrayList<>();
        List<Entry> entriesMatching = new ArrayList<>();

        // TODO additional / other / better test cases?

        // completely out
        entriesNotMatching.add(createEntry(null, "NM: Last year", ref.minus(1, ChronoUnit.YEARS), ref.minus(1, ChronoUnit.YEARS), false, true, null, null));

        // matching only with exclusive start filter time so not matching at all
        entriesNotMatching.add(createEntry(null, "NM: Last year to filter start", ref.minus(1, ChronoUnit.YEARS), ref, false, true, null, null));

        // matching only with exclusive end filter time so not matching at all
        entriesNotMatching.add(createEntry(null, "NM: Filter end to end of month", filterEnd, ref.with(ChronoField.DAY_OF_MONTH, 31), false, true, null, null));

        // 0 timespan - matching only with exclusive end filter time so not matching at all
        entriesNotMatching.add(createEntry(null, "NM: Filter end to filter end", filterEnd, filterEnd, false, true, null, null));

        // crossing filter timespan (match by 1 nanosecond)
        entriesMatching.add(createEntry(null, "M: Last year to filter start + 1ns", ref.minus(1, ChronoUnit.YEARS), ref.plusNanos(1), false, true, null, null));
        entriesMatching.add(createEntry(null, "M: Filter end - 1ns to end of month", filterEnd.minusNanos(1), ref.with(ChronoField.DAY_OF_MONTH, 31), false, true, null, null));

        // matches filter timespan completely
        entriesMatching.add(createEntry(null, "M: Filter start to filter end", ref, filterEnd, false, true, null, null));

        // inner filter period match
        entriesMatching.add(createEntry(null, "M: Filter start + 1ns to filter end", ref.plusNanos(1), filterEnd, false, true, null, null));
        entriesMatching.add(createEntry(null, "M: Filter start to filter end - 1ns", ref, filterEnd.minusNanos(1), false, true, null, null));
        entriesMatching.add(createEntry(null, "M: Inside of filter timespan", ref.plus(29, ChronoUnit.MINUTES), filterEnd.plus(31, ChronoUnit.MINUTES), false, true, null, null));

        provider.addEntries(entriesNotMatching);
        provider.addEntries(entriesMatching);

        List<Entry> entriesFound = provider.getEntries(ref);

        // sort so that we have matching lists
        entriesMatching.sort(Comparator.comparing(Entry::getTitle));
        entriesFound.sort(Comparator.comparing(Entry::getTitle));

        Assertions.assertEquals(entriesMatching.size(), entriesFound.size(), () -> buildListBasedErrorString(entriesMatching, entriesFound));
        Assertions.assertEquals(entriesMatching, new ArrayList<>(entriesFound), () -> buildListBasedErrorString(entriesMatching, entriesFound));
    }

    // TODO deprecated?
//    @Test
//    void test_removeAndAddDifferentEntriesWithSameIdInOneCycle() throws IllegalAccessException {
//        FullCalendar calendar = new FullCalendar();
//        FieldUtils.writeField(calendar, "attached", true, true);
//
//        TestProvider provider = new TestProvider();
//        calendar.setEntryProvider(provider);
//
//        Entry oldEntry1 = createEntry("1", "Entry 1");
//        Entry oldEntry2 = createEntry("2", "Entry 2");
//        Entry oldEntry3 = createEntry("3", "Entry 3");
//
//        List<Entry> oldEntries = Arrays.asList(oldEntry1, oldEntry2, oldEntry3);
//        provider.addEntries(oldEntries);
//        provider.refreshAll(); // simulate client side update
//
//        provider.startRecord();
//        provider.removeAllEntries();
//        Entry newEntry1 = createEntry("1", "Entry A");
//        Entry newEntry2 = createEntry("2", "Entry B");
//        Entry newEntry3 = createEntry("3", "Entry C");
//        List<Entry> newEntries = Arrays.asList(newEntry1, newEntry2, newEntry3);
//        provider.addEntries(newEntries);
//        provider.refreshAll(); // simulate client side update
//
//        Map<String, Set<String>> tmpItemSnapshots = provider.getTmpItemSnapshots();
//        // in both cases they need to have the same entries, since hashCode bases only on the id
//        assertEqualAsSet(tmpItemSnapshots.get("removeEvents"), oldEntries.stream().map(JsonItem::toString), "remove events snapshots");
//        assertEqualAsSet(tmpItemSnapshots.get("addEvents"), newEntries.stream().map(JsonItem::toString), "add events snapshots");
//
//        Map<String, JsonArray> jsonArrays = provider.getCreatedJsonArrays();
//
//        // check, if the json array "sent" to the client contains the same values as it would to be expected
//        Set<?> manuallyConverted = JsonUtils.ofJsonValue(provider.convertItemsToJson(oldEntries, JsonItem::toJsonWithIdOnly), HashSet.class);
//        Set<?> triggerConverted = JsonUtils.ofJsonValue(jsonArrays.get("removeEvents"), HashSet.class);
//        assertEquals(manuallyConverted, triggerConverted, "remove events json array");
//
//        // check, if the json array "sent" to the client contains the same values as it would to be expected
//        manuallyConverted = JsonUtils.ofJsonValue(provider.convertItemsToJson(newEntries, JsonItem::toJson), HashSet.class);
//        triggerConverted = JsonUtils.ofJsonValue(jsonArrays.get("addEvents"), HashSet.class);
//        assertEquals(manuallyConverted, triggerConverted, "add events json array");
//    }

    // TODO deprecated?
//    @Test
//    void test_removeAndAddSameEntriesInOneCycle() throws IllegalAccessException {
//        FullCalendar calendar = new FullCalendar();
//        FieldUtils.writeField(calendar, "attached", true, true);
//
//        TestProvider provider = new TestProvider();
//        calendar.setEntryProvider(provider);
//
//        Entry entry1 = createEntry("1", "Entry 1");
//        Entry entry2 = createEntry("2", "Entry 2");
//        Entry entry = createEntry("3", "Entry 3");
//
//        List<Entry> entries = Arrays.asList(entry1, entry2, entry);
//        provider.addEntries(entries);
//        provider.refreshAll(); // simulate client side update
//
//        provider.startRecord();
//        provider.removeAllEntries();
//        entry1.setTitle("Entry A");
//        entry2.setTitle("Entry B");
//        entry3.setTitle("Entry C");
//
//        provider.addEntries(entries);
//        provider.refreshAll(); // simulate client side update
//
//        Map<String, Set<String>> tmpItemSnapshots = provider.getTmpItemSnapshots();
//        // in both cases they need to have the same entries, since hashCode bases only on the id
//        assertEqualAsSet(tmpItemSnapshots.get("removeEvents"), entries.stream().map(JsonItem::toString), "remove events snapshots");
//        assertEqualAsSet(tmpItemSnapshots.get("addEvents"), entries.stream().map(JsonItem::toString), "add events snapshots");
//
//        Map<String, JsonArray> jsonArrays = provider.getCreatedJsonArrays();
//
//        // check, if the json array "sent" to the client contains the same values as it would to be expected
//        Set<?> manuallyConverted = JsonUtils.ofJsonValue(provider.convertItemsToJson(entries, JsonItem::toJsonWithIdOnly), HashSet.class);
//        Set<?> triggerConverted = JsonUtils.ofJsonValue(jsonArrays.get("removeEvents"), HashSet.class);
//        assertEquals(manuallyConverted, triggerConverted, "remove events json array");
//
//        // check, if the json array "sent" to the client contains the same values as it would to be expected
//        manuallyConverted = JsonUtils.ofJsonValue(provider.convertItemsToJson(entries, JsonItem::toJson), HashSet.class);
//        triggerConverted = JsonUtils.ofJsonValue(jsonArrays.get("addEvents"), HashSet.class);
//        assertEquals(manuallyConverted, triggerConverted, "add events json array");
//    }

    // TODO deprecated?
//    @Test
//    @Disabled
//    void test_addAndRemoveSameEntriesInOneCycle() throws IllegalAccessException {
//        FullCalendar calendar = new FullCalendar();
//        FieldUtils.writeField(calendar, "attached", true, true);
//
//        TestProvider provider = new TestProvider();
//        calendar.setEntryProvider(provider);
//
//        Entry entry1 = createEntry("1", "Entry 1");
//        Entry entry2 = createEntry("2", "Entry 2");
//        Entry entry = createEntry("3", "Entry 3");
//
//        provider.startRecord();
//        List<Entry> entries = Arrays.asList(entry1, entry2, entry);
//        provider.addEntries(entries);
//        provider.removeAllEntries();
//
//        provider.refreshAll(); // simulate client side update
//
//        Map<String, Set<String>> tmpItemSnapshots = provider.getTmpItemSnapshots();
//        // the registered items should be still the full items list
//        assertEqualAsSet(tmpItemSnapshots.get("removeEvents"), entries.stream().map(JsonItem::toString), "remove events snapshots");
//        assertEqualAsSet(tmpItemSnapshots.get("addEvents"), entries.stream().map(JsonItem::toString), "add events snapshots");
//
//        // ... but the resulting json array should be empty
//        Map<String, JsonArray> jsonArrays = provider.getCreatedJsonArrays();
//
//        // check, if the json array "sent" to the client contains the same values as it would to be expected
//        assertEquals(0, jsonArrays.get("removeEvents").length(), "remove events json array");
//
//        // check, if the json array "sent" to the client contains the same values as it would to be expected
//        assertEquals(0, jsonArrays.get("addEvents").length(), "add events json array");
//    }

    @Getter
    private static class TestProvider extends InMemoryEntryProvider<Entry> {

        private final Map<String, ArrayNode> createdJsonArrays = new HashMap<>();
        private final Map<String, Set<String>> tmpItemSnapshots = new HashMap<>();
        private boolean record;

        protected void startRecord() {
            this.record = true;
        }
    }

}
