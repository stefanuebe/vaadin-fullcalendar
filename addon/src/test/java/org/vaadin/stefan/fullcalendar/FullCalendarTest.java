package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.ComponentEventBusUtil;
import elemental.json.Json;
import elemental.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.FullCalendar.Option;

import java.lang.reflect.Constructor;
import java.time.*;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings("ALL")
public class FullCalendarTest {

    public static final String FULL_CALENDAR_HTML = "fullcalendar/full-calendar.html";

    @BeforeAll
    static void beforeAll() {
//        TestUtils.initVaadinService(FULL_CALENDAR_HTML);
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
        Assertions.assertEquals(expectedOptionsCount, Arrays.stream(Option.values()).map(calendar::getOption).filter(Optional::isPresent).count());
    }

    @Test
    void testClientSideMethods() {
        FullCalendar calendar = new FullCalendar();

        calendar.next();
        calendar.previous();
        calendar.today();

        assertNPE(calendar, c -> c.changeView(null));
        calendar.changeView(CalendarViewImpl.DAY_GRID_MONTH);

        assertNPE(calendar, c -> c.gotoDate(null));
        calendar.gotoDate(LocalDate.now());
        calendar.gotoDate(LocalDate.MIN);
        calendar.gotoDate(LocalDate.MAX);

        calendar.render();
    }

    @Test
    void testClientSideOptionMethods() {
        FullCalendar calendar = new FullCalendar();

        // first day of week
        assertNPE(calendar, c -> c.setFirstDay(null));

        calendar.setFirstDay(DayOfWeek.MONDAY);
        assertOptionalEquals(DayOfWeek.MONDAY, calendar.getOption(Option.FIRST_DAY));
        assertOptionalEquals(DayOfWeek.MONDAY.getValue(), calendar.getOption(Option.FIRST_DAY, true));

        calendar.setFirstDay(DayOfWeek.SUNDAY);
        assertOptionalEquals(DayOfWeek.SUNDAY, calendar.getOption(Option.FIRST_DAY));
        assertOptionalEquals(0, calendar.getOption(Option.FIRST_DAY, true));

        calendar.setHeight(500);
        assertOptionalEquals(500, calendar.getOption(Option.HEIGHT));

        calendar.setHeightByParent();
        assertOptionalEquals("parent", calendar.getOption(Option.HEIGHT));

        calendar.setHeightAuto();
        assertOptionalEquals("auto", calendar.getOption(Option.HEIGHT));

        assertNPE(calendar, c -> c.setLocale(null));

        Locale locale = CalendarLocale.GREEK;

        // we want to be sure to not use the default to test.
        Assertions.assertNotEquals(CalendarLocale.getDefault(), locale);

        calendar.setLocale(locale);
        Assertions.assertSame(locale, calendar.getLocale());
        assertOptionalEquals(locale, calendar.getOption(Option.LOCALE));
        assertOptionalEquals(locale.toLanguageTag().toLowerCase(), calendar.getOption(Option.LOCALE, true));

        assertCorrectBooleanOption(calendar, Option.SELECTABLE, calendar::setTimeslotsSelectable);
        assertCorrectBooleanOption(calendar, Option.WEEK_NUMBERS, calendar::setWeekNumbersVisible);
        assertCorrectBooleanOption(calendar, Option.NOW_INDICATOR, calendar::setNowIndicatorShown);

        // this must be tested before the other setNumberClickForwads...Target methods
        assertCorrectBooleanOption(calendar, Option.NAV_LINKS, calendar::setNumberClickable);

        assertNPE(calendar, c -> calendar.setBusinessHours(null));
        BusinessHours hours = new BusinessHours(LocalTime.of(5, 0), LocalTime.of(10, 0), BusinessHours.ALL_DAYS);
        calendar.setBusinessHours(hours);

        Optional<Object> option = calendar.getOption(Option.BUSINESS_HOURS);
        Assertions.assertTrue(option.isPresent());
        Assertions.assertTrue(option.get() instanceof BusinessHours[]);
        Assertions.assertEquals(hours, ((BusinessHours[]) option.get())[0]);

        calendar.removeBusinessHours();
        option = calendar.getOption(Option.BUSINESS_HOURS);
        Assertions.assertFalse(option.isPresent());
    }

