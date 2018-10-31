package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.VaadinService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings("ALL")
public class FullCalendarIT {

    @BeforeAll
    static void beforeAll() {
        VaadinService vaadinService = Mockito.mock(VaadinService.class);
        VaadinService.setCurrent(vaadinService);

        Mockito.when(vaadinService.getDeploymentConfiguration())
                .thenAnswer(invocation -> {
                    DeploymentConfiguration config = Mockito.mock(DeploymentConfiguration.class);
                    Mockito.when(config.isProductionMode()).thenReturn(false);
                    return config;
                });

        Mockito.when(vaadinService.
                getResourceAsStream(ArgumentMatchers.eq("frontend://bower_components/fullcalendar/full-calendar.html"), ArgumentMatchers.any(), ArgumentMatchers.any())).
                thenAnswer(invocation -> {
                    Path path = Paths.get("src/main/resources/META-INF/resources/frontend/bower_components/fullcalendar/full-calendar.html").toAbsolutePath();
                    return Files.newInputStream(path);
                });
    }

    @Test
    void testNonArgsConstructor() {
        FullCalendar calendar = new FullCalendar();

        // this shall assure that all init options are handled
        assertExistingOptionCount(calendar, 1);
        Assertions.assertSame(CalendarLocale.getDefault(), calendar.getLocale());
    }

    @Test
    void testArgsConstructor() {
        int entryLimit = 5;

        FullCalendar calendar = new FullCalendar(entryLimit);

        // this shall assure that all init options are handled
        assertExistingOptionCount(calendar, 1);
        Assertions.assertSame(CalendarLocale.getDefault(), calendar.getLocale());

        Assertions.assertEquals(entryLimit, calendar.getElement().getProperty("eventLimit", -1));
    }

    private void assertExistingOptionCount(FullCalendar calendar, int expectedOptionsCount) {
        Assertions.assertEquals(expectedOptionsCount, Arrays.stream(FullCalendar.Option.values()).map(calendar::getOption).filter(Optional::isPresent).count());
    }

    @Test
    void testClientSideMethods() {
        FullCalendar calendar = new FullCalendar();

        calendar.next();
        calendar.previous();
        calendar.today();

        assertNPE(calendar, c -> c.changeView(null));
        calendar.changeView(CalendarView.MONTH);

        assertNPE(calendar, c -> c.gotoDate(null));
        calendar.gotoDate(LocalDate.now());
        calendar.gotoDate(LocalDate.MIN);
        calendar.gotoDate(LocalDate.MAX);

    }

    private void assertNPE(FullCalendar calendar, Consumer<FullCalendar> function) {
        Assertions.assertThrows(NullPointerException.class, () -> function.accept(calendar));
    }

    @Test
    void testEmptyOptionalOnFetchingNonExistingEntryById() {
        FullCalendar calendar = new FullCalendar();

        Optional<Entry> optional = calendar.getEntryById("");
        Assertions.assertNotNull(optional);
        Assertions.assertFalse(optional.isPresent());
    }

    @Test
    void testFetchingExistingEntryById() {
        FullCalendar calendar = new FullCalendar();

        Entry entry = new Entry();
        calendar.addEntry(entry);

        Optional<Entry> optional = calendar.getEntryById(entry.getId());
        Assertions.assertNotNull(optional);
        Assertions.assertTrue(optional.isPresent());
        Assertions.assertSame(entry, optional.get());
    }

    @Test
    void testAddEntry() {
        FullCalendar calendar = new FullCalendar();

        Entry entry1 = new Entry();
        Entry entry2 = new Entry();
        Entry entry3 = new Entry();

        calendar.addEntry(entry1);
        calendar.addEntry(entry2);
        calendar.addEntry(entry3);

        Collection<Entry> entries = calendar.getEntries();
        Assertions.assertEquals(3, entries.size());

        Assertions.assertTrue(entries.contains(entry1));
        Assertions.assertTrue(entries.contains(entry2));
        Assertions.assertTrue(entries.contains(entry3));

        Assertions.assertSame(entry1, calendar.getEntryById(entry1.getId()).get());
        Assertions.assertSame(entry2, calendar.getEntryById(entry2.getId()).get());
        Assertions.assertSame(entry3, calendar.getEntryById(entry3.getId()).get());
    }

