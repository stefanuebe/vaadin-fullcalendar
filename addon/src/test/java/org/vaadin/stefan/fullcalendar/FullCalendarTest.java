package org.vaadin.stefan.fullcalendar;

import com.vaadin.flow.component.ComponentEventBusUtil;
import com.vaadin.flow.dom.Element;
import elemental.json.Json;
import elemental.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.vaadin.stefan.fullcalendar.FullCalendar.Option;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.time.*;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

@SuppressWarnings("ALL")
public class FullCalendarTest {

    // TODO create "client side" times not based on LocalDate but ZoneDate or similar to have different timezones

    public static final String FULL_CALENDAR_HTML = "fullcalendar/full-calendar.html";

    @BeforeAll
    static void beforeAll() {
//        TestUtils.initVaadinService(FULL_CALENDAR_HTML);
    }

    private FullCalendar createTestCalendar() {
        return setupTestCalendar(new FullCalendar());
    }

    private FullCalendar createTestCalendar(int entries) {
        return setupTestCalendar(new FullCalendar(entries));
    }

    private FullCalendar createTestCalendar(JsonObject options) {
        return setupTestCalendar(new FullCalendar(options));
    }

    private FullCalendar setupTestCalendar(FullCalendar calendar) {
        // to simulate a client timezone, we have to use the server time zone, since all the LocalDate... instances
        // will not be on utc, but on the server timezone.
        calendar.setTimezoneClient(Timezone.getSystem());
        return calendar;
    }

    @Test
    void testNonArgsConstructor() {
        FullCalendar calendar = new FullCalendar();

        // this shall assure that all init options are handled
        assertExistingOptionCount(calendar, 1);
        Assertions.assertSame(CalendarLocale.getDefault(), calendar.getLocale());
    }

    @Test
    void testArgsConstructor_dayMaxEvents() {
        int entryLimit = 5;

        FullCalendar calendar = new FullCalendar(entryLimit);

        // this shall assure that all init options are handled
        assertExistingOptionCount(calendar, 1);
        Assertions.assertSame(CalendarLocale.getDefault(), calendar.getLocale());

        Assertions.assertEquals(entryLimit, calendar.getElement().getProperty("dayMaxEvents", -1));
    }

    @Test
    void testArgsConstructor_initialOptions() throws ExecutionException, InterruptedException, TimeoutException {
        JsonObject options = Json.createObject();

        FullCalendar calendar = new FullCalendar(options);
        Element element = calendar.getElement();

        // this shall assure that all init options are handled
        assertExistingOptionCount(calendar, 0);
        Serializable returnedOptions = element.getPropertyRaw("initialOptions");

        Assertions.assertTrue(returnedOptions instanceof JsonObject, "Returned initial options not instanceof JsonObject");

        // TODO integrate Testbench test

    }

    private void assertExistingOptionCount(FullCalendar calendar, int expectedOptionsCount) {
        Assertions.assertEquals(expectedOptionsCount, Arrays.stream(Option.values()).map(calendar::getOption).filter(Optional::isPresent).count());
    }

    @Test
    void testClientSideMethods() {
        FullCalendar calendar = createTestCalendar();

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
        FullCalendar calendar = createTestCalendar();

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
        FullCalendar calendar = createTestCalendar();

        Optional<Entry> optional = calendar.getEntryById("");
        Assertions.assertNotNull(optional);
        Assertions.assertFalse(optional.isPresent());
    }

    @Test
    void testFetchingExistingEntryById() {
        FullCalendar calendar = createTestCalendar();

        Entry entry = new Entry();
        calendar.addEntry(entry);

        Optional<Entry> optional = calendar.getEntryById(entry.getId());
        Assertions.assertNotNull(optional);
        assertOptionalEquals(entry, optional);
    }

    @Test
    void testAddEntry() {
        FullCalendar calendar = createTestCalendar();

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
        FullCalendar calendar = createTestCalendar();

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
        FullCalendar calendar = createTestCalendar();

        Collection<Entry> entries = calendar.getEntries();
        Assertions.assertNotNull(entries);
        Assertions.assertEquals(0, entries.size());
    }