    private void assertCorrectBooleanOption(FullCalendar calendar, Option optionToCheck, Consumer<Boolean> function) {
        function.accept(true);
        assertOptionalEquals(true, calendar.getOption(optionToCheck), "Checking set true for setter of "
                + optionToCheck.name() + " failed. Option returned false.");
    }

    private <T> void assertOptionalEquals(T expected, Optional<T> value) {
        Assertions.assertTrue(value.isPresent());
        Assertions.assertEquals(expected, value.get());
    }

    private <T> void assertOptionalEquals(T expected, Optional<T> value, String supplier) {
        Assertions.assertTrue(value.isPresent(), supplier);
        Assertions.assertEquals(expected, value.get(), supplier);
    }

    private void assertNPE(FullCalendar calendar, Consumer<FullCalendar> function) {
        Assertions.assertThrows(NullPointerException.class, () -> function.accept(calendar));
    }

    private void assertIAE(FullCalendar calendar, Consumer<FullCalendar> function) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> function.accept(calendar));
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
        assertOptionalEquals(entry, optional);
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

        assertOptionalEquals(entry1, calendar.getEntryById(entry1.getId()));
        assertOptionalEquals(entry2, calendar.getEntryById(entry2.getId()));
        assertOptionalEquals(entry3, calendar.getEntryById(entry3.getId()));
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

        assertOptionalEquals(entry1, calendar.getEntryById(entry1.getId()));
        assertOptionalEquals(entry3, calendar.getEntryById(entry3.getId()));

        Assertions.assertFalse(calendar.getEntryById(entry2.getId()).isPresent());
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

        assertOptionalEquals(entry1, calendar.getEntryById(entry1.getId()));
        assertOptionalEquals(entry2, calendar.getEntryById(entry2.getId()));
        assertOptionalEquals(entry3, calendar.getEntryById(entry3.getId()));
    }

    @Test
    void testGetEntriesByClosedDateTimeInterval() {
        LocalDate ref = LocalDate.of(2000, 1, 1);
        Instant refStartOfDay = ref.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant refEndOfDay = ref.atTime(23, 0).toInstant(ZoneOffset.UTC);

        Instant filterStart = ref.atTime(7, 0).toInstant(ZoneOffset.UTC);
        Instant filterEnd = ref.atTime(8, 0).toInstant(ZoneOffset.UTC);

        List<Entry> entriesNotMatching = new ArrayList<>();
        List<Entry> entriesMatching = new ArrayList<>();

        // completely out
        entriesNotMatching.add(new Entry(null, "NM: Start / end at start of day", refStartOfDay, refStartOfDay, false, true, null, null));
        entriesNotMatching.add(new Entry(null, "NM: Start / end at end of day", refEndOfDay, refEndOfDay, false, true, null, null));

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
        entriesMatching.add(new Entry(null, "M: Inside of filter timespan", filterStart.plus(29, ChronoUnit.MINUTES), filterStart.plus(31, ChronoUnit.MINUTES), false, true, null, null));

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
    void testGetEntriesByDateTimeIntervalWithoutFilterStart() {
        LocalDate ref = LocalDate.of(2000, 1, 1);
        Instant refStartOfDay = ref.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant refEndOfDay = ref.atTime(23, 0).toInstant(ZoneOffset.UTC);

        Instant filterStart = ref.atTime(7, 0).toInstant(ZoneOffset.UTC);
        Instant filterEnd = ref.atTime(8, 0).toInstant(ZoneOffset.UTC);

        List<Entry> entriesNotMatching = new ArrayList<>();
        List<Entry> entriesMatching = new ArrayList<>();

        // completely out
        entriesNotMatching.add(new Entry(null, "NM: Start / end at end of day", refEndOfDay, refEndOfDay, false, true, null, null));

        // matching only with exclusive end filter time so not matching at all
        entriesNotMatching.add(new Entry(null, "NM: Filter end to end of day", filterEnd, refEndOfDay, false, true, null, null));

        // 0 timespan - matching only with exclusive end filter time so not matching at all
        entriesNotMatching.add(new Entry(null, "NM: Filter end to filter end", filterEnd, filterEnd, false, true, null, null));

        // these three are now matching since open filter start (@see testGetEntriesByDateTimeInterval())
        entriesMatching.add(new Entry(null, "M: Start / end at start of day", refStartOfDay, refStartOfDay, false, true, null, null));
        entriesMatching.add(new Entry(null, "M: Start of day to filter start", refStartOfDay, filterStart, false, true, null, null));
        entriesMatching.add(new Entry(null, "M: Filter start to filter start", filterStart, filterStart, false, true, null, null));

        // crossing filter timespan (match by 1 nanosecond)
        entriesMatching.add(new Entry(null, "M: Start of day to filter start + 1ns", refStartOfDay, filterStart.plusNanos(1), false, true, null, null));
        entriesMatching.add(new Entry(null, "M: Filter end - 1ns to end of day", filterEnd.minusNanos(1), refEndOfDay, false, true, null, null));

        // matches filter timespan completely
        entriesMatching.add(new Entry(null, "M: Filter start to filter end", filterStart, filterEnd, false, true, null, null));

        // inner filter period match
        entriesMatching.add(new Entry(null, "M: Filter start + 1ns to filter end", filterStart.plusNanos(1), filterEnd, false, true, null, null));
        entriesMatching.add(new Entry(null, "M: Filter start to filter end - 1ns", filterStart, filterEnd.minusNanos(1), false, true, null, null));
        entriesMatching.add(new Entry(null, "M: Inside of filter timespan", filterStart.plus(29, ChronoUnit.MINUTES), filterStart.plus(31, ChronoUnit.MINUTES), false, true, null, null));

        FullCalendar calendar = new FullCalendar();
        entriesNotMatching.forEach(calendar::addEntry);
        entriesMatching.forEach(calendar::addEntry);

        List<Entry> entriesFound = calendar.getEntries(null, filterEnd);

        // sort so that we have matching lists
        entriesMatching.sort(Comparator.comparing(Entry::getTitle));
        entriesFound.sort(Comparator.comparing(Entry::getTitle));

        Assertions.assertEquals(entriesMatching.size(), entriesFound.size(), () -> buildListBasedErrorString(entriesMatching, entriesFound));
        Assertions.assertEquals(entriesMatching, new ArrayList<>(entriesFound), () -> buildListBasedErrorString(entriesMatching, entriesFound));
    }

    @Test
    void testGetEntriesByClosedDateTimeIntervalWithoutFilterEnd() {
        LocalDate ref = LocalDate.of(2000, 1, 1);
        Instant refStartOfDay = ref.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant refEndOfDay = ref.atTime(23, 0).toInstant(ZoneOffset.UTC);

        Instant filterStart = ref.atTime(7, 0).toInstant(ZoneOffset.UTC);
        Instant filterEnd = ref.atTime(8, 0).toInstant(ZoneOffset.UTC);

        List<Entry> entriesNotMatching = new ArrayList<>();
        List<Entry> entriesMatching = new ArrayList<>();

        // completely out
        entriesNotMatching.add(new Entry(null, "NM: Start / end at start of day", refStartOfDay, refStartOfDay, false, true, null, null));

        // matching only with exclusive start filter time so not matching at all
        entriesNotMatching.add(new Entry(null, "NM: Start of day to filter start", refStartOfDay, filterStart, false, true, null, null));


        // 0 timespan - matching only with exclusive start filter time so not matching at all
        entriesNotMatching.add(new Entry(null, "NM: Filter start to filter start", filterStart, filterStart, false, true, null, null));

        // these three are now matching since open filter start (@see testGetEntriesByDateTimeInterval())
        entriesMatching.add(new Entry(null, "M: Start / end at end of day", refEndOfDay, refEndOfDay, false, true, null, null));
        entriesMatching.add(new Entry(null, "M: Filter end to end of day", filterEnd, refEndOfDay, false, true, null, null));
        entriesMatching.add(new Entry(null, "M: Filter end to filter end", filterEnd, filterEnd, false, true, null, null));

        // crossing filter timespan (match by 1 nanosecond)
        entriesMatching.add(new Entry(null, "M: Start of day to filter start + 1ns", refStartOfDay, filterStart.plusNanos(1), false, true, null, null));
        entriesMatching.add(new Entry(null, "M: Filter end - 1ns to end of day", filterEnd.minusNanos(1), refEndOfDay, false, true, null, null));

        // matches filter timespan completely
        entriesMatching.add(new Entry(null, "M: Filter start to filter end", filterStart, filterEnd, false, true, null, null));

        // inner filter period match
        entriesMatching.add(new Entry(null, "M: Filter start + 1ns to filter end", filterStart.plusNanos(1), filterEnd, false, true, null, null));
        entriesMatching.add(new Entry(null, "M: Filter start to filter end - 1ns", filterStart, filterEnd.minusNanos(1), false, true, null, null));
        entriesMatching.add(new Entry(null, "M: Inside of filter timespan", filterStart.plus(29, ChronoUnit.MINUTES), filterStart.plus(31, ChronoUnit.MINUTES), false, true, null, null));

        FullCalendar calendar = new FullCalendar();
        entriesNotMatching.forEach(calendar::addEntry);
        entriesMatching.forEach(calendar::addEntry);

        List<Entry> entriesFound = calendar.getEntries(filterStart, null);

        // sort so that we have matching lists
        entriesMatching.sort(Comparator.comparing(Entry::getTitle));
        entriesFound.sort(Comparator.comparing(Entry::getTitle));

        Assertions.assertEquals(entriesMatching.size(), entriesFound.size(), () -> buildListBasedErrorString(entriesMatching, entriesFound));
        Assertions.assertEquals(entriesMatching, new ArrayList<>(entriesFound), () -> buildListBasedErrorString(entriesMatching, entriesFound));
    }

    @Test
    void testGetEntriesByClosedDateTimeIntervalWithoutParameters() {
        LocalDate ref = LocalDate.of(2000, 1, 1);
        Instant refStartOfDay = ref.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant refEndOfDay = ref.atTime(23, 0).toInstant(ZoneOffset.UTC);

        Instant filterStart = ref.atTime(7, 0).toInstant(ZoneOffset.UTC);
        Instant filterEnd = ref.atTime(8, 0).toInstant(ZoneOffset.UTC);

        List<Entry> entriesNotMatching = new ArrayList<>();
        List<Entry> entriesMatching = new ArrayList<>();

        // completely out
        entriesMatching.add(new Entry(null, "M: Start / end at start of day", refStartOfDay, refStartOfDay, false, true, null, null));
        entriesMatching.add(new Entry(null, "M: Start / end at end of day", refEndOfDay, refEndOfDay, false, true, null, null));

        // matching only with exclusive start filter time so not matching at all
        entriesMatching.add(new Entry(null, "M: Start of day to filter start", refStartOfDay, filterStart, false, true, null, null));

        // matching only with exclusive end filter time so not matching at all
        entriesMatching.add(new Entry(null, "M: Filter end to end of day", filterEnd, refEndOfDay, false, true, null, null));

        // 0 timespan - matching only with exclusive start filter time so not matching at all
        entriesMatching.add(new Entry(null, "M: Filter start to filter start", filterStart, filterStart, false, true, null, null));

        // 0 timespan - matching only with exclusive end filter time so not matching at all
        entriesMatching.add(new Entry(null, "M: Filter end to filter end", filterEnd, filterEnd, false, true, null, null));

        // crossing filter timespan (match by 1 nanosecond)
        entriesMatching.add(new Entry(null, "M: Start of day to filter start + 1ns", refStartOfDay, filterStart.plusNanos(1), false, true, null, null));
        entriesMatching.add(new Entry(null, "M: Filter end - 1ns to end of day", filterEnd.minusNanos(1), refEndOfDay, false, true, null, null));

        // matches filter timespan completely
        entriesMatching.add(new Entry(null, "M: Filter start to filter end", filterStart, filterEnd, false, true, null, null));

        // inner filter period match
        entriesMatching.add(new Entry(null, "M: Filter start + 1ns to filter end", filterStart.plusNanos(1), filterEnd, false, true, null, null));
        entriesMatching.add(new Entry(null, "M: Filter start to filter end - 1ns", filterStart, filterEnd.minusNanos(1), false, true, null, null));
        entriesMatching.add(new Entry(null, "M: Inside of filter timespan", filterStart.plus(29, ChronoUnit.MINUTES), filterStart.plus(31, ChronoUnit.MINUTES), false, true, null, null));

        FullCalendar calendar = new FullCalendar();
        entriesNotMatching.forEach(calendar::addEntry); // should be empty
        entriesMatching.forEach(calendar::addEntry);

        List<Entry> entriesFound = calendar.getEntries((Instant) null, null);
        List<Entry> allEntries = calendar.getEntries();

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
    void testGetEntriesByDate() {
        FullCalendar calendar = new FullCalendar();
        assertNPE(calendar, c -> c.getEntries((Instant) null));

        LocalDateTime ref = LocalDate.of(2000, 1, 1).atStartOfDay();
        LocalDateTime filterEnd = ref.plusDays(1);

        List<Entry> entriesNotMatching = new ArrayList<>();
        List<Entry> entriesMatching = new ArrayList<>();

        // TODO additional / other / better test cases?

        // completely out
        entriesNotMatching.add(new Entry(null, "NM: Last year", ref.minus(1, ChronoUnit.YEARS), ref.minus(1, ChronoUnit.YEARS), false, true, null, null));

        // matching only with exclusive start filter time so not matching at all
        entriesNotMatching.add(new Entry(null, "NM: Last year to filter start", ref.minus(1, ChronoUnit.YEARS), ref, false, true, null, null));

        // matching only with exclusive end filter time so not matching at all
        entriesNotMatching.add(new Entry(null, "NM: Filter end to end of month", filterEnd, ref.with(ChronoField.DAY_OF_MONTH, 31), false, true, null, null));

        // 0 timespan - matching only with exclusive end filter time so not matching at all
        entriesNotMatching.add(new Entry(null, "NM: Filter end to filter end", filterEnd, filterEnd, false, true, null, null));

        // crossing filter timespan (match by 1 nanosecond)
        entriesMatching.add(new Entry(null, "M: Last year to filter start + 1ns", ref.minus(1, ChronoUnit.YEARS), ref.plusNanos(1), false, true, null, null));
        entriesMatching.add(new Entry(null, "M: Filter end - 1ns to end of month", filterEnd.minusNanos(1), ref.with(ChronoField.DAY_OF_MONTH, 31), false, true, null, null));

        // matches filter timespan completely
        entriesMatching.add(new Entry(null, "M: Filter start to filter end", ref, filterEnd, false, true, null, null));

        // inner filter period match
        entriesMatching.add(new Entry(null, "M: Filter start + 1ns to filter end", ref.plusNanos(1), filterEnd, false, true, null, null));
        entriesMatching.add(new Entry(null, "M: Filter start to filter end - 1ns", ref, filterEnd.minusNanos(1), false, true, null, null));
        entriesMatching.add(new Entry(null, "M: Inside of filter timespan", ref.plus(29, ChronoUnit.MINUTES), filterEnd.plus(31, ChronoUnit.MINUTES), false, true, null, null));

        entriesNotMatching.forEach(calendar::addEntry);
        entriesMatching.forEach(calendar::addEntry);


        List<Entry> entriesFound = new ArrayList<>(calendar.getEntries(ref.toInstant(ZoneOffset.UTC)));

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

        assertNPE(calendar, c -> c.getOption((Option) null));
        assertNPE(calendar, c -> c.setOption((Option) null, null));
        assertNPE(calendar, c -> c.setOption((Option) null, "someValue"));

        calendar.setOption(Option.LOCALE, "someValue");
        Assertions.assertTrue(calendar.getOption(Option.LOCALE).isPresent());

        calendar.setOption(Option.LOCALE, null);
        Assertions.assertFalse(calendar.getOption(Option.LOCALE).isPresent());
    }

    @Test
    void testGetAndSetOptionWithStringKeys() {
        FullCalendar calendar = new FullCalendar();

        assertNPE(calendar, c -> c.getOption((String) null));
        assertNPE(calendar, c -> c.setOption((String) null, null));
        assertNPE(calendar, c -> c.setOption((String) null, "someValue"));

        String optionKey = Option.LOCALE.getOptionKey();

        calendar.setOption(optionKey, "someValue");
        Assertions.assertTrue(calendar.getOption(optionKey).isPresent());

        calendar.setOption(optionKey, null);
        Assertions.assertFalse(calendar.getOption(optionKey).isPresent());
    }

    @Test
    void testGetAndSetOptionWithServerSideValues() {
        FullCalendar calendar = new FullCalendar();

        Locale locale = Locale.getDefault();

        calendar.setOption(Option.LOCALE, "someValue", locale);
        assertOptionalEquals(locale, calendar.getOption(Option.LOCALE));
        assertOptionalEquals("someValue", calendar.getOption(Option.LOCALE, true));

        calendar.setOption(Option.LOCALE, "someValue", null);
        assertOptionalEquals("someValue", calendar.getOption(Option.LOCALE));

        calendar.setOption(Option.LOCALE, "someOtherValue", locale);
        calendar.setOption(Option.LOCALE, "someOtherValue");
        assertOptionalEquals("someOtherValue", calendar.getOption(Option.LOCALE));
    }

    @Test
    void testGetAndSetOptionWithServerSideValuesWithStringKeys() {
        FullCalendar calendar = new FullCalendar();

        Locale locale = Locale.getDefault();
        String option = Option.LOCALE.getOptionKey();

        calendar.setOption(option, "someValue", locale);
        assertOptionalEquals(locale, calendar.getOption(option));
        assertOptionalEquals("someValue", calendar.getOption(option, true));

        calendar.setOption(option, "someValue", null);
        assertOptionalEquals("someValue", calendar.getOption(option));

        calendar.setOption(option, "someOtherValue", locale);
        calendar.setOption(option, "someOtherValue");
        assertOptionalEquals("someOtherValue", calendar.getOption(option));
    }

    @Test
    void testEntryClickedEvent() throws Exception {
        FullCalendar calendar = new FullCalendar();

        LocalDateTime refDate = LocalDate.of(2000, 1, 1).atStartOfDay();
        Instant refDateAsDateTime = refDate.toInstant(ZoneOffset.UTC);
        Instant refDateTime = refDate.withHour(7).toInstant(ZoneOffset.UTC);

        // check all day and time entries
        Entry allDayEntry = new Entry("allDay", "title", refDateAsDateTime, refDateAsDateTime.plus(1, ChronoUnit.DAYS), true, true, "color", null);
        Entry timedEntry = new Entry("timed", "title", refDateTime, refDateTime.plus(1, ChronoUnit.HOURS), false, true, "color", null);
        calendar.addEntry(allDayEntry);
        calendar.addEntry(timedEntry);

        Assertions.assertSame(allDayEntry, new EntryClickedEvent(calendar, true, allDayEntry.getId()).getEntry());
        Assertions.assertSame(timedEntry, new EntryClickedEvent(calendar, true, timedEntry.getId()).getEntry());
    }

    @Test
    void testLimitedEntriesClickedEvent() throws Exception {
        FullCalendar calendar = new FullCalendar();

        LocalDate refDate = LocalDate.of(2000, 1, 1);

        Assertions.assertEquals(refDate, new LimitedEntriesClickedEvent(calendar, true, refDate.toString()).getClickedDate());
    }

    @Test
    void testDateTimeEventSubClasses() throws Exception {
        subTestDateTimeEventSubClass(TimeslotClickedEvent.class);
        subTestDateEventSubClass(DayNumberClickedEvent.class);
        subTestDateEventSubClass(WeekNumberClickedEvent.class);
    }


    @Test
    void testTimeslotsSelectedEvent() throws Exception {
        FullCalendar calendar = new FullCalendar();

        LocalDate refDateStart = LocalDate.of(2000, 1, 1);
        LocalDate refDateEnd = LocalDate.of(2000, 1, 2);

        LocalDateTime refDateTimeStart = LocalDateTime.of(2000, 1, 1, 7, 0);
        LocalDateTime refDateTimeEnd = LocalDateTime.of(2000, 1, 1, 8, 0);

        TimeslotsSelectedEvent event;
        event = new TimeslotsSelectedEvent(calendar, true, refDateStart.toString(), refDateEnd.toString(), true);
        Assertions.assertEquals(refDateStart.atStartOfDay(), event.getStartDateTime());
        Assertions.assertEquals(refDateEnd.atStartOfDay(), event.getEndDateTime());
        Assertions.assertTrue(event.isAllDay());

        event = new TimeslotsSelectedEvent(calendar, true, refDateTimeStart.toString(), refDateTimeEnd.toString(), false);
        Assertions.assertEquals(refDateTimeStart, event.getStartDateTime());
        Assertions.assertEquals(refDateTimeEnd, event.getEndDateTime());
        Assertions.assertFalse(event.isAllDay());
    }

    @Test
    void testTimeChangedEventSubClass() throws Exception {
        subTestEntryTimeChangedEventSubClass(EntryDroppedEvent.class);
        subTestEntryTimeChangedEventSubClass(EntryResizedEvent.class);
    }

    private <T extends DateEvent> void subTestDateEventSubClass(Class<T> eventClass) throws Exception {
        FullCalendar calendar = new FullCalendar();

        LocalDate refDate = LocalDate.of(2000, 1, 1);

        T event;
        Constructor<T> constructor = ComponentEventBusUtil.getEventConstructor(eventClass);
        event = constructor.newInstance(calendar, true, refDate.toString());
        Assertions.assertEquals(refDate, event.getDate());
    }

    private <T extends DateTimeEvent> void subTestDateTimeEventSubClass(Class<T> eventClass) throws Exception {
        FullCalendar calendar = new FullCalendar();

        LocalDate refDate = LocalDate.of(2000, 1, 1);
        LocalDateTime refDateTime = LocalDate.of(2000, 1, 1).atStartOfDay();

        T event;
        Constructor<T> constructor = ComponentEventBusUtil.getEventConstructor(eventClass);
        event = constructor.newInstance(calendar, true, refDate.toString(), true);
        Assertions.assertEquals(refDate.atStartOfDay(), event.getDateTime());
        Assertions.assertTrue(event.isAllDay());

        event = constructor.newInstance(calendar, true, refDateTime.toString(), false);
        Assertions.assertEquals(refDateTime, event.getDateTime());
        Assertions.assertFalse(event.isAllDay());

    }

    private <T extends EntryTimeChangedEvent> void subTestEntryTimeChangedEventSubClass(Class<T> eventClass) throws Exception {
        FullCalendar calendar = new FullCalendar();

        Instant refDate = LocalDate.of(2000, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant refDateTime = LocalDate.of(2000, 1, 1).atStartOfDay().withHour(7).toInstant(ZoneOffset.UTC);

        // check all day and time entries
        Entry allDayEntry = new Entry("allDay", "title", refDate, refDate.plus(1, ChronoUnit.DAYS), true, true, "color", null);
        Entry timedEntry = new Entry("timed", "title", refDateTime, refDateTime.plus(1, ChronoUnit.HOURS), false, true, "color", null);
        calendar.addEntry(allDayEntry);
        calendar.addEntry(timedEntry);

        // the original entry will be modified by the event. we test if the modified original event matches the json source
        Delta delta = new Delta(1, 1, 1, 1, 1, 1);
        JsonObject jsonDelta = Json.createObject();
        jsonDelta.put("years", 1);
        jsonDelta.put("months", 1);
        jsonDelta.put("days", 1);
        jsonDelta.put("hours", 1);
        jsonDelta.put("minutes", 1);
        jsonDelta.put("seconds", 1);

        Entry modifiedAllDayEntry = new Entry(allDayEntry.getId(), allDayEntry.getTitle() + 1, delta.applyOn(allDayEntry.getStartUTC()), delta.applyOn(allDayEntry.getEndUTC()), allDayEntry.isAllDay(), !allDayEntry.isEditable(), allDayEntry.getColor() + 1, allDayEntry.getDescription());
        Entry modifiedTimedEntry = new Entry(timedEntry.getId(), timedEntry.getTitle() + 1, delta.applyOn(timedEntry.getStartUTC()), delta.applyOn(timedEntry.getEndUTC()), timedEntry.isAllDay(), !timedEntry.isEditable(), timedEntry.getColor() + 1, timedEntry.getDescription());
        JsonObject jsonModifiedAllDayEntry = modifiedAllDayEntry.toJson();
        JsonObject jsonModifiedTimedEntry = modifiedTimedEntry.toJson();

        Constructor<T> constructor = ComponentEventBusUtil.getEventConstructor(eventClass);

        /*
            Day event
         */
        T event = constructor.newInstance(calendar, true, jsonModifiedAllDayEntry, jsonDelta);
        Assertions.assertEquals(delta, event.getDelta());

        // not changed automatically
        EntryTest.assertFullEqualsByJsonAttributes(allDayEntry, event.getEntry());

        // apply changes and test modifications
        event.applyChangesOnEntry();
        EntryTest.assertFullEqualsByJsonAttributes(modifiedAllDayEntry, event.getEntry());

        /*
            Time slot event
         */
        event = constructor.newInstance(calendar, true, jsonModifiedTimedEntry, jsonDelta);

        Assertions.assertEquals(delta, event.getDelta());

        // not changed automatically
        EntryTest.assertFullEqualsByJsonAttributes(timedEntry, event.getEntry());

        // apply changes and test modifications
        event.applyChangesOnEntry();
        EntryTest.assertFullEqualsByJsonAttributes(modifiedTimedEntry, event.getEntry());
    }

    @Test
    void testAddEntriesArray() {
        FullCalendar calendar = new FullCalendar();
        assertNPE(calendar, c -> c.addEntries((Entry[]) null));

        Entry entry1 = new Entry();
        Entry entry2 = new Entry();
        Entry entry3 = new Entry();

        calendar.addEntries(entry1, entry2, entry3, entry3);

        Collection<Entry> entries = calendar.getEntries();
        Assertions.assertEquals(3, entries.size());

        Assertions.assertTrue(entries.contains(entry1));
        Assertions.assertTrue(entries.contains(entry2));
        Assertions.assertTrue(entries.contains(entry3));

        assertOptionalEquals(entry1, calendar.getEntryById(entry1.getId()));
        assertOptionalEquals(entry2, calendar.getEntryById(entry2.getId()));
        assertOptionalEquals(entry3, calendar.getEntryById(entry3.getId()));
    }

    @Test
    void testAddEntriesIterable() {
        FullCalendar calendar = new FullCalendar();
        assertNPE(calendar, c -> c.addEntries((Iterable<Entry>) null));

        Entry entry1 = new Entry();
        Entry entry2 = new Entry();
        Entry entry3 = new Entry();

        calendar.addEntries(Arrays.asList(entry1, entry2, entry3, entry3));

        Collection<Entry> entries = calendar.getEntries();
        Assertions.assertEquals(3, entries.size());

        Assertions.assertTrue(entries.contains(entry1));
        Assertions.assertTrue(entries.contains(entry2));
        Assertions.assertTrue(entries.contains(entry3));

        assertOptionalEquals(entry1, calendar.getEntryById(entry1.getId()));
        assertOptionalEquals(entry2, calendar.getEntryById(entry2.getId()));
        assertOptionalEquals(entry3, calendar.getEntryById(entry3.getId()));
    }

    @Test
    void testUpdateEntries() {
        // checks only for exceptions

        FullCalendar calendar = new FullCalendar();
        assertNPE(calendar, c -> c.updateEntries((Entry[]) null));
        assertNPE(calendar, c -> c.updateEntries((Iterable<Entry>) null));


        Entry entry1 = new Entry();
        Entry entry2 = new Entry();
        Entry entry3 = new Entry();

        calendar.addEntries(entry1, entry2, entry3, entry3);
        calendar.updateEntries(entry1, entry2, entry3, entry3);
        calendar.updateEntries(Arrays.asList(entry1, entry2, entry3, entry3));
    }

    @Test
    void testRemoveEntriesArray() {
        FullCalendar calendar = new FullCalendar();

        assertNPE(calendar, c -> c.removeEntries((Entry[]) null));

        Entry entry1 = new Entry();
        Entry entry2 = new Entry();
        Entry entry3 = new Entry();

        calendar.addEntries(entry1, entry2, entry3);
        calendar.removeEntries(entry1, entry2);

        Collection<Entry> entries = calendar.getEntries();
        Assertions.assertEquals(1, entries.size());

        Assertions.assertFalse(entries.contains(entry1));
        Assertions.assertFalse(entries.contains(entry2));
        Assertions.assertTrue(entries.contains(entry3));
    }

    @Test
    void testRemoveEntriesIterable() {
        FullCalendar calendar = new FullCalendar();

        assertNPE(calendar, c -> c.removeEntries((Iterable<Entry>) null));

        Entry entry1 = new Entry();
        Entry entry2 = new Entry();
        Entry entry3 = new Entry();

        calendar.addEntries(entry1, entry2, entry3);
        calendar.removeEntries(Arrays.asList(entry1, entry2));

        Collection<Entry> entries = calendar.getEntries();
        Assertions.assertEquals(1, entries.size());

        Assertions.assertFalse(entries.contains(entry1));
        Assertions.assertFalse(entries.contains(entry2));
        Assertions.assertTrue(entries.contains(entry3));
    }
}