    @Test
    void testRemoveContent() {
        FullCalendar calendar = new FullCalendar();

        Entry entry1 = new Entry();
        Entry entry2 = new Entry();
        Entry entry3 = new Entry();

        calendar.addEntry(entry1);
        calendar.addEntry(entry2);
        calendar.addEntry(entry3);

        calendar.removeEntry(entry2);

        Collection<Entry> entries = calendar.getEntries();
        Assertions.assertEquals(2, entries.size());

        Assertions.assertTrue(entries.contains(entry1));
        Assertions.assertFalse(entries.contains(entry2));
        Assertions.assertTrue(entries.contains(entry3));

        Assertions.assertSame(entry1, calendar.getEntryById(entry1.getId()).get());
        Assertions.assertFalse(calendar.getEntryById(entry2.getId()).isPresent());
        Assertions.assertSame(entry3, calendar.getEntryById(entry3.getId()).get());
    }

    @Test
    void testInitialEmptyCollection() {
        FullCalendar calendar = new FullCalendar();

        Collection<Entry> entries = calendar.getEntries();
        Assertions.assertNotNull(entries);
        Assertions.assertEquals(0, entries.size());
    }

    @Test
    void testEntriesInstanceAreSameAfterUpdate() {
        FullCalendar calendar = new FullCalendar();

        Entry entry1 = new Entry();
        Entry entry2 = new Entry();
        Entry entry3 = new Entry();

        calendar.addEntry(entry1);
        calendar.addEntry(entry2);
        calendar.addEntry(entry3);

        entry1.setTitle("1");
        entry2.setTitle("2");
        entry3.setTitle("3");

        calendar.updateEntry(entry1);
        calendar.updateEntry(entry2);
        calendar.updateEntry(entry3);

        Collection<Entry> entries = calendar.getEntries();
        Assertions.assertEquals(3, entries.size());

        Assertions.assertTrue(entries.contains(entry1));
        Assertions.assertTrue(entries.contains(entry2));
        Assertions.assertTrue(entries.contains(entry3));

        Assertions.assertSame(entry1, calendar.getEntryById(entry1.getId()).get());
        Assertions.assertSame(entry2, calendar.getEntryById(entry2.getId()).get());
        Assertions.assertSame(entry3, calendar.getEntryById(entry3.getId()).get());
    }

