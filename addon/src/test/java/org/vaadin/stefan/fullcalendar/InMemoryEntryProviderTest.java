package org.vaadin.stefan.fullcalendar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryProvider;
import org.vaadin.stefan.fullcalendar.dataprovider.EntryQuery;
import org.vaadin.stefan.fullcalendar.dataprovider.InMemoryEntryProvider;

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

        InMemoryEntryProvider<Entry> provider = EntryProvider.inMemoryFrom(entries);

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

    // Issue #443. The provider works like a ListDataProvider on the given collection.

    @Test
    void test_ConstructorUsesGivenCollection() {
        List<Entry> items = new ArrayList<>(List.of(entry1, entry2));
        InMemoryEntryProvider<Entry> provider = new InMemoryEntryProvider<>(items);

        assertSame(items, provider.getItems());
        assertEqualAsSet(Set.of(entry1, entry2), provider.fetchAll());
        assertOptionalEquals(entry2, provider.fetchById("2"));
    }

    @Test
    void test_SetItems() {
        InMemoryEntryProvider<Entry> provider = new InMemoryEntryProvider<>();
        assertEquals(0, provider.fetchAll().count());

        List<Entry> items = new ArrayList<>(List.of(entry1));
        provider.setItems(items);
        assertSame(items, provider.getItems());
        assertEqualAsSet(Set.of(entry1), provider.fetchAll());

        provider.setItems(entry2, entry3);
        assertEqualAsSet(Set.of(entry2, entry3), provider.fetchAll());
        assertTrue(provider.fetchById("1").isEmpty());

        // the varargs variant gives a modifiable collection, like the no-arg constructor
        provider.getItems().add(entry1);
        assertEquals(3, provider.getItems().size());
    }

    @Test
    void test_SetItemsRefreshesTheClient() {
        InMemoryEntryProvider<Entry> provider = new InMemoryEntryProvider<>();
        List<Object> events = new ArrayList<>();
        provider.addEntriesChangeListener(events::add);

        provider.setItems(entry1);

        assertEquals(1, events.size());
    }

    @Test
    void test_CollectionChangeShowsOnlyAfterRefreshAll() {
        List<Entry> items = new ArrayList<>(List.of(entry1));
        InMemoryEntryProvider<Entry> provider = new InMemoryEntryProvider<>(items);
        assertEqualAsSet(Set.of(entry1), provider.fetchAll());

        items.add(entry2);
        items.remove(entry1);
        assertEqualAsSet(Set.of(entry1), provider.fetchAll());
        assertOptionalEquals(entry1, provider.fetchById("1"));
        assertTrue(provider.fetchById("2").isEmpty());

        provider.refreshAll();
        assertEqualAsSet(Set.of(entry2), provider.fetchAll());
        assertOptionalEquals(entry2, provider.fetchById("2"));
        assertTrue(provider.fetchById("1").isEmpty());
    }

    @Test
    void test_FirstFetchAfterConstructionNeedsNoRefresh() {
        List<Entry> items = new ArrayList<>();
        InMemoryEntryProvider<Entry> provider = new InMemoryEntryProvider<>(items);

        // nothing fetched yet, so the first fetchById builds the map from the current collection
        items.add(entry1);
        assertOptionalEquals(entry1, provider.fetchById("1"));
    }

    @Test
    void test_DuplicateIdThrowsOnFetch() {
        Entry duplicate = new Entry("1");
        InMemoryEntryProvider<Entry> provider = new InMemoryEntryProvider<>(List.of(entry1, duplicate));

        IllegalStateException ex = assertThrows(IllegalStateException.class, provider::fetchAll);
        assertTrue(ex.getMessage().contains("'1'"), ex.getMessage());
        assertThrows(IllegalStateException.class, () -> provider.fetchById("1"));
    }

    @Test
    void test_SeveralRefreshAllCallsCostOneRebuild() {
        int[] iterations = {0};
        List<Entry> backing = new ArrayList<>(List.of(entry1, entry2));
        Collection<Entry> items = new AbstractCollection<>() {
            @Override
            public Iterator<Entry> iterator() {
                iterations[0]++;
                return backing.iterator();
            }

            @Override
            public int size() {
                return backing.size();
            }
        };
        InMemoryEntryProvider<Entry> provider = new InMemoryEntryProvider<>(items);

        provider.refreshAll();
        provider.refreshAll();
        assertEquals(0, iterations[0]);

        provider.fetchAll().count();
        provider.fetchById("1");
        assertEquals(1, iterations[0]);
    }

    @Test
    void test_NullCollectionThrows() {
        assertThrows(NullPointerException.class, () -> new InMemoryEntryProvider<>((Collection<Entry>) null));
        InMemoryEntryProvider<Entry> provider = new InMemoryEntryProvider<>();
        assertThrows(NullPointerException.class, () -> provider.setItems((Collection<Entry>) null));
        assertThrows(NullPointerException.class, () -> provider.setItems((Entry[]) null));
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

        provider.getItems().addAll(entriesNotMatching);
        provider.getItems().addAll(entriesMatching);

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

        provider.getItems().addAll(entriesNotMatching);
        provider.getItems().addAll(entriesMatching);

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

        provider.getItems().addAll(entriesNotMatching);
        provider.getItems().addAll(entriesMatching);

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

        provider.getItems().addAll(entriesNotMatching);
        provider.getItems().addAll(entriesMatching);

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

        provider.getItems().addAll(entriesNotMatching);
        provider.getItems().addAll(entriesMatching);

        List<Entry> entriesFound = provider.getEntries(ref);

        // sort so that we have matching lists
        entriesMatching.sort(Comparator.comparing(Entry::getTitle));
        entriesFound.sort(Comparator.comparing(Entry::getTitle));

        Assertions.assertEquals(entriesMatching.size(), entriesFound.size(), () -> buildListBasedErrorString(entriesMatching, entriesFound));
        Assertions.assertEquals(entriesMatching, new ArrayList<>(entriesFound), () -> buildListBasedErrorString(entriesMatching, entriesFound));
    }
}