    @Test
    void testEntriesInstanceAreSameAfterUpdate() {
        FullCalendar calendar = createTestCalendar();

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
    
    private Entry createEntry(String id, String title, Instant start, Instant end, boolean allDay, boolean editable, String color, String description) {
    	Entry entry = new Entry(id);
    	
    	entry.setTitle(title);
        entry.setStart(start);
        entry.setEnd(end);
        entry.setAllDay(allDay);
        entry.setEditable(editable);
        entry.setColor(color);
        entry.setDescription(description);
        
        return entry;
    }
    
    private Entry createEntry(String id, String title, LocalDateTime start, LocalDateTime end, boolean allDay, boolean editable, String color, String description) {
    	Entry entry = new Entry(id);
    	
    	entry.setTitle(title);
        entry.setStart(start);
        entry.setEnd(end);
        entry.setAllDay(allDay);
        entry.setEditable(editable);
        entry.setColor(color);
        entry.setDescription(description);
        
        return entry;
    }

    @Test
    void testGetEntriesByClosedDateTimeInterval() {
        FullCalendar calendar = createTestCalendar();
        Timezone timezoneClient = calendar.getTimezoneClient();

        LocalDate ref = LocalDate.of(2000, 1, 1);
        Instant refStartOfDay = timezoneClient.convertToUTC(ref.atStartOfDay());
        Instant refEndOfDay = timezoneClient.convertToUTC(ref.atTime(23, 0));

        Instant filterStart = timezoneClient.convertToUTC(ref.atTime(7, 0));
        Instant filterEnd = timezoneClient.convertToUTC(ref.atTime(8, 0));

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
        FullCalendar calendar = createTestCalendar();
        Timezone timezoneClient = calendar.getTimezoneClient();
        Timezone timezoneServer = calendar.getTimezoneServer();

        LocalDate ref = LocalDate.of(2000, 1, 1);
        Instant refStartOfDay = timezoneClient.convertToUTC(ref.atStartOfDay());
        Instant refEndOfDay = timezoneClient.convertToUTC(ref.atTime(23, 0));

        Instant filterStart = timezoneClient.convertToUTC(ref.atTime(7, 0));
        Instant filterEnd = timezoneClient.convertToUTC(ref.atTime(8, 0));

        List<Entry> entriesNotMatching = new ArrayList<>();
        List<Entry> entriesMatching = new ArrayList<>();

        // completely out
        entriesNotMatching.add(createEntry(null, "NM: Start / end at end of day", refEndOfDay, refEndOfDay, false, true, null, null));

        // matching only with exclusive end filter time so not matching at all
        entriesNotMatching.add(createEntry(null, "NM: Filter end to end of day", filterEnd, refEndOfDay, false, true, null, null));

        // 0 timespan - matching only with exclusive end filter time so not matching at all
        entriesNotMatching.add(createEntry(null, "NM: Filter end to filter end", filterEnd, filterEnd, false, true, null, null));

        // these three are now matching since open filter start (@see testGetEntriesByDateTimeInterval())
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
        FullCalendar calendar = createTestCalendar();
        Timezone timezoneClient = calendar.getTimezoneClient();
        Timezone timezoneServer = calendar.getTimezoneServer();

        LocalDate ref = LocalDate.of(2000, 1, 1);
        Instant refStartOfDay = timezoneClient.convertToUTC(ref.atStartOfDay());
        Instant refEndOfDay = timezoneClient.convertToUTC(ref.atTime(23, 0));

        Instant filterStart = timezoneClient.convertToUTC(ref.atTime(7, 0));
        Instant filterEnd = timezoneClient.convertToUTC(ref.atTime(8, 0));

        List<Entry> entriesNotMatching = new ArrayList<>();
        List<Entry> entriesMatching = new ArrayList<>();

        // completely out
        entriesNotMatching.add(createEntry(null, "NM: Start / end at start of day", refStartOfDay, refStartOfDay, false, true, null, null));

        // matching only with exclusive start filter time so not matching at all
        entriesNotMatching.add(createEntry(null, "NM: Start of day to filter start", refStartOfDay, filterStart, false, true, null, null));


        // 0 timespan - matching only with exclusive start filter time so not matching at all
        entriesNotMatching.add(createEntry(null, "NM: Filter start to filter start", filterStart, filterStart, false, true, null, null));

        // these three are now matching since open filter start (@see testGetEntriesByDateTimeInterval())
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
        FullCalendar calendar = createTestCalendar();
        Timezone timezoneClient = calendar.getTimezoneClient();
        Timezone timezoneServer = calendar.getTimezoneServer();

        LocalDate ref = LocalDate.of(2000, 1, 1);
        Instant refStartOfDay = timezoneClient.convertToUTC(ref.atStartOfDay());
        Instant refEndOfDay = timezoneClient.convertToUTC(ref.atTime(23, 0));

        Instant filterStart = timezoneClient.convertToUTC(ref.atTime(7, 0));
        Instant filterEnd = timezoneClient.convertToUTC(ref.atTime(8, 0));

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
        FullCalendar calendar = createTestCalendar();
        assertNPE(calendar, c -> c.getEntries((Instant) null));

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

        calendar.addEntries(entriesNotMatching);
        calendar.addEntries(entriesMatching);

        List<Entry> entriesFound = new ArrayList<>(calendar.getEntries(ref));

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
        FullCalendar calendar = createTestCalendar();
        calendar.addEntry(new Entry());
        calendar.addEntry(new Entry());
        calendar.addEntry(new Entry());

        Assertions.assertEquals(3, calendar.getEntries().size());

        calendar.removeAllEntries();
        Assertions.assertEquals(0, calendar.getEntries().size());
    }

    @Test
    void testGetEntriesReturnListCopy() {
        FullCalendar calendar = createTestCalendar();
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
        FullCalendar calendar = createTestCalendar();

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
        FullCalendar calendar = createTestCalendar();

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
        FullCalendar calendar = createTestCalendar();

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
        FullCalendar calendar = createTestCalendar();

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
        FullCalendar calendar = createTestCalendar();
        Timezone timezoneClient = calendar.getTimezoneClient();

        LocalDateTime refDate = LocalDate.of(2000, 1, 1).atStartOfDay();
        Instant refDateAsDateTime = timezoneClient.convertToUTC(refDate);
        Instant refDateTime = timezoneClient.convertToUTC(refDate.withHour(7));

        // check all day and time entries
        Entry allDayEntry = createEntry("allDay", "title", refDateAsDateTime, refDateAsDateTime.plus(1, ChronoUnit.DAYS), true, true, "color", null);
        Entry timedEntry = createEntry("timed", "title", refDateTime, refDateTime.plus(1, ChronoUnit.HOURS), false, true, "color", null);
        calendar.addEntry(allDayEntry);
        calendar.addEntry(timedEntry);

        JsonObject jsonData = Json.createObject();
        jsonData.put("id", allDayEntry.getId());
        Assertions.assertSame(allDayEntry, new EntryClickedEvent(calendar, true, jsonData).getEntry());

        jsonData = Json.createObject();
        jsonData.put("id", timedEntry.getId());
        Assertions.assertSame(timedEntry, new EntryClickedEvent(calendar, true, jsonData).getEntry());
    }

    @Test
    void testDateTimeEventSubClasses() throws Exception {
        subTestDateTimeEventSubClass(TimeslotClickedEvent.class);
        subTestDateEventSubClass(DayNumberClickedEvent.class);
        subTestDateEventSubClass(WeekNumberClickedEvent.class);
    }


    @Test
    void testTimeslotsSelectedEvent() throws Exception {
        FullCalendar calendar = createTestCalendar();

        Timezone timezoneClient = calendar.getTimezoneClient();
        Timezone timezoneServer = calendar.getTimezoneServer();

        // client timezone may differ server timezone, so we have to simulate that for the event creation
        Instant refDateStart = timezoneClient.convertToUTC(LocalDate.of(2000, 1, 1));
        Instant refDateEnd = timezoneClient.convertToUTC(LocalDate.of(2000, 1, 2));
        Instant refDateTimeStart = timezoneClient.convertToUTC(LocalDateTime.of(2000, 1, 1, 7, 0));
        Instant refDateTimeEnd = timezoneClient.convertToUTC(LocalDateTime.of(2000, 1, 1, 8, 0));

        TimeslotsSelectedEvent event;
        // now we simulate the conversion to the server side timezone
        event = new TimeslotsSelectedEvent(calendar, true, refDateStart.toString(), refDateEnd.toString(), true);
        Assertions.assertEquals(timezoneServer.convertToLocalDate(refDateStart).atStartOfDay(), event.getStartDateTime());
        Assertions.assertEquals(timezoneServer.convertToLocalDate(refDateEnd).atStartOfDay(), event.getEndDateTime());
        Assertions.assertTrue(event.isAllDay());

        event = new TimeslotsSelectedEvent(calendar, true, refDateTimeStart.toString(), refDateTimeEnd.toString(), false);
        Assertions.assertEquals(timezoneServer.convertToLocalDateTime(refDateTimeStart), event.getStartDateTime());
        Assertions.assertEquals(timezoneServer.convertToLocalDateTime(refDateTimeEnd), event.getEndDateTime());
        Assertions.assertFalse(event.isAllDay());
    }

    @Test
    void testTimeChangedEventSubClass() throws Exception {
        subTestEntryTimeChangedEventSubClass(EntryDroppedEvent.class);
        subTestEntryTimeChangedEventSubClass(EntryResizedEvent.class);
    }

    private <T extends DateEvent> void subTestDateEventSubClass(Class<T> eventClass) throws Exception {
        FullCalendar calendar = createTestCalendar();
        Timezone timezoneClient = calendar.getTimezoneClient();
        Timezone timezoneServer = calendar.getTimezoneServer();

        Instant refDate = timezoneClient.convertToUTC(LocalDate.of(2000, 1, 1));

        T event;
        Constructor<T> constructor = ComponentEventBusUtil.getEventConstructor(eventClass);
        event = constructor.newInstance(calendar, true, refDate.toString());
        Assertions.assertEquals(timezoneServer.convertToLocalDate(refDate), event.getDate());
    }

    private <T extends DateTimeEvent> void subTestDateTimeEventSubClass(Class<T> eventClass) throws Exception {
        FullCalendar calendar = createTestCalendar();
        Timezone timezoneClient = calendar.getTimezoneClient();
        Timezone timezoneServer = calendar.getTimezoneServer();

        Instant refDate = timezoneClient.convertToUTC(LocalDate.of(2000, 1, 1));
        Instant refDateTime = timezoneClient.convertToUTC(LocalDate.of(2000, 1, 1).atStartOfDay());

        T event;
        Constructor<T> constructor = ComponentEventBusUtil.getEventConstructor(eventClass);
        event = constructor.newInstance(calendar, true, refDate.toString(), true);
        Assertions.assertEquals(timezoneServer.convertToLocalDate(refDate).atStartOfDay(), event.getDateTime());
        Assertions.assertTrue(event.isAllDay());

        event = constructor.newInstance(calendar, true, refDateTime.toString(), false);
        Assertions.assertEquals(timezoneServer.convertToLocalDateTime(refDateTime), event.getDateTime());
        Assertions.assertFalse(event.isAllDay());

    }

    private <T extends EntryTimeChangedEvent> void subTestEntryTimeChangedEventSubClass(Class<T> eventClass) throws Exception {
        FullCalendar calendar = createTestCalendar();
        Timezone timezoneClient = calendar.getTimezoneClient();
        Timezone timezoneServer = calendar.getTimezoneServer();

        Instant refDate = timezoneClient.convertToUTC(LocalDate.of(2000, 1, 1).atStartOfDay());
        Instant refDateTime = timezoneClient.convertToUTC(LocalDate.of(2000, 1, 1).atStartOfDay().withHour(7));

        // check all day and time entries
        Entry allDayEntry = createEntry("allDay", "title", refDate, refDate.plus(1, ChronoUnit.DAYS), true, true, "color", null);
        Entry timedEntry = createEntry("timed", "title", refDateTime, refDateTime.plus(1, ChronoUnit.HOURS), false, true, "color", null);
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

        Entry modifiedAllDayEntry = createEntry(allDayEntry.getId(), allDayEntry.getTitle() + 1, delta.applyOn(allDayEntry.getStartUTC()), delta.applyOn(allDayEntry.getEndUTC()), allDayEntry.isAllDay(), !allDayEntry.isEditable(), allDayEntry.getColor() + 1, allDayEntry.getDescription());
        Entry modifiedTimedEntry = createEntry(timedEntry.getId(), timedEntry.getTitle() + 1, delta.applyOn(timedEntry.getStartUTC()), delta.applyOn(timedEntry.getEndUTC()), timedEntry.isAllDay(), !timedEntry.isEditable(), timedEntry.getColor() + 1, timedEntry.getDescription());
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
        FullCalendar calendar = createTestCalendar();
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
        FullCalendar calendar = createTestCalendar();
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

        FullCalendar calendar = createTestCalendar();
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
        FullCalendar calendar = createTestCalendar();

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
        FullCalendar calendar = createTestCalendar();

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