    @Test
    void testGetEntriesByDateTimeInterval() {
        LocalDate ref = LocalDate.of(2000, 1, 1);
        LocalDateTime refStartOfDay = ref.atStartOfDay();
        LocalDateTime refEndOfDay = ref.atTime(23, 0);

        LocalDateTime filterStart = ref.atTime(7, 0);
        LocalDateTime filterEnd = ref.atTime(8, 0);

        List<Entry> entriesNotMatching = new ArrayList<>();
        List<Entry> entriesMatching = new ArrayList<>();

        // completely out
        entriesNotMatching.add(new Entry(null, "NM: Start / end at start of day", refStartOfDay, refStartOfDay, false, true, null, null));

        // matching only with exclusive start filter time so not matching at all
        entriesNotMatching.add(new Entry(null, "NM: Start of day to filter start", refStartOfDay, filterStart, false, true, null, null));

        // matching only with exclusive end filter time so not matching at all
        entriesNotMatching.add(new Entry(null, "NM: Filter end to end of day", filterEnd, refEndOfDay, false, true, null, null));

        // 0 timespan - matching only with exclusive start filter time so not matching at all
        entriesNotMatching.add(new Entry(null, "NM: Filter start to filter start", filterStart, filterStart, false, true, null, null));

        // 0 timespan - matching only with exclusive end filter time so not matching at all
        entriesNotMatching.add(new Entry(null, "NM: Filter end to filter end", filterEnd, filterEnd, false, true, null, null));

        // crossing filter timespan (match by 1 nanosecond)
        entriesMatching.add(new Entry(null, "M: Start of day to filter start + 1ns", refStartOfDay, filterStart.plusNanos(1), false, true, null, null));
        entriesMatching.add(new Entry(null, "M: Filter end - 1ns to end of day", filterEnd.minusNanos(1), refEndOfDay, false, true, null, null));

        // matches filter timespan completely
        entriesMatching.add(new Entry(null, "M: Filter start to filter end", filterStart, filterEnd, false, true, null, null));

        // inner filter period match
        entriesMatching.add(new Entry(null, "M: Filter start + 1ns to filter end", filterStart.plusNanos(1), filterEnd, false, true, null, null));
        entriesMatching.add(new Entry(null, "M: Filter start to filter end - 1ns", filterStart, filterEnd.minusNanos(1), false, true, null, null));
        entriesMatching.add(new Entry(null, "M: Inside of filter timespan", filterStart.plusMinutes(29), filterStart.plusMinutes(31), false, true, null, null));

        FullCalendar calendar = new FullCalendar();
        entriesNotMatching.forEach(calendar::addEntry);
        entriesMatching.forEach(calendar::addEntry);



        List<Entry> entriesFound = calendar.getEntries(filterStart, filterEnd);

        // sort so that we have matching lists
        entriesMatching.sort(Comparator.comparing(Entry::getTitle));
        entriesFound.sort(Comparator.comparing(Entry::getTitle));

        Assertions.assertEquals(entriesMatching.size(), entriesFound.size(), () -> buildListBasedErrorString(entriesMatching, entriesFound));
        Assertions.assertEquals(entriesMatching, new ArrayList<>(entriesFound), () -> buildListBasedErrorString(entriesMatching, entriesFound));
    }

    @Test
    void testGetEntriesByDate() {
        FullCalendar calendar = new FullCalendar();
        assertNPE(calendar, c -> c.getEntries(null));

        LocalDateTime ref = LocalDate.of(2000, 1, 1).atStartOfDay();
        LocalDateTime filterEnd = ref.plusDays(1);

        List<Entry> entriesNotMatching = new ArrayList<>();
        List<Entry> entriesMatching = new ArrayList<>();

        // TODO additional / other / better test cases?

        // completely out
        entriesNotMatching.add(new Entry(null, "NM: Last year", ref.minusYears(1), ref.minusYears(1), false, true, null, null));

        // matching only with exclusive start filter time so not matching at all
        entriesNotMatching.add(new Entry(null, "NM: Last year to filter start", ref.minusYears(1), ref, false, true, null, null));

        // matching only with exclusive end filter time so not matching at all
        entriesNotMatching.add(new Entry(null, "NM: Filter end to end of month", filterEnd, ref.withDayOfMonth(31), false, true, null, null));

        // 0 timespan - matching only with exclusive end filter time so not matching at all
        entriesNotMatching.add(new Entry(null, "NM: Filter end to filter end", filterEnd, filterEnd, false, true, null, null));

        // crossing filter timespan (match by 1 nanosecond)
        entriesMatching.add(new Entry(null, "M: Last year to filter start + 1ns", ref.minusYears(1), ref.plusNanos(1), false, true, null, null));
        entriesMatching.add(new Entry(null, "M: Filter end - 1ns to end of month", filterEnd.minusNanos(1), ref.withDayOfMonth(31), false, true, null, null));

        // matches filter timespan completely
        entriesMatching.add(new Entry(null, "M: Filter start to filter end", ref, filterEnd, false, true, null, null));

        // inner filter period match
        entriesMatching.add(new Entry(null, "M: Filter start + 1ns to filter end", ref.plusNanos(1), filterEnd, false, true, null, null));
        entriesMatching.add(new Entry(null, "M: Filter start to filter end - 1ns", ref, filterEnd.minusNanos(1), false, true, null, null));
        entriesMatching.add(new Entry(null, "M: Inside of filter timespan", ref.plusMinutes(29), filterEnd.plusMinutes(31), false, true, null, null));

        entriesNotMatching.forEach(calendar::addEntry);
        entriesMatching.forEach(calendar::addEntry);


        List<Entry> entriesFound = new ArrayList<>(calendar.getEntries(ref.toLocalDate()));

        // sort so that we have matching lists
        entriesMatching.sort(Comparator.comparing(Entry::getTitle));
        entriesFound.sort(Comparator.comparing(Entry::getTitle));

        Assertions.assertEquals(entriesMatching.size(), entriesFound.size(), () -> buildListBasedErrorString(entriesMatching, entriesFound));
        Assertions.assertEquals(entriesMatching, new ArrayList<>(entriesFound), () -> buildListBasedErrorString(entriesMatching, entriesFound));
    }

    private String buildListBasedErrorString(List<Entry> entriesMatching, Collection<Entry> entriesFound) {
        StringBuffer sb = new StringBuffer("Searched for:");
        entriesMatching.stream().map(Entry::getTitle).forEach(s -> sb.append(s).append("\n"));
        sb.append("\n\nbut found:");
        entriesFound.stream().map(Entry::getTitle).forEach(s -> sb.append(s).append("\n"));

        ArrayList<Entry> missingMatching = new ArrayList<>(entriesMatching);
        missingMatching.removeAll(entriesFound);

        ArrayList<Entry> missingFound = new ArrayList<>(entriesFound);
        missingFound.removeAll(entriesMatching);

        if (!missingMatching.isEmpty()) {
            sb.append("\n\nExpected these to be found, but we did not:\n");
            missingMatching.stream().map(Entry::getTitle).forEach(s -> sb.append(s).append("\n"));
        }

        if (!missingFound.isEmpty()) {
            sb.append("\n\nThese have been found, but should not match:\n");
            missingFound.stream().map(Entry::getTitle).forEach(s -> sb.append(s).append("\n"));
        }

        return sb.toString();
    }

    @Test
    void testRemoveAll() {
        FullCalendar calendar = new FullCalendar();
        calendar.addEntry(new Entry());
        calendar.addEntry(new Entry());
        calendar.addEntry(new Entry());

        Assertions.assertEquals(3, calendar.getEntries().size());

        calendar.removeAllEntries();
        Assertions.assertEquals(0, calendar.getEntries().size());
    }

    @Test
    void testGetEntriesReturnListCopy() {
        FullCalendar calendar = new FullCalendar();
        calendar.addEntry(new Entry());
        calendar.addEntry(new Entry());
        calendar.addEntry(new Entry());

        Collection<Entry> entries = calendar.getEntries();
        Assertions.assertEquals(3, entries.size());

        calendar.removeAllEntries();
        Assertions.assertEquals(3, entries.size());
    }

    @Test
    void testGetAndSetOption() {
        FullCalendar calendar = new FullCalendar();

        assertNPE(calendar, c -> c.getOption(null));
        assertNPE(calendar, c -> c.setOption(null, null));
        assertNPE(calendar, c -> c.setOption(null, "someValue"));

        calendar.setOption(FullCalendar.Option.LOCALE, "someValue");
        Assertions.assertTrue(calendar.getOption(FullCalendar.Option.LOCALE).isPresent());

        calendar.setOption(FullCalendar.Option.LOCALE, null);
        Assertions.assertFalse(calendar.getOption(FullCalendar.Option.LOCALE).isPresent());
    }
}
